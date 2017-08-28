/**
 * 
 */
package edu.illinois.cs.cogcomp.category;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dxquang
 * Feb 10, 2009
 */

public class LexicalMatching {

	public static final String ENGLISH_PREPOSITIONS = "aboard,about,above,across,after,against,along,alongside,amid,amidst,among,amongst,around,as,aside,at,athwart,atop,barring,before,behind,below,beneath,beside,besides,between,beyond,but,by,circa,concerning,despite,down,during,except,failing,following,for,from,in,inside,into,like,mid,minus,near,next,notwithstanding,of,off,on,onto,opposite,out,outside,over,pace,past,per,plus,regarding,round,save,since,than,through,throughout,till,times,to,toward,towards,under,underneath,unlike,until,up,upon,versus,via,with,within,without,worth";
	
	Set<String> setPrepositions;
	
	/**
	 * 
	 */
	public LexicalMatching() {

		setPrepositions = parsePrepositionString();
	
	}
	
	private Set<String> parsePrepositionString() {
		Set<String> setSW = new HashSet<String>();
		String tokens[] = ENGLISH_PREPOSITIONS.split(",+");
		for (String token : tokens)
			setSW.add(token.trim());
		return setSW;
	}
	
	public String generalizeTitle(String title) {
		
		String mainTitle = title.replaceAll("\\(.*\\)", " ");
		
		mainTitle = mainTitle.toLowerCase();
		
		mainTitle = mainTitle.replaceAll("\\p{Punct}", " ");

		mainTitle = mainTitle.trim();
		
		String tokens[] = mainTitle.split("\\s+");
		
		int i=0;
		for (String token : tokens) {

			if (setPrepositions.contains(token)) {
				break;
			}

			i ++;
		}
		
		if (i == tokens.length)	// There is no preposition in the title.
			return "";
		
		StringBuffer generalizedTitle = new StringBuffer("");
		
		for (int j=0; j<i; j++) {
			generalizedTitle.append(tokens[j] + " ");
		}
		
		mainTitle = generalizedTitle.toString().trim();
		return mainTitle;
	}
}
