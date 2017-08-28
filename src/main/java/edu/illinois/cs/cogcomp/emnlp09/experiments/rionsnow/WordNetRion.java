/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.experiments.rionsnow;

import java.io.BufferedWriter;
import java.util.ArrayList;

import net.didion.jwnl.data.POS;

import edu.illinois.cs.cogcomp.evaluation.TargetClassMapping;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Feb 17, 2009
 */
public class WordNetRion {

	public static final int TASK_PARENTCHILD = 1;

	public static final int TASK_SIBLING = 2;

	public static final int MAX_LEVEL_UP = 5;

	String inputFile;

	WordNetManager wnManager;

	TargetClassMapping targetClassMapper = null;

	public static final String ANCESTOR = ">ANCESTOR<";
	public static final String SIBLING = ">SIBLING<";
	public static final String NULL = ">NULL<";

	/**
	 * 
	 */
	public WordNetRion(String inputFile) throws Exception {

		this.inputFile = inputFile;

		wnManager = new WordNetManager("file_properties.xml");

	}

	public void setTargetClassMapper(String mapFile) {

		targetClassMapper = new TargetClassMapping(mapFile);

	}

	public void evaluate(String outputResultFile, int task) throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputResultFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		int n = arrLines.size();

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

		String prediction = "";

		for (String line : arrLines) {

			System.out.println("[" + i + "/" + n + "] " + line);

			String relation = NULL;

			String tokens[] = line.split("\\t+");

			if (tokens.length != 3)
				continue;

			String entityClass = tokens[0];
			String e_1 = tokens[1];
			String e_2 = tokens[2];

			if (entityClass.equals(e_1)) {
				relation = ANCESTOR;
			}

			if (!entityClass.equals(e_1) && !e_1.equals(e_2))
				relation = SIBLING;

			if (entityClass.equals("NONE"))
				relation = NULL;

			// System.out.println("Relation: " + relation);

			String result = null;

			if (task == TASK_SIBLING)

				result = evaluateExampleSibling(line);

			else if (task == TASK_PARENTCHILD)

				result = evaluateExampleParentChild(line);

			else { // TASK_ALL

				result = evaluateExampleParentChild(line);

				if (result == null || result.trim().length() == 0
						|| result.trim().equals(NULL)) {

					result = evaluateExampleSibling(line);

					if (result == null || result.trim().length() == 0
							|| result.trim().equals(NULL)) {

						result = NULL + "\n";
						prediction = NULL;

					} else {

						result = result + "\n" + SIBLING + "\n";
						prediction = SIBLING;

					}

				} else {

					result = result + "\n" + ANCESTOR + "\n";
					prediction = ANCESTOR;
				}
			}

			if (result == null || result == "") {
				result = NULL;
				prediction = NULL;
			}

			// System.out.println("Prediction: " + prediction);

			if (relation.equals(ANCESTOR))
				totalAnc++;

			else if (relation.equals(SIBLING))
				totalCou++;

			else if (relation.equals(NULL))
				totalNon++;

			if (prediction.equals(relation)) {

				correct++;

				if (prediction.equals(SIBLING))
					correctCou++;

				else if (prediction.equals(ANCESTOR))
					correctAnc++;

				else if (prediction.equals(NULL))
					correctNon++;

			}

			else {

				if (prediction.equals(SIBLING))
					incorrectCou++;

				else if (prediction.equals(ANCESTOR))
					incorrectAnc++;

				else if (prediction.equals(NULL))
					incorrectNon++;
			}

			i++;

			// writeResult(line, result, writer);

		}

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

