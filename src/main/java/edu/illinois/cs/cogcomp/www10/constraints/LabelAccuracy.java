/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import java.text.DecimalFormat;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;

/**
 * @author dxquang Oct 15, 2009
 */
public class LabelAccuracy {

	int correctXY = 0;
	int totalXY = 0;

	int correctYX = 0;
	int totalYX = 0;

	int correctZZ = 0;
	int totalZZ = 0;

	int correctNone = 0;
	int totalNone = 0;

	int correctAll = 0;
	int totalAll = 0;

	public void add(Instance ins, boolean result) {
		switch (ins.relation) {
		case Constants.ANCESTOR_E1_TO_E2:
			if (result == true)
				correctXY++;
			totalXY++;
			break;
		case Constants.ANCESTOR_E2_TO_E1:
			if (result == true)
				correctYX++;
			totalYX++;
			break;
		case Constants.COUSIN:
			if (result == true)
				correctZZ++;
			totalZZ++;
			break;
		case Constants.NONE:
			if (result == true)
				correctNone++;
			totalNone++;
			break;
		}

		if (result == true)
			correctAll++;
		totalAll++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		DecimalFormat df = new DecimalFormat("#.####");

		double acc;
		String accString;

		if (totalXY == 0)
			acc = 1;
		else
			acc = (double) correctXY / (double) totalXY;
		accString = df.format(acc);

		String xy = correctXY + " | " + totalXY + " | " + accString + "|";

		if (totalYX == 0)
			acc = 1;
		else
			acc = (double) correctYX / (double) totalYX;
		accString = df.format(acc);

		String yx = correctYX + " | " + totalYX + " | " + accString + "|";

		if (totalZZ == 0)
			acc = 1;
		else
			acc = (double) correctZZ / (double) totalZZ;
		accString = df.format(acc);

		String zz = correctZZ + " | " + totalZZ + " | " + accString + "|";

		if (totalNone == 0)
			acc = 1;
		else
			acc = (double) correctNone / (double) totalNone;
		accString = df.format(acc);

		String none = correctNone + " | " + totalNone + " | " + accString + "|";

		if (totalAll == 0)
			acc = 1;
		else
			acc = (double) correctAll / (double) totalAll;
		accString = df.format(acc);

		String all = correctAll + " | " + totalAll + " | " + accString + "|";

		return xy + yx + zz + none + all;
	}
}
