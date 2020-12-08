/* This class contains all of the components (C) that were formed as a result
   of partitioning the set of all vertices (V). In other words, the union
   of all c ∈ C = V. This can also be referred to as a disjoint forest.
   This class makes use of disjoint sets practical programming algorithm with union by rank to determine
   whether or not vertices are a part of the same disjoint forest or not.
   Watch more on disjoint sets here: https://www.youtube.com/watch?v=UBY4sF86KEY&feature=youtu.be */

import java.util.ArrayList;
import java.util.HashMap;

public class SegmentedComponents {
    private int totalVertices;
    private final HashMap<Integer, Component> comps = new HashMap<>();
    private final ArrayList<Edge> edges;

    public SegmentedComponents(int totalVertices, ArrayList<Edge> edges, int[] allPixels) {
        this.totalVertices = totalVertices;
        this.edges = edges;

        // Generates a new component for each pixel.
        System.out.println("Creating initial components. (1 pixel = 1 component).");
        for (int i = 0; i < allPixels.length; i++) {
            comps.put(allPixels[i], new Component(allPixels[i], 0, 1, 0.0));
        }

    }

    public int find(int edge) {
        if (comps.get(edge).getParent() == edge) {
            return edge;
        } else {
            return find(comps.get(edge).getParent());
        }
    }

    public void union(int a, int b, double weight) {
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return;
        }

        if (comps.get(rootA).getRank() > comps.get(rootB).getRank()) {
            comps.get(rootB).setParent(rootA);
            comps.get(rootA).setSize(comps.get(rootA).getSize() + comps.get(rootB).getSize());
            comps.get(rootA).setInternalDifference(weight);
        } else if (comps.get(rootB).getRank() > comps.get(rootA).getRank()) {
            comps.get(rootA).setParent(rootB);
            comps.get(rootB).setSize(comps.get(rootB).getSize() + comps.get(rootA).getSize());
            comps.get(rootB).setInternalDifference(weight);
        } else {
            comps.get(rootA).setParent(rootB);
            int currentRank = comps.get(rootB).getRank();
            comps.get(rootB).setRank(currentRank + 1);
            comps.get(rootB).setSize(comps.get(rootB).getSize() + comps.get(rootA).getSize());
            comps.get(rootB).setInternalDifference(weight);
        }
    }

    public int getComponentSize(int x) {
        return comps.get(find(x)).getSize();
    }

    public double getInternalDifference(int vertex){
        int root = find(vertex);
        return comps.get(root).getInternalDifference();
    }

}


