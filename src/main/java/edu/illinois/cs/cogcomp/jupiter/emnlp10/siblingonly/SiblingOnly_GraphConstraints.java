/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10.siblingonly;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.datatypes.Pair;
import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.acl10.experiments.RecallPrecisionFscore;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.www10.constraints.LabelAccuracy;

/**
 * @author dxquang Oct 22, 2009
 */
public class SiblingOnly_GraphConstraints {

	private static String[] invalidCombinationRankedForward = new String[] {
			"1_3_3", "3_1_3", "3_2_3", "0_2_3", "3_2_0", "1_0_3", "2_2_2",
			"2_0_3", "1_3_0", "1_3_1", "3_0_0", "2_3_3", "2_1_0", "1_1_0",
			"0_3_3", "0_3_2", "3_3_1", "2_3_0", "3_3_0", "3_0_3" };

	private Set<String> setInvalidCombinations = new HashSet<String>();

	public static final int NUM_CLASS = 2;

	public static final String PMI_FILE = "pmi_value.txt";

	public static final String CONSTRAINTS_WEIGHT = "constraints.weight.txt";

	private Map<String, Double> mapConstraintProbs = new HashMap<String, Double>();
	private Map<String, Double> mapConstraintLogProbs = new HashMap<String, Double>();
	private Map<String, Double> mapConstraintNegLogProbs = new HashMap<String, Double>();

	private RelationClassifier localClassifier = null;

	private double largestPMI;

	public Map<String, LabelAccuracy> mapAccuracy = new HashMap<String, LabelAccuracy>();

	public SiblingOnly_GraphConstraints(String constraintFile) throws Exception {

		loadConstraints(constraintFile);

		for (String key : invalidCombinationRankedForward) {
			setInvalidCombinations.add(key);
		}

		System.out.println("Number of invalid combinations: "
				+ setInvalidCombinations.size());

		localClassifier = new RelationClassifier();

		largestPMI = getLargestPMI(PMI_FILE);
		System.out.println("Largest PMI = " + largestPMI);

		loadConstraintsWeight();

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

	public double evaluateWithGraphConstraints(
			ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction, int maxAnc,
			int maxSib, int maxChi, boolean debug) throws Exception {

		// Classify original instances
		System.out.println("Classifying the instances: " + arrInstances.size());
		double result = classifyOriginalInstances(arrInstances,
				mapSupportingPrediction, maxAnc, maxSib, maxChi, debug);

		return result;

	}

	// This function was added in ACL10
	public Map<String, Double> evaluateWithGraphConstraintsRecallPrecisionFscore(
			ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction, int maxAnc,
			int maxSib, int maxChi, boolean debug) throws Exception {

		// Classify original instances
		System.out.println("Classifying the instances: " + arrInstances.size());
		Map<String, Double> result = classifyOriginalInstancesRecallPrecisionFscore(
				arrInstances, mapSupportingPrediction, maxAnc, maxSib, maxChi,
				debug);

		return result;

	}

	public double evaluateWithGraphConstraints(String interFile,
			String supportingInterFile, String readMode, int maxAnc,
			int maxSib, int maxChi, boolean debug) throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportingInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(arrSupportingInstances);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE, mode);

		// Classify original instances
		System.out.println("Classifying the instances.");
		double result = classifyOriginalInstances(arrInstances,
				mapSupportingPrediction, maxAnc, maxSib, maxChi, debug);

