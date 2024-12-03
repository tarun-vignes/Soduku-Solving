import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class that implements a Depth-Limited Search approach to solving Sudoku puzzles
public class SudokuDLSSolver extends SudokuGraph {

    private int depthLimit; // Maximum depth for the DLS algorithm
    private int nodesExpanded; // Counter for the number of nodes expanded

    // Constructor that initializes the solver with a given grid size and depth limit
    public SudokuDLSSolver(int gridSize, int depthLimit) {
        super(gridSize);
        this.depthLimit = depthLimit;
        this.nodesExpanded = 0; // Initialize the counter
    }

    @Override
    public List<Map<Integer, Integer>> solve() {
        // Store all valid solutions found
        List<Map<Integer, Integer>> solutions = new ArrayList<>();
        // Start the recursive DLS from depth 0
        Map<Integer, Integer> initialState = new HashMap<>(puzzle);
        depthLimitedSearch(initialState, 0, solutions);
        return solutions;
    }

    // Recursive method for Depth-Limited Search
    private boolean depthLimitedSearch(Map<Integer, Integer> state, int depth, List<Map<Integer, Integer>> solutions) {
        // Increment the node expansion counter
        nodesExpanded++;

        // Check if the state is a solution
        if (isSolved(state)) {
            if (isValidSolution(state)) {
                // Add a copy of the solution to the list
                solutions.add(new HashMap<>(state));
            }
            return false; // Continue searching for other solutions
        }

        // Check if the depth limit has been reached
        if (depth >= depthLimit) {
            return false; // Cut off the search at this depth
        }

        // Find the next empty cell
        int nextCell = getNextEmptyCell(state);
        if (nextCell == -1) {
            return false; // No empty cells but puzzle is not solved, backtrack
        }

        // Try all valid choices for the next cell
        for (int choice : validChoices) {
            if (isValidChoice(state, nextCell, choice)) {
                // Create a new state with the choice applied
                state.put(nextCell, choice);
                // Recurse to the next depth level
                depthLimitedSearch(state, depth + 1, solutions);
                // Backtrack: remove the choice
                state.put(nextCell, 0);
            }
        }
        return false; // No valid choices led to a solution at this depth
    }

    // Getter for the number of nodes expanded
    public int getNodesExpanded() {
        return nodesExpanded;
    }
}