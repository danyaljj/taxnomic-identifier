/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.yago;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import basics.configuration.DBConfig;
import javatools.administrative.D;
import javatools.database.Database;
import javatools.parsers.PlingStemmer;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.utils.IOManager;

import queryprocessing.QueryProcessor;

/**
 * @author dxquang Oct 5, 2009
 */
public class EvaluateYago {

	private int K = 3;

	private static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	private static Map<String, String> mapClassMapping = new HashMap<String, String>();
	private static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	private QueryProcessor qp = null;

	private Database db = null;

	public EvaluateYago() throws Exception {

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

	public void queryYago() throws Exception {

		while (true) {
			D
					.p("Enter query lines, followed by a blank line. Type blank line to quit.");
			String line = "";
			while (true) {
				String input = D.r();
				if (input == null || input.length() == 0)
					break;
				line += "; " + input;
			}
			if (line.length() == 0)
				break;
			try {
				List<QueryProcessor.Template> list = QueryProcessor.templatesFor(line);
				D.p(list);
				for (Map<String, String> solution : qp.solutions(list, db, 100)) {
					D.p(solution);
				}
			} catch (Exception e) {
				D.p(e.getMessage());
			}
		}
		D.p("Closing database");
		db.close();

	}

	public void closeYago() {
		D.p("Closing database");
		db.close();
	}

	public void evaluate(String interFile, int maxLevelUp, String readMode)
			throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		K = maxLevelUp;

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, mode);

		System.out.println("Testing size: " + arrInstances.size());
		int correct = 0;
		int total = 0;

		for (Instance ins : arrInstances) {

			int relation = solving(ins.entity1, ins.entity2);

			// System.out.println("concept1: " + ins.entity1 + " - concept2: "
			// + ins.entity2 + " - relation: " + ins.relation
			// + " - predicted relation: " + relation);

			if (relation == ins.relation) {
				// System.out.println((total + 1) + ". T");
				System.out.println("T" + "\t" + relation + "\t" + ins.relation);
				correct++;
			} else {
				// System.out.println((total + 1) + ". F");
				System.out.println("F" + "\t" + relation + "\t" + ins.relation);
			}
			// System.out.println();
			total++;

		}

		System.out.println("Correct: " + correct);
		System.out.println("Total: " + total);
		double acc = (double) correct / (double) total;
		System.out.println("Accuracy: " + acc);

