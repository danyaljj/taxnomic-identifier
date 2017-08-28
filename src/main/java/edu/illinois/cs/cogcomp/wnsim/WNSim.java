/**
 * 
 */
package edu.illinois.cs.cogcomp.wnsim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Apr 14, 2010
 */
public class WNSim {

	// Singleton
	private static WNSim instance = null;
	private static boolean INFO_CONTENT = false;

	private static int MAX_DEPTH = 50;

	public static final String PATH_LCS = "lcs";
	public static final String PATH_N1 = "n1";
	public static final String PATH_N2 = "n2";
	public static final String PATH_SHORTEST = "l";
	public static final String IC_LCS = "ic_lcs";
	public static final String IC_SYNSET1 = "ic_word1";
	public static final String IC_SYNSET2 = "ic_word2";

	protected String pathWordNet;

	protected Map<String, SynsetNode> nounSynsets = null;
	protected Map<String, SynsetNode> verbSynsets = null;
	protected Map<String, SynsetNode> adjSynsets = null;
	protected Map<String, SynsetNode> advSynsets = null;

	protected Map<String, List<String>> nounLexicon = null;
	protected Map<String, List<String>> verbLexicon = null;
	protected Map<String, List<String>> adjLexicon = null;
	protected Map<String, List<String>> advLexicon = null;

	protected static Map<String, Double> nountInfoContent = null;
	protected static Map<String, Double> verbInfoConctent = null;

	protected class MyInteger {
		private int value;

		/**
		 * 
		 */
		public MyInteger(int value) {
			this.value = value;
		}

		/**
		 * @return the value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * @param value
		 *            the value to set
		 */
		public void setValue(int value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return Integer.toString(value);
		}
	}

	/**
	 * 
	 */
	protected WNSim(String pathWordNet) {
		this.pathWordNet = pathWordNet;
		initializeWNSim();
	}

	public static WNSim getInstance(String pathWordNet) {
		if (instance == null) {
			instance = new WNSim(pathWordNet);
		}
		return instance;
	}

	public static void initilizeInformationContent(String icFile) {
		if (INFO_CONTENT == false) {
			loadInformationContent(icFile);
		}
		INFO_CONTENT = true;
	}

	/**
	 * @param icFile
	 */
	private static void loadInformationContent(String icFile) {

		nountInfoContent = new HashMap<String, Double>();
		verbInfoConctent = new HashMap<String, Double>();

		ArrayList<String> lines = IOManager.readLines(icFile);
		int n = lines.size();
		for (int i = 1; i < n; i++) {
			String line = lines.get(i);
			String[] parts = line.split("\\s+");
			int pos = parts[0].charAt(parts[0].length() - 1);
			String synset = parts[0].substring(0, parts[0].length() - 1);
			if (pos == 'n') {
				nountInfoContent.put(fullIdentity(synset), Double
						.parseDouble(parts[1]));
			} else {
				verbInfoConctent.put(fullIdentity(synset), Double
						.parseDouble(parts[1]));
			}
		}
	}

	/**
	 * @param synset
	 * @return
	 */
	private static String fullIdentity(String synset) {
		int diff = 8 - synset.length();
		StringBuffer s = new StringBuffer("");
		for (int i = 0; i < diff; i++) {
			s.append("0");
		}
		return (s.append(synset)).toString();
	}

	/**
	 * 
	 */
	private void initializeWNSim() {
		nounSynsets = new HashMap<String, SynsetNode>();
		verbSynsets = new HashMap<String, SynsetNode>();
		adjSynsets = new HashMap<String, SynsetNode>();
		advSynsets = new HashMap<String, SynsetNode>();

		nounLexicon = new HashMap<String, List<String>>();
		verbLexicon = new HashMap<String, List<String>>();
		adjLexicon = new HashMap<String, List<String>>();
		advLexicon = new HashMap<String, List<String>>();

		System.out.println("\nLoading WordNet data and index...");

		loadWNData(this.pathWordNet + "/dict/data.noun", nounSynsets);
		System.out.println("\t# of noun synset: " + nounSynsets.size());
		loadWNData(this.pathWordNet + "/dict/data.verb", verbSynsets);
		System.out.println("\t# of verb synset: " + verbSynsets.size());
		loadWNData(this.pathWordNet + "/dict/data.adj", adjSynsets);
		System.out.println("\t# of adj synset: " + adjSynsets.size());
		loadWNData(this.pathWordNet + "/dict/data.adv", advSynsets);
		System.out.println("\t# of adv synset: " + advSynsets.size());

		loadWNIndex(this.pathWordNet + "/dict/index.noun", nounLexicon);
		System.out.println("\t# of noun: " + nounLexicon.size());
		loadWNIndex(this.pathWordNet + "/dict/index.verb", verbLexicon);
		System.out.println("\t# of verb: " + verbLexicon.size());
		loadWNIndex(this.pathWordNet + "/dict/index.adj", adjLexicon);
		System.out.println("\t# of adj: " + adjLexicon.size());
		loadWNIndex(this.pathWordNet + "/dict/index.adv", advLexicon);
		System.out.println("\t# of adv: " + advLexicon.size());

		System.out.println("Done.");
	}

