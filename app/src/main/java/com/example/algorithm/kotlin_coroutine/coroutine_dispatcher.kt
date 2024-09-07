package com.example.algorithm.kotlin_coroutine

import com.example.algorithm.kotlin_coroutine.data.Coffee
import com.example.algorithm.kotlin_coroutine.data.DiscReader
import com.example.algorithm.kotlin_coroutine.data.Order
import com.example.algorithm.kotlin_coroutine.data.UserData
import com.example.algorithm.kotlin_coroutine.data.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/* 12장 디스페처 */


// 1000개의 코루틴을 동시에 실행하여, 각 코루틴이 랜덤숫자리스트를 생성하여 최대값을 찾은뒤, 실행중인 스레드의 이름을 출력하는 함수
// Dispatchers.Default -> 컴퓨터의 CPU개수와 동일한수의 스레드풀을 가짐
// MakBook Air M1 -> 8코어 CPU -> 풀의 스레드수 8개

/* 참고 - runBlocking은 디스패처 기본설정값이 Dispatchers.Default가 아니기때문에, coroutineScope대신 runBlocking을 사용하면 모든코루틴은 main에서 실행됨 */

suspend fun main() = coroutineScope { //coroutineScope-> 자식코루틴이 완료될때까지 기다림
    repeat(1000) {
        launch {    // 또는 launch(Dispatchers.Default) {
            List(1000) { Random.nextLong() }.maxOrNull()    //1000개의 무작위 Long값을 생성한뒤 리스트에 넣고, 리스트에서 가장큰값을 찾아서 반환(리스트비어있을경우 null값 반환)

            val threadName = Thread.currentThread().name    // 현재 실행중인 스레드의 이름을 반환(코루틴은 여러스레드에서 실행될수있으므로)
            println("Running on thread: $threadName")
        }
    }
}
//Running on thread: DefaultDispatcher-worker-8
//Running on thread: DefaultDispatcher-worker-2
//Running on thread: DefaultDispatcher-worker-6
//Running on thread: DefaultDispatcher-worker-5
//Running on thread: DefaultDispatcher-worker-1
//Running on thread: DefaultDispatcher-worker-3
//Running on thread: DefaultDispatcher-worker-7
//Running on thread: DefaultDispatcher-worker-4
//Running on thread: DefaultDispatcher-worker-7
//...


