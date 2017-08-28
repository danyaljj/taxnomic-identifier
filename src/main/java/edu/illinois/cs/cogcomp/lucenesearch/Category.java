/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;


/**
 * @author dxquang
 * Jan 28, 2009
 */

public class Category {

	public String category;
	public int upLevel;
	public double score;
	
	public String catParent;
	
	public String catOrigin;
	
	public String matchCategory1;
	public String matchCategory2;
	
	/**
	 * 
	 */
	public Category(String category, int upLevel, double score) {
		this.category = category;
		this.upLevel = upLevel;
		this.score = score;
		this.catParent = "";
		this.catOrigin = "";
		this.matchCategory1 = "";
		this.matchCategory2 = "";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof Category)) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		Category otherCat = (Category)obj;
		return (category.equals(otherCat.category));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + category.hashCode();
		return hash;
	}
}
