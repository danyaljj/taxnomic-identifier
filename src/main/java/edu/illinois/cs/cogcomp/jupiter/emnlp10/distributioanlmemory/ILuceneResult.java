/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10.distributioanlmemory;

/**
 * @author vivek
 *
 */
public interface ILuceneResult
{
	String getId();
	
	double getScore();
	
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
