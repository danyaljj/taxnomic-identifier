package edu.illinois.cs.cogcomp.indexer;

import java.util.HashSet;

/**
 * 
 * @author dxquang
 * Jan 27, 2009
 */

public class CategoryDocInfo extends ADocInfo {

	protected static final HashSet<String> VALID_BEGIN_TITLE = new HashSet<String>();
	static {
		VALID_BEGIN_TITLE.add("Category:");
	}


	public CategoryDocInfo() {
		super();
	}

	public boolean isDocumentValid() {

		if (category.length() == 0)
			return false;

		if (title.length() == 0)
			return false;

		for (String valid : VALID_BEGIN_TITLE) {
			if (title.startsWith(valid)) {
				//if ("Category:".equals(valid))
				//	title = title.substring("Category:".length());
				return true;
			}
		}

		return false;
	}
	
}
