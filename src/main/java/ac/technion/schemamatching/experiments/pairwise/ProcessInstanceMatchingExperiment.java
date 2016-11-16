package ac.technion.schemamatching.experiments.pairwise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.ensembles.SimpleWeightedEnsemble;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.AttributeSetMatcher;
import ac.technion.schemamatching.matchers.firstline.AttributeSetMatcher.ASMMode;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.firstline.ProcessInstanceMatcher;
import ac.technion.schemamatching.matchers.firstline.ProcessInstanceMatcher.PIMMode;
import ac.technion.schemamatching.matchers.secondline.SLMList;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.ConfidenceDistributions;
import ac.technion.schemamatching.statistics.K2Statistic;
import ac.technion.schemamatching.statistics.MCC;
import ac.technion.schemamatching.statistics.MatchCompetitorDeviation;
import ac.technion.schemamatching.statistics.MatchCorrelationPrinter;
import ac.technion.schemamatching.statistics.MatchDistance;
import ac.technion.schemamatching.statistics.NBGolden;
import ac.technion.schemamatching.statistics.NBGoldenAtDynamicK;
import ac.technion.schemamatching.statistics.NBGoldenAtK;
import ac.technion.schemamatching.statistics.NBGoldenAtR;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.statistics.VerboseBinaryGolden;
import ac.technion.schemamatching.statistics.predictors.MatrixPredictors;
import ac.technion.schemamatching.testbed.ExperimentSchemaPair;

/**
 * @author Han van der Aa
 *
 */
class ProcessInstanceMatchingExperiment implements PairWiseExperiment {
	private ArrayList<FirstLineMatcher> flM;
	private Properties properties;
	private boolean isMemory;

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#runExperiment(ac.technion.schemamatching.experiments.ExperimentSchemaPair)
	 */
	public ArrayList<Statistic> runExperiment(ExperimentSchemaPair esp) {
		
		// Using 1st line matchers chosen as parameters
		ArrayList<Statistic> evaluations = new ArrayList<>();
		isMemory = true;

//		FirstLineMatcher term = new OBTermMatch();
//		mi = esp.getSimilarityMatrix(term,isMemory);
		
		List<FirstLineMatcher> flmsToUse = new ArrayList<FirstLineMatcher>();
		flmsToUse.add(new AttributeSetMatcher(ASMMode.TFIDF));
		flmsToUse.add(new AttributeSetMatcher(ASMMode.PREREQS));
		flmsToUse.add(new ProcessInstanceMatcher(PIMMode.FREQUENCY));
		flmsToUse.add(new ProcessInstanceMatcher(PIMMode.POSITION));
		flmsToUse.add(new ProcessInstanceMatcher(PIMMode.TIME));
		if (!esp.getTargetOntology().getName().endsWith("01")) {
			flmsToUse.add(new AttributeSetMatcher(ASMMode.DOMAIN));
		}
		
		Map<String, MatchInformation> flmResults = new HashMap<String, MatchInformation>();
		List<MatchInformation> flmResultsList = new ArrayList<MatchInformation>();
		
//		
		for (FirstLineMatcher m : flmsToUse) {
			
			System.out.println("matching using: " + m.getName());
			MatchInformation mi;
			
			mi = esp.getSimilarityMatrix(m,  isMemory);
			
			//Direct matching using the first line matcher allows to set parameters in the flm
//			mi = m.match(esp.getCandidateOntology(), esp.getTargetOntology(), false);

//			ConversionUtils.zeroWeightsByThresholdAndRemoveMatches(mi, 0.01);
//			ConversionUtils.limitToKMatches(mi, 10);
			
			//Calculate Non-Binary Precision and Recall
			K2Statistic nb = new NBGolden();
			String instanceDesc =  esp.getID() + "," + m.getName();
			nb.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nb);
			
			//Calculate Non-Binary Precision and Recall @ K
			K2Statistic nbk = new NBGoldenAtK();
			nbk.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nbk);
			
			//Calculate Non-Binary Precision and Recall @ KA
			K2Statistic nbka = new NBGoldenAtDynamicK();
			nbka.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nbka);			
			
			//Calculate Non-Binary Precision @ R
			K2Statistic nbr = new NBGoldenAtR();
			nbr.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nbr);
			
			//Calculate MatchDisatance
			K2Statistic md = new MatchDistance();
			md.init(instanceDesc, mi,esp.getExact());
			evaluations.add(md);
			
			//calculate ConfidenceDistributions
			K2Statistic cd = new ConfidenceDistributions();
			cd.init(instanceDesc, mi, esp.getExact());
			evaluations.add(cd);
			
			K2Statistic mcd = new MatchCompetitorDeviation();
			mcd.init(instanceDesc, mi, esp.getExact());
			evaluations.add(mcd);
			
