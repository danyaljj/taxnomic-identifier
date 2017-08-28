/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Jun 15, 2010
 */
public class RTEGenerator {

	private static final String RTE_TEXT = "Text";
	private static final String RTE_HYPOTHESIS = "Hypothesis";

	Map<String, Set<String>> mapClassNames = null;
	Map<String, Set<String>> mapPascaData = null;
	Map<String, List<String>> mapRteData = null;

	Map<String, List<String>> mapClassNameList = null;

	CuratorClient client = null;

	WordNetManager wnManager = null;
	private static final String wordNetPropertiesFile = "file_properties.xml";

	/**
	 * 
	 */
	public RTEGenerator() {
		mapClassNames = new HashMap<String, Set<String>>();
		mapPascaData = new HashMap<String, Set<String>>();
		mapRteData = new HashMap<String, List<String>>();
		mapClassNameList = new HashMap<String, List<String>>();

		client = new CuratorClient("grandpa.cs.uiuc.edu", 9090);
		wnManager = new WordNetManager(wordNetPropertiesFile);
	}

	private void readClasses(String classFile) {
		ArrayList<String> lines = IOManager.readLines(classFile);

		for (String s : lines) {
			if (s.length() == 0)
				continue;
			String[] splits = s.split("\\t+");
			if (splits.length != 2)
				continue;
			String className = splits[0];
			String[] extensions = splits[1].split(",");
			Set<String> set = new HashSet<String>();
			List<String> list = new ArrayList<String>();
			for (String e : extensions) {
				e = e.trim().toLowerCase();
				set.add(e);
				list.add(e);
			}
			mapClassNames.put(className, set);
			mapClassNameList.put(className, list);
		}
	}

	private void readPascaData(String pascaFile) {
		ArrayList<String> lines = IOManager.readLines(pascaFile);
		for (String s : lines) {
			String[] splits = s.split("\\t+");
			if (splits.length != 2)
				continue;
			String mem = splits[0].toLowerCase();
			String cla = splits[1].toLowerCase();
			if (mapPascaData.containsKey(cla)) {
				Set<String> members = mapPascaData.get(cla);
				members.add(mem);
				mapPascaData.put(cla, members);
			} else {
				Set<String> members = new HashSet<String>();
				members.add(mem);
				mapPascaData.put(cla, members);
			}
		}
	}

	private void readRteData(String rteFile) {

		ArrayList<String> lines = IOManager.readLines(rteFile);

		List<String> texts = new ArrayList<String>();
		List<String> hypos = new ArrayList<String>();

		for (String s : lines) {
			if (s.startsWith("<t>")) {
				int pos = s.indexOf("</t>");
				String t = s.substring(3, pos).trim();
				texts.add(t);
			} else if (s.startsWith("<h>")) {
				int pos = s.indexOf("</h>");
				String h = s.substring(3, pos).trim();
				hypos.add(h);
			}
		}

		if (texts.size() != hypos.size()) {
			System.out
					.println("ERROR: The numbers of texts and hypotheses are not equal!");
			System.exit(1);
		}

		mapRteData.put(RTE_TEXT, texts);
		mapRteData.put(RTE_HYPOTHESIS, hypos);
	}

