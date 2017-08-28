/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.indexer.ListSearchSimilarity;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;

/**
 * @author dxquang Jan 26, 2009
 */

public class ListSearcher extends SimpleLuceneSearcher {

	public final static int SLOP = 3;
	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	private static Log log = LogFactory.getLog(ListSearcher.class);
	private ExecutionTimeUtil timer = new ExecutionTimeUtil();

	protected Set<String> setStopWords;

	/**
	 * @param fields
	 * @param ngrams
	 */
	public ListSearcher(String[] fields, boolean ngrams) {
		super(fields, ngrams);
		setStopWords = parseStopWordString(STOPWORD_STRING);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cogcomp.lucenesearch.simple.SimpleLuceneSearcher#open(java.lang.String)
	 */
	@Override
	public void open(String indexDirectory) throws IOException {
		timer.start();

		File indexDir = new File(indexDirectory);

		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fsDir = FSDirectory.getDirectory(indexDir);

		searcher = new IndexSearcher(fsDir);

		searcher.setSimilarity(new ListSearchSimilarity());

		timer.end();

		log.info("Done opening index at " + indexDirectory + ". Total time"
				+ timer.getTimeMillis());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cogcomp.lucenesearch.simple.SimpleLuceneSearcher#makeQuery(java.lang.
	 * String)
	 */
	@Override
	protected Query makeQuery(String queryText) throws Exception {

		Object terms[] = standardizeListQuery(queryText);

		Term termHandler = new Term("text", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}
		System.out.println();

		pq.setSlop(SLOP);

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);

		return bq;
	}

	/**
	 * @param queryText
	 * @return
	 */
	private Object[] standardizeListQuery(String queryText) {

		// Lowercase
		queryText = queryText.toLowerCase();

		// Remove punctuation
		queryText = queryText.replaceAll("\\p{Punct}", " ");

		// Remove stop words
		String tokens[] = queryText.split("\\s+");

		ArrayList<String> arrTerms = new ArrayList<String>();
		for (String token : tokens)
			if (!setStopWords.contains(token))
				arrTerms.add(token);

		return arrTerms.toArray();
	}

	public ArrayList<ILuceneResult> search(String entity1, String entity2, int numResult)
			throws Exception {

		ArrayList<ILuceneResult> results = new ArrayList<ILuceneResult>();

		if (entity1.replaceAll("\\s+", "").length() == 0 ||
				entity2.replaceAll("\\s+", "").length() == 0)
			return results;

		// System.out.println("***Searching: " + queryText.trim());

		Query query1 = makeQuery(entity1);
		Query query2 = makeQuery(entity2);
		Query query = Query.mergeBooleanQueries(new Query[] {query1, query2});
		
		TopDocs searchResults = searcher.search(query, null, numResult);
		totalHits = searchResults.totalHits;

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {
			Document doc = searcher.doc(searchResults.scoreDocs[i].doc);

			results.add(new SimpleLuceneResult(Integer
					.toString(searchResults.scoreDocs[i].doc),
					doc.get("title"), doc.get("text"), doc.get("category"),
					searchResults.scoreDocs[i].score));

		}
		
		return results;

	}

}
