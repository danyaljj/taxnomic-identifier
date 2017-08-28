/**
 * 
 */
package edu.illinois.cs.cogcomp.indexer;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * 
 * @author dxquang
 * Jan 27, 2009
 */

public class ListSearchSimilarity extends DefaultSimilarity
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
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.DefaultSimilarity#tf(float)
	 */
	@Override
	public float tf(float freq) {

		//System.out.println("Float freq = " + freq);
		
		if (freq - 1 == 0.0)
			return (float)0;
		
		return (float)1.0/freq;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Similarity#tf(int)
	 */
	@Override
	public float tf(int freq) {

		//System.out.println("Float freq = " + freq);

		if (freq - 1 == 0)
			return (float)0;
		
		return (float)1.0/(float)freq;
	}

}
