/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.TitleSearcher;

/**
 * @author dxquang Feb 15, 2009
 */
public class EntityInfo {

	public static final int NUM_RESULT = 10;
	public static final String ROOT_ID = "root";
	public static final String USER_ID = "user";

	public String entity;

	public ArrayList<Title> arrTitles = null;
	public Map<String, Title> mapTitles = null;

	public int numResult;

	public int task;

	/**
	 * 
	 */
	public EntityInfo(String entity) {

		this.entity = entity;

		this.arrTitles = new ArrayList<Title>();

		this.mapTitles = new HashMap<String, Title>();

		this.numResult = NUM_RESULT;

		this.task = RelationDetector.TASK_PARENTCHILD;
	}

	public EntityInfo(String entity, int task) {

		this.entity = entity;

		this.arrTitles = new ArrayList<Title>();

		this.mapTitles = new HashMap<String, Title>();

		this.numResult = NUM_RESULT;

		this.task = task;
	}

	/**
	 * @param numResult
	 *            the numResult to set
	 */
	public void setNumResult(int numResult) {
		this.numResult = numResult;
	}

	public void collectInfo(TitleSearcher titleSearcher,
			CategorySearcher catSearcher, CateAnalyzer analyzer, int task)
			throws Exception {

		retrieveTitles(titleSearcher, catSearcher, analyzer, task);

		makeMapTitle();

	}

	/**
	 * 
	 */
	private void makeMapTitle() {
		for (Title title : arrTitles) {
			mapTitles.put(title.id, title);
		}
	}

	private void retrieveTitles(TitleSearcher titleSearcher,
			CategorySearcher catSearcher, CateAnalyzer analyzer, int task)
			throws Exception {

		ArrayList<ILuceneResult> arrResutls = titleSearcher.search(this.entity,
				numResult);

		boolean checkTitle = false;

		for (ILuceneResult result : arrResutls) {

			String title = result.getTitle();
			if (checkTitle == false && title.equals(this.entity))
				checkTitle = true;
			String id = result.getId();
			String category = result.getCategory();
			double score = result.getScore();

			Title titleObj = new Title(id, title, category, score);
			titleObj.retrieveCategories(catSearcher, analyzer, task);

			arrTitles.add(titleObj);
		}

		if (checkTitle == false) {
			Title titleObj = new Title(USER_ID, this.entity, "", 0);

			arrTitles.add(titleObj);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuffer buf = new StringBuffer("");

		buf.append("*Entity: " + entity + "\n");

		int n = arrTitles.size();

		for (int i = 0; i < n; i++) {

			Title t = arrTitles.get(i);

			buf.append("[" + (i + 1) + "]" + t.toString() + "\n");

		}

		return buf.toString();
	}

	public ArrayList<Match> matchTitleCategory(EntityInfo eInfo, int whatToMatch)
			throws Exception {

		ArrayList<Title> arrMyTitles = eInfo.arrTitles;

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		for (Title myTitle : arrMyTitles) {

			// System.out.println("X title: " + myTitle.title);

			ArrayList<Match> arrMatchCates = matchTitleCategory(myTitle,
					whatToMatch);

			if (arrMatchCates.size() > 0) {

				// System.out.println("**arrMatchCates.size()="
				// + arrMatchCates.size());

				arrMatches.addAll(arrMatchCates);
			}

		}

		// System.out.println();
		return arrMatches;
	}

	/**
	 * @param title
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Match> matchTitleCategory(Title myTitle, int whatToMatch)
			throws Exception {

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		for (Title title : arrTitles) {

			// System.out.println("\tY title: " + title.title);

			ArrayList<Match> arrMatchCates = title.matchCategory(myTitle,
					whatToMatch);

			if (arrMatchCates.size() > 0) {

				// System.out.println("\t\t**arrMatchCates.size()="
				// + arrMatchCates.size());

				arrMatches.addAll(arrMatchCates);

			}
		}

		return arrMatches;
	}

	/**
	 * @param cate
	 * @return
	 */
	public String getTrace(String titleId, int catId) {

		// System.out.println("titleId = " + titleId + ", catId = " + catId);

		Title title = mapTitles.get(titleId);

		Cate cate = title.getCate(catId);

		if (cate == null)
			return "(" + title.title + ")";

		StringBuffer buf = new StringBuffer("");

		while (cate.level > 1) {
			buf.append(cate.categoryName + " ~ ");
			cate = title.getCate(cate.fromdId);
		}
		buf.append(cate.categoryName + " ~ " + "(" + title.title + ")");

		return buf.toString();
	}

	/**
	 * @param info_1
	 * @param matchCategory
	 * @return
	 */
	public ArrayList<Match> matchCategoryCategory(EntityInfo eInfo,
			int whatToMatch) throws Exception {

		ArrayList<Title> arrMyTitles = eInfo.arrTitles;

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		for (Title myTitle : arrMyTitles) {

			// System.out.println("X title: " + myTitle.title);

			ArrayList<Match> arrMatchCates = matchCategoryCategory(myTitle,
					whatToMatch);

			if (arrMatchCates.size() > 0) {

				// System.out.println("**arrMatchCates.size()="
				// + arrMatchCates.size());

				arrMatches.addAll(arrMatchCates);
			}

		}

		System.out.println();

		return arrMatches;
	}

	private ArrayList<Match> matchCategoryCategory(Title myTitle,
			int whatToMatch) throws Exception {

		ArrayList<Match> arrMatches = new ArrayList<Match>();

		for (Title title : arrTitles) {

			// System.out.println("\tY title: " + title.title);

			ArrayList<Match> arrMatchCates = title.matchCategorySibling(
					myTitle, whatToMatch);

			if (arrMatchCates.size() > 0) {

				// System.out.println("\t\t**arrMatchCates.size()="
				// + arrMatchCates.size());

				arrMatches.addAll(arrMatchCates);

			}
		}

		return arrMatches;
	}

}
