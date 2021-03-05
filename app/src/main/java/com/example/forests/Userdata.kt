package com.example.forests

import androidx.preference.PreferenceManager
import org.kodein.di.android.AndroidContextGetter

data class Userdata(var lattitude:String,var longitude:String, var targertrees:Int, var normalizedscore:Int, var plantedtrees:Int,var rating:String){
    constructor(): this("","",0,0,0,"Rookie")
}
