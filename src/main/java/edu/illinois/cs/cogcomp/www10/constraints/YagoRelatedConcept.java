/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javatools.administrative.D;
import javatools.database.Database;
import javatools.parsers.NounGroup;
import javatools.parsers.PlingStemmer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;

import queryprocessing.QueryProcessor;
import queryprocessing.QueryProcessor.Template;
import basics.configuration.DBConfig;
import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.FeatureExtraction;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneSearcher;
import edu.illinois.cs.cogcomp.utils.CosineSimilarity;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.IdfManager;

/**
 * @author dxquang Oct 26, 2009
 */
public class YagoRelatedConcept {

	private static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	public static int NUM_SEARCH_RESULTS = 10;

	String indexDir;
	private IndexReader idxReader = null;

	private IdfManager idfMan = null;

	Vector<String> vecTerms = null;

	private SimpleLuceneSearcher searcher = null;

	private static Map<String, String> mapClassMapping = new HashMap<String, String>();
	private static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	private Set<String> setConceptAncestors;
	private Set<String> setConceptChildren;
	private Set<String> setConceptSiblings;
	private Set<String> setConceptMeans;
	private Map<String, Set<String>> mapOrgConceptAncestors;
	private Map<String, Set<String>> mapOrgConceptMeans;

	private QueryProcessor qp = null;

	private Database db = null;

	private Vector<Double> tfIdfVectorRelatedDocs;

	/**
	 * 
	 */
	public YagoRelatedConcept(String indexDir, String categoryMapping,
			String titleMapping, String idfFile) throws Exception {

		this.indexDir = indexDir;

		idxReader = IndexReader.open(this.indexDir);
		System.out.println("The index " + indexDir + " was opened.");

		idfMan = new IdfManager(idfFile);

		Set<String> keySet = idfMan.mapIdf.keySet();
		vecTerms = new Vector<String>(keySet);

		String[] fields = new String[] { "text" };
		searcher = new SimpleLuceneSearcher(fields, true);
		searcher.open(this.indexDir);

		setConceptAncestors = new HashSet<String>();
		setConceptSiblings = new HashSet<String>();
		setConceptChildren = new HashSet<String>();
		setConceptMeans = new HashSet<String>();

		mapOrgConceptAncestors = new HashMap<String, Set<String>>();

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
		
		setConceptMeans = new HashSet<String>();
		
		String queryConcept = formingQueryTypeBPrime(concept);

		if (debug) {
			System.out.println("\t*Get concept's children.");
			System.out.println("\tQuery: " + queryConcept);
		}

		Set<Map<String, String>> setSolutions = getConceptSolutions(
				queryConcept, debug);

		extractRelatedConcepts(concept, supportingConcept, setSolutions, debug);

		int i = 0;

		ArrayList<ConceptSimilarity> arrConceptSimilarity = new ArrayList<ConceptSimilarity>();

		for (String s : setConceptMeans) {

			Vector<Double> tfidfVectorCurrent = getTfIdfVectorRelatedDocs(
					concept, s);

			double similarity = getCosineSimilarity(tfidfVectorCurrent);

			ConceptSimilarity cs = new ConceptSimilarity(s, similarity);
			arrConceptSimilarity.add(cs);

			i++;
			if (i >= 20)
				break;
		}

		sortConceptSimilarity(arrConceptSimilarity);

		setConceptChildren = new HashSet<String>();

		i = 0;
		for (ConceptSimilarity cs : arrConceptSimilarity) {

			Set<String> setNewConcepts = mapOrgConceptAncestors.get(cs.concept);

			for (String newConcept : setNewConcepts) {
				queryConcept = formingQueryTypeD(newConcept);

				setSolutions = getConceptSolutions(queryConcept, debug);

				extractRelatedConcepts(concept, supportingConcept,
						setSolutions, debug);
			}

			i++;
			if (i >= 3)
				break;
		}

	}

