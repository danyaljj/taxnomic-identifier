/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.identification;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.common.RelationIdentifierConstants;
import edu.illinois.cs.cogcomp.detection.EntityInfo;
import edu.illinois.cs.cogcomp.emnlp09.search.TextFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TextTitleFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TitleFieldSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
//import edu.illinois.cs.cogcomp.test.AllTests;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.IdfManager;
import edu.illinois.cs.cogcomp.utils.UtilManager;

/**
 * @author dxquang Apr 15, 2009
 */
public class EntityDisambiguation {

	public class TokenScore {

		public String token;
		public double score;

		public TokenScore(String token, double score) {
			this.token = token;
			this.score = score;
		}
	}

	public static final int NUM_PICKED_TOKENS = 10;

	public static final int NUM_RESULT_DISAMB = 100;

	public static final int NUM_MAX_RESULT = 10;

	public static final int NUM_MAX_DOC = 5;

	public static final String STOPWORD_STRING = "i,a,about,an,are,as,at,be,by,com,de,en,for,from,how,in,is,it,la,of,on,or,that,the,this,to,was,what,when,where,who,will,with,und,the,www";

	public TextFieldSearcher textSearcher;

	public TitleFieldSearcher titleSearcher;

	public TextTitleFieldSearcher textTitleSearcher;

	protected Set<String> setStopWords;

	protected IdfManager idfManager = null;

	public ArrayList<ILuceneResult> arrRetrivalEntity1 = new ArrayList<ILuceneResult>();

	public ArrayList<ILuceneResult> arrRetrivalEntity2 = new ArrayList<ILuceneResult>();

	Map<String, ArrayList<Integer>> mapTitle = new HashMap<String, ArrayList<Integer>>();

	public ArrayList<ILuceneResult> arrMappingResult = null;

	/**
	 * 
	 */
	public EntityDisambiguation(String indexDir, String titleMapping) throws Exception {

		String[] fields = new String[] { "text" };

		textSearcher = new TextFieldSearcher(fields, true);

		textSearcher.open(indexDir);

		System.out.println("There are " + textSearcher.numDocs + " docs.");

		fields = new String[] { "title" };

		titleSearcher = new TitleFieldSearcher(fields, false);

		titleSearcher.open(indexDir);

		fields = new String[] { "text" };

		textTitleSearcher = new TextTitleFieldSearcher(fields, false);

		textTitleSearcher.open(indexDir);

		setStopWords = parseStopWordString(STOPWORD_STRING);

		loadTitleMapping(titleMapping);

		titleSearcher.mapTitle = mapTitle;

		textTitleSearcher.mapTitle = mapTitle;

		idfManager = new IdfManager("idf_unnormalized.txt.uniqued");

	}

	public EntityDisambiguation(String indexDir, String titleMapping,
			String idfFile) throws Exception {

		String[] fields = new String[] { "text" };

		textSearcher = new TextFieldSearcher(fields, true);

		textSearcher.open(indexDir);

		System.out.println("There are " + textSearcher.numDocs + " docs.");

		fields = new String[] { "title" };

		titleSearcher = new TitleFieldSearcher(fields, false);

		titleSearcher.open(indexDir);

		fields = new String[] { "text" };

		textTitleSearcher = new TextTitleFieldSearcher(fields, false);

		textTitleSearcher.open(indexDir);

		setStopWords = parseStopWordString(STOPWORD_STRING);

		loadTitleMapping(titleMapping);

		titleSearcher.mapTitle = mapTitle;

		textTitleSearcher.mapTitle = mapTitle;

		idfManager = new IdfManager(idfFile);
	}

	private void initialize() {

		arrRetrivalEntity1 = new ArrayList<ILuceneResult>();

		arrRetrivalEntity2 = new ArrayList<ILuceneResult>();

		arrMappingResult = null;

	}

