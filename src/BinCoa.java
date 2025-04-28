import java.util.Arrays;

import tools.*;

public class BinCoa {

    public static int bestMemIdx(Candidate[] pop) {
        int best = 0;
        for (int i = 0; i < pop.length; i++) {
            if (pop[i].objValue > pop[best].objValue) {
                best = i;
            }
        }
        return best;
    }

    public static double[] betterPos(Candidate cand1, Candidate cand2) {
        return (cand1.objValue > cand2.objValue) ? cand1.position : cand2.position;
    }

    public static Candidate approachIguanaT(MKP mkpInstance, Candidate coati, Candidate iguana) {
        double[] newPos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            double r = Math.random();
            int v = (int) (Math.random() * 2) + 1; // gives 1 or 2
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
                newPos[i] = (double) (coati.position[i] + (r * (iguana.position[i] - (v * coati.position[i]))));
            }
        } else {
            for (int i = 0; i < mkpInstance.numItems; i++) {
                double r = Math.random();
                newPos[i] = (double) (coati.position[i] + (r * (coati.position[i] - iguana.position[i])));
            }
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;

    }

    public static Candidate iguanaGPos(MKP mkpInstance, int ub, int lb) {
        double[] pos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            pos[i] = lb + (Math.random() * (ub - lb));
        }
        Candidate newCand = new Candidate(mkpInstance, pos);
        return newCand;
    }

    public static Candidate escape(MKP mkpInstance, Candidate coati, int ub, int lb) {
        double[] newPos = new double[mkpInstance.numItems];
        for (int i = 0; i < mkpInstance.numItems; i++) {
            double r = Math.random();
            newPos[i] = coati.position[i] + (1 - r / 5) * (lb + r * (ub - lb));
        }
        Candidate newCand = new Candidate(mkpInstance, newPos);
        return newCand;
    }

    public static Candidate applyXOR(MKP mkpInstance, Candidate coati, Candidate iguana, Candidate neighbour,
            double xorProb) {
        if (Math.random() < xorProb) {
            // System.out.print("after xor: ");
            // coati.printObj();
            double[] newPos = new double[mkpInstance.numItems];
            for (int i = 0; i < mkpInstance.numItems; i++) {
                newPos[i] = (double) ((int) coati.position[i]
                        ^ ((int) neighbour.position[i] ^ (int) iguana.position[i]));
            }
            Candidate newCand = new Candidate(mkpInstance, newPos);
            // System.out.print("after xor: ");
            // newCand.printObj();
            return newCand;
        }
        return coati;
    }

    public static Candidate binCoaAlg(MKP mkpInstance, int nPop, int nIter, double xorProb, int effBias, int ub,
            int lb) {
        // Initiate the population
        Candidate[] pop = new Candidate[nPop];
        for (int i = 0; i < nPop; i++) {
            pop[i] = new Candidate(mkpInstance, effBias);
        }

        int iguana = bestMemIdx(pop);
        for (int t = 1; t < nIter + 1; t++) {
            for (int i = 0; i < nPop / 2; i++) {
                if (i != iguana) {
                    Candidate afterApproachT = approachIguanaT(mkpInstance, pop[i], pop[iguana]);
                    pop[i].updatePosition(mkpInstance, betterPos(pop[i], afterApproachT));
                }
            }
            for (int i = nPop / 2; i < nPop; i++) {
                if (i != iguana) {
                    Candidate iguG = iguanaGPos(mkpInstance, ub, lb);
                    Candidate afterApproachG = approachIguanaG(mkpInstance, pop[i], iguG);
                    pop[i].updatePosition(mkpInstance, betterPos(pop[i], afterApproachG));
                }
            }
            /* --------------------------------------------------------------------- */
            for (int i = 0; i < nPop; i++) {
                if (i != iguana) {
                    Candidate afterEscape = escape(mkpInstance, pop[i], ub / t, lb / t);
                    pop[i].updatePosition(mkpInstance, betterPos(pop[i], afterEscape));
                }
            }
            /* --------------------------------------------------------------------- */
            for (int i = 0; i < nPop; i++) {
                pop[i].applyTransferFunc(mkpInstance);
                pop[i].repairPosition(mkpInstance);
                pop[i].localSearch(mkpInstance);
            }
            /* --------------------------------------------------------------------- */
            iguana = bestMemIdx(pop);
            for (int i = 0; i < nPop; i++) {
                int neighbourIdx = (int) (Math.random() * nPop);
                Candidate afterXor = applyXOR(mkpInstance, pop[i], pop[iguana],
                        pop[neighbourIdx], xorProb);
                afterXor.repairPosition(mkpInstance);
                afterXor.localSearch(mkpInstance);
                pop[i].updatePosition(mkpInstance, betterPos(pop[i], afterXor));
            }
            iguana = bestMemIdx(pop);
        }
        return pop[bestMemIdx(pop)];
    }

    public static void main(String[] args) {
        final int nPop = 200;
        final int nIter = 10;
        final double xorProb = (double) 0.7;
        final int effBias = 2;
        final int ub = 1;
        final int lb = -1;
        final int optimum = 24381;

        MKP mkpInstance = new MKP("..\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat");
        // mkpInstance.printMKPDetails();

        double totalPercentage = (double) 0;
        for (int i = 0; i < 15; i++) {
            long startTime = System.currentTimeMillis();

            Candidate bestSol = binCoaAlg(mkpInstance, nPop, nIter, xorProb, effBias, ub,
                    lb);
            System.out.println(
                    "Candidate objective value: " + bestSol.objValue + " " + bestSol.objValue *
                            100 / optimum + " " +
                            bestSol.checkConstraints(mkpInstance));

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double percentage = bestSol.objValue * 100 / optimum;
            totalPercentage += percentage;
            System.out.println("Execution time: " + duration / 1000.0 + " seconds");
        }
        System.out.println("average percentage: " + totalPercentage / 15);

    }

}
