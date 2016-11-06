package ac.technion.schemamatching.processinstancematching;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XESPropertyExtractor {

	
	
	public Set<String> getEventClasses(XLog log) {
		Set<String> res = new HashSet<String>();
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				res.add(eClass);
			}
		}
		return res;
	}
	
	public Set<String> getAttributeClasses(XLog log, String eventClass) {
		Set<String> res = new HashSet<String>();
		for (XTrace t : log) {
			for (XEvent e : t) {
				String eClass = XConceptExtension.instance().extractName(e);
				if (eClass.equals(eventClass)) {
					for (XAttribute a : e.getAttributes().values()) {
						res.add(a.getKey());
					}
				}
			}
		}
		return res;
	}
}