		return result;

	}

	/**
	 * @param arrInstances
	 * @param mapSupportingPrediction
	 * @param maxAnc
	 * @param maxSib
	 * @param maxChi
	 * @param debug
	 * @return
	 */
	private double classifyOriginalInstances(ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction, int maxAnc,
			int maxSib, int maxChi, boolean debug) {

		for (Instance ins : arrInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();
		int count = 0;
		int n = arrInstances.size();
		Softmax sm = new Softmax();

		for (Instance ins : arrInstances) {

			String line = ins.textLine;
			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			if (debug == true) {
				System.out.println("--------");
				System.out.println("*ConceptX: " + conceptX);
				System.out.println("*ConceptY: " + conceptY);
			}

			Set<String> setSupportConcepts = new HashSet<String>();

			getSupportConcepts(setSupportConcepts, line, maxAnc, maxSib, maxChi);

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

			int p = Integer.parseInt(relation);
			String orgLabel = ((Long) Math.round(scoreXYs[4])).toString();

			if (p == ins.relation) {

				String out = "T" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				truePrediction.add(out);
				count++;

				if (debug == true) {
					System.out.println(out);
				}

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				falsePrediction.add(out);

				if (debug == true) {
					System.out.println(out);
				}

				addToLabelAccuracy(ins, false);
			}

		}

		if (debug == true) {
			for (String out : truePrediction)
				System.out.println(out);
			for (String out : falsePrediction)
				System.out.println(out);

			printMapAccuracy();

			System.out.println("Correct: " + count);
			System.out.println("Total: " + n);
		}

		double acc = 0.0;
		if (arrInstances.size() > 0)
			acc = (double) count / (double) n;

		System.out.println("Acc: " + acc);

		return acc;
	}

	// This function was added in ACL10
	private Map<String, Double> classifyOriginalInstancesRecallPrecisionFscore(
			ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction, int maxAnc,
			int maxSib, int maxChi, boolean debug) {

		Map<String, Double> results = new HashMap<String, Double>();
		List<String> listOut = new ArrayList<String>();

		for (Instance ins : arrInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();
		int count = 0;
		int n = arrInstances.size();
		Softmax sm = new Softmax();

		for (Instance ins : arrInstances) {

			String line = ins.textLine;
			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			if (debug == true) {
				System.out.println("--------");
				System.out.println("*ConceptX: " + conceptX);
				System.out.println("*ConceptY: " + conceptY);
			}

			Set<String> setSupportConcepts = new HashSet<String>();

			getSupportConcepts(setSupportConcepts, line, maxAnc, maxSib, maxChi);

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
				// System.out.println("1: " + scoreXYs[1]);
				// System.out.println("2: " + scoreXYs[2]);
				System.out.println("3: " + scoreXYs[3]);
			}

			String relation = inference(scoreXYs, ins.entity1, ins.entity2,
					mapSupportingPrediction, setSupportConcepts, debug);

			int p = Integer.parseInt(relation);
			String orgLabel = ((Long) Math.round(scoreXYs[4])).toString();

			if (p == ins.relation) {

				String out = "T" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				truePrediction.add(out);
				count++;

				if (debug == true) {
					System.out.println(out);
				}

				listOut.add(out);

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				falsePrediction.add(out);

				if (debug == true) {
					System.out.println(out);
				}

				listOut.add(out);

				addToLabelAccuracy(ins, false);
			}

		}

		if (debug == true) {
			for (String out : truePrediction)
				System.out.println(out);
			for (String out : falsePrediction)
				System.out.println(out);

			printMapAccuracy();

			System.out.println("Correct: " + count);
			System.out.println("Total: " + n);
		}

		double acc = 0.0;
		if (arrInstances.size() > 0)
			acc = (double) count / (double) n;

		System.out.println("Acc: " + acc);

		results.put("Acc", acc);

		Map<String, Double> prf = RecallPrecisionFscore
				.getResultsInference(listOut);
		results.putAll(prf);

		return results;
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

		int n = arrBestScores.size();
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

	/**
	 * @param ins
	 * @param sm
	 * @return
	 */
	private Double[] classifyConceptPair(Instance ins, Softmax sm) {

		Double[] classifierScore = new Double[5];

		String predLabel = localClassifier.discreteValue(ins);
		ScoreSet scoreSet = localClassifier.scores(ins);

		sm.normalize(scoreSet);

		classifierScore[0] = scoreSet.get("0");
		// classifierScore[1] = scoreSet.get("1");
		// classifierScore[2] = scoreSet.get("2");
		classifierScore[3] = scoreSet.get("3");
		classifierScore[4] = Double.parseDouble(predLabel);

		return classifierScore;

	}

	private void getSupportConcepts(Set<String> setSupportConcepts,
			String line, int maxAnc, int maxSib, int maxChi) {

		String[] parts = line.split("\\t+");

		String[] ancXs = parts[12].split("_");
		String[] sibXs = parts[13].split("_");
		String[] chiXs = parts[14].split("_");
		String[] ancYs = parts[15].split("_");
		String[] sibYs = parts[16].split("_");
		String[] chiYs = parts[17].split("_");

		doGetSupporConcepts(maxAnc, ancXs, setSupportConcepts);
		doGetSupporConcepts(maxAnc, ancYs, setSupportConcepts);
		doGetSupporConcepts(maxSib, sibXs, setSupportConcepts);
		doGetSupporConcepts(maxSib, sibYs, setSupportConcepts);
		doGetSupporConcepts(maxChi, chiXs, setSupportConcepts);
		doGetSupporConcepts(maxChi, chiYs, setSupportConcepts);

	}

	/**
	 * @param maxAnc
	 * @param concepts
	 * @param setSupportConcepts
	 */
	private void doGetSupporConcepts(int maxAnc, String[] concepts,
			Set<String> setSupportConcepts) {
		int n = concepts.length;
		int i = 0;
		int count = 0;
		while (i < n && count < maxAnc) {
			if (setSupportConcepts.contains(concepts[i])) {
				i++;
				continue;
			}
			setSupportConcepts.add(concepts[i]);
			i++;
			count++;
		}
	}

	/**
	 * @param arrSupportingInstances
	 * @return
	 */
	public Map<String, Double[]> classifySupportingInstances(
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

	/**
	 * @param pmiFile
	 * @return
	 */
	private double getLargestPMI(String pmiFile) {

		double pmiValue = 0.0;
		ArrayList<String> arrLines = IOManager.readLines(pmiFile);
		String pmi = arrLines.get(0);
		pmi = pmi.trim();
		pmiValue = Double.parseDouble(pmi);
		return pmiValue;

	}

	public double getPenaltyFromGraphConstraints(
			ArrayList<String> arrTriangleGraphs, boolean hardConstraint) {

		double penalty = 0.0;

		for (String triangle : arrTriangleGraphs) {
			if (setInvalidCombinations.contains(triangle)) {
				penalty += mapConstraintNegLogProbs.get(triangle);
			}
		}

		if (hardConstraint == true) {
			if (penalty > 0.0)
				return 1000000;
			else
				return 0.0;
		} else {
			return penalty;
		}
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

	private void addToLabelAccuracy(Instance ins, boolean b) {

		String key = null;
		switch (ins.relation) {
		case Constants.ANCESTOR_E1_TO_E2:
		case Constants.ANCESTOR_E2_TO_E1:
		case Constants.COUSIN:
			key = ins.entityClass;
			break;
		}

		if (key != null) {
			addToMapAccuracy(ins, b, key);
		} else {
			String[] className = ins.entityClass.split("\\|");
			addToMapAccuracy(ins, b, className[0]);
			addToMapAccuracy(ins, b, className[1]);
		}

	}

	private void addToMapAccuracy(Instance ins, boolean b, String key) {
		if (mapAccuracy.containsKey(key)) {
			LabelAccuracy la = mapAccuracy.get(key);
			la.add(ins, b);
		} else {
			LabelAccuracy la = new LabelAccuracy();
			la.add(ins, b);
			mapAccuracy.put(key, la);
		}
	}

	/**
	 * 
	 */
	private void printMapAccuracy() {

		Set<String> keySet = mapAccuracy.keySet();

		for (String key : keySet) {
			System.out.println("| " + key + " | "
					+ mapAccuracy.get(key).toString());
		}
	}

}
