package com.example.forests

data class DashboardData(var normalizedscore:Int,
                    var airQualityIndex:Int,
                    var forestDensity:Int,
                    var so2:Int,
                    var co:Int,
                    var no2:Int,
                    var o3: Int,
                    var actualForest:Int,
                    var openForest:Int,
                    var noForest:Int,
                    var recommendedTarget:Int
){
    constructor(): this(
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
    0
    )
}