// MakBook Air M1 -> 8코어 CPU -> 풀의 스레드수 8개
suspend fun mains_001() = coroutineScope {
    repeat(8) {
        launch {    // 또는 launch(Dispatchers.Default) {
            List(8) { Random.nextLong() }.maxOrNull()

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}
//Running on thread: DefaultDispatcher-worker-1
//Running on thread: DefaultDispatcher-worker-5
//Running on thread: DefaultDispatcher-worker-4
//Running on thread: DefaultDispatcher-worker-3
//Running on thread: DefaultDispatcher-worker-6
//Running on thread: DefaultDispatcher-worker-7
//Running on thread: DefaultDispatcher-worker-2
//Running on thread: DefaultDispatcher-worker-8




private val dispatchers = Dispatchers.Default
    // 동시에 실행할수있는 코루틴의 최대수를 5로 제한
    .limitedParallelism(5)



// 테스트할때(테스트용은 여기말고 ExampleUnitTest쪽에서 작성해야함)
//class SomeTest {
//    // 하나의 스레드에서 동작하는 dispatcher생성(병렬실행X, 모든작업 순차적 실행)
//    private val dispatcher = Executors
//        .newSingleThreadExecutor()
//        .asCoroutineDispatcher()
//
//    // 테스트가 실행되기전에 호출
//    @Before
//    fun setup() {
//        // Dispatchers.Main을 가짜 디스패처로 설정
//        // 따라서 테스트중에 Dispatchers.Main을 사용하면 실제 UI스레드 대신, 가짜 스레드에서 동작
//        Dispatchers.setMain(dispatcher)
//    }
//
//    // 테스트가 완료된후 호출
//    @After
//    fun tearDown() {
//        // 메인 디스패처를 원래의 Main 디스패처로 되돌림
//        Dispatchers.resetMain()
//        dispatcher.close()  // dispatcher에서 사용된 리소스해제
//    }
//
//    // 테스트 메서드
//    @Test
//    fun testSomeUI() = runBlocking {    // 코루틴이 완료될때까지 기다림
//        launch(Dispatchers.Main) {  // 이렇게 실제로 코루틴실행하는것처럼 테스트수행 -> setup()에서 설정한 가짜 메인 디스페처에서 실행됨
//            // ..
//        }
//    }
//}



// Dispatchers.IO => 병렬실행(50개의 스레드가 동시에 실행) / 많은수의 스레드생성사에 유용
// 모든 코루틴이 동일한시간에 Thread.sleep(1000)을 호출하고 1초후에 깨어나기때문에 1초만 걸림
suspend fun mains_02() {
    val time = measureTimeMillis{
        coroutineScope{ // 자식코루틴 끝날때까지 일시중단
            repeat (50){
                // Dispatchers.IO => IO작업(파일 읽기,쓰기/네트워크 요청)과 같은 블로킹작업에 최적화
                // Dispatchers.IO => 병렬실행(50개의 스레드가 동시에 실행)
                launch (Dispatchers.IO){
                    Thread.sleep(1000)  //1초동안 스레드중단
                }
            }
        }
    }
    println(time) // 1초
}



// 활성화된 스레드수가 너무많으면 메모리부족문제 발생가능 -> 같은시간에 사용할수있는 스레드수를 64개로 제한한 디스패처인 Dispatchers.IO 사용
// 이코드에서는 1000개의 코루틴을 Dispatchers.IO에서 실행하므로, worker-1부터 worker-53까지의 스레드풀에서 사용할수있는 스레드 중 하나를 선택하여 실행
// 실행시간은 약 0.2초걸림
suspend fun mains_003() = coroutineScope {
    repeat(1000) {
        launch(Dispatchers.IO) {
            Thread.sleep(200)   // 0.2초동안 중단

            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }
}
// Running on thread: DefaultDispatcher-worker-1
// ...
// Running on thread: DefaultDispatcher-worker-53
// Running on thread: DefaultDispatcher-worker-14



// Dispatchers.Default와 Dispatchers.IO를 사용하여 코루틴이 어떤 스레드에서 실행되는지를 보여주는 함수
// 결과가 DefaultDispatcher-worker-2로 출력값이 같은걸보니, Dispatchers.Default에서 시작된 코루틴이 Dispatchers.IO로 컨텍스트를 변경하더라도 동일한 스레드에서 실행될 수 있음
suspend fun mains_04(): Unit = coroutineScope {
    launch(Dispatchers.Default) {
        println(Thread.currentThread().name)
        withContext(Dispatchers.IO) {   // withContext로 코루틴의 디스패처를 Dispatchers.IO로 변경
            println(Thread.currentThread().name)    //Dispatchers.IO로 변경했음에도 같은 스레드에서 실행됨
        }
    }
}
//DefaultDispatcher-worker-2
//DefaultDispatcher-worker-2




class DiscUserRepository01(
    private val discReader: DiscReader
) : UserRepository {
    override suspend fun getUser(): UserData =
        withContext(Dispatchers.IO) {   // Dispatchers.IO에서 스레드를 실행하도록
            UserData(discReader.read("userName"))
        }
}




// Dispatchers.IO와 limitedParallelism을 이용하여 코루틴의 병렬처리를 비교하는 코드
suspend fun mains_05(): Unit = coroutineScope {
    launch {
        // Dispatchers.IO에서 100개 코루틴실행하고, 1초간 일시중단하고 시간측정
        printCoroutinesTime(Dispatchers.IO) // Dispatchers. IO took: 2074 (약 2초)
    }
    launch {
        // limitedParallelism(100)을 사용해서 동시에 최대 100개의 코루틴만 실행되도록 병렬을 제한
        val dispatcher = Dispatchers.IO
            .limitedParallelism(100)
        printCoroutinesTime(dispatcher) // LimitedDispatcher@xX took: 1082 (약 1초)
    }
}


suspend fun printCoroutinesTime(
    dispatcher: CoroutineDispatcher //어떤 디스패처에서 실행할지 결정하는 객체
) {
    val test = measureTimeMillis {
        coroutineScope {
            repeat(100){
            launch(dispatcher) {    // 각 전달된 dispatcher에서 실행
                Thread.sleep(1000)  // 블로킹하는것처럼 보이지만, 코루틴내부에서 dispatcher가 처리하므로 1초간 일시중단만 하는 코드(블로킹X)
            }
        }
    }
}
        println("$dispatcher took: $test")
}





// limitedParallelism(5)를 사용해서 한번에 너무많은 작업이 동시에 실행되지않도록 제한 => 시스템 리소스,성능 최적화
class DiscUserRepository02(
    private val discReader: DiscReader
) : UserRepository {
    private val dispatcher = Dispatchers.IO     // 동시에 실행될수있는 스레드 5개로 제한
        .limitedParallelism(5)

    override suspend fun getUser(): UserData =
        withContext(dispatcher) {
            UserData(discReader.read("userName"))
        }
}



// 싱글스레드로 제한된 디스패처
// 10000개의 코루틴을 동시에 실행하여 i의 값을 증가시키는 함수
var i = 0

suspend fun mains_06(): Unit = coroutineScope {
    repeat (10_000) {
        launch(Dispatchers.IO) { // 또는 Default 디스패처
            i++     // 동시성 문제가 발생할수있음 => 동시에 올바르게 증가한다는게 보장되지 않음
        }
    }
        delay(1000)    // 결과값확인위해 코루틴이 모두 실행되도록 대기
        println(i) // ~9930     // 10000이 출력되야하는데, 그보다 작은값인 9930출력 (race condition 때문 -> 여러코루틴이 동시에 i를 업데이트하려하면서 일부값이 손실)
    }




// 싱글스레드 디스패처를 사용하여 동시성문제 해결
// newSingleThreadExecutor()를 사용해 싱글스레드풀을 생성하고,이를 코루틴 디스패처로 변환
// => 이렇게하면 한번에 하나의 작업만 실행함 -> 직렬로 실행해서 동시성문제 해결
val dispatcher = Executors.newSingleThreadExecutor()
    .asCoroutineDispatcher()
// val dispatcher = newSingleThreadContext("My name")    // newSingleThreadContext() 써도 동일

    suspend fun mains_06_1(): Unit = coroutineScope {
        repeat (10_000) {
            launch(Dispatchers.IO) {
                i++
            }
        }
        delay(1000)
        println(i) // 10000     // 정확히 10000 출력
    }





// limitedParallelism을 1로 제한해서 동시성 문제해결(직렬적으로 실행되도록함)
suspend fun mains_07(): Unit = coroutineScope {
    val dispatcher = Dispatchers.Default
        .limitedParallelism(1)

    repeat(10000) {
        launch(dispatcher) {
            i++
        }

        delay(1000)
        println(i) // 10000
    }





    suspend fun mains_08(): Unit = coroutineScope {
        val dispatcher = Dispatchers.Default
            .limitedParallelism(1)  // 직렬실행

        val job = Job()     // job을 통해 코루틴생명주기 관리
        repeat(5) {
            launch(dispatcher + job) {  // limitedParallelism(1)이기때문에, 5개의 코루틴은 직렬로 실행
                Thread.sleep(1000)  // 1초간 스레드 차단
            }
        }
        job.complete()
        val time = measureTimeMillis { job.join() }
        println("Took $time") // 500ms가 걸립니다.
    }


//// Loom의 가상스레드 사용하기 -> 아직 도입된지얼마안됨 -> 아직 실질적으로 사용하기 힘듬
//val LoomDispatcher = Executors
//    .newVirtualThreadPerTaskExecutor()
//    .asCoroutineDispatcher()
//// ExecutorCoroutineDispatcher를 구현하는 객체를 만들 수도 있습니다.
//
//
//object LoomDispatcher : ExecutorCoroutineDispatcher() {
//
//    override val executor: Executor = Executor { command ->
//        Thread.startVirtualThread(command)  //새로운 가상스레드 생성
//    }
//
//    override fun dispatch(
//        context: CoroutineContext,
//        block: Runnable
//    ) {
//        executor.execute(block)
//    }
//
//    override fun close() {
//        error("Cannot be invoked on Dispatchers.LOOM")
//    }
//}


//// Loom으로 사용한 결과 -> 2초 조금 더걸림 (효율성 굉장히좋음) (아직은 안드로이드스튜디오에선 사용X)
//suspend fun mains_09() = measureTimeMillis {
//    coroutineScope {
//        repeat(100_000) {
//            launch(Dispatchers.Loom) {
//                Thread.sleep(1000)
//            }
//        }
//    }
//}.let (::println) // 2.273초


    // Dispatchers.IO를 사용 -> 약 23초 (Loom의 10배가 걸림)
    suspend fun mains_10() = measureTimeMillis {
        val dispatcher = Dispatchers.IO
            .limitedParallelism(100_000)
        coroutineScope {
            repeat(100_000) {
                launch(dispatcher) {
                    Thread.sleep(1000)  // Dispatchers.IO는 많은양의 병렬처리작업에 적합하지만, 100,000개의 코루틴이 동시에 1초동안중단되면 많은스레드를 생성해야해서 많은시간 소요!
                }
            }
        }
    }.let(::println) // 23.803초


    // 제한받지 않는 디스패처 => 모든작업이 같은 스레드에서 실행 => 연산순서 쉽게 통제할수있는 장점
// 그치만 현업에서는 잘 쓰지 않음 => 블로킹호출할때 Main에서 실행된다면 앱전체가 종료되는 큰문제가 발생할수있기때문
//
    suspend fun mains_11(): Unit =
        withContext(newSingleThreadContext("Thread1")) {   // 이름이 "Thread1"인 새로운 스레드실행
            var continuation: Continuation<Unit>? = null

            launch(newSingleThreadContext("Thread2")) {     // 이름이 "Thread2"인 새로운 스레드실행
                delay(1000)
                continuation?.resume(Unit)  // suspendCancellableCoroutine 재개
            }

            // Dispatchers.Unconfined => 특정 스레드에 종속되지 않고, 현재스레드에서 실행을 시작한 후, 다음 중단점에서 현재스레드가 아닌 다른스레드에서 실행될 수 있음
            launch(Dispatchers.Unconfined) {
                println(Thread.currentThread().name) // Thread1 출력

                suspendCancellableCoroutine<Unit> { // 코루틴 일시중단
                    continuation = it
                }

                println(Thread.currentThread().name) // Thread2 출력 (1초지연후, suspendCancellableCoroutine 재개되므로)
                delay(1000)

                println(Thread.currentThread().name)
                // kotlinx.coroutines.DefaultExecutor
                // (delay가 사용된 스레드입니다.)
            }
        }


//// Dispatchers.Main => 만약 이미 Main스레드(UI스레드)에서 실행중이라면, 약간의 지연발생
//suspend fun showUser01(user: User) =
//    withContext (Dispatchers.Main) {
//        userNameElement.text = user.name
//        // ...
//    }


//// Dispatchers.Main.immediate => 만약 이미 Main스레드(UI스레드)에서 실행중이라면, 지연없이 즉시 코드실행하도록 도와줌!
//suspend fun showUser02(user: User) =
//    withContext (Dispatchers.Main.immediate){
//        userNameElement.text = user.name
//    //...
//}


// ContinuationInterceptor들어가면 나오는 코드
// ContinuationInterceptor => ContinuationInterceptor는 코루틴 컨텍스트의 한요소로, 코루틴이 특정 지점에서 일시중단되고 재개될때 Continuation을 가로채거나 변환할수있음
// 1. 디스패처 구현   2. 로그기록     3. 실행흐름 커스텀
//public interface ContinuationInterceptor :
//    CoroutineContext.Element {
//
//    companion object Key :
//        CoroutineContext.Key<ContinuationInterceptor>
//
//    fun <T> interceptContinuation(
//        continuation: Continuation<T>
//    ): Continuation<T>
//
//    fun releaseInterceptedContinuation(
//        continuation: Continuation<*>
//    ){
//
//    }
//// ...
//}



    // 테스트 클래스에 따로 작성함
//    class DiscUserRepository03(
//        private val discReader: DiscReader,
//        private val dispatcher: CoroutineContext = Dispatchers.IO,
//    ) : UserRepository {
//        override suspend fun getUser(): UserData =
//            withContext(dispatcher) {
//                UserData(discReader.read("userName"))
//            }
//    }
//
//    // 테스트 클래스
//    class UserReaderTests {
//        @Test
//        fun `some test`() = runTest {
//            // 테스트 준비 과정
//            val discReader = FakeDiscReader()
//            val repo = DiscUserRepository03(
//                discReader,
//                // 테스트를 수행할 코루틴 중 하나
//                this.coroutineContext[ContinuationInterceptor]!!
//            )
//            //...
//        }
//    }



    // CPU 집약적인 연산 시뮬레이션 => Dispatchers.Defalt를 사용하는것이 가장 좋음
    fun cpu(order: Order): Coffee {
        var i = Int.MAX_VALUE
        while (i > 0) {
            i -= if (i % 2 == 0) 1 else 2   // i가 짝수이면 1을뺌 / 홈수이면 2를뺌
        }
        return Coffee(order.copy(customer = order.customer + i))
    }



    // 메모리 집약적인 연산 시뮬레이션 => 더 많은 스레드를 사용하는것이 나음
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



    // 블로킹할 경우 시뮬레이션 => 스레드 수가 많을수록 모든 코루틴이 종료되는 시간이 빨라짐
    fun blocking(order: Order): Coffee {
        Thread.sleep(1000)  // 1초간 블로킹
        return Coffee(order)
    }


    // 중단할 경우 => 단지 중단만 할경우에는 사용하고있는 스레드수가 얼마나 많은지는 문제가안됨
    suspend fun suspending(order: Order): Coffee {
        delay(1000) // 1초간 일시중단
        return Coffee(order)
    }
}