/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dxquang
 * Apr 16, 2009
 */
public class IdfManager {

	public Map<String, Double> mapIdf = new HashMap<String, Double>();
	
	/**
	 * 
	 */
	public IdfManager(String idfFile) {
		
		System.out.println("Loading idf data.");
		readIdf(idfFile);
		
	}

	private void readIdf(String idfFile) {

		ArrayList<String> arrLines = IOManager.readLines(idfFile);

		for (String line : arrLines) {

			line = line.trim();
			String items[] = line.split("\\t");

			if (items.length != 3)
				continue;

			mapIdf.put(items[1], Double.parseDouble(items[2]));

		}

	}
	
	public boolean containTerm(String term) {
		if (mapIdf.containsKey(term))
			return true;
		return false;
	}

	
	public double getIdf(String term) {
		if (containTerm(term))
			return mapIdf.get(term);
		return 0;
	}
}
