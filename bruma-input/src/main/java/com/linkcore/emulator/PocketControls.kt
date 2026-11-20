// SPDX-License-Identifier: GPL-3.0-or-later
package com.linkcore.emulator
import android.content.Context
import android.graphics.*
import android.view.*
class PocketControls(c:Context):View(c){
 @Volatile var keys=0;private set
 var gba=true
 var editing=false
 var onFastForward:()->Unit={}
 var fastForwardActive=false;set(value){field=value;invalidate()}
 private var fastPointer=-1
 var screenRegion=RectF();set(value){if(field==value)return;field=RectF(value);refresh()}
 private val prefs=c.getSharedPreferences("pocket-controls",0)
 private val d=resources.displayMetrics.density
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 private data class Button(val name:String,val mask:Int,val rect:RectF)
 private val buttons=mutableListOf<Button>();private var selected:Button?=null
 private fun prefix()=(if(width>height)"wide"else"tall")+(if(gba)".gba."else".gb.")
 fun refresh(){onSizeChanged(width,height,width,height);invalidate()}
 fun clearKeys(){keys=0;fastPointer=-1;invalidate()}
 fun reset(){prefs.edit().clear().apply();refresh()}
 override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int){
  if(w==0||h==0)return;buttons.clear();val wide=w>h;val size=prefs.getFloat("size",1f).coerceIn(.65f,1.4f)
  val panelTop=if(screenRegion.isEmpty)h-300*d else screenRegion.bottom+12*d
  val cy=if(wide)h-105*d else panelTop+(h-panelTop)*.47f
  fun add(n:String,m:Int,cx:Float,yy:Float,ww:Float,hh:Float){val rw=minOf(ww*d*size,w*.39f);val rh=minOf(hh*d*size,h*.30f);val x=prefs.getFloat(prefix()+n+"x",cx/w)*w;val y=prefs.getFloat(prefix()+n+"y",yy/h)*h;buttons+=Button(n,m,RectF((x-rw/2).coerceIn(0f,maxOf(0f,w-rw)),(y-rh/2).coerceIn(0f,maxOf(0f,h-rh)),0f,0f).apply{right=left+rw;bottom=top+rh})}
  add("DPAD",0,85*d,cy,122f,122f)
  add("A",1,w-63*d,cy-25*d,62f,62f);add("B",2,w-131*d,cy+25*d,62f,62f)
  add("SELECT",4,w/2f-46*d,h-50*d,72f,28f);add("START",8,w/2f+46*d,h-50*d,72f,28f)
  add("FAST",0,w/2f,cy-25*d,48f,48f)
  if(gba){add("L",512,64*d,if(wide)95*d else panelTop+24*d,86f,30f);add("R",256,w-64*d,if(wide)95*d else panelTop+24*d,86f,30f)}
  clearKeys()
 }
 override fun onDraw(canvas:Canvas){
  val portrait=height>width
  if(portrait){
   val top=if(screenRegion.isEmpty)height-300*d else screenRegion.bottom+8*d
   p.style=Paint.Style.FILL;p.alpha=255;p.color=0xfff2f1ea.toInt();canvas.drawRoundRect(8*d,top,width-8*d,height-8*d,24*d,24*d,p)
   if(!screenRegion.isEmpty){p.style=Paint.Style.STROKE;p.color=0xff718074.toInt();p.strokeWidth=3*d;canvas.drawRoundRect(screenRegion,10*d,10*d,p);p.style=Paint.Style.FILL}
   p.typeface=Typeface.create("sans-serif-black",Typeface.NORMAL);p.textSize=13*d;p.textAlign=Paint.Align.CENTER
   val colors=intArrayOf(0xff3478a7.toInt(),0xffa84d80.toInt(),0xff50894b.toInt(),0xffb77a2b.toInt(),0xff7555aa.toInt(),0xffbf6252.toInt());val text="Pocket";var x=width/2f-p.measureText(text)/2;for(i in text.indices){p.color=colors[i];p.textAlign=Paint.Align.LEFT;canvas.drawText(text[i].toString(),x,top+24*d,p);x+=p.measureText(text[i].toString())}
  }
  val alpha=(prefs.getFloat("alpha",.85f).coerceIn(.2f,1f)*255).toInt()
  for(b in buttons){p.color=if(keys and b.mask!=0&&b.mask!=0)0xffd6ef82.toInt() else if(portrait)when(b.name){"DPAD"->0xff30383c.toInt();"A","B"->0xffa84d80.toInt();else->0xffb5c0b7.toInt()}else 0xffdbe6d6.toInt();p.alpha=alpha;p.style=Paint.Style.FILL
   if(b.name=="FAST"){
    p.color=if(fastForwardActive)0xffd6ef82.toInt()else 0xffdce9d9.toInt();canvas.drawCircle(b.rect.centerX(),b.rect.centerY(),b.rect.width()*.44f,p)
    p.color=0xff20352b.toInt();val cx=b.rect.centerX();val cy=b.rect.centerY();val unit=b.rect.width()/48
    for(offset in listOf(-8f,1f)){val arrow=Path().apply{moveTo(cx+offset*unit,cy-7*unit);lineTo(cx+(offset+8)*unit,cy);lineTo(cx+offset*unit,cy+7*unit);close()};canvas.drawPath(arrow,p)}
   }else if(b.name=="DPAD"){
    val r=b.rect;val a=r.width()/3
    val cross=Path().apply{moveTo(r.left+a,r.top);lineTo(r.left+2*a,r.top);lineTo(r.left+2*a,r.top+a);lineTo(r.right,r.top+a);lineTo(r.right,r.top+2*a);lineTo(r.left+2*a,r.top+2*a);lineTo(r.left+2*a,r.bottom);lineTo(r.left+a,r.bottom);lineTo(r.left+a,r.top+2*a);lineTo(r.left,r.top+2*a);lineTo(r.left,r.top+a);lineTo(r.left+a,r.top+a);close()};p.pathEffect=CornerPathEffect(6*d);canvas.drawPath(cross,p);p.pathEffect=null
    p.color=if(portrait)0xff718074.toInt()else 0xff43634f.toInt();p.alpha=alpha;canvas.drawCircle(r.centerX(),r.centerY(),5*d,p)
   }else{canvas.drawRoundRect(b.rect,if(b.name.length==1)40*d else 10*d,if(b.name.length==1)40*d else 10*d,p);p.color=if(portrait&&b.name in listOf("A","B"))Color.WHITE else 0xff17342c.toInt();p.alpha=alpha;p.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);p.textAlign=Paint.Align.CENTER;p.textSize=if(b.name.length==1)22*d else 10*d;canvas.drawText(b.name,b.rect.centerX(),b.rect.centerY()-(p.ascent()+p.descent())/2,p)}
   if(editing){p.style=Paint.Style.STROKE;p.color=0xffd6ef82.toInt();p.alpha=255;p.strokeWidth=2*d;canvas.drawRoundRect(b.rect,8*d,8*d,p);p.style=Paint.Style.FILL}
  }
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(editing){when(e.actionMasked){MotionEvent.ACTION_DOWN->{selected=buttons.lastOrNull{it.rect.contains(e.x,e.y)};return selected!=null};MotionEvent.ACTION_MOVE->{selected?.let{b->b.rect.offsetTo((e.x-b.rect.width()/2).coerceIn(0f,width-b.rect.width()),(e.y-b.rect.height()/2).coerceIn(0f,height-b.rect.height()))};invalidate()};MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{selected?.let{prefs.edit().putFloat(prefix()+it.name+"x",it.rect.centerX()/width).putFloat(prefix()+it.name+"y",it.rect.centerY()/height).apply()};selected=null}};clearKeys();return true}
  if(e.actionMasked==MotionEvent.ACTION_DOWN&&buttons.none{it.rect.contains(e.x,e.y)})return false
  if(e.actionMasked==MotionEvent.ACTION_DOWN||e.actionMasked==MotionEvent.ACTION_POINTER_DOWN){val i=e.actionIndex;if(buttons.any{it.name=="FAST"&&it.rect.contains(e.getX(i),e.getY(i))})fastPointer=e.getPointerId(i)}
  if(e.actionMasked==MotionEvent.ACTION_CANCEL)fastPointer=-1
  if(e.actionMasked==MotionEvent.ACTION_UP||e.actionMasked==MotionEvent.ACTION_POINTER_UP){val i=e.actionIndex;if(e.getPointerId(i)==fastPointer){fastPointer=-1;if(buttons.any{it.name=="FAST"&&it.rect.contains(e.getX(i),e.getY(i))}){performClick();onFastForward()}}}
  var state=0
  if(e.actionMasked!=MotionEvent.ACTION_CANCEL&&e.actionMasked!=MotionEvent.ACTION_UP){for(i in 0 until e.pointerCount){if(e.actionMasked==MotionEvent.ACTION_POINTER_UP&&i==e.actionIndex)continue;val x=e.getX(i);val y=e.getY(i)
   for(b in buttons)if(b.rect.contains(x,y)){if(b.name!="DPAD")state=state or b.mask else{val dx=(x-b.rect.centerX())/(b.rect.width()/2);val dy=(y-b.rect.centerY())/(b.rect.height()/2);if(dx>.25)state=state or 16;if(dx<-.25)state=state or 32;if(dy>.25)state=state or 128;if(dy<-.25)state=state or 64}}
  }}
  if(state!=0&&state!=keys&&prefs.getBoolean("vibration",false))performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
  keys=state;invalidate();return true
 }
 override fun performClick():Boolean{super.performClick();return true}
}
