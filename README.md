# Illinois Relation Identifier

This is a reconfigured version of Quang Do's Taxonomic Relation code, formerly "Jupiter". It is now mavenized, and updated to use LBJava instead of LBJ.

The Yahoo search library this code uses is out of date, so some functionality won't work until this is updated. 

The Yago code this project uses appears to be missing one method that the original Jupiter code used: NounGroup.plural(). For now, the relevant variable is set to 'false'.

The original Jupiter code makes reference to an lbjava classifier "AFRelationClassifier".  I have not been able to track down the source file for this, so have for now simply copied RelationClassifier.lbj to AFRelationClassifier.lbj.
This affects a relatively small part of the source code. 


##TODO: 

replace this with descriptive content for the illinois-relation-identifier project. 

This is a [Markdown](http://daringfireball.net/projects/markdown/)-flavoured README containing an overview
of the project that will be picked up by modern Git frontends (Gitlab, Github, Bitbucket).

## Markdown basics

### Code snippets
Include code snippets here to demonstrate system usage. Use _backticks_ (\`) to inline `code`.

For larger snippets use fencing with three backticks ` ``` ` or indent with 4 spaces:

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World");
    }

### Headers
Use `# Text` to get a level 1 header, `## Text` for level 2 and so on.

### Lists
1. Use numbers (`1. Item`) for odrered lists
* Use asterisks (`* Item`) for unordered lists


For more Markdown goodness see the [Markdown Cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet).