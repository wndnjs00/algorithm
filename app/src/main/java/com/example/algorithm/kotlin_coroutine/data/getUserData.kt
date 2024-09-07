package com.example.algorithm.kotlin_coroutine.data

import kotlinx.coroutines.delay

suspend fun getUserData(): UserData {
    delay(1000)
    return UserData(name = "John Doe")
}

