//package com.example.algorithm
//
//
//fun main() {
//
//    fun solution(arr: IntArray, divisor: Int): ArrayList<Int> {
//        val answer = arrayListOf<Int>()
//
//
//        // for문을 돌면서 i에 arr값 하나하나 넣기
//        // i = 3,
//        for (i in arr){
//
//            if (i % divisor == 0){
//                answer.add(i)
//            }
//        }
//
//
//        // 나누어 떨어지는 값이 하나도 없을때
//        if (answer.isEmpty()){
//            answer.add(-1)
//        }
//
//
//        answer.sort()
//        return answer
//    }
//
//
//    println(solution(arr = intArrayOf(2,36,1,3), 1))
//}