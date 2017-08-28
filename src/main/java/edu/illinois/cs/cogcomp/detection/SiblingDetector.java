/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.lucenesearch.HitCountSearcher;

/**
 * @author dxquang Feb 15, 2009
 */
public class SiblingDetector {

	public static final int MAX_PRINT = 20;

	public static final int MAX_TOP_PROMINENCE = 30;

	public ArrayList<Match> arrMatches = null;

	public Map<String, ArrayList<Match>> mapGroupMatches = null;

	public ArrayList<String> arrProminenceMatch = null;

	/**
	 * 
	 */
	public SiblingDetector() {

		arrMatches = null;
		mapGroupMatches = new HashMap<String, ArrayList<Match>>();
		arrProminenceMatch = new ArrayList<String>();

	}

	public boolean detectSibling(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {

		arrMatches = new ArrayList<Match>();

		mapGroupMatches = new HashMap<String, ArrayList<Match>>();
		arrProminenceMatch = new ArrayList<String>();

		ArrayList<Match> arrCxCy = null;
		// System.out.println("Matching categories.");
		// 1. I(c_X, c_Y) = 1
		arrCxCy = eInfo_2.matchCategoryCategory(eInfo_1, Title.MATCH_CATEGORY);

		if (arrCxCy.size() > 0) {
			arrMatches.addAll(arrCxCy);
			// System.out.println("arrMatches.size()=" + arrMatches.size());
			// return true;
		}

		ArrayList<Match> arrGxGy = null;
		// System.out.println("Matching generalized categories.");
		// 2. I(g_X, g_Y) = 1
		arrGxGy = eInfo_2.matchCategoryCategory(eInfo_1, Title.MATCH_GENCAT);

		if (arrGxGy.size() > 0) {
			arrMatches.addAll(arrGxGy);
			// System.out.println("arrMatches.size()=" + arrMatches.size());
			// return true;
		}

		if (arrMatches.size() > 0)
			return true;

		return false;
	}

	public void groupMatchesSibling() {

		for (Match match : arrMatches) {

			String titleIds = match.titleId1 + "_" + match.titleId2;

			if (mapGroupMatches.containsKey(titleIds)) {
				ArrayList<Match> arrCurMatches = mapGroupMatches.get(titleIds);
				arrCurMatches.add(match);
			} else {
				ArrayList<Match> arrCurMatches = new ArrayList<Match>();
				arrCurMatches.add(match);
				mapGroupMatches.put(titleIds, arrCurMatches);
				arrProminenceMatch.add(titleIds);
			}
		}

	}

	public String printSiblingRelation(EntityInfo eInfo_1, EntityInfo eInfo_2) {

		StringBuffer buf = new StringBuffer("");

		int i = 1;

		for (String titleIds : arrProminenceMatch) {

			ArrayList<Match> arrCurMatches = mapGroupMatches.get(titleIds);

			if (arrCurMatches.size() > 0) {

				Match firstMatch = arrCurMatches.get(0);

				// System.out.println("[" + i + "] " + firstMatch.title1 +
				// " Vs. "
				// + firstMatch.title2 + " - Prominence: "
				// + firstMatch.prominenceScore);
				buf.append("[" + i + "] " + firstMatch.title1 + " Vs. "
						+ firstMatch.title2 + " - Prominence: "
						+ firstMatch.prominenceScore + "\n");

				int j = 1;

				Match prevMatch = null;

				for (Match match : arrCurMatches) {

					if (prevMatch != null) {
						if (prevMatch.targetClass.equals(match.targetClass))
							continue;
					}
					// System.out.println("\t" + match.titleId1 + ", " +
					// match.catId1);
					// System.out.println("\t" + match.titleId2 + ", " +
					// match.catId2);

					String trace_1 = eInfo_1.getTrace(match.titleId1,
							match.catId1);
					String trace_2 = eInfo_2.getTrace(match.titleId2,
							match.catId2);

					// System.out.println("\t(" + j + ") " + match.targetClass
					// + " - MI: " + match.miScore);
					buf.append("\t(" + j + ") " + match.targetClass + " - MI: "
							+ match.miScore + "\n");

					buf.append("\t\t" + trace_1 + "\n");
					buf.append("\t\t" + trace_2 + "\n");
					// System.out.println("\t\t" + trace_1);
					// System.out.println("\t\t" + trace_2);

					prevMatch = match;

					j++;

					if (j > MAX_PRINT)
						break;
				}

			}

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

	public void calculateMutualInformation(HitCountSearcher hcSearcher)
			throws Exception {

		int i = 0;
		for (String titleIds : arrProminenceMatch) {

			ArrayList<Match> arrCurMatches = mapGroupMatches.get(titleIds);

			if (arrCurMatches.size() > 0) {

				for (Match match : arrCurMatches) {

					double pwMI1 = getPointwiseMutualInformation(match.title1,
							match.targetClass, hcSearcher);
					double pwMI2 = getPointwiseMutualInformation(match.title2,
							match.targetClass, hcSearcher);

					match.miScore = (pwMI1 + pwMI2) / (double) 2;

					match.miScore = match.miScore * match.levelScore;
				}

			}

			sortMIScore(arrCurMatches);

			i++;

			if (i >= MAX_TOP_PROMINENCE)
				break;
		}
	}

	private double getPointwiseMutualInformation(String title, String category,
			HitCountSearcher hcSearcher) throws Exception {
		title = title.replaceAll("\\(.*\\)", " ");
		return hcSearcher.pointwiseMutualInformation(title, category);
	}

	public void sortMIScore(ArrayList<Match> arrCurMatches) {
		Collections.sort(arrCurMatches, new Comparator<Match>() {
			public int compare(Match arg0, Match arg1) {
				if (arg0.miScore < arg1.miScore)
					return 1;
				else if (arg0.miScore == arg1.miScore)
					return 0;
				else
					return -1;
			}
		});
	}

}
