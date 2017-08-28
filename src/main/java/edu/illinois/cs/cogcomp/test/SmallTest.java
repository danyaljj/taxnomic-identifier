package edu.illinois.cs.cogcomp.test;

import java.util.HashMap;
import java.io.*;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.MainRelationIdentification;

public class SmallTest {
	public static void main(String[] args) {
		String queryFile = "DataI/train_short.txt";
//		String concept1 = "university";
//		String concept2 = "university of nijmegen";
//		HashMap<String, String> mapNames = new HashMap<String, String>();
//		mapNames.put("FIRST_STRING", concept1);
//		mapNames.put("SECOND_STRING", concept2);
		try {
			MainRelationIdentification mri = new MainRelationIdentification();
			InputStream in = new FileInputStream(queryFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			int total = 0, correct = 0;
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				total++;
				String [] itemList = strLine.split("\\t+");
				if (itemList.length < 4) {
					System.out.println("ERROR!");
					break;
				}
				int relation = Integer.valueOf(itemList[0]);
				HashMap<String, String> mapNames = new HashMap<String, String>();
				mapNames.put("FIRST_STRING", itemList[2]);
				mapNames.put("SECOND_STRING", itemList[3]);
				HashMap<String, String> orig_result = mri.identify(mapNames);
				mapNames.replace("FIRST_STRING", itemList[1]);
				mapNames.replace("SECOND_STRING", itemList[2]);
				HashMap<String, String> result1 = mri.identify(mapNames);
				mapNames.replace("FIRST_STRING", itemList[1]);
				mapNames.replace("SECOND_STRING", itemList[3]);
				HashMap<String, String> result2 = mri.identify(mapNames);
				int prediction = 0;
				int res1 = Integer.valueOf(result1.get("RELATION")), res2 = Integer.valueOf(result2.get("RELATION"));
				if (res1 == res2 && res1 == Constants.ANCESTOR_E1_TO_E2) {
					prediction = Constants.COUSIN;
				} else {
					prediction = Integer.valueOf(orig_result.get("RELATION"));
				}
//				System.out.println("orig: " + orig_result.get("RELATION") + ", 1-3: " + result1.get("RELATION") + ", 2-3: " + result2.get("RELATION"));
//				System.out.println("prediction: " + prediction + "    expected: " + relation + "\n");
				if (relation == prediction) {
					correct++;
				} else {
//					System.out.println("orig: " + orig_result.get("RELATION") + ", 1-3: " + result1.get("RELATION") + ", 2-3: " + result2.get("RELATION"));
//					System.out.println(strLine);
				}
			}
			System.out.println(correct + "/" + total + " correct");
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
