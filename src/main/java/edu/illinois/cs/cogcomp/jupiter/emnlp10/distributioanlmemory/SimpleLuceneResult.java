/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10.distributioanlmemory;

/**
 * @author vivek + quang
 * 
 */
public class SimpleLuceneResult implements ILuceneResult {

	private String id;
	private double score;
	private String doc;
	private int luceneId;

	public SimpleLuceneResult(String id, String doc, double score) {
		this.id = id;
		this.score = score;
		this.doc = doc;
	}

	public SimpleLuceneResult(String id, String doc, double score, int luceneId) {
		this.id = id;
		this.score = score;
		this.doc = doc;
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
		return (id.equals(otherObj.id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + id.hashCode();
		return hash;
	}

}
