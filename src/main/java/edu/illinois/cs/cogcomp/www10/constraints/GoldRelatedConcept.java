/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javatools.parsers.PlingStemmer;


import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Oct 20, 2009
 */
public class GoldRelatedConcept {

	private static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	private static Map<String, String> mapClassMapping = new HashMap<String, String>();
	private static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	Map<String, String> mapConceptParent = null;
	Map<String, ArrayList<String>> mapParentConcepts = null;

	Random generator = null;

	/**
	 * @throws Exception
	 * 
	 */
	public GoldRelatedConcept(String originalFile) throws Exception {

		mapConceptParent = new HashMap<String, String>();
		mapParentConcepts = new HashMap<String, ArrayList<String>>();
		generator = new Random();

		loadOriginalFile(originalFile);
		loadClassCluster();
	}

	/**
	 * @param originalFile
	 */
	private void loadOriginalFile(String originalFile) {

		ArrayList<String> arrLines = IOManager.readLines(originalFile);

		for (String line : arrLines) {

			line = line.trim();
			line = line.toLowerCase();

			String[] parts = line.split("\\t+");

			if (parts.length != 2)
				continue;

			String concept = parts[0];
			String parent = parts[1];

			mapConceptParent.put(concept, parent);

			if (!mapParentConcepts.containsKey(parent)) {
				ArrayList<String> arrConcepts = new ArrayList<String>();
				arrConcepts.add(concept);
				mapParentConcepts.put(parent, arrConcepts);
			} else {
				ArrayList<String> arrConcepts = mapParentConcepts.get(parent);
				arrConcepts.add(concept);
			}

		}
	}

	public void getRelatedConcepts(String interFile, String outputFile,
			int maxAnc, int maxSib, int maxChild) throws Exception {

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

		ArrayList<String> arrNewInstances = DataHandler.makeStringInstances(
				arrInstances, Constants.INPUT_TYPE_INTERMEDIATE);

		ArrayList<String> arrOutput = new ArrayList<String>();

		int n = arrInstances.size();
		for (int i = 0; i < n; i++) {

			Instance ins = arrInstances.get(i);
			String newIns = arrNewInstances.get(i);
			newIns = newIns.trim();

			System.out.println(newIns);

			String concept1 = ins.entity1;
			String concept2 = ins.entity2;

			if (ins.relation == Constants.ANCESTOR_E1_TO_E2) {
				concept1 = ins.entityClass;
			}

			if (ins.relation == Constants.ANCESTOR_E2_TO_E1) {
				concept2 = ins.entityClass;
			}

			ArrayList<String> arrSiblings = getSibling(concept1, concept2,
					maxSib);
			ArrayList<String> arrAncestors = getAncestor(concept1, concept2,
					maxAnc);
			ArrayList<String> arrChildren = getChildren(concept1, concept2,
					maxChild);

			String sSiblings = getArrayString(arrSiblings);
			String sAncestors = getArrayString(arrAncestors);
			String sChildren = getArrayString(arrChildren);

			newIns += "\t" + sAncestors + "\t" + sSiblings + "\t" + sChildren;

			arrSiblings = getSibling(concept2, concept1, maxSib);
			arrAncestors = getAncestor(concept2, concept1, maxAnc);
			arrChildren = getChildren(concept2, concept1, maxChild);

			sSiblings = getArrayString(arrSiblings);
			sAncestors = getArrayString(arrAncestors);
			sChildren = getArrayString(arrChildren);

			newIns += "\t" + sAncestors + "\t" + sSiblings + "\t" + sChildren;

			arrOutput.add(newIns);
			// System.out.println(newIns);
		}

		IOManager.writeLinesAddingReturn(arrOutput, outputFile);

	}

	/**
	 * @param arrStrings
	 * @return
	 */
	private String getArrayString(ArrayList<String> arrStrings) {

		int size = arrStrings.size();

		if (size == 0)
			return "_";

		String s = "";
		for (int i = 0; i < size; i++) {
			if (i == size - 1) {
				s += arrStrings.get(i);
			} else {
				s += arrStrings.get(i) + "_";
			}
		}

		return s;
	}

	/**
	 * @param targetConcept
	 * @param supportingConcept
	 * @param maxChild
	 * @return
	 */
	private ArrayList<String> getChildren(String targetConcept,
			String supportingConcept, int maxChild) {

		ArrayList<String> arrChildren = new ArrayList<String>();

		if (!mapParentConcepts.containsKey(targetConcept))
			return arrChildren;

		ArrayList<String> arrConcepts = mapParentConcepts.get(targetConcept);

		int n = arrConcepts.size();

		int i = 0;

		Set<Integer> setInts = new HashSet<Integer>();

		while (i < maxChild) {

			int rand = getRandomInteger(n, setInts);
			String z = arrConcepts.get(rand);

			if (z.equals(supportingConcept))
				continue;

			arrChildren.add(z);

			i++;

		}

		return arrChildren;

	}

	/**
	 * @param targetConcept
	 * @param supportingConcept
	 * @param maxAnc
	 * @return
	 */
	private ArrayList<String> getAncestor(String targetConcept,
			String supportingConcept, int maxAnc) {

		ArrayList<String> arrAncestors = new ArrayList<String>();

		if (!mapConceptParent.containsKey(targetConcept)) {
			return arrAncestors;
		}

		String parent = mapConceptParent.get(targetConcept);

		if (mapClassMapping.containsKey(parent)) {
			parent = mapClassMapping.get(parent);
		}

		if (mapClassMapping.containsKey(supportingConcept)) {
			supportingConcept = mapClassMapping.get(supportingConcept);
		}

		System.out.println("Parent: " + parent);
		System.out.println("Supporting: " + supportingConcept);
		if (!parent.equals(supportingConcept))
			arrAncestors.add(parent);

		return arrAncestors;
	}

	/**
	 * @param targetConcept
	 * @param supportingConcept
	 * @param maxSib
	 */
	private ArrayList<String> getSibling(String targetConcept,
			String supportingConcept, int maxSib) {

		ArrayList<String> arrSiblings = new ArrayList<String>();

		if (!mapConceptParent.containsKey(targetConcept)) {
			return arrSiblings;
		}

		String parent = mapConceptParent.get(targetConcept);

		ArrayList<String> arrConcepts = mapParentConcepts.get(parent);

		int n = arrConcepts.size();

		int i = 0;

		Set<Integer> setInts = new HashSet<Integer>();

		while (i < maxSib) {

			int rand = getRandomInteger(n, setInts);
			String z = arrConcepts.get(rand);

			if (z.equals(supportingConcept))
				continue;

			arrSiblings.add(z);

			i++;

		}

		return arrSiblings;
	}

	private int getRandomInteger(int n, Set<Integer> setInts) {

		int rand = generator.nextInt(n);

		while (setInts.contains(rand)) {
			rand = generator.nextInt(n);
		}

		setInts.add(rand);

		return rand;
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
