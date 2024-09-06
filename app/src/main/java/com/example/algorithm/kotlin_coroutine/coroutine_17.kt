package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import splitties.coroutines.raceOf

/* 17장 - 셀렉트 */


suspend fun requestData1(): String {
    delay(100_000)  // 100초 일시중단
    return "Data1"
}
suspend fun requestData2(): String {
    delay(1000) //1초 일시중단
    return "Data2"
}

// SupervisorJob() -> 자식 코루틴이 실패해도 다른자식 코루틴에는 영향을 미치지 않음
val scope = CoroutineScope(SupervisorJob())

suspend fun askMultipleForData01(): String {
    val defData1 = scope.async { requestData1() }
    val defData2 = scope.async { requestData2() }
    return select {
        defData1.onAwait { it }
        defData2.onAwait { it }
    }
}


suspend fun mains01(): Unit = coroutineScope {
    println(askMultipleForData01())
}
// (1초 후)
// Data2





// ...

// coroutineScope -> 자식이 모두 완료될때까지 기다림 -> 100초까지 완료될때까지 기다림
// select에 의해서 data2가 반환되는데, coroutineScope에 의해서 100초까지 기다린 후에, data2가 출력
suspend fun askMultipleForData02(): String = coroutineScope {
    select<String> {
        async { requestData1() }.onAwait { it }
        async { requestData2() }.onAwait { it }
    }
}

suspend fun mains02(): Unit = coroutineScope {
    println(askMultipleForData02())
}
// (100초 후)
// Data2




// also를 통해 다른 코루틴 취소가능
suspend fun askMultipleForData03(): String = coroutineScope {
    select<String> {
        async { requestData1() }.onAwait { it }
        async { requestData2() }.onAwait { it }
    }.also { coroutineContext.cancelChildren() }    // 먼저 완료된 작업의 결과가 반환되면, 나머지 아직 완료되지 않은 작업들을 취소 => 1초후 data2실행되고 100초 기다릴 필요없이 data1은 취소
}

suspend fun mains03(): Unit = coroutineScope {
    println(askMultipleForData03())
}
// (1초 후)
// Data2




// Splitties 라이브러리의 raceOf를 사용한 구현 방법입니다.
// select대신, raceOf 라이브러리로 좀 더 간결하게 표현
// 이 함수는 내부적으로 비동기 작업을 병렬로 실행하며, 두 작업 중 먼저 완료된 결과를 반환한 후 나머지 작업을 자동으로 취소하는 역할. 이는 select 함수와 유사한 동작 방식
suspend fun askMultipleForData04(): String = raceOf({
        requestData1()
    },{
        requestData2()
    })

suspend fun mains04(): Unit = coroutineScope {
    println(askMultipleForData04())
}
// (1초 후)
// Data2




// 이 함수는 무한히 주어진 문자열을 일정한 시간 간격으로 채널에 전송하는 코루틴을 생성
// produce함수로 데이터를 생산하고, send()를 통해 채널로 데이터 전송
suspend fun CoroutineScope.produceString(
    s: String,      // 전송할 문자열
    time: Long      // 지연 시간
) = produce {
    while (true) {  // 무한 루프
        delay(time) // 주어진 시간 동안 대기
        send(s)     // 채널에 문자열 전송
    }
}

fun mains05() = runBlocking {   // 모든 자식코루틴이 완료될때까지 대기
    val fooChannel = produceString("foo", 210L) // 0.21초마다 foo전송
    val barChannel = produceString("BAR", 500L) // 0.5초마다 BAR전송

    repeat(7) {
        select {
            fooChannel.onReceive {
                println("From fooChannel: $it")
            }
            barChannel.onReceive {
                println("From barChannel: $it")
            }
        }
    }
    coroutineContext.cancelChildren()   // 현재 코루틴 컨텍스트에서 실행 중인 모든 자식 코루틴을 취소
}
// From fooChannel: foo
// From fooChannel: foo
// From barChannel: BAR
// From fooChannel: foo
// From fooChannel: foo
// From barChannel: BAR
// From fooChannel: foo






fun main(): Unit = runBlocking {
    val c1 = Channel<Char>(capacity = 2)    // Char 값을 저장하는 용도의 용량 2인 채널 c1 => 용량이 2이기 때문에 한번에 최대2개의 값저장 가능
    val c2 = Channel<Char>(capacity = 2)    // Char 값을 저장하는 용도의 용량 2인 채널 c2


    // 값을 보냅니다.
    // 'A'부터 'H'까지의 문자를 0.4초 간격으로 전송
    launch { for (c in 'A'..'H') {  // 'A'부터 'H'까지 문자를 순차적으로 보냄
        delay(400)  // 0.4 지연
        select<Unit> {  // select 구문으로 어느 채널로 값을 보낼지 선택
            c1.onSend(c) { println("Sent $c to 1") }    // c1으로 보내는 경우
            c2.onSend(c) { println("Sent $c to 2") }    // c2으로 보내는 경우
        }
    }
}

    // 값을 받습니다.
    launch {
        while (true) {
            delay(1000)     // 1초 지연 후 값 수신
            val c = select<String> {    // select 구문으로 먼저오는값 수신
                c1.onReceive { "$it from 1" }   // c1에서 값을 받는 경우
                c2.onReceive { "$it from 2" }   // c2에서 값을 받는 경우
            }
            println("Received $c")  // 받은 값을 출력
        }
    }
}

// Sent A to 1
// Sent B to 1          // A,B는 c1에 전송
// Received A from 1    // 1초뒤에 c1에서 A받음
// Sent C to 1          // c1공간 1개 생겼으니깐 C를 c1에 전송
// Sent D to 2          // c1공간 없으니깐 D는 c2에 전송
// Received B from 1    // c1에서 B받음
// Sent E to 1          // c1공간 1개 생겼으니깐 E를 c1에 전송
// Sent F to 2          // c1공간 없으니깐 F는 c2에 전송
// Received C from 1    // ...이 과정 H까지 계속 반복...
// Sent G to 1
// Received E from 1
// Sent H to 1
// Received G from 1
// Received H from 1
// Received D from 2
// Received F from 2