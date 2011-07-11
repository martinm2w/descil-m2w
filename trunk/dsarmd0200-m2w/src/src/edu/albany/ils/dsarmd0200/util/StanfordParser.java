package edu.albany.ils.dsarmd0200.util;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.*;
//import edu.stanford.nlp.parser.lexparser.*;

public class StanfordParser 
{
    Tree t;
    //LexicalizedParser lp = new LexicalizedParser("/projects/SHARED/stanford-parser-2010-08-20/englishFactored.ser.gz");
    TokenizerFactory tf = PTBTokenizer.factory(false, new WordTokenFactory());
    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
    int count = 0;
    int subj_count = 0;
    int ad_subj_count = 0;
    int ad_count = 0;
    Collection tdl;
    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    
    public void parse(String sentence)
    {
	try {
	    System.out.println("--------- Start Parsing ---------");			
	    //			FileInputStream fis = new FileInputStream(filepath);
	    //			DataInputStream dis = new DataInputStream(fis);
	    //			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    
			
	    // prepare parser, tokenizer and tree printer
	    System.out.println("Parse sentence: " + sentence);
	    List tokens = tf.getTokenizer(new StringReader(sentence)).tokenize();
//	    lp.parse(tokens);
//	    t = lp.getBestParse();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(t);
	    tdl = gs.typedDependenciesCollapsed();
	    System.out.println("--------- End Parsing --------- " + count);	
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    System.out.println("count is while exception: " + sentence);
	    e.printStackTrace();
	}
	
    }
	
    public void showAllDependencies(Collection tdl,
				    String tag)
    {
	boolean added = false;
	boolean ad_added = false;
	boolean has_subj = false;
	count++;
	for(Iterator it = tdl.iterator(); it.hasNext();)
	    {
	    	TypedDependency td = (TypedDependency) it.next();
		if (td.reln().getLongName().indexOf("subject") != -1) {
		    has_subj = true;
		    if (!added) {
			subj_count++;
			if (tag.equalsIgnoreCase("Action-Directive")) {
			    ad_subj_count++;
			}
			added = true;
		    } 
		}
		
		if (tag.equalsIgnoreCase("Action-Directive") &&
		    !ad_added) {
		    ad_count++;
		    ad_added = true;
		    System.out.println("This utterance is " + tag);
		}
	    	/*System.out.println("Dependency " + (++count));
	    	System.out.println("Dependent: " + td.dep().value());
	    	System.out.println("Governor: " + td.gov().value());
	    	System.out.println("Relation: " + td.reln().getLongName());
	    	System.out.println("---------------");*/
	    }
	if (!has_subj) {
	    printTree(getTree());
	}
	System.out.println("Total action-directive: " + ad_count);
	System.out.println("has subj: " + ad_subj_count);
	System.out.println("Total utterances: " + count);
	System.out.println("Total subjects of the utterances: " + subj_count);
    }
    
	
    public Collection getDependencies()
    {
	return tdl;
    }
    
    
    public Tree getTree()
    {
	return t;
    }
    
    public void printTree(Tree t)
    {
	tp.printTree(t);
	tp.printTree(t.headTerminal(new CollinsHeadFinder()));//SemanticHeadFinder()));
	//System.out.println("tree label: " + t.label());
	List trees = t.subTreeList();
	
	for (int i = 0; i < trees.size(); i++) {
	    Tree sbt = (Tree)trees.get(i);
	    /*
	    if (!sbt.isLeaf()) {
		trees.addAll(sbt.subTreeList());
	    }
	    */
	    //System.out.println("sbt lable: " + sbt.label());
	}
	//System.out.println("done");
	List<Tree> leaves = t.getLeaves();
	for (int i = 0; i < leaves.size(); i++) {
	    Tree leaf = leaves.get(i);
	    //if (leaf.parent() != null) {
	    System.out.println(leaf.pennString() + " " + leaf.value());
		//}
	}
	/*
	Set dependencies = t.dependencies();
	Iterator it = dependencies.iterator();
	while (it.hasNext()) {
	    Dependency dependency = (Dependency)it.next();
	    System.out.println(dependency.toString());
	    System.out.println(dependency.name());
	}
	*/
    }
}