		closeYago();

	}

	public void evaluateACL10(String interFile, int maxLevelUp, String readMode)
			throws Exception {

		int mode = (readMode.equalsIgnoreCase("read_all") ? DataHandler.READ_ALL
				: (readMode.equalsIgnoreCase("read_only_wiki") ? DataHandler.READ_ONLY_WIKI
						: (readMode.equalsIgnoreCase("read_only_nonwiki") ? DataHandler.READ_ONLY_NONWIKI
								: -1)));
		if (mode == -1) {
			System.out.println("ERROR: Wrong READ_MODE!");
			System.exit(1);
		}

		K = maxLevelUp;

		ArrayList<Instance> arrInstances = DataHandler.readTestingInstances(
				interFile, Constants.INPUT_TYPE_INTERMEDIATE, mode);

		System.out.println("Testing size: " + arrInstances.size());
		int correct = 0;
		int total = 0;

		for (Instance ins : arrInstances) {

			int relation = solvingACL10(ins.entity1, ins.entity2);

			// System.out.println("concept1: " + ins.entity1 + " - concept2: "
			// + ins.entity2 + " - relation: " + ins.relation
			// + " - predicted relation: " + relation);

			if (relation == ins.relation) {
				// System.out.println((total + 1) + ". T");
				System.out.println("T" + "\t" + relation + "\t" + ins.relation);
				correct++;
			} else {
				// System.out.println((total + 1) + ". F");
				System.out.println("F" + "\t" + relation + "\t" + ins.relation);
			}
			// System.out.println();
			total++;

		}

		System.out.println("Correct: " + correct);
		System.out.println("Total: " + total);
		double acc = (double) correct / (double) total;
		System.out.println("Accuracy: " + acc);

		closeYago();

	}

	/**
	 * @param concept1
	 * @param concept2
	 * @return
	 * @throws InvalidTripleException
	 */
	private int solving(String concept1, String concept2)
			throws QueryProcessor.InvalidTripleException {

		Set<String> setCluster1 = new HashSet<String>();
		Set<String> setConcept1Means = new HashSet<String>();
		Set<String> setConcept1Classes = new HashSet<String>();

		if (mapClassMapping.containsKey(concept1)) {
			concept1 = mapClassMapping.get(concept1);
			setCluster1 = mapClassCluster.get(concept1);
		} else {
			setCluster1.add(concept1);
		}

		Set<String> setCluster2 = new HashSet<String>();
		Set<String> setConcept2Means = new HashSet<String>();
		Set<String> setConcept2Classes = new HashSet<String>();

		if (mapClassMapping.containsKey(concept2)) {
			concept2 = mapClassMapping.get(concept2);
			setCluster2 = mapClassCluster.get(concept2);
		} else {
			setCluster2.add(concept2);
		}

		for (String concept : setCluster1) {

			String queryConcept1 = formingQuery(concept);
			// System.out.println("\tQuery for concept 1: " + queryConcept1);

			List<QueryProcessor.Template> listConcept1 = null;
			Set<Map<String, String>> setConcept1Solutions = null;

			try {
				listConcept1 = QueryProcessor.templatesFor(queryConcept1);
				setConcept1Solutions = qp.solutions(listConcept1, db, 100000);
			} catch (QueryProcessor.InvalidTripleException e) {
				return Constants.NONE;
			}

			Map<String, Set<String>> mapResults = extractClasses(setConcept1Solutions);
			Set<String> setMeans = mapResults.get("MEANS");
			Set<String> setClasses = mapResults.get("CLASSES");

			setConcept1Means.addAll(setMeans);
			setConcept1Means.add(concept);

			setConcept1Classes.addAll(setClasses);

			// System.out.println(setConcept1Classes);

		}

		for (String concept : setCluster2) {

			String queryConcept2 = formingQuery(concept);
			// System.out.println("\tQuery for concept 2: " + queryConcept2);

			List<QueryProcessor.Template> listConcept2 = null;
			Set<Map<String, String>> setConcept2Solutions = null;

			try {
				listConcept2 = QueryProcessor.templatesFor(queryConcept2);
				setConcept2Solutions = qp.solutions(listConcept2, db, 100000);
			} catch (Exception e) {
				return Constants.NONE;
			}

			Map<String, Set<String>> mapResults = extractClasses(setConcept2Solutions);
			Set<String> setMeans = mapResults.get("MEANS");
			Set<String> setClasses = mapResults.get("CLASSES");

			setConcept2Means.addAll(setMeans);
			setConcept2Means.add(concept);

			setConcept2Classes.addAll(setClasses);

			// System.out.println(setConcept2Classes);

		}

		Set<String> setOperator = new HashSet<String>();
		setOperator.addAll(setConcept1Means);

		setOperator.retainAll(setConcept2Classes);

		if (setOperator.size() > 0)
			return Constants.ANCESTOR_E1_TO_E2;
		else {
			setOperator.addAll(setConcept2Means);
			setOperator.retainAll(setConcept1Classes);

			if (setOperator.size() > 0) {
				return Constants.ANCESTOR_E2_TO_E1;
			} else {
				setOperator.addAll(setConcept1Classes);
				setOperator.retainAll(setConcept2Classes);

				if (setOperator.size() > 0) {
					return Constants.COUSIN;
				} else {
					return Constants.NONE;
				}
			}
		}
	}

	private int solvingACL10(String concept1, String concept2)
			throws QueryProcessor.InvalidTripleException {

		Set<String> setCluster1 = new HashSet<String>();
		Set<String> setConcept1Means = new HashSet<String>();
		Set<String> setConcept1Classes = new HashSet<String>();

		if (mapClassMapping.containsKey(concept1)) {
			concept1 = mapClassMapping.get(concept1);
			setCluster1 = mapClassCluster.get(concept1);
		} else {
			setCluster1.add(concept1);
		}

		Set<String> setCluster2 = new HashSet<String>();
		Set<String> setConcept2Means = new HashSet<String>();
		Set<String> setConcept2Classes = new HashSet<String>();

		if (mapClassMapping.containsKey(concept2)) {
			concept2 = mapClassMapping.get(concept2);
			setCluster2 = mapClassCluster.get(concept2);
		} else {
			setCluster2.add(concept2);
		}

		for (String concept : setCluster1) {

			String queryConcept1 = formingQuery(concept);

			String testQuery = formingTestQueryACL10(concept);
			try {
				List<QueryProcessor.Template> testList = QueryProcessor
						.templatesFor(testQuery);
				Set<Map<String, String>> setTestConceptSolutions = qp
						.solutions(testList, db, 100000);
				if (setTestConceptSolutions.size() > 0) {
					queryConcept1 = formingQueryClassACL10(concept);
				}
			} catch (QueryProcessor.InvalidTripleException e) {
				return Constants.NONE;
			}

			// System.out.println("\tQuery for concept 1: " + queryConcept1);

			List<QueryProcessor.Template> listConcept1 = null;
			Set<Map<String, String>> setConcept1Solutions = null;

			try {
				listConcept1 = QueryProcessor.templatesFor(queryConcept1);
				setConcept1Solutions = qp.solutions(listConcept1, db, 100000);
			} catch (QueryProcessor.InvalidTripleException e) {
				return Constants.NONE;
			}

			Map<String, Set<String>> mapResults = extractClasses(setConcept1Solutions);
			Set<String> setMeans = mapResults.get("MEANS");
			Set<String> setClasses = mapResults.get("CLASSES");

			setConcept1Means.addAll(setMeans);
			setConcept1Means.add(concept);

			setConcept1Classes.addAll(setClasses);

			// System.out.println(setConcept1Classes);

		}

		for (String concept : setCluster2) {

			String queryConcept2 = formingQuery(concept);

			String testQuery = formingTestQueryACL10(concept);
			try {
				List<QueryProcessor.Template> testList = QueryProcessor
						.templatesFor(testQuery);
				Set<Map<String, String>> setTestConceptSolutions = qp
						.solutions(testList, db, 100000);
				if (setTestConceptSolutions.size() > 0) {
					queryConcept2 = formingQueryClassACL10(concept);
				}
			} catch (QueryProcessor.InvalidTripleException e) {
				return Constants.NONE;
			}

			// System.out.println("\tQuery for concept 2: " + queryConcept2);

			List<QueryProcessor.Template> listConcept2 = null;
			Set<Map<String, String>> setConcept2Solutions = null;

			try {
				listConcept2 = QueryProcessor.templatesFor(queryConcept2);
				setConcept2Solutions = qp.solutions(listConcept2, db, 100000);
			} catch (Exception e) {
				return Constants.NONE;
			}

			Map<String, Set<String>> mapResults = extractClasses(setConcept2Solutions);
			Set<String> setMeans = mapResults.get("MEANS");
			Set<String> setClasses = mapResults.get("CLASSES");

			setConcept2Means.addAll(setMeans);
			setConcept2Means.add(concept);

			setConcept2Classes.addAll(setClasses);

			// System.out.println(setConcept2Classes);

		}

		Set<String> setOperator = new HashSet<String>();
		setOperator.addAll(setConcept1Means);

		setOperator.retainAll(setConcept2Classes);

		if (setOperator.size() > 0)
			return Constants.ANCESTOR_E1_TO_E2;
		else {
			setOperator = new HashSet<String>();
			setOperator.addAll(setConcept2Means);
			setOperator.retainAll(setConcept1Classes);

			if (setOperator.size() > 0) {
				return Constants.ANCESTOR_E2_TO_E1;
			} else {
				setOperator = new HashSet<String>();
				setOperator.addAll(setConcept1Classes);
				setOperator.retainAll(setConcept2Classes);

				if (setOperator.size() > 0) {
					return Constants.COUSIN;
				} else {
					return Constants.NONE;
				}
			}
		}
	}

	/**
	 * @param setConceptSolutions
	 * @return
	 */
	private Map<String, Set<String>> extractClasses(
			Set<Map<String, String>> setConceptSolutions) {

		Map<String, Set<String>> mapResults = new HashMap<String, Set<String>>();

		Set<String> setMeans = new HashSet<String>();
		Set<String> setClasses = new HashSet<String>();

		for (Map<String, String> solution : setConceptSolutions) {

			Set<String> setKeys = solution.keySet();
			for (String key : setKeys) {

				String value = solution.get(key);
				value = value.toLowerCase();

				if (key.charAt(1) > 'A') {

					if (value.startsWith("wordnet")) {
						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
					} else if (value.startsWith("wikicategory")) {
						int posB = value.indexOf('_');
						value = value.substring(posB + 1);
					}

					value = value.replace('_', ' ');

					setClasses.add(value);
				} else {
					value = value.replace('_', ' ');
					setMeans.add(value);
				}
			}

		}

		mapResults.put("MEANS", setMeans);
		mapResults.put("CLASSES", setClasses);

		return mapResults;

	}

	/**
	 * @param concept
	 * @return
	 */
	private String formingQuery(String concept) {
		String query = ";\"" + concept + "\" means ?A; " + "?A type ?B";
		String[] notation = new String[] { "B", "C", "D", "E", "F", "G" };

		for (int i = 0; i < K; i++) {
			query += "; " + "?" + notation[i] + " subClassOf " + "?"
					+ notation[i + 1];
		}
		return query;
	}

	// This function was added for ACL10
	private String formingQueryClassACL10(String concept) {
		String query = ";\"" + concept + "\" means ?A; " + "?A subClassOf ?B";
		String[] notation = new String[] { "B", "C", "D", "E", "F", "G" };

		for (int i = 0; i < K; i++) {
			query += "; " + "?" + notation[i] + " subClassOf " + "?"
					+ notation[i + 1];
		}
		return query;
	}

	// This function was added for ACL10
	private String formingTestQueryACL10(String concept) {
		String query = ";\"" + concept + "\" means ?A; " + "?A subClassOf ?B";
		return query;
	}
}
