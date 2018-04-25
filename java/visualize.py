import pandas as pd
import subprocess
import os

results_folder = "results"
output_folder = "images"
gnuplot_folder = "D:/Tools/Graphics/gnuplot/bin"

plot_definition = """set term postscript color enhanced font 'Helvetica,18'
set output '{0}'
set datafile separator ','
set grid

set style line 1 lt rgb '#e41a1c'
set style line 6 lt rgb '#377eb8'
set style line 5 lt rgb '#4daf4a'
set style line 7 lt rgb '#984ea3'
set style line 2 lt rgb '#ff7f00'
set style line 3 lt rgb '#999999'
set style line 4 lt rgb '#a65628'
set style line 8 lt rgb '#f781bf'

set format x '%.0s %c'
set format y '%.0s'
set ylabel '{1}'
set xlabel 'Processed instances'
set key nobox vertical bottom right inside
set format y '%1.2f'
plot  [{2}:{3}][{4}:{5}]"""
line_definition = "'{0}' using 1:{1}:(1.0) with linespoints ls {2} lw 3 pointinterval 4 title '{3}'"
min_x = "500"
max_x = ""
min_y = "0"
max_y = "1"

measures = ["Accuracy", "Bal. Acc.", "Kappa", "G-mean", "F1-score", "Precision", "Recall", "MCC"]
measure_columns = {"Accuracy": 5, "Bal. Acc.": 6, "Kappa": 7, "G-mean": 8, "F1-score": 9, "Precision": 10, "Recall": 11, "MCC": 12}

def run_gnuplot(script):
    with open("tmp.plt", "w") as script_file:
        script_file.write(script)

    subprocess.call(os.path.join(gnuplot_folder, "gnuplot") + " tmp.plt", cwd=os.path.dirname(__file__), shell=True)
    os.remove("tmp.plt")


if __name__ == "__main__":
    for file in ["RatioChangeNMinority.csv", "RatioChangePMinority.csv"]:
        print("Visualizations for {0} dataset".format(file))

        # All measures for each classifier separately
        script = plot_definition.format(os.path.join(os.path.abspath(output_folder), file[:-4] + ".eps"),
                                        "Measure value", min_x, max_x, min_y, max_y)

        for line_num, measure in enumerate(measures):
            script += line_definition.format(os.path.join(os.path.abspath(results_folder), file),
                                             measure_columns[measure], line_num + 1, measure) + ", "
        run_gnuplot(script[:-2])
