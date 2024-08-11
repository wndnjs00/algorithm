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


fun main(): Unit = runBlocking (CoroutineName ("main")) {   // 부모
    val name = coroutineContext[CoroutineName]?.name
    println(name) // main

    launch {    // 자식       // 새로운 코루틴이 재정의되지 않았으므로, 자식은 부모의 Context상속
        delay(1000)
        val name = coroutineContext[CoroutineName]?.name
        println(name) // main
    }
}



// Job의 여러 수명주기를 보여주는 함수
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



// launch 코루틴빌더는 Job을 반환
fun main_002(): Unit = runBlocking {
    val job: kotlinx.coroutines.Job = launch {
        delay(1000)
        println("Test")
    }
}

// async는 Deferred<T>를 반환하는데, Deferred<T>는 Job인터페이스 구현 => Job을 통해 코루틴 상태추적 가능
fun main_002_1(): Unit = runBlocking {
    val deferred: Deferred<String> = async {
        delay(1000)
        "Test"  // 반환값 -> Defferd객체에 저장(await을 통해 호출가능)
    }
    val job: Job = deferred
}


// 확장 프로퍼티 (job을 좀더 접근하기 편하게 만들어주는 확장프로퍼티)
val CoroutineContext.job: kotlinx.coroutines.Job
    get() = get(Job) ?: error("Current context doesn't...")

//사용 예
fun main_003(): Unit = runBlocking {
    print(coroutineContext.job.isActive) // true
}



fun main_004(): Unit = runBlocking {
    val name = CoroutineName ("Some name")
    val job = Job() // Job생성(부모)       // Job => 코루틴 컨텍스트의 일부 / 코루틴의 샐명주기 관리

    launch (name + job){    // 코루틴 빌더가 생성될때마다 내부적으로 새로운자식 Job 생성!!
        val childName = coroutineContext[CoroutineName]
//        println(childName == name)  // true
        val childJob = coroutineContext[Job]    // Job생성(자식)
        println(childJob == job)    // false    // 부모Job != 자식Job
        println(childJob == job.children.first()) // true   // childJob은 job의 자식임
    }
}



// 부모Job != 자식Job
fun main_005(): Unit = runBlocking {    //Job생성(부모)
    val job : Job = launch {    // Job생성(자식)-runBlocking기준
    delay(1000)
}

    // coroutineContext.job을통해 현재코루틴의 Job추출 => runBlocking의 Job이됨
    val parentJob : Job = coroutineContext.job   // 또는 coroutineContext[Job]!!
    println(job == parentJob)   // false        // launch로 생성된 자식 Job != runBlocking으로 생성된 부모 Job
    val parentChildren: Sequence<Job> = parentJob.children      // 부모Job의 자식 전부
    println(parentChildren.first() == job)  // true     // 부모Job의자식중 첫번째자식 == launch로 생성된 Job
}



fun main_006(): Unit = runBlocking {    //Job생성(부모)
    launch(Job()) { // 코루틴이 자신만의 독자적인 Job()을 가지고있으면, 부모와 아무런 관계가없음 => runBlocking이 자식코루틴을 기다리지X
        delay(1000)
        println("will not be printed")
    }
}
// 아무것도 출력하지않고, 즉시 종료



// join => job이 끝날때까지 기다려줌
fun main_007(): Unit = runBlocking {
    val job1 = launch {
        delay(1000)
        println("Test1")
    }

    val job2 = launch {
        delay(2000)
        println("Test2")
    }

    job1.join() // job1이 끝날때까지 기다림
    job2.join() // job2가 끝날때까지 기다림
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

    val children = coroutineContext[Job]?.children  // runBlocking의 자식코루틴들을 가져옴

    val childrenNum = children?.count() //자식코루틴의 개수출력
    println("Number of children: $childrenNum")
    children?.forEach { it.join() } // join을 통해 자식코루틴이 완료될때까지 기다림
    println("All tests are done")
}
// Number of children: 2
// (1초 후)
// Text1
// (1초 후)
// Text2
// All tests are done



// 잘못된 함수
suspend fun main_009(): Unit = coroutineScope {
    val job = Job()
    launch(job) {       // 새로운 잡이 부모로부터 상속받은 잡을 대체
        delay(1000)
        println("Text 1")
    }
    launch(job) {       // 새로운 잡이 부모로부터 상속받은 잡을 대체
        delay(2000)
        println("Text 2")
    }
    job.join() // 여기서 영원히 대기 (job은 두자식 코루틴이 모두 종료되도 스스로 종료X) => 모든 자식코루틴에서 join호출해야함
    println("Will not be printed")
}
// (1초 후)
// Text1
// (1초 후)
// Text2
// (영원히 실행)


suspend fun main_10(): Unit = coroutineScope {
    val job = Job()
    launch(job) {           // 새로운 잡이 부모로부터 상속받은 잠을 대체합니다.
        delay(1000)
        println("Text 1")
    }

    launch(job) {           // 새로운 잡이 부모로부터 상속받은 집을 대체합니다.
        delay(2000)
        println("Text 2")
    }

    job.children.forEach { it.join() }  // 모든 자식에대해서 join()호출해서 job이 종료되도록함
}
// (1초 후)
// Text1
// (1초 후)
// Text2



fun main_11() = runBlocking {
    val job = Job() // 부모Job

    launch(job) {   // 자식1
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }

    launch {        // 자식2 (Job과 관련없는 자식)
        delay(500)
        job.complete()  // Job을 완료상태로 변경
    }

    job.join()  // job과 관련된 코루틴종료

    launch(job) {    // 자식3     // job이 이미 완료상태이므로 실행X
        println("will not be printed")
    }

    println("Done")
}
//Rep0
//Rep1
//Rep2
//Rep3
//Rep4
//Done



// 0.2초마다 호출되고, 0.5초후에 예외적으로 중단되기때문에 Rep0,Rep1까지만 출력
fun main_12() = runBlocking {
    val job = Job() // 부모Job

    launch(job) {   // 자식1
        repeat(5) { num ->
            delay(200)
            println("Rep$num")
        }
    }

    launch {        // 자식2 (Job과 관련없는 자식)
        delay (500)
        job.completeExceptionally(Error("Some error"))  // 예외적으로 job을 완료
    }

    job.join()      // job과 관련된 코루틴종료

    launch(job){    // 자식3     // job이 이미 완료상태이므로 실행X
        println("Will not be printed")
    }

    println("Done")
}
// Rep0
// Rep1
// Done



// 주로 complete함수는 잡의 마지막 코루틴을 시작한후 주로 사용
// 이후에는 join()을 사용해 잡이 완료되기까지 기다리기만하면됨
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

    job.complete()  // job을 완료상태로 변경
    job.join()      // job이 완료될때까지 기다림
}
// (1초 후)
// Text1
// (1초 후)
// Text2



// Job함수의 인자로 부모잡의 참조값 전달
// 부모Job이 취소되면 자식Job도 같이 취소
suspend fun main_15(): Unit = coroutineScope {
    val parentJob = Job()       // 부모Job
    val job = Job(parentJob)    // parentJob을 부모로하는 자식Job

    launch(job){    // job의 자식1
        delay (1000)    // 1초중단
        println("Text 1")
    }

    launch(job) {   // job의 자식2
        delay(2000)     // 2초중단
        println("Text 2")
    }

    delay (1100)        // 1.1초 중단
    parentJob.cancel()           // 취소(parentJob과 그 자식1,2모두 취소)   => 1.1초뒤에 중단되었기때문에 2번째 자식만 출력X
    job.children.forEach { it.join() }
}
// Text1