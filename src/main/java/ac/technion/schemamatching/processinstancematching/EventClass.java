package ac.technion.schemamatching.processinstancematching;

import java.util.Collection;

import ac.technion.iem.ontobuilder.core.ontology.Attribute;
import ac.technion.iem.ontobuilder.core.ontology.Domain;
import ac.technion.iem.ontobuilder.core.ontology.DomainEntry;
import ac.technion.iem.ontobuilder.core.ontology.Term;

public class EventClass extends Term {

	protected double frequencyPerTrace;
	protected double avgPreceedingEvents;
	protected double avgSucceedingEvents;
	protected Collection<String> mandatoryPrereqs;
	protected Collection<String> optionalPrereqs;
	
	
	
	public EventClass(XESOverviewElement el) {
		super(el.getName(), el.getName());

		this.frequencyPerTrace = el.getFrequency();
		this.avgPreceedingEvents = el.getAveragePreceding();
		this.avgSucceedingEvents = el.getAverageSuceeding();

		for (String attrName : el.getAttributeNames()) {
			Attribute attribute = new Attribute(attrName, attrName);
			addAttribute(attribute);
			Domain domain = attribute.getDomain();
			for (Object attrValue : el.getAttributeValues(attrName)) {
				domain.addEntry(new DomainEntry(domain, attrValue));
			}
		}
	}


	public void setFrequency(double frequency) {
		this.frequencyPerTrace = frequency;
	}



	public double getAvgPreceedingEvents() {
		return avgPreceedingEvents;
	}



	public void setAvgPreceedingEvents(double avgPreceedingEvents) {
		this.avgPreceedingEvents = avgPreceedingEvents;
	}



	public double getAvgSucceedingEvents() {
		return avgSucceedingEvents;
	}



	public void setAvgSucceedingEvents(double avgSucceedingEvents) {
		this.avgSucceedingEvents = avgSucceedingEvents;
	}



	public double getFrequency() {
		return frequencyPerTrace;
	}
	
	
	
}
