/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;

/**
 * @author dxquang May 24, 2009
 */
public class UnsupervisedModel2 {

	public static final double CONF_THRES = 1.5;
	int topK = 1;

	public int totalRound = 100;

	public int trainingRound = 500;

	public ArrayList<Instance> arrTrainInstances = null;
	public int arrTrainMarks[] = null;

	public ArrayList<Instance> arrTestInstances = null;

	public ArrayList<Instance> arrD0 = new ArrayList<Instance>();
	public ArrayList<Instance> arrD1 = new ArrayList<Instance>();
	public ArrayList<Instance> arrD2 = new ArrayList<Instance>();
	public ArrayList<Instance> arrD3 = new ArrayList<Instance>();

	RelationClassifier classifier = new RelationClassifier();

	double largestPMI = 0.0;

	/**
	 * @throws Exception
	 * 
	 */
	public UnsupervisedModel2(String trainFile, String testFile)
			throws Exception {

		System.out.println("Reading training data...");
		arrTrainInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		int size = arrTrainInstances.size();

		System.out.println("Training size: " + size);

		arrTrainMarks = new int[size];
		for (int i = 0; i < size; i++) {
			arrTrainMarks[i] = 1;
		}

		System.out.println("Reading testing data...");
		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);
		System.out.println("Testing size: " + arrTestInstances.size());
	}

	/**
	 * @throws Exception
	 * 
	 */
	public UnsupervisedModel2(String trainFile, String testFile, int totalRound)
			throws Exception {

		arrTrainInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		arrTestInstances = DataHandler.readTrainingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		this.totalRound = totalRound;

	}

	/**
	 * @throws Exception
	 * 
	 */
	public UnsupervisedModel2(String trainFile, String testFile,
			int totalRound, int topK) throws Exception {

		arrTrainInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		arrTestInstances = DataHandler.readTrainingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		this.totalRound = totalRound;
		this.topK = topK;

	}

	public void train() {

		// Find the largest PMI value for normalization process.
		for (Instance ins : arrTrainInstances) {
			if (ins.scorePmi_E1E2 > largestPMI)
				largestPMI = ins.scorePmi_E1E2;
		}
		System.out.println("Largest PMI: " + largestPMI);

		for (Instance ins : arrTrainInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		for (Instance ins : arrTestInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		System.out.println("Discovering...");
		discover();

		System.out.println("Semi-training...");
		semiTrain();

	}

	/**
	 * 
	 */
	private void semiTrain() {

		int round = 0;

		boolean flag = true;

		while (round < totalRound && flag == true) {

			ArrayList<Instance> arrTrains = new ArrayList<Instance>();

			arrTrains.addAll(arrD0);
			arrTrains.addAll(arrD1);
			arrTrains.addAll(arrD2);
			arrTrains.addAll(arrD3);
			Collections.shuffle(arrTrains);

			System.out.println("\n\tTraining size: " + arrTrains.size());
			train(arrTrains);

			System.out.println("\tTesting.");
			test();

			System.out.println("\nAugmenting.");
			flag = augmentTrainSet();

			round++;
		}
	}

	/**
	 * 
	 */
	private boolean augmentTrainSet() {

		boolean hasMore = false;

		int size = arrTrainInstances.size();
		
		double highestScore = -100.0;
		for (int i = 0; i < size; i++) {

			if (arrTrainMarks[i] == 0)
				continue;

			Instance ins = arrTrainInstances.get(i);

			ScoreSet scoreSet = classifier.scores(ins);

			String label = scoreSet.highScoreValue();
			double score = scoreSet.get(label);
			
			if (score > highestScore)
				highestScore = score;

			// System.out.print(score + "\t");
			// System.out.println(ins.toString());

			if (score > CONF_THRES) {
				switch (Integer.parseInt(label)) {
				case Constants.NONE:
					arrD0.add(ins);
					break;
				case Constants.ANCESTOR_E1_TO_E2:
					arrD1.add(ins);
					break;
				case Constants.ANCESTOR_E2_TO_E1:
					arrD2.add(ins);
					break;
				case Constants.COUSIN:
					arrD3.add(ins);
					break;
				default:
					System.out.println("Wrong label!");
					System.exit(1);
					break;
				}
				arrTrainMarks[i] = 0;
				hasMore = true;
			}

		}
		
		System.out.println("\tHighest score: " + highestScore);

		return hasMore;
	}

	public double test() {

		int count = 0;
		for (Instance ins : arrTestInstances) {

			// Normalize the PMI value
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;

			String pString = classifier.discreteValue(ins);

			int p = Integer.parseInt(pString);
			// System.out.println(p + "\t" + ins.toString());

			if (p == ins.relation)
				count++;
		}

		System.out.println("Correct: " + count);
		System.out.println("Total: " + arrTestInstances.size());

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) arrTestInstances.size();

		System.out.println("Acc: " + acc);
		return acc;

	}

	private void train(ArrayList<Instance> arrTrains) {

		classifier = new RelationClassifier();

		for (int round = 0; round < trainingRound; round++) {

			for (Instance ins : arrTrains) {
				classifier.learn(ins);
			}

		}
	}

	public void discover() {

		ArrayList<Instance> arrInterD1 = new ArrayList<Instance>();
		ArrayList<Instance> arrInterD2 = new ArrayList<Instance>();
		ArrayList<Integer> arrInterD1Marks = new ArrayList<Integer>();
		ArrayList<Integer> arrInterD2Marks = new ArrayList<Integer>();

		candidateAncestor(arrInterD1, arrInterD2, arrInterD1Marks,
				arrInterD2Marks);

		// System.out.println("* arrInterD1:");
		// Instance.printArrayInstances(arrInterD1);
		// System.out.println("* arrInterD2:");
		// Instance.printArrayInstances(arrInterD2);

		int size = arrInterD1.size();
		for (int i = 0; i < size; i++) {
			Instance ins = arrInterD1.get(i);
			boolean flag = true;

			String entityX = ins.entity1;
			String entityY = ins.entity2;

			if (checkSecondEntity(entityX, arrInterD1, null) == true)
				flag = false;

			else if (checkFirstEntity(entityX, arrInterD2, null) == true)
				flag = false;

			else if (checkSecondEntity(entityY, arrInterD1, entityX) == true)
				flag = false;

			else if (checkFirstEntity(entityY, arrInterD2, entityX) == true)
				flag = false;

			else if (checkFirstEntity(entityY, arrInterD1, null) == true)
				flag = false;

			else if (checkSecondEntity(entityY, arrInterD2, null) == true)
				flag = false;

			if (flag == true) {
				ins.relation = Constants.ANCESTOR_E1_TO_E2;
				arrD1.add(ins);
				arrTrainMarks[arrInterD1Marks.get(i)] = 0;
			}

		}
		System.out.println("\tarrD1: " + arrD1.size());
		Instance.printArrayInstances(arrD1);

		size = arrInterD2.size();
		for (int i = 0; i < size; i++) {
			Instance ins = arrInterD2.get(i);

			boolean flag = true;

			String entityX = ins.entity1;
			String entityY = ins.entity2;

			if (checkFirstEntity(entityY, arrInterD2, null) == true)
				flag = false;

			else if (checkSecondEntity(entityY, arrInterD1, null) == true)
				flag = false;

			else if (checkFirstEntity(entityX, arrInterD2, entityY) == true)
				flag = false;

			else if (checkSecondEntity(entityX, arrInterD1, entityY) == true)
				flag = false;

			else if (checkSecondEntity(entityX, arrInterD2, null) == true)
				flag = false;

			else if (checkFirstEntity(entityX, arrInterD1, null) == true)
				flag = false;

			if (flag == true) {
				ins.relation = Constants.ANCESTOR_E2_TO_E1;
				arrD2.add(ins);
				arrTrainMarks[arrInterD2Marks.get(i)] = 0;
			}

		}
		System.out.println("\tarrD2: " + arrD2.size());
		Instance.printArrayInstances(arrD2);

		/*-Get cousin examples-------*/

		Set<String> setValidPairs = new HashSet<String>();
		Map<String, ArrayList<String>> mapEntities = new HashMap<String, ArrayList<String>>();

		for (Instance ins : arrD1) {
			if (mapEntities.containsKey(ins.entity1)) {
				ArrayList<String> arrE2 = mapEntities.get(ins.entity1);
				arrE2.add(ins.entity2);
			} else {
				ArrayList<String> arrE2 = new ArrayList<String>();
				arrE2.add(ins.entity2);
				mapEntities.put(ins.entity1, arrE2);
			}
		}

		for (Instance ins : arrD2) {
			if (mapEntities.containsKey(ins.entity2)) {
				ArrayList<String> arrE1 = mapEntities.get(ins.entity2);
				arrE1.add(ins.entity1);
			} else {
				ArrayList<String> arrE1 = new ArrayList<String>();
				arrE1.add(ins.entity1);
				mapEntities.put(ins.entity2, arrE1);
			}
		}

		Set<String> keySet = mapEntities.keySet();
		for (String key : keySet) {
			ArrayList<String> arrEs = mapEntities.get(key);
			for (String e1 : arrEs) {
				for (String e2 : arrEs) {
					String exa = e1 + "_" + e2;
					if (!setValidPairs.contains(exa))
						setValidPairs.add(exa);
				}
			}
		}

		size = arrTrainInstances.size();
		for (int i = 0; i < size; i++) {
			Instance ins = arrTrainInstances.get(i);
			String exa = ins.entity1 + "_" + ins.entity2;
			if (setValidPairs.contains(exa)) {
				ins.relation = Constants.COUSIN;
				arrD3.add(ins);
				arrTrainMarks[i] = 0;
			}
		}
		System.out.println("\tarrD3: " + arrD3.size());
		Instance.printArrayInstances(arrD3);

		/*-Get negative examples----*/

		// 1. ClassEntity & EntityClass && EntityEntity
		setValidPairs = new HashSet<String>();
		for (String key1 : keySet) {
			ArrayList<String> arrUs = mapEntities.get(key1);
			for (String key2 : keySet) {
				if (!key1.equals(key2)) {
					ArrayList<String> arrEs = mapEntities.get(key2);
					for (String e : arrEs) {
						String exa = key1 + "_" + e;
						String exaReverse = e + "_" + key1;
						if (!setValidPairs.contains(exa))
							setValidPairs.add(exa);
						if (!setValidPairs.contains(exaReverse))
							setValidPairs.add(exaReverse);
						for (String u : arrUs) {
							String exaEU = e + "_" + u;
							if (!setValidPairs.contains(exaEU))
								setValidPairs.add(exaEU);
						}
					}
				}
			}
		}

		size = arrTrainInstances.size();
		for (int i = 0; i < size; i++) {
			Instance ins = arrTrainInstances.get(i);
			String exa = ins.entity1 + "_" + ins.entity2;
			if (setValidPairs.contains(exa)) {
				ins.relation = Constants.NONE;
				arrD0.add(ins);
				arrTrainMarks[i] = 0;
			}
		}
		System.out.println("\tarrD0: " + arrD0.size());
		Instance.printArrayInstances(arrD0);

	}

	/**
	 * @param entity
	 * @param arrInterD
	 * @return
	 */
	private boolean checkFirstEntity(String entity,
			ArrayList<Instance> arrInterD, String constraint) {

		for (Instance ins : arrInterD) {
			if (constraint != null) {
				if (!ins.entity2.equals(constraint))
					if (ins.entity1.equals(entity))
						return true;
			} else if (ins.entity1.equals(entity))
				return true;
		}

		return false;
	}

	/**
	 * @param entity
	 * @param arrInterD
	 * @return
	 */
	private boolean checkSecondEntity(String entity,
			ArrayList<Instance> arrInterD, String constraint) {

		for (Instance ins : arrInterD) {
			if (constraint != null) {
				if (!ins.entity1.equals(constraint))
					if (ins.entity2.equals(entity))
						return true;
			} else if (ins.entity2.equals(entity))
				return true;
		}

		return false;
	}

	/**
	 * @param arrInterD1
	 * @param arrInterD2
	 * @param arrInterD2Marks
	 * @param arrInterD1Marks
	 */
	private void candidateAncestor(ArrayList<Instance> arrInterD1,
			ArrayList<Instance> arrInterD2, ArrayList<Integer> arrInterD1Marks,
			ArrayList<Integer> arrInterD2Marks) {

		int size = arrTrainInstances.size();
		for (int i = 0; i < size; i++) {
			Instance ins = arrTrainInstances.get(i);
			if (ins.ratio_TtlCat > 0.0 && ins.ratio_CatTtl == 0.0) {
				arrInterD1.add(ins);
				arrInterD1Marks.add(i);
			} else if (ins.ratio_TtlCat == 0.0 && ins.ratio_CatTtl > 0.0) {
				arrInterD2.add(ins);
				arrInterD2Marks.add(i);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		UnsupervisedModel2 learner = new UnsupervisedModel2(
				"/Users/dxquang/Desktop/test.inter",
				"/Users/dxquang/Desktop/test.inter");
		learner.train();
	}
}
