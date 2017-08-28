/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;

/**
 * @author dxquang Jan 26, 2009
 */

public class TitleSearcher extends SimpleLuceneSearcher {

	public final static int SLOP = 3;
	public final static String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";
	public final static int MAX_RESULTS = 1000;
	public final static int MIN_NEW_ENTITY_LENGTH = 4;

	protected static final Set<String> REDIRECT_BEGIN_TEXT = new HashSet<String>();
	static {
		REDIRECT_BEGIN_TEXT.add("#REDIRECT");
		REDIRECT_BEGIN_TEXT.add("#redirect");
		REDIRECT_BEGIN_TEXT.add("#Redirect");
	}

	private static Log log = LogFactory.getLog(TitleSearcher.class);
	private ExecutionTimeUtil timer = new ExecutionTimeUtil();

	protected Set<String> setStopWords;

	private Set<String> setNewTitles = null;

	private boolean sortByProminence = false;

	public HitCountSearcher hcSearcher = null;

	/**
	 * @param fields
	 * @param ngrams
	 */
	public TitleSearcher(String[] fields, boolean ngrams) {
		super(fields, ngrams);
		setStopWords = parseStopWordString(STOPWORD_STRING);
		sortByProminence = false;
	}

	/**
	 * @param sortByProminence
	 *            the sortByProminence to set
	 */
	public void setSortByProminence(boolean sortByProminence) {
		this.sortByProminence = sortByProminence;
	}

	/**
	 * 
	 */
	public boolean getSortByProminence() {
		return this.sortByProminence;
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

		searcher.setSimilarity(new DefaultSimilarity());

		timer.end();

		log.info("Done opening index at " + indexDirectory + ". Total time"
				+ timer.getTimeMillis());

		try {
			hcSearcher = new HitCountSearcher(indexDirectory);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to initialize the HitCountSearcher.");
			System.exit(1);
		}

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

		Object terms[] = standardizeQuery(queryText);

		Term termHandler = new Term("title", "");

		PhraseQuery pq = new PhraseQuery();

		for (Object term : terms) {

			String currentTerm = (String) term;
			// System.out.print(currentTerm + "\t");
			Term focusTerm = termHandler.createTerm(currentTerm);
			pq.add(focusTerm);

		}
		// System.out.println();

		pq.setSlop(SLOP);

		BooleanQuery bq = new BooleanQuery();
		bq.add(pq, BooleanClause.Occur.MUST);

		return bq;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.lucenesearch.SimpleLuceneSearcher#search(java.lang.String,
	 * int)
	 */
	@Override
	public ArrayList<ILuceneResult> search(String queryText, int numResults)
			throws Exception {

		// System.out.println("Search with Title: >" + queryText + "<");

		int num = (sortByProminence == true) ? MAX_RESULTS : numResults;

		// System.out.println("First search.");
		ArrayList<ILuceneResult> arrResults = super.search(queryText, num);

		// System.out.println("Sort by Prominence. arrResults size: "
		// + arrResults.size());

		if (sortByProminence == true)
			arrResults = sortByProminence(queryText, arrResults, numResults);

		// These lines of code are for fixing the "Redirect" pages.
		arrResults = resolvingRedirectPagesNew(arrResults, num, numResults);

		return arrResults;
	}

