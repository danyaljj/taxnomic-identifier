/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

import java.util.ArrayList;

import net.didion.jwnl.data.POS;
import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.HitCountSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.TitleSearcher;
import edu.illinois.cs.cogcomp.utils.PeopleManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Feb 15, 2009
 */
public class RelationDetector {

	public static final int TASK_PARENTCHILD = 1;
	public static final int TASK_SIBLING = 2;
	public static final int TASK_ALL = 3;

	protected TitleSearcher titleSearcher = null;
	protected CategorySearcher catSearcher = null;
	protected int task = TASK_PARENTCHILD;

	protected boolean useProminence = true;
	
	protected boolean useMutualInformation = true;

	protected WordNetManager wnManager = null;

	protected PeopleManager peopleManager = null;

	protected ParentChildDetector parentChildDetector = null;

	protected SiblingDetector siblingDetector = null;

	protected HitCountSearcher hcSearcher = null;

	public boolean outputToStdout = true;

	public CateAnalyzer analyzer = null;

	/**
	 * 
	 */
	public RelationDetector(String titleIndexDirectory,
			String categoryIndexDirectory) throws Exception {

		titleSearcher = new TitleSearcher(null, false);
		titleSearcher.open(titleIndexDirectory);
		titleSearcher.setSortByProminence(useProminence);

		catSearcher = new CategorySearcher(null, false);
		catSearcher.open(categoryIndexDirectory);

		hcSearcher = new HitCountSearcher(titleIndexDirectory);

		wnManager = new WordNetManager("file_properties.xml");

		siblingDetector = new SiblingDetector();

		parentChildDetector = new ParentChildDetector();

		analyzer = new CateAnalyzer();

	}

	/**
	 * @param useProminence the useProminence to set
	 */
	public void setUseProminence(boolean useProminence) {
		this.useProminence = useProminence;
		titleSearcher.setSortByProminence(this.useProminence);
	}
	
	public void setPeopleManager(String peopleFileName) {
		peopleManager = new PeopleManager(peopleFileName);
	}

	public void setTask(int task) {
		this.task = task;
	}
	
	public String detectRelation(String e_1, String e_2, int task)
			throws Exception {

		StringBuffer outputBuffer = new StringBuffer("");

		e_1 = formatEntity(e_1);
		EntityInfo eInfo_1 = new EntityInfo(e_1);
		eInfo_1.collectInfo(titleSearcher, catSearcher, analyzer, task);
		// System.out.println(eInfo_1.toString());

		// System.out.println("--------");

		e_2 = formatEntity(e_2);
		EntityInfo eInfo_2 = new EntityInfo(e_2);
		eInfo_2.collectInfo(titleSearcher, catSearcher, analyzer, task);
		// System.out.println(eInfo_2.toString());

		this.task = task;

		switch (this.task) {
		case TASK_PARENTCHILD:
			// System.out.println("-----Matching Parent-------");

			boolean isParent;

			if (eInfo_2.arrTitles.size() == 0) {
				isParent = false;
				outputBuffer.append("NOT FOUND IN WIKIPEDIA " + e_2);
			} else
				isParent = detectParent(eInfo_1, eInfo_2);

			if (isParent == true) {

				parentChildDetector.sortPromScore();

				// System.out.println("PARENT");
				// outputBuffer.append("PARENT\n");

				String result = parentChildDetector.printParentRelation(
						eInfo_1, eInfo_2);

				outputBuffer.append(result + "\n");

			} else {

				// System.out.println("NOT PARENT");
				outputBuffer.append("NOT PARENT\n");

			}

			/*
			 * Note that, we don't do Child detection because we know that e1 is
			 * always parent, and e2 is always child, if e1 is indeed aparent of
			 * 
			 * e2. // System.out.println("-----Matching Child-------");
			 * 
			 * boolean isChild = detectParent(eInfo_2, eInfo_1);
			 * 
			 * if (isChild == true) {
			 * 
			 * System.out.println("CHILD");
			 * 
			 * printParentRelation(eInfo_2, eInfo_1);
			 * 
			 * } else System.out.println("NOT CHILD");
			 */

			break;
		case TASK_SIBLING:

			// System.out.println("-----Matching Sibling------");

			boolean isSibling = detectSibling(eInfo_1, eInfo_2);

			if (isSibling == true) {

				// System.out.println("Sorting by Prominence.");

				siblingDetector.sortPromScore();

				// System.out.println("Grouping.");

				siblingDetector.groupMatchesSibling();

				// System.out.println("Sorting by Mutual Information.");

				siblingDetector.calculateMutualInformation(hcSearcher);

				// System.out.println("SIBLING.");

				String result = siblingDetector.printSiblingRelation(eInfo_1,
						eInfo_2);

				outputBuffer.append(result + "\n");

			} else {
				outputBuffer.append("NOT SIBLING." + "\n");
				// System.out.println("NONE");
			}
			break;

		default:
		}

		if (outputToStdout == true)
			System.out.println(outputBuffer.toString());

		return outputBuffer.toString();
	}

	/**
	 * @param info_1
	 * @param info_2
	 * @return
	 */
	private boolean detectSibling(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {
		return siblingDetector.detectSibling(eInfo_1, eInfo_2);
	}

	/**
	 * @param info_1
	 * @param info_2
	 * @return
	 * @throws Exception
	 */
	private boolean detectParent(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {
		return parentChildDetector.detectParent(eInfo_1, eInfo_2);
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	public String formatEntity(String e) throws Exception {

		// lexical
		String entity = e.replaceAll("\\p{Punct}", " ");
		entity = entity.replaceAll("\\s+", " ");
		entity = entity.toLowerCase();
		entity = entity.trim();

		// morphology
		// ArrayList<String> arrMorphs = null;
		// if (entity.indexOf(" ") == -1 && entity.matches(".*\\d+.*") == false)
		// {
		// arrMorphs = wnManager.getMorph(POS.NOUN, entity);
		// if (arrMorphs.size() == 1)
		// entity = arrMorphs.get(0);
		// }

		// entity = getMorph(entity);

		return entity;
	}

	public String getMorph(String entity) throws Exception {

		String tokens[] = entity.split("\\s+");

		int n = tokens.length;

		String last = tokens[n - 1];

		// morphology
		ArrayList<String> arrMorphs = null;
		if (last.matches(".*\\d+.*") == false) {
			if (peopleManager != null) {
				if (!peopleManager.isValidPeopleName(last)) {
					arrMorphs = wnManager.getMorph(POS.NOUN, last);
					if (arrMorphs.size() == 1)
						last = arrMorphs.get(0);
				}
			} else {
				arrMorphs = wnManager.getMorph(POS.NOUN, last);
				if (arrMorphs.size() == 1)
					last = arrMorphs.get(0);
			}
		}

		if (n == 1)
			return last;

		tokens[n - 1] = last;

		StringBuffer buf = new StringBuffer("");

		for (int i = 0; i < n; i++) {
			buf.append(tokens[i] + " ");
		}

		return buf.toString().trim();

	}

}
