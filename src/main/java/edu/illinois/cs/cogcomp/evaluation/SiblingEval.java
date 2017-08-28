/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.category.CategoryMatching;
import edu.illinois.cs.cogcomp.detection.RelationDetector;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 8, 2009
 */
public class SiblingEval {

	String inputTestFile;

	CategoryMatching catMatcher;

	RelationDetector detector;

	TargetClassMapping targetClassMapper = null;

	/**
	 * 
	 */
	public SiblingEval(String inputTestFile, String titleIndexDir,
			String categoryIndexDir, boolean useProminence,
			boolean useMutualInformation) throws Exception {
		this.inputTestFile = inputTestFile;
		catMatcher = new CategoryMatching(titleIndexDir, categoryIndexDir,
				useProminence, useMutualInformation);
	}

	/**
	 * 
	 */
	public SiblingEval(String inputTestFile, String titleIndexDir,
			String categoryIndexDir, String peopleFile, boolean useProminence,
			boolean useMutualInformation) throws Exception {

		this.inputTestFile = inputTestFile;

		detector = new RelationDetector(titleIndexDir, categoryIndexDir);

		detector.setPeopleManager(peopleFile);

		detector.outputToStdout = false;
	}

	public void setTargetClassMapper(String mapFile) {

		targetClassMapper = new TargetClassMapping(mapFile);
	
	}

	public void evaluateOld(String outputResultFile) throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputResultFile);

		ArrayList<String> arrLines = IOManager.readLines(inputTestFile);

		for (String line : arrLines) {

			System.out.println(line);
			String result = evaluateExample(line);
			if (result == null || result == "")
				result = "NULL";
			writeResult(line, result, writer);

		}

		IOManager.closeWriter(writer);
	}

	public void evaluate(String outputResultFile) throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputResultFile);

		ArrayList<String> arrLines = IOManager.readLines(inputTestFile);

		int i = 1;

		for (String line : arrLines) {

			System.out.println("[" + i + "/" + arrLines.size() + "] " + line);
			String result = evaluateExample(line);

			if (result == null || result == "")
				result = "NULL";

			writeResult(line, result, writer);

			i++;
		}

		IOManager.closeWriter(writer);
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

	private String evaluateExample(String line) throws Exception {

		String result = "";
		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		// String entityClass = tokens[0];

		String e_1 = tokens[1];
		String e_2 = tokens[2];

		if (targetClassMapper != null && targetClassMapper.isValidTargetClass(e_1))
			e_1 = targetClassMapper.getFirstItem(e_1);

		if (targetClassMapper != null && targetClassMapper.isValidTargetClass(e_2))
			e_2 = targetClassMapper.getFirstItem(e_2);

		result = detector.detectRelation(e_1, e_2, RelationDetector.TASK_SIBLING);

		return result;

	}

	/**
	 * @param line
	 * @return
	 */
	private String evaluateExampleOld(String line) throws Exception {

		String result = "";
		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		String entityClass = tokens[0];
		String entity1 = tokens[1];
		String entity2 = tokens[2];

		result = catMatcher.matchCategory(entity1, entity2, true);

		return result;
	}
}
