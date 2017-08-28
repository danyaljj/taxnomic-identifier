package edu.illinois.cs.cogcomp.relation;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.retrieval.ARetriever;
import edu.illinois.cs.cogcomp.retrieval.DocumentXML;
import edu.illinois.cs.cogcomp.retrieval.SimpleRetriever;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.MySentenceSplitter;
import edu.illinois.cs.cogcomp.utils.WebPage;

/**
 * 
 * @author dxquang
 * Feb 2, 2009
 */

public class MakeSimpleCorpus extends AMakeCorpus {

	ARetriever retriever = null;
	WebPage webPage = null;
	int numResult;
	String outputDir;

	public MakeSimpleCorpus(String outputDir, int numResult) {

		this.retriever = new SimpleRetriever();
		this.numResult = numResult;
		this.outputDir = outputDir;
		this.webPage = new WebPage();

		if (!IOManager.isDirectoryExist(outputDir))
			IOManager.createDirectory(outputDir);
	}

	@Override
	public void makeCorpus(ArrayList<Prototype> arrPrototypes) {

		System.out.println("Retrieving pages with entities.");

		retrievePages(arrPrototypes);

		System.out.println("Finish retrieving pages");

		System.out.println("Downloading pages");

		downloadPages(arrPrototypes);

		System.out.println("Finish downloading pages.");
	}

	private void downloadPages(ArrayList<Prototype> arrPrototypes) {

		int i = 1;
		int n = arrPrototypes.size();

		WebPage webPage = new WebPage();
		MySentenceSplitter splitter = new MySentenceSplitter();

		for (Prototype prototype : arrPrototypes) {

			System.out.println(i + "/" + n + ". entity1=" + prototype.entity1
					+ " - entity2=" + prototype.entity2 + " - relation="
					+ prototype.relation);

			String fileName;

			fileName = retriever.makeFileName(prototype.entity1,
					prototype.entity2);

			if (!IOManager.isFileExist(outputDir + "/" + fileName + ".xml")) {
				System.out.println("*Unable to open file " + outputDir + "/"
						+ fileName + ".xml");
				i++;
				continue;
			}

			if (!IOManager.isFileExist(outputDir + "/" + fileName + ".exa")) {
				ArrayList<DocumentXML> arrDocs = retriever.importXML(outputDir
						+ "/" + fileName + ".xml");
				System.out.println("arrDocs.size()=" + arrDocs.size());
				ArrayList<String> arrExamples = new ArrayList<String>();
				int j = 1;
				int m = arrDocs.size();
				for (DocumentXML doc : arrDocs) {

					String url = doc.url;
					url = url.replaceAll("\\s+", "");
					System.out.println("\t" + j + "/" + m + ". url=" + url);

					String content = webPage.parseWebPage(url);

					if (content == null) {
						System.out.println("\tContent=null, url=" + url);
						j++;
						continue;
					}

					ArrayList<String> arrSentences = splitter
							.splitTextLBJ(content);
					if (arrSentences.size() < 5) {
						System.out
								.println("\tContent has less than 5 sentences, url = "
										+ url);
						j++;
						continue;
					}

					String lowerEntity1 = prototype.entity1.toLowerCase();
					lowerEntity1 = formatText(lowerEntity1);
					String eTokens1[] = lowerEntity1.split("\\s+");

					String lowerEntity2 = prototype.entity2.toLowerCase();
					lowerEntity2 = formatText(lowerEntity2);
					String eTokens2[] = lowerEntity2.split("\\s+");

					if (lowerEntity1.length() == 0
							|| lowerEntity2.length() == 0) {
						j++;
						System.out.println("Empty entities: entity1="
								+ lowerEntity1 + ", entity2=" + lowerEntity2);
						continue;
					}

					for (String sentence : arrSentences) {

						sentence = sentence.toLowerCase();

						String sentences[] = sentence.split("\n");

						for (String sent : sentences) {

							sent = formatText(sent);

							// System.out.println("\tentity1=" + lowerEntity1 +
							// ", entity2=" + lowerEntity2);

							String tokens[] = sent.split("\\s+");

							ArrayList<Integer> arrPosE1 = containsEntity(
									tokens, eTokens1);
							ArrayList<Integer> arrPosE2 = containsEntity(
									tokens, eTokens2);
							if (arrPosE1.size() > 0 && arrPosE2.size() > 0) {
								int dis = minDistance(arrPosE1,
										eTokens1.length, arrPosE2,
										eTokens2.length);
								if (dis <= MAXIMUM_DISTANCE) {
									arrExamples.add(sent);

									// System.out.println("\t*sent=" + sent);
									// System.out
									// .println("\tTrue. Adding to the list.");
								}
							}
							// else {
							// System.out.println("\tFlase.");
							// }
						}
					}
					j++;
				}
				System.out.println("\tWriting to file " + outputDir + "/"
						+ fileName + ".exa");
				IOManager.writeLinesAddingReturn(arrExamples, outputDir + "/"
						+ fileName + ".exa");
			}
			i++;
		}
	}

