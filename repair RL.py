import numpy as np
import gym
from gym import spaces
import torch
import torch.nn as nn
import torch.optim as optim
import random
from collections import deque


class MKPRepairEnv(gym.Env):
    def __init__(self, profits, weights, capacities):
        super(MKPRepairEnv, self).__init__()
        self.profits = np.array(profits)
        self.weights = np.array(weights)
        self.capacities = np.array(capacities)
        self.n_items = len(profits)
        self.n_dims = len(capacities)

        self.action_space = spaces.Discrete(self.n_items)
        self.observation_space = spaces.MultiBinary(self.n_items)
        self.state = None

    def reset(self):
        # Start with a random solution (possibly infeasible)
        self.state = np.random.randint(0, 2, size=self.n_items)
        return self.state

    def step(self, action):
        # Flip item at action index
        self.state[action] = 1 - self.state[action]

        profit = np.sum(self.profits * self.state)
        total_weights = self.weights @ self.state
        overuse = np.maximum(0, total_weights - self.capacities)
        penalty = np.sum(overuse) * 10  # penalize hard constraint violations

        done = np.all(total_weights <= self.capacities)

        reward = profit - penalty

        return self.state.copy(), reward, done, {}

    def render(self):
        print(f"State: {self.state}, Profit: {np.sum(self.profits * self.state)}")


class DQN(nn.Module):
    def __init__(self, input_dim, output_dim):
        super(DQN, self).__init__()
        self.net = nn.Sequential(
            nn.Linear(input_dim, 128),
            nn.ReLU(),
            nn.Linear(128, 64),
            nn.ReLU(),
            nn.Linear(64, output_dim)
        )

    def forward(self, x):
        return self.net(x)


class DQNAgent:
    def __init__(self, state_size, action_size, gamma=0.99, lr=1e-3, batch_size=64, buffer_size=10000):
        self.state_size = state_size
        self.action_size = action_size
        self.gamma = gamma
        self.lr = lr
        self.batch_size = batch_size

        self.memory = deque(maxlen=buffer_size)
        self.model = DQN(state_size, action_size)
        self.target_model = DQN(state_size, action_size)
        self.optimizer = optim.Adam(self.model.parameters(), lr=lr)

    def act(self, state, eps=0.1):
        if random.random() < eps:
            return random.randint(0, self.action_size - 1)
        with torch.no_grad():
            state = torch.FloatTensor(state).unsqueeze(0)
            q_values = self.model(state)
            return q_values.argmax().item()

    def remember(self, s, a, r, s2, done):
        self.memory.append((s, a, r, s2, done))

    def update(self):
        if len(self.memory) < self.batch_size:
            return
        batch = random.sample(self.memory, self.batch_size)
        s, a, r, s2, d = zip(*batch)

        s = torch.FloatTensor(s)
        a = torch.LongTensor(a).unsqueeze(1)
        r = torch.FloatTensor(r).unsqueeze(1)
        s2 = torch.FloatTensor(s2)
        d = torch.FloatTensor(d).unsqueeze(1)

        q_values = self.model(s).gather(1, a)
        next_q_values = self.target_model(s2).max(1, keepdim=True)[0]
        expected_q = r + (1 - d) * self.gamma * next_q_values

        loss = nn.MSELoss()(q_values, expected_q)

        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()

    def update_target(self):
        self.target_model.load_state_dict(self.model.state_dict())


def train_mkp_dqn(env, agent, episodes=1000, target_update=20):
    for ep in range(episodes):
        state = env.reset()
        total_reward = 0
        done = False
        for t in range(200):  # limit steps per episode
            action = agent.act(state)
            next_state, reward, done, _ = env.step(action)
            agent.remember(state, action, reward, next_state, done)
            agent.update()
            state = next_state
            total_reward += reward
            if done:
                break
        if ep % target_update == 0:
            agent.update_target()
        if ep % 50 == 0:
            print(f"Episode {ep}, Reward: {total_reward}")


def read_mkp_data(filepath: str) -> list:
    """
        Reads the data of a OR-MxN mkp file which has the following structure:\n
        #NUM_VARIABLES(N) #NUM_CONSTRAINTS(M) #OPTIMAL_VALUE\n
        [PROFITS]\n
        [[CONSTRAINTS]]\n
        [CAPACITIES]\n
        Returns :\n
        [ [n_var, n_constr, optimal_val] , list_profits, matrix_constraints, list_capacities]
    """
    with open(filepath, 'r') as f:
        numbers = list(map(int, f.read().split()))
    num_variables, num_constraints, obj_value = numbers[:3]
    # Profit array
    profit_list = numbers[3:3 + num_variables]
    # Constraint matrix
    start = 3 + num_variables
    end = start + num_constraints * num_variables
    constraint_matrix = [numbers[i:i + num_variables]
                         for i in range(start, end, num_variables)]
    # Constraint right-side array
    capacity_list = numbers[end:end + num_constraints]
    return [numbers[:3], np.array(profit_list), np.array(constraint_matrix), np.array(capacity_list)]


# Problem setup
metadata, values, weights, capacities = read_mkp_data("All-MKP-Instances\\chubeas\\OR5x100\\OR5x100-0.25_1.dat")
n_items = metadata[0]
n_constraints = metadata[1]
# values = np.random.randint(10, 100, size=n_items)
# weights = np.random.randint(1, 20, size=(n_items, n_constraints))
# capacities = np.random.randint(100, 200, size=n_constraints)

# Init
# env = MKPRepairEnv(values, weights, capacities)
# agent = DQNAgent(state_dim=n_items, action_dim=n_items)
env = MKPRepairEnv(values, weights, capacities)
agent = DQNAgent(state_size=n_items, action_size=n_items)
train_mkp_dqn(env, agent, episodes=500)
# Training
# episodes = 1000
# target_update_freq = 10

# for ep in range(episodes):
#     state = env.reset()
#     done = False
#     total_reward = 0
#     steps = 0

#     while not done and steps < n_items:
#         action = agent.act(state)
#         next_state, reward, done = env.step(action)
#         agent.remember(state, action, reward, next_state, done)
#         agent.learn()
#         state = next_state
#         total_reward += reward
#         steps += 1

#     if ep % target_update_freq == 0:
#         agent.update_target()

#     if ep % 50 == 0:
#         print(f"Episode {ep} | Steps: {steps} | Total reward: {total_reward:.2f} | Epsilon: {agent.eps:.2f}")

# solution = np.random.randint(0, 2, n_items)
# while env.is_feasible(solution):
#     solution = np.random.randint(0, 2, n_items)

# print("Before Repair - Feasible?", env.is_feasible(solution))
# state = env.reset(solution)
# done = False

# while not done:
#     action = agent.act(state)
#     state, reward, done = env.step(action)

# print("After Repair - Feasible?", env.is_feasible(state))
# print("Repaired Solution Value:", np.dot(state, values))