//			K2Statistic vp = new VectorPrinterUsingExact();
//			vp.init(instanceDesc, mi, esp.getExact());
//			evaluations.add(vp);


			
			Statistic  p = new MatrixPredictors();
			p.init(instanceDesc, mi);
			evaluations.add(p);
			
			
			flmResults.put(m.getName(), mi);
			flmResultsList.add(mi);
			System.gc();
			
			List<SecondLineMatcher> singleSLMs = new ArrayList<SecondLineMatcher>();
			singleSLMs.add(SLMList.OBMWBG.getSLM());
			for (SecondLineMatcher s : singleSLMs) {
				//Second Line Match
				MatchInformation mi1 = s.match(mi);
				//calculate Precision and Recall
//				K2Statistic b2 = new BinaryGolden();
				String instanceDesc1 =  esp.getID() + "," + m.getName() + "," + s.getName()+ "," + s.getConfig();
//				b2.init(instanceDesc1, mi1,esp.getExact());
//				evaluations.add(b2);
				//Calculate verbose binary
				VerboseBinaryGolden b3 = new VerboseBinaryGolden();
				instanceDesc1 =  esp.getID() + "," + m.getName() + "," + s.getName()+ "," + s.getConfig();
				b3.init(instanceDesc1, mi1,esp.getExact());
				evaluations.add(b3);
				//Calculate MatchDisatance
				K2Statistic md2 = new MatchDistance();
				md2.init(instanceDesc1, mi1,esp.getExact());
				evaluations.add(md2);
				//Calculate MCC
				K2Statistic mcc = new MCC();
				mcc.init(instanceDesc1, mi1,esp.getExact());
				evaluations.add(mcc);
			}
		}
		
		System.out.println("Creating ensembled match");
		
		Map<String, Double> weights = new HashMap<String, Double>();
		
		MatchCorrelationPrinter mcp = new MatchCorrelationPrinter();
		mcp.init(String.valueOf(esp.getID()), flmsToUse, flmResultsList, esp.getExact());
		evaluations.add(mcp);
		
		//TODO: how to normalize weights?
		for (String name : flmResults.keySet()) {
			weights.put(name, 1.0 / flmResults.size());
		}
		
		SimpleWeightedEnsemble ensemble = new SimpleWeightedEnsemble();
		ensemble.init(flmResults, weights);
		MatchInformation weightedMI = ensemble.getWeightedMatch();
		
			//selecting second line matchers to use
			ArrayList<SecondLineMatcher> ensembleSLMs= new ArrayList<>();

			ensembleSLMs.add(SLMList.OBMWBG.getSLM());
			ensembleSLMs.add(SLMList.OBMaxDelta005.getSLM());
			ensembleSLMs.add(SLMList.OBMaxDelta01.getSLM());

			for (SecondLineMatcher s : ensembleSLMs)
			{
				//Second Line Match
				MatchInformation mi1 = s.match(weightedMI);
				//calculate Precision and Recall
				K2Statistic b2 = new BinaryGolden();
				String instanceDesc =  esp.getID() + "," + "weighted" + "," + s.getName()+ "," + s.getConfig();
				b2.init(instanceDesc, mi1,esp.getExact());
				evaluations.add(b2);
				//Calculate verbose binary
				VerboseBinaryGolden b3 = new VerboseBinaryGolden();
				instanceDesc =  esp.getID() + "," + "weighted" + "," + s.getName()+ "," + s.getConfig();
				b3.init(instanceDesc, mi1,esp.getExact(), weightedMI);
				evaluations.add(b3);
				//Calculate MatchDisatance
				K2Statistic md2 = new MatchDistance();
				md2.init(instanceDesc, mi1,esp.getExact());
				evaluations.add(md2);
				//Calculate MCC
				K2Statistic mcc = new MCC();
				mcc.init(instanceDesc, mi1,esp.getExact());
				evaluations.add(mcc);
			}
			

		return evaluations;
	}

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#init(java.util.Properties, java.util.ArrayList)
	 */
	public boolean init(OBExperimentRunner oer, Properties properties, ArrayList<FirstLineMatcher> flM, ArrayList<SecondLineMatcher> slM, boolean isMemory) {
		/*Using the supplied first line matcher list and second line matcher list allows run-time 
		changes to matchers used in the experiment*/
		this.flM = flM;
		this.isMemory=isMemory;
		//this.slM = slM;
		//using property files allows to modify experiment parameters at runtime
		this.properties = properties;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#getDescription()
	 */
	public String getDescription() {
		return "Process instance matching experiment";
	}
	
	public ArrayList<Statistic> summaryStatistics() {
		//unused
		return null;
	}

}
