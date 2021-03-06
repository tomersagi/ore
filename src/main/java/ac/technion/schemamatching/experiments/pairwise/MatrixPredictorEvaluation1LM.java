package ac.technion.schemamatching.experiments.pairwise;

import java.util.ArrayList;
import java.util.Properties;

import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.secondline.SLMList;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.K2Statistic;
import ac.technion.schemamatching.statistics.NBGolden;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.statistics.predictors.MatrixPredictors;
import ac.technion.schemamatching.testbed.ExperimentSchemaPair;

/**
 * Evaluates matrix predictors by returning the predictor value next to
 * precision, recall and L2 similarity measures 
 * @author Tomer Sagi
 *
 */
public class MatrixPredictorEvaluation1LM implements PairWiseExperiment {
	private ArrayList<FirstLineMatcher> flM;
	private boolean isMemory;

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#runExperiment(ac.technion.schemamatching.experiments.ExperimentSchemaPair)
	 */
	public ArrayList<Statistic> runExperiment(ExperimentSchemaPair esp) {
		// Using all 1st line matchers 
		ArrayList<Statistic> predictions = new ArrayList<>();
		ArrayList<Statistic> evaluations = new ArrayList<>();
		for (FirstLineMatcher m : flM)
		{
			//Match
			MatchInformation mi = esp.getSimilarityMatrix(m,isMemory);
			// Calculate predictors
			Statistic  p = new MatrixPredictors();
			String instanceDesc = esp.getID()+"_"+m.getName()+"_"+m.getConfig();
			p.init(instanceDesc, mi);
			predictions.add(p);
			//Calculate NBprecision, NBrecall
			K2Statistic  nb = new NBGolden();
			nb.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nb);
			//Precision Recall
			MatchInformation matchSelected = SLMList.OBSM.getSLM().match(mi);
			K2Statistic b = new BinaryGolden();
			b.init(instanceDesc, matchSelected,esp.getExact());
			evaluations.add(b);
		}
		predictions.addAll(evaluations);
		return predictions;
	}

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#init(java.util.Properties, java.util.ArrayList)
	 */
	public boolean init(OBExperimentRunner oer, Properties properties, ArrayList<FirstLineMatcher> flM, ArrayList<SecondLineMatcher> slM, boolean isMemory) {
		this.flM = flM;
		this.isMemory=isMemory;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#getDescription()
	 */
	public String getDescription() {
		return "Matrix Predictor Evaluation";
	}
	
	public ArrayList<Statistic> summaryStatistics() {
		//unused
		return null;
	}

}
