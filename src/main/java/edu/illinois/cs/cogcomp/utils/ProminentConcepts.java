/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.indexer.UnNormalizedLuceneSimilarity;

/**
 * @author dxquang Feb 3, 2009
 */

public class ProminentConcepts {

	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	public final static int LOWERBOUND_PROMINENCE = 0;

	public final static int MAX_BUFFER = 10000;

	protected IndexSearcher searcher;

	protected Set<String> setStopWords;

	/**
	 * 
	 */
	public ProminentConcepts(String indexDir) throws Exception {
		setStopWords = parseStopWordString(STOPWORD_STRING);
		open(indexDir);
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

		searcher.setSimilarity(new UnNormalizedLuceneSimilarity());

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

	public void extractProminentTitles(String outputFile) throws Exception {
		BufferedWriter writer = IOManager.openWriter(outputFile);

		IndexReader reader = searcher.getIndexReader();
		Map<String, Integer> mapTitles = new HashMap<String, Integer>();
		int numDocs = reader.numDocs();
		System.out.println("Total number of documents: " + numDocs);
		int count = 0;
		for (int i = 0; i < numDocs; i++) {
			Document doc = reader.document(i);
			String title = doc.get("title");
			int hits = prominentTitle(title);
			if (hits > LOWERBOUND_PROMINENCE) {
				mapTitles.put(title, hits);
				if (mapTitles.size() >= MAX_BUFFER) {
					flushBuffer(mapTitles, writer);
					mapTitles = new HashMap<String, Integer>();
					System.out.println("Flushed " + count + " titles.");
				}
				count ++;
			}
		}
		if (mapTitles.size() > 0) {
			flushBuffer(mapTitles, writer);
			System.out.println("Flushed " + count + " titles.");
		}

		IOManager.closeWriter(writer);
	}

	/**
	 * @param writer
	 */
	private void flushBuffer(Map<String, Integer> mapTitles,
			BufferedWriter writer) throws Exception {
		Set<String> setTitles = mapTitles.keySet();
		for (String title : setTitles) {
			int hits = mapTitles.get(title);
			String outString = title + "\t" + hits + "\n";
			writer.write(outString);
		}
	}

	public int prominentTitle(String title) throws Exception {
		int hits = searchTextForTotalHits(title);
		return hits;
	}

	protected Query makeQueryForProminence(String queryText) throws Exception {

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

	private int searchTextForTotalHits(String queryText) throws Exception {

		if (queryText.replaceAll("\\s+", "").length() == 0)
			return 0;

		Query query = makeQueryForProminence(queryText);

		Hits hits = searcher.search(query);
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

}
