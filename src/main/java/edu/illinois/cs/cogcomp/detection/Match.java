/**
 * 
 */
package edu.illinois.cs.cogcomp.detection;

/**
 * @author dxquang Feb 16, 2009
 */
public class Match {

	public String titleId1;

	public String title1;

	public int catId1;
	
	public double promScore1;

	public String titleId2;

	public String title2;

	public int catId2;
	
	public double promScore2;
	
	public double prominenceScore;
	
	public String targetClass;
	
	public double miScore;
	
	public double levelScore;

	/**
	 * 
	 */
	public Match(String titleId1, String title1, int catId1, double promScore1, String titleId2,
			String title2, int catId2, double promScore2) {
		this.titleId1 = titleId1;

		this.title1 = title1;
		
		this.catId1 = catId1;

		this.promScore1 = promScore1;
		
		this.titleId2 = titleId2;

		this.title2 = title2;
		
		this.catId2 = catId2;
		
		this.promScore2 = promScore2;
		
		this.prominenceScore = (double)(this.promScore1 + this.promScore2) / (double)2;
	}
}
