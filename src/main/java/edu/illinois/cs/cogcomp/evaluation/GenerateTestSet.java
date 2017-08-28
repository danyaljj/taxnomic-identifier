/**
 * 
 */
package edu.illinois.cs.cogcomp.evaluation;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 7, 2009
 */
public class GenerateTestSet {

	public class TestPair {
		public String entity1;
		public String entity2;
		public String entityClass;

		/**
		 * 
		 */
		public TestPair(String entity1, String entity2, String entityClass) {
			this.entity1 = entity1.toLowerCase().trim();
			this.entity2 = entity2.toLowerCase().trim();
			this.entityClass = entityClass.toLowerCase().trim();
		}
	}

	String inputFile;

	/**
	 * 
	 */
	public GenerateTestSet(String inputFile) {
		this.inputFile = inputFile;
	}

	public void generate(String outputFile, int num) {
		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		BufferedWriter writer = IOManager.openWriter(outputFile);

		try {
			for (String line : arrLines) {
				if (line.length() > 0) {
					ArrayList<TestPair> arrTestPairs = generateTestPairs(line,
							num);
					for (TestPair testPair : arrTestPairs) {
						String writeLine = testPair.entityClass + "\t"
								+ testPair.entity1 + "\t" + testPair.entity2
								+ "\n";
						writer.write(writeLine);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to write to file " + outputFile);
			System.exit(1);
		}

		IOManager.closeWriter(writer);
	}

	/**
	 * @param line
	 * @param num
	 * @return
	 */
	private ArrayList<TestPair> generateTestPairs(String line, int num) {

		ArrayList<TestPair> arrTestPairs = new ArrayList<TestPair>();

		String parts[] = line.split("\\t+");

		if (parts.length != 2)
			return arrTestPairs;

		String entityClass = parts[0];
		String entities = parts[1];
		String tokens[] = entities.split(",+");

		ArrayList<String> arrTokens = new ArrayList<String>();

		for (String token : tokens) {

			arrTokens.add(token.toLowerCase().trim());
		
		}

		ArrayList<TestPair> arrAllTestPairs = new ArrayList<TestPair>();
		int n = arrTokens.size();

		int count = 0;
		
		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {

				TestPair testPair = new TestPair(arrTokens.get(i), arrTokens
						.get(j), entityClass);

				arrAllTestPairs.add(testPair);

				count ++;
				
			}
		}
		
		int numPair = Math.min(num, count);

		// System.out.println("count: " + count + ", numPair: " + numPair);

		Random randGenerator = new Random();

		Set<Integer> setInts = new HashSet<Integer>();

		int k = 0;

		while (k < numPair) {

			int rand = randGenerator.nextInt(count);

			// System.out.println(rand);

			if (setInts.contains(rand))
				continue;

			arrTestPairs.add(arrAllTestPairs.get(rand));

			setInts.add(rand);

			k++;
			
		}

		return arrTestPairs;
	}
	
}
