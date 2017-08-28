/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.identification;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.parsers.NounGroup;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Apr 24, 2009
 */
public class EntityCategorization {

	public static final int ABSTRACT_LENGTH = 300;

	public EntityDisambiguation disambiguator = null;

	int K = -1;

	Map<String, Integer> mapCategory = new HashMap<String, Integer>();

	public ArrayList<String> arrCategories1 = new ArrayList<String>();
	public ArrayList<String> arrHeads1 = new ArrayList<String>();
	public ArrayList<String> arrDomains1 = new ArrayList<String>();

	public ArrayList<String> arrCategories2 = new ArrayList<String>();
	public ArrayList<String> arrHeads2 = new ArrayList<String>();
	public ArrayList<String> arrDomains2 = new ArrayList<String>();

	public ArrayList<String> arrTitles1 = new ArrayList<String>();
	public ArrayList<String> arrTitles2 = new ArrayList<String>();

	public ArrayList<String> arrAbstracts1 = new ArrayList<String>();
	public ArrayList<String> arrAbstracts2 = new ArrayList<String>();

	// This feature was added for ACL10
	public int[] additionalFeatures1 = new int[3]; // ["born"?, year?, many
	// year?]
	public int[] additionalFeatures2 = new int[3]; // ["born"?, year?, many
	// year?]

	public static final Set<String> INVALID_CATEGORY = new HashSet<String>();
	static {
		INVALID_CATEGORY.add("names");
		INVALID_CATEGORY.add("genealogy");
		INVALID_CATEGORY.add("given names");
		INVALID_CATEGORY.add("surnames");
		INVALID_CATEGORY.add("unisex given names");
		INVALID_CATEGORY.add("first names");
		INVALID_CATEGORY.add("Naming conventions");
		INVALID_CATEGORY.add("english surnames");
		INVALID_CATEGORY.add("english names");
		INVALID_CATEGORY.add("american surnames");
		INVALID_CATEGORY.add("american names");
		INVALID_CATEGORY.add("australian surnames");
		INVALID_CATEGORY.add("australian names");
		INVALID_CATEGORY.add("british isles surnames");
		INVALID_CATEGORY.add("british isles names");
		INVALID_CATEGORY.add("canadian surnames");
		INVALID_CATEGORY.add("canadian names");
		INVALID_CATEGORY.add("germanic surnames");
		INVALID_CATEGORY.add("germanic names");

		INVALID_CATEGORY.add("wikipedia categories in need of attention");
		INVALID_CATEGORY.add("wikipedia category cleanup categories");
	}

	public static final Set<String> INVALID_CATEGORY_HEAD = new HashSet<String>();
	static {
		INVALID_CATEGORY_HEAD.add("name");
		INVALID_CATEGORY_HEAD.add("surname");
		INVALID_CATEGORY_HEAD.add("genealogy");
		INVALID_CATEGORY_HEAD.add("convention");
		INVALID_CATEGORY_HEAD.add("category");
		INVALID_CATEGORY_HEAD.add("person");
		INVALID_CATEGORY_HEAD.add("redirect");
		INVALID_CATEGORY_HEAD.add("birth");
		INVALID_CATEGORY_HEAD.add("family");
		INVALID_CATEGORY_HEAD.add("history");
		INVALID_CATEGORY_HEAD.add("abstract");
		INVALID_CATEGORY_HEAD.add("form");
	}

	/**
	 * 
	 */
	public EntityCategorization(String indexDir, String categoryMapping, String titleMapping, int K) throws Exception {

		disambiguator = new EntityDisambiguation(indexDir, titleMapping);

		this.K = K;

		System.out.println("K=" + this.K);

		loadCategoryMapping(categoryMapping);

	}

	public EntityCategorization(String indexDir, String categoryMapping,
			String titleMapping, String idfFile, int K) throws Exception {

		disambiguator = new EntityDisambiguation(indexDir, titleMapping, idfFile);

		this.K = K;

		System.out.println("K=" + this.K);

		loadCategoryMapping(categoryMapping);

	}

