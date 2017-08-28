// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B2A4D4CC155580A4DC94C29CCCFC37EC94C2E2ECC4BCC4D22515134D0FCCB2E294CCB4E4558CCCB26D450B1D55841F9A07EA4D158417EA0F180502E514E66AE8241784548009D5C011202FC912C309020080F295CD36000000

package edu.illinois.cs.cogcomp.lbjava;

import edu.illinois.cs.cogcomp.cikm09.learning.Constants;
import edu.illinois.cs.cogcomp.cikm09.learning.Instance;
import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.*;


public class RelationClassifier$$1 extends Classifier
{
  private static final dLxCy __dLxCy = new dLxCy();
  private static final dCxLy __dCxLy = new dCxLy();
  private static final dCxCy __dCxCy = new dCxCy();
  private static final pmi __pmi = new pmi();
  private static final sTxTy __sTxTy = new sTxTy();
  private static final sCxCy __sCxCy = new sCxCy();
  private static final sTxCy __sTxCy = new sTxCy();
  private static final sCxTy __sCxTy = new sCxTy();

  public RelationClassifier$$1()
  {
    containingPackage = "edu.illinois.cs.cogcomp.lbjava";
    name = "RelationClassifier$$1";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.cikm09.learning.Instance"; }
  public String getOutputType() { return "real%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Instance))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'RelationClassifier$$1(Instance)' defined on line 34 of RelationClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    __result.addFeature(__dLxCy.featureValue(__example));
    __result.addFeature(__dCxLy.featureValue(__example));
    __result.addFeature(__dCxCy.featureValue(__example));
    __result.addFeature(__pmi.featureValue(__example));
    __result.addFeature(__sTxTy.featureValue(__example));
    __result.addFeature(__sCxCy.featureValue(__example));
    __result.addFeature(__sTxCy.featureValue(__example));
    __result.addFeature(__sCxTy.featureValue(__example));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Instance[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'RelationClassifier$$1(Instance)' defined on line 34 of RelationClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "RelationClassifier$$1".hashCode(); }
  public boolean equals(Object o) { return o instanceof RelationClassifier$$1; }

  public java.util.LinkedList getCompositeChildren()
  {
    java.util.LinkedList result = new java.util.LinkedList();
    result.add(__dLxCy);
    result.add(__dCxLy);
    result.add(__dCxCy);
    result.add(__pmi);
    result.add(__sTxTy);
    result.add(__sCxCy);
    result.add(__sTxCy);
    result.add(__sCxTy);
    return result;
  }
}

