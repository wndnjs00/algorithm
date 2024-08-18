package com.example.algorithm.baekjoon

// 그리디 알고리즘 - 그 순간에 최적이라고 생각되는 것을 선택해 나가는방식
// 큰값 동전부터 차례대로 사용해서 k원을 만들어가도록

private fun main() {
    println("동전종류의 개수, 총가치의합(원)을 입력하세요:")
    var (n, k) = readln().split(" ").map { it.toInt() }
    var result = 0

    val arr = mutableListOf<Int>()  //빈배열생성

    println("동전의 종류를 오름차순으로 입력하세요:")
    repeat (n) {
        arr.add(readln().toInt())
    }

    // 가장큰 동전부터 차례대로 반복
    for (i in n - 1 downTo 0) {
        val coinCount = k / arr[i]
        if (coinCount > 0){
            k %= arr[i]
            result += coinCount
        }
    }

    println("필요한 동전의 최소개수:")
    println(result)
}


// ex) n=10, k=4200
// arr = [1, 5, 10, 50, 100, 500, 1000, 5000, 10000, 50000]

// coinCount=4200/1000= 4 , k=200, result=4누적
// coinCount=200/100= 2 , k=0, result=6