	private void initialize() {
		arrCategories1 = new ArrayList<String>();
		arrHeads1 = new ArrayList<String>();
		arrDomains1 = new ArrayList<String>();

		arrCategories2 = new ArrayList<String>();
		arrHeads2 = new ArrayList<String>();
		arrDomains2 = new ArrayList<String>();

		arrTitles1 = new ArrayList<String>();
		arrTitles2 = new ArrayList<String>();

		arrAbstracts1 = new ArrayList<String>();
		arrAbstracts2 = new ArrayList<String>();
	}

	/**
	 * @param categoryMapping
	 */
	private void loadCategoryMapping(String categoryMapping) throws Exception {
		System.out.println("Reading category mapping...");

		BufferedReader reader = IOManager.openReader(categoryMapping);

		String line;

		while ((line = reader.readLine()) != null) {
			String chunks[] = line.split("\t");

			if (chunks.length != 2)
				continue;

			mapCategory.put(chunks[0], Integer.parseInt(chunks[1]));
		}

		IOManager.closeReader(reader);

		System.out.println("Done.");
	}

	public void categorize(String entity1, String entity2) throws Exception {

		initialize();

		disambiguator.collectInfo(entity1, entity2);

		ArrayList<ILuceneResult> arrRetrieval1 = disambiguator.arrRetrivalEntity1;
		ArrayList<ILuceneResult> arrRetrieval2 = disambiguator.arrRetrivalEntity2;

		// System.out.println("--------");

		ArrayList<String> arrCats1 = getCategories(arrRetrieval1,
				arrAbstracts1, arrTitles1);
		analyzeCategories(arrCats1, arrCategories1, arrHeads1, arrDomains1);

		// System.out.println("\n*** arrCategories1:");
		// printArrayList(arrCategories1, false);
		//
		// System.out.println("*** arrHeads1:");
		// printArrayList(arrHeads1, false);
		//
		// System.out.println("*** arrDomains1:");
		// printArrayList(arrDomains1, false);

		// System.out.println("--------");

		ArrayList<String> arrCats2 = getCategories(arrRetrieval2,
				arrAbstracts2, arrTitles2);
		analyzeCategories(arrCats2, arrCategories2, arrHeads2, arrDomains2);

		// System.out.println("\n*** arrCategories2:");
		// printArrayList(arrCategories2, false);
		//
		// System.out.println("*** arrHeads2:");
		// printArrayList(arrHeads2, false);
		//
		// System.out.println("*** arrDomains2:");
		// printArrayList(arrDomains2, false);

	}

	// This function was added for ACL10
	public void categorizeAdditionalFeatures(String entity1, String entity2)
			throws Exception {

		initialize();

		disambiguator.collectInfo(entity1, entity2);

		ArrayList<ILuceneResult> arrRetrieval1 = disambiguator.arrRetrivalEntity1;
		ArrayList<ILuceneResult> arrRetrieval2 = disambiguator.arrRetrivalEntity2;

		// System.out.println("--------");

		ArrayList<String> arrCats1 = getCategoriesAdditionalFeatures(
				arrRetrieval1, arrAbstracts1, arrTitles1, additionalFeatures1);
		analyzeCategoriesAddFeats(arrCats1, arrCategories1, arrHeads1, arrDomains1);

		// System.out.println("\n*** arrCategories1:");
		// printArrayList(arrCategories1, false);
		//
		// System.out.println("*** arrHeads1:");
		// printArrayList(arrHeads1, false);
		//
		// System.out.println("*** arrDomains1:");
		// printArrayList(arrDomains1, false);

		// System.out.println("--------");

		ArrayList<String> arrCats2 = getCategoriesAdditionalFeatures(
				arrRetrieval2, arrAbstracts2, arrTitles2, additionalFeatures2);
		analyzeCategoriesAddFeats(arrCats2, arrCategories2, arrHeads2, arrDomains2);

		// System.out.println("\n*** arrCategories2:");
		// printArrayList(arrCategories2, false);
		//
		// System.out.println("*** arrHeads2:");
		// printArrayList(arrHeads2, false);
		//
		// System.out.println("*** arrDomains2:");
		// printArrayList(arrDomains2, false);

	}

