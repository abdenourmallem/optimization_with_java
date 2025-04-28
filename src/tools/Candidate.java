package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Candidate {
    int size;
    public double[] position;
    public double objValue;
    public double fitness;
    public List<Integer> chromosome;
    public MKP mkpInstance;

    // Constructor to create a candidate with a given position
    public Candidate(MKP mkpInstance, List<Integer> chromosome) {
        this.mkpInstance = mkpInstance;
        this.size = mkpInstance.numItems;
        this.chromosome = new ArrayList<>(chromosome);
        this.position = new double[this.size];
        // this.position=calcPosition();
        this.objValue = calcObjValCromosome(mkpInstance.profits);
        this.fitness = calcFitnessCromosome(mkpInstance);

    }

    public Candidate(MKP mkpInstance, double[] position) {
        this.mkpInstance = mkpInstance;
        this.size = mkpInstance.numItems;
        this.position = position;
        this.chromosome = new ArrayList<>();
        this.chromosome = calcChromosome();
        this.objValue = calcObjValCromosome(mkpInstance.profits);
        this.fitness = calcFitnessCromosome(mkpInstance);

    }

    public void calcPosition() {
        for (int item : chromosome) {
            this.position[item] = 1;
        }

    }

    public void setChromosome(List<Integer> chromosome, MKP mkp) {
        this.chromosome = chromosome;
        this.objValue = calcObjValCromosome(mkp.profits);
        this.fitness = calcFitnessCromosome(mkp);
    }

    public void flipBit(int idx) {
        if (this.chromosome.contains(idx)) {
            this.chromosome.remove(Integer.valueOf(idx));
            this.position[idx] = 0;
            this.objValue = this.objValue - (this.mkpInstance.profits[idx]);
            this.fitness = this.fitness - (this.mkpInstance.profits[idx]*this.mkpInstance.EffList.get(idx).getValue());
        } else {
            this.chromosome.add(idx);
            this.position[idx] = 1;
            this.objValue = this.objValue + (this.mkpInstance.profits[idx]);
            this.fitness = this.fitness + (this.mkpInstance.profits[idx]*this.mkpInstance.EffList.get(idx).getValue());

        }
    }

    public double getFitness() {
        return this.fitness;
    }

    public double getObjVal() {
        return this.objValue;
    }

    // chromosome methods

    public List<Integer> calcChromosome() {
        if (this.chromosome == null) {
            this.chromosome = new ArrayList<>();
        } else {
            this.chromosome.clear();
        }
        for (int i = 0; i < this.position.length; i++) {
            if (this.position[i] != 0.0) {
                this.chromosome.add(i);
            }
        }
        return this.chromosome;
    }


    public double calcObjValCromosome(int[] profits) {
        this.objValue = (double) 0;
        for (int i = 0; i < this.chromosome.size(); i++) {
            int idx = this.chromosome.get(i);
            this.objValue += profits[idx];
        }
        return this.objValue;
    }

    public double calcFitnessCromosome(MKP mkpInstance) {
        this.fitness = fitness_functions.fitness_functionChromosome(this.chromosome, mkpInstance.EffList,
                mkpInstance.profits);
        return this.fitness;
    }

    public boolean checkConstraintsChromosome(MKP mkpInstance) {
        int[] totalWeights = new int[mkpInstance.numConstraints];
        for (int i = 0; i < mkpInstance.numConstraints; i++) {
            int weightedSum = 0;
            for (int j = 0; j < this.chromosome.size(); j++) {
                weightedSum += mkpInstance.weights[i][this.chromosome.get(j)];
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
