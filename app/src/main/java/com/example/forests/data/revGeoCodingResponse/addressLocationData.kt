package com.example.forests.data.revGeoCodingResponse


import com.google.gson.annotations.SerializedName

data class addressLocationData(
    val responseCode: Int,
    val results: List<Result>,
    val version: String
)