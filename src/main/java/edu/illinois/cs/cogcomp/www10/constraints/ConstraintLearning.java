/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Oct 17, 2009
 */
public class ConstraintLearning {

	public static final String PMI_FILE = "pmi_value.txt";

	public static final int NUM_CLASS = 4;

	Map<String, Integer> mapCombinationFreq = new HashMap<String, Integer>();

	/**
	 * 
	 */
	public ConstraintLearning() {

		for (int i = 0; i < NUM_CLASS; i++) {
			for (int j = 0; j < NUM_CLASS; j++) {
				for (int k = 0; k < NUM_CLASS; k++) {
					String combination = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					mapCombinationFreq.put(combination, 0);
				}
			}
		}

	}

	public void contraintLearning(String interTrainFile,
			String interSupportFile, int maxAnc, int maxSib, int maxChi)
			throws Exception {

		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportInstances = DataHandler
				.readTestingInstances(interSupportFile,
						Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);
		System.out.println("Number of supporting instance: "
				+ arrSupportInstances.size());

		RelationClassifier localClassifier = new RelationClassifier();

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportPrediction = classifySupportInstances(
				arrSupportInstances, localClassifier);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interTrainFile,
						Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);

		System.out.println("Learning contraints.");
		constraintLearning(arrInstances, mapSupportPrediction, localClassifier,
				maxAnc, maxSib, maxChi);

	}

	/**
	 * @param arrInstances
	 * @param mapSupportingPrediction
	 * @param localClassifier
	 */
	private void constraintLearning(ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction,
			RelationClassifier localClassifier, int maxAnc, int maxSib,
			int maxChi) {

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		// Softmax sm = new Softmax();
		int n = arrInstances.size();

		int total = 0;

		for (int i = 0; i < n; i++) {
			Instance ins = arrInstances.get(i);
			System.out.println(ins.toString());

			String line = ins.textLine;

			String[] parts = line.split("\\t+");

			String ancX = parts[12];
			String[] ancXs = ancX.split("_");
			String sibX = parts[13];
			String[] sibXs = sibX.split("_");
			String chiX = parts[14];
			String[] chiXs = chiX.split("_");

			String ancY = parts[15];
			String[] ancYs = ancY.split("_");
			String sibY = parts[16];
			String[] sibYs = sibY.split("_");
			String chiY = parts[17];
			String[] chiYs = chiY.split("_");

			Set<String> setSupportingConcepts = new HashSet<String>();

			getSupportingConcept(maxAnc, ancXs, setSupportingConcepts);
			getSupportingConcept(maxAnc, ancYs, setSupportingConcepts);
			getSupportingConcept(maxSib, sibXs, setSupportingConcepts);
			getSupportingConcept(maxSib, sibYs, setSupportingConcepts);
			getSupportingConcept(maxChi, chiXs, setSupportingConcepts);
			getSupportingConcept(maxChi, chiYs, setSupportingConcepts);

			// Double[] scoreXYs = new Double[4];
			// String orgLabel = localClassifier.discreteValue(ins);
			// ScoreSet scoreSet = localClassifier.scores(ins);
			// sm.normalize(scoreSet);
			// scoreXYs[0] = scoreSet.get("0");
			// scoreXYs[1] = scoreSet.get("1");
			// scoreXYs[2] = scoreSet.get("2");
			// scoreXYs[3] = scoreSet.get("3");
			// System.out.println("Predicted label: " + orgLabel);
			// System.out.println("0: " + scoreXYs[0]);
			// System.out.println("1: " + scoreXYs[1]);
			// System.out.println("2: " + scoreXYs[2]);
			// System.out.println("3: " + scoreXYs[3]);

			for (String z : setSupportingConcepts) {

				System.out.println("\tConcept Z: " + z);

				String key = ins.entity1 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreXZs = mapSupportingPrediction.get(key);
				int relationXZ = getMaxRelation(scoreXZs);

				key = ins.entity2 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreYZs = mapSupportingPrediction.get(key);
				int relationYZ = getMaxRelation(scoreYZs);

				String combination = Integer.toString(ins.relation) + "_"
						+ Integer.toString(relationXZ) + "_"
						+ Integer.toString(relationYZ);

				int value = mapCombinationFreq.get(combination);
				value++;
				mapCombinationFreq.put(combination, value);

				total++;
			}
		}

		// Output to std output
		Set<String> setCombinations = mapCombinationFreq.keySet();

		for (String com : setCombinations) {
			int freq = mapCombinationFreq.get(com);
			if (freq == 0) {
				System.out.println(((double) 1 / (double)total) + "\t" + (Math.log((double)1/(double)total)) + "\t" + (-Math.log((double) 1 / (double) total)) + "\t"
						+ com);
			} else {
				System.out.println(((double) freq / (double)total) + "\t" + (Math.log((double) freq / (double)total)) + "\t" + (-Math.log((double) freq / (double) total)) + "\t"
						+ com);
			}
		}

	}

	/**
	 * @param scores
	 * @return
	 */
	private int getMaxRelation(Double[] scores) {

		if (scores == null || scores.length == 0) {
			System.out.println("ERROR: input is null or empty");
			System.exit(1);
		}

		int relation = 0;
		double maxScore = scores[0];
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > maxScore) {
				maxScore = scores[i];
				relation = i;
			}
		}

		return relation;
	}

	private void getSupportingConcept(int maxAnc, String[] concepts,
			Set<String> setSupportingConcepts) {
		int n = concepts.length;
		int i = 0;
		int count = 0;
		while (i < n && count < maxAnc) {
			if (setSupportingConcepts.contains(concepts[i])) {
				i++;
				continue;
			}
			setSupportingConcepts.add(concepts[i]);
			i++;
			count++;
		}
	}

	/**
	 * @param arrSupportInstances
	 * @param localClassifier
	 * @return
	 */
	private Map<String, Double[]> classifySupportInstances(
			ArrayList<Instance> arrSupportInstances,
			RelationClassifier localClassifier) {

		Map<String, Double[]> mapResults = new HashMap<String, Double[]>();

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrSupportInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		Softmax sm = new Softmax();
		for (Instance ins : arrSupportInstances) {

			String key = ins.entity1 + "___" + ins.entity2;

			if (mapResults.containsKey(key))
				continue;

			Double[] scores = new Double[4];
			ScoreSet scoreSet = localClassifier.scores(ins);
			sm.normalize(scoreSet);
			scores[0] = scoreSet.get("0");
			scores[1] = scoreSet.get("1");
			scores[2] = scoreSet.get("2");
			scores[3] = scoreSet.get("3");

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

}
