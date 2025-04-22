package tools;

import java.util.List;
import java.util.ArrayList;

public class util {
    public static double objective_function(int[] bitstring, double[] profits) {
        double objective = 0.0;
        for (int i = 0; i < bitstring.length; i++) {
                objective += profits[i]*bitstring[i];
            
        }
        return objective;
    }

    public static boolean check_constraints(double[] bitstring, double[] capacities, double[][] weights) {
        double[] totalWeights = new double[capacities.length];
        double numCols = weights[0].length;
        double numRows = weights.length;
        for (int i = 0; i < numRows; i++) {
            int weightedSum = 0;
            for (int j = 0; j < numCols; j++) {
                weightedSum += weights[i][j]*bitstring[j];
            }
            totalWeights[i] = (int) weightedSum;
        }
        for (int j = 0; j < numRows; j++) {
            if (totalWeights[j] > capacities[j]) {
                return false;
            }
        }
        return true;
    }

    

    public static List<Double> probability_distribution(int n_items, double r1, double r2) {
        double r = (r2 - r1) / (n_items - 1);
        List<Double> prob = new ArrayList<>();
        for (int i = 0; i < n_items; i++) {
            prob.add(r2 - (i * r));
        }
        return prob;
    }
}
