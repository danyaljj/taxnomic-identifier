/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.wikiannotator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.Acl10Constants;
import edu.illinois.cs.cogcomp.acl10.wikiannotator.Chunking.TagWord;

/**
 * @author dxquang Jun 25, 2009
 */
public class WikiTitleAnnotator {

	public class Phrase {

		public ArrayList<String> arrWords = new ArrayList<String>();
		public boolean useful = false;

		public void addWord(String word) {
			arrWords.add(word);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String s = "";
			for (String word : arrWords) {
				s += word + " ";
			}
			return s.trim();
		}

		/**
		 * @param useful
		 *            the useful to set
		 */
		public void setUseful(boolean useful) {
			this.useful = useful;
		}

	}

	public class WordSpan {
		String span;
		int begin;
		int end;
		boolean wikiTitle;

		/**
		 * 
		 */
		public WordSpan(String span, int begin, int end, boolean wikiTitle) {
			this.span = span;
			this.begin = begin;
			this.end = end;
			this.wikiTitle = wikiTitle;
		}

		public double getScore() {

			int len = (end - begin) + 1;
			double score = len;
			if (wikiTitle == true)
				score = score * (len + 1);
			return score;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			String s = "";
			if (wikiTitle == true) {
				s = "[" + span + "]";
			} else {
				s = span;
			}

			return s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {

			WordSpan ws = (WordSpan) obj;
			return (ws.span.equals(this.span) && (ws.begin == this.begin) && (ws.end == this.end));

		}
	}

	public class TitleSpan {
		public ArrayList<WordSpan> arrWordSpans = new ArrayList<WordSpan>();
		double score = 0;

		public void addWordSpan(WordSpan ws) {
			arrWordSpans.add(ws);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			String s = "";
			for (WordSpan ws : arrWordSpans) {
				if (ws.wikiTitle == true)
					s += "[WIKI " + ws.span + "] ";
				else
					s += ws.span + " ";
			}

			// s += "\t- " + Double.toString(score);

			return s.trim();
		}

		public ArrayList<String> toStringArray() {

			ArrayList<String> arrTitles = new ArrayList<String>();

			for (WordSpan ws : arrWordSpans) {
				if (ws.wikiTitle == true)
					arrTitles.add(ws.span);
			}

			return arrTitles;

		}

	}

	public static Chunking chunk = null;

	public static StopWord sw = null;

	public static IdfManager idfMan = null;

	public static WikiTitleManager titleMan = null;

	public static StringBuffer wikiString = null;

	public static ArrayList<String> wikiTitles = null;

	public static final boolean OUTPUT_TOP = true;

	/**
	 * 
	 */
	public WikiTitleAnnotator() {

		if (chunk == null)
			chunk = new Chunking();

		if (sw == null)
			sw = new StopWord(false);

		if (idfMan == null)
			idfMan = new IdfManager();

		if (titleMan == null)
			titleMan = new WikiTitleManager();
	}

	public WikiTitleAnnotator(Map<String, String> configs) {

		if (chunk == null)
			chunk = new Chunking();

		if (sw == null)
			sw = new StopWord(false);

		if (idfMan == null)
			idfMan = new IdfManager(configs.get(Acl10Constants.IDF_FILE));

		if (titleMan == null)
			titleMan = new WikiTitleManager(configs
					.get(Acl10Constants.NGRAM_TITLE_FILE));
	}

	public String annotate(String sentence) {

		wikiString = new StringBuffer();
		wikiTitles = new ArrayList<String>();

		sentence = sentence.trim();
		sentence = sentence.toLowerCase();

		TagWord[] chunks = chunk.chunkSentenceTagWord(sentence);

		ArrayList<Phrase> arrPhrases = extractPhrases(chunks);

		// for (Phrase p : arrPhrases) {
		// System.out.println(p);
		// }

		annotateAllTitles(arrPhrases);

		/*
		 * StringBuffer output = new StringBuffer("");
		 * 
		 * for (String title : wikiTitles) {
		 * 
		 * output.append(title); output.append("|");
		 * 
		 * }
		 * 
		 * String out = output.toString(); if (out.length() > 0) out =
		 * out.substring(0, out.length()-1);
		 * 
		 * return out;
		 */

		return wikiString.toString().trim();

	}

