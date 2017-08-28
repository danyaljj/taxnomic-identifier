package edu.illinois.cs.cogcomp.relation;

import java.util.HashMap;

/**
 * 
 * @author dxquang
 *
 */
public class Prototype {
	
	public final static HashMap<String, Integer> RELATIONS = new HashMap<String, Integer>();
	static {
		RELATIONS.put("NONE", 0);
		RELATIONS.put("ROLE", 1);
		RELATIONS.put("PART", 2);
		RELATIONS.put("AT", 3);
		RELATIONS.put("SOC", 4);
		RELATIONS.put("COOR", 5);
	}

	public String entity1;
	public String entity2;
	public int relation;
	
	public Prototype(String entity1, String entity2, String rel) {
		this.entity1 = entity1;
		this.entity2 = entity2;
		rel = rel.toUpperCase();
		if (RELATIONS.containsKey(rel)) {
			relation = RELATIONS.get(rel);
		}
		else {
			relation = RELATIONS.get("NONE");
		}
	}
	
}
