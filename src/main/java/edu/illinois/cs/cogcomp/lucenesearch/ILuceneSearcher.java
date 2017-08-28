/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public interface ILuceneSearcher extends Closeable
{
	public ArrayList<ILuceneResult> search(String query, int numResults) throws Exception;
	
	public void open(String index) throws IOException;
	
	public int getTotalHits();
	
	public Set<Category> extractCategories(int docId, double score) throws Exception;

}
