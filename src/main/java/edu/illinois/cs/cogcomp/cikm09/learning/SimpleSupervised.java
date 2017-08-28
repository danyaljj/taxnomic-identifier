/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;

import edu.illinois.cs.cogcomp.utils.IOManager;

import edu.illinois.cs.cogcomp.lbjava.RelationClassifier;

/**
 * @author dxquang May 13, 2009
 */
public class SimpleSupervised {

	public class LabelAccuracy {
		int correctXY = 0;
		int totalXY = 0;

		int correctYX = 0;
		int totalYX = 0;

		int correctZZ = 0;
		int totalZZ = 0;

		int correctNone = 0;
		int totalNone = 0;

		int correctAll = 0;
		int totalAll = 0;

		public void add(Instance ins, boolean result) {
			switch (ins.relation) {
			case Constants.ANCESTOR_E1_TO_E2:
				if (result == true)
					correctXY++;
				totalXY++;
				break;
			case Constants.ANCESTOR_E2_TO_E1:
				if (result == true)
					correctYX++;
				totalYX++;
				break;
			case Constants.COUSIN:
				if (result == true)
					correctZZ++;
				totalZZ++;
				break;
			case Constants.NONE:
				if (result == true)
					correctNone++;
				totalNone++;
				break;
			}

			if (result == true)
				correctAll++;
			totalAll++;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			DecimalFormat df = new DecimalFormat("#.####");

			double acc;
			String accString;

			if (totalXY == 0)
				acc = 1;
			else
				acc = (double) correctXY / (double) totalXY;
			accString = df.format(acc);

			String xy = correctXY + " | " + totalXY + " | " + accString + "|";

			if (totalYX == 0)
				acc = 1;
			else
				acc = (double) correctYX / (double) totalYX;
			accString = df.format(acc);

			String yx = correctYX + " | " + totalYX + " | " + accString + "|";

			if (totalZZ == 0)
				acc = 1;
			else
				acc = (double) correctZZ / (double) totalZZ;
			accString = df.format(acc);

			String zz = correctZZ + " | " + totalZZ + " | " + accString + "|";

			if (totalNone == 0)
				acc = 1;
			else
				acc = (double) correctNone / (double) totalNone;
			accString = df.format(acc);

			String none = correctNone + " | " + totalNone + " | " + accString
					+ "|";

			if (totalAll == 0)
				acc = 1;
			else
				acc = (double) correctAll / (double) totalAll;
			accString = df.format(acc);

			String all = correctAll + " | " + totalAll + " | " + accString
					+ "|";

			return xy + yx + zz + none + all;
		}
	}

	public static final String PMI_FILE = "pmi_value.txt";

	ArrayList<Instance> arrTrainInstances = null;
	ArrayList<Instance> arrTestInstances = null;

	RelationClassifier classifier = null;

	int trainingRound = 1500;

	// public static final int TEST_SIZE = 240;
	public static final int TEST_SIZE = 1000000;

	// public static final int TRAIN_SIZE = 720;
	public static final int TRAIN_SIZE = 1000000;

	public Map<String, LabelAccuracy> mapAccuracy = new HashMap<String, LabelAccuracy>();

	/**
	 * @throws Exception
	 * 
	 */
	public SimpleSupervised() throws Exception {
		// System.out.println("Reading training data...");
		// arrTrainInstances = DataHandler.readTrainingInstances(
		// "/Users/dxquang/Desktop/test.shuffled.inter",
		// Constants.INPUT_TYPE_INTERMEDIATE);
		// System.out.println("Training size: " + arrTrainInstances.size());
		//
		// System.out.println("Reading testing data...");
		// arrTestInstances = DataHandler.readTestingInstances(
		// "/Users/dxquang/Desktop/test.shuffled.inter",
		// Constants.INPUT_TYPE_INTERMEDIATE, true);
		// System.out.println("Testing size: " + arrTestInstances.size());
	}

