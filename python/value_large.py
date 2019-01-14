import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np

output_path = "../output/"
file = output_path+"value_large.csv"

df = pd.read_csv(file)
x = df['ActionSet']
y1 = df['SGD']
y2 = df['GD']

plt.figure(figsize=(15, 10), dpi=100)
plt.plot(x, y1, label="SGD")
plt.plot(x, y2, label="GD")
plt.legend()
plt.xticks(np.arange(0, 50000, 5000))
plt.yticks(np.arange(0.0, 1.0, 0.05))
plt.hlines(np.arange(0.0, 1.0, 0.05), 0, 50000, linestyle='dashed')
plt.savefig("value_large.png")
plt.close()