		IOManager.closeWriter(writer);
	}

	/**
	 * @param line
	 * @return
	 */
	private String evaluateExampleParentChild(String line) throws Exception {
		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		String entityClass = tokens[0];
		String e_1 = tokens[1];
		String e_2 = tokens[2];

		if (entityClass.equals("NONE")) {
			entityClass = e_1;
			e_1 = e_2;
		}

		if (entityClass.equals(e_1)) {
			entityClass = e_1;
			e_1 = e_2;
		}

		// if (targetClassMapper != null
		// && targetClassMapper.isValidTargetClass(entityClass))
		// entityClass = targetClassMapper.getFirstItem(entityClass);

		// if (targetClassMapper != null
		// && targetClassMapper.isValidTargetClass(entityClass))
		// entityClass = targetClassMapper.getFirstItem(entityClass);

		// System.out.println("\t" + entityClass + ", " + e_1);

		String res = evalParentChild(entityClass, e_1);

		if (res.trim().length() == 0)
			return ">NULL<";

		return res;
	}

	/**
	 * @param entityClass
	 * @param e_1
	 * @return
	 */
	private String evalParentChild(String entityClass, String e_1)
			throws Exception {

		String res;

		if (wnManager.getWord(POS.NOUN, e_1) == null) {

			res = "";
			// res = "NOT FOUND IN WORDNET " + e_1 + "\n" + ">NULL<" + "\n";
		} else {

			ArrayList<String> arrHypernymE2 = wnManager.getHypernymForNoun(e_1,
					MAX_LEVEL_UP);

			// System.out.println("entityClass: " + entityClass + ", e_1: " +
			// e_1);
			// if (arrHypernymE2.size() == 0)
			// System.out.println("NO HYPERNYM.");
			// else {
			// for (String hyp : arrHypernymE2)
			// System.out.print(hyp + ", ");
			//
			// System.out.println();
			// }
			res = matching(entityClass, arrHypernymE2);

		}
		return res;

	}

	/**
	 * @param e_1
	 * @param arrHypernymE2
	 * @return
	 */
	private String matching(String e_1, ArrayList<String> arrHypernymE2) {

		StringBuffer buf = new StringBuffer("");

		ArrayList<String> arrTargets = targetClassMapper
				.getTargetClassItems(e_1);

		if (arrTargets == null || arrTargets.size() == 0) {
			arrTargets = new ArrayList<String>();
			arrTargets.add(e_1);
		}

		for (String h2 : arrHypernymE2) {

			h2 = h2.replace('_', ' ');

			boolean check = false;
			for (String target : arrTargets)
				if (h2.indexOf(target) != -1 || target.indexOf(h2) != -1) {
					buf.append("\t--- " + h2 + "\n");
					check = true;
					break;
				}

			// if (h2.equals(e_1))
			// buf.append("\t--- " + h2 + "\n");

			if (check == true)
				break;
		}

		return buf.toString();

	}

	/**
	 * @param line
	 * @param result
	 * @param writer
	 */
	private void writeResult(String line, String result, BufferedWriter writer)
			throws Exception {
		StringBuffer outString = new StringBuffer("");
		outString.append("<DOC>\n");
		outString.append("<QUERY>" + line + "</QUERY>\n");
		outString.append("<RESULT>\n" + result + "\n</RESULT>\n");
		outString.append("</DOC>\n");
		writer.write(outString.toString());
	}

	/**
	 * @param line
	 * @return
	 */
	private String evaluateExampleSibling(String line) throws Exception {

		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		String e_1 = tokens[1];
		String e_2 = tokens[2];

		if (targetClassMapper != null
				&& targetClassMapper.isValidTargetClass(e_1))
			e_1 = targetClassMapper.getFirstItem(e_1);

		if (targetClassMapper != null
				&& targetClassMapper.isValidTargetClass(e_2))
			e_2 = targetClassMapper.getFirstItem(e_2);

		String res = evalSibling(e_1, e_2);

		if (res.trim().length() == 0)
			return ">NULL<";

		return res;
	}

	/**
	 * @param e_1
	 * @param e_2
	 * @return
	 */
	private String evalSibling(String e_1, String e_2) throws Exception {

		String res;

		if (wnManager.getWord(POS.NOUN, e_1) == null) {
			if (wnManager.getWord(POS.NOUN, e_2) == null) {
				// res = "NOT FOUND IN WORDNET " + e_1 + "\n"
				// + "NOT FOUND IN WORDNET " + e_2 + "\n" + ">NULL<" + "\n";
				res = "";
			} else {
				// res = "NOT FOUND IN WORDNET " + e_1 + "\n" + ">NULL<" + "\n";
				res = "";
			}
		} else {
			if (wnManager.getWord(POS.NOUN, e_2) == null) {
				// res = "NOT FOUND IN WORDNET " + e_2 + "\n" + ">NULL<" + "\n";
				res = "";
			} else {

				ArrayList<String> arrHypernymE1 = wnManager.getHypernymForNoun(
						e_1, MAX_LEVEL_UP);

				ArrayList<String> arrHypernymE2 = wnManager.getHypernymForNoun(
						e_2, MAX_LEVEL_UP);

				res = matching(arrHypernymE1, arrHypernymE2);
			}
		}
		return res;
	}

	/**
	 * @param arrHypernymE1
	 * @param arrHypernymE2
	 * @return
	 */
	private String matching(ArrayList<String> arrHypernymE1,
			ArrayList<String> arrHypernymE2) {
		StringBuffer buf = new StringBuffer("");

		for (String h1 : arrHypernymE1) {

			for (String h2 : arrHypernymE2) {

				if (h1.equals(h2))
					buf.append("\t--- " + h1 + "\n");
			}
		}

		return buf.toString();
	}

}
