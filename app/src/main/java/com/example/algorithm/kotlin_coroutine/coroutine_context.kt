package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/* 7장 코루틴 컨텍스트*/

//public fun CoroutineScope.launch(
//    context: CoroutineContext = EmptyCoroutineContext,
//    start: CoroutineStart = CoroutineStart.DEFAULT,
//    block: suspend CoroutineScope.() -> Unit
//): Job {
//    //...
//}


//public interface Continuation<in T> {
//    public val context: CoroutineContext
//    public fun resumeWith(result: Result<T>)
//}



//fun main(){
//    val name: CoroutineName = CoroutineName ("A name")
//    val element: CoroutineContext.Element = name
//    val context: CoroutineContext = element
//
//    val job: Job = Job()
//    val jobElement: CoroutineContext.Element = job
//    val jobContext: CoroutineContext = jobElement
//}


fun main(){
    // CoroutineContext => 코루틴의 동작과 속성 정의   // CoroutineName => 코루틴에 이름을 지정(주로 복잡한 동시앱에서 로그를 통해 어떤 코루틴이 실행되는지 추적하는데 도움줌)
    val ctx: CoroutineContext = CoroutineName ("A name")  // CoroutineName을 A name으로 지정
    val coroutineName: CoroutineName? = ctx[CoroutineName]      // CoroutineContext는 get이나 []를 사용하여 원소 찾을수있음   // 또는 ctx.get(CoroutineName)
    println (coroutineName?.name)   // A name

    // Job의 원소검색 => Job의 원소는 없으므로 null이 출력
    val job: Job? = ctx[Job]        // 또는 ctx.get(Job)
    println(job)                    // null
}


// 실제 사용예시
// Dispatchers.Default -> 코루틴에서 사용할 스레드 풀을 지정
// CoroutineName("ExampleCoroutine") -> 디버깅을 쉽게하기위해 코루틴의 이름 지정
// Job() -> 코루틴의 수명주기를 관리하여 취소 및 완료처리
val context: CoroutineContext = Dispatchers.Default + CoroutineName("ExampleCoroutine") + Job()



// CoroutineName 내부코드(자바코드로 디컴파일했을때와 동일)
data class CoroutineName(
    val name: String
) : AbstractCoroutineContextElement(CoroutineName){     // CoroutineContext.Element     // CoroutineName = 전달된 생성자 매개변수 => 특정 컨텍스트요소를 검색하는데 사용되는 키를 나타냄

    override fun toString(): String = "CoroutineName($name)"    // CoroutineName에 이름을 지정함으로써 디버깅유용
    companion object Key : CoroutineContext.Key<CoroutineName>  // companion object를 key로 사용해 같은이름을 가진 원소를 찾음
}



// CoroutineContext 원소 더하기
fun main_03(){
    val ctx1: CoroutineContext = CoroutineName("Name1")
    println(ctx1[CoroutineName]?.name)  // Name1
    println(ctx1[Job]?.isActive)        // null

    val ctx2: CoroutineContext = Job()
    println(ctx2[CoroutineName]?.name)  // null
    println(ctx2[Job]?.isActive)        // Active 상태이므로 true    // 빌더를 통해 생성되는 Job의 기본 상태가 'Actice'이므로 true가됨

    val ctx3 = ctx1 + ctx2              // CoroutineContext 더하기
    println(ctx3[CoroutineName]?.name)  // Name1
    println(ctx3[Job]?.isActive)        // true
}


// CoroutineContext 원소 더하기
// CoroutineContext원소가 더해지면, 맵처럼 새로운원소가 기존원소를 대체
fun main_04(){
    val ctx1: CoroutineContext = CoroutineName ("Name1")
    println(ctx1[CoroutineName]?.name) // Namel

    val ctx2: CoroutineContext = CoroutineName("Name2")
    println(ctx2[CoroutineName]?.name) // Name2

    // CoroutineContext원소가 더해지면, 새로운원소가 기존원소를 대체
    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name) // Name2
}


// 비어있는 코루틴컨텍스트는 더해도 아무런 변화X
fun main_05(){
    val empty: CoroutineContext = EmptyCoroutineContext
    println(empty[CoroutineName])   // null
    println(empty[Job])             // null

    // empty는 비어있는 요소이기떄문에 더해도 아무런 변화X
    val ctxName = empty + CoroutineName ("Name1") + empty
    println(ctxName[CoroutineName]) // CoroutineName(Name1)
}


// minusKey함수에 키를 넣는 방법으로 -> 원소를 컨텍스트에서 제거
fun main_06() {
    val ctx = CoroutineName ("Name1") + Job()
    println(ctx[CoroutineName]?.name) // Name1
    println(ctx[Job]?.isActive)       // true

    val ctx2 = ctx.minusKey(CoroutineName)
    println(ctx2[CoroutineName]?.name) // null
    println(ctx2[Job]?.isActive)       // true

    val ctx3 = (ctx + CoroutineName("Name2")).minusKey(CoroutineName)
    println(ctx3[CoroutineName]?.name)  // null
    println(ctx3[Job]?.isActive)        // true
}



