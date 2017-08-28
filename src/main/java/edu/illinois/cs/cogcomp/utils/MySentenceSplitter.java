package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;

public class MySentenceSplitter {

	public MySentenceSplitter() {
	}

	// public void splitText(String inputFile, String outputFile) {
	// SentParDetector sentSplitter = new SentParDetector();
	// String content = IOManager.readContentAddingPeriod(inputFile);
	// String splittedContent = sentSplitter.markupRawText(0, content);
	// IOManager.writeContent(splittedContent, outputFile);
	// }

	public void splitTextLBJ(String inputFile, String outputFile) {
		SentenceSplitter senSplit = new SentenceSplitter(inputFile);
		Sentence allSentences[] = senSplit.splitAll();
		StringBuffer sBuf = new StringBuffer("");
		for (Sentence sentence : allSentences) {
			sBuf.append(sentence.text + "\n");
		}
		IOManager.writeContent(sBuf.toString(), outputFile);
	}

	public ArrayList<String> splitTextLBJ(String content) {
		ArrayList<String> arrSentences = new ArrayList<String>();
		String text[] = content.split("\n");
		if (text.length < 5)
			return arrSentences;
		// System.out.println("text.length=" + text.length);
		SentenceSplitter senSplit = new SentenceSplitter(text);
		Sentence allSentences[] = senSplit.splitAll();
		for (Sentence sentence : allSentences) {
			arrSentences.add(sentence.text);
		}
		return arrSentences;
	}
}
