// SPDX-License-Identifier: GPL-3.0-or-later
package com.linkcore.emulator
import android.content.Context
import android.util.AtomicFile
import java.io.*
import java.util.zip.*
import org.json.JSONObject
class SaveManager(private val c:Context){
 private val root=File(c.filesDir,"saves").apply{mkdirs()}
 private fun valid(id:String){require(id.matches(Regex("[a-f0-9]{64}")))}
 fun battery(id:String):File {valid(id);return File(root,"$id.sav")}
 fun state(id:String,slot:Int):File {valid(id);require(slot in 0..4);return File(root,"$id-slot$slot.state")}
 private fun digest(data:ByteArray)=java.security.MessageDigest.getInstance("SHA-256").digest(data).joinToString(""){"%02x".format(it.toInt() and 255)}
 fun export(id:String,out:OutputStream){valid(id);val files=(listOf(battery(id))+(0..4).map{state(id,it)}).filter{it.isFile};val hashes=JSONObject();files.forEach{hashes.put(it.name,digest(it.readBytes()))};ZipOutputStream(out).use{z->z.putNextEntry(ZipEntry("manifest.json"));z.write(JSONObject().put("format",1).put("gameSha256",id).put("files",hashes).toString().toByteArray());z.closeEntry();for(f in files){z.putNextEntry(ZipEntry(f.name));f.inputStream().use{it.copyTo(z)};z.closeEntry()}}}
 /** Raw mGBA SRAM/EEPROM/Flash format accepted by BRUMA SaveArchive.decodeImport. */
 fun exportBattery(id:String,out:OutputStream){val f=battery(id);require(f.isFile&&f.length() in setOf(512L,8192L,32768L,65536L,131072L)){"No compatible in-game save yet"};f.inputStream().use{it.copyTo(out)}}
 fun importBattery(id:String,input:InputStream){
  valid(id);val data=input.use{val out=ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){val n=it.read(buffer);if(n<0)break;require(out.size()+n<=131072){"Invalid .sav size"};out.write(buffer,0,n)};out.toByteArray()};require(data.size in setOf(512,8192,32768,65536,131072)){"Invalid .sav size"}
  val file=battery(id);val existed=file.exists();val backup=File(file.path+".before-sav-import");if(existed)file.copyTo(backup,true)
  val oldStates=(0..4).map{state(id,it)}.filter{it.isFile};for(state in oldStates)state.copyTo(File(state.path+".before-sav-import"),true)
  try{val atomic=AtomicFile(file);val stream=atomic.startWrite();try{stream.write(data);atomic.finishWrite(stream)}catch(e:Exception){atomic.failWrite(stream);throw e};for(state in oldStates)check(state.delete())}
  catch(e:Exception){if(existed)backup.copyTo(file,true)else file.delete();for(state in oldStates)File(state.path+".before-sav-import").copyTo(state,true);throw e}
 }
 /** Validate every entry before replacing anything; retain pre-import backups. */
 fun import(id:String,input:InputStream){valid(id);val staged=linkedMapOf<String,ByteArray>();var manifest=false;var metadata:JSONObject?=null;var total=0L
  ZipInputStream(input).use{z->var count=0;while(true){val e=z.nextEntry?:break;require(++count<=8&&!e.isDirectory);val n=e.name;require(n=="manifest.json"||n=="$id.sav"||(0..4).any{n=="$id-slot$it.state"});require(!staged.containsKey(n));val out=ByteArrayOutputStream();val b=ByteArray(8192);while(true){val k=z.read(b);if(k<0)break;total+=k;require(total<=12*1024*1024);out.write(b,0,k)};val data=out.toByteArray();if(n=="manifest.json"){require(!manifest);metadata=JSONObject(String(data));require(metadata!!.getInt("format")==1 && metadata!!.getString("gameSha256")==id);manifest=true}else staged[n]=data;z.closeEntry()}}
  require(manifest&&staged.isNotEmpty())
  val hashes=metadata!!.getJSONObject("files");require(hashes.length()==staged.size)
  for((name,data)in staged){require(data.isNotEmpty()&&data.size<=2*1024*1024);require(name.endsWith(".sav")||data.size>=1024);require(hashes.getString(name)==digest(data)){"Backup checksum mismatch"}}

  val committed=mutableListOf<Pair<File,Boolean>>()
  try{for((name,data)in staged){val f=File(root,name);val existed=f.exists();if(existed)f.copyTo(File(root,"$name.before-import"),true);val a=AtomicFile(f);val s=a.startWrite();try{s.write(data);a.finishWrite(s)}catch(e:Exception){a.failWrite(s);throw e};committed+=f to existed}}
  catch(e:Exception){for((f,existed)in committed){if(existed)File(root,f.name+".before-import").copyTo(f,true)else f.delete()};throw e}
 }
}
