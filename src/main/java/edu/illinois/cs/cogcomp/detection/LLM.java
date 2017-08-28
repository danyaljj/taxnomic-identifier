/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;

import net.didion.jwnl.data.POS;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Feb 16, 2009
 */
public class LLM {

	public static WordNetManager wnMyManager = new WordNetManager("file_properties.xml");

	public boolean headMatch(String u, String v, boolean useMorphU, boolean useMorphV) throws Exception {
		
		return true;

	}

	public static boolean exactMatch(String u, String v, boolean useMorphU, boolean useMorphV) throws Exception {
	
		if (u.equals(v))
			return true;

		if (useMorphU == true)
			u = getMorph(u);

		if (useMorphV == true)
			v = getMorph(v);
		
		if (u.equals(v))
			return true;
		
		return false;
	}

	public static String getMorph(String entity) throws Exception {

		String tokens[] = entity.split("\\s+");

		int n = tokens.length;
		
		String last = tokens[n - 1];

		// morphology
		ArrayList<String> arrMorphs = null;
		if (last.matches(".*\\d+.*") == false) {
			arrMorphs = wnMyManager.getMorph(POS.NOUN, last);
			if (arrMorphs.size() == 1)
				last = arrMorphs.get(0);
		}

		if (n == 1)
			return last;
		
		tokens[n - 1] = last;
		
		StringBuffer buf = new StringBuffer("");
		
		for (int i=0; i<n; i++) {
			buf.append(tokens[i] + " ");
		}
		
		return buf.toString().trim();
		
	}

}
