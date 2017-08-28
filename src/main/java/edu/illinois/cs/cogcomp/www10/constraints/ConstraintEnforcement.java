/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.cikm09.learning.MainRelationIdentification;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Oct 13, 2009
 */
public class ConstraintEnforcement {

	public static final String PMI_FILE = "pmi_value.txt";

	public static final String CONSTRAINTS_WEIGHT = "constraints.weight.txt";

	public Map<String, Double> mapConstraintProbs = new HashMap<String, Double>();
	public Map<String, Double> mapConstraintLogProbs = new HashMap<String, Double>();
	public Map<String, Double> mapConstraintNegLogProbs = new HashMap<String, Double>();

	public MainRelationIdentification mainClassifier = null;

	public final static String[] invalidCombinations = new String[] { "1_2_1",
			"1_2_3", "1_3_1", "1_3_2", "1_3_3", "2_1_2", "2_1_3", "2_2_3",
			"2_3_2", "2_3_3", "3_1_1", "3_1_2", "3_1_3", "3_2_1", "3_2_3",
			"3_3_1", "3_3_2", "3_3_0", "0_3_3", "3_0_3" };

	public final static String[] invalidCombinationsBackup = new String[] {
			"1_2_1", "1_2_3", "1_3_1", "1_3_2", "1_3_3", "2_1_2", "2_1_3",
			"2_2_3", "2_3_2", "2_3_3", "3_1_1", "3_1_2", "3_1_3", "3_2_1",
			"3_2_3", "3_3_1", "3_3_2", "3_3_0", "0_3_3", "3_0_3" };

	public final static String[] invalidCombinationRankedForward = new String[] {
			"1_3_3", "3_1_3", "3_2_3", "0_2_3", "3_2_0", "1_0_3", "2_2_2",
			"2_0_3", "1_3_0", "1_3_1", "3_0_0", "2_3_3", "2_1_0", "1_1_0",
			"0_3_3", "0_3_2", "3_3_1", "2_3_0", "3_3_0", "3_0_3" };

	public Set<String> setInvalidCombinations = new HashSet<String>();

	public Map<String, LabelAccuracy> mapAccuracy = new HashMap<String, LabelAccuracy>();

	public static final int NUM_CLASS = 4;

	/**
	 * 
	 */
	public ConstraintEnforcement() {
		for (String key : invalidCombinationRankedForward) {
			setInvalidCombinations.add(key);
		}
		System.out.println("Number of invalid combinations: "
				+ setInvalidCombinations.size());
	}

