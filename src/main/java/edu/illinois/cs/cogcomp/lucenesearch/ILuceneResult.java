/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

/**
 * @author vivek
 *
 */
public interface ILuceneResult
{
	String getId();
	
	String getTitle();
	
	double getScore();
	
	String getCategory();
	
	public void setCategory(String cat);
	
	String getDoc();

	int getLuceneId();
	
	public void setScore(double score);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode();
}
