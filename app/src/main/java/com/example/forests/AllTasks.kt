package com.example.forests

data class AllTasks(
    val id: Int,
    val topic:String,
    val subtopic: String,
    val value: Int) {
    constructor():this(
        0,
        "",
        "",
        0
    )
}