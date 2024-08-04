package com.example.algorithm

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// 람다식에 의해 값을 반환, Deferred<T> 타입의 객체를 리턴 (T는 값의타입)
// Deffered에는 await이라는 메서드 존재 -> await은 작업이 끝나면 값을 반환하는 중단메서드!
// 하지만 값이 발생되기 전에 await이 호출되는 경우, 값이 준비될 때까지 기다림

// 아래의 예시를 보면 값 42가 생산되었고, 이 타입은 Int 이므로 Deferred<Int>가 리턴됨
// 그리고 리턴된 Deferred<Int>의 await를 통해 값 42가 리턴된다
fun main() = runBlocking {
    val resultDeferred: Deferred<Int> = GlobalScope.async {
        delay(1000L)
        42
    }
    val result: Int = resultDeferred.await() // (1초 후)
    println(result) // 42
    // 아니면 이런식으로 간단하게 작성도 가능
    println(resultDeferred.await()) // 42
}



// async는 launch 빌더처럼 호출되는 시점에서 즉시 코루틴을 시작함
// 따라서 몇개의 작업을 한번에 시작하고, 모든결과를 한꺼번에 기다릴때 사용!
// Defferred는 람다식에 의해 값이 생성되면 값을 Defferred 내부에 저장하기때문에, await에서 값이 반환되는 즉시 사용가능!
// but, 값이 생성되기전에 await을 호출하면 값이 나올때까지 suspend된다.

// launch와 async는 await을 통해 값을 반환하는 특징뺴고는 비슷함
// 대치가 가능하지만 용법에 맞게써야함! => async는 값을 생성할떄 사용 / 값이 필요하지 않을때는 launch 써야함!! (반환값이 없으면 launch사용하기)
fun main1() = runBlocking {
    val res1 = GlobalScope.async {
        delay(1000L)
        "Text 1"
    }
    val res2 = GlobalScope.async {
        delay(3000L)
        "Text 3"
    }
    val res3 = GlobalScope.async {
        delay(2000L)
        "Text 2"
    }
    println(res1.await())
    println(res2.await())
    println(res3.await())
}
// (1초 후)
// Text 1
// (2초 후)
// Text 2
// Text 3



// 병렬 실행
// 다른 두 위치에서 데이터를 가져와야 하는 경우, 데이터를 결합하기 위해 사용가능
//scope.launch {
//    val news = async {
//        newsRepo.getNews().sortedByDescending { it.date }
//    }
//    val newsSummary = newsRepo.getNewsSummary()
//    views.showNews(
//        newsSummary,
//        news.await()
//    )
//}