// fold 메서드 -> 모든요소를 반복하고, 결과를 누적(컨텍스트의 각 원소를 조작해야하는 경우 사용)
fun main_07() {
    val ctx = CoroutineName("Name1") + Job()
    ctx.fold("") { acc, element -> "$acc$element" }
        .also(::println)
// CoroutineName(Name1) JobImpl{Active}@dbab622e

    val empty = emptyList<CoroutineContext>()
    // ctx.fold(empty) => 어짜피 empty를 누적하기 때문에 결과 똑같음
    ctx.fold(empty) { acc, element -> acc + element }
        .joinToString()  // 쉼표로 구분된 문자열로 변환
        .also(::println)
    // CoroutineName(Name1), JobImpl{Active}@dbab622e
}




fun CoroutineScope.log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

// CoroutineName이 재정의되지 않는이상 자식은 얘를 상속받음
    fun main_08() = runBlocking(CoroutineName("main")) {
        log("Started" )     // [main] Started

        val v1 = async {    // async => 결과반환하는 코루틴(여기서는 42반환)
            delay(500)
            log("Running async")    // [main] Running async
            42
        }
            launch {
                delay(1000)
                log("Running launch")   // [main] Running launch
            }

            log("The answer is ${v1.await()}")  // [main] The answer is 42
    }


fun main_09() = runBlocking(CoroutineName("main")) {
    log("Started")      // [main] Started

    val v1 = async(CoroutineName ("c1")) {
        delay(500)
        log("Running async")    // [c1] Running async
        42
    }
    launch(CoroutineName("c2")){
        delay (1000)
        log("Running launch")   // [c2] Running launch
    }
    // runBlocking코루틴 내에 있으므로 CoroutineName은 [main]
    log ("The answer is ${v1.await()}")     // [main] The answer is 42
}


// coroutineContext는 모든 중단함수에서 사용가능
suspend fun printName(){
    println(coroutineContext[CoroutineName]?.name)
}

suspend fun mains() = withContext(CoroutineName("Outer")) {
    printName()             // Outer

    launch(CoroutineName("Inner")) {
        printName()         // Inner
    }
    delay(10)
    printName()             // Outer
}


// CoroutineContext를 개별적으로 사용 (사용자가 정의한 Context클래스)
// class의 컴패니언 객체를 키로 사용
class MyCustomContext: CoroutineContext.Element{
    override val key: CoroutineContext.Key<*> = Key

    companion object Key:
        CoroutineContext.Key<MyCustomContext>   // CoroutineContext에서 요소를 식별하기위한 key
}

fun mains2() = runBlocking {
    // MyCustomContext => 사용자가 정의한 Context클래스
    val customContext = MyCustomContext()
    val context = coroutineContext + customContext

    val retrievedContext = context[MyCustomContext.Key] // 사용자가 정의한 Context클래스의 key값 검색가능
    println(retrievedContext)
}




class CounterContext (
    private val name: String
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key
    private var nextNumber = 0

    fun printNext() {
        println("$name: $nextNumber")
        nextNumber++
    }
    companion object Key : CoroutineContext.Key<CounterContext>
}

    // coroutineContext를 통해, CounterContext의 printNext()를 호출하는 중단함수
    suspend fun printNext(){
        coroutineContext[CounterContext]?.printNext()
    }

    suspend fun mains3(): Unit =
        withContext(CounterContext("Outer")) {
            printNext()         // Outer: 0
            launch {
                printNext()     // Outer: 1
                launch {
                    printNext() // Outer: 2
                }

                // 이름이 "Inner"인 새로운 Context 생성
                launch(CounterContext("Inner")) {
                    printNext()     // Inner: 0
                    printNext()     // Inner: 1
                    launch {
                        printNext() // Inner: 2
                    }
                }
            }
            printNext() // Outer: 3
        }





data class User(val id: String, val name: String)

abstract class UuidProviderContext: CoroutineContext.Element {
    abstract fun nextUuid(): String
    override val key: CoroutineContext.Key<*> = Key

    companion object Key :
        CoroutineContext.Key<UuidProviderContext>
}


class RealUuidProviderContext : UuidProviderContext(){
    // nextUuid() 실제구현부
    override fun nextUuid(): String =
        UUID.randomUUID().toString()
}

class FakeUuidProviderContext(
    private val fakeUuid: String
) : UuidProviderContext() {
    override fun nextUuid(): String = fakeUuid
}

suspend fun nextUuid(): String =
    // UuidProviderContext존재하는지 확인하고, 존재하지않으면 메시지와 함께 예외발생
    checkNotNull(coroutineContext[UuidProviderContext]) {
        "UuidProviderContext not present"
    }.nextUuid()

        // 테스트하려는 항수입니다.
        suspend fun makeUser(name: String) = User(
            id = nextUuid(),
            name = name
        )

        suspend fun mains4(): Unit {
            // 프로덕션 환경일 때
            withContext(RealUuidProviderContext()){
                println(makeUser("Michat"))     // User(id=d260482a-..., name=Michat)
            }

            // 테스트 환경일때
            withContext (FakeUuidProviderContext("FAKE_UUID")) {
                val user = makeUser ("Michat")
                println(user) // User(id=FAKE_UUID, name=Michat)
            }
        }
