package socialnetwork.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    int V;
    Map<Long, ArrayList<Long>> adjList;

    /**
     * creates a graph with v vertices
     *
     * @param V - number of vertices
     */
    public Graph(int V) {
        this.V = V;
        adjList = new HashMap<>();
    }

    public void addVertex(long i) {
        if (!adjList.containsKey(i))
            adjList.put(i, new ArrayList<>());
    }

    /**
     * add an edge from i to j and an edge from j to i
     *
     * @param i i vertex from the graph
     * @param j j vertex from the graph
     */
    public void addEdge(long i, long j) {
        if (!adjList.containsKey(i))
            adjList.put(i, new ArrayList<>());
        adjList.get(i).add(j);

        if (!adjList.containsKey(j))
            adjList.put(j, new ArrayList<>());
        adjList.get(j).add(i);
    }

    /**
     * find all the reachable vertices from a source vertex
     *
     * @param x       - the start vertex
     * @param visited - contains all visited vertices
     */
    void DFS(long x, List<Long> visited) {
        visited.add(x);
        for (long y : adjList.get(x)) {
            if (!visited.contains(y))
                DFS(y, visited);
        }
    }

    /**
     * find all the reachable vertices from a source vertex
     *
     * @param v          - the source vertex
     * @param visited    - all visited vertices
     * @param components - the vertices from the current connected component
     * @param sizecon    - size of the connected component
     * @return - the size of the connected component
     */
    public int DFS2(long v, List<Long> visited, List<Long> components, int sizecon) {
        visited.add(v);
        sizecon++;
        for (long x : adjList.get(v)) {
            if (!visited.contains(x)) {
                components.add(x);
                sizecon = DFS2(x, visited, components, sizecon);
            }
        }

        return sizecon;
    }

    /**
     * @return the number of connected components
     */
    public int nrOfComponents() {
        List<Long> visited = new ArrayList<>();
        int nrComp = 0;
        for (long key : adjList.keySet()) {

            if (!visited.contains(key)) {
                DFS(key, visited);
                nrComp++;
            }
        }
        int izolated = this.V - adjList.keySet().size();
        return nrComp + izolated;
        //return  nrComp;
    }

    /**
     * @return the vertices from the largest connected component
     */
    public List<Long> getLargestComponent() {
        List<Long> visited = new ArrayList<>();
        int maxComp = 0;
        List<Long> largestComp = new ArrayList<>();
        for (long key : adjList.keySet()) {
            if (!visited.contains(key) && adjList.get(key).size() != 0) {
                List<Long> componentEl = new ArrayList<>();
                componentEl.add(key);
                int sizecon = 0;
                int x = DFS2(key, visited, componentEl, sizecon);
                if (x > maxComp) {
                    maxComp = x;
                    largestComp = componentEl;
                }
            }
        }
        return largestComp;
    }

    public List<Long> getComponent(Long id) {
        List<Long> visited = new ArrayList<>();
        List<Long> component = new ArrayList<>();
        component.add(id);
        int x = 0;
        x = DFS2(id, visited, component, x);

        return component;
    }
}
