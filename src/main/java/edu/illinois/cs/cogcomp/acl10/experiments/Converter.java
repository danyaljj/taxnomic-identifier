/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 4, 2010
 */
public class Converter {

	public static void convertWikiTitle2LearningData(String inputFile,
			String outputFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		Pattern pattern = Pattern.compile("\\[.+?\\]");

		ArrayList<String> arrPairs = new ArrayList<String>();

		List<String> arrOdds = new ArrayList<String>();
		List<String> arrEvens = new ArrayList<String>();

		int c = 1;
		int pair = 1;
		for (String line : arrLines) {

			if (line.length() == 0) {
				continue;
			}

			System.out.println("Sentence " + c);

			if (c % 2 == 1) {
				arrOdds = new ArrayList<String>();
			} else {
				arrEvens = new ArrayList<String>();
			}

			Matcher match = pattern.matcher(line);

			while (match.find()) {
				String concept = match.group();
				if (concept.startsWith("[WIKI"))
					concept = concept.substring(5, concept.length() - 1).trim();
				else
					concept = concept.substring(1, concept.length() - 1).trim();
				// System.out.println(concept);

				if (c % 2 == 1) {
					arrOdds.add(concept);
				} else {
					arrEvens.add(concept);
				}
			}
			// System.out.println();
			System.out.println("arrOdds size: " + arrOdds.size());
			System.out.println("arrEvens size: " + arrEvens.size());

			if (c % 2 == 0) {
				arrPairs.add("#Pair " + pair++);
				for (String c1 : arrOdds) {
					for (String c2 : arrEvens) {
						arrPairs.add("0" + "\t_|_\t" + c1.trim() + "\t"
								+ c2.trim());
					}
				}
			}

			c++;
		}

		IOManager.writeLinesAddingReturn(arrPairs, outputFile);

	}

	public static void convertWikiTitle2XMLAnnotation(String inputFile,
			String outputFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		Pattern pattern = Pattern.compile("\\[.+?\\]");

		ArrayList<String> arrPairs = new ArrayList<String>();

		List<String> arrOdds = new ArrayList<String>();
		List<String> arrEvens = new ArrayList<String>();

		int c = 1;
		int pair = 1;
		String header = "";
		String text = "";
		String hypo = "";
		arrPairs.add("<?xml version='1.0' encoding='utf-8'?>");
		arrPairs.add("<entailment-corpus>");

		for (String line : arrLines) {

			if (line.length() == 0) {
				continue;
			}

			System.out.println("Sentence " + c);

			if (c % 2 == 1) {
				header = "<pair corpus=\"RTE-Corpus\" labels=\"Relevant\" id=\""
						+ (pair++) + "\" llm=\"0.0\" entails=\"\">";

				arrOdds = new ArrayList<String>();
				text = line;
			} else {
				arrEvens = new ArrayList<String>();
				hypo = line;
			}

			Matcher match = pattern.matcher(line);

			int pos = 0;
			while (match.find()) {
				String concept = match.group();
				int start = match.start();
				int end = match.end();
				String prevStr = line.substring(pos, start).trim();
				pos = end;

				if (concept.startsWith("[WIKI"))
					concept = concept.substring(5, concept.length() - 1).trim();
				else
					concept = concept.substring(1, concept.length() - 1).trim();
				// System.out.println(concept);

				if (c % 2 == 1) {
					if (prevStr.length() > 0)
						arrOdds.add(prevStr);
					arrOdds.add("c_" + concept);
				} else {
					if (prevStr.length() > 0)
						arrEvens.add(prevStr);
					arrEvens.add("c_" + concept);
				}
			}

			String lastStr = line.substring(pos, line.length()).trim();
			if (c % 2 == 1) {
				if (lastStr.length() > 0)
					arrOdds.add(lastStr);
			} else {
				if (lastStr.length() > 0)
					arrEvens.add(lastStr);
			}

			// System.out.println();
			System.out.println("arrOdds size: " + arrOdds.size());
			System.out.println("arrEvens size: " + arrEvens.size());

			if (c % 2 == 0) {
				arrPairs.add(" " + header);
				arrPairs.add("  <txt>");
				text = text.replaceAll("\\[WIKI ", "");
				text = text.replaceAll("\\[", "");
				text = text.replaceAll("\\]", "");
				arrPairs.add("   " + text);
				arrPairs.add("  </txt>");
				arrPairs.add("  <hyp>");
				hypo = hypo.replaceAll("\\[WIKI ", "");
				hypo = hypo.replaceAll("\\[", "");
				hypo = hypo.replaceAll("\\]", "");
				arrPairs.add("   " + hypo);
				arrPairs.add("  </hyp>");
				arrPairs.add("  <views>");
				arrPairs.add("   <view name=\"default\">");
				arrPairs.add("    <txt-annotation>");
				int chunkId = 1;
				for (String c1 : arrOdds) {
					if (c1.startsWith("c_")) {
						arrPairs.add("     <node id=\"chunk" + chunkId + "\">");
						arrPairs.add("      " + c1.substring(2).trim());
						arrPairs.add("     </node>");
						chunkId++;
					} else {
						arrPairs.add("     " + c1);
					}
				}
				arrPairs.add("    </txt-annotation>");
				arrPairs.add("    <hyp-annotation>");
				for (String c1 : arrEvens) {
					if (c1.startsWith("c_")) {
						arrPairs.add("     <node id=\"chunk" + chunkId + "\">");
						arrPairs.add("      " + c1.substring(2).trim());
						arrPairs.add("     </node>");
						chunkId++;
					} else {
						arrPairs.add("     " + c1);
					}
				}
				arrPairs.add("    </hyp-annotation>");
				arrPairs.add("    <edges>");
				arrPairs.add("    </edges>");
				arrPairs.add("   </view>");
				arrPairs.add("  </views>");
				arrPairs.add(" </pair>");

			}

			c++;
		}

		arrPairs.add("</entailment-corpus>");

		IOManager.writeLinesAddingReturn(arrPairs, outputFile);
	}

