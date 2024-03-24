//package com.example.algorithm
//
// 실행 오류나는 코드
//
//fun main() {
//
//
//    fun solution(num : Int): Int {
//        var a = num
//        var answer  = 0
//        var result = mutableListOf<Int>()
//
//        // num이 1이 아닐때까지 반복
//        while(a != 1){
//
//            // 입력된수가 짝수일때
//            if(a % 2 == 0){
//                a /= 2
//
//            }else {
//                // 홀수일때
//                var value = a.toDouble()
//                value = (value * 3) + 1
//                a = value.toInt()
//            }
//
//            result.add(a)
//
//
//        }
//
//
//
//        return if(answer < 501){
//            result.size
//        }else{
//            -1
//        }
//    }
//
//
//        println(solution(626331))
//    }
//
//
//
//// num = 6 ,3, 10, 5, 16, 8,4,2,1