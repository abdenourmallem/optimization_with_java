package tools;

import java.util.Arrays;

public class Candidate {
    public int size;
    public double[] position;
    public double objValue;
    public double fitness;

    // Constructor to create a candidate with a given position
    public Candidate(MKP mkpInstance, double[] position) {
        this.size = mkpInstance.numItems;
        this.position = position;
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    // Constructor to create a candidate with an efficient position
    public Candidate(MKP mkpInstance, int effBias) {
        this.size = mkpInstance.numItems;
        this.position = creEffPos(mkpInstance, effBias);
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    public double calcObjVal(MKP mkpInstance) {
        double obj_val = (double) 0;
        for (int i = 0; i < mkpInstance.profits.length; i++) {
            obj_val += (double) (position[i] * mkpInstance.profits[i]);
        }
        return obj_val;
    }

    public double calcFitness(MKP mkpInstance) {
        double fitness = fitness_functions.fitness_function(this.position, mkpInstance.EffList, mkpInstance.profits);
        return fitness;
    }

    public void updatePosition(MKP mkpInstance, double[] pos) {
        for (int i = 0; i < mkpInstance.numItems; i++) {
            this.position[i] = pos[i];
        }
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    public double[] creEffPos(MKP mkpInstance, int effBias) {
        double[] effPos = new double[mkpInstance.numItems];

        int[] remCapac = Arrays.copyOf(mkpInstance.capacities, mkpInstance.numConstraints);

        double[] probBias = new double[mkpInstance.numItems];
        for (int i = 1; i < mkpInstance.numItems + 1; i++) {
            probBias[i - 1] = 1 - i * (1 / ((mkpInstance.numItems - 1) * (double) effBias));
        }

        for (int i = 0; i < mkpInstance.numItems; i++) {
            int itm = mkpInstance.SortedItems.get(i).getId();
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

    public void applyTransferFunc(MKP mkpInstance) {
        // System.out.println("before applying transfer funct: ");
        // this.printPos();

        for (int i = 0; i < this.size; i++) {
            this.position[i] = TransferFunc.shiftedSigmoid2(this.position[i]);
        }
        for (int i = 0; i < this.size; i++) {
            if (Math.random() < this.position[i]) {
                this.position[i] = 1;
            } else {
                this.position[i] = 0;
            }
        }
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
        // System.out.println("after applying transfer funct: ");
        // this.printPos();
    }

    public void repairPosition(MKP mkpInstance) {
        if (checkConstraints(mkpInstance)) {
            return;
        }
        // System.out.println("before repair: ");
        // this.printObj();

        double[] totalWeight = this.computeTotalWeight(mkpInstance);

        for (int i = mkpInstance.numItems - 1; i >= 0; i--) {
            int item = mkpInstance.SortedItems.get(i).getId();
            if (anyExceeds(totalWeight, mkpInstance.capacities)) {
                this.position[item] = (double) 0;
                totalWeight = this.computeTotalWeight(mkpInstance);
            } else {
                break;
            }
        }

        for (int i = 0; i < mkpInstance.numItems; i++) {
            int item = mkpInstance.SortedItems.get(i).getId();
            if (canAddItem(totalWeight, item, mkpInstance)) {
                this.position[item] = (double) 1;
                totalWeight = this.computeTotalWeight(mkpInstance);
            } else {
                break;
            }
        }
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
        // System.out.println("after repair: ");
        // this.printObj();
    }

    public void localSearch(MKP mkpInstance) {
        // System.out.println("Before local search: ");
        // this.printObj();

        Candidate best = new Candidate(mkpInstance, this.position);

        for (int i = 0; i < mkpInstance.numItems; i++) {
            if (this.position[i] == 0) {
                best.position[i] = 1;
                if (!best.checkConstraints(mkpInstance)) {
                    best.position[i] = 0;
                }
            }
        }
        for (int i = 0; i < mkpInstance.numItems; i++) {
            this.position[i] = best.position[i];
        }
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
        // System.out.println("after local search: ");
        // this.printObj();

    }

    public void printPos() {
        System.out.println("cand Position: " + Arrays.toString(this.position));
        // System.out.println("cand fitness: " + this.fitness);
    }

    public void printObj() {
        System.out.println("Obj value: " + this.objValue);
    }

    /* --------------------------------------------------------------------- */
    private double[] computeTotalWeight(MKP mkpInstance) {
        double[] totalWeight = new double[mkpInstance.numConstraints];
        for (int i = 0; i < mkpInstance.numItems; i++) {

            if ((int) this.position[i] == 1) {
                for (int j = 0; j < mkpInstance.numConstraints; j++) {
                    totalWeight[j] += mkpInstance.weights[j][i];
                }
            }
        }
        return totalWeight;
    }

    private static boolean anyExceeds(double[] weights, int[] CAPAC) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > CAPAC[i]) {
                return true;
            }
        }
        return false;
    }

    private static boolean canAddItem(double[] weights, int item, MKP mkpInstance) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] + mkpInstance.weights[i][item] > mkpInstance.capacities[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean canFitItem(int item, int[] remainingCapac, int[][] weights) {
        for (int i = 0; i < remainingCapac.length; i++) {
            if (remainingCapac[i] - weights[i][item] < 0) {
                return false;
            }
        }
        return true;
    }

}