	public void categorizeSepSearch(String entity1, String entity2)
			throws Exception {

		initialize();

		disambiguator.naiveCollectInfo(entity1, entity2);

		ArrayList<ILuceneResult> arrRetrieval1 = disambiguator.arrRetrivalEntity1;
		ArrayList<ILuceneResult> arrRetrieval2 = disambiguator.arrRetrivalEntity2;

		// System.out.println("--------");

		ArrayList<String> arrCats1 = getCategories(arrRetrieval1,
				arrAbstracts1, arrTitles1);
		analyzeCategories(arrCats1, arrCategories1, arrHeads1, arrDomains1);

		// System.out.println("\n*** arrCategories1:");
		// printArrayList(arrCategories1, false);
		//
		// System.out.println("*** arrHeads1:");
		// printArrayList(arrHeads1, false);
		//
		// System.out.println("*** arrDomains1:");
		// printArrayList(arrDomains1, false);

		// System.out.println("--------");

		ArrayList<String> arrCats2 = getCategories(arrRetrieval2,
				arrAbstracts2, arrTitles2);
		analyzeCategories(arrCats2, arrCategories2, arrHeads2, arrDomains2);

		// System.out.println("\n*** arrCategories2:");
		// printArrayList(arrCategories2, false);
		//
		// System.out.println("*** arrHeads2:");
		// printArrayList(arrHeads2, false);
		//
		// System.out.println("*** arrDomains2:");
		// printArrayList(arrDomains2, false);

	}

	public void categorizeNaiveDisamb(String entity1, String entity2)
			throws Exception {

		initialize();

		disambiguator.naiveCollectInfo(entity1, entity2);

		ArrayList<ILuceneResult> arrRetrieval1 = disambiguator.arrRetrivalEntity1;
		ArrayList<ILuceneResult> arrRetrieval2 = disambiguator.arrRetrivalEntity2;

		// System.out.println("--------");

		ArrayList<String> arrCats1 = getCategories(arrRetrieval1,
				arrAbstracts1, arrTitles1);
		analyzeCategories(arrCats1, arrCategories1, arrHeads1, arrDomains1);

		// System.out.println("\n*** arrCategories1:");
		// printArrayList(arrCategories1, false);
		//
		// System.out.println("*** arrHeads1:");
		// printArrayList(arrHeads1, false);
		//
		// System.out.println("*** arrDomains1:");
		// printArrayList(arrDomains1, false);

		// System.out.println("--------");

		ArrayList<String> arrCats2 = getCategories(arrRetrieval2,
				arrAbstracts2, arrTitles2);
		analyzeCategories(arrCats2, arrCategories2, arrHeads2, arrDomains2);

		// System.out.println("\n*** arrCategories2:");
		// printArrayList(arrCategories2, false);
		//
		// System.out.println("*** arrHeads2:");
		// printArrayList(arrHeads2, false);
		//
		// System.out.println("*** arrDomains2:");
		// printArrayList(arrDomains2, false);

	}

	public void printArrayList(ArrayList<String> arrStrings, boolean multiLine) {

		for (String s : arrStrings) {
			if (multiLine) {
				System.out.println(s);
			} else {
				System.out.print(s + "|");
			}
		}
		System.out.println();

	}

