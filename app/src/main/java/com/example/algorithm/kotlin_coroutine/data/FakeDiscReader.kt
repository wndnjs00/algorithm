package com.example.algorithm.kotlin_coroutine.data

class FakeDiscReader : DiscReader {
    override fun read(key: String): String {
        // 가짜 데이터를 반환하도록 설정
        return when (key) {
            "userName" -> "Test User"
            else -> "Unknown"
        }
    }
}