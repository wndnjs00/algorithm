package com.example.algorithm.baekjoon
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

// BFS - 너비우선탐색 (가까운 곳부터 탐색)
// 그래프 탐색문제 -> 최단경로 -> BFS적합

// (x,y) -> 현재탐색중인 위치좌표값(행,열)
data class Node(val x: Int, val y: Int)

// 상하좌우로 이동하기위한 방향배열
val dx = arrayOf(0, 0, 1, -1)   // dx[0]=0 (이동X)   dx[1]=0 (이동X)  dx[2]=1 (아래로1칸)  dx[3]=-1 (위로한칸)
val dy = arrayOf(1, -1, 0, 0)   // dy[0]=1 (오른쪽1칸)   dy[1]=-1 (왼쪽1칸)  dy[2]=0 (이동X)  dy[3]=0 (이동X)



fun main() {
    val br = BufferedReader(InputStreamReader(System.`in`))

    println("도착할 행,열을 입력하세요 (예: 4 6):")
    val nm = br.readLine().split(" ")
    val n = nm[0].toInt()   //4
    val m = nm[1].toInt()   //6

    val map = Array(n) { Array(m) { 0 } }   // 인접행렬(2차원배열)   // 크기는 nXm , 초기값은 0

    println("미로를 입력하세요:")
    // 미로의 각 행을 읽어와서(한줄씩 읽어와서) 2차원배열인 map에 저장
    repeat(n) { x ->
        val line = br.readLine()
        repeat(m) { y ->
            map[x][y] = line[y] - '0'   // 아스키값을 빼줌으로써 0,1로 변환
        }
    }

    println("BFS(최단거리) 탐색 결과:")
    bfs(n, m, map)      // bfs함수로 탐색 시작
}


// bfs를 통해 최단경로르 찾는함수
fun bfs(n: Int, m: Int, map: Array<Array<Int>>) {
    // 1 - 이동가능, 0 - 벽
    val queue: Queue<Node> = LinkedList()       //탐색할 노드를 순서대로 저장하기위해
    val visited = Array(n) { Array(m) { 1 } }   //각위치를 방문했는지 확인하는 배열(방문하지않은위치=1, 방문한위치 =이전위치의값+1)

    queue.offer(Node(0, 0)) // 탐색을 시작할 출발점(0,0)을 큐에추가
    visited[0][0] = 1   //출발점을 방문했음을 표시

    //큐가 비어있지 않을때까지 반복
    while (queue.isNotEmpty()) {
        val target = queue.poll()   //큐에서 탐색할노드 꺼냄

        // i를 0,1,2,3까지 반복하면서, 상하좌우로 이동할 수 있는지 확인
        for (i in 0 until 4) {
            val nx = target.x + dx[i]
            val ny = target.y + dy[i]

            // 이동할수없는경우
            // nx !in 0 until n || ny !in 0 until m   => nx, ny가 미로의 경계를 벗어났을때
            // map[nx][ny] == 0   => 해당위치가 벽일때
            // visited[nx][ny] != 1  => 해당위치를 이미 방문했을때
            if (nx !in 0 until n || ny !in 0 until m || map[nx][ny] == 0 || visited[nx][ny] != 1)
                continue

            // 이동가능한경우
            queue.offer(Node(nx, ny))   // 큐에 새로운 탐색값 추가
            visited[nx][ny] = visited[target.x][target.y] + 1   // visited[nx][ny]값을 갱신해서, 해당위치를 방문했음을 표시


            // 현재위치가 도착점일때
            if (nx == n - 1 && ny == m - 1) {
                println(visited[nx][ny])    // 최종거리 출력(이게 최단거리가됨)
                break   //탐색종료
            }
        }
    }
}