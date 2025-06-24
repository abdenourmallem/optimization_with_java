package tools;

import java.util.*;

import tools.qAgent.StepResult;

public class QLearningAgent {

    static final int EPISODES = 10;
    static final double ALPHA = 0.9; // learning rate
    static final double GAMMA = 0.1; // discount factor
    static final double EPSILON = 0.1; // exploration rate

    MKP mkp;

    public Map<String, double[]> qTable = new HashMap<>();

    public QLearningAgent(MKP mkpInstance) {
        this.mkp = mkpInstance;
    }

    public static class StepResult {
        public double[] newState;
        public double reward;
        public boolean done;
        public double objValue;

        public StepResult(double[] newState, double reward, boolean done, double objValue) {
            this.newState = newState;
            this.reward = reward;
            this.done = done;
            this.objValue = objValue; // Initialize objValue to 0.0, can be set later if needed
        }
    }

    public boolean isFeasible(boolean[] solution) {
        for (int c = 0; c < this.mkp.numConstraints; c++) {
            double total = 0;
            for (int i = 0; i < this.mkp.numItems; i++) {
                if (solution[i]) {
                    total += this.mkp.weights[c][i];
                }
            }
            if (total > this.mkp.capacities[c]) {
                return false;
            }
        }
        return true;
    }

    public double reward(boolean[] solution) {
        if (!isFeasible(solution)) {
            double overPenalty = 0;
            for (int c = 0; c < this.mkp.numConstraints; c++) {
                double total = 0;
                for (int i = 0; i < this.mkp.numItems; i++) {
                    if (solution[i]) {
                        total += this.mkp.weights[c][i];
                    }
                }
                overPenalty += Math.max(0, total - this.mkp.capacities[c]);
            }
            return -10 * overPenalty; // scale penalty
        }
        double totalProfit = 0;
        for (int i = 0; i < this.mkp.numItems; i++) {
            if (solution[i])
                totalProfit += this.mkp.profits[i];
        }
        return totalProfit;
    }

    public StepResult step(int index, Candidate currentState) {
        Candidate newState = new Candidate(this.mkp, currentState.position);

        double oldFitness = newState.objValue;

        double newPos[] = Arrays.copyOf(newState.position, this.mkp.numItems);
        newPos[index] = 1 - newPos[index];

        newState.updatePosition(this.mkp, newPos);
        boolean satisfiesConstraints = newState.checkConstraints(this.mkp);

        double reward;
        if (!satisfiesConstraints) {
            reward = -10.0;
        } else {
            reward = newState.fitness - oldFitness;
        }
        return new StepResult(newState.position, reward, satisfiesConstraints, newState.objValue);
    }

