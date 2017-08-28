/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10.distributioanlmemory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.utils.CosineSimilarity;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Jun 20, 2010
 */
public class DM {

	protected StrudelSearcher searcher = null;
	protected String indexDir;
	protected Map<String, List<String>> prototypes = null;
	protected Map<String, Vector<Double>> protoCentroidVectors = null;

	/**
	 * @throws IOException
	 * 
	 */
	public DM(String indexDir) throws IOException {
		searcher = new StrudelSearcher();
		this.indexDir = indexDir;

		prototypes = new HashMap<String, List<String>>();
		protoCentroidVectors = new HashMap<String, Vector<Double>>();
	}

	public void GetConceptInStrudel(String inputFile, String outputFile)
			throws Exception {
		searcher.open(this.indexDir);
		ArrayList<String> lines = IOManager.readLines(inputFile);
		ArrayList<String> outLines = new ArrayList<String>();
		int c = 0;
		int total = 0;
		for (String line : lines) {
			System.out.println(line);
			String[] splits = line.split("\\t+");
			if (splits.length != 2)
				continue;
			System.out.println(splits[0] + " -- " + splits[1]);
			String concept = splits[0].toLowerCase();
			if (isInStrudel(concept)) {
				outLines.add(line);
				c++;
			}
			total++;
		}
		searcher.close();
		System.out.println("Total = " + c);
		System.out.println("c = " + c);
		IOManager.writeLinesAddingReturn(outLines, outputFile);
	}

