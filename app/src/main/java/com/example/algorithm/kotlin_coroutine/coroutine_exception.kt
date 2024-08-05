package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException

/* 10장 예외처리*/

// 예외발생시, 부모가 취소되면 자식도 같이 취소됨
fun main() : Unit = runBlocking {
    launch {        // 부모
        launch {    // 자식1
            delay(1000) // 1초동안 중단
            throw Error("Some error")   // 에러 발생 -> 모든 자식 코루틴 취소
        }

        launch {    // 자식2
            delay(2000)
            println("Will not be printed")
        }

        launch {    // 자식3
            delay(500)  // 0.5초 동안 중단
            println("Will be printed")  // Error발생 시점보다 먼저 실행되었으므로, 실행O
        }

        launch {    // 자식4
            delay(2000)
            println("Will not be printed")
        }
    }
}
// Will be printed
// 에러 발생



fun main01() : Unit = runBlocking {
    try {
        launch {
            delay(1000)
            throw Error("Some error")
        }
    } catch (e : Throwable) {
        println("Will not be printed")
    }

    launch {
        delay(2000)
        println("Will not be printed")
    }
}


fun main_2() : Unit = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    scope.launch {
        delay(1000)
        throw Error("Some error")
    }

    scope.launch {
        delay(2000)
        println("Will be printed")
    }

    delay(3000)
}


fun main03() : Unit = runBlocking {
    launch(SupervisorJob()) {
        launch {
            delay(1000)
            throw Error("Some error")
        }

        launch {
            delay(2000)
            println("Will not be printed")
        }
    }

    delay(3000)
}


fun main04() : Unit = runBlocking {
    val job = SupervisorJob()
    launch(job) {
        delay(1000)
        throw Error("Some error")
    }

    launch(job) {
        delay(2000)
        println("Will not be printed")
    }
    job.join()
}


fun main05() : Unit = runBlocking {
    supervisorScope {
        launch{
            delay(1000)
            throw Error("Some error")
        }

        launch{
            delay(2000)
            println("Will be printed")
        }
        delay(1000)
        println("Done")
    }
}


//suspend fun notifyAnalytics(actions : List<UserAction>)=
//    supervisorScope {
//        actions.forEach{action ->
//            launch {
//                notifyAnalytics(action)
//            }
//        }
//    }


//suspend fun sendNotifications(
//    notifications : List<Notification>
//) = withContext(SupervisorJob()){
//    for (notification in notifications){
//        launch {
//            client.send(notification)
//        }
//    }
//}


class MyException : Throwable()

suspend fun main06() = supervisorScope {
    val str1 = async<String> {
        delay (1000)
        throw MyException()
    }
    val str2 = async {
        delay(2000)
        "Text2"
    }
    try {
        println(str1.await())
    } catch (e: MyException){
        println(e)
    }
    println(str2.await())
}



object MyNonPropagatingException : CancellationException()

suspend fun main07(): Unit = coroutineScope {
    launch {      //1
        launch { // 2
            delay(2000)
            println("will not be printed")
        }
        throw MyNonPropagatingException // 3
    }
    launch { // 4
        delay(2000)
        println("Will be printed")
    }
}


fun main08(): Unit = runBlocking {
    val handler =
        CoroutineExceptionHandler { ctx, exception ->
            println("Caught Sexception")
        }
    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch{
        delay (1080)
        throw Error ("Some error")
    }
    scope.launch{
        delay (2080)
        println("Will be printed")
    }
    delay (3000)
}
