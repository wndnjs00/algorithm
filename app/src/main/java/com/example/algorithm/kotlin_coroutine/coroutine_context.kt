package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

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