	/**
	 * @param arrCategories12
	 */
	private void analyzeCategories(ArrayList<String> arrCats,
			ArrayList<String> arrCategories, ArrayList<String> arrHeads,
			ArrayList<String> arrDomains) {

		for (String cat : arrCats) {

			NounGroup nounGroup = new NounGroup(cat);

			// We do not take the plural of the head noun into account.

			boolean plural = nounGroup.plural();

			if (plural || !plural) {

				String head = nounGroup.head();

				if (INVALID_CATEGORY_HEAD.contains(head))
					continue;

				arrCategories.add(cat);

				String preMod = nounGroup.preModifier();
				NounGroup postMod = nounGroup.postModifier();

				arrHeads.add(head);

				if (preMod != null)
					head = preMod.replace('_', ' ') + " " + head;

				arrHeads.add(head);

				if (postMod != null) {

					String postHead = postMod.head();
					String postPreMod = postMod.preModifier();

					arrDomains.add(postHead);
					if (postPreMod != null)
						postHead = postPreMod.replace('_', ' ') + " "
								+ postHead;

					arrDomains.add(postHead);

				}

			}
		}

	}

	private void analyzeCategoriesAddFeats(ArrayList<String> arrCats,
			ArrayList<String> arrCategories, ArrayList<String> arrHeads,
			ArrayList<String> arrDomains) {

		for (String cat : arrCats) {

			NounGroup nounGroup = new NounGroup(cat);

			// We do not take the plural of the head noun into account.
            boolean plural = nounGroup.plural();
				String head = nounGroup.head();

				if (INVALID_CATEGORY_HEAD.contains(head))
					continue;

				arrCategories.add(cat);

				String preMod = nounGroup.preModifier();
				NounGroup postMod = nounGroup.postModifier();
				String preposition = nounGroup.preposition();

				if (plural && preMod != null && postMod != null) {
					arrCategories.add((preMod.replace('_', ' ') + " " + head
							+ " " + preposition + " " + postMod).replaceAll(
							"\\s+", " "));
				} else if (plural && preMod != null) {
					arrCategories.add((preMod.replace('_', ' ') + " " + head));
				}

				arrHeads.add(head);

				if (preMod != null)
					head = preMod.replace('_', ' ') + " " + head;

				arrHeads.add(head);

				if (postMod != null) {

					String postHead = postMod.head();
					String postPreMod = postMod.preModifier();

					arrDomains.add(postHead);
					if (postPreMod != null)
						postHead = postPreMod.replace('_', ' ') + " "
								+ postHead;

					arrDomains.add(postHead);

				}

		}

	}

	/**
	 * @param arrRetrieval1
	 * @return
	 */
	private ArrayList<String> getCategories(
			ArrayList<ILuceneResult> arrRetrieval,
			ArrayList<String> arrAbstracts, ArrayList<String> arrTitles)
			throws Exception {

		ArrayList<String> arrCategories = new ArrayList<String>();

		for (ILuceneResult result : arrRetrieval) {

			// System.out.println("Title: " + result.getTitle());
			// System.out.println("Category: [" + result.getCategory() + "]");

			// Extract the categories

			ArrayList<String> arrCats = extractCategory(result);

			arrCategories.addAll(arrCats);

			// Extract the title of the article

			String title = result.getTitle();
			arrTitles.add(title);

			// Extract the abstract of the article

			String content = result.getDoc();
			String abst = content.substring(0, Math.min(content.length(),
					ABSTRACT_LENGTH));
			arrAbstracts.add(abst);

		}

		return arrCategories;
	}

