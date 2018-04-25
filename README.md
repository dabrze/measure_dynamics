# Measure Dynamics

Source codes, reproducible experiment scripts, and numerical results accompanying the paper: "On the Dynamics of Classification Measures for Imbalanced Data" by Brzezinski, D. *et al.*

The repository is divided into three main sections (folders): 
- `matlab`
- `python`
- `java`

## matlab

The `matlab` folder contains code responsible for generating all possible confusion matrices for a given size of data set *n*, and visualizing this data in the form of histograms of measure values and gradients in barycentric space.

To run the code, simply run the file `run_visualizations.m` in [matlab](https://www.mathworks.com/products/matlab.html). After a certain set of visualizations is displayed, press Enter to generate the next set of images.

## python

The `python` folder contains source code and data sets needed to run the measure normalization experiments. The following libraries are required:
- pandas 
- numpy
- scipy
- scikit-learn

Package  versions are listed in the `requirements.txt` file.

To run the code, use the following command:
```{python}
python normalization_experiment.py
```

## java

The `java` folder contains classes required to reproduce the drift detection and class ratio change experiments. The experiments run [MOA](https://moa.cms.waikato.ac.nz/).

To re-run the experiments include into MOA the classes from the `moa` folder and run java with the follwoing to classes: `moa.experiments.PHTests.java` and `moa.experiments.RatioChangeTests.java`. The plots for the ratio change experiment can be reproduced using [gnuplot](http://www.gnuplot.info/) and the `visualize.py` script.