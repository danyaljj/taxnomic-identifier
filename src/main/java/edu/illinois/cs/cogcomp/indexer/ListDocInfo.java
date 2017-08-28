package edu.illinois.cs.cogcomp.indexer;

import java.util.HashSet;

/**
 * 
 * @author dxquang
 * Jan 27, 2009
 */

public class ListDocInfo extends ADocInfo {

	protected static final HashSet<String> VALID_BEGIN_TITLE = new HashSet<String>();
	static {
		VALID_BEGIN_TITLE.add("List of");
	}


	public ListDocInfo() {
		super();
	}

	public boolean isDocumentValid() {

		if (category.length() == 0)
			return false;

		if (title.length() == 0)
			return false;

		for (String valid : VALID_BEGIN_TITLE) {
			if (title.startsWith(valid))
				return true;
		}

		return false;
	}
	
}
