/**
 * 
 */
package edu.illinois.cs.cogcomp.lucenesearch;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;


public class SearcherFactory
{
	private static boolean	ngrams;
	private static boolean	verbose;
	private static String[]	fields;

	public enum SearcherType
	{

		SimpleSearch
		{
			ILuceneSearcher getSearcher(HashMap<String, String> parameters)
			{
				SimpleLuceneSearcher searcher = new SimpleLuceneSearcher(fields, ngrams);
				searcher.setVerbosity(verbose);

				return searcher;
			}
		},
		
		ListSearch
		{
			ILuceneSearcher getSearcher(HashMap<String, String> parameters)
			{
				ListSearcher searcher = new ListSearcher(fields, ngrams);
				searcher.setVerbosity(verbose);

				return searcher;
			}
		},

		CategorySearch
		{
			ILuceneSearcher getSearcher(HashMap<String, String> parameters)
			{
				CategorySearcher searcher = new CategorySearcher(fields, ngrams);
				searcher.setVerbosity(verbose);

				return searcher;
			}
		},
		
		TitleSearch
		{
			ILuceneSearcher getSearcher(HashMap<String, String> parameters)
			{
				TitleSearcher searcher = new TitleSearcher(fields, ngrams);
				searcher.setVerbosity(verbose);

				return searcher;
			}
		},

		TextSearch
		{
			ILuceneSearcher getSearcher(HashMap<String, String> parameters)
			{
				fields = new String[] { "text" };

				TextSearcher searcher = new TextSearcher(fields, ngrams);
				searcher.setVerbosity(verbose);

				return searcher;
			}
		};

		abstract ILuceneSearcher getSearcher(HashMap<String, String> parameters);
	}

	public static ILuceneSearcher getSearcher(SearcherType type,
			String indexDirectory, String configFile,
			HashMap<String, String> parameters) throws IOException
	{
		Configuration conf = new Configuration();

		conf.addResource("config.xml");

		verbose = Boolean.parseBoolean(conf.get("cogcomp.retriever.verbose"));
//		if (conf.get("cogcomp.retriever.useTitleAndText").equals("true"))
//		{
//			fields = new String[] { "text", "title" };
//		}
//		else
//		{
//			fields = new String[] { "text" };
//		}
//
//		ngrams = Boolean.parseBoolean(conf.get("cogcomp.retriever.useNGrams"));

		ILuceneSearcher searcher = type.getSearcher(parameters);

		searcher.open(indexDirectory);

		return searcher;
	}

}
