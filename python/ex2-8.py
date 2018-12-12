import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np
import sys

output_path = "../output/"
file_name = "diff_mode8.csv"
file_path = output_path+file_name

args = sys.argv
max = int(args[1])

df = pd.read_csv(file_path)
x = df['ActionSet']
y = df['Difference']

plt.figure(figsize=(15, 10), dpi=100)
plt.plot(x, y, label="Difference")
plt.legend()
plt.xticks(np.arange(0, max, 500))
plt.yticks(np.arange(-0.6, 0.6, 0.2))
plt.hlines(np.arange(-0.6, 0.6, 0.2), 0, max, linestyle='dashed')
plt.vlines(np.arange(0, max, 500), -0.6, 0.6, linestyle='dashed')
plt.savefig("ex2-8.png")

print("ex2-8.png is output!!")
