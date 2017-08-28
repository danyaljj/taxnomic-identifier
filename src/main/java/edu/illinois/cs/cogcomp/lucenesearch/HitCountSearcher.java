/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author dxquang Feb 3, 2009
 */

public class HitCountSearcher {

	protected IndexSearcher searcher;

	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	protected Set<String> setStopWords;

	protected int numDocs;

	/**
	 * 
	 */
	public HitCountSearcher(String indexDir) throws Exception {
		setStopWords = parseStopWordString(STOPWORD_STRING);
		open(indexDir);
		IndexReader reader = searcher.getIndexReader();
		numDocs = reader.numDocs();
	}

	/**
	 * Opens the index created by the Lucene indexer
	 */
	public void open(String indexDirectory) throws IOException {

		File indexDir = new File(indexDirectory);

		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fsDir = FSDirectory.getDirectory(indexDir);

		searcher = new IndexSearcher(fsDir);

		searcher.setSimilarity(new DefaultSimilarity());

	}

	/**
	 * @param stopwordString
	 * @return
	 */
	private Set<String> parseStopWordString(String stopwordString) {
		Set<String> setSW = new HashSet<String>();
		String tokens[] = STOPWORD_STRING.split(",+");
		for (String token : tokens)
			setSW.add(token);
		return setSW;
	}

	protected Query makeQueryForProminence(String queryText) throws Exception {

		// System.out.println("Input query: " + queryText);

		Object terms[] = standardizeQuery(queryText);

		Term termHandler = new Term("text", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			// System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}
		// System.out.println();

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);

		return bq;
	}

	public int searchTextForTotalHits(String queryText) throws Exception {

		if (queryText.replaceAll("\\s+", "").length() == 0)
			return 0;

		Query query = makeQueryForProminence(queryText);

		Hits hits = searcher.search(query);
		int totalHits = hits.length();

		return totalHits;
	}

	public int searchTextForTotalHits(String text1, String text2)
			throws Exception {

		if (text1.replaceAll("\\s+", "").length() == 0)
			return 0;

		if (text2.replaceAll("\\s+", "").length() == 0)
			return 0;

		Query query1 = makeQueryForProminence(text1);
		Query query2 = makeQueryForProminence(text2);

		BooleanQuery bq = new BooleanQuery();
		bq.add(query1, BooleanClause.Occur.MUST);
		bq.add(query2, BooleanClause.Occur.MUST);

		Hits hits = searcher.search(bq);
		int totalHits = hits.length();

		return totalHits;
	}

	/**
	 * @param queryText
	 * @return
	 */
	private Object[] standardizeQuery(String queryText) {

		// Lowercase
		queryText = queryText.toLowerCase();

		// Delete all tokens between "(" and ")"
		queryText = queryText.replaceAll("\\(.*\\)", " ");

		// Remove punctuation
		queryText = queryText.replaceAll("\\(", "xZxZ");
		queryText = queryText.replaceAll("\\)", "yZyZ");
		queryText = queryText.replaceAll("\\|", "wZwZ");
		queryText = queryText.replaceAll(":", "vZvZ");

		queryText = queryText.replaceAll("\\p{Punct}", " ");

		queryText = queryText.replaceAll("xZxZ", "( ");
		queryText = queryText.replaceAll("yZyZ", " )");
		queryText = queryText.replaceAll("wZwZ", "|");
		queryText = queryText.replaceAll("vZvZ", ":");

		queryText = queryText.trim();

		// Remove stop words
		String tokens[] = queryText.split("\\s+");

		ArrayList<String> arrTerms = new ArrayList<String>();
		for (String token : tokens)
			if (!setStopWords.contains(token))
				arrTerms.add(token);

		return arrTerms.toArray();
	}

	public double pointwiseMutualInformation(String text1, String text2)
			throws Exception {

		int cXY = searchTextForTotalHits(text1, text2);
		// System.out.println("cXY: " + cXY);

		if (cXY == 0)
			return (double) 0.0;

		int cX = searchTextForTotalHits(text1);
		// System.out.println("cX: " + cX);

		int cY = searchTextForTotalHits(text2);
		// System.out.println("cY: " + cY);

		// double pXY = (double)cXY / (double)Math.min(cX, cY);
		double pXY = (double) cXY / (double) numDocs;
		double pX = (double) cX / (double) numDocs;
		double pY = (double) cY / (double) numDocs;

		// discounting factor
		// double disFact = ((double) cXY / (double) (cXY + 1))
		// * ((double)(Math.min(cX, cY)) / (double)(Math.min(cX, cY) + 1));
		// System.out.println("disFact: " + disFact);

		// double pwMI = Math.log(cXY * Math.log((pXY / (pX * pY)) * disFact));
		double pwMI = Math.log(cXY * Math.log(pXY / (pX * pY)));

		return pwMI;
	}
}
