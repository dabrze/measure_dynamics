/*
 *    ImbalancedPerformanceEvaluator.java
 *    Copyright (C) 2016 Poznan University of Technology
 *    @author Dariusz Brzezinski (dbrzezinski@cs.put.poznan.pl)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.  
 */
package moa.evaluation;

import java.util.TreeSet;

import moa.core.Example;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.core.Utils;
import moa.options.AbstractOptionHandler;

import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstanceImpl;
import com.yahoo.labs.samoa.instances.Prediction;

import moa.tasks.TaskMonitor;

public class WindowImbalancedPerformanceEvaluator extends
		AbstractOptionHandler implements ClassificationPerformanceEvaluator {

	private static final long serialVersionUID = 1L;

	public IntOption widthOption = new IntOption("width", 'w',
			"Size of Window", 500);

	protected double totalObservedInstances = 0;
	private Estimator estimator;
	private SimpleEstimator weightMajorityClassifier;
	protected int numClasses;

    public class SimpleEstimator {
        protected double len;

        protected double sum;

        public void add(double value) {
            sum += value;
            len++;
        }

        public double estimation(){
            return sum/len;
        }
    }
	
	public class Estimator {

		public class Score implements Comparable<Score> {
			/**
			 * Predicted score of the example
			 */
			protected double value;

			/**
			 * Age of example - position in the window where the example was
			 * added
			 */
			protected int posWindow;

			/**
			 * True if example's true label is positive
			 */
			protected boolean isPositive;

			/**
			 * Constructor.
			 * 
			 * @param value
			 *            score value
			 * @param position
			 *            score position in window (defines its age)
			 * @param isPositive
			 *            true if the example's true label is positive
			 */
			public Score(double value, int position, boolean isPositive) {
				this.value = value;
				this.posWindow = position;
				this.isPositive = isPositive;
			}

			/**
			 * Sort descending based on score value.
			 */
			@Override
			public int compareTo(Score o) {
				if (o.value < this.value) {
					return -1;
				} else if (o.value > this.value){
					return 1;
				} else {
					if (!o.isPositive && this.isPositive) {
						return -1;
					} else if (o.isPositive && !this.isPositive){
						return 1;
					} else {
						if (o.posWindow > this.posWindow) {
							return -1;
						} else if (o.posWindow < this.posWindow){
							return 1;
						} else {
							return 0;
						}
					}
				}
			}
			
			@Override
			public boolean equals(Object o) {
				return (o instanceof Score) && ((Score)o).posWindow == this.posWindow;
			}
		}

		protected TreeSet<Score> sortedScores;
		
		protected TreeSet<Score> holdoutSortedScores;

		protected Score[] window;
		
		protected double[] predictions;

		protected int posWindow;

		protected int size;

		protected double numPos;

		protected double numNeg;
		
		protected double holdoutNumPos;

		protected double holdoutNumNeg;
		
		protected double correctPredictions;
		
		protected double correctPositivePredictions;
		
	    protected double[] columnKappa;

	    protected double[] rowKappa;
		
		protected double tp, tn, fp, fn;

		public Estimator(int sizeWindow) {
			this.sortedScores = new TreeSet<Score>();
			this.holdoutSortedScores = new TreeSet<Score>();
			this.size = sizeWindow;
			this.window = new Score[sizeWindow];
			this.predictions = new double[sizeWindow];
			
	        this.rowKappa = new double[numClasses];
	        this.columnKappa = new double[numClasses];
	        for (int i = 0; i < numClasses; i++) {
	            this.rowKappa[i] = 0.0;
	            this.columnKappa[i] = 0.0;
	        }
			
			this.posWindow = 0;
			this.numPos = 0;
			this.numNeg = 0;
			this.holdoutNumPos = 0;
			this.holdoutNumNeg = 0;
			this.correctPredictions = 0;
			this.correctPositivePredictions = 0;
			this.tp = 0; 
			this.tn = 0;
			this.fp = 0;
			this.fn = 0;
		}

		public void add(double score, boolean isPositive, boolean correctPrediction) {
            // // periodically update holdout evaluation
			if (size > 0 && posWindow % this.size == 0) {
				this.holdoutSortedScores = new TreeSet<Score>();
				
				for (Score s : this.sortedScores) {
					this.holdoutSortedScores.add(s);
				}
				
				this.holdoutNumPos = this.numPos;
				this.holdoutNumNeg = this.numNeg;
			}
			
			// // if the window is used and it's full			
			if (size > 0 && posWindow >= this.size) {
				// // remove the oldest example
				sortedScores.remove(window[posWindow % size]);
				correctPredictions -= predictions[posWindow % size];
				correctPositivePredictions -= window[posWindow % size].isPositive ? predictions[posWindow % size] : 0;
				
				if (window[posWindow % size].isPositive) {
					numPos--;
				} else {
					numNeg--;
				}
				
				if (predictions[posWindow % size] == 1) {
					if (window[posWindow % size].isPositive) {
						tp -= 1;
					} else {
						tn -= 1;
					}
				} else {
					if (window[posWindow % size].isPositive) {
						fn -= 1;
					} else {
						fp -= 1;
					}
				}
				
				int oldestExampleTrueClass = window[posWindow % size].isPositive ? 1 : 0;
	            int oldestExamplePredictedClass = predictions[posWindow % size] == 1.0 ? oldestExampleTrueClass : Math.abs(oldestExampleTrueClass - 1);  
	            
				this.rowKappa[oldestExamplePredictedClass] -= 1;
	            this.columnKappa[oldestExampleTrueClass] -= 1;
			}
			
			// // add new example
			Score newScore = new Score(score, posWindow, isPositive);
			sortedScores.add(newScore);
			correctPredictions += correctPrediction ? 1 : 0;
			correctPositivePredictions += correctPrediction && isPositive ? 1 : 0;
			if (correctPrediction) {
				if (isPositive) {
					tp += 1;
				} else {
					tn += 1;
				}
			} else {
				if (isPositive) {
					fn += 1;
				} else {
					fp += 1;
				}
			}
			
            int trueClass = isPositive ? 1 : 0;
            int predictedClass = correctPrediction ? trueClass : Math.abs(trueClass - 1);    
            this.rowKappa[predictedClass] += 1;
            this.columnKappa[trueClass] += 1;
			
			if (newScore.isPositive) {
				numPos++;
			} else {
				numNeg++;
			}

			if (size > 0) {
				window[posWindow % size] = newScore;
				predictions[posWindow % size] = correctPrediction ? 1 : 0;
			}
			
			//// posWindow needs to be always incremented to differentiate between examples in the red-black tree
			posWindow++;
		}

		public double getAUC() {
			double AUC = 0;
			double c = 0;
			double prevc = 0;
			double lastPosScore = Double.MAX_VALUE;
					
			if (numPos == 0 || numNeg == 0) {
				return 1;
			}
			
			for (Score s : sortedScores){
				if(s.isPositive) {			
					if (s.value != lastPosScore) {
						prevc = c;
						lastPosScore = s.value;
					}
					
					c += 1;
				} else {
					if (s.value == lastPosScore) {
						// tie
						AUC += ((double)(c + prevc))/2.0;
					} else {
						AUC += c;
					}
				}
			}
			
			return AUC / (numPos * numNeg);
		}
		
		public double getHoldoutAUC() {
			double AUC = 0;
			double c = 0;
			double prevc = 0;
			double lastPosScore = Double.MAX_VALUE;

			if (holdoutSortedScores.isEmpty()) {
				return 0;
			}
			
			if (holdoutNumPos == 0 || holdoutNumNeg == 0) {
				return 1;
			}

			for (Score s : holdoutSortedScores){
				if(s.isPositive) {
					if (s.value != lastPosScore) {
						prevc = c;
						lastPosScore = s.value;
					}
					
					c += 1;
				} else {
					if (s.value == lastPosScore) {
						// tie
						AUC += ((double)(c + prevc))/2.0;
					} else {
						AUC += c;
					}
				}
			}
			
			return AUC / (holdoutNumPos * holdoutNumNeg);
		}

		public double getScoredAUC() {
			double AOC = 0;
			double AUC = 0;
			double r = 0;
			double prevr = 0;
			double c = 0;
			double prevc = 0;
			double R_plus, R_minus;
			double lastPosScore = Double.MAX_VALUE;
			double lastNegScore = Double.MAX_VALUE;
			
			if (numPos == 0 || numNeg == 0) {
				return 1;
			}
			
			for (Score s : sortedScores){
				if(s.isPositive) {
					if (s.value != lastPosScore) {
						prevc = c;
						lastPosScore = s.value;
					}
					
					c += s.value;
					
					if (s.value == lastNegScore) {
						// tie
						AOC += ((double)(r + prevr))/2.0;
					} else {
						AOC += r;
					}
				} else {
					if (s.value != lastNegScore) {
						prevr = r;
						lastNegScore = s.value;
					}
					
					r += s.value;
					
					if (s.value == lastPosScore) {
						// tie
						AUC += ((double)(c + prevc))/2.0;
					} else {
						AUC += c;
					}
				}
			}
			
			R_minus = (numPos*r - AOC)/(numPos * numNeg);
			R_plus = (AUC)/(numPos * numNeg);		
			return R_plus - R_minus;
		}
		
		public double getRatio() {
			if(numNeg == 0) {
				return Double.MAX_VALUE;
			} else {
				return numPos/numNeg;
			}
		}
		
		public double getAccuracy() {
			double value = (tp + tn) / (tp + fp + fn + tn);
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getKappa() {
            double p0 = getAccuracy();
            double pc = 0.0;
            
            if (size > 0) {
	            for (int i = 0; i < numClasses; i++) {
	                pc += (this.rowKappa[i]/Math.min(size, totalObservedInstances)) * (this.columnKappa[i]/Math.min(size, totalObservedInstances));
	            }
            } else {
            	for (int i = 0; i < numClasses; i++) {
	                pc += (this.rowKappa[i]/totalObservedInstances) * (this.columnKappa[i]/totalObservedInstances);
	            }
            }
            double value =  (p0 - pc) / (1.0 - pc);
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
	    }
		
		public double getGMean() {
			double positiveAccuracy = tp / (tp + fn);
			double negativeAccuracy = tn / (tn + fp);
			
			double value =  Math.sqrt(positiveAccuracy * negativeAccuracy);
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getBalancedAccuracy() {
			double positiveAccuracy = tp / (tp + fn);
			double negativeAccuracy = tn / (tn + fp);
			double value =  (positiveAccuracy + negativeAccuracy) / 2.0;
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getRecall() {
			double value =  tp  / (tp + fn);
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getPrecision() {
			double value =  tp  / (tp + fp);
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getF1Score() {
			double value =  2 * (getPrecision() * getRecall())/(getPrecision() + getRecall());
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
		
		public double getMCC() {
			double value =  value = (tp*tn - fp*fn) / Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));
			
			if(Double.isNaN(value)) {
				value = 0;
			}
			
			return value;
		}
	}

	@Override
	public void reset() {
		reset(this.numClasses);
	}

	public void reset(int numClasses) {
		if (numClasses != 2) {
			throw new RuntimeException(
					"Too many classes ("
							+ numClasses
							+ "). AUC evaluation can be performed only for two-class problems!");
		}

		this.numClasses = numClasses;
		
		this.estimator = new Estimator(this.widthOption.getValue());
		this.weightMajorityClassifier = new SimpleEstimator();
		this.totalObservedInstances = 0;
	}

	@Override
	public void addResult(Example<Instance> exampleInstance, double[] classVotes) {
		InstanceImpl inst = (InstanceImpl)exampleInstance.getData();
		double weight = inst.weight();
		
		if (inst.classIsMissing() == false){
			int trueClass = (int) inst.classValue();
	
			if (weight > 0.0) {
				// // initialize evaluator
				if (totalObservedInstances == 0) {
					reset(inst.dataset().numClasses());
				}
				this.totalObservedInstances += 1;
				
				//// if classVotes has length == 1, then the negative (0) class got all the votes
				Double normalizedVote = 0.0;
				
				//// normalize and add score
				if(classVotes.length == 2) {
					normalizedVote = classVotes[1]/(classVotes[0] + classVotes[1]);
				}
				
				if(normalizedVote.isNaN()){
					normalizedVote = 0.0;
				}
				
				this.estimator.add(normalizedVote, trueClass == 1, Utils.maxIndex(classVotes) == trueClass);
				this.weightMajorityClassifier.add((this.estimator.getRatio() <= 1 ? 0 : 1) == trueClass ? weight: 0);
			}
		}
	}

	@Override
	public Measurement[] getPerformanceMeasurements() {
		return new Measurement[] {
				new Measurement("classified instances",
						this.totalObservedInstances),			
				new Measurement("Accuracy", this.estimator.getAccuracy()),
				new Measurement("Balanced accuracy", this.estimator.getBalancedAccuracy()),
				new Measurement("Kappa", this.estimator.getKappa()),
				new Measurement("G-Mean", this.estimator.getGMean()),
				new Measurement("F1-score", this.estimator.getF1Score()),
				new Measurement("Precision", this.estimator.getPrecision()),
				new Measurement("Recall", this.estimator.getRecall()),
				new Measurement("MCC", this.estimator.getMCC())};

	}

	@Override
	public void getDescription(StringBuilder sb, int indent) {
		Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
				sb, indent);
	}

	@Override
	public void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
	}

	public Estimator getEstimator() {
		return estimator;
	}

	@Override
	public void addResult(Example<Instance> arg0, Prediction arg1) {
		throw new RuntimeException("Designed for scoring classifiers");
	}
}
