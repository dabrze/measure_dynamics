package experiments;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;

import moa.evaluation.preview.LearningCurve;
import moa.options.ClassOption;
import moa.tasks.AbstractTask;
import moa.tasks.MainTask;

public class RatioChangeTests {

	public static void main(String[] args) throws Exception {
			String cliString = "EvaluatePrequential "
					+ "-l (trees.HoeffdingTree) "
					+ "-s (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 15 -b) "
					+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 4 -b) "
					+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 1 -b) "
					+ "-w 50 -p 10000 ) -w 50 -p 10000 ) -w 50 -p 30000)"
					+ "-e WindowImbalancedPerformanceEvaluator -i 50000 -f 500 -w 500";
			
			AbstractTask task = (AbstractTask) ClassOption.cliStringToObject(
        			cliString.toString(), MainTask.class, null);
			LearningCurve result = (LearningCurve)task.doTask();
			System.out.println(result);
			
			cliString = "EvaluatePrequential "
					+ "-l (trees.HoeffdingTree) "
					+ "-s (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 15 -b -z) "
					+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 4 -b -z) "
					+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 1 -b -z) "
					+ "-w 50 -p 10000 ) -w 50 -p 10000 ) -w 50 -p 30000)"
					+ "-e WindowImbalancedPerformanceEvaluator -i 50000 -f 500 -w 500";
			
			task = (AbstractTask) ClassOption.cliStringToObject(
        			cliString.toString(), MainTask.class, null);
			result = (LearningCurve)task.doTask();
			System.out.println(result);
			
//			+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 1 -b) "
//			+ "-d (ConceptDriftStream -s (generators.SEAGeneratorIm -i 1 -f 1 -r 0.25 -b) "
//			+ "-d (generators.SEAGeneratorIm -i 1 -f 1 -r 0.066666667 -b) "
	}

}
