package tools;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalDouble;

public class EffFuncs {

    public static List<Pairs> general_efficiency(int[][] weights, int[] profits, int[] capacities) {
        int numRows = weights.length;
        int numCols = weights[0].length;
        int[] totalWeights = new int[numCols];
        double[] efficiencyScores = new double[numCols];
        double[] r_values = new double[numRows];

        for (int i = 0; i < numRows; i++) {
            int weightedSum = 0;
            for (int j = 0; j < numCols; j++) {
                weightedSum += weights[i][j];
            }
            totalWeights[i] = (int) weightedSum;
        }

        for (int i = 0; i < numRows; i++) {
            double r_value = (double)(totalWeights[i] - capacities[i]) / totalWeights[i];
            r_values[i] = r_value;
        }
        double[] columnSums = new double[numCols];
        for (int j = 0; j < numCols; j++) {
            double sum = 0.0;
            for (int i = 0; i < numRows; i++) {
                sum = r_values[i] * weights[i][j] + sum;
            }
            columnSums[j] = sum;
        }
        for (int j = 0; j < numCols; j++) {
            efficiencyScores[j] = profits[j] / columnSums[j];
        }
        OptionalDouble avg = Arrays.stream(efficiencyScores).average();
        List<Pairs> efficiency = new ArrayList<>();
        for (int i = 0; i < profits.length; i++) {
            efficiency.add(new Pairs(i, (efficiencyScores[i] - avg.getAsDouble())));
        }

        return efficiency;
    }

    public static Pairs calcSimpEff(int itmIdx, MKP mkpInstance) {
        int sumWeights = 0;
        for (int i = 0; i < mkpInstance.numConstraints; i++) {
            sumWeights += mkpInstance.weights[i][itmIdx];
        }
        double itmEff = mkpInstance.profits[itmIdx] / sumWeights;
        return new Pairs(itmIdx, itmEff);
    }

    public static void printEfficiency(List<Pairs> efficiency) {
        for (Pairs pair : efficiency) {
            System.out.println("ID: " + pair.getId() + ", Value: " + pair.getValue());
        }
    }

    public static void main(String[] args) {
        int[][] weights = {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 } };
        int[] profits = { 10, 20, 30 };
        int[] capacities = { 5, 10, 15 };
        List<Pairs> efficiencyScores = new ArrayList<Pairs>(general_efficiency(weights, profits, capacities));
        for (Pairs pair : efficiencyScores) {
            System.out.println(pair.value);
        }
    }

}