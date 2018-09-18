# coding: utf-8
# Authors: Dariusz Brzezinski <dariusz.brzezinski@cs.put.poznan.pl>
# License: MIT

import os
import utils
import pandas as pd
import numpy as np

from sklearn.metrics import make_scorer, get_scorer, cohen_kappa_score,\
    matthews_corrcoef
from sklearn.base import clone
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import make_pipeline
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.model_selection import cross_validate, RepeatedStratifiedKFold
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC

SEED = 23
DATA_PATH = os.path.join(os.path.dirname(__file__), "data")

classifiers = {
    "kNN": KNeighborsClassifier(),
    "NB": GaussianNB(),
    "SVM": SVC(random_state=SEED),
    "LR": LogisticRegression(random_state=SEED),
    "CART": DecisionTreeClassifier(random_state=SEED),
    "RF": RandomForestClassifier(random_state=SEED),
    "GBM": GradientBoostingClassifier(random_state=SEED),
}
scorers = {
    "Accuracy": utils.accuracy,
    "Bal. acc.": utils.balanced_accuracy,
    "Recall": utils.recall,
    "Precision": utils.precision,
    "F1-score": utils.f1_score,
    "G-mean": utils.g_mean,
    "Kappa": utils.kappa,
    "MCC": utils.mcc,
}
scikit_scorers = {
    "Accuracy": get_scorer("accuracy"),
    "Bal. acc.": get_scorer("recall_macro"),
    "Recall": get_scorer("recall"),
    "Precision": get_scorer("precision"),
    "F1-score": get_scorer("f1"),
    "G-mean": make_scorer(utils.g_mean_scikit),
    "Kappa": make_scorer(cohen_kappa_score),
    "MCC": make_scorer(matthews_corrcoef),
}
datasets = {
    "transfusion": "yes", "glass": "v-float", "breast-w": "malignant",
    "ionosphere": "bad", "new-thyroid": "hyper", "arcene": "positive",
    "colon": "1", "micromass": "AUG.AEX", "yeast": "ME2", "solar-flare": "F",
    "ecoli": "imU", "credit-g": "bad"
}


if __name__ == '__main__':
    i = 0
    result_df = pd.DataFrame()
    dataset_df = pd.DataFrame()
    hists = dict()

    for d_idx, d in enumerate(datasets.keys()):
        print(str(d))
        X = pd.read_csv(os.path.join(DATA_PATH, d + "_X.csv"), index_col=False)
        y = pd.read_csv(os.path.join(DATA_PATH, d + "_y.csv"), index_col=False)

        dataset = {}
        dataset["Name"] = d
        dataset["Examples"] = X.shape[0]
        dataset["Features"] = X.shape[1]
        dataset["Imbalance ratio"] = float(y.iloc[:, 0].value_counts()[1])/float(y.shape[0])
        dataset["Minority class"] = datasets[d]

        if dataset_df is None:
            dataset_df = pd.DataFrame(dataset, index=[d_idx])
        else:
            dataset_df = dataset_df.append(pd.DataFrame(dataset, index=[d_idx]))

        print(str(d) + ": Calculating pmf for all scorers...")
        for s in scorers:
            hists[s] = utils.calculate_hist(scorers[s], y)

        for c in classifiers:
            print(str(d) + ": " + str(c) + "... ")
            clf = make_pipeline(StandardScaler(), clone(classifiers[c]))

            r = cross_validate(clf, X, y, scoring=scikit_scorers,
                               cv=RepeatedStratifiedKFold(10, 10, SEED),
                               return_train_score=False)

            for s in scikit_scorers:
                result = {"Id": 0, "Dataset": d, "Classifier": c,
                          "Measure": None, "Type": None, "Mean": 0, "Std": 0}
                result["Measure"] = s
                result["Mean"] = np.mean(r["test_" + s])
                result["Std"] = np.std(r["test_" + s])
                result["Type"] = "Standard"
                result["Id"] = i
                if result_df is None:
                    result_df = pd.DataFrame(result, index=[i])
                else:
                    result_df = result_df.append(pd.DataFrame(result, index=[i]))

                result["Measure"] = s
                result["Mean"] = np.mean(utils.hnorm(r["test_" + s], hists[s]))
                result["Std"] = np.std(utils.hnorm(r["test_" + s], hists[s]))
                result["Type"] = "Normalized"
                result["Id"] = i + 1
                result_df = result_df.append(pd.DataFrame(result, index=[i]))
            i = i + 2

    result_df = result_df[["Id", "Dataset", "Classifier", "Measure",
                           "Type", "Mean", "Std"]]
    result_df.to_csv("ExperimentResults.csv", index=False)

    dataset_df = dataset_df[["Name", "Examples", "Features", "Imbalance ratio",
                             "Minority class"]]
    dataset_df.to_csv("Datasets.csv", index=False)
