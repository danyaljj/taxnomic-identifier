/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;

import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;

/**
 * @author dxquang Jun 18, 2009
 */
public class MainRelationIdentification {

	public static final String CONFIG_FILE = "config.txt";

	public String PMI_FILE = "./pmi_value.txt";

	public String INDEX_DIR = "./pages_xml_indexed_jupiter_category";

	public String CATEGORY_MAPPING = "./pages_xml_indexed_jupiter_category.map";

	public String TITLE_MAPPING = "./bigramTitleMapping.txt";

	public static FeatureExtraction fex = null;

	public static RelationClassifier classifier = null;

	public static double largestPMI = -1.0;

	private static CacheManager cacheManager = null;

	private static Cache conceptCache = null;

	private static Cache privateConceptCache = null;

	private boolean usePrivateConceptCache = false;

	public HashMap<String, String> someArbitraryFunc(HashMap<String, String> mapNames) throws Exception {
		HashMap <String, String> newMap = new HashMap <String, String> ();
		newMap.put("SCORE", mapNames.get("FIRST_STRING"));
		newMap.put("REASON", mapNames.get("SECOND_STRING"));
		return newMap;
	}
	
	/**
	 * 
	 */
	public MainRelationIdentification(String indexDir, String categoryMapping,
			String titleMapping, int K) throws Exception {

		fex = new FeatureExtraction(indexDir, categoryMapping, titleMapping, K);

		classifier = new RelationClassifier();

		largestPMI = getLargestPMI(PMI_FILE);

		cacheManager = new CacheManager();

		conceptCache = cacheManager.getCache("conceptCache");
	}

	public MainRelationIdentification(String indexDir, String categoryMapping,
			String titleMapping, boolean usePrivateConceptCatche, int K)
			throws Exception {

		fex = new FeatureExtraction(indexDir, categoryMapping, titleMapping, K);

		classifier = new RelationClassifier();

		largestPMI = getLargestPMI(PMI_FILE);

		cacheManager = new CacheManager();

		this.usePrivateConceptCache = usePrivateConceptCatche;

		if (usePrivateConceptCatche == true)
			privateConceptCache = cacheManager.getCache("privateConceptCache");
		else
			conceptCache = cacheManager.getCache("conceptCache");

	}

	public MainRelationIdentification() throws Exception {

		PMI_FILE = "";
		INDEX_DIR = "";
		CATEGORY_MAPPING = "";
		TITLE_MAPPING = "";
		readConfigFile(CONFIG_FILE);
		if (PMI_FILE.length() == 0) {
			System.out
					.println("PMI_FILE = ? Pelase set it up in the config file.");
			System.exit(1);
		} else if (INDEX_DIR.length() == 0) {
			System.out
					.println("INDEX_DIR = ? Pelase set it up in the config file.");
			System.exit(1);
		} else if (CATEGORY_MAPPING.length() == 0) {
			System.out
					.println("CATEGORY_MAPPING = ? Pelase set it up in the config file.");
			System.exit(1);
		} else if (TITLE_MAPPING.length() == 0) {
			System.out
					.println("TITLE_MAPPING = ? Pelase set it up in the config file.");
			System.exit(1);
		}

		int K = 2;

		System.out.println("\n");
		System.out.println("IndexDir: " + INDEX_DIR);
		System.out.println("Category: " + CATEGORY_MAPPING);
		System.out.println("Title: " + TITLE_MAPPING);

		if (fex == null) {
			System.out.println("Constructing the feature extractor.");
			fex = new FeatureExtraction(INDEX_DIR, CATEGORY_MAPPING, TITLE_MAPPING, K);
		}

		if (classifier == null) {
			System.out.println("Constructing the classifier.");
			classifier = new RelationClassifier();
		}

		if (largestPMI == -1.0) {
			System.out.println("Reading the largest PMI value.");
			largestPMI = getLargestPMI(PMI_FILE);
		}
		
		if (cacheManager == null) {
			//cacheManager = new CacheManager();
			cacheManager = CacheManager.newInstance();
			conceptCache = cacheManager.getCache("conceptCache");
		}

	}

	private void readConfigFile(String configFile) {
		ArrayList<String> arrLines = IOManager.readLines(configFile);

		for (String line : arrLines) {
			if (line.startsWith("PMI_FILE")) {
				PMI_FILE = getValue(line);
			} else if (line.startsWith("INDEX_DIR")) {
				INDEX_DIR = getValue(line);
			} else if (line.startsWith("CATEGORY_MAPPING")) {
				CATEGORY_MAPPING = getValue(line);
			} else if (line.startsWith("TITLE_MAPPING")) {
				TITLE_MAPPING = getValue(line);
			}

		}
	}

