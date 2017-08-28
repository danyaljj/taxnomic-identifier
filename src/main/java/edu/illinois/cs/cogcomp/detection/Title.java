/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;

/**
 * 
 * @author dxquang Feb 15, 2009
 */
public class Title {

	public static final int ROOT_LEVEL = 1;
	public static final int FIRST_CATID = 0;
	public static final int MAX_LEVEL = 2;

	public static final int MATCH_CATEGORY = 1;
	public static final int MATCH_GENCAT = 2;
	public static final int MATCH_DOMAIN = 3;

	public static final double WEIGHT_GENCAT = 0.5;

	public String id;

	public String title;

	public double score;

	public ArrayList<Cate> arrCates = new ArrayList<Cate>();

	public Map<Integer, Cate> mapCates = new HashMap<Integer, Cate>();

	public int globalCatId;

	/**
	 * 
	 */
	public Title(String id, String title, String cate, double score) {

		this.id = id;

		this.title = title;

		this.score = score;

		this.globalCatId = 1;

		if (cate.length() > 0) {
			String cats[] = cate.split("\\|");

			for (String cat : cats) {

				Cate catObj = new Cate(this.id, globalCatId, FIRST_CATID, cat,
						ROOT_LEVEL);

				this.globalCatId++;

				arrCates.add(catObj);

			}
		}
	}

	public void retrieveCategories(CategorySearcher catSearcher, CateAnalyzer analyzer, int task)
			throws Exception {

		int n = arrCates.size();

		int i = 0;

		while (i < n) {

			Cate catObj = arrCates.get(i);

			String category = catObj.categoryName;

			int id = catObj.id;

			int level = catObj.level;

			ArrayList<Cate> arrCategories = retrieveCategories(catSearcher,
					category, id, level, analyzer, task);

			if (arrCategories.size() > 0)
				arrCates.addAll(arrCategories);

			n = arrCates.size();

			i++;
		}

		makeMapCates();
	}

	/**
	 * 
	 */
	private void makeMapCates() {

		for (Cate cate : arrCates) {
			mapCates.put(cate.id, cate);
		}

	}

	public Cate getCate(int cateId) {
		return mapCates.get(cateId);
	}

	private ArrayList<Cate> retrieveCategories(CategorySearcher catSearcher,
			String category, int fromId, int level, CateAnalyzer analyzer, int task) throws Exception {

		ArrayList<Cate> arrCategories = new ArrayList<Cate>();

		if (level >= MAX_LEVEL)
			return arrCategories;

		ArrayList<ILuceneResult> arrResults = catSearcher.search(category, 1);

		if (arrResults.size() == 1) {

			ILuceneResult result = arrResults.get(0);

			String catString = result.getCategory();

			String cats[] = catString.split("\\|");

			for (String cat : cats) {

				Cate cate = new Cate(this.id, globalCatId, fromId, cat,
						level + 1, analyzer, task);

				globalCatId++;

				arrCategories.add(cate);

			}

		}

		return arrCategories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuffer buf = new StringBuffer("");

		buf.append("**Title: " + title + " (id=" + id + ", score=" + score
				+ ")\n");

		int n = arrCates.size();

		for (int i = 0; i < n; i++) {

			Cate c = arrCates.get(i);

			buf.append("(" + (i + 1) + ")" + c.toString() + "\n");

		}

		return buf.toString();
	}

	public ArrayList<Match> matchCategory(Title myTitle, int whatToMatch)
			throws Exception {

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		// System.out.println("\t\tY category size: " + arrCates.size());
		for (Cate cate : arrCates) {

			// System.out.println("\t\tY category: " + cate.categoryName);

			String s = "";
			if (whatToMatch == MATCH_CATEGORY)
				s = cate.categoryName;
			else if (whatToMatch == MATCH_GENCAT)
				s = cate.generalizedName;
			else
				s = cate.domain;

			if (s.length() == 0)
				continue;

			// System.out.println("s=" + s);

			if (LLM.exactMatch(myTitle.title, s, false, true) == true) {

				// System.out.println("\t\t\t***Match X title = " + myTitle
				// + " and Y category = " + cate.categoryName);

				Match match = new Match(myTitle.id, myTitle.title, 0,
						myTitle.score, this.id, this.title, cate.id, this.score);

				arrMatches.add(match);

			}
		}

		return arrMatches;
	}

	public ArrayList<Match> matchCategorySibling(Title myTitle, int whatToMatch)
			throws Exception {

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		ArrayList<Cate> arrMyCates = myTitle.arrCates;

		// System.out.println("\t\tY category size: " + arrCates.size());
		for (Cate myCate : arrMyCates) {

			String myS = "";

			if (whatToMatch == MATCH_CATEGORY)
				myS = myCate.categoryName;
			else
				myS = myCate.generalizedName;

			if (myS.length() == 0)
				continue;

			for (Cate cate : arrCates) {

				// System.out.println("\t\tX category: " + myCate.categoryName +
				// ", Y category: " + cate.categoryName);

				String s = "";

				if (whatToMatch == MATCH_CATEGORY)
					s = cate.categoryName;
				else
					s = cate.generalizedName;

				if (s.length() == 0)
					continue;

				// System.out.println("s=" + s);

				if (LLM.exactMatch(myS, s, false, false) == true) {

					// System.out.println("\t\t\t***Match X title = " + myTitle
					// + " and Y category = " + cate.categoryName);

					Match match = new Match(myTitle.id, myTitle.title,
							myCate.id, myTitle.score, this.id, this.title,
							cate.id, this.score);

					match.targetClass = myS;

					int totalLevel = myCate.level + cate.level;

					match.levelScore = ((double) 1 / (double) (totalLevel + 1));

					// This is very subjective!!!
					if (whatToMatch == MATCH_GENCAT)
						match.levelScore *= WEIGHT_GENCAT;

					arrMatches.add(match);

				}
			}
		}
		return arrMatches;
	}
}
