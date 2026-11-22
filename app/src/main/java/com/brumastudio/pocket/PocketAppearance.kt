// SPDX-License-Identifier: GPL-3.0-or-later
package com.brumastudio.pocket

import android.app.Activity
import android.app.AlertDialog
import android.content.Context

object PocketAppearance {
 fun dark(c:Context)=c.getSharedPreferences("pocket-options",0).getBoolean("darkTheme",false)
 fun apply(a:Activity){
  Ui.dark=dark(a)
  a.setTheme(if(Ui.dark)R.style.PocketDarkTheme else R.style.PocketTheme)
 }
 fun choose(a:Activity){
  val names=arrayOf(a.getString(R.string.theme_light),a.getString(R.string.theme_dark))
  AlertDialog.Builder(a).setTitle(R.string.appearance).setSingleChoiceItems(names,if(dark(a))1 else 0){dialog,index->
   dialog.dismiss()
   if((index==1)!=dark(a)){
    a.getSharedPreferences("pocket-options",0).edit().putBoolean("darkTheme",index==1).apply()
    a.recreate()
   }
  }.setNegativeButton(R.string.cancel,null).show()
 }
}
