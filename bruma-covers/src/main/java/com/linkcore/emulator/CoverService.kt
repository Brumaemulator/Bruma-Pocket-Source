// SPDX-License-Identifier: GPL-3.0-or-later
package com.linkcore.emulator
import android.content.Context
import android.graphics.BitmapFactory
import android.util.AtomicFile
class CoverService(private val c:Context){
 private val client=LibretroClient(c)
 fun cancel()=client.cancel()
 fun fetch(g:LibraryGame):Boolean {
  client.begin();val names=client.catalog(g.system);val match=LibretroClient.match(g.name,names,c.resources.configuration.locales[0].language=="es")?:return false
  val bytes=client.fetchCover(match,g.system)
  CoverImages.store(c,g.key,bytes);return true
 }
}
/** Bound decoded image memory and disk dimensions for large libraries. */
object CoverImages {
 fun store(c:Context,id:String,bytes:ByteArray){
  require(bytes.size<=8*1024*1024)
  val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
  require(bounds.outWidth in 1..4096&&bounds.outHeight in 1..4096)
  val options=BitmapFactory.Options().apply{inSampleSize=1};while(maxOf(bounds.outWidth,bounds.outHeight)/options.inSampleSize>1536)options.inSampleSize*=2
  val decoded=BitmapFactory.decodeByteArray(bytes,0,bytes.size,options)?:error("Invalid image")
  val scale=minOf(1f,768f/maxOf(decoded.width,decoded.height));val image=if(scale<1f)android.graphics.Bitmap.createScaledBitmap(decoded,(decoded.width*scale).toInt().coerceAtLeast(1),(decoded.height*scale).toInt().coerceAtLeast(1),true)else decoded
  try{val file=LibraryStore(c).cover(id);file.parentFile!!.mkdirs();val a=AtomicFile(file);val stream=a.startWrite();try{check(image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream));a.finishWrite(stream)}catch(e:Exception){a.failWrite(stream);throw e}}
  finally{if(image!==decoded)image.recycle();decoded.recycle()}
 }
}
