package ac.technion.schemamatching.processinstancematching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipFile;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

import ac.technion.iem.ontobuilder.core.ontology.Ontology;
import ac.technion.iem.ontobuilder.io.imports.ImportException;
import ac.technion.iem.ontobuilder.io.imports.Importer;

public class XESImporter implements Importer {


	
	
	@Override
	public Ontology importFile(File file) throws ImportException {
		Ontology xesOntology = new Ontology(file.getName());
		
		XLog log = importXLog(file);
		
		
		
		return xesOntology;
	}

	@Override
	public Ontology importFile(File arg0, File arg1) throws ImportException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		//TODO: what should we do with this?
		return null;
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

//		/*
//		 * Set the log name as the name of the provided object.
//		 */
//		if (context != null) {
//			context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(log));
//		}

		return log;
	}
	
}
