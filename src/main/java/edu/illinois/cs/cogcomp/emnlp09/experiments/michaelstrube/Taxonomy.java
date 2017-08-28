/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.experiments.michaelstrube;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javatools.parsers.PlingStemmer;

import edu.illinois.cs.cogcomp.emnlp09.experiments.DatasetCreation;
import edu.illinois.cs.cogcomp.emnlp09.experiments.Example;
import edu.illinois.cs.cogcomp.emnlp09.identification.RelationIdentification;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Apr 29, 2009
 */
public class Taxonomy {

	public static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	public static Map<String, String> mapClassMapping = new HashMap<String, String>();
	public static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	int K;

	Map<String, Set<Integer>> mapConcept = new HashMap<String, Set<Integer>>();

	Map<Integer, Set<Integer>> mapIsa = new HashMap<Integer, Set<Integer>>();

	/**
	 * 
	 */
	public Taxonomy(String conceptFile, String isaFile) throws Exception {

		System.out.println("Loading concepts...");
		readConceptFile(conceptFile);

		System.out.println("Loading isa...");
		readIsAFile(isaFile);

		System.out.println("Done.");
		
		loadClassCluster();

		K = 10;

	}

	/**
	 * @param isaFile
	 */
	private void readIsAFile(String isaFile) {

		ArrayList<String> arrIsA = IOManager.readLines(isaFile);

		for (String isa : arrIsA) {

			isa = isa.trim();

			String chunks[] = isa.split("\\t+");

			if (chunks.length != 2)
				continue;

			Integer i1 = Integer.parseInt(chunks[0]);
			Integer i2 = Integer.parseInt(chunks[1]);

			if (mapIsa.containsKey(i1)) {
				Set<Integer> arrInt = mapIsa.get(i1);
				arrInt.add(i2);
			} else {
				Set<Integer> setInt = new HashSet<Integer>();
				setInt.add(i2);
				mapIsa.put(i1, setInt);
			}

		}
	}

	/**
	 * @param conceptFile
	 */
	private void readConceptFile(String conceptFile) {

		ArrayList<String> arrConcepts = IOManager.readLines(conceptFile);

		for (String concept : arrConcepts) {

			concept = concept.trim();

			String chunks[] = concept.split("\\t+");

			if (chunks.length < 2)
				continue;

			Integer value = Integer.parseInt(chunks[0]);

			String key = chunks[1];

			key = formatKey(key);

			key = PlingStemmer.stem(key);
			
			if (mapConcept.containsKey(key)) {
				Set<Integer> setInt = mapConcept.get(key);
				setInt.add(value);
			} else {
				Set<Integer> setInt = new HashSet<Integer>();
				setInt.add(value);
				mapConcept.put(key, setInt);
			}

		}
	}

	/**
	 * @param key
	 * @return
	 */
	private String formatKey(String key) {

		key = key.replaceAll("\\p{Punct}", " ");
		key = key.toLowerCase();
		key = key.replaceAll("\\s+", " ");

		return key;

	}

	public Set<Integer> getCategory(String input) {

		Set<Integer> setCat = new HashSet<Integer>();

		input = formatKey(input);

		// System.out.println("input: " + input);

		if (mapConcept.containsKey(input)) {

			Set<Integer> setInt = mapConcept.get(input);

			for (Integer c : setInt) {

				setCat.add(c);
				setCat.addAll(categorize(c, 0));

			}

		}

		return setCat;
	}

	/**
	 * @param c
	 * @return
	 */
	private Set<Integer> categorize(Integer c, int level) {

		Set<Integer> setCat = new HashSet<Integer>();

		if (level >= K)
			return setCat;

		if (mapIsa.containsKey(c)) {

			Set<Integer> setInt = mapIsa.get(c);

			for (Integer i : setInt) {
				setCat.add(i);

				level++;
				setCat.addAll(categorize(i, level));
				level--;
			}
		}

		return setCat;
	}

