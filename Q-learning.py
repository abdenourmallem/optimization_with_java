from numpy.random import randint
from numpy.random import rand, uniform
import numpy as np
import time
import tools.efficiency_functions as efficiency_functions
import threading
import tools.sol_verif as sol_verif
import matplotlib.pyplot as plt


def read_mkp_data(filename):
    with open(filename, 'r') as f:
        numbers = list(map(int, f.read().split()))

    num_variables, num_constraints, obj_value = numbers[:3]

    # Profit array
    profit_array = np.array(numbers[3:3 + num_variables])

    # Constraint matrix
    start = 3 + num_variables
    end = start + num_constraints * num_variables
    constraint_matrix = np.array([numbers[i:i + num_variables]
                                  for i in range(start, end, num_variables)])

    # Constraint right-side array
    rightside_array = np.array(numbers[end:end + num_constraints])

    return [profit_array, constraint_matrix, rightside_array]


P, W, C = read_mkp_data('All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat')


class MKPEnv:
    def init(self, profits, weights, capacities):
        self.profits = np.array(profits)
        self.weights = np.array(weights)
        self.capacities = np.array(capacities)
        self.n_items = len(profits)

    def reset(self):
        self.state = np.random.randint(0, 2, size=self.n_items)
        return self.state.copy()

    def step(self, action):
        self.state[action] = 1 - self.state[action]
        profit = np.sum(self.profits * self.state)
        total_weights = self.weights @ self.state
        overuse = np.maximum(0, total_weights - self.capacities)
        penalty = np.sum(overuse) * 10
        reward = profit - penalty
        done = np.all(total_weights <= self.capacities)
        return self.state.copy(), reward, done


def train_q_learning(env, episodes=1000, alpha=0.1, gamma=0.9, epsilon=0.1):
    n_states = 2 ** env.n_items  # only feasible for small n_items
    n_actions = env.n_items
    Q = {}

    def get_state_key(state):
        return ''.join(map(str, state))

    for ep in range(episodes):
        state = env.reset()
        for step in range(200):
            state_key = get_state_key(state)
            if state_key not in Q:
                Q[state_key] = np.zeros(n_actions)

            # Epsilon-greedy action
            if np.random.rand() < epsilon:
                action = np.random.randint(n_actions)
            else:
                action = np.argmax(Q[state_key])

            next_state, reward, done = env.step(action)
            next_key = get_state_key(next_state)
            if next_key not in Q:
                Q[next_key] = np.zeros(n_actions)

            # Q-learning update
            Q[state_key][action] += alpha * (reward + gamma * np.max(Q[next_key]) - Q[state_key][action])
            state = next_state

            if done:
                break

        if ep % 50 == 0:
            print(f"Episode {ep} done.")

    return Q


def q_learning_local_search(solution, Q, env):
    state = solution.copy()
    for _ in range(100):
        state_key = ''.join(map(str, state))
        if state_key not in Q:
            break
        action = np.argmax(Q[state_key])
        next_state, _, done = env.step(action)
        state = next_state
        if done:
            break
    return state


def objective(bitstring, p):
    return np.dot(bitstring, p)


env = MKPEnv(P, W, C)  # your MKP environment
# Q = train_q_learning(env, episodes=1000, alpha=0.1, gamma=0.9, epsilon=0.1)
# state=q_learning_local_search(env.reset(), Q, env)
# print("Final state:", state)
# print("Objective value:", objective(state, P))