	/**
	 * @param concept
	 * @param supportingConcept
	 * @param debug
	 * @throws Exception
	 */
	private void getConceptAncestorAndSibling(String concept,
			String supportingConcept, boolean debug) throws Exception {

		String queryConcept = formingQueryTypeAPrime(concept);

		if (debug) {
			System.out
					.println("\t*Get concept's ancestors and siblings with query in type A'");
			System.out.println("\tQuery: " + queryConcept);
		}

		Set<Map<String, String>> setSolutions = getConceptSolutions(
				queryConcept, debug);

		extractRelatedConcepts(concept, supportingConcept, setSolutions, debug);

		int i = 0;

		ArrayList<ConceptSimilarity> arrConceptSimilarity = new ArrayList<ConceptSimilarity>();

		for (String s : setConceptAncestors) {

			Vector<Double> tfidfVectorCurrent = getTfIdfVectorRelatedDocs(
					concept, s);

			double similarity = getCosineSimilarity(tfidfVectorCurrent);

			ConceptSimilarity cs = new ConceptSimilarity(s, similarity);
			arrConceptSimilarity.add(cs);

			i++;
			if (i >= 20)
				break;
		}

		sortConceptSimilarity(arrConceptSimilarity);

		setConceptAncestors = new HashSet<String>();
		setConceptSiblings = new HashSet<String>();

		i = 0;
		
		for (ConceptSimilarity cs : arrConceptSimilarity) {

			setConceptAncestors.add(cs.concept);

			Set<String> setNewConcepts = mapOrgConceptAncestors.get(cs.concept);

			for (String newConcept : setNewConcepts) {
				queryConcept = formingQueryTypeC(newConcept);

				setSolutions = getConceptSolutions(queryConcept, debug);

				extractRelatedConcepts(concept, supportingConcept,
						setSolutions, debug);
			}

			i++;
			if (i >= 3)
				break;
		}
	}

	/**
	 * @param arrConceptSimilarity
	 */
	private void sortConceptSimilarity(
			ArrayList<ConceptSimilarity> arrConceptSimilarity) {
		Collections.sort(arrConceptSimilarity,
				new Comparator<ConceptSimilarity>() {
					@Override
					public int compare(ConceptSimilarity o1,
							ConceptSimilarity o2) {
						if (o1.similarity < o2.similarity)
							return 1;
						else if (o1.similarity == o2.similarity)
							return 0;
						else
							return -1;
					}
				});
	}

	private double getCosineSimilarity(Vector<Double> tfidfVectorCurrent)
			throws Exception {

		double res = CosineSimilarity.getSimilarity(tfIdfVectorRelatedDocs,
				tfidfVectorCurrent);

		return res;
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

					String org = value;
					
					if (value.startsWith("wordnet")) {

						int posB = value.indexOf('_');
						int posE = value.lastIndexOf('_');
						value = value.substring(posB + 1, posE);
						value = value.replace('_', ' ');

					}

					value = value.replace('_', ' ');

					if (!value.equals(concept)
							&& !value.equals(supportingConcept)) {
						setConceptMeans.add(value);
						if (mapOrgConceptAncestors.containsKey(value)) {
							Set<String> setConcepts = mapOrgConceptAncestors
									.get(value);
							setConcepts.add(org);
						} else {
							Set<String> setConcepts = new HashSet<String>();
							setConcepts.add(org);
							mapOrgConceptAncestors.put(value, setConcepts);
						}
					}

				} else if (symbol == 'B') {

					String org = value;

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
							&& !value.equals(supportingConcept)) {
						setConceptAncestors.add(value);
						if (mapOrgConceptAncestors.containsKey(value)) {
							Set<String> setConcepts = mapOrgConceptAncestors
									.get(value);
							setConcepts.add(org);
						} else {
							Set<String> setConcepts = new HashSet<String>();
							setConcepts.add(org);
							mapOrgConceptAncestors.put(value, setConcepts);
						}
					}

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

	private String formingQueryTypeAPrime(String concept) {

		String query = ";\"" + concept + "\" means ?A; " + "?A type ?B";
		return query;
	}

	private String formingQueryTypeB(String concept) {
		String query = ";\"" + concept + "\" means ?A; " + "?D type ?A";
		return query;
	}

	private String formingQueryTypeBPrime(String concept) {
		String query = ";\"" + concept + "\" means ?A";
		return query;
	}

	private String formingQueryTypeC(String concept) {
		String query = ";?C type " + concept;
		return query;
	}

