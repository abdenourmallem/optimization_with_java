package tools;

import java.util.List;

public class fitness_functions {
    public static double fitness_function(double[] bitstring, List<Pairs> efficiency, int[] profits) {
        double fitness = 0.0;
        for (int i = 0; i < bitstring.length; i++) {
                fitness += bitstring[i]*profits[i] * efficiency.get(i).getValue();
        }
        return fitness;
    }

    public static void main(String[] args) {
        // Example usage
        double[] bitstring = { 1, 0, 1 };
        int[][] weights = {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 } };
        int[] profits = { 10, 20, 30 };
        int[] capacities = { 5, 10, 15 };
        List<Pairs> efficiency = EffFuncs.general_efficiency(weights, profits, capacities);
        EffFuncs.printEfficiency(efficiency);
        // double fitness = fitness_function(bitstring, efficiency, profits);
        // System.out.println("Fitness: " + fitness);
    }
}