	public void generateHypotheses(String rteFile, String pascaFile,
			String classFile) {
		readRteData(rteFile);
		readPascaData(pascaFile);
		readClasses(classFile);

		List<String> hypotheses = mapRteData.get(RTE_HYPOTHESIS);

		String[] usefulArgs = new String[] { "A0", "A1", "A2", "A3", "A4", "A5" };
		Set<String> setUsefulArgs = new HashSet<String>();
		for (String arg : usefulArgs)
			setUsefulArgs.add(arg);

		for (String hypo : hypotheses) {
			TextAnnotation ta = getAnnotation(hypo);

			TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
			System.out.println(posView);

			SpanLabelView chunkView = (SpanLabelView) ta
					.getView(ViewNames.SHALLOW_PARSE);
			// System.out.println(chunkView);

			PredicateArgumentView srlView = (PredicateArgumentView) ta
					.getView(ViewNames.SRL);

			List<Constituent> preds = srlView.getPredicates();
			for (Constituent p : preds) {
				// System.out.println(p);
				List<Relation> relations = srlView.getArguments(p);
				for (Relation r : relations) {
					// System.out.println(r.getSource());
					// System.out.println(r.getTarget());
					// System.out.println(r.getRelationName());

					Constituent arg = r.getTarget();
					String relName = r.getRelationName();
					if (setUsefulArgs.contains(relName)) {
						List<Constituent> phrases = chunkView
								.getConstituentsCovering(arg);
						// System.out.println(phrases.size());
						// System.out.println(phrases);
						for (Constituent ph : phrases) {
							if (ph.getLabel().equals("NP")) {
								String[] tokens = ta.getTokensInSpan(ph
										.getStartSpan(), ph.getEndSpan());

								// String[] poses = new String[tokens.length];
								// int iter = 0;
								// for (int tokenId = ph.getStartSpan(); tokenId
								// < ph
								// .getEndSpan(); tokenId++) {
								// poses[iter++] = WordFeatures.getPOS(ta,
								// tokenId);
								// }

								List<String> poses = posView
										.getLabelsCoveringSpan(ph
												.getStartSpan(), ph
												.getEndSpan());
								// System.out.println(poses);

								String[] lemmaTokens = lemmatizeTokens(tokens,
										poses);
								for (int i = 0; i < lemmaTokens.length; i++) {
									lemmaTokens[i] = lemmaTokens[i].replaceAll(
											"\\s+", "-");
								}

								int n = lemmaTokens.length;
								List<List<String>> listTokens = new ArrayList<List<String>>();
								for (int i = 0; i < n; i++) {
									String s1 = lemmaTokens[i];
									String s2 = (i < n - 1 ? (lemmaTokens[i]
											+ " " + lemmaTokens[i + 1])
											.toLowerCase() : (""));
									String s3 = (i < n - 2 ? (lemmaTokens[i]
											+ " " + lemmaTokens[i + 1] + " " + lemmaTokens[i + 2])
											.toLowerCase()
											: (""));
									String s = "";
									if (isInPascaData(s3) == true) {
										s = s3;
									} else if (isInPascaData(s2) == true) {
										s = s2;
									} else if (isInPascaData(s1) == true) {
										s = s1;
									}
									if (s.length() > 0) {
										List<Pair<String, String>> replacements = getReplacements(s);
										if (replacements != null) {
											System.out
													.println("-Replace: " + s);
											System.out.println(replacements);
											List<String> ts = new ArrayList<String>();
											int nn = replacements.size();
											for (int x = 0; x < nn; x++) {
												Pair<String, String> rep = replacements
														.get(x);
												ts.add((rep.getFirst().equals(
														"parent") ? "!" : "~")
														+ rep.getSecond());
											}
											listTokens.add(ts);
											// Exit the loop : only replace at
											// most 1 place in an argument
											for (int j = i + 1; j < n; j++) {
												List<String> tt = new ArrayList<String>();
												tt.add(tokens[i]);
												listTokens.add(tt);
											}
											break;
										}
										i += (s.split("\\s+").length);
									} else {
										List<String> ts = new ArrayList<String>();
										ts.add(tokens[i]);
										listTokens.add(ts);
									}
								}

								int len = 1;
								for (int v = 0; v < listTokens.size(); v++) {
									if (listTokens.get(v).size() > 1) {
										len = listTokens.get(v).size();
										break;
									}
								}

								for (int v = 0; v < listTokens.size(); v++) {
									List<String> ts = listTokens.get(v);
									int l = ts.size();
									if (l < len) {
										for (int w = l; w < len; w++) {
											ts.add(ts.get(0));
										}
									}
								}

								for (int v = 0; v < len; v++) {
									String s = "";
									for (int w = 0; w < listTokens.size(); w++) {
										s += listTokens.get(w).get(v) + " ";
									}
									System.out.println(s);
								}
							}
						}
					}
				}
			}

		}
	}

