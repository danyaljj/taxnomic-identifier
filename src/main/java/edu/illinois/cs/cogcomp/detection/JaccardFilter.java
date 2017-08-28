/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.SearcherFactory;
import edu.illinois.cs.cogcomp.lucenesearch.TextSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.SearcherFactory.SearcherType;

/**
 * @author dxquang
 * Feb 19, 2009
 */
public class JaccardFilter {

	/**
	 * 
	 */
	public ILuceneSearcher searcher = null;
	
	public int numResult;
	
	public JaccardFilter(String indexDirectory, int numResult) throws Exception {

		this.numResult = numResult;
		
		HashMap<String, String> parameters = new HashMap<String, String>();
		
		parameters.put("TotalResultsToRerank", "1000");

		SearcherType type = null;

		try {
			
			type = SearcherType.valueOf("TextSearch");
			
		} catch (Exception ex) {
			
			System.out.println("\nIUnable to get TextSearcher\n");
			System.exit(1);
		}

		searcher = SearcherFactory.getSearcher(type,
				indexDirectory, "config.xml", parameters);
		
	}
	
	public double getJaccardScore(String e_1, String e_2) throws Exception {
		
		ArrayList<ILuceneResult> arrResultE1 = searcher.search(e_1, numResult);
		
		System.out.println("Search e_1 is done!");
		
		ArrayList<String> arrDocIdE1 = getArrDocIds(arrResultE1);

		System.out.println("getArrDocIds for e_1 is done.");

		ArrayList<ILuceneResult> arrResultE2 = searcher.search(e_2, numResult);

		System.out.println("Search e_2 is done!");

		ArrayList<String> arrDocIdE2 = getArrDocIds(arrResultE2);

		System.out.println("getArrDocIds for e_2 is done.");

		Set<String> setResultE1Inter = new HashSet<String>(arrDocIdE1);

		Set<String> setResultE1Union = new HashSet<String>(arrDocIdE1);

		Set<String> setResultE2 = new HashSet<String>(arrDocIdE2);
		
		setResultE1Inter.retainAll(setResultE2);
		
		setResultE1Union.addAll(setResultE2);
		
		double jaccard = (double) setResultE1Inter.size() / (double) setResultE1Union.size();
		
		return jaccard;
	}

	/**
	 * @param arrResultE1
	 * @return
	 */
	private ArrayList<String> getArrDocIds(ArrayList<ILuceneResult> arrResultE1) {
		ArrayList<String> arrInts = new ArrayList<String>();
		
		for (ILuceneResult result : arrResultE1) {
			
			arrInts.add(result.getId());
			
			
		}
		return arrInts;
	}
}
