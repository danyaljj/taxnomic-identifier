/**
 * 
 */
package edu.illinois.cs.cogcomp.jupiter.emnlp10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.common.CuratorViewNames;
import edu.illinois.cs.cogcomp.thrift.curator.Curator;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.utils.IOManager;
import edu.illinois.cs.cogcomp.utils.WordNetManager;

/**
 * @author dxquang Jun 13, 2010
 */
public class RTEData {

	private static final String RTE_TEXT = "Text";
	private static final String RTE_HYPOTHESIS = "Hypothesis";

	WordNetManager wnManager = null;
	private static final String wordNetPropertiesFile = "file_properties.xml";

	private TTransport transport = null;
	private TProtocol protocol = null;
	private Curator.Client client = null;
	private Record record = null;

	/**
	 * 
	 */
	public RTEData() {
		initializeCurator();
		wnManager = new WordNetManager(wordNetPropertiesFile);

	}

	private void initializeCurator() {
		// First we need a transport
		transport = new TSocket("grandpa.cs.uiuc.edu", 9090);
		// we are going to use a non-blocking server so need framed transport
		transport = new TFramedTransport(transport);
		// Now define a protocol which will use the transport
		protocol = new TBinaryProtocol(transport);
		// make the client
		client = new Curator.Client(protocol);
	}

	public void getOverlap(String rteFile, String pascaFile,
			String classNameFile) {

		try {
			transport.open();
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}

		System.out.println("Loading the RTE data.");
		Map<String, List<String>> rteData = readRteData(rteFile);
		System.out.println("\tAnnotating the data.");
		Map<String, List<Set<String>>> rteModifiedData = convertToSetsRteData(rteData);
		System.out.println("Done");

		transport.close();

		System.out.println("Loading Pasca's data.");
		Map<String, List<String>> classNameMapper = readClassNameFile(classNameFile);
		Set<String> pascaData = readPascaData(pascaFile, classNameMapper);
		System.out.println("Pasca's data size: " + pascaData.size());
		System.out.println("Done");

		List<Set<String>> rteTexts = rteModifiedData.get(RTE_TEXT);
		List<Set<String>> rteHypos = rteModifiedData.get(RTE_HYPOTHESIS);

		int n = rteTexts.size();

		int pairMatch = 0;
		int textMatch = 0;
		int hypoMatch = 0;

		for (int i = 0; i < n; i++) {
			Set<String> t = rteTexts.get(i);
			Set<String> h = rteHypos.get(i);
			boolean ispairmatch = false;
			boolean istextmatch = false;
			boolean ishypomatch = false;
			for (String ts : t) {
				if (pascaData.contains(ts)) {
					ispairmatch = true;
					istextmatch = true;
					System.out.println(ts);
				}
			}
			for (String hs : h) {
				if (pascaData.contains(hs)) {
					ispairmatch = true;
					ishypomatch = true;
					System.out.println(hs);
				}
			}
			if (ispairmatch == true)
				pairMatch++;
			if (istextmatch == true)
				textMatch++;
			if (ishypomatch == true)
				hypoMatch++;
		}

		System.out.println("# of pair: " + n);
		System.out.println("# of pair match: " + pairMatch);
		System.out.println("# of text match: " + textMatch);
		System.out.println("# of hypo match: " + hypoMatch);

	}

	/**
	 * @param classNameFile
	 * @return
	 */
	private Map<String, List<String>> readClassNameFile(String classNameFile) {
		ArrayList<String> lines = IOManager.readLines(classNameFile);
		Map<String, List<String>> mapper = new HashMap<String, List<String>>();
		for (String s : lines) {
			String[] splits = s.split("\\t+");
			if (splits.length != 2)
				continue;
			String className = splits[0].toLowerCase();
			String[] maps = splits[1].split(",");
			List<String> listMaps = new ArrayList<String>();
			for (String m : maps) {
				listMaps.add(m.trim().toLowerCase());
			}
			mapper.put(className, listMaps);
		}
		return mapper;
	}

	/**
	 * @param pascaFile
	 * @param classNameMapper
	 * @return
	 */
	private Set<String> readPascaData(String pascaFile,
			Map<String, List<String>> classNameMapper) {
		ArrayList<String> lines = IOManager.readLines(pascaFile);

		Set<String> data = new HashSet<String>();

		for (String s : lines) {
			String[] splits = s.split("\\t+");
			if (splits.length != 2)
				continue;
			data.add(splits[0].toLowerCase());
			List<String> classMaps = classNameMapper.get(splits[1]
					.toLowerCase());
			data.addAll(classMaps);
		}
		return data;
	}

