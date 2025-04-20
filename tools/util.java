package tools;


public class util {
    public static double objective_function(int[] bitstring, double[] profits) {
        double objective = 0.0;
        for (int i = 0; i < bitstring.length; i++) {
            if (bitstring[i] == 1) {
                objective += profits[i];
            }
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
                weightedSum += weights[i][j];
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
}


