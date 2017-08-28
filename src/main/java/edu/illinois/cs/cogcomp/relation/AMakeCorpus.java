package edu.illinois.cs.cogcomp.relation;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 
 * @author dxquang
 * Feb 2, 2009
 */

public abstract class AMakeCorpus {
	
	public final static HashSet<String> INVALID_DOMAIN = new HashSet<String>();
	static {
		INVALID_DOMAIN.add("digg.com");
	}
	
	public final static int MAXIMUM_DISTANCE = 10;

	public static boolean isValidDomain(String url) {
		for (String domain : INVALID_DOMAIN) {
			if (url.indexOf(domain) != -1)
				return false;
		}
		return true;
	}

	abstract public ArrayList<Prototype> parsePrototype(String inputPrototypeFile);
	abstract public void makeCorpus(ArrayList<Prototype> arrPrototypes);
	
}
