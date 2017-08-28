/**
 * 
 */
package edu.illinois.cs.cogcomp.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import edu.illinois.cs.cogcomp.category.CategoryMatching;
import edu.illinois.cs.cogcomp.category.LexicalMatching;
import edu.illinois.cs.cogcomp.detection.CateAnalyzer;
import edu.illinois.cs.cogcomp.detection.EntityInfo;
import edu.illinois.cs.cogcomp.detection.JaccardFilter;
import edu.illinois.cs.cogcomp.detection.RelationDetector;
import edu.illinois.cs.cogcomp.detection.TfIdfFilter;
import edu.illinois.cs.cogcomp.evaluation.BaselineWikiEval;
import edu.illinois.cs.cogcomp.evaluation.BaselineWordNetRion;
import edu.illinois.cs.cogcomp.evaluation.MakeExamples;
import edu.illinois.cs.cogcomp.evaluation.MappingTargetClass;
import edu.illinois.cs.cogcomp.evaluation.ParentChildEval;
import edu.illinois.cs.cogcomp.evaluation.SPCEval;
import edu.illinois.cs.cogcomp.evaluation.SiblingEval;
import edu.illinois.cs.cogcomp.evaluation.TargetClassMapping;
import edu.illinois.cs.cogcomp.indexer.DocIndexer;
import edu.illinois.cs.cogcomp.lucenesearch.Category;
import edu.illinois.cs.cogcomp.lucenesearch.CategorySearcher;
import edu.illinois.cs.cogcomp.lucenesearch.HitCountSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.SearcherFactory;
import edu.illinois.cs.cogcomp.lucenesearch.TitleSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.SearcherFactory.SearcherType;
import edu.illinois.cs.cogcomp.parser.WikiDumpParser;
import edu.illinois.cs.cogcomp.relation.AMakeCorpus;
import edu.illinois.cs.cogcomp.relation.MakeSimpleCorpus;
import edu.illinois.cs.cogcomp.relation.Prototype;
import edu.illinois.cs.cogcomp.retrieval.ARetriever;
import edu.illinois.cs.cogcomp.retrieval.RetrieverFactory;
import edu.illinois.cs.cogcomp.retrieval.RetrieverFactory.RetrieverType;
import edu.illinois.cs.cogcomp.utils.CommandDescription;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.MySentenceSplitter;
import edu.illinois.cs.cogcomp.utils.ProminentConcepts;
import edu.illinois.cs.cogcomp.utils.WebPage;
import edu.illinois.cs.cogcomp.utils.WordNetManager;
import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.yahoo.YahooSearch;

/**
 * @author dxquang Apr 15, 2009
 */
public class TestOutdated {

	@CommandDescription(description = "TestYahooSearch <input: query> <intput: numResult>\n"
			+ "For example: java -jar Jupiter.jar TestYahooSearch microsoft 10")
	public static void TestYahooSearch(String query, String numResult)
			throws Exception {
		YahooSearch ySearch = new YahooSearch();
		int numResultInt = Integer.parseInt(numResult);
		ArrayList<IResult> arrResult = ySearch.searchWeb(query, numResultInt);
		System.out.println();
		int i = 1;
		for (IResult result : arrResult) {
			System.out.println(i + ". Title: " + result.getTitle());
			System.out.println("Snippet: " + result.getSnippet());
			System.out.println("Url: " + result.getUrl());
			System.out.println();
			i++;
		}
	}

	@CommandDescription(description = "Retriever <RetrieverType> <input: entity1> <input: entity2> <intput: numResult> <output: XML file>\n"
			+ "Example:\n"
			+ "\tjava -jar Jupiter.jar Retriever SimpleRetrieverEnum microsoft \"bill gates\" 10 ../data/tmp/retriever\n")
	public static void Retriever(String retrieverType, String entity1,
			String entity2, String numResult, String outputXMLFile)
			throws Exception {

		RetrieverType type = null;
		try {
			type = RetrieverType.valueOf(retrieverType);
		} catch (Exception ex) {
			String output = "\nInvalid retriever type. List of allowed values: \n";
			for (RetrieverType allowedType : RetrieverType.values()) {
				output += ("\t" + allowedType + "\n");
			}
			throw new Exception(output);
		}

		int numResultInt = Integer.parseInt(numResult);
		// System.out.println("NumResult = " + numResult);

		ARetriever retriever = RetrieverFactory.getRetriever(type);

		String madeQuery = retriever.makeQuery(entity1);
		retriever.retrieve(madeQuery, numResultInt);
		retriever.exportXML(outputXMLFile + "_"
				+ retriever.makeFileName(entity1) + ".txt");

		madeQuery = retriever.makeQuery(entity2);
		retriever.retrieve(madeQuery, numResultInt);
		retriever.exportXML(outputXMLFile + "_"
				+ retriever.makeFileName(entity2) + ".txt");

		madeQuery = retriever.makeQuery(entity1, entity2);
		retriever.retrieve(madeQuery, numResultInt);
		retriever.exportXML(outputXMLFile + "_"
				+ retriever.makeFileName(entity1, entity2) + ".txt");

	}

