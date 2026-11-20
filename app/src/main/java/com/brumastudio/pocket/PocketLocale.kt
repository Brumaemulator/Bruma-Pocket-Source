// SPDX-License-Identifier: GPL-3.0-or-later
// Adapted from BRUMA BrumaLocale: separate preferences; same nine supported languages.
package com.brumastudio.pocket
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale
object PocketLocale {
 val tags=listOf("","en","es","pt-BR","fr","de","it","ja","ko","zh-CN")
 val nativeNames=listOf("", "English","Español","Português (Brasil)","Français","Deutsch","Italiano","日本語","한국어","简体中文")
 fun selectedTag(c:Context):String=if(Build.VERSION.SDK_INT>=33)c.getSystemService(android.app.LocaleManager::class.java)?.applicationLocales?.toLanguageTags().orEmpty()else c.getSharedPreferences("pocket-language",0).getString("tag","").orEmpty()
 fun attach(base:Context):Context {val tag=selectedTag(base);if(tag.isBlank())return base;val locale=Locale.forLanguageTag(tag);val configuration=Configuration(base.resources.configuration);configuration.setLocale(locale);configuration.setLayoutDirection(locale);return base.createConfigurationContext(configuration)}
 fun setLanguage(c:Context,tag:String){require(tag in tags);if(Build.VERSION.SDK_INT>=33)c.getSystemService(android.app.LocaleManager::class.java)?.applicationLocales=LocaleList.forLanguageTags(tag)else c.getSharedPreferences("pocket-language",0).edit().putString("tag",tag).commit()}
 fun choose(a:Activity){val names=nativeNames.toMutableList();names[0]=a.getString(R.string.language_system);val selected=tags.indexOf(selectedTag(a)).coerceAtLeast(0);AlertDialog.Builder(a).setTitle(R.string.language).setSingleChoiceItems(names.toTypedArray(),selected){dialog,index->dialog.dismiss();if(tags[index]!=selectedTag(a)){setLanguage(a,tags[index]);a.recreate()}}.setNegativeButton(R.string.cancel,null).show()}
}