	private int minDistance(ArrayList<Integer> arrPosE1, int eLen1,
			ArrayList<Integer> arrPosE2, int eLen2) {

		int minDis = 1000000;

		for (Integer e1 : arrPosE1) {
			for (Integer e2 : arrPosE2) {
				int dis = e1.intValue() > e2.intValue() ? e1.intValue()
						- (e2.intValue() + eLen2) : e2.intValue()
						- (e1.intValue() + eLen1);
				if (dis < minDis)
					minDis = dis;
			}
		}
		return minDis;
	}

	private String formatText(String inputString) {
		inputString = inputString.replaceAll("\\p{Punct}", "");
		inputString = inputString.replaceAll("\\s+", " ");
		return inputString;
	}

	private ArrayList<Integer> containsEntity(String[] tokens, String[] eTokens) {

		ArrayList<Integer> arrPos = new ArrayList<Integer>();
		int m = eTokens.length;
		int j = 0;
		int pos = -1;
		int i = 0;
		String eToken = eTokens[j];
		for (String token : tokens) {
			if (token.equals(eToken)) {
				if (j == 0)
					pos = i;
				j++;
				if (j == m) {
					arrPos.add(new Integer(pos));
					j = 0;
					eToken = eTokens[j];
				} else
					eToken = eTokens[j];
			}
			i++;
		}
		return arrPos;
	}

	private void retrievePages(ArrayList<Prototype> arrPrototypes) {

		int i = 1;
		int n = arrPrototypes.size();

		for (Prototype prototype : arrPrototypes) {

			System.out.println(i + "/" + n + ". entity1=" + prototype.entity1
					+ " - entity2=" + prototype.entity2 + " - relation="
					+ prototype.relation);

			String fileName;

			/*
			 * fileName = retriever.makeFileName(prototype.entity1); if
			 * (!IOManager.isFileExist(outputDir + "/" + fileName + ".xml")) {
			 * String queryEntity1 = retriever.makeQuery(prototype.entity1);
			 * retriever.retrieve(queryEntity1, numResult);
			 * retriever.exportXML(outputDir + "/" + fileName + ".xml"); }
			 * 
			 * fileName = retriever.makeFileName(prototype.entity2); if
			 * (!IOManager.isFileExist(outputDir + "/" + fileName + ".xml")) {
			 * String queryEntity2 = retriever.makeQuery(prototype.entity2);
			 * retriever.retrieve(queryEntity2, numResult);
			 * retriever.exportXML(outputDir + "/" + fileName + ".xml"); }
			 */

			fileName = retriever.makeFileName(prototype.entity1,
					prototype.entity2);
			if (!IOManager.isFileExist(outputDir + "/" + fileName + ".xml")) {
				String queryEntities12 = retriever.makeQuery(prototype.entity1,
						prototype.entity2);
				retriever.retrieve(queryEntities12, numResult);
				retriever.exportXML(outputDir + "/" + fileName + ".xml");
			}
			i++;
		}
	}

	@Override
	public ArrayList<Prototype> parsePrototype(String inputPrototypeFile) {
		ArrayList<String> arrLines = IOManager.readLines(inputPrototypeFile);
		ArrayList<Prototype> arrPrototypes = new ArrayList<Prototype>();
		for (String line : arrLines) {
			String parts[] = line.split("\\t+");
			if (parts.length != 4) {
				System.out.println("Ignoring line: " + line);
				continue;
			} else {
				Prototype prototype = new Prototype(parts[1], parts[2],
						parts[0]);
				arrPrototypes.add(prototype);
			}
		}
		return arrPrototypes;
	}

}
