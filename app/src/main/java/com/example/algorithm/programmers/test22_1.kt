package com.example.algorithm.programmers


fun main() {

    // a=1, b=3
    fun solution(a: Int, b: Int) =

        if (a<b){
            ((a..b).average() * (b - a + 1)).toLong()

        }else if(a>b) {
            ((b..a).average() * (a - b +1)).toLong()

        }else a.toLong()


    println(solution(1,3))
}







// 1+2+3 / 3 = 2
// 2 * 3 = 6


// 3+4+5 / 3 = 4
// 4 * 3 = 12