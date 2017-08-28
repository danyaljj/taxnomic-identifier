/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.identification;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.emnlp09.identification.EntityCategorization;
import edu.illinois.cs.cogcomp.test.AllTests;
import edu.illinois.cs.cogcomp.utils.CosineSimilarity;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.Stemmer;
import edu.illinois.cs.cogcomp.utils.StopWord;

/**
 * @author dxquang Apr 25, 2009
 */
public class RelationIdentification {

	public static final double NO_TITLE_E1 = -1.0;
	public static final double NO_TITLE_E2 = -2.0;
	public static final double NO_TITLE_E1E2 = -3.0;

	public static final String FILE_CLASS_CLUSTER = "class-cluster.txt";

	public static final int FROM_E1_TO_E2 = 1;
	public static final int FROM_E2_TO_E1 = 2;

	public static final int NONE = 0;
	public static final int ANCESTOR_E1_TO_E2 = 1;
	public static final int ANCESTOR_E2_TO_E1 = 2;
	public static final int COUSIN = 3;

	public static final double INC_STEP_WEIGHT = 0.05;
	public static final double INC_STEP_THRES = 0.05;

	EntityCategorization categorizer = null;

	ArrayList<String> arrTitleWords1 = new ArrayList<String>();
	ArrayList<String> arrCatWords1 = new ArrayList<String>();
	ArrayList<String> arrAbsWords1 = new ArrayList<String>();

	ArrayList<String> arrTitleWords2 = new ArrayList<String>();
	ArrayList<String> arrCatWords2 = new ArrayList<String>();
	ArrayList<String> arrAbsWords2 = new ArrayList<String>();

	StopWord stopWord = new StopWord(true);

	double alpha = 0.8;

	double beta = 0.2;

	double thetaAncestor = 0.03;

	double thetaCousin = 0.03;

	double thetaRatio1 = 0.2;

	double thetaRatio2 = 0.1;

	Stemmer stemmer = new Stemmer();

	public String myEntity1 = null;
	public String myEntity2 = null;

	public static Map<String, String> mapClassMapping = new HashMap<String, String>();
	public static Map<String, Set<String>> mapClassCluster = new HashMap<String, Set<String>>();

	// General purpose variables

	public double scoreCosine_Anc;
	public double scorePmi_Anc;

	public double scoreCosineCat_Cou;
	public double scoreCosineAbs_Cou;

	public double ratioCat_Anc;
	public double ratioCat_Cou;

	/**
	 * 
	 */
	/**
	 * 
	 */
	public RelationIdentification() {

	}

	public RelationIdentification(String indexDir, String categoryMapping,
			String titleMapping, int K) throws Exception {

		categorizer = new EntityCategorization(indexDir, categoryMapping,
				titleMapping, K);

		loadClassCluster();

	}

	/**
	 * 
	 */
	public static void loadClassCluster() throws Exception {

		BufferedReader reader = IOManager.openReader(FILE_CLASS_CLUSTER);

		String line;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			String chunks[] = line.split("\\t+");

			if (chunks.length != 2)
				continue;

			String key = chunks[0].trim();

			String chunk = chunks[1].trim();

			String parts[] = chunk.split(",");

			if (parts.length == 0)
				continue;

			String value = null;

			Set<String> setValues = new HashSet<String>();

			int i = 0;
			for (String part : parts) {
				part = part.trim();
				if (i == 0)
					value = part;
				setValues.add(part);
				i++;
			}

			if (value == null)
				continue;

			mapClassMapping.put(key, value);
			mapClassCluster.put(value, setValues);

		}

