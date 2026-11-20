// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket
import android.app.Activity
import android.content.*
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.*
import android.widget.*
object Ui {
 private val coverCache=object:android.util.LruCache<String,Bitmap>((Runtime.getRuntime().maxMemory()/1024/12).toInt().coerceIn(4096,24576)) { override fun sizeOf(key:String,value:Bitmap)=value.byteCount/1024 }
 private val coverWorker=java.util.concurrent.Executors.newFixedThreadPool(2)

 val bg=0xfff2f1ea.toInt();val ink=0xff20352b.toInt();val muted=0xff718074.toInt();val sage=0xffdce9d9.toInt();val accent=0xff447460.toInt();val lime=0xffd6ef82.toInt()
 fun dp(c:Context,n:Int)=(n*c.resources.displayMetrics.density).toInt()
 fun text(c:Context,s:CharSequence,size:Int=16,color:Int=ink)=TextView(c).apply{text=s;textSize=size.toFloat();setTextColor(color);includeFontPadding=false}
 fun panel(v:View,color:Int=android.graphics.Color.WHITE,radius:Int=22){v.background=GradientDrawable().apply{setColor(color);cornerRadius=dp(v.context,radius).toFloat()}}
 fun button(c:Context,s:String,filled:Boolean=true,action:()->Unit)=Button(c).apply{text=s;isAllCaps=false;minWidth=0;minimumWidth=0;stateListAnimator=null;textSize=14f;setTextColor(if(filled)android.graphics.Color.WHITE else ink);panel(this,if(filled)accent else sage,16);setPadding(dp(c,16),dp(c,10),dp(c,16),dp(c,10));setOnClickListener{if(android.animation.ValueAnimator.areAnimatorsEnabled()){animate().cancel();scaleX=.97f;scaleY=.97f;animate().scaleX(1f).scaleY(1f).setDuration(130).start()};action()}}
 fun column(c:Context)=LinearLayout(c).apply{orientation=LinearLayout.VERTICAL}
 fun space(c:Context,h:Int)=View(c).apply{layoutParams=LinearLayout.LayoutParams(1,dp(c,h))}
 fun root(a:Activity):LinearLayout {
  a.window.statusBarColor=bg;a.window.navigationBarColor=bg
  a.window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
  return column(a).apply{setBackgroundColor(bg);setPadding(dp(a,20),dp(a,8),dp(a,20),dp(a,12));setOnApplyWindowInsetsListener{v,i->v.setPadding(dp(a,20)+i.systemWindowInsetLeft,dp(a,8)+i.systemWindowInsetTop,dp(a,20)+i.systemWindowInsetRight,dp(a,12)+i.systemWindowInsetBottom);i};a.setContentView(this);requestApplyInsets()}
 }
 fun row(c:Context)=LinearLayout(c).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
 fun go(c:Context,cls:Class<*>,id:String?=null,resume:Boolean=false){c.startActivity(Intent(c,cls).apply{if(id!=null)putExtra("id",id);putExtra("resume",resume)})}
 fun bruma(a:Activity){try{a.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.brumastudio.bruma")).setPackage("com.android.vending"))}catch(e:ActivityNotFoundException){try{a.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.brumastudio.bruma")))}catch(missing:ActivityNotFoundException){(a.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(ClipData.newPlainText("BRUMA Emulator","https://play.google.com/store/apps/details?id=com.brumastudio.bruma"));message(a,a.getString(R.string.store_unavailable))}}}
 fun message(a:Activity,s:String)=android.app.AlertDialog.Builder(a).setMessage(s).setPositiveButton(R.string.ok,null).show()
 fun cover(c:Context,id:String,system:String):View {
  val f=com.linkcore.emulator.LibraryStore(c).cover(id)
  val frame=FrameLayout(c);frame.addView(PocketArt(c,system),FrameLayout.LayoutParams(-1,-1))
  if(!f.isFile)return frame
  val key=f.path+":"+f.lastModified();frame.tag=key
  fun show(bitmap:Bitmap){if(frame.tag!=key)return;frame.removeAllViews();frame.addView(ImageView(c).apply{setImageBitmap(bitmap);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription=system},FrameLayout.LayoutParams(-1,-1))}
  coverCache.get(key)?.let{show(it);return frame}
  coverWorker.execute {
   try{val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(f.path,bounds)
    if(bounds.outWidth in 1..4096 && bounds.outHeight in 1..4096){val options=BitmapFactory.Options();while(bounds.outWidth/options.inSampleSize.coerceAtLeast(1)>768 || bounds.outHeight/options.inSampleSize.coerceAtLeast(1)>768)options.inSampleSize=options.inSampleSize.coerceAtLeast(1)*2
     val bitmap=BitmapFactory.decodeFile(f.path,options);if(bitmap!=null){coverCache.put(key,bitmap);frame.post{show(bitmap)}}}
   }catch(_:Exception){}
  }
  return frame
 }

}
class PocketArt(c:Context,private val system:String):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 override fun onDraw(cv:Canvas){val w=width.toFloat();val h=height.toFloat();p.color=when(system){"GB"->0xffdce9d9.toInt();"GBC"->0xffe8dbec.toInt();else->0xffdce8ef.toInt()};cv.drawRoundRect(0f,0f,w,h,24f,24f,p)
  val s=minOf(w,h)/11;p.color=Ui.accent
  for(y in 0..4)for(x in 0..4)if((x*3+y*2)%5<2)cv.drawRoundRect(w*.24f+x*s,h*.2f+y*s,s*.7f+w*.24f+x*s,s*.7f+h*.2f+y*s,s*.1f,s*.1f,p)
  p.color=Ui.ink;p.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);p.textSize=minOf(w*.20f,54f);p.textAlign=Paint.Align.CENTER;cv.drawText(system,w/2,h*.78f,p)
 }
}
