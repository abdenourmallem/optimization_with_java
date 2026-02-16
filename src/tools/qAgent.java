package tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import GGA.GGA;

public class qAgent {
    public final static Map<String, Map<Action, Double>> Q = new HashMap<>();

    static class Action {
        int index;
        String operation; // "add" or "remove"

        public Action(int index, String operation) {
            this.index = index;
            this.operation = operation;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Action))
                return false;
            Action other = (Action) obj;
            return this.index == other.index && this.operation.equals(other.operation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, operation);
        }
    }

    public static class StepResult {
        public int[] newState;
        public double reward;
        public boolean done;
        public double objValue;

        public StepResult(int[] newState, double reward, boolean done, double objValue) {
            this.newState = newState;
            this.reward = reward;
            this.done = done;
            this.objValue = objValue; // Initialize objValue to 0.0, can be set later if needed
        }
    }

    private static double sShapeTransferFuncV2(double x) {
        while (x > 5 || x < -5) {
            x = x / 10;
        }
        return 1.0 / (1.0 + Math.exp(-0.5 * x));
    }

    public void clearQ() {
        Q.clear();
    }

    public static StepResult step(qAgent.Action action, Candidate currentState) {
        currentState.calcPosition();
        Candidate newState = new Candidate(currentState.mkpInstance, currentState.position);
        int index = action.index;
        String op = action.operation;

        double oldFitness = newState.calcFitnessCromosome(currentState.mkpInstance);

        newState.flipBit(index);
        boolean done = newState.checkConstraintsChromosome(currentState.mkpInstance);

        double reward;
        if (!done) {
            reward = -1.0;
        } else {
            reward = sShapeTransferFuncV2(newState.fitness - oldFitness);
            // System.out.println("Old fitness: " + oldFitness);
            // System.out.println("New fitness: " + newState.fitness);
            // System.out.printf("n-old=%f", newState.fitness - oldFitness);
        }
        // System.out.println(reward);
        newState.calcPosition();
        return new StepResult(newState.position.clone(), reward, done, newState.objValue);
    }

    public void train(MKP env, Candidate candidate, List<Pairs> x1, double alpha, double gamma,
            double epsilon, int eps) {
        candidate.calcPosition();
        Candidate state = new Candidate(env, candidate.position.clone());
        double oldObjValue = state.objValue;
        int nActions = env.numItems;

        int startIdx = 0;
        if (Math.random() < (1 - (env.numItems / 1000) * 1.8))
            startIdx = x1.size();
        List<String> keylist = new ArrayList<>();
        for (int ep = 0; ep < eps; ep++) {
            String stateKey = Arrays.toString(state.position.clone());
            if (Q.containsKey(stateKey)) {
                // Random rand=new Random();
                // int idx=rand.nextInt(nActions);
                // state.position[idx]=1-state.position[idx];
                break;
            }
            int[] newposition = state.position.clone();

            Q.putIfAbsent(stateKey, new HashMap<>());

            Map<Action, Double> qValues = Q.get(stateKey);
            double maxreward = -1000.0;
            int i = 1;
            for (int step = startIdx; step < nActions; step++) {

                int item = env.SortedItems.get(step).getId();
                // Q.putIfAbsent(stateKey, new HashMap<>());

                // Choose action
                Action action;
                int idx = item;

                String op = state.position[idx] == 0 ? "add" : "remove";
                action = new Action(idx, op);

                StepResult result = step(action, state);
                int[] nextState = result.newState;
                double reward = result.reward;
                String nextKey = Arrays.toString(nextState);
                if (reward > maxreward && result.done && !keylist.contains(nextKey)) {
                    maxreward = reward;
                    newposition = nextState;
                    keylist.add(stateKey);
                    // System.out.println("new state in episode " + ep);
                }

                // Q.putIfAbsent(nextKey, new HashMap<>());
                // Map<Action, Double> nextQ = Q.get(nextKey);
                // for (int i = 0; i < nActions; i++) {
                // nextQ.putIfAbsent(new Action(i, "add"), 0.0);
                // nextQ.putIfAbsent(new Action(i, "remove"), 0.0);
                // }

                // double maxNextQ = nextQ.values().stream().mapToDouble(v ->
                // v).max().orElse(0);
                // double oldQ = qValues.getOrDefault(action, 0.0);
                qValues.put(action, alpha * (reward + gamma * sShapeTransferFuncV2(result.objValue - oldObjValue)));

                i++;
            }
            state.setPosition(newposition);
            // System.out.println(Arrays.toString(state.position));
            // System.out.println(Q.size() + " Q-tables size");
        }
        // System.out.println(Q.size() + " Q-values learned.");
        // System.out.println("actions: " + Q.values().stream()
        // .flatMap(map -> map.entrySet().stream())
        // .map(Map.Entry::getKey)
        // .distinct()
        // .count());
        //saveQtable.saveToFile(Q, "qtable.csv");
        // return Q;
    }

    public void trainV2(MKP env, Candidate candidate, List<Pairs> x1, double alpha, double gamma,
            double epsilon,int eps) {
        candidate.calcPosition();
        Candidate state = new Candidate(env, candidate.position.clone());
        for (int ep = 0; ep < eps; ep++) {
            String stateKey = Arrays.toString(state.position.clone());
            Q.putIfAbsent(stateKey, new HashMap<>());
            Map<Action, Double> qValues = Q.get(stateKey);
            double fitness = state.fitness;
            double objVal = state.objValue;
            Candidate clone = new Candidate(env, state.position.clone());
            int startIdx = 0;
            if (Math.random() < (1 - (env.numItems / 1000) * 1.8))
                startIdx = x1.size();
            boolean ab = false;
            for (int i = startIdx; i < env.numItems; i++) {
                int item = env.SortedItems.get(i).getId();

                clone.flipBit(item);
                double newFitness = clone.fitness;
                double newObjValue = clone.objValue;

                if (clone.checkConstraintsChromosome(env)) {
                    if (newObjValue < objVal) {
                        if (newFitness > fitness && fitness / newFitness > newObjValue / objVal) {
                            fitness = newFitness;
                            objVal = newObjValue;

                            int idx = item;
                            String op = state.position[idx] == 0 ? "add" : "remove";
                            double reward = sShapeTransferFuncV2(newFitness - fitness);
                            Action action = new Action(idx, op);
                            qValues.put(action,
                                    alpha * (reward + gamma * sShapeTransferFuncV2(newObjValue - objVal)));
                            ab = true;
                        } else {
                            clone.flipBit(item);
                        }

                    } else {
                        if (newFitness > fitness) {
                            fitness = newFitness;
                            objVal = newObjValue;

                            int idx = item;
                            String op = state.position[idx] == 0 ? "add" : "remove";
                            double reward = sShapeTransferFuncV2(newFitness - fitness);
                            Action action = new Action(idx, op);
                            qValues.put(action,
                                    alpha * (reward + gamma * sShapeTransferFuncV2(newObjValue - objVal)));
                            ab = true;
                        } else {
                            if (objVal / newObjValue > newFitness / fitness) {
                                fitness = newFitness;
                                objVal = newObjValue;

                                int idx = item;
                                String op = state.position[idx] == 0 ? "add" : "remove";
                                double reward = sShapeTransferFuncV2(newFitness - fitness);
                                Action action = new Action(idx, op);
                                qValues.put(action,
                                        alpha * (reward + gamma * sShapeTransferFuncV2(newObjValue - objVal)));
                                ab = true;
                            } else {
                                clone.flipBit(item);
                            }

                        }
                    }
                } else {
                    clone.flipBit(item);
                }

            }
            if (!ab) {
                break;
            }
            clone.calcPosition();
            state.setPosition(clone.position.clone());
        }
        // saveQtable.saveToFile(Q, "qtable.csv");
    }

    public List<Integer> qLearningLocalSearch(Candidate solution, List<Pairs> x1) {

        Candidate state = new Candidate(solution.mkpInstance, solution.chromosome);
        state.calcPosition();
        int startIdx = 0;
        if (Math.random() < (1 - (solution.mkpInstance.numItems / 1000) * 1.8))
            startIdx = x1.size();
        for (int iter = startIdx; iter < state.size; iter++) {
            int item = state.mkpInstance.SortedItems.get(iter).getId();
            String stateKey = Arrays.toString(state.position);
            if (!Q.containsKey(stateKey)) {
                // System.out.println("break");
                if (Math.random() < 0.1)
                    this.train(state.mkpInstance, state, x1, 0.1, 0.9, 0.1, 35);
                else
                    return solution.chromosome; // If no Q-values for this state, return current solution
            }
            Map<qAgent.Action, Double> qValues = Q.get(stateKey);
            List<Map.Entry<qAgent.Action, Double>> sortedActions = qValues.entrySet()
                    .stream()
                    .sorted(Map.Entry.<qAgent.Action, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());

            for (Map.Entry<qAgent.Action, Double> entry : sortedActions) {

                qAgent.Action action = entry.getKey();

                // System.out.println("Trying action: " + action.index + " " + action.operation
                // + " at iter " + i);

                StepResult result = qAgent.step(action, state);
                // System.out.println("obj after step: " + state.objValue);

                if (result.done) {
                    state = new Candidate(state.mkpInstance, result.newState);
                    break;
                } else {
                    return state.chromosome;
                }

            }

            // Optionally return something if no valid actions were found

        }
        return state.chromosome;
    }

    public static void main(String[] args) {
        double IntegrationRate = 0.9;
        int CoreSize = 30;
        double r1 = 0.4;
        double r2 = r1 + 0.2;
        MKP mkp = new MKP(
                "C:\\Users\\USER\\Desktop\\my_projects\\optimization_with_java\\All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat");
        // calculate Guide parts
        List<List<Pairs>> GuideParts = GGA.CalcGuidParts(mkp, CoreSize);

        // set the probability distribution
        double[] pb = GGA.ProbabilityDistrbution(mkp.numItems, r1, r2);
        Candidate initialCandidate = GGA.generateIndividual(mkp, GuideParts.get(0), IntegrationRate, pb);
        qAgent agent = new qAgent();
        double time = System.nanoTime();
        agent.train(mkp, initialCandidate, GuideParts.get(0), 0.1, 0.9, 0.1, 30);
        double time2 = (System.nanoTime());
        // // List<Integer> optimizedChromosome =
        agent.qLearningLocalSearch(initialCandidate, GuideParts.get(0));
        System.out.println((System.nanoTime() - time2) / 1000000000);
        System.out.println((time2 - time) / 1000000000);
        // // System.out.println("Optimized Chromosome: " + optimizedChromosome);
    }

}
