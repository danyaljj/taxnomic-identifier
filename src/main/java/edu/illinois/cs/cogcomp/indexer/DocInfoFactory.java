/**
 * 
 */
package edu.illinois.cs.cogcomp.indexer;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * @author dxquang
 * Jan 27, 2009
 */
public class DocInfoFactory {

	public enum DocInfoType {
		
		Simple {
			ADocInfo getDocInfo() {
			
				SimpleDocInfo docInfo = new SimpleDocInfo();
				return docInfo;
				
			}
			
			DefaultSimilarity getSimilarityMetric() {

				DefaultSimilarity metric = new DefaultSimilarity();
				return metric;
				
			}
			
		},
		
		List {
			ADocInfo getDocInfo() {
			
				ListDocInfo docInfo = new ListDocInfo();
				return docInfo;
				
			}
			
			DefaultSimilarity getSimilarityMetric() {

				ListSearchSimilarity metric = new ListSearchSimilarity();
				return metric;
				
			}

		},
		
		Category {
			ADocInfo getDocInfo() {
				
				CategoryDocInfo docInfo = new CategoryDocInfo();
				return docInfo;
				
			}

			DefaultSimilarity getSimilarityMetric() {

				DefaultSimilarity metric = new DefaultSimilarity();
				return metric;
				
			}
		};
		
		abstract ADocInfo getDocInfo();
		
		abstract DefaultSimilarity getSimilarityMetric();
		
	}
	
	public static ADocInfo getDocInfo(DocInfoType type) {
		
		ADocInfo docInfo = type.getDocInfo();
		return docInfo;
		
	}
	
	public static DefaultSimilarity getSimilaritymetric(DocInfoType type) {

		DefaultSimilarity metric = type.getSimilarityMetric();
		return metric;
	
	}
	
}
