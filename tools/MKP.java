import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MKP {
    public int[] details; // Contains an array with three elements: [numItems,numConstraints,Optimum],
                          // Optimum unused for now
    public int[] profits;
    public int[][] weights;
    public int[] capacities;

    public MKP(String filepath) {
        try {
            Object[] result = parseFile(filepath);

            this.details = (int[]) result[0];
            this.profits = (int[]) result[1];
            this.weights = (int[][]) result[2];
            this.capacities = (int[]) result[3];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object[] parseFile(String filepath) throws IOException {
        // Read the file
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line = reader.readLine();

        // Parse all numbers from the file
        List<Integer> numbers = new ArrayList<>();
        while (line != null) {
            String[] tokens = line.trim().split("\\s+");
            for (String token : tokens) {
                numbers.add(Integer.parseInt(token));
            }
            line = reader.readLine();
        }
        reader.close();

        // Extract the number of variables, constraints, and objective value
        int numVariables = numbers.get(0);
        int numConstraints = numbers.get(1);
        int objValue = numbers.get(2);

        // Extract the profit array
        List<Integer> profitList = numbers.subList(3, 3 + numVariables);

        // Extract the constraint matrix
        int start = 3 + numVariables;
        int end = start + numConstraints * numVariables;
        List<List<Integer>> constraintMatrix = new ArrayList<>();
        for (int i = start; i < end; i += numVariables) {
            List<Integer> row = numbers.subList(i, i + numVariables);
            constraintMatrix.add(row);
        }

        // Extract the capacity list
        List<Integer> capacityList = numbers.subList(end, end + numConstraints);

        // Convert the lists to arrays
        int[] profitArray = profitList.stream().mapToInt(i -> i).toArray();
        int[][] constraintArray = new int[numConstraints][numVariables];
        for (int i = 0; i < numConstraints; i++) {
            for (int j = 0; j < numVariables; j++) {
                constraintArray[i][j] = constraintMatrix.get(i).get(j);
            }
        }
        int[] capacityArray = capacityList.stream().mapToInt(i -> i).toArray();

        // Return all the parsed data in an array
        return new Object[] {
                new int[] { numVariables, numConstraints, objValue },
                profitArray,
                constraintArray,
                capacityArray
        };
    }

    public static void main(String[] args) {
        String filepath = "..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat";
        try {
            Object[] result = parseFile(filepath);
            // Example of how to access the result
            int[] problemDetails = (int[]) result[0];
            int[] profitArray = (int[]) result[1];
            int[][] constraintArray = (int[][]) result[2];
            int[] capacityArray = (int[]) result[3];

            // Print the result for verification
            System.out.println("Problem Details: " + Arrays.toString(problemDetails));
            System.out.println("Profit Array: " + Arrays.toString(profitArray));
            System.out.println("Constraint Matrix: ");
            for (int[] row : constraintArray) {
                System.out.println(Arrays.toString(row));
            }
            System.out.println("Capacity Array: " + Arrays.toString(capacityArray));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
