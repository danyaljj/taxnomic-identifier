/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;

import net.didion.jwnl.data.POS;

import edu.illinois.cs.cogcomp.wnsim.WNSim;

/**
 * @author dxquang Apr 22, 2010
 */
public class QuangWordNetManager {

	public WNSim wnsim = null;

	/**
	 * 
	 */
	public QuangWordNetManager(String pathWordNet) {
		wnsim = WNSim.getInstance(pathWordNet);
	}

	public boolean isWordNetWord(POS pos, String word) {
		if (pos == POS.NOUN) {
			return wnsim.isWordNetNoun(word);
		}
		// TODO: Implement other POS
		return false;
	}

	public ArrayList<String> getHypernymForNoun(String word, int maxLevelUp) {
		return wnsim.getNounHypernyms(word, maxLevelUp);
	}

}
