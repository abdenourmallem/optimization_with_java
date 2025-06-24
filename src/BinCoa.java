import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tools.*;
import tools.ResultBatch.ResultRow;
import tools.ResultBatchSac.ResultRowSac;
import tools.TrainingData.DataRow;

public class BinCoa {

    public static final int nPop = 600;
    public static final int nIter = 20;
    public static final double xorProb = (double) 1;
    public static final int effBias = 2;
    public static final int ub = 1;
    public static final int lb = -1;
    public static final int optimum = 24381;
    public static final int nReps = 30;

    public static int bestMemIdx(Candidate[] pop, int currentBest) {
        for (int i = 0; i < pop.length; i++) {
            if (pop[i].objValue >= pop[currentBest].objValue) {
                currentBest = i;
            }
        }
        return currentBest;
    }

    public static double[] bestPos(Candidate cand1, Candidate cand2) {
        return (cand1.objValue > cand2.objValue) ? cand1.position : cand2.position;
    }

    public static double[] betterPos(MKP mkpInstance, Candidate iguana, Candidate bestSol) {
        if (!iguana.checkConstraints(mkpInstance)) {
            return bestSol.position;
        }
        if (iguana.objValue >= bestSol.objValue) {
            return iguana.position;
        } else {
            return bestSol.position;
        }
    }

    public static Candidate approachIguanaT(MKP mkpInstance, Candidate coati, Candidate iguana) {
        double[] newPos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            double r = Math.random();
            int v = (int) (Math.random() * 2) + 1;
            newPos[i] = 5 * (double) (coati.position[i] + (r * (iguana.position[i] - (v * coati.position[i]))));
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;
    }

