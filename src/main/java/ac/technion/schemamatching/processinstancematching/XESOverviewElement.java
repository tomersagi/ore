package ac.technion.schemamatching.processinstancematching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XESOverviewElement {

	String name;
	Map<String, List<Object>> attributeValues;
	int frequency;
	List<Integer> preceding;
	List<Integer> succeeding;
	int traces;
	List<Long> cycleTimes;
	Set<String> mandatoryPrereqs;
	Set<String> optionalPrereqs;
	
	public XESOverviewElement(String name) {
		super();
		this.name = name;
		attributeValues = new HashMap<String, List<Object>>();
		attributeValues.put("throughputtime", new ArrayList<Object>());
		preceding = new ArrayList<Integer>();
		succeeding = new ArrayList<Integer>();
		mandatoryPrereqs = new HashSet<String>();
		optionalPrereqs = new HashSet<String>();
	}
	
	public void  setTraces(int n) {
		this.traces = n;
	}
	
	public void incrementFrequency() {
		frequency++;
	}
	
	public void addPrecedingNumber(int n) {
		preceding.add(n);
	}
	
	public void addSucceedingNumber(int n) {
		succeeding.add(n);
	}
	
	public void addCycleTime(long ms) {
		attributeValues.get("throughputtime").add(ms);
	}
	
	public void addAttributeValue(String attr, Object val) {
		if (!attributeValues.containsKey(attr)) {
			attributeValues.put(attr, new ArrayList<Object>());
		}
		attributeValues.get(attr).add(val);
	}
	
	public Set<String> getAttributeNames() {
		return attributeValues.keySet();
	}
	
	public List<Object> getAttributeValues(String attr) {
		return attributeValues.get(attr);
	}
	
	public double getFrequency() {
		return (double) frequency / traces;
	}
	
	public double getAveragePreceding() {
		int sum = 0;
		for (int n : preceding) {
			sum = sum + n;
		}
		return (double) sum / frequency; 
	}
	
	public double getAverageSuceeding() {
		int sum = 0;
		for (int n : succeeding) {
			sum = sum + n;
		}
		return (double) sum / frequency; 
	}

	public String getName() {
		return name;
	}
	
	public void addPrereqs(Set<String> prereqs) {
		if (mandatoryPrereqs.isEmpty() && optionalPrereqs.isEmpty()) {
			mandatoryPrereqs.addAll(prereqs);
		} else {
			Set<String> diff = new HashSet<String>(mandatoryPrereqs);
			diff.removeAll(prereqs);
			for (String attrName : diff) {
				mandatoryPrereqs.remove(attrName);
				optionalPrereqs.add(attrName);
			}
		}
	}
	
	
}
