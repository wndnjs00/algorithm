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


// 하위배열의 요소를 재배열하고, 피벗의 최종위치 반환하는 함수 -> 피벗을 기준으로 왼쪽에는 작은값, 오른쪽에는 큰값 배치됨
// low=시작 인덱스, high=마지막 인덱스
fun partition(arr: MutableList<Int>, low: Int, high: Int): Int {
    // pivot설정(마지막 값으로 지정) [처음,중간 또는 랜덤으로 지정해도됨]
    val pivot = arr[high]
    var i = low - 1
    /// 하위배열 반복
    for (j in low until high) {
        // arr[j] <= pivot 이면
        // i증가하고, arr[i]와 arr[j] 교환
        if (arr[j] <= pivot) {
            i++
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }
    }

    // 피벗을 올바른 위치에 할당 -> 피벗을 기준으로 왼쪽에는 작은값, 오른쪽에는 큰값 배치
    val temp = arr[i + 1]   // 피벗과 바꿀수있도록 인덱스의값을 일시적으로 저장
    arr[i + 1] = arr[high]  // 피벗을 올바른 위치에 할당
    arr[high] = temp
    return i + 1            // 분할 후 피벗의 최종인덱스 반환
}


// 배열을 하위배열로 나누고, 각부분을 별도로 정렬하는 함수
fun quickSort(arr: MutableList<Int>, low: Int, high: Int) {
    // 하위배열에 두개이상의 요소가 있는지 확인
    if (low < high) {
        // 피벗이 최종정렬된 위치에 있도록 partition 재정렬
        val pi = partition(arr, low, high)
        quickSort(arr, low, pi - 1) // 피벗보다 작거나 같은 요소가 포함된 하위 배열을 재귀적으로 정렬
        quickSort(arr, pi + 1, high) // 피벗보다 큰 요소가 포함된 하위 배열을 재귀적으로 정렬
    }
}
