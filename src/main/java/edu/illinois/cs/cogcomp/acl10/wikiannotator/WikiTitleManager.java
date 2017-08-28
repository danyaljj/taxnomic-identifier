/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.wikiannotator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang
 * Jun 25, 2009
 */
public class WikiTitleManager {

	Set<String> setWikiTitles = new HashSet<String>();
	
	public static String WIKITITLE_FILE = "5gramTitleMapping.clean.txt";
		
	/**
	 * 
	 */
	public WikiTitleManager() {
		System.out.println("\nLoading wiki title data.");
		readIdf(WIKITITLE_FILE);		
		System.out.println("Done.");
	}

	public WikiTitleManager(String ngramFile) {
		System.out.println("\nLoading wiki title data.");
		readIdf(ngramFile);		
		System.out.println("Done.");
	}

	private void readIdf(String wikiTitleFile) {

		ArrayList<String> arrLines = IOManager.readLines(wikiTitleFile);

		for (String line : arrLines) {

			line = line.trim();

			if (line.length() == 0)
				continue;

			setWikiTitles.add(line);

		}

	}
	
	public boolean containTitle(String title) {
		if (setWikiTitles.contains(title))
			return true;
		return false;
	}
	
}
