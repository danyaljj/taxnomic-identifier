/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.experiments;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.emnlp09.identification.RelationIdentification;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Apr 27, 2009
 */
public class DatasetCreation {

	public static final int INPUT_TYPE_GOLD = 0;
	public static final int INPUT_TYPE_PREDICT = 1;
	public static final int INPUT_TYPE_INTERMEDIATE = 2;

	String fname;
	Map<String, Set<String>> mapEntities = null;

	/**
	 * 
	 */
	public DatasetCreation(String file) throws Exception {

		this.fname = file;
		mapEntities = new HashMap<String, Set<String>>();

		parse1();

	}

	public void parse1() throws Exception {

		BufferedReader reader = IOManager.openReader(fname);

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

	}

	/**
	 * 
	 * @param outputFile
	 * @param task
	 *            = (1: ClassClass, 2: ClassEntity, 3: EntityEntity)
	 * @param numPairs
	 */
	public void generateNegExamples(String outputFile, int numPairs, int task) {

		Set<String> setClassName = mapEntities.keySet();

		ArrayList<String> arrClassName = new ArrayList<String>(setClassName);

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		switch (task) {
		// case 1:
		// arrExamples = generateClassClassExample(arrClassName, numPairs);
		// break;
		case 2:
			arrExamples = generateNegClassEntityExample(arrClassName,
					mapEntities, numPairs);
			break;
		case 3:
			arrExamples = generateNegEntityEntityExample(arrClassName,
					mapEntities, numPairs);
			break;
		default:
			System.out.println("ERROR: Incorrect task.");
			System.exit(1);
		}

		if (arrExamples.size() == 0) {
			System.out.println("WARNING: There is no example generated!!!");
		}

		ArrayList<String> arrStringExamples = makeStringExamples(arrExamples,
				INPUT_TYPE_GOLD);

		IOManager.writeLines(arrStringExamples, outputFile);

	}

	public void generateNegExamples(String outputFile, int numPairs) {

		Set<String> setClassName = mapEntities.keySet();

		ArrayList<String> arrClassName = new ArrayList<String>(setClassName);

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		int n1 = numPairs / 2;
		int n2 = numPairs - n1;

		ArrayList<Example> arrCEExamples = generateNegClassEntityExample(
				arrClassName, mapEntities, n1);

		ArrayList<Example> arrEEExamples = generateNegEntityEntityExample(
				arrClassName, mapEntities, n2);

		arrExamples.addAll(arrEEExamples);

		arrExamples.addAll(arrCEExamples);

		ArrayList<String> arrStringExamples = makeStringExamples(arrExamples,
				INPUT_TYPE_GOLD);

		IOManager.writeLines(arrStringExamples, outputFile);

	}

	private ArrayList<Example> generateNegEntityEntityExample(
			ArrayList<String> arrClassName,
			Map<String, Set<String>> mapEntities, int numPairs) {

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		Random randGenerator = new Random();

		Set<String> setIds = new HashSet<String>();

		int sizeClass = arrClassName.size();

		int i = 0;

		int collision = 0;

		while (i < numPairs) {

			int rand1 = randGenerator.nextInt(sizeClass);

			int rand2 = randGenerator.nextInt(sizeClass);

			if (rand1 == rand2)
				continue;

			String className1 = arrClassName.get(rand1);

			String className2 = arrClassName.get(rand2);

			Set<String> setEntities1 = mapEntities.get(className1);

			Set<String> setEntities2 = mapEntities.get(className2);

			ArrayList<String> arrEntities1 = new ArrayList<String>(setEntities1);

			ArrayList<String> arrEntities2 = new ArrayList<String>(setEntities2);

			int sizeEntity1 = arrEntities1.size();

			int sizeEntity2 = arrEntities2.size();

			int rand3 = randGenerator.nextInt(sizeEntity1);

			int rand4 = randGenerator.nextInt(sizeEntity2);

			String id = Integer.toString(rand1) + "_" + Integer.toString(rand2)
					+ "_" + Integer.toString(rand3) + "_"
					+ Integer.toString(rand4);

			if (setIds.contains(id)) {
				collision++;
				if (collision == 1000) {
					System.out.println("Terminated because collision = 1000.");
					break;
				}
				continue;
			} else
				collision = 0;

			String entity1 = arrEntities1.get(rand3);

			String entity2 = arrEntities2.get(rand4);

			Example example = new Example(entity1, entity2);

			example.relation = RelationIdentification.NONE;

			example.entityClass = "_";

			arrExamples.add(example);

			setIds.add(id);

			i++;
		}

		return arrExamples;
	}

