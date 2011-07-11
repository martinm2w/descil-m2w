
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

// author Laura G.H. Jiao

import java.util.ArrayList;
import java.util.HashMap;

public class TagRulesPredefined {

    public HashMap<String, ArrayList<String>> tagFeaturesPredefined =
            new HashMap<String, ArrayList<String>>();

    public TagRulesPredefined(){
        init();
    }

    public String rules_filtered(String str){

        String str2 = "<start> " + str.toLowerCase().trim() + " <finish>";
        
        ArrayList<String> AD_Features = tagFeaturesPredefined.get(DsarmdDATag.AD);
        ArrayList<String> DR_Features = tagFeaturesPredefined.get(DsarmdDATag.DR);
        for(String s : AD_Features){
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.AD);
            }
        }
        for(String s : DR_Features){
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.DR);
            }
        }

        return str;
        
    }

    private void init(){
        /* Action-Directive pre-defined features */
        ArrayList<String> AD_Features = new ArrayList<String>();
        /* Disagree-Reject pre-defined features */
        ArrayList<String> DR_Features = new ArrayList<String>();

        AD_Features.add("hyperlink");
        AD_Features.add("hyperlinks");

        AD_Features.add("<start> hold on");
        AD_Features.add("<start> go");
        AD_Features.add("<start> i think we should");
        AD_Features.add("<start> why don\'t");
        AD_Features.add("<start> then pick");
        AD_Features.add("<start> and figure out");
        AD_Features.add("<start> look");
        AD_Features.add("<start> you may");
        AD_Features.add("<start> please");
        AD_Features.add("<start> compare");
        AD_Features.add("<start> remove");
        AD_Features.add("<start> we need to");
        AD_Features.add("<start> we can");

        AD_Features.add("please");
        AD_Features.add("you may");
        AD_Features.add("you could");
        AD_Features.add("google it");
        AD_Features.add("i request you to");
        AD_Features.add("we could");
        AD_Features.add("we should");
        AD_Features.add("we have to");
        AD_Features.add("we need to");
        AD_Features.add("let\'s");
        AD_Features.add("lets");
        AD_Features.add("let");
        AD_Features.add("please remember to");
        AD_Features.add("i think you can");
        AD_Features.add("you guys can");
        AD_Features.add("you guys may");
        AD_Features.add("everyone should");
        AD_Features.add("maybe we should");
        AD_Features.add("remind you to");
        AD_Features.add("remind everyone to");
        AD_Features.add("don\'t pick on");
        AD_Features.add("so decide now");




        DR_Features.add("though <finish>");
        DR_Features.add("tho <finish>");
        DR_Features.add("<start> but");
        DR_Features.add("<start> yes but");
        DR_Features.add("<start> yeah but");
        DR_Features.add("<start> nah");
        DR_Features.add("<start> no");
        DR_Features.add("<start> not really");
        DR_Features.add("<start> i\'m not");
        DR_Features.add("<start> that\'s not");
        DR_Features.add("<start> but they");
        DR_Features.add("<start> but you");
        DR_Features.add("<start> but we");
        DR_Features.add("<start> but i");
        DR_Features.add("<start> but he");
        DR_Features.add("<start> but she");
        DR_Features.add("<start> but it");
        DR_Features.add("<start> wait");
        DR_Features.add("i disagree");
        DR_Features.add("<start> i disagree");
        DR_Features.add("not really");
        DR_Features.add("but i think");
        DR_Features.add("i don\'t");
        DR_Features.add("but");
        DR_Features.add("but if");
        DR_Features.add("i disagree");
        


        tagFeaturesPredefined.put(DsarmdDATag.AD, AD_Features);
        tagFeaturesPredefined.put(DsarmdDATag.DR, DR_Features);

    }

}
