package edu.illinois.cs.cogcomp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import converters.SQLConverter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import queryprocessing.QueryProcessor;

import edu.illinois.cs.cogcomp.acl10.addfeats.AddFeatsSimpleSupervised;
import edu.illinois.cs.cogcomp.acl10.experiments.AnnotateTECorpus;
import edu.illinois.cs.cogcomp.acl10.experiments.Converter;
import edu.illinois.cs.cogcomp.acl10.experiments.PreparePantelData;
import edu.illinois.cs.cogcomp.acl10.experiments.RecallPrecisionFscore;
import edu.illinois.cs.cogcomp.acl10.experiments.SelectBestRelatedConceptCombination;
import edu.illinois.cs.cogcomp.cikm09.identification.DatasetCreation;
import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.DataHandler;
import edu.illinois.cs.cogcomp.cikm09.learning.FeatureExtraction;
import edu.illinois.cs.cogcomp.cikm09.learning.MainRelationIdentification;
import edu.illinois.cs.cogcomp.cikm09.learning.SimpleSupervised;
import edu.illinois.cs.cogcomp.cikm09.learning.Supervised;
import edu.illinois.cs.cogcomp.cikm09.learning.Unsupervised;
import edu.illinois.cs.cogcomp.cikm09.learning.UnsupervisedModel2;
import edu.illinois.cs.cogcomp.cikm09.setdiscovery.SetDiscovery;
import edu.illinois.cs.cogcomp.cikm09.snow.QuangWordNetSnow;
import edu.illinois.cs.cogcomp.cikm09.snow.WordNetSnow;
import edu.illinois.cs.cogcomp.cikm09.strube.Taxonomy;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityCategorization;
import edu.illinois.cs.cogcomp.emnlp09.identification.EntityDisambiguation;
import edu.illinois.cs.cogcomp.emnlp09.search.TextFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TextTitleFieldSearcher;
import edu.illinois.cs.cogcomp.emnlp09.search.TitleFieldSearcher;
import edu.illinois.cs.cogcomp.jupiter.emnlp10.RTEData;
import edu.illinois.cs.cogcomp.jupiter.emnlp10.distributioanlmemory.DM;
import edu.illinois.cs.cogcomp.jupiter.emnlp10.siblingonly.SiblingOnly_SelectBestRelatedConceptCombination;
import edu.illinois.cs.cogcomp.lucenesearch.ILuceneResult;
import edu.illinois.cs.cogcomp.lucenesearch.SimpleLuceneSearcher;
import edu.illinois.cs.cogcomp.network.RelationClient;
import edu.illinois.cs.cogcomp.network.RelationServer;
import edu.illinois.cs.cogcomp.parser.WikiDumpParserCategory;
import edu.illinois.cs.cogcomp.utils.CommandDescription;
import edu.illinois.cs.cogcomp.utils.ExecutionTimeUtil;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.Permutation;
import edu.illinois.cs.cogcomp.web.IResult;
import edu.illinois.cs.cogcomp.web.yahoo.YahooSearch;
import edu.illinois.cs.cogcomp.www10.constraints.ConstraintEnforcement;
import edu.illinois.cs.cogcomp.www10.constraints.ConstraintLearning;
import edu.illinois.cs.cogcomp.www10.constraints.GoldRelatedConcept;
import edu.illinois.cs.cogcomp.www10.constraints.GraphConstraints;
import edu.illinois.cs.cogcomp.www10.constraints.MainConstraintRelationIdentification;
import edu.illinois.cs.cogcomp.www10.constraints.RelatedConcepts;
import edu.illinois.cs.cogcomp.www10.constraints.SealConstraints;
import edu.illinois.cs.cogcomp.www10.constraints.YagoRelatedConcept;
import edu.illinois.cs.cogcomp.www10.network.ConstraintRelationClient;
import edu.illinois.cs.cogcomp.www10.network.ConstraintRelationServer;
import edu.illinois.cs.cogcomp.www10.yago.EvaluateYago;

/**
 * 
 * @author dxquang Jan 27, 2009
 */

public class AllTests {

