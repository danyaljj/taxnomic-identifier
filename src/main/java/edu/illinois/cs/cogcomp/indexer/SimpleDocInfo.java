package edu.illinois.cs.cogcomp.indexer;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author dxquang Jan 27, 2009
 */

public class SimpleDocInfo extends ADocInfo {

	protected final static Set<String> INVALID_BEGIN_TITLE = new HashSet<String>();
	static {
		INVALID_BEGIN_TITLE.add("Wikipedia:");
		// INVALID_BEGIN_TITLE.add("Category:");
		INVALID_BEGIN_TITLE.add("Template:");
		INVALID_BEGIN_TITLE.add("Image:");
		INVALID_BEGIN_TITLE.add("Portal:");
		// INVALID_BEGIN_TITLE.add("List of");
	}

	protected static final Set<String> REDIRECT_BEGIN_TEXT = new HashSet<String>();
	static {
		REDIRECT_BEGIN_TEXT.add("#REDIRECT");
		REDIRECT_BEGIN_TEXT.add("#redirect");
		REDIRECT_BEGIN_TEXT.add("#Redirect");
	}

	protected static final Set<String> INVALID_CATEGORY = new HashSet<String>();
	static {
		INVALID_CATEGORY.add("Protected redirects");
		INVALID_CATEGORY.add("Wikipedia");
	}

	public SimpleDocInfo() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.indexer.ADocInfo#isDocumentValid()
	 */
	@Override
	public boolean isDocumentValid() {

		if (title.length() == 0)
			return false;

		for (String invalidTitle : INVALID_BEGIN_TITLE) {
			if (title.startsWith(invalidTitle))
				return false;
		}

		if (category.length() == 0) {

			boolean check = false;

			for (String redirectText : REDIRECT_BEGIN_TEXT) {
				if (text.startsWith(redirectText)) {
					check = true;
					break;
				}
			}

			if (check == false)
				return false;

		}

		for (String invalidCat : INVALID_CATEGORY) {
			if (category.startsWith(invalidCat))
				return false;
		}

		return true;
	}

}
