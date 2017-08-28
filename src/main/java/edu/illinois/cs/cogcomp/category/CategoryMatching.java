/**
 * 
 */
package edu.illinois.cs.cogcomp.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.data.POS;
import edu.illinois.cs.cogcomp.lucenesearch.Category;
import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.HitCountSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.TitleSearcher;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * 
 * @author dxquang Jan 29, 2009
 */

public class CategoryMatching {

	public class TitlesScore {

		public String titles;
		public double score;

		/**
		 * 
		 */
		public TitlesScore(String titles, double score) {
			this.titles = titles;
			this.score = score;
		}

	}

	public static final int MAX_NUMRESULTS = 10;
	public static final int MAX_UPLEVEL = 3;

	public TitleSearcher titleSearcher = null;
	public ILuceneSearcher categorySearcher = null;
	public CategoryExtraction catExtractor = null;
	public HitCountSearcher hcSearcher = null;
	public boolean useProminence = false;
	public boolean useMutualInformation = false;
	public LexicalMatching lexMatcher = null;
	public WordNetManager wnManager = null;

	/**
	 * 
	 */
	public CategoryMatching(String titleIndexDir, String categoryIndexDir,
			boolean useProminence, boolean useMutualInformation)
			throws Exception {

		titleSearcher = new TitleSearcher(null, false);
		titleSearcher.open(titleIndexDir);
		titleSearcher.setSortByProminence(useProminence);

		categorySearcher = new CategorySearcher(null, false);
		categorySearcher.open(categoryIndexDir);

		catExtractor = new CategoryExtraction(categorySearcher);

		hcSearcher = new HitCountSearcher(titleIndexDir);

		this.useProminence = useProminence;
		this.useMutualInformation = useMutualInformation;

		lexMatcher = new LexicalMatching();

		wnManager = new WordNetManager("file_properties.xml");
	}

	public void matchCategoryOld(String entity1, String entity2)
			throws Exception {

		Map<String, Category> mapProminentsTitles1 = new HashMap<String, Category>();
		Map<String, Category> mapProminentsTitles2 = new HashMap<String, Category>();

		Set<Category> setCatEntity1 = extractAllCategories(entity1,
				mapProminentsTitles1);
		Set<Category> setCatEntity2 = extractAllCategories(entity2,
				mapProminentsTitles2);

		// for (Category c : setCatEntity1) {
		// System.out.print(c.category + " | ");
		// }
		// System.out.println();
		//
		// for (Category c : setCatEntity2) {
		// System.out.print(c.category + " | ");
		// }
		// System.out.println();
		//
		// System.out.println("- Entity " + entity1 + " has "
		// + setCatEntity1.size() + " categories.");
		// System.out.println("- Entity " + entity2 + " has "
		// + setCatEntity2.size() + " categories.");

		Set<Category> setMatches = extractMatch(setCatEntity1, setCatEntity2);
		// System.out.println("There are " + setMatches.size() + " matches.");

		Map<String, Category> mapCatEntity1 = getHashMap(setCatEntity1);
		Map<String, Category> mapCatEntity2 = getHashMap(setCatEntity2);

		if (useProminence == true)
			setMatches = changeScoreByProminentConcepts(setMatches,
					mapCatEntity1, mapCatEntity2);

		if (useMutualInformation == true)
			setMatches = changeScoreByMutualInformation(setMatches, entity1,
					entity2, mapCatEntity1, mapCatEntity2);

		if (setMatches.size() > 0) {
			// System.out.println("- There are " + setMatches.size() +
			// " matches.");

			// Sorting
			ArrayList<Category> arrMatches = new ArrayList<Category>(setMatches);
			sortScore(arrMatches);

			int i = 1;
			for (Category c : arrMatches) {
				System.out.print("[" + i + "] " + c.category + " (" + c.score
						+ ")");
				if (c.upLevel == 0)
					System.out.println(" - SIBLING");
				else if (c.upLevel == 1)
					System.out.println(" - PARENT");
				else
					System.out.println(" - CHILD");

				printTraceCategory(entity1, mapCatEntity1, c);
				printTraceCategory(entity2, mapCatEntity2, c);

				i++;
			}
		} else
			System.out.println("NO RELATION");
	}