		IOManager.closeReader(reader);

	}

	private void initialize() {
		arrTitleWords1 = new ArrayList<String>();
		arrCatWords1 = new ArrayList<String>();
		arrAbsWords1 = new ArrayList<String>();

		arrTitleWords2 = new ArrayList<String>();
		arrCatWords2 = new ArrayList<String>();
		arrAbsWords2 = new ArrayList<String>();
	}

	public int identify(String entity1, String entity2) throws Exception {

		initialize();

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		myEntity1 = entity1;
		myEntity2 = entity2;

		categorizer.categorize(entity1, entity2);

		fillUpWordArray();

		if (AllTests.DEBUG == 1)
			System.out.println("\n--- Ancestor score:");

		double ancestor = getAncestorScore(FROM_E1_TO_E2);

		// System.out.println();

		if (AllTests.DEBUG == 1)
			System.out.println("\n--- Cousin score:");

		double cousin = getCousinScore();

		int relation = NONE;

		if (ancestor < thetaAncestor && cousin < thetaCousin)
			relation = NONE;

		else if (ancestor < thetaAncestor && cousin >= thetaCousin) {
			boolean satisfied = constraintSatisfactionCousin();
			if (satisfied) {
				if (ratioCat_Cou >= thetaRatio2)
					relation = COUSIN;
				else
					relation = NONE;
			} else
				relation = NONE;
		}

		else if (ancestor >= thetaAncestor && cousin < thetaCousin) {
			boolean satisfied = constraintSatisfactionAncestor(FROM_E1_TO_E2);
			if (satisfied)
				relation = ANCESTOR_E1_TO_E2;
			else
				relation = NONE;
		}

		else {
			boolean satisfiedAncestor = constraintSatisfactionAncestor(FROM_E1_TO_E2);
			boolean satisfiedCousin = constraintSatisfactionCousin();
			if (satisfiedAncestor && satisfiedCousin) {
				if (ratioCat_Cou >= thetaRatio1)
					relation = COUSIN;
				else
					relation = ANCESTOR_E1_TO_E2;
			} else if (satisfiedAncestor && !satisfiedCousin)
				relation = ANCESTOR_E1_TO_E2;
			else if (!satisfiedAncestor && satisfiedCousin)
				relation = COUSIN;
			else
				relation = NONE;
		}

		return relation;
	}

	/**
	 * @param from_e1_to_e22
	 * @return
	 */
	private boolean constraintSatisfactionAncestor(int direction) {

		ArrayList<String> arrTitles = null;
		ArrayList<String> arrCats = null;
		ArrayList<String> arrHeads = null;
		ArrayList<String> arrDomains = null;

		Set<String> setValues = new HashSet<String>();

		if (direction == FROM_E1_TO_E2) {
			arrTitles = categorizer.arrTitles1;
			arrCats = categorizer.arrCategories2;
			arrHeads = categorizer.arrHeads2;
			arrDomains = categorizer.arrDomains2;

			if (mapClassCluster.containsKey(myEntity1))
				setValues.addAll(mapClassCluster.get(myEntity1));

		} else {
			arrTitles = categorizer.arrTitles2;
			arrCats = categorizer.arrCategories1;
			arrHeads = categorizer.arrHeads1;
			arrDomains = categorizer.arrDomains1;

			if (mapClassCluster.containsKey(myEntity2))
				setValues.addAll(mapClassCluster.get(myEntity2));
		}

		Set<String> setMatchCats = new HashSet<String>();
		Set<String> setMatchHeads = new HashSet<String>();
		Set<String> setMatchDomains = new HashSet<String>();

		int count = 0;

		setValues.addAll(arrTitles);

		if (AllTests.DEBUG == 1) {
			System.out.println("\n---- All titles + class cluster:");
			for (String v : setValues)
				System.out.println(v);
		}

		for (String c : arrCats) {
			if (setValues.contains(c)) {
				setMatchCats.add(c);
			}
		}

		for (String h : arrHeads) {
			if (setValues.contains(h)) {
				setMatchHeads.add(h);
			}
		}

		for (String d : arrDomains) {
			if (setValues.contains(d)) {
				setMatchDomains.add(d);
			}
		}

		// for (String t : arrTitles) {
		//
		// // System.out.println("Title: " + t);
		//
		// for (String c : arrCats) {
		// if (t.equals(c)) {
		// setMatchCats.add(c);
		// }
		// }
		//
		// for (String h : arrHeads) {
		// // System.out.println("\th: " + h);
		// if (t.equals(h)) {
		// setMatchHeads.add(h);
		// }
		// }
		//
		// for (String d : arrDomains) {
		// if (t.equals(d)) {
		// setMatchDomains.add(d);
		// }
		// }
		//
		// }

		ArrayList<String> arrMatchCats = new ArrayList<String>(setMatchCats);
		ArrayList<String> arrMatchHeads = new ArrayList<String>(setMatchHeads);
		ArrayList<String> arrMatchDomains = new ArrayList<String>(
				setMatchDomains);

		count = arrMatchCats.size() + arrMatchHeads.size()
				+ arrMatchDomains.size();

		if (categorizer.getNumCat() == 0)
			ratioCat_Anc = (double) 0.0;
		else
			ratioCat_Anc = (double) count / (double) categorizer.getNumCat();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nANCESTOR - " + count + " match(es)."
					+ " Ratio: " + (double) count
					/ (double) categorizer.getNumCat());

			if (count > 0) {

				if (arrMatchCats.size() > 0) {
					System.out.println("\n- Match categories:");
					categorizer.printArrayList(arrMatchCats, false);
				}
				if (arrMatchHeads.size() > 0) {
					System.out.println("\n- Match heads:");
					categorizer.printArrayList(arrMatchHeads, false);
				}
				if (arrMatchDomains.size() > 0) {
					System.out.println("\n- Match domains:");
					categorizer.printArrayList(arrMatchDomains, false);
				}
			}
		}

		return (count > 0) ? true : false;

	}

	private boolean constraintAncestor(int direction) {

		ArrayList<String> arrTitles = null;
		ArrayList<String> arrCats = null;
		ArrayList<String> arrHeads = null;
		ArrayList<String> arrDomains = null;

		Set<String> setValues = new HashSet<String>();

		if (direction == FROM_E1_TO_E2) {
			arrTitles = categorizer.arrTitles1;
			arrCats = categorizer.arrCategories2;
			arrHeads = categorizer.arrHeads2;
			arrDomains = categorizer.arrDomains2;

			if (mapClassCluster.containsKey(myEntity1))
				setValues.addAll(mapClassCluster.get(myEntity1));

		} else {
			arrTitles = categorizer.arrTitles2;
			arrCats = categorizer.arrCategories1;
			arrHeads = categorizer.arrHeads1;
			arrDomains = categorizer.arrDomains1;

			if (mapClassCluster.containsKey(myEntity2))
				setValues.addAll(mapClassCluster.get(myEntity2));
		}

		setValues.addAll(arrTitles);

		Set<String> setCats = new HashSet<String>(arrCats);
		Set<String> setHeads = new HashSet<String>(arrHeads);
		Set<String> setDomains = new HashSet<String>(arrDomains);
		
		Set<String> setAll = new HashSet<String>();
		setAll.addAll(setCats);
		setAll.addAll(setHeads);
		setAll.addAll(setDomains);

		Set<String> setInter = new HashSet<String>(setValues);
		setInter.retainAll(setAll);
		
		Set<String> setUnion = new HashSet<String>(setValues);
		setUnion.addAll(setAll);
		
		if (setUnion.size() == 0)
			ratioCat_Anc = (double) 0.0;
		else
			ratioCat_Anc = (double) setInter.size() / (double) setUnion.size();

		return (setInter.size() > 0) ? true : false;

	}
	/**
	 * @return
	 */
	private boolean constraintSatisfactionCousin() {

		int count = 0;

		Set<String> setMatchCats = new HashSet<String>();
		Set<String> setMatchHeads = new HashSet<String>();

		ArrayList<String> arrCats1 = categorizer.arrCategories1;
		ArrayList<String> arrCats2 = categorizer.arrCategories2;

		for (String cat1 : arrCats1) {
			for (String cat2 : arrCats2) {

				if (cat1.equals(cat2)) {
					setMatchCats.add(cat1);
				}

			}
		}

		ArrayList<String> arrHeads1 = categorizer.arrHeads1;
		ArrayList<String> arrHeads2 = categorizer.arrHeads2;

		for (String head1 : arrHeads1) {
			for (String head2 : arrHeads2) {

				if (head1.equals(head2)) {
					setMatchHeads.add(head1);
				}
			}
		}

		ArrayList<String> arrMatchCats = new ArrayList<String>(setMatchCats);
		ArrayList<String> arrMatchHeads = new ArrayList<String>(setMatchHeads);

		count = arrMatchCats.size() + arrMatchHeads.size();

		if (categorizer.getNumCat() == 0)
			ratioCat_Cou = (double) 0.0;
		else
			ratioCat_Cou = (double) count / (double) categorizer.getNumCat();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCOUSIN - " + count + " match(es)."
					+ " Ratio: " + (double) count
					/ (double) categorizer.getNumCat());

			if (count > 0) {

				if (arrMatchCats.size() > 0) {
					System.out.println("\n- Match categories:");
					categorizer.printArrayList(arrMatchCats, false);
				}
				if (arrMatchHeads.size() > 0) {
					System.out.println("\n- Match heads:");
					categorizer.printArrayList(arrMatchHeads, false);
				}
			}
		}

		return (count > 0) ? true : false;
		// return (ratioCat_Cou >= thetaRatio2) ? true : false;
	}

	private boolean constraintCousin() {

		Set<String> setEntity1 = new HashSet<String>(categorizer.arrCategories1);
		setEntity1.addAll(categorizer.arrHeads1);
		
		Set<String> setEntity2 = new HashSet<String>(categorizer.arrCategories2);
		setEntity2.addAll(categorizer.arrHeads2);
		
		Set<String> setInter = new HashSet<String>(setEntity1);
		setInter.retainAll(setEntity2);
		
		Set<String> setUnion = new HashSet<String>(setEntity1);
		setUnion.addAll(setEntity2);
		
		if (setUnion.size() == 0)
			ratioCat_Cou = (double) 0.0;
		else
			ratioCat_Cou = (double) setInter.size() / (double) setUnion.size();

		return (setInter.size() > 0) ? true : false;
		// return (ratioCat_Cou >= thetaRatio2) ? true : false;
	}

	/**
	 * @return
	 */
	private double getCousinScore() throws Exception {

		/**
		 * Formula: 1/2 * [ beta * cossim(arrCatWords1, arrCatWords2) + (1 -
		 * beta) * cossim(arrAbsWords1, arrAbsWords2) ]
		 */

		double scoreCosineCat = getCosSim(arrCatWords1, arrCatWords2);

		scoreCosineCat_Cou = scoreCosineCat;

		scoreCosineCat = beta * scoreCosineCat;

		double scoreCosineAbs = getCosSim(arrAbsWords1, arrAbsWords2);

		scoreCosineAbs_Cou = scoreCosineAbs;

		scoreCosineAbs = (1 - beta) * scoreCosineAbs;

		double score = (double) (scoreCosineCat + scoreCosineAbs);

		if (AllTests.DEBUG == 1) {
			System.out.println("scoreLeft:\t" + scoreCosineCat);
			System.out.println("scoreRight:\t" + scoreCosineAbs);
			System.out.println("scoreAll:\t" + score);
		}

		return score;
	}

	/**
	 * @param i
	 * @return
	 */
	private double getAncestorScore(int direction) throws Exception {

		double score = 0;

		ArrayList<String> arrAbsWords = null;

		ArrayList<String> arrCatWords = null;

		if (direction == FROM_E1_TO_E2) {

			arrAbsWords = arrAbsWords1;

			arrCatWords = arrCatWords2;

		} else {

			arrAbsWords = arrAbsWords2;

			arrCatWords = arrCatWords1;
		}

		/**
		 * Formula: 1/2 * [ alpha * cossim(arrTitleCatWords, arrCatWords) + (1 -
		 * alpha) * cossim(arrAbsWords, arrCatWords) ]
		 */

		double scoreCosine = getCosSim(arrAbsWords, arrCatWords);

		scoreCosine_Anc = scoreCosine;

		scoreCosine = alpha * scoreCosine;

		double scorePmi = getPmiScore(myEntity1, myEntity2);

		scorePmi_Anc = scorePmi;

		scorePmi = (1 - alpha) * scorePmi;

		score = (double) (scoreCosine + scorePmi);

		if (AllTests.DEBUG == 1) {
			System.out.println("scoreLeft:\t" + scoreCosine);
			System.out.println("scoreRight:\t" + scorePmi);
			System.out.println("scoreAll:\t" + score);
		}

		return score;

	}

	/**
	 * @param myEntity12
	 * @param myEntity22
	 * @return
	 */
	private double getPmiScore(String myEntity1, String myEntity2)
			throws Exception {

		String query = myEntity1 + " " + myEntity2;

		int hitBoth = categorizer.disambiguator.getTotalHits(query);

		int hitE1 = categorizer.disambiguator.getTotalHits(myEntity1);

		int hitE2 = categorizer.disambiguator.getTotalHits(myEntity2);

		int numDocs = categorizer.disambiguator.textSearcher.numDocs;

		double pE1E2 = (double) hitBoth / (double) numDocs;

		double pE1 = (double) hitE1 / (double) numDocs;

		double pE2 = (double) hitE2 / (double) numDocs;

		double pmi = Math.log(pE1E2 / (pE1 * pE2));

		return pmi;
	}

	/**
	 * @param setTitleCatWords
	 * @param setCatWords
	 * @return
	 */
	private double getCosSim(ArrayList<String> arrBagOfWords_1,
			ArrayList<String> arrBagOfWords_2) throws Exception {

		Map<String, Integer> mapTokenFreq1 = getTokenFreq(arrBagOfWords_1);

		Map<String, Integer> mapTokenFreq2 = getTokenFreq(arrBagOfWords_2);

		Set<String> keySet = new HashSet<String>(mapTokenFreq1.keySet());

		Set<String> anotherSet = new HashSet<String>(mapTokenFreq2.keySet());

		keySet.addAll(anotherSet);

		ArrayList<Integer> arrVector1 = new ArrayList<Integer>();
		ArrayList<Integer> arrVector2 = new ArrayList<Integer>();

		for (String key : keySet) {

			if (mapTokenFreq1.containsKey(key)) {
				// arrVector1.add(mapTokenFreq1.get(key));
				arrVector1.add(1);
			} else {
				arrVector1.add(0);
			}

			if (mapTokenFreq2.containsKey(key)) {
				// arrVector2.add(mapTokenFreq2.get(key));
				arrVector2.add(1);
			} else {
				arrVector2.add(0);
			}

		}

		double cosSim = CosineSimilarity.getSimilarity(arrVector1, arrVector2);

		return cosSim;
	}

	/**
	 * @param arrTitleCatWords
	 * @return
	 */
	private Map<String, Integer> getTokenFreq(ArrayList<String> arrTokens) {

		Map<String, Integer> mapTokenFreq = new HashMap<String, Integer>();

		for (String token : arrTokens) {

			if (mapTokenFreq.containsKey(token)) {
				Integer freq = mapTokenFreq.get(token);
				freq++;
				mapTokenFreq.put(token, freq);
			} else {
				Integer freq = new Integer(1);
				mapTokenFreq.put(token, freq);
			}
		}

		return mapTokenFreq;
	}

	/**
	 * @param categorizer2
	 */
	private void fillUpWordArray() {

		fillUpWordArray(categorizer.arrTitles1, arrTitleWords1);
		fillUpWordArray(categorizer.arrCategories1, arrCatWords1);
		fillUpWordArray(categorizer.arrAbstracts1, arrAbsWords1);

		fillUpWordArray(categorizer.arrTitles2, arrTitleWords2);
		fillUpWordArray(categorizer.arrCategories2, arrCatWords2);
		fillUpWordArray(categorizer.arrAbstracts2, arrAbsWords2);

	}

	/**
	 * @param arrTitles1
	 * @param setTitleWords12
	 */
	private void fillUpWordArray(ArrayList<String> arrStrings,
			ArrayList<String> arrWords) {
		for (String title : arrStrings) {

			ArrayList<String> arrTokens = stopWord.removeStopWords(title);
			for (String token : arrTokens)
				arrWords.add(token);

		}

	}

	public Example generateIntermediateExample(Example example)
			throws Exception {

		initialize();

		String entity1 = example.entity1;
		String entity2 = example.entity2;

		if (mapClassMapping.containsKey(entity1))
			entity1 = mapClassMapping.get(entity1);

		if (mapClassMapping.containsKey(entity2))
			entity2 = mapClassMapping.get(entity2);

		// TODO: Should we format/standardize two entities here?

		myEntity1 = entity1;
		myEntity2 = entity2;
		
		example.entity1 = entity1;
		example.entity2 = entity2;

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();
		
		timmer.start();
		categorizer.categorize(example.entity1, example.entity2);
		timmer.end();

		if (AllTests.DEBUG == 1) {
			System.out.println("\nCategorizer: " + timmer.getTimeMillis());
		}

		// If there is any entity that cannot be found in Wikipedia,
		// we need to take special corresponding actions

		Example newExample = new Example(example);

		if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(newExample, NO_TITLE_E1E2);

		} else if (categorizer.arrTitles1.size() == 0
				&& categorizer.arrTitles2.size() != 0) {

			setNoTitleResult(newExample, NO_TITLE_E1);

		} else if (categorizer.arrTitles1.size() != 0
				&& categorizer.arrTitles2.size() == 0) {

			setNoTitleResult(newExample, NO_TITLE_E2);

		} else {

			fillUpWordArray();

			timmer.start();
			getAncestorScore(FROM_E1_TO_E2);
			timmer.end();
			
			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetAncestorScore: " + timmer.getTimeMillis());
			}

			timmer.start();
			getCousinScore();
			timmer.end();

			if (AllTests.DEBUG == 1) {
				System.out.println("\nGetCousinScore: " + timmer.getTimeMillis());
			}

			constraintSatisfactionAncestor(ANCESTOR_E1_TO_E2);
			constraintSatisfactionCousin();

			newExample.scoreCosine_Anc = scoreCosine_Anc;
			scorePmi_Anc = sigmoid(scorePmi_Anc);
			newExample.scorePmi_Anc = scorePmi_Anc;
			
			newExample.scoreCosineCat_Cou = scoreCosineCat_Cou;
			newExample.scoreCosineAbs_Cou = scoreCosineAbs_Cou;

			newExample.ratioCat_Anc = ratioCat_Anc;
			newExample.ratioCat_Cou = ratioCat_Cou;

		}

		return newExample;
	}

	/**
	 * @param scorePmi_Anc2
	 * @return
	 */
	private double sigmoid(double scorePmi_Anc) {
		double res = (double) 1/ ((double) 1 + Math.exp(-scorePmi_Anc));
		return res;
	}

	/**
	 * @param no_title_e1e22
	 */
	private void setNoTitleResult(Example newExample, double noTitle) {

		newExample.scoreCosine_Anc = noTitle;
		newExample.scorePmi_Anc = noTitle;

		newExample.scoreCosineCat_Cou = noTitle;
		newExample.scoreCosineAbs_Cou = noTitle;

		newExample.ratioCat_Anc = noTitle;
		newExample.ratioCat_Cou = noTitle;

	}

	public void setParameters(double alpha, double beta, double thetaAnc,
			double thetaCou, double ratio1, double ratio2) {

		this.alpha = alpha;
		this.beta = beta;
		this.thetaAncestor = thetaAnc;
		this.thetaCousin = thetaCou;
		this.thetaRatio1 = ratio1;
		this.thetaRatio2 = ratio2;
	}

	/**
	 * @param arrTestEntity1
	 * @param arrTestEntity2
	 */
	public void evaluate(String intermediateFile, String outputFile)
			throws Exception {

		ArrayList<Example> arrInputExamples = DatasetCreation.readExampleFile(
				intermediateFile, DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		ArrayList<Example> arrOutputExamples = new ArrayList<Example>();

		System.out.println("Total # of examples: " + arrInputExamples.size());

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 1;

		int totalAnc = 0;
		int totalCou = 0;
		int totalNon = 0;

		int correct = 0;

		int correctAnc = 0;
		int incorrectAnc = 0;

		int correctCou = 0;
		int incorrectCou = 0;

		int correctNon = 0;
		int incorrectNon = 0;

		int n = arrInputExamples.size();

		for (Example example : arrInputExamples) {

			// System.out.println("[" + i + "] " + example.entity1 + " - "
			// + example.entity2);

			Example newExample = generatePredictedExample(example);

			if (example.relation == ANCESTOR_E1_TO_E2
					|| example.relation == ANCESTOR_E2_TO_E1)
				totalAnc++;
			else if (example.relation == COUSIN)
				totalCou++;
			else if (example.relation == NONE)
				totalNon++;

			if (newExample.predictedRelation == example.relation) {

				correct++;

				if (newExample.relation == COUSIN)
					correctCou++;

				else if (newExample.relation == ANCESTOR_E1_TO_E2
						|| newExample.relation == ANCESTOR_E2_TO_E1)
					correctAnc++;

				else if (newExample.relation == NONE)
					correctNon++;

			}

			else {

				if (newExample.predictedRelation == COUSIN)
					incorrectCou++;

				else if (newExample.predictedRelation == ANCESTOR_E1_TO_E2
						|| newExample.predictedRelation == ANCESTOR_E2_TO_E1)
					incorrectAnc++;

				else if (newExample.predictedRelation == NONE)
					incorrectNon++;
			}

			arrOutputExamples.add(newExample);

			i++;

		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		double accuracy = (double) correct / (double) n;

		System.out.println("- Accuracy: " + accuracy + " (" + correct + "/" + n
				+ ")");

		double precision;
		double recall;
		double fscore;
		double avgF1 = 0;

		System.out.println("\n- Ancestor");

		precision = (double) correctAnc / (double) (correctAnc + incorrectAnc);
		System.out.println("\t+ Precision: " + precision + " (" + correctAnc
				+ "/" + (correctAnc + incorrectAnc) + ")");

		recall = (double) correctAnc / (double) (totalAnc);
		System.out.println("\t+ Recall: " + recall + " (" + correctAnc + "/"
				+ (totalAnc) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- Cousin");

		precision = (double) correctCou / (double) (correctCou + incorrectCou);
		System.out.println("\t+ Precision: " + precision + " (" + correctCou
				+ "/" + (correctCou + incorrectCou) + ")");

		recall = (double) correctCou / (double) (totalCou);
		System.out.println("\t+ Recall: " + recall + " (" + correctCou + "/"
				+ (totalCou) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- None");

		precision = (double) correctNon / (double) (correctNon + incorrectNon);
		System.out.println("\t+ Precision: " + precision + " (" + correctNon
				+ "/" + (correctNon + incorrectNon) + ")");

		recall = (double) correctNon / (double) (totalNon);
		System.out.println("\t+ Recall: " + recall + " (" + correctNon + "/"
				+ (totalNon) + ")");

		fscore = (double) (2 * precision * recall)
				/ (double) (precision + recall);
		System.out.println("\t+ F1: " + fscore);

		avgF1 += fscore;

		System.out.println("\n- Average F1: " + (avgF1 / (double) 3));

		System.out.println("\nFlushing to file...");

		ArrayList<String> arrStringExamples = DatasetCreation
				.makeStringExamples(arrOutputExamples,
						DatasetCreation.INPUT_TYPE_PREDICT);

		IOManager.writeLines(arrStringExamples, outputFile);

		System.out.println("Done.");

	}

	/**
	 * @param example
	 * @return
	 */
	private Example generatePredictedExample(Example example) {
		Example newExample = new Example(example);

		double scoreLeft_Anc = this.alpha * newExample.scoreCosine_Anc;

		double scoreRight_Anc = (1 - this.alpha) * newExample.scorePmi_Anc;

		double score_Anc = (double) (scoreLeft_Anc + scoreRight_Anc);

		double scoreLeft_Cou = this.beta * newExample.scoreCosineCat_Cou;

		double scoreRight_Cou = (1 - this.beta) * newExample.scoreCosineAbs_Cou;

		double score_Cou = (double) (scoreLeft_Cou + scoreRight_Cou);

		int relation = NONE;

		if (score_Anc <= this.thetaAncestor && score_Cou <= this.thetaCousin)
			relation = NONE;

		else if (score_Anc <= this.thetaAncestor
				&& score_Cou > this.thetaCousin) {

			if (example.ratioCat_Cou > this.thetaRatio2)
				relation = COUSIN;

			else
				relation = NONE;
		}

		else if (score_Anc > this.thetaAncestor
				&& score_Cou <= this.thetaCousin) {

			if (example.ratioCat_Anc > 0)
				relation = ANCESTOR_E1_TO_E2;

			else
				relation = NONE;
		}

		else {

			if (example.ratioCat_Anc > 0 && example.ratioCat_Cou > 0) {
				if (example.ratioCat_Cou > this.thetaRatio1)
					relation = COUSIN;
				else
					relation = ANCESTOR_E1_TO_E2;
			}

			else if (example.ratioCat_Anc > 0 && example.ratioCat_Cou == 0)
				relation = ANCESTOR_E1_TO_E2;

			else if (example.ratioCat_Anc == 0 && example.ratioCat_Cou > 0)
				relation = COUSIN;

			else
				relation = NONE;
		}

		// System.out.println("\nalpha: " + this.alpha + ", beta: " + this.beta
		// + ", thetaA: " + this.thetaAncestor + ", thetaC: "
		// + this.thetaCousin + ", ratio1: " + this.thetaRatio1
		// + ", ratio2: " + this.thetaRatio2);
		// System.out.println("ratioCat_Anc: " + example.ratioCat_Anc +
		// ", ratioCat_Cou: " + example.ratioCat_Cou);
		// System.out.println("score_Anc: " + score_Anc + ", score_Cou: " +
		// score_Cou);

		if (relation == NONE)
			newExample.finalScore = 0.0;
		else if (relation == COUSIN)
			newExample.finalScore = score_Cou;
		else
			newExample.finalScore = score_Anc;

		newExample.predictedRelation = relation;

		return newExample;
	}

	public void tune(String intermediateFile, String outputFile)
			throws Exception {

		ArrayList<Example> arrInputExamples = DatasetCreation.readExampleFile(
				intermediateFile, DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		int n = arrInputExamples.size();

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		double bestAlpha = -1;
		double bestBeta = -1;
		double bestThetaAnc = -1;
		double bestThetaCou = -1;
		double bestRatio1 = -1;
		double bestRatio2 = -1;

		double bestAccuracy = -1;

		for (this.alpha = 0; this.alpha <= 1; this.alpha += 0.1) {

			timmer.start();

			System.out.println("alpha: " + this.alpha);

			for (this.beta = 0; this.beta <= 1; this.beta += 0.1) {

				System.out.println("\tbeta: " + this.beta);

				for (this.thetaAncestor = 0; this.thetaAncestor <= 0.3; this.thetaAncestor += 0.01) {

					for (this.thetaCousin = 0; this.thetaCousin <= 0.3; this.thetaCousin += 0.01) {

						for (this.thetaRatio1 = 0; this.thetaRatio1 <= 0.3; this.thetaRatio1 += 0.01) {

							for (this.thetaRatio2 = 0; this.thetaRatio2 <= 0.3; this.thetaRatio2 += 0.01) {

								int correct = 0;

								for (Example example : arrInputExamples) {

									int relation = predict(example);

									if (relation == example.relation)
										correct++;

								}

								double accuracy = (double) correct / (double) n;

								if (accuracy > bestAccuracy) {
									bestAccuracy = accuracy;
									bestAlpha = this.alpha;
									bestBeta = this.beta;
									bestThetaAnc = this.thetaAncestor;
									bestThetaCou = this.thetaCousin;
									bestRatio1 = this.thetaRatio1;
									bestRatio2 = this.thetaRatio2;
								}

							}
						}

					}
				}

			}

			timmer.end();

			System.out.println("--------- Total time for alapha=" + this.alpha
					+ ": " + timmer.getTimeSeconds() + " secs.");
		}

		String output = "Accuracy: " + bestAccuracy + "\n" + "alpha: "
				+ bestAlpha + "\n" + "beta: " + bestBeta + "\n" + "thetaAnc: "
				+ bestThetaAnc + "\n" + "thetaCou: " + bestThetaCou + "\n"
				+ "ratio1: " + bestRatio1 + "\n" + "ratio2: " + bestRatio2
				+ "\n";

		IOManager.writeContent(output, outputFile);

	}

	/**
	 * @param example
	 * @return
	 */
	private int predict(Example example) {

		double scoreLeft_Anc = this.alpha * example.scoreCosine_Anc;

		double scoreRight_Anc = (1 - this.alpha) * example.scorePmi_Anc;

		double score_Anc = (double) (scoreLeft_Anc + scoreRight_Anc);

		double scoreLeft_Cou = this.beta * example.scoreCosineCat_Cou;

		double scoreRight_Cou = (1 - this.beta) * example.scoreCosineAbs_Cou;

		double score_Cou = (double) (scoreLeft_Cou + scoreRight_Cou);

		int relation = NONE;

		if (score_Anc <= this.thetaAncestor && score_Cou <= this.thetaCousin)
			relation = NONE;

		else if (score_Anc <= this.thetaAncestor
				&& score_Cou > this.thetaCousin) {

			if (example.ratioCat_Cou > this.thetaRatio2)
				relation = COUSIN;

			else
				relation = NONE;
		}

		else if (score_Anc > this.thetaAncestor
				&& score_Cou <= this.thetaCousin) {

			if (example.ratioCat_Anc > 0)
				relation = ANCESTOR_E1_TO_E2;

			else
				relation = NONE;
		}

		else {

			if (example.ratioCat_Anc > 0 && example.ratioCat_Cou > 0) {
				if (example.ratioCat_Cou > this.thetaRatio1)
					relation = COUSIN;
				else
					relation = ANCESTOR_E1_TO_E2;
			}

			else if (example.ratioCat_Anc > 0 && example.ratioCat_Cou == 0)
				relation = ANCESTOR_E1_TO_E2;

			else if (example.ratioCat_Anc == 0 && example.ratioCat_Cou > 0)
				relation = COUSIN;

			else
				relation = NONE;
		}

		return relation;
	}

}
