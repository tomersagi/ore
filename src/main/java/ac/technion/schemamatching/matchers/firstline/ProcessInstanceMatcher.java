package ac.technion.schemamatching.matchers.firstline;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import ac.technion.iem.ontobuilder.core.ontology.Attribute;
import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.matchers.MatcherType;

public class ProcessInstanceMatcher implements FirstLineMatcher {

	@Override
	public String getName() {
		return "Process Instance Matcher";
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
			Term cTerm = candidateTerms.get(i); 
			
			for (int j = 0; j < targetTerms.size(); j++) {
				Term tTerm = targetTerms.get(j);
				double conf = computeConfidence(cTerm, tTerm);
				res.updateMatch(tTerm, cTerm, conf);
		
			}
		}
		
//		for (int i=0; i<Math.min(candidateTerms.size(), targetTerms.size());i++) {
//			res.updateMatch(targetTerms.get(i), candidateTerms.get(i), 1.0);
//		candidate.removeTerm(candidate.getTerm(0));//new addition by Roee
		return res;
	}

	@Override
	public String getConfig() {
		return "no configurable parameters";
	}

	@Override
	public MatcherType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDBid() {
		return 20;
	}
	
	private double computeConfidence(Term cTerm, Term tTerm) {
		Set<String> cAttributes = getAttributeNames(cTerm);
		Set<String> tAttributes = getAttributeNames(tTerm);
		Set<String> sharedAttributes = new HashSet<String>(cAttributes);
		sharedAttributes.retainAll(tAttributes);
		
		
		// for debugging
		Set<String> diffAttributes1 = new HashSet<String>(cAttributes);
		diffAttributes1.removeAll(tAttributes);
		Set<String> diffAttributes2 = new HashSet<String>(tAttributes);
		diffAttributes2.removeAll(cAttributes);
		
		// for now takes average of attribute sizes
		double conf = (double) sharedAttributes.size() / ((cAttributes.size() + tAttributes.size()) / 2);
		return conf;
	}

	private Set<String> getAttributeNames(Term t) {
		Set<String> res = new HashSet<String>();
		for (Attribute a : t.getAttributes()) {
			res.add(a.getName());
		}
		return res;
	}
	
}
