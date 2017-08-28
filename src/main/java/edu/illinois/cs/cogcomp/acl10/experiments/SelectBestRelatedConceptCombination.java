/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.experiments;

import java.util.ArrayList;
import java.util.Map;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.www10.constraints.GraphConstraints;

/**
 * @author dxquang Feb 8, 2010
 */
public class SelectBestRelatedConceptCombination {

	public void selectBestCombination(String interFile,
			String supportInterFile, String constraintFile, String readMode)
			throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		GraphConstraints classifier = new GraphConstraints(constraintFile);

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifier
				.classifySupportingInstances(arrSupportingInstances);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE, mode);

		double bestAcc = -1;
		int[] combination = new int[3];
		for (int i = 0; i <= 5; i++) {
			for (int j = 0; j <= 5; j++) {
				for (int k = 0; k <= 5; k++) {
					ArrayList<Instance> tempInstances = new ArrayList<Instance>();
					for (Instance ins : arrInstances) {
						Instance newIns = new Instance(ins);
						tempInstances.add(newIns);
					}
					double acc = classifier.evaluateWithGraphConstraints(
							tempInstances, mapSupportingPrediction, i, j, k,
							false);
					// double acc = classifer.evaluateWithGraphConstraints(
					// interFile, supportInterFile, readMode, i, j, k,
					// false);
					if (acc > bestAcc) {
						bestAcc = acc;
						combination[0] = i;
						combination[1] = j;
						combination[2] = k;
					}
					System.out.println((i + j + k) + "\t(" + i + "," + j + ","
							+ k + ") : " + acc);
				}
			}
		}

		System.out.println("Best combination:");
		System.out.println("(" + combination[0] + "," + combination[1] + ","
				+ combination[2] + ") : " + bestAcc);
	}

	public void selectBestCombinationRecPreF(String interFile,
			String supportInterFile, String constraintFile, String readMode)
			throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		GraphConstraints classifier = new GraphConstraints(constraintFile);

		// Load the supportingInterFile
		System.out.println("Loading supporting intermidiate file.");
		ArrayList<Instance> arrSupportingInstances = DataHandler
				.readTestingInstances(supportInterFile,
						Constants.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ONLY_WIKI);

		System.out.println("Number of supporting instance: "
				+ arrSupportingInstances.size());

		// Classify supporting instances
		System.out.println("Classifying supporting instances.");
		Map<String, Double[]> mapSupportingPrediction = classifier
				.classifySupportingInstances(arrSupportingInstances);

		// Load the interFile
		System.out.println("Loading the target intermidiate file.");
		ArrayList<Instance> arrInstances = DataHandler
				.readExtendedTestingInstances(interFile,
						Constants.INPUT_TYPE_INTERMEDIATE, mode);

		double bestF = -1;
		int[] combination = new int[3];
		for (int i = 0; i <= 5; i++) {
			for (int j = 0; j <= 5; j++) {
				for (int k = 0; k <= 5; k++) {
					System.out.println();
					ArrayList<Instance> tempInstances = new ArrayList<Instance>();
					for (Instance ins : arrInstances) {
						Instance newIns = new Instance(ins);
						tempInstances.add(newIns);
					}
					Map<String, Double> result = classifier
							.evaluateWithGraphConstraintsRecallPrecisionFscore(
									tempInstances, mapSupportingPrediction, i,
									j, k, false);
					double avgF = result.get("F1_Avg");
					if (avgF > bestF) {
						bestF = avgF;
						combination[0] = i;
						combination[1] = j;
						combination[2] = k;
					}

					System.out.println("Combination: " + "(" + i + "," + j
							+ "," + k + ")");
					for (int key = 0; key < 4; key++) {
						System.out.println(key + ": ("
								+ result.get("PREC_" + key) + ", "
								+ result.get("REC_" + key) + ", "
								+ result.get("F1_" + key) + ")");
					}
					System.out.println("Avg: " + "(" + result.get("PREC_Avg")
							+ ", " + result.get("REC_Avg") + ", " + avgF + ")");
				}
			}
		}

		System.out.println("Best combination:");
		System.out.println("(" + combination[0] + "," + combination[1] + ","
				+ combination[2] + ") : " + bestF);
	}
}
