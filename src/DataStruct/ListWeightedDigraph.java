package DataStruct;

import java.util.ArrayList;
import java.util.List;

class ListWeightedDigraph implements Graph {

    private List<Edge>[] graph;

    public ListWeightedDigraph(int n) {
        graph = new List[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
    }

    public void addEdge(int from, int to, int weight) {
        graph[from].add(new Edge(to, weight));
    }

    public void removeEdge(int from, int to) {
        for (int i = 0; i < graph[from].size(); i++) {
            if (graph[from].get(i).to == to) {
                graph[from].remove(i);
                break;
            }
        }
    }

    public boolean hasEdge(int from, int to) {
        for (Edge e : graph[from]) {
            if (e.to == to) {
                return true;
            }
        }
        return false;
    }

    public int weight(int from, int to) {
        for (Edge e : graph[from]) {
            if (e.to == to) {
                return e.weight;
            }
        }
        throw new IllegalArgumentException("no such edge");
    }

    public List<Edge> neighbors(int v) {
        return graph[v];
    }

    @Override
    public int size() {
        return 0;
    }

    void traverse(Graph graph, int s, boolean[] visited) {
        if (s < 0 || s >= graph.size()) {
            return;
        }

        if (visited[s]) {
            return;
        }

        visited[s] = true;
        System.out.println("visit: " + s);
        for (Edge e : graph.neighbors(s)) {
            traverse(graph, e.to, visited);
        }
    }

    void traverseEdges(Graph graph, int s, boolean[][] visited) {
        if (s < 0 || s >= graph.size()) {
            return;
        }
        for (Edge e : graph.neighbors(s)) {
            if (visited[s][e.to]) {
                continue;
            }
            visited[s][e.to] = true;
            System.out.println("visit edge: " + s + "->" + e.to);
            traverseEdges(graph, e.to, visited);
        }
    }

    public static void main(String[] args) {
        ListWeightedDigraph graph = new ListWeightedDigraph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 0, 3);
        graph.addEdge(2, 1, 4);

        System.out.println(graph.hasEdge(0, 1)); // true
        System.out.println(graph.hasEdge(1, 0)); // false

        graph.neighbors(2).forEach(edge -> {
            System.out.println(2 + " -> " + edge.to + ", wight: " + edge.weight);
        });
        // 2 -> 0, wight: 3
        // 2 -> 1, wight: 4

        graph.removeEdge(0, 1);
        System.out.println(graph.hasEdge(0, 1)); // false
    }
}
