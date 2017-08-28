/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dxquang
 * Feb 2, 2009
 */
public class PeopleManager {

	Set<String> setValidPeopleName = new HashSet<String>();
	/**
	 * 
	 */
	public PeopleManager(String peopleFileName) {
		ArrayList<String> arrLines = IOManager.readLines(peopleFileName);
		for (String line : arrLines) {
			line = line.toLowerCase();
			String tokens[] = line.split("\\s+");
			for (String token : tokens) {
				setValidPeopleName.add(token);
			}
		}
	}
	
	public boolean isValidPeopleName(String token) {
		return setValidPeopleName.contains(token);
	}
}
