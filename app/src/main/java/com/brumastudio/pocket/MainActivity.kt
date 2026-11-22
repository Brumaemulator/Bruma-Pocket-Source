// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket
import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import com.linkcore.emulator.*
import java.util.concurrent.Executors
class MainActivity:Activity(){
 private var themeAtCreate=false
 override fun attachBaseContext(base:Context){super.attachBaseContext(PocketLocale.attach(base))}
 private lateinit var store:LibraryStore;private var games=listOf<LibraryGame>();private var filter="GBA";private var query="";private var favorites=false
 private val io=Executors.newSingleThreadExecutor();private var scanTask:java.util.concurrent.Future<*>?=null;private var scanCanceled=java.util.concurrent.atomic.AtomicBoolean(false);private var scanGeneration=0;private var closed=false;private var scanning=false
 private lateinit var grid:GridView;private lateinit var count:TextView;private lateinit var adapter:BaseAdapter
 override fun onCreate(b:Bundle?){PocketAppearance.apply(this);themeAtCreate=PocketAppearance.dark(this);super.onCreate(b);store=LibraryStore(this);RomAccess.sweep(this);games=try{store.read()}catch(e:Exception){emptyList()};draw()}
 override fun onResume(){super.onResume();if(themeAtCreate!=PocketAppearance.dark(this)){recreate();return};if(languageAtDraw!=PocketLocale.selectedTag(this)){recreate();return};if(::adapter.isInitialized){games=try{store.read()}catch(e:Exception){games};draw()}}
 private fun shown()=games.filter{(favorites||it.system==filter)&&(!favorites||store.favorite(it.key))&&it.title.contains(query,true)}
 private fun draw(){languageAtDraw=PocketLocale.selectedTag(this);val root=Ui.root(this);val head=Ui.row(this)
  val brand=Ui.text(this,"Bruma Pocket",26).apply{
   typeface=android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.NORMAL)
   val title=android.text.SpannableString("Bruma Pocket");val colors=intArrayOf(0xff3478a7.toInt(),0xffa84d80.toInt(),0xff50894b.toInt(),0xffb77a2b.toInt(),0xff7555aa.toInt(),0xffbf6252.toInt());for(i in 0..5)title.setSpan(android.text.style.ForegroundColorSpan(colors[i]),6+i,7+i,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);text=title
  }
  head.addView(brand,LinearLayout.LayoutParams(0,-2,1f));head.addView(Ui.button(this,"+",false){folder()}.apply{contentDescription=getString(R.string.add_folder)},LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)).apply{marginEnd=Ui.dp(this@MainActivity,7)});head.addView(Ui.button(this,"☰",false){menu()}.apply{contentDescription=getString(R.string.pause)},LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));root.addView(head)
  val recent=games.filter{store.last(it.key)>0}.sortedByDescending{store.last(it.key)}
  if(recent.isNotEmpty()&&resources.configuration.screenHeightDp>=550){
   val g=recent.first();val card=Ui.row(this);Ui.panel(card,Ui.sage);card.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12))
   card.addView(Ui.cover(this,g.key,g.system),LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,60)))
   val copy=Ui.column(this);copy.setPadding(Ui.dp(this,12),0,Ui.dp(this,6),0);copy.addView(Ui.text(this,getString(R.string.continue_playing),11,Ui.accent));copy.addView(Ui.text(this,g.title,16).apply{maxLines=2});card.addView(copy,LinearLayout.LayoutParams(0,-2,1f));card.addView(Ui.button(this,"▶"){Ui.go(this,PlayerActivity::class.java,g.key,true)});root.addView(card);root.addView(Ui.space(this,10))
  }
  val search=EditText(this).apply{hint=getString(R.string.search);setSingleLine();setText(query);textSize=15f;setPadding(Ui.dp(this@MainActivity,15),Ui.dp(this@MainActivity,8),Ui.dp(this@MainActivity,15),Ui.dp(this@MainActivity,8));Ui.panel(this,Ui.card,16);setTextColor(Ui.ink);setHintTextColor(Ui.muted);addTextChangedListener(object:TextWatcher{override fun beforeTextChanged(s:CharSequence?,st:Int,c:Int,a:Int){};override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int){query=s.toString();refresh()};override fun afterTextChanged(e:Editable?) {}})}
  root.addView(search,LinearLayout.LayoutParams(-1,Ui.dp(this,48)));root.addView(Ui.space(this,10))
  val chips=Ui.row(this);for(s in listOf("GBA","GBC","GB")){val label=if(s.isBlank())getString(R.string.all)else s;chips.addView(Ui.button(this,label,!favorites&&filter==s){filter=s;favorites=false;draw()}.apply{setPadding(Ui.dp(this@MainActivity,6),0,Ui.dp(this@MainActivity,6),0)},LinearLayout.LayoutParams(0,Ui.dp(this,42),1f).apply{marginEnd=Ui.dp(this@MainActivity,6)})};chips.addView(Ui.button(this,if(favorites)"★"else"☆",favorites){favorites=!favorites;draw()}.apply{contentDescription=getString(R.string.favorites)},LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,40)));root.addView(chips)
  count=Ui.text(this,getString(R.string.library_count,shown().size),11,Ui.muted);count.setPadding(0,Ui.dp(this,10),0,Ui.dp(this,9));root.addView(count)
  grid=GridView(this).apply{numColumns=if(resources.configuration.screenWidthDp>=700)4 else 2;horizontalSpacing=Ui.dp(this@MainActivity,10);verticalSpacing=Ui.dp(this@MainActivity,12);isVerticalScrollBarEnabled=false;clipToPadding=false;setPadding(0,0,0,Ui.dp(this@MainActivity,8))}
  adapter=object:BaseAdapter(){override fun getCount()=shown().size;override fun getItem(p:Int)=shown()[p];override fun getItemId(p:Int)=p.toLong();override fun getView(p:Int,old:View?,parent:ViewGroup):View{
   val g=getItem(p);val box=Ui.column(this@MainActivity);Ui.panel(box);box.setPadding(Ui.dp(this@MainActivity,9),Ui.dp(this@MainActivity,9),Ui.dp(this@MainActivity,9),Ui.dp(this@MainActivity,12))
   if(resources.configuration.screenHeightDp<550){
    val row=Ui.row(this@MainActivity);row.addView(Ui.cover(this@MainActivity,g.key,g.system),LinearLayout.LayoutParams(Ui.dp(this@MainActivity,48),Ui.dp(this@MainActivity,64)));val copy=Ui.column(this@MainActivity);copy.setPadding(Ui.dp(this@MainActivity,8),0,0,0);copy.addView(Ui.text(this@MainActivity,g.title,13).apply{maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END});copy.addView(Ui.text(this@MainActivity,g.system+(if(store.favorite(g.key))" ★"else""),10,Ui.accent));row.addView(copy,LinearLayout.LayoutParams(0,-2,1f));box.addView(row)
   }else{
    box.addView(Ui.cover(this@MainActivity,g.key,g.system),LinearLayout.LayoutParams(-1,Ui.dp(this@MainActivity,if(resources.configuration.screenWidthDp>=700)180 else 154)))
    box.addView(Ui.space(this@MainActivity,9));box.addView(Ui.text(this@MainActivity,g.title,14).apply{maxLines=2;minLines=2;ellipsize=android.text.TextUtils.TruncateAt.END});box.addView(Ui.text(this@MainActivity,g.system+(if(store.favorite(g.key))"  ★"else""),10,Ui.accent))
   };box.setOnClickListener{gameMenu(g)};return box}}
  grid.adapter=adapter;grid.setOnItemClickListener{_,_,position,_->shown().getOrNull(position)?.let{gameMenu(it)}}
  val frame=FrameLayout(this);frame.addView(grid,FrameLayout.LayoutParams(-1,-1))
  if(games.isNotEmpty()){val empty=Ui.text(this,getString(if(favorites)R.string.no_favorites else R.string.no_results),15,Ui.muted).apply{gravity=Gravity.CENTER};frame.addView(empty,FrameLayout.LayoutParams(-1,-1));grid.emptyView=empty}
  if(games.isEmpty()) {val empty=Ui.column(this);empty.gravity=Gravity.CENTER;empty.addView(PocketArt(this,"GB · GBC · GBA"),LinearLayout.LayoutParams(Ui.dp(this,170),Ui.dp(this,140)));empty.addView(Ui.space(this,12));empty.addView(Ui.text(this,getString(R.string.library_empty),21));empty.addView(Ui.space(this,10));empty.addView(Ui.text(this,getString(R.string.empty_body),14,Ui.muted).apply{gravity=Gravity.CENTER});empty.addView(Ui.space(this,14));empty.addView(Ui.button(this,getString(R.string.add_folder)){folder()});frame.addView(empty,FrameLayout.LayoutParams(-1,-1))}
  root.addView(frame,LinearLayout.LayoutParams(-1,0,1f))
 }
 private fun gameMenu(g:LibraryGame){
  val ids=mutableListOf<Int>();if(SaveManager(this).state(g.key,0).exists())ids+=R.string.resume;ids+=R.string.play;ids+=R.string.more_options
  AlertDialog.Builder(this).setTitle(g.title).setItems(ids.map{getString(it)}.toTypedArray()){_,i->when(ids[i]){R.string.resume->Ui.go(this,PlayerActivity::class.java,g.key,true);R.string.play->Ui.go(this,PlayerActivity::class.java,g.key,false);else->Ui.go(this,DetailActivity::class.java,g.key)}}.setNegativeButton(R.string.cancel,null).show()
 }
 private fun refresh(){if(::adapter.isInitialized){adapter.notifyDataSetChanged();count.text=getString(R.string.library_count,shown().size)}}
 private fun menu(){if(scanning){AlertDialog.Builder(this).setMessage(R.string.working).setPositiveButton(R.string.scan_cancel){_,_->scanCanceled.set(true);scanGeneration++;scanTask?.cancel(true);scanning=false;count.text=getString(R.string.scan_canceled)}.setNegativeButton(R.string.back,null).show();return};val items=intArrayOf(R.string.change_folder,R.string.retry,R.string.cover_all,R.string.language,R.string.settings,R.string.discover);AlertDialog.Builder(this).setItems(items.map{getString(it)}.toTypedArray()){_,i->when(i){0->folder();1->{val u=store.prefs.getString("pending_folder",null)?:store.prefs.getString("folder",null);if(u!=null)scan(Uri.parse(u))else folder()};2->covers();3->PocketLocale.choose(this);4->Ui.go(this,SettingsActivity::class.java);5->AlertDialog.Builder(this).setMessage(R.string.discover_body).setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.get_bruma){_,_->Ui.bruma(this)}.show()}}.show()}
 private var languageAtDraw=""
 private fun folder(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),10)}
 override fun onActivityResult(r:Int,result:Int,data:Intent?){super.onActivityResult(r,result,data);if(r==10&&result==RESULT_OK)data?.data?.let{u->try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);store.prefs.edit().putString("pending_folder",u.toString()).apply();scan(u)}catch(e:Exception){Ui.message(this,getString(R.string.scan_failed))}}}
 private fun scan(u:Uri){
  if(scanning)return
  scanning=true;val generation=++scanGeneration;val canceled=java.util.concurrent.atomic.AtomicBoolean(false);scanCanceled=canceled
  count.text=getString(R.string.scanning,0)
  scanTask=io.submit{try{
   var lastUpdate=0L;val found=store.scan(u,{closed||canceled.get()}){n->val now=android.os.SystemClock.elapsedRealtime();if(now-lastUpdate>200){lastUpdate=now;runOnUiThread{if(!closed&&generation==scanGeneration)count.text=getString(R.string.scanning,n)}}}
   runOnUiThread{if(!closed&&generation==scanGeneration){games=found;scanning=false;draw();if(store.lastScanIssues.isNotEmpty())scanIssues()else autoCovers()}}
  }catch(e:Exception){runOnUiThread{if(generation==scanGeneration){scanning=false;if(!closed&&!canceled.get()){android.util.Log.w("PocketLibrary","Scan failed: "+e.javaClass.simpleName+": "+e.message);Ui.message(this,getString(if(e is SecurityException)R.string.scan_root_unavailable else R.string.scan_failed))}}}}}
 }
 private fun scanIssues(){val issues=store.lastScanIssues;val detail=issues.joinToString("\n"){it.name+" — "+getString(when(it.kind){"zip"->R.string.scan_issue_zip;"folder"->R.string.scan_issue_folder;"limit"->R.string.scan_issue_limit;else->R.string.scan_issue_file})};AlertDialog.Builder(this).setTitle(getString(R.string.scan_partial,games.size,issues.size)).setMessage(detail).setPositiveButton(R.string.ok){_,_->autoCovers()}.show()}
 private fun autoCovers(){
  val prefs=getSharedPreferences("pocket-options",0)
  if(prefs.getBoolean("coverAuto",false)){downloadCovers();return}
  if(prefs.getBoolean("coverAsked",false))return
  prefs.edit().putBoolean("coverAsked",true).apply()
  AlertDialog.Builder(this).setTitle(R.string.cover_all).setNeutralButton(R.string.privacy){_,_->Ui.message(this,getString(R.string.privacy_body))}.setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.download){_,_->prefs.edit().putBoolean("coverAuto",true).apply();downloadCovers()}.show()
 }
 private fun covers(){AlertDialog.Builder(this).setTitle(R.string.cover_all).setNeutralButton(R.string.privacy){_,_->Ui.message(this,getString(R.string.privacy_body))}.setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.download){_,_->downloadCovers()}.show()}
 private fun downloadCovers(){
  val todo=games.filter{!store.cover(it.key).isFile};if(todo.isEmpty())return
  count.text=getString(R.string.working)
  io.execute{val client=CoverService(this);var n=0;for(g in todo){if(closed||Thread.currentThread().isInterrupted)break;try{if(client.fetch(g))n++}catch(_:Exception){}}
   runOnUiThread{if(!closed){draw();Toast.makeText(this,getString(R.string.cover_result,n),Toast.LENGTH_LONG).show()}}
  }
 }
 override fun onConfigurationChanged(c:android.content.res.Configuration){super.onConfigurationChanged(c);draw()}
 override fun onDestroy(){closed=true;io.shutdownNow();super.onDestroy()}
}
