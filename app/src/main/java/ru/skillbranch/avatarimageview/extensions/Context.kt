package ru.skillbranch.avatarimageview.extensions

import android.content.Context

fun Context.dpToPix(dp:Int): Float{
    return dp.toFloat()*this.resources.displayMetrics.density
}