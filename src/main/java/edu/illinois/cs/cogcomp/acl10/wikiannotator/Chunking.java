/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.wikiannotator;

import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

/**
 * @author dxquang Jun 25, 2009
 */
public class Chunking {

	public class TagWord {

		public String tag;
		public String word;

		/**
		 * 
		 */
		public TagWord(String tag, String word) {
			this.tag = tag;
			this.word = word;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String s = "";
			s = "[" + tag + " " + word + "]";
			return s;
		}
	}

	public static Chunker chunker = null;

	public TagWord[] chunkSentenceTagWord(String sentence) {
		sentence = sentence.trim();

		if (sentence.length() == 0)
			return null;

		return chunkSentenceTagWord(new Sentence(sentence));
	}

	public TagWord[] chunkSentenceTagWord(Sentence sentence) {

		if (chunker == null)
			chunker = new Chunker();

		int size = sentence.wordSplit().size();
		String[] tags = new String[size];
		String[] words = new String[size];

		Word w = (Word) sentence.wordSplit().get(0);
		Token t = new Token(w, null, null);

		int position = 0;
		for (w = (Word) w.next; w != null; w = (Word) w.next) {
			t.next = new Token(w, t, null);

			words[position] = t.form;
			tags[position++] = chunker.discreteValue(t);

			t = (Token) t.next;

		}

		if (t != null) {
			t.next = null;
			words[position] = t.form;
			tags[position] = chunker.discreteValue(t);
		}

		TagWord[] tagwords = new TagWord[position + 1];
		for (int i = 0; i <= position; i++) {
			TagWord tw = new TagWord(tags[i], words[i]);
			tagwords[i] = tw;
		}

		return tagwords;
	}

	public void chunkingTagWord(String sent) {

		TagWord[] chunks = chunkSentenceTagWord(sent);

		if (chunks != null) {

			for (TagWord tw : chunks) {
				System.out.print("[" + tw.tag + " " + tw.word + "] ");
			}
		}

	}

	public String[] chunkSentence(String sentence) {
		sentence = sentence.trim();

		if (sentence.length() == 0)
			return null;

		return chunkSentence(new Sentence(sentence));
	}

	public String[] chunkSentence(Sentence sentence) {

		if (chunker == null)
			chunker = new Chunker();

		String[] tags = new String[sentence.wordSplit().size()];

		Word w = (Word) sentence.wordSplit().get(0);
		Token t = new Token(w, null, null);

		int position = 0;
		for (w = (Word) w.next; w != null; w = (Word) w.next) {
			t.next = new Token(w, t, null);

			tags[position++] = chunker.discreteValue(t);

			t = (Token) t.next;

		}

		if (t != null)
			tags[position] = chunker.discreteValue(t);

		return tags;
	}

	public void chunking(String sent) {

		String[] chunks = chunkSentence(sent);

		if (chunks != null) {

			for (String c : chunks) {
				System.out.print(c + " ");
			}
		}

	}

	public static void main(String[] args) {
		Chunking chunk = new Chunking();

		chunk
				.chunkingTagWord("I want to become a president of the United States of America when I come to the United States of America.");
	}

}