	/**
	 * @param fname
	 * @param mapLexicon
	 */
	private void loadWNIndex(String fname, Map<String, List<String>> mapLexicon) {
		ArrayList<String> lines = IOManager.readLines(fname);

		int n = lines.size();
		for (int i = 29; i < n; i++) {
			String line = lines.get(i);
			String[] parts = line.split("\\s+");
			if (parts.length <= 4)
				continue;
			String word = parts[0];
			int numPointer = Integer.parseInt(parts[3]);
			List<String> lexicons = new ArrayList<String>();
			for (int j = 4 + numPointer + 2; j < parts.length; j++) {
				lexicons.add(parts[j]);
			}
			mapLexicon.put(word, lexicons);
		}
	}

	/**
	 * @param fname
	 * @param mapSynset
	 */
	private void loadWNData(String fname, Map<String, SynsetNode> mapSynset) {

		// Creat a dummy root
		SynsetNode root = new SynsetNode();
		String rootId = "*Root*";
		root.nodeId = "0000000000";
		root.words.add(rootId);
		mapSynset.put(root.nodeId, root);

		ArrayList<String> lines = IOManager.readLines(fname);

		int n = lines.size();
		for (int i = 29; i < n; i++) {
			String line = lines.get(i);
			String[] parts = line.split("\\s+");
			if (parts.length <= 4)
				continue;
			SynsetNode node = new SynsetNode();
			node.nodeId = parts[0];
			int numWord = Integer.parseInt(parts[3], 16);
			if (numWord == 0)
				continue;
			for (int j = 0; j < numWord; j++) {
				node.words.add(parts[4 + (2 * j)]);
			}
			int offset = 3 + (2 * numWord) + 1;
			int numPointer = Integer.parseInt(parts[offset]);
			for (int j = 0; j < numPointer; j++) {
				// Hypernym
				if (parts[offset + 1 + (4 * j)].equals("@")) {
					node.hyperIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
				// Part-Of Holonym
				else if (parts[offset + 1 + (4 * j)].equals("#p")) {
					node.hyperIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
				// Member-Of Holonym
				else if (parts[offset + 1 + (4 * j)].equals("#m")) {
					node.hyperIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
				// Entailment
				else if (parts[offset + 1 + (4 * j)].equals("*")) {
					node.entailIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
				// Derivationally related forms
				else if (parts[offset + 1 + (4 * j)].equals("+")) {
					node.derivationIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
				// Antonym
				else if (parts[offset + 1 + (4 * j)].equals("!")) {
					node.antonIds.add(parts[offset + 1 + (4 * j) + 1]);
				}
			}

			if (node.hyperIds.size() == 0)
				node.hyperIds.add(root.nodeId);

			mapSynset.put(node.nodeId, node);
		}
	}

	/**
	 * @param synset
	 * @param mapSynsets
	 * @return
	 */
	private int getSynsetDepth(String synset, Map<String, SynsetNode> mapSynsets) {
		int level = 0;
		MyInteger depth = new MyInteger(-1);
		getDepth(synset, mapSynsets, level, depth);
		return depth.getValue();
	}

	/**
	 * @param synset
	 * @param mapSynsets
	 * @param level
	 * @param maxLevel
	 * @return
	 */
	private void getDepth(String synset, Map<String, SynsetNode> mapSynsets,
			int level, MyInteger depth) {
		SynsetNode node = mapSynsets.get(synset);

		if (node.hyperIds.size() == 0) {
			if (depth.getValue() < level)
				depth.setValue(level);
			return;
		}

		if (level > MAX_DEPTH)
			return;

		level++;
		for (String h : node.hyperIds) {
			getDepth(h, mapSynsets, level, depth);
		}
		level--;
	}

	public int getWordDepth(String word, String partOfSpeech, int sense) {
		if (partOfSpeech.startsWith("N")) {
			if (nounLexicon.containsKey(word)
					&& nounLexicon.get(word).size() > sense)
				return getSynsetDepth(nounLexicon.get(word).get(sense),
						nounSynsets);
		}
		if (partOfSpeech.startsWith("V")) {
			if (verbLexicon.containsKey(word)
					&& verbLexicon.get(word).size() > sense)
				return getSynsetDepth(verbLexicon.get(word).get(sense),
						verbSynsets);
		}
		if (partOfSpeech.startsWith("J")) {
			if (adjLexicon.containsKey(word)
					&& adjLexicon.get(word).size() > sense)
				return getSynsetDepth(adjLexicon.get(word).get(sense),
						adjSynsets);
		}
		if (partOfSpeech.startsWith("R")) {
			if (advLexicon.containsKey(word)
					&& advLexicon.get(word).size() > sense)
				return getSynsetDepth(advLexicon.get(word).get(sense),
						advSynsets);
		}
		return MAX_DEPTH;
	}

	public void findShosrtestPath(String word1, String partOfSpeech1,
			String word2, String partOfSpeech2, Map<String, Object> mapResults) {

		if (partOfSpeech1.charAt(0) != partOfSpeech2.charAt(0)) {
			mapResults.put(PATH_SHORTEST, new Integer(Integer.MAX_VALUE));
			return;
		}

		char pos = partOfSpeech1.charAt(0);

		switch (pos) {
		case 'N':
			// System.out.println("Word1: " + word1 + ", " + "Word2: " + word2);
			findShortestPath(nounLexicon.get(word1), nounLexicon.get(word2),
					nounSynsets, mapResults);
			break;
		case 'V':
			findShortestPath(verbLexicon.get(word1), verbLexicon.get(word2),
					verbSynsets, mapResults);
			break;
		case 'J':
			findShortestPath(adjLexicon.get(word1), adjLexicon.get(word2),
					adjSynsets, mapResults);
			break;
		case 'R':
			findShortestPath(advLexicon.get(word1), advLexicon.get(word2),
					advSynsets, mapResults);
			break;
		default:
			mapResults.put(PATH_SHORTEST, new Integer(Integer.MAX_VALUE));
			break;
		}
	}

	public void findMaxInformationContent(String word1, String partOfSpeech1,
			String word2, String partOfSpeech2, Map<String, Object> mapResults) {

		if (partOfSpeech1.charAt(0) != partOfSpeech2.charAt(0)) {
			mapResults.put(IC_LCS, new Double(Double.MIN_VALUE));
			return;
		}

		char pos = partOfSpeech1.charAt(0);

		switch (pos) {
		case 'N':
			findMaxInformationContent(nounLexicon.get(word1), nounLexicon
					.get(word2), nounSynsets, nountInfoContent, mapResults);
			break;
		case 'V':
			findMaxInformationContent(verbLexicon.get(word1), verbLexicon
					.get(word2), verbSynsets, verbInfoConctent, mapResults);
			break;
		default:
			mapResults.put(IC_LCS, new Double(Double.MIN_VALUE));
			break;
		}
	}

	/**
	 * @param synsets1
	 * @param mapSynsets
	 * @param synset2
	 * @param result
	 */
	private void findShortestPath(List<String> synsets1, List<String> synsets2,
			Map<String, SynsetNode> mapSynsets, Map<String, Object> mapResults) {
		mapResults.put(PATH_SHORTEST, new Integer(Integer.MAX_VALUE));
		for (String s1 : synsets1) {
			for (String s2 : synsets2) {
				Map<String, Object> results = new HashMap<String, Object>();

				if (s1.equals(s2)) {
					mapResults.put(PATH_SHORTEST, 0);
					mapResults.put(PATH_N1, 0);
					mapResults.put(PATH_N2, 0);
					mapResults.put(PATH_LCS, s1);
				}

				findShortestPath(s1, s2, mapSynsets, results);
				if ((Integer) results.get(PATH_SHORTEST) < (Integer) mapResults
						.get(PATH_SHORTEST)) {
					mapResults.put(PATH_SHORTEST, results.get(PATH_SHORTEST));
					mapResults.put(PATH_N1, results.get(PATH_N1));
					mapResults.put(PATH_N2, results.get(PATH_N2));
					mapResults.put(PATH_LCS, results.get(PATH_LCS));
				}
			}
		}
	}

	private void findMaxInformationContent(List<String> synsets1,
			List<String> synsets2, Map<String, SynsetNode> mapSynsets,
			Map<String, Double> mapInfoContent, Map<String, Object> mapResults) {
		mapResults.put(IC_LCS, new Double(Double.MIN_VALUE));
		for (String s1 : synsets1) {
			for (String s2 : synsets2) {
				Map<String, Object> results = new HashMap<String, Object>();
				findMaxInformationContent(s1, s2, mapSynsets, mapInfoContent,
						results);
				if ((Double) results.get(IC_LCS) > (Double) mapResults
						.get(IC_LCS)) {
					mapResults.put(IC_LCS, results.get(IC_LCS));
					mapResults.put(IC_SYNSET1, results.get(IC_SYNSET1));
					mapResults.put(IC_SYNSET2, results.get(IC_SYNSET2));
				}
			}
		}
	}

	/**
	 * @param synset1
	 * @param synset2
	 * @param relation
	 */
	private void findShortestPath(String synset1, String synset2,
			Map<String, SynsetNode> mapSynsets, Map<String, Object> mapResults) {
		int level = 0;
		Map<String, Integer> parents1 = new HashMap<String, Integer>();
		getParents(synset1, mapSynsets, level, parents1);
		parents1.put(synset1, 0);

		level = 0;
		Map<String, Integer> parents2 = new HashMap<String, Integer>();
		getParents(synset2, mapSynsets, level, parents2);
		parents2.put(synset2, 0);

		findShortestPath(parents1, parents2, mapResults);
	}

	private void findMaxInformationContent(String synset1, String synset2,
			Map<String, SynsetNode> mapSynsets,
			Map<String, Double> mapInfoContent, Map<String, Object> mapResults) {
		int level = 0;
		Map<String, Integer> parents1 = new HashMap<String, Integer>();
		getParents(synset1, mapSynsets, level, parents1);

		level = 0;
		Map<String, Integer> parents2 = new HashMap<String, Integer>();
		getParents(synset2, mapSynsets, level, parents2);

		findMaxInformationContent(parents1, parents2, mapInfoContent,
				mapResults);
		if ((Double) mapResults.get(IC_LCS) > Double.MIN_VALUE) {
			mapResults.put(IC_SYNSET1, mapInfoContent.get(synset1));
			mapResults.put(IC_SYNSET2, mapInfoContent.get(synset2));
		}
	}

	/**
	 * @param parents1
	 * @param parents2
	 */
	private void findShortestPath(Map<String, Integer> parents1,
			Map<String, Integer> parents2, Map<String, Object> mapResults) {
		int l = Integer.MAX_VALUE;
		for (String p1 : parents1.keySet()) {
			for (String p2 : parents2.keySet()) {
				if (p1.equals(p2)) {
					int n = parents1.get(p1) + parents2.get(p2);
					if (l > n) {
						l = n;
						mapResults.put(PATH_LCS, new String(p1));
						mapResults.put(PATH_N1, new Integer(parents1.get(p1)));
						mapResults.put(PATH_N2, new Integer(parents2.get(p2)));
						mapResults.put(PATH_SHORTEST, new Integer(l));
					}
				}
			}
		}
		if (l == Integer.MAX_VALUE) {
			mapResults.put(PATH_SHORTEST, new Integer(l));
		}
	}

	private void findMaxInformationContent(Map<String, Integer> parents1,
			Map<String, Integer> parents2, Map<String, Double> mapInfoContent,
			Map<String, Object> mapResults) {
		double maxIc = Double.MIN_VALUE;
		for (String p1 : parents1.keySet()) {
			if (p1.equals("0000000000"))
				continue;
			for (String p2 : parents2.keySet()) {
				if (p2.equals("0000000000"))
					continue;
				if (p1.equals(p2)) {
					if (maxIc < mapInfoContent.get(p1)) {
						maxIc = mapInfoContent.get(p1);
						mapResults.put(IC_LCS, new Double(maxIc));
					}
				}
			}
		}
		if (maxIc == Double.MIN_VALUE) {
			mapResults.put(IC_LCS, new Double(Double.MIN_VALUE));
		}
	}

	/**
	 * @param synset
	 * @param mapSynsets
	 * @param level
	 * @param parents
	 * @return
	 */
	private void getParents(String synset, Map<String, SynsetNode> mapSynsets,
			int level, Map<String, Integer> parents) {
		if (level > MAX_DEPTH)
			return;

		SynsetNode node = mapSynsets.get(synset);

		level++;
		for (String pSynset : node.hyperIds) {
			if (parents.containsKey(pSynset)) {
				if (level < parents.get(pSynset))
					parents.put(pSynset, level);
			} else {
				parents.put(pSynset, level);
			}
			getParents(pSynset, mapSynsets, level, parents);
		}
		level--;
	}

	public ArrayList<String> getNounHypernyms(String word, int level) {
		if (isWordNetNoun(word) == false) {
			return null;
		}

		MAX_DEPTH = level;

		ArrayList<String> hypernyms = new ArrayList<String>();

		List<String> synsets = nounLexicon.get(word);

		for (String synset : synsets) {
			Map<String, Integer> parents = new HashMap<String, Integer>();
			getParents(synset, nounSynsets, 0, parents);
			if (parents != null && parents.size() > 0)
				for (String parent : parents.keySet()) {
					List<String> words = nounSynsets.get(parent).words;
					hypernyms.addAll(words);
				}
		}
		return hypernyms;
	}

	public double getLinSimilarity(String word1, String partOfSpeech1,
			String word2, String partOfSpeech2) {
		if (partOfSpeech1.charAt(0) != partOfSpeech2.charAt(0)) {
			return 0.0;
		}

		double sim = 0.0;
		Map<String, Object> mapResults = new HashMap<String, Object>();
		findMaxInformationContent(word1, partOfSpeech1, word2, partOfSpeech2,
				mapResults);
		if ((Double) mapResults.get(IC_LCS) > Double.MIN_VALUE) {
			System.out
					.println(IC_LCS + " : " + (Double) mapResults.get(IC_LCS));
			System.out.println(IC_SYNSET1 + " : "
					+ (Double) mapResults.get(IC_SYNSET1));
			System.out.println(IC_SYNSET2 + " : "
					+ (Double) mapResults.get(IC_SYNSET2));
			sim = (2 * (Double) mapResults.get(IC_LCS))
					/ ((Double) mapResults.get(IC_SYNSET1) + (Double) mapResults
							.get(IC_SYNSET2));
		} else {
			sim = 0.0;
		}

		return sim;
	}

	public double getWupSimilarity(String word1, String partOfSpeech1,
			String word2, String partOfSpeech2) {
		if (partOfSpeech1.charAt(0) != partOfSpeech2.charAt(0)) {
			return 0.0;
		}
		if (partOfSpeech1.charAt(0) != 'N' && partOfSpeech1.charAt(0) != 'V') {
			return 0.0;
		}

		double sim = 0.0;
		Map<String, Object> mapResults = new HashMap<String, Object>();
		findShosrtestPath(word1, partOfSpeech1, word2, partOfSpeech2,
				mapResults);
		if ((Integer) mapResults.get(PATH_SHORTEST) < Integer.MAX_VALUE) {
			String synset = (String) mapResults.get(PATH_LCS);
			int lcsDepth = 0;
			if (partOfSpeech1.charAt(0) == 'N')
				lcsDepth = getSynsetDepth(synset, nounSynsets);
			else
				lcsDepth = getSynsetDepth(synset, verbSynsets);

			// System.out.println(mapResults.get(WNSim.PATH_SHORTEST));
			// System.out.println(mapResults.get(WNSim.PATH_N1));
			// System.out.println(mapResults.get(WNSim.PATH_N2));
			// System.out.println(mapResults.get(WNSim.PATH_LCS));
			// System.out.println("lcsDepth: " + lcsDepth);

			sim = (2.0 * lcsDepth)
					/ ((Integer) mapResults.get(PATH_SHORTEST) + (2.0 * lcsDepth));
		}
		return sim;
	}

	/**
	 * @param word
	 * @return
	 */
	public boolean isWordNetNoun(String word) {
		if (nounLexicon.containsKey(word))
			return true;
		return false;
	}

	public static void main(String[] args) throws IOException {
		String pathWordNet = "../WordNet-3.0";
		String icFile = "./ic-bnc-resnik-add1.dat";

		/**
		 * We should only use WNSim (or WordNet to be exact) to compute word
		 * similarity between Nouns, not Verbs. The hierarchy structures of
		 * Verbs is very shallow and contains multiple roots.
		 */

		WNSim wnsim = WNSim.getInstance(pathWordNet);
		// WNSim.initilizeInformationContent(icFile);

		String word1 = "";
		String word2 = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter two nouns (_ to end): ");
		word1 = in.readLine();
		if (word1.equals("_"))
			return;
		word2 = in.readLine();

		do {
			double sim = wnsim.getWupSimilarity(word1, "NN", word2, "NN");
			System.out.println("Similarity: " + sim);
			System.out.print("Enter two nouns (_ to end): ");
			word1 = in.readLine();
			if (word1.equals("_"))
				return;
			word2 = in.readLine();
		} while (!word2.equals("_"));

	}

}
