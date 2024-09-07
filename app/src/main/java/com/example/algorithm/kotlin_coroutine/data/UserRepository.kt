package com.example.algorithm.kotlin_coroutine.data

import android.service.autofill.UserData

interface UserRepository {
    suspend fun getUser(): com.example.algorithm.kotlin_coroutine.data.UserData
}