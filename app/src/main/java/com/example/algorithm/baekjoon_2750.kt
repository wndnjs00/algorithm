//package com.example.algorithm
//
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
//fun main(){
//    val reader = BufferedReader(InputStreamReader(System.`in`)) // BufferedReader->빠른 입력(여러줄의 입력을 처리하는데 효율적) / readLine()메서드를 통해 String형으로 데이터를 받음
//    println("줄의 개수를 입력하세요:")
//    val n = reader.readLine().toInt() //BufferedReader를 통해,입력한 줄의 개수를 int형으로 변환해서 가져옴
//    val list = mutableListOf<Int>()
//
//    println("숫자를 입력하세요:")
////    for (i in 0 until n) {
////        list.add(reader.readLine().toInt())
////    }
//    repeat(n) {     // n번 반복
//        list.add(reader.readLine().toInt())
//    }
//
//    println("결과:")
//    list.sorted()
//        .forEach { println(it) }    // forEach -> 리스트의 길이만큼 반복, 람다함수처럼 리스트값을 인자로받아 사용가능
//
//    reader.close()
//}