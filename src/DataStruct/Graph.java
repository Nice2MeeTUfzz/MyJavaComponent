package DataStruct;

import java.util.List;

public interface Graph {
    class Edge {
        int weight;
        int to;

        public Edge(int weight, int to) {
            this.weight = weight;
            this.to = to;
        }
    }

    void addEdge(int from, int to, int weight);

    void removeEdge(int from, int to);

    boolean hasEdge(int from, int to);

    int weight(int from, int to);

    List<Edge> neighbors(int v);

    int size();
}
