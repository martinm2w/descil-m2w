
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

// author Laura G.H. Jiao

public class URLReplacer {

    public static String urlPattern = "http://[A-Za-z0-9]+.[A-Za-z0-9]+[/=?%-&_~`@[/]/':+!]*([^\"\"])*$";

    public static String urlReplacer(String str, String pattern){
        String result = "";
        if(str != null && pattern != null){
            String[] array = ExtractRegex.testSplit(str, pattern);
            if(array != null){
//                System.err.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                for(String i : array){
                    str = str.replace(i, " hyperlink ");
                }
            }
        }
        // by Laura, Jan 3, 2011
        if(str.toLowerCase().contains("youtubelink")){
            str = str.toLowerCase().replace("youtubelink", "hyperlink");
        }
        return str;
    }

//    public static void main(String[] args){
//        String str = "http://www.youtube.com/watch?v=ixmZak0XdYI";
//        String str2 = "something, http://www.boston.com/news/world/europe/articles/2009/03, something else.";
//        String[] array = ExtractRegex.testSplit(str2, urlPattern);
////        System.out.println(Arrays.toString(array));
////        System.out.println(urlReplacer(str2, urlPattern));
////        /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^\"\"])*$/
//    }
}
