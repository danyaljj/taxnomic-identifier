/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.detection.CateAnalyzer;
import edu.illinois.cs.cogcomp.detection.EntityInfo;
import edu.illinois.cs.cogcomp.detection.ParentChildDetector;
import edu.illinois.cs.cogcomp.detection.SiblingDetector;
import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.HitCountSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.TitleSearcher;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.PeopleManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Feb 19, 2009
 */
public class SPCEval {

	public static final int TASK_PARENTCHILD = 1;
	public static final int TASK_SIBLING = 2;
	public static final int TASK_ALL = 3;

	protected TitleSearcher titleSearcher = null;
	protected CategorySearcher catSearcher = null;

	protected boolean useProminence = true;
	protected boolean useMutualInformation = true;

	protected WordNetManager wnManager = null;

	protected PeopleManager peopleManager = null;

	protected ParentChildDetector parentChildDetector = null;

	protected SiblingDetector siblingDetector = null;

	protected HitCountSearcher hcSearcher = null;

	public boolean outputToStdout = false;

	public CateAnalyzer analyzer = null;

	TargetClassMapping targetClassMapper = null;

	public SPCEval(String titleIndexDirectory, String categoryIndexDirectory,
			String peopleFile, boolean useProminence,
			boolean useMutualInformation) throws Exception {

		this.useProminence = useProminence;
		this.useMutualInformation = useMutualInformation;

		titleSearcher = new TitleSearcher(null, false);
		titleSearcher.open(titleIndexDirectory);
		titleSearcher.setSortByProminence(this.useProminence);

		catSearcher = new CategorySearcher(null, false);
		catSearcher.open(categoryIndexDirectory);

		hcSearcher = new HitCountSearcher(titleIndexDirectory);

		wnManager = new WordNetManager("file_properties.xml");

		siblingDetector = new SiblingDetector();

		parentChildDetector = new ParentChildDetector();

		analyzer = new CateAnalyzer();

	}

	public String detectRelation(String entity_1, String entity_2)
			throws Exception {

		String e_1 = formatEntity(entity_1);
		EntityInfo eInfo_1 = new EntityInfo(e_1);
		eInfo_1.collectInfo(titleSearcher, catSearcher, analyzer,
				TASK_PARENTCHILD);

		String e_2 = formatEntity(entity_2);
		EntityInfo eInfo_2 = new EntityInfo(e_2);
		eInfo_2.collectInfo(titleSearcher, catSearcher, analyzer,
				TASK_PARENTCHILD);

		String e_3 = formatEntity(entity_1);
		EntityInfo eInfo_3 = new EntityInfo(e_3);
		eInfo_3.collectInfo(titleSearcher, catSearcher, analyzer, TASK_SIBLING);

		String e_4 = formatEntity(entity_2);
		EntityInfo eInfo_4 = new EntityInfo(e_4);
		eInfo_4.collectInfo(titleSearcher, catSearcher, analyzer, TASK_SIBLING);

		StringBuffer outputBuffer = new StringBuffer("");

		boolean hasRelation;

		// 1. Two entities go through the Ancestor Detector
		hasRelation = detectParent(eInfo_1, eInfo_2);

		if (hasRelation == true) {

			// System.out.println("PARENT");
			outputBuffer.append(">ANCESTOR<\n");

			parentChildDetector.sortPromScore();

			String result = parentChildDetector.printParentRelation(eInfo_1,
					eInfo_2);

			outputBuffer.append(result + "\n");

		} else {

			hasRelation = detectSibling(eInfo_3, eInfo_4);

			if (hasRelation == true) {

				outputBuffer.append(">SIBLING<\n");
				// System.out.println("Sorting by Prominence.");

				siblingDetector.sortPromScore();

				// System.out.println("Grouping.");

				siblingDetector.groupMatchesSibling();

				// System.out.println("Sorting by Mutual Information.");

				siblingDetector.calculateMutualInformation(hcSearcher);

				// System.out.println("SIBLING.");

				String result = siblingDetector.printSiblingRelation(eInfo_3,
						eInfo_4);

				outputBuffer.append(result + "\n");

			} else {
				outputBuffer.append(">NONE<" + "\n");
				// System.out.println("NONE");
			}

		}

		if (outputToStdout == true)
			System.out.println(outputBuffer.toString());

		return outputBuffer.toString();

	}

	private boolean detectSibling(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {
		return siblingDetector.detectSibling(eInfo_1, eInfo_2);
	}

	private boolean detectParent(EntityInfo eInfo_1, EntityInfo eInfo_2)
			throws Exception {

		return parentChildDetector.detectParent(eInfo_1, eInfo_2);

	}

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

	public void evaluate(String inputTestFile, String outputResultFile)
			throws Exception {

		BufferedWriter writer = IOManager.openWriter(outputResultFile);

		ArrayList<String> arrLines = IOManager.readLines(inputTestFile);

		int i = 1;

		for (String line : arrLines) {

			System.out.println("[" + i + "/" + arrLines.size() + "] " + line);

			String result = evaluateExample(line);

			if (result == null || result == "")
				result = "NULL";

			writeResult(line, result, writer);

			i++;
		}

		IOManager.closeWriter(writer);
	}

	private void writeResult(String line, String result, BufferedWriter writer)
			throws Exception {
		StringBuffer outString = new StringBuffer("");
		outString.append("<DOC>\n");
		outString.append("<QUERY>" + line + "</QUERY>\n");
		outString.append("<RESULT>\n" + result + "\n</RESULT>\n");
		outString.append("</DOC>\n");
		writer.write(outString.toString());
	}

	private String evaluateExample(String line) throws Exception {

		String result = "";
		String tokens[] = line.split("\\t+");

		if (tokens.length != 3)
			return null;

		String entityClass = tokens[0];
		String e_1 = tokens[1];
		String e_2 = tokens[2];

		if (targetClassMapper != null
				&& targetClassMapper.isValidTargetClass(e_1))
			e_1 = targetClassMapper.getFirstItem(e_1);

		if (targetClassMapper != null
				&& targetClassMapper.isValidTargetClass(e_2))
			e_2 = targetClassMapper.getFirstItem(e_2);

		result = detectRelation(e_1, e_2);

		return result;
	}

	public void setTargetClassMapper(String mapFile) {

		targetClassMapper = new TargetClassMapping(mapFile);

	}

}