	private String getValue(String line) {
		int pos = line.indexOf('=');
		if (pos == -1) {
			System.out.println("ERROR: Wrong format line: " + line);
			System.exit(1);
		}
		String value = line.substring(pos + 1);
		value = value.trim();
		if (value.length() == 0) {
			System.out.println("ERROR: There is no value: " + line);
			System.exit(1);
		}
		return value;
	}

	private double getLargestPMI(String pmiFile) {
		double pmiValue = 0.0;
		ArrayList<String> arrLines = IOManager.readLines(pmiFile);
		String pmi = arrLines.get(0);
		pmi = pmi.trim();
		pmiValue = Double.parseDouble(pmi);
		return pmiValue;
	}

	public int identifyPair(String concept1, String concept2) throws Exception {
		int relation = Constants.NONE;

		// 1. Extract features
		Instance ins = new Instance(concept1, concept2);
		fex.featureExtraction(ins);
		ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		System.out.println(ins.ratio_TtlCat + "\t" + ins.ratio_CatTtl + "\t"
				+ ins.ratio_CatCat);

		// 2. Classify
		System.out.println("ins : " + ins);
		String prediction = classifier.discreteValue(ins);

		relation = Integer.parseInt(prediction);

		return relation;
	}

	public HashMap<String, String> identify(HashMap<String, String> mapNames) throws Exception {
		HashMap<String, String> mapResults = identifyConcepts(mapNames.get("FIRST_STRING"), mapNames.get("SECOND_STRING"));
		return mapResults;
	}

	public HashMap<String, String> identifyConcepts(String concept1, String concept2) throws Exception {

		System.out.println(concept1 + " Vs. " + concept2);

		if (concept1.equals(concept2)) {
			String relString = "Exact match.";

			HashMap<String, String> mapResults = new HashMap<String, String>();
			mapResults.put("SCORE", "1.000");
			mapResults.put("REASON", relString);
			mapResults.put("RELATION", Integer.toString(Constants.ANCESTOR_E1_TO_E2));
			System.out.println(relString);

			return mapResults;
		}

//		String keyCache = concept1 + "____" + concept2;
//		System.out.println("conceptCache: " + conceptCache);
//		if (conceptCache.isKeyInCache((String) keyCache)) {
//			Element element = conceptCache.get((String) keyCache);
//			String output = (String) element.getObjectValue();
//			String[] parts = output.split("____");
//
//			HashMap<String, String> mapResults = new HashMap<String, String>();
//			mapResults.put("SCORE", parts[0]);
//			mapResults.put("REASON", parts[1]);
//
//			System.out.println("From concept cache.");
//			System.out.println(parts[0]);
//
//			return mapResults;
//		}

		// 1. Extract features
		Instance ins = new Instance(concept1, concept2);
		fex.featureExtraction(ins);
//		System.out.println("unscaled scorePmi_E1E2: " + ins.scorePmi_E1E2);
		ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
//		System.out.println("scaled scorePmi_E1E2: " + ins.scorePmi_E1E2);
//		System.out.println(ins.ratio_TtlCat + "\t" + ins.ratio_CatTtl + "\t" + ins.ratio_CatCat);
//		System.out.println(ins.scoreCos_AbsAbs + "\t" + ins.scoreCos_AbsCat + "\t" + ins.scoreCos_CatAbs + "\t" + ins.scoreCos_CatCat);

		// 2. Classify
//		System.out.println("classifier: " + classifier);
//		String prediction = classifier.discreteValue(ins);
//		double score = ins.scorePmi_E1E2;
//		ScoreSet scoreSet = classifier.scores(ins);
//		double score = scoreSet.get(prediction);
//		int relation = Integer.parseInt(prediction);
		// 3. Convert
		String relString = "";
		
		int relation;
		if (ins.ratio_CatCat > ins.ratio_CatTtl && ins.ratio_CatCat > ins.ratio_TtlCat) {
			if (ins.scoreCos_AbsCat > ins.scoreCos_CatAbs) relation = Constants.ANCESTOR_E1_TO_E2;
			else relation = Constants.ANCESTOR_E2_TO_E1;
		} else if (ins.ratio_CatCat < ins.ratio_CatTtl || ins.ratio_CatCat < ins.ratio_TtlCat) {
			relation = Constants.COUSIN;
		} else {
			relation = Constants.NONE;
		}
		

		if (relation == Constants.NONE) {
			relString = "No relation.";
		} else if (relation == Constants.ANCESTOR_E1_TO_E2) {
			relString = "\"" + concept1 + "\" is an ancestor of \"" + concept2 + "\"";
		} else if (relation == Constants.ANCESTOR_E2_TO_E1) {
			relString = "\"" + concept1 + "\" is a child of \"" + concept2 + "\"";
		} else if (relation == Constants.COUSIN) {
			relString = "Sibling.";
		} else {
			relString = "Relation: " + relation;
		}

		String scoreString = Double.toString(ins.scorePmi_E1E2);
		HashMap<String, String> mapResults = new HashMap<String, String>();
		mapResults.put("SCORE", scoreString);
		mapResults.put("REASON", relString);
		mapResults.put("RELATION", Integer.toString(relation));

//		Element element = new Element(keyCache, relString + "____" + scoreString);
//		conceptCache.put(element);
//		conceptCache.flush();

//		System.out.println(relString);

		return mapResults;

	}

