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
    quickSort(list, 0, list.size - 1)
    val endTime = System.currentTimeMillis()
    list.forEach { println(it) }

    println("quickSort 수행 시간: ${endTime - startTime}")
    reader.close()
}



fun quickSort(arr: MutableList<Int>, low: Int, high: Int) {
    if (low < high) {
        val pi = partition(arr, low, high)
        quickSort(arr, low, pi - 1)
        quickSort(arr, pi + 1, high)
    }
}



fun partition(arr: MutableList<Int>, low: Int, high: Int): Int {
    val pivot = arr[high]
    var i = low - 1
    for (j in low until high) {
        if (arr[j] <= pivot) {
            i++
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }
    }
    val temp = arr[i + 1]
    arr[i + 1] = arr[high]
    arr[high] = temp
    return i + 1
}