	// This function was added for ACL10
	private ArrayList<String> getCategoriesAdditionalFeatures(
			ArrayList<ILuceneResult> arrRetrieval,
			ArrayList<String> arrAbstracts, ArrayList<String> arrTitles,
			int[] addFeats) throws Exception {

		ArrayList<String> arrCategories = new ArrayList<String>();

		addFeats[0] = 0;
		addFeats[1] = 0;
		addFeats[2] = 0;

		for (ILuceneResult result : arrRetrieval) {

			// System.out.println("Title: " + result.getTitle());
			// System.out.println("Category: [" + result.getCategory() + "]");

			// Extract the categories

			ArrayList<String> arrCats = extractCategory(result);

			arrCategories.addAll(arrCats);

			// Extract the title of the article

			String title = result.getTitle();
			arrTitles.add(title);

			// Extract the abstract of the article

			String content = result.getDoc();
			String abst = content.substring(0, Math.min(content.length(),
					ABSTRACT_LENGTH));
			arrAbstracts.add(abst);

			// First sentence of the abstract
			// System.out.println("Abstract: " + abst);
			String sen = abst;

			// int pos1 = abst.indexOf(". ");
			// if (pos1 == -1)
			// sen = abst.substring(0, (abst.length() > 200 ? 200 : abst
			// .length()));
			// else
			// sen = abst.substring(0, pos1 + 1);
			//
			// System.out.println("Sentence: " + sen);

			if (addFeats[0] == 0)
				if (sen.indexOf("(born ") >= 0 || sen.indexOf(" born ") >= 0
						|| sen.indexOf("(b ") >= 0 || sen.indexOf("(c ") >= 0
						|| sen.indexOf("( b ") >= 0 || sen.indexOf("( c ") >= 0) {
					addFeats[0] = 1;
					// System.out.println("YES -- BORN");
				}

			if (addFeats[1] == 0 || addFeats[2] == 0) {
				Pattern p = Pattern.compile("\\[\\[\\d+]\\]");
				Matcher m = p.matcher(sen);
				int gc = 0;
				while (m.find() == true) {
					gc++;
				}
				if (gc > 0) {
					addFeats[1] = 1;
					// System.out.println("YES -- YEAR");
				}
				if (gc > 1) {
					addFeats[2] = 1;
					// System.out.println("YES -- MANY YEARS");
				}
			}
			// System.out.println();

		}

		return arrCategories;
	}

	/**
	 * @param result
	 * @return
	 */
	private ArrayList<String> extractCategory(ILuceneResult result)
			throws Exception {

		ArrayList<String> arrCats = new ArrayList<String>();

		int level = 0;
		arrCats = extract(result, level);

		return arrCats;
	}

	/**
	 * @param result
	 * @param level
	 * @return
	 */
	private ArrayList<String> extract(ILuceneResult result, int level)
			throws Exception {

		ArrayList<String> arrCats = new ArrayList<String>();

		if (level >= K) {
			return arrCats;
		}

		// System.out.println("[Level: " + level + "] " + "Title: " +
		// result.getTitle());

		String cat = result.getCategory();

		// System.out.println("\tCategory: [" + cat + "]");

		String cats[] = cat.split("\\|");

		for (String c : cats) {

			NounGroup nounGroup = new NounGroup(c);

			if (INVALID_CATEGORY_HEAD.contains(nounGroup.head()))
				continue;

			arrCats.add(c);

			String query = "category:" + c;

			/*
			 * ArrayList<ILuceneResult> arrResults = disambiguator.titleSearch(
			 * query, 10);
			 * 
			 * System.out.println("- Query: " + c + ", arrResults.size()=" +
			 * arrResults.size());
			 * 
			 * ILuceneResult correctResult = getCorrectResult(arrResults,
			 * query);
			 * 
			 * if (correctResult == null) continue;
			 */

			int docId = -1;
			ILuceneResult correctResult = null;

			// System.out.println("\tQuery: " + query);

			if (mapCategory.containsKey(query)) {
				docId = mapCategory.get(query);

				// System.out.println("\tdocId: " + docId);

				correctResult = disambiguator.getDocumentResult(docId);
			} else
				continue;

			if (correctResult == null)
				continue;

			// System.out.println("\t" + correctResult.getTitle());

			// Recursively extract categories.
			level++;
			arrCats.addAll(extract(correctResult, level));
			level--;
		}

		return arrCats;
	}

	public int getNumCat() {
		return arrCategories1.size() + arrCategories2.size();
	}

	/**
	 * @param arrResults
	 * @param query
	 * @return
	 */
	protected ILuceneResult getCorrectResult(
			ArrayList<ILuceneResult> arrResults, String query) {

		for (ILuceneResult result : arrResults) {
			if (result.getTitle().equals(query))
				return result;
		}

		return null;
	}

	/**
	 * @param k
	 *            the k to set
	 */
	public void setK(int k) {
		this.K = k;
	}

}
