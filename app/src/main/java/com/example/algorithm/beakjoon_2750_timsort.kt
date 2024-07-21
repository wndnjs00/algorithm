//import java.io.BufferedReader
//import java.io.InputStreamReader
//
//const val RUN = 32
//
//
//fun main() {
//    val reader = BufferedReader(InputStreamReader(System.`in`))
//    println("줄의 개수를 입력하세요:")
//    val n = reader.readLine().toInt()
//    val list = mutableListOf<Int>()
//
//    println("숫자를 입력하세요:")
//    repeat(n) {
//        list.add(reader.readLine().toInt())
//    }
//
//    println("결과:")
//    val startTime = System.currentTimeMillis()  // 시간비교위해
//    timSort(list, list.size)
//    val endTime = System.currentTimeMillis()
//    list.forEach { println(it) }
//
//    println("TimSort 수행 시간: ${endTime - startTime}")
//    reader.close()
//}
//
//// 한칸씩 뒤로 밀어내며 적절한 위치에 끼워넣는 정렬방식
//// left=정렬할 시작 인덱스, right=정렬할 마지막 인덱스
//fun insertionSort(arr: MutableList<Int>, left: Int, right: Int) {
//    for (i in left + 1..right) {
//        val temp = arr[i]
//        var j = i - 1
//        while (j >= left && arr[j] > temp) {
//            // 오른쪽으로 한칸씩 이동
//            arr[j + 1] = arr[j]
//            // 왼쪽에 있는 다음요소를 확인하기위해 감소
//            j--
//        }
//        // while문 다돌면 temp를 올바를 위치에 삽입
//        arr[j + 1] = temp
//    }
//}
//
//// 리스트를 반으로 나누면서 정렬한뒤, 병합하는 정렬방식
//// l=시작 인덱스, m=중간 인덱스, r=마지막 인덱스
//fun mergeSort(arr: MutableList<Int>, l: Int, m: Int, r: Int) {
//    val len1 = m - l + 1    //왼쪽배열의 길이
//    val len2 = r - m        //오른쪽배열의 길이
//    // 임시로 배열 생성
//    val left = IntArray(len1)
//    val right = IntArray(len2)
//
//    // 임시배열에 데이터복사 (리스트 반으로 나누는과정)
//    for (x in 0 until len1) left[x] = arr[l + x]
//    for (x in 0 until len2) right[x] = arr[m + 1 + x]
//
//    var i = 0
//    var j = 0
//    var k = l
//
//    // 병합하는 과정
//    while (i < len1 && j < len2) {
//        if (left[i] <= right[j]) {
//            arr[k] = left[i]
//            i++
//        } else {
//            arr[k] = right[j]
//            j++
//        }
//        k++
//    }
//
//    // 남은수가 있으면 삽입
//    while (i < len1) {
//        arr[k] = left[i]
//        k++
//        i++
//    }
//
//    while (j < len2) {
//        arr[k] = right[j]
//        k++
//        j++
//    }
//}
//
//
//fun timSort(arr: MutableList<Int>, n: Int) {
//    // insertionSort 사용해서 배열정렬
//    for (i in 0 until n step RUN) {
//        insertionSort(arr, i, Math.min((i + 31), (n - 1)))
//    }
//
//    // size 초기화
//    var size = RUN
//
//    // mergeSort 사용해서 병합
//    while (size < n) {
//        for (left in 0 until n step 2 * size) {
//            val mid = left + size - 1
//            val right = Math.min((left + 2 * size - 1), (n - 1))
//            if (mid < right) mergeSort(arr, left, mid, right)
//        }
//        // 크기를 2배로 -> 하위배열들이 정렬될때까지 효율적으로 병합
//        size *= 2
//    }
//}
