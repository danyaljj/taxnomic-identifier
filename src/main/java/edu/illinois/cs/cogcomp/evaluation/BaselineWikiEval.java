/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.ListSearcher;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang
 * Feb 8, 2009
 */
public class BaselineWikiEval {

	String inputFile;
	int numResult;
	ListSearcher listSearcher;
	
	WordNetManager wnManager;
	
	/**
	 * 
	 */
	public BaselineWikiEval(String inputFile, String listIndexDir, int numResults) throws Exception {
		this.inputFile = inputFile;
		this.numResult = numResults;
		listSearcher = new ListSearcher(null, true);
		listSearcher.open(listIndexDir);
		wnManager = new WordNetManager("file_properties.xml");
	}
	
	public void evaluate(String outputResultFile) throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputResultFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		for (String line : arrLines) {

			System.out.println(line);
			String result = evaluateExample(line);
			if (result == null || result == "")
				result = "NULL";
			writeResult(line, result, writer);

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

	/**
	 * @param line
	 * @return
	 */
	private String evaluateExample(String line) throws Exception {

		StringBuffer result = new StringBuffer("");
		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		String entityClass = tokens[0];
		String entity1 = tokens[1];
		String entity2 = tokens[2];

		ArrayList<ILuceneResult> arrResults = listSearcher.search(entity1, entity2, numResult);
		
		int i=1;
		for (ILuceneResult lResult : arrResults) {
			result.append("[" + i + "] " + lResult.getTitle() + "\n");
			i ++;
		}

		return result.toString();
	}

}
