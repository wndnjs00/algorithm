package com.example.algorithm


fun main() {

    fun solution(arr: IntArray, divisor: Int): IntArray {
        val answer = arr.sorted().filter {

            it % divisor == 0
        }


        return if(answer.isEmpty()) {
            intArrayOf(-1)
        }else answer.toIntArray()

    }


    println(solution(intArrayOf(2,36,1), 5))
}