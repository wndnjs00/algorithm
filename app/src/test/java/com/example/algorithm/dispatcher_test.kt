package com.example.algorithm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors


// Test 라이브러리 추가해줘야함

    class SomeTest {
        // 하나의 스레드에서 동작하는 dispatcher생성(병렬실행X, 모든작업 순차적 실행)
        private val dispatcher = Executors
            .newSingleThreadExecutor()
            .asCoroutineDispatcher()

        // 테스트가 실행되기전에 호출
        @Before
        fun setup() {
            // Dispatchers.Main을 가짜 디스패처로 설정
            // 따라서 테스트중에 Dispatchers.Main을 사용하면 실제 UI스레드 대신, 가짜 스레드에서 동작
            Dispatchers.setMain(dispatcher)
        }

        // 테스트가 완료된후 호출
        @After
        fun tearDown() {
            // 메인 디스패처를 원래의 Main 디스패처로 되돌림
            Dispatchers.resetMain()
            dispatcher.close()  // dispatcher에서 사용된 리소스해제
        }

        // 테스트 메서드
        @Test
        fun testSomeUI(): Unit = runBlocking {    // 코루틴이 완료될때까지 기다림
            launch(Dispatchers.Main) {  // 이렇게 실제로 코루틴실행하는것처럼 테스트수행 -> setup()에서 설정한 가짜 메인 디스페처에서 실행됨
                // ..
            }
        }
    }
