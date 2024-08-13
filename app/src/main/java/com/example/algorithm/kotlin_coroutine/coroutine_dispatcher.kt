package com.example.algorithm.kotlin_coroutine

import android.service.autofill.UserData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/* 12장 디스페처 */

suspend fun main_1() = coroutineScope {
    repeat(1000) {
        launch {    // 또는 launch(Dispatchers.Default) {
            // 바쁘게 만들기 위해 실행합니다.
            List(1000) { Random.nextLong() }.maxOrNull()

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}
//Running on thread: DefaultDispatcher-worker-1
//Running on thread: DefaultDispatcher-worker-5
//Running on thread: DefaultDispatcher-worker-7
//Running on thread: DefaultDispatcher-worker-6
//Running on thread: DefaultDispatcher-worker-11
//Running on thread: DefaultDispatcher-worker-2
//Running on thread: DefaultDispatcher-worker-10
//Running on thread: DefaultDispatcher-worker-4


private val dispatcher = Dispatchers.Default
    .limitedParallelism(5)



class SomeTest {

    private val dispatcher = Executors
        .newSingleThreadExecutor()
        .asCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        // 메인 디스패처를
        // 원래의 Main 디스패처로 되돌립니다.
        Dispatchers.resetMain()
        dispatcher.close()
    }

    @Test
    fun testSomeUI() = runBlocking {
        launch(Dispatchers.Main) {
            // ..
        }
    }
}



suspend fun mains_02() {
    val time = measureTimeMillis{
        coroutineScope{
            repeat (50){
                launch (Dispatchers.IO){
                    Thread.sleep(1000)
                }
            }
        }
    }
    println(time) // ~1000
}



suspend fun mains_03() = coroutineScope {
    repeat(1000) {
        launch(Dispatchers.IO) {
            Thread.sleep(200)

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}
// Running on thread: DefaultDispatcher-worker-1
// ...
// Running on thread: DefaultDispatcher-worker-53
// Running on thread: DefaultDispatcher-worker-14



suspend fun mains_04(): Unit = coroutineScope {
    launch(Dispatchers.Default) {
        println(Thread.currentThread().name)
        withContext(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
    }
}
//DefaultDispatcher-worker-2
//DefaultDispatcher-worker-2



class DiscUserRepository(
    private val discReader: DiscReader
) : UserRepository {
    override suspend fun getUser(): UserData =
        withContext(Dispatchers.IO) {
            UserData(discReader.read("userName"))
        }
}


suspend fun mains_05(): Unit = coroutineScope {
    launch {
        printCoroutinesTime(Dispatchers.IO) // Dispatchers. IO took: 2074
    }
    launch {
        val dispatcher = Dispatchers.IO
            .limitedParallelism(100)
        printCoroutinesTime(dispatcher) // LimitedDispatcher@xX took: 1082
    }
}


suspend fun printCoroutinesTime(
    dispatcher: CoroutineDispatcher
) {
    val test = measureTimeMillis {
        coroutineScope {
            repeat(100){
            launch(dispatcher) {
                Thread.sleep(1000)
            }
        }
    }
}
        println("$dispatcher took: $test")
}



// 개수 제한이 없는 스레드 품을 사용하는 디스패처
//private val pool = ...
//
//Dispatchers.IO = pool.limitedParallelism(64)
//Dispatchers.IO.limitedParallelism(x) = pool.limitedParallelism(x)



class DiscUserRepository(
    private val discReader: DiscReader
) : UserRepository {
    private val dispatcher = Dispatchers.IO
        .limitParallelism(5)

    override suspend fun getUser(): UserData =
        withContext(dispatcher) {
            UserData(discReader.read("userName"))
        }
}


val NUMBER_OF_THREADS = 20
val dispatcher = Executors
    .newFixedThreadPool(NUMBER_OF_THREADS)
    .asCoroutineDispatcher()



var i = 0

suspend fun mains_06(): Unit = coroutineScope {
    repeat (10_000) {
        launch(Dispatchers.IO) { // 또는 Default 디스패처
            i++
        }
    }
        delay(1000)
        println(i) // ~9930
    }


val dispatcher = Executors.newSingleThreadExecutor()
    .asCoroutineDispatcher()
// 이전 방식은 다음과 같습니다.
// val dispatcher = newSingleThreadContext ("My name")



var i = 0

suspend fun mains_07(): Unit = coroutineScope {
    val dispatcher = Dispatchers.Default
        .limitedParallelism(1)

    repeat(10000) {
        launch(dispatcher) {
            i++
        }
    }
    delay(1000)
    println(i) // 10000
}



suspend fun mains_08(): Unit = coroutineScope {
    val dispatcher = Dispatchers.Default
        .limitedParallelism(1)

    val job = Job()
    repeat (5) {
        launch(dispatcher + job) {
            Thread.sleep(1000)
        }
    }
        job. complete()
        val time = measureTimeMillis { job.join() }
        println("Took $time") // 500ms가 걸립니다.
    }



val LoomDispatcher = Executors
    .newVirtualThreadPerTaskExecutor()
    .asCoroutineDispatcher()

// ExecutorCoroutineDispatcher를 구현하는 객체를 만들 수도 있습니다.

object LoomDispatcher : ExecutorCoroutineDispatcher() {

    override val executor: Executor = Executor { command ->
        Thread.startVirtualThread(command)
    }

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        executor.execute(block)
    }

    override fun close() {
        error("Cannot be invoked on Dispatchers.LOOM")
    }
}



suspend fun mains_09() = measureTimeMillis {
    coroutineScope {
        repeat(100_000) {
            launch(Dispatchers.Loom) {
                Thread.sleep(1000)
            }
        }
    }
}. let (::println) // 2 273



suspend fun mains_10() = measureTimeMillis {
    val dispatcher = Dispatchers.IO
        .limitedParallelism(100_000)
    coroutineScope {
        repeat(100_000) {
            launch(dispatcher) {
                Thread.sleep(1000)
            }
        }
    }
}.let(::println) // 23 803



suspend fun mains_11(): Unit =
    withContext (newSingleThreadContext ("Thread1")) {
        var continuation: Continuation<Unit>? = null

        launch(newSingleThreadContext("Thread2")) {
            delay(1000)
            continuation?.resume(Unit)
        }
        launch(Dispatchers.Unconfined) {
            println(Thread.currentThread().name) // Threadl

            suspendCancellableCoroutine<Unit> {
                continuation = it
            }

            println(Thread.currentThread().name) // Thread2
            delay(1008)

            println(Thread.currentThread().name)
            // kotlinx.coroutines.DefaultExecutor
            // (delay가 사용된 스레드입니다.)
        }
    }



suspend fun shoUser(user: User) =
    withContext (Dispatchers.Main) {
        userNameElement.text = user.name
        // ...
    }


suspend fun shoUser(user: User) =
    withContext (Dispatchers.Main.immediate){
        userNameElement.text = user.name
    //...
}




public interface ContinuationInterceptor :
    CoroutineContext.Element {

    companion object Key :
        CoroutineContext.Key<ContinuationInterceptor>

    fun <T> interceptContinuation(
        continuation: Continuation<T>
    ): Continuation<T>

    fun releaseInterceptedContinuation(
        continuation: Continuation<*>
    ){

    }
// ...
}


class DiscUserRepository(
    private val discReader: DiscReader,
    private val dispatcher: CoroutineContext = Dispatchers.IO,
) : UserRepository {
    override suspend fun getUser(): UserData =
        withContext(dispatcher) {
            UserData(discReader.read("userName"))
        }
}
class UserReaderTests {
    @Test
    fun 'some test'() = runTest {
        // 테스트 준비 과정
        val discReader = FakeDiscReader()
        val repo = DiscUserRepository(
            discReader,
            // 테스트를 수행할 코루틴 중 하나
            this.coroutineContext[ContinuationInterceptor]!!
        )
        //...
    }
}



fun cpu(order: Order): Coffee{
    var i = Int.MAX_VALUE
    while (i > 0) {
        i -= if (i % 2 == 0) 1 else 2
    }
    return Coffee(order.copy(customer = order.customer + i))
}

    fun memory(order: Order): Coffee {
        val list = List(1_000) { it }
        val list2 = List(1_000) { list }
        val list3 = List(1_000) { list2 }
        return Coffee(
            order.copy(
                customer = order.customer + list3.hashCode()
            )
        )
    }



fun blocking(order: Order): Coffee {
    Thread.sleep(1000)
    return Coffee(order)
}

suspend fun suspending(order: Order): Coffee {
    delay(1000)
    return Coffee(order)
}