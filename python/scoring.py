# coding: utf-8
# Authors: Dariusz Brzezinski <dariusz.brzezinski@cs.put.poznan.pl>
# License: MIT

import numpy as np
import scipy as sp

from sklearn.utils.multiclass import unique_labels
from sklearn.metrics.classification import _prf_divide
from sklearn.preprocessing import LabelEncoder


def g_mean_scikit(y_true, y_pred, labels=None, correction=0.0):
    present_labels = unique_labels(y_true, y_pred)

    if labels is None:
        labels = present_labels
        n_labels = None
    else:
        n_labels = len(labels)
        labels = np.hstack([labels, np.setdiff1d(present_labels, labels, assume_unique=True)])

    le = LabelEncoder()
    le.fit(labels)
    y_true = le.transform(y_true)
    y_pred = le.transform(y_pred)
    sorted_labels = le.classes_

    # labels are now from 0 to len(labels) - 1 -> use bincount
    tp = y_true == y_pred
    tp_bins = y_true[tp]

    if len(tp_bins):
        tp_sum = np.bincount(tp_bins, weights=None, minlength=len(labels))
    else:
        # Pathological case
        true_sum = tp_sum = np.zeros(len(labels))

    if len(y_true):
        true_sum = np.bincount(y_true, weights=None, minlength=len(labels))

    # Retain only selected labels
    indices = np.searchsorted(sorted_labels, labels[:n_labels])
    tp_sum = tp_sum[indices]
    true_sum = true_sum[indices]

    recall = _prf_divide(tp_sum, true_sum, "recall", "true", None, "recall")
    recall[recall == 0] = correction

    return sp.stats.mstats.gmean(recall)


def calculate_hist_slow(scorer, y):
    # very general implementation, but very slow - can be easily vectorized if
    # one implements scorers as simple vector functions instead of what they
    # are in scikit (as done below)
    hist = []
    n = y.values.__len__() # all examples
    P = np.sum(y.values == 1) # positives
    N = n - P # negatives
    y_true = ([1] * P) + ([0] * N)

    t = np.concatenate((np.transpose(
        np.kron([np.arange(0, P + 1, 1), np.arange(P, -1, -1)],
                np.ones(N + 1))),
        np.tile(np.transpose([np.arange(0, N + 1, 1), np.arange(N, -1, -1)]),
                (P + 1, 1))),
        axis=1).astype(int)

    for i in range(t.shape[0]):
        cm = t[i, :]
        y_pred = ([1]*cm[0]) + ([0]*cm[1]) + ([1]*cm[2]) + ([0]*cm[3])
        hist.append(scorer._score_func(y_true, y_pred))

    return np.array(hist)


def accuracy(cm):
    return (cm[:, 0] + cm[:, 3]).astype(np.float) / (cm[:, 0] + cm[:, 1] + cm[:, 2] + cm[:, 3]).astype(np.float)


def balanced_accuracy(cm):
    return np.nan_to_num((cm[:, 0]).astype(np.float) / (cm[:, 0] + cm[:, 1]).astype(np.float) +
            (cm[:, 3]).astype(np.float) / (cm[:, 2] + cm[:, 3]).astype(np.float)) / 2.0


def recall(cm):
    return np.nan_to_num(cm[:, 0]).astype(np.float) / (cm[:, 0] + cm[:, 1]).astype(np.float)


def precision(cm):
    return np.nan_to_num((cm[:, 0]).astype(np.float) / (cm[:, 0] + cm[:, 2]).astype(np.float))


def f1_score(cm):
    return np.nan_to_num(2.0 * (precision(cm) * recall(cm)) / (precision(cm) + recall(cm)))


def g_mean(cm):
    return np.nan_to_num(
        np.sqrt(
            (cm[:, 0]).astype(np.float) / (cm[:, 0] + cm[:, 1]).astype(np.float) *
            (cm[:, 3]).astype(np.float) / (cm[:, 2] + cm[:, 3]).astype(np.float)
        )
    )


def kappa(cm):
    a = (cm[:, 0]).astype(np.float)
    b = (cm[:, 2]).astype(np.float)
    c = (cm[:, 1]).astype(np.float)
    d = (cm[:, 3]).astype(np.float)

    n = a + b + c + d
    N = b + d
    P = a + c
    P_ = a + b
    N_ = c + d

    return np.nan_to_num(
        (accuracy(cm) - 1 / n * (P * P_ / n + N * N_ / n)) /
        (1 - 1 / n * (P * P_ / n + N * N_ / n))
    )


def mcc(cm):
    a = (cm[:, 0]).astype(np.float)
    b = (cm[:, 2]).astype(np.float)
    c = (cm[:, 1]).astype(np.float)
    d = (cm[:, 3]).astype(np.float)
    return np.nan_to_num((a * d - b * c) / np.sqrt(((a + b) * (b + d) * (a + c) * (c + d))))


def calculate_hist(scorer, y):
    n = y.values.__len__() # all examples
    P = np.sum(y.values == 1) # positives
    N = n - P # negatives

    t = np.concatenate((np.transpose(
        np.kron([np.arange(0, P + 1, 1), np.arange(P, -1, -1)],
                np.ones(N + 1))),
        np.tile(np.transpose([np.arange(0, N + 1, 1), np.arange(N, -1, -1)]),
                (P + 1, 1))),
        axis=1).astype(int)

    return scorer(t)


def hnorm(values, hist):
    norm = []

    for value in values:
        norm.append(float(np.sum(hist <= value)) / float(hist.__len__()))

    return norm

