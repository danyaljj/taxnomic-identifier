/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;

/**
 * @author dxquang May 21, 2009
 */
public class Unsupervised {

	int topInit = 50;
	int topK = 2;

	public int totalRound = 100;

	public int trainingRound = 500;

	public ArrayList<Instance> arrAllInstances = null;
	public ArrayList<Instance> arrTestInstances = null;

	public Set<String> setUsedInstances_D0 = new HashSet<String>();
	public Set<String> setUsedInstances_D1 = new HashSet<String>();
	public Set<String> setUsedInstances_D2 = new HashSet<String>();
	public Set<String> setUsedInstances_D3 = new HashSet<String>();

	RelationClassifier classifier = new RelationClassifier();

	/**
	 * @throws Exception
	 * 
	 */
	public Unsupervised(String trainFile, String testFile) throws Exception {

		System.out.println("Reading training data...");
		arrAllInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);
		System.out.println("Training size: " + arrAllInstances.size());

		System.out.println("Reading testing data...");
		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);
		System.out.println("Testing size: " + arrTestInstances.size());
	}

	/**
	 * @throws Exception
	 * 
	 */
	public Unsupervised(String trainFile, String testFile, int totalRound)
			throws Exception {

		arrAllInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		arrTestInstances = DataHandler.readTrainingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		this.totalRound = totalRound;

	}

	/**
	 * @throws Exception
	 * 
	 */
	public Unsupervised(String trainFile, String testFile, int totalRound,
			int topK) throws Exception {

		arrAllInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		arrTestInstances = DataHandler.readTrainingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE);

		this.totalRound = totalRound;
		this.topK = topK;

	}

	public void train() {

		System.out.println("Start training...");
		int round = 0;

		// Set<String> keySet = mapAllInstances.keySet();
		System.out.println("Initializing...");

		ArrayList<Instance> arrD0 = initializeTopK(Constants.NONE, topInit);
		
		System.out.println("- arrD0, size=" + arrD0.size());
		Instance.printArrayInstances(arrD0);

		ArrayList<Instance> arrD1 = initializeTopK(Constants.ANCESTOR_E1_TO_E2,
				topInit);
		System.out.println("- arrD1, size=" + arrD1.size());
		
		Instance.printArrayInstances(arrD1);

		ArrayList<Instance> arrD2 = initializeTopK(Constants.ANCESTOR_E2_TO_E1,
				topInit);
		
		System.out.println("- arrD2, size=" + arrD2.size());
		Instance.printArrayInstances(arrD2);

		ArrayList<Instance> arrD3 = initializeTopK(Constants.COUSIN, topInit);
		System.out.println("- arrD3, size=" + arrD3.size());
		Instance.printArrayInstances(arrD3);

		System.out.println("Loops...");
		while (round < this.totalRound) {

			System.out.println("\t- Round: " + (round + 1));
			ArrayList<Instance> arrTrains = new ArrayList<Instance>();
			arrTrains.addAll(arrD0);
			arrTrains.addAll(arrD1);
			arrTrains.addAll(arrD2);
			arrTrains.addAll(arrD3);
			Collections.shuffle(arrTrains);

			// System.out.println("\t\tTraining size: " + arrTrains.size());
			// Instance.printArrayInstances(arrTrains);

			trainClassifier(arrTrains);

			double acc = testClassifier();
			System.out.println("\t\tAccuracy: " + acc);

			ArrayList<Instance> arrP0 = new ArrayList<Instance>();
			ArrayList<Instance> arrP1 = new ArrayList<Instance>();
			ArrayList<Instance> arrP2 = new ArrayList<Instance>();
			ArrayList<Instance> arrP3 = new ArrayList<Instance>();

			splitData(arrAllInstances, arrP0, arrP1, arrP2, arrP3);

			ArrayList<Instance> arrT1 = getTopK(arrP1,
					Constants.ANCESTOR_E1_TO_E2, topK);

			ArrayList<Instance> arrT2 = getTopK(arrP2,
					Constants.ANCESTOR_E2_TO_E1, topK);

			ArrayList<Instance> arrT3 = getTopK(arrP3, Constants.COUSIN, topK);

			arrD1.addAll(arrT1);
			arrD2.addAll(arrT2);
			arrD3.addAll(arrT3);

			ArrayList<Instance> arrT0 = getTopK(arrP0, Constants.NONE, topK);
			// arrD0 = getTopK(arrP0, Constants.NONE, arrD1.size());
			arrD0.addAll(arrT0);

			round++;
		}
		System.out.println("Done.");

	}

	/**
	 * @param arrTestInstances2
	 */
	private double testClassifier() {

		int count = 0;
		for (Instance ins : arrTestInstances) {

			String pString = classifier.discreteValue(ins);

			int p = Integer.parseInt(pString);

			if (p == ins.relation)
				count++;
		}

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) arrTestInstances.size();

		return acc;

	}

	/**
	 * @param arrAllInstances
	 * @param arrP0
	 * @param arrP1
	 * @param arrP2
	 * @param arrP3
	 */
	private void splitData(ArrayList<Instance> arrAllInstances,
			ArrayList<Instance> arrP0, ArrayList<Instance> arrP1,
			ArrayList<Instance> arrP2, ArrayList<Instance> arrP3) {

		for (Instance ins : arrAllInstances) {

			ScoreSet scoreSet = classifier.scores(ins);

			Instance newIns = new Instance(ins);
			double score = scoreSet.get(Integer.toString(Constants.NONE));
			newIns.relation = Constants.NONE;
			newIns.finalScore = score;
			arrP0.add(newIns);

			newIns = new Instance(ins);
			score = scoreSet.get(Integer.toString(Constants.ANCESTOR_E1_TO_E2));
			newIns.relation = Constants.ANCESTOR_E1_TO_E2;
			newIns.finalScore = score;
			arrP1.add(newIns);

			newIns = new Instance(ins);
			score = scoreSet.get(Integer.toString(Constants.ANCESTOR_E2_TO_E1));
			newIns.relation = Constants.ANCESTOR_E2_TO_E1;
			newIns.finalScore = score;
			arrP2.add(newIns);

			newIns = new Instance(ins);
			score = scoreSet.get(Integer.toString(Constants.COUSIN));
			newIns.relation = Constants.COUSIN;
			newIns.finalScore = score;
			arrP3.add(newIns);

		}
	}

	/**
	 * @param relClass
	 * @param k
	 * @return
	 */
	private ArrayList<Instance> initializeTopK(int relClass, int k) {

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();

		ArrayList<Instance> arrCurInstances = null;

		switch (relClass) {

		case Constants.ANCESTOR_E1_TO_E2:
			sort1(arrAllInstances);
			arrCurInstances = new ArrayList<Instance>();
			for (Instance ins : arrAllInstances) {
				// if (ins.ratio_TtlCat > 0 && ins.ratio_CatTtl == 0 &&
				// ins.ratio_CatCat == 0) {
				if (ins.ratio_TtlCat > 0) {
					ins.relation = Constants.ANCESTOR_E1_TO_E2;
					arrCurInstances.add(ins);
					setUsedInstances_D1.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
				}
			}
			arrInstance = pickTopK(arrCurInstances, k);
			break;

		case Constants.ANCESTOR_E2_TO_E1:
			sort2(arrAllInstances);
			arrCurInstances = new ArrayList<Instance>();
			for (Instance ins : arrAllInstances) {
				// if (ins.ratio_TtlCat == 0 && ins.ratio_CatTtl > 0
				// && ins.ratio_CatCat == 0) {
				if (ins.ratio_CatTtl > 0) {
					ins.relation = Constants.ANCESTOR_E2_TO_E1;
					arrCurInstances.add(ins);
					setUsedInstances_D2.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
				}
			}
			arrInstance = pickTopK(arrCurInstances, k);
			break;

		case Constants.COUSIN:
			sort3(arrAllInstances);
			arrCurInstances = new ArrayList<Instance>();
			for (Instance ins : arrAllInstances) {
				if (ins.ratio_TtlCat == 0 && ins.ratio_CatTtl == 0
						&& ins.ratio_CatCat > 0) {
					// if (ins.ratio_CatCat > 0) {
					ins.relation = Constants.COUSIN;
					arrCurInstances.add(ins);
					setUsedInstances_D3.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
				}
			}
			arrInstance = pickTopK(arrCurInstances, k);
			break;

		case Constants.NONE:
			arrCurInstances = new ArrayList<Instance>();
			for (Instance ins : arrAllInstances) {
				if (ins.ratio_TtlCat == 0 && ins.ratio_CatTtl == 0
						&& ins.ratio_CatCat == 0) {
					ins.relation = Constants.NONE;
					arrCurInstances.add(ins);
					setUsedInstances_D0.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
				}
			}
			Collections.shuffle(arrCurInstances);
			arrInstance = pickTopK(arrCurInstances, k);
			break;

		default:
			System.out.println("Wrong relation class!");
			System.exit(1);
			break;
		}

		return arrInstance;

	}

	private ArrayList<Instance> getTopK(ArrayList<Instance> arrCurInstances,
			int relClass, int k) {

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();

		switch (relClass) {

		case Constants.ANCESTOR_E1_TO_E2:
			sortScore(arrCurInstances);
			int i = 0;
			int c = 0;
			while (c < k && i < arrCurInstances.size()) {
				Instance ins = arrCurInstances.get(i);
				String key = ins.entityClass + "_" + ins.entity1 + "_"
						+ ins.entity2;
				if (!setUsedInstances_D1.contains(key)) {
					ins.relation = Constants.ANCESTOR_E1_TO_E2;
					arrInstance.add(ins);
					setUsedInstances_D1.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
					c++;
				}
				i++;
			}
			break;

		case Constants.ANCESTOR_E2_TO_E1:
			sortScore(arrCurInstances);
			i = 0;
			c = 0;
			while (c < k && i < arrCurInstances.size()) {
				Instance ins = arrCurInstances.get(i);
				String key = ins.entityClass + "_" + ins.entity1 + "_"
						+ ins.entity2;
				if (!setUsedInstances_D2.contains(key)) {
					ins.relation = Constants.ANCESTOR_E2_TO_E1;
					arrInstance.add(ins);
					setUsedInstances_D2.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
					c++;
				}
				i++;
			}
			break;

		case Constants.COUSIN:
			sortScore(arrCurInstances);
			i = 0;
			c = 0;
			while (c < k && i < arrCurInstances.size()) {
				Instance ins = arrCurInstances.get(i);
				String key = ins.entityClass + "_" + ins.entity1 + "_"
						+ ins.entity2;
				if (!setUsedInstances_D3.contains(key)) {
					ins.relation = Constants.COUSIN;
					arrInstance.add(ins);
					setUsedInstances_D3.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
					c++;
				}
				i++;
			}
			break;

		case Constants.NONE:
			sortScore(arrCurInstances);
			i = 0;
			c = 0;
			while (c < k && i < arrCurInstances.size()) {
				Instance ins = arrCurInstances.get(i);
				String key = ins.entityClass + "_" + ins.entity1 + "_"
						+ ins.entity2;
				if (!setUsedInstances_D0.contains(key)) {
					ins.relation = Constants.NONE;
					arrInstance.add(ins);
					setUsedInstances_D0.add(ins.entityClass + "_" + ins.entity1
							+ "_" + ins.entity2);
					c++;
				}
				i++;
			}
			/*
			sortScore(arrCurInstances);
			i = 0;
			c = 0;
			while (c < k && i < arrCurInstances.size()) {
				Instance ins = arrCurInstances.get(i);
				ins.relation = Constants.NONE;
				arrInstance.add(ins);
				c++;
				i++;
			}
			*/
			break;

		default:
			System.out.println("Wrong relation class!");
			System.exit(1);
			break;
		}

		return arrInstance;

	}

	/**
	 * @param arrCurInstances
	 * @param k
	 */
	private ArrayList<Instance> pickTopK(ArrayList<Instance> arrCurInstances,
			int k) {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		int n = Math.min(arrCurInstances.size(), k);

		for (int i = 0; i < n; i++) {
			arrInstances.add(arrCurInstances.get(i));
		}

		return arrInstances;
	}

	/**
	 * @param arrCurInstances
	 */
	private void sort1(ArrayList<Instance> arrCurInstances) {
		Collections.sort(arrCurInstances, new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				if (o1.ratio_TtlCat < o2.ratio_TtlCat)
					return 1;
				else if (o1.ratio_TtlCat == o2.ratio_TtlCat)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param arrCurInstances
	 */
	private void sort2(ArrayList<Instance> arrCurInstances) {
		Collections.sort(arrCurInstances, new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				if (o1.ratio_CatTtl < o2.ratio_CatTtl)
					return 1;
				else if (o1.ratio_CatTtl == o2.ratio_CatTtl)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param arrCurInstances
	 */
	private void sort3(ArrayList<Instance> arrCurInstances) {
		Collections.sort(arrCurInstances, new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				if (o1.ratio_CatCat < o2.ratio_CatCat)
					return 1;
				else if (o1.ratio_CatCat == o2.ratio_CatCat)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param arrTrains
	 */
	private void trainClassifier(ArrayList<Instance> arrTrains) {

		classifier = new RelationClassifier();

		for (int round = 0; round < trainingRound; round++) {

			for (Instance ins : arrTrains) {
				classifier.learn(ins);
			}

		}
	}

	/**
	 * @param arrInstance
	 */
	private void sortScore(ArrayList<Instance> arrInstance) {
		Collections.sort(arrInstance, new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				if (o1.finalScore < o2.finalScore)
					return 1;
				else if (o1.finalScore == o2.finalScore)
					return 0;
				else
					return -1;
			}
		});
	}
}