	private void loadTitleMapping(String titleMapping) throws Exception {

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

	private Set<String> parseStopWordString(String stopwordString) {
		Set<String> setSW = new HashSet<String>();
		String tokens[] = STOPWORD_STRING.split(",+");
		for (String token : tokens)
			setSW.add(token);
		return setSW;
	}

	public void naiveCollectInfo(String entity1, String entity2)
			throws Exception {

		initialize();

		String e1 = UtilManager.formatString(entity1);
		EntityInfo eInfo_1 = new EntityInfo(e1);

		String e2 = UtilManager.formatString(entity2);
		EntityInfo eInfo_2 = new EntityInfo(e2);

		if ( RelationIdentifierConstants.DEBUG ) {
			System.out.println("\nEntity1: " + eInfo_1.entity);
			System.out.println("\nEntity2: " + eInfo_2.entity);
		}

		// System.out.println("\n*Pair: " + eInfo_1.entity + ", " +
		// eInfo_2.entity);

		ArrayList<ILuceneResult> arrTitle1 = titleSearcher.search(
				eInfo_1.entity, NUM_MAX_RESULT);

		// System.out.println("\"" + eInfo_1.entity + "\"");
		// printILuceneResults(arrTitle1);

		for (ILuceneResult res : arrTitle1) {
			arrRetrivalEntity1.add(res);
		}

		ArrayList<ILuceneResult> arrTitle2 = titleSearcher.search(
				eInfo_2.entity, NUM_MAX_RESULT);

		for (ILuceneResult res : arrTitle2) {
			arrRetrivalEntity2.add(res);
		}

		// System.out.println("\"" + eInfo_2.entity + "\"");
		// printILuceneResults(arrTitle2);

	}

	public void collectInfo(String entity1, String entity2) throws Exception {

		initialize();

		String e1 = UtilManager.formatString(entity1);
		EntityInfo eInfo_1 = new EntityInfo(e1);

		String e2 = UtilManager.formatString(entity2);
		EntityInfo eInfo_2 = new EntityInfo(e2);

		if (RelationIdentifierConstants.DEBUG) {
//			System.out.println("\nEntity1: " + eInfo_1.entity);
//			System.out.println("\nEntity2: " + eInfo_2.entity);
		}

		// Text Search for both entities

		String query = eInfo_1.entity + " " + eInfo_2.entity;

		// System.out.println("Text search for both entities. " + query);

		// ArrayList<ILuceneResult> results12 = searchText4Titles(query,
		// NUM_RESULT_DISAMB);
		ArrayList<ILuceneResult> results12 = searchText4Titles(eInfo_1.entity,
				eInfo_2.entity, NUM_RESULT_DISAMB);

		Set<String> setCommonTitles = new HashSet<String>();

		// Gather all categories

		// System.out.println("Gather all categories...");

		for (ILuceneResult result : results12) {

			// We *don't* use the titles to disambiguate
			// If we wish to use the titles, uncomment the following line of
			// code.
			// setCommonTitles.add(result.getTitle());

			// We use the category to pick the top relevant vocabolaries.
			String cat = result.getCategory();
			cat = cat.replaceAll("\\|", " ");
			setCommonTitles.add(cat);
			// System.out.println(" - " + result.getTitle());
		}

		// Get term frequency for category tokens

		// System.out.println("Get term frequency for category tokens.");

		Map<String, Integer> histogram = getHistogram(setCommonTitles);

		// Score the tokens using tf*idf

		// System.out.println("Score the tokens suing tf*idf");

		ArrayList<TokenScore> arrTokenScore = scoringToken(histogram);

		// for (TokenScore tokenScore : arrTokenScore) {
		// System.out.println(tokenScore.token + ": " + tokenScore.score);
		// }

		// Pick top tokens

		String topString = pickTopTokens(eInfo_1.entity, eInfo_2.entity,
				arrTokenScore, NUM_PICKED_TOKENS);

		// System.out.println("Pick top tokens. " + topString);

		// Title + Text search for entity1 and top tokens

		query = eInfo_1.entity + "cogcomp" + topString;
		textTitleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrResults1 = textTitleSearcher.search(query,
				NUM_MAX_DOC);

		// Making result for entity 1.
		Set<Integer> setAddedDocIds = new HashSet<Integer>();
		if (mapTitle.containsKey(e1)) {
			ArrayList<Integer> arrInt = mapTitle.get(e1);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null
						&& !setAddedDocIds
								.contains(mappingResult.getLuceneId())) {
					arrRetrivalEntity1.add(mappingResult);
					setAddedDocIds.add(mappingResult.getLuceneId());
				}
			}
		}
		for (ILuceneResult result : arrResults1) {
			if (!setAddedDocIds.contains(result.getLuceneId())) {
				arrRetrivalEntity1.add(result);
				setAddedDocIds.add(result.getLuceneId());
			}
		}

