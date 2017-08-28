package edu.illinois.cs.cogcomp.web.yahoo;

/**
 * @author dxquang
 */

import java.math.BigInteger;
import java.util.ArrayList;


import com.yahoo.search.SearchClient;
import com.yahoo.search.WebSearchRequest;
import com.yahoo.search.WebSearchResult;
import com.yahoo.search.WebSearchResults;
import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.ISearch;

public class YahooSearch implements ISearch {

	// =======
	// Constants
	private static final String APP_ID = "FFyKxinV34GkEXoIz5tslCigQN3Qa3t4F1u53nIa4btRUf46MNqeup.b6PcgMA--";
	private static final String LANGUAGE = "en";
	private static final int RETURN_LIMIT = 100;

	// ========
	// Variables
	private SearchClient searchClient;
	private long totalResult;

	// ========
	public YahooSearch() {
		searchClient = new SearchClient(APP_ID);
		totalResult = -1;
	}

	// ========
	@Override
	public ArrayList<IResult> searchWeb(String query, int numResult)
			throws Exception {
		System.out.println("Querying " + "\"" + query + "\"" + " for "
				+ numResult + " results...");
		int numIteration = (int) (Math
				.ceil((double) ((float) numResult / (float) RETURN_LIMIT)));
		//System.out.println("numIteration: " + numIteration);
		totalResult = -1;
		ArrayList<IResult> arrAllResults = new ArrayList<IResult>();
		for (int i = 0; i < numIteration; i++) {
			int offset = i * RETURN_LIMIT;
			int count = Math.min(RETURN_LIMIT, numResult - offset);
			//System.out.println("Offset: " + offset + "; count: " + count);
			System.out.println("\tQuery results from " + (offset + 1) + " to "
					+ (offset + count) + "...");
			WebSearchRequest request = makeWebSearchRequest(query, offset, count);
			ArrayList<IResult> arrResults = doSearch(request);
			arrAllResults.addAll(arrResults);
		}
		//System.out.println("# of returned result: " + arrAllResults.size());
		return arrAllResults;
	}

	// ========
	private WebSearchRequest makeWebSearchRequest(String query, int offset, int numResult) {
		WebSearchRequest request = new WebSearchRequest(query);
		request.setLanguage(LANGUAGE);
		request.setResults(numResult);
		request.setStart(new BigInteger(String.valueOf(offset)));
		return request;
	}

	// ========
	private ArrayList<IResult> doSearch(WebSearchRequest request) {
		ArrayList<IResult> arrResults = new ArrayList<IResult>();
		try {
			WebSearchResults results = searchClient.webSearch(request);
			if (totalResult == -1)
				totalResult = results.getTotalResultsAvailable().longValue();
			arrResults = getSearchResults(results);
		} catch (Exception e) {
			System.err.println("Error calling Yahoo! Search Service: "
					+ e.toString());
			e.printStackTrace(System.err);
		}
		return arrResults;
	}

	/*
	 * //======== private String getSearchResults(WebSearchResults results) {
	 * StringBuffer output = new StringBuffer(""); // Iterate over the results.
	 * for (int i = 0; i < results.listResults().length; i++) { WebSearchResult
	 * result = results.listResults()[i]; String title = result.getTitle();
	 * String summary = result.getSummary(); output.append("\t<RESULTS>\n");
	 * output.append("\t\t<INDEX>" + globalIndex + "</INDEX>\n"); globalIndex
	 * ++; if (title != null) output.append("\t\t<TITLE>" + title +
	 * "</TITLE>\n"); if (summary != null) output.append("\t\t<DESCRIPTION>" +
	 * summary + "</DESCRIPTION>\n"); output.append("\t</RESULTS>\n"); } return
	 * output.toString(); }
	 */
	// ======
	
	private ArrayList<IResult> getSearchResults(WebSearchResults results) {
		int n = results.listResults().length;
		ArrayList<IResult> yResults = new ArrayList<IResult>();
		for (int i = 0; i < n; i++) {
			WebSearchResult result = results.listResults()[i];
			String title = result.getTitle();
			String snippet = result.getSummary();
			String url = result.getUrl();
			YahooResult yResult = new YahooResult(title, snippet, url);
			yResults.add(yResult);
		}
		return yResults;
	}

}
