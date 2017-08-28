/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.setdiscovery.SetDiscovery;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang May 22, 2009
 */
public class DataHandler {

	public static final int READ_ONLY_WIKI = 0;
	public static final int READ_ONLY_NONWIKI = 1;
	public static final int READ_ALL = 2;

	public static ArrayList<Instance> readTrainingInstances(String inputFile,
			int type) throws Exception {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 4)
				continue;

			Instance instance = new Instance(chunks[2], chunks[3]);
			instance.relation = Integer.parseInt(chunks[0]);
			instance.entityClass = chunks[1];

			if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				if (Double.parseDouble(chunks[4]) < 0.0)
					continue;

				instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
				instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
				instance.ratio_CatCat = Double.parseDouble(chunks[6]);

				instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

				instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
				instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
				instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
				instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

			}

			arrInstances.add(instance);

		}

		IOManager.closeReader(reader);

		return arrInstances;
	}

	// This function was added for ACL10
	public static ArrayList<Instance> readTrainingInstancesAddFeats(
			String inputFile, int type) throws Exception {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 4)
				continue;

			Instance instance = new Instance(chunks[2], chunks[3]);
			instance.relation = Integer.parseInt(chunks[0]);
			instance.entityClass = chunks[1];

			if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				if (Double.parseDouble(chunks[4]) < 0.0)
					continue;

				instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
				instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
				instance.ratio_CatCat = Double.parseDouble(chunks[6]);

				instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

				instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
				instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
				instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
				instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

				instance.additionalFeatures1[0] = Integer.parseInt(chunks[12]);
				instance.additionalFeatures1[1] = Integer.parseInt(chunks[13]);
				instance.additionalFeatures1[2] = Integer.parseInt(chunks[14]);

				instance.additionalFeatures2[0] = Integer.parseInt(chunks[15]);
				instance.additionalFeatures2[1] = Integer.parseInt(chunks[16]);
				instance.additionalFeatures2[2] = Integer.parseInt(chunks[17]);

			}

			arrInstances.add(instance);

		}

		IOManager.closeReader(reader);

		return arrInstances;
	}

	public static ArrayList<Instance> readTestingInstances(String inputFile,
			int type, int ignoreUnknow) throws Exception {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			for (int i = 0; i < chunks.length; i++) {
				chunks[i] = chunks[i].trim();
			}

			if (chunks.length < 4)
				continue;

			if (ignoreUnknow == READ_ONLY_WIKI) {
				if (Double.parseDouble(chunks[4]) < 0.0)
					continue;
			} else if (ignoreUnknow == READ_ONLY_NONWIKI) {
				if (Double.parseDouble(chunks[4]) >= 0.0)
					continue;
			}

			Instance instance = new Instance(chunks[2], chunks[3]);
			instance.relation = Integer.parseInt(chunks[0]);
			instance.entityClass = chunks[1];

			if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
				instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
				instance.ratio_CatCat = Double.parseDouble(chunks[6]);

				instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

				instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
				instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
				instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
				instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

			}

			arrInstances.add(instance);

		}

		IOManager.closeReader(reader);

		return arrInstances;
	}

	// This function was added in ACL10
	public static ArrayList<Instance> readTestingInstancesAddFeats(
			String inputFile, int type, int ignoreUnknow) throws Exception {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			for (int i = 0; i < chunks.length; i++) {
				chunks[i] = chunks[i].trim();
			}

			if (chunks.length < 4)
				continue;

			if (ignoreUnknow == READ_ONLY_WIKI) {
				if (Double.parseDouble(chunks[4]) < 0.0)
					continue;
			} else if (ignoreUnknow == READ_ONLY_NONWIKI) {
				if (Double.parseDouble(chunks[4]) >= 0.0)
					continue;
			}

			Instance instance = new Instance(chunks[2], chunks[3]);
			instance.relation = Integer.parseInt(chunks[0]);
			instance.entityClass = chunks[1];

			if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
				instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
				instance.ratio_CatCat = Double.parseDouble(chunks[6]);

				instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

				instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
				instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
				instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
				instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

				instance.additionalFeatures1[0] = Integer.parseInt(chunks[12]);
				instance.additionalFeatures1[1] = Integer.parseInt(chunks[13]);
				instance.additionalFeatures1[2] = Integer.parseInt(chunks[14]);

				instance.additionalFeatures2[0] = Integer.parseInt(chunks[15]);
				instance.additionalFeatures2[1] = Integer.parseInt(chunks[16]);
				instance.additionalFeatures2[2] = Integer.parseInt(chunks[17]);

			}

			arrInstances.add(instance);

		}

		IOManager.closeReader(reader);

		return arrInstances;
	}

	public static ArrayList<Instance> readExtendedTestingInstances(
			String inputFile, int type, int ignoreUnknow) throws Exception {

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 4)
				continue;

			if (ignoreUnknow == READ_ONLY_WIKI) {
				if (Double.parseDouble(chunks[4]) < 0.0)
					continue;
			} else if (ignoreUnknow == READ_ONLY_NONWIKI) {
				if (Double.parseDouble(chunks[4]) >= 0.0)
					continue;
			}

			Instance instance = new Instance(chunks[2], chunks[3]);
			instance.relation = Integer.parseInt(chunks[0]);
			instance.entityClass = chunks[1];

			if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
				instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
				instance.ratio_CatCat = Double.parseDouble(chunks[6]);

				instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

				instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
				instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
				instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
				instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

			}

			instance.textLine = line;

			arrInstances.add(instance);

		}

		IOManager.closeReader(reader);

		return arrInstances;
	}

	public static ArrayList<String> makeStringInstances(
			ArrayList<Instance> arrInstances, int type) {

		ArrayList<String> arrStringInstances = new ArrayList<String>();

		for (Instance e : arrInstances) {

			// String className = (e.relation == Constants.ANCESTOR_E1_TO_E2 ||
			// e.relation == Constants.ANCESTOR_E2_TO_E1) ? "_"
			// : e.entityClass;

			String className = e.entityClass;

			String instance = null;
			if (type == Constants.INPUT_TYPE_GOLD)
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\n";
			else if (type == Constants.INPUT_TYPE_INTERMEDIATE) {
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
						+ e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
						+ e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
						+ e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
						+ e.scoreCos_CatAbs + "\n";
			} else {
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
						+ e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
						+ e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
						+ e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
						+ e.scoreCos_CatAbs + "\t" + e.finalScore + "\t"
						+ e.predictedRelation + "\n";
			}

			arrStringInstances.add(instance);

		}
		return arrStringInstances;
	}

	// This function was added for ACL10
	public static ArrayList<String> makeStringInstancesAdditionalFeatures(
			ArrayList<Instance> arrInstances, int type) {

		ArrayList<String> arrStringInstances = new ArrayList<String>();

		for (Instance e : arrInstances) {

			// String className = (e.relation == Constants.ANCESTOR_E1_TO_E2 ||
			// e.relation == Constants.ANCESTOR_E2_TO_E1) ? "_"
			// : e.entityClass;

			String className = e.entityClass;

			String instance = null;
			if (type == Constants.INPUT_TYPE_GOLD)
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\n";
			else if (type == Constants.INPUT_TYPE_INTERMEDIATE) {
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
						+ e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
						+ e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
						+ e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
						+ e.scoreCos_CatAbs + "\t" + e.additionalFeatures1[0]
						+ "\t" + e.additionalFeatures1[1] + "\t"
						+ e.additionalFeatures1[2] + "\t"
						+ e.additionalFeatures2[0] + "\t"
						+ e.additionalFeatures2[1] + "\t"
						+ e.additionalFeatures2[2] + "\n";
			} else {
				instance = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
						+ e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
						+ e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
						+ e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
						+ e.scoreCos_CatAbs + "\t" + e.finalScore + "\t"
						+ e.predictedRelation + "\t" + e.additionalFeatures1[0]
						+ "\t" + e.additionalFeatures1[1] + "\t"
						+ e.additionalFeatures1[2] + "\t"
						+ e.additionalFeatures2[0] + "\t"
						+ e.additionalFeatures2[1] + "\t"
						+ e.additionalFeatures2[2] + "\n";
			}

			arrStringInstances.add(instance);

		}
		return arrStringInstances;
	}

	public static void createUnsupervisedData(String poolFile, String output) {

		ArrayList<String> arrLines = IOManager.readLines(poolFile);

		ArrayList<String> arrExamples = new ArrayList<String>();
		int n = arrLines.size();

		for (int i = 0; i < n; i++) {

			String x = arrLines.get(i);
			if (x.length() == 0)
				continue;

			for (int j = 0; j < n; j++) {

				if (i == j)
					continue;

				String y = arrLines.get(j);
				if (y.length() == 0)
					continue;

				if (x.equals(y))
					continue;

				String s = "0" + "\t" + "_" + "\t" + x + "\t" + y + "\n";
				s = s.toLowerCase();

				arrExamples.add(s);
			}

		}

		IOManager.writeLines(arrExamples, output);
	}

	public static Map<String, Set<String>> parseClassInstanceFile(
			String classInstanceFile) throws Exception {

		Map<String, Set<String>> mapEntities = new HashMap<String, Set<String>>();

		BufferedReader reader = IOManager.openReader(classInstanceFile);

		String line = "";

		int countClass = 0;
		int countInstance = 0;

		while ((line = reader.readLine()) != null) {

			line = line.trim();
			line = line.toLowerCase();

			String chunks[] = line.split("\\t+");

			if (chunks.length != 2)
				continue;

			String instance = chunks[0];
			String iClass = chunks[1];

			if (mapEntities.containsKey(iClass)) {

				Set<String> setTemp = mapEntities.get(iClass);
				setTemp.add(instance);

			} else {

				Set<String> setTemp = new HashSet<String>();
				setTemp.add(instance);
				mapEntities.put(iClass, setTemp);

				countClass++;
			}

			countInstance++;

		}

		IOManager.closeReader(reader);

		return mapEntities;
	}

	public static void createUnsupervisedDataPool(String classInstanceFile,
			int numEach, String output) throws Exception {

		Map<String, Set<String>> mapEntities = parseClassInstanceFile(classInstanceFile);

		ArrayList<String> arrOutput = new ArrayList<String>();

		Set<String> keySet = mapEntities.keySet();

		for (String key : keySet) {

			arrOutput.add(key);

			ArrayList<String> arrEntities = new ArrayList<String>(mapEntities
					.get(key));

			if (arrEntities.size() <= numEach) {
				for (String ins : arrEntities)
					arrOutput.add(ins);
			} else {
				Random rand = new Random();
				Set<Integer> setInt = new HashSet<Integer>();
				int size = arrEntities.size();
				int i = 0;
				while (i < numEach) {
					int r = rand.nextInt(size);
					if (!setInt.contains(rand)) {
						arrOutput.add(arrEntities.get(r));
						setInt.add(r);
						i++;
					}
				}
			}
		}

		IOManager.writeLinesAddingReturn(arrOutput, output);
	}

	public static void createSameClassExampleFromCohenFile(String cohenFile,
			String output) {
		Map<String, Set<String>> mapEntities = SetDiscovery
				.parseCohenFile(cohenFile);

		Set<String> keySet = mapEntities.keySet();

		ArrayList<String> arrOutput = new ArrayList<String>();
		for (String key : keySet) {

			Set<String> setMembers = mapEntities.get(key);

			ArrayList<String> arrMem1 = new ArrayList<String>(setMembers);
			arrMem1.addAll(setMembers);
			ArrayList<String> arrMem2 = new ArrayList<String>(setMembers);
			arrMem2.addAll(setMembers);

			Set<String> usedPair = new HashSet<String>();

			Collections.shuffle(arrMem1);
			Collections.shuffle(arrMem2);

			int n = arrMem1.size();
			int c = 0;
			for (int i = 0; i < n; i++) {

				String m1 = arrMem1.get(i);
				String m2 = arrMem2.get(i);
				String m1m2 = m1 + "\t" + m2;

				if (usedPair.contains(m1m2))
					continue;

				arrOutput.add(m1m2);

				usedPair.add(m1m2);

				c++;

				if (c == setMembers.size())
					break;
			}

			IOManager.writeLinesAddingReturn(arrOutput, output);
		}
	}

	public static void makeSymmetricDataset(String interFile, String outputFile)
			throws Exception {

		ArrayList<Instance> arrInstance = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE,
				DataHandler.READ_ALL);

		ArrayList<Instance> arrNewInstance = new ArrayList<Instance>();

		for (Instance ins : arrInstance) {

			Instance newIns = new Instance(ins);

			switch (ins.relation) {
			case Constants.NONE:
				String s = ins.entityClass;
				String parts[] = s.split("\\|");
				newIns.entityClass = parts[1] + "|" + parts[0];
				break;
			case Constants.ANCESTOR_E1_TO_E2:
				double x = newIns.ratio_TtlCat;
				newIns.ratio_TtlCat = newIns.ratio_CatTtl;
				newIns.ratio_CatTtl = x;
				newIns.relation = Constants.ANCESTOR_E2_TO_E1;
				break;
			case Constants.ANCESTOR_E2_TO_E1:
				double y = newIns.ratio_TtlCat;
				newIns.ratio_TtlCat = newIns.ratio_CatTtl;
				newIns.ratio_CatTtl = y;
				newIns.relation = Constants.ANCESTOR_E1_TO_E2;
				break;
			case Constants.COUSIN:
				break;
			}

			String c = newIns.entity1;
			newIns.entity1 = newIns.entity2;
			newIns.entity2 = c;

			double t = newIns.scoreCos_AbsCat;
			newIns.scoreCos_AbsCat = newIns.scoreCos_CatAbs;
			newIns.scoreCos_CatAbs = t;

			arrNewInstance.add(ins);
			arrNewInstance.add(newIns);

		}

		ArrayList<String> arrOutput = makeStringInstances(arrNewInstance,
				Constants.INPUT_TYPE_INTERMEDIATE);

		IOManager.writeLines(arrOutput, outputFile);

	}

	public static void createCrossValidationData(String pascaFile,
			int numFolds, int trainPortion, int testPortion, String outputPrefix) {

		// Create data for 5-fold cross validation

		Map<String, Set<String>> mapConcepts = SetDiscovery
				.parsePascaFile(pascaFile);

		ArrayList<String> arrClasses = new ArrayList<String>(mapConcepts
				.keySet());

		int n = arrClasses.size();

		System.out.println("Number of class: " + n);

		for (int i = 1; i <= numFolds; i++) {

			Collections.shuffle(arrClasses);

			Map<String, Set<String>> mapTrains = new HashMap<String, Set<String>>();
			Map<String, Set<String>> mapTests = new HashMap<String, Set<String>>();

			for (int j = 0; j < trainPortion; j++) {
				String key = arrClasses.get(j);
				mapTrains.put(key, mapConcepts.get(key));
			}

			for (int j = n - 1; j > (n - 1) - testPortion; j--) {
				String key = arrClasses.get(j);
				mapTests.put(key, mapConcepts.get(key));
			}

			DatasetCreation creator = new DatasetCreation(mapTrains);

			int num = 2500;

			ArrayList<String> arr0 = creator.generateNegExamples(num);
			ArrayList<String> arr1 = creator
					.generatePosClassEntityExamples(num);
			ArrayList<String> arr2 = creator
					.generatePosEntityClassExamples(num);
			ArrayList<String> arr3 = creator
					.generatePosEntityEntityExamples(num);

			ArrayList<String> arrAll = new ArrayList<String>();
			arrAll.addAll(arr0);
			arrAll.addAll(arr1);
			arrAll.addAll(arr2);
			arrAll.addAll(arr3);

			Collections.shuffle(arrAll);

			ArrayList<String> arrTrains = new ArrayList<String>();
			ArrayList<String> arrTests = new ArrayList<String>();

			for (int k = 0; k < 8000; k++) {
				arrTrains.add(arrAll.get(k));
			}
			for (int k = 8000; k < 10000; k++) {
				arrTests.add(arrAll.get(k));
			}

			String outputFile = outputPrefix + "_train_" + i + ".gold";
			IOManager.writeLines(arrTrains, outputFile);

			outputFile = outputPrefix + "_test_internal_" + i + ".gold";
			IOManager.writeLines(arrTests, outputFile);

			creator = new DatasetCreation(mapTests);

			outputFile = outputPrefix + "_test_external_" + i + ".gold";
			num = 500;

			arr0 = creator.generateNegExamples(num);
			arr1 = creator.generatePosClassEntityExamples(num);
			arr2 = creator.generatePosEntityClassExamples(num);
			arr3 = creator.generatePosEntityEntityExamples(num);

			arrAll = new ArrayList<String>();
			arrAll.addAll(arr0);
			arrAll.addAll(arr1);
			arrAll.addAll(arr2);
			arrAll.addAll(arr3);

			Collections.shuffle(arrAll);
			IOManager.writeLines(arrAll, outputFile);

		}

	}

	public static void extractData(String fromFile, String toFile,
			String outFile, int dataType) throws Exception {
		ArrayList<Instance> arrInstanceFrom = DataHandler.readTestingInstances(
				fromFile, Constants.INPUT_TYPE_INTERMEDIATE,
				DataHandler.READ_ALL);

		ArrayList<String> arrInstanceTo = IOManager.readLines(toFile);

		ArrayList<String> arrInstanceOut = new ArrayList<String>();

		assert (arrInstanceFrom.size() == arrInstanceTo.size());

		for (int i = 0; i < arrInstanceFrom.size(); i++) {
			Instance from = arrInstanceFrom.get(i);
			String to = arrInstanceTo.get(i);

			if (dataType == DataHandler.READ_ONLY_WIKI) {
				if (from.ratio_TtlCat < 0.0)
					continue;
			} else if (dataType == DataHandler.READ_ONLY_NONWIKI) {
				if (from.ratio_TtlCat >= 0.0)
					continue;
			}

			arrInstanceOut.add(to);
		}

		IOManager.writeLinesAddingReturn(arrInstanceOut, outFile);

	}

}
