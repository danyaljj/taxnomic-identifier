package edu.illinois.cs.cogcomp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import edu.illinois.cs.cogcomp.emnlp09.experiments.DatasetCreation;
import edu.illinois.cs.cogcomp.emnlp09.experiments.Example;
import edu.illinois.cs.cogcomp.emnlp09.experiments.michaelstrube.Taxonomy;
import edu.illinois.cs.cogcomp.emnlp09.experiments.rionsnow.WordNetRion;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityCategorization;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityDisambiguation;
import edu.illinois.cs.cogcomp.emnlp09.identification.RelationIdentification;
import edu.illinois.cs.cogcomp.emnlp09.search.TextFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TextTitleFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TitleFieldSearcher;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneSearcher;
import edu.illinois.cs.cogcomp.utils.CommandDescription;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * 
 * @author dxquang Jan 27, 2009
 */

public class AllTests_Emnlp09 {

	@CommandDescription(description = "TextFieldSearch <indexDirectory> <maxCount>\n"
			+ "Examples: TextFieldSearch ../data/wikipedia/pages_xml_indexed_jupiter 100")
	public static void TextFieldSearch(String indexDirectory, String maxCount)
			throws Exception {

		int maxCountVal = Integer.parseInt(maxCount);

		String fields[] = new String[] { "text" };

		TextFieldSearcher searcher = new TextFieldSearcher(fields, true);

		searcher.open(indexDirectory);

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

	@CommandDescription(description = "TitleFieldSearch <indexDirectory> <maxCount>\n"
			+ "Examples: TitleFieldSearch ../data/wikipedia/pages_xml_indexed_jupiter 100")
	public static void TitleFieldSearch(String indexDirectory, String maxCount)
			throws Exception {

		int maxCountVal = Integer.parseInt(maxCount);

		String fields[] = new String[] { "title" };

		TitleFieldSearcher searcher = new TitleFieldSearcher(fields, false);

		searcher.open(indexDirectory);

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

	@CommandDescription(description = "TitleFieldSearchMustNot <indexDirectory> <input: must-not phrase> <maxCount>\n"
			+ "Examples: TitleFieldSearchMustNot ../data/wikipedia/pages_xml_indexed_jupiter \"category\" 10")
	public static void TitleFieldSearchMustNot(String indexDirectory,
			String mustNotQuery, String maxCount) throws Exception {

		int maxCountVal = Integer.parseInt(maxCount);

		String fields[] = new String[] { "title" };

		TitleFieldSearcher searcher = new TitleFieldSearcher(fields, false);

		searcher.open(indexDirectory);

		String query = "";

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter a query (_ to end): ");
		query = in.readLine();

		if (query.equals("_"))
			return;

		do {

			if (query.length() != 0) {

				searcher.setMustNotQuery(mustNotQuery);
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

	@CommandDescription(description = "TextTitleFieldSearch <input: text index directory> <input: numResult>\n"
			+ "For example: TextTitleFieldSearch /scratch/quangdo2/pages_xml_indexed_jupiter_category 10\n")
	public static void TextTitleFieldSearch(String indexDir, String numResult)
			throws Exception {

		String fields[] = new String[] { "text" };

		TextTitleFieldSearcher searcher = new TextTitleFieldSearcher(fields,
				true);

		searcher.open(indexDir);

		String title;
		String text;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the title (_ to end): ");
		title = in.readLine();

		if (title.equals("_"))
			return;

		int maxCountVal = Integer.parseInt(numResult);

		do {

			if (title.length() != 0) {

				System.out.print("Enter the text (_ to end): ");
				text = in.readLine();

				if (text.equals("_"))
					return;

				String query = title + "cogcomp" + text;

				System.out.println("Query: " + query);

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

			System.out.print("Enter the 1st entity (_ to end): ");
			title = in.readLine();

		} while (!title.equals("_"));

	}

	@CommandDescription(description = "TitleDisambiguation <input: text index directory> <input: titleMapping>\n"
			+ "For example: TitleDisambiguation ../data/wikipedia/pages_xml_indexed_jupiter /scratch/quangdo2/bigramTitleMapping.txt\n")
	public static void TitleDisambiguation(String indexDir, String titleMapping)
			throws Exception {

		EntityDisambiguation disambiguator = new EntityDisambiguation(indexDir,
				titleMapping);

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

				// disambiguator.collectInfoDisambiguation(entity1, entity2);
				disambiguator.collectInfo(entity1, entity2);

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));

	}

	@CommandDescription(description = "EntityCategorizing "
			+ "<input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: K>\n"
			+ "For example: EntityCategorizing ../data/wikipedia/pages_xml_indexed_jupiter "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map"
			+ "/scratch/quangdo2/bigramTitleMapping.map 3\n")
	public static void EntityCategorizing(String indexDir,
			String categoryMapping, String titleMapping, String K)
			throws Exception {

		EntityCategorization categorizer = new EntityCategorization(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));

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

				categorizer.categorize(entity1, entity2);

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));

	}

	@CommandDescription(description = "ExtractCategoryMapping <input: index directory> <output: file>\n"
			+ "For example: ExtractCategoryMapping ../data/wikipedia/pages_xml_indexed_jupiter ../data/wikipedia/categoryMapping.txt\n")
	public static void ExtractCategoryMapping(String indexDir, String outputFile)
			throws Exception {

		String fields[] = new String[] { "title" };

		SimpleLuceneSearcher searcher = new SimpleLuceneSearcher(fields, false);

		searcher.open(indexDir);

		String query = "category";

		ExecutionTimeUtil timer = new ExecutionTimeUtil();

		System.out.println("Searching...");
		timer.start();
		ArrayList<ILuceneResult> results = searcher.search(query, 10000000);
		timer.end();
		System.out.println("Done. " + timer.getTimeSeconds() + " secs.");
		System.out.println("Total hits: " + results.size() + "\t"
				+ searcher.getTotalHits());

		StringBuffer buf = new StringBuffer("");

		int count = 0;

		BufferedWriter writer = IOManager.openWriter(outputFile);

		for (ILuceneResult result : results) {

			String title = result.getTitle();

			if (title.indexOf(':') == -1)
				continue;

			buf.append(result.getTitle() + "\t" + result.getLuceneId() + "\n");

			count++;

			if (count % 1000 == 0) {
				writer.write(buf.toString());
				buf = new StringBuffer("");
			}
		}

		if (count % 1000 != 0) {
			writer.write(buf.toString());
			System.out.println("Wrote " + count + " lines.");
		}

		IOManager.closeWriter(writer);
		System.out.println("Finish.");
	}

	@CommandDescription(description = "ExtractNgramTitleMapping <input: index directory> <input: n> <output: file>\n"
			+ "For example: ExtractNgramTitleMapping /scratch/quangdo2/pages_xml_indexed_jupiter_category 2 /scratch/quangdo2/bigramTitleMapping.txt\n")
	public static void ExtractNgramTitleMapping(String indexDir, String ngram,
			String outputFile) throws Exception {

		IndexReader reader = IndexReader.open(indexDir);

		int n = Integer.parseInt(ngram);

		int numDocs = reader.numDocs();

		System.out.println("Total number of documents: " + numDocs);

		StringBuffer buf = new StringBuffer("");

		int count = 0;

		BufferedWriter writer = IOManager.openWriter(outputFile);

		for (int i = 0; i < numDocs; i++) {

			Document doc = reader.document(i);

			String title = doc.get("title");

			String tokens[] = title.split("\\s+");

			if (tokens.length == 0)
				continue;

			if (tokens.length <= n && title.indexOf(':') == -1
					&& title.indexOf('(') == -1) {

				// buf.append(title + "\t" + i + "\n");

				count++;

				// if (count % 1000 == 0) {
				// writer.write(buf.toString());
				// buf = new StringBuffer("");
				// }

			}

			if (i % 100000 == 0)
				System.out.println("Processed to " + i);
		}

		if (count % 1000 != 0) {
			writer.write(buf.toString());
			System.out.println("Wrote " + count + " lines.");
		}

		IOManager.closeWriter(writer);
		System.out.println("Finish.");
	}

	@CommandDescription(description = "IdentifyRelation "
			+ "<input: index directory> " + "<input: categoryMapping> "
			+ "<input: titleMapping> " + "<input: K>\n"
			+ "For example: IdentifyRelation "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt 3\n")
	public static void IdentifyRelation(String indexDir,
			String categoryMapping, String titleMapping, String K)
			throws Exception {

		RelationIdentification identifier = new RelationIdentification(
				indexDir, categoryMapping, titleMapping, Integer.parseInt(K));

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

				int relation = identifier.identify(entity1, entity2);

				System.out.print("\nPrediction: ");

				switch (relation) {
				case RelationIdentification.NONE:
					System.out.println("NONE");
					break;
				case RelationIdentification.ANCESTOR_E1_TO_E2:
					System.out.println("ANCESTOR_E1_TO_E2");
					break;
				case RelationIdentification.ANCESTOR_E2_TO_E1:
					System.out.println("ANCESTOR_E2_TO_E1");
					break;
				case RelationIdentification.COUSIN:
					System.out.println("COUSIN");
					break;
				default:
					System.out.println("Oops! Something wrong happened.");
					break;
				}

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			entity1 = in.readLine();

		} while (!entity1.equals("_"));

	}

	@CommandDescription(description = "GenerateOfflineData "
			+ "<input: index directory> " + "<input: categoryMapping> "
			+ "<input: titleMapping> " + "<input: Gold example file> "
			+ "<input: K> " + "<output: Intermediate example file>\n"
			+ "For example: GenerateOfflineData "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/emnlp09/test/all_100.gold 3 "
			+ "../data/emnlp09/test/all_100.inter" + "\n")
	public static void GenerateOfflineData(String indexDir,
			String categoryMapping, String titleMapping, String inputFile, String K,
			String outputFile) throws Exception {

		RelationIdentification identifier = new RelationIdentification(
				indexDir, categoryMapping, titleMapping, Integer.parseInt(K));

		ArrayList<Example> arrInputExamples = DatasetCreation.readExampleFile(
				inputFile, DatasetCreation.INPUT_TYPE_GOLD);

		ArrayList<Example> arrOutputExamples = new ArrayList<Example>();

		System.out.println("Total # of examples: " + arrInputExamples.size());

		System.out.println("Generating offline data...");

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		int i = 1;

		for (Example example : arrInputExamples) {

			System.out.println("[" + i + "] " + example.entity1 + " - "
					+ example.entity2);

			Example newExample = identifier
					.generateIntermediateExample(example);

			arrOutputExamples.add(newExample);

			i++;

		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs");

		System.out.println("Flushing to file...");

		ArrayList<String> arrStringExamples = DatasetCreation
				.makeStringExamples(arrOutputExamples,
						DatasetCreation.INPUT_TYPE_INTERMEDIATE);

		IOManager.writeLines(arrStringExamples, outputFile);

		System.out.println("Done.");
	}

	@CommandDescription(description = "CreateDataset <input: class instance file> <input: numPairs> <input: type> <output: outputFile>\n"
			+ "(type=1: greatePosEntityEntityExamples; type=2: generatePosClassEntityExamples; type=3: generateNegExamples)\n"
			+ "For example: CreateDataset "
			+ "../data/emnlp09/www07-classes.txt 1000 1 ../data/emnlp09/K3/1000_posCousin.gold\n")
	public static void CreateDataset(String classInstanceFile, String numPairs,
			String type, String outputFile) throws Exception {

		DatasetCreation creator = new DatasetCreation(classInstanceFile);

		int num = Integer.parseInt(numPairs);

		int option = Integer.parseInt(type);

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		System.out.println("Generating examples...");

		switch (option) {
		case 1:
			creator.generatePosEntityEntityExamples(outputFile, num);
			break;

		case 2:
			creator.generatePosClassEntityExamples(outputFile, num);
			break;

		case 3:
			creator.generateNegExamples(outputFile, num);
			break;

		default:
			System.out.println("Please input the type = 1, 2, or 3.");
			break;
		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs.");

	}

	@CommandDescription(description = "SplitIntermediaFile <input: intermediate file>\n"
			+ "For example: SplitIntermediaFile "
			+ "../data/emnlp09/K1/try1/6000_posCposAnegCA.inter\n")
	public static void SplitIntermediaFile(String intermediateFile)
			throws Exception {

		System.out.println("Splitting...");
		DatasetCreation.splitIntermediateFile(intermediateFile);
		System.out.println("Done.");

	}

	@CommandDescription(description = "TuneParameters <input: intermediate file> <output: outputFile>\n"
			+ "For example: TuneParameters "
			+ "../data/emnlp09/K3/try1/test.inter ../data/emnlp09/K3/try1/test.param\n")
	public static void TuneParameters(String intermediateFile, String outputFile)
			throws Exception {

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		System.out.println("Tunning parameters...");

		RelationIdentification identifier = new RelationIdentification();
		identifier.tune(intermediateFile, outputFile);

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs.");

	}

	@CommandDescription(description = "Evaluate <input: intermediate file> <input: param file> <output: outputFile>\n"
			+ "For example: Evaluate "
			+ "../data/emnlp09/K3/try1/4000_posCposAnegCA.inter "
			+ "../data/emnlp09/K3/try1/1000_tune.param "
			+ "../data/emnlp09/K3/try1/4000_posCposAnegCA.predict\n")
	public static void Evaluate(String intermediateFile, String paramFile,
			String outputFile) throws Exception {

		BufferedReader reader = IOManager.openReader(paramFile);

		String line;

		double alpha = -1;
		double beta = -1;
		double thetaAnc = -1;
		double thetaCou = -1;
		double ratio1 = -1;
		double ratio2 = -1;

		while ((line = reader.readLine()) != null) {

			line = line.trim();

			if (line.length() == 0)
				continue;

			int posB = line.indexOf(':');

			if (line.startsWith("alpha:"))
				alpha = Double.parseDouble(line.substring(posB + 1).trim());
			if (line.startsWith("beta:"))
				beta = Double.parseDouble(line.substring(posB + 1).trim());
			if (line.startsWith("thetaAnc:"))
				thetaAnc = Double.parseDouble(line.substring(posB + 1).trim());
			if (line.startsWith("thetaCou:"))
				thetaCou = Double.parseDouble(line.substring(posB + 1).trim());
			if (line.startsWith("ratio1:"))
				ratio1 = Double.parseDouble(line.substring(posB + 1).trim());
			if (line.startsWith("ratio2:"))
				ratio2 = Double.parseDouble(line.substring(posB + 1).trim());

		}

		if (alpha == -1 || beta == -1 || thetaAnc == -1 || thetaCou == -1
				|| ratio1 == -1 || ratio2 == -1) {
			System.out
					.println("Unable to initialize all parameters. Please check the param file.");
			System.exit(1);
		}

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		System.out.println("Evaluating...");

		RelationIdentification identifier = new RelationIdentification();

		identifier.setParameters(alpha, beta, thetaAnc, thetaCou, ratio1,
				ratio2);

		identifier.evaluate(intermediateFile, outputFile);

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs.");

	}

	@CommandDescription(description = "StrubeTaxonomy <input: conceptFile> <input: isaFile> <input: intermediateFile>\n"
			+ "For example: StrubeTaxonomy "
			+ "../data/emnlp09/MichaelStrube07/original/concepts.txt "
			+ "../data/emnlp09/MichaelStrube07/original/isa.txt "
			+ "../data/emnlp09/K3/try1/4000_posCposAnegCA.inter\n")
	public static void StrubeTaxonomy(String conceptFile, String isaFile,
			String interFile) throws Exception {

		Taxonomy taxonomy = new Taxonomy(conceptFile, isaFile);

		taxonomy.evaluate(interFile);

	}

	@CommandDescription(description = "WordNetRionEval <input: input test file> <input: target mapping file> <output: result file> <input: task - (1) for ParentChild, (2) for Sibling, (3) for All>\n"
			+ "For example: WordNetRionEval ../data/evaluation/siblingEval1.txt ../data/evaluation/www07-classes-modified/www07-targetclass.txt ../data/evaluation/baselineWikiEvalResult.xml 1")
	public static void WordNetRionEval(String inputTestFile, String targetFile,
			String resultFile, String task) throws Exception {

		WordNetRion wordnet = new WordNetRion(inputTestFile);
		wordnet.setTargetClassMapper(targetFile);
		wordnet.evaluate(resultFile, Integer.parseInt(task));

	}

}