	@CommandDescription(description = "TestGetWebPage <input: URL> <output: htmlFile>\n"
			+ "For example: java -jar Jupiter.jar TestGetWebPage http://www.yahoo.com ../data/tmp/yahoo.html")
	public static void TestGetWebPage(String url, String htmlFile)
			throws Exception {
		WebPage webPage = new WebPage();
		webPage.getPage(url, htmlFile);
	}

	@CommandDescription(description = "TestParseHTML <input: url>\n"
			+ "For example: java -jar Jupiter.jar TestParseHTML http://www.yahoo.com")
	public static void TestParseHTML(String url) throws Exception {
		WebPage webpage = new WebPage();
		System.out.println("URL = " + url);
		String text = webpage.parseWebPage(url);
		System.out.println(text);
	}

	@CommandDescription(description = "SentenceSegmentation <input: fileName> <output: fileName>\n"
			+ "For example: java -jar Jupiter.jar SentenceSegmentation ../data/tmp/test.txt ../data/tmp/test.split")
	public static void SentenceSegmentation(String inputFile, String outputFile)
			throws Exception {
		MySentenceSplitter sentSplitter = new MySentenceSplitter();
		// sentSplitter.splitTextLBJ(inputFile, outputFile);
		String content = IOManager.readContent(inputFile);
		// System.out.println(content);
		// System.out.println();
		// System.out.println();
		ArrayList<String> arrSentences = sentSplitter.splitTextLBJ(content);
		IOManager.writeLinesAddingReturn(arrSentences, outputFile);
	}

	@CommandDescription(description = "MakeCorpus <input: prototype file> <output: output directory> <input: numResults>\n"
			+ "For example: java -jar Jupiter.jar MakeCorpus ../data/SimpleCorpus/training.entitypair.txt ../data/SimpleCorpus/test 1000")
	public static void MakeCorpus(String inputPrototypeFile,
			String outputDirectory, String numResult) throws Exception {
		AMakeCorpus corpusMaker = new MakeSimpleCorpus(outputDirectory, Integer
				.parseInt(numResult));
		ArrayList<Prototype> arrPrototypes = corpusMaker
				.parsePrototype(inputPrototypeFile);
		corpusMaker.makeCorpus(arrPrototypes);
	}

	@CommandDescription(description = "DocIndexer <docInfoType> <input TREC file> <output index directory>"
			+ "\tThis is a common tester for all docIndexer. Any docInfo that can be generated by \n"
			+ "\tthe DocInfoFactory and can be tested by this function. \n\n"
			+ "Examples:\n\n"
			+ "\t DocIndexer Simple ../data/wikipedia/pages_xml_parsed/wikipedia.full.parsed.xml ../data/wikipedia/index_allpage\n\n"
			+ "\t DocIndexer List ../data/wikipedia/pages_xml_parsed/wikipedia.full.parsed.xml ../data/wikipedia/index_listof\n\n"
			+ "\t DocIndexer Category ../data/wikipedia/pages_xml_parsed/wikipedia.full.parsed.xml ../data/wikipedia/index_category\n\n")
	public static void DocIndexer(String docInfoType, String inputFile,
			String outputDir) {

		DocIndexer docIndexer = new DocIndexer(docInfoType, inputFile,
				outputDir);

		Date start = new Date();
		docIndexer.indexDocs();
		Date end = new Date();

		System.out.println("Time: " + (end.getTime() - start.getTime())
				/ (float) 1000 + " secs.");
	}