	public String matchCategory(String entity1, String entity2,
			boolean returnString) throws Exception {

		entity1 = entity1.toLowerCase();
		entity2 = entity2.toLowerCase();

		ArrayList<String> arrMorphs = null;
		if (entity1.indexOf(" ") == -1 && entity1.matches(".*\\d+.*") == false) {
			arrMorphs = wnManager.getMorph(POS.NOUN, entity1);
			if (arrMorphs.size() == 1)
				entity1 = arrMorphs.get(0);
		}
		// System.out.println("entity1: " + entity1);

		if (entity2.indexOf(" ") == -1 && entity2.matches("\\d") == false) {
			arrMorphs = wnManager.getMorph(POS.NOUN, entity2);
			if (arrMorphs.size() == 1)
				entity2 = arrMorphs.get(0);
		}
		// System.out.println("entity2: " + entity2);

		Map<String, Category> mapProminentsTitles1 = new HashMap<String, Category>();
		Map<String, Category> mapProminentsTitles2 = new HashMap<String, Category>();

		Set<Category> setCatEntity1 = extractAllCategories(entity1,
				mapProminentsTitles1);
		Set<String> setRedirectTitles1 = titleSearcher.getSetNewTitles();

		// System.out.println("setRedirectTitles1: " +
		// setRedirectTitles1.size());
		// System.out.println("setCatEntity1: " + setCatEntity1.size());
		// printSetString(setRedirectTitles1);

		Set<Category> setCatEntity2 = extractAllCategories(entity2,
				mapProminentsTitles2);
		Set<String> setRedirectTitles2 = titleSearcher.getSetNewTitles();

		// System.out.println("setRedirectTitles2: " +
		// setRedirectTitles2.size());
		// System.out.println("setCatEntity2: " + setCatEntity2.size());
		// printSetString(setRedirectTitles2);

		// for (Category c : setCatEntity1) {
		// System.out.print(c.category + " | ");
		// }
		// System.out.println();
		//
		// for (Category c : setCatEntity2) {
		// System.out.print(c.category + " | ");
		// }
		// System.out.println();
		//
		// System.out.println("- Entity " + entity1 + " has "
		// + setCatEntity1.size() + " categories.");
		// System.out.println("- Entity " + entity2 + " has "
		// + setCatEntity2.size() + " categories.");

		Set<Category> setGenCatEntity1 = generalizedCategories(setCatEntity1);
		// System.out.println("setGenCatEn1 size: " + setGenCatEntity1.size());
		Set<Category> setGenCatEntity2 = generalizedCategories(setCatEntity2);
		// System.out.println("setGenCatEn2 size: " + setGenCatEntity2.size());

		boolean useLexicalMaching = true; // This should be a parameter of this
		// function

		Set<Category> setMatches = extractMatch(setCatEntity1, setCatEntity2);

		// System.out.println("There are " + setMatches.size() +
		// " setMatches.");

		Map<String, Category> mapCatEntity1 = getHashMap(setCatEntity1);
		Map<String, Category> mapCatEntity2 = getHashMap(setCatEntity2);

		ArrayList<TitlesScore> arrTitlesProScore = new ArrayList<TitlesScore>();

		Map<String, ArrayList<Category>> mapSeparateTitles = separateTitles(
				entity1, entity2, setMatches, mapCatEntity1, mapCatEntity2,
				mapProminentsTitles1, mapProminentsTitles2, arrTitlesProScore,
				setRedirectTitles1, setRedirectTitles2);

		if (useLexicalMaching) {
			Set<Category> setGenMatches = extractGeneralizedMatch(
					setCatEntity1, setCatEntity2, setGenCatEntity1,
					setGenCatEntity2);
			// System.out.println("There are " + setGenMatches.size()
			// + " setGenMatches.");

			separateGeneralizedTitles(entity1, entity2, setGenMatches,
					mapCatEntity1, mapCatEntity2, mapProminentsTitles1,
					mapProminentsTitles2, arrTitlesProScore,
					setRedirectTitles1, setRedirectTitles2, mapSeparateTitles);
		}

		sortTitlesScore(arrTitlesProScore);

		sortSeparateCategories(mapSeparateTitles);

		StringBuffer result = new StringBuffer("");
		if (setMatches.size() > 0) {

			int i = 1;
			for (TitlesScore titlesProScore : arrTitlesProScore) {

				String titles = titlesProScore.titles;

				ArrayList<Category> arrCategories = mapSeparateTitles
						.get(titles);

				sortScore(arrCategories);

				titles = "\"" + titles.replaceAll("_", "\" Vs. \"") + "\"";

				result.append("[" + i + "] " + titles + " ("
						+ titlesProScore.score + ") " + "\n");
				// System.out.println("[" + i + "] " + titles + " ("
				// + titlesProScore.score + "): ");

				int j = 1;

				for (Category c : arrCategories) {

					result.append("\t(" + j + ") " + c.category + " ("
							+ c.score + ")");
					// System.out.print("\t(" + j + ") " + c.category + " ("
					// + c.score + ")");

					if (c.upLevel == 0) {
						result.append(" - SIBLING" + "\n");
						// System.out.println(" - SIBLING");
					} else if (c.upLevel == 1) {
						result.append(" - PARENT" + "\n");
						// System.out.println(" - PARENT");
					} else {
						result.append(" - CHILD" + "\n");
						// System.out.println(" - CHILD");
					}

					StringBuffer trace;

					if (useLexicalMaching == true) {
						// System.out.println("c.matchCat1: " + c.matchCategory1
						// + ", c.matchCat2: " + c.matchCategory2);
						if (c.matchCategory1.length() == 0) {
							trace = printTraceCategory(entity1, mapCatEntity1,
									c);
							result.append(trace);

							trace = printTraceCategory(entity2, mapCatEntity2,
									c);
							result.append(trace);
						} else {
							trace = printTraceCategory(entity1, mapCatEntity1,
									new Category(c.matchCategory1, c.upLevel,
											c.score));
							result.append(trace);

							trace = printTraceCategory(entity2, mapCatEntity2,
									new Category(c.matchCategory2, c.upLevel,
											c.score));
							result.append(trace);

						}

					} else {
						trace = printTraceCategory(entity1, mapCatEntity1, c);
						result.append(trace);

						trace = printTraceCategory(entity2, mapCatEntity2, c);
						result.append(trace);
					}
					j++;
				}
				// result.append("\n");
				// System.out.println();
				i++;
			}
		} else {
			result.append("NO RELATION");
			// System.out.println("NO RELATION");
		}

		if (returnString == true)
			return result.toString();
		else {
			System.out.println(result.toString());
			return "";
		}
	}

