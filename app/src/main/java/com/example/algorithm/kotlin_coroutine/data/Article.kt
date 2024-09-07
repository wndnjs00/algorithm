package com.example.algorithm.kotlin_coroutine.data

data class Article(
    val id: String,           // 기사 고유 식별자
    val title: String,        // 기사 제목
    val content: String,      // 기사 내용
    val publishedAt: Long     // 게시 날짜
)
