/**
 * 
 */
package edu.illinois.cs.cogcomp.indexer;

import org.apache.lucene.search.DefaultSimilarity;

public class PreferringShortLengthSimilarity extends DefaultSimilarity
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
			return (float)1/(float)numTerms;
		else 
			return 0;		
	}
	
}
