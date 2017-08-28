/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.web.WebConcept;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityCategorization;
import edu.illinois.cs.cogcomp.test.AllTests;
import edu.illinois.cs.cogcomp.utils.CosineSimilarity;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.Stemmer;
import edu.illinois.cs.cogcomp.utils.StopWord;

/**
 * @author dxquang May 21, 2009
 */
public class FeatureExtraction {

	EntityCategorization categorizer = null;

	ArrayList<String> arrTitleWords1 = new ArrayList<String>();
	ArrayList<String> arrCatWords1 = new ArrayList<String>();
	ArrayList<String> arrAbsWords1 = new ArrayList<String>();

	ArrayList<String> arrTitleWords2 = new ArrayList<String>();
	ArrayList<String> arrCatWords2 = new ArrayList<String>();
	ArrayList<String> arrAbsWords2 = new ArrayList<String>();

	StopWord stopWord = new StopWord(true);

	Stemmer stemmer = new Stemmer();

	public String myEntity1 = null;
	public String myEntity2 = null;

	public static Map<String, String> mapClassMapping = new HashMap<String, String>();
	public static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	public WebConcept wc = new WebConcept();

	public FeatureExtraction(String indexDir, String categoryMapping, String titleMapping, int K) throws Exception {

		categorizer = new EntityCategorization(indexDir, categoryMapping, titleMapping, K);

		loadClassCluster();

	}

	public FeatureExtraction(String indexDir, String categoryMapping, String titleMapping, String idfFile, int K) throws Exception {

		categorizer = new EntityCategorization(indexDir, categoryMapping, titleMapping, idfFile, K);

		loadClassCluster();

	}

	/**
	 * 
	 */
	public static void loadClassCluster() throws Exception {

		BufferedReader reader = IOManager
				.openReader(Constants.FILE_CLASS_CLUSTER);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length != 2)
				continue;

			String key = chunks[0].trim();

			String chunk = chunks[1].trim();

			String parts[] = chunk.split(",");

			if (parts.length == 0)
				continue;

			String value = null;

			Set<String> setValues = new HashSet<String>();

			int i = 0;
			for (String part : parts) {
				part = part.trim();
				if (i == 0)
					value = part;
				setValues.add(part);
				i++;
			}

			if (value == null)
				continue;

