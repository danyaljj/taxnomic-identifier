/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 11, 2010
 */
public class RecallPrecisionFscore {

	public static void getResults(String inputFile) {
		ArrayList<String> lines = IOManager.readLines(inputFile);

		TreeMap<Integer, Integer> corrects = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> goldTotal = new HashMap<Integer, Integer>();
		Map<Integer, Integer> predictTotal = new HashMap<Integer, Integer>();

		for (String line : lines) {

			if (line.startsWith("T\t") || line.startsWith("F\t")) {
				String[] parts = line.split("\\t+");

				int gold = Integer.parseInt(parts[2]);
				int predict = Integer.parseInt(parts[1]);

				if (gold == predict) {
					if (corrects.containsKey(gold)) {
						int c = corrects.get(gold);
						corrects.put(gold, c + 1);
					} else
						corrects.put(gold, 1);
				}

				if (goldTotal.containsKey(gold)) {
					int c = goldTotal.get(gold);
					goldTotal.put(gold, c + 1);
				} else
					goldTotal.put(gold, 1);

				if (predictTotal.containsKey(predict)) {
					int c = predictTotal.get(predict);
					predictTotal.put(predict, c + 1);
				} else
					predictTotal.put(predict, 1);

			}
		}

		double tPrec = 0.0;
		double tRec = 0.0;
		double tF = 0.0;

		for (int key : corrects.keySet()) {
			double prec = (double) corrects.get(key)
					/ (double) predictTotal.get(key);
			double rec = (double) corrects.get(key)
					/ (double) goldTotal.get(key);
			double f = 2 * ((prec * rec) / (prec + rec));
			tPrec += prec;
			tRec += rec;
			tF += f;
			System.out.println(key + " Precision = " + prec);
			System.out.println(key + " Recall = " + rec);
			System.out.println(key + " F1 = " + f);
		}

		System.out.println("Avg. Prec = " + ((double) tPrec / 4.0));
		System.out.println("Avg. Rec  = " + ((double) tRec / 4.0));
		System.out.println("Avg. F    = " + ((double) tF / 4.0));
	}

	public static Map<String, Double> getResultsInference(List<String> listOut) {

		TreeMap<Integer, Integer> corrects = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> goldTotal = new HashMap<Integer, Integer>();
		Map<Integer, Integer> predictTotal = new HashMap<Integer, Integer>();

		for (String line : listOut) {
			if (line.startsWith("T\t") || line.startsWith("F\t")) {
				String[] parts = line.split("\\t+");

				int gold = Integer.parseInt(parts[3]); // This is the reason why
				// "Inference" is in the
				// name of the function
				int predict = Integer.parseInt(parts[1]);

				if (gold == predict) {
					if (corrects.containsKey(gold)) {
						int c = corrects.get(gold);
						corrects.put(gold, c + 1);
					} else
						corrects.put(gold, 1);
				}

				if (goldTotal.containsKey(gold)) {
					int c = goldTotal.get(gold);
					goldTotal.put(gold, c + 1);
				} else
					goldTotal.put(gold, 1);

				if (predictTotal.containsKey(predict)) {
					int c = predictTotal.get(predict);
					predictTotal.put(predict, c + 1);
				} else
					predictTotal.put(predict, 1);

			}
		}

		double tPrec = 0.0;
		double tRec = 0.0;
		double tF = 0.0;

		Map<String, Double> results = new HashMap<String, Double>();
		for (int key : corrects.keySet()) {
			double prec = (double) corrects.get(key)
					/ (double) predictTotal.get(key);
			double rec = (double) corrects.get(key)
					/ (double) goldTotal.get(key);
			double f = 2 * ((prec * rec) / (prec + rec));
			tPrec += prec;
			tRec += rec;
			tF += f;
			// System.out.println(key + " Precision = " + prec);
			// System.out.println(key + " Recall = " + rec);
			// System.out.println(key + " F1 = " + f);
			results.put("PREC_" + key, prec);
			results.put("REC_" + key, rec);
			results.put("F1_" + key, f);
		}

		double avgPrec = (double) tPrec / (double) corrects.size();
		// System.out.println("Avg. Prec = " + avgPrec);
		double avgRec = (double) tRec / (double) corrects.size();
		// System.out.println("Avg. Rec  = " + avgRec);
		double avgF1 = (double) tF / (double) corrects.size();
		// System.out.println("Avg. F1    = " + avgF1);

		results.put("PREC_Avg", avgPrec);
		results.put("REC_Avg", avgRec);
		results.put("F1_Avg", avgF1);

		return results;
	}

	public static void getMicroAveragePrecision(String inputFile) {
		ArrayList<String> lines = IOManager.readLines(inputFile);

		TreeMap<Integer, Integer> corrects = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> goldTotal = new HashMap<Integer, Integer>();
		Map<Integer, Integer> predictTotal = new HashMap<Integer, Integer>();

		for (String line : lines) {

			if (line.startsWith("T\t") || line.startsWith("F\t")) {
				String[] parts = line.split("\\t+");

				int gold = Integer.parseInt(parts[2]);
				int predict = Integer.parseInt(parts[1]);

				if (gold == predict) {
					if (corrects.containsKey(gold)) {
						int c = corrects.get(gold);
						corrects.put(gold, c + 1);
					} else
						corrects.put(gold, 1);
				}

				if (goldTotal.containsKey(gold)) {
					int c = goldTotal.get(gold);
					goldTotal.put(gold, c + 1);
				} else
					goldTotal.put(gold, 1);

				if (predictTotal.containsKey(predict)) {
					int c = predictTotal.get(predict);
					predictTotal.put(predict, c + 1);
				} else
					predictTotal.put(predict, 1);

			}
		}

		double tPrec = 0.0;
		double tRec = 0.0;
		double tF = 0.0;

		for (int key : corrects.keySet()) {
			double prec = (double) corrects.get(key)
					/ (double) predictTotal.get(key);
			double rec = (double) corrects.get(key)
					/ (double) goldTotal.get(key);
			double f = 2 * ((prec * rec) / (prec + rec));
			tPrec += prec;
			tRec += rec;
			tF += f;
			System.out.println(key + " Precision = " + prec);
			System.out.println(key + " Recall = " + rec);
			System.out.println(key + " F1 = " + f);
		}

		System.out.println("Avg. Prec = " + ((double) tPrec / 4.0));
		System.out.println("Avg. Rec  = " + ((double) tRec / 4.0));
		System.out.println("Avg. F    = " + ((double) tF / 4.0));

		int totalTP = 0;
		int totalPrediction = 0;
		int totalGold = 0;
		for (int key : corrects.keySet()) {
			totalTP += corrects.get(key);
			totalPrediction += predictTotal.get(key);
			totalGold += goldTotal.get(key);
		}
		double microPrec = (double) totalTP / (double) totalPrediction;
		System.out.println("Micro precision = " + microPrec);
		double microRec = (double) totalTP / (double) totalGold;
		System.out.println("Micro recall = " + microRec);
		double microF = 2 * (microPrec * microRec) / (microPrec + microRec);
		System.out.println("Micro f = " + microF);
	}
}
