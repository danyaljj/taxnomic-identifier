/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;

import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.TextSearcher;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 19, 2009
 */
public class TfIdfFilter {

	public static final int MAX_TOKENS = 400;

	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	public TextSearcher searcher = null;

	public int numResult;

	Map<String, Double> mapIdf = new HashMap<String, Double>();

	IndexReader reader;

	protected Set<String> setStopWords;

	public TfIdfFilter(String indexDirectory, int numResult, String idfFile)
			throws Exception {

		this.numResult = numResult;

		String[] fields = new String[] { "text" };

		searcher = new TextSearcher(fields, true);

		searcher.setVerbosity(false);

		searcher.open(indexDirectory);

		reader = searcher.getSearcher().getIndexReader();

		readIdf(idfFile);

		setStopWords = parseStopWordString(STOPWORD_STRING);
	}

	private Set<String> parseStopWordString(String stopwordString) {
		Set<String> setSW = new HashSet<String>();
		String tokens[] = STOPWORD_STRING.split(",+");
		for (String token : tokens)
			setSW.add(token);
		return setSW;
	}

	/**
	 * @param idfFile
	 * 
	 */
	private void readIdf(String idfFile) {

		ArrayList<String> arrLines = IOManager.readLines(idfFile);

		for (String line : arrLines) {

			line = line.trim();
			String items[] = line.split("\\t");

			if (items.length != 3)
				continue;

			mapIdf.put(items[1], Double.parseDouble(items[2]));

		}

	}

	public double getFilterScore(String e_1, String e_2) throws Exception {

		ArrayList<ILuceneResult> arrResultE1 = searcher.search(e_1, numResult);
		// System.out.println("- E1: " + arrResultE1.size());

		ArrayList<ILuceneResult> arrResultE2 = searcher.search(e_2, numResult);
		// System.out.println("- E2: " + arrResultE2.size());

		double score = getTitleTfIdfScore(arrResultE1, arrResultE2);

		return score;
	}

	private double getTitleTfIdfScore(ArrayList<ILuceneResult> arrResultE1,
			ArrayList<ILuceneResult> arrResultE2) {

		ArrayList<String> arrWordE1 = extractTitleWords(arrResultE1);

		Map<String, Integer> mapWordFreq1 = getMapWordFreqs(arrWordE1);

		ArrayList<String> arrWordE2 = extractTitleWords(arrResultE2);

		Map<String, Integer> mapWordFreq2 = getMapWordFreqs(arrWordE2);

		Set<String> setWordE1Intersection = new HashSet<String>(arrWordE1);

		// Set<String> setWordE1Union = new HashSet<String>(arrWordE1);

		Set<String> setWordE2 = new HashSet<String>(arrWordE2);

		setWordE1Intersection.retainAll(setWordE2);

		// setWordE1Union.addAll(setWordE2);

		if (setWordE1Intersection.size() == 0)
			return 0;

		double score = calculateTfIdfScore(setWordE1Intersection, mapWordFreq1,
				mapWordFreq2);

		return score;

	}

	/**
	 * @param setWordEIntersection
	 * @param mapWordFreq1
	 * @param mapWordFreq2
	 * @return
	 */
	private double calculateTfIdfScore(Set<String> setWordEIntersection,
			Map<String, Integer> mapWordFreq1, Map<String, Integer> mapWordFreq2) {

		int numWords = 0;

		double score = 0;

		for (String word : setWordEIntersection) {
			if (mapIdf.containsKey(word) && mapWordFreq1.containsKey(word)
					&& mapWordFreq2.containsKey(word)) {
				score += (double) ((double) mapWordFreq1.get(word) * mapIdf
						.get(word)) * (double)((double) mapWordFreq2.get(word) * mapIdf
								.get(word));
				numWords += mapWordFreq1.get(word) + mapWordFreq2.get(word);
			}
		}
		
		return (score == 0 ? 0 : score / (double) numWords);
	}

	private double getTextTfIdfScore(ArrayList<ILuceneResult> arrResultE1,
			ArrayList<ILuceneResult> arrResultE2) {

		ArrayList<String> arrWordE1 = extractWords(arrResultE1);

		Map<String, Integer> mapWordFreq1 = getMapWordFreqs(arrWordE1);

		ArrayList<String> arrWordE2 = extractWords(arrResultE2);

		Map<String, Integer> mapWordFreq2 = getMapWordFreqs(arrWordE2);

		Set<String> setWordE1Intersection = new HashSet<String>(arrWordE1);

		// Set<String> setWordE1Union = new HashSet<String>(arrWordE1);

		Set<String> setWordE2 = new HashSet<String>(arrWordE2);

		setWordE1Intersection.retainAll(setWordE2);

		// setWordE1Union.addAll(setWordE2);

		if (setWordE1Intersection.size() == 0)
			return 0;

		double score = calculateTfIdfScore(setWordE1Intersection, mapWordFreq1,
				mapWordFreq2);

		return score;

	}

