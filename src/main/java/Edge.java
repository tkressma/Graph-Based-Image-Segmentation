/* Represents the edge between two pixels. The weight is
   determined by the color distance of the pixels. */

public class Edge implements Comparable<Edge>{
    private int width;
    private double weight;
    private Pixel vertexA;
    private Pixel vertexB;
    private int vertexAPosition;
    private int vertexBPosition;

    public Edge(Pixel vertexA, Pixel vertexB, double weight, int width) {
            this.vertexA = vertexA;
            this.vertexB = vertexB;
            this.weight = weight;
            this.width = width;
            this.vertexAPosition = vertexA.getxCoordinate() + (vertexA.getyCoordinate() * width);
            this.vertexBPosition = vertexB.getxCoordinate() + (vertexB.getyCoordinate() * width);
    }

    // Prints the Edge object in a readable manner for debugging.
    @Override
    public String toString() {
        return "Edge{" +
                "weight=" + weight +
                ", vertexA=" + vertexA +
                ", vertexB=" + vertexB +
                '}';
    }

    // This sorts the edges by weight in non-descending order.
    // For all e in Elements, e <= nextElement.
    @Override
    public int compareTo(Edge o) {
        if (this.weight < o.getWeight()) {
            return -1;
        } else if (this.weight > o.getWeight()) {
            return 1;
        } else {
            return 0;
        }
    }

    // Getters
    public double getWeight() {
        return this.weight;
    }

    public int getvertexAPosition(){
        return vertexAPosition;
    }

    public int getvertexBPosition(){
        return vertexBPosition;
    }

}
