import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main Sudoku solver class that implements a multithreaded solution using BFS and DLS approaches.
 */
public class Sudoku {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        // Input and output streams
        String fileName = "puzzle.txt";
        if (args.length >= 1) {
            fileName = args[0];
        }
        FileInputStream file_in = new FileInputStream(fileName);
        Scanner scanner = new Scanner(file_in);

        // Create a list to store each line of the puzzle input
        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        // Determine the size of the Sudoku grid from the input
        int gridSize = inferGridSize(lines);

        // Ensure the grid size is valid (must be a perfect square)
        if (!isValidGridSize(gridSize)) {
            System.out.println("Error: Invalid grid size. Only square grids with sizes that are perfect squares are supported.");
            return;
        }

        // Initialize the BFS solver
        SudokuBFSSolver bfsSolver = new SudokuBFSSolver(gridSize);
        bfsSolver.parseInput(lines);

        // Initialize the DLS solver
        SudokuDLSSolver dlsSolver = new SudokuDLSSolver(gridSize, calculateDepthLimit(bfsSolver));
        dlsSolver.parseInput(lines);

        // Create a thread pool with 2 threads to handle parallel solving
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Define the BFS solving task
        Callable<Void> bfsTask = () -> {
            System.out.println("Solving using BFS...");
            long startTime = System.nanoTime();
            List<Map<Integer, Integer>> bfsSolutions = bfsSolver.solve();
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1e9; // Convert to seconds
            int validSolution = bfsSolutions.size();

            // Write valid solutions to an output file
            try (PrintWriter writer = new PrintWriter(new FileOutputStream("bfs_solutions.txt"))) {
                writer.println("Solutions via BFS:");
                for (Map<Integer, Integer> solution : bfsSolutions) {
                    bfsSolver.printSolution(writer, solution);
                }
                writer.println("Execution Time: " + duration + " seconds");
                writer.println("Nodes Expanded: " + bfsSolver.getNodesExpanded());
            }
            System.out.println("Solutions found via BFS: " + validSolution);
            System.out.println("BFS Execution Time: " + duration + " seconds");
            System.out.println("BFS Nodes Expanded: " + bfsSolver.getNodesExpanded());
            return null;
        };

        // Define the DLS solving task
        Callable<Void> dlsTask = () -> {
            System.out.println("Solving using DLS...");
            long startTime = System.nanoTime();
            List<Map<Integer, Integer>> dlsSolutions = dlsSolver.solve();
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1e9; // Convert to seconds
            int validSolution = dlsSolutions.size();

            // Write valid solutions to an output file
            try (PrintWriter writer = new PrintWriter(new FileOutputStream("dls_solutions.txt"))) {
                writer.println("Solutions via DLS:");
                for (Map<Integer, Integer> solution : dlsSolutions) {
                    dlsSolver.printSolution(writer, solution);
                }
                writer.println("Execution Time: " + duration + " seconds");
                writer.println("Nodes Expanded: " + dlsSolver.getNodesExpanded());
            }
            System.out.println("Solutions found via DLS: " + validSolution);
            System.out.println("DLS Execution Time: " + duration + " seconds");
            System.out.println("DLS Nodes Expanded: " + dlsSolver.getNodesExpanded());
            return null;
        };

        // Submit tasks to executor
        Future<Void> bfsFuture = executor.submit(bfsTask);
        Future<Void> dlsFuture = executor.submit(dlsTask);

        // Wait for completion
        bfsFuture.get();
        dlsFuture.get();

        executor.shutdown();
        scanner.close();
    }

    /**
     * Determines the size of the Sudoku grid based on input dimensions.
     * Assumes the grid is square (same number of rows and columns).
     * @param lines List of input lines representing the puzzle
     * @return The size of one dimension of the grid
     */
    private static int inferGridSize(List<String> lines) {
        return lines.size();
    }

    /**
     * Validates that the grid size is a perfect square.
     * @param gridSize The size to validate
     * @return true if the size is valid (perfect square), false otherwise
     */
    private static boolean isValidGridSize(int gridSize) {
        int sqrt = (int) Math.sqrt(gridSize);
        return sqrt * sqrt == gridSize;
    }

    /**
     * Calculates the maximum depth for DLS (Depth-Limited Search) based on empty cells.
     * @param graph The Sudoku puzzle graph
     * @return The number of empty cells in the puzzle
     */
    private static int calculateDepthLimit(SudokuGraph graph) {
        return (int) graph.getPuzzle().values().stream().filter(v -> v == 0).count();
    }
}
