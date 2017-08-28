/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

import java.util.ArrayList;

/**
 * @author dxquang Apr 15, 2009
 */
public class UtilManager {

	public static String formatString(String e) {

		// lexical
		String entity = e.replaceAll("\\p{Punct}", " ");
		entity = entity.replaceAll("\\s+", " ");
		entity = entity.toLowerCase();
		entity = entity.trim();

		return entity;
	}

	public static void counting(String fname) {
		ArrayList<String> lines = IOManager.readLines(fname);

		int c0 = 0;
		int totalC0 = 0;

		int c1 = 0;
		int totalC1 = 0;

		int c2 = 0;
		int totalC2 = 0;

		int c3 = 0;
		int totalC3 = 0;

		int d1 = 0;
		int d2 = 0;
		int d3 = 0;

		int e1 = 0;
		int totalE1 = 0;

		for (String line : lines) {
			if (line.length() == 0) {
				continue;
			}

			String[] parts = line.split("\\t+");

			if (Integer.parseInt(parts[0]) == 1) {
				if (Double.parseDouble(parts[4]) != 0.0
						&& Double.parseDouble(parts[5]) == 0.0)
					c1++;
				// else
				// System.out.println(line);
				totalC1++;
			}

			if (Integer.parseInt(parts[0]) == 2) {
				if (Double.parseDouble(parts[4]) == 0.0
						&& Double.parseDouble(parts[5]) != 0.0)
					c2++;
				totalC2++;
			}

			if (Integer.parseInt(parts[0]) == 3) {
				if (Double.parseDouble(parts[4]) == 0.0
						&& Double.parseDouble(parts[5]) == 0.0)
					c3++;
				totalC3++;
			}

			if (Integer.parseInt(parts[0]) == 0) {
				if (Double.parseDouble(parts[4]) != 0.0
						&& Double.parseDouble(parts[5]) == 0.0)
					d1++;
				else if (Double.parseDouble(parts[4]) != 0.0
						&& Double.parseDouble(parts[5]) == 0.0)
					d2++;
				else if (Double.parseDouble(parts[4]) == 0.0
						&& Double.parseDouble(parts[5]) == 0.0)
					d3++;
			}

			if (Double.parseDouble(parts[4]) != 0.0
					&& Double.parseDouble(parts[5]) == 0.0) {
				if (Integer.parseInt(parts[0]) == 1)
					e1++;
				else
					System.out.println(line);
				totalE1++;
			}
		}

		System.out.println("c1/totalc1 = " + ((double) c1 / (double) totalC1));
		System.out.println("c2/totalc1 = " + ((double) c2 / (double) totalC2));
		System.out.println("c3/totalc1 = " + ((double) c3 / (double) totalC3));
		System.out.println(d1);
		System.out.println(d2);
		System.out.println(d3);
		System.out.println("e1/totalE1 = " + ((double) e1 / (double) totalE1));

	}

	public static void main(String[] args) {
		UtilManager.counting(args[0]);
	}

	public boolean checkIsNumber(String toCheck) {
		try {
			Integer.parseInt(toCheck);
			return true;
		} catch (NumberFormatException numForEx) {
			return false;
		}
	}

}
