package edu.illinois.cs.cogcomp.web;

/**
 * @author dxquang
 */

import java.util.ArrayList;

public interface ISearch {

	ArrayList<IResult> searchWeb(String query, int numResult) throws Exception;
	
}
