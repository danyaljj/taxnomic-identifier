package edu.illinois.cs.cogcomp.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetManager {

	// Dictionary object
	public Dictionary wordnet;

	// Initialize the database!
	public WordNetManager(String propsFile) {
		try {
			JWNL.initialize(new FileInputStream(propsFile));
		} catch (FileNotFoundException e) {
			try {
				JWNL.initialize(new FileInputStream("conf/" + propsFile));
			} catch (Exception e1) {
				e.printStackTrace();
				e1.printStackTrace();
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		// Create dictionary object
		wordnet = Dictionary.getInstance();
	}

	// Return array of POS objects for a given String
	public POS[] getPOS(String s) throws JWNLException {
		// Look up all IndexWords (an IndexWord can only be one POS)
		IndexWordSet set = wordnet.lookupAllIndexWords(s);
		// Turn it into an array of IndexWords
		IndexWord[] words = set.getIndexWordArray();
		// Make the array of POS
		POS[] pos = new POS[words.length];
		for (int i = 0; i < words.length; i++) {
			pos[i] = words[i].getPOS();
		}
		return pos;
	}

	// Just gets the related words for first sense of a word
	// Revised to get the list of related words for the 1st Synset that has them
	// We might want to try all of them
	public ArrayList<Synset> getRelated(IndexWord word, PointerType type)
			throws JWNLException {
		try {
			Synset[] senses = word.getSenses();
			// Look for the related words for all Senses
			for (int i = 0; i < senses.length; i++) {
				ArrayList<Synset> a = getRelated(senses[i], type);
				// If we find some, return them
				if (a != null && !a.isEmpty()) {
					return a;
				}
			}
		} catch (NullPointerException e) {
			// System.out.println("Oops, NULL problem: " + e);
		}
		return null;
	}

	// Related words for a given sense (do synonyms by default)
	// Probably should implement all PointerTypes
	public ArrayList<Synset> getRelated(Synset sense, PointerType type)
			throws JWNLException, NullPointerException {
		PointerTargetNodeList relatedList;
		// Call a different function based on what type of relationship you are
		// looking for
		if (type == PointerType.HYPERNYM) {
			relatedList = PointerUtils.getInstance().getDirectHypernyms(sense);
		} else if (type == PointerType.HYPONYM) {
			relatedList = PointerUtils.getInstance().getDirectHyponyms(sense);
		} else {
			relatedList = PointerUtils.getInstance().getSynonyms(sense);
		}
		// Iterate through the related list and make an ArrayList of Synsets to
		// send back
		Iterator<PointerTargetNode> i = relatedList.iterator();
		ArrayList<Synset> a = new ArrayList<Synset>();
		while (i.hasNext()) {
			PointerTargetNode related = (PointerTargetNode) i.next();
			Synset s = related.getSynset();
			a.add(s);
		}
		return a;
	}

	// Just shows the Tree of related words for first sense
	// We may someday want to the Tree for all senses
	public void showRelatedTree(IndexWord word, int depth, PointerType type)
			throws JWNLException {
		showRelatedTree(word.getSense(1), depth, type);
	}

	public void showRelatedTree(Synset sense, int depth, PointerType type)
			throws JWNLException {
		PointerTargetTree relatedTree;
		// Call a different function based on what type of relationship you are
		// looking for
		if (type == PointerType.HYPERNYM) {
			relatedTree = PointerUtils.getInstance().getHypernymTree(sense,
					depth);
		} else if (type == PointerType.HYPONYM) {
			relatedTree = PointerUtils.getInstance().getHyponymTree(sense,
					depth);
		} else {
			relatedTree = PointerUtils.getInstance().getSynonymTree(sense,
					depth);
		}
		// If we really need this info, we wil have to write some code to
		// Process the tree
		// Not just display it
		relatedTree.print();
	}

	// This method looks for any possible relationship
	public Relationship getRelationship(IndexWord start, IndexWord end,
			PointerType type) throws JWNLException {
		// All the start senses
		Synset[] startSenses = start.getSenses();
		// All the end senses
		Synset[] endSenses = end.getSenses();
		// Check all against each other to find a relationship
		for (int i = 0; i < startSenses.length; i++) {
			for (int j = 0; j < endSenses.length; j++) {
				RelationshipList list = RelationshipFinder.getInstance()
						.findRelationships(startSenses[i], endSenses[j], type);
				if (!list.isEmpty()) {
					return (Relationship) list.get(0);
				}
			}
		}
		return null;
	}

	// If you have a relationship, this function will create an ArrayList of
	// Synsets
	// that make up that relationship
	public ArrayList<Synset> getRelationshipSenses(Relationship rel)
			throws JWNLException {
		ArrayList<Synset> a = new ArrayList<Synset>();
		PointerTargetNodeList nodelist = rel.getNodeList();
		Iterator<PointerTargetNode> i = nodelist.iterator();
		while (i.hasNext()) {
			PointerTargetNode related = (PointerTargetNode) i.next();
			a.add(related.getSynset());
		}
		return a;
	}

	// Get the IndexWord object for a String and POS
	public IndexWord getWord(POS pos, String s) throws JWNLException {
		// IndexWord word = wordnet.getIndexWord(pos,s);
		IndexWord word = wordnet.lookupIndexWord(pos, s); // This function tries
		// the stemmed form
		// of the lemma
		return word;
	}

	/*
	 * Quang added functions from here
	 */

	// =============
	public ArrayList<String> getSynonymForAllSenses(IndexWord indexWord)
			throws JWNLException {
		ArrayList<String> arrSynonym = new ArrayList<String>();
		HashSet<String> hsWords = new HashSet<String>();
		Synset[] synsets = indexWord.getSenses();
		if (synsets != null) {
			for (int i = 0; i < synsets.length; i++) {
				Synset synset = synsets[i];
				Word[] words = synset.getWords();
				for (int j = 0; j < words.length; j++) {
					Word word = words[j];
					hsWords.add(word.getLemma());
				}
			}
		}
		arrSynonym.addAll(hsWords);
		return arrSynonym;
	}

	// ==============
	public ArrayList<String> getAllSynonym(String inputWord)
			throws JWNLException {
		ArrayList<String> arrSynonym = new ArrayList<String>();
		HashSet<String> hsWords = new HashSet<String>();
		POS[] poses = { POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB };
		for (int i = 0; i < poses.length; i++) {
			POS pos = poses[i];
			IndexWord indexWord = getWord(pos, inputWord);
			if (indexWord != null) {
				ArrayList<String> synonyms = getSynonymForAllSenses(indexWord);
				hsWords.addAll(synonyms);
			}
		}
		arrSynonym.addAll(hsWords);
		return arrSynonym;
	}

	// =============
	public ArrayList<String> getHypernymForAllSenses(IndexWord indexWord)
			throws JWNLException {
		ArrayList<String> arrHypernyms = new ArrayList<String>();
		HashSet<String> hsWords = new HashSet<String>();
		ArrayList<Synset> arrSynsets = getRelatedSynset(indexWord,
				PointerType.HYPERNYM);
		int n = arrSynsets.size();
		for (int i = 0; i < n; i++) {
			Synset synset = arrSynsets.get(i);
			Word[] words = synset.getWords();
			for (int j = 0; j < words.length; j++) {
				Word word = words[j];
				hsWords.add(word.getLemma());
			}
		}
		arrHypernyms.addAll(hsWords);
		return arrHypernyms;
	}

	// =============
	public ArrayList<String> getHypernymForNoun(String word, int maxLevelUp)
			throws JWNLException {

		ArrayList<String> arrHypernyms = new ArrayList<String>();

		ArrayList<String> arrCurWords = new ArrayList<String>();
		arrCurWords.add(word);

		int level = 0;

		while (level < maxLevelUp) {

			ArrayList<String> arrCurLevelWords = new ArrayList<String>();

			for (String curWord : arrCurWords) {

				IndexWord indexWord = getWord(POS.NOUN, curWord);
				System.out.println("indexWord of " + curWord + " is "
						+ indexWord);
				if (indexWord != null) {

					ArrayList<String> arrOutHypernyms = getHypernymForAllSenses(indexWord);

					if (arrOutHypernyms.size() > 0) {
						arrHypernyms.addAll(arrOutHypernyms);
						arrCurLevelWords.addAll(arrOutHypernyms);
					}

				}

			}

			arrCurWords = null;
			arrCurWords = new ArrayList<String>(arrCurLevelWords);

			level++;
		}

		return arrHypernyms;

	}

	// =============
	public ArrayList<String> getHyponymForAllSenses(IndexWord indexWord)
			throws JWNLException {
		ArrayList<String> arrHyponyms = new ArrayList<String>();
		HashSet<String> hsWords = new HashSet<String>();
		ArrayList<Synset> arrSynsets = getRelatedSynset(indexWord,
				PointerType.HYPONYM);
		int n = arrSynsets.size();
		for (int i = 0; i < n; i++) {
			Synset synset = arrSynsets.get(i);
			Word[] words = synset.getWords();
			for (int j = 0; j < words.length; j++) {
				Word word = words[j];
				hsWords.add(word.getLemma());
			}
		}
		arrHyponyms.addAll(hsWords);
		return arrHyponyms;
	}

	// =============
	private ArrayList<Synset> getRelatedSynset(IndexWord indexWord,
			PointerType relationType) throws JWNLException {
		Synset[] synsets = indexWord.getSenses();
		ArrayList<Synset> arrAllSynsets = new ArrayList<Synset>();
		for (int i = 0; i < synsets.length; i++) {
			ArrayList<Synset> arrSynsets = getRelated(synsets[i], relationType);
			if (arrSynsets != null && !arrSynsets.isEmpty()) {
				arrAllSynsets.addAll(arrSynsets);
			}
		}
		return arrAllSynsets;
	}

	// =============
	public ArrayList<String> getAllHypernym(String lexicalForm)
			throws JWNLException {
		return new ArrayList<String>(lookupWordsFollowingPointer(lexicalForm,
				PointerType.HYPERNYM));
	}

	// =============
	public ArrayList<String> getAllHyponym(String lexicalForm)
			throws JWNLException {
		return new ArrayList<String>(lookupWordsFollowingPointer(lexicalForm,
				PointerType.HYPONYM));
	}

	// =============
	private HashSet<String> lookupWordsFollowingPointer(String lexicalForm,
			PointerType pointerType) throws JWNLException {
		HashSet<String> relatedWords = new HashSet<String>();
		POS[] poses = { POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB };
		for (POS pos : poses) {
			IndexWord indexWord = getWord(pos, lexicalForm);
			if (indexWord == null)
				return relatedWords;
			Synset[] synSets = indexWord.getSenses();
			for (Synset synset : synSets) {
				PointerTarget[] targets = synset.getTargets(pointerType);
				if (targets != null) {
					for (PointerTarget target : targets) {
						Word[] words = ((Synset) target).getWords();
						for (Word word : words) {
							relatedWords.add(word.getLemma());
						}
					}
				}
			}
		}
		return relatedWords;
	}

	// =============
	public ArrayList<String> getAllMorph(String lexicalForm)
			throws JWNLException {
		ArrayList<String> arrMorph = new ArrayList<String>();
		HashSet<String> hsMorph = new HashSet<String>();
		POS[] poses = { POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB };
		for (POS pos : poses) {
			ArrayList<String> morphs = getMorph(pos, lexicalForm);
			hsMorph.addAll(morphs);
		}
		arrMorph.addAll(hsMorph);
		return arrMorph;
	}

	// =============
	public ArrayList<String> getMorph(POS pos, String lexicalForm)
			throws JWNLException {
		HashSet<String> forms = new HashSet<String>();
		List baseForms = wordnet.getMorphologicalProcessor()
				.lookupAllBaseForms(pos, lexicalForm);
		for (Object baseForm : baseForms) {
			forms.add(baseForm.toString());
		}
		return new ArrayList<String>(forms);
	}

	// ===============
	public String getLemma(String word, POS pos) throws JWNLException {
		IndexWord iw = getIndexWord(pos, word);
		if (iw == null)
			return word;
		else
			return iw.getLemma();
	}

	// ================
	// Get the IndexWord object for a String and POS
	public IndexWord getIndexWord(POS pos, String s) throws JWNLException {
		// IndexWord word = wordnet.getIndexWord(pos,s);
		IndexWord word = wordnet.lookupIndexWord(pos, s); // This function
		// tries the stemmed
		// form of the lemma
		return word;
	}

	/*
	 * Main function
	 */

	// ===============
	public static void main(String[] args) throws Exception {
		WordNetManager wnManager = new WordNetManager("file_properties.xml");

		// ---------------
		ArrayList<String> arrSynonym = wnManager.getAllSynonym("dog");
		String outString = "";
		for (int i = 0; i < arrSynonym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrSynonym.get(i);
		}
		if (arrSynonym.isEmpty())
			System.out.println("There is no synonym.");
		else
			System.out
					.println("All synonyms of \"dog\" (including all possible POS and Senses):\n"
							+ outString);
		// ---------------
		ArrayList<String> arrHypernym = wnManager.getAllHypernym("dog");
		outString = "";
		for (int i = 0; i < arrHypernym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrHypernym.get(i);
		}
		if (arrHypernym.isEmpty())
			System.out.println("There is no hypernym");
		else
			System.out
					.println("All hypernyms of \"dog\" (including all possible POS and Senses):\n"
							+ outString);
		// ---------------
		ArrayList<String> arrHyponym = wnManager.getAllHyponym("dog");
		outString = "";
		for (int i = 0; i < arrHyponym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrHyponym.get(i);
		}
		if (arrHyponym.isEmpty())
			System.out.println("There is no hyponym");
		else
			System.out
					.println("All hyponyms of \"dog\" (including all possible POS and Senses):\n"
							+ outString);
		// ---------------
		ArrayList<String> arrMorph = wnManager.getAllMorph("classifying");
		outString = "";
		for (int i = 0; i < arrMorph.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrMorph.get(i);
		}
		System.out
				.println("All morphs of \"dogs\" (including all possible POS and Senses):\n"
						+ outString);
	}

}