	/**
	 * @param tokens
	 * @param poses
	 * @return
	 */
	private String[] lemmatizeTokens(String[] tokens, List<String> poses) {
		int n = tokens.length;
		String[] lemmas = new String[n];
		try {
			for (int i = 0; i < n; i++) {
				String tok = tokens[i];
				String pos = poses.get(i);
				String lemma = "";
				if (pos.startsWith("N")) {
					lemma = wnManager.getLemma(tok, POS.NOUN);
				} else if (pos.startsWith("V")) {
					lemma = wnManager.getLemma(tok, POS.VERB);
				} else {
					lemma = tok;
				}
				lemmas[i] = lemma;
			}
			return lemmas;
		} catch (JWNLException e) {
			e.printStackTrace();
			System.out.println("ERROR: Unable to get word's lemma.");
		}
		return null;
	}

	/**
	 * @param s
	 * @return
	 */
	private List<Pair<String, String>> getReplacements(String s) {
		List<Pair<String, String>> replacements = new ArrayList<Pair<String, String>>();
		for (String c : mapPascaData.keySet()) {
			Set<String> classes = mapClassNames.get(c);
			if (classes.contains(s)) {
				Set<String> siblings = mapPascaData.get(c);
				int i = 0;
				for (String sib : siblings) {
					if (sib.equals(s))
						continue;
					replacements.add(new Pair<String, String>("sibling", sib));
					i++;
					if (i == 3)
						return replacements;
				}
			} else {
				Set<String> siblings = mapPascaData.get(c);
				if (siblings.contains(s)) {
					String classname = mapClassNameList.get(c).get(0);
					replacements.add(new Pair<String, String>("parent",
							classname));
					int i = 0;
					for (String sib : siblings) {
						if (sib.equals(s))
							continue;
						replacements.add(new Pair<String, String>("sibling",
								sib));
						i++;
						if (i == 2)
							return replacements;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param s
	 * @return
	 */
	private Set<String> getSemanticClassMembers(String s) {
		Map<String, Set<String>> mapResults = new HashMap<String, Set<String>>();
		for (String c : mapPascaData.keySet()) {
			Set<String> semClassList = mapClassNames.get(c);
			if (semClassList.contains(s)) {
				return mapPascaData.get(s);
			}

		}
		return null;
	}

	private boolean isInPascaData(String s) {
		for (String c : mapPascaData.keySet()) {
			Set<String> semClassList = mapClassNames.get(c);
			if (semClassList.contains(s)) {
				return true;
			}

			Set<String> siblings = mapPascaData.get(c);
			if (siblings.contains(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param hypo
	 */
	private TextAnnotation getAnnotation(String hypo) {
		TextAnnotation ta = null;
		try {
			ta = client.getTextAnnotation("", "", hypo, false);
			client.addPOSView(ta, false);
			client.addChunkView(ta, false);
			client.addSRLView(ta, false);
		} catch (ServiceUnavailableException e) {
			e.printStackTrace();
		} catch (AnnotationFailedException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
		if (ta == null) {
			System.exit(1);
		}
		return ta;
	}

	public static void main(String[] args) {
		String rteFile = "/Users/dxquang/tmp/jupiter/annotated_test.xml";
		String pascaFile = "/Users/dxquang/tmp/jupiter/www07-classes.txt";
		String classFile = "/Users/dxquang/tmp/jupiter/class-cluster-reduced.txt";
		RTEGenerator generator = new RTEGenerator();
		generator.generateHypotheses(rteFile, pascaFile, classFile);
	}
}
