package ac.technion.schemamatching.processinstancematching;

import java.util.Arrays;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

public class XESModelWriter {


	
	public static void writeXESModel(String name, XESOverviewModel model) {
		String folderpath = "/Users/han/vusvn/instancematching/data/";
		String filepath = folderpath + name + ".csv";
			try {
				CSVWriter writer = new CSVWriter(new FileWriter(filepath, false), ';');
				
				writer.writeNext(new String[]{"Event class", "Attribute", "Values"});
//				for (String eventClass : model.getEventClasses()) {
//					for (String attrName : model.getEventAttributes(eventClass)) {
//						List<Object> vals = model.getAttributeValues(eventClass, attrName);
//						String[] line = new String[vals.size() + 2];
//						line[0] = eventClass;
//						line[1] = attrName;
//						for (int i = 0; i < vals.size(); i++) {
//							line[i + 2] = String.valueOf(vals.get(i));
//						}
//						writer.writeNext(line);
//					}
//				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
