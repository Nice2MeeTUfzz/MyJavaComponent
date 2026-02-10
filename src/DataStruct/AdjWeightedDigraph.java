package DataStruct;

import java.util.ArrayList;
import java.util.List;

public class AdjWeightedDigraph implements Graph {

    private int[][] matrix;

    public AdjWeightedDigraph(int n) {
        matrix = new int[n][n];
    }

    @Override
    public void addEdge(int from, int to, int weight) {
        matrix[from - 1][to - 1] = weight;
    }

    @Override
    public void removeEdge(int from, int to) {
        matrix[from - 1][to - 1] = 0;
    }

    @Override
    public boolean hasEdge(int from, int to) {
        if (matrix[from - 1][to - 1] > 0) {
            return true;
        }
        return false;
    }

    @Override
    public int weight(int from, int to) {
        return matrix[from - 1][to - 1];
    }

    public List<Edge> neighbors(int v) {
        List<Edge> res = new ArrayList<>();
        for (int i = 0; i < matrix[v].length; i++) {
            if (matrix[v][i] != 0) {
                res.add(new Edge(i, matrix[v][i]));
            }
        }
        return res;
    }

    @Override
    public int size() {
        return 0;
    }

    public static void main(String[] args) {
        AdjWeightedDigraph graph = new AdjWeightedDigraph(3);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 2);
        graph.addEdge(3, 1, 3);
        graph.addEdge(3, 2, 4);

        System.out.println(graph.hasEdge(1, 2)); // true
        System.out.println(graph.hasEdge(2, 1)); // false

        graph.neighbors(2).forEach(edge -> {
            System.out.println(2 + " -> " + edge.to + ", wight: " + edge.weight);
        });
        // 2 -> 0, wight: 3
        // 2 -> 1, wight: 4

        graph.removeEdge(1, 2);
        System.out.println(graph.hasEdge(1, 2)); // false
    }
}
