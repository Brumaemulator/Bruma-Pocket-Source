// SPDX-License-Identifier: GPL-3.0-or-later
package com.linkcore.emulator
interface TranslationAvailability { data class Compatible(val id:String,val gameSha256:String,val patchSha256:String,val language:String); fun find(gameSha256:String):List<Compatible> }
