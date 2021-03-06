package ac.technion.schemamatching.experiments.holistic;

import java.util.*;

import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.matching.match.MatchInformation;
import ac.technion.schemamatching.experiments.OBExperimentRunner;
import ac.technion.schemamatching.matchers.firstline.FirstLineMatcher;
import ac.technion.schemamatching.matchers.firstline.Stat;
import ac.technion.schemamatching.matchers.secondline.SecondLineMatcher;
import ac.technion.schemamatching.statistics.BinaryGolden;
import ac.technion.schemamatching.statistics.Statistic;
import ac.technion.schemamatching.testbed.ExperimentSchema;
import jdk.internal.vm.compiler.collections.Pair;


public class HolisticStat implements HolisticExperiment{

	@Override
	public List<Statistic> runExperiment(Set<ExperimentSchema> hashSet) {
		Stat s = new Stat();
		ArrayList<Statistic> res = new ArrayList<>();
		ArrayList<Ontology> ontologiesArray = new ArrayList<>();
		for(ExperimentSchema es : hashSet){
			ontologiesArray.add(es.getTargetOntology());
		}
		
		HashMap<Pair<Ontology,Ontology>,MatchInformation> miMap = s.match(ontologiesArray, true);
		
		for (Pair<Ontology,Ontology> key : miMap.keySet())
		{
			BinaryGolden bg = new BinaryGolden();
			MatchInformation mi = miMap.get(key);
			bg.init(key.getLeft().getId()+ "," + key.getRight().getId(), mi);
			res.add(bg);
		}	
		return res;
	}

	@Override
	public boolean init(OBExperimentRunner oer, Properties properties,
			ArrayList<FirstLineMatcher> flM, ArrayList<SecondLineMatcher> slM) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getDescription() {
	
		return "Statistical matching for more then 2 Ontologies";
	}

}