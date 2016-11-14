/**
 * 
 */
package ac.technion.schemamatching.matchers.firstline;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ac.technion.iem.ontobuilder.core.ontology.Attribute;
import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.matching.algorithms.line1.domain.DomainAlgorithm;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.matchers.MatcherType;
import ac.technion.schemamatching.processinstancematching.EventClass;

/**
 * @author Han van der Aa
 * 
 * This matcher compares the similarity of the sets of attributes of terms
 * 
 */
public class AttributeSetMatcher implements FirstLineMatcher {

	public enum ASMMode {
		BASIC(1), TFIDF(2), DOMAIN(3), PREREQS(4);
		
		int value;
		ASMMode(int value) {
			this.value = value;
		}
		
	}
	
	private Map<String, Integer> attributeDictionary;
	private int documentCount;
	private Map<String, Double> idfMap;
	ASMMode mode;
	
	
	public AttributeSetMatcher(ASMMode mode) {
		this.mode = mode;
	}
	

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#getName()
	 */
	@Override
	public String getName() {
		return "Attribute set matcher: " + getConfig();
	}
	
	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#getConfig()
	 */
	@Override
	public String getConfig() {
		return mode.toString();
	}

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#getType()
	 */
	@Override
	public MatcherType getType() {
		return MatcherType.STRUCTURAL_SIBLING;
	}

	
	

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#hasBinary()
	 */
	@Override
	public boolean hasBinary() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#match(ac.technion.iem.ontobuilder.core.ontology.Ontology, ac.technion.iem.ontobuilder.core.ontology.Ontology, boolean)
	 */
	@Override
	public MatchInformation match(Ontology candidate, Ontology target,
			boolean binary) {
		
		if (mode != ASMMode.DOMAIN) {
			populateDictionary(candidate, target);
		}
		
		Vector<Term> candidateTerms = candidate.getTerms(false);
		Vector<Term> targetTerms = target.getTerms(false);
		MatchInformation mi = new MatchInformation(candidate,target);
		
		for (int i = 0; i < candidateTerms.size(); i++) {
			Term cTerm = candidateTerms.get(i); 
			
			for (int j = 0; j < targetTerms.size(); j++) {
				Term tTerm = targetTerms.get(j);
				double conf = computeConfidence(cTerm, tTerm);
				mi.updateMatch(tTerm, cTerm, conf);
		
			}
		}
		return mi;
	}




	/* (non-Javadoc)
	 * @see ac.technion.schemamatching.matchers.firstline.FirstLineMatcher#getDBid()
	 */
	@Override
	public int getDBid() {
		return 250 + mode.value ;
	}
	
	private double computeConfidence(Term cTerm, Term tTerm) {
		double conf = 0;
		if (mode == ASMMode.TFIDF || mode == ASMMode.BASIC) {
			conf = cosineSimilarity(getAttributeNames(cTerm), getAttributeNames(cTerm));
		}
		
		if (mode == ASMMode.PREREQS) {
			conf = cosineSimilarity(
					((EventClass) cTerm).getPrereqs(),
					((EventClass) tTerm).getPrereqs());
		}
		if (mode == ASMMode.DOMAIN) {
			conf = attributesDomainSimilarity(cTerm, tTerm);
		}
		if (Double.isNaN(conf)) {
			return 0;
		}
		return conf;
	}

	private Set<String> getAttributeNames(Term t) {
		Set<String> res = new HashSet<String>();
		for (Attribute a : t.getAttributes()) {
			res.add(a.getName());
		}
		return res;
	}
	
	
	private double cosineSimilarity(Set<String> attributes1, Set<String> attributes2) {
		if (attributes1.equals(attributes2)) {
 			return 1.0;
		}
		double num = 0.0;
		double denomA = 0.0;
		double denomB = 0.0;
		for (String attr : attributes1) {
			double idf = getIDF(attr);
			double tfidf1 = 1 * idf;
			double tfidf2 = 0;
			if (attributes2.contains(attr)) {
					tfidf2 = 1 * idf;
			}
			denomA += tfidf1 * tfidf1;
			num +=  tfidf1 * tfidf2;
		}
		for (String attr : attributes2) {
			double idf = getIDF(attr);
			double tfidf2 = 1 * idf;
			denomB += tfidf2 * tfidf2;
		}
		double cos = num / (Math.sqrt(denomA) * Math.sqrt(denomB)); 
		return cos;
	}
	
	private void populateDictionary(Ontology candidate, Ontology target) {
		attributeDictionary = new HashMap<String, Integer>();
		idfMap = new HashMap<String, Double>();
		for (Term t : candidate.getTerms(true)) {
			Set<String> attributes;
			if (mode == ASMMode.TFIDF) {
				attributes = getAttributeNames(t);
			} else {
				attributes = ((EventClass) t).getPrereqs();
			}
			for (String attr: attributes) {
				int c = 0;
				if (attributeDictionary.containsKey(attr)) {
					c = attributeDictionary.get(attr);
				}
				attributeDictionary.put(attr, c + 1);
				documentCount++;
			}
		}
		for (Term t : target.getTerms(true)) {
			Set<String> attributes;
			if (mode == ASMMode.TFIDF) {
				attributes = getAttributeNames(t);
			} else {
				attributes = ((EventClass) t).getPrereqs();
			}
			for (String attr: attributes) {
				int c = 0;
				if (attributeDictionary.containsKey(attr)) {
					c = attributeDictionary.get(attr);
				}
				attributeDictionary.put(attr, c + 1);
				documentCount++;
			}
		}
	}
	
	private double getIDF(String attributeName) {
		if (mode == ASMMode.BASIC) {
			return 1;
		}
		if (idfMap.containsKey(attributeName)) {
			return idfMap.get(attributeName);
		}
		int tf = attributeDictionary.get(attributeName);
		double score = Math.log10((documentCount) * 1.0 / tf);
		idfMap.put(attributeName,  score);
		return score;
	}
	
	private double attributesDomainSimilarity(Term cTerm, Term tTerm) {
		DomainAlgorithm domainAlgorithm = new DomainAlgorithm();
		Set<String> cAttributes = getAttributeNames(cTerm);
		Set<String> tAttributes = getAttributeNames(tTerm);
		
		double sum = 0;
		for (Attribute attribute : cTerm.getAttributes()) {
			if (tAttributes.contains(attribute.getName())) {
				for (Attribute attribute2 : tTerm.getAttributes()) {
					if (attribute2.getName().equals(attribute.getName())) {
						double dist = domainAlgorithm.getDistance(attribute.getDomain(), attribute2.getDomain());
						sum = sum + dist;
					}
				}
			}
		}
		double conf = sum / (Math.sqrt(cAttributes.size()) * Math.sqrt(tAttributes.size()));
		return conf;
	}
	


}