	private ArrayList<ILuceneResult> resolvingRedirectPagesNew(
			ArrayList<ILuceneResult> arrResults, int num, int numResults)
			throws Exception {

		// System.out.println("Resolving Redirect pages.");
		Set<SimpleLuceneResult> setNewResults = new HashSet<SimpleLuceneResult>();

		Set<String> setNewTitles = new HashSet<String>();

		for (ILuceneResult result : arrResults) {

			// System.out.println("Title: " + result.getTitle() + ", cat: "
			// + result.getCategory());

			if (result.getCategory().length() > 0)
				setNewResults.add((SimpleLuceneResult) result);

			if (isRedirectPage(result) == true) {

				// System.out.println("*This page is a Redirect page: "
				// + result.getTitle());

				String newTitle = getRedirectTitle(result);

				if (newTitle != null) {

					// System.out.println("\tNew Title: " + newTitle);

					String test = newTitle.replaceAll("\\(.+\\)", " ");
					test = test.replaceAll("\\p{Punct}", " ");
					test = test.replaceAll("\\s+", "");

					if (test.length() < MIN_NEW_ENTITY_LENGTH) {
						// System.out.println("- The title is too short.");
						continue;
					}

					newTitle = newTitle.toLowerCase();
					// newTitle = newTitle.replaceAll("\\p{Punct}", " ");
					// newTitle = newTitle.replaceAll("\\s+", " ");

					if (!setNewTitles.contains(newTitle))
						setNewTitles.add(newTitle);
					else {
						//System.out.println("\tThis new title already exists.")
						// ;
						continue;
					}

					ArrayList<ILuceneResult> arrSubResults = super.search(
							newTitle, num);

					// System.out.println("\tThere are " + arrSubResults.size()
					// + " search results for the new title.");

					// if (sortByProminence == true)
					// arrSubResults = sortByProminence(newTitle,
					// arrSubResults, numResults);

					// System.out.println("\tThere are " + arrSubResults.size()
					// + " search results after sorting.");

					// Taking the first result only.
					for (ILuceneResult subResult : arrSubResults) {
						if (subResult.getCategory().length() > 0) {

							String title = subResult.getTitle();

							// System.out
							// .println("First good sub-title: " + title);

							if (sortByProminence == true) {
								int totalHit = hcSearcher
										.searchTextForTotalHits(title);

								// result
								// .setScore(result.getScore()
								// * Math.log((double) totalHit + 1));

								subResult.setScore(Math
										.log((double) totalHit + 1));
							}

							setNewResults.add((SimpleLuceneResult) subResult);
							break;
						}
					}
				}
			}

		}

		this.setNewTitles = setNewTitles;

		// System.out.println("*** setNewResults.size(): " +
		// setNewResults.size());

		ArrayList<ILuceneResult> arrNewResults = new ArrayList<ILuceneResult>();

		int j = 1;
		for (ILuceneResult result : setNewResults) {

			// System.out.println("(" + j + ") " + result.getTitle() +
			// " - Category: >" + result.getCategory() + "<");

			if (result.getCategory().length() > 0)
				arrNewResults.add(result);

			j++;
		}

		// sortScore(arrNewResults);

		return arrNewResults;

		/*
		 * // System.out.println("\tThere are total " + arrNewResults.size() //
		 * + " new search results.");
		 * 
		 * arrResults = new ArrayList<ILuceneResult>();
		 * 
		 * // int n = Math.min(numResults, arrNewResults.size()); int n =
		 * arrNewResults.size();
		 * 
		 * int i = 0; while (i < n) {
		 * 
		 * ILuceneResult result = arrNewResults.get(i);
		 * 
		 * arrResults.add(result);
		 * 
		 * i++; }
		 * 
		 * return arrResults;
		 */
	}

