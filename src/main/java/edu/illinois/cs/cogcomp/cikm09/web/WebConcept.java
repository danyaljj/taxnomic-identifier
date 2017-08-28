/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.yahoo.YahooSearch;

/**
 * @author dxquang May 29, 2009
 */
public class WebConcept {

	public class ConceptFreq {
		public String concept;
		public int freq;
		
		/**
		 * 
		 */
		public ConceptFreq(String concept, int freq) {
			this.concept = concept;
			this.freq = freq;
		}
	}
	
	public static final String[] DELIMINATORS = {"\\.", ",", "\\*"};
	
	public static void sortConceptFreq(ArrayList<ConceptFreq> arrConceptFreqs) {
		Collections.sort(arrConceptFreqs, new Comparator<ConceptFreq>() {
			@Override
			public int compare(ConceptFreq o1, ConceptFreq o2) {
				if (o1.freq < o2.freq)
					return 1;
				else if (o1.freq == o2.freq)
					return 0;
				else
					return -1;
			}
		});
	}

	public ArrayList<String> webConcepts(String query, int topKWeb,
			int topK) throws Exception {
		
		ArrayList<String> arrTokens = new ArrayList<String>();
		
		YahooSearch ySearch = new YahooSearch();
		
		ArrayList<IResult> arrResult = ySearch.searchWeb(query, topKWeb);
		System.out.println();
		int i = 1;
		
		for (IResult result : arrResult) {
			System.out.println("[" + i + "] " + "Title: " + result.getTitle());
			System.out.println("Snippet: " + result.getSnippet());
			System.out.println("Url: " + result.getUrl());
			System.out.println();
			i++;
		}

		return arrTokens;

	}

	public ArrayList<String> webConcepts(String query1, String query2, int topKWeb,
			int topK) throws Exception {
		
		ArrayList<String> arrCandidates = new ArrayList<String>();
		
		YahooSearch ySearch = new YahooSearch();
		
		query1 = "\"" + query1 + "\"";
		
		query2 = "\"" + query2 + "\"";
		
		ArrayList<IResult> arrResult = ySearch.searchWeb(query1 + " " + query2, topKWeb);

		int i = 1;
		
		for (IResult result : arrResult) {
			// System.out.println("\n[" + i + "] " + "Title: " + result.getTitle());
			// System.out.println("Snippet: " + result.getSnippet());
			// System.out.println("Url: " + result.getUrl());
			ArrayList<String> arrConcepts = analyzeSnippet_1(result.getSnippet());
			arrCandidates.addAll(arrConcepts);
			i++;
		}

		Map<String, Integer> mapConceptFreqs = new HashMap<String, Integer>();
		for (String c : arrCandidates) {
			c = c.toLowerCase();
			c = c.trim();
			if (!mapConceptFreqs.containsKey(c)) {
				mapConceptFreqs.put(c, 1);
			}
			else {
				int freq = mapConceptFreqs.get(c);
				freq ++;
				mapConceptFreqs.put(c, freq);
			}
		}
		
		Set<String> keySet = mapConceptFreqs.keySet();
		
		ArrayList<ConceptFreq> arrConceptFreqs = new ArrayList<ConceptFreq>();
		
		for (String key : keySet) {
			ConceptFreq cf = new ConceptFreq(key, mapConceptFreqs.get(key));
			arrConceptFreqs.add(cf);	
		}
		
		sortConceptFreq(arrConceptFreqs);
		
		ArrayList<String> arrOutConcepts = new ArrayList<String>();
		for (ConceptFreq cf : arrConceptFreqs) {
			arrOutConcepts.add(cf.concept);
			//System.out.println(cf.concept + " - " + cf.freq);
		}
		
		return arrOutConcepts;

	}

	/**
	 * @param snippet
	 * @return
	 */
	private ArrayList<String> analyzeSnippet_1(String snippet) {

		ArrayList<String> arrConcepts = new ArrayList<String>();
		
		if (snippet == null)
			return arrConcepts;
		
		int n = DELIMINATORS.length;
		
		for (int i=0; i<n; i++) {
			String d = DELIMINATORS[i];
			
			String s = snippet.replaceAll("\\.\\.\\.", d);
			
			String regexp = ".{2,1000}?" + d;
			
			Pattern p = Pattern.compile(regexp);
			Matcher m = p.matcher(s);
			
			ArrayList<String> arrTemps = new ArrayList<String>();
			while (m.find()) {
				String concept = m.group();
				concept = concept.replaceAll(d, "");
				concept = concept.trim();
				arrTemps.add(concept);
			}
			
			int j = 0;
			ArrayList<String> arrTs = new ArrayList<String>();
			for (String t : arrTemps) {
				
				if (t.length() < 20) {
					arrTs.add(t);
					j ++;
				}
				else {
					if (j > 4)
						arrConcepts.addAll(arrTs);
					arrTs = new ArrayList<String>();
					j = 0;
				}
				
			}
			
			if (j>4) {
				arrConcepts.addAll(arrTs);
			}
		}
		
		return arrConcepts;
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		WebConcept wc = new WebConcept();
		ArrayList<String> arrConcepts = wc.webConcepts("fish", "beachsalmon", 50, 10);
		for (String s : arrConcepts) {
			System.out.println(s);
		}
	}
}