	/**
	 * @param rteData
	 * @return
	 */
	private Map<String, List<Set<String>>> convertToSetsRteData(
			Map<String, List<String>> rteData) {

		Map<String, List<Set<String>>> data = new HashMap<String, List<Set<String>>>();

		List<String> texts = rteData.get(RTE_TEXT);
		List<Set<String>> textSplits = splitToSets(texts);
		data.put(RTE_TEXT, textSplits);

		List<String> hypos = rteData.get(RTE_HYPOTHESIS);
		List<Set<String>> hypoSplits = splitToSets(hypos);
		data.put(RTE_HYPOTHESIS, hypoSplits);

		return data;
	}

	/**
	 * @param texts
	 * @return
	 */
	private List<Set<String>> splitToSets(List<String> texts) {
		List<Set<String>> res = new ArrayList<Set<String>>();

		List<String> lemmaTexts = new ArrayList<String>();
		for (String t : texts) {

			System.out.println(t);

			record = null;

			int nTok = 0;
			int nPos = 0;
			List<Span> spanTokens = null;
			List<Span> spanPos = null;
            boolean forceUpdate = false;
			try {
				record = client.provide(CuratorViewNames.pos, t, forceUpdate);
				spanTokens = record.labelViews.get(CuratorViewNames.tokens ).getLabels();
				nTok = spanTokens.size();
				spanPos = record.labelViews.get( CuratorViewNames.pos ).getLabels();
				nPos = spanPos.size();
				if (nTok != nPos) {
					System.out
							.println("ERROR: The numbers of tokens and poses are different!");
					System.out.println("Bug text: " + t);
					System.exit(1);
				}
			} catch (ServiceUnavailableException e) {
				e.printStackTrace();
				System.out.println("Bug text: " + t);
				System.exit(1);
			} catch (AnnotationFailedException e) {
				e.printStackTrace();
				System.out.println("Bug text: " + t);
				System.exit(1);
			} catch (TException e) {
				e.printStackTrace();
				System.out.println("Bug text: " + t);
				System.exit(1);
			}

			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < nTok; i++) {
				String pos = spanPos.get(i).getLabel();
				String tok = t.substring(spanTokens.get(i).getStart(),
						spanTokens.get(i).getEnding()).toLowerCase();
				try {
					String lemma = "";
					if (pos.startsWith("N")) {
						lemma = wnManager.getLemma(tok, POS.NOUN);
					} else if (pos.startsWith("V")) {
						lemma = wnManager.getLemma(tok, POS.VERB);
					} else {
						lemma = tok;
					}
					buf.append(lemma);
					buf.append(" ");
				} catch (JWNLException e) {
					e.printStackTrace();
					System.out
							.println("ERROR: Unable to get lemma by wnManager.");
					System.out.println("Bug text: " + t);
					System.exit(1);
				}
			}
			lemmaTexts.add(buf.toString().trim());
		}

		for (String t : lemmaTexts) {
			String[] splits = t.split("\\s+");
			int n = splits.length;
			Set<String> set = new HashSet<String>();
			for (int i = 0; i < n; i++) {
				set.add(splits[i]);
				if (i < n - 1) {
					set.add(splits[i] + " " + splits[i + 1]);
				}
				if (i < n - 2) {
					set.add(splits[i] + " " + splits[i + 1] + " "
							+ splits[i + 2]);
				}
			}
			res.add(set);
		}

		return res;
	}

	/**
	 * @param rteFile
	 * @return
	 */
	private Map<String, List<String>> readRteData(String rteFile) {

		Map<String, List<String>> rteData = new HashMap<String, List<String>>();

		ArrayList<String> lines = IOManager.readLines(rteFile);

		List<String> texts = new ArrayList<String>();
		List<String> hypos = new ArrayList<String>();

		for (String s : lines) {
			if (s.startsWith("<t>")) {
				int pos = s.indexOf("</t>");
				String t = s.substring(3, pos).trim();
				texts.add(t);
			} else if (s.startsWith("<h>")) {
				int pos = s.indexOf("</h>");
				String h = s.substring(3, pos).trim();
				hypos.add(h);
			}
		}

		if (texts.size() != hypos.size()) {
			System.out
					.println("ERROR: The numbers of texts and hypotheses are not equal!");
			System.exit(1);
		}

		rteData.put(RTE_TEXT, texts);
		rteData.put(RTE_HYPOTHESIS, hypos);

		return rteData;
	}

	public static void main(String[] args) {
		String rteFile = "/Users/dxquang/tmp/jupiter/annotated_test.xml";
		String pascaFile = "/Users/dxquang/tmp/jupiter/www07-classes.txt";
		String classFile = "/Users/dxquang/tmp/jupiter/class-cluster.txt";
		RTEData data = new RTEData();
		data.getOverlap(rteFile, pascaFile, classFile);
	}
}