	public int identify(String entity1, String entity2) {

		entity1 = formatKey(entity1);
		entity2 = formatKey(entity2);

		int relation = RelationIdentification.NONE;

		Set<String> setCluster1 = new HashSet<String>();

		Set<Integer> setCluster1_Int = new HashSet<Integer>();

		if (mapClassMapping.containsKey(entity1)) {

			entity1 = mapClassMapping.get(entity1);
			setCluster1 = mapClassCluster.get(entity1);

			for (String s : setCluster1) {
				if (mapConcept.containsKey(s)) {
					setCluster1_Int.addAll(mapConcept.get(s));
				}
			}

			if (mapConcept.containsKey(entity1))
				setCluster1_Int.addAll(mapConcept.get(entity1));

		}

		Set<Integer> setCat1 = getCategory(entity1);

//		System.out.println("\nEntity1: " + entity1);
//		for (Integer s : setCat1)
//			System.out.println(s);

		Set<Integer> setCat2 = getCategory(entity2);

//		System.out.println("\nEntity2: " + entity2);
//		for (Integer s : setCat2)
//			System.out.println(s);

		// Check if entity1 is the parent of entity2

		setCluster1_Int.retainAll(setCat2);

		if (setCluster1_Int.size() > 0) {

			relation = RelationIdentification.ANCESTOR_E1_TO_E2;

		} else {

			setCat1.retainAll(setCat2);

			if (setCat1.size() > 0)
				relation = RelationIdentification.COUSIN;

			else
				relation = RelationIdentification.NONE;
		}

		return relation;

	}

	public void evaluate(String intermediateFile) throws Exception {

		ArrayList<Example> arrInputExamples = DatasetCreation.readExampleFile(
				intermediateFile, DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		System.out.println("Total # of examples: " + arrInputExamples.size());

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 1;

		int totalAnc = 0;
		int totalCou = 0;
		int totalNon = 0;

		int correct = 0;

		int correctAnc = 0;
		int incorrectAnc = 0;

		int correctCou = 0;
		int incorrectCou = 0;

		int correctNon = 0;
		int incorrectNon = 0;

		int n = arrInputExamples.size();

		for (Example example : arrInputExamples) {

			// System.out.println("[" + i + "] " + example.entity1 + " - "
			// + example.entity2);

			example.entity1 = PlingStemmer.stem(example.entity1);
			example.entity2 = PlingStemmer.stem(example.entity2);

			int relation = identify(example.entity1, example.entity2);

			if (example.relation == RelationIdentification.ANCESTOR_E1_TO_E2
					|| example.relation == RelationIdentification.ANCESTOR_E2_TO_E1)
				totalAnc++;
			else if (example.relation == RelationIdentification.COUSIN)
				totalCou++;
			else if (example.relation == RelationIdentification.NONE)
				totalNon++;

			if (relation == example.relation) {

				correct++;

				if (example.relation == RelationIdentification.COUSIN)
					correctCou++;

				else if (example.relation == RelationIdentification.ANCESTOR_E1_TO_E2
						|| example.relation == RelationIdentification.ANCESTOR_E2_TO_E1)
					correctAnc++;

				else if (example.relation == RelationIdentification.NONE)
					correctNon++;

			}

			else {

				if (relation == RelationIdentification.COUSIN)
					incorrectCou++;

				else if (relation == RelationIdentification.ANCESTOR_E1_TO_E2
						|| relation == RelationIdentification.ANCESTOR_E2_TO_E1)
					incorrectAnc++;

				else if (relation == RelationIdentification.NONE)
					incorrectNon++;
			}

			i++;

		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		double accuracy = (double) correct / (double) n;

		System.out.println("- Accuracy: " + accuracy + " (" + correct + "/" + n
				+ ")");

		double precision;
		double recall;
		double fscore;
		double avgF1 = 0;

		System.out.println("\n- Ancestor");

		precision = (double) correctAnc / (double) (correctAnc + incorrectAnc);
		System.out.println("\t+ Precision: " + precision + " (" + correctAnc
				+ "/" + (correctAnc + incorrectAnc) + ")");

		recall = (double) correctAnc / (double) (totalAnc);
		System.out.println("\t+ Recall: " + recall + " (" + correctAnc + "/"
				+ (totalAnc) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- Cousin");

		precision = (double) correctCou / (double) (correctCou + incorrectCou);
		System.out.println("\t+ Precision: " + precision + " (" + correctCou
				+ "/" + (correctCou + incorrectCou) + ")");

		recall = (double) correctCou / (double) (totalCou);
		System.out.println("\t+ Recall: " + recall + " (" + correctCou + "/"
				+ (totalCou) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- None");

		precision = (double) correctNon / (double) (correctNon + incorrectNon);
		System.out.println("\t+ Precision: " + precision + " (" + correctNon
				+ "/" + (correctNon + incorrectNon) + ")");

		recall = (double) correctNon / (double) (totalNon);
		System.out.println("\t+ Recall: " + recall + " (" + correctNon + "/"
				+ (totalNon) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- Average F1: " + (avgF1 / (double) 3));

	}

	public static void loadClassCluster() throws Exception {

		BufferedReader reader = IOManager.openReader(FILE_CLASS_CLUSTER);

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
				part = PlingStemmer.stem(part);
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

}
