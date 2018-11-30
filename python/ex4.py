import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np

output_path = "../output/"
file_name = "ErrorValues.csv"
file_path = output_path+file_name

df = pd.read_csv(file_path)
x = df['ActionSet']
y1 = df['SGD']
y2 = df['GD']
y3 = df['Hybrid']
plt.figure(figsize=(15, 10), dpi=100)
plt.plot(x, y1, label="SGD", marker="o", markersize=1)
plt.plot(x, y2, label="GD")
plt.plot(x, y3, label="Hybrid")
plt.legend()
plt.xticks(np.arange(0, 7500, 500))
plt.yticks(np.arange(0.0, 0.5, 0.05))
plt.savefig("ex4.png")
