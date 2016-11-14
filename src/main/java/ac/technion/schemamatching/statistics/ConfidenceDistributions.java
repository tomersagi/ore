package ac.technion.schemamatching.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.matching.match.Match;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.processinstancematching.EventClass;

/**
 * 
 * @author Han van der Aa
 *
 */
public class ConfidenceDistributions implements K2Statistic {
	private ArrayList<String[]> data;
	private String[] header;
	private int[] tps;
	private int[] fps;
	private int[] fns;
	
	public String[] getHeader() {
		return header;
	}

	public String getName() {
		return "Confidence distribution statistic";
	}

	public ArrayList<String[]> getData() {
		return data;
	}

	public boolean init(String instanceDescription, MatchInformation mi) {
		return false; //Golden statistics don't implement this method
	}

	public boolean init(String instanceDescription, MatchInformation mi, MatchInformation exactMatch) {
		data = new ArrayList<String[]>();
		header = new String[]{"instance", "cat", "tps", "fps", "precision"};	
		
		Set<Match> matches = new HashSet<Match>();
		for (Match m : mi.getCopyOfMatches()) {
			if(m.getEffectiveness()>0.0) {
				matches.add(m);
			}
		}
		
		Set<Match> exactMatches = new HashSet<Match>();
		exactMatches.addAll(exactMatch.getCopyOfMatches());
		
		tps = new int[11];
		fps = new int[11];
//		fns = new int[11];
		
		for (Match m : matches) {
			int index = (int) (m.getEffectiveness() * 10);
			if (exactMatches.contains(m)) {
				tps[index]++;
				exactMatches.remove(m);
			} else {
				fps[index]++;
			}
		}
		
//		for (Match m : exactMatches) {
//			int index = (int) (m.getEffectiveness() * 10);
//			if (m.getEffectiveness() == 1.0) {
//				tps[index]++;
//			}
//			fns[index]++;
//		}
	

		for (int i = 0; i < 11; i++) {
			String cat = (double) i /10 + "-" + (double) (i + 1) / 10;
			if (i == 10) {
				cat = String.valueOf(1.0);
			}
			double prec = (double) tps[i] / (tps[i] + fps[i]);
			if (Double.isNaN(prec)) {
				prec = 0;
			}
//			double rec = (double) tps[i] / (tps[i] + fns[i]);
//			if (Double.isNaN(rec)) {
//				rec = 0;
//			}
			data.add(new String[]{instanceDescription, cat, String.valueOf(tps[i]), String.valueOf(fps[i]),
					String.valueOf(prec).replace(".", ",")}); 
		}
		
		return true;
	}
	
	

}
