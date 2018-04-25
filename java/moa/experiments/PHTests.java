package experiments;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;

import moa.evaluation.preview.LearningCurve;
import moa.options.ClassOption;
import moa.tasks.AbstractTask;
import moa.tasks.MainTask;

public class PHTests {
	
	private static double avg(ArrayList<Double> a) {
		double sum = 0;
		for (Double d : a) {
			sum += d;
		}
		return sum / a.size();
	}
	
	private static double sd(ArrayList<Double> a) {
		double sum = 0, sd = 0;
		for (Double d : a) {
			sum += d;
		}

        double mean = sum/a.size();

        for(Double d : a) {
            sd += Math.pow(d - mean, 2);
        }

        return Math.sqrt(sd/a.size());
	}

	public static void main(String[] args) throws Exception {
		String[] measures = {"ACCURACY", "BALANCED_ACCURACY", "KAPPA", "G_MEAN", "F1_SCORE", "PRECISION", "RECALL", "MCC"};
		// look out! class 0 is the negative class, class 1 is positive, so the ratio is given as N;P!
		String[] proportions = {"0.9325;0.0675", "0.8;0.2", "0.5;0.5", "0.2;0.8", "0.0675;0.9325"};
		int repetitions = 100;
		
		System.out.println("Measure,Proportion,Mean,Standard deviation");
		for (String proportion : proportions) {
			ArrayList<Double> a = new ArrayList<>(), b = new ArrayList<>(), k = new ArrayList<>(), g = new ArrayList<>(), f = new ArrayList<>(), p = new ArrayList<>(), r = new ArrayList<>(), m = new ArrayList<>();
			
			for(int i = 0; i< repetitions; i++) {
				String cliString = "EvaluatePrequential "
						+ "-l (trees.HoeffdingTree -g 20 -c 1.0E-4) "
						+ "-s (ImbalancedStream -s (generators.AgrawalGenerator -p 0.1 -i " + i + ") -c " + proportion + " -i " + i + ") "
						+ "-e WindowImbalancedPerformanceEvaluator -i 10000 -f 10000 -w 500";
				
				AbstractTask task = (AbstractTask) ClassOption.cliStringToObject(
            			cliString.toString(), MainTask.class, null);
				LearningCurve result = (LearningCurve)task.doTask();
				a.add(result.getMeasurement(0, 4));
				b.add(result.getMeasurement(0, 5));
				k.add(result.getMeasurement(0, 6));
				g.add(result.getMeasurement(0, 7));
				f.add(result.getMeasurement(0, 8));
				p.add(result.getMeasurement(0, 9));
				r.add(result.getMeasurement(0, 10));
				m.add(result.getMeasurement(0, 11));
			}
			
			System.out.println("ACCURACY," + proportion + "," + avg(a) + "," + sd(a));
			System.out.println("BALANCED_ACCURACY," + proportion + "," + avg(b) + "," + sd(b));
			System.out.println("KAPPA," + proportion + "," + avg(k) + "," + sd(k));
			System.out.println("G_MEAN," + proportion + "," + avg(g) + "," + sd(g));
			System.out.println("F1_SCORE," + proportion + "," + avg(f) + "," + sd(f));
			System.out.println("PRECISION," + proportion + "," + avg(p) + "," + sd(p));
			System.out.println("RECALL," + proportion + "," + avg(r) + "," + sd(r));
			System.out.println("MCC," + proportion + "," + avg(m) + "," + sd(m));
		}
		
		System.out.println("Measure,Proportion,False alarms");
		for (String measure : measures) {
			for (String proportion : proportions) {
				int sum = 0;
				
				for(int i = 0; i< repetitions; i++) {
					String cliString = "EvaluatePrequential "
							+ "-l (drift.DriftDetectionMethodClassifier "
							+ "-l (trees.HoeffdingTree -g 20 -c 1.0E-4) -d (PageHinkleyWindowDM -w 100 -m " + measure + ")) "
							+ "-s (ImbalancedStream -s (generators.AgrawalGenerator -p 0.1 -i " + i + ") -c " + proportion + " -i " + i + ") "
							+ "-e BasicConceptDriftPerformanceEvaluator -i 10000 -f 10000 -w 500";
					
					AbstractTask task = (AbstractTask) ClassOption.cliStringToObject(
                			cliString.toString(), MainTask.class, null);
					LearningCurve result = (LearningCurve)task.doTask();
					sum += result.getMeasurement(0, 13) > 0 ? 1 : 0;
				}
				
				System.out.println(measure + "," + proportion + "," + sum);
			}
		}

	}

}