	/**
	 * @param arrResults
	 * @param num
	 * @param numResults
	 * @return
	 */
	private ArrayList<ILuceneResult> resolvingRedirectPages(
			ArrayList<ILuceneResult> arrResults, int num, int numResults)
			throws Exception {

		// System.out.println("Resolving Redirect pages.");
		Set<SimpleLuceneResult> setNewResults = new HashSet<SimpleLuceneResult>();

		Set<String> setNewTitles = new HashSet<String>();

		for (ILuceneResult result : arrResults) {

			setNewResults.add((SimpleLuceneResult) result);

			if (isRedirectPage(result) == true) {

				// System.out.println("*This page is a Redirect page: "
				// + result.getTitle());

				String newTitle = getRedirectTitle(result);

				if (newTitle != null) {

					// System.out.println("\tNew Title: " + newTitle);

					String test = newTitle.replaceAll("\\(.+\\)", " ");
					test = test.replaceAll("\\p{Punct}", " ");
					test = test.replaceAll("\\s+", "");

					if (test.length() < MIN_NEW_ENTITY_LENGTH)
						continue;

					newTitle = newTitle.toLowerCase();
					newTitle = newTitle.replaceAll("\\p{Punct}", " ");
					newTitle = newTitle.replaceAll("\\s+", " ");
					if (!setNewTitles.contains(newTitle))
						setNewTitles.add(newTitle);
					else {
						//System.out.println("\tThis new title already exists.")
						// ;
						continue;
					}

					ArrayList<ILuceneResult> arrSubResults = super.search(
							newTitle, num);

					// System.out.println("\tThere are " + arrSubResults.size()
					// + " search results for the new title.");

					if (sortByProminence == true)
						arrSubResults = sortByProminence(newTitle,
								arrSubResults, numResults);

					// System.out.println("\tThere are " + arrSubResults.size()
					// + " search results after sorting.");

					for (ILuceneResult subResult : arrSubResults) {
						if (subResult.getCategory().length() > 0)
							setNewResults.add((SimpleLuceneResult) subResult);
					}
				}
			}

		}

		this.setNewTitles = setNewTitles;

		// System.out.println("*** setNewResults.size(): " +
		// setNewResults.size());

		ArrayList<ILuceneResult> arrNewResults = new ArrayList<ILuceneResult>();

		int j = 1;
		for (ILuceneResult result : setNewResults) {

			// System.out.println("(" + j + ") " + result.getTitle() +
			// " - Category: >" + result.getCategory() + "<");

			if (result.getCategory().length() > 0)
				arrNewResults.add(result);
			j++;
		}

		sortScore(arrNewResults);

		// System.out.println("\tThere are total " + arrNewResults.size()
		// + " new search results.");

		arrResults = new ArrayList<ILuceneResult>();

		int n = Math.min(numResults, arrNewResults.size());

		int i = 0;
		while (i < n) {

			ILuceneResult result = arrNewResults.get(i);

			arrResults.add(result);

			i++;
		}

		return arrResults;
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

		String text = result.getDoc();

		for (String redirect : REDIRECT_BEGIN_TEXT) {

			if (text.startsWith(redirect))
				return true;

		}
		return false;
	}

	/**
	 * @param arrResults
	 */
	private ArrayList<ILuceneResult> sortByProminence(String queryText,
			ArrayList<ILuceneResult> arrResults, int numResults)
			throws Exception {

		ArrayList<ILuceneResult> arrNewResults = new ArrayList<ILuceneResult>();

		// Standardizing
		queryText = queryText.toLowerCase();
		queryText = queryText.replaceAll("\\(.*\\)", " ");
		queryText = queryText.replaceAll("\\p{Punct}", " ");

		for (ILuceneResult result : arrResults) {
			String title = result.getTitle();

			if (checkTitle(title, queryText) == false)
				continue;

			// int totalHit = searchTextForTotalHits(title);
			int totalHit = hcSearcher.searchTextForTotalHits(title);

			// result
			// .setScore(result.getScore()
			// * Math.log((double) totalHit + 1));

			result.setScore(Math.log((double) totalHit + 1));

			// if (title.equals("george w bush"))
			// System.out.println("**************************" + totalHit
			// + ", " + Math.log((double) totalHit + 1));

			arrNewResults.add(result);
		}

		sortScore(arrNewResults);

		arrResults = new ArrayList<ILuceneResult>();

		int n = Math.min(numResults, arrNewResults.size());

		int i = 0;
		int count = 0;
		while (count < n && i < arrNewResults.size()) {

			ILuceneResult result = arrNewResults.get(i);

			arrResults.add(result);

			if (result.getCategory().length() > 0)
				count++;

			i++;
		}

		return arrResults;
	}

	private boolean checkTitle(String title, String entity) {
		title = title.replaceAll("\\(.*\\)", " ");
		title = title.toLowerCase();
		title = title.trim();
		String titleTokens[] = title.split("\\s+");
		String entityTokens[] = entity.split("\\s+");
		Set<String> setTitleTokens = new HashSet<String>();

		for (String titleToken : titleTokens)
			setTitleTokens.add(titleToken);

		for (String entityToken : entityTokens) {
			if (!setTitleTokens.contains(entityToken))
				return false;
		}
		return true;
	}

	/**
	 * @param arrMatches
	 */
	private void sortScore(ArrayList<ILuceneResult> arrResults) {
		Collections.sort(arrResults, new Comparator<ILuceneResult>() {
			@Override
			public int compare(ILuceneResult o1, ILuceneResult o2) {
				if (o1.getScore() < o2.getScore())
					return 1;
				else if (o1.getScore() == o2.getScore())
					return 0;
				else
					return -1;
			}
		});
	}

}