		// System.out.println("\n* Pair: " + eInfo_1.entity + ", " +
		// eInfo_2.entity);

		// System.out.println("-------Entity 1 disambiguated results.");
		//
		// printILuceneResults(arrRetrivalEntity1);

		// Title + Text search for entity2 and top tokens

		query = eInfo_2.entity + "cogcomp" + topString;
		textTitleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrResults2 = textTitleSearcher.search(query,
				NUM_MAX_DOC);

		// Making result for entity 2.
		setAddedDocIds = new HashSet<Integer>();
		if (mapTitle.containsKey(e2)) {
			ArrayList<Integer> arrInt = mapTitle.get(e2);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null
						&& !setAddedDocIds
								.contains(mappingResult.getLuceneId())) {
					arrRetrivalEntity2.add(mappingResult);
					setAddedDocIds.add(mappingResult.getLuceneId());
				}
			}
		}
		for (ILuceneResult result : arrResults2) {
			if (!setAddedDocIds.contains(result.getLuceneId())) {
				arrRetrivalEntity2.add(result);
				setAddedDocIds.add(result.getLuceneId());
			}
		}

		// System.out.println("-------Entity 2 disambiguated results.");
		//
		// printILuceneResults(arrRetrivalEntity2);

	}

	public void collectInfoSepSearch(String entity1, String entity2)
			throws Exception {

		initialize();

		String e1 = UtilManager.formatString(entity1);
		EntityInfo eInfo_1 = new EntityInfo(e1);

		String e2 = UtilManager.formatString(entity2);
		EntityInfo eInfo_2 = new EntityInfo(e2);

		if (RelationIdentifierConstants.DEBUG) {
			System.out.println("\nEntity1: " + eInfo_1.entity);
			System.out.println("\nEntity2: " + eInfo_2.entity);
		}

		// Text Search for both entities

		String query = eInfo_1.entity + " " + eInfo_2.entity;

		// System.out.println("Text search for both entities. " + query);

		// ArrayList<ILuceneResult> results12 = searchText4Titles(query,
		// NUM_RESULT_DISAMB);
		ArrayList<ILuceneResult> results12 = searchText4Titles(eInfo_1.entity,
				eInfo_2.entity, NUM_RESULT_DISAMB);

		Set<String> setCommonTitles = new HashSet<String>();

		// Gather all categories

		// System.out.println("Gather all categories...");

		for (ILuceneResult result : results12) {

			// We *don't* use the titles to disambiguate
			// If we wish to use the titles, uncomment the following line of
			// code.
			// setCommonTitles.add(result.getTitle());

			// We use the category to pick the top relevant vocabolaries.
			String cat = result.getCategory();
			cat = cat.replaceAll("\\|", " ");
			setCommonTitles.add(cat);
			// System.out.println(" - " + result.getTitle());
		}

		// Get term frequency for category tokens

		// System.out.println("Get term frequency for category tokens.");

		Map<String, Integer> histogram = getHistogram(setCommonTitles);

		// Score the tokens using tf*idf

		// System.out.println("Score the tokens suing tf*idf");

		ArrayList<TokenScore> arrTokenScore = scoringToken(histogram);

		// for (TokenScore tokenScore : arrTokenScore) {
		// System.out.println(tokenScore.token + ": " + tokenScore.score);
		// }

		// Pick top tokens

		String topString = pickTopTokens(eInfo_1.entity, eInfo_2.entity,
				arrTokenScore, NUM_PICKED_TOKENS);

		// System.out.println("Pick top tokens. " + topString);

		// Title + Text search for entity1 and top tokens

		query = eInfo_1.entity + "cogcomp" + topString;
		textTitleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrResults1 = textTitleSearcher.search(query,
				NUM_MAX_DOC);

		// Making result for entity 1.
		Set<Integer> setAddedDocIds = new HashSet<Integer>();
		if (mapTitle.containsKey(e1)) {
			ArrayList<Integer> arrInt = mapTitle.get(e1);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null
						&& !setAddedDocIds
								.contains(mappingResult.getLuceneId())) {
					arrRetrivalEntity1.add(mappingResult);
					setAddedDocIds.add(mappingResult.getLuceneId());
				}
			}
		}
		for (ILuceneResult result : arrResults1) {
			if (!setAddedDocIds.contains(result.getLuceneId())) {
				arrRetrivalEntity1.add(result);
				setAddedDocIds.add(result.getLuceneId());
			}
		}

		// System.out.println("\n* Pair: " + eInfo_1.entity + ", " +
		// eInfo_2.entity);

		// System.out.println("-------Entity 1 disambiguated results.");

		// printILuceneResults(arrRetrivalEntity1);

		// Title + Text search for entity2 and top tokens

		query = eInfo_2.entity + "cogcomp" + topString;
		textTitleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrResults2 = textTitleSearcher.search(query,
				NUM_MAX_DOC);

		// Making result for entity 2.
		setAddedDocIds = new HashSet<Integer>();
		if (mapTitle.containsKey(e2)) {
			ArrayList<Integer> arrInt = mapTitle.get(e2);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null
						&& !setAddedDocIds
								.contains(mappingResult.getLuceneId())) {
					arrRetrivalEntity2.add(mappingResult);
					setAddedDocIds.add(mappingResult.getLuceneId());
				}
			}
		}
		for (ILuceneResult result : arrResults2) {
			if (!setAddedDocIds.contains(result.getLuceneId())) {
				arrRetrivalEntity2.add(result);
				setAddedDocIds.add(result.getLuceneId());
			}
		}

		// System.out.println("-------Entity 2 disambiguated results.");

		// printILuceneResults(arrRetrivalEntity2);

	}

	public void collectInfoDisambiguation(String entity1, String entity2)
			throws Exception {

		initialize();

		String e1 = UtilManager.formatString(entity1);
		EntityInfo eInfo_1 = new EntityInfo(e1);

		String e2 = UtilManager.formatString(entity2);
		EntityInfo eInfo_2 = new EntityInfo(e2);

		// Text Search for both entities

		String query = eInfo_1.entity + " " + eInfo_2.entity;

		// System.out.println("Text search for both entities. " + query);

		ArrayList<ILuceneResult> results12 = searchText4Titles(query,
				NUM_RESULT_DISAMB);

		Set<String> setCommonTitles = new HashSet<String>();

		// Gather all categories

		// System.out.println("Gather all categories.");

		for (ILuceneResult result : results12) {

			// We *don't* use the titles to disambiguate
			// If we wish to use the titles, uncomment the following line of
			// code.
			// setCommonTitles.add(result.getTitle());

			// We use the category to pick the top relevant vocabolaries.
			String cat = result.getCategory();
			cat = cat.replaceAll("\\|", " ");
			setCommonTitles.add(cat);
		}

		// Get term frequency for category tokens

		// System.out.println("Get term frequency for category tokens.");

		Map<String, Integer> histogram = getHistogram(setCommonTitles);

		// Score the tokens using tf*idf

		// System.out.println("Score the tokens suing tf*idf");

		ArrayList<TokenScore> arrTokenScore = scoringToken(histogram);

		// for (TokenScore tokenScore : arrTokenScore) {
		// System.out.println(tokenScore.token + ": " + tokenScore.score);
		// }

		// Pick top tokens

		String topString = pickTopTokens(eInfo_1.entity, eInfo_2.entity,
				arrTokenScore, NUM_PICKED_TOKENS);

		// System.out.println("Pick top tokens. " + topString);

		// Text search for entity1 + top tokens

		query = eInfo_1.entity + " " + topString;

		// System.out.println("Text search for entity1 + top tokens. " + query);

		ArrayList<ILuceneResult> arrResults1 = searchText4Titles(query,
				NUM_RESULT_DISAMB);
		Set<Integer> setDocIds1 = getSetDocIds(arrResults1);

		// Text search for entity2 + top tokens

		query = eInfo_2.entity + " " + topString;

		// System.out.println("Text search for entity2 + top tokens. " + query);

		ArrayList<ILuceneResult> arrResults2 = searchText4Titles(query,
				NUM_RESULT_DISAMB);
		Set<Integer> setDocIds2 = getSetDocIds(arrResults2);

		// Title search for entity1

		query = eInfo_1.entity;

		// System.out.println("Title search for entity " + query);

		titleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrTitle1 = titleSearcher.search(query,
				NUM_RESULT_DISAMB);
		ArrayList<Integer> arrDocIds1 = getArrayDocIds(arrTitle1);
		// System.out.println("- Results for " + eInfo_1.entity);
		// printILuceneResults(arrTitle1);

		// Disambiguate entity1
		arrMappingResult = new ArrayList<ILuceneResult>();
		if (mapTitle.containsKey(e1)) {
			ArrayList<Integer> arrInt = mapTitle.get(e1);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null) {
					arrRetrivalEntity1.add(mappingResult);
					arrMappingResult.add(mappingResult);
				}
			}
		}
		arrRetrivalEntity1.addAll(disambTitles(arrTitle1, arrDocIds1,
				setDocIds1));

		System.out.println("- Disambiguated results for " + eInfo_1.entity);

		printILuceneResults(arrRetrivalEntity1);

		// Title search for entity2

		query = eInfo_2.entity;

		// System.out.println("Title search for entity2 " + query);

		titleSearcher.setMustNotQuery("category");
		ArrayList<ILuceneResult> arrTitle2 = titleSearcher.search(query,
				NUM_RESULT_DISAMB);
		ArrayList<Integer> arrDocIds2 = getArrayDocIds(arrTitle2);
		// System.out.println("- Results for " + eInfo_2.entity);
		// printILuceneResults(arrTitle2);

		// Disambiguate entity2
		arrMappingResult = new ArrayList<ILuceneResult>();
		if (mapTitle.containsKey(e2)) {
			ArrayList<Integer> arrInt = mapTitle.get(e2);
			for (Integer in : arrInt) {
				ILuceneResult mappingResult = getDocumentResult(in);
				if (mappingResult != null) {
					arrRetrivalEntity2.add(mappingResult);
					arrMappingResult.add(mappingResult);
				}
			}
		}
		arrRetrivalEntity2.addAll(disambTitles(arrTitle2, arrDocIds2,
				setDocIds2));

		System.out.println("- Disambiguated results for " + eInfo_2.entity);

		printILuceneResults(arrRetrivalEntity2);

	}

	public ArrayList<ILuceneResult> titleSearch(String query, int topK)
			throws Exception {

		return titleSearcher.search(query, topK);

	}

	public ILuceneResult getDocumentResult(int docId) throws Exception {

		return titleSearcher.getDocumentResult(docId);

	}

	/**
	 * @param arrDocIds1
	 * @param setDocIds1
	 * @return
	 */
	private ArrayList<ILuceneResult> disambTitles(
			ArrayList<ILuceneResult> arrTitles, ArrayList<Integer> arrDocIds,
			Set<Integer> setDocIds) {

		ArrayList<ILuceneResult> arrDisTitles = new ArrayList<ILuceneResult>();
		Set<Integer> setAddedIds = new HashSet<Integer>();
		for (ILuceneResult result : arrMappingResult)
			setAddedIds.add(result.getLuceneId());

		int n = arrDocIds.size();
		int c = 0;

		for (int i = 0; i < n; i++) {

			Integer docId = arrDocIds.get(i);

			if (setDocIds.contains(docId) && !setAddedIds.contains(docId)) {
				arrDisTitles.add(arrTitles.get(i));
				setAddedIds.add(docId);
				c++;
				if (c >= NUM_MAX_RESULT)
					return arrDisTitles;
			}

		}

		return arrDisTitles;
	}

	/**
	 * @param arrResults1
	 * @return
	 */
	private ArrayList<Integer> getArrayDocIds(
			ArrayList<ILuceneResult> arrResults1) {

		ArrayList<Integer> arrDocIds = new ArrayList<Integer>();

		for (ILuceneResult result : arrResults1) {
			arrDocIds.add(result.getLuceneId());
		}

		return arrDocIds;
	}

	/**
	 * @param arrResults1
	 * @return
	 */
	private Set<Integer> getSetDocIds(ArrayList<ILuceneResult> arrResults1) {

		Set<Integer> setDocIds = new HashSet<Integer>();

		for (ILuceneResult result : arrResults1) {
			setDocIds.add(result.getLuceneId());
		}

		return setDocIds;
	}

	private void printILuceneResults(ArrayList<ILuceneResult> arrResults) {
		int i = 1;
		for (ILuceneResult result : arrResults) {
			System.out.println("[" + i + "] " + result.getTitle() + " ("
					+ result.getLuceneId() + "), Category: ["
					+ result.getCategory() + "]");
			i++;
		}
	}

	/**
	 * @param arrTitle1
	 * @param arrResults1
	 * @return
	 */
	private ArrayList<ILuceneResult> disambTitles(
			ArrayList<ILuceneResult> arrTitles,
			ArrayList<ILuceneResult> arrResults) {
		ArrayList<ILuceneResult> arrDisambTitles = new ArrayList<ILuceneResult>();
		for (ILuceneResult title : arrTitles) {
			for (ILuceneResult result : arrResults) {
				if (title.getTitle().equals(result.getTitle())) {
					arrDisambTitles.add(title);
					break;
				}
			}
		}
		return arrDisambTitles;
	}

	/**
	 * @param arrTokenScore
	 * @param numPickedTokens
	 * @return
	 */
	private String pickTopTokens(String entity1, String entity2,
			ArrayList<TokenScore> arrTokenScore, int numPickedTokens) {

		Set<String> tokenSet1 = makeTokenSet(entity1);
		Set<String> tokenSet2 = makeTokenSet(entity2);

		int numTokens = Math.min(numPickedTokens, arrTokenScore.size());

		String outString = "";
		int count = 0;
		int i = 0;
		while (count < numTokens && i < arrTokenScore.size()) {
			String topToken = arrTokenScore.get(i).token;
			if (!tokenSet1.contains(topToken) && !tokenSet2.contains(topToken)) {
				outString += " " + topToken;
				count++;
			}
			i++;
		}

		return outString.trim();
	}

	private Set<String> makeTokenSet(String entity) {
		String tokens[] = entity.split("\\s+");
		Set<String> tokenSet = new HashSet<String>();
		for (String token : tokens) {
			tokenSet.add(token);
		}
		return tokenSet;
	}

	/**
	 * 
	 * @param histogram
	 * @return
	 */
	private ArrayList<TokenScore> scoringToken(Map<String, Integer> histogram) {

		ArrayList<TokenScore> arrTokenScore = new ArrayList<TokenScore>();
		Set<String> keySet = histogram.keySet();
		for (String key : keySet) {
			double idf = idfManager.getIdf(key);
			Integer tf = histogram.get(key);
			TokenScore tokenScore = new TokenScore(key, idf * tf.intValue());
			arrTokenScore.add(tokenScore);
		}

		sortScore(arrTokenScore);

		return arrTokenScore;
	}

	private void sortScore(ArrayList<TokenScore> arrTokenScore) {
		Collections.sort(arrTokenScore, new Comparator<TokenScore>() {
			@Override
			public int compare(TokenScore o1, TokenScore o2) {
				if (o1.score < o2.score)
					return 1;
				else if (o1.score == o2.score)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param histogram
	 */
	private void printHistogram(Map<String, Integer> histogram) {
		Set<String> keySet = histogram.keySet();
		for (String key : keySet) {
			System.out.println(key + ": " + histogram.get(key));
		}
	}

	/**
	 * @param setCommonTitles
	 * @return
	 */
	private Map<String, Integer> getHistogram(Set<String> setCommonTitles) {

		Map<String, Integer> histogram = new HashMap<String, Integer>();

		for (String title : setCommonTitles) {

			title = UtilManager.formatString(title);
			String tokens[] = title.split("\\s+");

			for (String token : tokens) {

				if (setStopWords.contains(token))
					continue;

				if (histogram.containsKey(token)) {
					Integer count = histogram.get(token);
					count++;
					histogram.put(token, count);
				} else {
					Integer count = new Integer(1);
					histogram.put(token, count);
				}
			}

		}

		return histogram;
	}

	private ArrayList<ILuceneResult> disambiguate(
			ArrayList<ILuceneResult> arrResults, Set<String> setCommonTitles,
			String entity) {

		ArrayList<ILuceneResult> arrOutputs = new ArrayList<ILuceneResult>();

		int i = 0;
		Set<Integer> setIndex = new HashSet<Integer>();

		for (ILuceneResult result : arrResults) {
			String title = result.getTitle();
			if (setCommonTitles.contains(title)) {
				arrOutputs.add(result);
				setIndex.add(new Integer(i));
			}

			i++;
		}

		if (arrOutputs.size() == 0) {
			int numResult = NUM_RESULT_DISAMB / 10;
			for (i = 0; i < numResult; i++) {
				ILuceneResult result = arrResults.get(i);
				arrOutputs.add(result);
			}
		}

		return cleanTitles(arrOutputs, entity);

	}

	private ArrayList<ILuceneResult> cleanTitles(
			ArrayList<ILuceneResult> arrResults, String entity) {

		ArrayList<ILuceneResult> arrOutputs = new ArrayList<ILuceneResult>();

		String tokens[] = entity.split("\\s+");

		for (ILuceneResult result : arrResults) {

			String title = result.getTitle();
			title = UtilManager.formatString(title);
			String titleTokens[] = title.split("\\s+");
			Set<String> setTitleTokens = new HashSet<String>();
			for (String t : titleTokens)
				setTitleTokens.add(t);
			for (String t : tokens) {
				if (setTitleTokens.contains(t)) {
					arrOutputs.add(result);
					break;
				}

			}
		}

		return arrOutputs;
	}

	public ArrayList<ILuceneResult> searchText4Titles(String query,
			int numResult) throws Exception {

		ArrayList<ILuceneResult> results = textSearcher
				.search(query, numResult);

		// Print the results to stdout

		// System.out.println("- Query: " + query);
		//
		// int i = 1;
		// for (ILuceneResult result : results) {
		// System.out.println("[" + i + "] " + result.getTitle() + " ("
		// + result.getScore() + ")");
		// i++;
		// if (i > numResult)
		// break;
		// }

		return results;
	}

	public ArrayList<ILuceneResult> searchText4Titles(String query1,
			String query2, int numResult) throws Exception {

		ArrayList<ILuceneResult> results = textSearcher.search(query1, query2,
				numResult);

		// Print the results to stdout

		// System.out.println("- Query: " + query);
		//
		// int i = 1;
		// for (ILuceneResult result : results) {
		// System.out.println("[" + i + "] " + result.getTitle() + " ("
		// + result.getScore() + ")");
		// i++;
		// if (i > numResult)
		// break;
		// }

		return results;
	}

	public int getTotalHits(String query) throws Exception {

		textSearcher.search(query, 1);
		return textSearcher.getTotalHits();

	}

}