	@CommandDescription(description = "RetrievalTester <searcherType> <indexDirectory> <maxCount>\n"
			+ "\tThis is a common tester for all retrievers. Any retriever that can be generated by \n"
			+ "\tthe SearcherFactory can be tested by this function. \n\n"
			+ "Examples:\n\n"
			+ "\t RetrievalTester SimpleSearch ../data/wikipedia/index_listof 10\n\n"
			+ "\t RetrievalTester ListSearch ../data/wikipedia/index_listof 10\n\n"
			+ "\t RetrievalTester CategorySearch ../data/wikipedia/index_category 10\n\n"
			+ "\t RetrievalTester TitleSearch ../data/wikipedia/index_allpage 10\n\n"
			+ "\t RetrievalTester TextSearch ../data/wikipedia/index_allpage_lowercase 100\n\n")
	public static void RetrievalTester(String searcherType,
			String indexDirectory, String maxCount) throws Exception {
		int maxCountVal = Integer.parseInt(maxCount);

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("TotalResultsToRerank", "1000");

		SearcherType type = null;
		try {
			type = SearcherType.valueOf(searcherType);
		} catch (Exception ex) {
			String output = "\nInvalid searcher type. List of allowed values: \n";
			for (SearcherType allowedType : SearcherType.values()) {
				output += ("\t" + allowedType + "\n");
			}
			throw new Exception(output);
		}

		ILuceneSearcher searcher = SearcherFactory.getSearcher(type,
				indexDirectory, "config.xml", parameters);

		String query = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter a query (_ to end): ");
		query = in.readLine();

		if (query.equals("_"))
			return;

		do {

			if (query.length() != 0) {
				ArrayList<ILuceneResult> results = searcher.search(query,
						maxCountVal);

				int i = 1;
				for (ILuceneResult result : results) {
					System.out.println("[" + i + "] " + result.getTitle()
							+ " (" + result.getScore() + ")");
					System.out.println("\t" + "Category: "
							+ result.getCategory());
					i++;

					if (i > maxCountVal)
						break;
				}

			}
			System.out.println("Total hits: " + searcher.getTotalHits());
			System.out.print("Enter a query (_ to end): ");
			query = in.readLine();
		} while (!query.equals("_"));

		searcher.close();
	}

	@CommandDescription(description = "TitleSearchByProminence <indexDirectory> <input: true|flase = prominence|no> <maxCount>\n"
			+ "Example: TitleSearchByProminence ../data/wikipedia/index_withtext/ true 10")
	public static void TitleSearchByProminence(String indexDirectory,
			String useProminence, String maxCount) throws Exception {

		int maxCountVal = Integer.parseInt(maxCount);

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("TotalResultsToRerank", "1000");

		TitleSearcher searcher = new TitleSearcher(null, true);
		searcher.open(indexDirectory);
		searcher.setSortByProminence(Boolean.parseBoolean(useProminence));

		String query = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter a query (_ to end): ");
		query = in.readLine();

		if (query.equals("_"))
			return;

		do {

			if (query.length() != 0) {
				ArrayList<ILuceneResult> results = searcher.search(query,
						maxCountVal);

				int i = 1;
				for (ILuceneResult result : results) {
					System.out.println("[" + i + "] " + result.getTitle()
							+ " (" + result.getScore() + ")");
					// System.out.println("\t" + "Category: "
					// + result.getCategory());
					i++;

					if (i > maxCountVal)
						break;
				}

			}

			System.out.print("Enter a query (_ to end): ");
			query = in.readLine();
		} while (!query.equals("_"));

		searcher.close();
	}

	@CommandDescription(description = "WikiDumpParserFile <input xml file> <output file>")
	public static void WikiDumpParserFile(String inputFile, String outputFile) {
		Date start = new Date();
		WikiDumpParser dumpParser = new WikiDumpParser(inputFile);
		dumpParser.setOutputFileName(outputFile);
		dumpParser.parse();
		Date end = new Date();
		System.out.println("Done. " + (end.getTime() - start.getTime())
				/ (float) 1000);
	}

	@CommandDescription(description = "WikiDumpParserFolder <input folder> <output folder (different with the input folder)>")
	public static void WikiDumpParserFolder(String inputFolder,
			String outputFolder) {
		String[] files = IOManager.listDirectory(inputFolder);
		int n = files.length;
		System.out.println("There are " + n + " files in the directory.");
		for (int i = 0; i < n; i++) {
			String inputFile = files[i];
			String outputFile = outputFolder + "/" + inputFile + ".parsed.xml";
			if (IOManager.isFileExist(outputFile)) {
				System.out
						.println("The outputFile already exists. Moving to the next file in the folder.");
				continue;
			}
			inputFile = inputFolder + "/" + inputFile;
			System.out.println("InputFile=" + inputFile + "; OutputFile="
					+ outputFile);
			Date start = new Date();
			WikiDumpParser dumpParser = new WikiDumpParser(inputFile);
			dumpParser.setOutputFileName(outputFile);
			dumpParser.parse();
			Date end = new Date();
			System.err.println("Done. " + (end.getTime() - start.getTime())
					/ (float) 1000);
		}
	}

