import java.util.Random;

public class genetic_operators {
    public static int[][] selection(int[][] population, double[] scores) {
        Random rand = new Random();
        int[][] new_population = new int[population.length][population[0].length];
        int[] random_idx = new int[5];
        for (int p = 0; p < population.length; p++) {
            for (int i = 0; i < 5; i++) {
                int rn = rand.nextInt(population.length);
                if (random_idx[0] == rn) {
                    i--;
                    continue;
                }
                random_idx[i] = rn;
            }
            int max_idx = random_idx[0];
            for (int i = 1; i < random_idx.length; i++) {
                if (scores[random_idx[i]] > scores[max_idx]) {
                    max_idx = random_idx[i];
                }
            }
            new_population[p] = population[max_idx];
        }

        return new_population;
    }

    public static int[][] crossover(int[] parent1, int[] parent2) {
        Random rand = new Random();
        int crossover_point = rand.nextInt(parent1.length - 1) + 1;
        int[] child1 = new int[parent1.length];
        int[] child2 = new int[parent1.length];
        for (int i = 0; i < crossover_point; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
        }
        for (int i = crossover_point; i < parent2.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return new int[][] { child1, child2 };

    }

    public static int[] mutation(int[] individual, int nmp) {
        Random rand = new Random();
        for (int i = 0; i < nmp; i++) {
            int mutation_point = rand.nextInt(individual.length);
            individual[mutation_point] = 1 - individual[mutation_point];
        }

        return individual;
    }
}