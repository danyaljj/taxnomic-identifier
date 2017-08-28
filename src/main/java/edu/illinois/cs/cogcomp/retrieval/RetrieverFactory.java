package edu.illinois.cs.cogcomp.retrieval;

import java.io.IOException;

/**
 * 
 * @author dxquang
 * Feb 2, 2009
 */

public class RetrieverFactory {

	public enum RetrieverType {
		
		SimpleRetrieverEnum {
			
			ARetriever getRetriever() {
				ARetriever retriever = new SimpleRetriever();
				return retriever;
			}
		};
		
		abstract ARetriever getRetriever();
	}
	
	public static ARetriever getRetriever(RetrieverType type) throws IOException
	{
		ARetriever retriever = type.getRetriever();

		return retriever;
	}

}
