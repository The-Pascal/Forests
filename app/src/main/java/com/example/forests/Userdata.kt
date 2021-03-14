package com.example.forests

import androidx.preference.PreferenceManager
import org.kodein.di.Copy
import org.kodein.di.android.AndroidContextGetter

data class Userdata(var normalizedscore:Int,
                    var lattitude:String,
                    var longitude:String,
                    var plantedtrees:Int,
                    var treesreferred:Int,
                    var ongoingaction:List<Int>,
                    var completedaction:List<Int>,
                    var remainingaction:List<Int>,
                    var presentaction:Int,
                    var rating:String,
                    var targetTrees:Int,
                    var task1:Int,
                    var task2:Int,
                    var task3:Int,
                    var task4:Int

){
    constructor(): this(
        0,
        "",
        "",
        0,
        0,
        listOf<Int>(0),
        listOf<Int>(0),
        listOf<Int>(0),
        0,
        "Rookie",
        0,
        0,
        0,
        0,
        0
    )
}
