import java.io.BufferedReader
import java.io.InputStreamReader

const val RUN = 32


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
    timSort(list, list.size)
    val endTime = System.currentTimeMillis()
    list.forEach { println(it) }

    println("TimSort 수행 시간: ${endTime - startTime}")
    reader.close()
}


fun insertionSort(arr: MutableList<Int>, left: Int, right: Int) {
    for (i in left + 1..right) {
        val temp = arr[i]
        var j = i - 1
        while (j >= left && arr[j] > temp) {
            arr[j + 1] = arr[j]
            j--
        }
        arr[j + 1] = temp
    }
}

fun mergeSort(arr: MutableList<Int>, l: Int, m: Int, r: Int) {
    val len1 = m - l + 1
    val len2 = r - m
    val left = IntArray(len1)
    val right = IntArray(len2)

    for (x in 0 until len1) left[x] = arr[l + x]
    for (x in 0 until len2) right[x] = arr[m + 1 + x]

    var i = 0
    var j = 0
    var k = l

    while (i < len1 && j < len2) {
        if (left[i] <= right[j]) {
            arr[k] = left[i]
            i++
        } else {
            arr[k] = right[j]
            j++
        }
        k++
    }

    while (i < len1) {
        arr[k] = left[i]
        k++
        i++
    }

    while (j < len2) {
        arr[k] = right[j]
        k++
        j++
    }
}

fun timSort(arr: MutableList<Int>, n: Int) {
    for (i in 0 until n step RUN) {
        insertionSort(arr, i, Math.min((i + 31), (n - 1)))
    }

    var size = RUN
    while (size < n) {
        for (left in 0 until n step 2 * size) {
            val mid = left + size - 1
            val right = Math.min((left + 2 * size - 1), (n - 1))
            if (mid < right) mergeSort(arr, left, mid, right)
        }
        size *= 2
    }
}