	/**
	 * @param arrPhrases
	 */
	private void annotateAllTitles(ArrayList<Phrase> arrPhrases) {

		for (Phrase p : arrPhrases) {

			if (p.useful == false) {
				wikiString.append(p.toString());
				wikiString.append(" ");
				continue;
			}

			// System.out.println("\n----------\n\"" + p + "\"\n----------");

			annotateAllTitles(p);

		}

	}

	/**
	 * @param p
	 */
	private void annotateAllTitles(Phrase p) {

		ArrayList<String> arrWords = p.arrWords;

		ArrayList<WordSpan> arrWordSpans = new ArrayList<WordSpan>();

		int size = arrWords.size();

		for (int ngram = 5; ngram > 0; ngram--) {

			for (int i = 0; i < size; i++) {

				String s = getNgram(arrWords, i, size, ngram);

				if (s != null) {

					if (sw.isStopWord(s, false) == false
							&& titleMan.containTitle(s)) {
						WordSpan ws = new WordSpan(s, i, i + ngram - 1, true);
						arrWordSpans.add(ws);

					}

				}

			}

		}

		for (int i = 0; i < size; i++) {
			WordSpan ws = new WordSpan(arrWords.get(i), i, i, false);
			arrWordSpans.add(ws);
		}

		ArrayList<ArrayList<WordSpan>> arrArrWordSpans = constructTitleSpan(
				arrWordSpans, size);

		ArrayList<TitleSpan> arrTitleSpans = new ArrayList<TitleSpan>();

		for (ArrayList<WordSpan> arrWSs : arrArrWordSpans) {

			TitleSpan ts = new TitleSpan();

			String s = "";
			for (WordSpan ws : arrWSs) {
				s += ws.toString() + " ";
				ts.addWordSpan(ws);
				ts.score += ws.getScore();
			}

			arrTitleSpans.add(ts);

		}

		sortScore(arrTitleSpans);

		if (OUTPUT_TOP == false) {
			for (TitleSpan ts : arrTitleSpans) {

				// System.out.println(ts);
				wikiTitles.addAll(ts.toStringArray());

			}
		} else {
			if (arrTitleSpans.size() > 0) {
				wikiTitles.addAll(arrTitleSpans.get(0).toStringArray());
			}
			wikiString.append(arrTitleSpans.get(0).toString());
			wikiString.append(" ");
		}

	}

