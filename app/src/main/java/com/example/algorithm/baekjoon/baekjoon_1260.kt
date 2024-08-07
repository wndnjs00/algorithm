package com.example.algorithm.baekjoon

import java.util.*

fun main() {
    println("노드개수,간선개수,시작노드를 입력하세요:")
    // n-노드의개수, m-간선의개수, v-처음탐색시작할 노드번호
    val (n,m,v) = readLine()!!.split(" ").map {it.toInt()}
    val graph = Array(n+1) {IntArray(n+1)}      // 인접행렬     // graph[i][j] = 1  i,j연결      //graph[i][j] = 0  i,j연결안됨
    var visit = ArrayList<Int>()    // 방문한 노드 저장하기위해

    println("연결되어있는 두노드를 입력해주세요:")
    // 간선 개수만큼 반복
    repeat(m) {
        // x,y - 연결되어있는 노드번호
        val (x,y) = readLine()!!.split(" ").map {it.toInt()}

        // x,y 서로연결     // x,y / y,x모두 연결되어있음 -> 방향이없는 그래프임을 표시
        graph[x][y] = 1
        graph[y][x] = 1
    }

    println("DFS 연산결과:")
    dfs(v, graph, visit)

    visit = ArrayList<Int>()  // 방문 노드 초기화
    println()
    println("BFS 연산결과:")
    bfs(v, graph, visit)
}


// 깊이우선탐색(갈수있는 최대한 멀리까지가서 탐색)
// 스택(후입선출), 재귀호출
// v에서 시작해서, 더이상 연결된 정점이 없을때까지 재귀함수를 호출해가면서 깊이우선탐색으로 탐색
fun dfs(start: Int, graph: Array<IntArray>, visit: ArrayList<Int>) {
    visit.add(start)
    print("$start ")    // 방문한 모든 노드들 출력

    // 모든 노드 탐색
    for (i in 1 until graph.size) {
        // 노드가 연결되어있고, i가 이미 방문한 노드인지 확인
        if (graph[start][i] == 1 && !visit.contains(i)) {
            dfs(i,graph,visit)
        }
    }
}


// 너비우선탐색 (가까운 곳부터 탐색)
// 큐(선입선출)
fun bfs(start: Int, graph: Array<IntArray>, visit: ArrayList<Int>) {
    val queue = LinkedList<Int>()   // LinkedList를 사용하여 초기화
    queue.add(start)
    visit.add(start)

    while(queue.isNotEmpty()) {
        // 큐의 앞에있는 노드제거하고 반환
        val top = queue.poll()
        print("$top ")

        // 모든 노드 탐색
        for (i in 1 until graph.size) {
            // 노드가 연결되어있고, i가 이미 방문한 노드인지 확인
            if (graph[top][i] == 1 && !visit.contains(i)) {
                queue.add(i)
                visit.add(i)
            }
        }
    }
}