	private ArrayList<Example> generateNegClassEntityExample(
			ArrayList<String> arrClassName,
			Map<String, Set<String>> mapEntities, int numPairs) {

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		Random randGenerator = new Random();

		Set<String> setIds = new HashSet<String>();

		int sizeClass = arrClassName.size();

		int i = 0;
		int collision = 0;

		while (i < numPairs) {

			int rand1 = randGenerator.nextInt(sizeClass);

			int rand = randGenerator.nextInt(sizeClass);

			if (rand1 == rand)
				continue;

			String className1 = arrClassName.get(rand1);

			String className2 = arrClassName.get(rand);

			Set<String> setEntities = mapEntities.get(className2);

			ArrayList<String> arrEntities = new ArrayList<String>(setEntities);

			int sizeEntity = arrEntities.size();

			int rand2 = randGenerator.nextInt(sizeEntity);

			String id = Integer.toString(rand1) + "_" + Integer.toString(rand)
					+ "_" + Integer.toString(rand2);

			if (setIds.contains(id)) {
				collision++;
				if (collision == 1000) {
					System.out.println("Terminated because collision = 1000.");
					break;
				}
				continue;
			} else
				collision = 0;

			String entity = arrEntities.get(rand2);

			Example example = new Example(className1, entity);

			example.relation = RelationIdentification.NONE;

			example.entityClass = "_";

			arrExamples.add(example);

			setIds.add(id);

			i++;
		}

		return arrExamples;
	}

	public void generatePosEntityEntityExamples(String outputFile, int numPairs) {

		Set<String> setClassName = mapEntities.keySet();

		ArrayList<Example> arrAllExamples = new ArrayList<Example>();

		for (String className : setClassName) {

			Set<String> setEntities = mapEntities.get(className);

			System.out.println("className = " + className
					+ ", setEntities.size() = " + setEntities.size());

			ArrayList<Example> arrExamples = makeExamples(setEntities,
					className, numPairs);

			arrAllExamples.addAll(arrExamples);

		}

		ArrayList<Example> arrFinalPicks = pickExamples(arrAllExamples,
				numPairs);

		ArrayList<String> arrStringExamples = makeStringExamples(arrFinalPicks,
				INPUT_TYPE_GOLD);

		IOManager.writeLines(arrStringExamples, outputFile);

	}

	private ArrayList<Example> makeExamples(Set<String> setEntities,
			String className, int numPairs) {

		ArrayList<Example> arrAllExamples = makeAllExamples(setEntities,
				className);

		int n = Math.min(numPairs, arrAllExamples.size());

		ArrayList<Example> arrExamples = pickExamples(arrAllExamples, n);

		return arrExamples;
	}

	private ArrayList<Example> pickExamples(ArrayList<Example> arrAllExamples,
			int n) {

		ArrayList<Example> arrExamples = new ArrayList<Example>();
		Random randGenerator = new Random();
		Set<Integer> setInts = new HashSet<Integer>();

		int k = 0;
		int size = arrAllExamples.size();

		while (k < n) {

			int rand = randGenerator.nextInt(size);

			if (setInts.contains(rand))
				continue;

			arrExamples.add(arrAllExamples.get(rand));
			setInts.add(rand);

			k++;

		}

		return arrExamples;

	}

