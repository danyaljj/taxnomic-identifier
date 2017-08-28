package edu.illinois.cs.cogcomp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.htmlparser.parserapplications.StringExtractor;
import org.htmlparser.util.ParserException;

/**
 * 
 * @author dxquang
 * 
 */

public class WebPage {

	public String getPage(String urlToRead) {
		URL url; // The URL to read
		HttpURLConnection conn; // The actual connection to the web page
		BufferedReader rd; // Used to read results from the web page
		String line; // An individual line of the web page HTML
		String result = ""; // A long string containing all the HTML
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void getPage(String urlToRead, String htmlFile) {
		String page = getPage(urlToRead);
		BufferedWriter writer = IOManager.openWriter(htmlFile);
		try {
			writer.write(page);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to write to file " + htmlFile);
			System.exit(1);
		}
		IOManager.closeWriter(writer);
	}

	public String parseHTML(String inputHTMLFile) {
		HTMLEditorKit.ParserCallback callBack = new HTMLEditorKit.ParserCallback() {
			public void handleText(char[] data, int pos) {
				System.out.println(data);
			}
		};
		try {
			Reader reader = new FileReader(inputHTMLFile);
			new ParserDelegator().parse(reader, callBack, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Unable to read file " + inputHTMLFile);
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to parse the input file "
					+ inputHTMLFile);
			System.exit(1);
		}
		return null;
	}

	public String parseWebPage(String url) {
		try {
			// if (!AMakeCorpus.isValidDomain(url)) {
			// System.out.println("Invalid domain! url=" + url);
			// return null;
			// }
			StringExtractor sExtractor = new StringExtractor(url);
			String outString = sExtractor.extractStrings(false);
			return outString;
		} catch (ParserException e) {
			e.printStackTrace();
			System.out.println("\tUnable to parse Webpage: " + url);
			System.out
					.println("\tIgnore this webpage, continue with the next one.");
			return null;
		}
	}

	public static void main(String[] args) {
		WebPage webpage = new WebPage();
		System.out.println("URL = " + args[0]);
		String text = webpage.parseWebPage(args[0]);
		if (text == null) {
			System.out.println("NULL");
		} else {
			System.out.println(text);
		}
	}
}
