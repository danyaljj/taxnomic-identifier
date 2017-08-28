package edu.illinois.cs.cogcomp.retrieval;

public class NormalRetriever extends SimpleRetriever {
	
	public NormalRetriever() {
		super();
	}
	
	@Override
	public String makeQuery(String entity1, String entity2) {
		return "\"" + entity1 + " * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * * * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * * * * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * * * * * " + entity2 + "\"" + " OR " + 
		"\"" + entity1 + " * * * * * * * " + entity2 + "\"" + " OR " + 
		"\"" + entity2 + " * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * * * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * * * * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * * * * * " + entity1 + "\"" + " OR " + 
		"\"" + entity2 + " * * * * * * * " + entity1 + "\""; 
	}

}
