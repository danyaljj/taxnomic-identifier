// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B2A4D4CC15828CDC4D0FCCB2E294CCB4E4558CCCB26D450B1D558A6582A4D292D2AC30908E51727E715A604E666CBBA1ABA195B24D200081C8024B73000000

package edu.illinois.cs.cogcomp.lbjava;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;


public class pmi extends Classifier
{
  public pmi()
  {
    containingPackage = "edu.illinois.cs.cogcomp.lbjava";
    name = "pmi";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.cikm09.learning.Instance"; }
  public String getOutputType() { return "real"; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    double result = realValue(__example);
    return new RealPrimitiveStringFeature(containingPackage, name, "", result);
  }

  public double realValue(Object __example)
  {
    if (!(__example instanceof Instance))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'pmi(Instance)' defined on line 21 of RelationClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Instance ins = (Instance) __example;

    return ins.scorePmi_E1E2;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Instance[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'pmi(Instance)' defined on line 21 of RelationClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "pmi".hashCode(); }
  public boolean equals(Object o) { return o instanceof pmi; }
}

