import ij.*;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/* This is an implementation of a Graph Based Image Segmentation Algorithm
   proposed by  P. Felzenszwalb, D. Huttenlocher International Journal of
   Computer Vision, Vol. 59, No. 2, September 2004. */

public class Main {

    // Algorithm Variables
    private static double sigmaValue = 1.0;
    // Sets a scale of observation. A larger k value causes a preference for larger components.
    private static double kValue = 1000;
    private static int minSize = 50;

    public static void main(String[] args) throws IOException {
        //taskOne();

        // Test images: people.png, sunflower.png, sunset.png, beach.gif
        ImagePlus originalImage = new ImagePlus("sunflower.png");
        double start = System.currentTimeMillis();
        ImageProcessor segmentationProcessor = originalImage.getProcessor();
        segmentImage(segmentationProcessor);
        double end = System.currentTimeMillis();
        System.out.println("Finished segmenting the image. Total runtime: " + (end - start) + "ms");
        IJ.saveAs("png", "tee");

    }

    // Manages the graph-based image segmentation
    private static void segmentImage(ImageProcessor segmentationProcessor) {
        // Stores the width and height of the image.
        final int width = segmentationProcessor.getWidth();
        final int height = segmentationProcessor.getHeight();
        final int totalVertices = width * height;
        final int totalEdges = (((width - 1) * height) + ((height - 1) * width) + (2 * ((width - 1) * (height - 1))));

        // Fills the array with colors. This is used to color the components later.
        int[][] colorList = new int[width * height][3];
        fillColorList(totalVertices, colorList);

        ImageProcessor imgProcessor = segmentationProcessor.duplicate();
        imgProcessor.blurGaussian(sigmaValue);

        // Creates a graph. Generates all vertices (pixels) and edges.
        System.out.println("Creating graph...");
        ArrayList<Edge> edges = generateEdges(segmentationProcessor, width, height);

        // Sorts the edges by weight value in non-descending order.
        Collections.sort(edges);

        // DEBUGGING EDGE CREATION
//        for (Edge edge : edges) {
//            for (int x = 0; x < 3; x++) {
//                System.out.println("Edge: (" + edge.getVertexA().getxCoordinate() + ", " + edge.getVertexA().getyCoordinate() + ") <======> (" + edge.getVertexB().getxCoordinate() + ", " + edge.getVertexB().getyCoordinate() + ") | WEIGHT: " + edge.getWeight() + " | RGB Vals: " + edge.getVertexA().get_rgb() + " " + edge.getVertexB().get_rgb());
//            }
//        }

        System.out.println("Graph created with " + totalVertices + " vertices and " + totalEdges + " edges.");

        // Create a matrix of every possible pixel position
        int[] allPixels = new int[totalVertices];
        int current = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                    allPixels[current] = x + (y * width);
                    current++;
            }
        }

        System.out.println("Starting to segment the image...");
        // Partition the vertex set V of the generated graph into components
        SegmentedComponents sv = new SegmentedComponents(totalVertices, edges, allPixels);

        // Connect the partitioned components based on weight of connection.
        System.out.println("Connecting similar components based on internal difference (highest edge weight in component).");
        connectComponents(sv, edges);

        // Color the components
        System.out.println("Coloring each component with a unique color.");
        ImageProcessor finalImage = colorSegments(imgProcessor, sv, width, height, colorList);

        // Displays the segmented image on screen
        new ImagePlus("Image Result", finalImage).show();
    }

    private static void connectComponents(SegmentedComponents sv, ArrayList<Edge> edges) {
        for (Edge edge : edges) {
            int vertexA = edge.getvertexAPosition();
            int vertexB = edge.getvertexBPosition();

            if (sv.find(vertexA) != sv.find(vertexB)) {
                    mergeCondition(sv, edge, vertexA , vertexB);
            }
        }

        // Connect the smaller components
        for (Edge edge : edges) {
            int vertexA = edge.getvertexAPosition();
            int vertexB = edge.getvertexBPosition();

            // Merges components if they are below the minimum component size.
            if(sv.find(vertexA) != (sv.find(vertexB)) && (sv.getComponentSize(vertexA)<minSize) || sv.getComponentSize(vertexB)<minSize) {
                sv.union(vertexA, vertexB, edge.getWeight());
            }
        }
    }


    // Tests the merge condition weight(edge) <= intDif(component) + (k/threshold)
    // If true, merge the two components.
    private static void mergeCondition(SegmentedComponents sv, Edge edge, int vertexA, int vertexB) {
        // Threshold value = (kValue / |C|)
        double thresholdFunctionA = kValue / sv.getComponentSize(vertexA);
        double thresholdFunctionB = kValue / sv.getComponentSize(vertexB);
        double intDifThresholdA = sv.getInternalDifference(vertexA) + thresholdFunctionA;
        double intDifThresholdB = sv.getInternalDifference(vertexB) + thresholdFunctionB;
        if ((edge.getWeight() <= intDifThresholdA) && (edge.getWeight() <= intDifThresholdB)) {
            sv.union(vertexA, vertexB, edge.getWeight());
        }
    }

    private static ArrayList<Edge> generateEdges (ImageProcessor segmentationProcessor,int width, int height){
            ArrayList<Edge> allEdges = new ArrayList<>();
            int[] vertexA_rgb = new int[3];
            int[] vertexB_rgb = new int[3];

            // Iterates through each pixel starting from the top left of the image (0,0).
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    // Creates edge connecting to vertex on the right of the current vertex.
                    if (y < height - 1) {
                        // System.out.println(x + ", " + y + " ==EDGE==> " + x + ", " + (y + 1));
                        segmentationProcessor.getPixel(x, y, vertexA_rgb);
                        segmentationProcessor.getPixel(x, (y + 1), vertexB_rgb);

                        Edge newEdge = new Edge(new Pixel(x, y), new Pixel(x, y + 1), getEdgeWeight(vertexA_rgb, vertexB_rgb), width);
                        allEdges.add(newEdge);
                    }

                    // Creates edge connecting to vertex below the current vertex.
                    if (x < width - 1) {
                        // System.out.println(x + ", " + y + " ==EDGE==> " + (x+1) + ", " + (y));
                        segmentationProcessor.getPixel(x, y, vertexA_rgb);
                        segmentationProcessor.getPixel((x + 1), y, vertexB_rgb);


                        Edge newEdge = new Edge(new Pixel(x, y), new Pixel(x + 1, y), getEdgeWeight(vertexA_rgb, vertexB_rgb), width);
                        allEdges.add(newEdge);
                    }

                    // Creates edge connecting to vertex diagonally (right) of the current vertex.
                    if (x < width - 1 && y < height - 1) {
                        // System.out.println(x + ", " + y + " ==EDGE==> " + (x+1) + ", " + (y + 1));
                        segmentationProcessor.getPixel(x, y, vertexA_rgb);
                        segmentationProcessor.getPixel((x + 1), (y + 1), vertexB_rgb);

                        Edge newEdge = new Edge(new Pixel(x, y), new Pixel(x + 1, y + 1), getEdgeWeight(vertexA_rgb, vertexB_rgb), width);
                        allEdges.add(newEdge);
                    }

                    // Creates edge connecting to vertex diagonally (left) of the current vertex.
                    if (!(y == 0) && x < width - 1) {
                        // System.out.println(x + ", " + y + " ==EDGE==> " + (x+1) + ", " + (y-1));
                        segmentationProcessor.getPixel(x, y, vertexA_rgb);
                        segmentationProcessor.getPixel((x + 1), (y - 1), vertexB_rgb);

                        Edge newEdge = new Edge(new Pixel(x, y), new Pixel(x + 1, y - 1), getEdgeWeight(vertexA_rgb, vertexB_rgb), width);
                        allEdges.add(newEdge);
                    }

                }
            }
            return allEdges;
        }

    /* Returns the weight of the edge connecting two vertices.
       Based on the color difference, or distance between two colors.
       Read more here: https://en.wikipedia.org/wiki/Color_difference */
        private static double getEdgeWeight (int[] rgb_vertexA, int[] rgb_vertexB){
            double r_difference = (rgb_vertexB[0] - rgb_vertexA[0]);
            double r_difference_squared = Math.pow(r_difference, 2);
            double g_difference = (rgb_vertexB[1] - rgb_vertexA[1]);
            double g_difference_squared = Math.pow(g_difference, 2);
            double b_difference = (rgb_vertexB[2] - rgb_vertexA[2]);
            double b_difference_squared = Math.pow(b_difference, 2);
            double edgeWeight = Math.sqrt(r_difference_squared + g_difference_squared + b_difference_squared);
            return edgeWeight;
        }

    private static ImageProcessor colorSegments(ImageProcessor imgProcessor, SegmentedComponents sv, int width, int height, int[][] colorList) {
        ImageProcessor finalImage = imgProcessor.duplicate();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentVertex = sv.find((y * width) + x);
                finalImage.putPixel(x, y, colorList[currentVertex]);
            }
        }
        return finalImage;
    }

    private static void fillColorList(int totalVertices, int[][] colorList) {
        for (int i = 0; i < totalVertices; i++) {
            for (int j = 0; j < 3; j++) {
                colorList[i][j] = (int) (Math.random() * 255);
            }
        }
    }

    // Takes an image and blacks out the top 50% of the image.
    private static void taskOne() {
        ImagePlus taskOneImg = new ImagePlus("Building.jpg");
        ImageProcessor blackoutProcessor = taskOneImg.getProcessor();

        int[] rgb = new int[3];

        System.out.println("Blacking out the top 50% of the photo using imageJ.");
        for (int i = 0; i < taskOneImg.getWidth(); i++) {
            for (int j = 0; j < taskOneImg.getHeight() / 2; j++) {
                blackoutProcessor.getPixel(i, j, rgb);
                blackoutProcessor.set(i, j, 0);
            }
        }

        taskOneImg.show();
        System.out.println("Finished.");
        IJ.saveAs("jpg", "Blackout.jpg");
    }
}
