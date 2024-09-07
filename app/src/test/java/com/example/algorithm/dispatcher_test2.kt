package com.example.algorithm

import com.example.algorithm.kotlin_coroutine.data.DiscReader
import com.example.algorithm.kotlin_coroutine.data.FakeDiscReader
import com.example.algorithm.kotlin_coroutine.data.UserData
import com.example.algorithm.kotlin_coroutine.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class DiscUserRepository03(
    private val discReader: DiscReader,
    private val dispatcher: CoroutineContext = Dispatchers.IO,
) : UserRepository {
    override suspend fun getUser(): UserData =
        withContext(dispatcher) {
            UserData(discReader.read("userName"))
        }
}

// 테스트 클래스
// ContinuationInterceptor를 사용하여 테스트하는 방법
class UsersReaderTests {
    @Test
    fun `some test`() = runTest {
        // 테스트 준비 과정
        val discReader = FakeDiscReader()   // 실제 discReader대신 FakeDiscReader()라는 가짜객체 사용해서 테스트
        val repo = DiscUserRepository03(
            discReader,
            // 테스트를 수행할 코루틴 중 하나
            // dispatcher로 테스트 코루틴의 ContinuationInterceptor를 전달
            this.coroutineContext[ContinuationInterceptor]!!
        )
        //...
    }
}