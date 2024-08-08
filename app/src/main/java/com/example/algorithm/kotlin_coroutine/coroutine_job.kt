package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

/* 8장 Job과 자식코루틴 기다리기*/

fun main(): Unit = runBlocking (CoroutineName ("main")) {
    val name = coroutineContext[CoroutineName]?.name
    println(name) // main
    launch {
        delay(1000)
        val name = coroutineContext[CoroutineName]?.name
        println(name) // main
    }
}


suspend fun main_01() = coroutineScope {
    // 빌더로 생성된 잡은
    val job = Job()
    println(job) // JobImpl{Active}@ADD
    // 에서드로 완료시킬 때까지 Active 상태입니다.
    job.complete()
    println(job) // JobImpl{Completed}@ADD

    // launch는 기본적으로 활성화되어 있습니다.
    val activeJob = launch {
        delay (1000)
    }
    println(activeJob) // StandaloneCoroutine{Active}@ADD
    // 여기서 집이 완료될 때까지 기다립니다.
    activeJob.join() // (11초후)
    println(activeJob) // StandaloneCoroutine{Completed}@ADD

    // launch는 New 상태로 지연 시작됩니다.
    val lazyJob = launch(start = CoroutineStart.LAZY) {
        delay(1000)
    }

    println(lazyJob)    // LazyStandaloneCoroutine{New}@ADD
    // Activie 상태가 되려면 시작하는 함수를 호출해야함
    lazyJob.start()
    println(lazyJob)    // LazyStandaloneCoroutine{Active}@ADD
    lazyJob.join()      // (1초후)
    println(lazyJob)    // LazyStandaloneCoroutine{Completed}@ADD
}


fun main_002(): Unit = runBlocking {
    val job: kotlinx.coroutines.Job = launch {
        delay(1000)
        println("Test")
    }
}


fun main_002_1(): Unit = runBlocking {
    val deferred: Deferred<String> = async {
        delay(1000)
        "Test"
    }
    val job: Job = deferred
}


// 확장 프로퍼티
val CoroutineContext.job: kotlinx.coroutines.Job
    get() = get(Job) ?: error("Current context doesn't...")

//사용 예
fun main_003(): Unit = runBlocking {
    print(coroutineContext.job.isActive) // true
}



fun main_004(): Unit = runBlocking {
    val name = CoroutineName ("Some name")
    val job = Job()

    launch (name + job){
        val childName = coroutineContext [CoroutineName]
        println(childName == name)  // true
        val childJob = coroutineContext[Job]
        println(childJob == job)    // false
        println(childJob == job.children.first()) // true
    }
}



fun main_005(): Unit = runBlocking {
    val job: Job = launch {
    delay(1000)
}

    val parentJob: Job = coroutineContext.job
    // 또는 coroutineContext [10b]!!
    println(job == parentJob) // false
    val parentChildren: Sequence<Job> = parentJob.children
    println(parentChildren.first() == job) // true
}


fun main_006(): Unit = runBlocking {
    launch(Job()) { // 새로운 잡이 부모로부터 상속받은 잡을 대체한다
        delay(1000)
        println("will not be printed")
    }
}


fun main_007(): Unit = runBlocking {
    val job1 = launch {
        delay(1000)
        println("Test1")
    }

    val job2 = launch {
        delay(2000)
        println("Test2")
    }

    job1.join()
    job2.join()
    println("All tests are done")
}
// (1초 후)
// Text1
// (1초 후)
// Text2
// All tests are done



fun main_008(): Unit = runBlocking {
    launch {
        delay (1000)
        println ("Test1")
    }

    launch {
        delay(2000)
        println("Test2")
    }

    val children = coroutineContext[Job]
            ?.children

    val childrenNum = children?.count()
    println("Number of children: $childrenNum")
    children?.forEach { it.join() }
    println("All tests are done")
}
// (1초 후)
// Text1
// (1초 후)
// Text2
// All tests are done



suspend fun main_009(): Unit = coroutineScope {
    val job = Job()
    launch(job) { // 새로운 장이 부모로부터 상속받은 잠을 대체합니다.
        delay(1000)
        println("Text 1")
    }
    launch(job) { // 새로운 집이 부모로부터 상속받은 잡을 대체합니다.
        delay(2000)
        println("Text 2")
    }
    job.join() // 여기서 영원히 대기하게 됩니다.
    println("Will not be printed")
}
// (1초 후)
// Text1
// (1초 후)
// Text2
// (영원히 실행)


suspend fun main_10(): Unit = coroutineScope {
    val job = Job()
    launch(job) {// 새로운 잡이 부모로부터 상속받은 잠을 대체합니다.
        delay(1000)
        println("Text 1")
    }

    launch(job) { // 새로운 잡이 부모로부터 상속받은 집을 대체합니다.
        delay(2000)
        println("Text 2")
    }

    job.children.forEach { it.join() }
}
// (1초 후)
// Text1
// (1초 후)
// Text2



fun main_11() = runBlocking {
    val job = Job()

    launch(job) {
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }

    launch {
        delay(500)
        job.complete()
    }

    job.join()

    launch(job) {
        println("will not be printed")
    }

    println("Done")
}





fun main_12() = runBlocking {
    val job = Job()

    launch(job) {
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }

    launch {
        delay(500)
        job.complete()
    }

    job.join()

    launch(job) {
        println("Will not be printed")
    }

    println("Done")
}
// Rep0
// Rep1
// Rep2
// Rep3
// Rep4




fun main_13() = runBlocking {
    val job = Job()

    launch(job) {
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }

    launch {
        delay (500)
        job.completeExceptionally(Error("Some error"))
    }

    job.join()

    launch(job){
        println("Will not be printed")
    }

    println("Done")
}
// Rep0
// Rep1
// Done



suspend fun main_14(): Unit = coroutineScope {
    val job = Job()
    launch(job) { // 새로운 잡이 부모로부터 상속받은 집을 대체합니다.
        delay(1000)
        println("Text 1")
    }
    launch(job) { // 새로운 잡이 부모로부터 상속받은 집을 대체합니다.
        delay(2000)
        println("Text 2")
    }

    job.complete()
    job.join()
}
// (1초 후)
// Text1
// (1초 후)
// Text2



suspend fun main_15(): Unit = coroutineScope {
    val parentJob = Job()
    val job = Job(parentJob)
    launch(job){
        delay (1000)
        println("Text 1")
    }

    launch(job) {
        delay(2000)
        println("Text 2")
    }
    delay (1100)
    parentJob.cancel()
    job.children.forEach { it.join() }
}
// Text1