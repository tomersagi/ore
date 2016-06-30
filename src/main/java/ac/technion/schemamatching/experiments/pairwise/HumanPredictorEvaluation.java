package ac.technion.schemamatching.experiments.pairwise;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ac.technion.iem.ontobuilder.matching.match.Match;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.firstline.SimMatrixShell;
import ac.technion.schemamatching.matchers.secondline.SLMList;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.AttributeNBGolden;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.K2Statistic;
import ac.technion.schemamatching.statistics.MCC;
import ac.technion.schemamatching.statistics.MatchDistance;
import ac.technion.schemamatching.statistics.NBGolden;
import ac.technion.schemamatching.statistics.NBGoldenAtR;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.statistics.predictors.AttributePredictors;
import ac.technion.schemamatching.statistics.predictors.BehavioralPredictors;
import ac.technion.schemamatching.statistics.predictors.MCDAPredictor;
import ac.technion.schemamatching.statistics.predictors.MatrixPredictors;
import ac.technion.schemamatching.testbed.ExperimentSchemaPair;
import ac.technion.schemamatching.testbed.OREDataSetEnum;

/**
 * Evaluates matrix predictors by returning the predictor value next to
 * precision, recall and L2 similarity measures 
 * @author Tomer Sagi
 *
 */
public class HumanPredictorEvaluation implements PairWiseExperiment {
	private SimMatrixShell sms = new SimMatrixShell();
	private String pairPath = "";
	HashMap<Integer,ArrayList<File>> fileMap = new HashMap<Integer,ArrayList<File>>();
	Map<String,Double> slope=new HashMap<>();

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#runExperiment(ac.technion.schemamatching.experiments.ExperimentSchemaPair)
	 */
	public ArrayList<Statistic> runExperiment(ExperimentSchemaPair esp) {
		// Using all 1st line matchers 
		ArrayList<Statistic> predictions = new ArrayList<Statistic>();
		ArrayList<Statistic> evaluations = new ArrayList<Statistic>();
		
		for (File f : fileMap.get(esp.getID()))
		{
			//Match by importing results from human matcher csv files
			MatchInformation mi = null;
			if (!sms.setPath(f.getParent() ,f.getName()))
			{
				System.err.println("No file path found for pair:" + esp.getID());
				return evaluations;
			}
			System.out.println("Starting " + f.getName());
			sms.setImporter(OREDataSetEnum.Thalia.getMatchImp());
			mi = sms.match(esp.getCandidateOntology(), esp.getTargetOntology(), false);
			
			// Calculate predictors
			//TODO add to HumanCSV generator script + importer here the elapsed and diff information
			Statistic  p = new MatrixPredictors();
			String participant = f.getName().split("\\.")[0].split("_")[0];
			String instanceDesc = "" + esp.getID()+" , "+ participant;
			p.init(instanceDesc, mi);
			predictions.add(p);
			//predictions.add(mcd);
			// Calculate attribute structural predictors
			Statistic  pa = new AttributePredictors();
			pa.init(instanceDesc, mi);
			predictions.add(pa);
			K2Statistic MCDA2 = new MCDAPredictor();
			String instanceDesc_MCDA = instanceDesc +",MWBG";
			MCDA2.init(instanceDesc_MCDA, mi, SLMList.OBMWBG.getSLM().match(mi));
			predictions.add(MCDA2);
			
			//Calculate attribute behavioral predictors
			BehavioralPredictors bp = new BehavioralPredictors();
			bp.init(instanceDesc, mi);
			predictions.add(bp);
			
			//Create dcm modified matrices
			MatchInformation miPostDCM = mi.clone();
			double maxDiff = bp.getMaxDiff();
			double mySlope = (slope.containsKey(participant) ? slope.get(participant) : 0.0);
			if (mySlope != 0.0 && maxDiff>0.0) {
			for (Match m : mi.getCopyOfMatches()) {
				Properties props = m.getProps();
				if (props==null)
				{
					System.err.println("Behavioral predictor didn't find extended properties in match");
					continue;
				}
				//double elapsed = (Double)props.get("elapsed");
				double diff = (Double)props.get("diff");
				if (diff > maxDiff) continue;
				double increment = -(mySlope*diff/maxDiff);
				double eff = m.getEffectiveness()+(increment<.1 ? 0 :increment);
				miPostDCM.updateMatch(m.getTargetTerm(), m.getCandidateTerm(),(eff>1.0?1.0:eff) );
			} }
				
			//Run structural predictions on dcm modified predictors
			Statistic pap = new AttributePredictors();
			pap.init(instanceDesc+",dcm", miPostDCM);
			predictions.add(pap);
			
			Statistic pam = new MatrixPredictors();
			pam.init(instanceDesc +  ",dcm", miPostDCM);
			predictions.add(pam);
			
			//Calculate NBprecision, NBrecall
			K2Statistic  nba = new AttributeNBGolden();
			nba.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nba);
			
			K2Statistic b = new BinaryGolden();
			b.init(instanceDesc, mi,esp.getExact());
			evaluations.add(b);
			K2Statistic mcc = new MCC();
			mcc.init(instanceDesc, mi, esp.getExact());
			evaluations.add(mcc);
			//Calculate Non-Binary Precision and Recall
			K2Statistic nb = new NBGolden();
			nb.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nb);
			//Calculate Non-Binary Precision @ R
			K2Statistic nbr = new NBGoldenAtR();
			nbr.init(instanceDesc, mi,esp.getExact());
			evaluations.add(nbr);
			
			//Calculate MatchDisatance
			K2Statistic md = new MatchDistance();
			md.init(instanceDesc, mi,esp.getExact());
			evaluations.add(md);

		}
		predictions.addAll(evaluations);
		return predictions;
	}
	

	/*
	 * (non-Javadoc)
	 * @see ac.technion.schemamatching.experiments.MatchingExperiment#init(java.util.Properties, java.util.ArrayList)
	 */
	public boolean init(OBExperimentRunner oer, Properties properties, ArrayList<FirstLineMatcher> flM, ArrayList<SecondLineMatcher> slM, boolean isMemory) {
		pairPath = properties.getProperty("pairPath");
		//iterate over files in path, and load to HashMap<spid,ArrayList<File>>
		File[] files = new File(pairPath).listFiles();
		Integer schemaPair = new Integer(0);
		for (File file : files)
		{
			schemaPair = new Integer(Integer.parseInt(file.getName().split("\\.")[0].split("_")[1]));
			ArrayList<File> fileList = ( fileMap.containsKey(schemaPair) 
					? fileMap.get(schemaPair) : new ArrayList<File>());
			fileList.add(file);
			fileMap.put(schemaPair, fileList);
		}
		slope.put("0004", -.498);
		slope.put("0007", -.601);
		slope.put("0009", -.485);
		slope.put("0012", -.513);
		slope.put("0013", -.599);
		slope.put("0018", -.365);
		slope.put("0025", -.153);
		slope.put("1001", -.39);
		slope.put("1004", -.514);
		slope.put("1005", -.589);
		slope.put("2002", -.537);
		slope.put("2006", -.411);
		slope.put("2008", -.613);
		slope.put("2012", -.533);
		slope.put("2013", -.373);
		slope.put("3004", -.32);
		slope.put("3006", -.346);
		slope.put("3007", -.411);
		slope.put("3015", -.464);
		slope.put("3018", -.776);
		slope.put("4010", -.456);
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
