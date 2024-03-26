//package com.example.algorithm
//
//
//fun main() {
//
//
//    fun solution(num : Int): Int {
//        var a = num
//        var answer  = 0
//
//
//        // 무한루프
//        while(true){
//
//            if(a == 1){
//                break
//
//            }else if(answer >= 500){
//                answer = -1
//                break
//
//            }else{
//                // 짝수
//                if (a%2 == 0){
//                    a /= 2
//                }else{
//                    // 홀수
//                    // a = a*3+1 이렇게하면 오류뜸
//                    var value = a.toDouble()
//                    value = (value * 3) + 1
//                    a = value.toInt()
//
//                }
//
//                answer ++
//
//            }
//
//        }
//
//
//        return answer
//    }
//
//
//
//    println(solution(626331))
//}
//
//
//
//
//// num = 6, 3 , 10, 5, 16,8,4,2,   1
//
//// answer = 1 + 1 + 1 + 1 + 1 + 1 + 1 + 1