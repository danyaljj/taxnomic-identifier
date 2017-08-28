/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang
 * Feb 12, 2009
 */
public class MakeExamples {
	
	String inputFile;
	Map<String, Set<String>> mapEntities = null;
	
	/**
	 * 
	 */
	public MakeExamples(String inputFile) {
		this.inputFile = inputFile;
		mapEntities = new HashMap<String, Set<String>>();
		parseInputFile();
	}
	
	private void parseInputFile() {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);
		for (String line : arrLines) {
			String parts[] = line.split("\\t+");
			if (parts.length != 2)
				continue;
			String className = parts[1].toLowerCase().trim();
			String entity = parts[0].toLowerCase().trim();
			if (mapEntities.containsKey(className)) {
				Set<String> setEntities = mapEntities.get(className);
				setEntities.add(entity);	
			}
			else {
				Set<String> setEntities = new HashSet<String>();
				setEntities.add(entity);
				mapEntities.put(className, setEntities);
			}
		}
	}
	
	/**
	 * 
	 * @param outputFile
	 * @param task = (1: ClassClass, 2: ClassEntity, 3: EntityEntity) 
	 * @param numPairs
	 */
	public void generateNegExamples(String outputFile, int numPairs, int task) {

		Set<String> setClassName = mapEntities.keySet();
		
		ArrayList<String> arrClassName = new ArrayList<String>(setClassName);
		
		ArrayList<Example> arrExamples = new ArrayList<Example>();

		switch (task) {
		case 1:
			arrExamples = generateClassClassExample(arrClassName, numPairs);
			break;
		case 2:
			arrExamples = generateClassEntityExample(arrClassName, mapEntities, numPairs);
			break;
		case 3:
			arrExamples = generateEntityEntityExample(arrClassName, mapEntities, numPairs);
			break;
		default:
			System.out.println("ERROR: Incorrect task.");
			System.exit(1);
		}
		
		if (arrExamples.size() == 0) {
			System.out.println("WARNING: There is no example generated!!!");
		}

		BufferedWriter writer = IOManager.openWriter(outputFile);
		
		ArrayList<String> arrStringExamples = makeStringExamples(arrExamples);
		
		IOManager.writeLines(arrStringExamples, writer);

		IOManager.closeWriter(writer);
	}
	
	public void generateNegExamples(String outputFile, int numPairs) {

		BufferedWriter writer = IOManager.openWriter(outputFile);
		
		Set<String> setClassName = mapEntities.keySet();
		
		ArrayList<String> arrClassName = new ArrayList<String>(setClassName);
		
		ArrayList<Example> arrExample = new ArrayList<Example>();
		
		ArrayList<Example> arrTempExamples = null;
		
		int entityEntityNum = numPairs / 2;
		
		int temp = numPairs - entityEntityNum;
		
		int classClassNum = temp / 2;
		
		int classEntityNum = temp - classClassNum;
		
		System.out.println("entityEntityNum=" + entityEntityNum + ", classClassNum=" + classClassNum + ", classEntityNum=" + classEntityNum);
		
		arrTempExamples = generateClassClassExample(arrClassName, numPairs);
		
		for (int i=0; i<classClassNum; i++)
			arrExample.add(arrTempExamples.get(i));

		arrTempExamples = generateClassEntityExample(arrClassName, mapEntities, numPairs);

		for (int i=0; i<classEntityNum; i++)
			arrExample.add(arrTempExamples.get(i));

		arrTempExamples = generateEntityEntityExample(arrClassName, mapEntities, numPairs);
		
		for (int i=0; i<entityEntityNum; i++)
			arrExample.add(arrTempExamples.get(i));
		
		ArrayList<String> arrStringExamples = makeStringExamples(arrExample);
		
		IOManager.writeLines(arrStringExamples, writer);

		IOManager.closeWriter(writer);
	}
	
	/**
	 * @param arrClassName
	 * @param mapEntities2
	 * @param numPairs
	 * @return
	 */
	private ArrayList<Example> generateClassEntityExample(
			ArrayList<String> arrClassName,
			Map<String, Set<String>> mapEntities, int numPairs) {
		
		ArrayList<Example> arrExamples = new ArrayList<Example>();
		
		Random randGenerator = new Random();
		
		Set<String> setIds = new HashSet<String>();
		
		int sizeClass = arrClassName.size();
		
		int i = 0;
		
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
			
			String id = Integer.toString(rand1) + "_" + Integer.toString(rand2);
			
			if (setIds.contains(id))
				continue;

			String entity = arrEntities.get(rand2);
			
			Example example = new Example(className1, entity);

			example.className = "NONE";
			
			arrExamples.add(example);
			
			setIds.add(id);
			
			i ++;
		}
		
		return arrExamples;
	}

	
	private ArrayList<Example> generateEntityEntityExample(
			ArrayList<String> arrClassName,
			Map<String, Set<String>> mapEntities, int numPairs) {
		
		ArrayList<Example> arrExamples = new ArrayList<Example>();
		
		Random randGenerator = new Random();
		
		Set<String> setIds = new HashSet<String>();
		
		int sizeClass = arrClassName.size();
		
		int i = 0;
		
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
				
			String id = Integer.toString(rand1) + "_" + Integer.toString(rand2) + "_" + Integer.toString(rand3) + "_" + Integer.toString(rand4);
			
			if (setIds.contains(id))
				continue;

			String entity1 = arrEntities1.get(rand3);

			String entity2 = arrEntities2.get(rand4);
			
			Example example = new Example(entity1, entity2);
			
			example.className = "NONE";
			
			arrExamples.add(example);
			
			setIds.add(id);
			
			i ++;
		}
		
		return arrExamples;
	}

	/**
	 * @param arrClassName
	 * @param numPairs
	 * @return
	 */
	private ArrayList<Example> generateClassClassExample(
			ArrayList<String> arrClassName, int numPairs) {
		
		ArrayList<Example> arrExamples = new ArrayList<Example>();

		Random randGenerator = new Random();
		
		Set<String> setIds = new HashSet<String>();
		
		int size = arrClassName.size();
		
		int i = 0;
		
		while (i < numPairs) {
		
			int rand1 = randGenerator.nextInt(size);
			
			int rand2 = randGenerator.nextInt(size);
			
			if (rand1 == rand2)
				continue;
			
			String id = Integer.toString(rand1) + "_" + Integer.toString(rand2);
			
			if (setIds.contains(id))
				continue;

			String entity1 = arrClassName.get(rand1);
			String entity2 = arrClassName.get(rand2);
			
			Example example = new Example(entity1, entity2);
			example.className = "NONE";
			
			arrExamples.add(example);
			
			setIds.add(id);
			
			i ++;
			
		}
		
		return arrExamples;
	}

	public void generateExamples(String outputFile, int numPairs) {
		
		BufferedWriter writer = IOManager.openWriter(outputFile);
		Set<String> setClassName = mapEntities.keySet();
		
		for (String className : setClassName) {

			Set<String> setEntities = mapEntities.get(className);
			
			System.out.println("className = " + className + ", setEntities.size() = " + setEntities.size());
			
			ArrayList<Example> arrExamples = makeExamples(setEntities, className, numPairs);
						
			// System.out.println("arrExamples.size() = " + arrExamples.size());
			
			ArrayList<String> arrStringExamples = makeStringExamples(arrExamples);
		
			IOManager.writeLines(arrStringExamples, writer);
		}
		
		IOManager.closeWriter(writer);
	}

	/**
	 * @param arrExamples
	 * @return
	 */
	private ArrayList<String> makeStringExamples(ArrayList<Example> arrExamples) {
		ArrayList<String> arrStringExamples = new ArrayList<String>();
		for (Example e : arrExamples) {

			String example = e.className + "\t" + e.entity1 + "\t" + e.entity2 + "\n";
			arrStringExamples.add(example);
		
		}
		return arrStringExamples;
	}

	/**
	 * @param setEntities
	 * @param className
	 * @param numPairs
	 * @return
	 */
	private ArrayList<Example> makeExamples(Set<String> setEntities,
			String className, int numPairs) {
		ArrayList<Example> arrAllExamples = makeAllExamples(setEntities, className);
		int n = Math.min(numPairs, arrAllExamples.size());
		ArrayList<Example> arrExamples = pickExamples(arrAllExamples, n);
		return arrExamples;
	}

	/**
	 * @param arrAllExamples
	 * @param n
	 * @return
	 */
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
		for (int i=0; i<n-1; i++) {
			String entity1 = arrEntities.get(i);
			for (int j=i+1; j<n; j++) {
				String entity2 = arrEntities.get(j);
				Example example = new Example(entity1, entity2);
				example.className = className;
				arrExamples.add(example);
			}
		}
		return arrExamples;
	}
}
