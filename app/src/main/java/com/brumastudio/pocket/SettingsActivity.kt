// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket
import android.app.*
import android.os.Bundle
import android.widget.*
class SettingsActivity:Activity(){
 override fun attachBaseContext(base:android.content.Context){super.attachBaseContext(PocketLocale.attach(base))}
 override fun onCreate(b:Bundle?){super.onCreate(b);val outer=Ui.root(this);val scroll=ScrollView(this);val root=Ui.column(this);scroll.addView(root);outer.addView(scroll,LinearLayout.LayoutParams(-1,-1));root.addView(Ui.button(this,getString(R.string.back),false){finish()});root.addView(Ui.space(this,18));root.addView(Ui.text(this,getString(R.string.settings),28));val prefs=getSharedPreferences("pocket-options",0)
  fun toggle(id:Int,key:String,default:Boolean):Switch{val sw=Switch(this);sw.text=getString(id);sw.isChecked=prefs.getBoolean(key,default);sw.setPadding(0,Ui.dp(this,13),0,Ui.dp(this,13));sw.setOnCheckedChangeListener{_,v->prefs.edit().putBoolean(key,v).apply()};root.addView(sw);return sw}
  root.addView(Ui.button(this,getString(R.string.language),false){PocketLocale.choose(this)});root.addView(Ui.space(this,12))
  toggle(R.string.auto_resume,"autoResume",true);toggle(R.string.show_touch,"showTouch",true);toggle(R.string.hide_touch,"hideTouch",true);val integer=toggle(R.string.integer,"integer",false);val stretch=toggle(R.string.stretch,"stretch",false);
  integer.setOnCheckedChangeListener{_,enabled->prefs.edit().putBoolean("integer",enabled).apply();if(enabled)stretch.isChecked=false}
  stretch.setOnCheckedChangeListener{_,enabled->prefs.edit().putBoolean("stretch",enabled).apply();if(enabled)integer.isChecked=false}
  if(integer.isChecked&&stretch.isChecked)stretch.isChecked=false
  toggle(R.string.fast_sound,"fastSound",false)
  val covers=Switch(this);covers.setText(R.string.cover_auto);covers.isChecked=prefs.getBoolean("coverAuto",false);covers.setPadding(0,Ui.dp(this,13),0,Ui.dp(this,13));covers.setOnCheckedChangeListener{_,enabled->if(!enabled)prefs.edit().putBoolean("coverAuto",false).apply()else AlertDialog.Builder(this).setTitle(R.string.cover_all).setNeutralButton(R.string.privacy){_,_->Ui.message(this,getString(R.string.privacy_body))}.setPositiveButton(R.string.download){_,_->prefs.edit().putBoolean("coverAuto",true).putBoolean("coverAsked",true).apply()}.setNegativeButton(R.string.cancel){_,_->covers.isChecked=false}.setOnCancelListener{covers.isChecked=false}.show()};root.addView(covers)
  root.addView(Ui.space(this,18));root.addView(Ui.text(this,getString(R.string.volume),16));val volume=SeekBar(this);volume.max=100;volume.progress=prefs.getInt("volume",80);volume.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,p:Int,user:Boolean){if(user)prefs.edit().putInt("volume",p).apply()};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}});root.addView(volume)
  root.addView(Ui.space(this,24));root.addView(Ui.text(this,getString(R.string.legal_information),12,Ui.muted).apply{setPadding(0,Ui.dp(this@SettingsActivity,12),0,Ui.dp(this@SettingsActivity,12));isFocusable=true;setOnClickListener{information()}})

 }
 private fun information(){val ids=listOf(R.string.about,R.string.licenses,R.string.privacy,R.string.source_code);AlertDialog.Builder(this).setTitle(R.string.legal_information).setItems(ids.map{getString(it)}.toTypedArray()){_,i->when(ids[i]){R.string.about->Ui.message(this,getString(R.string.legal_intro));R.string.privacy->AlertDialog.Builder(this).setTitle(R.string.privacy).setMessage(R.string.privacy_body).setPositiveButton(R.string.full_policy){_,_->openPublic("https://brumastudio.netlify.app/bruma-pocket/privacidad")}.setNegativeButton(R.string.back,null).show();R.string.source_code->openPublic("https://github.com/Brumaemulator/Bruma-Pocket-Source");else->licenses()}}.setNegativeButton(R.string.back,null).show()}
 private fun openPublic(url:String){try{startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(url)))}catch(e:android.content.ActivityNotFoundException){(getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(android.content.ClipData.newPlainText("Bruma Pocket",url));Ui.message(this,url)}}
 private fun licenses(){val names=assets.list("licenses")!!.sorted();AlertDialog.Builder(this).setTitle(R.string.licenses).setItems(names.toTypedArray()){_,i->val t=Ui.text(this,assets.open("licenses/"+names[i]).bufferedReader().use{it.readText()},12);t.setPadding(24,24,24,24);val s=ScrollView(this);s.addView(t);AlertDialog.Builder(this).setTitle(names[i]).setView(s).setPositiveButton(R.string.ok,null).show()}.show()}
}
