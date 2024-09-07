package com.example.algorithm.kotlin_coroutine.data

import kotlinx.coroutines.delay

suspend fun getNotifications(): List<Notification> {
    delay(1000) // Simulate a network or database delay
    return listOf(
        Notification("Welcome!"),
        Notification("You have new messages.")
    )
}