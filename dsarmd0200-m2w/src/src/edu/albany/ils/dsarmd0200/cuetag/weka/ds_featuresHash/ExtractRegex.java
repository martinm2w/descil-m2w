/*
 * This class extracts regular expression(s) subString from a String
 * And saves it(them) into a String array.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Laura G.H. Jiao
 */
public class ExtractRegex {
    public static List<String> split(String s, String pattern) {
        assert s != null;
        assert pattern != null;
        return split(s, Pattern.compile(pattern));
    }

    public static List<String> split(String s, Pattern pattern) {
        assert s != null;
        assert pattern != null;
        Matcher m = pattern.matcher(s);
        List<String> ret = new ArrayList<String>();
        int start = 0;
        while (m.find()) {
            ret.add(s.substring(start, m.start()));
            ret.add(m.group());
            start = m.end();
        }
        ret.add(start >= s.length() ? "" : s.substring(start));
        return ret;
    }

    /**
     * Split a String with regex, then save all matched regex into a String array
     * @param s String to search for regex
     * @param pattern pattern to be found
     * @return String array with regex
     */
    public static String[] testSplit(String s, String pattern) {

        //System.out.printf("Splitting '%s' with pattern '%s'%n", s, pattern);
        List<String> tokens = split(s, pattern);
        //System.out.printf("Found %d matches%n", tokens.size());
//        int i = 0;
//        for (String token : tokens) {
//            System.out.printf("  %d/%d: '%s'%n", ++i, tokens.size(), token);
//        }
//        System.out.println();
        Object[] tokens_array = tokens.toArray();
        int length = tokens_array.length;
        if(length > 2){
            String[] regex_array = new String[(length-1)/2];
            int i = 0;
            for(int j = 1; j < length;){
                regex_array[i] = tokens_array[j].toString();
                //System.out.println(tokens_array[j]);
                j = j + 2;
                i++;
            }

            return regex_array;
        }
        else{ // no regex was found
            return null;
        }
    }

    /**
     * Split a String with regex patter, and return with all parts of the String including regex part.
     * @param s String to be extract with
     * @param pattern regex pattern
     * @return String array that contains all part of the String, splitted around regex.
     */
    public static String[] testSplitWithRegex(String s, String pattern) {

        List<String> tokens = split(s, pattern);
        Object[] tokens_array = tokens.toArray();
        int length = tokens_array.length;
        if(length > 0){
            String[] regex_array = new String[length];
            for(int i = 0; i < length; i++){
                regex_array[i] = tokens_array[i].toString();
            }

            return regex_array;
        }
        else{ // no regex was found
            return null;
        }
    }


//    public static void main(String args[]) {
////        testSplit("abcdefghij", "z"); // "abcdefghij"
////        testSplit("abcdefghij", "f"); // "abcde", "f", "ghi"
////        testSplit("abcdefghij", "j"); // "abcdefghi", "j", ""
////        testSplit("abcdefghij", "a"); // "", "a", "bcdefghij"
////        testSplit("abcdefghij", "[bdfh]"); // "a", "b", "c", "d", "e", "f", "g", "h", "ij"
//
//
//        String[] array = testSplit("abcdefghij", "[bdfh]");
//        System.out.println(Arrays.toString(array));
//
////        String[] array =
////                testSplit("(ROOT [37.047] (SBARQ [31.638] (WHNP [2.639] (WP [1.477] what)) (SQ [27.242] (VBZ [0.560] is) (NP [16.155] (DT [0.650] the) (NNP [11.597] QUOTATIONbigSPACEappleQUOTATION)) (ADVP [4.607] (RB [4.316] here))) (. [0.004] ?)))",
////                "\\(.*\\)");
////        if(array != null){
////            for(int i = 0; i < array.length; i++){
////                System.out.println(array[i]);
////            }
////        }
////System.out.println("-------------------------------------------------");
////        String[] arrayWithRegex =
////                testSplitWithRegex("(ROOT [37.047] (SBARQ [31.638] (WHNP [2.639] (WP [1.477] what)) (SQ [27.242] (VBZ [0.560] is) (NP [16.155] (DT [0.650] the) (NNP [11.597] QUOTATIONbigSPACEappleQUOTATION)) (ADVP [4.607] (RB [4.316] here))) (. [0.004] ?)))",
////                "\\(NP.*\\)");
////        if(arrayWithRegex != null){
////            System.out.println("length: " + arrayWithRegex.length);
////            for(int i = 0; i < arrayWithRegex.length; i++){
////                System.out.println(arrayWithRegex[i]);
////            }
////        }
//    }
}



