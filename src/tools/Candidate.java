/*
 * Contains the different functions and attributes of a BinCOA Candidate
 */

package tools;

import java.util.Arrays;
import java.util.List;

public class Candidate {
    public int size;
    public double[] position;
    public double objValue;
    public double fitness;

    /**
     * Constructor to create a candidate with a given position
     * 
     * @param mkpInstance
     * @param position
     */
    public Candidate(MKP mkpInstance, double[] position) {
        this.size = mkpInstance.numItems;
        this.position = position;
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Constructor to create a candidate with an efficient position
     * 
     * @param mkpInstance
     * @param effBias
     */
    public Candidate(MKP mkpInstance, int effBias) {
        this.size = mkpInstance.numItems;
        this.position = creEffPos(mkpInstance, effBias);
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Constructor to create a candidate with a feasible position
     * 
     * @param mkpInstance
     */
    public Candidate(MKP mkpInstance) {
        this.size = mkpInstance.numItems;
        this.position = creFeasiblePos(mkpInstance);
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Calculates the objective value of the candidate
     * 
     * @param mkpInstance
     * @return
     */
    public double calcObjVal(MKP mkpInstance) {
        double obj_val = (double) 0;
        for (int i = 0; i < mkpInstance.profits.length; i++) {
            obj_val += (double) (position[i] * mkpInstance.profits[i]);
        }
        return obj_val;
    }

    /**
     * Fitness here is considered to be different than the objective value
     * 
     * @param mkpInstance
     * @return
     */
    public double calcFitness(MKP mkpInstance) {
        double fitness = fitnessFuncs.fitnessFunc(this.position, mkpInstance.EffList, mkpInstance.profits);
        return fitness;
    }

    /**
     * Change the position of the candidate with a new given position
     * 
     * @param mkpInstance
     * @param newPos
     */
    public void updatePosition(MKP mkpInstance, double[] newPos) {
        this.position = newPos.clone();
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Returns an efficient position
     * 
     * @param mkpInstance
     * @param effBias
     * @return
     */
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

    public static boolean[] creEffPosBool(MKP mkpInstance, int effBias) {
        boolean[] effPos = new boolean[mkpInstance.numItems];

        int[] remCapac = Arrays.copyOf(mkpInstance.capacities, mkpInstance.numConstraints);

        double[] probBias = new double[mkpInstance.numItems];
        for (int i = 1; i < mkpInstance.numItems + 1; i++) {
            probBias[i - 1] = 1 - i * (1 / ((mkpInstance.numItems - 1) * (double) effBias));
        }

        for (int i = 0; i < mkpInstance.numItems; i++) {
            int itm = mkpInstance.SortedItems.get(i).getId();
            if (canFitItem(itm, remCapac, mkpInstance.weights)) {
                if (Math.random() < probBias[i]) {
                    effPos[itm] = true;
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

    /**
     * Returns a feasible solution
     * 
     * @param mkpInstance
     * @return
     */
    public double[] creFeasiblePos(MKP mkpInstance) {
        double[] validPos = new double[mkpInstance.numItems];

        for (int i = 0; i < mkpInstance.numItems; i++) {
            if (Math.random() > 0.5) {
                validPos[i] = 1.0;
                if (!checkConstraints(mkpInstance)) {
                    validPos[i] = 0.0;
                }
            }
        }
        return validPos;
    }

    /**
     * Verifies whether the position is feasible or not
     * 
     * @param mkpInstance
     * @return
     */
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

    /**
     * Applies a transfer function on the candidate
     * 
     * @param mkpInstance
     */
    public void applyTransferFunc(MKP mkpInstance) {
        // System.out.println("before applying transfer funct: ");
        // this.printPos();

        for (int i = 0; i < this.size; i++) {
            this.position[i] = TransferFunc.shiftedSigmoid(this.position[i]);
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

    /**
     * Applies the effiency based repair algorithm
     * 
     * @param mkpInstance
     */
    public void repairPosition(MKP mkpInstance) {
        if (checkConstraints(mkpInstance)) {
            return;
        }
        double[] totalWeight = this.computeTotalWeight(mkpInstance);

        for (int i = mkpInstance.numItems - 1; i >= 0; i--) {
            int item = mkpInstance.SortedItems.get(i).getId();
            if (anyExceeds(totalWeight, mkpInstance.capacities)) {
                this.position[item] = (double) 0;
                totalWeight = this.computeTotalWeight(mkpInstance);
            }
        }

        for (int i = 0; i < mkpInstance.numItems; i++) {
            int item = mkpInstance.SortedItems.get(i).getId();
            if (canAddItem(totalWeight, item, mkpInstance)) {
                this.position[item] = (double) 1;
                totalWeight = this.computeTotalWeight(mkpInstance);
            }
        }
        mkpInstance.agent.addState(this.position);
        this.objValue = calcObjVal(mkpInstance);
        // System.out.println("obj after repair: " + this.objValue);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Repairs a position via the Q learning agent
     * 
     * @param mkpInstance
     */
    public void repairViaQAgent(MKP mkpInstance) {
        if (checkConstraints(mkpInstance)) {
            return;
        }
        // QLearningAgent agent = new QLearningAgent(mkpInstance);
        // agent.qTable = HashMapToFile.readFromFile("q-agent-" +
        // mkpInstance.instanceName);

        // boolean[] repairedSolBool = mkpInstance.agent.repairSolution(this);
        boolean[] repairedSolBool = mkpInstance.agent.getBestSolution();

        for (int i = 0; i < mkpInstance.numItems; i++) {
            this.position[i] = repairedSolBool[i] ? 1.00 : 0.00;
        }
        this.objValue = calcObjVal(mkpInstance);
        // System.out.println("obj after repair: " + this.objValue);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Applies a naive bit flip local search
     * 
     * @param mkpInstance
     */
    public void localSearch(MKP mkpInstance) {
        if (!checkConstraints(mkpInstance)) {
            return;
        }
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

    public void flipBit(MKP mkpInstance, int idx) {
        this.position[idx] = 1.00 - this.position[idx];
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /**
     * Applies the neighborhood local search function
     * 
     * @param mkpInstance
     */
    public void localSearchFitness(MKP mkpInstance) {
        double fitness = this.fitness;
        double objVal = this.objValue;
        Candidate clone = new Candidate(mkpInstance, this.position);
        for (int i = 0; i < mkpInstance.numItems; i++) {
            int item = mkpInstance.SortedItems.get(i).getId();
            clone.flipBit(mkpInstance, item);
            double newFitness = clone.fitness;
            double newObjValue = clone.objValue;
            if (clone.checkConstraints(mkpInstance)) {
                if (newObjValue < objVal) {
                    if (newFitness > fitness && fitness / newFitness > newObjValue / objVal) {
                        fitness = newFitness;
                        objVal = newObjValue;
                    } else {
                        clone.flipBit(mkpInstance, item);
                    }

                } else {
                    if (newFitness > fitness) {
                        fitness = newFitness;
                        objVal = newObjValue;
                    } else {
                        if (objVal / newObjValue > newFitness / fitness) {
                            fitness = newFitness;
                            objVal = newObjValue;
                        } else {
                            clone.flipBit(mkpInstance, item);
                        }
                    }
                }
            } else {
                clone.flipBit(mkpInstance, item);
            }

        }
        this.position = clone.position;
        this.objValue = calcObjVal(mkpInstance);
        this.fitness = calcFitness(mkpInstance);
    }

    /* --------------------------------------------------------------------- */

    public static boolean[] creValidPosBool(MKP mkpInstance) {
        boolean[] validPos = new boolean[mkpInstance.numItems];

        for (int i = 0; i < mkpInstance.numItems; i++) {
            if (Math.random() > 0.5) {
                validPos[i] = true;
                if (!checkConstraints(validPos, mkpInstance)) {
                    validPos[i] = false;
                }
            }
        }
        return validPos;
    }

    public static boolean[] doubleToBoolPositon(double[] doublePos) {
        int numItems = doublePos.length;
        boolean[] boolPos = new boolean[numItems];
        for (int i = 0; i < numItems; i++) {
            boolPos[i] = doublePos[i] == 1.00 ? true : false;
        }
        return boolPos;
    }

    public static double[] boolToDoublePositon(boolean[] boolPos) {
        int numItems = boolPos.length;
        double[] doublePos = new double[numItems];
        for (int i = 0; i < numItems; i++) {
            doublePos[i] = boolPos[i] ? 1.00 : 0.00;
        }
        return doublePos;
    }

    public static boolean checkConstraints(boolean[] pos, MKP mkpInstance) {
        int[] totalWeights = new int[mkpInstance.numConstraints];
        for (int i = 0; i < mkpInstance.numConstraints; i++) {
            int weightedSum = 0;
            for (int j = 0; j < mkpInstance.numItems; j++) {
                weightedSum += mkpInstance.weights[i][j] * (pos[j] ? 1 : 0);
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

    /* --------------------------------------------------------------------- */

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

    private static boolean canFitItem(int item, int[] remainingCapac, int[][] weights) {
        for (int i = 0; i < remainingCapac.length; i++) {
            if (remainingCapac[i] - weights[i][item] < 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        MKP mkpInstance = new MKP("OR5x100-0.25_1");
        // boolean[] validPos = creEffPosBool(mkpInstance, 2);
        // System.out.println(Arrays.toString(validPos));
        boolean[] pos = new boolean[] { false, true, false, true, true, false, true, false, true, false, false, false,
                false, false, false, true, false, false, true, false, false, false, false, true, false, false, true,
                false, true, true, false, true, false, false, true, false, false, false, false, false, false, false,
                false, true, false, false, false, false, false, true, false, false, false, false, false, false, true,
                false, false, false, false, false, true, false, false, true, false, false, true, false, false, false,
                false, true, false, false, true, false, true, false, false, false, false, false, false, true, false,
                false, false, false, false, false, true, false, false, false, false, false, true, false };

        System.out.println(checkConstraints(pos, mkpInstance));
    }
}