	public static void convertXMLAnnotation2LearningData(String inputFile,
			String outputFile) {

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		Map<String, String> nodes = new HashMap<String, String>();
		Map<String, String> invNodes = new HashMap<String, String>();
		List<String> txtNodes = new ArrayList<String>();
		List<String> hypNodes = new ArrayList<String>();
		List<Relation> relations = new ArrayList<Relation>();
		Set<String> usedPairs = new HashSet<String>();

		ArrayList<String> examples = new ArrayList<String>();

		int i = 0;
		int n = arrLines.size();

		String prefix = "";

		int cpair = 0;

		while (i < n) {

			String line = arrLines.get(i);

			if (line.startsWith("<view name=")) {

			}

			if (line.startsWith("<txt-annotation>")) {
				prefix = "t_";
			}

			if (line.startsWith("<hyp-annotation>")) {
				prefix = "h_";
			}

			if (line.startsWith("<node id=")) {
				String nid = line.substring(line.indexOf('"') + 1, line
						.lastIndexOf('"'));
				i += 1;
				String nextLine = arrLines.get(i).trim();
				nodes.put(prefix + nid, nextLine);
				// System.out.println("id: " + prefix+ nid);
				invNodes.put(prefix + nextLine, prefix + nid);
				if (prefix.equals("t_")) {
					txtNodes.add(nextLine);
					// System.out.println("Add to txtNode: " + nextLine);
				} else {
					hypNodes.add(nextLine);
					// System.out.println("Add to hypNode: " + nextLine);
				}
			}

			if (line.startsWith("</node>")) {

			}

			if (line.startsWith("<edge from=")) {
				int pos = -1;
				int fromPos = line.indexOf("from");
				pos = line.indexOf('"', fromPos);
				String fromid = line.substring(pos + 1, line.indexOf('"',
						pos + 1));
				int toPos = line.indexOf("to");
				pos = line.indexOf('"', toPos);
				String toid = line.substring(pos + 1, line
						.indexOf('"', pos + 1));
				int scorePos = line.indexOf("score");
				pos = line.indexOf('"', scorePos);
				String scoreid = line.substring(pos + 1, line.indexOf('"',
						pos + 1));
				Relation rel = new Relation();
				rel.source = fromid;
				rel.target = toid;
				rel.relation = scoreid;
				relations.add(rel);
				usedPairs.add("t_" + fromid + "____" + "h_" + toid);
			}

			if (line.startsWith("</edge>")) {

			}

			if (line.startsWith("</view>")) {
				for (Relation rel : relations) {
					String from = "";
					String to = "";
					String relation = rel.relation;
					String relCode = "";
					if (nodes.containsKey("t_" + rel.source))
						from = nodes.get("t_" + rel.source);
					else
						from = nodes.get("h_" + rel.source);
					if (nodes.containsKey("h_" + rel.target))
						to = nodes.get("h_" + rel.target);
					else
						to = nodes.get("t_" + rel.target);
					if (relation.equals("ancestor-e1-to-e2-class"))
						relCode = "1";
					else if (relation.equals("ancestor-e2-to-e1-class"))
						relCode = "2";
					else if (relation.equals("sibling-class"))
						relCode = "3";
					else {
						System.out.println("ERROR: Wrong relation class!");
						System.exit(1);
					}
					examples.add(relCode + "\t" + "_|_" + "\t" + from + "\t"
							+ to);
				}
				relations = new ArrayList<Relation>();
				nodes = new HashMap<String, String>();

				System.out.println();
				for (String s : usedPairs)
					System.out.print(s + ", ");
				System.out.println();

				for (String t : txtNodes) {
					String tid = invNodes.get("t_" + t);
					for (String h : hypNodes) {
						if (t.equals(h)) {
							System.out.println("hihi: equal " + t + " " + h);
							continue;
						}
						String hid = invNodes.get("h_" + h);
						String s = tid + "____" + hid;
						System.out.println("hehe: " + s);
						if (!usedPairs.contains(s)) {
							examples.add("0" + "\t" + "_|_" + "\t" + t + "\t"
									+ h);
						}
					}
				}

				nodes = new HashMap<String, String>();
				invNodes = new HashMap<String, String>();
				txtNodes = new ArrayList<String>();
				hypNodes = new ArrayList<String>();
				relations = new ArrayList<Relation>();
				usedPairs = new HashSet<String>();
				cpair++;
				System.out.println(cpair);
			}

			i++;
		}

		IOManager.writeLinesAddingReturn(examples, outputFile);
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("ERROR: 2 arguments required.");
			System.exit(1);
		}
		Converter.convertXMLAnnotation2LearningData(args[0], args[1]);
	}
}
