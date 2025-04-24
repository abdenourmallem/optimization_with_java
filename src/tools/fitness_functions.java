package tools;

import java.util.List;

public class fitness_functions {
    public static double fitness_function(double[] bitstring, List<Pairs> efficiency, int[] profits) {
        double fitness = 0.0;
        for (int i = 0; i < bitstring.length; i++) {
            fitness += bitstring[i] * profits[i] * efficiency.get(i).getValue();
        }
        return fitness;
    }

}