    public String getStateKey(boolean[] solution) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : solution)
            sb.append(b ? "1" : "0");
        return sb.toString();
    }

    public String getStateKey(double[] solution) {
        StringBuilder sb = new StringBuilder();
        for (double b : solution)
            sb.append(b == 1.00 ? "1" : "0");
        return sb.toString();
    }

    public int chooseAction(boolean[] state) {
        String key = getStateKey(state);
        this.qTable.putIfAbsent(key, new double[this.mkp.numItems]);

        if (Math.random() < EPSILON) {
            return new Random().nextInt(this.mkp.numItems);
        }

        double[] qValues = this.qTable.get(key);
        int bestAction = 0; // index of the best action
        for (int i = 1; i < qValues.length; i++) {
            if (qValues[i] > qValues[bestAction]) {
                bestAction = i;
            }
        }
        return bestAction;
    }

    public boolean[] copySolution(boolean[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public double[] copySolution(double[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public void learn(boolean[] state) {
        // for (int episode = 0; episode < EPISODES; episode++) {
        // Candidate solution = new Candidate(this.mkp, state);
        for (int step = 0; step < state.length; step++) {
            int action = chooseAction(state);
            boolean[] nextState = copySolution(state);
            nextState[action] = !nextState[action]; // flip inclusion

            String currentKey = getStateKey(state);
            String nextKey = getStateKey(nextState);
            double r = reward(nextState);

            qTable.putIfAbsent(currentKey, new double[this.mkp.numItems]);
            qTable.putIfAbsent(nextKey, new double[this.mkp.numItems]);

            double[] currentQ = qTable.get(currentKey);
            double[] nextQ = qTable.get(nextKey);
            currentQ[step] += ALPHA * (r + GAMMA * max(nextQ) - currentQ[step]);

            // StepResult result = step(step, solution);
            // double[] nextState = result.newState;
            // double reward = result.reward;

            // qTable.putIfAbsent(currentKey, new double[this.mkp.numItems]);
            // this.qTable.putIfAbsent(key, new double[this.mkp.numItems]);

        }
        // }
    }

    public double max(double[] array) {
        double m = array[0];
        for (double v : array)
            if (v > m)
                m = v;
        return m;
    }

    public boolean[] getBestSolution() {
        Candidate cand = new Candidate(mkp, 2);
        cand.repairPosition(mkp);
        // System.out.println("cand after repair obj: " + cand.objValue);
        boolean[] solution = Candidate.doubleToBoolPositon(cand.position);

        for (int step = 0; step < 10; step++) {
            int action = chooseAction(solution);
            boolean[] newSolution = copySolution(solution);
            newSolution[action] = !newSolution[action];

            if (reward(newSolution) > reward(solution)) {
                solution = newSolution;
            }
        }
        return solution;
    }

    public void addSolution(boolean[] solution) {
        for (int step = 0; step < 10; step++) {
            int action = chooseAction(solution);
            boolean[] newSolution = copySolution(solution);
            newSolution[action] = !newSolution[action];

            if (reward(newSolution) > reward(solution)) {
                solution = newSolution;
            }
        }
    }

    public boolean[] repairSolution(Candidate cand) {

        // boolean[] solution = Candidate.creValidPosBool(this.mkp);
        boolean[] solution = Candidate.doubleToBoolPositon(cand.position);
        // boolean[] solution = Candidate.creEffPosBool(this.mkp, 2);

        for (int step = 0; step < 10; step++) {
            int action = chooseAction(solution);
            boolean[] newSolution = copySolution(solution);
            newSolution[action] = !newSolution[action];

            if (reward(newSolution) > reward(solution)) {
                solution = newSolution;
            }
        }
        // printHashmap();
        return solution;
    }

    public void addState(double[] solution) {
        // if (this.qTable.size() >= 1000)
        // return;
        String key = getStateKey(solution);
        qTable.putIfAbsent(key, new double[this.mkp.numItems]);
        // this.learn(solution);
    }

    public void saveQTableToFile(String filename) {
        HashMapToFile.saveToFile(this.qTable, filename);
    }

    public void loadQTableFromFile(String filename) {
        this.qTable = HashMapToFile.readFromFile(filename);
    }

    public void printHashmap() {
        for (Map.Entry<String, double[]> entry : this.qTable.entrySet()) {
            System.out.print("Key: " + entry.getKey() + " ");
            // for (int i = 0; i < entry.getValue().length; i++) {
            // System.out.print(entry.getValue()[i] + " ");
            // }
            System.out.print('\n');
        }
    }

    public static void main(String[] args) {
        MKP mkpInstance = new MKP("OR5x100-0.25_1");
        QLearningAgent agent = new QLearningAgent(mkpInstance);
        // agent.qTable = HashMapToFile.readFromFile("q-agent-eff-1");
        // agent.learn();

        HashMapToFile.saveToFile(agent.qTable, "q-agent-eff-1");

        boolean[] solution = agent.getBestSolution();
        System.out.println("Best repaired solution:");
        System.out.println(Arrays.toString(solution));
        System.out.println("Profit: " + agent.reward(solution));
        System.out.println("Feasible: " + agent.isFeasible(solution));
    }
}
