/**
 * 
 */
package edu.illinois.cs.cogcomp.category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.lucenesearch.Category;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneSearcher;

/**
 * 
 * @author dxquang Jan 29, 2009
 */

public class CategoryExtraction {

	public final static int MAX_NUM_DOCUMENT = 10000000;

	ILuceneSearcher searcher = null;

	/**
	 * 
	 */
	public CategoryExtraction(ILuceneSearcher searcher) {
		this.searcher = searcher;
	}

	public Set<Category> extractCategories(int level,
			Set<Category> setLevelCategories, int maxUpLevel) throws Exception {

		if (level == maxUpLevel || setLevelCategories == null
				|| setLevelCategories.size() == 0) {
			return null;
		}

		level++;

		Set<Category> setCategories = new HashSet<Category>();

		Set<Category> setCats = new HashSet<Category>();

		for (Category levelCat : setLevelCategories) {

			ArrayList<ILuceneResult> arrUpLevelResults = searcher.search(
					levelCat.category, 1);

			if (arrUpLevelResults.size() > 0) {

				ILuceneResult result = arrUpLevelResults.get(0);

				String catString = result.getCategory();

				//double score = result.getScore();
				double score = levelCat.score;

				String cats[] = catString.split("\\|");

				for (String cat : cats) {
					Category c = new Category(cat, level, score);
					c.catParent = levelCat.category;
					setCats.add(c);
				}

			}
		}

		Set<Category> setUpCategories = extractCategories(level, setCats,
				maxUpLevel);

		setCategories.addAll(setLevelCategories);

		if (setUpCategories != null) {

			setCategories.addAll(setUpCategories);
		}

		return setCategories;
	}

}