	/**
	 * @param arrWordE1
	 * @return
	 */
	private Map<String, Integer> getMapWordFreqs(ArrayList<String> arrWordE) {
		Map<String, Integer> mapWordFreqs = new HashMap<String, Integer>();
		for (String word : arrWordE) {
			if (!mapWordFreqs.containsKey(word)) {
				Integer freq = new Integer(1);
				mapWordFreqs.put(word, freq);
			} else {
				Integer freq = mapWordFreqs.get(word);
				freq++;
			}
		}
		return mapWordFreqs;
	}

	private double getTitleScore(ArrayList<ILuceneResult> arrResultE1,
			ArrayList<ILuceneResult> arrResultE2) {

		ArrayList<String> arrWordE1 = extractTitleWords(arrResultE1);

		ArrayList<String> arrWordE2 = extractTitleWords(arrResultE2);

		Set<String> setWordE1Intersection = new HashSet<String>(arrWordE1);

		Set<String> setWordE1Union = new HashSet<String>(arrWordE1);

		Set<String> setWordE2 = new HashSet<String>(arrWordE2);

		setWordE1Intersection.retainAll(setWordE2);

		setWordE1Union.addAll(setWordE2);

		if (setWordE1Intersection.size() == 0)
			return 0;

		double score = (double) setWordE1Intersection.size()
				/ (double) setWordE1Union.size();

		return score;
	}

	/**
	 * @param arrResultE1
	 * @return
	 */
	private ArrayList<String> extractTitleWords(
			ArrayList<ILuceneResult> arrResultE) {

		ArrayList<String> arrWords = new ArrayList<String>();

		for (ILuceneResult result : arrResultE) {

			String text = result.getTitle();

			text = text.replaceAll("\\p{Punct}", " ");

			String tokens[] = text.split("\\s+");

			int n = Math.min(tokens.length, MAX_TOKENS);

			ArrayList<String> arrTokens = new ArrayList<String>();

			for (int i = 0; i < n; i++) {
				if (!setStopWords.contains(tokens[i]))
					arrTokens.add(tokens[i]);
			}

			arrWords.addAll(arrTokens);
		}

		return arrWords;

	}

	/**
	 * @param arrResultE1
	 * @param arrResultE2
	 * @return
	 */
	private double getScore(ArrayList<ILuceneResult> arrResultE1,
			ArrayList<ILuceneResult> arrResultE2) {

		ArrayList<String> arrWordE1 = extractWords(arrResultE1);

		ArrayList<String> arrWordE2 = extractWords(arrResultE2);

		Set<String> setWordE1Intersection = new HashSet<String>(arrWordE1);

		Set<String> setWordE2 = new HashSet<String>(arrWordE2);

		setWordE1Intersection.retainAll(setWordE2);

		if (setWordE1Intersection.size() == 0)
			return 0;

		double score = getIdfScore(setWordE1Intersection);

		return score;
	}

	/**
	 * @param setWordE1Intersection
	 * @return
	 */
	private double getIdfScore(Set<String> setWordIntersection) {

		double score = 0;

		for (String word : setWordIntersection) {

			if (mapIdf.containsKey(word))
				score += mapIdf.get(word);

		}

		return score;
	}

	/**
	 * @param arrResultE1
	 * @return
	 */
	private ArrayList<String> extractWords(ArrayList<ILuceneResult> arrResultE) {

		ArrayList<String> arrWords = new ArrayList<String>();

		for (ILuceneResult result : arrResultE) {

			String text = result.getDoc();

			text = text.replaceAll("\\p{Punct}", " ");

			String tokens[] = text.split("\\s+");

			int n = Math.min(tokens.length, MAX_TOKENS);

			ArrayList<String> arrTokens = new ArrayList<String>();

			for (int i = 0; i < n; i++) {
				if (!setStopWords.contains(tokens[i]))
					arrTokens.add(tokens[i]);
			}

			arrWords.addAll(arrTokens);
		}

		return arrWords;
	}

}
