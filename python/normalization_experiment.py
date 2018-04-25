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
from sklearn.model_selection import cross_val_score, RepeatedStratifiedKFold
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC

SEED = 23
DATA_PATH = os.path.join(os.path.dirname(__file__), "data")
breast_X = os.path.join(DATA_PATH, "breast-w_X.csv")
breast_y = os.path.join(DATA_PATH, "breast-w_y.csv")
thyroid_X = os.path.join(DATA_PATH, "new-thyroid_X.csv")
thyroid_y = os.path.join(DATA_PATH, "new-thyroid_y.csv")

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
    "Accuracy": get_scorer("accuracy"),
    "Balanced accuracy": get_scorer("recall_macro"),
    "Recall": get_scorer("recall"),
    "Precision": get_scorer("precision"),
    "F1-score": get_scorer("f1"),
    "G-mean": make_scorer(utils.g_mean),
    "Kappa": make_scorer(cohen_kappa_score),
    "MCC": make_scorer(matthews_corrcoef),
}
datasets = {
    "transfusion": (pd.read_csv(os.path.join(DATA_PATH, "transfusion_X.csv"),
                                index_col=False),
                    pd.read_csv(os.path.join(DATA_PATH, "transfusion_y.csv"),
                                index_col=False)),
    "glass": (pd.read_csv(os.path.join(DATA_PATH, "glass_X.csv"),
                          index_col=False),
              pd.read_csv(os.path.join(DATA_PATH, "glass_y.csv"),
                          index_col=False)),
    "breast-w": (pd.read_csv(os.path.join(DATA_PATH, "breast-w_X.csv"),
                             index_col=False),
                 pd.read_csv(os.path.join(DATA_PATH, "breast-w_y.csv"),
                             index_col=False)),
    "ionosphere": (pd.read_csv(os.path.join(DATA_PATH, "ionosphere_X.csv"),
                               index_col=False),
                   pd.read_csv(os.path.join(DATA_PATH, "ionosphere_y.csv"),
                               index_col=False)),
    "new-thyroid": (pd.read_csv(os.path.join(DATA_PATH, "new-thyroid_X.csv"),
                                index_col=False),
                    pd.read_csv(os.path.join(DATA_PATH, "new-thyroid_y.csv"),
                                index_col=False)),
    "balance-scale": (pd.read_csv(os.path.join(DATA_PATH, "balance-scale_X.csv"),
                          index_col=False),
              pd.read_csv(os.path.join(DATA_PATH, "balance-scale_y.csv"),
                          index_col=False)),
}

if __name__ == '__main__':
    i = 0
    result_df = pd.DataFrame()
    hists = dict()

    for d in datasets:
        X = datasets[d][0]
        y = datasets[d][1]

        print(str(d) + ": Calculating histograms for all scorers...")
        for s in scorers:
            hists[s] = utils.calculate_hist(scorers[s], y)

        for c in classifiers:
            result = {"Accuracy": 0, "Balanced accuracy": 0, "Recall": 0,
                      "Precision": 0, "F1-score": 0, "G-mean": 0, "Kappa": 0,
                      "MCC": 0, "Classifier": c, "Dataset": d,
                      "Accuracy norm": 0, "Balanced accuracy norm": 0,
                      "Recall norm": 0, "Precision norm": 0,
                      "F1-score norm": 0, "G-mean norm": 0, "Kappa norm": 0,
                      "MCC norm": 0}

            for s in scorers:
                print(str(d) + ": " + str(c) + ": " + str(s) + "... ")
                clf = make_pipeline(StandardScaler(), clone(classifiers[c]))
                r = cross_val_score(clf, X, y, scoring=scorers[s],
                                    cv=RepeatedStratifiedKFold(10, 10, SEED))
                result[s] = np.mean(r)
                result[s + " norm"] = np.mean(utils.hnorm(r, hists[s]))

            if result_df is None:
                result_df = pd.DataFrame(result, index=[i])
            else:
                result_df = result_df.append(pd.DataFrame(result, index=[i]))
            i = i + 1

    result_df = result_df[["Dataset", "Classifier", "Accuracy",
                           "Balanced accuracy", "Kappa", "G-mean", "F1-score",
                           "Precision", "Recall", "MCC", "Accuracy norm",
                           "Balanced accuracy norm", "Kappa norm",
                           "G-mean norm", "F1-score norm", "Precision norm",
                           "Recall norm", "MCC norm"]]
    result_df.to_csv("ExperimentResults.csv", index=False)