	/**
	 * @throws Exception
	 * 
	 */
	public ConstraintEnforcement(String indexDir, String categoryMapping,
			String titleMapping, int K) throws Exception {

		mainClassifier = new MainRelationIdentification(indexDir,
				categoryMapping, titleMapping, K);

		for (String key : invalidCombinations) {
			setInvalidCombinations.add(key);
		}
		System.out.println("Number of invalid combinations: "
				+ setInvalidCombinations.size());

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

	public int classifyWithConstraints(String conceptX, String conceptY,
			String conceptZ) throws Exception {

		HashMap<String, Object> mapResultXY = mainClassifier
				.privateIdentifyConcepts(conceptX, conceptY);
		HashMap<String, Object> mapResultXZ = mainClassifier
				.privateIdentifyConcepts(conceptX, conceptZ);
		HashMap<String, Object> mapResultYZ = mainClassifier
				.privateIdentifyConcepts(conceptY, conceptZ);

		System.out.println("Original classifying:");
		System.out.println("\t(" + conceptX + ", " + conceptY + ") = "
				+ mapResultXY.get("RELATION"));
		System.out.println("\t(" + conceptX + ", " + conceptZ + ") = "
				+ mapResultXZ.get("RELATION"));
		System.out.println("\t(" + conceptY + ", " + conceptZ + ") = "
				+ mapResultYZ.get("RELATION"));

		ArrayList<Double> arrSoftmaxXY = getSoftmaxScores(mapResultXY);
		ArrayList<Double> arrSoftmaxXZ = getSoftmaxScores(mapResultXZ);
		ArrayList<Double> arrSoftmaxYZ = getSoftmaxScores(mapResultYZ);

		// mapInferenceRsults contains such elements as ["3_1_2", 2.3],
		// ["3_0_0", 2.0], ...
		ArrayList<InferenceOutput> arrOutputs = bruteforthConstraintSatisfactionInference(
				arrSoftmaxXY, arrSoftmaxXZ, arrSoftmaxYZ);

		// for (int i = 0; i < arrOutputs.size(); i++) {
		// InferenceOutput inferenceOutput = arrOutputs.get(i);
		// System.out.println(inferenceOutput.key + " ("
		// + inferenceOutput.value + ")");
		// }

		if (arrOutputs != null && arrOutputs.size() > 0) {
			InferenceOutput output = arrOutputs.get(0);
			String key = output.key;
			return Integer.parseInt(key.substring(0, key.indexOf('_')));
		} else {
			return (Integer) mapResultXY.get("RELATION");
		}
	}

	/**
	 * @param arrSoftmaxXY
	 * @param arrSoftmaxXZ
	 * @param arrSoftmaxYZ
	 * @return
	 */
	private ArrayList<InferenceOutput> bruteforthConstraintSatisfactionInference(
			ArrayList<Double> arrSoftmaxXY, ArrayList<Double> arrSoftmaxXZ,
			ArrayList<Double> arrSoftmaxYZ) {

		ArrayList<InferenceOutput> arrOutputs = new ArrayList<InferenceOutput>();

		int sizeC1 = arrSoftmaxXY.size();
		int sizeC2 = arrSoftmaxXZ.size();
		int sizeC3 = arrSoftmaxYZ.size();

		// System.out.println("sizeC1: " + sizeC1);
		// System.out.println("sizeC2: " + sizeC2);
		// System.out.println("sizeC3: " + sizeC3);

		int n = 0;
		for (int i = 0; i < sizeC1; i++) {
			for (int j = 0; j < sizeC2; j++) {
				for (int k = 0; k < sizeC3; k++) {
					String key = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					if (!violateConstraints(key)) {
						// System.out.println(key + ": valid");
						double value = arrSoftmaxXY.get(i)
								+ arrSoftmaxXZ.get(j) + arrSoftmaxYZ.get(k);
						InferenceOutput output = new InferenceOutput(key, value);
						arrOutputs.add(output);
						n++;
					} else {
						// System.out.println(key + ": invalid");
					}
				}
			}
		}

		// System.out.println("Valid combinations: " + n);

		sortInferenceOutput(arrOutputs);

		return arrOutputs;
	}

	/**
	 * @param arrOutputs
	 */
	private void sortInferenceOutput(ArrayList<InferenceOutput> arrOutputs) {
		Collections.sort(arrOutputs, new Comparator<InferenceOutput>() {
			@Override
			public int compare(InferenceOutput o1, InferenceOutput o2) {
				if (o1.value < o2.value)
					return 1;
				else if (o1.value == o2.value)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param key
	 * @return
	 */
	private boolean violateConstraints(String key) {

		if (setInvalidCombinations.contains(key)) {
			return true;
		}

		return false;
	}

	/**
	 * @param mapResultXY
	 * @return
	 */
	public ArrayList<Double> getSoftmaxScores(HashMap<String, Object> mapResults) {

		ArrayList<Double> arrSoftmax = new ArrayList<Double>();

		Double scoreL0 = (Double) mapResults.get("L0");
		Double scoreL1 = (Double) mapResults.get("L1");
		Double scoreL2 = (Double) mapResults.get("L2");
		Double scoreL3 = (Double) mapResults.get("L3");

		Double scoreL0_exp = Math.exp(scoreL0);
		Double scoreL1_exp = Math.exp(scoreL1);
		Double scoreL2_exp = Math.exp(scoreL2);
		Double scoreL3_exp = Math.exp(scoreL3);

		Double totalScore = scoreL0_exp + scoreL1_exp + scoreL2_exp
				+ scoreL3_exp;

		Double scoreL0_sm = scoreL0_exp / totalScore;
		Double scoreL1_sm = scoreL1_exp / totalScore;
		Double scoreL2_sm = scoreL2_exp / totalScore;
		Double scoreL3_sm = scoreL3_exp / totalScore;

		arrSoftmax.add(scoreL0_sm);
		arrSoftmax.add(scoreL1_sm);
		arrSoftmax.add(scoreL2_sm);
		arrSoftmax.add(scoreL3_sm);

		return arrSoftmax;
	}

	public double testWithConstraints(String interFile,
			String supportingInterFile, int maxAnc, int maxSib, int maxChi,
			boolean debug) throws Exception {

		loadConstraintsWeight();

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportingInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		RelationClassifier localClassifier = new RelationClassifier();

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(
				arrSupportingInstances, localClassifier);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		// Classify original instances
		System.out.println("Classifying the instances.");
		double result = classifyOriginalInstances(arrInstances,
				mapSupportingPrediction, localClassifier, maxAnc, maxSib,
				maxChi, debug);

		return result;

	}

	/**
	 * @param arrInstances
	 * @param mapSupportingPrediction
	 * @param localClassifier
	 */
	private double classifyOriginalInstances(ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction,
			RelationClassifier localClassifier, int maxAnc, int maxSib,
			int maxChi, boolean debug) {

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();
		int count = 0;

		Softmax sm = new Softmax();
		int n = arrInstances.size();
		for (int i = 0; i < n; i++) {
			Instance ins = arrInstances.get(i);

			String line = ins.textLine;

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			if (debug == true) {
				System.out.println();
				System.out.println("ConceptX: " + conceptX);
				System.out.println("ConceptY: " + conceptY);
			}

			if (!ins.entity1.equals(conceptX) || !ins.entity2.equals(conceptY)) {
				System.out.println("ERROR: Different concepts.");
				System.out.println("ins.entity1: " + ins.entity1);
				System.out.println("ins.entity2: " + ins.entity2);
				System.out.println("conceptX: " + conceptX);
				System.out.println("conceptY: " + conceptY);
				System.exit(1);
			}

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

			Double[] scoreXYs = new Double[4];
			String orgLabel = localClassifier.discreteValue(ins);
			ScoreSet scoreSet = localClassifier.scores(ins);
			sm.normalize(scoreSet);
			scoreXYs[0] = scoreSet.get("0");
			scoreXYs[1] = scoreSet.get("1");
			scoreXYs[2] = scoreSet.get("2");
			scoreXYs[3] = scoreSet.get("3");

			if (debug == true) {
				System.out.println("Original label: " + orgLabel);
				System.out.println("0: " + scoreXYs[0]);
				System.out.println("1: " + scoreXYs[1]);
				System.out.println("2: " + scoreXYs[2]);
				System.out.println("3: " + scoreXYs[3]);
			}

			Map<String, Double> mapClassScore = new HashMap<String, Double>();
			Map<String, Integer> mapClassFreq = new HashMap<String, Integer>();

			for (String z : setSupportingConcepts) {

				if (debug == true) {
					System.out.println("\tConcept Z: " + z);
				}

				String key = ins.entity1 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreXZs = mapSupportingPrediction.get(key);

				if (debug == true) {
					System.out.println("\t*" + key);
					for (int k = 0; k < scoreXZs.length; k++) {
						System.out.println("\t\t" + k + ":" + scoreXZs[k]);
					}
				}

				key = ins.entity2 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreYZs = mapSupportingPrediction.get(key);

				if (debug == true) {
					System.out.println("\t*" + key);
					for (int k = 0; k < scoreYZs.length; k++) {
						System.out.println("\t\t" + k + ":" + scoreYZs[k]);
					}
				}

				if (scoreXZs[4] == 0.0 && scoreYZs[4] == 0.0) {
					if (debug == true) {
						System.out.println("\tBoth scores are 0.0");
					}
					continue;
				}

				ArrayList<InferenceOutput> arrInferenceOutputs = bruteforthConstraintSatisfactionInference(
						scoreXYs, scoreXZs, scoreYZs, debug);

				// Incorporate soft constraints
				for (InferenceOutput output : arrInferenceOutputs) {
					String s = output.key;
					output.value = output.value * mapConstraintProbs.get(s);
				}

				if (arrInferenceOutputs.size() > 0) {
					InferenceOutput output = arrInferenceOutputs.get(0);
					String combination = output.key;
					String relation = combination.substring(0, 1);
					double score = output.value;
					if (mapClassScore.containsKey(relation)) {
						double total = mapClassScore.get(relation) + score;
						mapClassScore.put(relation, total);
						int freq = mapClassFreq.get(relation) + 1;
						mapClassFreq.put(relation, freq);
					} else {
						mapClassScore.put(relation, score);
						mapClassFreq.put(relation, 1);
					}
					for (int k = 0; (k < arrInferenceOutputs.size() && k < 5); k++) {
						InferenceOutput inferenceOutput = arrInferenceOutputs
								.get(k);
						if (debug == true) {
							System.out.println(inferenceOutput.key + " ("
									+ inferenceOutput.value + ")");
						}
					}
				}

			}

			Set<String> setRelations = mapClassScore.keySet();

			for (String relation : setRelations) {
				int freq = mapClassFreq.get(relation);
				double score = mapClassScore.get(relation);
				double newScore = (score) / ((double) freq);
				mapClassScore.put(relation, newScore);

				if (debug == true) {
					System.out.println(relation + ": " + score + " " + freq
							+ " - " + newScore);
				}
			}

			String maxRelation = orgLabel;
			double maxScore = -100000;
			for (String relation : setRelations) {
				double score = mapClassScore.get(relation);
				if (score > maxScore) {
					maxRelation = relation;
					maxScore = score;
				}
			}

			int p = Integer.parseInt(maxRelation);

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

	/**
	 * @param softmaxXYs
	 * @param softmaxXZs
	 * @param softmaxYZs
	 * @return
	 */
	private ArrayList<InferenceOutput> bruteforthConstraintSatisfactionInference(
			Double[] softmaxXYs, Double[] softmaxXZs, Double[] softmaxYZs,
			boolean debug) {

		ArrayList<InferenceOutput> arrOutputs = new ArrayList<InferenceOutput>();

		int sizeC1 = 4; // softmaxXYs.length;
		int sizeC2 = 4; // softmaxXZs.length;
		int sizeC3 = 4; // softmaxYZs.length;

		int n = 0;
		for (int i = 0; i < sizeC1; i++) {
			for (int j = 0; j < sizeC2; j++) {
				for (int k = 0; k < sizeC3; k++) {
					String key = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					if (!violateConstraints(key)) {
						double value = softmaxXYs[i] + softmaxXZs[j]
								+ softmaxYZs[k];
						InferenceOutput output = new InferenceOutput(key, value);
						arrOutputs.add(output);
						n++;
						// if (debug == true) {
						// System.out.println("\t" + key + ": valid" + " ("
						// +
						// value + ")");
						// }
					} else {
						double value = softmaxXYs[i] + softmaxXZs[j]
								+ softmaxYZs[k];
						if (debug == true) {
							System.out.println("\t" + key + ": invalid" + " ("
									+ value + ")");
						}
					}
				}
			}
		}

		// System.out.println("Valid combinations: " + n);

		sortInferenceOutput(arrOutputs);

		return arrOutputs;
	}

	/**
	 * @param arrSupportingInstances
	 * @param localClassifier
	 * @return
	 */
	private Map<String, Double[]> classifySupportingInstances(
			ArrayList<Instance> arrSupportingInstances,
			RelationClassifier localClassifier) {

		Map<String, Double[]> mapResults = new HashMap<String, Double[]>();

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrSupportingInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		Softmax sm = new Softmax();
		for (Instance ins : arrSupportingInstances) {

			String key = ins.entity1 + "___" + ins.entity2;

			if (mapResults.containsKey(key))
				continue;

			Double[] scores = new Double[5];
			String orgLabel = localClassifier.discreteValue(ins);
			ScoreSet scoreSet = localClassifier.scores(ins);
			sm.normalize(scoreSet);
			scores[0] = scoreSet.get("0");
			scores[1] = scoreSet.get("1");
			scores[2] = scoreSet.get("2");
			scores[3] = scoreSet.get("3");
			scores[4] = Double.parseDouble(orgLabel);

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

	public void testWithSoftConstraints(String interFile,
			String supportingInterFile, int maxAnc, int maxSib, int maxChi)
			throws Exception {

		loadConstraintsWeight();

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportingInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		RelationClassifier localClassifier = new RelationClassifier();

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(
				arrSupportingInstances, localClassifier);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		// Classify original instances
		System.out.println("Classifying the instances.");
		classifyOriginalInstancesWithSoftConstraints(arrInstances,
				mapSupportingPrediction, localClassifier, maxAnc, maxSib,
				maxChi);

	}

	/**
	 * @param arrInstances
	 * @param mapSupportingPrediction
	 * @param localClassifier
	 * @param maxAnc
	 * @param maxSib
	 * @param maxChi
	 */
	private void classifyOriginalInstancesWithSoftConstraints(
			ArrayList<Instance> arrInstances,
			Map<String, Double[]> mapSupportingPrediction,
			RelationClassifier localClassifier, int maxAnc, int maxSib,
			int maxChi) {

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();
		int count = 0;

		Softmax sm = new Softmax();
		int n = arrInstances.size();
		for (int i = 0; i < n; i++) {
			Instance ins = arrInstances.get(i);

			String line = ins.textLine;

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			System.out.println();
			System.out.println("ConceptX: " + conceptX);
			System.out.println("ConceptY: " + conceptY);

			if (!ins.entity1.equals(conceptX) || !ins.entity2.equals(conceptY)) {
				System.out.println("ERROR: Different concepts.");
				System.out.println("ins.entity1: " + ins.entity1);
				System.out.println("ins.entity2: " + ins.entity2);
				System.out.println("conceptX: " + conceptX);
				System.out.println("conceptY: " + conceptY);
			}

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

			Double[] scoreXYs = new Double[4];
			String orgLabel = localClassifier.discreteValue(ins);
			ScoreSet scoreSet = localClassifier.scores(ins);
			sm.normalize(scoreSet);
			scoreXYs[0] = scoreSet.get("0");
			scoreXYs[1] = scoreSet.get("1");
			scoreXYs[2] = scoreSet.get("2");
			scoreXYs[3] = scoreSet.get("3");
			System.out.println("Original label: " + orgLabel);
			System.out.println("0: " + scoreXYs[0]);
			System.out.println("1: " + scoreXYs[1]);
			System.out.println("2: " + scoreXYs[2]);
			System.out.println("3: " + scoreXYs[3]);

			Map<String, Double> mapClassScore = new HashMap<String, Double>();
			Map<String, Integer> mapClassFreq = new HashMap<String, Integer>();

			for (String z : setSupportingConcepts) {

				System.out.println("\tConcept Z: " + z);
				String key = ins.entity1 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreXZs = mapSupportingPrediction.get(key);
				System.out.println("\t*" + key);
				for (int k = 0; k < scoreXZs.length; k++) {
					System.out.println("\t\t" + k + ":" + scoreXZs[k]);
				}

				key = ins.entity2 + "___" + z;
				if (!mapSupportingPrediction.containsKey(key)) {
					continue;
				}
				Double[] scoreYZs = mapSupportingPrediction.get(key);

				System.out.println("\t*" + key);
				for (int k = 0; k < scoreYZs.length; k++) {
					System.out.println("\t\t" + k + ":" + scoreYZs[k]);
				}

				if (scoreXZs[4] == 0.0 && scoreYZs[4] == 0.0)
					continue;

				ArrayList<InferenceOutput> arrInferenceOutputs = inferenceWithSoftConstraints(
						scoreXYs, scoreXZs, scoreYZs);

				if (arrInferenceOutputs.size() > 0) {
					InferenceOutput output = arrInferenceOutputs.get(0);
					String combination = output.key;
					String relation = combination.substring(0, 1);
					double score = output.value;
					if (mapClassScore.containsKey(relation)) {
						double total = mapClassScore.get(relation) + score;
						mapClassScore.put(relation, total);
						int freq = mapClassFreq.get(relation) + 1;
						mapClassFreq.put(relation, freq);
					} else {
						mapClassScore.put(relation, score);
						mapClassFreq.put(relation, 1);
					}
				}

			}

			Set<String> relations = mapClassScore.keySet();

			// String[] relations = new String[] { "0", "1", "2", "3" };

			for (String relation : relations) {
				if (mapClassFreq.containsKey(relation) == false)
					continue;
				int freq = mapClassFreq.get(relation);
				double score = mapClassScore.get(relation);
				double newScore = (score) / ((double) freq);
				mapClassScore.put(relation, newScore);
				System.out.println(relation + ": " + score + " " + freq + " - "
						+ newScore);
			}

			String preRelation = orgLabel;
			double preScore = -100000;
			for (String relation : relations) {
				if (mapClassScore.containsKey(relation) == false)
					continue;
				double score = mapClassScore.get(relation);
				if (score > preScore) {
					preRelation = relation;
					preScore = score;
				}
			}

			int p = Integer.parseInt(preRelation);

			if (p == ins.relation) {
				String out = "T" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				truePrediction.add(out);
				count++;

				System.out.println(out);

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + orgLabel + "\t"
						+ ins.toString();
				falsePrediction.add(out);

				System.out.println(out);

				addToLabelAccuracy(ins, false);
			}

		}

		for (String out : truePrediction)
			System.out.println(out);
		for (String out : falsePrediction)
			System.out.println(out);

		printMapAccuracy();

		System.out.println("Correct: " + count);
		System.out.println("Total: " + n);

		double acc = 0.0;
		if (arrInstances.size() > 0)
			acc = (double) count / (double) n;

		System.out.println("Acc: " + acc);
	}

	/**
	 * @param scoreXYs
	 * @param scoreXZs
	 * @param scoreYZs
	 * @return
	 */
	private ArrayList<InferenceOutput> inferenceWithSoftConstraints(
			Double[] scoreXYs, Double[] scoreXZs, Double[] scoreYZs) {

		ArrayList<InferenceOutput> arrInferenceOutputs = new ArrayList<InferenceOutput>();

		int sizeC1 = 4; // scoreXYs.length;
		int sizeC2 = 4; // scoreXZs.length;
		int sizeC3 = 4; // scoreYZs.length;

		ArrayList<InferenceOutput> arrTempOutputs = new ArrayList<InferenceOutput>();
		Map<String, Double> mapCombinationScore = new HashMap<String, Double>();

		for (int i = 0; i < sizeC1; i++) {
			for (int j = 0; j < sizeC2; j++) {
				for (int k = 0; k < sizeC3; k++) {

					String key = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					double value = scoreXYs[i] + scoreXZs[j] + scoreYZs[k];
					mapCombinationScore.put(key, value);
					InferenceOutput output = new InferenceOutput(key, value);
					arrTempOutputs.add(output);

				}
			}
		}

		// sortInferenceOutput(arrTempOutputs);

		strategy1(arrInferenceOutputs, arrTempOutputs, mapCombinationScore);

		// strategy2(arrInferenceOutputs, arrTempOutputs);

		return arrInferenceOutputs;

	}

	private void strategy2(ArrayList<InferenceOutput> arrInferenceOutputs,
			ArrayList<InferenceOutput> arrTempOutputs) {

		for (InferenceOutput tempOutput : arrTempOutputs) {
			String com = tempOutput.key;
			double prob = mapConstraintProbs.get(com);
			double newScore = tempOutput.value * prob;
			System.out.println(com + "\t" + tempOutput.value + "\t" + newScore);
			arrInferenceOutputs.add(new InferenceOutput(com, newScore));
		}

		System.out.println("---");

		sortInferenceOutput(arrInferenceOutputs);
		for (InferenceOutput output : arrInferenceOutputs) {
			System.out.println(output.key + "\t" + output.value);
		}

	}

	private void strategy1(ArrayList<InferenceOutput> arrInferenceOutputs,
			ArrayList<InferenceOutput> arrTempOutputs,
			Map<String, Double> mapCombinationScore) {
		String[] labels = new String[] { "0", "1", "2", "3" };

		// Set<String> setCombinations = mapCombinationScore.keySet();

		for (String label : labels) {

			double sum = 0;

			// for (String com : setCombinations) {
			for (InferenceOutput tempOutput : arrTempOutputs) {

				String com = tempOutput.key;

				if (com.charAt(0) == label.charAt(0)) {
					System.out.println(com + ": "
							+ mapCombinationScore.get(com) + " -- weight: "
							+ mapConstraintNegLogProbs.get(com));
					if (setInvalidCombinations.contains(com)) {
						continue;
					} else {
						sum += mapConstraintNegLogProbs.get(com)
								* mapCombinationScore.get(com);
						break;
					}
				}
			}

			System.out.println(label + ": " + sum);
			InferenceOutput output = new InferenceOutput(label, sum);
			arrInferenceOutputs.add(output);

		}

		sortInferenceOutput(arrInferenceOutputs);
	}

	public void backwardConstraintSelection(String interFile,
			String supportingInterFile, int maxAnc, int maxSib, int maxChi)
			throws Exception {

		setInvalidCombinations = new HashSet<String>();

		for (int i = 0; i < NUM_CLASS; i++) {
			for (int j = 0; j < NUM_CLASS; j++) {
				for (int k = 0; k < NUM_CLASS; k++) {
					String combination = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					setInvalidCombinations.add(combination);
				}
			}

		}

		loadConstraintsWeight();

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportingInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		RelationClassifier localClassifier = new RelationClassifier();

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(
				arrSupportingInstances, localClassifier);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		// Classify original instances
		// System.out.println("Classifying the instances.");

		double accuracy = -1.0;

		System.out.println("Round 0 accuracy: " + accuracy);

		ArrayList<String> arrCombinations = new ArrayList<String>();
		for (String s : setInvalidCombinations)
			arrCombinations.add(s);

		boolean isIncreasing = true;

		double bestAcc = accuracy;

		int i = 1;
		while (isIncreasing == true) {

			System.out.println("setInvalidCombinations size: "
					+ setInvalidCombinations.size());
			isIncreasing = false;
			String minCombination = "";

			int j = 1;
			for (String s : arrCombinations) {

				setInvalidCombinations.remove(s);

				accuracy = classifyOriginalInstances(arrInstances,
						mapSupportingPrediction, localClassifier, maxAnc,
						maxSib, maxChi, false);

				if (accuracy > bestAcc) {
					minCombination = s;
					bestAcc = accuracy;
					isIncreasing = true;
				}

				setInvalidCombinations.add(s);

				System.out.print(j + " ");
				j++;

			}

			if (isIncreasing == true) {
				System.out.println("Round " + i + " accuracy: " + bestAcc);
				System.out.println("Throw away: " + minCombination);
				setInvalidCombinations.remove(minCombination);
				arrCombinations.remove(minCombination);
				i++;
			} else {
				System.out.println("There is no increasement. Done!");
			}

		}

		System.out.println("*** Hard constraints: ");
		for (String s : setInvalidCombinations) {
			System.out.println(s);
		}

	}

	public void forwardConstraintSelection(String interFile,
			String supportingInterFile, int maxAnc, int maxSib, int maxChi)
			throws Exception {

		setInvalidCombinations = new HashSet<String>();

		ArrayList<String> arrCombinations = new ArrayList<String>();

		for (int i = 0; i < NUM_CLASS; i++) {
			for (int j = 0; j < NUM_CLASS; j++) {
				for (int k = 0; k < NUM_CLASS; k++) {
					String combination = Integer.toString(i) + "_"
							+ Integer.toString(j) + "_" + Integer.toString(k);
					arrCombinations.add(combination);
				}
			}

		}

		loadConstraintsWeight();

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportingInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		RelationClassifier localClassifier = new RelationClassifier();

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifySupportingInstances(
				arrSupportingInstances, localClassifier);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		// Classify original instances
		System.out.println("Classifying the instances.");
		double accuracy = classifyOriginalInstances(arrInstances,
				mapSupportingPrediction, localClassifier, maxAnc, maxSib,
				maxChi, false);

		System.out.println("Round 0 accuracy: " + accuracy);

		boolean isIncreasing = true;

		// Quang modified on Feb 9., 2010, assigned bestAcc = 0.0 so that we always can mine constraints.
		// double bestAcc = accuracy;
		double bestAcc = 0.0;

		int i = 1;
		while (isIncreasing == true) {

			System.out.println("setInvalidCombinations size: "
					+ setInvalidCombinations.size());
			isIncreasing = false;
			String maxCombination = "";

			int j = 1;
			for (String s : arrCombinations) {

				setInvalidCombinations.add(s);

				System.out.print(j + " ");
				accuracy = classifyOriginalInstances(arrInstances,
						mapSupportingPrediction, localClassifier, maxAnc,
						maxSib, maxChi, false);

				if (accuracy > bestAcc) {
					maxCombination = s;
					bestAcc = accuracy;
					isIncreasing = true;
				}

				setInvalidCombinations.remove(s);

				j++;

			}

			if (isIncreasing == true) {
				System.out.println("Round " + i + " accuracy: " + bestAcc);
				System.out.println("Adding: " + maxCombination);
				setInvalidCombinations.add(maxCombination);
				arrCombinations.remove(maxCombination);
				i++;
			} else {
				System.out.println("There is no increasement. Done!");
			}

		}

		System.out.println("*** Hard constraints: ");
		for (String s : setInvalidCombinations) {
			System.out.println(s);
		}

	}
}
