package com.example.algorithm.programmers


fun main() {

    fun solution(seoul: Array<String>): String {
        var answer = ""

        // indexOf("Kim")은 Kim의 인덱스를 호출!
        answer = "김서방은 ${seoul.indexOf("Kim")}에 있다"

        return answer
    }


    println(solution(seoul = arrayOf("Jang","youn","Kim")))
}