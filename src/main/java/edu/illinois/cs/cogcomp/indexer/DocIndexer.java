package edu.illinois.cs.cogcomp.indexer;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;

import edu.illinois.cs.cogcomp.indexer.DocInfoFactory.DocInfoType;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * 
 * @author dxquang
 * Feb 3, 2009
 */

public class DocIndexer {
	// =======
	// Constants
	private static final String DOC_TAG = "DOC";
	private static final int COUNT_NUM = 10000;

	// =======
	// Variables
	private String fname;
	private String indexDir;
	private IndexWriter indexer;
	private String[] stopWords = null;
	private DocInfoType type = null;
	private String docInfoType;

	// =======
	public DocIndexer(String docInfoType, String fname, String indexDir) {
		try {
			this.docInfoType = docInfoType;
			this.fname = fname;
			this.indexDir = indexDir;
			if (IOManager.isDirectoryExist(this.indexDir)) {
				System.out
						.println("Notice: The directory "
								+ this.indexDir
								+ " already existed in the system. It is going to be deleted now.");
				IOManager.deleteDirectory(this.indexDir);
			}

			Configuration conf = new Configuration();
			conf.addResource("config.xml");

			String stringStopWords = conf.get("cogcomp.indexer.stopwords");
			stopWords = stringStopWords.split(",+");

			StandardAnalyzer stdAnalyzer = new StandardAnalyzer(stopWords);
			indexer = new IndexWriter(this.indexDir, stdAnalyzer, true);

			try {

				type = DocInfoType.valueOf(docInfoType);

			} catch (Exception ex) {

				String output = "\nInvalid docInfo type. List of allowed values: \n";

				for (DocInfoType allowedType : DocInfoType.values()) {

					output += ("\t" + allowedType + "\n");

				}
				throw new Exception(output);

			}

			indexer.setSimilarity(type.getSimilarityMetric());

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// =======
	public void indexDocs() {

		try {

			BufferedReader reader = IOManager.openReader(fname);

			String line;
			boolean startDoc = false;
			ArrayList<String> arrLines = new ArrayList<String>();
			int i = 0;
			int k = 0;

			while ((line = reader.readLine()) != null) {

				line = line.trim();

				if (line.length() == 0)
					continue;
				if (line.equals("<" + DOC_TAG + ">")) {
					startDoc = true;
				} else if (line.equals("</" + DOC_TAG + ">")) {

					startDoc = false;

					ADocInfo docInfo = type.getDocInfo();

					boolean isValid = docInfo.parse(arrLines);

					if (isValid) {
						Document doc = makeDocument(docInfo);
						indexer.addDocument(doc);
						i++;
					}
					arrLines = null;
					arrLines = new ArrayList<String>();
					k++;
					if ((k % COUNT_NUM) == 0) {
						System.out.println("Indexed " + i + " articles out of "
								+ k);

					}
				}
				if (startDoc)
					arrLines.add(line);
			}

			IOManager.closeReader(reader);

			System.out.println("Finish indexing. " + i
					+ " articles was indexed.");

			System.out.println("Optimizing.");

			indexer.optimize();

			System.out.println("Finish optimizing");

			indexer.close();

			System.out.println("Done.");

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// =======
	public Document makeDocument(ADocInfo docInfo) {
		Document doc = new Document();

		// Id
		Fieldable idField = new Field("id", docInfo.getId(), Field.Store.YES,
				Field.Index.NO);
		doc.add(idField);

		// Title
		Fieldable titleField = new Field("title", docInfo.getTitle(),
				Field.Store.YES, Field.Index.TOKENIZED);
		doc.add(titleField);

		if (docInfoType.equals("Simple") || docInfoType.equals("List")) {
			// Text
			Fieldable textField = new Field("text", docInfo.getText(),
					Field.Store.YES, Field.Index.TOKENIZED,
					Field.TermVector.YES);
			doc.add(textField);
		}

		// Category
		Fieldable categoryField = new Field("category", docInfo.getCategory(),
				Field.Store.YES, Field.Index.NO);
		doc.add(categoryField);

		return doc;
	}

	// ========
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out
					.println("Usage: [program] <type> <input TREC file> <output index directory>");
			System.exit(1);
		}
		DocIndexer docIndexer = new DocIndexer(args[0], args[1], args[2]);
		Date start = new Date();
		docIndexer.indexDocs();
		Date end = new Date();
		System.out.println("Time: " + (end.getTime() - start.getTime())
				/ (float) 1000 + " secs.");
	}
}
