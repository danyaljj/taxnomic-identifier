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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.indexer.UnNormalizedLuceneSimilarity;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;

/**
 * 
 * @author dxquang Jan 27, 2009
 */

public class SimpleLuceneSearcher implements ILuceneSearcher {

	protected static final Set<String> USELESS_CATEGORIES = new HashSet<String>();
	static {
		USELESS_CATEGORIES.add("protected redirects");
		USELESS_CATEGORIES.add("redirects from full names");
	}

	protected IndexSearcher searcher;
	protected Analyzer analyzer = new StandardAnalyzer();
	protected boolean verbose;

	protected String[] fields;
	protected boolean addNGrams = true;

	protected int totalHits = -1;

	private static Log log = LogFactory.getLog(SimpleLuceneSearcher.class);
	private ExecutionTimeUtil timer = new ExecutionTimeUtil();

	public SimpleLuceneSearcher(String[] fields, boolean ngrams) {

		verbose = false;

		this.fields = fields;

		this.addNGrams = ngrams;
	}

	/**
	 * Opens the index created by the Lucene indexer
	 */
	public void open(String indexDirectory) throws IOException {
		timer.start();

		File indexDir = new File(indexDirectory);

		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fsDir = FSDirectory.getDirectory(indexDir);

		searcher = new IndexSearcher(fsDir);

		searcher.setSimilarity(new UnNormalizedLuceneSimilarity());

		timer.end();

		log.info("Done opening index at " + indexDirectory + ". Total time"
				+ timer.getTimeMillis());

	}

	/**
	 * Searches the index for the query and returns an ArrayList of results
	 */
	public ArrayList<ILuceneResult> search(String queryText, int numResults)
			throws Exception {

		ArrayList<ILuceneResult> results = new ArrayList<ILuceneResult>();

		queryText = formatText(queryText);

		if (queryText.length() == 0)
			return results;

		Query query = makeQuery(queryText);

		if (query == null)
			System.out.println("NULL!!!");
		
		timer.start();

		TopDocs searchResults = searcher.search(query, null, numResults);
		totalHits = searchResults.totalHits;

		timer.end();
		// System.out.println("Done with retrieving results. " +
		// timer.getTimeSeconds() + " secs.");

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {

			Document doc = searcher.doc(searchResults.scoreDocs[i].doc);

			results.add(new SimpleLuceneResult(Integer
					.toString(searchResults.scoreDocs[i].doc),
					doc.get("title"), doc.get("text"), doc.get("category"),
					searchResults.scoreDocs[i].score, searchResults.scoreDocs[i].doc));

		}

		return results;
	}

	protected boolean isValidCategory(String category) {
		if (category.length() == 0)
			return false;

		for (String uC : USELESS_CATEGORIES) {
			if (category.startsWith(uC))
				return false;
		}
		
		return true;
	}

	protected ArrayList<ILuceneResult> refineCategories(
			ArrayList<ILuceneResult> arrResults) {

		ArrayList<ILuceneResult> arrRefinement = new ArrayList<ILuceneResult>();

		for (ILuceneResult result : arrResults) {
			if (!isValidCategory(result.getCategory()))
				continue;
			arrRefinement.add(result);
		}

		return arrRefinement;
	}

	/**
	 * @return the totalHits
	 */
	public int getTotalHits() {
		return totalHits;
	}

	protected String formatText(String text) {

		text = text.toLowerCase();

		// text = text.replaceAll("[!\"#$%&'*+,-./;<=>?@\\^_`{}~]", " ");
		text = text.replaceAll("\\p{Punct}", " ");

		text = text.replaceAll("\\s+", " ");

		text = text.trim();

		return text;

	}

	/**
	 * @param queryText
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	protected Query makeQuery(String queryText) throws Exception {

		Object terms[] = standardizeQuery(queryText);

		Term termHandler = new Term("title", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);
		
		return bq;

	}

	private Object[] standardizeQuery(String queryText) {

		queryText = queryText.replaceAll("\\(.*\\)", " ");

		queryText = formatText(queryText);

		String tokens[] = queryText.split("\\s+");

		ArrayList<String> arrTerms = new ArrayList<String>();
		
		for (String token : tokens) {
			
			arrTerms.add(token);
		}

		return arrTerms.toArray();
	}

	/**
	 * @param hitDoc
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isResultValid(Document hitDoc) {
		if (hitDoc.get("text").split(" ").length > 5)
			return true;

		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		searcher.close();

	}

	/**
	 * @param verbose
	 */
	public void setVerbosity(boolean verbose) {
		this.verbose = verbose;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.lucenesearch.ILuceneSearcher#extractCategories(int, double)
	 */
	@Override
	public Set<Category> extractCategories(int docId, double score)
			throws Exception {

		String stringCat = searcher.doc(docId).get("category");

		Set<Category> setCategory = new HashSet<Category>();

		if (stringCat.length() == 0)
			return setCategory;

		String cats[] = stringCat.split("\\|");

		for (String cat : cats) {

			Category c = new Category(cat, 0, score);

			c.catParent = "*" + searcher.doc(docId).get("title");

			setCategory.add(c);

		}

		return setCategory;
	}
}
