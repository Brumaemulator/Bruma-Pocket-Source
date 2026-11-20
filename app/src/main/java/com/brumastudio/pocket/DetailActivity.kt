// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket
import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import com.linkcore.emulator.*
import java.util.concurrent.Executors
class DetailActivity:Activity(){
 override fun attachBaseContext(base:android.content.Context){super.attachBaseContext(PocketLocale.attach(base))}
 private lateinit var store:LibraryStore;private lateinit var game:LibraryGame;private val io=Executors.newSingleThreadExecutor();private var closed=false
 override fun onCreate(b:Bundle?){super.onCreate(b);store=LibraryStore(this);val found=store.read().find{it.key==intent.getStringExtra("id")};if(found==null){finish();return};game=found;draw()}
 override fun onResume(){super.onResume();if(::game.isInitialized)draw()}
 private fun draw(){val outer=Ui.root(this);val head=Ui.row(this);head.addView(Ui.button(this,"‹",false){finish()});head.addView(Ui.text(this,getString(R.string.game_info),12,Ui.muted));outer.addView(head)
  val box=Ui.column(this);val scroll=ScrollView(this);scroll.addView(box);outer.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));box.addView(Ui.space(this,16));box.addView(Ui.cover(this,game.key,game.system),LinearLayout.LayoutParams(-1,Ui.dp(this,240)));box.addView(Ui.space(this,18));box.addView(Ui.text(this,game.system,12,Ui.accent));box.addView(Ui.space(this,7));box.addView(Ui.text(this,game.title,27));box.addView(Ui.space(this,12));box.addView(Ui.text(this,getString(R.string.play_time,store.seconds(game.key)/60),14,Ui.muted));box.addView(Ui.text(this,getString(R.string.last_played,if(store.last(game.key)>0)java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM,java.text.DateFormat.SHORT,resources.configuration.locales[0]).format(java.util.Date(store.last(game.key)))else getString(R.string.never_played)),12,Ui.muted));box.addView(Ui.space(this,18))
  if(SaveManager(this).state(game.key,0).exists()){box.addView(Ui.button(this,getString(R.string.resume)){Ui.go(this,PlayerActivity::class.java,game.key,true)});box.addView(Ui.space(this,8))}
  box.addView(Ui.button(this,getString(R.string.play)){Ui.go(this,PlayerActivity::class.java,game.key,false)})
  box.addView(Ui.space(this,9));box.addView(Ui.button(this,getString(R.string.export_sav),false){if(!SaveManager(this).battery(game.key).isFile){Ui.message(this,getString(R.string.no_battery_save))}else startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream").putExtra(Intent.EXTRA_TITLE,game.title.replace(Regex("[^\\p{L}\\p{N} ._-]"),"_")+".sav"),23)})
  box.addView(Ui.space(this,9));box.addView(Ui.button(this,getString(R.string.import_sav),false){AlertDialog.Builder(this).setMessage(R.string.sav_warning).setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.import_confirm){_,_->startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),24)}.show()})
  val actions=listOf((if(store.favorite(game.key))R.string.favorite_remove else R.string.favorite_add) to {store.toggle(game.key);draw()},R.string.cover_download to {download()},R.string.cover_custom to {startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),20)},R.string.export_save to {startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/zip").putExtra(Intent.EXTRA_TITLE,"Pocket-${game.key.take(12)}-saves.zip"),21)},R.string.import_save to {AlertDialog.Builder(this).setMessage(R.string.import_warning).setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.import_confirm){_,_->startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/zip").addCategory(Intent.CATEGORY_OPENABLE),22)}.show()})
  for((id,action)in actions){box.addView(Ui.space(this,9));box.addView(Ui.button(this,getString(id),false){action()})};box.addView(Ui.space(this,16));box.addView(Ui.text(this,getString(R.string.source_retained),12,Ui.muted));box.addView(Ui.space(this,16))
 }
 private fun download(){AlertDialog.Builder(this).setTitle(R.string.cover_all).setNeutralButton(R.string.privacy){_,_->Ui.message(this,getString(R.string.privacy_body))}.setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.download){_,_->io.execute{try{val ok=CoverService(this).fetch(game);runOnUiThread{if(!closed){draw();if(!ok)Ui.message(this,getString(R.string.cover_missing))}}}catch(e:Exception){runOnUiThread{if(!closed)Ui.message(this,if(e is CoverFailure)getString(e.messageId)else getString(R.string.error))}}}}.show()}
 override fun onActivityResult(r:Int,result:Int,data:Intent?){super.onActivityResult(r,result,data);if(result!=RESULT_OK)return;val uri=data?.data?:return
  if(r==24)PlayerActivity.closeForSaveImport(game.key)
  io.execute{try{when(r){20->{val bytes=contentResolver.openInputStream(uri)!!.use{val b=java.io.ByteArrayOutputStream();val buf=ByteArray(8192);while(true){val n=it.read(buf);if(n<0)break;require(b.size()+n<=8*1024*1024);b.write(buf,0,n)};b.toByteArray()};CoverImages.store(this,game.key,bytes)};21->contentResolver.openOutputStream(uri)!!.use{SaveManager(this).export(game.key,it)};22->contentResolver.openInputStream(uri)!!.use{SaveManager(this).import(game.key,it)};23->contentResolver.openOutputStream(uri)!!.use{SaveManager(this).exportBattery(game.key,it)};24->{require(RomFiles.name(this,uri).endsWith(".sav",true));contentResolver.openInputStream(uri)!!.use{SaveManager(this).importBattery(game.key,it)}}};runOnUiThread{if(!closed){draw();if(r!=20)Ui.message(this,getString(if(r==22||r==24)R.string.import_done else R.string.saved))}}}catch(e:Exception){runOnUiThread{if(!closed)Ui.message(this,getString(R.string.error))}}}
 }
 override fun onDestroy(){closed=true;io.shutdownNow();super.onDestroy()}
}