	private void printSetString(Set<String> setRedirectTitles) {
		int k = 1;
		for (String title : setRedirectTitles) {
			System.out.println("[" + k + "] " + title);
			k++;
		}
	}

	/**
	 * @param setCatEntity1
	 * @return
	 */
	private Set<Category> generalizedCategories(Set<Category> setCatEntity) {
		Set<Category> setGenCats = new HashSet<Category>();

		for (Category c : setCatEntity) {
			String genC = lexMatcher.generalizeTitle(c.category);
			if (genC.length() > 0) {
				Category cat = new Category(genC, c.upLevel, c.score);
				cat.catParent = c.catParent;
				cat.catOrigin = c.category;
				setGenCats.add(cat);
			}
		}

		return setGenCats;
	}

	/**
	 * @param mapSeparateTitles
	 */
	private void sortSeparateCategories(
			Map<String, ArrayList<Category>> mapSeparateTitles) {

		Set<String> keySet = mapSeparateTitles.keySet();
		for (String key : keySet) {
			ArrayList<Category> arrCategories = mapSeparateTitles.get(key);
			sortScore(arrCategories);
		}
	}

	/**
	 * @param arrTitlesProScore
	 */
	private void sortTitlesScore(ArrayList<TitlesScore> arrTitlesProScore) {
		Collections.sort(arrTitlesProScore, new Comparator<TitlesScore>() {
			@Override
			public int compare(TitlesScore o1, TitlesScore o2) {
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
	 * 
	 * @param entity1
	 * @param entity2
	 * @param setMatches
	 * @param mapCatEntity1
	 * @param mapCatEntity2
	 * @param mapProminentsTitles1
	 * @param mapProminentsTitles2
	 * @param arrTitlesScore
	 * @param setRedirectTitles1
	 * @param setRedirectTitles2
	 * @return
	 * @throws Exception
	 */
	private Map<String, ArrayList<Category>> separateTitles(String entity1,
			String entity2, Set<Category> setMatches,
			Map<String, Category> mapCatEntity1,
			Map<String, Category> mapCatEntity2,
			Map<String, Category> mapProminentsTitles1,
			Map<String, Category> mapProminentsTitles2,
			ArrayList<TitlesScore> arrTitlesScore,
			Set<String> setRedirectTitles1, Set<String> setRedirectTitles2)
			throws Exception {

		Map<String, ArrayList<Category>> mapSeparateTitles = new HashMap<String, ArrayList<Category>>();

		for (Category c : setMatches) {

			// System.out.println("Match: " + c.category);

			String title1 = getCategoryTitle(c, mapCatEntity1);

			String title1prime = title1.toLowerCase();
			title1prime = title1prime.replaceAll("\\p{Punct}", " ");
			title1prime = title1prime.replaceAll("\\s+", " ");

			// System.out.println("Title1prime: " + title1prime);

			if (checkTitle(title1, entity1) == false
					&& !setRedirectTitles1.contains(title1prime))
				continue;

			String title2 = getCategoryTitle(c, mapCatEntity2);

			String title2prime = title2.toLowerCase();
			title2prime = title2prime.replaceAll("\\p{Punct}", " ");
			title2prime = title2prime.replaceAll("\\s+", " ");

			// System.out.println("Title2prime: " + title2prime);

			if (checkTitle(title2, entity2) == false
					&& !setRedirectTitles2.contains(title2prime))
				continue;

			// System.out.println("title1: " + title1 + ", title2: " + title2);

			Category c1 = mapProminentsTitles1.get(title1);
			Category c2 = mapProminentsTitles2.get(title2);

			String keyTitle = title1 + "_" + title2;

			if (mapSeparateTitles.containsKey(keyTitle)) {
				ArrayList<Category> arrCategories = mapSeparateTitles
						.get(keyTitle);

				if (useMutualInformation == false)
					arrCategories.add(c);
				else {
					double pwMI1 = getPointwiseMutualInformation(title1,
							c.category);
					double pwMI2 = getPointwiseMutualInformation(title2,
							c.category);
					double avgPwMI = (pwMI1 + pwMI2) / (double) 2;

					arrCategories.add(new Category(c.category, c.upLevel,
							avgPwMI * c.score));
				}

			} else {
				double proScore = (c1.score + c2.score) / (double) 2;

				ArrayList<Category> arrCategories = new ArrayList<Category>();
				arrCategories.add(c);
				mapSeparateTitles.put(keyTitle, arrCategories);

				TitlesScore titlesScore = new TitlesScore(keyTitle, proScore);
				arrTitlesScore.add(titlesScore);
			}

		}
		return mapSeparateTitles;
	}

	/**
	 * 
	 * @param entity1
	 * @param entity2
	 * @param setGenMatches
	 * @param mapCatEntity1
	 * @param mapCatEntity2
	 * @param mapProminentsTitles1
	 * @param mapProminentsTitles2
	 * @param arrTitlesScore
	 * @param setRedirectTitles1
	 * @param setRedirectTitles2
	 * @param mapSeparateTitles
	 * @return
	 * @throws Exception
	 */
	private Map<String, ArrayList<Category>> separateGeneralizedTitles(
			String entity1, String entity2, Set<Category> setGenMatches,
			Map<String, Category> mapCatEntity1,
			Map<String, Category> mapCatEntity2,
			Map<String, Category> mapProminentsTitles1,
			Map<String, Category> mapProminentsTitles2,
			ArrayList<TitlesScore> arrTitlesScore,
			Set<String> setRedirectTitles1, Set<String> setRedirectTitles2,
			Map<String, ArrayList<Category>> mapSeparateTitles)
			throws Exception {

		for (Category c : setGenMatches) {

			// System.out.println("Match: " + c.category);

			String title1 = getGeneralizedCategoryTitle(c, mapCatEntity1, 1);

			String title1prime = title1.toLowerCase();
			title1prime = title1prime.replaceAll("\\p{Punct}", " ");
			title1prime = title1prime.replaceAll("\\s+", " ");

			// System.out.println("Title1prime: " + title1prime);

			if (checkTitle(title1, entity1) == false
					&& !setRedirectTitles1.contains(title1prime))
				continue;

			String title2 = getGeneralizedCategoryTitle(c, mapCatEntity2, 2);

			String title2prime = title2.toLowerCase();
			title2prime = title2prime.replaceAll("\\p{Punct}", " ");
			title2prime = title2prime.replaceAll("\\s+", " ");

			// System.out.println("Title2prime: " + title2prime);

			if (checkTitle(title2, entity2) == false
					&& !setRedirectTitles2.contains(title2prime))
				continue;

			// System.out.println("title1: " + title1 + ", title2: " + title2);

			Category c1 = mapProminentsTitles1.get(title1);
			Category c2 = mapProminentsTitles2.get(title2);

			String keyTitle = title1 + "_" + title2;

			if (mapSeparateTitles.containsKey(keyTitle)) {
				ArrayList<Category> arrCategories = mapSeparateTitles
						.get(keyTitle);

				if (useMutualInformation == false)
					arrCategories.add(c);
				else {
					double pwMI1 = getPointwiseMutualInformation(title1,
							c.category);
					double pwMI2 = getPointwiseMutualInformation(title2,
							c.category);
					double avgPwMI = (pwMI1 + pwMI2) / (double) 2;

					Category cat = new Category(c.category, c.upLevel, avgPwMI
							* c.score);
					cat.matchCategory1 = c.matchCategory1;
					cat.matchCategory2 = c.matchCategory2;
					// System.out.println("***" + cat.matchCategory1 + " - " +
					// cat.matchCategory2);

					arrCategories.add(cat);
				}

			} else {
				double proScore = (c1.score + c2.score) / (double) 2;

				ArrayList<Category> arrCategories = new ArrayList<Category>();
				arrCategories.add(c);
				mapSeparateTitles.put(keyTitle, arrCategories);

				TitlesScore titlesScore = new TitlesScore(keyTitle, proScore);
				arrTitlesScore.add(titlesScore);
			}

		}
		return mapSeparateTitles;
	}

	/**
	 * @param title1
	 * @param category
	 * @return
	 */
	private double getPointwiseMutualInformation(String title, String category)
			throws Exception {
		title = title.replaceAll("\\(.*\\)", " ");
		return hcSearcher.pointwiseMutualInformation(title, category);
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
	 * @param c
	 * @param mapCatEntity1
	 * @return
	 */
	private String getCategoryTitle(Category c,
			Map<String, Category> mapCatEntity) {

		Category c1 = mapCatEntity.get(c.category);

		String catParent = c1.catParent;
		while (!catParent.startsWith("*")) {
			c1 = mapCatEntity.get(catParent);
			catParent = c1.catParent;
		}
		return catParent.substring(1);
	}

	private String getGeneralizedCategoryTitle(Category genC,
			Map<String, Category> mapCatEntity, int entity) {

		// System.out.println("getGeneralizedCategoryTitle: genC.category="
		// + genC.category + ", genC.catOrigin="
		// + ((entity == 1) ? genC.matchCategory1 : genC.matchCategory2));

		Category c1 = mapCatEntity.get((entity == 1) ? genC.matchCategory1
				: genC.matchCategory2);

		String catParent = c1.catParent;
		while (!catParent.startsWith("*")) {
			c1 = mapCatEntity.get(catParent);
			catParent = c1.catParent;
		}
		return catParent.substring(1);
	}

	/**
	 * @param setMatches
	 * @param mapCatEntity1
	 * @param mapCatEntity2
	 * @return
	 */
	private Set<Category> changeScoreByMutualInformation(
			Set<Category> setMatches, String entity1, String entity2,
			Map<String, Category> mapCatEntity1,
			Map<String, Category> mapCatEntity2) throws Exception {

		Set<Category> setNewMatches = new HashSet<Category>();

		for (Category c : setMatches) {

			Category c1 = mapCatEntity1.get(c.category);
			Category c2 = mapCatEntity2.get(c.category);

			double pwMI1 = hcSearcher.pointwiseMutualInformation(entity1,
					c1.category);
			// System.out.println("(" + entity1 + ", " + c1.category + ") = "
			// + pwMI1);

			double pwMI2 = hcSearcher.pointwiseMutualInformation(entity2,
					c2.category);
			// System.out.println("(" + entity2 + ", " + c2.category + ") = "
			// + pwMI2);

			double tmpScore = (0.8 * c.score) + (0.2 * ((pwMI1 + pwMI2) / 2));

			// System.out.println("Category: " + c.category + ", c.score: "
			// + c.score + ", pwMI1: " + pwMI1 + ", pwMI2: " + pwMI2
			// + ", pwMI1*pwMI2: " + pwMI1 * pwMI2);

			double newScore = (tmpScore >= 0) ? Math.log(tmpScore) : 0;

			// System.out.println("score: " + newScore);
			// System.out.println();

			Category newCat = new Category(c.category, c.upLevel, newScore);
			setNewMatches.add(newCat);
		}
		return setNewMatches;
	}

	/**
	 * @param setMatches
	 * @param mapCatEntity1
	 * @param mapCatEntity2
	 */
	private Set<Category> changeScoreByProminentConcepts(
			Set<Category> setMatches, Map<String, Category> mapCatEntity1,
			Map<String, Category> mapCatEntity2) {
		Set<Category> setNewMatches = new HashSet<Category>();
		for (Category c : setMatches) {
			Category c1 = mapCatEntity1.get(c.category);
			Category c2 = mapCatEntity2.get(c.category);
			double newScore = Math.log(c.score * c1.score * c2.score);
			Category newCat = new Category(c.category, c.upLevel, newScore);
			setNewMatches.add(newCat);
		}
		return setNewMatches;
	}

	private StringBuffer printTraceCategory(String entity,
			Map<String, Category> mapCatEntity, Category c) {

		StringBuffer trace = new StringBuffer("");

		Category c1 = mapCatEntity.get(c.category);

		trace.append("\t\t" + c1.category);

		// System.out.print("\t\t" + c1.category);

		String catParent = c1.catParent;

		while (!catParent.startsWith("*")) {

			c1 = mapCatEntity.get(catParent);

			trace.append(" ~ " + c1.category);

			// System.out.print(" <- " + c1.category);

			catParent = c1.catParent;
		}

		trace.append(" ~ " + catParent + " ~ " + entity + "\n");

		// System.out.println(" <-- " + catParent + " <-- " + entity);

		return trace;
	}

	/**
	 * @param setCatEntity1
	 * @return
	 */
	private Map<String, Category> getHashMap(Set<Category> setCatEntity) {
		Map<String, Category> mapCatEntity = getMapCategory(setCatEntity);
		return mapCatEntity;
	}

	/**
	 * @param arrMatches
	 */
	private void sortScore(ArrayList<Category> arrMatches) {
		Collections.sort(arrMatches, new Comparator<Category>() {
			public int compare(Category arg0, Category arg1) {
				if (arg0.score < arg1.score)
					return 1;
				else if (arg0.score == arg1.score)
					return 0;
				else
					return -1;
			}
		});
	}

	/**
	 * @param setCatEntity1
	 * @param setCatEntity2
	 * @return
	 */
	private Set<Category> extractMatch(Set<Category> setCatEntity1,
			Set<Category> setCatEntity2) {

		Map<String, Category> mapCatEntity2 = getMapCategory(setCatEntity2);

		Set<Category> setMatches = new HashSet<Category>();

		matchCategories(setCatEntity1, setCatEntity2, mapCatEntity2, setMatches);

		return setMatches;
	}

	/**
	 * @param setCatEntity1
	 * @param setCatEntity2
	 * @return
	 */
	private Set<Category> extractGeneralizedMatch(Set<Category> setCatEntity1,
			Set<Category> setCatEntity2, Set<Category> setGenCatEntity1,
			Set<Category> setGenCatEntity2) {

		Map<String, Category> mapCatEntity1 = getMapCategory(setCatEntity1);
		Map<String, Category> mapCatEntity2 = getMapCategory(setCatEntity2);
		Map<String, Category> mapGenCatEntity2 = getMapCategory(setGenCatEntity2);

		Set<Category> setMatches = new HashSet<Category>();

		matchGeneralizedCategories(setCatEntity2, setGenCatEntity1,
				mapCatEntity2, setMatches, -1); // genC1 is a child of c2

		matchGeneralizedCategories(setCatEntity1, setGenCatEntity2,
				mapCatEntity1, setMatches, 1); // c1 is a parent of genC2

		matchBothGeneralizedCategories(setGenCatEntity2, setGenCatEntity1,
				mapGenCatEntity2, setMatches, 0); // genC1 and genC2 are
		// siblings

		return setMatches;
	}

	private void matchGeneralizedCategories(Set<Category> setCatEntity2,
			Set<Category> setGenCatEntity1,
			Map<String, Category> mapCatEntity2, Set<Category> setMatches,
			int direction) {

		for (Category genC1 : setGenCatEntity1) {

			if (setCatEntity2.contains(genC1)) {
				int c2Level = mapCatEntity2.get(genC1.category).upLevel;

				int upLevel = genC1.upLevel + c2Level;

				double score = (double) 1 / (double) (upLevel + 1);

				Category c = new Category(genC1.category, direction, score);
				c.matchCategory1 = genC1.catOrigin;
				c.matchCategory2 = mapCatEntity2.get(genC1.category).category;

				setMatches.add(c);
			}
		}
	}

	private void matchBothGeneralizedCategories(Set<Category> setGenCatEntity2,
			Set<Category> setGenCatEntity1,
			Map<String, Category> mapGenCatEntity2, Set<Category> setMatches,
			int direction) {
		for (Category genC1 : setGenCatEntity1) {

			if (setGenCatEntity2.contains(genC1)) {

				// System.out.println("\tgenC1.catOrigin=" + genC1.category);
				int c2Level = mapGenCatEntity2.get(genC1.category).upLevel;

				int upLevel = genC1.upLevel + c2Level;

				double score = (double) 1 / (double) (upLevel + 1);

				Category c = new Category(genC1.category, direction, score);
				c.matchCategory1 = genC1.catOrigin;
				c.matchCategory2 = mapGenCatEntity2.get(genC1.category).catOrigin;

				if (!c.matchCategory1.equals(c.matchCategory2))
					setMatches.add(c);
			}
		}
	}

	private void matchCategories(Set<Category> setCatEntity1,
			Set<Category> setCatEntity2, Map<String, Category> mapCatEntity2,
			Set<Category> setMatches) {

		for (Category c1 : setCatEntity1) {

			// System.out.println("c1: " + c1.category);

			if (setCatEntity2.contains(c1)) {

				// System.out.println("\tc2: "
				// + mapCatEntity2.get(c1.category).category);

				int c2Level = mapCatEntity2.get(c1.category).upLevel;

				int upLevel = c1.upLevel + c2Level;

				double score = (double) 1 / (double) (upLevel + 1);

				int direction = 0; // sibling
				if (c1.upLevel == 0 && c2Level == 0) {
					direction = 0;
				} else {
					if (c1.upLevel == 0)
						direction = 1; // c1 is the PARENT
					else if (c2Level == 0)
						direction = -1; // c1 is a CHILD
					else
						direction = 0; // sibling
				}

				setMatches.add(new Category(c1.category, direction, score));
			}
		}
	}

	private Map<String, Category> getMapCategory(Set<Category> setCatEntity) {
		Map<String, Category> mapCatEntity = new HashMap<String, Category>();
		for (Category c : setCatEntity) {
			mapCatEntity.put(c.category, c);
		}
		return mapCatEntity;
	}

	public Set<Category> extractAllCategories(String entity,
			Map<String, Category> mapProminentTitles) throws Exception {

		ArrayList<ILuceneResult> arrResults = titleSearcher.search(entity,
				MAX_NUMRESULTS);

		for (ILuceneResult result : arrResults) {
			Category c = new Category(result.getTitle(), 0, result.getScore());
			mapProminentTitles.put(result.getTitle(), c);
		}

		Set<Category> catEntity = new HashSet<Category>();

		for (ILuceneResult result : arrResults) {

			Set<Category> arrFirstCategories = titleSearcher.extractCategories(
					Integer.parseInt(result.getId()), result.getScore());

			// System.out.println("arrFirstCategories size: " +
			// arrFirstCategories.size());

			Set<Category> setCategories = catExtractor.extractCategories(0,
					arrFirstCategories, MAX_UPLEVEL);

			Map<String, Category> mapCatEntity = getMapCategory(catEntity);

			for (Category c : setCategories) {
				if (catEntity.contains(c)
						&& mapCatEntity.get(c.category).upLevel > c.upLevel)
					catEntity.remove(c);
			}

			catEntity.addAll(setCategories);
		}

		return catEntity;
	}

}