	public static ArrayList<String> makeStringExamples(
			ArrayList<Example> arrExamples, int type) {

		ArrayList<String> arrStringExamples = new ArrayList<String>();

		for (Example e : arrExamples) {

			String className = (e.relation == RelationIdentification.ANCESTOR_E1_TO_E2 || e.relation == RelationIdentification.ANCESTOR_E2_TO_E1) ? "_"
					: e.entityClass;

			String example = null;
			if (type == INPUT_TYPE_GOLD)
				example = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\n";
			else if (type == INPUT_TYPE_INTERMEDIATE) {
				example = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.scoreLeft_Anc + "\t"
						+ e.scoreRight_Anc + "\t" + e.scoreLeft_Cou + "\t"
						+ e.scoreRight_Cou + "\t" + e.ratioCat_Anc + "\t"
						+ e.ratioCat_Cou + "\n";
			} else {
				example = e.relation + "\t" + className + "\t" + e.entity1
						+ "\t" + e.entity2 + "\t" + e.scoreLeft_Anc + "\t"
						+ e.scoreRight_Anc + "\t" + e.scoreLeft_Cou + "\t"
						+ e.scoreRight_Cou + "\t" + e.ratioCat_Anc + "\t"
						+ e.ratioCat_Cou + "\t" + e.finalScore + "\t"
						+ e.predictedRelation + "\n";
			}

			arrStringExamples.add(example);

		}
		return arrStringExamples;
	}

	/**
	 * @param setEntities
	 * @param className
	 * @return
	 */
	private ArrayList<Example> makeAllExamples(Set<String> setEntities,
			String className) {

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		ArrayList<String> arrEntities = new ArrayList<String>(setEntities);

		int n = arrEntities.size();

		for (int i = 0; i < n - 1; i++) {

			String entity1 = arrEntities.get(i);

			for (int j = i + 1; j < n; j++) {

				String entity2 = arrEntities.get(j);
				Example example = new Example(entity1, entity2);
				example.relation = RelationIdentification.COUSIN;
				example.entityClass = className;
				arrExamples.add(example);

			}
		}
		return arrExamples;
	}

	public void generatePosClassEntityExamples(String outputFile, int numPairs) {

		Set<String> setClassName = mapEntities.keySet();

		ArrayList<Example> arrAllExamples = new ArrayList<Example>();

		for (String className : setClassName) {

			Set<String> setEntities = mapEntities.get(className);

			System.out.println("className = " + className
					+ ", setEntities.size() = " + setEntities.size());

			ArrayList<Example> arrExamples = makePosClassEntityExamples(
					setEntities, className, numPairs);

			arrAllExamples.addAll(arrExamples);

		}

		ArrayList<Example> arrFinalPicks = pickExamples(arrAllExamples,
				numPairs);

		ArrayList<String> arrStringExamples = makeStringExamples(arrFinalPicks,
				INPUT_TYPE_GOLD);

		IOManager.writeLines(arrStringExamples, outputFile);

	}

	/**
	 * @param setEntities
	 * @param className
	 * @param numPairs
	 * @return
	 */
	private ArrayList<Example> makePosClassEntityExamples(
			Set<String> setEntities, String className, int numPairs) {

		int num = Math.min(setEntities.size(), numPairs);

		ArrayList<String> arrEntities = new ArrayList<String>(setEntities);

		Random randGenerator = new Random();
		Set<Integer> setInts = new HashSet<Integer>();

		int k = 0;
		int size = arrEntities.size();

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		int collision = 0;

		while (k < num) {

			int rand = randGenerator.nextInt(size);

			if (setInts.contains(rand)) {
				collision++;
				if (collision == 1000) {
					System.out.println("Terminated because collision = 1000.");
					break;
				}
				continue;
			}

			String entity = arrEntities.get(rand);

			Example example = new Example(className, entity);

			example.relation = RelationIdentification.ANCESTOR_E1_TO_E2;

			example.entityClass = "_";

			arrExamples.add(example);

			setInts.add(rand);

			k++;

		}

		return arrExamples;

	}

	public static ArrayList<Example> readExampleFile(String inputFile, int type)
			throws Exception {

		ArrayList<Example> arrExamples = new ArrayList<Example>();

		BufferedReader reader = IOManager.openReader(inputFile);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length < 4)
				continue;

			Example example = new Example(chunks[2], chunks[3]);
			example.relation = Integer.parseInt(chunks[0]);
			example.entityClass = chunks[1];

