package ac.technion.schemamatching.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;

/**
 * 
 * @author Han van der Aa
 *
 */
public class MatchCorrelationPrinter implements K2Statistic {
	private ArrayList<String[]> data;
	private String[] header;
	private int[] tps;
	private int[] fps;
	private int[] fns;
	
	public String[] getHeader() {
		return header;
	}

	public String getName() {
		return "Match correlation statistic";
	}

	public ArrayList<String[]> getData() {
		return data;
	}

	public boolean init(String instanceDescription, MatchInformation mi) {
		return false; //Golden statistics don't implement this method
	}

	public boolean init(String instanceDescription, MatchInformation mi, MatchInformation exactMatch) {
		return false;
	}
	
	
	public boolean init(String instanceDescription, List<FirstLineMatcher> matchers, List<MatchInformation> mis, MatchInformation exactMatch) {
		
		if (matchers.size() < 6) {
			return true;
		}
		
		data = new ArrayList<String[]>();
		header = new String[4 + mis.size()];
		header[0] = "pair";
		header[1] = "cterm";
		header[2] = "tterm";
		header[3] = "exactmatch";
		for (int i = 0; i < matchers.size(); i++) {
			header[4 + i] = matchers.get(i).getName();
		}
		Vector<Term> cTerms = mis.get(0).getCandidateOntology().getTerms(true);
		Vector<Term> tTerms = mis.get(0).getTargetOntology().getTerms(true);
		
		for (Term cTerm : cTerms) {
			for (Term tTerm : tTerms) {
				String[] row = new String[4 + mis.size()];
				row[0] = instanceDescription;
				row[1] = cTerm.getName();
				row[2] = tTerm.getName();
				row[3] = String.valueOf(exactMatch.getMatchConfidence(cTerm, tTerm));
				for (int i = 0; i < matchers.size(); i++) {
					MatchInformation mi = mis.get(i);
					double conf = mi.getMatchConfidence(cTerm, tTerm);
					row[4 + i] = String.valueOf(conf);
				}
				data.add(row);
			}
		}
		return true;
	}
	
	

}
