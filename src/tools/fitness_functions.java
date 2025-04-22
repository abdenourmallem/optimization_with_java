package tools;

import java.util.List;

public class fitness_functions {
    public static double fitness_function(int[] bitstring, List<pairs> efficiency, double[] profits) {
        double fitness = 0.0;
        for (int i = 0; i < bitstring.length; i++) {
            if (bitstring[i] == 1) {
                fitness += profits[i] * efficiency.get(i).getValue();
            }
        }
        return fitness;
    }

    public static void main(String[] args) {
        // Example usage
        int[] bitstring = { 1, 0, 1 };
        int[][] weights = {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 } };
        int[] profits = { 10, 20, 30 };
        int[] capacities = { 5, 10, 15 };
        List<pairs> efficiency = efficiency_functions.general_efficiency(weights, profits, capacities);
        efficiency_functions.printEfficiency(efficiency);
        // double fitness = fitness_function(bitstring, efficiency, profits);
        // System.out.println("Fitness: " + fitness);
    }
}