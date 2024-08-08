package com.example.algorithm.kotlin_coroutine

import android.telecom.Call
import android.view.WindowInsetsAnimation
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
suspend fun main(): Unit= coroutineScope {
    val job = launch {
        repeat(1_000) { i ->
            delay(200)
            println("Printing Si")
        }
    }
    delay(1100)
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




suspend fun main__01() = coroutineScope {
    val job = launch {
        repeat(1_000) { i ->
            delay (100)
            Thread.sleep(100) // 오래 걸리는 연산이라 가정합니다.
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




suspend fun main__02() = coroutineScope{
    val job = launch {
        repeat(1_000) { i ->
            delay(100)
            Thread.sleep(100)  // 오래 걸리는 연산이라 가정합니다.
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



suspend fun main__03(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        repeat(1_000) { i ->
            delay(200)
            println("Printing $i")
        }
    }

    delay(1100)
    job.cancelAndJoin()
    println("Cancelled successfully")
}
// Printing 8
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



class ProfileViewModel : ViewModel() {
    private val scope =
        CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun onCreate() {
        scope.launch { loadUserData() }
    }

    override fun onCleared(){
            scope.coroutineContext.cancelChildren()
    }
    //...
}



suspend fun main__04(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
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
    delay(1100)
    job.cancelAndJoin()
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



suspend fun main__05(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            delay(Random.nextLong(2000))
            println("Done")
        } finally {
            print("Will always be printed")
        }
    }
    delay(1100)
    job.cancelAndJoin()
}
// Will always be printed
// (또는)
// Done
// will always be printed



suspend fun main__06(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        try {
            delay(2000)
            println("Job is done")
        } finally {
            println("Finally")
            launch { // 우시됩니다.
                println("Will not be printed")
            }
            delay(1000) // 여기서 예외가 발생합니다
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



suspend fun main__07(): Unit = coroutineScope{
    val job = Job()
    launch(job) {
        try{
            delay(200)
            println("Coroutine finished")
        } finally {
            println("Finally")
            withContext (NonCancellable) {
                delay (1000L)
                println("Cleanup done")
            }
        }
    }
    delay(100)
    job.cancelAndJoin
    println("Done")
}
// Finally
// Cleanup done
// Done



suspend fun main__08(): Unit = coroutineScope {
    val job = launch {
        delay(1000)
    }
    job.invokeOnCompletion { exception: Throwable? ->
        println("Fanished")
    }
    delay(400)
    job.cancelAndJoin()
}
// Finished



suspend fun main__09(): Unit = coroutineScope {
    val job = launch {
        delay(Random.nextLong(2400))
        println("Finished")
    }
    delay(800)
    job.invokeOnCompletion { exception: Throwable? ->
        println("will always be printed")
        println("The exception was: $exception")
    }
    delay(800)
    job.cancelAndJoin()
}
// will always be printed
// The exception was:
// kotlinx.coroutines.JobCancellationException
// (또는)
// Finished
// Will always be printed
// The exception was null



suspend fun main__10(): Unit = coroutineScope{
val job = Job()
launch(job) {
    repeat(1_000) { i ->
        Thread.sleep(208) // 여기서 복잡한 연산이나 파일을 읽는 등의 작업이 있다고 가정
        println("Printing $i")
    }
}
    delay (1000)
    job.cancelAndJoin()
    println("Cancelled successfully")
    delay (1000)
}
// Printing 0
// Printing 1
// Printing 2
// ... (1800까지)



suspend fun main__11(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        repeat(1_000) { i ->
            Thread.sleep(200)
            yield()
            println("Printing $i")
        }
    }
    delay(1100)
    job.cancelAndJoin()
    println("Cancelled successfully")
    delay(1000)
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully



//suspend fun cpuintensiveOperations() =
//    withContext (Dispatchers.Default) {
//        cpuIntensiveOperation1()
//        yield()
//        cpuintensiveOperation2()
//        yield()
//        cpuintensiveOperation3()
//    }


suspend fun main__12(): Unit = coroutineScope {
    val job = Job()
    launch(job) {
        do {
            Thread.sleep(200)
            println("Printing")
        } while (isActive)
    }
    delay(1100)
    job.cancelAndJoin()
    println("Cancelled successfully")
}
// Printing
// Printing
// Printing
// Printing
// Printing
// Printing
// Cancelled successfully




suspend fun main__13(): Unit = coroutineScope {
    val job = Job()
    launch (job) {
        repeat(1000) { num ->
            Thread.sleep(200)
            ensureActive()
            println("Printing Snum")
        }
    }
    delay(1100)
    job.cancelAndJoin()
    println ("Cancelled successfully")
}
// Printing 0
// Printing 1
// Printing 2
// Printing 3
// Printing 4
// Cancelled successfully




//suspend fun getOrganizationRepos(
//    organization: String
//): List<Repo> =
//    suspendCancellableCoroutine { continuation ->
//        val orgReposCall = apiService
//            .getOrganizationRepos(organization)
//        orgReposCall.enqueue(object : WindowInsetsAnimation.Callback<List<Repo>> {
//            override fun onResponsel(
//                call: Call<List<Repo>>,
//                response: Response<List<Repo>>
//            ) {
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    if (body != null) {
//                        continuation.resume(body)
//                    } else {
//                        continuation.resumeWithException(
//                            ResponseWithEmptyBody
//                        )
//                    }
//                } else {
//                    continuation.resumeWithException(
//                        ApiException(
//                            response.code(),
//                            response.message()
//                        )
//                    )
//                }
//            }
//
//            override fun onFailure(
//                call: Call<List<Repo>>,
//                t: Throwable
//            ) {
//                continuation.resumeWithException(t)
//            }
//        })
//        continuation.invokeOnCancellation {
//            orgReposCall.cancel()
//        }
//    }