	public void GetPairsInStrudel(String inputFile, String outputFile)
			throws Exception {
		searcher.open(this.indexDir);
		ArrayList<String> lines = IOManager.readLines(inputFile);
		int c = 0;
		ArrayList<String> outLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.length() == 0)
				continue;
			String[] splits = line.split("\\t+");
			String term1 = splits[2];
			String term2 = splits[3];
			if (isInStrudel(term1) && isInStrudel(term2)) {
				outLines.add(line);
				c++;
			}
		}
		System.out.println("c=" + c);
		searcher.close();

		IOManager.writeLinesAddingReturn(outLines, outputFile);

	}

	private boolean isInStrudel(String term) throws Exception {
		List<ILuceneResult> result = searcher.search(term + "-n");
		if (result.size() == 0)
			return false;
		return true;
	}

	private void loadPrototypes(String protoFile) {
		ArrayList<String> lines = IOManager.readLines(protoFile);
		for (String line : lines) {
			String[] splits = line.split("\\t+");
			if (splits.length != 2)
				continue;
			String semclass = splits[1];
			String member = splits[0].toLowerCase();
			if (prototypes.containsKey(semclass)) {
				List<String> protos = prototypes.get(semclass);
				protos.add(member);
			} else {
				List<String> protos = new ArrayList<String>();
				protos.add(member);
				prototypes.put(semclass, protos);
			}
		}
	}

	private void makePrototypeCentroidVector() throws Exception {
		for (String semclass : prototypes.keySet()) {
			List<String> protos = prototypes.get(semclass);
			Vector<Double> protoVector = null;
			int cN = 0;
			for (String p : protos) {
				List<ILuceneResult> result = searcher.search(p + "-n");
				if (result.size() == 0)
					continue;
				String content = result.get(0).getDoc();
				String[] s1 = content.split("\\t+");
				Vector<Double> v = new Vector<Double>();
				for (String s : s1) {
					v.add(Double.parseDouble(s));
				}
				if (protoVector == null) {
					protoVector = new Vector<Double>();
					for (int i = 0; i < v.size(); i++) {
						protoVector.add(0.0);
					}
				}
				for (int i = 0; i < v.size(); i++) {
					protoVector.set(i, protoVector.get(i) + v.get(i));
				}
				cN++;
			}
			int nN = protoVector.size();
			for (int i = 0; i < nN; i++) {
				protoVector.set(i, protoVector.get(i) / (double) cN);
			}
			if (protoVector == null) {
				System.out.println("ERROR: protoVector = null!");
				System.exit(1);
			}
			protoCentroidVectors.put(semclass, protoVector);
		}
	}

	private List<Pair<String, Pair<String, String>>> loadConceptPairs(
			String inputFile) {
		List<Pair<String, Pair<String, String>>> data = new ArrayList<Pair<String, Pair<String, String>>>();
		ArrayList<String> lines = IOManager.readLines(inputFile);
		int c = 0;
		ArrayList<String> outLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.length() == 0)
				continue;
			String[] splits = line.split("\\t+");
			String relation = splits[0];
			String term1 = splits[2];
			String term2 = splits[3];
			Pair<String, Pair<String, String>> instance = new Pair<String, Pair<String, String>>(
					relation, new Pair<String, String>(term1, term2));
			data.add(instance);
		}
		return data;
	}

	public void classify(String inputFile, String protoFile) throws Exception {

		System.out.println("Open Strudel index directory.");
		searcher.open(indexDir);

		System.out.println("Load prototypes.");
		loadPrototypes(protoFile);

		System.out.println("Make prototype centroid vectors.");
		makePrototypeCentroidVector();

		System.out.println("Load pairs of concepts.");
		List<Pair<String, Pair<String, String>>> data = loadConceptPairs(inputFile);

		System.out.println("Classify...");
		int correct = 0;
		int total = 0;
		for (Pair<String, Pair<String, String>> instance : data) {
			String relation = instance.getFirst();
			String term1 = instance.getSecond().getFirst();
			String term2 = instance.getSecond().getSecond();

			String semclass1 = classifySemClass(term1);

			if (semclass1.length() == 0) {
				System.out.println("-" + instance);
				continue;
			}

			String semclass2 = classifySemClass(term2);

			if (semclass2.length() == 0) {
				System.out.println("-" + instance);
				continue;
			}

			if (semclass1.equals(semclass2)) {
				if (relation.equals("3")) {
					// System.out.println("T\t" + instance);
					correct++;
				} else {
					System.out.println("F\t" + instance);
				}
			} else {
				if (relation.equals("0")) {
					// System.out.println("T\t" + instance);
					correct++;
				} else {
					System.out.println("F\t" + instance);
				}
			}
			total++;
		}

		System.out.println("Accuracy: " + correct + "/" + total + " = "
				+ ((double) correct / (double) total));

		searcher.close();
	}

	/**
	 * @param term
	 * @return
	 * @throws Exception
	 */
	private String classifySemClass(String term) throws Exception {
		List<ILuceneResult> result = searcher.search(term + "-n");
		if (result.size() == 0) {
			System.out.println("* " + term + " is not in Strudel.");
			return "";

		}
		Vector<Double> vectorNoun = new Vector<Double>();
		String content = result.get(0).getDoc();
		String[] s1 = content.split("\\t+");
		for (String s : s1) {
			vectorNoun.add(Double.parseDouble(s));
		}

		double bestScore = -100.0;
		String res = "";
		for (String semclass : protoCentroidVectors.keySet()) {
			Vector<Double> centroid = protoCentroidVectors.get(semclass);
			double sim = CosineSimilarity.getSimilarity(vectorNoun, centroid);
			if (sim > bestScore) {
				bestScore = sim;
				res = semclass;
			}
		}

		return res;
	}

	public void GetCleanPairs(String inputFile, String outputFile) {
		ArrayList<String> lines = IOManager.readLines(inputFile);
		ArrayList<String> outLines = new ArrayList<String>();
		for (String line : lines) {
			String[] splits = line.split("\\t+");
			if (splits.length != 4)
				continue;
			if (!splits[2].endsWith("_junk") && !splits[3].endsWith("_junk")) {
				outLines.add(line);
			}
		}
		IOManager.writeLinesAddingReturn(outLines, outputFile);
	}

}
