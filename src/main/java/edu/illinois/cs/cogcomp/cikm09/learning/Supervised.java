/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;

/**
 * @author dxquang May 23, 2009
 */
public class Supervised {

	public int trainingRound = 500;

	public ArrayList<Instance> arrTrainInstances = null;
	public ArrayList<Instance> arrTestInstances = null;

	//public int instanceNums[] = { 1, 5, 10, 20, 50, 100, 200, 500, 700, 1000,
	//		1500, 2000, 2500, 3000, 3200, 3400, 3600, 3800, 4000 };
	 public int instanceNums[] = { 400 };
	public double accuracies[] = new double[instanceNums.length];

	ArrayList<Instance> arrD0 = new ArrayList<Instance>();
	ArrayList<Instance> arrD1 = new ArrayList<Instance>();
	ArrayList<Instance> arrD2 = new ArrayList<Instance>();
	ArrayList<Instance> arrD3 = new ArrayList<Instance>();

	RelationClassifier classifier = new RelationClassifier();

	double largestPMI = 0.0;

	/**
	 * @throws Exception
	 * 
	 */
	public Supervised(String trainFile, String testFile) throws Exception {

		System.out.println("Reading training data...");
		arrTrainInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);
		System.out.println("Training size: " + arrTrainInstances.size());

		System.out.println("Reading testing data...");
		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);
		System.out.println("Testing size: " + arrTestInstances.size());

		groupInstances();
	}

	/**
	 * 
	 */
	private void groupInstances() {

		for (Instance ins : arrTrainInstances) {

			switch (ins.relation) {
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

		}
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

		int sizeD0 = arrD0.size();
		int sizeD1 = arrD1.size();
		int sizeD2 = arrD2.size();
		int sizeD3 = arrD3.size();

		int sizeMin = Math.min(sizeD0, Math.min(sizeD1, Math
				.min(sizeD2, sizeD3)));
		System.out.println("sizeMin=" + sizeMin);

		int n = instanceNums.length;

		boolean flag = false;

		int count = 0;

		for (int i = 0; i < n; i++) {

			int k = instanceNums[i];

			if (k > sizeMin) {
				k = sizeMin;
				instanceNums[i] = k;
				flag = true;
			}

			System.out.println("\nk=" + k);

			ArrayList<Instance> arrSub0 = getInstances(arrD0, k);
			ArrayList<Instance> arrSub1 = getInstances(arrD1, k);
			ArrayList<Instance> arrSub2 = getInstances(arrD2, k);
			ArrayList<Instance> arrSub3 = getInstances(arrD3, k);

			ArrayList<Instance> arrTrains = new ArrayList<Instance>();
			arrTrains.addAll(arrSub0);
			arrTrains.addAll(arrSub1);
			arrTrains.addAll(arrSub2);
			arrTrains.addAll(arrSub3);
			Collections.shuffle(arrTrains);

			System.out.println("Training size: " + arrTrains.size());

			train(arrTrains);

			double acc = test();
			acc = test();

			accuracies[i] = acc;

			count++;

			if (flag == true)
				break;

		}

		for (int i = 0; i < count; i++) {
			System.out.print(" | " + "K=" + instanceNums[i]);
		}
		System.out.println();

		for (int i = 0; i < count; i++) {

			DecimalFormat df = new DecimalFormat("#.####");
			String res = df.format(accuracies[i]);
			System.out.print(" | " + res);
		}
		System.out.println();
	}

	/**
	 * @param arrCurInstances
	 * @param k
	 * @return
	 */
	private ArrayList<Instance> getInstances(
			ArrayList<Instance> arrCurInstances, int k) {

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();
		for (int i = 0; i < k; i++)
			arrInstance.add(arrCurInstances.get(i));

		return arrInstance;
	}

	public void train(ArrayList<Instance> arrInstances) {

		classifier = new RelationClassifier();

		// System.out.print("Round: ");
		for (int round = 0; round < trainingRound; round++) {

			// System.out.print((round+1) + " ");
			for (Instance ins : arrInstances) {

				classifier.learn(ins);
			}

		}
		// System.out.println();

	}

	public double test() {

		int count = 0;
		for (Instance ins : arrTestInstances) {


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

	public static void main(String[] args) throws Exception {

		Supervised learner = new Supervised(
				"//Users//dxquang//Desktop//testSuper.inter",
				"//Users//dxquang//Desktop//testSuper.inter");

		System.out.println("Start training...");
		learner.train();
		System.out.println("Done.");

		System.out.println("\nTesting...");
		double acc = learner.test();
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

}
