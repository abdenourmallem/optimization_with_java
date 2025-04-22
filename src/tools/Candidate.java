package tools;

import java.util.Arrays;

public class Candidate {
    int size;
    public double[] position;
    public double objValue;
    public double fitness;

    // Constructor to create a candidate with a given position
    public Candidate(MKP mkpInstance, double[] position) {
        this.size = mkpInstance.numItems;
        this.position = position;
        this.objValue = calcObjVal(mkpInstance.profits);
        this.fitness = calcFitness(mkpInstance.profits);
    }

    // Constructor to create a candidate with an efficient position
    public Candidate(MKP mkpInstance, int effBias) {
        this.size = mkpInstance.numItems;
        this.position = creEffPos(mkpInstance, effBias);
        this.objValue = calcObjVal(mkpInstance.profits);
    }

    public double calcObjVal(int[] profits) {
        double obj_val = (double) 0;
        for (int i = 0; i < profits.length; i++) {
            obj_val += position[i] * profits[i];
        }
        return obj_val;
    }

    public double calcFitness(int[] profits) {
        double fitness = 0.0;

        // Calculate the fitness

        return fitness;
    }

    public double[] creEffPos(MKP mkpInstance, int effBias) {
        double[] effPos = new double[mkpInstance.numItems];

        int[] remCapac = Arrays.copyOf(mkpInstance.capacities, mkpInstance.numConstraints);

        double[] probBias = new double[mkpInstance.numItems];
        for (int i = 1; i < mkpInstance.numItems + 1; i++) {
            probBias[i - 1] = 1 - i * (1 / ((mkpInstance.numItems - 1) * (double) effBias));
        }

        for (int i = 0; i < mkpInstance.numItems; i++) {
            int itm = mkpInstance.simpSortedItms[i];
            if (canFitItem(itm, remCapac, mkpInstance.weights)) {
                if (Math.random() < probBias[i]) {
                    effPos[itm] = 1.0;
                    for (int j = 0; j < mkpInstance.numConstraints; j++) {
                        remCapac[j] -= mkpInstance.weights[j][itm];
                    }
                }
            } else {
                break;
            }
        }
        return effPos;
    }

    private boolean canFitItem(int item, int[] remainingCapac, int[][] weights) {
        for (int i = 0; i < remainingCapac.length; i++) {
            if (remainingCapac[i] - weights[i][item] < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean checkConstraints(MKP mkpInstance) {
        int[] totalWeights = new int[mkpInstance.numConstraints];
        for (int i = 0; i < mkpInstance.numConstraints; i++) {
            int weightedSum = 0;
            for (int j = 0; j < mkpInstance.numItems; j++) {
                weightedSum += mkpInstance.weights[i][j] * this.position[j];
            }
            totalWeights[i] = (int) weightedSum;
        }
        for (int j = 0; j < mkpInstance.numConstraints; j++) {
            if (totalWeights[j] > mkpInstance.capacities[j]) {
                return false;
            }
        }
        return true;
    }

}
