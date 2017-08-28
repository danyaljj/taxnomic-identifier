/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.detection.RelationDetector;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 17, 2009
 */
public class ParentChildEval {

	String inputTestFile;

	RelationDetector detector;

	TargetClassMapping targetClassMapper = null;

	/**
	 * 
	 */
	public ParentChildEval(String inputTestFile, String titleIndexDir,
			String categoryIndexDir, String peopleFile, boolean useProminence,
			boolean useMutualInformation) throws Exception {

		this.inputTestFile = inputTestFile;

		detector = new RelationDetector(titleIndexDir, categoryIndexDir);
		
		detector.setUseProminence(useProminence);

		detector.setPeopleManager(peopleFile);

		detector.outputToStdout = false;
	}

	public void setTargetClassMapper(String mapFile) {

		targetClassMapper = new TargetClassMapping(mapFile);
	
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

		String entityClass = tokens[0];
		String e_1 = tokens[1];
		String e_2 = tokens[2];
		
		if (entityClass.equals("NONE")) {
			entityClass = e_1;
			e_1 = e_2;
		}

		if (targetClassMapper != null && targetClassMapper.isValidTargetClass(entityClass))
			entityClass = targetClassMapper.getFirstItem(entityClass);

		if (targetClassMapper != null && targetClassMapper.isValidTargetClass(e_1))
			e_1 = targetClassMapper.getFirstItem(e_1);

		result = detector.detectRelation(entityClass, e_1, 1);

		return result;
	}

}
