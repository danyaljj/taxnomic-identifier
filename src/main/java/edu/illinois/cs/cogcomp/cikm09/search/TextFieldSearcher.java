/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.indexer.UnNormalizedLuceneSimilarity;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneSearcher;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;

/**
 * @author dxquang Feb 19, 2009
 */
public class TextFieldSearcher extends SimpleLuceneSearcher {

	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	protected Set<String> setStopWords;
	
	public int numDocs = 0;

	// public final static int SLOP = 3;

	private ExecutionTimeUtil timer = new ExecutionTimeUtil();

	/**
	 * 
	 */
	public TextFieldSearcher(String[] fields, boolean ngrams) {
		super(fields, ngrams);

		setStopWords = parseStopWordString(STOPWORD_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.lucenesearch.SimpleLuceneSearcher#open(java.lang.String)
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

		searcher.setSimilarity(new UnNormalizedLuceneSimilarity());

		timer.end();

		System.out.println("Done with opening index at " + indexDirectory
				+ ". " + timer.getTimeSeconds() + " secs.");

		numDocs = searcher.getIndexReader().numDocs();
	}

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
	 * @see cogcomp.lucenesearch.SimpleLuceneSearcher#search(java.lang.String,
	 * int)
	 */
	@Override
	public ArrayList<ILuceneResult> search(String queryText, int numResults)
			throws Exception {

		ArrayList<ILuceneResult> arrResults = super.search(queryText, numResults);
		
		arrResults = super.refineCategories(arrResults);
		
		return arrResults;
		
	}

	protected Query makeQuery(String queryText) throws Exception {

		BooleanQuery bq = makeInitialQuery(queryText);

		if (addNGrams) {

			bq = addNGrams(queryText, bq);

		}

		// System.out.println(bq.toString());

		return bq;
	}

	protected BooleanQuery makeInitialQuery(String queryText)
			throws ParseException {

		queryText.replaceAll("\"", "");
		queryText.replaceAll("\'", "");

		String finalQuery = queryText;
		Query query = null;

		if (fields.length == 1) {
			QueryParser parser = new QueryParser("text", analyzer);
			query = parser.parse(finalQuery);
		} else {

			MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,
					analyzer);
			query = parser.parse(finalQuery);
		}

		// BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
		BooleanQuery bq = new BooleanQuery();

		bq.add(query, BooleanClause.Occur.SHOULD);
		return bq;
	}

	protected BooleanQuery addNGrams(String queryText, BooleanQuery bq) {
		String[] words = queryText.split("\\s+");

		Term termHandler = new Term("text", "");

		// add bigrams
		for (int i = 0; i < words.length - 1; i++) {
			// String str = (" \"" + words[i] + " " + words[i + 1] + "\"");

			PhraseQuery pq = new PhraseQuery();
			pq.add(termHandler.createTerm(words[i]));
			pq.add(termHandler.createTerm(words[i + 1]));

			bq.add(pq, BooleanClause.Occur.SHOULD);
		}

		// add trigrams
		for (int i = 0; i < words.length - 2; i++) {
			// finalQuery += (" \"" + words[i] + " " + words[i + 1] + " " +
			// words[i + 2] + "\"");
			PhraseQuery pq = new PhraseQuery();
			pq.add(termHandler.createTerm(words[i]));
			pq.add(termHandler.createTerm(words[i + 1]));
			pq.add(termHandler.createTerm(words[i + 2]));

			bq.add(pq, BooleanClause.Occur.SHOULD);
		}

		return bq;
	}

	public IndexSearcher getSearcher() {
		return searcher;
	}
	
	public int getTotalHits() {
		return super.getTotalHits();
	}

}
