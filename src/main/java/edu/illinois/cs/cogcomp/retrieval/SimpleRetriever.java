package edu.illinois.cs.cogcomp.retrieval;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.yahoo.YahooSearch;

/**
 * 
 * @author dxquang
 * 
 */

public class SimpleRetriever extends ARetriever {

	public SimpleRetriever() {
		searcher = new YahooSearch();
	}

	@Override
	public String makeQuery(String entity) {
		return entity;
	}

	@Override
	public String makeQuery(String entity1, String entity2) {
		return "\"" + entity1 + "\"" + " AND " + "\"" + entity2 + "\"";
	}

	@Override
	public void retrieve(String query, int numResult) {
		try {
			arrResult = null;
			arrResult = new ArrayList<IResult>();
			arrResult = searcher.searchWeb(query, numResult);
			//System.out.println("arrResult.size()=" + arrResult.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to search the web with the query: "
					+ query);
			System.exit(1);
		}
	}

}