	public SimpleSupervised(String trainFile, String testFile) throws Exception {

		System.out.println("Reading training data...");
		arrTrainInstances = DataHandler.readTrainingInstances(trainFile,
				Constants.INPUT_TYPE_INTERMEDIATE);
		System.out.println("Training size: " + arrTrainInstances.size());

		System.out.println("Reading testing data...");
		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);
		System.out.println("Testing size: " + arrTestInstances.size());

	}

	public void train() {

		// Find the largest PMI value for normalization process.
		double largestPMI = 0.0;
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

		// Write PMI value to file
		setLargestPMI(largestPMI);

		train(arrTrainInstances);

		double acc = test();

	}

	/**
	 * @param largestPMI
	 */
	private void setLargestPMI(double largestPMI) {
		String pmi = Double.toString(largestPMI);
		IOManager.writeContent(pmi, PMI_FILE);
	}

	public void train(ArrayList<Instance> arrInstances) {

		System.out.println("Training round: " + trainingRound);

		classifier = new RelationClassifier();

		int trainSize = Math.min(TRAIN_SIZE, arrTrainInstances.size());
		System.out.println("\ntrainSize=" + trainSize);

		for (int round = 0; round < trainingRound; round++) {

			for (int i = 0; i < trainSize; i++) {
				Instance ins = arrInstances.get(i);
				classifier.learn(ins);
			}

		}

		classifier.doneLearning();

		classifier.save();

	}

	public double test() {

		int testSize = Math.min(TEST_SIZE, arrTestInstances.size());
		System.out.println("testSize=" + testSize + "\n");
		int count = 0;

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();

		for (int i = 0; i < testSize; i++) {

			Instance ins = arrTestInstances.get(i);
			System.out.println("instance: " + ins);
			String pString = classifier.discreteValue(ins);

			int p = Integer.parseInt(pString);

			if (p == ins.relation) {
				String out = "T" + "\t" + p + "\t" + ins.toString();
				truePrediction.add(out);
				count++;

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + ins.toString();
				falsePrediction.add(out);

				addToLabelAccuracy(ins, false);
			}

		}

		for (String out : truePrediction)
			System.out.println(out);
		for (String out : falsePrediction)
			System.out.println(out);

		printMapAccuracy();

		System.out.println("Correct: " + count);
		System.out.println("Total: " + testSize);

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) testSize;

		System.out.println("Acc: " + acc);

		return acc;

	}

	public double testLevData() {

		int testSize = Math.min(TEST_SIZE, arrTestInstances.size());
		System.out.println("testSize=" + testSize + "\n");
		int count = 0;

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();

		for (int i = 0; i < testSize; i++) {

			Instance ins = arrTestInstances.get(i);

			String pString = classifier.discreteValue(ins);

			int p = Integer.parseInt(pString);

			if (((p == 0 || p == 3) && (ins.relation == 0 || ins.relation == 3))
					|| ((p == 1 || p == 2) && (ins.relation == 1 || ins.relation == 2))) {
				String out = "T" + "\t" + p + "\t" + ins.toString();
				truePrediction.add(out);
				count++;

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + ins.toString();
				falsePrediction.add(out);

				addToLabelAccuracy(ins, false);
			}

		}

		for (String out : truePrediction)
			System.out.println(out);
		for (String out : falsePrediction)
			System.out.println(out);

		printMapAccuracy();

		System.out.println("Correct: " + count);
		System.out.println("Total: " + testSize);

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) testSize;

		System.out.println("Acc: " + acc);

		return acc;

	}

	public double testAcl10WithoutGraphConstraints(String testFile)
			throws Exception {

		System.out.println("Reading testing data...\n\n");

		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);
		System.out.println("Testing size: " + arrTestInstances.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrTestInstances) {

			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		classifier = new RelationClassifier();

		return testAcl10WithoutGraphConstraints();
	}

	public double testAcl10WithoutGraphConstraints() {

		int testSize = Math.min(TEST_SIZE, arrTestInstances.size());
		System.out.println("testSize=" + testSize + "\n");
		int count = 0;

		Softmax sm = new Softmax();

		for (int i = 0; i < testSize; i++) {

			Instance ins = arrTestInstances.get(i);

			String out = "";

			if (ins.entity1.equals(ins.entity2)) {
				out = "I" + "\t" + "I" + "\t" + ins.toString();
			} else if (ins.scorePmi_E1E2 < 0.0) {
				out = "N" + "\t" + "N" + "\t" + ins.toString();
			} else {

				String pString = classifier.discreteValue(ins);
				int p = Integer.parseInt(pString);

				ScoreSet scores = classifier.scores(ins);

				scores = sm.normalize(scores);

				double d0 = scores.get(Integer.toString(Constants.NONE));
				double d1 = scores.get(Integer
						.toString(Constants.ANCESTOR_E1_TO_E2));
				double d2 = scores.get(Integer
						.toString(Constants.ANCESTOR_E2_TO_E1));
				double d3 = scores.get(Integer.toString(Constants.COUSIN));

				List<Double> d = new ArrayList<Double>();
				d.add(d0);
				d.add(d1);
				d.add(d2);
				d.add(d3);
				Collections.sort(d);

				double largest = d.get(3);
				double secondLargest = d.get(2);

				double sum = d.get(0) + d.get(1) + d.get(2);
				if ((largest - sum) / sum > .999) {
					out = "D" + "\t" + p + "\t" + ins.toString() + "\t" + d0
							+ "\t" + d1 + "\t" + d2 + "\t" + d3 + "\t"
							+ d.get(0) + "\t" + d.get(1) + "\t" + d.get(2)
							+ "\t" + d.get(3);
				} else {
					out = "A" + "\t" + p + "\t" + ins.toString();
				}

			}

			System.out.println(out);

		}

		double acc = 0.0;
		return acc;

	}

	public double test(String testFile, String readMode) throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		System.out.println("Reading testing data...\n\n");

		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, mode);

		System.out.println("Testing size: " + arrTestInstances.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrTestInstances) {

			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		classifier = new RelationClassifier();

		return test();
	}

	public double testLevData(String testFile, String readMode)
			throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		System.out.println("Reading testing data...\n\n");

		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, mode);

		System.out.println("Testing size: " + arrTestInstances.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrTestInstances) {

			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		classifier = new RelationClassifier();

		return testLevData();
	}

	public double testWithInference(String testFile) throws Exception {

		System.out.println("Reading testing data...");
		arrTestInstances = DataHandler.readTestingInstances(testFile,
				Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);
		System.out.println("Testing size: " + arrTestInstances.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrTestInstances) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		classifier = new RelationClassifier();

		return testWithInference();
	}

	public double testWithInference() {

		int testSize = Math.min(TEST_SIZE, arrTestInstances.size());
		System.out.println("testSize=" + testSize + "\n");
		int count = 0;

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();

		for (int i = 0; i < testSize; i++) {

			Instance ins = arrTestInstances.get(i);

			Instance newIns = swapInstance(ins);

			ScoreSet scoreSet = classifier.scores(ins);
			ScoreSet scoreSetSwap = classifier.scores(newIns);

			double scoreXY = scoreSet.get(Integer
					.toString(Constants.ANCESTOR_E1_TO_E2))
					+ scoreSetSwap.get(Integer
							.toString(Constants.ANCESTOR_E2_TO_E1));

			double scoreYX = scoreSet.get(Integer
					.toString(Constants.ANCESTOR_E2_TO_E1))
					+ scoreSetSwap.get(Integer
							.toString(Constants.ANCESTOR_E1_TO_E2));

			double scoreZZ = scoreSet.get(Integer.toString(Constants.COUSIN))
					+ scoreSetSwap.get(Integer.toString(Constants.COUSIN));

			double scoreNO = scoreSet.get(Integer.toString(Constants.NONE))
					+ scoreSetSwap.get(Integer.toString(Constants.NONE));

			double maxScore = Math.max(scoreXY, Math.max(scoreYX, Math.max(
					scoreZZ, scoreNO)));

			int p;

			if (scoreXY == maxScore)
				p = Constants.ANCESTOR_E1_TO_E2;
			else if (scoreYX == maxScore)
				p = Constants.ANCESTOR_E2_TO_E1;
			else if (scoreZZ == maxScore)
				p = Constants.COUSIN;
			else
				p = Constants.NONE;

			if (p == ins.relation) {
				String out = "T" + "\t" + p + "\t" + ins.toString();
				truePrediction.add(out);
				count++;

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + ins.toString();
				falsePrediction.add(out);

				addToLabelAccuracy(ins, false);
			}

		}

		for (String out : truePrediction)
			System.out.println(out);
		for (String out : falsePrediction)
			System.out.println(out);

		printMapAccuracy();

		System.out.println("Correct: " + count);
		System.out.println("Total: " + testSize);

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) testSize;

		System.out.println("Acc: " + acc);

		return acc;

	}

	/**
	 * @param ins
	 * @return
	 */
	private Instance swapInstance(Instance ins) {
		Instance newIns = new Instance(ins);

		newIns.entity1 = ins.entity2;
		newIns.entity2 = ins.entity1;

		if (ins.relation == Constants.ANCESTOR_E1_TO_E2)
			newIns.relation = Constants.ANCESTOR_E2_TO_E1;
		else if (ins.relation == Constants.ANCESTOR_E2_TO_E1)
			newIns.relation = Constants.ANCESTOR_E1_TO_E2;

		return newIns;
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
	 * @param ins
	 * @param b
	 */
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

	public double doubleTest() {

		System.out.println("Double Test");

		int testSize = Math.min(TEST_SIZE, arrTestInstances.size());
		System.out.println("testSize=" + testSize + "\n");
		int count = 0;

		ArrayList<String> truePrediction = new ArrayList<String>();
		ArrayList<String> falsePrediction = new ArrayList<String>();

		for (int i = 0; i < testSize; i++) {

			Instance ins = arrTestInstances.get(i);
			String pString = classifier.discreteValue(ins);

			int p = Integer.parseInt(pString);

			if (p == ins.relation) {
				String out = "T" + "\t" + p + "\t" + ins.toString();
				truePrediction.add(out);
				count++;

				addToLabelAccuracy(ins, true);

			} else {
				String out = "F" + "\t" + p + "\t" + ins.toString();
				falsePrediction.add(out);

				addToLabelAccuracy(ins, false);
			}

		}

		for (String out : truePrediction)
			System.out.println(out);
		for (String out : falsePrediction)
			System.out.println(out);

		printMapAccuracy();

		System.out.println("Correct: " + count);
		System.out.println("Total: " + testSize);

		double acc = 0.0;
		if (arrTestInstances.size() > 0)
			acc = (double) count / (double) testSize;

		System.out.println("Acc: " + acc);

		return acc;

	}

	public static void main(String[] args) throws Exception {

		// This code is for testing the features read from the data file, and
		// testing the features acquired by LBJ
		// MainClassifier cl = new MainClassifier();
		//
		// for(int i=0;i<10;i++)
		// {
		// System.out.println(trainSet.get(i).scoreCosine_Anc);
		//			
		// FeatureVector f = cl.getExtractor().classify(trainSet.get(i));
		//
		// System.out.println(f);
		//			
		// System.out.println(trainSet.get(i).relation);
		// }

		SimpleSupervised lbj = new SimpleSupervised();

		lbj.train();
		lbj.test();

	}
}
