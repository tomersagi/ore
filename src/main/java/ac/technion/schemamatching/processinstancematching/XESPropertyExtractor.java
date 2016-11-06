package ac.technion.schemamatching.processinstancematching;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;

public class XESPropertyExtractor {

	
	
	public static Set<String> getEventClasses(XLog log) {
		Set<String> res = new HashSet<String>();
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				res.add(eClass);
			}
		}
		return res;
	}
	
	public static Set<String> getAttributeClasses(XLog log, String eventClass) {
		Set<String> res = new HashSet<String>();
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				if (eClass.equals(eventClass)) {
					for (XAttribute a : e.getAttributes().values()) {
						res.add(XConceptExtension.instance().extractName(a));
					}
				}
			}
		}
		return res;
	}
	
	public static Set<Object> getAttributeValues(XLog log, String eventClass, String attributeName) {
		//TODO: in non-static class this could be made much more computationally efficient with provenance maps. However, it is still O(n)
		Set<Object> res = new HashSet<Object>();
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				if (eClass.equals(eventClass)) {
					if (e.getAttributes().containsKey(attributeName)) {
						res.add(XUtils.getAttributeValue(e.getAttributes().get(attributeName)));
					}
				}
			}
		}
		return res;
	}
	
	public static String getAttributeType(XLog log, String eventClass, String attributeName) {
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				if (eClass.equals(eventClass)) {
					if (e.getAttributes().containsKey(attributeName)) {
						Class<?> c = XUtils.getAttributeClass(e.getAttributes().get(attributeName));
						return convertClassToOREDomainType(c);
					}
				}
			}
		}
		return "unknown domain type";
	}
	
	private static String convertClassToOREDomainType(Class<?> c) {
		//TODO: do we need to do this or can we use a sniffer? sniffers could be more specific, since java classes cannot express e.g. if string is an email etc
		switch (c.getName()) {
		case "String": return "ontology.domain.text";
		case "Boolean": return "ontology.domain.boolean";
		default: return "unknown domain type";
		}	
	}
	
//	ontology.domain.text",
//	   "ontology.domain.number",
//	   "ontology.domain.boolean",
//	   "ontology.domain.date",
//	   "ontology.domain.time",
//	   "ontology.domain.float",
//	   "ontology.domain.integer",
//	   "ontology.domain.pinteger",
//	   "ontology.domain.ninteger",
//	   "ontology.domain.choice",
//	   "ontology.domain.url",
//	   "ontology.domain.email"};
	
}
