package com.example.algorithm.kotlin_coroutine.data

data class UserAction(
    val actionId: String,
    val userId: String,
    val timestamp: Long,
    val actionType: String
)