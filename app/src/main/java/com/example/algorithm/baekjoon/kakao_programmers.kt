package com.example.algorithm.baekjoon
import java.util.*
// 도넛 -> 사이클 / 모든노드가 하나의 간선이 있음
// 막대 -> 마지막노드는 간선이 없음 (리프노드가 있음)
// 8자 -> 한노드당 2개의 간선이 있음
// 생성노드 -> 들어오는 간선없고, 나가는간선만 2개 이상일경우

class Solution {

    fun solution(edges: Array<IntArray>): IntArray {
        val graph = List(1000001) { mutableListOf<Int>() }
        val answer = IntArray(4)
        val inNode = IntArray(1000001)
        val outNode = IntArray(1000001)


        // 엣지 순회(마지막으로 들어오는 엣지, 처음으로 나가는 엣지 누적)
        edges.forEach { (start, end) ->
            inNode[end]++
            outNode[start]++
            graph[start].add(end)
        }

        // 생성노드 찾기
        // 들어오는 간선이 0이고, 나가는간선 2개이상인 노드찾아서 answer[0]에 저장
        for (i in 0 until 1000001) {
            if (inNode[i] == 0 && outNode[i] > 1) {
                answer[0] = i   //첫번쨰 인덱스에 저장

                break
            }
        }

        // 생성노드에서부터 시작해서 bfs적용해서 그래프 탐색
        graph[answer[0]].forEach {
            answer[bfs1(graph, it)]++
        }
        return answer
    }


    private fun bfs1(graph: List<MutableList<Int>>, start: Int): Int {
        val queue: Queue<Int> = LinkedList()
        var node = 0

        queue.offer(start)

        while (queue.isNotEmpty()) {
            val p = queue.poll()    //큐에서 탐색할노드 꺼냄
            node++

            // 시작 노드와 만났을때 (도넛)
            if (node > 1 && p == start) {
                return 1
            }

            // 돌아올때 간선개수가 2개이상이면 (8자형)
            graph[p].forEach {
                if (graph[it].size >= 2) {
                    return 3
                }

                queue.offer(it)
            }
        }
        // 위의 두조건 해당하지 않을경우 (막대)
        return 2
    }
}
