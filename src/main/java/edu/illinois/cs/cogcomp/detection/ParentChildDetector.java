/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author dxquang Feb 15, 2009
 */
public class ParentChildDetector {

	public static final int MAX_TOP_PROMINENCE = 30;

	public ArrayList<Match> arrMatches = null;

	/**
	 * 
	 */
	public ParentChildDetector() {

	}

	public boolean detectParent(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {

		arrMatches = null;

		// System.out.println("Matching categories.");
		// 1. I(X, c_Y) = 1
		arrMatches = eInfo_2.matchTitleCategory(eInfo_1, Title.MATCH_CATEGORY);

		if (arrMatches.size() > 0) {
			// System.out.println("arrMatches.size()=" + arrMatches.size());
			return true;
		}

		// System.out.println("Matching generalized categories.");
		// 2. I(X, g_Y) = 1
		arrMatches = eInfo_2.matchTitleCategory(eInfo_1, Title.MATCH_GENCAT);

		if (arrMatches.size() > 0) {
			// System.out.println("arrMatches.size()=" + arrMatches.size());
			return true;
		}
		
		//System.out.println("Matching domains.");

		// 3. I(X, d_Y) = 1
		arrMatches = eInfo_2.matchTitleCategory(eInfo_1, Title.MATCH_DOMAIN);

		if (arrMatches.size() > 0) {
			// System.out.println("arrMatches.size()=" + arrMatches.size());
			return true;
		}

		return false;
	}
	
	public ArrayList<String> getTargetClasses() {
		ArrayList<String> arrClasses = new ArrayList<String>();
		for (Match match : arrMatches) {
			arrClasses.add(match.title1);
		}
		return arrClasses;
	}

	public String printParentRelation(EntityInfo eInfo_1, EntityInfo eInfo_2) {

		StringBuffer buf = new StringBuffer("");

		int i = 1;

		for (Match match : arrMatches) {

			String trace_1 = eInfo_1.getTrace(match.titleId1, match.catId1);
			String trace_2 = eInfo_2.getTrace(match.titleId2, match.catId2);

			// System.out.println("[" + i + "] " + match.title1 + " Vs. "
			// + match.title2);
			buf.append("[" + i + "] " + match.title1 + " Vs. " + match.title2
					+ "\n");

			// System.out.println("\t" + trace_1);
			// System.out.println("\t" + trace_2);
			buf.append("\t" + trace_1 + "\n");
			buf.append("\t" + trace_2 + "\n");

			i++;

			if (i > MAX_TOP_PROMINENCE)
				break;
		}

		return buf.toString();
	}

	/**
	 * @param arrMatches
	 */
	public void sortPromScore() {
		Collections.sort(arrMatches, new Comparator<Match>() {
			public int compare(Match arg0, Match arg1) {
				if (arg0.prominenceScore < arg1.prominenceScore)
					return 1;
				else if (arg0.prominenceScore == arg1.prominenceScore)
					return 0;
				else
					return -1;
			}
		});
	}

}
