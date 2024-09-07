package com.example.algorithm.kotlin_coroutine.data

import kotlinx.coroutines.delay

data class Profile(val name: String, val age: Int)
data class Friend(val name: String)
data class User01(val profile: Profile, val friends: List<Friend>)

class UserRepositories {
    suspend fun getProfile(): Profile {
        delay(1000) // Simulate a network or database delay
        return Profile(name = "John Doe", age = 30)
    }

    suspend fun getFriends(): List<Friend> {
        delay(1000) // Simulate a network or database delay
        return listOf(
            Friend(name = "Alice"),
            Friend(name = "Bob"),
            Friend(name = "Charlie")
        )
    }
}

val repo = UserRepositories()
