/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

/**
 * @author dxquang Feb 15, 2009
 */
public class Cate {

	public String titleId;
	
	public int id;

	public int fromdId;

	public String categoryName;

	public String generalizedName;

	public String domain;

	public int level;

	public double score;

	public CateAnalyzer catAnalyzer = null;

	/**
	 * 
	 */
	public Cate(String titleId, int id, int fromId, String categoryName, int level) {

		this.titleId = titleId;
		
		this.id = id;

		this.fromdId = fromId;

		this.categoryName = categoryName;

		this.level = level;

		this.catAnalyzer = new CateAnalyzer();

		this.catAnalyzer.analyze(this.categoryName, RelationDetector.TASK_PARENTCHILD);

		this.generalizedName = catAnalyzer.getGeneralizedName();

		this.domain = catAnalyzer.getDomain();

		/*
		 * try {
		 * 
		 * this.categoryName = LLM.getMorph(this.categoryName);
		 * 
		 * if (this.generalizedName.length() > 0) this.generalizedName =
		 * LLM.getMorph(this.generalizedName);
		 * 
		 * if (this.domain.length() > 0) this.domain =
		 * LLM.getMorph(this.domain);
		 * 
		 * } catch (Exception e) {
		 * 
		 * e.printStackTrace();
		 * System.out.println("Unable to getMorph! Cate construction.");
		 * System.exit(1); }
		 */

	}

	public Cate(String titleId, int id, int fromId, String categoryName, int level, CateAnalyzer catAnalyzer, int task) {

		this.titleId = titleId;
		
		this.id = id;

		this.fromdId = fromId;

		this.categoryName = categoryName;

		this.level = level;

		catAnalyzer.analyze(this.categoryName, task);

		this.generalizedName = catAnalyzer.getGeneralizedName();

		this.domain = catAnalyzer.getDomain();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ("***Category: " + categoryName + " (genName=" + generalizedName
				+ ", domain=" + domain + ", id=" + id + ", fromId=" + fromdId
				+ ", level=" + level + ")");
	}

}
