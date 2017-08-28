/**
 * 
 */
package edu.illinois.cs.cogcomp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 2, 2010
 */
public class Acl10Constants {

	public static final String IDF_FILE = "IDF_FILE";
	public static final String NGRAM_TITLE_FILE = "NGRAM_TITLE_FILE";

	public static Map<String, String> readConfigFile(String configFile) {

		Map<String, String> configs = new HashMap<String, String>();

		ArrayList<String> arrLines = IOManager.readLines(configFile);

		for (String line : arrLines) {
			if (line.length() == 0)
				continue;
			if (line.startsWith(IDF_FILE))
				configs.put(IDF_FILE, line.substring(line.indexOf("=") + 1)
						.trim());
			else if (line.startsWith(NGRAM_TITLE_FILE))
				configs.put(NGRAM_TITLE_FILE, line.substring(
						line.indexOf("=") + 1).trim());
		}
		return configs;
	}

}
