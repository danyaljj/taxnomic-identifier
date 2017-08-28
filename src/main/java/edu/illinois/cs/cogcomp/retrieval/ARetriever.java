package edu.illinois.cs.cogcomp.retrieval;

import java.io.BufferedWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.ISearch;

/**
 * 
 * @author dxquang
 *
 */

public abstract class ARetriever {
	
	protected ISearch searcher = null;
	protected ArrayList<IResult> arrResult = null;
	
	abstract public String makeQuery(String entity);
	
	abstract public String makeQuery(String entity1, String entity2);
	
	abstract public void retrieve(String query, int numResult);
	
	public ArrayList<IResult> getArrResult() {
		return arrResult;
	}
	
	public void exportXML(String outputXMLFile) {
		BufferedWriter writer = IOManager.openWriter(outputXMLFile);
		try {
			int i=1;
			for (IResult result : this.arrResult) {
				StringBuffer sBuf = new StringBuffer("");
				sBuf.append("<DOC>\n");
				sBuf.append("\t<TITLE>" + result.getTitle() + "</TITLE>\n");
				sBuf.append("\t<SNIPPET>" + result.getSnippet() + "</SNIPPET>\n");
				sBuf.append("\t<URL>" + result.getUrl() + "</URL>\n");
				sBuf.append("</DOC>\n");
				//System.out.println(i + ". " + sBuf.toString());
				writer.write(sBuf.toString());
				i ++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to write to file " + outputXMLFile);
			System.exit(1);
		}
		IOManager.closeWriter(writer);
	}
	
	public String makeFileName(String entity) {
		String res = entity.trim();
		res = res.toLowerCase();
		res = res.replaceAll("\\s+", "");
		res = res.replaceAll("\\p{Punct}", "");
		return res;
	}
	
	public String makeFileName(String entity1, String entity2) {
		return makeFileName(entity1) + "_" + makeFileName(entity2);
	}
	
	public ArrayList<DocumentXML> importXML(String inputXMLFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputXMLFile);
		ArrayList<String> arrStrings = new ArrayList<String>();
		ArrayList<DocumentXML> arrDocXML = new ArrayList<DocumentXML>();
		for (String line : arrLines) {
			if (line.startsWith("<DOC>")) {
				arrStrings = null;
				arrStrings = new ArrayList<String>();
			}
			else if (line.startsWith("</DOC>")) {
				DocumentXML docXML = extractInformation(arrStrings);
				arrDocXML.add(docXML);
			}
			else {
				arrStrings.add(line);
			}
		}
		return arrDocXML;
	}

	private DocumentXML extractInformation(ArrayList<String> arrStrings) {
		String title = "";
		String snippet = "";
		String url = "";
		for (String string : arrStrings) {
			if (string.startsWith("<TITLE>")) {
				title = string.substring("<TITLE>".length(), string.length() - "</TITLE>".length());
			}
			else if (string.startsWith("<SNIPPET>")) {
				snippet = string.substring("<SNIPPET>".length(), string.length() - "</SNIPPET>".length());
			}
			else if (string.startsWith("<URL>")) {
				url = string.substring("<URL>".length(), string.length() - "</URL>".length());
			}
		}
		//System.out.println("Title: " + title);
		//System.out.println("Snippet: " + snippet);
		//System.out.println("Url: " + url);
		return new DocumentXML(title, snippet, url);
	}
	
}
