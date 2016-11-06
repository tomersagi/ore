package ac.technion.schemamatching.processinstancematching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.ZipFile;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;

import ac.technion.iem.ontobuilder.core.ontology.Attribute;
import ac.technion.iem.ontobuilder.core.ontology.Domain;
import ac.technion.iem.ontobuilder.core.ontology.DomainEntry;
import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.core.ontology.OntologyClass;
import ac.technion.iem.ontobuilder.core.ontology.Term;
import ac.technion.iem.ontobuilder.io.imports.ImportException;
import ac.technion.iem.ontobuilder.io.imports.Importer;

public class XESImporter implements Importer {

	private Ontology ontology;
	
	
	@Override
	public Ontology importFile(File file) throws ImportException {
		return importFile(file, null);
	}

	@Override
	public Ontology importFile(File modelFile, File instanceFile) throws ImportException, UnsupportedOperationException {
		//TODO: since we only have .XES files, for now these two will be the same
		//TODO: if they also might not be the same, add some handler for that scenario
		
		File file = modelFile;
		ontology = new Ontology(file.getName().substring(0, file.getName().length()-4));
		XLog log = importXLog(file);
		
		createOntology(log);
		
		addInstanceInformation(log);
		
		return ontology;
		
	}

	private void createOntology(XLog log) {
		OntologyClass eventClassClass = new OntologyClass("eventClass");
		ontology.addClass(eventClassClass);
		
		for (String eventClass : XESPropertyExtractor.getEventClasses(log)) {
			Term term = new Term(eventClass, eventClass);
			ontology.addTerm(term);
			term.setSuperClass(eventClassClass);
			for (String attrName : XESPropertyExtractor.getAttributeClasses(log, eventClass)) {
				Attribute attribute = new Attribute(attrName, attrName);
				term.addAttribute(attribute);
				attribute.getDomain().setType(XESPropertyExtractor.getAttributeType(log, eventClass, attrName));
			}
		}
	}
	
	private void addInstanceInformation(XLog log) {
		//Create domains
		for (Term t : ontology.getTerms(false)) {
			for (Attribute attr : t.getAttributes()) {
				Domain d = attr.getDomain();
				for (Object o : XESPropertyExtractor.getAttributeValues(log, t.getName(), attr.getName())) {
					d.addEntry(new DomainEntry(d, o));
				}
			}
		}
	}
	
	
	private XLog importXLog(File file) throws ImportException {
		InputStream input;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ImportException("File " + file.getAbsolutePath() + " not found");
		}
		return importXLogFromStream(input, file.getAbsolutePath(), file.length(), new XFactoryBufferedImpl());
	}
	
	private XLog importXLogFromStream(InputStream input, String filename, long fileSizeInBytes,
			XFactory factory) {
		//this is modified from the ProMImport framework to work without pluginContexts
	
		//	System.out.println("Open file");
		XParser parser;
		if (filename.toLowerCase().endsWith(".xes") || filename.toLowerCase().endsWith(".xez")
				|| filename.toLowerCase().endsWith(".xes.gz")) {
			parser = new XesXmlParser(factory);
		} else {
			parser = new XMxmlParser(factory);
		}
		Collection<XLog> logs = null;
		Exception firstException = null;
		String errorMessage = "";
		try {
//			logs = parser.parse(new XContextUnmonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
			logs = parser.parse(input);
		} catch (Exception e) {
			logs = null;
			firstException = e;
			errorMessage = errorMessage + e;
		}
		if (logs == null || logs.isEmpty()) {
			// try any other parser
			for (XParser p : XParserRegistry.instance().getAvailable()) {
				if (p == parser) {
					continue;
				}
				try {
					logs = p.parse(input);
					if (logs.size() > 0) {
						break;
					}
				} catch (Exception e1) {
					// ignore and move on.
					logs = null;
					errorMessage = errorMessage + " [" + p.name() + ":" + e1 + "]";
				}
			}
		}

		// log sanity checks;
		// notify user if the log is awkward / does miss crucial information
		if (logs == null || logs.size() == 0) {
			return null;
		}

		XLog log = logs.iterator().next();
		if (XConceptExtension.instance().extractName(log) == null) {
			/*
			 * Log name not set. Create a default log name.
			 */
			XConceptExtension.instance().assignName(log, "Anonymous log imported from " + filename);
		}

//		if (log.isEmpty()) {
//			throw new Exception("No process instances contained in log!");
//		}
		return log;
	}
	
}
