import pandas as pd
import pylab
import matplotlib.pyplot as plt
import numpy as np
import sys
import glob
import os

output_path = "../output/"
large = "large_3_3/"
valueDir = "value/"
errorDir = "error/"
file_list = glob.glob(output_path+large+valueDir+'*.csv')
args = sys.argv
max = int(args[1])

for file in file_list:
	df = pd.read_csv(file)
	x = df['ActionSet']
	y1 = df['SGD']
	y2 = df['GD']
	y3 = df['Hybrid']

	plt.figure(figsize=(15, 10), dpi=100)
	plt.plot(x, y1, label="SGD")
	plt.plot(x, y2, label="GD")
	plt.plot(x, y3, label="Hybrid")
	plt.legend()
	plt.xticks(np.arange(0, max, 2000))
	plt.yticks(np.arange(0.0, 1.0, 0.05))
	plt.hlines(np.arange(0.0, 1.0, 0.05), 0, max, linestyle='dashed')
	basename = os.path.basename(file)
	filename, ext = os.path.splitext(basename)
	image_filename = "./"+large+valueDir+filename+".png"
	plt.savefig(image_filename)
	plt.close()
	print(image_filename+" is output!!")

file_list = glob.glob(output_path+large+errorDir+'*.csv')

for file in file_list:
        df = pd.read_csv(file)
        x = df['ActionSet']
        y1 = df['SGD']
        y2 = df['GD']
        y3 = df['Hybrid']

        plt.figure(figsize=(15, 10), dpi=100)
        plt.plot(x, y1, label="SGD")
        plt.plot(x, y2, label="GD")
        plt.plot(x, y3, label="Hybrid")
        plt.legend()
        plt.xticks(np.arange(0, max, 2000))
        plt.yticks(np.arange(0.0, 0.5, 0.05))
        plt.hlines(np.arange(0.0, 0.5, 0.05), 0, max, linestyle='dashed')
        basename = os.path.basename(file)
        filename, ext = os.path.splitext(basename)
        image_filename = "./"+large+errorDir+filename+".png"
        plt.savefig(image_filename)
        plt.close()
        print(image_filename+" is output!!")


