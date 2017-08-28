/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javatools.datatypes.Pair;
import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.FeatureExtraction;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Dec 3, 2009
 */
public class MainConstraintRelationIdentification {

	public static final String CONFIG_FILE = "config.txt";

	public String PMI_FILE = "pmi_value.txt";

	public String INDEX_DIR = "/scratch/quangdo2/pages_xml_indexed_jupiter_category";

	public String CATEGORY_MAPPING = "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map";

	public String TITLE_MAPPING = "/scratch/quangdo2/bigramTitleMapping.txt";

	public static FeatureExtraction fex = null;

	public static RelationClassifier classifier = null;

	public static double largestPMI = -1.0;

	private static CacheManager cacheManager = null;

	private static Cache constraintConceptCache = null;
	private static Cache relatedConceptCache = null;

	public static final boolean USE_RELATEDCONCEPTCACHE = true;

	private RelatedConcepts relatedExtractor = null;

	private int maxAnc = 4;

	private int maxSib = 5;

	private int maxChi = 4;

	private static final int NUM_CLASS = 4;

	private String CONSTRAINT_FILE = "./www10Results/constraints_forward_K3_train_gold.555.txt";

	private static String[] invalidCombinationRankedForward = new String[] {
			"1_3_3", "3_1_3", "3_2_3", "0_2_3", "3_2_0", "1_0_3", "2_2_2",
			"2_0_3", "1_3_0", "1_3_1", "3_0_0", "2_3_3", "2_1_0", "1_1_0",
			"0_3_3", "0_3_2", "3_3_1", "2_3_0", "3_3_0", "3_0_3" };

	private Set<String> setInvalidCombinations = new HashSet<String>();

	private Map<String, Double> mapConstraintProbs = new HashMap<String, Double>();
	private Map<String, Double> mapConstraintLogProbs = new HashMap<String, Double>();
	private Map<String, Double> mapConstraintNegLogProbs = new HashMap<String, Double>();

	private String CONSTRAINTS_WEIGHT = "constraints.weight.txt";

	private String IDF_FILE = "";

	/**
	 * 
	 */
	public MainConstraintRelationIdentification() throws Exception {

		PMI_FILE = "";
		INDEX_DIR = "";
		CATEGORY_MAPPING = "";
		TITLE_MAPPING = "";
		CONSTRAINT_FILE = "";
		CONSTRAINTS_WEIGHT = "";
		IDF_FILE = "";
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
		} else if (CONSTRAINT_FILE.length() == 0) {
			System.out
					.println("CONSTRAINT_FILE = ? Pelase set it up in the config file.");
			System.exit(1);
		} else if (CONSTRAINTS_WEIGHT.length() == 0) {
			System.out
					.println("CONSTRAINTS_WEIGHT = ? Pelase set it up in the config file.");
			System.exit(1);
		} else if (IDF_FILE.length() == 0) {
			System.out
					.println("IDF_FILE = ? Pelase set it up in the config file.");
			System.exit(1);
		}
		int K = 3;

		System.out.println("\n");
		System.out.println("IndexDir: " + INDEX_DIR);
		System.out.println("Category: " + CATEGORY_MAPPING);
		System.out.println("Title: " + TITLE_MAPPING);

		if (fex == null) {
			System.out.println("Constructing the feature extractor.");
			fex = new FeatureExtraction(INDEX_DIR, CATEGORY_MAPPING,
					TITLE_MAPPING, IDF_FILE, K);
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
			cacheManager = new CacheManager();
			constraintConceptCache = cacheManager
					.getCache("constraintConceptCache");
			if (USE_RELATEDCONCEPTCACHE == true) {
				relatedConceptCache = cacheManager
						.getCache("relatedConceptCache");
			}
			// cacheManager.clearAll();
		}

		relatedExtractor = new RelatedConcepts();

		System.out.println("Loading constraints in " + CONSTRAINT_FILE);
		loadConstraints(CONSTRAINT_FILE);

