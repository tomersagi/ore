package ac.technion.schemamatching.processinstancematching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import ac.technion.iem.ontobuilder.core.ontology.Attribute;
import ac.technion.iem.ontobuilder.core.ontology.Domain;
import ac.technion.iem.ontobuilder.core.ontology.DomainEntry;
import ac.technion.iem.ontobuilder.core.ontology.Term;

public class EventClass extends Term {

	int frequency;
	int traces;
	List<Double> pos;
	double avgRelPos;
	Set<String> mandatoryPrereqs;
	Set<String> optionalPrereqs;
	Set<String> allPrereqs;
	DescriptiveStatistics timeDistrib;
	Map<String, Attribute> attributeMap;
	
	public EventClass(String name) {
		super(name);
		pos = new ArrayList<Double>();
		allPrereqs = new HashSet<String>();
		mandatoryPrereqs = new HashSet<String>();
		optionalPrereqs = new HashSet<String>();
		timeDistrib = new DescriptiveStatistics();
		attributeMap = new HashMap<String, Attribute>();
		avgRelPos = -1;
	}
	
	public void  setTraces(int n) {
		this.traces = n;
	}
	
	public void incrementFrequency() {
		frequency++;
	}
	
	public void addRelPos(double relPos) {
		pos.add(relPos);
	}
	
	public void addCycleTime(long ms) {
		timeDistrib.addValue(ms);
	}
	
	public DescriptiveStatistics getTimeDistribution() {
		
		return timeDistrib;
	}
	
	public void addAttributeValue(String attrName, Object val) {
		if (!attributeMap.containsKey(attrName)) {
			Attribute attribute = new Attribute(attrName, attrName);
			attributeMap.put(attrName, attribute);
			addAttribute(attribute);
		}
		Domain d = attributeMap.get(attrName).getDomain();
		d.addEntry(new DomainEntry(d, val));
	}
	
	public double getFrequency() {
		return (double) frequency / traces;
	}
	
	public double getRelativePosition() {
		if (avgRelPos > 0) {
			return avgRelPos;
		}
		double sum = 0;
		for (double d : pos) {
			sum = sum + d;
		}
		avgRelPos = sum / pos.size(); 
		return avgRelPos;
	}

	public String getName() {
		return name;
	}
	
	public void addPrereqs(Set<String> prereqs) {
		allPrereqs.addAll(prereqs);
//		if (mandatoryPrereqs.isEmpty() && optionalPrereqs.isEmpty()) {
//			mandatoryPrereqs.addAll(prereqs);
//		} else {
//			Set<String> diff = new HashSet<String>(mandatoryPrereqs);
//			diff.removeAll(prereqs);
//			for (String attrName : diff) {
//				mandatoryPrereqs.remove(attrName);
//				optionalPrereqs.add(attrName);
//			}
//		}
	}
	
	public Set<String> getPrereqs() {
		return allPrereqs;
	}
	
	
}
