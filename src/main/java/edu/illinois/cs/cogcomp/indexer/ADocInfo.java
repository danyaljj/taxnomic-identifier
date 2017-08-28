/**
 * 
 */
package edu.illinois.cs.cogcomp.indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dxquang Jan 27, 2009
 */

public abstract class ADocInfo {
	// ======
	// Constants
	protected static final String ID_TAG = "<ID>";
	protected static final String TITLE_TAG = "<TITLE>";
	protected static final String TEXT_TAG = "<TEXT>";
	protected static final String CATEGORY_TAG = "<CATEGORY>";

	protected static final int ID_LEN = ID_TAG.length();
	protected static final int TITLE_LEN = TITLE_TAG.length();
	protected static final int TEXT_LEN = TEXT_TAG.length();
	protected static final int CATEGORY_LEN = CATEGORY_TAG.length();

	// protected static final Set<String> INVALID_BEGIN_TEXT = new
	// HashSet<String>();
	// static {
	// INVALID_BEGIN_TEXT.add("#REDIRECT");
	// INVALID_BEGIN_TEXT.add("#redirect");
	// INVALID_BEGIN_TEXT.add("#Redirect");
	// }

	// ======
	// Variables
	protected String id;
	protected String title;
	protected String text;
	protected String category;

	abstract public boolean isDocumentValid();

	public ADocInfo() {
		id = "";
		title = "";
		text = "";
		category = "";
	}

	public boolean parse(ArrayList<String> arrLines) {

		int n = arrLines.size();
		for (int i = 0; i < n; i++) {
			String line = arrLines.get(i);
			try {

				if (line.startsWith(ID_TAG)) {
					id = line.substring(ID_LEN, line.length() - (ID_LEN + 1));
				} else if (line.startsWith(TITLE_TAG)) {
					title = line.substring(TITLE_LEN, line.length()
							- (TITLE_LEN + 1));
				} else if (line.startsWith(TEXT_TAG)
						&& line.endsWith("</TEXT>")) {
					text = line.substring(TEXT_LEN, line.length()
							- (TEXT_LEN + 1));
				} else if (line.startsWith(CATEGORY_TAG)) {
					category = line.substring(CATEGORY_LEN, line.length()
							- (CATEGORY_LEN + 1));
				}
			} catch (Exception ex) {
				System.out.println(id + "\t" + line);
				ex.printStackTrace();
				System.exit(-1);
			}
		}

		boolean isValid = isDocumentValid();

		title = title.toLowerCase();
		text = text.toLowerCase();
		category = category.toLowerCase();

		return isValid;
	}

	public String getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public String getText() {
		return this.text;
	}

	public String getCategory() {
		return this.category;
	}

}
