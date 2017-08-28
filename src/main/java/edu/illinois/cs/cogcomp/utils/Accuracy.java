/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;

/**
 * @author dxquang
 * May 14, 2009
 */
public class Accuracy {

	/**
	 * 
	 */
	public Accuracy() {
	
	}
	
	public static void accuracyMeasure(String labeledFile, String predictedFile) {
		ArrayList<String> arrLables = IOManager.readLines(labeledFile);
		ArrayList<String> arrPredicts = IOManager.readLines(predictedFile);
		
		
	}
	
}
