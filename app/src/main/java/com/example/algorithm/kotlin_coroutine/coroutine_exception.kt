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

// runBlocking => runBlocking함수 안의 모든 코루틴이 완료될때까지 스레드 차단시켜줌
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
// 에러 발생(Some error)



// 이렇게 사용하지 XXX
// 코루틴 빌더 내부에서(runBlocking내부에서) 새로운 코루틴빌더를(launch빌더를) try-catch문을 통해 랩핑하는건 아무의미X
fun main01() : Unit = runBlocking {
    // try-catch 구문으로 써봤자 여기선 무시됨
    try {
        launch {
            delay(1000)
            throw Error("Some error")
        }
    } catch (e : Throwable) {   // 예외 처리가 내부에서 발생한 예외를 잡도록 구성되어 있지 않기 때문에(코루틴 컨텍스트에 의해 처리되기 때문에), 실행X
        println("Will not be printed")
    }

    launch {
        delay(2000)
        println("Will be printed")
    }
}
// 에러 발생 (Some error)
// (2초후)
// Will be printed



// 위의 함수 수정
// 코루틴 빌더 안에(launch빌더 안에) try-catch문
fun main01_modify() : Unit = runBlocking {
    launch {
        try {
            delay(1000)
            throw Error("Some error")
        } catch (e: Throwable) {
            println("Error caught: ${e.message}")
        }
    }

    launch {
        delay(2000)
        println("This will be printed")
    }
}
// Error caught: Some error
// (2초후)
// This will be printed



// SupervisorJob => 나머지 자식에게는 영향 미치지X (독립적으로 취소)
fun main_2() : Unit = runBlocking {     // runBlocking -> 최상위부모(그치만 코루틴의 수명주기를 직접관리하지는X)
    val scope = CoroutineScope(SupervisorJob())  // 부모 (얘가 직접적인 부모역할함)     //CoroutineScope => 코루틴의 라이프사이클을 정의하여, 코루틴이 시작되고 종료되는 시점을 관리(구조화된 동시성을 관리하는데 도움)
    scope.launch {  //자식1
        delay(1000)
        throw Error("Some error")       //취소O
    }

    scope.launch {  //자식2
        delay(2000)
        println("Will be printed")     //취소X
    }

    delay(3000)     // 코루틴 종료되지 않도록(코루틴 전부 실행되는걸 확인하기위해) 3초단 중단시킴
}
//(1초 중단)
// Some error
//(2초 중단)
// Will be printed



// 이렇게 사용하지 XXX
// 여기서 SupervisorJob은 단 하나의 자식만 가지기때문에, 예외처리에 도움안됨
fun main03() : Unit = runBlocking {
    launch(SupervisorJob()) {   //부모    // SupervisorJob이 컨텍스트 외부예외처리를 제대로 못해서, 부모 코루틴 취소
        launch {    // 자식1
            delay(1000)
            throw Error("Some error")
        }

        launch {
            delay(2000)
            println("Will not be printed")  // 부모코루틴 취소되었기때문에, 아무것도 출력되지 않음
        }
    }

    delay(3000)
}



fun main04() : Unit = runBlocking {
    val job = SupervisorJob()   //부모
    launch(job) {   //자식1
        delay(1000)
        throw Error("Some error")
    }

    launch(job) {   //자식2
        delay(2000)
        println("Will be printed")
    }
    job.join()  // runBlocking모든 자식이(자식1,2가) SupervisorJob완료할때까지 코루틴을 일시 중단
}
// (1초후)
// Some error
// (1초후)
// Will be printed



// 코루틴 빌더를 supervisorScope로 래핑해서도 사용가능!
fun main05() : Unit = runBlocking {
    supervisorScope {   //부모
        launch{ //자식1
            delay(1000)
            throw Error("Some error")
        }

        launch{ //자식2
            delay(2000)
            println("Will be printed")
        }
        delay(1000)
        println("Done")
    }
}
// Some error
// Will be printed
// (1초후)
// Done



//suspend fun notifyAnalytics(actions : List<UserAction>)=
//    supervisorScope {     // supervisorScope에서 동작하기 때문에 예외발생시, 다른 자식에게 영향X
//        actions.forEach{action ->
//            launch {
//                notifyAnalytics(action)   // notifyAnalytics함수를 호출함으로써, 함수를 재귀적으로 호출
//            }
//        }
//    }



// 이렇게 사용하지 XXX

//suspend fun sendNotifications(notifications : List<Notification>)
// = withContext(SupervisorJob()){      // SupervisorJob은 코루틴범위내에서 사용해야함, 이렇게 직접 사용하면 안됨
                                        // withContext => 일반적으로 특정 코드 블록 내에서 코루틴 컨텍스트를 일시적으로 변경하는데 사용
//    for (notification in notifications){
//        launch {
//            client.send(notification)
//        }
//    }
//}


// 위의 코드 이렇게 수정가능
//suspend fun sendNotifications(notifications: List<Notification>) = coroutineScope {
//    for (notification in notifications) {
//        launch {
//            client.send(notification)
//        }
//    }
//}



// 수정2
//fun sendNotifications(notifications: List<Notification>) = runBlocking {
//    val supervisorJob = SupervisorJob()
//    val scope = CoroutineScope(Dispatchers.Default + supervisorJob)
//
//    for (notification in notifications) {
//        scope.launch {
//            client.send(notification)
//        }
//    }
//    supervisorJob.join()  // 자식코루틴이 끝날때까지 기다림
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
        println(str1.await())   // 어기선 어짜피 str1은 예외를 던졌기때문에 출력X
    } catch (e: MyException){
        println(e)              // 여기로 넘어오는데, 예외를 프린트하라했으므로 MyException출력
    }
    println(str2.await())       // supervisorScope스코프 안에있으므로 str2는 영향 안받음(출력O)
}
// MyException
// Text2



// 예외가 CancellationException의 서브클래스면 부모로 전파시키지않고, 현재 코루틴을 취소
object MyNonPropagatingException : CancellationException()

suspend fun main07(): Unit = coroutineScope {   // 최상위 부모
    launch {        // 1   //부모
        launch {    // 2   //자식(1의 자식)
            delay(2000)
            println("will not be printed")
        }
        throw MyNonPropagatingException //3     //코루틴1이 예외를 던짐 => CancellationException이므로 1(자기자신)과 2(자식)이 취소됨 / 다른 코루틴에 전파하지는 않음
    }

    launch { // 4   //1의 형제
        delay(2000)
        println("Will be printed")  // 얘는 영향받지않고 출력됨
    }
}
// (2초후)
// Will be printed




// CoroutineExceptionHandler 사용 -> 예외를 중단시키지는 않지만, 포착되지않은 예외를 처리하는데 사용
fun main08(): Unit = runBlocking {
    val handler =
        CoroutineExceptionHandler { ctx, exception ->
            println("Caught : $exception")
        }
    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch{
        delay (1000)
        throw Error ("Some error")  //취소, handler실행 -> Caught : Some error 출력
    }
    scope.launch{
        delay (2000)
        println("Will be printed")  //출력
    }
    delay (3000)    // 코루틴이 완료될때까지 기다림
}
// Caught : Some error 출력
// Will be printed