	private String formingQueryTypeD(String concept) {
		String query = ";?D type " + concept;
		return query;
	}

	public void getRelatedConceptForFile(String interFile, String outputFile,
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

			tfIdfVectorRelatedDocs = getTfIdfVectorRelatedDocs(ins.entity1,
					ins.entity2);

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

	/**
	 * @param concept1
	 * @param concept2
	 * @return
	 * @throws Exception
	 */
	private Vector<Double> getTfIdfVectorRelatedDocs(String concept1,
			String concept2) throws Exception {

		ArrayList<Vector<Double>> arrRelatedDocs = new ArrayList<Vector<Double>>();

		String query = concept1 + " " + concept2;
		ArrayList<ILuceneResult> arrResults = searcher.search(query,
				NUM_SEARCH_RESULTS);

		ArrayList<Integer> arrDocIdx = new ArrayList<Integer>();
		for (ILuceneResult result : arrResults) {
			int id = Integer.parseInt(result.getId());
			arrDocIdx.add(id);
		}

		Vector<Double> tfidfVector = getTfIdfVector(arrDocIdx);

		return tfidfVector;
	}

	/**
	 * @param docIdx
	 * @return
	 * @throws IOException
	 */
	private Vector<Double> getTfIdfVector(ArrayList<Integer> arrDocIdx)
			throws IOException {

		Vector<Double> tfidfVector = new Vector<Double>();
		int totalTerm = 0;
		Map<String, Double> mapTermFreq = new HashMap<String, Double>();

		for (Integer docIdx : arrDocIdx) {
			TermFreqVector termFreqVector = idxReader.getTermFreqVector(docIdx,
					"text");
			String[] terms = termFreqVector.getTerms();
			int[] freqs = termFreqVector.getTermFrequencies();

			for (int f : freqs)
				totalTerm += f;

			int len = terms.length;
			for (int j = 0; j < len; j++) {
				if (!mapTermFreq.containsKey(terms[j])) {
					mapTermFreq.put(terms[j], (double) freqs[j]);
				} else {
					double freq = mapTermFreq.get(terms[j]);
					freq += (double) freqs[j];
					mapTermFreq.put(terms[j], freq);
				}
			}
		}

		// Calculating tf*idf of the terms in the document
		for (String term : vecTerms) {

			double tfidf = 0.0;
			if (mapTermFreq.containsKey(term)) {
				double tf = mapTermFreq.get(term).doubleValue()
						/ (double) totalTerm;
				double idf = idfMan.getIdf(term);
				tfidf = tf * idf;
				tfidfVector.add(tfidf);
			} else {
				tfidfVector.add(0.0);
			}

		}

		return tfidfVector;
	}

	/**
	 * @param docIdx
	 * @return
	 * @throws IOException
	 */
	private Vector<Double> getTfIdfVector(int docIdx) throws IOException {

		Vector<Double> tfidfVector = new Vector<Double>();

		TermFreqVector termFreqVector = idxReader.getTermFreqVector(docIdx,
				"text");
		String[] terms = termFreqVector.getTerms();
		int[] freqs = termFreqVector.getTermFrequencies();

		// Creating term frequency map
		int sum = 0;
		for (int f : freqs)
			sum += f;
		Map<String, Double> mapTermFreq = new HashMap<String, Double>();
		int len = terms.length;
		for (int j = 0; j < len; j++) {
			if (!mapTermFreq.containsKey(terms[j])) {
				mapTermFreq.put(terms[j], (double) freqs[j] / (double) sum);
			}
		}

		// Calculating tf*idf of the terms in the document
		for (String term : vecTerms) {

			double tfidf = 0.0;
			if (mapTermFreq.containsKey(term)) {
				double tf = mapTermFreq.get(term).doubleValue();
				double idf = idfMan.getIdf(term);
				tfidf = tf * idf;
				tfidfVector.add(tfidf);
			} else {
				tfidfVector.add(0.0);
			}

		}

		return tfidfVector;
	}

	public void createConceptRelationInterFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile, int K,
			String outputFile, int maxConcept) throws Exception {

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
			String categoryMapping, String titleMapping, String inputFile, int K,
			String outputFile, int maxConceptAnc, int maxConceptSib,
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
				exclusiveFile, Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);

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

}
