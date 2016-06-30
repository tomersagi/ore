package ac.technion.schemamatching.experiments.pairwise;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.firstline.SimMatrixShell;
import ac.technion.schemamatching.matchers.secondline.BehavioralMatcher;
import ac.technion.schemamatching.matchers.secondline.OBMaxDelta;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.K2Statistic;
import ac.technion.schemamatching.statistics.MatchDistance;
import ac.technion.schemamatching.statistics.NBGolden;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.testbed.ExperimentSchemaPair;
import ac.technion.schemamatching.testbed.OREDataSetEnum;
/**
 * Compares performance of a weighted ensemble of Term-HM with Term-Behavioral2LM(HM)
 * Calcs precision, recall and L2 similarity measures 
 * @author Tomer Sagi
 *
 */
public class Behavioral2LMensemble implements PairWiseExperiment {

		private SimMatrixShell sms = new SimMatrixShell();
		private String pairPath = "";
		HashMap<Integer,ArrayList<File>> fileMap = new HashMap<Integer,ArrayList<File>>();
		Map<String,Double> slope=new HashMap<>();
		private ArrayList<FirstLineMatcher> flM;
		private Properties properties;

		/*
		 * (non-Javadoc)
		 * @see ac.technion.schemamatching.experiments.MatchingExperiment#runExperiment(ac.technion.schemamatching.experiments.ExperimentSchemaPair)
		 */
		public ArrayList<Statistic> runExperiment(ExperimentSchemaPair esp) { 
			ArrayList<Statistic> evaluations = new ArrayList<Statistic>();
			
			for (File f : fileMap.get(esp.getID()))
			{
				//Match by importing results from human matcher csv files
				MatchInformation hmi = null;
				if (!sms.setPath(f.getParent() ,f.getName()))
				{
					System.err.println("No file path found for pair:" + esp.getID());
					return evaluations;
				}
				System.out.println("Starting " + f.getName());
				sms.setImporter(OREDataSetEnum.Thalia.getMatchImp());
				hmi = sms.match(esp.getCandidateOntology(), esp.getTargetOntology(), false);
				//Calculate NBprecision, NBrecall prior to 2LM application
				String participant = f.getName().split("\\.")[0].split("_")[0];
				String iDesc = "" + esp.getID()+" , "+ participant;
				//if (!participant.equals("1012"))
				//	continue;
				String instanceDesc = iDesc  + ", Human Matcher";
				K2Statistic nb = new NBGolden();
				nb.init(instanceDesc, hmi,esp.getExact());
				evaluations.add(nb);
				
				K2Statistic md = new MatchDistance();
				md.init(instanceDesc, hmi,esp.getExact());
				evaluations.add(md);
				
				instanceDesc = iDesc  + ", Human Matcher - 2LM applied";
				SecondLineMatcher b2LM = new BehavioralMatcher();
				b2LM.init(properties);
				MatchInformation hmiB = b2LM.match(hmi);
				
				K2Statistic nb2 = new NBGolden();
				nb2.init(instanceDesc, hmiB,esp.getExact());
				evaluations.add(nb2);
				
				K2Statistic md2 = new MatchDistance();
				md2.init(instanceDesc, hmiB,esp.getExact());
				evaluations.add(md2);
				
				for (FirstLineMatcher flm : this.flM) {
					MatchInformation fmi =  flm.match(esp.getCandidateOntology(), esp.getTargetOntology(), false);
					
					instanceDesc = iDesc +"," + flm.getName() + ", - raw";
					K2Statistic nb3 = new NBGolden();
					nb3.init(instanceDesc, fmi,esp.getExact());
					evaluations.add(nb3);
					
					K2Statistic md3 = new MatchDistance();
					md3.init(instanceDesc, fmi,esp.getExact());
					evaluations.add(md3);
					
					SecondLineMatcher b2LM2 = new BehavioralMatcher(fmi);
					MatchInformation fmi_hmi = b2LM2.match(hmi);
					
					instanceDesc = iDesc  + "," + flm.getName() + " + hm";
					K2Statistic nb4 = new NBGolden();
					nb4.init(instanceDesc, fmi_hmi,esp.getExact());
					evaluations.add(nb4);
					
					K2Statistic md4 = new MatchDistance();
					md4.init(instanceDesc, fmi_hmi,esp.getExact());
					evaluations.add(md4);
					
					instanceDesc =iDesc  + "," + flm.getName() + " + hm + maxD";
					SecondLineMatcher slm = new OBMaxDelta(0.1);
					MatchInformation flm_hmi_2LM = slm.match(fmi_hmi);
					
					K2Statistic nb5 = new NBGolden();
					nb5.init(instanceDesc, flm_hmi_2LM,esp.getExact());
					evaluations.add(nb5);
					
					K2Statistic md5 = new MatchDistance();
					md5.init(instanceDesc, flm_hmi_2LM,esp.getExact());
					evaluations.add(md5);
					
					K2Statistic bin1 = new BinaryGolden();
					bin1.init(instanceDesc, flm_hmi_2LM,esp.getExact());
					evaluations.add(bin1);
					
					instanceDesc =iDesc  + "," + flm.getName() + "+ maxD";
					MatchInformation flm_2LM = slm.match(fmi);
					
					K2Statistic bin2 = new BinaryGolden();
					bin2.init(instanceDesc, flm_2LM,esp.getExact());
					evaluations.add(bin2);
					
					instanceDesc =iDesc  + ", hm + maxD";
					MatchInformation hm_2LM = slm.match(hmi);
					
					K2Statistic bin3 = new BinaryGolden();
					System.err.println("start hm+maxD");
					bin3.init(instanceDesc, hm_2LM,esp.getExact());
					evaluations.add(bin3);

					instanceDesc =iDesc  + ", hm + behavioral2LM + maxD";
					MatchInformation hmiB_2LM = slm.match(hmiB);
					
					K2Statistic bin4 = new BinaryGolden();
					System.err.println("start hm+behavioral2LM+maxD");
					bin4.init(instanceDesc, hmiB_2LM,esp.getExact());
					evaluations.add(bin4);
					
				}
			}
			return evaluations;
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
			this.flM = flM;
			//using property files allows to modify experiment parameters at runtime
			this.properties = properties;			
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