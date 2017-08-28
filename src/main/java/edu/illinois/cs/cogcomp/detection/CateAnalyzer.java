/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dxquang Feb 15, 2009
 */
public class CateAnalyzer {

	public static final int NOT_FOUND = -1;

	private String generalizedName;

	private String domain;

	public static final String ENGLISH_PREPOSITIONS = "aboard,about,above,across,after,against,along,alongside,amid,amidst,among,amongst,around,as,aside,at,athwart,atop,barring,before,behind,below,beneath,beside,besides,between,beyond,but,by,circa,concerning,despite,down,during,except,failing,following,for,from,in,inside,into,like,mid,minus,near,next,notwithstanding,of,off,on,onto,opposite,out,outside,over,pace,past,per,plus,regarding,round,save,since,than,through,throughout,till,times,to,toward,towards,under,underneath,unlike,until,up,upon,versus,via,with,within,without,worth";

	Set<String> setPrepositions;

	/**
	 * 
	 */
	public CateAnalyzer() {

		setPrepositions = parsePrepositionString();

		generalizedName = "";

		domain = "";

	}

	private Set<String> parsePrepositionString() {

		Set<String> setSW = new HashSet<String>();

		String tokens[] = ENGLISH_PREPOSITIONS.split(",+");

		for (String token : tokens) {

			setSW.add(token.toLowerCase().trim());

		}

		return setSW;
	}

	/**
	 * @return the generalizedName
	 */
	public String getGeneralizedName() {
		return generalizedName;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	public void analyze(String s, int task) {

		generalizedName = "";

		domain = "";

		String mainString = formatText(s);

		String tokens[] = mainString.split("\\s+");

		int n = tokens.length;

		int i = indexOfPrep(tokens, 0, n);

		if (i == NOT_FOUND) { // There is no preposition in the title.

			if (task == RelationDetector.TASK_PARENTCHILD) {
				if (n > 1)
					generalizedName = tokens[n - 1];
			}
			else if (task == RelationDetector.TASK_SIBLING) {
				generalizedName = "";
			}
			else
				generalizedName = "";

			domain = "";

		} else {

			generalizedName = subString(tokens, 0, i);

			int k = indexOfPrep(tokens, i + 1, n);

			if (k == NOT_FOUND) {

				domain = subString(tokens, i + 1, n);
			} else {

				domain = subString(tokens, i + 1, k);

			}
		}

	}

	private String subString(String tokens[], int from, int to) {

		StringBuffer buf = new StringBuffer(" ");

		for (int i = from; i < to; i++) {
			buf.append(tokens[i] + " ");
		}

		return buf.toString().trim();
	}

	private int indexOfPrep(String tokens[], int from, int to) {

		int i = from;

		for (; i < to; i++) {

			if (setPrepositions.contains(tokens[i])) {
				break;
			}

		}

		if (i < to)
			return i;

		return NOT_FOUND;
	}

	private String formatText(String s) {

		String mainString = s.replaceAll("\\(.*\\)", " ");

		mainString = mainString.toLowerCase();

		mainString = mainString.replaceAll("\\p{Punct}", " ");

		mainString = mainString.trim();

		return mainString;
	}

}
