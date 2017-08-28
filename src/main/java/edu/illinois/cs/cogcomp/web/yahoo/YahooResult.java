package edu.illinois.cs.cogcomp.web.yahoo;

import edu.illinois.cs.cogcomp.web.IResult;

public class YahooResult implements IResult {
	
	String title;
	String snippet;
	String url; 
	
	public YahooResult(String title, String snippet, String url) {
		this.title = title;
		this.snippet = snippet;
		this.url = url;
	}
	
	@Override
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String getSnippet() {
		return this.snippet;
	}
	
	@Override
	public String getUrl() {
		return this.url;
	}
}
