# coding: utf-8
# Authors: Dariusz Brzezinski <dariusz.brzezinski@cs.put.poznan.pl>
# License: MIT

import os
import pandas as pd
import numpy as np
import logging

from scoring import calculate_hist, hnorm
from sklearn.base import clone
from sklearn.datasets import make_classification
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import make_pipeline
from sklearn.model_selection import cross_validate, RepeatedStratifiedKFold
from imblearn.datasets import make_imbalance


class Experiment:
    def __init__(self, name, dataset_path, classifiers, scorers, scikit_scorers, dataset_minority_class_mapping,
                 cv_folds, cv_repetitions, seed):
        self.name = name
        self.dataset_path = dataset_path
        self.classifiers = classifiers
        self.scorers = scorers
        self.scikit_scorers = scikit_scorers
        self.dataset_minority_class_mapping = dataset_minority_class_mapping
        self.cv_folds = cv_folds
        self.cv_repetitions = cv_repetitions
        self.seed = seed

    def run(self):
        i = 0
        experiment_result = ExperimentResult(self)

        for dataset_name, minority_class in list(self.dataset_minority_class_mapping.items()):
            minority_class = self.dataset_minority_class_mapping[dataset_name]
            dataset = ExperimentDataset(self.dataset_path, dataset_name, minority_class)
            experiment_result.append_dataset(dataset, i)

            for s in self.scorers:
                experiment_result.hists[s] = calculate_hist(self.scorers[s], dataset.y)

            for c in self.classifiers:
                logging.info(dataset_name + ": " + str(c) + "... ")
                clf = make_pipeline(StandardScaler(), clone(self.classifiers[c]))
                cv_result = cross_validate(clf, dataset.X, dataset.y.values.ravel(), scoring=self.scikit_scorers,
                                           cv=RepeatedStratifiedKFold(self.cv_folds, self.cv_repetitions, self.seed),
                                           return_train_score=False, n_jobs=-1)

                for s in self.scikit_scorers:
                    normalized_cv_result = hnorm(cv_result["test_" + s], experiment_result.hists[s])
                    experiment_result.append_cv_result(dataset, c, s, cv_result["test_" + s], "Standard", i)
                    experiment_result.append_cv_result(dataset, c, s, normalized_cv_result, "Normalized", i+1)

            i = i + 2

        experiment_result.save_datasets_df()
        experiment_result.save_results_df()


class ExperimentDataset:
    def __init__(self, dataset_path, name, minority_class):
        self.name = name
        self.minority_class = minority_class
        self.X = pd.read_csv(os.path.join(dataset_path, self.name + "_X.csv"), index_col=False)
        self.y = pd.read_csv(os.path.join(dataset_path, self.name + "_y.csv"), index_col=False)

        self.description = self._get_description()

    def _get_description(self):
        dataset = {}
        dataset["Name"] = self.name
        dataset["Examples"] = self.X.shape[0]
        dataset["Features"] = self.X.shape[1]
        dataset["Imbalance ratio"] = float(self.y.iloc[:, 0].value_counts()[1]) / float(self.y.shape[0])
        dataset["Minority class"] = self.minority_class

        return dataset


class ExperimentResult:
    def __init__(self, experiment):
        self.experiment = experiment
        self.result_df = None
        self.dataset_df = None
        self.hists = dict()

    def append_dataset(self, dataset, id):
        if self.dataset_df is None:
            self.dataset_df = pd.DataFrame(dataset.description, index=[id])
        else:
            self.dataset_df = self.dataset_df.append(pd.DataFrame(dataset.description, index=[id]))

    def append_cv_result(self, dataset, classifier, scorer, cv_test_result, type, id):
        result = {"Id": 0, "Dataset": dataset.name, "Classifier": classifier,
                  "Measure": None, "Type": None, "Mean": 0, "Std": 0}
        result["Measure"] = scorer
        result["Mean"] = np.mean(cv_test_result)
        result["Std"] = np.std(cv_test_result)
        result["Type"] = type
        result["Id"] = id

        if self.result_df is None:
            self.result_df = pd.DataFrame(result, index=[id])
        else:
            self.result_df = self.result_df.append(pd.DataFrame(result, index=[id]))

    def save_datasets_df(self):
        csv_df = self.dataset_df[["Name", "Examples", "Features", "Imbalance ratio", "Minority class"]]
        csv_df.to_csv(self.experiment.name + "Datasets.csv", index=False)

    def save_results_df(self):
        csv_df = self.result_df[["Id", "Dataset", "Classifier", "Measure", "Type", "Mean", "Std"]]
        csv_df.to_csv(self.experiment.name + "ExperimentResults.csv", index=False)


def sample_dataset(X, y, base_dataset_size, minority_class_ratio, seed):
    maj_num = base_dataset_size / 2.0 * (1.0 - minority_class_ratio)
    min_num = base_dataset_size / 2.0 * (minority_class_ratio)

    return make_imbalance(X, y, sampling_strategy={0: int(maj_num), 1: int(min_num)}, random_state=seed)


def generate_synthetic_datasets(base_dataset_size, feature_sets, ratios, save_to_folder, seed):
    minority_class_mapping = {}

    for num_of_features in feature_sets:
        X, y = make_classification(n_samples=base_dataset_size + 100, n_features=num_of_features, n_redundant=0, n_classes=2,
                                   n_informative=num_of_features, random_state=seed, weights=[0.5, 0.5])

        for ratio in ratios:
            dataset_name = "Synthetic" + \
                           "_n=" + str(base_dataset_size) + \
                           "_p=" + str(num_of_features) + \
                           "_r=" + str(ratio).replace('.', '_')
            logging.info("Creating dataset: " + dataset_name)
            minority_class_mapping[dataset_name] = "Minority"
            X_sampled, y_sampled = sample_dataset(X, y, base_dataset_size, ratio, seed)

            if not os.path.exists(save_to_folder):
                os.makedirs(save_to_folder)

            pd.DataFrame(X_sampled).to_csv(os.path.join(save_to_folder, dataset_name + "_X.csv"), index=False)
            pd.DataFrame(y_sampled).to_csv(os.path.join(save_to_folder, dataset_name + "_y.csv"), index=False)

    return minority_class_mapping