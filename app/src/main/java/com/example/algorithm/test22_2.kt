package com.example.algorithm


fun main() {

    // a=1, b=3
    fun solution(a: Int, b: Int) : Long {
        val start : Long = (if(a > b) b else a).toLong()    // start = b = 1
        val end : Long = (if(a > b) a else b).toLong()      // end = a = 3
        return (start..end).sum()
    }


    println(solution(3,1))
}

