package com.example.algorithm

import java.io.BufferedReader
import java.io.InputStreamReader

fun main(){
    val reader = BufferedReader(InputStreamReader(System.`in`))
    println("줄의 개수를 입력하세요:")
    val n = reader.readLine().toInt()
    val list = mutableListOf<Int>()

    println("숫자를 입력하세요:")
//    for (i in 0 until n) {
//        list.add(reader.readLine().toInt())
//    }
    repeat(n) {
        list.add(reader.readLine().toInt())
    }

    println("결과:")
    list.sorted()
        .forEach { println(it) }

    reader.close()
}