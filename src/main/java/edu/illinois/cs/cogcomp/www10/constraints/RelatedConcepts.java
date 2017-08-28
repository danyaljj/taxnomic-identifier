/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.administrative.D;
import javatools.database.Database;
import javatools.parsers.NounGroup;
import javatools.parsers.PlingStemmer;
import queryprocessing.QueryProcessor;
import queryprocessing.QueryProcessor.Template;
import basics.configuration.DBConfig;
import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.FeatureExtraction;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Oct 9, 2009
 */
public class RelatedConcepts {

	private static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	private static Map<String, String> mapClassMapping = new HashMap<String, String>();
	private static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	private Set<String> setConceptAncestors;
	private Set<String> setConceptChildren;
	private Set<String> setConceptSiblings;
	private Set<String> setConceptMeans;

	private QueryProcessor qp = null;

	private Database db = null;

	/**
	 * 
	 */
	public RelatedConcepts() throws Exception {
		setConceptAncestors = new HashSet<String>();
		setConceptSiblings = new HashSet<String>();
		setConceptChildren = new HashSet<String>();
		setConceptMeans = new HashSet<String>();

		db = DBConfig.getYagoDatabase();
		qp = new QueryProcessor();
		D.p("Connected to", db);

		System.out.print("Loading class cluster... ");
		loadClassCluster();
		System.out.println("done!");

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

	public void getRelatedConcepts(String concept, String supportingConcept,
			boolean debug) throws Exception {

		if (debug)
			System.out.println("Original concept: " + concept);

		if (mapClassMapping.containsKey(concept)) {
			concept = mapClassMapping.get(concept);
		}

		if (debug)
			System.out.println("Input concept: " + concept);

		setConceptSiblings = new HashSet<String>();
		setConceptAncestors = new HashSet<String>();
		setConceptChildren = new HashSet<String>();
		setConceptMeans = new HashSet<String>();

		concept = concept.replace('"', ' ');
		concept = concept.replaceAll("\\s+", " ");

		getConceptAncestorAndSibling(concept, supportingConcept, debug);
		getConceptChild(concept, supportingConcept, debug);

	}

	public void getRelatedConceptsWithSubClassOf(String concept,
			String supportingConcept, boolean debug) throws Exception {

		if (debug)
			System.out.println("Original concept: " + concept);

		if (mapClassMapping.containsKey(concept)) {
			concept = mapClassMapping.get(concept);
		}

		if (debug)
			System.out.println("Input concept: " + concept);

		setConceptSiblings = new HashSet<String>();
		setConceptAncestors = new HashSet<String>();
		setConceptChildren = new HashSet<String>();
		setConceptMeans = new HashSet<String>();

		concept = concept.replace('"', ' ');
		concept = concept.replaceAll("\\s+", " ");

		getConceptAncestorAndSiblingWithSubClassOf(concept, supportingConcept,
				debug);
		getConceptChild(concept, supportingConcept, debug);

	}

	public String getAncestorString(int maxConcept) {

		StringBuffer ancestor = new StringBuffer("");

		int i = 0;
		int n = setConceptAncestors.size();

		for (String s : setConceptAncestors) {
			ancestor.append(s + "_");
			i++;
			if (i >= n || i >= maxConcept)
				break;
		}

		if (ancestor.length() > 0)
			ancestor = ancestor.deleteCharAt(ancestor.length() - 1);

		return ancestor.length() > 0 ? ancestor.toString() : "_";
	}

	public String getChildString(int maxConcept) {

		StringBuffer children = new StringBuffer("");

		int i = 0;
		int n = setConceptChildren.size();

		for (String s : setConceptChildren) {
			children.append(s + "_");
			i++;
			if (i >= n || i >= maxConcept)
				break;
		}

		if (children.length() > 0)
			children = children.deleteCharAt(children.length() - 1);

		return children.length() > 0 ? children.toString() : "_";
	}

	public String getSiblingString(int maxConcept) {

		StringBuffer sibling = new StringBuffer("");

		int i = 0;
		int n = setConceptSiblings.size();

		for (String s : setConceptSiblings) {
			sibling.append(s + "_");
			i++;
			if (i >= n || i >= maxConcept)
				break;
		}

		if (sibling.length() > 0)
			sibling = sibling.deleteCharAt(sibling.length() - 1);

		return sibling.length() > 0 ? sibling.toString() : "_";
	}

	/**
	 * @param concept
	 * @param debug
	 * @throws Exception
	 */
	private void getConceptChild(String concept, String supportingConcept,
			boolean debug) throws Exception {
		String queryConcept = formingQueryTypeB(concept);

		if (debug) {
			System.out.println("\t*Get concept's children.");
			System.out.println("\tQuery: " + queryConcept);
		}

		Set<Map<String, String>> setSolutions = getConceptSolutions(
				queryConcept, debug);

		extractRelatedConcepts(concept, supportingConcept, setSolutions, debug);
	}

	/**
	 * @param concept
	 * @param supportingConcept
	 * @param debug
	 * @throws Exception
	 */
	private void getConceptAncestorAndSibling(String concept,
			String supportingConcept, boolean debug) throws Exception {

		String queryConcept = formingQueryTypeA(concept);

		if (debug) {
			System.out.println("\t*Get concept's ancestors and siblings.");
			System.out.println("\tQuery: " + queryConcept);
		}

		Set<Map<String, String>> setSolutions = getConceptSolutions(
				queryConcept, debug);

		extractRelatedConcepts(concept, supportingConcept, setSolutions, debug);

	}

	/**
	 * @param concept
	 * @param supportingConcept
	 * @param debug
	 * @throws Exception
	 */
	private void getConceptAncestorAndSiblingWithSubClassOf(String concept,
			String supportingConcept, boolean debug) throws Exception {

		String queryConceptTypeC = formingQueryTypeC(concept);
		if (debug) {
			System.out
					.println("\t*Get concept's ancestors and siblings type C.");
			System.out.println("\tQuery: " + queryConceptTypeC);
		}
		Set<Map<String, String>> setSolutions = getConceptSolutions(
				queryConceptTypeC, debug);

		if (setSolutions.size() > 0) {
			extractRelatedConceptsWithSubClassOf(concept, supportingConcept,
					setSolutions, debug);
		} else {

			String queryConceptTypeA = formingQueryTypeA(concept);

			if (debug) {
				System.out
						.println("\t*Get concept's ancestors and siblings type A.");
				System.out.println("\tQuery: " + queryConceptTypeA);
			}

			setSolutions = getConceptSolutions(queryConceptTypeA, debug);

			extractRelatedConcepts(concept, supportingConcept, setSolutions,
					debug);
		}
	}

	private Set<Map<String, String>> getConceptSolutions(String queryConcept,
			boolean debug) throws Exception {

		List<Template> listConcept = QueryProcessor.templatesFor(queryConcept);
		Set<Map<String, String>> setConceptSolutions = qp.solutions(
				listConcept, db, 10000);

		if (debug)
			System.out.println("\tSet concept solution size: "
					+ setConceptSolutions.size());

		return setConceptSolutions;
	}

	public void extractRelatedConcepts(String concept,
			String supportingConcept,
			Set<Map<String, String>> setConceptSolutions, boolean debug) {

		for (Map<String, String> solution : setConceptSolutions) {

			Set<String> setKeys = solution.keySet();
			for (String key : setKeys) {

				String value = solution.get(key);
				value = value.toLowerCase();

				char symbol = key.charAt(1);

				if (symbol == 'A') {

					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					}

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptMeans.add(value);

				} else if (symbol == 'B') {

					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					} else if (value.startsWith("wikicategory")) {

						int posB = value.indexOf('_');
						String ancestor = value.substring(posB + 1);
						ancestor = ancestor.replace('_', ' ');

						NounGroup nounGroup = new NounGroup(ancestor);

						boolean plural = nounGroup.plural();
						if (plural || !plural) {
							String head = nounGroup.head();
							value = head;
						}

					}

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptAncestors.add(value);

				} else if (symbol == 'C') {

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptSiblings.add(value);

				} else if (symbol == 'D') {

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptChildren.add(value);
				}
			}

		}

	}

	public void extractRelatedConceptsWithSubClassOf(String concept,
			String supportingConcept,
			Set<Map<String, String>> setConceptSolutions, boolean debug) {

		for (Map<String, String> solution : setConceptSolutions) {

			Set<String> setKeys = solution.keySet();
			for (String key : setKeys) {

				String value = solution.get(key);
				value = value.toLowerCase();

				char symbol = key.charAt(1);

				if (symbol == 'A') {

					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					}

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptMeans.add(value);

				} else if (symbol == 'B') {

					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					} else if (value.startsWith("wikicategory")) {

						int posB = value.indexOf('_');
						String ancestor = value.substring(posB + 1);
						ancestor = ancestor.replace('_', ' ');

						NounGroup nounGroup = new NounGroup(ancestor);

						boolean plural = nounGroup.plural();
						if (plural || !plural) {
							String head = nounGroup.head();
							value = head;
						}

					}

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptAncestors.add(value);

				} else if (symbol == 'C') {

					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					} else if (value.startsWith("wikicategory")) {

						int posB = value.indexOf('_');
						String ancestor = value.substring(posB + 1);
						ancestor = ancestor.replace('_', ' ');

						NounGroup nounGroup = new NounGroup(ancestor);

						boolean plural = nounGroup.plural();
						if (plural || !plural) {
							String head = nounGroup.head();
							value = head;
						}

					} else {
						value = value.replace('_', ' ');
					}

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptSiblings.add(value);

				} else if (symbol == 'D') {

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept))
						setConceptChildren.add(value);
				}
			}

		}

	}

	public void printRelatedConcepts() {

		StringBuffer means = new StringBuffer("");
		for (String key : setConceptMeans) {
			means.append(key + ", ");
		}

		StringBuffer ancestors = new StringBuffer("");
		for (String key : setConceptAncestors) {
			ancestors.append(key + ", ");
		}

		StringBuffer siblings = new StringBuffer("");
		for (String key : setConceptSiblings) {
			siblings.append(key + ", ");
		}

		StringBuffer chidren = new StringBuffer("");
		for (String key : setConceptChildren) {
			chidren.append(key + ", ");
		}

		System.out.println();
		System.out.println("Means: " + means.toString());
		System.out.println();
		System.out.println("Ancestors: " + ancestors.toString());
		System.out.println();
		System.out.println("Sibling: " + siblings.toString());
		System.out.println();
		System.out.println("Children: " + chidren.toString());
		System.out.println();

	}

	private String formingQueryTypeA(String concept) {

		String query = ";\"" + concept + "\" means ?A; " + "?A type ?B; "
				+ "?C type ?B";
		return query;
	}

	private String formingQueryTypeC(String concept) {

		String query = ";\"" + concept + "\" means ?A; " + "?A subclassof ?B; "
				+ "?C subclassof ?B";
		return query;
	}

	private String formingQueryTypeB(String concept) {
		String query = ";\"" + concept + "\" means ?A; " + "?D type ?A";
		return query;
	}

	public void getRelatedConceptForFile(String interFile, String outputFile,
			int maxAnc, int maxSib, int maxChild) throws Exception {

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE,
				DataHandler.READ_ALL);

		ArrayList<String> arrNewInstances = DataHandler.makeStringInstances(
				arrInstances, Constants.INPUT_TYPE_INTERMEDIATE);

		ArrayList<String> arrOutput = new ArrayList<String>();

		int n = arrInstances.size();
		for (int i = 0; i < n; i++) {

			Instance ins = arrInstances.get(i);
			String newIns = arrNewInstances.get(i);
			newIns = newIns.trim();

			System.out.println(newIns);

			getRelatedConcepts(ins.entity1, ins.entity2, false);
			String anc = getAncestorString(maxAnc);
			String sib = getSiblingString(maxSib);
			String chi = getChildString(maxChild);

			newIns += "\t" + anc + "\t" + sib + "\t" + chi;

			getRelatedConcepts(ins.entity2, ins.entity1, false);
			anc = getAncestorString(maxAnc);
			sib = getSiblingString(maxSib);
			chi = getChildString(maxChild);

			newIns += "\t" + anc + "\t" + sib + "\t" + chi;

			arrOutput.add(newIns);
			// System.out.println(newIns);
		}

		IOManager.writeLinesAddingReturn(arrOutput, outputFile);
	}

	public void getRelatedConceptWithSubClassOfForFile(String interFile,
			String outputFile, int maxAnc, int maxSib, int maxChild)
			throws Exception {

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE,
				DataHandler.READ_ALL);

		ArrayList<String> arrNewInstances = DataHandler.makeStringInstances(
				arrInstances, Constants.INPUT_TYPE_INTERMEDIATE);

		ArrayList<String> arrOutput = new ArrayList<String>();

		int n = arrInstances.size();
		for (int i = 0; i < n; i++) {

			Instance ins = arrInstances.get(i);
			String newIns = arrNewInstances.get(i);
			newIns = newIns.trim();

			System.out.println(newIns);

			getRelatedConceptsWithSubClassOf(ins.entity1, ins.entity2, false);
			String anc = getAncestorString(maxAnc);
			String sib = getSiblingString(maxSib);
			String chi = getChildString(maxChild);

			newIns += "\t" + anc + "\t" + sib + "\t" + chi;

			getRelatedConceptsWithSubClassOf(ins.entity2, ins.entity1, false);
			anc = getAncestorString(maxAnc);
			sib = getSiblingString(maxSib);
			chi = getChildString(maxChild);

			newIns += "\t" + anc + "\t" + sib + "\t" + chi;

			arrOutput.add(newIns);
			// System.out.println(newIns);
		}

		IOManager.writeLinesAddingReturn(arrOutput, outputFile);
	}

	public void createConceptRelationInterFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String outputFile, int K, int maxConcept) throws Exception {

		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, K);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		Set<Instance> setInstances = new HashSet<Instance>();

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 1;

		for (String line : arrLines) {

			System.out.println("[" + i + "] " + line);

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			String ancX = parts[12];
			String[] ancXs = ancX.split("_");
			String sibX = parts[13];
			String[] sibXs = sibX.split("_");
			String chiX = parts[14];
			String[] chiXs = chiX.split("_");

			String ancY = parts[15];
			String[] ancYs = ancY.split("_");
			String sibY = parts[16];
			String[] sibYs = sibY.split("_");
			String chiY = parts[17];
			String[] chiYs = chiY.split("_");

			ArrayList<Instance> arrNewInstances = null;

			arrNewInstances = extractFeatures(fex, conceptX, ancXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiXs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			arrNewInstances = extractFeatures(fex, conceptX, ancYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiYs, maxConcept,
					setInstances);
			arrOutputInstances.addAll(arrNewInstances);

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

	public void createConceptRelationInterFileBigFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			int K, String outputFile, int maxConceptAnc, int maxConceptSib,
			int maxConceptChi, int from, int to) throws Exception {

		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, K);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		Set<Instance> setInstances = new HashSet<Instance>();

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int count = 0;

		int n = arrLines.size();

		for (int i = from; (i < to && i < n); i++) {

			String line = arrLines.get(i);

			System.out.println("[" + i + "] " + line);

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			String ancX = parts[12];
			String[] ancXs = ancX.split("_");
			String sibX = parts[13];
			String[] sibXs = sibX.split("_");
			String chiX = parts[14];
			String[] chiXs = chiX.split("_");

			String ancY = parts[15];
			String[] ancYs = ancY.split("_");
			String sibY = parts[16];
			String[] sibYs = sibY.split("_");
			String chiY = parts[17];
			String[] chiYs = chiY.split("_");

			ArrayList<Instance> arrNewInstances = null;

			arrNewInstances = extractFeatures(fex, conceptX, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			arrNewInstances = extractFeatures(fex, conceptX, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			count++;
			if (count % 100 == 0) {
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

		System.out.println("Done.");

	}

	public void createConceptRelationInterAdditionalFeaturesFileBigFile(
			String indexDir, String categoryMapping, String titleMapping,
			String inputFile, int K, String outputFile, int maxConceptAnc,
			int maxConceptSib, int maxConceptChi, int from, int to)
			throws Exception {

		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, K);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		Set<Instance> setInstances = new HashSet<Instance>();

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int count = 0;

		int n = arrLines.size();

		for (int i = from; (i < to && i < n); i++) {

			String line = arrLines.get(i);

			System.out.println("[" + i + "] " + line);

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			String ancX = parts[12];
			String[] ancXs = ancX.split("_");
			String sibX = parts[13];
			String[] sibXs = sibX.split("_");
			String chiX = parts[14];
			String[] chiXs = chiX.split("_");

			String ancY = parts[15];
			String[] ancYs = ancY.split("_");
			String sibY = parts[16];
			String[] sibYs = sibY.split("_");
			String chiY = parts[17];
			String[] chiYs = chiY.split("_");

			ArrayList<Instance> arrNewInstances = null;

			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptX, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeaturesAdditionalFeatures(fex, conceptY, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			count++;
			if (count % 100 == 0) {
				ArrayList<String> arrStringInstances = DataHandler
						.makeStringInstancesAdditionalFeatures(arrOutputInstances,
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
					.makeStringInstancesAdditionalFeatures(arrOutputInstances,
							DatasetCreation.INPUT_TYPE_INTERMEDIATE);
			for (String s : arrStringInstances) {
				writer.write(s);
			}
		}

		IOManager.closeWriter(writer);

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Done.");

	}

	public void createConceptRelationInterFileBigFileExclusive(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String exclusiveFile, int K, String outputFile, int maxConceptAnc,
			int maxConceptSib, int maxConceptChi, int from, int to)
			throws Exception {

		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, K);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

		System.out.print("Loading excluvise concepts... ");
		Set<Instance> setInstances = readExclusiveFile(exclusiveFile);
		System.out.print("done.");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int count = 0;

		int n = arrLines.size();

		for (int i = from; (i < to && i < n); i++) {

			String line = arrLines.get(i);

			System.out.println("[" + i + "] " + line);

			String[] parts = line.split("\\t+");

			String conceptX = parts[2];
			String conceptY = parts[3];

			String ancX = parts[12];
			String[] ancXs = ancX.split("_");
			String sibX = parts[13];
			String[] sibXs = sibX.split("_");
			String chiX = parts[14];
			String[] chiXs = chiX.split("_");

			String ancY = parts[15];
			String[] ancYs = ancY.split("_");
			String sibY = parts[16];
			String[] sibYs = sibY.split("_");
			String chiY = parts[17];
			String[] chiYs = chiY.split("_");

			ArrayList<Instance> arrNewInstances = null;

			arrNewInstances = extractFeatures(fex, conceptX, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancXs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibXs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiXs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			arrNewInstances = extractFeatures(fex, conceptX, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptX, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, ancYs,
					maxConceptAnc, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, sibYs,
					maxConceptSib, setInstances);
			arrOutputInstances.addAll(arrNewInstances);
			arrNewInstances = extractFeatures(fex, conceptY, chiYs,
					maxConceptChi, setInstances);
			arrOutputInstances.addAll(arrNewInstances);

			count++;
			if (count % 100 == 0) {
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

		System.out.println("Done.");

	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Set<Instance> readExclusiveFile(String exclusiveFile)
			throws Exception {
		Set<Instance> setInstances = new HashSet<Instance>();

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				exclusiveFile, Constants.INPUT_TYPE_INTERMEDIATE,
				DataHandler.READ_ONLY_WIKI);

		setInstances.addAll(arrInstances);

		return setInstances;

	}

	/**
	 * @param fex
	 * @param concept
	 * @param relatedConcepts
	 * @param maxConcept
	 * @param setInstances
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Instance> extractFeatures(FeatureExtraction fex,
			String concept, String[] relatedConcepts, int maxConcept,
			Set<Instance> setInstances) throws Exception {

		// System.out.println(setInstances.size());
		// System.out.println("Concept: " + concept);
		// System.out.print("Related concepts: ");

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();

		int n = relatedConcepts.length;
		int i = 0;
		int count = 0;
		while (count < maxConcept && i < n) {

			String s = relatedConcepts[i];

			if (s.equals("_")) {
				i++;
				continue;
			}

			Instance ins = new Instance(concept, s);
			ins.entityClass = "_|_";

			// System.out.println(ins.toString());

			if (setInstances.contains(ins)) {
				// System.out.println("hello ins: " + ins.entity1 + " \t " +
				// ins.entity2);
				i++;
				count++;
				continue;
			}

			setInstances.add(ins);

			fex.featureExtraction(ins);
			arrInstance.add(ins);

			// System.out.print(s + " | ");

			count++;
			i++;
		}

		// System.out.println();

		return arrInstance;
	}

	// This function was added for ACL10
	private ArrayList<Instance> extractFeaturesAdditionalFeatures(
			FeatureExtraction fex, String concept, String[] relatedConcepts,
			int maxConcept, Set<Instance> setInstances) throws Exception {

		// System.out.println(setInstances.size());
		// System.out.println("Concept: " + concept);
		// System.out.print("Related concepts: ");

		ArrayList<Instance> arrInstance = new ArrayList<Instance>();

		int n = relatedConcepts.length;
		int i = 0;
		int count = 0;
		while (count < maxConcept && i < n) {

			String s = relatedConcepts[i];

			if (s.equals("_")) {
				i++;
				continue;
			}

			Instance ins = new Instance(concept, s);
			ins.entityClass = "_|_";

			// System.out.println(ins.toString());

			if (setInstances.contains(ins)) {
				// System.out.println("hello ins: " + ins.entity1 + " \t " +
				// ins.entity2);
				i++;
				count++;
				continue;
			}

			setInstances.add(ins);

			fex.featureExtractionAdditionalFeatures(ins);
			arrInstance.add(ins);

			// System.out.print(s + " | ");

			count++;
			i++;
		}

		// System.out.println();

		return arrInstance;
	}

}