	@CommandDescription(description = "CategoryMatcher <input: title index directory> <input: category index directory> <input: useProminence?> <input: useMutualInformation?>\n"
			+ "For example: CategoryMatcher ../data/wikipedia/index_withtext ../data/wikipedia/index_category true true")
	public static void CategoryMatcher(String titleIndexDir,
			String categoryIndexDir, String useProminence,
			String useMutualInformation) throws Exception {

		CategoryMatching catMatcher = new CategoryMatching(titleIndexDir,
				categoryIndexDir, Boolean.parseBoolean(useProminence), Boolean
						.parseBoolean(useMutualInformation));

		String entity1;
		String entity2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {
				System.out.print("Enter the 2nd entity (_ to end): ");
				entity2 = in.readLine();

				if (entity2.equals("_"))
					return;

				catMatcher.matchCategory(entity1, entity2, false);
			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));
	}

	@CommandDescription(description = "ExtractProminentTitles <input: index directory> <output: outputFile>\n"
			+ "For example: ExtractProminentTitles ../data/wikipedia/index_withtext ../data/wikipedia/prominentTitles.txt")
	public static void ExtractProminentTitles(String indexDir, String outputFile)
			throws Exception {
		ProminentConcepts proConcepts = new ProminentConcepts(indexDir);
		proConcepts.extractProminentTitles(outputFile);
		System.out.println("Done.");
	}

	@CommandDescription(description = "HitCountSearch <input: index directory> <input: 1 or 2 entities?>\n"
			+ "For example: HitCountSearch ../data/wikipedia/index_withtext 2")
	public static void HitCountSearch(String indexDir, String stringNumEntities)
			throws Exception {

		HitCountSearcher hcSearcher = new HitCountSearcher(indexDir);
		int numEntities = Integer.parseInt(stringNumEntities);

		String entity1;
		String entity2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {

				if (numEntities == 2) {

					System.out.print("Enter the 2nd entity (_ to end): ");
					entity2 = in.readLine();

					if (entity2.equals("_"))
						return;

					int totalHits = hcSearcher.searchTextForTotalHits(entity1,
							entity2);
					double pwMI = hcSearcher.pointwiseMutualInformation(
							entity1, entity2);

					System.out.println("Total hits: " + totalHits);
					System.out.println("Pointwise mutual information: " + pwMI);

				} else {

					int totalHits = hcSearcher.searchTextForTotalHits(entity1);
					System.out.println("Total hits: " + totalHits);

				}
			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));
	}

	@CommandDescription(description = "GenerateEvaluationData <input: data file> <output: outputFile> <input: number of pairs> <input: Positive(1) or Negative(2) examples?>\n"
			+ "For example: GenerateEvaluationData ../data/evaluation/testdata.txt ../data/evaluation/randtestdata.txt 5 1\n"
			+ "GenerateEvaluationData ../data/evaluation/testdata.txt ../data/evaluation/randtestdata.txt 200 2")
	public static void GenerateEvaluationData(String inputTestData,
			String outputFile, String numPairs, String type) throws Exception {

		MakeExamples maker = new MakeExamples(inputTestData);

		if (Integer.parseInt(type) == 1) {
			maker.generateExamples(outputFile, Integer.parseInt(numPairs));
		} else {
			maker.generateNegExamples(outputFile, Integer.parseInt(numPairs));
		}
	}

	@CommandDescription(description = "SiblingEvaluation <input: input test file> <input: titleIndexDir> <input: categoryIndexDir> <input: useProminence> <input: useMutualInformation> <output: result file>\n"
			+ "For example: SiblingEvaluation ../data/evaluation/randtestdata.txt ../data/wikipedia/index_withtext ../data/wikipedia/index_category true true ../data/wikipedia/siblingEvalResult.xml")
	public static void SiblingEvaluation(String inputTestFile,
			String titleIndexDir, String categoryIndexDir,
			String useProminence, String useMutualInformation, String resultFile)
			throws Exception {
		SiblingEval sibEval = new SiblingEval(inputTestFile, titleIndexDir,
				categoryIndexDir, Boolean.parseBoolean(useProminence), Boolean
						.parseBoolean(useMutualInformation));
		sibEval.evaluate(resultFile);
	}

	@CommandDescription(description = "BaselineWikiEvaluation <input: input test file> <input: listofIndexDir> <input: numResult> <output: result file>\n"
			+ "For example: BaselineWikiEvaluation ../data/evaluation/siblingEval1.txt ../data/wikipedia/index_listof 10 ../data/evaluation/baselineWikiEvalResult.xml")
	public static void BaselineWikiEvaluation(String inputTestFile,
			String listofIndexDir, String numResult, String resultFile)
			throws Exception {
		BaselineWikiEval baseline = new BaselineWikiEval(inputTestFile,
				listofIndexDir, Integer.parseInt(numResult));
		baseline.evaluate(resultFile);
	}

	@CommandDescription(description = "ExtractAllCategories <input: title index directory> <input: category index directory> <input: useProminence?>\n"
			+ "For example: ExtractAllCategories ../data/wikipedia/index_withtext ../data/wikipedia/index_category true")
	public static void ExtractAllCategories(String titleIndexDir,
			String categoryIndexDir, String useProminence) throws Exception {

		CategoryMatching catMatcher = new CategoryMatching(titleIndexDir,
				categoryIndexDir, Boolean.parseBoolean(useProminence), true);

		String entity1;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {

				Map<String, Category> mapProminentsTitles = new HashMap<String, Category>();
				Set<Category> setCats = catMatcher.extractAllCategories(
						entity1, mapProminentsTitles);
				int i = 1;
				for (Category c : setCats) {
					System.out.println("[" + i + "] " + c.category
							+ ", level: " + c.upLevel);
					i++;
				}

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));
	}

	@CommandDescription(description = "GeneralizeTitle <input: title index directory> <input: category index directory> <input: useProminence?>\n"
			+ "For example: GeneralizeTitle ../data/wikipedia/index_withtext ../data/wikipedia/index_category true")
	public static void GeneralizeTitle(String titleIndexDir,
			String categoryIndexDir, String useProminence) throws Exception {

		CategoryMatching catMatcher = new CategoryMatching(titleIndexDir,
				categoryIndexDir, Boolean.parseBoolean(useProminence), true);

		LexicalMatching lexMatcher = new LexicalMatching();

		String entity1;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {

				Map<String, Category> mapProminentsTitles = new HashMap<String, Category>();
				Set<Category> setCats = catMatcher.extractAllCategories(
						entity1, mapProminentsTitles);
				int i = 1;
				for (Category c : setCats) {
					String outString = "[" + i + "] " + c.category
							+ ", level: " + c.upLevel;

					String generalizeTitle = lexMatcher
							.generalizeTitle(c.category);

					if (generalizeTitle.length() > 0)
						outString = outString + " * Generalized title: "
								+ generalizeTitle;

					System.out.println(outString);

					i++;
				}

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));
	}

	@CommandDescription(description = "TestWordNetManager <input word>")
	public static void TestWordNetManager(String inputWord)
			throws JWNLException {
		WordNetManager wnManager = new WordNetManager("file_properties.xml");

		// ---------------
		ArrayList<String> arrSynonym = wnManager.getAllSynonym(inputWord);
		String outString = "";
		for (int i = 0; i < arrSynonym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrSynonym.get(i);
		}
		if (arrSynonym.isEmpty())
			System.out.println("There is no synonym.");
		else
			System.out.println("All synonyms of \"" + inputWord
					+ "\" (including all possible POS and Senses):\n"
					+ outString);
		// ---------------
		ArrayList<String> arrHypernym = wnManager.getAllHypernym(inputWord);
		outString = "";
		for (int i = 0; i < arrHypernym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrHypernym.get(i);
		}
		if (arrHypernym.isEmpty())
			System.out.println("There is no hypernym");
		else
			System.out.println("All hypernyms of \"" + inputWord
					+ "\" (including all possible POS and Senses):\n"
					+ outString);
		// ---------------
		ArrayList<String> arrHyponym = wnManager.getAllHyponym(inputWord);
		outString = "";
		for (int i = 0; i < arrHyponym.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrHyponym.get(i);
		}
		if (arrHyponym.isEmpty())
			System.out.println("There is no hyponym");
		else
			System.out.println("All hyponyms of \"" + inputWord
					+ "\" (including all possible POS and Senses):\n"
					+ outString);
		// ---------------
		ArrayList<String> arrMorph = wnManager.getAllMorph(inputWord);
		outString = "";
		for (int i = 0; i < arrMorph.size(); i++) {
			if (i > 0)
				outString += ", ";
			outString += arrMorph.get(i);
		}
		System.out.println("All morphs of \"" + inputWord
				+ "\" (including all possible POS and Senses):\n" + outString);
	}

	@CommandDescription(description = "GetEntityInfo <input: title index directory> <input: category index directory> <input: useProminence?>\n"
			+ "For example: GetEntityInfo ../data/wikipedia/index_allpages_lowercase ../data/wikipedia/index_category true")
	public static void GetEntityInfo(String titleIndexDir, String catIndexDir,
			String useProminence) throws Exception {

		WordNetManager wnManager = new WordNetManager("file_properties.xml");

		TitleSearcher titleSearcher = new TitleSearcher(null, false);
		titleSearcher.open(titleIndexDir);
		titleSearcher.setSortByProminence(Boolean.parseBoolean(useProminence));

		CategorySearcher catSearcher = new CategorySearcher(null, false);
		catSearcher.open(catIndexDir);

		String entity;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		CateAnalyzer analyzer = new CateAnalyzer();

		System.out.print("Enter the 1st entity (_ to end): ");
		entity = in.readLine();

		if (entity.equals("_"))
			return;

		do {

			// lexical
			entity = entity.replaceAll("\\p{Punct}", " ");
			entity = entity.replaceAll("\\s+", " ");
			entity = entity.toLowerCase();
			entity = entity.trim();

			// morphology
			ArrayList<String> arrMorphs = null;
			if (entity.indexOf(" ") == -1
					&& entity.matches(".*\\d+.*") == false) {
				arrMorphs = wnManager.getMorph(POS.NOUN, entity);
				if (arrMorphs.size() == 1)
					entity = arrMorphs.get(0);
			}

			if (entity.length() != 0) {

				EntityInfo eInfo = new EntityInfo(entity);
				eInfo.collectInfo(titleSearcher, catSearcher, analyzer,
						RelationDetector.TASK_PARENTCHILD);
				System.out.println(eInfo.toString());

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity = in.readLine();

		} while (!entity.equals("_"));

	}

	@CommandDescription(description = "RelationDetection <input: title index directory> <input: category index directory> <input: peopleFileName> <input: useProminence?> <input: useMutualInformation?> <input: task>\n"
			+ "For example: RelationDetection ../data/wikipedia/index_allpage_lowercase ../data/wikipedia/index_category_lowercase ../data/utilities/people.txt true true 1")
	public static void RelationDetection(String titleIndexDir,
			String categoryIndexDir, String peopleFileName,
			String useProminence, String useMutualInformation, String task)
			throws Exception {

		RelationDetector detector = new RelationDetector(titleIndexDir,
				categoryIndexDir);

		detector.setUseProminence(Boolean.parseBoolean(useProminence));

		detector.setPeopleManager(peopleFileName);

		String entity1;
		String entity2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {
				System.out.print("Enter the 2nd entity (_ to end): ");
				entity2 = in.readLine();

				if (entity2.equals("_"))
					return;

				detector.detectRelation(entity1, entity2, Integer
						.parseInt(task));

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));
	}

	@CommandDescription(description = "SibEval <input: test file> <input: title index directory> <input: category index directory> <input: peopleFileName> <input: useProminence?> <input: useMutualInformation?> <output: outputFile> <input: targetMappingFile>\n"
			+ "For example: SibEval ../data/evaluation/www-2007-modified/input../data/wikipedia/index_allpage_lowercase ../data/wikipedia/index_category_lowercase ../data/utilities/people.txt true true ../data/evaluate/www-2007-modified/output ../data/evaluation/www07-classes-modified/www07-targetclass.txt")
	public static void SibEval(String inputFile, String titleIndexDir,
			String categoryIndexDir, String peopleFileName,
			String useProminence, String useMutualInformation,
			String outputFile, String targetMapFile) throws Exception {

		SiblingEval sibEval = new SiblingEval(inputFile, titleIndexDir,
				categoryIndexDir, peopleFileName, Boolean
						.parseBoolean(useProminence), Boolean
						.parseBoolean(useMutualInformation));

		sibEval.setTargetClassMapper(targetMapFile);

		ExecutionTimeUtil timer = new ExecutionTimeUtil();
		timer.start();
		sibEval.evaluate(outputFile);
		timer.end();
		System.out.println("Eplaped time: " + timer.getTimeSeconds()
				+ " seconds.");
	}

	@CommandDescription(description = "PCEval <input: test file> <input: title index directory> <input: category index directory> <input: peopleFileName> <input: useProminence?> <input: useMutualInformation?> <output: outputFile> <input: targetMappingFile>\n"
			+ "For example: PCEval ../data/evaluation/www-2007-modified/input../data/wikipedia/index_allpage_lowercase ../data/wikipedia/index_category_lowercase ../data/utilities/people.txt true true ../data/evaluate/www-2007-modified/output ../data/evaluation/www07-classes-modified/www07-targetclass.txt")
	public static void PCEval(String inputFile, String titleIndexDir,
			String categoryIndexDir, String peopleFileName,
			String useProminence, String useMutualInformation,
			String outputFile, String targetMapFile) throws Exception {

		ParentChildEval parentChildEval = new ParentChildEval(inputFile,
				titleIndexDir, categoryIndexDir, peopleFileName, Boolean
						.parseBoolean(useProminence), Boolean
						.parseBoolean(useMutualInformation));

		parentChildEval.setTargetClassMapper(targetMapFile);

		ExecutionTimeUtil timer = new ExecutionTimeUtil();
		timer.start();
		parentChildEval.evaluate(outputFile);
		timer.end();
		System.out.println("Eplaped time: " + timer.getTimeSeconds()
				+ " seconds.");
	}

	@CommandDescription(description = "BaselineWordNetRionEval <input: input test file> <input: target mapping file> <output: result file> <input: task - (1) for ParentChild, (2) for Sibling, (3) for All>\n"
			+ "For example: BaselineWordNetRionEval ../data/evaluation/siblingEval1.txt ../data/evaluation/www07-classes-modified/www07-targetclass.txt ../data/evaluation/baselineWikiEvalResult.xml 1")
	public static void BaselineWordNetRionEval(String inputTestFile,
			String targetFile, String resultFile, String task) throws Exception {

		BaselineWordNetRion baseline = new BaselineWordNetRion(inputTestFile);
		baseline.setTargetClassMapper(targetFile);
		baseline.evaluate(resultFile, Integer.parseInt(task));

	}

	@CommandDescription(description = "GenerateNegEvaluationData <input: data file> <output: outputFile> <input: number of pairs> <input: (1) ClassClass, (2) ClassEntity, (3) EntityEntity?>\n"
			+ "For example: GenerateNegEvaluationData ../data/evaluation/testdata.txt ../data/evaluation/randtestdata.txt 100 1\n")
	public static void GenerateNegEvaluationData(String inputTestData,
			String outputFile, String numPairs, String type) throws Exception {

		MakeExamples maker = new MakeExamples(inputTestData);

		maker.generateNegExamples(outputFile, Integer.parseInt(numPairs),
				Integer.parseInt(type));
	}

	@CommandDescription(description = "TestJaccardFilter <input: indexDirectory> <input: numResult>\n"
			+ "For example: TestJaccardFilter ../data/wikipedia/pages_xml_indexed_unnormalized 1000\n")
	public static void TestJaccardFilter(String inputDirectory, String numResult)
			throws Exception {

		JaccardFilter filter = new JaccardFilter(inputDirectory, Integer
				.parseInt(numResult));

		String entity1;
		String entity2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st entity (_ to end): ");
		entity1 = in.readLine();

		if (entity1.equals("_"))
			return;

		do {

			if (entity1.length() != 0) {
				System.out.print("Enter the 2nd entity (_ to end): ");
				entity2 = in.readLine();

				if (entity2.equals("_"))
					return;

				double jaccard = filter.getJaccardScore(entity1, entity2);
				System.out.println("Jaccard score: " + jaccard);
			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));

	}

	@CommandDescription(description = "TestJaccardFilterFile <input: indexDirectory> <input: numResult> <input: inputFile>\n"
			+ "For example: TestJaccardFilterFile ../data/wikipedia/pages_xml_indexed_unnormalized 1000 ../data/evaluation/www07-classes-modified/try2/positivePair.2000.txt\n")
	public static void TestJaccardFilterFile(String inputDirectory,
			String numResult, String inputFile) throws Exception {

		JaccardFilter filter = new JaccardFilter(inputDirectory, Integer
				.parseInt(numResult));

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		int n = arrLines.size();
		int i = 1;
		for (String line : arrLines) {
			String entities[] = line.split("\\t");
			if (entities.length != 2)
				continue;
			System.err.println("[" + i + "/" + n + "] " + line);
			double jaccard = filter.getJaccardScore(entities[0], entities[1]);
			System.out.println(jaccard + "\t" + line);
			i++;
		}

	}

	@CommandDescription(description = "TestTfIdfFilterFile <input: indexDirectory> <input: numResult> <input: idfFile> <input: inputFile>\n"
			+ "For example: TestTfIdfFilterFile ../data/wikipedia/pages_xml_indexed_unnormalized 10 ../data/wikipedia/idf_unnormalized.txt.uniqued ../data/evaluation/www07-classes-modified/try2/positivePair.2000.txt\n")
	public static void TestTfIdfFilterFile(String inputDirectory,
			String numResult, String idfFile, String inputFile)
			throws Exception {

		TfIdfFilter filter = new TfIdfFilter(inputDirectory, Integer
				.parseInt(numResult), idfFile);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		int n = arrLines.size();
		int i = 1;
		for (String line : arrLines) {
			String entities[] = line.split("\\t");
			if (entities.length != 2)
				continue;
			System.err.println("[" + i + "/" + n + "] " + line);
			double jaccard = filter.getFilterScore(entities[0], entities[1]);
			System.out.println(jaccard + "\t" + line);
			i++;
		}

	}

	@CommandDescription(description = "MappingResult <input: resultFile> <input: targetClassFile> <output: outputFile> <input: sign (0 negative, 1 positive)>\n"
			+ "For example: MappingResult \n")
	public static void MappingResult(String resultFile, String targetClass,
			String outputFile, String sign) throws Exception {
		MappingTargetClass mapping = new MappingTargetClass(targetClass,
				Integer.parseInt(sign));
		mapping.mapping(resultFile, outputFile);
	}

	@CommandDescription(description = "AllEval <input: test file> <input: title index directory> <input: category index directory> <input: peopleFileName> <input: useProminence?> <input: useMutualInformation?> <output: outputFile> <input: targetMappingFile>\n"
			+ "For example: AllEval ../data/evaluation/www07-classes-modified/try3_ancestor/www07-classes.pc.pos.first1894.examples ../data/wikipedia/index_allpage_lowercase ../data/wikipedia/index_category_lowercase ../data/utilities/people.txt true true ../data/evaluation/www07-classes-modified/try3_ancestor/result/positive.ancestor.first1894.nomorph.xml ../data/evaluation/www07-classes-modified/www07-targetclass.txt")
	public static void AllEval(String inputFile, String titleIndexDir,
			String categoryIndexDir, String peopleFileName,
			String useProminence, String useMutualInformation,
			String outputFile, String targetMapFile) throws Exception {

		SPCEval allEval = new SPCEval(titleIndexDir, categoryIndexDir,
				peopleFileName, Boolean.parseBoolean(useProminence), Boolean
						.parseBoolean(useMutualInformation));

		allEval.setTargetClassMapper(targetMapFile);

		ExecutionTimeUtil timer = new ExecutionTimeUtil();
		timer.start();
		allEval.evaluate(inputFile, outputFile);
		timer.end();
		System.out.println("Eplaped time: " + timer.getTimeSeconds()
				+ " seconds.");
	}

	@CommandDescription(description = "WordNetChecking <input: www07-classes.txt> <input: target class>\n"
			+ "For example: WordNetChecking input\n")
	public static void WordNetChecking(String inputFile, String targetClass)
			throws Exception {
		WordNetManager wnManager = new WordNetManager("file_properties.xml");

		TargetClassMapping mapping = new TargetClassMapping(targetClass);

		ArrayList<String> arrLines = IOManager.readLines(inputFile);

		for (String line : arrLines) {
			String tokens[] = line.split("\\t");
			String w = tokens[0];
			if (mapping.isValidTargetClass(w))
				w = mapping.getFirstItem(w);
			// System.out.println("---" + line);
			IndexWord word = wnManager.getWord(POS.NOUN, w);
			if (word != null) {
				System.out.println(line);
			}
		}

	}

}
