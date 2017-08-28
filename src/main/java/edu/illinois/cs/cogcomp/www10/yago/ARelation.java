/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.yago;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Oct 5, 2009
 */
public abstract class ARelation {

	private String[] dataFiles = null;
	private String relName = "";

	public Map<String, Set<String>> mapEntities = null;

	/**
	 * 
	 */
	public ARelation(String relName, String relFolder) {
		this.relName = relName;
		
		dataFiles = IOManager.listDirectory(relFolder);
		for (int i=0; i<dataFiles.length; i++) {
			dataFiles[i] = relFolder + "/" + dataFiles[i];
		}
		
		mapEntities = new HashMap<String, Set<String>>();
	}

	public void loadData() {
		
		System.out.println("*Loading data for " + relName);

		for (String file : dataFiles) {
			readDataFile(file);
		}
		
		System.out.println();
	}

	/**
	 * @param file
	 */
	protected void readDataFile(String file) {

		System.out.print("\tLoading file " + file + "... ");

		ArrayList<String> arrLines = IOManager.readLines(file);
		
		int size = arrLines.size();
		
		int bin = size / 10;

		int i = 0;
		
		for (String line : arrLines) {

			line = line.trim();
			String parts[] = line.split("\t");

			String e1 = parts[1].toLowerCase();
			String e2 = parts[2].toLowerCase();

			e1 = e1.replace('_', ' ');
			e2 = e2.replace('_', ' ');

			putIntoDictionary(e1, e2);
			
			i ++;
			
			if (bin > 0) {
				if (i % bin == 0)
					System.out.print(i + " ");
			}
		}
		
		System.out.println(" done!");
	}

	/**
	 * @param e1
	 * @param e2
	 */
	private void putIntoDictionary(String e1, String e2) {

		e1 = formatEntity(e1);
		e2 = formatEntity(e2);
		
		if (mapEntities.containsKey(e1)) {
			Set<String> setEntities = mapEntities.get(e1);
			setEntities.add(e2);
		} else {
			Set<String> setEntities = new HashSet<String>();
			setEntities.add(e2);
			mapEntities.put(e1, setEntities);
		}
		
	}

	private String formatEntity(String e) {
		
		if (e.startsWith("wikicategory")) {
			e = e.substring(e.indexOf(' ') + 1);
		} else if (e.startsWith("wordnet")) {
			e = e.substring(e.indexOf(' ') + 1, e.lastIndexOf(' '));
		}
		
		return e;
		
	}
	
	public Set<String> getEntities(String e) {
		
		return mapEntities.get(e);
		
	}

}
