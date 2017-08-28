/**
 * 
 */
package edu.illinois.cs.cogcomp.indexer;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * 
 * @author dxquang
 * Feb 19, 2009
 */

public class UnNormalizedLuceneSimilarity extends DefaultSimilarity
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.DefaultSimilarity#lengthNorm(java.lang.String, int)
	 */
	@Override
	public float lengthNorm(String fieldName, int numTerms)
	{
		if(numTerms>0)
			return 1;
		else 
			return 0;		
	}
	
}
