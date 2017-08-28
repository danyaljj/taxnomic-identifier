/**
 * 
 */
package edu.illinois.cs.cogcomp.cikm09.identification;

/**
 * @author dxquang
 * Apr 27, 2009
 */
public class Example {

	public String entity1;
	public String entity2;
	public int relation; // (0: None, 1: ANCESTOR_E1_E2, 2: ANCESTOR_E2_E1, 3: COUSIN)
	public String entityClass;
	
	public double scoreCosine_Anc;
	public double scorePmi_Anc;
	
	public double scoreCosineCat_Cou;
	public double scoreCosineAbs_Cou;
	
	public double ratioCat_Anc;
	public double ratioCat_Cou;
	
	public double finalScore;
	public int predictedRelation; // (0: None, 1: ANCESTOR_E1_E2, 2: ANCESTOR_E2_E1, 3: COUSIN)
	
	/**
	 * 
	 */
	public Example(String entity1, String entity2) {

		if (RelationIdentification.mapClassMapping.containsKey(entity1))
			entity1 = RelationIdentification.mapClassMapping.get(entity1);

		if (RelationIdentification.mapClassMapping.containsKey(entity2))
			entity2 = RelationIdentification.mapClassMapping.get(entity2);
		
		this.entity1 = entity1;
		this.entity2 = entity2;
		
		this.relation = RelationIdentification.NONE;
		
		scoreCosine_Anc = 0;
		scorePmi_Anc = 0;

		scoreCosineCat_Cou = 0;
		scoreCosineAbs_Cou = 0;
		
		ratioCat_Anc = 0;
		ratioCat_Cou = 0;
		
	}
	
	/**
	 * 
	 */
	public Example(Example example) {

		this.entity1 = example.entity1;
		this.entity2 = example.entity2;
		this.relation = example.relation;
		this.entityClass = example.entityClass;
		
		scoreCosine_Anc = example.scoreCosine_Anc;
		scorePmi_Anc = example.scorePmi_Anc;

		scoreCosineCat_Cou = example.scoreCosineCat_Cou;
		scoreCosineAbs_Cou = example.scoreCosineAbs_Cou;
		
		ratioCat_Anc = example.ratioCat_Anc;
		ratioCat_Cou = example.ratioCat_Cou;

	}
	
}