			mapClassMapping.put(key, value);
			mapClassCluster.put(value, setValues);

		}

		IOManager.closeReader(reader);

	}

	public void extractFeatures(String inputFile, String outputFile)
			throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler
				.readTrainingInstances(inputFile,
						DatasetCreation.INPUT_TYPE_GOLD);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 1;

		for (Instance instance : arrInputInstances) {

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			featureExtraction(instance);

			arrOutputInstances.add(instance);

			i++;

		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Flushing to file...");

		ArrayList<String> arrStringInstances = DataHandler.makeStringInstances(
				arrOutputInstances, DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		IOManager.writeLines(arrStringInstances, outputFile);

		System.out.println("Done.");

	}

	public void extractFeaturesBigFile(String inputFile, int from, int to,
			String outputFile) throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler.readTrainingInstances(inputFile, DatasetCreation.INPUT_TYPE_GOLD);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		int count = 0;
		for (int i = from; (i < to && i < arrInputInstances.size()); i++) {

			Instance instance = arrInputInstances.get(i);

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			featureExtraction(instance);

			arrInstances.add(instance);

			count++;
			if (count % 1000 == 0) {
				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstances(arrInstances,
								DatasetCreation.INPUT_TYPE_INTERMEDIATE);
				for (String s : arrStringInstances) {
					writer.write(s);
				}
				arrInstances = new ArrayList<Instance>();
				count = 0;
				writer.flush();
			}
		}

		timmer.end();

		if (count > 0) {
			ArrayList<String> arrStringInstances = DataHandler
					.makeStringInstances(arrInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Done.");

	}

	public void extractFeaturesAdditionalFeaturesBigFile(String inputFile,
			int from, int to, String outputFile) throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler
				.readTrainingInstancesAddFeats(inputFile,
						DatasetCreation.INPUT_TYPE_GOLD);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		int count = 0;
		for (int i = from; (i < to && i < arrInputInstances.size()); i++) {

			Instance instance = arrInputInstances.get(i);

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			featureExtractionAdditionalFeatures(instance);

			arrInstances.add(instance);

			count++;
			if (count % 1000 == 0) {
				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstancesAdditionalFeatures(arrInstances,
								DatasetCreation.INPUT_TYPE_INTERMEDIATE);
				for (String s : arrStringInstances) {
					writer.write(s);
				}
				arrInstances = new ArrayList<Instance>();
				count = 0;
				writer.flush();
			}
		}

		timmer.end();

		if (count > 0) {
			ArrayList<String> arrStringInstances = DataHandler
					.makeStringInstancesAdditionalFeatures(arrInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Done.");

	}

	public void extractFeaturesBigFileNaiveDisamb(String inputFile, int from,
			int to, String outputFile) throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler
				.readTrainingInstances(inputFile,
						DatasetCreation.INPUT_TYPE_GOLD);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		int count = 0;
		for (int i = from; (i < to && i < arrInputInstances.size()); i++) {

			Instance instance = arrInputInstances.get(i);

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			featureExtractionNaiveDisamb(instance);

			arrInstances.add(instance);

			count++;
			if (count % 1000 == 0) {
				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstances(arrInstances,
								DatasetCreation.INPUT_TYPE_INTERMEDIATE);
				for (String s : arrStringInstances) {
					writer.write(s);
				}
				arrInstances = new ArrayList<Instance>();
				count = 0;
				writer.flush();
			}
		}

		timmer.end();

		if (count > 0) {
			ArrayList<String> arrStringInstances = DataHandler
					.makeStringInstances(arrInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Done.");

	}

	public void extractFeaturesBeyondWiki(String inputFile, String outputFile)
			throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler
				.readTestingInstances(inputFile,
						DatasetCreation.INPUT_TYPE_INTERMEDIATE,
						DataHandler.READ_ALL);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 0;

		int count = 0;

		for (Instance instance : arrInputInstances) {

			i++;

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			if (instance.ratio_TtlCat >= 0.0) {

				arrOutputInstances.add(instance);

			} else {

				featureExtractionBeyondWik(instance);
				arrOutputInstances.add(instance);

			}

			count++;

			if (count % 1 == 0) {

				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstances(arrOutputInstances,
								DatasetCreation.INPUT_TYPE_INTERMEDIATE);

				for (String s : arrStringInstances) {
					writer.write(s);
				}

				arrOutputInstances = new ArrayList<Instance>();

				count = 0;

				writer.flush();
			}

		}

		timmer.end();

		if (count > 0) {
			ArrayList<String> arrStringInstances = DataHandler
					.makeStringInstances(arrOutputInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		// System.out.println("Flushing to file...");
		//
		// ArrayList<String> arrStringInstances =
		// DataHandler.makeStringInstances(
		// arrOutputInstances, DatasetCreation.INPUT_TYPE_INTERMEDIATE);
		//
		// IOManager.writeLines(arrStringInstances, outputFile);
		//
		// System.out.println("Done.");

	}

	private void initialize() {
		arrTitleWords1 = new ArrayList<String>();
		arrCatWords1 = new ArrayList<String>();
		arrAbsWords1 = new ArrayList<String>();

		arrTitleWords2 = new ArrayList<String>();
		arrCatWords2 = new ArrayList<String>();
		arrAbsWords2 = new ArrayList<String>();
	}

	public void featureExtraction(Instance instance) throws Exception {

		initialize();

		String entity1 = instance.entity1;
		String entity2 = instance.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;

		instance.entity1 = entity1;
		instance.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();
		categorizer.categorize(instance.entity1, instance.entity2);
		timmer.end();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E2);

		} else {

			fillUpWordArray();

			timmer.start();
			getCosineScores(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCosineScores: "
						+ timmer.getTimeMillis());
			}

			timmer.start();
			getPmiScore(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetPmiScore: " + timmer.getTimeMillis());
			}

			getRatios(instance);

		}

	}

	public void featureExtractionAdditionalFeatures(Instance instance)
			throws Exception {

		initialize();

		String entity1 = instance.entity1;
		String entity2 = instance.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;

		instance.entity1 = entity1;
		instance.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();
		categorizer.categorizeAdditionalFeatures(instance.entity1,
				instance.entity2);
		timmer.end();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E2);

		} else {

			fillUpWordArrayAddFeats();

			timmer.start();
			getCosineScores(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCosineScores: "
						+ timmer.getTimeMillis());
			}

			timmer.start();
			getPmiScore(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetPmiScore: " + timmer.getTimeMillis());
			}

			getRatiosAddFeats(instance);

			// for additional features
			instance.additionalFeatures1[0] = categorizer.additionalFeatures1[0];
			instance.additionalFeatures1[1] = categorizer.additionalFeatures1[1];
			instance.additionalFeatures1[2] = categorizer.additionalFeatures1[2];
			instance.additionalFeatures2[0] = categorizer.additionalFeatures2[0];
			instance.additionalFeatures2[1] = categorizer.additionalFeatures2[1];
			instance.additionalFeatures2[2] = categorizer.additionalFeatures2[2];

		}

	}

	public void featureExtractionNaiveDisamb(Instance instance)
			throws Exception {

		initialize();

		String entity1 = instance.entity1;
		String entity2 = instance.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;

		instance.entity1 = entity1;
		instance.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();
		categorizer.categorizeNaiveDisamb(instance.entity1, instance.entity2);
		timmer.end();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E2);

		} else {

			fillUpWordArray();

			timmer.start();
			getCosineScores(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCosineScores: "
						+ timmer.getTimeMillis());
			}

			timmer.start();
			getPmiScore(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetPmiScore: " + timmer.getTimeMillis());
			}

			getRatios(instance);

		}

	}

	public void featureExtractionBeyondWik(Instance instance) throws Exception {

		initialize();

		String entity1 = instance.entity1;
		String entity2 = instance.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;

		instance.entity1 = entity1;
		instance.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();
		categorizer.categorize(instance.entity1, instance.entity2);
		timmer.end();

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			System.out.println("\tWeb search (both): " + instance.entity1
					+ " / " + instance.entity2);
			ArrayList<String> arrConcepts = wc.webConcepts(instance.entity1,
					instance.entity2, 50, 10);
			int n = Math.min(arrConcepts.size(), 5);
			System.out.println("\t\tn=" + n);
			for (int k = 0; k < n - 1; k++) {
				String s1 = arrConcepts.get(k);
				if (s1.equals(instance.entity1))
					continue;
				if (s1.length() == 0)
					continue;
				for (int l = k + 1; l < n; l++) {
					String s2 = arrConcepts.get(l);
					if (s2.equals(instance.entity2))
						continue;
					System.out.println("s1: " + s1 + " / s2: " + s2);
					if (s2.length() == 0)
						continue;
					categorizer.categorize(s1, s2);
					if (categorizer.arrTitles1.size() != 0
							&& categorizer.arrTitles2.size() != 0) {
						l = n;
						k = n;
						instance.entity1 = s1;
						instance.entity2 = s2;
						System.out.println("\t\tFound: " + instance.entity1
								+ " / " + instance.entity2);
					}

				}
			}

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			System.out.println("\tWeb search (first): " + instance.entity1
					+ " / " + instance.entity2);
			ArrayList<String> arrConcepts = wc.webConcepts(instance.entity1,
					instance.entity2, 50, 10);
			int n = Math.min(arrConcepts.size(), 5);
			System.out.println("\t\tn=" + n);
			for (int k = 0; k < n; k++) {
				String s1 = arrConcepts.get(k);
				if (s1.equals(instance.entity1))
					continue;
				System.out.println("s1: " + s1);
				if (s1.trim().length() == 0)
					continue;
				categorizer.categorize(s1, instance.entity2);
				if (categorizer.arrTitles1.size() != 0
						&& categorizer.arrTitles2.size() != 0) {
					k = n;
					instance.entity1 = s1;
					System.out.println("\t\tFound: " + instance.entity1 + " / "
							+ instance.entity2);
				}

			}

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			System.out.println("\tWeb search (second): " + instance.entity1
					+ " / " + instance.entity2);
			ArrayList<String> arrConcepts = wc.webConcepts(instance.entity1,
					instance.entity2, 50, 10);
			int n = Math.min(arrConcepts.size(), 5);
			System.out.println("\t\tn=" + n);
			for (int k = 0; k < n; k++) {
				String s2 = arrConcepts.get(k);
				if (s2.equals(instance.entity2))
					continue;
				System.out.println("s2: " + s2);
				if (s2.trim().length() == 0)
					continue;
				categorizer.categorize(instance.entity1, s2);
				if (categorizer.arrTitles1.size() != 0
						&& categorizer.arrTitles2.size() != 0) {
					k = n;
					instance.entity2 = s2;
					System.out.println("\t\tFound: " + instance.entity1 + " / "
							+ instance.entity2);
				}

			}
		}

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E2);

		} else {

			fillUpWordArray();

			timmer.start();
			getCosineScores(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCosineScores: "
						+ timmer.getTimeMillis());
			}

			timmer.start();
			getPmiScore(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetPmiScore: " + timmer.getTimeMillis());
			}

			getRatios(instance);

		}

	}

	/**
	 * @param instance
	 */
	private void getRatios(Instance instance) {

		instance.ratio_TtlCat = getDirectionalRatio(Constants.FROM_E1_TO_E2);
		// System.out.println("TtlCat: " + instance.ratio_TtlCat);

		instance.ratio_CatTtl = getDirectionalRatio(Constants.FROM_E2_TO_E1);
		// System.out.println("CatTtl: " + instance.ratio_CatTtl);

		instance.ratio_CatCat = getCatCatRatio();

	}

	// This function was added fro ACL10
	private void getRatiosAddFeats(Instance instance) {

		instance.ratio_TtlCat = getDirectionalRatioAddFeats(Constants.FROM_E1_TO_E2);
		// System.out.println("TtlCat: " + instance.ratio_TtlCat);

		instance.ratio_CatTtl = getDirectionalRatioAddFeats(Constants.FROM_E2_TO_E1);
		// System.out.println("CatTtl: " + instance.ratio_CatTtl);

		instance.ratio_CatCat = getCatCatRatio();

	}

	private double getDirectionalRatio(int direction) {

		ArrayList<String> arrTitles = null;
		ArrayList<String> arrCats = null;
		ArrayList<String> arrHeads = null;
		ArrayList<String> arrDomains = null;

		Set<String> setValues = new HashSet<String>();

		if (direction == Constants.FROM_E1_TO_E2) {
			arrTitles = categorizer.arrTitles1;
			arrCats = categorizer.arrCategories2;
			arrHeads = categorizer.arrHeads2;
			arrDomains = categorizer.arrDomains2;

			if (mapClassCluster.containsKey(myEntity1))
				setValues.addAll(mapClassCluster.get(myEntity1));

		} else {
			arrTitles = categorizer.arrTitles2;
			arrCats = categorizer.arrCategories1;
			arrHeads = categorizer.arrHeads1;
			arrDomains = categorizer.arrDomains1;

			if (mapClassCluster.containsKey(myEntity2))
				setValues.addAll(mapClassCluster.get(myEntity2));
		}

		setValues.addAll(arrTitles);

		// System.out.print("setValues: ");
		// for (String s : setValues)
		// System.out.print(s + ", ");
		// System.out.println();

		Set<String> setCats = new HashSet<String>(arrCats);
		Set<String> setHeads = new HashSet<String>(arrHeads);
		Set<String> setDomains = new HashSet<String>(arrDomains);

		Set<String> setAll = new HashSet<String>();
		setAll.addAll(setCats);
		setAll.addAll(setHeads);
		setAll.addAll(setDomains);

		// System.out.print("setAll Value: ");
		// for (String s : setAll)
		// System.out.print(s + ", ");
		// System.out.println();

		Set<String> setInter = new HashSet<String>(setValues);
		setInter.retainAll(setAll);

		Set<String> setUnion = new HashSet<String>(setValues);
		setUnion.addAll(setAll);

		double ratio = 0.0;

		if (setUnion.size() > 0)
			ratio = (double) setInter.size() / (double) setUnion.size();

		// System.out.println("ratio: " + ratio);

		DecimalFormat df = new DecimalFormat("#.###");
		// DecimalFormat df = new DecimalFormat("#.########");
		ratio = Double.parseDouble(df.format(ratio));

		return ratio;

	}

	private double getDirectionalRatioAddFeats(int direction) {

		ArrayList<String> arrTitles = null;
		ArrayList<String> arrCats = null;
		ArrayList<String> arrHeads = null;
		ArrayList<String> arrDomains = null;

		Set<String> setValues = new HashSet<String>();

		if (direction == Constants.FROM_E1_TO_E2) {
			arrTitles = categorizer.arrTitles1;
			arrCats = categorizer.arrCategories2;
			arrHeads = categorizer.arrHeads2;
			arrDomains = categorizer.arrDomains2;

			if (mapClassCluster.containsKey(myEntity1))
				setValues.addAll(mapClassCluster.get(myEntity1));

		} else {
			arrTitles = categorizer.arrTitles2;
			arrCats = categorizer.arrCategories1;
			arrHeads = categorizer.arrHeads1;
			arrDomains = categorizer.arrDomains1;

			if (mapClassCluster.containsKey(myEntity2))
				setValues.addAll(mapClassCluster.get(myEntity2));
		}

		setValues.addAll(arrTitles);

		// System.out.print("setValues: ");
		// for (String s : setValues)
		// System.out.print(s + ", ");
		// System.out.println();

		Set<String> setCats = new HashSet<String>(arrCats);
		Set<String> setHeads = new HashSet<String>(arrHeads);
		Set<String> setDomains = new HashSet<String>(arrDomains);

		Set<String> setAll = new HashSet<String>();
		setAll.addAll(setCats);
		setAll.addAll(setHeads);
		setAll.addAll(setDomains);
		// System.out.print("setAll Value: ");
		// for (String s : setAll)
		// System.out.print(s + ", ");
		// System.out.println();

		Set<String> setInter = new HashSet<String>(setValues);
		setInter.retainAll(setAll);

		Set<String> setUnion = new HashSet<String>(setValues);
		setUnion.addAll(setAll);

		double ratio = 0.0;

		if (setUnion.size() > 0)
			ratio = (double) setInter.size() / (double) setUnion.size();

		// System.out.println("ratio: " + ratio);

		DecimalFormat df = new DecimalFormat("#.###");
		// DecimalFormat df = new DecimalFormat("#.########");
		ratio = Double.parseDouble(df.format(ratio));

		return ratio;

	}

	private double getCatCatRatio() {

		Set<String> setEntity1 = new HashSet<String>(categorizer.arrCategories1);
		setEntity1.addAll(categorizer.arrHeads1);

		Set<String> setEntity2 = new HashSet<String>(categorizer.arrCategories2);
		setEntity2.addAll(categorizer.arrHeads2);

		Set<String> setInter = new HashSet<String>(setEntity1);
		setInter.retainAll(setEntity2);

		Set<String> setUnion = new HashSet<String>(setEntity1);
		setUnion.addAll(setEntity2);

		double ratio = 0.0;
		if (setUnion.size() > 0)
			ratio = (double) setInter.size() / (double) setUnion.size();

		DecimalFormat df = new DecimalFormat("#.###");
		// DecimalFormat df = new DecimalFormat("#.########");
		ratio = Double.parseDouble(df.format(ratio));

		return ratio;
	}

	private void getPmiScore(Instance instance) throws Exception {

		String query = instance.entity1 + " " + instance.entity2;

		int hitBoth = categorizer.disambiguator.getTotalHits(query);

		int hitE1 = categorizer.disambiguator.getTotalHits(instance.entity1);

		int hitE2 = categorizer.disambiguator.getTotalHits(instance.entity2);

		int numDocs = categorizer.disambiguator.textSearcher.numDocs;

		double pE1E2 = (double) hitBoth / (double) numDocs;

		double pE1 = (double) hitE1 / (double) numDocs;

		double pE2 = (double) hitE2 / (double) numDocs;

		double pmi = 0.0;
		if (pE1 * pE2 == 0)
			pmi = 0.0;
		else {
			pmi = Math.log(pE1E2 / (pE1 * pE2));
			// pmi = sigmoid(pmi);
			DecimalFormat df = new DecimalFormat("#.###");
			// DecimalFormat df = new DecimalFormat("#.########");
			pmi = Double.parseDouble(df.format(pmi));
		}
		instance.scorePmi_E1E2 = pmi;
	}

	private double sigmoid(double t) {
		double res = (double) 1 / ((double) 1 + Math.exp(-(t)));
		return res;
	}

	private void setNoTitleResult(Instance instance, double noTitle) {

		instance.scoreCos_AbsCat = noTitle;
		instance.scoreCos_CatAbs = noTitle;

		instance.scorePmi_E1E2 = noTitle;

		instance.scoreCos_CatCat = noTitle;
		instance.scoreCos_AbsAbs = noTitle;

		instance.ratio_TtlCat = noTitle;
		instance.ratio_CatTtl = noTitle;

		instance.ratio_CatCat = noTitle;

	}

	private void fillUpWordArray() {

		fillUpWordArray(categorizer.arrTitles1, arrTitleWords1);
		fillUpWordArray(categorizer.arrCategories1, arrCatWords1);
		fillUpWordArray(categorizer.arrAbstracts1, arrAbsWords1);

		fillUpWordArray(categorizer.arrTitles2, arrTitleWords2);
		fillUpWordArray(categorizer.arrCategories2, arrCatWords2);
		fillUpWordArray(categorizer.arrAbstracts2, arrAbsWords2);

	}

	// This function was added for ACL10
	private void fillUpWordArrayAddFeats() {

		fillUpWordArrayAddFeats(categorizer.arrTitles1, arrTitleWords1);
		fillUpWordArrayAddFeats(categorizer.arrCategories1, arrCatWords1);
		fillUpWordArrayAddFeats(categorizer.arrAbstracts1, arrAbsWords1);

		fillUpWordArrayAddFeats(categorizer.arrTitles2, arrTitleWords2);
		fillUpWordArrayAddFeats(categorizer.arrCategories2, arrCatWords2);
		fillUpWordArrayAddFeats(categorizer.arrAbstracts2, arrAbsWords2);

	}

	private void fillUpWordArray(ArrayList<String> arrStrings,
			ArrayList<String> arrWords) {

		for (String title : arrStrings) {

			ArrayList<String> arrTokens = stopWord.removeStopWords(title);
			for (String token : arrTokens)
				arrWords.add(token);

		}

	}

	// This function was added for ACL10
	private void fillUpWordArrayAddFeats(ArrayList<String> arrStrings,
			ArrayList<String> arrWords) {

		for (String title : arrStrings) {

			arrWords.add(title);

			ArrayList<String> arrTokens = stopWord.removeStopWords(title);
			for (String token : arrTokens)
				arrWords.add(token);

		}

	}

	private void getCosineScores(Instance instance) throws Exception {

		instance.scoreCos_AbsCat = getCosSim(arrAbsWords1, arrCatWords2);
		instance.scoreCos_CatAbs = getCosSim(arrCatWords1, arrAbsWords2);
		instance.scoreCos_CatCat = getCosSim(arrCatWords1, arrCatWords2);
		instance.scoreCos_AbsAbs = getCosSim(arrAbsWords1, arrAbsWords2);

	}

	private double getCosSim(ArrayList<String> arrBagOfWords_1,
			ArrayList<String> arrBagOfWords_2) throws Exception {

		Map<String, Integer> mapTokenFreq1 = getTokenFreq(arrBagOfWords_1);

		Map<String, Integer> mapTokenFreq2 = getTokenFreq(arrBagOfWords_2);

		Set<String> keySet = new HashSet<String>(mapTokenFreq1.keySet());

		Set<String> anotherSet = new HashSet<String>(mapTokenFreq2.keySet());

		keySet.addAll(anotherSet);

		ArrayList<Integer> arrVector1 = new ArrayList<Integer>();
		ArrayList<Integer> arrVector2 = new ArrayList<Integer>();

		for (String key : keySet) {

			if (mapTokenFreq1.containsKey(key)) {
				// arrVector1.add(mapTokenFreq1.get(key));
				arrVector1.add(1);
			} else {
				arrVector1.add(0);
			}

			if (mapTokenFreq2.containsKey(key)) {
				// arrVector2.add(mapTokenFreq2.get(key));
				arrVector2.add(1);
			} else {
				arrVector2.add(0);
			}

		}

		double cosSim = CosineSimilarity.getSimilarity(arrVector1, arrVector2);

		DecimalFormat df = new DecimalFormat("#.###");
		// DecimalFormat df = new DecimalFormat("#.########");
		cosSim = Double.parseDouble(df.format(cosSim));

		return cosSim;
	}

	private Map<String, Integer> getTokenFreq(ArrayList<String> arrTokens) {

		Map<String, Integer> mapTokenFreq = new HashMap<String, Integer>();

		for (String token : arrTokens) {

			if (mapTokenFreq.containsKey(token)) {
				Integer freq = mapTokenFreq.get(token);
				freq++;
				mapTokenFreq.put(token, freq);
			} else {
				Integer freq = new Integer(1);
				mapTokenFreq.put(token, freq);
			}
		}

		return mapTokenFreq;
	}

	public void extractSepSearchFeaturesBigFile(String inputFile, int from,
			int to, String outputFile) throws Exception {

		ArrayList<Instance> arrInputInstances = DataHandler
				.readTrainingInstances(inputFile,
						DatasetCreation.INPUT_TYPE_GOLD);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		System.out.println("Total # of Instances: " + arrInputInstances.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		ArrayList<Instance> arrInstances = new ArrayList<Instance>();

		int count = 0;
		for (int i = from; (i < to && i < arrInputInstances.size()); i++) {

			Instance instance = arrInputInstances.get(i);

			System.out.println("[" + i + "] " + instance.entity1 + " - "
					+ instance.entity2);

			featureSepSearchExtraction(instance);

			arrInstances.add(instance);

			count++;
			if (count % 1000 == 0) {
				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstances(arrInstances,
								DatasetCreation.INPUT_TYPE_INTERMEDIATE);
				for (String s : arrStringInstances) {
					writer.write(s);
				}
				arrInstances = new ArrayList<Instance>();
				count = 0;
				writer.flush();
			}
		}

		timmer.end();

		if (count > 0) {
			ArrayList<String> arrStringInstances = DataHandler
					.makeStringInstances(arrInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Done.");

	}

	public void featureSepSearchExtraction(Instance instance) throws Exception {

		initialize();

		String entity1 = instance.entity1;
		String entity2 = instance.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;

		instance.entity1 = entity1;
		instance.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();
		categorizer.categorizeSepSearch(instance.entity1, instance.entity2);
		timmer.end();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(instance, Constants.NO_TITLE_E2);

		} else {

			fillUpWordArray();

			timmer.start();
			getCosineScores(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCosineScores: "
						+ timmer.getTimeMillis());
			}

			timmer.start();
			getPmiScore(instance);
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetPmiScore: " + timmer.getTimeMillis());
			}

			getRatios(instance);

		}

	}

}