			if (type == INPUT_TYPE_INTERMEDIATE) {

				if (chunks.length < 10)
					continue;

				example.scoreLeft_Anc = Double.parseDouble(chunks[4]);
				example.scoreRight_Anc = Double.parseDouble(chunks[5]);

				example.scoreLeft_Cou = Double.parseDouble(chunks[6]);
				example.scoreRight_Cou = Double.parseDouble(chunks[7]);

				example.ratioCat_Anc = Double.parseDouble(chunks[8]);
				example.ratioCat_Cou = Double.parseDouble(chunks[9]);

			}

			arrExamples.add(example);

		}

		IOManager.closeReader(reader);

		return arrExamples;
	}

	public static void splitIntermediateFile(String inputFile) {

		String path = "";
		int posE = inputFile.lastIndexOf('/');
		
		if (posE != -1) {
			path = inputFile.substring(0, posE) + "/";
		}
		
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		int n = arrLines.size();

		if (n % 3 != 0) {
			System.out.println("n % 3 != 0");
			System.exit(1);
		}

		int m = n / 3;

		int list[] = new int[] { 5, 10, 20, 50, 100, 200, 500, 1000 };

		for (int k : list) {

			
			ArrayList<String> arrOutLines = new ArrayList<String>();
			
			for (int i = 0; i < k; i++) {
				arrOutLines.add(arrLines.get(i));
			}
			
			for (int i = (m*1+0); i < (m*1+k); i++) {
				arrOutLines.add(arrLines.get(i));
			}

			for (int i = (m*2+0); i < (m*2+k); i++) {
				arrOutLines.add(arrLines.get(i));
			}
			
			String outName = path + Integer.toString(k) + "_tune.inter";
			
			IOManager.writeLinesAddingReturn(arrOutLines, outName);
			
		}

		
		ArrayList<String> arrOutLines = new ArrayList<String>();

		for (int i = (m*1-4000); i < (m*1); i++) {
			arrOutLines.add(arrLines.get(i));
		}
		
		for (int i = (m*2-4000); i < (m*2); i++) {
			arrOutLines.add(arrLines.get(i));
		}

		for (int i = (m*3-4000); i < (m*3); i++) {
			arrOutLines.add(arrLines.get(i));
		}

		String outName = path + "4000_posCposAnegCA.inter";
		
		IOManager.writeLinesAddingReturn(arrOutLines, outName);

	}

	public static void main(String[] args) throws Exception {

		DatasetCreation creator = new DatasetCreation(
				"data/emnlp09/www07-classes.txt");

		System.out.println("Generating examples...");

		// creator.generatePosEntityEntityExamples(
		// "data/emnlp09/posCousin_100.txt", 100);

		// creator.generatePosClassEntityExamples(
		// "data/emnlp09/posAncestor_100.txt", 100);

		creator.generateNegExamples("data/emnlp09/negCousinAncestor_100.txt",
				100);

		System.out.println("Done.");

	}

	/*
	 * public void create1(Map<String, ArrayList<String>> mapInstance) {
	 * 
	 * Map<String, ArrayList<Example>> mapExample = new HashMap<String,
	 * ArrayList<Example>>();
	 * 
	 * Set<String> keySet = mapInstance.keySet();
	 * 
	 * for (String key : keySet) {
	 * 
	 * ArrayList<String> arrInstances = mapInstance.get(key);
	 * 
	 * System.out.println("iClass: " + key + " - Number of instance: " +
	 * arrInstances.size());
	 * 
	 * ArrayList<Example> arrExample = new ArrayList<Example>();
	 * 
	 * for (String ins1 : arrInstances) {
	 * 
	 * for (String ins2 : arrInstances) { Example ex = new Example(ins1, ins2);
	 * arrExample.add(ex); }
	 * 
	 * }
	 * 
	 * mapExample.put(key, arrExample);
	 * 
	 * }
	 * 
	 * //======================
	 * 
	 * Map<Example, Integer> mapCousinPositiveTable = new HashMap<Example,
	 * Integer>(); Map<Example, Integer> mapAncestorPositiveTable = new
	 * HashMap<Example, Integer>(); Map<Example, Integer> mapCousinNegativeTable
	 * = new HashMap<Example, Integer>(); Map<Example, Integer>
	 * mapAncestorNegativeTable = new HashMap<Example, Integer>();
	 * 
	 * // Create cousin positive example pool
	 * 
	 * for ()
	 * 
	 * 
	 * }
	 */

}
