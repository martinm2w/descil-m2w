/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.util;

import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.io.*;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import java.util.List;
/**
 *
 * @author ruobo
 */
public class ChineseSegmenter {
    
    public static String segmentChinese(String input){
//    if (args.length != 1) {
//      System.err.println("usage: java -mx2g SegDemo filename");
//      return;
//    }

    Properties props = new Properties();
    props.setProperty("sighanCorporaDict", "/home/ruobo/develop/scil0200/tools/chinese_segmenter/data");
    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
    // props.setProperty("normTableEncoding", "UTF-8");
    // below is needed because CTBSegDocumentIteratorFactory accesses it
    props.setProperty("serDictionary","/home/ruobo/develop/scil0200/tools/chinese_segmenter/data/dict-chris6.ser.gz");
//    props.setProperty("testFile", "/home/-ruobo/Desktop/1");
    props.setProperty("inputEncoding", "UTF-8");
    props.setProperty("sighanPostProcessing", "true");
    
    CRFClassifier classifier = new CRFClassifier(props);
    classifier.loadClassifierNoExceptions("/home/ruobo/develop/scil0200/tools/chinese_segmenter/data/ctb.gz", props);
    // flags must be re-set after data is loaded
    classifier.flags.setProperties(props);
    List segList = classifier.segmentString(input);
    String forTagging = "";
    for(Object a : segList){
                            forTagging += (String)a + " ";
                        }
                        System.out.println(forTagging);
//    classifier.classifyAndWriteAnswers("/home/ruobo/Desktop/1");
                        return forTagging;
  }
    
    
    public static void main(String[] args) throws Exception {
//    if (args.length != 1) {
//      System.err.println("usage: java -mx2g SegDemo filename");
//      return;
//    }

    Properties props = new Properties();
    props.setProperty("sighanCorporaDict", "/home/ruobo/develop/scil0200/tools/chinese_segmenter/data");
    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
    // props.setProperty("normTableEncoding", "UTF-8");
    // below is needed because CTBSegDocumentIteratorFactory accesses it
    props.setProperty("serDictionary","/home/ruobo/develop/scil0200/tools/chinese_segmenter/data/dict-chris6.ser.gz");
//    props.setProperty("testFile", "/home/-ruobo/Desktop/1");
    props.setProperty("inputEncoding", "UTF-8");
    props.setProperty("sighanPostProcessing", "true");
    
    CRFClassifier classifier = new CRFClassifier(props);
    classifier.loadClassifierNoExceptions("/home/ruobo/develop/scil0200/tools/chinese_segmenter/data/ctb.gz", props);
    // flags must be re-set after data is loaded
    classifier.flags.setProperties(props);
    List segList = classifier.segmentString("儿子与父亲一起种地，儿子负责把握方向，父亲拉着驴走，每次儿子套好车，跟前面的父亲说：“爹，走吧”，父亲就拉着驴向前走，日复一日。一天，父亲没来，儿子一个人套好车后，不论怎么赶驴，驴都不走，儿子气急，冷静下来，高喊一句“爹，走吧！”驴缓缓前进");
    String forTagging = "";
    for(Object a : segList){
                            forTagging += (String)a + " ";
                        }
                        System.out.println(forTagging);
//    classifier.classifyAndWriteAnswers("/home/ruobo/Desktop/1");
  }
    
    
}
