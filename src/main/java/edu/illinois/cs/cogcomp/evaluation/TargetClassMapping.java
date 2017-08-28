/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang
 * Feb 17, 2009
 */
public class TargetClassMapping {

	Map<String, ArrayList<String>> mapTargetClasses = new HashMap<String, ArrayList<String>>();
	
	/**
	 * 
	 */
	public TargetClassMapping(String mapFile) {
		
		parseMapFile(mapFile);
		
	}

	/**
	 * @param mapFile
	 */
	private void parseMapFile(String mapFile) {
		
		ArrayList<String> arrLines = IOManager.readLines(mapFile);
		
		for (String line : arrLines) {
			String parts[] = line.split("\t");

			if (parts.length != 2)
				continue;
			
			String targetClass = parts[0];
			
			String temp = parts[1];
			
			String items[] = temp.split(",+");
			
			ArrayList<String> arrItems = new ArrayList<String>();
			
			for (String item : items) {
				item = item.toLowerCase().trim();
				
				arrItems.add(item);
			}
	
			if (arrItems.size() > 0)
				mapTargetClasses.put(targetClass, arrItems);
		}
		
	}
	
	public boolean isValidTargetClass(String targetClass) {
		return mapTargetClasses.containsKey(targetClass);
	}
	
	public ArrayList<String> getTargetClassItems(String targetClass) {
		return mapTargetClasses.get(targetClass);
	}
	
	public String getFirstItem(String targetClass) {
		return mapTargetClasses.get(targetClass).get(0);
	}
}
