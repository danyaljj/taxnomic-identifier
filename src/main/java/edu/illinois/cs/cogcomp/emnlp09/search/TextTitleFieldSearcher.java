/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneSearcher;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Jan 26, 2009
 */

public class TextTitleFieldSearcher extends SimpleLuceneSearcher {

	public final static int SLOP = 10;
	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";
	public final static int MAX_RESULTS = 100;
	public final static int MIN_NEW_ENTITY_LENGTH = 4;

	protected static final Set<String> REDIRECT_BEGIN_TEXT = new HashSet<String>();
	static {
		REDIRECT_BEGIN_TEXT.add("#REDIRECT");
		REDIRECT_BEGIN_TEXT.add("#redirect");
		REDIRECT_BEGIN_TEXT.add("#Redirect");
	}

	private ExecutionTimeUtil timer = new ExecutionTimeUtil();

	protected Set<String> setStopWords;

	private Set<String> setNewTitles = null;

	protected String mustNotQuery = "";

	public Map<String, ArrayList<Integer>> mapTitle = new HashMap<String, ArrayList<Integer>>();

	/**
	 * @param fields
	 * @param ngrams
	 */
	public TextTitleFieldSearcher(String[] fields, boolean ngrams) {

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

	public void loadTitleMapping(String titleMapping) throws Exception {

		System.out.println("Reading title mapping...");

		BufferedReader reader = IOManager.openReader(titleMapping);

		String line;

		while ((line = reader.readLine()) != null) {
			String chunks[] = line.split("\t");

			if (chunks.length != 2)
				continue;

			if (mapTitle.containsKey(chunks[0])) {
				ArrayList<Integer> arrInt = mapTitle.get(chunks[0]);
				arrInt.add(Integer.parseInt(chunks[1]));
			} else {
				ArrayList<Integer> arrInt = new ArrayList<Integer>();
				arrInt.add(Integer.parseInt(chunks[1]));
				mapTitle.put(chunks[0], arrInt);
			}
		}

		IOManager.closeReader(reader);

		System.out.println("Done.");
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

		searcher.setSimilarity(new DefaultSimilarity());

		timer.end();

		System.out.println("Done with opening index at " + indexDirectory
				+ ". " + timer.getTimeSeconds() + " secs.");

	}

	public ILuceneResult getDocumentResult(int docId) throws Exception {

		Document doc = searcher.doc(docId);

		ILuceneResult result = new SimpleLuceneResult(Integer.toString(docId),
				doc.get("title"), doc.get("text"), doc.get("category"), -1.0,
				docId);

		if (isRedirectPage(result)) {

			String newTitle = getRedirectTitle(result);

			if (newTitle != null) {

				String test = super.formatText(newTitle);

				if (mapTitle.containsKey(test)) {

					ArrayList<Integer> arrInt = mapTitle.get(test);
					for (Integer in : arrInt) {
						Document d = searcher.doc(in.intValue());
						ILuceneResult mappingResult = new SimpleLuceneResult(
								Integer.toString(in), d.get("title"), d
										.get("text"), d.get("category"), -1.0,
								in);
						if (mappingResult != null
								&& super.isValidCategory(mappingResult
										.getCategory())) {
							if (result.getCategory().length() > 0)
								mappingResult.setCategory(mappingResult
										.getCategory()
										+ "|" + result.getCategory());
							return mappingResult;
						}
					}
				}

				else {

					if (test.length() < MIN_NEW_ENTITY_LENGTH) {
						if (result.getCategory().length() > 0)
							return result;
						else
							return null;
					}

					ArrayList<ILuceneResult> arrSubResults = super.search(
							newTitle, MAX_RESULTS);

					if (arrSubResults.size() > 0) {
						String nT = super.formatText(newTitle);
						for (ILuceneResult subResult : arrSubResults) {
							if (nT.equals(subResult.getTitle())
									&& super.isValidCategory(subResult
											.getCategory())) {
								if (result.getCategory().length() > 0)
									subResult.setCategory(subResult
											.getCategory()
											+ "|" + result.getCategory());
								return subResult;
							}
						}
					}
				}

			}
		}

		if (result.getCategory().length() == 0)
			return null;

		return result;
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

		String queries[] = queryText.split("cogcomp");

		if (queries.length != 2) {

			BooleanQuery titleQuery = makeTitleQuery(queries[0]);
			return titleQuery;

		}

		BooleanQuery titleQuery = makeTitleQuery(queries[0]);

		BooleanQuery textQuery = makeTextQuery(queries[1]);

		Query query = Query.mergeBooleanQueries(new Query[] { titleQuery,
				textQuery });

		return query;

	}

	protected BooleanQuery makeTextQuery(String query) throws Exception {

		BooleanQuery bq = makeInitialQuery(query);

		if (addNGrams) {

			bq = addNGrams(query, bq);

		}

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

	protected BooleanQuery makeTitleQuery(String query) {

		Object terms[] = standardizeQuery(query);

		Term termHandler = new Term("title", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			// System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}

		pq.setSlop(SLOP);

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);

		if (mustNotQuery.length() > 0) {

			terms = standardizeQuery(mustNotQuery);

			pq = new PhraseQuery();

			for (Object term : terms) {

				String currentTerm = (String) term;
				Term focusTerm = termHandler.createTerm(currentTerm);
				pq.add(focusTerm);

			}

			bq.add(pq, BooleanClause.Occur.MUST_NOT);

			mustNotQuery = "";

		}

		return bq;
	}

	/**
	 * @param queryText
	 * @return
	 */
	private Object[] standardizeQuery(String queryText) {

		queryText = queryText.replaceAll("\\(.*\\)", " ");

		queryText = super.formatText(queryText);

		String tokens[] = queryText.split("\\s+");

		ArrayList<String> arrTerms = new ArrayList<String>();

		for (String token : tokens) {

			if (setStopWords.contains(token))
				continue;

			arrTerms.add(token);
		}

		return arrTerms.toArray();
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

		ArrayList<ILuceneResult> arrResults = super.search(queryText,
				numResults);

		arrResults = resolvingRedirectPagesNew(arrResults, numResults);

		arrResults = super.refineCategories(arrResults);

		return arrResults;
	}

	public void setMustNotQuery(String mustNotQuery) {

		this.mustNotQuery = mustNotQuery;

	}

	private ArrayList<ILuceneResult> resolvingRedirectPagesNew(
			ArrayList<ILuceneResult> arrResults, int numResults)
			throws Exception {

		// System.out.println("Resolving Redirect pages.");
		ArrayList<ILuceneResult> arrResolvedResults = new ArrayList<ILuceneResult>();

		Set<String> setNewTitles = new HashSet<String>();

		for (ILuceneResult result : arrResults) {

			if (result.getCategory().length() > 0)
				arrResolvedResults.add((SimpleLuceneResult) result);

			if (isRedirectPage(result)) {

				String newTitle = getRedirectTitle(result);

				if (newTitle != null) {

					String test = super.formatText(newTitle);

					if (mapTitle.containsKey(test)) {
						ArrayList<Integer> arrInt = mapTitle.get(test);
						for (Integer in : arrInt) {
							Document d = searcher.doc(in.intValue());
							ILuceneResult mappingResult = new SimpleLuceneResult(
									Integer.toString(in), d.get("title"), d
											.get("text"), d.get("category"), -1.0,
									in);
							if (mappingResult != null
									&& super.isValidCategory(mappingResult
											.getCategory())) {
								arrResolvedResults.add(mappingResult);
							}
						}
					}

					else {

						if (test.length() < MIN_NEW_ENTITY_LENGTH) {
							continue;
						}

						if (setNewTitles.contains(newTitle))
							continue;
						else
							setNewTitles.add(newTitle);

						ArrayList<ILuceneResult> arrSubResults = super.search(
								newTitle, MAX_RESULTS);

						if (arrSubResults.size() > 0) {
							String nT = super.formatText(newTitle);
							for (ILuceneResult subResult : arrSubResults) {
								if (nT.equals(subResult.getTitle())
										&& super.isValidCategory(subResult
												.getCategory())) {
									arrResolvedResults
											.add((SimpleLuceneResult) subResult);
									break;
								}
							}
						}
					}

				}
			}
		}

		this.setNewTitles = setNewTitles;

		return arrResolvedResults;

	}

	/**
	 * @return the setNewTitles
	 */
	public Set<String> getSetNewTitles() {
		return setNewTitles;
	}

	/**
	 * @param result
	 * @return
	 */
	private String getRedirectTitle(ILuceneResult result) {

		String text = result.getDoc();

		int posB = text.indexOf("[[");
		int posE = text.indexOf("]]", posB + 1);

		if (posB == -1 || posE == -1) {
			System.out.println("Unable to locate the redirect title.");
			System.out.println("Title: " + result.getTitle() + ", id: "
					+ result.getId());
			System.out.println("Text: " + text);
			return null;
		}

		String newTitle = text.substring(posB + 2, posE);

		if (newTitle.length() == 0)
			return null;

		return newTitle;
	}

	/**
	 * @param result
	 * @return
	 */
	private boolean isRedirectPage(ILuceneResult result) {

		String title = result.getTitle();

		String text = result.getDoc();

		String subText = text.substring(0, Math.min(title.length() + 50, text
				.length()));

		// Given that the document contents were lowercased before going to this
		// step,
		// we can simply compare the text with "redirect".

		return (subText.indexOf("#redirect") >= 0);

		// We do not need to do the following check!

		// for (String redirect : REDIRECT_BEGIN_TEXT) {
		//
		// if (text.startsWith(redirect))
		// return true;
		//
		// }

		// return false;

	}

}