		System.out.println("Loading constraint weights in "
				+ CONSTRAINTS_WEIGHT);
		loadConstraintsWeight();

	}

	/**
	 * @param configFile
	 */
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
			} else if (line.startsWith("CONSTRAINT_FILE")) {
				CONSTRAINT_FILE = getValue(line);
			} else if (line.startsWith("CONSTRAINTS_WEIGHT")) {
				CONSTRAINTS_WEIGHT = getValue(line);
			} else if (line.startsWith("IDF_FILE")) {
				IDF_FILE = getValue(line);
			}

		}

	}

	/**
	 * @param line
	 * @return
	 */
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

	public void loadConstraintsWeight() throws NumberFormatException,
			IOException {
		BufferedReader reader = IOManager.openReader(CONSTRAINTS_WEIGHT);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length != 4)
				continue;

			String prob = chunks[0].trim();
			String logProb = chunks[1].trim();
			String negLogProb = chunks[2].trim();

			String combination = chunks[3].trim();

			mapConstraintProbs.put(combination, Double.parseDouble(prob));
			mapConstraintLogProbs.put(combination, Double.parseDouble(logProb));
			mapConstraintNegLogProbs.put(combination, Double
					.parseDouble(negLogProb));

		}

		IOManager.closeReader(reader);
	}

	public void loadConstraints(String fileName) {

		ArrayList<String> arrLines = IOManager.readLines(fileName);

		ArrayList<String> arrConstraints = new ArrayList<String>();

		for (String line : arrLines) {

			line = line.trim();

			if (line.length() != 5)
				continue;
			if (line.charAt(1) != '_' && line.charAt(3) != '_')
				continue;

			arrConstraints.add(line);
		}

		int size = arrConstraints.size();
		invalidCombinationRankedForward = new String[size];

		for (int i = 0; i < size; i++) {
			invalidCombinationRankedForward[i] = arrConstraints.get(i);
		}
	}

	private double getLargestPMI(String pmiFile) {
		double pmiValue = 0.0;
		ArrayList<String> arrLines = IOManager.readLines(pmiFile);
		String pmi = arrLines.get(0);
		pmi = pmi.trim();
		pmiValue = Double.parseDouble(pmi);
		return pmiValue;
	}

	public HashMap<String, String> identify(HashMap<String, String> mapNames)
			throws Exception {
		HashMap<String, String> mapResults = identifyConcepts(mapNames
				.get("FIRST_STRING"), mapNames.get("SECOND_STRING"));
		return mapResults;
	}

	public HashMap<String, String> identifyConcepts(String concept1,
			String concept2) throws Exception {

		System.out.println("\n" + concept1 + " Vs. " + concept2);

		if (concept1.equals(concept2)) {
			String relString = "Exact match.";

			HashMap<String, String> mapResults = new HashMap<String, String>();
			mapResults.put("SCORE", relString);
			mapResults.put("REASON", "100000");

			System.out.println(relString);

			return mapResults;
		}

		String keyCache = concept1 + "____" + concept2;
		if (constraintConceptCache.isKeyInCache((String) keyCache)) {
			Element element = constraintConceptCache.get((String) keyCache);
			String output = (String) element.getObjectValue();
			String[] parts = output.split("____");

			HashMap<String, String> mapResults = new HashMap<String, String>();
			mapResults.put("SCORE", parts[0]);
			mapResults.put("REASON", parts[1]);

			System.out.println("From concept cache.");
			System.out.println(parts[1]);

			return mapResults;
		}

		// 1. Extract features
		System.out.println("Extracting features for two input concepts.");
		Instance ins = new Instance(concept1, concept2);
		fex.featureExtraction(ins);
		ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		System.out.println(ins.ratio_TtlCat + "\t" + ins.ratio_CatTtl + "\t"
				+ ins.ratio_CatCat);

		// 2. Extract related concepts
		System.out.println("Extracting related concepts.");
		relatedExtractor.getRelatedConcepts(concept1, concept2, false);
		ArrayList<String> arrAncConcept1 = getAncestorList(maxAnc);
		ArrayList<String> arrSibConcept1 = getSiblingList(maxSib);
		ArrayList<String> arrChiConcept1 = getChildList(maxChi);
		System.out.print("Anc1:\t");
		for (String s : arrAncConcept1) {
			System.out.print(s + "\t");
		}
		System.out.println();
		System.out.print("Sib1:\t");
		for (String s : arrSibConcept1) {
			System.out.print(s + "\t");
		}
		System.out.println();
		System.out.print("Chi1:\t");
		for (String s : arrChiConcept1) {
			System.out.print(s + "\t");
		}
		System.out.println();

		relatedExtractor.getRelatedConcepts(concept2, concept1, false);
		ArrayList<String> arrAncConcept2 = getAncestorList(maxAnc);
		ArrayList<String> arrSibConcept2 = getSiblingList(maxSib);
		ArrayList<String> arrChiConcept2 = getChildList(maxChi);
		System.out.print("Anc2:\t");
		for (String s : arrAncConcept2) {
			System.out.print(s + "\t");
		}
		System.out.println();
		System.out.print("Sib2:\t");
		for (String s : arrSibConcept2) {
			System.out.print(s + "\t");
		}
		System.out.println();
		System.out.print("Chi2:\t");
		for (String s : arrChiConcept2) {
			System.out.print(s + "\t");
		}
		System.out.println();

		// 3. Extract features for related concepts
		System.out.println("Extracting features for related concepts");
		HashMap<String, Double[]> mapRelatedConceptFromCache = new HashMap<String, Double[]>();

		ArrayList<Instance> arrSupportingInstances = extractFeatures4SupportingInstances(
				concept1, concept2, arrAncConcept1, arrSibConcept1,
				arrChiConcept1, arrAncConcept2, arrSibConcept2, arrChiConcept2,
				mapRelatedConceptFromCache);

		// 4. Classify related concepts
		System.out
				.println("Classifying related concepts (supporting instances).");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(arrSupportingInstances);

		// ----------------------------------
		// Adding into related concept cache
		if (USE_RELATEDCONCEPTCACHE == true) {
			Set<String> keySet = mapSupportingPrediction.keySet();
			for (String keyRelatedCache : keySet) {
				Double[] scores = mapSupportingPrediction.get(keyRelatedCache);
				Element element = new Element(keyRelatedCache, scores[0]
						+ "___" + scores[1] + "___" + scores[2] + "___"
						+ scores[3] + "___" + scores[4]);
				relatedConceptCache.put(element);
			}
			relatedConceptCache.flush();
		}
		// ----------------------------------

		Set<String> setSupportConcepts = supportingConceptSet(arrAncConcept1,
				arrSibConcept1, arrChiConcept1, arrAncConcept2, arrSibConcept2,
				arrChiConcept2);

		// 5. Apply constraint
		System.out.println("Classifying the instances.");
		String prediction = classifyOriginalInstances(ins, setSupportConcepts,
				mapSupportingPrediction, maxAnc, maxSib, maxChi, false);

		// 6. Convert
		System.out.println("Converting result: " + prediction);
		String relString = "";
		int relation = Integer.parseInt(prediction);

		if (relation == Constants.NONE) {
			relString = "No relation.";
		} else if (relation == Constants.ANCESTOR_E1_TO_E2) {
			relString = "\"" + concept1 + "\" is an ancestor of \"" + concept2
					+ "\"";
		} else if (relation == Constants.ANCESTOR_E2_TO_E1) {
			relString = "\"" + concept1 + "\" is a child of \"" + concept2
					+ "\"";
		} else if (relation == Constants.COUSIN) {
			relString = "Sibling.";
		} else {
			relString = "Relation: " + relation;
		}

		HashMap<String, String> mapResults = new HashMap<String, String>();
		mapResults.put("SCORE", prediction);
		mapResults.put("REASON", relString);

		Element element = new Element(keyCache, prediction + "____" + relString);
		constraintConceptCache.put(element);
		constraintConceptCache.flush();

		System.out.println(relString);

		return mapResults;

	}

	/**
	 * @param arrAncConcept1
	 * @param arrSibConcept1
	 * @param arrChiConcept1
	 * @param arrAncConcept2
	 * @param arrSibConcept2
	 * @param arrChiConcept2
	 * @return
	 */
	private Set<String> supportingConceptSet(ArrayList<String> arrAncConcept1,
			ArrayList<String> arrSibConcept1, ArrayList<String> arrChiConcept1,
			ArrayList<String> arrAncConcept2, ArrayList<String> arrSibConcept2,
			ArrayList<String> arrChiConcept2) {

		HashSet<String> concepts = new HashSet<String>();

		for (String concept : arrAncConcept1) {
			concepts.add(concept);
		}

		for (String concept : arrSibConcept1) {
			concepts.add(concept);
		}

		for (String concept : arrChiConcept1) {
			concepts.add(concept);
		}

		for (String concept : arrAncConcept2) {
			concepts.add(concept);
		}

		for (String concept : arrSibConcept2) {
			concepts.add(concept);
		}

		for (String concept : arrChiConcept2) {
			concepts.add(concept);
		}

		return concepts;
	}

	private String classifyOriginalInstances(Instance ins,
			Set<String> setSupportConcepts,
			Map<String, Double[]> mapSupportingPrediction, int maxAnc,
			int maxSib, int maxChi, boolean debug) {

		ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;

		Softmax sm = new Softmax();

		if (debug == true) {
			System.out.print("Set of supporting concepts ("
					+ setSupportConcepts.size() + "): ");
			for (String s : setSupportConcepts) {
				System.out.println(s + ", ");
			}
		}

		Double[] scoreXYs = classifyConceptPair(ins, sm);

		if (debug == true) {
			System.out.println("Predicted label: " + scoreXYs[4]);
			System.out.println("0: " + scoreXYs[0]);
			System.out.println("1: " + scoreXYs[1]);
			System.out.println("2: " + scoreXYs[2]);
			System.out.println("3: " + scoreXYs[3]);
		}

		String relation = inference(scoreXYs, ins.entity1, ins.entity2,
				mapSupportingPrediction, setSupportConcepts, debug);

		return relation;
	}

	private String inference(Double[] scoreXYs, String concept1,
			String concept2, Map<String, Double[]> mapSupportingPrediction,
			Set<String> setSupportConcepts, boolean debug) {

		ArrayList<Double[]> arrScores = new ArrayList<Double[]>();
		arrScores.add(scoreXYs);

		for (String supportConcept : setSupportConcepts) {

			if (debug == true) {
				System.out.println("\t*Support concept: " + supportConcept);
			}

			String key1 = concept1 + "___" + supportConcept;
			String key2 = concept2 + "___" + supportConcept;

			if (!mapSupportingPrediction.containsKey(key1)
					|| !mapSupportingPrediction.containsKey(key2)) {
				if (debug == true)
					System.out
							.println("\t--No information for at least on supporting concept.");
				continue;
			}

			Double[] scoreXZs = mapSupportingPrediction.get(key1);
			Double[] scoreYZs = mapSupportingPrediction.get(key2);

			if (scoreXZs[4] == 0.0 && scoreYZs[4] == 0.0) {
				if (debug == true) {
					System.out
							.println("\t--Both predicted classes are No Relation.");
				}
				continue;
			}

			if (debug == true) {
				System.out.println("\t\t" + key1);
				for (int k = 0; k < scoreXZs.length; k++)
					System.out.println("\t\t> " + k + ":" + scoreXZs[k]);
				System.out.println("\t\t" + key2);
				for (int l = 0; l < scoreYZs.length; l++)
					System.out.println("\t\t> " + l + ":" + scoreYZs[l]);
			}

			arrScores.add(scoreXZs);
			arrScores.add(scoreYZs);

		}

		String res;
		if (arrScores.size() > 1) {
			if (debug)
				System.out.println("Solving constraint satisfaction problem.");
			res = solve(arrScores, debug);
			if (debug) {
				System.out.println("######");
				System.out.println("Decision: " + res);
			}
		} else {
			if (debug)
				System.out.println("No constraints usage.");
			res = ((Long) Math.round(scoreXYs[4])).toString();
		}

		return res;

	}

	/**
	 * @param arrScores
	 * @param debug
	 */
	private String solve(ArrayList<Double[]> arrScores, boolean debug) {

		Double[] scoreXYs = arrScores.get(0);

		ArrayList<Pair<Double[], String[]>> arrBestScores = new ArrayList<Pair<Double[], String[]>>();

		int n = arrScores.size();
		int i = 1;
		while (i < n) {

			Double[] scoreXZs = arrScores.get(i++);
			Double[] scoreYZs = arrScores.get(i++);

			Pair<Double[], String[]> bestScores = bestScoreEachLabel(scoreXYs,
					scoreXZs, scoreYZs, debug);
			arrBestScores.add(bestScores);

		}

		String res = bestLabel(arrBestScores, scoreXYs, debug);

		return res;

	}

	/**
	 * @param arrBestScores
	 * @param scoreXYs
	 * @param debug
	 * @return
	 */
	private String bestLabel(ArrayList<Pair<Double[], String[]>> arrBestScores,
			Double[] scoreXYs, boolean debug) {

		Double[] bestScores = new Double[NUM_CLASS];
		int[] freqs = new int[NUM_CLASS];

		for (int i = 0; i < NUM_CLASS; i++) {
			bestScores[i] = 0.0;
			freqs[i] = 0;
		}

		for (Pair<Double[], String[]> pair : arrBestScores) {
			Double[] scores = pair.first;
			if (debug)
				System.out.println("###");
			for (int i = 0; i < NUM_CLASS; i++) {
				if (debug)
					System.out.println(i + ": " + scores[i]);
				if (scores[i] == 0.0)
					continue;
				bestScores[i] += scores[i];
				freqs[i]++;
			}
		}

		if (debug)
			System.out.println("#######Best scores");
		for (int i = 0; i < NUM_CLASS; i++) {
			// double score = scoreXYs[i];
			// bestScores[i] -= (n - 1) * score;
			if (freqs[i] == 0)
				bestScores[i] = -1000000.0;
			else
				bestScores[i] /= (double) freqs[i];
			if (debug)
				System.out.println(i + ": " + bestScores[i]);
		}

		double best = -1000000;
		String label = ((Long) Math.round(scoreXYs[4])).toString();
		for (int i = 0; i < NUM_CLASS; i++) {
			if (bestScores[i] > best) {
				best = bestScores[i];
				label = Integer.toString(i);
			}
		}

		return label;
	}

	/**
	 * @param scoreXYs
	 * @param scoreXZs
	 * @param scoreYZs
	 * @return
	 */
	private Pair<Double[], String[]> bestScoreEachLabel(Double[] scoreXYs,
			Double[] scoreXZs, Double[] scoreYZs, boolean debug) {

		Double[] bestScores = new Double[NUM_CLASS];
		String[] bestTriangles = new String[NUM_CLASS];

		for (int i = 0; i < NUM_CLASS; i++) {
			double scoreXY = scoreXYs[i];
			double bestScore = -1000000;
			String bestTriangle = "";
			for (int j = 0; j < NUM_CLASS; j++) {
				double scoreXZ = scoreXZs[j];
				for (int k = 0; k < NUM_CLASS; k++) {
					double scoreYZ = scoreYZs[k];
					double score = scoreXY + scoreXZ + scoreYZ;
					String triangle = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					if (setInvalidCombinations.contains(triangle)) {
						// score -= mapConstraintNegLogProbs.get(triangle);
						continue;
					}
					if (score > bestScore) {
						bestScore = score;
						bestTriangle = triangle;
					}
				}
			}

			bestScores[i] = bestScore;
			bestTriangles[i] = bestTriangle;
		}

		winnerTakeAll(bestScores);

		if (debug) {
			System.out.println("=====");
			for (int i = 0; i < NUM_CLASS; i++) {
				System.out.println(bestTriangles[i] + ": " + bestScores[i]);
			}
		}

		for (int i = 0; i < NUM_CLASS; i++) {
			if (bestScores[i] > 0.0)
				bestScores[i] *= mapConstraintProbs.get(bestTriangles[i]);
		}

		Pair<Double[], String[]> resPair = new Pair<Double[], String[]>(
				bestScores, bestTriangles);
		return resPair;
	}

	/**
	 * @param bestScores
	 */
	private void winnerTakeAll(Double[] bestScores) {
		int index = 0;
		Double best = bestScores[index];
		for (int i = 0; i < NUM_CLASS; i++) {
			if (best < bestScores[i]) {
				best = bestScores[i];
				index = i;
			}
		}
		for (int i = 0; i < NUM_CLASS; i++) {
			if (i != index)
				bestScores[i] = 0.0;
		}
	}

	private Map<String, Double[]> classifySupportingInstances(
			ArrayList<Instance> arrSupportingInstances) {

		Map<String, Double[]> mapResults = new HashMap<String, Double[]>();

		for (Instance ins : arrSupportingInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		Softmax sm = new Softmax();
		for (Instance ins : arrSupportingInstances) {

			String key = ins.entity1 + "___" + ins.entity2;

			if (mapResults.containsKey(key))
				continue;

			Double[] scores = classifyConceptPair(ins, sm);

			mapResults.put(key, scores);

		}

		return mapResults;

	}

	private Double[] classifyConceptPair(Instance ins, Softmax sm) {

		Double[] classifierScore = new Double[5];

		String predLabel = classifier.discreteValue(ins);
		ScoreSet scoreSet = classifier.scores(ins);

		sm.normalize(scoreSet);

		classifierScore[0] = scoreSet.get("0");
		classifierScore[1] = scoreSet.get("1");
		classifierScore[2] = scoreSet.get("2");
		classifierScore[3] = scoreSet.get("3");
		classifierScore[4] = Double.parseDouble(predLabel);

		return classifierScore;

	}

	/**
	 * @param conceptX
	 * @param conceptY
	 * @param arrAncConcept1
	 * @param arrSibConcept1
	 * @param arrChiConcept1
	 * @param arrAncConcept2
	 * @param arrSibConcept2
	 * @param arrChiConcept2
	 * @return
	 */
	private ArrayList<Instance> extractFeatures4SupportingInstances(
			String conceptX, String conceptY, ArrayList<String> arrAncConcept1,
			ArrayList<String> arrSibConcept1, ArrayList<String> arrChiConcept1,
			ArrayList<String> arrAncConcept2, ArrayList<String> arrSibConcept2,
			ArrayList<String> arrChiConcept2,
			HashMap<String, Double[]> mapRelatedConceptFromCache) {

		String[] ancXs = getConceptArray(arrAncConcept1);
		String[] sibXs = getConceptArray(arrSibConcept1);
		String[] chiXs = getConceptArray(arrChiConcept1);
		String[] ancYs = getConceptArray(arrAncConcept2);
		String[] sibYs = getConceptArray(arrSibConcept2);
		String[] chiYs = getConceptArray(arrChiConcept2);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		Set<Instance> setInstances = new HashSet<Instance>();

		ArrayList<Instance> arrNewInstances = null;

		arrNewInstances = extractFeatures(fex, conceptX, ancXs, maxAnc,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptX, sibXs, maxSib,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptX, chiXs, maxChi,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, ancXs, maxAnc,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, sibXs, maxSib,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, chiXs, maxChi,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);

		arrNewInstances = extractFeatures(fex, conceptX, ancYs, maxAnc,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptX, sibYs, maxSib,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptX, chiYs, maxChi,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, ancYs, maxAnc,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, sibYs, maxSib,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		arrNewInstances = extractFeatures(fex, conceptY, chiYs, maxChi,
				setInstances, mapRelatedConceptFromCache);
		arrOutputInstances.addAll(arrNewInstances);
		return arrOutputInstances;
	}

	/**
	 * @param arrConcept
	 * @return
	 */
	private String[] getConceptArray(ArrayList<String> arrConcept) {
		int n = arrConcept.size();
		String[] concepts = new String[n];

		for (int i = 0; i < n; i++) {
			concepts[i] = arrConcept.get(i);
		}

		return concepts;
	}

	private ArrayList<Instance> extractFeatures(FeatureExtraction fex,
			String concept, String[] relatedConcepts, int maxConcept,
			Set<Instance> setInstances,
			HashMap<String, Double[]> mapRelatedConceptFromCache) {

		// System.out.println(setInstances.size());
		// System.out.println("Concept: " + concept);
		// System.out.print("Related concepts: ");

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();

		int n = relatedConcepts.length;
		int i = 0;
		int count = 0;
		while (count < maxConcept && i < n) {

			String s = relatedConcepts[i];

			if (s.equals("_")) {
				i++;
				continue;
			}

			Instance ins = new Instance(concept, s);
			ins.entityClass = "_|_";

			// System.out.println(ins.toString());

			if (setInstances.contains(ins)) {
				// System.out.println("hello ins: " + ins.entity1 + " \t " +
				// ins.entity2);
				i++;
				count++;
				continue;
			}

			setInstances.add(ins);

			// ---------------------------------------------------------------
			// Check and use cache for related concepts
			if (USE_RELATEDCONCEPTCACHE == true) {
				String keyCache = ins.entity1 + "___" + ins.entity2;
				if (mapRelatedConceptFromCache.containsKey(keyCache))
					continue;

				if (relatedConceptCache.isKeyInCache((String) keyCache)) {
					Element element = relatedConceptCache
							.get((String) keyCache);
					String output = (String) element.getObjectValue();
					String[] parts = output.split("___");

					Double[] scores = new Double[parts.length];
					for (int j = 0; j < parts.length; j++) {
						String p = parts[j];
						scores[j] = Double.parseDouble(p);
					}

					mapRelatedConceptFromCache.put(keyCache, scores);
					continue;
				}
			}
			// ---------------------------------------------------------------

			try {
				fex.featureExtraction(ins);
			} catch (Exception e) {
				System.out.println("ERROR: Unable to extract features.");
				e.printStackTrace();
				System.exit(1);
			}
			arrInstance.add(ins);

			// System.out.print(s + " | ");

			count++;
			i++;
		}

		// System.out.println();

		return arrInstance;
	}

	/**
	 * @param maxChi
	 * @return
	 */
	private ArrayList<String> getChildList(int maxChi) {
		ArrayList<String> arrChildren = new ArrayList<String>();

		String ancConcept = relatedExtractor.getChildString(maxChi);

		if (ancConcept.equals("_")) {
			return arrChildren;
		} else {
			String[] concepts = ancConcept.split("_");
			for (String c : concepts) {
				arrChildren.add(c);
			}
		}

		return arrChildren;
	}

	/**
	 * @param maxSib
	 * @return
	 */
	private ArrayList<String> getSiblingList(int maxSib) {
		ArrayList<String> arrSiblings = new ArrayList<String>();

		String ancConcept = relatedExtractor.getSiblingString(maxSib);

		if (ancConcept.equals("_")) {
			return arrSiblings;
		} else {
			String[] concepts = ancConcept.split("_");
			for (String c : concepts) {
				arrSiblings.add(c);
			}
		}

		return arrSiblings;
	}

	/**
	 * @param maxAnc
	 * @return
	 */
	private ArrayList<String> getAncestorList(int maxAnc) {
		ArrayList<String> arrAncestors = new ArrayList<String>();

		String ancConcept = relatedExtractor.getAncestorString(maxAnc);

		if (ancConcept.equals("_")) {
			return arrAncestors;
		} else {
			String[] concepts = ancConcept.split("_");
			for (String c : concepts) {
				arrAncestors.add(c);
			}
		}

		return arrAncestors;
	}
}
