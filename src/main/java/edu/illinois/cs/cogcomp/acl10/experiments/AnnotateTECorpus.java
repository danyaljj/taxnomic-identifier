/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.Acl10Constants;
import edu.illinois.cs.cogcomp.acl10.wikiannotator.WikiTitleAnnotator;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 2, 2010
 */
public class AnnotateTECorpus {

	protected WikiTitleAnnotator wikiAnnotator = null;

	/**
	 * 
	 */
	public AnnotateTECorpus(String configFile) {

		Map<String, String> configs = Acl10Constants.readConfigFile(configFile);
		System.out.println(configs.get(Acl10Constants.NGRAM_TITLE_FILE));
		System.out.println(configs.get(Acl10Constants.IDF_FILE));

		wikiAnnotator = new WikiTitleAnnotator(configs);

	}

	public String annotateWikiTitle(String text) {
		return wikiAnnotator.annotate(text);
	}

	public void annotateCorpus(String corpusTEFile, String outputFile) {

		List<String> teTexts = getTETexts(corpusTEFile);

		System.out.println("# of texts: " + teTexts.size());

		ArrayList<String> results = new ArrayList<String>();
		int i = 0;
		for (String te : teTexts) {
			System.out.println((i + 1) + ". "
					+ te.substring(0, te.length() > 50 ? 50 : te.length()));
			te = te.replaceAll("\\[", "");
			String s = annotateWikiTitle(te);
			results.add(s);
			i++;
			if (i % 100 == 0)
				System.out.println("Processed: " + i);
		}
		IOManager.writeLinesAddingReturn(results, outputFile);

	}

	public static List<String> getTETexts(String corpusTEFile) {

		ArrayList<String> arrLines = IOManager.readLines(corpusTEFile);

		List<String> teTexts = new ArrayList<String>();

		for (String line : arrLines) {

			if (line.startsWith("<t>") || line.startsWith("<h>")) {
				line = line.substring(3, line.length() - 4);
				teTexts.add(line);
			}
		}

		return teTexts;
	}

	public static double averageLength(String corpusTEFile) {
		List<String> textList = getTETexts(corpusTEFile);

		int length = 0;
		for (String t : textList) {
			String[] parts = t.split("\\s+");
			length += parts.length;
		}

		System.out.println("Total texts & hypotheses: " + textList.size());
		System.out.println("Total length: " + length);

		return ((double) length / (double) textList.size());

	}

	public static void main(String[] args) {

		System.out.println(AnnotateTECorpus.averageLength(args[0]));

	}
}
