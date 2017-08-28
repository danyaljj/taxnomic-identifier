/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10.distributioanlmemory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.utils.CosineSimilarity;

/**
 * @author dxquang May 18, 2010
 */
public class StrudelSearcher {

	protected Set<String> setStopWords = null;
	protected IndexSearcher searcher = null;
	protected int numDocs = 0;
	protected int totalHits = 0;

	/**
	 * 
	 */
	public StrudelSearcher() {
		String stopwords = "";
		setStopWords = parseStopWordString(stopwords);
	}

	private Set<String> parseStopWordString(String stopwords) {
		Set<String> setSW = new HashSet<String>();
		String tokens[] = stopwords.split(",+");
		for (String token : tokens)
			setSW.add(token);
		return setSW;
	}

	public void open(String indexDirectory) throws IOException {

		File indexDir = new File(indexDirectory);

		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new IOException(indexDir
					+ "does not exist or is not a directory");
		}
		Directory fsDir = FSDirectory.getDirectory(indexDir);

		searcher = new IndexSearcher(fsDir);
		searcher.setSimilarity(new DefaultSimilarity());

		System.out.println("Opened the index at " + indexDirectory);

		numDocs = searcher.getIndexReader().numDocs();
	}

	public List<ILuceneResult> search(String queryText) throws Exception {

		List<ILuceneResult> results = new ArrayList<ILuceneResult>();

		// queryText = formatText(queryText);

		if (queryText.length() == 0)
			return results;

		Query query = makeQuery(queryText);

		if (query == null)
			System.out.println("NULL!!!");

		TopDocs searchResults = searcher.search(query, null, 10);
		totalHits = searchResults.totalHits;

		for (int i = 0; i < searchResults.scoreDocs.length; i++) {

			Document doc = searcher.doc(searchResults.scoreDocs[i].doc);

			results.add(new SimpleLuceneResult(doc.get("word"), doc
					.get("content"), searchResults.scoreDocs[i].score,
					searchResults.scoreDocs[i].doc));

		}

		return results;
	}

	protected String formatText(String text) {

		text = text.toLowerCase();
		text = text.replaceAll("\\p{Punct}", " ");
		text = text.replaceAll("\\s+", " ");
		text = text.trim();
		return text;

	}

	protected Query makeQuery(String queryText) throws Exception {

		Object terms[] = standardizeQuery(queryText);

		Term termHandler = new Term("word", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			// System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);

		return bq;

	}

	private Object[] standardizeQuery(String queryText) {

		ArrayList<String> arrTerms = new ArrayList<String>();
		arrTerms.add(queryText);

		return arrTerms.toArray();
	}

	/**
	 * @return the totalHits
	 */
	public int getTotalHits() {
		return totalHits;
	}

	public void getSearchResults(String queryText) throws Exception {
		List<ILuceneResult> results = search(queryText);

		for (ILuceneResult res : results) {
			System.out.println(res.getId());
			System.out.println(res.getDoc());
		}

		System.out.println("Total hits: " + getTotalHits());
	}

	public double getSimilarity(String word1, String word2) throws Exception {
		List<ILuceneResult> result1 = search(word1);
		List<ILuceneResult> result2 = search(word2);
		System.out.println("# of results of " + word1 + ": " + result1.size());
		System.out.println("# of results of " + word2 + ": " + result2.size());
		double score = 0.0;
		if (result1.size() == 0)
			if (result2.size() == 0)
				score = -3;
			else
				score = -2;
		else if (result2.size() == 0)
			score = -1;
		else {
			String content1 = result1.get(0).getDoc();
			String[] s1 = content1.split("\\t+");
			String content2 = result2.get(0).getDoc();
			String[] s2 = content2.split("\\t+");
			if (s1.length != 5000 || s2.length != 5000) {
				score = -4;
				System.out.println("# of vector's dimension != 5000");
			} else {
				Vector<Double> v1 = new Vector<Double>();
				for (String s : s1) {
					v1.add(Double.parseDouble(s));
				}
				Vector<Double> v2 = new Vector<Double>();
				for (String s : s2) {
					v2.add(Double.parseDouble(s));
				}
				score = CosineSimilarity.getSimilarity(v1, v2);
			}
		}

		return score;
	}

	public double getBestSimilarity(String word1, String word2)
			throws Exception {

		List<String> words1 = new Vector<String>();
		if (!word1.endsWith("-n") && !word1.endsWith("-v")
				&& !word1.endsWith("-j")) {
			words1.add(word1 + "-n");
			words1.add(word1 + "-v");
			words1.add(word1 + "-j");
		} else {
			words1.add(word1);
		}

		List<String> words2 = new Vector<String>();
		if (!word2.endsWith("-n") && !word2.endsWith("-v")
				&& !word2.endsWith("-j")) {
			words2.add(word2 + "-n");
			words2.add(word2 + "-v");
			words2.add(word2 + "-j");
		} else {
			words2.add(word2);
		}

		List<Vector<Double>> vectors1 = new ArrayList<Vector<Double>>();
		for (String w : words1) {
			List<ILuceneResult> result = search(w);
			if (result.size() > 0) {
				Vector<Double> v = new Vector<Double>();
				String content = result.get(0).getDoc();
				String[] ss = content.split("\\t+");
				for (String s : ss) {
					v.add(Double.parseDouble(s));
				}
				vectors1.add(v);
			}
		}

		List<Vector<Double>> vectors2 = new ArrayList<Vector<Double>>();
		for (String w : words2) {
			List<ILuceneResult> result = search(w);
			if (result.size() > 0) {
				Vector<Double> v = new Vector<Double>();
				String content = result.get(0).getDoc();
				String[] ss = content.split("\\t+");
				for (String s : ss) {
					v.add(Double.parseDouble(s));
				}
				vectors2.add(v);
			}
		}

		double best = -100.0;
		for (Vector<Double> v1 : vectors1) {
			for (Vector<Double> v2 : vectors2) {
				double scr = CosineSimilarity.getSimilarity(v1, v2);
				if (scr > best) {
					best = scr;
				}
			}
		}

		return best;
	}

	public void close() throws IOException {
		searcher.close();

	}

}
