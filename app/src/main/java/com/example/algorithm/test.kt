package com.example.algorithm


fun main() {

    fun solution(x: Int): Boolean {
        var sum = 0
        var X = x

        while (X != 0) {
            sum += X % 10   //  sum=2,   sum = 3         // sum = 5, sum=6
            X /= 10     // X=1, x=0                     // x=1, x=0
        }

        return (x % sum == 0) // 12%3 == 0
    }


    println(solution(12))
}
