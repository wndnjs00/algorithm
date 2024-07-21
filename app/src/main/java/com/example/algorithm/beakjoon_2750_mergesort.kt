import java.io.BufferedReader
import java.io.InputStreamReader


fun main() {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    println("줄의 개수를 입력하세요:")
    val n = reader.readLine().toInt()
    val list = mutableListOf<Int>()

    println("숫자를 입력하세요:")
    repeat(n) {
        list.add(reader.readLine().toInt())
    }

    println("결과:")
    val startTime = System.currentTimeMillis()  // 시간비교위해
    val sortedList = mergeSort(list)
    val endTime = System.currentTimeMillis()
    sortedList.forEach { println(it) }

    println("TimSort 수행 시간: ${endTime - startTime}")
    reader.close()
}


// 리스트를 반으로 나누면서 정렬한뒤, 병합하는 정렬방식
fun mergeSort(arr: MutableList<Int>): MutableList<Int> {

    if (arr.size <= 1) {
        return arr
    }

    // 정렬된 리스트를 반으로 나눔
    val middle = arr.size / 2
    // 둘로 나눈것을 재귀적으로 정렬
    val left = arr.subList(0, middle).toMutableList()
    val right = arr.subList(middle, arr.size).toMutableList()

    return merge(mergeSort(left), mergeSort(right)) //정렬된 두 절반을 merge함수를 사용해 병합
}


// 두개의 정렬된 리스트(left,right)을 받아서, 하나의 정렬된 라스트로 병합
fun merge(left: MutableList<Int>, right: MutableList<Int>): MutableList<Int> {
    var leftIndex = 0
    var rightIndex = 0
    val result = mutableListOf<Int>()

    // 두 리스트를(left,right) 반복하면서 각 리스트에서 더작은거를 추가 (리스트중 하나가 소진될때까지 반복)
    while (leftIndex < left.size && rightIndex < right.size) {
        if (left[leftIndex] <= right[rightIndex]) {
            result.add(left[leftIndex])
            leftIndex++
        } else {
            result.add(right[rightIndex])
            rightIndex++
        }
    }

    // 소진되지 않은 리스트가 있다면, 요소를 비교해서 추가
    while (leftIndex < left.size) {
        result.add(left[leftIndex])
        leftIndex++
    }

    while (rightIndex < right.size) {
        result.add(right[rightIndex])
        rightIndex++
    }

    return result
}