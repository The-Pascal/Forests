package com.example.forests

import androidx.preference.PreferenceManager
import org.kodein.di.android.AndroidContextGetter

data class Userdata(var completedaction:List<Int>,
                    var lattitude:String,
                    var longitude:String,
                    var normalizedscore:Int,
                    var ongoingaction:List<Int>,
                    var plantedtrees:Int,
                    var presentaction:Int,
                    var rating:String,
                    var targertrees:Int,
                    var task1:Int,
                    var task2:Int,
                    var task3:Int,
                    var task4:Int

){
    constructor(): this(
        listOf<Int>(0),
        "",
        "",
        0,
        listOf<Int>(0),
        0,
        0,
        "Rookie",
        0,
        0,
        0,
        0,
        0

    )
}
