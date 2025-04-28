import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import tools.*;
import tools.GGALocalSearchResultsBatch.ResultRow;

public class GGA {
    public static List<List<Pairs>> CalcGuidParts(MKP mkp, int coreSize) {
        int[] remainingCapacities = Arrays.copyOf(mkp.capacities, mkp.numConstraints);
        int cb1, cb2;
        List<Pairs> x1 = new ArrayList<>();
        List<Pairs> x0 = new ArrayList<>();
        List<Pairs> core = new ArrayList<>();
        boolean fits = true;
        for (int i = 0; i < mkp.numItems; i++) {
            int item = mkp.SortedItems.get(i).getId();
            for (int j = 0; j < mkp.numConstraints; j++) {
                if (remainingCapacities[j] - mkp.weights[j][item] < 0) {
                    fits = false;
                    break;

                } else {
                    remainingCapacities[j] -= mkp.weights[j][item];
                }
            }
            if (!fits) {
                if ((i - (coreSize / 2)) >= 0) {
                    cb1 = (i - (coreSize / 2));
                } else {
                    cb1 = 0;
                }
                if ((i + (coreSize / 2)) + coreSize % 2 < mkp.numItems) {
                    cb2 = (i + (coreSize / 2)) + coreSize % 2;
                } else {
                    cb2 = mkp.numItems;
                }
                x1 = new ArrayList<>(mkp.SortedItems.subList(0, cb1));
                core = new ArrayList<>(mkp.SortedItems.subList(cb1, cb2));
                x0 = new ArrayList<>(mkp.SortedItems.subList(cb2, mkp.numItems));
                break;
            }

        }
        List<List<Pairs>> result = new ArrayList<>();
        result.add(x1);
        result.add(core);
        result.add(x0);
        return result;
    }

    public static Candidate generateIndividual(MKP mkp, List<Pairs> x1, double ir, double[] pb) {
        int[] remainingCapacities = Arrays.copyOf(mkp.capacities, mkp.numConstraints);
        int startIdx = x1.size();
        List<Integer> chr = new ArrayList<>();
        boolean fits = true;
        if (Math.random() < ir) {

            for (int i = 0; i < startIdx; i++) {
                int item = x1.get(i).getId();
                chr.add(item);
                for (int j = 0; j < mkp.numConstraints; j++) {
                    remainingCapacities[j] -= mkp.weights[j][item];
                }

            }
        } else {
            startIdx = 0;
        }
        for (int i = startIdx; i < mkp.numItems; i++) {
            int item = mkp.SortedItems.get(i).getId();
            int[] newRemainingCapacities = Arrays.copyOf(remainingCapacities, mkp.numConstraints);
            for (int j = 0; j < mkp.numConstraints; j++) {
                if (remainingCapacities[j] - mkp.weights[j][item] < 0) {
                    fits = false;
                    break;
                } else {
                    newRemainingCapacities[j] = remainingCapacities[j] - mkp.weights[j][item];
                }
            }
            if (fits) {
                if (Math.random() < pb[i]) {
                    for (int j = 0; j < mkp.numConstraints; j++) {
                        remainingCapacities[j] = newRemainingCapacities[j];
                    }

                    chr.add(item);
                }

            } else {
                break;
            }
        }
        Candidate candidate = new Candidate(mkp, chr);
        return candidate;
    }

    public static Candidate[] createPopulation(MKP mkp, int ps, double ir, double[] pb, List<Pairs> gp) {
        Candidate[] population = new Candidate[ps];
        for (int i = 0; i < ps; i++) {
            population[i] = generateIndividual(mkp, gp, ir, pb);
        }
        return population;
    }

