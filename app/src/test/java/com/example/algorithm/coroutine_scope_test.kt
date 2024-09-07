package com.example.algorithm

import org.junit.Test
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout


class Test {
    @Test
    fun testTime2() = runTest {
        withTimeout(1000) {
            // 1000ms보다 적게 걸리는 작업
            delay(900) // 가상 시간
        }
    }

    @Test(expected = TimeoutCancellationException::class)
    fun tesTime1() = runTest {
        withTimeout(1000) {
            // 1000ms보다 오래 걸리는 작업
            delay(1100) // 가상 시간
        }
    }

    // runBlocking내부에서도 withTimeout사용 가능
    // 실제시간 테스트
    @Test
    fun testTine3() = runBlocking {
        withTimeout(1000) {
            // 그다지 오래 걸리지 않는 일반적인 테스트
            delay(900) // 실제로 900ms만큼 기다립니다.(실제시간)
        }
    }
}