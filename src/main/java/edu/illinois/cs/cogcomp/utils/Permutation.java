/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author dxquang May 13, 2009
 */
public class Permutation {

	/**
	 * 
	 */
	public Permutation() {

	}

	public void permute(String input, String output) {

		ArrayList<String> arrLines = IOManager.readLines(input);

		Collections.shuffle(arrLines);
		
		IOManager.writeLinesAddingReturn(arrLines, output);
		
	}

	public static void main(String[] args) {

		Permutation p = new Permutation();
		p.permute(args[0], args[1]);
	}
}
