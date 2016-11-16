package ac.technion.schemamatching.matchers.firstline;

import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.matchers.MatcherType;
import ac.technion.schemamatching.processinstancematching.EventClass;

public class ProcessInstanceMatcher implements FirstLineMatcher {

	public enum PIMMode {
		FREQUENCY(1), TIME(2), POSITION(3);
		
		int value;
		PIMMode(int value) {
			this.value = value;
		}
		
	}

	private PIMMode mode;
	
	public ProcessInstanceMatcher(PIMMode mode) {
		this.mode = mode;
	}
	
	@Override
	public String getName() {
		return "Process Instance Matcher: " + mode;
	}

	@Override
	public boolean hasBinary() {	
		return false;
	}

	@Override
	public MatchInformation match(Ontology candidate, Ontology target, boolean binary) {
		Vector<Term> candidateTerms = candidate.getTerms(false);
		Vector<Term> targetTerms = target.getTerms(false);
		MatchInformation res = new MatchInformation(candidate,target);

		for (int i = 0; i < candidateTerms.size(); i++) {
			EventClass cTerm = (EventClass) candidateTerms.get(i); 		
			for (int j = 0; j < targetTerms.size(); j++) {
				EventClass tTerm = (EventClass) targetTerms.get(j);
				double conf = 0;
				if (mode == PIMMode.FREQUENCY) {
					conf = frequencyConfidence(cTerm, tTerm);
//					max = Math.max(max, conf);
				}
				if (mode == PIMMode.POSITION) {
					conf = positionConfidence(cTerm, tTerm);
				}
				if (mode == PIMMode.TIME) {
					conf = timeDistributionConfidence(cTerm, tTerm);
				}
				if (Double.isNaN(conf)) {
					conf = 0;
				}
				res.updateMatch(tTerm, cTerm, conf);
			}
		}
		return res;
	}

	@Override
	public String getConfig() {
		return mode.toString();
	}

	@Override
	public MatcherType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDBid() {
		return 200 + mode.value;
	}
	
	private double positionConfidence(EventClass cTerm, EventClass tTerm) {
//		return 1 - Math.abs(cTerm.getRelativePosition() - tTerm.getRelativePosition());		
//		double min = Math.min(cTerm.getRelativePosition(), tTerm.getRelativePosition());
//		double max = Math.max(cTerm.getRelativePosition(), tTerm.getRelativePosition());
//		if (max == 0.0) {
//			return 1;
//		}
		return 1 - Math.abs(cTerm.getRelativePosition() - tTerm.getRelativePosition());
	}
	
	private double frequencyConfidence(EventClass cTerm, EventClass tTerm) {
		double min = Math.min(cTerm.getFrequency(), tTerm.getFrequency());
		double max = Math.max(cTerm.getFrequency(), tTerm.getFrequency());
		if (max == 0.0) {
			return 1;
		}
		return 1 - (max - min) / max;
	}
	
	private double timeDistributionConfidence(EventClass cTerm, EventClass tTerm) {
//		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
//	
//
		DescriptiveStatistics cStats = cTerm.getTimeDistribution();
		DescriptiveStatistics tStats = tTerm.getTimeDistribution();
//		
//		if (cStats.getN() < 2 || tStats.getN() < 2) {
//			double min = Math.min(cStats.getMean(), tStats.getMean());
//			double max = Math.max(cStats.getMean(), tStats.getMean());
//			
//			if (max == 0.0) {
//				return 1;
//			}
//			return 1 - (max - min) / max;
//		}
//		
//		double p = test.kolmogorovSmirnovTest(cStats.getValues(), tStats.getValues());
//		
//		return 1 - p;
		
		
		double min = Math.min(cStats.getMean(), tStats.getMean());
		double max = Math.max(cStats.getMean(), tStats.getMean());
		
		if (max == 0.0) {
			return 1;
		}
		return 1 - (max - min) / max;

	}
}
