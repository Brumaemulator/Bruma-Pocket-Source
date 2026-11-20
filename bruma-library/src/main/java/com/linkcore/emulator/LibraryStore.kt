// SPDX-License-Identifier: GPL-3.0-or-later
package com.linkcore.emulator
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract as D
import android.provider.OpenableColumns
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.ArrayDeque

data class LibraryGame(val key:String,val name:String,val uri:String="",val cover:String="",val cached:String="",val size:Long=0,val modified:Long=0,val system:String="GBA",val identity:String="",val source:String="") {
 val title:String get()=name.substringBeforeLast('.').replace('_',' ')
}
object RomFiles {
 fun system(name:String)=when(name.substringAfterLast('.').lowercase(java.util.Locale.ROOT)){"gb"->"GB";"gbc"->"GBC";"gba"->"GBA";else->""}
 fun name(c:Context,u:Uri)=c.contentResolver.query(u,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0)else null}?:u.lastPathSegment?:"game"
}
class LibraryStore(private val context:Context) {
 private val index=AtomicFile(File(context.filesDir,"pocket-library.json"))
 val prefs=context.getSharedPreferences("pocket-games",0)
 companion object { fun key(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it.toInt() and 255)} }
 @Synchronized fun read():List<LibraryGame> = try {val a=JSONArray(index.openRead().bufferedReader().use{it.readText()});(0 until a.length()).map{val j=a.getJSONObject(it);LibraryGame(j.getString("id"),j.getString("name"),j.getString("uri"),cached=j.getString("id"),system=j.getString("system"),source=j.getString("uri"),size=j.optLong("size"))}}catch(e:java.io.FileNotFoundException){emptyList()}
 @Synchronized fun write(games:List<LibraryGame>){val a=JSONArray();games.distinctBy{it.key}.forEach{a.put(JSONObject().put("id",it.key).put("name",it.name).put("uri",it.uri).put("system",it.system).put("size",it.size))};val s=index.startWrite();try{s.write(a.toString().toByteArray());index.finishWrite(s)}catch(e:Exception){index.failWrite(s);throw e}}
 fun favorite(id:String)=prefs.getBoolean("fav.$id",false)
 fun toggle(id:String){prefs.edit().putBoolean("fav.$id",!favorite(id)).apply()}
 fun last(id:String)=prefs.getLong("last.$id",0)
 fun seconds(id:String)=prefs.getLong("seconds.$id",0)
 @Synchronized fun played(id:String,seconds:Long){prefs.edit().putLong("last.$id",System.currentTimeMillis()).putLong("seconds.$id",seconds(id)+seconds.coerceAtLeast(0)).putString("latest",id).commit()}
 fun cover(id:String)=File(context.filesDir,"covers/$id.png")
 data class ScanIssue(val name:String,val kind:String)
 @Volatile var lastScanIssues:List<ScanIssue> = emptyList();private set
 @Volatile var lastOtherSystemFolders=0;private set
 private data class Document(val id:String,val name:String,val mime:String,val size:Long,val modified:Long)
 /** Only count relevant documents; prune RPG resource trees and isolate broken entries. */
 fun scan(tree:Uri,cancelled:()->Boolean={false},progress:(Int)->Unit={}):List<LibraryGame>{
  val result=linkedMapOf<String,LibraryGame>();val queue=ArrayDeque<Triple<String,Int,String>>();val root=D.getTreeDocumentId(tree);queue.add(Triple(root,0,root.substringAfterLast('/')));val visited=hashSetOf<String>();val issues=mutableListOf<ScanIssue>();var examined=0;var otherSystems=0
  fun active(){if(cancelled()||Thread.currentThread().isInterrupted)throw java.util.concurrent.CancellationException()}
  fun issue(name:String,kind:String){if(issues.size<100)issues+=ScanIssue(name,kind)}
  val cr=context.contentResolver
  while(queue.isNotEmpty()){
   active();val(id,depth,folderName)=queue.removeFirst();if(!visited.add(id))continue
   if(depth>20||visited.size>10000){issue(folderName,"folder");continue}
   val docs=mutableListOf<Document>()
   try{cr.query(D.buildChildDocumentsUriUsingTree(tree,id),arrayOf(D.Document.COLUMN_DOCUMENT_ID,D.Document.COLUMN_DISPLAY_NAME,D.Document.COLUMN_MIME_TYPE,D.Document.COLUMN_SIZE,D.Document.COLUMN_LAST_MODIFIED),null,null,null)?.use{c->while(c.moveToNext()){active();require(docs.size<100000){"Directory limit"};docs+=Document(c.getString(0),c.getString(1).orEmpty(),c.getString(2).orEmpty(),if(c.isNull(3))0 else c.getLong(3),if(c.isNull(4))0 else c.getLong(4))}}?:error("Folder unavailable")}
   catch(e:Exception){active();if(id==root)throw e;issue(folderName,"folder");continue}
   val names=docs.map{it.name.lowercase(java.util.Locale.ROOT)}.toSet()
   val rpg=("game.ini" in names&&("data" in names||names.any{it.endsWith(".rgssad")||it.endsWith(".rgss2a")||it.endsWith(".rgss3a")}))||("package.json" in names&&"www" in names)
   if(rpg)otherSystems++
   for(doc in docs){
    active()
    if(doc.mime==D.Document.MIME_TYPE_DIR){if(!rpg)queue.add(Triple(doc.id,depth+1,doc.name));continue}
    val system=RomFiles.system(doc.name);val zip=doc.name.endsWith(".zip",true);if(system.isBlank()&&!zip)continue
    if(++examined>50000){issue(doc.name,"limit");queue.clear();break}
    val uri=D.buildDocumentUriUsingTree(tree,doc.id)
    try{
     if(zip){for(g in ArchiveCatalog.read(context,uri,doc.size,doc.modified).games)result.putIfAbsent(g.cached,g.copy(key=g.cached))}
     else{
      require(doc.size<=0||doc.size>=if(system=="GBA")192 else 336){"ROM too small"}
      val signature="${doc.size}:${doc.modified}:";val fingerprintKey="scan."+key(uri.toString());val saved=prefs.getString(fingerprintKey,null)
      val hash=if(doc.size>0&&doc.modified>0&&saved?.startsWith(signature)==true)saved.removePrefix(signature)else RomAccess.hash(context,uri.toString()).also{if(doc.size>0&&doc.modified>0)prefs.edit().putString(fingerprintKey,signature+it).apply()}
      val candidate=LibraryGame(hash,doc.name,uri.toString(),cached=hash,system=system,source=uri.toString(),size=doc.size);val old=result[hash];if(old==null||old.uri.contains("#rom="))result[hash]=candidate
     }
    }catch(e:Exception){active();issue(doc.name,if(zip)"zip"else"file")}
    progress(result.size)
   }
  }
  active();lastScanIssues=issues.toList();lastOtherSystemFolders=otherSystems
  if(result.isEmpty()&&issues.isNotEmpty())throw java.io.IOException("No readable compatible games")
  val games=result.values.sortedWith(compareBy<LibraryGame>{it.system}.thenBy{it.title.lowercase(java.util.Locale.ROOT)});write(games);prefs.edit().putString("folder",tree.toString()).remove("pending_folder").commit();return games
 }
}
