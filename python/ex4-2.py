import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np
import sys

output_path = "../output/"
file_name = "ErrorValues.csv"
file_path = output_path+file_name

args = sys.argv
max = int(args[1])

df = pd.read_csv(file_path)
x = df['ActionSet']
y1 = df['SGD']
y2 = df['GD']
y3 = df['Hybrid(0.001)']
y4 = df['Hybrid(0.005)']
y5 = df['Hybrid(0.01)']
y6 = df['Hybrid(0.05)']
y7 = df['Hybrid(0.1)']
y8 = df['Hybrid(0.5)']

plt.figure(figsize=(15, 10), dpi=100)
plt.plot(x, y1, label="SGD(0.1)")
plt.plot(x, y2, label="GD(0.1)")
plt.plot(x, y3, label="Hybrid(0.001)")
plt.plot(x, y4, label="Hybrid(0.005)")
plt.plot(x, y5, label="Hybrid(0.01)")
plt.plot(x, y6, label="Hybrid(0.05)")
plt.plot(x, y7, label="Hybrid(0.1)")
plt.plot(x, y8, label="Hybrid(0.5)")
plt.legend()
plt.xticks(np.arange(0, max, 500))
plt.yticks(np.arange(0.0, 0.5, 0.05))
plt.hlines(np.arange(0.0, 0.5, 0.05), 0, max, linestyle='dashed')
plt.savefig("ex4.png")

print("ex4.png is output!!")
