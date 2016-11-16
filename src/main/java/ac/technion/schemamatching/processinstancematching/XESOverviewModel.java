package ac.technion.schemamatching.processinstancematching;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;

import ac.technion.iem.ontobuilder.core.ontology.Ontology;

public class XESOverviewModel {

	XLog log;
	private Map<String, Integer> attributeDictionary;
	
	private HashMap<String, EventClass> eventClassMap;
	Set<String> currentPrereqs;

	public XESOverviewModel() {
		eventClassMap = new HashMap<String, EventClass>();
	}
	
	public void populateModel(XLog log, List<String> hiddenAttributes) {
		this.log = log;
		for (XTrace trace : log) {
			currentPrereqs = new HashSet<String>();
			for (int i = 0; i < trace.size(); i++) {
				addEvent(trace, i, hiddenAttributes);
			}
		}
	}
	
	public void addEvent(XTrace trace, int pos, List<String> hiddenAttributes) {
		XEvent event = trace.get(pos);
		String eventClass = XConceptExtension.instance().extractName(event);
		EventClass el;
		if (!eventClassMap.containsKey(eventClass)) {
			el = new EventClass(eventClass);
			el.setId(eventClass.hashCode());
			el.setTraces(log.size());
			eventClassMap.put(eventClass, el);
		} else {
			el = eventClassMap.get(eventClass);
		}
		el.incrementFrequency();
		el.addRelPos((double) (pos + 1) / trace.size()); 
		el.addPrereqs(currentPrereqs);
		
		if (pos > 0) {
			el.addCycleTime(calcThroughputTime(trace.get(pos - 1), event));
		}
		
		for (XAttribute attr : event.getAttributes().values()) {
			String attrName = attr.getKey();
//			if (!hiddenAttributes.contains(attrName) && !attrName.startsWith("(case)")) {
			if (!hiddenAttributes.contains(attrName)) {
				Object val = XUtils.getAttributeValue(attr);
				el.addAttributeValue(attrName, val);
				currentPrereqs.add(attrName);
			}
		}
	}
	
	private long calcThroughputTime(XEvent pred, XEvent current) {
		return XUtils.getTimestamp(current).getTime() - XUtils.getTimestamp(pred).getTime();
		
	}


	public void addAttributeValue(String eventClass, String attribute, Object value) {
		eventClassMap.get(eventClass).addAttributeValue(attribute, value);
	}
	
	public Collection<EventClass> getEventClasses() {
		return eventClassMap.values();
	}
	
	
	
}
