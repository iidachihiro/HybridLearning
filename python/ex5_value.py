import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np
import sys

output_path = "../output/"
ex5 = "ex5/"
args = sys.argv
max = int(args[1])

file = output_path+ex5+"ValuesEX5.csv"

df = pd.read_csv(file)
x = df['ActionSet']
y = df['arrive.m_move.e_arrive.e']

plt.figure(figsize=(15, 10), dpi = 100)
plt.plot(x, y, label="Value")
plt.legend()
plt.xticks(np.arange(0, max, 500))
plt.yticks(np.arange(0.0, 1.0, 0.1))
plt.hlines(np.arange(0.0, 1.0, 0.1), 0, max, linestyle='dashed')
plt.savefig("ex5.png")
plt.close()
