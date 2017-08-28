/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 19, 2009
 */
public class MappingTargetClass {

	public class ResultDoc {

		public String targetClass;

		public ArrayList<String> arrPredicts = new ArrayList<String>();

		public String relation;

	}

	public Map<String, ArrayList<String>> mapTargetClasses = new HashMap<String, ArrayList<String>>();

	public int sign;

	/**
	 * 
	 * @param targetClassFile
	 * @param sign
	 *            : 0 negative, 1 postive
	 */
	public MappingTargetClass(String targetClassFile, int sign) {
		this.sign = sign;
		readTargetClasses(targetClassFile);
	}

	/**
	 * @param targetClassFile
	 */
	private void readTargetClasses(String targetClassFile) {
		ArrayList<String> arrLines = IOManager.readLines(targetClassFile);
		for (String line : arrLines) {
			line = line.trim();
			// System.out.println(line);
			if (line.length() == 0)
				continue;
			String items[] = line.split("\\t");
			String targetClass = items[0];
			String classes = items[1];
			if (classes.trim().length() == 0)
				continue;
			String names[] = classes.split(",+");
			ArrayList<String> arrNames = new ArrayList<String>();
			for (String name : names) {
				name = name.trim();
				arrNames.add(name);
			}
			// System.out.println("\t- ArrayNames: " + arrNames.size());
			mapTargetClasses.put(targetClass, arrNames);
		}
		// System.out.println("- MapTargetClasses: " + mapTargetClasses.size());
	}

	public void mapping(String resultFile, String outputFile) throws Exception {

		ArrayList<ResultDoc> arrResultDocs = parseResultFile(resultFile);

		mappingResultDocs(arrResultDocs, outputFile);
	}

	/**
	 * @param arrResultDocs
	 * @param outputFile
	 */
	private void mappingResultDocs(ArrayList<ResultDoc> arrResultDocs,
			String outputFile) throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputFile);

		for (ResultDoc resultDoc : arrResultDocs) {
			StringBuffer buf = new StringBuffer("");
			if (resultDoc.relation.equals("NONE")) {
				buf.append(">NONE<\n");
			} else if (resultDoc.relation.equals("PARENT")) {
				if (sign == 0) {	// negative
					buf.append(">ANCESTOR< - NONE\n");
				} else {	// positive
					if (match(resultDoc.targetClass, resultDoc.arrPredicts))
						buf.append(">ANCESTOR<\n");
					else {
						buf.append(">NONE< - ANCESTOR - "
								+ resultDoc.targetClass + " - ");
						for (String predict : resultDoc.arrPredicts)
							buf.append(predict + ", ");
						buf.append("\n");
					}
				}
			} else { // sibling
				if (sign == 0) { // negative
					buf.append(">SIBLING< - NONE\n");
				} else { // postive
					if (match(resultDoc.targetClass, resultDoc.arrPredicts))
						buf.append(">SIBLING<\n");
					else {
						buf.append(">NONE< - SIBLING - "
								+ resultDoc.targetClass + " - ");
						for (String predict : resultDoc.arrPredicts)
							buf.append(predict + ", ");
						buf.append("\n");
					}
				}
			}
			writer.write(buf.toString());
		}

		IOManager.closeWriter(writer);

	}

	/**
	 * @param targetClass
	 * @param arrPredicts
	 * @return
	 */
	private boolean match(String targetClass, ArrayList<String> arrPredicts) {
		if (!mapTargetClasses.containsKey(targetClass))
			return false;
		ArrayList<String> arrTargetClass = mapTargetClasses.get(targetClass);
		// System.out.println("- Comparing:");
		for (String target : arrTargetClass) {
			// System.out.println(">>>" + target + "<<<");
			for (String predict : arrPredicts) {
				// System.out.println("\t>>>" + predict + "<<<");
				if (predict.indexOf(target) != -1)
					return true;
				if (target.indexOf(predict) != -1)
					return true;
			}
		}
		return false;
	}

	/**
	 * @param resultFile
	 * @return
	 */
	private ArrayList<ResultDoc> parseResultFile(String resultFile) {
		ArrayList<ResultDoc> arrResultDocs = new ArrayList<ResultDoc>();
		ArrayList<String> arrLines = IOManager.readLines(resultFile);
		ArrayList<String> arrDoc = null;
		for (String line : arrLines) {
			if (line.length() == 0)
				continue;

			if (line.startsWith("<DOC>")) {
				arrDoc = new ArrayList<String>();
				arrDoc.add(line);
			} else if (line.startsWith("</DOC>")) {
				arrDoc.add(line);
				ResultDoc resultDoc = getResult(arrDoc);
				arrResultDocs.add(resultDoc);
				// System.out.println("\n----------------------\n");
			} else {
				arrDoc.add(line);
			}
		}
		return arrResultDocs;
	}

	/**
	 * @param arrDoc
	 * @return
	 */
	private ResultDoc getResult(ArrayList<String> arrDoc) {
		ResultDoc resultDoc = new ResultDoc();

		int check = 0;
		ArrayList<String> arrClasses = new ArrayList<String>();
		int type = 0;
		for (String line : arrDoc) {
			if (check == 1) {
				if (line.matches("\\(\\d+\\) .+MI: .+")) {
					// System.out.println("\tSIBLING - " + line);
					int posS = line.indexOf(')');
					int posE = line.indexOf('-');
					String predict = line.substring(posS + 1, posE);
					predict = predict.trim();
					arrClasses.add(predict);
					// System.out.println("\t(" + predict + ")");
					type = 1; // sibling
				} else if (line.matches("\\(.+\\)")) {
					// System.out.println("\tPARENT - " + line);
					int posS = line.indexOf('(');
					int posE = line.indexOf(')');
					String predict = line.substring(posS + 1, posE);
					predict = predict.trim();
					arrClasses.add(predict);
					// System.out.println("\t(" + predict + ")");
					type = 2; // parent
				}
			}
			if (line.startsWith("<QUERY>")) {
				String query = line.substring("<QUERY>".length(), line.length()
						- "</QUERY>".length());
				String items[] = query.split("\\t");
				resultDoc.targetClass = items[0];
				// System.out.println("- Query: " + query);
				// System.out
				// .println("\t- Target class: " + resultDoc.targetClass);
			} else if (line.startsWith("<RESULT>")) {
				check = 1;
			} else if (line.startsWith("</RESULT>")) {
				check = 0;
			}
		}
		if (type == 1)
			resultDoc.relation = "SIBLING";
		else if (type == 2)
			resultDoc.relation = "PARENT";
		else
			resultDoc.relation = "NONE";
		resultDoc.arrPredicts = arrClasses;
		return resultDoc;
	}
}
