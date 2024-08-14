package com.example.algorithm.kotlin_coroutine

import android.telecom.Call
import android.view.WindowInsetsAnimation
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.Response
import com.google.firebase.database.core.Repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

/* 9장 취소*/

// 0.2초마다 1000번 반복하여 print출력해서 시작하다가, 1.1초후에 코루틴 취소되는 함수
suspend fun mains_01(): Unit= coroutineScope {
    val job = launch {
        // 0.2초마다 print 1000번 반복해서 출력
        repeat(1_000) { i ->
            delay(200)
            println("Printing $i")
        }
    }
    delay(1100) // 1.1초간 일시중단(coroutineScope 일시중단 -> 그동안 launch코루틴 실행)
    job.cancel()         // launch코루틴 취소 (1.1초 실행되고 중단 -> 약5번실행되고 중단)
    job.join()
    println("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



// job.join 호출(X)
// 같은코드라도 join을 호출하지 않으면, 경쟁상태(실행순서에따라 프로그램 동작이 달라지는것)가 발생할 수 있음
// 즉, 같은코드라도 실행순서에따라 프로그램 동작이 달라질 수 있음
suspend fun main__01() = coroutineScope {
    val job = launch {
        repeat(1_000) { i ->
            delay (100)
            Thread.sleep(100) // 오래 걸리는 연산이라 가정
            println("Printing $i")
    }
}

    delay(1000)
    job.cancel()
    println("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Cancelled successfully
// Printing 4



// job.join호출(O)
// join을 호출하면, 코루틴이 취소가 완료될때까지 기다려주기때문에, 경쟁상태가 발생하지않음
// 즉. 실행순서에따라 프로그램동작이 달라지지 않음
suspend fun main__02() = coroutineScope{
    val job = launch {
        repeat(1_000) { i ->
            delay(100)
            Thread.sleep(100)  // 오래 걸리는 연산이라 가정
            println("Printing $i")
        }
    }

    delay (1000)
    job.cancel()
    job.join()
    println("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



// cancelAndJoin => cancel + join
// cancelAndJoin => 잡에 딸린 자식코루틴들을 한꺼번에 취소할때 자주사용
suspend fun main__03(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        repeat(1_000) { i ->
            delay(200)
            println("Printing $i")
        }
    }
    delay(1100)
    job.cancelAndJoin() // cancel + join (Job객체를 취소하고, 해당작업이 완료될때까지 기다림)
    println("Cancelled successfully")
}
// Printing 8
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



// 사용자가 뷰창을 나갔을때 뷰에서 시작된 모든 코루틴을 취소하는 경우
class ProfileViewModel : ViewModel() {
    private val scope =
        CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun onCreate() {
//        scope.launch { loadUserData() }
    }

    override fun onCleared(){   // onCleared -> viewModel이 더이상 사용되지 않을때 호출
        scope.coroutineContext.cancelChildren()     // ViewModel의 코루틴스코프에 속한 모든 자식코루틴 취소
    }
    //...
}


// 취소된 코루틴은 단지 멈추는것이 아니라, 이런식으로 내부적으로 예외를 사용해 취소되는것임!!
suspend fun main__04(): Unit = coroutineScope { //부모
    val job = Job() //부모Job
    launch(job) {   //자식Job
        try {
            repeat(1_000) { i ->
                delay(200)
                println("Printing $i")
            }
        } catch (e: CancellationException) {
            println(e)
            throw e
        }
    }
    delay(1100) // 1.1초간 일시중단(coroutineScope 일시중단 -> 그동안 launch코루틴 실행)
    job.cancelAndJoin()  // 취소됐으니깐 catch문으로 이동
    println("Cancelled successfully")
    delay(1000)
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// JobCancellationException...
// Cancelled successfully




// 코루틴이 취소되더라도, finally는 반드시 실행!!
// finally문은 코루틴이 정상적으로 종료되지 않더라도, 자원을 해제하거나 정리하는 작업을 해야될때 매우유용함
suspend fun main__05(): Unit = coroutineScope { //부모
    val job = Job() //부모Job
    launch(job) {   //자식Job
        try {
            delay(Random.nextLong(2000))    // 0~2초 사이의 랜덤시간동안 중단
            println("Done")
        } finally { // 코루틴이 정상적으로 완료되든 취소되는 finally블록은 무조건 실행
            print("Will always be printed")
        }
    }
    delay(1100) // 1.1초 중단
    job.cancelAndJoin()
}
// Will always be printed (또는) Done
// will always be printed


// cancel상태중에 중단하려고하면, CancellationException 발생!
suspend fun main__06(): Unit = coroutineScope { //부모
    val job = Job() //부모Job
    launch(job) {   //자식Job
        try {
            delay(2000)
            println("Job is done")
        } finally {     //finally블록은 항상 실행
            println("Finally")

            launch { // 무시됨   // 1초뒤에 바로 취소되었으므로 이 코루틴은 무시
                println("Will not be printed")
            }
            delay(1000) // cancel상태중에 중단하려했기때문에  // 여기서 예외가 발생
            println("Will not be printed")
        }
    }
    delay(1000)
    job.cancelAndJoin()
    println("Cancel done")
}
// (1초 후)
// Finally
// Cancel done


// 코루틴이 이미 취소되었을때, 중단함수를 반드시 호출해야하는 경우 => withContext(NonCancellable) 사용
suspend fun main__07(): Unit = coroutineScope{
    val job = Job()

    launch(job) {
        try{
            delay(200)  // 0.1초뒤에 job이 취소되기때문에 실행X
            println("Coroutine finished")
        } finally {
            println("Finally")

            withContext(NonCancellable) {  // withContext(NonCancellable)블록 내부에서는 중단함수 호출해도 취소X
                delay(1000L)    // 1초간 중단(중단함수 취소X)
                println("Cleanup done")
            }
        }
    }
    delay(100)
    job.cancelAndJoin()
    println("Done")
}
// Finally
// Cleanup done
// Done



suspend fun main__08(): Unit = coroutineScope {
    val job = launch {  // 0.4초뒤에 취소가 발생하므로 실행X
        delay(1000)
    }

    // job.invokeOnCompletion => 코루틴이 완료되었을때 호출
    job.invokeOnCompletion { exception: Throwable? ->   // 코루틴이 완료될때 발생한 예외를 전달 / 예외가 없으면 null전달
        println("Finished")
    }
    delay(400)
    job.cancelAndJoin()
}
// Finished



suspend fun main__09(): Unit = coroutineScope {
    val job = launch {
        delay(Random.nextLong(2400))    // 0~2.4초의 무작위 시간동안 랜덤중단
        println("Finished")
    }
    delay(800)     // coroutineScope 0.8초간 중단(이동안 job코루틴은 무작위시간동안 대기중)

    // job이 완료되었을때 호출될 콜백 등록
    job.invokeOnCompletion { exception: Throwable? ->
        println("will always be printed")
        println("The exception was: $exception")
    }
    delay(800)  // coroutineScope 다시 0.8초간 중단(이동안 job코루틴은 이미완료됐을수도있고 아닐수도 있음)
    job.cancelAndJoin()  // job취소 -> 이때 invokeOnCompletion콜백 호출
}
// will always be printed
// The exception was:
// kotlinx.coroutines.JobCancellationException (또는) Finished
// Will always be printed
// The exception was null



// 코루틴 내부에 중단점이 없기때문에 취소할수X
// Thread.sleep(200)은 코루틴을 일시중단시키는것이 아니라 현재스레드를 0.2초동안 블로킹하는것 -> 비동기적으로 일시중단하는 코루틴의 장점을 무효화
suspend fun main__10(): Unit = coroutineScope{
val job = Job()
launch(job) {
    repeat(1_000) { i ->
        Thread.sleep(200)  // 0.2초동안 현재스레드를 블로킹(다른코루틴 동시실행X)    // 여기서 복잡한 연산이나 파일을 읽는 등의 작업이 있다고 가정
        println("Printing $i")
    }
}
    delay (1000)    // 메인코루틴(coroutineScope)이 1초동안 중단(launch코루틴은 계속 실행중)
    job.cancelAndJoin()      // Thread.sleep()으로 인해 즉시 취소되지않고, Thread.sleep()이 끝날때까지 취소지연
    println("Cancelled successfully")
    delay (1000)
}
// Printing 0
// Printing 1
// Printing 2
// ... (1000까지)



// yield()는 코루틴을 중단하고 즉시 재실행하는 역할!!
// 중단점이 생겼기때문에 블로킹되도 취소 가능!
suspend fun main__11(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        repeat(1_000) { i ->
            Thread.sleep(200)   // 0.2초동안 현재스레드를 블로킹(다른코루틴 동시실행X)
            yield()                   // 코루틴 중간하고 재실행하는 역할! (중단점 생겼기때문에 취소가능)
            println("Printing $i")
        }
    }
    delay(1100)     // 메인코루틴(coroutineScope)이 1.1초동안 중단(launch코루틴은 계속 실행중)
    job.cancelAndJoin()      // 취소
    println("Cancelled successfully")
    delay(1000)
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



// isActive를 통해 잡이 Active한지 확인가능, Active하지 않으면 연산중단
suspend fun main__12(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        do {    // do~while문을 사용해서 코루틴이 isActive상태일동안 반복작업수행
            Thread.sleep(200)   //0.2초단 블로킹(다른코루틴 동시실행X)
            println("Printing")
        } while (isActive)
    }
    delay(1100) // 메인코루틴 1.1초간 중단
    job.cancelAndJoin()  // 1.1초후에 취소 -> isActive false로 설정
    println("Cancelled successfully")
}
// Printing
// Printing
// Printing
// Printing
// Printing
// Printing
// Cancelled successfully



// ensureActive
suspend fun main__13(): Unit = coroutineScope {
    val job = Job()
    launch (job) {
        repeat(1000) { num ->
            Thread.sleep(200)   //0.2초단 블로킹(다른코루틴 동시실행X)
            ensureActive()      // 코루틴이 활성상태인지 확인 //코루틴이 취소되었을경우,CancellationException을 던져 코루틴중단
            println("Printing Snum")
        }
    }
    delay(1100)
    job.cancelAndJoin() //1.1초뒤에 취소 => ensureActive가 예외를 던져 repeat 반복문중단
    println ("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



// suspendCancellableCoroutine을 사용해, 네트워크 요청을 코루틴내에서 사용가능한 suspend함수로 래핑
//suspend fun getOrganizationRepos(
//    organization: String
//): List<Repo> =
//    // suspendCancellableCoroutine => 코루틴 일시중단, 나중에 코루틴을 재개할 수 있는 콜백함수 제공 => 따라서 비동기 API를 코루틴에서 쉽게 사용할 수 있음
//    // suspendCancellableCoroutine를 통해 코루틴을 일시중단시키고, 네트워크 요청이 완료되면 코루틴 재개
//    suspendCancellableCoroutine { continuation ->
//        val orgReposCall = apiService
//            .getOrganizationRepos(organization)
//
//        // 비동기적으로 API요청 수행, 응답을 콜백으로 처리(네트워크 요청이 완료되면 해당 콜백호출)
//        orgReposCall.enqueue(object : WindowInsetsAnimation.Callback<List<Repo>> {
//
//            // API요청이 성공적으로 완료되었을때 호출되는 메서드
//            override fun onResponse(
//                call: Call<List<Repo>>,
//                response: Response<List<Repo>>
//            ) {
//                if (response.isSuccessful) {    // 응답 성공 -> 결과반환
//                    val body = response.body()
//                    if (body != null) {
//                        continuation.resume(body)
//                    } else {
//                        continuation.resumeWithException(
//                            ResponseWithEmptyBody
//                        )
//                    }
//                } else {    // 응답실패
//                    continuation.resumeWithException(   //코루틴을 예외를 띄우면서 종료
//                        ApiException(
//                            response.code(),
//                            response.message()
//                        )
//                    )
//                }
//            }
//
//            // API요청이 실패했을때 호출되는 메서드
//            override fun onFailure(
//                call: Call<List<Repo>>,
//                t: Throwable
//            ) {
//                continuation.resumeWithException(t) //코루틴을 예외를 띄우면서 종료
//            }
//        })
//        // 코루틴이 취소될때 네트워크 요청도 같이 취소되도록
//        continuation.invokeOnCancellation {
//            orgReposCall.cancel()
//        }
//    }




// Retrofit도 이제 중단함수 지원!
//class GithubApi{
//    @GET("orgs/{organization}/repos?per_page=100")
//    suspend fun getOrganizationRepos1(
//        @Path("organization") organization : String
//    ):List<Repo>
//}