    public static double[] populationFitness(Candidate[] population, MKP mkp) {
        double[] fitness = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            fitness[i] = population[i].fitness;
        }
        return fitness;
    }

    public static double[] populationObjVal(Candidate[] population, MKP mkp) {
        double[] objVal = new double[population.length];
        for (int i = 0; i < population.length; i++) {
            objVal[i] = population[i].objValue;
        }
        return objVal;
    }

    public static Candidate[] concat(Candidate[] a, Candidate[] b) {
        Candidate[] result = new Candidate[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static Candidate[] reproductionMethod(MKP mkp, Candidate[] population, double ir, double[] pb,
            List<Pairs> gp) {
        double[] popFit = populationFitness(population, mkp);
        int ps = population.length;
        int[] bestChromosomesIdx = TopIdx.topIdx(popFit, 10);
        Candidate[] newPopulation = new Candidate[10];
        for (int i = 0; i < 10; i++) {
            newPopulation[i] = population[bestChromosomesIdx[i]];
        }
        return concat(newPopulation, createPopulation(mkp, ps - 10, ir, pb, gp));
    }

    public static List<Integer> localSearchMethod(MKP mkp, List<Pairs> x1, Candidate candidate) {
        double fitness = candidate.fitness;
        double objVal = candidate.objValue;
        Candidate clone = new Candidate(mkp, candidate.chromosome);
        int startIdx = 0;
        if (Math.random() < (1-(mkp.numItems/1000)*1.8))
            startIdx = x1.size();
        for (int i = startIdx; i < mkp.numItems; i++) {
            int item = mkp.SortedItems.get(i).getId();

            clone.flipBit(item);
            double newFitness = clone.fitness;
            double newObjValue = clone.objValue;
            if (clone.checkConstraintsChromosome(mkp)) {
                if (newObjValue < objVal) {
                    if (newFitness > fitness && fitness / newFitness > newObjValue / objVal) {
                        fitness = newFitness;
                        objVal = newObjValue;
                    } else {
                        clone.flipBit(item);
                    }

                } else {
                    if (newFitness > fitness) {
                            fitness = newFitness;
                            objVal = newObjValue;
                        } 
                    else {
                        if (objVal / newObjValue > newFitness / fitness) {
                            fitness = newFitness;
                            objVal = newObjValue;
                        } else {
                            clone.flipBit(item);
                        }
                    }
                }
            } else {
                clone.flipBit(item);
            }

        }
        // candidate.setPosition(clone.position, mkp);
        return clone.chromosome;
    }

    public static Candidate[] selection(MKP mkp, Candidate[] population, int nbk) {
        Random rand = new Random();
        Candidate[] new_population = new Candidate[population.length];
        for (int p = 0; p < population.length; p++) {
            Set<Integer> random_idx = new HashSet<>();
            for (int i = 0; i < nbk; i++) {
                int rn = rand.nextInt(population.length);
                if (random_idx.contains(rn)) {
                    i--;
                    continue;
                }
                random_idx.add(rn);
            }
            Integer[] random_idx_array = random_idx.toArray(new Integer[0]);
            int max_idx = random_idx_array[0];
            for (int i = 1; i < random_idx_array.length; i++) {
                if (population[random_idx_array[i]].getFitness() > population[max_idx].getFitness()) {
                    max_idx = random_idx_array[i];
                }
            }
            new_population[p] = population[max_idx];
        }

        return new_population;
    }

    public static double[] ProbabilityDistrbution(int numItems, double r1, double r2) {
        double[] effBias = new double[numItems];
        double r = (r2 - r1) / (numItems - 1);
        for (int i = 0; i < numItems; i++) {
            double bias = r2 - (i * r);
            effBias[i] = bias;
        }
        return effBias;
    }

    public static Candidate[] crossover(MKP mkp, Candidate parent1, Candidate parent2, double pc) {
        parent1.calcPosition();
        parent2.calcPosition();
        Random rand = new Random();
        if (rand.nextDouble() < pc) {
            int crossover_point = rand.nextInt(parent1.position.length - 1) + 1;
            double[] child1 = new double[parent1.position.length];
            double[] child2 = new double[parent1.position.length];

            for (int i = 0; i < crossover_point; i++) {
                child1[i] = parent1.position[i];
                child2[i] = parent2.position[i];
            }
            for (int i = crossover_point; i < parent2.position.length; i++) {
                child1[i] = parent2.position[i];
                child2[i] = parent1.position[i];
            }
            Candidate childCandidate1 = new Candidate(mkp, child1);
            Candidate childCandidate2 = new Candidate(mkp, child2);
            return new Candidate[] { childCandidate1, childCandidate2 };
        } else {
            return new Candidate[] { parent1, parent2 };
        }

    }

    public static List<Integer> mutation(MKP mkp, List<Pairs> x1, double ir, double[] pb, Candidate individual, int nmp,
            double pm) {
        Random rand = new Random();
        Set<Integer> prev = new HashSet<>();
        Candidate pos = new Candidate(mkp, individual.chromosome);
        for (int i = 0; i < nmp; i++) {
            int mutation_point = rand.nextInt(individual.position.length);
            if (prev.contains(mutation_point)) {
                i--;
                continue;
            }
            prev.add(mutation_point);
            if (rand.nextDouble() < pm) {
                pos.flipBit(mutation_point);
            }
        }
        if (!pos.checkConstraintsChromosome(mkp)) {
            return generateIndividual(mkp, x1, ir, pb).chromosome;
        }
        return pos.chromosome;
    }

    public static Candidate executeGGA(MKP mkp) {
        int ps = 500;
        int ng = 200;
        double pc = 0.7;
        double pm = 0.2;
        double pr = 0.1;
        int nmp = 3;
        int nbk = 5;
        double IntegrationRate = 0.9;
        int CoreSize = 30;
        double r1 = 0.4;
        double r2 = 0.6;

        // calculate Guide parts
        List<List<Pairs>> GuideParts = CalcGuidParts(mkp, CoreSize);

        // set the probability distribution
        double[] pb = ProbabilityDistrbution(mkp.numItems, r1, r2);
        // create population (population is an array of individuals(Candidates))
        Candidate[] population = createPopulation(mkp, ps, r2, pb, GuideParts.get(0));

        // evaluate the best chromosomes
        int bestCandIdx = TopIdx.topIdx(populationObjVal(population, mkp), 1)[0];
        double bestScore = population[bestCandIdx].calcObjValCromosome(mkp.profits);
        Candidate besCandidate = new Candidate(mkp, population[bestCandIdx].chromosome);

        // initialize the stagnation trigger to 0
        int stagnationCount = 0;
        double r_mut = pm;
        for (int gen = 0; gen < ng; gen++) {
            // select the best chromosomes

            Candidate[] selected = selection(mkp, population, nbk);

            List<Candidate> newPopulationList = new ArrayList<>();
            for (int i = 0; i < selected.length; i += 2) {
                // perform crossover operator

                Candidate[] children = crossover(mkp, selected[i], selected[i + 1], pc);
                // perform mutation operator

                for (int j = 0; j < children.length; j++) {
                    children[j].setChromosome(
                            mutation(mkp, GuideParts.get(0), IntegrationRate, pb, children[j], nmp, r_mut), mkp);
                    newPopulationList.add(children[j]);
                }

            }
            population = newPopulationList.toArray(new Candidate[0]);

            r_mut = pm;
            if (gen < 190) {
                int[] elites = TopIdx.topIdx(populationFitness(population, mkp), 50);
                for (int i = 0; i < elites.length; i++) {
                    population[elites[i]]
                            .setChromosome(localSearchMethod(mkp, GuideParts.get(0), population[elites[i]]), mkp);
                }
            }

            if (Math.random() < pr) {
                // perform reproduction method
                population = reproductionMethod(mkp, population, IntegrationRate, pb, GuideParts.get(0));
            }

            bestCandIdx = TopIdx.topIdx(populationObjVal(population, mkp), 1)[0];
            if (population[bestCandIdx].calcObjValCromosome(mkp.profits) > bestScore) {

                bestScore = population[bestCandIdx].calcObjValCromosome(mkp.profits);
                besCandidate.setChromosome(population[bestCandIdx].chromosome, mkp);
                // System.out.println("best score" + bestScore);
                stagnationCount = 0;
            } else {
                stagnationCount++;
                if (stagnationCount >= 5) {
                    r_mut = pm * 2;
                    stagnationCount = 0;
                }
            }

        }
        return besCandidate;
    }

    public static void GGAscript() {
        int[] OR = { 5, 10, 30 };
        int[] l = { 100, 250, 500 };
        String[] l2 = { "0.25", "0.50", "0.75" };
        int[] l3 = { 1, 10 };
        List<ResultRow> allResults = new ArrayList<>();
        int idx = 0;
        for (int i : OR) {
            for (int j : l) {
                for (String k : l2) {
                    for (int z : l3) {
                        String filepath = "C:\\Users\\USER\\Desktop\\my_projects\\optimization_with_java\\All-MKP-Instances\\chubeas\\OR"
                                + i + "x" + j + "\\OR" + i + "x" + j + "-" + k + "_" + z + ".dat";
                        MKP mkp = new MKP(filepath);

                        double opt = OptimumValues.optimum[idx];
                        List<Double> scores = new ArrayList<>();
                        List<Double> times = new ArrayList<>();
                        System.out.println("running : " + filepath);
                        System.out.println("optimum : " + opt);
                        for (int ite = 0; ite < 30; ite++) {
                            long t1 = System.nanoTime();
                            Candidate can = executeGGA(mkp);
                            double exec = (System.nanoTime() - t1) / 1000000000.0;
                            times.add(exec);
                            scores.add(can.calcObjValCromosome(mkp.profits));

                        }
                        double averageTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        double bestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                        double worstScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                        allResults.add(new ResultRow("OR" + i + "x" + j + "-" + k + "_" + z,
                                ((opt - averageScore) / opt) * 100, ((opt - bestScore) / opt) * 100,
                                ((opt - worstScore) / opt) * 100, averageTime));
                        System.out.printf("Average D.F.O: %.4f%%\n", ((opt - averageScore) / opt) * 100);
                        System.out.printf("Best D.F.O: %.4f%%\n", ((opt - bestScore) / opt) * 100);
                        System.out.printf("Worst D.F.O: %.4f%%\n", ((opt - worstScore) / opt) * 100);
                        System.out.printf("Average Execution Time: %.2f seconds\n", averageTime);
                        if (z == 1) {
                            idx += 9;
                        } else {
                            idx += 1;
                        }
                    }
                }
            }
        }
        GGALocalSearchResultsBatch.saveAllResults(allResults, "gga_local_search_results.csv");
    }

    public static void main(String[] args) {

        MKP mkp = new MKP("C:\\Users\\USER\\Desktop\\my_projects\\optimization_with_java\\All-MKP-Instances\\chubeas\\OR5x250\\OR5x250-0.75_10.dat");
        for (int ite = 0; ite < 1; ite++) {
        long t1 = System.nanoTime();
        Candidate can = executeGGA(mkp);
        double exec = (System.nanoTime() - t1) / 1000000000.0;
        System.out.println(can.objValue);
        System.out.println(exec);
        System.out.println(can.checkConstraintsChromosome(mkp));
        can.calcPosition();
        System.out.println(Arrays.toString(can.position));
double opt=154662.0;
        }
        // GGAscript();
    }
}