	private void sortScore(ArrayList<TitleSpan> arrTitleSpans) {
		Collections.sort(arrTitleSpans, new Comparator<TitleSpan>() {
			public int compare(TitleSpan o1, TitleSpan o2) {
				if (o1.score < o2.score)
					return 1;
				else if (o1.score == o2.score)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param arrWordSpans
	 * @return
	 */
	private ArrayList<ArrayList<WordSpan>> constructTitleSpan(
			ArrayList<WordSpan> arrWordSpans, int size) {

		ArrayList<ArrayList<WordSpan>> arrResults = new ArrayList<ArrayList<WordSpan>>();

		HashMap<Integer, ArrayList<WordSpan>> mapWordSpans = new HashMap<Integer, ArrayList<WordSpan>>();

		for (WordSpan ws : arrWordSpans) {
			if (!mapWordSpans.containsKey(ws.begin)) {
				ArrayList<WordSpan> arrTemp = new ArrayList<WordSpan>();
				arrTemp.add(ws);
				mapWordSpans.put(new Integer(ws.begin), arrTemp);
			} else {
				ArrayList<WordSpan> arrTemp = mapWordSpans.get(ws.begin);
				arrTemp.add(ws);
			}
		}

		int i = 0;

		ArrayList<WordSpan> arrTemp = new ArrayList<WordSpan>();

		construct(mapWordSpans, i, size, arrTemp, arrResults);

		return arrResults;
	}

	/**
	 * @param mapWordSpans
	 * @param index
	 * @param size
	 * @param arrTemp
	 * @param arrResults
	 */
	private void construct(HashMap<Integer, ArrayList<WordSpan>> mapWordSpans,
			int index, int size, ArrayList<WordSpan> arrTemp,
			ArrayList<ArrayList<WordSpan>> arrResults) {

		if (index == size) {

			ArrayList<WordSpan> arrWSs = new ArrayList<WordSpan>();
			for (WordSpan ws : arrTemp) {
				arrWSs.add(ws);
			}

			arrResults.add(arrWSs);
			return;
		}

		ArrayList<WordSpan> arrWSs = mapWordSpans.get(index);

		for (int i = 0; i < arrWSs.size(); i++) {
			WordSpan ws = arrWSs.get(i);
			arrTemp.add(ws);
			int t = index + (ws.end - ws.begin + 1);
			construct(mapWordSpans, t, size, arrTemp, arrResults);
			arrTemp.remove(ws);
		}
	}

	/**
	 * @param arrPhrases
	 */
	public void annotate(ArrayList<Phrase> arrPhrases) {

		for (Phrase p : arrPhrases) {

			System.out.println("\n\"" + p + "\"");

			annotate(p, 0, p.arrWords.size(), 0);

		}
	}

	/**
	 * @param p
	 */
	private void annotate(Phrase p, int begin, int end, int level) {

		if (begin == end)
			return;

		ArrayList<String> arrWords = p.arrWords;

		for (int ngram = 5; ngram >= 1; ngram--) {

			int i = begin;

			while (i < end) {

				String s = getNgram(arrWords, i, end, ngram);
				if (s == null)
					i++;
				else {

					// System.out.println("\n>>> " + s);
					if (sw.isStopWord(s, false) == false
							&& titleMan.containTitle(s)) {

						System.out.println(s);

						level++;

						// System.out.println("Left: " + begin + " to " + i);
						annotate(p, begin, i, level); // left

						level--;

						level++;

						// System.out.println("Right: " + (i + ngram) + " to " +
						// end);
						annotate(p, i + ngram, end, level); // right

						level--;

						i = i + ngram;

						break;

					} else
						i++;
				}
			}

			if (level == 0)
				System.out.println("-- " + ngram);
		}

	}

	private String getNgram(ArrayList<String> arrWords, int begin, int end,
			int n) {

		if (begin + n > end)
			return null;

		String s = "";

		for (int i = 0; i < n; i++) {

			s += arrWords.get(begin + i) + " ";

		}

		return s.trim();
	}

	/**
	 * @param chunks
	 * @return
	 */
	private ArrayList<Phrase> extractPhrases(TagWord[] chunks) {

		ArrayList<Phrase> arrPhrases = new ArrayList<Phrase>();

		boolean flag = false;

		Phrase phrase = null;

		boolean added = false;

		Phrase outPhrase = null;

		for (TagWord chunk : chunks) {

			// System.out.println(chunk);

			String tag = chunk.tag;
			String word = chunk.word;

			if (tag.startsWith("B")) {

				String tagPhrase = tag.substring(2);

				if (tagPhrase.equals("NP")) {

					if (flag == false) {

						phrase = new Phrase();
						phrase.addWord(word);
						flag = true;
						added = true;

					} else {
						phrase.addWord(word);
						added = true;
					}

				} else if (tagPhrase.equals("PP")) {

					if (flag == true) {
						phrase.addWord(word);
						added = true;
					}

				} else {

					if (phrase != null) {
						phrase.setUseful(true);
						arrPhrases.add(phrase);
					}

					phrase = null;
					flag = false;

				}
			} else if (tag.startsWith("I")) {

				String tagPhrase = tag.substring(2);

				if (tagPhrase.equals("NP")) {

					if (flag == true) {

						phrase.addWord(word);
						added = true;

					}

				} else if (tagPhrase.equals("PP")) {

					if (flag == true) {
						phrase.addWord(word);
						added = true;
					}

				}

			} else if (tag.startsWith("O")) {

				if (phrase != null) {
					phrase.setUseful(true);
					arrPhrases.add(phrase);
				}
				phrase = null;
				flag = false;

			}

			if (added == false) {
				outPhrase = new Phrase();
				outPhrase.addWord(word);
				outPhrase.setUseful(false);
				arrPhrases.add(outPhrase);
				outPhrase = null;
			}

			added = false;
		}

		if (phrase != null) {
			phrase.setUseful(true);
			arrPhrases.add(phrase);
		}

		return arrPhrases;
	}

	public static void main(String[] args) throws IOException {
		WikiTitleAnnotator annotator = new WikiTitleAnnotator();

		String sentence = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter a sentence (_ to end): ");
		sentence = in.readLine();

		if (sentence.equals("_"))
			return;

		do {

			if (sentence.length() != 0) {
				String res = annotator.annotate(sentence);
				System.out.println(res);
			}

			System.out.print("Enter a sentence (_ to end): ");
			sentence = in.readLine();

		} while (!sentence.equals("_"));

	}
}
