package edu.illinois.cs.cogcomp.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * 
 * @author dxquang Jan 27, 2009
 */

public class WikiDumpParser {
	// ==========
	// Constants
	private static final String PAGE_TAG = "page";
	private static final int BUFFER_SIZE = 100;
	private static final int DOC_COUNT = 10000;

	// ==========
	// Variables
	private String fileName;
	private ArrayList<PageParser> arrPages;
	private BufferedWriter writer;
	private String outputFileName;
	private Configuration conf;
	private int lowerBoundLength;
	private int numOfLink;

	public WikiDumpParser(String fileName) {
		this.fileName = fileName;
		arrPages = new ArrayList<PageParser>();
		writer = null;
		// ------------
		// Read the configuration file
		conf = new Configuration();
		conf.addResource("config.xml");
		String stringLowerBoundLength = conf
				.get("cogcomp.parser.lowerboundlength");
		lowerBoundLength = Integer.parseInt(stringLowerBoundLength);
		String stringNumOfLink = conf.get("cogcomp.parser.numoflink");
		numOfLink = Integer.parseInt(stringNumOfLink);
		System.out.println("Configuration:");
		System.out.println("\tlowerBoundLength = " + lowerBoundLength);
		System.out.println("\tnumOfLink = " + numOfLink);
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
		writer = IOManager.openWriter(this.outputFileName);
	}

	public void parse() {
		try {
			BufferedReader reader = IOManager.openReader(fileName);
			String line;
			boolean startPage = false;
			ArrayList<String> pageContent = new ArrayList<String>();
			int count = 0;
			int validCount = 0;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				if (("<" + PAGE_TAG + ">").equals(line)) {
					startPage = true;
				}
				if (startPage) {
					pageContent.add(line);
				}
				if (("</" + PAGE_TAG + ">").equals(line)) {
					startPage = false;
					PageParser pageParser = new PageParser();
					boolean isValid = pageParser.parser(pageContent,
							lowerBoundLength, numOfLink);
					if (isValid) {
						addToBuffer(pageParser);
						validCount++;
						// System.out.println("ValidCount = " + validCount);
					}
					pageContent = null;
					pageContent = new ArrayList<String>();
					count++;
					if (count % DOC_COUNT == 0) {
						System.out.println("Parsed " + validCount + " out of "
								+ count + " documents.");
					}
				}
			}
			IOManager.closeReader(reader);
			if (arrPages.size() > 0) {
				outputPages();
			}
			if (writer != null)
				IOManager.closeWriter(writer);
			System.out.println("Total: " + validCount + " out of " + count
					+ " documents were parsed.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void addToBuffer(PageParser pageParser) {
		arrPages.add(pageParser);
		if (arrPages.size() == BUFFER_SIZE) {
			outputPages();
			arrPages = null;
			arrPages = new ArrayList<PageParser>();
		}
	}

	private void outputPages() {
		try {
			int n = arrPages.size();
			for (int i = 0; i < n; i++) {
				PageParser page = arrPages.get(i);
				if (page == null)
					continue;
				StringBuffer outString = new StringBuffer("");
				outString.append("<DOC>\n");
				outString.append("<ID>" + page.getId() + "</ID>\n");
				outString.append("<TITLE>" + page.getTitle() + "</TITLE>\n");
				outString.append("<TEXT>" + page.getText() + "</TEXT>\n");
				outString.append("<CATEGORY>" + page.getCategory()
						+ "</CATEGORY>\n");
				outString.append("</DOC>\n");
				if (writer != null) {
					writer.write(outString.toString());
				} else {
					System.out.println(outString.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out
					.println("Usage: [program] <input xml file> <output file> OR\n");
			System.out
					.println("Usage: [program] -d <input folder> <output folder -- should be different than the input folder>\n");
			System.exit(1);
		}
		if (args[0].equals("-d")) {
			String inputFolder = args[1];
			String outputFolder = args[2];
			String[] files = IOManager.listDirectory(inputFolder);
			int n = files.length;
			System.err.println("There are " + n + " files in the directory.");
			for (int i = 0; i < n; i++) {
				String inputFile = files[i];
				String outputFile = outputFolder + "/" + inputFile + ".out.xml";
				inputFile = inputFolder + "/" + inputFile;
				System.err.println("InputFile=" + inputFile + "; OutputFile="
						+ outputFile);
				Date start = new Date();
				WikiDumpParser dumpParser = new WikiDumpParser(inputFile);
				dumpParser.setOutputFileName(outputFile);
				dumpParser.parse();
				Date end = new Date();
				System.err.println("Done. " + (end.getTime() - start.getTime())
						/ (float) 1000);
			}
		} else {
			String inputFile = args[0];
			String outputFile = args[1];
			Date start = new Date();
			WikiDumpParser dumpParser = new WikiDumpParser(inputFile);
			dumpParser.setOutputFileName(outputFile);
			dumpParser.parse();
			Date end = new Date();
			System.out.println("Done. " + (end.getTime() - start.getTime())
					/ (float) 1000);
		}
	}
}
