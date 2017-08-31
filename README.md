# Illinois Relation Identifier

## Programmatic usage: 

```import edu.illinois.cs.cogcomp.cikm09.learning.MainRelationIdentification;```
```MainRelationIdentification mri = new MainRelationIdentification();```
```HashMap<String, String> result = mri.identify(mapNames);```
```result1.get("RELATION");```
Sample output: 0, 1, 2, 3 for each combination of relation, as specified in ```edu.illinois.cs.cogcomp.cikm09.learning.Constants```

See ```edu.illinois.cs.cogcomp.test.SmallTest``` for a short example

## To test on data:

JupiterMain has many modules for training and testing. To run each module, set up a different configuration.
Example for supervised training: ```SimpleSupervisedLearningTrain data/20000.new.first8000.shuffled.addfeat.inter data/20000.new.last12000.shuffled.addfeat.inter```
Example for supervised testing: ```SimpleSupervisedLearningTest data/20000.new.last12000.shuffled.addfeat.inter READ_ALL```

A problem exists where the cache does not function properly, so that has been removed from the code for now. It is most likely not working because of the LBJava code it was generated from. In its place, a naively implemented system is functioning.

Notably, ```edu.illinois.cs.cogcomp.lbjava.RelationClassifier``` consistently fails, even with correct arguments supplied to JupiterMain.
