package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MKP {
    public int numItems;
    public int numConstraints;
    public int optimum;
    public int[] profits;
    public int[][] weights;
    public int[] capacities;
    public int[] simpSortedItms;
    public int[] genSortedItms;

    // Constructor, receives a filepath to the data and create all mkp details
    public MKP(String filepath) {
        try {
            Object[] result = readMKPData(filepath);
            this.numItems = (int) result[0];
            this.numConstraints = (int) result[1];
            this.optimum = (int) result[2];
            this.profits = (int[]) result[3];
            this.weights = (int[][]) result[4];
            this.capacities = (int[]) result[5];
            this.simpSortedItms = sortItmsBySimpEff();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printMKPDetails() {
        System.out.println(this.numItems);
        System.out.println(this.numConstraints);
        System.out.println(Arrays.toString(this.profits));
        for (int[] row : this.weights) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println(Arrays.toString(this.capacities));
    }

    public static Object[] readMKPData(String filepath) throws IOException {
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
                numVariables,
                numConstraints,
                objValue,
                profitArray,
                constraintArray,
                capacityArray
        };
    }

    public Pairs calcSimpEff(int itmIdx) {
        int sumWeights = 0;
        for (int i = 0; i < this.numConstraints; i++) {
            sumWeights += this.weights[i][itmIdx];
        }
        double itmEff = this.profits[itmIdx] / (double) sumWeights;
        return new Pairs(itmIdx, itmEff);
    }

    public int[] sortItmsBySimpEff() {
        Pairs[] itmPairs = new Pairs[this.numItems];

        for (int i = 0; i < this.numItems; i++) {
            itmPairs[i] = calcSimpEff(i);
        }
        Arrays.sort(itmPairs);
        int[] sortedItms = new int[this.numItems];
        for (int i = 0; i < this.numItems; i++) {
            sortedItms[i] = itmPairs[i].getId();
        }
        return sortedItms;
    }
}
