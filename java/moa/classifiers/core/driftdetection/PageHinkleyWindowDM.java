package moa.classifiers.core.driftdetection;

import moa.core.Example;
import moa.core.ObjectRepository;
import moa.evaluation.WindowImbalancedPerformanceEvaluator;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.StringOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.tasks.TaskMonitor;
import moa.tasks.Plot.Terminal;

public class PageHinkleyWindowDM extends AbstractChangeDetector {

	private static final long serialVersionUID = -3518369648142099719L;

	public enum Measure {
		ACCURACY, BALANCED_ACCURACY, G_MEAN, KAPPA, F1_SCORE, PRECISION, RECALL, MCC;

		private static String[] descriptions = new String[] { "Accuracy", "BalancedAccuracy", "G-mean", "Kappa",
				"F1-score", "Precision", "Recall", "MCC" };

		/**
		 * Gets an array of string descriptions - one for each enum value.
		 * 
		 * @return a description for each enum value.
		 */
		public static String[] getDescriptions() {
			return descriptions;
		}

		/**
		 * Get string values for the enum values.
		 * 
		 * @return a set of string values for the enum values.
		 */
		public static String[] getStringValues() {
			int i = 0;
			String[] result = new String[values().length];

			for (Measure value : values()) {
				result[i++] = value.name();
			}

			return result;
		}
	}

	public IntOption minNumInstancesOption = new IntOption("minNumInstances", 'n',
			"The minimum number of instances before permitting detecting change.", 30, 0, Integer.MAX_VALUE);

	public FloatOption deltaOption = new FloatOption("delta", 'd', "Delta parameter of the Page Hinkley Test", 0.005,
			0.0, 1.0);

	public FloatOption lambdaOption = new FloatOption("lambda", 'l', "Lambda parameter of the Page Hinkley Test", 50,
			0.0, Float.MAX_VALUE);

	public IntOption windowOption = new IntOption("window", 'w', "Size of window", 1000);

	public MultiChoiceOption measureOption = new MultiChoiceOption("measure", 'm', "Measure to monitor",
			Measure.getStringValues(), Measure.getDescriptions(), 0);

	private WindowImbalancedPerformanceEvaluator evaluator;

	private int t;

	private double m_t;

	private double sr;

	private double delta;

	private double lambda;

	private double M_t;

	private int windowSize;

	private Measure measure;

	public PageHinkleyWindowDM() {
		resetLearning();
		this.isChangeDetected = true;
	}

	@Override
	public void resetLearning() {
		t = 1;
		sr = 0.0;
		M_t = 1;
		m_t = 0.0;
		delta = this.deltaOption.getValue();
		lambda = this.lambdaOption.getValue();

		windowSize = this.windowOption.getValue();
		evaluator = new WindowImbalancedPerformanceEvaluator();
		evaluator.widthOption.setValue(windowSize);

		measure = Measure.valueOf(measureOption.getChosenLabel());
	}

	@Override
	public void input(double x) {
		throw new RuntimeException("This detector uses a different testing scheme!");
	}

	public void input(Example<Instance> inst, double[] classVotes) {
		detectDrift(inst, classVotes);
	}

	private void detectDrift(Example<Instance> inst, double[] classVotes) {
		if (this.isChangeDetected == true) {
			resetLearning();
		}

		evaluator.addResult(inst, classVotes);

		double value = 0;

		switch (measure) {
			case ACCURACY:
				value = evaluator.getEstimator().getAccuracy();
				break;
			case BALANCED_ACCURACY:
				value = evaluator.getEstimator().getBalancedAccuracy();
				break;
			case G_MEAN:
				value = evaluator.getEstimator().getGMean();
				break;
			case KAPPA:
				value = evaluator.getEstimator().getKappa();
				break;
			case F1_SCORE:
				value = evaluator.getEstimator().getF1Score();
				break;
			case PRECISION:
				value = evaluator.getEstimator().getPrecision();
				break;
			case RECALL:
				value = evaluator.getEstimator().getRecall();
				break;
			case MCC:
				value = evaluator.getEstimator().getMCC();
				break;
		}
		
		if(Double.isNaN(value)) {
			value = 0;
		}
		
		value = 1.0-value; // need a loss function, not gain

		sr = sr + value;
		m_t = m_t + value - sr / t - delta;
		M_t = Math.min(M_t, m_t);

		t++;

		this.estimation = sr / t;
		this.isChangeDetected = false;
		this.isWarningZone = false;
		this.delay = 0;

		if (t < this.minNumInstancesOption.getValue()) {
			return;
		}

		if (m_t - M_t >= this.lambda) {
			this.isChangeDetected = true;
		}
	}

	@Override
	public void getDescription(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
		// TODO Auto-generated method stub
	}
}