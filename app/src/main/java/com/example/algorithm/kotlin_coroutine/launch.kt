package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// launch => 현재 스레드를 차단하지 않고 새로운 코루틴을 실행
// launch => public fun CoroutineScope.launch() 로 CoroutineScope의 확장함수기때문에 CoroutineScope내에 launch를 작성해줘야함
fun main(){
    GlobalScope.launch{
        delay(1000L)    // delay(중단함수) => suspend함수의 일종
        println("World!")
    }

    GlobalScope.launch{
        delay(1000L)
        println("World!")
    }

    GlobalScope.launch{
        delay(1000L)
        println("World!")
    }

    println("Hello,")
    Thread.sleep(2000L)     // Thread를 블록시킴(프로그램이 너무 빨리 중단되는것을 막기위해)
}

// Hello,
// (1 sec)
// World!
// World!
// World!



// 아무것도 출력X
// IOThread는 MainThread가 종료되면 같이 종료되기 때문!
// 해결하기 위해서는 IOThread 작업이 완료될 때까지 MainThread에서 기다려줘야 함
fun main000() {
    val job = CoroutineScope(Dispatchers.IO).launch() {
        delay(3000)
        println("launch완료")
    }
}


// MainThread에서 delay(중단기능)를 사용하기위해 suspend 키워드를 붙이고,
// delay로 MainThread를 Blocking 시켜 4초간 기다리게 만들었음
// 그치만 우리는 IO Thread가 언제 끝날지 항상 예측할 수 없기때문에, delay()로 하는 건 좋은방법X
suspend fun main011() {
    val job = CoroutineScope(Dispatchers.IO).launch() {
        delay(3000)
        println("launch완료")
    }
    delay(4000)
}


// launch는 job객체 반환
// job.join() 메서드를 사용하면 job이 종료될때까지 실행되고 있는 코루틴을 일시중단 시킴
// 이렇게하면 얼마나 걸릴지 모르는 작업도 수행가능
suspend fun main012() {
    val job = CoroutineScope(Dispatchers.IO).launch() {
        delay(3000)
        println("launch완료")
    }
    job.join()
}