    public static Candidate approachIguanaG(MKP mkpInstance, Candidate coati, Candidate iguana) {
        double[] newPos = new double[mkpInstance.numItems];
        if (iguana.objValue > coati.objValue) {
            for (int i = 0; i < mkpInstance.numItems; i++) {
                double r = Math.random();
                int v = (int) (Math.random() * 2) + 1;
                newPos[i] = 5 * (double) (coati.position[i] + (r * (iguana.position[i] - (v * coati.position[i]))));
            }
        } else {
            for (int i = 0; i < mkpInstance.numItems; i++) {
                double r = Math.random();
                newPos[i] = 5 * (double) (coati.position[i] + (r * (coati.position[i] - iguana.position[i])));
            }
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;

    }

    public static Candidate iguanaGPos(MKP mkpInstance, int ub, int lb, int effBias) {
        // double[] pos = new double[mkpInstance.numItems];
        // for (int i = 0; i < mkpInstance.numItems; i++) {
        // pos[i] = (double) (lb + (Math.random() * (ub - lb)));
        // }
        Candidate newCand = new Candidate(mkpInstance, effBias);
        return newCand;
    }

    public static Candidate escape(MKP mkpInstance, Candidate coati, int ub, int lb) {
        double[] newPos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            double r = Math.random();
            newPos[i] = coati.position[i] + (1 - r * 2) * (lb + r * (ub - lb));
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;
    }

    public static Candidate applyXOR(MKP mkpInstance, Candidate coati, Candidate iguana, Candidate neighbour,
            double xorProb) {
        double[] newPos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            newPos[i] = (double) ((int) coati.position[i]
                    ^ ((int) neighbour.position[i] ^ (int) iguana.position[i]));
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;
        // return coati;
    }

    public static Candidate binCoaAlg(MKP mkpInstance, int nPop, int nIter, double xorProb, int effBias, int ub,
            int lb) {
        // Initiate the population
        Candidate[] pop = new Candidate[nPop];
        for (int i = 0; i < nPop; i++) {
            pop[i] = new Candidate(mkpInstance, effBias);
        }

        int iguana = bestMemIdx(pop, 0);
        Candidate bestSol = new Candidate(mkpInstance, pop[iguana].position);

        for (int t = 1; t <= nIter; t++) {
            for (int i = 0; i < nPop / 2; i++) {
                if (i != iguana) {
                    Candidate afterApproachT = approachIguanaT(mkpInstance, pop[i], pop[iguana]);
                    pop[i].updatePosition(mkpInstance, bestPos(pop[i], afterApproachT));
                }
            }
            Candidate iguG = iguanaGPos(mkpInstance, ub, lb, effBias);
            for (int i = nPop / 2; i < nPop; i++) {
                if (i != iguana) {
                    Candidate afterApproachG = approachIguanaG(mkpInstance, pop[i], iguG);
                    pop[i].updatePosition(mkpInstance, bestPos(pop[i], afterApproachG));
                }
            }
            /* --------------------------------------------------------------------- */
            for (int i = 0; i < nPop; i++) {
                if (i != iguana) {
                    Candidate afterEscape = escape(mkpInstance, pop[i], ub / t, lb /
                            t);
                    pop[i].updatePosition(mkpInstance, bestPos(pop[i], afterEscape));
                }
            }
            /* --------------------------------------------------------------------- */
            for (int i = 0; i < nPop; i++) {
                if (i != iguana) {
                    pop[i].applyTransferFunc(mkpInstance);
                    if (t <= nIter - 5) {
                        pop[i].repairPosition(mkpInstance);
                    } else {
                        pop[i].repairViaQAgent(mkpInstance);
                    }
                    if (t == nIter) {
                        if (i > nPop / 2)
                            pop[i].localSearch(mkpInstance);
                    }
                }
            }
            /* --------------------------------------------------------------------- */
            iguana = bestMemIdx(pop, iguana);
            pop[iguana].localSearch(mkpInstance);
            bestSol.updatePosition(mkpInstance, betterPos(mkpInstance, pop[iguana],
                    bestSol));
            if (t >= nIter - 3) {
                for (int i = 0; i < nPop; i++) {
                    if (i != iguana) {
                        int neighbourIdx = (int) (Math.random() * nPop);
                        Candidate afterXor = applyXOR(mkpInstance, pop[i], pop[iguana],
                                pop[neighbourIdx], xorProb);
                        pop[i].updatePosition(mkpInstance, bestPos(pop[i], afterXor));
                        // pop[i].updatePosition(mkpInstance, afterXor.position);
                    }
                }
            }

            iguana = bestMemIdx(pop, iguana);
            pop[iguana].localSearch(mkpInstance);
            bestSol.updatePosition(mkpInstance, betterPos(mkpInstance, pop[iguana],
                    bestSol));
        }
        return bestSol;
    }

    public static void main(String[] args) {
        // binCoaScriptChuBeas();
        // binCoaScriptChuBeas5x100();
        // binCoaScriptSac();
        MKP mkpInstance = new MKP("..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat");
        QLearningAgent agent = new QLearningAgent(mkpInstance);
        // agent.learn();
        mkpInstance.agent = agent;

        // HashMapToFile.saveToFile(agent.qTable, "q-agent-" +
        // mkpInstance.instanceName);

        double totalExecTime = (double) 0;
        double totalPercentage = (double) 0;
        double totalDFO = (double) 0;
        double worstDFO = (double) 0;
        double bestDFO = (double) 100000;
        for (int i = 0; i < nReps; i++) {
            long startTime = System.currentTimeMillis();

            Candidate bestSol = binCoaAlg(mkpInstance, nPop, nIter, xorProb, effBias, ub,
                    lb);
            double percentage = bestSol.objValue * 100 / optimum;
            double dfo = 100 - percentage;
            if (dfo < bestDFO)
                bestDFO = dfo;
            if (dfo > worstDFO)
                worstDFO = dfo;
            totalDFO += dfo;
            totalPercentage += percentage;

            System.out.printf(
                    "%d: Candidate objective value: %.1f Percentage: %.2f%% DFO: %.2f%%%n",
                    i + 1, bestSol.objValue, percentage, dfo);
            // System.out.println("Candidate position: " +
            // Arrays.toString(bestSol.position));
            long endTime = System.currentTimeMillis();
            long execTime = endTime - startTime;
            totalExecTime += execTime;
            System.out.println("Execution time: " + execTime / 1000.0 + " seconds");
        }
        // System.out.printf("Average percentage: %.2f%%%n", totalPercentage /
        // numReps);
        System.out.printf("Number of repetitions: %d%n", nReps);
        System.out.printf("Average execution time: %.2fs%n", (totalExecTime / 1000.0)
                / nReps);
        System.out.printf("Average DFO: %.2f%%%n", totalDFO / nReps);
        System.out.printf("Worst DFO: %.2f%%%n", worstDFO);
        System.out.printf("Best DFO: %.2f%%%n", bestDFO);
    }

    public static void binCoaScriptChuBeas() {
        int[] numConstr = { 5, 10, 30 };
        int[] numItems = { 100, 250, 500 };
        String[] alpha = { "0.25", "0.50", "0.75" };
        int[] instanceNum = { 1, 10 };
        List<ResultRow> allResults = new ArrayList<>();
        int idx = 0;
        for (int i : numConstr) {
            for (int j : numItems) {
                for (String k : alpha) {
                    for (int z : instanceNum) {
                        String filepath = "OR" + i + "x" + j + "-" + k + "_" + z;
                        MKP mkp = new MKP(filepath);
                        double opt = OptimumValues.optimum[idx];

                        List<Double> scores = new ArrayList<>();
                        List<Double> times = new ArrayList<>();
                        System.out.println("running : " + filepath);
                        System.out.println("optimum : " + opt);
                        for (int ite = 0; ite < nReps; ite++) {
                            long t1 = System.nanoTime();
                            Candidate can = binCoaAlg(mkp, nPop, nIter, xorProb, effBias, ub, lb);
                            double exec = (System.nanoTime() - t1) / 1000000000.0;
                            times.add(exec);
                            scores.add(can.calcObjVal(mkp));

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
        ResultBatch.saveAllResults(allResults, "bincoa_mkp_results_new.csv");
    }

    public static void binCoaScriptChuBeas5x100() {
        List<ResultRow> allResults = new ArrayList<>();
        int idx = 0;
        String TransferFuncName = "vshaped";
        String filepath = "..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat";
        MKP mkp = new MKP(filepath);

        double opt = OptimumValues.optimum[idx];
        List<Double> scores = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        System.out.println("running : " + filepath);
        System.out.println("optimum : " + opt);
        for (int ite = 0; ite < nReps; ite++) {
            long t1 = System.nanoTime();
            Candidate can = binCoaAlg(mkp, nPop, nIter, xorProb, effBias, ub, lb);
            double exec = (System.nanoTime() - t1) / 1000000000.0;
            times.add(exec);
            scores.add(can.calcObjVal(mkp));

        }
        double averageTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double bestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double worstScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        allResults.add(new ResultRow(TransferFuncName,
                ((opt - averageScore) / opt) * 100, ((opt - bestScore) / opt) * 100,
                ((opt - worstScore) / opt) * 100, averageTime));
        System.out.printf("Average D.F.O: %.4f%%\n", ((opt - averageScore) / opt) * 100);
        System.out.printf("Best D.F.O: %.4f%%\n", ((opt - bestScore) / opt) * 100);
        System.out.printf("Worst D.F.O: %.4f%%\n", ((opt - worstScore) / opt) * 100);
        System.out.printf("Average Execution Time: %.2f seconds\n", averageTime);
        ResultBatch.saveAllResults(allResults, TransferFuncName + ".csv");
    }

    public static void binCoaScriptSac() {
        String[] datasetName = { "sento", "hp", "pb", "weing" };
        int[] datasetNumFiles = { 2, 2, 7, 8 };
        List<ResultRowSac> allResults = new ArrayList<>();
        int n = 0;
        for (String dataset : datasetName) {
            for (int i = 1; i <= datasetNumFiles[n]; i++) {
                if ("pb".equals(dataset) && i == 3)
                    continue;
                if ("pet".equals(dataset) && i == 1)
                    continue;
                String filepath;
                if (dataset == "weish") {
                    String result = String.format("%02d", i);
                    filepath = "..\\All-MKP-Instances\\sac94\\"
                            + dataset + "\\" + dataset + result + ".dat";
                } else {
                    filepath = "..\\All-MKP-Instances\\sac94\\"
                            + dataset + "\\" + dataset + i + ".dat";
                }
                MKP mkp = new MKP(filepath);

                int opt = mkp.optimum;
                int numSucc = 0;
                double[] objValues = new double[30];
                List<Double> scores = new ArrayList<>();
                List<Double> times = new ArrayList<>();
                System.out.println("running : " + filepath);
                System.out.println("optimum : " + opt);
                for (int ite = 0; ite < nReps; ite++) {
                    long t1 = System.nanoTime();
                    Candidate can = binCoaAlg(mkp, nPop, nIter, xorProb, effBias, ub, lb);
                    objValues[ite] = can.objValue;
                    if (can.objValue == opt)
                        numSucc++;
                    double exec = (System.nanoTime() - t1) / 1000000000.0;
                    times.add(exec);
                    scores.add(can.calcObjVal(mkp));

                }
                double mean = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double std = 0.0;
                for (int j = 0; j < nReps; j++) {
                    std += Math.pow(objValues[j] - mean, 2);
                }
                std = Math.sqrt(std / nReps);
                double averageTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double bestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double worstScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                allResults.add(new ResultRowSac(dataset + i,
                        numSucc / (double) nReps, std));
                System.out.printf("Average D.F.O: %.4f%%\n", ((opt - averageScore) / opt) * 100);
                System.out.printf("Best D.F.O: %.4f%%\n", ((opt - bestScore) / opt) * 100);
                System.out.printf("Worst D.F.O: %.4f%%\n", ((opt - worstScore) / opt) * 100);
                System.out.printf("Average Execution Time: %.2f seconds\n", averageTime);
                System.out.printf("Success rate: %.2f\n", (numSucc / (double) nReps));
                System.out.printf("std: %.2f\n", std);
            }
            n++;
        }
        ResultBatchSac.saveAllResults(allResults, "bincoa_mkp_results_sac94.csv");
    }

    // public static void trainingData() {
    // double[] alpha = { 0.9037924 };
    // int[] ng = { 313 };
    // int[] ps = { 288 };
    // double[] pc = { 0.58414083 };
    // double[] pm = { 0.45581203 };
    // int[] nmp = { 7 };
    // int[] OR = { 5, 10, 30 };
    // int[] l = { 100, 250, 500 };
    // String[] l2 = { "0.25", "0.50", "0.75" };
    // List<DataRow> allResults = new ArrayList<>();
    // int idx = 0;
    // for (int i = 0; i < alpha.length; i++) {
    // for (int j = 0; j < ng.length; j++) {
    // for (int t = 0; t < ps.length; t++) {
    // for (int k = 0; k < pc.length; k++) {
    // for (int kk = 0; kk < pm.length; kk++) {
    // for (int z = 0; z < nmp.length; z++) {
    // idx = 0;
    // for (int i2 = 0; i2 < OR.length; i2++) {
    // for (String k2 : l2) {
    // String filepath =
    // "C:\\Users\\USER\\Desktop\\my_projects\\optimization_with_java\\All-MKP-Instances\\chubeas\\OR"
    // + OR[i2] + "x" + l[i2] + "\\OR" + OR[i2] + "x" + l[i2] + "-" + k2
    // + "_1.dat";
    // MKP mkp = new MKP(filepath);
    // double opt = OptimumValues.optimum[idx];
    // System.out.println("running : " + filepath);
    // System.out.println("optimum : " + opt);
    // long t1 = System.nanoTime();
    // Candidate can = binCoaAlg(mkp, alpha[i], ng[j], ps[t], pc[k], pm[kk],
    // nmp[z]);
    // double exec = (System.nanoTime() - t1) / 1000000000.0;
    // double score = (can.calcObjVal(mkp.profits) / opt) * 100;
    // System.out.printf("Average D.F.O: %.4f%%\n", score);
    // System.out.printf("Execution Time: %.2f seconds\n", exec);
    // allResults.add(
    // new DataRow(filepath, alpha[i], ng[j], ps[t], pc[k], pm[kk],
    // nmp[z], exec, score));
    // idx = 10 + idx;
    // }
    // idx = idx + 60 + 30;
    // }
    // }
    // }
    // }
    // }
    // }
    // }

    // trainingData.saveTrainingData(allResults, "training_data.csv");
    // }
}