package com.example.algorithm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 일반적으로 코루틴은 스레드를 블로킹하지 않고, 작업을 중단시키기만 하는것이 일반적인데,
// runBlocking는 스레드를 블로킹하는 기능을 가짐(프로그램을 너무 빨리 종료시키지 않기위해 사용)
// runBlocking 내부에 delay(1000L)를 호출하면, Thread.sleep(1000L)와 같은기능 => 스레드 블록시키는것과 같은기능
fun main() {
    runBlocking{
        delay(1000L)
        println("World!")
    }

    runBlocking{
        delay(1000L)
        println("World!")
    }

    runBlocking{
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}
//(1초후)
//World!
//(1초후)
//World!
//(1초후)
//World!
//Hello,



// 위보다 이 방식이 좀 더 유용
fun main2() = runBlocking {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    println("Hello,")
    delay(2000L) // 여전히 필요
}
//Hello,
//(1초후)
//World!
//World!
//World!



// 그치만 현재는 runBlocking을 거의 사용하지 않음
// runBlocking 대신에 suspend fun함수를 사용해서 중단시킴
suspend fun main3() {
    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    GlobalScope.launch {
        delay(1000L)
        println("World!")
    }

    println("Hello,")
    delay(2000L)
}
//Hello,
//(1초후)
//World!
//World!
//World!
