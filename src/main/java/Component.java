/* This class represents a component of the disjoint forest.
   Rank is determined by the height of the tree. The size is
   determined by how many elements (pixels) are in this component. */

public class Component {

    private int parent;
    private int rank;
    private int size;
    private double internalDifference;

    public Component(int parent, int rank, int size, double intDif) {
        this.parent = parent;
        this.rank = rank;
        this.size = size;
        this.internalDifference = intDif;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public double getInternalDifference() {
        return internalDifference;
    }

    public void setInternalDifference(double internalDifference) {
        this.internalDifference = internalDifference;
    }

}