	public HashMap<String, Object> privateIdentifyConcepts(String concept1,
			String concept2) throws Exception {

		/**
		 * Caching
		 */
		/*
		 * if (privateConceptCache == null) {
		 * System.out.println("ERROR: privateConceptCache = null"); throw new
		 * Exception("privateConceptCache = null"); }
		 */

		System.out.println("\n" + concept1 + " Vs. " + concept2);

		if (concept1.equals(concept2)) {
			int relation = 4;

			HashMap<String, Object> mapResults = new HashMap<String, Object>();
			mapResults.put("RELATION", (Integer) new Integer(relation));
			mapResults.put("SCORE", (Double) new Double(10000));
			mapResults.put("L0", 0.0);
			mapResults.put("L1", 0.0);
			mapResults.put("L2", 0.0);
			mapResults.put("L3", 0.0);

			System.out.println("Exact match");
			System.out.println(relation);
			System.out.println("10000");

			return mapResults;
		}

		/**
		 * Caching
		 */
		/*
		 * String keyCache = concept1 + "____" + concept2; if
		 * (privateConceptCache.isKeyInCache((String) keyCache)) { Element
		 * element = privateConceptCache.get((String) keyCache); String output =
		 * (String) element.getObjectValue(); String[] parts =
		 * output.split("____");
		 * 
		 * HashMap<String, Object> mapResults = new HashMap<String, Object>();
		 * mapResults.put("RELATION", (Integer) new Integer(Integer
		 * .parseInt(parts[0]))); mapResults.put("SCORE", (Double) new
		 * Double(Double .parseDouble(parts[1])));
		 * 
		 * System.out.println("From concept cache.");
		 * System.out.println(parts[0]); System.out.println(parts[1]);
		 * 
		 * return mapResults; }
		 */

		// 1. Extract features
		Instance ins = new Instance(concept1, concept2);
		fex.featureExtraction(ins);
		ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		System.out.println(ins.ratio_TtlCat + "\t" + ins.ratio_CatTtl + "\t"
				+ ins.ratio_CatCat);

		// 2. Classify
		String prediction = classifier.discreteValue(ins);
		ScoreSet scoreSet = classifier.scores(ins);
		double score = scoreSet.get(prediction);

		double score0 = scoreSet.get("0");
		double score1 = scoreSet.get("1");
		double score2 = scoreSet.get("2");
		double score3 = scoreSet.get("3");

		int relation = Integer.parseInt(prediction);

		// String scoreString = Double.toString(score);

		HashMap<String, Object> mapResults = new HashMap<String, Object>();
		mapResults.put("RELATION", (Integer) new Integer(relation));
		mapResults.put("SCORE", (Double) new Double(score));
		mapResults.put("L0", (Double) new Double(score0));
		mapResults.put("L1", (Double) new Double(score1));
		mapResults.put("L2", (Double) new Double(score2));
		mapResults.put("L3", (Double) new Double(score3));

		/**
		 * Caching
		 */
		/*
		 * Element element = new Element(keyCache, Integer.toString(relation) +
		 * "____" + scoreString); privateConceptCache.put(element);
		 * privateConceptCache.flush();
		 */

		// System.out.println("Prediction.");
		// System.out.println(relation);
		// System.out.println(score);
		return mapResults;

	}
}
