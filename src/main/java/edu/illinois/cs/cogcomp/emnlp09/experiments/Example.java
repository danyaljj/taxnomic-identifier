/**
 * 
 */
package edu.illinois.cs.cogcomp.emnlp09.experiments;

import edu.illinois.cs.cogcomp.emnlp09.identification.RelationIdentification;

/**
 * @author dxquang
 * Apr 27, 2009
 */
public class Example {

	public String entity1;
	public String entity2;
	public int relation; // (0: None, 1: ANCESTOR_E1_E2, 2: ANCESTOR_E2_E1, 3: COUSIN)
	public String entityClass;
	
	public double scoreLeft_Anc;
	public double scoreRight_Anc;
	
	public double scoreLeft_Cou;
	public double scoreRight_Cou;
	
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
		
		scoreLeft_Anc = 0;
		scoreRight_Anc = 0;

		scoreLeft_Cou = 0;
		scoreRight_Cou = 0;
		
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
		
		scoreLeft_Anc = example.scoreLeft_Anc;
		scoreRight_Anc = example.scoreRight_Anc;

		scoreLeft_Cou = example.scoreLeft_Cou;
		scoreRight_Cou = example.scoreRight_Cou;
		
		ratioCat_Anc = example.ratioCat_Anc;
		ratioCat_Cou = example.ratioCat_Cou;

	}
	
}
