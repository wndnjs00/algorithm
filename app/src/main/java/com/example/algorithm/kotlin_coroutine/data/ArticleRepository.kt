package com.example.algorithm.kotlin_coroutine.data

interface ArticleRepository {
    suspend fun fetchArticles(): List<Article>
}