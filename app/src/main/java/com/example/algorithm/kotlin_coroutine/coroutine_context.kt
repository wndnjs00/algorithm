package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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


public interface Continuation<in T> {
    public val context: CoroutineContext
    public fun resumeWith(result: Result<T>)
}



fun main(){
    val name: CoroutineName = CoroutineName ("A name")
    val element: CoroutineContext.Element = name
    val context: CoroutineContext = element

    val job: Job = Job()
    val jobElement: CoroutineContext.Element = job
    val jobContext: CoroutineContext = jobElement
}


fun main_02(){
    val ctx: CoroutineContext = CoroutineName ("A name")
    val coroutineName: CoroutineName? = ctx[CoroutineName]

    println (coroutineName?.name) // A name
    val job: Job? = ctx[Job]
    println(job)
}


data class CoroutineName(
    val name: String
) : AbstractCoroutineContextElement(CoroutineName){

    override fun toString(): String = "CoroutineVane($name)"
    companion object Key : CoroutineContext.Key<CoroutineName>
}


interface Job: CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<Job>

    //..
}



fun main_03(){
    val ctx1: CoroutineContext = CoroutineName("Name1")
    println(ctx1[CoroutineName]?.name) // Namel
    println(ctx1[Job]?.isActive)      // null

    val ctx2: CoroutineContext = Job()
    println(ctx2[CoroutineName]?.name) // null
    println(ctx2[Job]?.isActive)    // Active' 상태이므로 true입니다    // 빌더를 통해 생성되는 집의 기본 상태가 'Actice' 상태이므로 true가 됩니다.

    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name) // Name1
    println(ctx3[Job]?.isActive) //true
}



fun main_04(){
    val ctx1: CoroutineContext = CoroutineName ("Name1")
    println(ctx1[CoroutineName]?.name) // Namel

    val ctx2: CoroutineContext = CoroutineName("Name2")
    println(ctx2[CoroutineName]?.name) // Name2

    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name) // Name
}



fun main_05(){
    val empty: CoroutineContext = EmptyCoroutineContext
    println(empty[CoroutineName]) // null
    println(empty[Job])         // null

    val ctxName = empty + CoroutineName ("Namel") + empty
    println(ctxName[CoroutineName]) // CoroutineName(Name1)
}



fun main_06() {
    val ctx = CoroutineName ("Namel") + Job()
    println(ctx[CoroutineName]?.name) // Namel
    println(ctx[Job]?.isActive)       // true

    val ctx2 = ctx.minusKey(CoroutineName)
    println(ctx2[CoroutineName]?.name) // null
    println(ctx2[Job]?.isActive)       // true

    val ctx3 = (ctx + CoroutineName("Name2")).minusKey(CoroutineName)
    println(ctx3[CoroutineName]?.name) // null
    println(ctx3[Job]?.isActive) // true
}


fun main_07() {
    val ctx = CoroutineName("Name1") + Job()
    ctx.fold("") { acc, element -> "Saccselement" }
        .also(::println)
// CoroutineName(Name1) JobImpl{Active)@dbab622e

    val empty = emptyList<CoroutineContext>()
    ctx.fold(empty) { acc, element -> acc + element }
        .joinToString()
        .also(::println)
    // Corout ineName(Namel), JobImpl{Active) @dbab622e
}



fun CoroutineScope.log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

    fun main_08() = runBlocking(CoroutineName("main")) {
        log("Started" ) // [main] Started
        val v1 = async {
            delay(500)
            log("Running async") // [main] Running async
            42
        }
            launch {
                delay(1000)
                log("Running launch") // [main] Running launch
            }
            log("The answer is ${v1.await()}")
            // [main] The answer is 42
    }



fun main_09() = runBlocking(CoroutineName("main")) {
    log("Started") // [main] Started

    val v1 = async(CoroutineName ("c1")) {
        delay(500)
        log("Running async") // [c1] Running async
        42
    }
    launch(CoroutineName("c2")){
        delay (1000)
        log("Running launch") // [c2] Running launch
    }
    log ("The answer is ${v1.await()}")
    // (main) The answer is 42
}



suspend fun printName(){
    println(coroutineContext [CoroutineName]?.name)
}

suspend fun main_10() = withContext (CoroutineName("Outer")) {
    printName() // Outer
    launch(CoroutineName("Inner")) {
        printName() // Inner
    }
    delay(10)
    printName() // Outer
}


class MyCustomContext: CoroutineContext.Element{
    override val key: CoroutineContext.Key<*> = Key

    companion object Key:
        CoroutineContext.Key<MyCustomContext>
}



class CounterContext (
    private val name: String
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key
    private var nextNumber = 8

    fun printNext() {
        println("Sname: SnextNumber")
        nextNumber++
    }

    companion object Key : CoroutineContext.Key<CounterContext>
}
    suspend fun printNext(){
        coroutineContext [CounterContext]?.printNext()
    }

    suspend fun main_12(): Unit =
        withContext (CounterContext("Outer")) {
            printNext()         // Outer: 8
            launch {
                printNext()     // Outer: 1
                launch {
                    printNext() // Outer: 2
                }


                launch(CounterContext("Inner")) {
                    printNext() // Inner: 0
                    printNext() // Inner: 1
                    launch {
                        printNext() // Inner: 2
                    }
                }
            }
            printNext() // Outer: 3
        }





data class User(val id: String, val name: String)

abstract class UuidProviderContext:
    CoroutineContext.Element {

    abstract fun nextUuid(): String
    override val key: CoroutineContext.Key<*> = Key

    companion object Key :
        CoroutineContext.Key<UuidProviderContext>
}


class RealUuidProviderContext : UuidProviderContext(){
    override fun nextUuid(): String =
        UUID.randomUUID().toString()
}

class FakeUuidProviderContext(
    private val fakeluid: String
) : UuidProviderContext() {
    override fun nextUuid(): String = fakeUuid
}

suspend fun nextUuid(): String =
    checkNotNull(coroutineContext[UuidProviderContext]) {
        "UuidProviderContext not present"
    }
        .nextUuid()

        // 테스트하려는 항수입니다.
        suspend fun makeUser (name: String) = User(
            id = nextUuid(),
            name = name
        )

        suspend fun main_11(): Unit {
            // 프로덕션 환경일 때
            withContext(RealUuidProviderContext()){
                println(makeUser ("Michat"))
                // 예를들어 User(id=d260482a-..., name=Michat)
            }

            // 테스트 환경일때
            withContext (FakeUuidProviderContext ("FAKE_UUID")) {
                val user = makeUser ("Michat")
                println(user) // User (id=FAKE_UUID, name=Michat)
                assertEquals(User("FAKE_UUID", "Michat"), user)
            }
        }
