/**
 * 
 */
package ac.technion.schemamatching.experiments.pairwise;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.firstline.OBTermMatch;
import ac.technion.schemamatching.matchers.secondline.OBMaxDelta;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.K2Statistic;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.testbed.ExperimentSchemaPair;

/**
 * @author Tomer Sagi
 *
 */
public class NBTuningTermTest implements PairWiseExperiment {

	OBTermMatch termMatcher;
	OBMaxDelta maxD;
	private ArrayList<SecondLineMatcher> slm;
	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.pairwise.PairWiseExperiment#runExperiment(ac.technion.schemamatching.testbed.ExperimentSchemaPair)
	 */
	@Override
	public List<Statistic> runExperiment(ExperimentSchemaPair esp) {
		
		MatchInformation mi = termMatcher.match(esp.getCandidateOntology(), esp.getTargetOntology(), false);
		MatchInformation res = maxD.match(mi);
		K2Statistic bg = new BinaryGolden();
		bg.init("" + esp.getID() +  ",MaxD" + maxD.getDelta()+ ","+ termMatcher.getConfig(), res, esp.getExact());
		List<Statistic> stats = new ArrayList<Statistic>();
		stats.add(bg);
		for (SecondLineMatcher s : slm) {
			MatchInformation smi = s.match(mi);
			K2Statistic bgs = new BinaryGolden();
			bgs.init("" + esp.getID() + "," + s.getName()+ ","+ termMatcher.getConfig(), smi, esp.getExact());
			stats.add(bgs);
		}
		return stats;
	}

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.pairwise.PairWiseExperiment#init(ac.technion.schemamatching.experiments.OBExperimentRunner, java.util.Properties, java.util.ArrayList, java.util.ArrayList)
	 */
	@Override
	public boolean init(OBExperimentRunner oer, Properties properties,
			ArrayList<FirstLineMatcher> flM, ArrayList<SecondLineMatcher> slM) {
		double nGramW = Double.parseDouble(properties.getProperty("nGramWeight"));
		double jaroW = Double.parseDouble(properties.getProperty("jaroWinkler"));
		double wordName = Double.parseDouble(properties.getProperty("wordName"));
		double stringName = 1-wordName;
		double wordLabel = Double.parseDouble(properties.getProperty("wordLabel"));
		double stringLabel = 1-wordLabel;
		boolean useSoundex = (Integer.parseInt(properties.getProperty("useSoundex"))==1);
		short aggStrategy = (short)Integer.parseInt(properties.getProperty("aggStrategy"));
		short nGram = (short)Integer.parseInt(properties.getProperty("nGram"));
		this.termMatcher = new OBTermMatch(nGramW, jaroW, wordName, stringName, stringLabel, wordLabel, useSoundex, aggStrategy, nGram);
		this.slm = slM;
		double delta = Double.parseDouble(properties.getProperty("delta"));
		this.maxD = new OBMaxDelta(delta );
		return true;
	}

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.pairwise.PairWiseExperiment#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Run term using supplied parameters and MaxDelta";
		
	}

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.pairwise.PairWiseExperiment#summaryStatistics()
	 */
	@Override
	public List<Statistic> summaryStatistics() {
		return null;
	}

}
