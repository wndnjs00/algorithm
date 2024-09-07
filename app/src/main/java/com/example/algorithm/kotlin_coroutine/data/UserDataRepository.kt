package com.example.algorithm.kotlin_coroutine.data


interface UserDataRepository {

    suspend fun getName(): String

    suspend fun getFriends(): List<String>

    suspend fun getProfile(): String

    suspend fun notifyProfileShown()
}
