/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.identification.Example;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityDisambiguation.TokenScore;

/**
 * @author dxquang May 16, 2009
 */
public class Unsupervised_Old {

	public double wCosine_Anc;
	public double wPmi_Anc;

	public double wCosineCat_Cou;
	public double wCosineAbs_Cou;

	public double wRatio_Anc;
	public double wRatio_Cou;

	public int numRound;

	/**
	 * 
	 */
	public Unsupervised_Old(int numRound) {
		this.numRound = numRound;
	}

	/**
	 * 
	 * @param exampleFile
	 * @throws Exception
	 */
	public void learn(String exampleFile) throws Exception {

		ArrayList<Example> arrExamples = DatasetCreation.readExampleFile(
				exampleFile, DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		Map<String, ArrayList<Example>> mapExamples = splitData(arrExamples);
		// 1. Initialization

		initializeWeightVector();

		// 2. Loop

		Set<String> keySet = mapExamples.keySet();

		int i = 0;

		while (i < numRound) {

			// 2a. Calculating scores and sort the examples.
			
			for (String key : keySet) {

				ArrayList<Example> arrExs = mapExamples.get(key);
				
				for (Example ex : arrExs) {
					ex.finalScore = score(ex);
				}
				
				sortExamples(arrExs);
			}
			
			// 2b. Get positive/negative examples
			
			for (String key : keySet) {
				
				ArrayList<Example> arrExs = mapExamples.get(key);
				
				if (arrExs.size() > 0) {
					Example first = arrExs.get(0);
					
					int relation = predict(first);
					
				}
			}

			i++;
		}

	}

	/**
	 * @param first
	 * @return
	 */
	private int predict(Example ex) {
		
		return 0;
		
	}

	/**
	 * @param arrExs
	 */
	private void sortExamples(ArrayList<Example> arrExs) {
		Collections.sort(arrExs, new Comparator<Example>() {
			@Override
			public int compare(Example o1, Example o2) {
				if (o1.finalScore < o2.finalScore)
					return 1;
				else if (o1.finalScore == o2.finalScore)
					return 0;
				else
					return -1;
			}
		});

	}

	/**
	 * @param arrExamples
	 * @return
	 */
	private Map<String, ArrayList<Example>> splitData(
			ArrayList<Example> arrExamples) {

		Map<String, ArrayList<Example>> mapExamples = new HashMap<String, ArrayList<Example>>();

		for (Example ex : arrExamples) {
			String entity1 = ex.entity1;

			if (mapExamples.containsKey(entity1)) {

				ArrayList<Example> arrExs = mapExamples.get(entity1);
				arrExs.add(ex);

			} else {

				ArrayList<Example> arrExs = new ArrayList<Example>();
				arrExs.add(ex);
				mapExamples.put(entity1, arrExs);

			}

		}

		return mapExamples;
	}

	/**
	 * @param ex
	 * @return
	 */
	private double score(Example ex) {

		double score = wCosine_Anc * ex.scoreCosine_Anc + wPmi_Anc
				* ex.scorePmi_Anc + wCosineCat_Cou * ex.scoreCosineCat_Cou
				+ wCosineAbs_Cou * ex.scoreCosineAbs_Cou + wRatio_Anc
				* ex.ratioCat_Anc + wRatio_Cou * ex.ratioCat_Cou;

		return score;

	}

	/**
	 * 
	 */
	private void initializeWeightVector() {

		wCosine_Anc = 1.0;
		wPmi_Anc = 1.0;

		wCosineCat_Cou = 1.0;
		wCosineAbs_Cou = 1.0;

		wRatio_Anc = 1.0;
		wRatio_Cou = 1.0;
	}

}
