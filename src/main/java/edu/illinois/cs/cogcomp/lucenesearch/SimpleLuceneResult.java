/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

/**
 * @author vivek + quang
 * 
 */
public class SimpleLuceneResult implements ILuceneResult {

	private String id;
	private double score;
	private String doc;
	private String title;
	private String category;
	private int luceneId;

	public SimpleLuceneResult(String id, String title, String doc,
			String category, double score) {
		this.id = id;
		this.title = title;
		this.score = score;
		this.doc = doc;
		this.category = category;
	}

	public SimpleLuceneResult(String id, String title, String doc,
			String category, double score, int luceneId) {
		this.id = id;
		this.title = title;
		this.score = score;
		this.doc = doc;
		this.category = category;
		this.luceneId = luceneId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.retrieval.IResult#getId()
	 */
	public String getId() {
		return id;
	}

	public double getScore() {
		return score;
	}

	public String getTitle() {
		return title;
	}

	public void setScore(double score) {
		this.score = score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.retrieval.IResult#getDoc()
	 */
	public String getDoc() {
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cogcomp.lucenesearch.ILuceneResult#getCategory()
	 */
	@Override
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String cat) {
		this.category = cat;
	}
	
	/**
	 * @return the luceneId
	 */
	public int getLuceneId() {
		return luceneId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof SimpleLuceneResult)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		SimpleLuceneResult otherObj = (SimpleLuceneResult) obj;
		return (title.equals(otherObj.title));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + title.hashCode();
		return hash;
	}

}