	public static final int DEBUG = 0;

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
			+ "For example: TitleDisambiguation /scratch/quangdo2/pages_xml_indexed_jupiter_category /scratch/quangdo2/bigramTitleMapping.txt\n")
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
			+ "<input: index directory> " + "<input: categoryMapping> "
			+ "<input: titleMapping>\n" + "For example: EntityCategorizing "
			+ "../data/wikipedia/pages_xml_indexed_jupiter "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map"
			+ "/scratch/quangdo2/bigramTitleMapping.map " + "3\n")
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
				buf.append(title + "\n");

				count++;

				if (count % 1000 == 0) {
					writer.write(buf.toString());
					buf = new StringBuffer("");
					count = 0;
				}

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

	@CommandDescription(description = "PermuteData <input: inputFile> <output: outputFile>\n"
			+ "For example: PermuteData "
			+ "../data/cikm09/K3/6000_posCposAnegCA.inter "
			+ "../data/cikm09/K3/6000_posCposAnegCA.permute.inter" + "\n")
	public static void PermuteData(String inputFile, String outputFile)
			throws Exception {

		Permutation p = new Permutation();
		p.permute(inputFile, outputFile);

	}

	@CommandDescription(description = "Intermediate2LibSVM <input: inputFile> <output: outputFile>\n"
			+ "For example: Intermediate2LibSVM "
			+ "../data/cikm09/K3/6000_posCposAnegCA.inter "
			+ "../data/cikm09/K3/6000_posCposAnegCA.libsvm" + "\n")
	public static void Intermediate2LibSVM(String inputFile, String outputFile)
			throws Exception {

		DatasetCreation.intermediate2libsvm(inputFile, outputFile);

	}

	@CommandDescription(description = "CreateUnsupervisedData <input: poolFile> <output: outputFile>\n"
			+ "For example: CreateUnsupervisedData "
			+ "../data/cikm09/unsupervised/test.pool "
			+ "../data/cikm09/unsupervised/test.gold" + "\n")
	public static void CreateUnsupervisedData(String poolFile, String outputFile)
			throws Exception {

		DataHandler.createUnsupervisedData(poolFile, outputFile);

	}

	@CommandDescription(description = "CreateUnsupervisedDataPool <input: classInstanceFile> <input: numEach> <output: outputFile>\n"
			+ "For example: CreateUnsupervisedDataPool "
			+ "../data/cikm09/data/www07-classes.txt "
			+ "5 "
			+ "../data/cikm09/data/unsuper_5each.pool" + "\n")
	public static void CreateUnsupervisedDataPool(String classInstanceFile,
			String numEach, String outputFile) throws Exception {

		DataHandler.createUnsupervisedDataPool(classInstanceFile, Integer
				.parseInt(numEach), outputFile);

	}

	@CommandDescription(description = "CreateDataset <input: class instance file> <input: numPairs> <input: type> <output: outputFile>\n"
			+ "(type=0:  generateNegExamples; type=1: generatePosClassEntityExamples; type=2: generatePosEntityClassExamples; type=3: greatePosEntityEntityExamples; type=4: everything)\n"
			+ "For example: CreateDataset "
			+ "../data/emnlp09/www07-classes.txt 5000 3 ../data/emnlp09/K3/1000_posCousin.gold\n")
	public static void CreateDataset(String classInstanceFile, String numPairs,
			String type, String outputFile) throws Exception {

		DatasetCreation creator = new DatasetCreation(classInstanceFile);

		int num = Integer.parseInt(numPairs);

		int option = Integer.parseInt(type);

		ExecutionTimeUtil timmer = new ExecutionTimeUtil();

		timmer.start();

		System.out.println("Generating examples...");

		switch (option) {
		case 0:
			creator.generateNegExamples(outputFile, num);
			break;

		case 1:
			creator.generatePosClassEntityExamples(outputFile, num);
			break;

		case 2:
			creator.generatePosEntityClassExamples(outputFile, num);
			break;

		case 3:
			creator.generatePosEntityEntityExamples(outputFile, num);
			break;

		case 4:
			System.out.println("generating 0...");
			creator.generateNegExamples(outputFile + "0", num);
			System.out.println("generating 1...");
			creator.generatePosClassEntityExamples(outputFile + "1", num);
			System.out.println("generating 2...");
			creator.generatePosEntityClassExamples(outputFile + "2", num);
			System.out.println("generating 3...");
			creator.generatePosEntityEntityExamples(outputFile + "3", num);
			break;

		default:
			System.out.println("Please input the type = 0, 1, 2, or 3.");
			break;
		}

		timmer.end();

		System.out.println("Done. Time: " + timmer.getTimeSeconds() + " secs.");

	}

	@CommandDescription(description = "ExtractFeatures <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Gold example file> "
			+ "<input: K> "
			+ "<output: Intermediate example file>\n"
			+ "For example: ExtractFeatures "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/emnlp09/test/all_100.gold 3 "
			+ "../data/emnlp09/test/all_100.inter" + "\n")
	public static void ExtractFeatures(String indexDir, String categoryMapping,
			String titleMapping, String inputFile, String K, String outputFile)
			throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeatures(inputFile, outputFile);
	}

	@CommandDescription(description = "ExtractFeaturesBigFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Gold example file> "
			+ "<input: K> "
			+ "<input: fromIndex> "
			+ "<input: toIndex> "
			+ "<output: Intermediate example file>\n"
			+ "For example: ExtractFeaturesBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/unsupervised/test.gold 3 "
			+ "0 100 "
			+ "../data/cikm09/unsupervised/test.inter" + "\n")
	public static void ExtractFeaturesBigFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String K, String sFrom, String sTo, String outputFile)
			throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeaturesBigFile(inputFile, Integer.parseInt(sFrom), Integer
				.parseInt(sTo), outputFile);
	}

	@CommandDescription(description = "ExtractFeaturesBigFileNaiveDisamb <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Gold example file> "
			+ "<input: K> "
			+ "<input: fromIndex> "
			+ "<input: toIndex> "
			+ "<output: Intermediate example file>\n"
			+ "For example: ExtractFeaturesBigFileNaiveDisamb "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/unsupervised/test.gold 3 "
			+ "0 100 "
			+ "../data/cikm09/unsupervised/test.inter" + "\n")
	public static void ExtractFeaturesBigFileNaiveDisamb(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String K, String sFrom, String sTo, String outputFile)
			throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeaturesBigFileNaiveDisamb(inputFile, Integer
				.parseInt(sFrom), Integer.parseInt(sTo), outputFile);
	}

	@CommandDescription(description = "ExtractFeaturesBeyondWiki <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Intermediate file> "
			+ "<input: K> "
			+ "<output: Intermediate example file>\n"
			+ "Example: nice java -jar Jupiter.jar ExtractFeaturesBeyondWiki "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/data/K4/20000.new.last12000.shuffled.inter 3 "
			+ "../data/cikm09/data/K4_BeyondWiki/20000.new.last12000.shuffled.inter\n")
	public static void ExtractFeaturesBeyondWiki(String indexDir,
			String categoryMapping, String titleMapping, String interFile,
			String K, String outputFile) throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeaturesBeyondWiki(interFile, outputFile);
	}

	@CommandDescription(description = "UnsupervisedLearning <input: trainFile> <input: trainFile>\n"
			+ "For example: UnsupervisedLearning "
			+ "../data/cikm09/data/8000_Train.inter "
			+ "../data/cikm09/data/12000_Test.inter" + "\n")
	public static void UnsupervisedLearning(String trainFile, String testFile)
			throws Exception {

		Unsupervised learner = new Unsupervised(trainFile, testFile);
		learner.train();

	}

	@CommandDescription(description = "UnsupervisedLearningModel2 <input: trainFile> <input: trainFile>\n"
			+ "For example: UnsupervisedLearningModel2 "
			+ "../data/cikm09/data/8000_Train.inter "
			+ "../data/cikm09/data/12000_Test.inter" + "\n")
	public static void UnsupervisedLearningModel2(String trainFile,
			String testFile) throws Exception {

		UnsupervisedModel2 learner = new UnsupervisedModel2(trainFile, testFile);
		learner.train();

	}

	@CommandDescription(description = "SupervisedLearning <input: trainFile> <input: trainFile>\n"
			+ "For example: SupervisedLearning "
			+ "../data/cikm09/data/8000_Train.inter "
			+ "../data/cikm09/data/12000_Test.inter" + "\n")
	public static void SupervisedLearning(String trainFile, String testFile)
			throws Exception {

		Supervised learner = new Supervised(trainFile, testFile);

		System.out.println("Start training...");
		learner.train();
		System.out.println("Done.");

		System.out.println("\nTesting...");
		double acc = learner.test();
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "SimpleSupervisedLearning <input: trainFile> <input: testFile>\n"
			+ "For example: SimpleSupervisedLearning "
			+ "../data/cikm09/data/8000_Train.inter "
			+ "../data/cikm09/data/12000_Test.inter"
			+ "\n"
			+ "Note: to compile *.lbj, execute the following command: \n"
			+ "java -cp bin:lbj/LBJ2.jar:lbj/LBJ2Library.jar -gsp src -d bin RelationClassifier.lbj")
	public static void SimpleSupervisedLearning(String trainFile,
			String testFile) throws Exception {

		SimpleSupervised learner = new SimpleSupervised(trainFile, testFile);

		System.out.println("Start training...");
		learner.train();
		System.out.println("Done.");

		System.out.println("\nTesting...");
		double acc = learner.test();
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "TestYahooSearch <input: query> <intput: numResult>\n"
			+ "For example: java -jar Jupiter.jar TestYahooSearch microsoft 10")
	public static void TestYahooSearch(String query, String numResult)
			throws Exception {
		YahooSearch ySearch = new YahooSearch();
		int numResultInt = Integer.parseInt(numResult);
		query = "\"" + query + "\"";
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

	@CommandDescription(description = "CreateSetDiscoveryData <input: cohenFile> <input: outputFile>\n"
			+ "For example: CreateSetDiscoveryData "
			+ "../data/cikm09_CohenData/K3/12classes.gold.txt "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.gold" + "\n")
	public static void CreateSetDiscoveryData(String cohenFile,
			String outputFile) throws Exception {

		SetDiscovery setDiscover = new SetDiscovery();
		setDiscover.createAllAncestorRelation(cohenFile, outputFile);

	}

	@CommandDescription(description = "CreateSetDiscoveryDataSeeds <input: cohenFile> <input: seedFile> <input: outputFile>\n"
			+ "For example: CreateSetDiscoveryDataSeeds "
			+ "../data/cikm09_CohenData/K3/12classes.gold.txt "
			+ "../data/cikm09_CohenData/K3/seeds.gold.txt "
			+ "../data/cikm09_CohenData/K3/12classes_cousin.gold" + "\n")
	public static void CreateSetDiscoveryDataSeeds(String cohenFile,
			String seedFile, String outputFile) throws Exception {

		SetDiscovery setDiscover = new SetDiscovery();
		setDiscover.createAllCousinRelation(cohenFile, seedFile, outputFile);

	}

	@CommandDescription(description = "SimpleSupervisedLearningTrain <input: trainFile> <input: testFile>\n"
			+ "For example: SimpleSupervisedLearningTrain "
			+ "../data/cikm09/data/K3/8000_Train.inter "
			+ "../data/cikm09/data/K3/12000_Test.inter" + "\n")
	public static void SimpleSupervisedLearningTrain(String trainFile,
			String testFile) throws Exception {

		SimpleSupervised learner = new SimpleSupervised(trainFile, testFile);

		System.out.println("Start training...");
		learner.train();
		System.out.println("Done.");

		// System.out.println("\nTesting...");
		// double acc = learner.test(testFile);
		// System.out.println("Acc: " + acc);
		//
		// acc = learner.test();
		// System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "SimpleSupervisedLearningTest <input: testFile> "
			+ "<input: read mode>\n"
			+ "For example: SimpleSupervisedLearningTest "
			+ "../data/cikm09/data/K3/12000_Test.inter "
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI] " + "\n")
	public static void SimpleSupervisedLearningTest(String testFile,
			String readMode) throws Exception {

		SimpleSupervised learner = new SimpleSupervised();

		System.out.println("\nTesting...");
		double acc = learner.test(testFile, readMode);
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "SimpleSupervisedLearningTestWithInference <input: testFile>\n"
			+ "For example: SimpleSupervisedLearningTestWithInference "
			+ "../data/cikm09/data/K3/12000_Test.inter" + "\n")
	public static void SimpleSupervisedLearningTestWithInference(String testFile)
			throws Exception {

		SimpleSupervised learner = new SimpleSupervised();

		System.out.println("\nTesting...");
		double acc = learner.testWithInference(testFile);
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "SetDiscover <input: interFile> <input: outputFile>\n"
			+ "For example: SetDiscover "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.inter "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.predict" + "\n")
	public static void SetDiscover(String interFile, String outputFile)
			throws Exception {

		SetDiscovery setDiscover = new SetDiscovery();
		setDiscover.discovery(interFile);

	}

	@CommandDescription(description = "SetDiscoverAncestorOnly <input: interFile> <input: outputFile>\n"
			+ "For example: SetDiscoverAncestorOnly "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.inter " + "\n")
	public static void SetDiscoverAncestorOnly(String interFile)
			throws Exception {

		SetDiscovery setDiscover = new SetDiscovery();
		setDiscover.separateDiscovery(interFile);

	}

	@CommandDescription(description = "SetDiscoverSeparate <input: interFile> <input: interSeedFile> <input: cohenSeedFile> <input: outputFile>\n"
			+ "For example: SetDiscoverSeparate "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.inter "
			+ "../data/cikm09_CohenData/K3/12classes_cousin.inter "
			+ "../data/cikm09_CohenData/K3/seeds.gold.inter "
			+ "../data/cikm09_CohenData/K3/12classes_ancestors.predict" + "\n")
	public static void SetDiscoverSeparate(String interFile,
			String interSeedFile, String cohenSeedFile, String outputFile)
			throws Exception {

		SetDiscovery setDiscover = new SetDiscovery();
		setDiscover.separateDiscovery(interFile, interSeedFile, cohenSeedFile);

	}

	@CommandDescription(description = "EvaluateTitleDisambiguation <input: text index directory> <input: titleMapping> <input: evalFile> <input: mode (0:no disambiguation; 1:simple disambiguate; 2:our method)>\n"
			+ "For example: EvaluateTitleDisambiguation /scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/data/evaluateDisambiguation/english_premier_football_clubs.txt "
			+ "2\n")
	public static void EvaluateTitleDisambiguation(String indexDir,
			String titleMapping, String pairFile, String mode) throws Exception {

		Map<String, Set<String>> mapPairs = SetDiscovery
				.parseCohenFile(pairFile);

		EntityDisambiguation disambiguator = new EntityDisambiguation(indexDir,
				titleMapping);

		int modeInt = Integer.parseInt(mode);
		String entity1;
		String entity2;

		Set<String> keySet = mapPairs.keySet();

		int idx = 1;
		for (String key : keySet) {
			System.out.println("\n*Key = " + key);
			Set<String> setConcepts = mapPairs.get(key);
			for (String con : setConcepts) {

				entity1 = key;
				entity2 = con;

				System.out.println("\n*Pair " + idx + ": " + entity1 + ", "
						+ entity2);

				if (modeInt == 0)
					disambiguator.naiveCollectInfo(entity1, entity2);
				else if (modeInt == 2)
					disambiguator.collectInfo(entity1, entity2);

				idx++;
			}
		}

	}

	@CommandDescription(description = "CreateSameClassExampleFromCohenFile <input: cohenFile> <input: outputFile>\n"
			+ "Note: This function is to create data set for evaluating concept disambiguation\n"
			+ "For example: CreateSameClassExampleFromCohenFile "
			+ "../data/cikm09/data/evaluateDisambiguation/superheros.txt "
			+ "../data/cikm09/data/evaluateDisambiguation/superheros.cousin.txt"
			+ "\n")
	public static void CreateSameClassExampleFromCohenFile(String cohenFile,
			String outputFile) throws Exception {

		DataHandler.createSameClassExampleFromCohenFile(cohenFile, outputFile);

	}

	@CommandDescription(description = "MakeSymmetricDataSet <input: interFile> <input: outputFile>\n"
			+ "For example: MakeSymmetricDataSet "
			+ "../data/cikm09/data/test.inter "
			+ "../data/cikm09/data/test.sym.inter" + "\n")
	public static void MakeSymmetricDataSet(String interFile, String outputFile)
			throws Exception {

		DataHandler.makeSymmetricDataset(interFile, outputFile);

	}

	@CommandDescription(description = "EvaluateStrube <input: indexDir> "
			+ "<input: titleMapping> <input: conceptFile> <input: isaFile> "
			+ "<input: interFile> <input: maxLevelUp> <in: readMode>\n"
			+ "For example: EvaluateStrube "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/strube07/original/concepts.txt "
			+ "../data/cikm09/strube07/original/isa.txt "
			+ "../data/cikm09/data/K3/20000.new.last12000.shuffled.inter 3 [READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]\n")
	public static void EvaluateStrube(String indexDir, String titleMapping,
			String conceptFile, String isaFile, String interFile,
			String maxLevelUp, String readMode) throws Exception {

		Taxonomy taxonomy = new Taxonomy(indexDir, titleMapping, conceptFile,
				isaFile);

		taxonomy.classifier(interFile, Integer.parseInt(maxLevelUp), readMode);

	}

	@CommandDescription(description = "EvaluateSnow <input: interFile> "
			+ "<input: maxLevelUp> "
			+ "<in: readMode>\n"
			+ "For example: EvaluateSnow "
			+ "../data/cikm09/data/K3/20000.new.last12000.shuffled.inter 3 [READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]\n")
	public static void EvaluateSnow(String interFile, String maxLevelUp,
			String readMode) throws Exception {

		WordNetSnow snow = new WordNetSnow(interFile);

		snow.classifier(Integer.parseInt(maxLevelUp), readMode);

	}

	@CommandDescription(description = "Create5FoldDataset <input: pascaFile> <input: numFolds> <input: trainPortion> <input: testPortion> <output: outputPrefix>\n"
			+ "For example: Create5FoldDataset "
			+ "../data/cikm09/data/www07-classes.txt "
			+ "5 30 10 "
			+ "../data/cikm09/data/5folds/fold\n")
	public static void Create5FoldDataset(String pascaFile, String numFolds,
			String trainPortion, String testPortion, String outputPrefix)
			throws Exception {

		DataHandler.createCrossValidationData(pascaFile, Integer
				.parseInt(numFolds), Integer.parseInt(trainPortion), Integer
				.parseInt(testPortion), outputPrefix);

	}

	@CommandDescription(description = "EvaluateSnowInWordNet <input: interFile> <in: max_level_up> <output: outputInWordNetFile>\n"
			+ "For example: EvaluateSnowInWordNet "
			+ "../data/cikm09/data/K3/20000.new.last12000.shuffled.inter "
			+ "3 "
			+ "../data/cikm09/data/K3/20000.new.last12000.inwn.shuffled.inter\n")
	public static void EvaluateSnowInWordNet(String interFile,
			String max_level_up, String outputInWordNetFile) throws Exception {

		WordNetSnow snow = new WordNetSnow(interFile);

		snow.classifierInWordNet(max_level_up, outputInWordNetFile);

	}

	@CommandDescription(description = "QuangEvaluateSnowInWordNet <input: interFile> <in: max_level_up> <output: outputInWordNetFile>\n"
			+ "For example: QuangEvaluateSnowInWordNet "
			+ "../data/cikm09/data/K3/20000.new.last12000.shuffled.inter "
			+ "3 "
			+ "../data/cikm09/data/K3/20000.new.last12000.inwn.shuffled.inter\n")
	public static void QuangEvaluateSnowInWordNet(String interFile,
			String max_level_up, String outputInWordNetFile) throws Exception {

		QuangWordNetSnow snow = new QuangWordNetSnow(interFile);

		snow.classifierInWordNet(max_level_up, outputInWordNetFile);

	}

	@CommandDescription(description = "MainRelationIdentifier <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: K>\n"
			+ "For example: MainRelationIdentifier "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt 3\n")
	public static void MainRelationIdentifier(String indexDir,
			String categoryMapping, String titleMapping, String K)
			throws Exception {

		MainRelationIdentification identifier = new MainRelationIdentification(
				indexDir, categoryMapping, titleMapping, Integer.parseInt(K));

		String concept1;
		String concept2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st concept (_ to end): ");
		concept1 = in.readLine();

		if (concept1.equals("_"))
			return;

		do {

			if (concept1.length() != 0) {

				System.out.print("Enter the 2nd concept (_ to end): ");
				concept2 = in.readLine();

				if (concept2.equals("_"))
					return;

				int relation = identifier.identifyPair(concept1, concept2);

				if (relation == Constants.NONE) {
					System.out.println("No relation.");
				} else if (relation == Constants.ANCESTOR_E1_TO_E2) {
					System.out.println("\"" + concept1
							+ "\" is an ancestor of \"" + concept2 + "\"");
				} else if (relation == Constants.ANCESTOR_E2_TO_E1) {
					System.out.println("\"" + concept1 + "\" is a child of \""
							+ concept2 + "\"");
				} else if (relation == Constants.COUSIN) {
					System.out.println("Sibling.");
				} else {
					System.out.println("Relation: " + relation);
				}

			}

			System.out.print("Enter the 1st entity (_ to end): ");
			concept1 = in.readLine();

		} while (!concept1.equals("_"));

	}

	@CommandDescription(description = "StartServer <input: port>\n"
			+ "For example: StartServer 1468\n")
	public static void StartServer(String port) throws Exception {

		RelationServer.main(new String[] { port });

	}

	@CommandDescription(description = "TestRelationClient <input: port>\n"
			+ "For example:TestRelationClient localhost 1468 car toyota\n")
	public static void TestRelationClient(String host, String port,
			String concept1, String concept2) throws Exception {
		RelationClient.main(new String[] { host, port, concept1, concept2 });
	}

	/*
	 * WWW10 stars here
	 */

	@CommandDescription(description = "TestConstraints <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: K>\n"
			+ "For example: TestConstraints "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt 3\n")
	public static void TestConstraints(String indexDir, String categoryMapping,
			String titleMapping, String K) throws Exception {

		SealConstraints seal = new SealConstraints(indexDir, categoryMapping,
				titleMapping, Integer.parseInt(K));
		seal.runAll();
		System.out.println("Done.");
	}

	@CommandDescription(description = "ConvertYagoToMySQL\n"
			+ "For example: ConvertYagoToMySQL\n")
	public static void ConvertYagoToMySQL() throws Exception {

		SQLConverter.main(null);

	}

	@CommandDescription(description = "QueryYago\n"
			+ "For example: QueryYago\n")
	public static void QueryYago() throws Exception {

		QueryProcessor.main(null);

	}

	@CommandDescription(description = "EvaluatingYago <input: intermediate file> "
			+ "<input: maxLevelUp> <in: readMode>\n"
			+ "For example: EvaluatingYago ../data/cikm09/data/K3/20000.new.last12000.shuffled.inter "
			+ "3 [READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]\n")
	public static void EvaluatingYago(String interFile, String maxLevelUp,
			String readMode) throws Exception {

		// System.out.println("Hello");
		EvaluateYago yago = new EvaluateYago();
		yago.evaluate(interFile, Integer.parseInt(maxLevelUp), readMode);

	}

	@CommandDescription(description = "MainPrivateRelationIdentifier <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: K>\n"
			+ "For example: MainPrivateRelationIdentifier "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt 3 \n"
			+ "*Note: this function is different with MainRelationIdentifier in terms of using private cache."
			+ "MainRelationIndeifier is mainly used for web demo.")
	public static void MainPrivateRelationIdentifier(String indexDir,
			String categoryMapping, String titleMapping, String K)
			throws Exception {

		MainRelationIdentification identifier = new MainRelationIdentification(
				indexDir, categoryMapping, titleMapping, Integer.parseInt(K));

		String concept1;
		String concept2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st concept (_ to end): ");
		concept1 = in.readLine();

		if (concept1.equals("_"))
			return;

		do {

			if (concept1.length() != 0) {

				System.out.print("Enter the 2nd concept (_ to end): ");
				concept2 = in.readLine();

				if (concept2.equals("_"))
					return;

				HashMap<String, Object> mapResults = identifier
						.privateIdentifyConcepts(concept1, concept2);

				int relation = ((Integer) mapResults.get("RELATION"))
						.intValue();

				double score = ((Double) mapResults.get("SCORE")).doubleValue();

				if (relation == Constants.NONE) {
					System.out.println("No relation.");
				} else if (relation == Constants.ANCESTOR_E1_TO_E2) {
					System.out.println("\"" + concept1
							+ "\" is an ancestor of \"" + concept2 + "\"");
				} else if (relation == Constants.ANCESTOR_E2_TO_E1) {
					System.out.println("\"" + concept1 + "\" is a child of \""
							+ concept2 + "\"");
				} else if (relation == Constants.COUSIN) {
					System.out.println("Sibling.");
				} else {
					System.out.println("Relation: " + relation);
				}

				System.out.println("Score: " + score);

				Double scoreL0 = (Double) mapResults.get("L0");
				Double scoreL1 = (Double) mapResults.get("L1");
				Double scoreL2 = (Double) mapResults.get("L2");
				Double scoreL3 = (Double) mapResults.get("L3");

				Double scoreL0_exp = Math.exp(scoreL0);
				Double scoreL1_exp = Math.exp(scoreL1);
				Double scoreL2_exp = Math.exp(scoreL2);
				Double scoreL3_exp = Math.exp(scoreL3);

				Double totalScore = scoreL0_exp + scoreL1_exp + scoreL2_exp
						+ scoreL3_exp;

				Double scoreL0_sm = scoreL0_exp / totalScore;
				Double scoreL1_sm = scoreL1_exp / totalScore;
				Double scoreL2_sm = scoreL2_exp / totalScore;
				Double scoreL3_sm = scoreL3_exp / totalScore;

				System.out.println("Score for label 0: " + scoreL0
						+ ", softmax=" + scoreL0_sm);
				System.out.println("Score for label 1: " + scoreL1
						+ ", softmax=" + scoreL1_sm);
				System.out.println("Score for label 2: " + scoreL2
						+ ", softmax=" + scoreL2_sm);
				System.out.println("Score for label 3: " + scoreL3
						+ ", softmax=" + scoreL3_sm);
			}

			System.out.print("Enter the 1st entity (_ to end): ");
			concept1 = in.readLine();

		} while (!concept1.equals("_"));

	}

	@CommandDescription(description = "TestClassifierWithConstraints <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: K>\n"
			+ "For example: TestClassifierWithConstraints "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt 3\n")
	public static void TestClassifierWithConstraints(String indexDir,
			String categoryMapping, String titleMapping, String K)
			throws Exception {

		ConstraintEnforcement identifier = new ConstraintEnforcement(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));

		identifier.classifyWithConstraints("mountain", "k2", "everest");
		identifier.classifyWithConstraints("president", "george w bush",
				"bill clinton");
		identifier.classifyWithConstraints("bill clinton", "george w bush",
				"president");
		identifier.classifyWithConstraints("honda", "toyota", "honda civic");
		identifier.classifyWithConstraints("nile", "Mississippi", "river");
		identifier.classifyWithConstraints("nile", "bill gates", "river");

	}

	@CommandDescription(description = "GetRelatedConcepts <input: concept>\n"
			+ "For example: GetRelatedConcepts \"nissan sentra\"\n")
	public static void GetRelatedConcepts(String concept) throws Exception {

		RelatedConcepts extractor = new RelatedConcepts();
		extractor.getRelatedConcepts(concept, "xxxxxxx", true);
		extractor.printRelatedConcepts();

	}

	@CommandDescription(description = "GetRelatedConceptForFile <in: interFile> <in: maxAnc> <in: maxSib> <in: maxChild> <out: outputFile>\n"
			+ "For example: GetRelatedConceptForFile "
			+ "../data/www10/K3/20000.new.last12000.shuffled.inter "
			+ "10 10 10 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter\n")
	public static void GetRelatedConceptForFile(String interFile,
			String maxAnc, String maxSib, String maxChild, String outputFile)
			throws Exception {

		RelatedConcepts extractor = new RelatedConcepts();
		extractor.getRelatedConceptForFile(interFile, outputFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChild));

	}

	@CommandDescription(description = "RelatedConceptExtractFeatures <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: related concept file> "
			+ "<input: K> "
			+ "<input: maxConcept> "
			+ "<output: Intermediate example file>\n"
			+ "For example: RelatedConceptExtractFeatures "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "3 1 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "\n")
	public static void RelatedConceptExtractFeatures(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String K, String maxConcept, String outputFile) throws Exception {
		RelatedConcepts extractor = new RelatedConcepts();
		extractor.createConceptRelationInterFile(indexDir, categoryMapping,
				titleMapping, inputFile, outputFile, Integer.parseInt(K),
				Integer.parseInt(maxConcept));
	}

	@CommandDescription(description = "RelatedConceptExtractFeaturesBigFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: related concept file> "
			+ "<input: K> "
			+ "<input: maxConceptAnc> <input: maxConceptSib> <input: maxConceptChild>"
			+ "<input: from> "
			+ "<input: to> "
			+ "<output: Intermediate example file>\n"
			+ "For example: RelatedConceptExtractFeaturesBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "3 0 1 0 0 1000 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "\n")
	public static void RelatedConceptExtractFeaturesBigFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String K, String maxConceptAnc, String maxConceptSib,
			String maxConceptChi, String from, String to, String outputFile)
			throws Exception {
		RelatedConcepts extractor = new RelatedConcepts();
		extractor.createConceptRelationInterFileBigFile(indexDir,
				categoryMapping, titleMapping, inputFile, Integer.parseInt(K),
				outputFile, Integer.parseInt(maxConceptAnc), Integer
						.parseInt(maxConceptSib), Integer
						.parseInt(maxConceptChi), Integer.parseInt(from),
				Integer.parseInt(to));
	}

	@CommandDescription(description = "RelatedConceptExtractFeaturesBigFileExclusive <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: related concept file> "
			+ "<input: exclusive intermediate file> "
			+ "<input: K> "
			+ "<input: maxConceptAnc> <input: maxConceptSib> <input: maxConceptChild>"
			+ "<input: from> "
			+ "<input: to> "
			+ "<output: Intermediate example file>\n"
			+ "For example: RelatedConceptExtractFeaturesBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "3 5 5 5 0 3000 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.other.inter "
			+ "\n")
	public static void RelatedConceptExtractFeaturesBigFileExclusive(
			String indexDir, String categoryMapping, String titleMapping,
			String inputFile, String exclusiveFile, String K,
			String maxConceptAnc, String maxConceptSib, String maxConceptChi,
			String from, String to, String outputFile) throws Exception {
		RelatedConcepts extractor = new RelatedConcepts();
		extractor.createConceptRelationInterFileBigFileExclusive(indexDir,
				categoryMapping, titleMapping, inputFile, exclusiveFile,
				Integer.parseInt(K), outputFile, Integer
						.parseInt(maxConceptAnc), Integer
						.parseInt(maxConceptSib), Integer
						.parseInt(maxConceptChi), Integer.parseInt(from),
				Integer.parseInt(to));
	}

	@CommandDescription(description = "ClassifyingWithConstraints <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: ClassifyingWithConstraints "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "0 1 0\n")
	public static void ClassifyingWithConstraints(String interFile,
			String supportInterFile, String maxAnc, String maxSib, String maxChi)
			throws Exception {

		ConstraintEnforcement classifer = new ConstraintEnforcement();
		classifer.testWithConstraints(interFile, supportInterFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChi), true);
	}

	@CommandDescription(description = "LearningConstraints <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: LearningConstraints "
			+ "../data/www10/K3/20000.new.first8000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.first8000.shuffled.relatedconcept.333.inter "
			+ "0 1 0\n")
	public static void LearningConstraints(String interFile,
			String supportInterFile, String maxAnc, String maxSib, String maxChi)
			throws Exception {

		ConstraintLearning learner = new ConstraintLearning();
		learner.contraintLearning(interFile, supportInterFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChi));
	}

	@CommandDescription(description = "ClassifyingWithSoftConstraints <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: ClassifyingWithSoftConstraints "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "0 1 0\n")
	public static void ClassifyingWithSoftConstraints(String interFile,
			String supportInterFile, String maxAnc, String maxSib, String maxChi)
			throws Exception {

		ConstraintEnforcement classifer = new ConstraintEnforcement();
		classifer.testWithSoftConstraints(interFile, supportInterFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChi));
	}

	@CommandDescription(description = "GetGoldRelatedConceptForFile <in: orgFile> <in: interFile> <in: maxAnc> <in: maxSib> <in: maxChild> <out: outputFile>\n"
			+ "For example: GetGoldRelatedConceptForFile "
			+ "../data/cikm09/data/www07-classes.txt "
			+ "../data/www10/K3/goldRelatedConcepts/20000.new.last12000.shuffled.inter "
			+ "10 10 10 "
			+ "../data/www10/K3/goldRelatedConcepts/20000.new.last12000.shuffled.expanded.inter\n")
	public static void GetGoldRelatedConceptForFile(String orgFile,
			String interFile, String maxAnc, String maxSib, String maxChild,
			String outputFile) throws Exception {

		GoldRelatedConcept extractor = new GoldRelatedConcept(orgFile);
		extractor.getRelatedConcepts(interFile, outputFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChild));

	}

	@CommandDescription(description = "BackwardConstraintSelection <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: BackwardConstraintSelection "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "3 3 3\n")
	public static void BackwardConstraintSelection(String interFile,
			String supportInterFile, String maxAnc, String maxSib, String maxChi)
			throws Exception {

		ConstraintEnforcement classifer = new ConstraintEnforcement();
		classifer.backwardConstraintSelection(interFile, supportInterFile,
				Integer.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
						.parseInt(maxChi));
	}

	@CommandDescription(description = "ForwardConstraintSelection <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: ForwardConstraintSelection "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "3 3 3\n")
	public static void ForwardConstraintSelection(String interFile,
			String supportInterFile, String maxAnc, String maxSib, String maxChi)
			throws Exception {

		ConstraintEnforcement classifer = new ConstraintEnforcement();
		classifer.forwardConstraintSelection(interFile, supportInterFile,
				Integer.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
						.parseInt(maxChi));
	}

	@CommandDescription(description = "ClassifyingWithGraphConstraints <in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: constraintFile> "
			+ "<in: READ_MODE> "
			+ "<in: maxAnc> "
			+ "<in: maxSib> "
			+ "<in: maxChi>\n"
			+ "For example: ClassifyingWithGraphConstraints "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "./www10Results/constraints_forward_K3_train_gold.555.txt "
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI] " + "0 1 0\n")
	public static void ClassifyingWithGraphConstraints(String interFile,
			String supportInterFile, String constraintFile, String readMode,
			String maxAnc, String maxSib, String maxChi) throws Exception {

		GraphConstraints classifer = new GraphConstraints(constraintFile);
		classifer.evaluateWithGraphConstraints(interFile, supportInterFile,
				readMode, Integer.parseInt(maxAnc), Integer.parseInt(maxSib),
				Integer.parseInt(maxChi), true);
	}

	@CommandDescription(description = "GetYagoRelatedConceptForFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<in: interFile> "
			+ "<in: idfFile> "
			+ "<in: maxAnc> <in: maxSib> <in: maxChild> "
			+ "<out: outputFile>\n"
			+ "For example: GetYagoRelatedConceptForFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "/scratch/quangdo2/idf_unnormalized.txt.uniqued "
			+ "../data/www10/K3/20000.new.last12000.shuffled.inter "
			+ "10 10 10 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter\n")
	public static void GetYagoRelatedConceptForFile(String indexDir,
			String categoryMapping, String titleMapping, String idfFile,
			String interFile, String maxAnc, String maxSib, String maxChild,
			String outputFile) throws Exception {

		YagoRelatedConcept extractor = new YagoRelatedConcept(indexDir,
				categoryMapping, titleMapping, idfFile);
		extractor.getRelatedConceptForFile(interFile, outputFile, Integer
				.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
				.parseInt(maxChild));

	}

	@CommandDescription(description = "ExtractDesiredData <in: fromFile> "
			+ "<in: toFile> "
			+ "<in: dataType (WIKI or NONWIKI)> "
			+ "<out: outFile>\n"
			+ "For example: ExtractDesiredData "
			+ "../data/www10/K3/20000.new.last12000.shuffled.inter "
			+ "../data/www10/K3/20000.new.last12000.beyondwiki.shuffled.inter "
			+ "NONWIKI "
			+ "../data/www10/K3/20000.new.last12000.beyondwiki.nonwiki.shuffled.inter\n")
	public static void ExtractDesiredData(String fromFile, String toFile,
			String dataType, String outFile) throws Exception {

		if (dataType.equalsIgnoreCase("wiki")) {
			DataHandler.extractData(fromFile, toFile, outFile,
					DataHandler.READ_ONLY_WIKI);
		} else if (dataType.equalsIgnoreCase("nonwiki")) {
			DataHandler.extractData(fromFile, toFile, outFile,
					DataHandler.READ_ONLY_NONWIKI);
		} else {
			System.out.println("ERROR: Wrong dataType!!!");
		}

	}

	@CommandDescription(description = "ExtractFeaturesSepSearchBigFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Gold example file> "
			+ "<input: K> "
			+ "<input: fromIndex> "
			+ "<input: toIndex> "
			+ "<output: Intermediate example file>\n"
			+ "For example: ExtractFeaturesSepSearchBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/www10//test.gold 3 "
			+ "0 100 "
			+ "../data/cikm09/unsupervised/test.inter" + "\n")
	public static void ExtractFeaturesSepSearchBigFile(String indexDir,
			String categoryMapping, String titleMapping, String inputFile,
			String K, String sFrom, String sTo, String outputFile)
			throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeaturesBigFile(inputFile, Integer.parseInt(sFrom), Integer
				.parseInt(sTo), outputFile);
	}

	@CommandDescription(description = "ConstraintRelationIdentification\n"
			+ "For example: ConstraintRelationIdentification\n")
	public static void ConstraintRelationIdentification() throws Exception {

		MainConstraintRelationIdentification identifier = new MainConstraintRelationIdentification();

		String concept1;
		String concept2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter the 1st concept (_ to end): ");
		concept1 = in.readLine();

		if (concept1.equals("_"))
			return;

		do {

			if (concept1.length() != 0) {

				System.out.print("Enter the 2nd concept (_ to end): ");
				concept2 = in.readLine();

				if (concept2.equals("_"))
					return;

				HashMap<String, String> mapResults = identifier
						.identifyConcepts(concept1, concept2);

				String relation = mapResults.get("SCORE");

				String score = mapResults.get("REASON");

				System.out.println("Relation: " + relation);
				System.out.println("Score: " + score);

			}

			System.out.println();
			System.out.print("Enter the 1st entity (_ to end): ");
			concept1 = in.readLine();

		} while (!concept1.equals("_"));

	}

	@CommandDescription(description = "StartConstraintRelationServer <input: port>\n"
			+ "For example: StartConstraintRelationServer 1234\n")
	public static void StartConstraintRelationServer(String port)
			throws Exception {
		ConstraintRelationServer.main(new String[] { port });
	}

	@CommandDescription(description = "TestConstraintRelationClient <in: host> <in: port> <in: concept 1> <in: concept 2>\n"
			+ "For example:TestConstraintRelationClient localhost 1234 car toyota\n")
	public static void TestConstraintRelationClient(String host, String port,
			String concept1, String concept2) throws Exception {
		ConstraintRelationClient.main(new String[] { host, port, concept1,
				concept2 });
	}

	// ======================
	// Codes for ACL10

	@CommandDescription(description = "AnnotateTECorpusWithWikiTitles <in: TE corpus file> <in: config file> <out: outputFile>\n"
			+ "For example:AnnotateTECorpusWithWikiTitles ../data/acl10/TE/RTE5_MainTask_TestSet_Gold.xml config.txt "
			+ "../data/acl10/TE/RTE5_MainTask_TestSet_Gold.wiki\n")
	public static void AnnotateTECorpusWithWikiTitles(String dataFile,
			String configFile, String outputFile) throws Exception {
		AnnotateTECorpus annotator = new AnnotateTECorpus(configFile);
		annotator.annotateCorpus(dataFile, outputFile);
	}

	@CommandDescription(description = "ConvertWikiTitle2Examples <in: wiki title file> <out: outputFile>\n"
			+ "For example:ConvertWikiTitle2Examples "
			+ "../data/acl10/TE/RTE5_TestSet/RTE5_MainTask_TestSet_Gold.wiki.annotated "
			+ "../data/acl10/TE/RTE5_TestSet/RTE5_MainTask_TestSet_Gold.example\n")
	public static void ConvertWikiTitle2Examples(String wikiFile,
			String outputFile) throws Exception {
		Converter.convertWikiTitle2LearningData(wikiFile, outputFile);
	}

	@CommandDescription(description = "ConvertWikiTitle2XML <in: wiki title file> <out: outputFile>\n"
			+ "For example:ConvertWikiTitle2XML "
			+ "../data/acl10/TE/RTE5_TestSet/RTE5_MainTask_TestSet_Gold.wiki.annotated "
			+ "../data/acl10/TE/RTE5_TestSet/RTE5_MainTask_TestSet_Gold.xml\n")
	public static void ConvertWikiTitle2XML(String wikiFile, String outputFile)
			throws Exception {
		Converter.convertWikiTitle2XMLAnnotation(wikiFile, outputFile);
	}

	@CommandDescription(description = "ConvertAnnotatedXML2Examples <in: wiki title file> <out: outputFile>\n"
			+ "For example: ConvertAnnotatedXML2Examples "
			+ "../data/acl10/RTEData/RTE1_Test/annotated_test.wiki.xml.annotated "
			+ "../data/acl10/RTEData/RTE1_Test/annotated_test.wiki.xml.annotated.examples\n")
	public static void ConvertAnnotatedXML2Examples(String inputFile,
			String outputFile) throws Exception {
		Converter.convertXMLAnnotation2LearningData(inputFile, outputFile);
	}

	@CommandDescription(description = "TestClassifierWithoutGraphConstrants <input: testFile>\n"
			+ "For example: TestClassifierWithoutGraphConstrants "
			+ "../data/acl10/TE/RTE5_TestSet/K3/RTE5_MainTask_TestSet_Gold.example.0.9000.inter\n")
	public static void TestClassifierWithoutGraphConstrants(String testFile)
			throws Exception {

		SimpleSupervised learner = new SimpleSupervised();

		System.out.println("\nTesting...");
		double acc = learner.testAcl10WithoutGraphConstraints(testFile);
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "PreparePantelConcepts <in: Pantel folder> <out: outputFile>\n"
			+ "For example: PreparePantelConcepts "
			+ "../data/emnlp09_PatrickData/original/wikipedia.20071218.goldsets "
			+ "../data/acl10/Pantel/pantelConcepts.txt\n")
	public static void PreparePantelConcepts(String pantelFolder,
			String outputFile) throws Exception {

		PreparePantelData.prepareData(pantelFolder, outputFile);

	}

	@CommandDescription(description = "SelectBestCombinationWithGraphConstraints "
			+ "<in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: constraintFile>"
			+ "<in: READ_MODE>\n"
			+ "For example: SelectBestCombinationWithGraphConstraints "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "./www10Results/constraints_forward_K3_train_gold.555.txt"
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]" + "\n")
	public static void SelectBestCombinationWithGraphConstraints(
			String interFile, String supportInterFile, String constraintFile,
			String readMode) throws Exception {

		SelectBestRelatedConceptCombination selector = new SelectBestRelatedConceptCombination();
		selector.selectBestCombination(interFile, supportInterFile,
				constraintFile, readMode);
	}

	@CommandDescription(description = "GetRelatedConceptWithSubClassOfForFile <in: interFile> <in: maxAnc> <in: maxSib> <in: maxChild> <out: outputFile>\n"
			+ "For example: GetRelatedConceptWithSubClassOfForFile "
			+ "../data/www10/K3/20000.new.last12000.shuffled.inter "
			+ "10 10 10 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter\n")
	public static void GetRelatedConceptWithSubClassOfForFile(String interFile,
			String maxAnc, String maxSib, String maxChild, String outputFile)
			throws Exception {

		RelatedConcepts extractor = new RelatedConcepts();
		extractor.getRelatedConceptWithSubClassOfForFile(interFile, outputFile,
				Integer.parseInt(maxAnc), Integer.parseInt(maxSib), Integer
						.parseInt(maxChild));

	}

	@CommandDescription(description = "PrecisionRecallFscore <in: the output file from testing>\n"
			+ "For example: PrecisionRecallFscore "
			+ "./acl10Results/Pantel/output3_ours_noconstraints.txt\n")
	public static void PrecisionRecallFscore(String fname) throws Exception {
		RecallPrecisionFscore.getResults(fname);
	}

	@CommandDescription(description = "ExtractFeaturesAdditionalFeaturesBigFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: Gold example file> "
			+ "<input: K> "
			+ "<input: fromIndex> "
			+ "<input: toIndex> "
			+ "<output: Intermediate example file>\n"
			+ "For example: ExtractFeaturesAdditionalFeaturesBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/cikm09/unsupervised/test.gold 3 "
			+ "0 100 "
			+ "../data/cikm09/unsupervised/test.inter" + "\n")
	public static void ExtractFeaturesAdditionalFeaturesBigFile(
			String indexDir, String categoryMapping, String titleMapping,
			String inputFile, String K, String sFrom, String sTo,
			String outputFile) throws Exception {
		FeatureExtraction fex = new FeatureExtraction(indexDir,
				categoryMapping, titleMapping, Integer.parseInt(K));
		fex.extractFeaturesAdditionalFeaturesBigFile(inputFile, Integer
				.parseInt(sFrom), Integer.parseInt(sTo), outputFile);
	}

	@CommandDescription(description = "RelatedConceptExtractFeaturesAdditionalFeaturesBigFile <input: index directory> "
			+ "<input: categoryMapping> "
			+ "<input: titleMapping> "
			+ "<input: related concept file> "
			+ "<input: K> "
			+ "<input: maxConceptAnc> <input: maxConceptSib> <input: maxConceptChild>"
			+ "<input: from> "
			+ "<input: to> "
			+ "<output: Intermediate example file>\n"
			+ "For example: RelatedConceptExtractFeaturesAdditionalFeaturesBigFile "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category "
			+ "/scratch/quangdo2/pages_xml_indexed_jupiter_category.map "
			+ "/scratch/quangdo2/bigramTitleMapping.txt "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "3 0 1 0 0 1000 "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.inter "
			+ "\n")
	public static void RelatedConceptExtractFeaturesAdditionalFeaturesBigFile(
			String indexDir, String categoryMapping, String titleMapping,
			String inputFile, String K, String maxConceptAnc,
			String maxConceptSib, String maxConceptChi, String from, String to,
			String outputFile) throws Exception {
		RelatedConcepts extractor = new RelatedConcepts();
		extractor.createConceptRelationInterAdditionalFeaturesFileBigFile(
				indexDir, categoryMapping, titleMapping, inputFile, Integer
						.parseInt(K), outputFile, Integer
						.parseInt(maxConceptAnc), Integer
						.parseInt(maxConceptSib), Integer
						.parseInt(maxConceptChi), Integer.parseInt(from),
				Integer.parseInt(to));
	}

	@CommandDescription(description = "SelectBestCombinationWithGraphConstraintsRecPreF "
			+ "<in: interFile> "
			+ "<in: supportInterFile> "
			+ "<in: constraintFile>"
			+ "<in: READ_MODE>\n"
			+ "For example: SelectBestCombinationWithGraphConstraintsRecPreF "
			+ "../data/www10/K3/20000.new.last12000.shuffled.expanded.inter "
			+ "../data/www10/K3/20000.new.last12000.shuffled.relatedconcept.333.inter "
			+ "./www10Results/constraints_forward_K3_train_gold.555.txt"
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]" + "\n")
	public static void SelectBestCombinationWithGraphConstraintsRecPreF(
			String interFile, String supportInterFile, String constraintFile,
			String readMode) throws Exception {

		SelectBestRelatedConceptCombination selector = new SelectBestRelatedConceptCombination();
		selector.selectBestCombinationRecPreF(interFile, supportInterFile,
				constraintFile, readMode);
	}

	@CommandDescription(description = "AddFeatsSimpleSupervisedLearningTrain <input: trainFile> <input: testFile>\n"
			+ "For example: AddFeatsSimpleSupervisedLearningTrain "
			+ "../data/acl10/Pasca/K3/additionalFeatures/20000.new.first8000.shuffled.addfeat.inter "
			+ "../data/acl10/Pasca/K3/additionalFeatures/20000.new.last12000.shuffled.addfeat.inter"
			+ "\n")
	public static void AddFeatsSimpleSupervisedLearningTrain(String trainFile,
			String testFile) throws Exception {

		AddFeatsSimpleSupervised learner = new AddFeatsSimpleSupervised(
				trainFile, testFile);

		System.out.println("Start training...");
		learner.train();
		System.out.println("Done.");

	}

	@CommandDescription(description = "AddFeatsSimpleSupervisedLearningTest <input: testFile> "
			+ "<input: read mode>\n"
			+ "For example: AddFeatsSimpleSupervisedLearningTest "
			+ "../data/acl10/RTEData/RTE1_Test/addfeats/annotated_test.wiki.xml.annotated.examples.inter "
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI] " + "\n")
	public static void AddFeatsSimpleSupervisedLearningTest(String testFile,
			String readMode) throws Exception {

		AddFeatsSimpleSupervised learner = new AddFeatsSimpleSupervised();

		System.out.println("\nTesting...");
		double acc = learner.test(testFile, readMode);
		System.out.println("Acc: " + acc);

		acc = learner.test();
		System.out.println("Acc: " + acc);

	}

	@CommandDescription(description = "EvaluatingYagoACL10 <input: intermediate file> "
			+ "<input: maxLevelUp> <in: readMode>\n"
			+ "For example: EvaluatingYago ../data/cikm09/data/K3/20000.new.last12000.shuffled.inter "
			+ "3 [READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI]\n")
	public static void EvaluatingYagoACL10(String interFile, String maxLevelUp,
			String readMode) throws Exception {

		// System.out.println("Hello");
		EvaluateYago yago = new EvaluateYago();
		yago.evaluateACL10(interFile, Integer.parseInt(maxLevelUp), readMode);

	}

	@CommandDescription(description = "MicroAveragePrecisionRecallFscore <in: the output file from testing>\n"
			+ "For example: MicroAveragePrecisionRecallFscore "
			+ "./acl10Results/Pantel/output3_ours_noconstraints.txt\n")
	public static void MicroAveragePrecisionRecallFscore(String fname)
			throws Exception {
		RecallPrecisionFscore.getMicroAveragePrecision(fname);
	}

	@CommandDescription(description = "WikiDumpParserCategoryFile <in: Wikipedia xml file> <out: parsed file>\n"
			+ "For example: WikiDumpParserCategoryFile /scratch/quangdo2/wiki2010Feb03/test.xml "
			+ "/scratch/quangdo2/wiki2010Feb03/test.xml.parsed")
	public static void WikiDumpParserCategoryFile(String inputFile,
			String outputFile) {
		Date start = new Date();
		WikiDumpParserCategory dumpParser = new WikiDumpParserCategory(
				inputFile);
		dumpParser.setOutputFileName(outputFile);
		dumpParser.parse();
		Date end = new Date();
		System.out.println("Done. " + (end.getTime() - start.getTime())
				/ (float) 1000);
	}

	// ===Work for Lev data.
	@CommandDescription(description = "SimpleSupervisedLearningTestLevData <input: testFile> "
			+ "<input: read mode>\n"
			+ "For example: SimpleSupervisedLearningTestLevData "
			+ "../data/cikm09/data/K3/12000_Test.inter "
			+ "[READ_ALL|READ_ONLY_WIKI|READ_ONLY_NONWIKI] " + "\n")
	public static void SimpleSupervisedLearningTestLevData(String testFile,
			String readMode) throws Exception {

		SimpleSupervised learner = new SimpleSupervised();

		System.out.println("\nTesting...");
		double acc = learner.testLevData(testFile, readMode);
		System.out.println("Acc: " + acc);

		acc = learner.testLevData();
		System.out.println("Acc: " + acc);

	}

	// ====================
	// EMNLP '10
	// ====================

	@CommandDescription(description = "RTEDataOverlapWithPasca <in: RTE XML file> <in: Pasca's data file> <in: class name mapping file>\n"
			+ "For example: RTEDataOverlapWithPasca "
			+ "../data/emnlp10/RTE1_Test/annotated_test.xml "
			+ "../data/emnlp10/Pasca/www07-classes.txt "
			+ "../data/emnlp10/Pasca/class-cluster.txt\n")
	public static void RTEDataOverlapWithPasca(String rteFile,
			String pascaFile, String classFile) throws Exception {

		RTEData rte = new RTEData();
		rte.getOverlap(rteFile, pascaFile, classFile);

	}

	@CommandDescription(description = "GetDataInStrudel <in: Strudel index dir> <in: test file> <out: outputFile>\n"
			+ "For example: GetDataInStrudel "
			+ "/home/roth/quangdo2/Apollo/data/Strudel/index_strudel "
			+ "../data/www10/K2/withSubClassOf/20000.new.last12000.shuffled.inter "
			+ "../data/emnlp10/Pasca/K2/20000.new.last12000.shuffled.instrudel.inter\n")
	public static void GetDataInStrudel(String indexDir, String pairFile,
			String outputFile) throws Exception {
		DM dm = new DM(indexDir);
		dm.GetPairsInStrudel(pairFile, outputFile);
	}

	@CommandDescription(description = "EvaluateStrudel <in: Strudel index dir> <in: test file> <in: prototype file>\n"
			+ "For example: EvaluateStrudel "
			+ "/home/roth/quangdo2/Apollo/data/Strudel/index_strudel "
			+ "../data/emnlp10/Strudel/K2/140_0_3.instrudel.inter "
			+ "../data/emnlp10/Strudel/prototype.txt\n")
	public static void EvaluateStrudel(String indexDir, String pairFile,
			String protoFile) throws Exception {
		DM dm = new DM(indexDir);
		dm.classify(pairFile, protoFile);
	}

	@CommandDescription(description = "GetConceptInStrudel <in: Strudel index dir> <in: inputFile> <out: outputFile>\n"
			+ "For example: GetConceptInStrudel "
			+ "/home/roth/quangdo2/Apollo/data/Strudel/index_strudel "
			+ "../data/emnlp10/Pasca/www07-classes.txt "
			+ "../data/emnlp10/Pasca/www07-classes-instrudel.txt\n")
	public static void GetConceptInStrudel(String indexDir, String inputFile,
			String outputFile) throws Exception {
		DM dm = new DM(indexDir);
		dm.GetConceptInStrudel(inputFile, outputFile);
	}

	@CommandDescription(description = "GetCleanPairInStrudel <in: inputFile> <out: outputFile>\n"
			+ "For example: GetCleanPairInStrudel "
			+ "../data/emnlp10/Pasca/TestPrototypes/6000.gold0 "
			+ "../data/emnlp10/Pasca/TestPrototypes/from6000.gold0\n")
	public static void GetCleanPairInStrudel(String inputFile, String outputFile)
			throws Exception {
		DM dm = new DM("");
		dm.GetCleanPairs(inputFile, outputFile);
	}

}
