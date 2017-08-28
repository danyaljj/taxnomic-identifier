/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.setdiscovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * @author dxquang Jun 3, 2009
 */

public class SetDiscovery {

	public static final String PMI_FILE = "pmi_value.txt";

	public void createAllAncestorRelation(String inputFile, String outputFile) {

		Map<String, Set<String>> mapEntities = parseCohenFile(inputFile);

		ArrayList<String> arrExamples = new ArrayList<String>();
		Set<String> setClass = mapEntities.keySet();

		for (String c : setClass) {

			for (String s : setClass) {

				Set<String> setMember = mapEntities.get(s);

				for (String m : setMember) {

					String ex;
					if (c.equals(s)) {
						ex = "1\t" + c + "\t" + c + "\t" + m + "\n";
					} else
						ex = "0\t" + c + "\t" + c + "\t" + m + "\n";
					arrExamples.add(ex);

				}
			}
		}

		IOManager.writeLines(arrExamples, outputFile);

	}

	public static Map<String, Set<String>> parseCohenFile(String inputFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		Map<String, Set<String>> mapEntities = new HashMap<String, Set<String>>();

		int countClass = 0;
		int countMember = 0;

		for (String line : arrLines) {

			line = line.trim();
			line = line.toLowerCase();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 2) {
				System.out.println(line);
				continue;
			}

			String className = chunks[0];
			String member = chunks[1];

			if (mapEntities.containsKey(className)) {

				Set<String> setTemp = mapEntities.get(className);
				if (setTemp.contains(member))
					System.out.println(member);
				setTemp.add(member);

			} else {

				Set<String> setTemp = new HashSet<String>();
				setTemp.add(member);
				mapEntities.put(className, setTemp);

				countClass++;
			}

			countMember++;

		}
		return mapEntities;
	}

	public static Map<String, Set<String>> parsePascaFile(String inputFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		Map<String, Set<String>> mapEntities = new HashMap<String, Set<String>>();

		int countClass = 0;
		int countMember = 0;

		for (String line : arrLines) {

			line = line.trim();
			line = line.toLowerCase();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 2) {
				System.out.println(line);
				continue;
			}

			String member = chunks[0];
			String className = chunks[1];

			if (mapEntities.containsKey(className)) {

				Set<String> setTemp = mapEntities.get(className);
				if (setTemp.contains(member))
					System.out.println(member);
				setTemp.add(member);

			} else {

				Set<String> setTemp = new HashSet<String>();
				setTemp.add(member);
				mapEntities.put(className, setTemp);

				countClass++;
			}

			countMember++;

		}
		return mapEntities;
	}

	public void createAllCousinRelation(String cohenFile, String seedFile,
			String outputFile) {

		Map<String, Set<String>> mapEntities = parseCohenFile(cohenFile);
		Map<String, Set<String>> mapSeeds = parseCohenFile(seedFile);

		ArrayList<String> arrExamples = new ArrayList<String>();

		Set<String> setEntityClass = mapEntities.keySet();
		Set<String> setSeedClass = mapSeeds.keySet();

		for (String c : setSeedClass) {

			Set<String> setSeeds = mapSeeds.get(c);

			for (String seed : setSeeds) {

				for (String s : setEntityClass) {

					Set<String> setMember = mapEntities.get(s);

					for (String m : setMember) {

						String ex;

						if (c.equals(s)) {
							ex = "3\t" + c + "|" + s + "\t" + seed + "\t" + m
									+ "\n";
						} else
							ex = "0\t" + c + "|" + s + "\t" + seed + "\t" + m
									+ "\n";
						arrExamples.add(ex);

					}
				}
			}
		}

		IOManager.writeLines(arrExamples, outputFile);

	}

	public void discovery(String interFile) throws Exception {

		ArrayList<Instance> arrInstance = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

		System.out.println("Testing size: " + arrInstance.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstance) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}

		RelationClassifier classifier = new RelationClassifier();

		for (Instance ins : arrInstance) {

			ScoreSet scoreSet = classifier.scores(ins);

			double ancestorScore = scoreSet.get(Integer
					.toString(Constants.ANCESTOR_E1_TO_E2));

			ins.finalScore = ancestorScore;

		}

		sortScore(arrInstance);

		System.out.println("Testing size: " + arrInstance.size());

		Instance.printInstanceScores(arrInstance);

		precisionTopK(arrInstance, 1000);

	}

	public void separateDiscovery(String interFile, String interSeedFile,
			String cohenSeedFile) throws Exception {

		ArrayList<Instance> arrInstance = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

		ArrayList<Instance> arrSeedInstance = DataHandler.readTestingInstances(
				interSeedFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

		Map<String, Instance> mapStringInstance = new HashMap<String, Instance>();
		for (Instance ins : arrSeedInstance) {
			String key = ins.entity1 + "_" + ins.entity2;
			mapStringInstance.put(key, ins);
		}

		Map<String, Set<String>> mapSeeds = parseCohenFile(cohenSeedFile);

		System.out.println("Testing size: " + arrInstance.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstance) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}
		
		System.out.println("Largest PMI = " + largestPMI);

		RelationClassifier classifier = new RelationClassifier();

		Map<String, ArrayList<Instance>> mapInstance = new HashMap<String, ArrayList<Instance>>();

		for (Instance ins : arrInstance) {
			String className = ins.entityClass;

			if (mapInstance.containsKey(className)) {
				ArrayList<Instance> arrIns = mapInstance.get(className);
				arrIns.add(ins);
			} else {
				ArrayList<Instance> arrIns = new ArrayList<Instance>();
				arrIns.add(ins);
				mapInstance.put(className, arrIns);
			}
		}

		Set<String> keySet = mapInstance.keySet();

		Softmax softMax = new Softmax();

		for (String key : keySet) {

			System.out.println("key = " + key);
			ArrayList<Instance> arrIns = mapInstance.get(key);

			Set<String> setSeeds = mapSeeds.get(key);

			for (Instance ins : arrIns) {

				ScoreSet scoreSet = classifier.scores(ins);
				softMax.normalize(scoreSet);

				double XYScore = scoreSet.get(Integer
						.toString(Constants.ANCESTOR_E1_TO_E2));
				
				double ZZSumScore = 0.0;
				for (String seed : setSeeds) {
					String stringInstance = seed + "_" + ins.entity2;
					Instance prototype = mapStringInstance.get(stringInstance);
					ScoreSet sS = classifier.scores(prototype);
					softMax.normalize(sS);
					ZZSumScore += sS.get(Integer.toString(Constants.COUSIN));
				}

				ins.testXY = XYScore;
				ins.testYX = scoreSet.get(Integer
						.toString(Constants.ANCESTOR_E2_TO_E1));
				ins.testZZ = scoreSet.get(Integer
						.toString(Constants.COUSIN));
				ins.testNO = scoreSet.get(Integer
						.toString(Constants.NONE));
				ins.finalScore = XYScore*ZZSumScore;
				// ins.finalScore = (XYScore) / (YXScore + ZZScore + NOScore);

			}

			sortScore(arrIns);
			System.out.println("\n*" + key);
			int count = 0;
			for (Instance ins : arrIns) {
				System.out.println(ins.relation + "\t" + ins.finalScore + "\t"
						+ ins.entity1 + "\t"
						+ ins.entity2 + "\t" + ins.testXY + "\t"
						+ ins.testYX + "\t" + ins.testZZ + "\t" + ins.testNO);
				count++;
				if (count == 100)
					break;
			}
			precisionTopK(arrIns, 100);
		}

		// sortScore(arrInstance);

		// System.out.println("Testing size: " + arrInstance.size());

		// Instance.printInstanceScores(arrInstance);

	}

	public void separateDiscovery(String interFile) throws Exception {

		ArrayList<Instance> arrInstance = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

		System.out.println("Testing size: " + arrInstance.size());

		double largestPMI = getLargestPMI(PMI_FILE);

		for (Instance ins : arrInstance) {
			ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
		}
		
		System.out.println("Largest PMI = " + largestPMI);

		RelationClassifier classifier = new RelationClassifier();

		Map<String, ArrayList<Instance>> mapInstance = new HashMap<String, ArrayList<Instance>>();

		for (Instance ins : arrInstance) {
			String className = ins.entityClass;

			if (mapInstance.containsKey(className)) {
				ArrayList<Instance> arrIns = mapInstance.get(className);
				arrIns.add(ins);
			} else {
				ArrayList<Instance> arrIns = new ArrayList<Instance>();
				arrIns.add(ins);
				mapInstance.put(className, arrIns);
			}
		}

		Set<String> keySet = mapInstance.keySet();

		Softmax softMax = new Softmax();

		for (String key : keySet) {

			System.out.println("key = " + key);
			ArrayList<Instance> arrIns = mapInstance.get(key);

			for (Instance ins : arrIns) {

				ScoreSet scoreSet = classifier.scores(ins);
				softMax.normalize(scoreSet);

				double XYScore = scoreSet.get(Integer
						.toString(Constants.ANCESTOR_E1_TO_E2));
				
				ins.finalScore = XYScore;
				// ins.finalScore = (XYScore) / (YXScore + ZZScore + NOScore);

			}

			sortScore(arrIns);
			System.out.println("\n*" + key);
			int count = 0;
			for (Instance ins : arrIns) {
				System.out.println(ins.relation + "\t" + ins.finalScore + "\t"
						+ ins.entityClass + "\t" + ins.entity1 + "\t"
						+ ins.entity2 + "\t" + ins.ratio_TtlCat + "\t"
						+ ins.ratio_CatTtl + "\t" + ins.ratio_CatCat);
				count++;
				if (count == 100)
					break;
			}
			precisionTopK(arrIns, 100);
		}

	}

	public double precisionTopK(ArrayList<Instance> arrIns, int topK) {
		double precision = 0.0;

		int correct = 0;
		int count = 1;
		for (Instance ins : arrIns) {
			if (ins.relation == Constants.ANCESTOR_E1_TO_E2)
				correct++;

			precision = (double) correct / (double) count;

			System.out.println(count + "\t" + precision);

			count++;
			if (count > topK)
				break;
		}

		precision = (double) correct / (double) count;

		return precision;
	}

	private double getLargestPMI(String pmiFile) {
		double pmiValue = 0.0;
		ArrayList<String> arrLines = IOManager.readLines(pmiFile);
		String pmi = arrLines.get(0);
		pmi = pmi.trim();
		pmiValue = Double.parseDouble(pmi);
		return pmiValue;
	}

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
