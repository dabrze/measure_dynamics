# coding: utf-8
# Authors: Dariusz Brzezinski <dariusz.brzezinski@cs.put.poznan.pl>
# License: MIT

import os
import scoring
import logging
logging.basicConfig(format='%(asctime)s - %(message)s', datefmt='%d-%b-%y %H:%M:%S', level=logging.INFO)

from sklearn.metrics import make_scorer, get_scorer, cohen_kappa_score,\
    matthews_corrcoef
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC

from experiments import Experiment

if __name__ == '__main__':
    SEED = 23
    CV_FOLDS = 10
    CV_REPS = 10
    DATA_PATH = os.path.join(os.path.dirname(__file__), "data")
    NAME = "Normalization"

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
        "Accuracy": scoring.accuracy,
        "Bal. acc.": scoring.balanced_accuracy,
        "Recall": scoring.recall,
        "Precision": scoring.precision,
        "F1-score": scoring.f1_score,
        "G-mean": scoring.g_mean,
        "Kappa": scoring.kappa,
        "MCC": scoring.mcc,
    }
    scikit_scorers = {
        "Accuracy": get_scorer("accuracy"),
        "Bal. acc.": get_scorer("recall_macro"),
        "Recall": get_scorer("recall"),
        "Precision": get_scorer("precision"),
        "F1-score": get_scorer("f1"),
        "G-mean": make_scorer(scoring.g_mean_scikit),
        "Kappa": make_scorer(cohen_kappa_score),
        "MCC": make_scorer(matthews_corrcoef),
    }
    dataset_minority_class_mapping = {
        "transfusion": "yes", "glass": "v-float", "breast-w": "malignant",
        "ionosphere": "bad", "new-thyroid": "hyper", "arcene": "positive",
        "colon": "1", "micromass": "AUG.AEX", "yeast": "ME2", "solar-flare": "F",
        "ecoli": "imU", "credit-g": "bad"
    }

    experiment = Experiment(NAME, DATA_PATH, classifiers, scorers, scikit_scorers, dataset_minority_class_mapping,
                            CV_FOLDS, CV_REPS, SEED)
    experiment.run()
