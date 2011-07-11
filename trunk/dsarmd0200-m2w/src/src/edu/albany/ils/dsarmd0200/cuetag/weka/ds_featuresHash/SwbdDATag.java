/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

/**
 *
 * @author Laura G.H. Jiao
 */
public class SwbdDATag {

    public static final String sd = "sd";
    public static final String b = "b";
    public static final String sv = "sv";
    public static final String aa = "aa";
    public static final String percentage_ = "percentage-";
    public static final String ba = "ba";
    public static final String qy = "qy";
    public static final String x = "x";
    public static final String ny = "ny";
    public static final String fc = "fc";
    public static final String percentage = "percentage";
    public static final String qw = "qw";
    public static final String nn = "nn";
    public static final String bk = "bk";
    public static final String h = "h";
    public static final String qy_d = "qy^d";
    public static final String o = "o";
    public static final String bc = "bc";
    public static final String by = "by";
    public static final String fo = "fo";
    public static final String fw = "fw";
    public static final String bh = "bh";
    public static final String _q = "^q";
    public static final String bf = "bf";
    public static final String na = "na";
    public static final String ny_e = "ny^e";
    public static final String ad = "ad";
    public static final String _2 = "^2";
    public static final String b_m = "b^m";
    public static final String qo = "qo";
    public static final String qh = "qh";
    public static final String _h = "^h";
    public static final String ar = "ar";
    public static final String ng = "ng";
    public static final String nn_e = "nn^e";
    public static final String br = "br";
    public static final String no = "no";
    public static final String fp = "fp";
    public static final String qrr = "qrr";
    public static final String arp = "arp";
    public static final String nd = "nd";
    public static final String t3 = "t3";
    public static final String oo = "oo";
    public static final String cc = "cc";
    public static final String co = "co";
    public static final String t1 = "t1";
    public static final String bd = "bd";
    public static final String aap_am = "aap/am";
    public static final String _g = "^g";
    public static final String qw_d = "qw^d";
    public static final String fa = "fa";
    public static final String ft = "ft";


    /**
     * Generate alias number for each tag
     * @param tag Tag name
     * @return alias number for tag
     */
    public int getTagAlias(String tag){

        if(tag.equals(SwbdDATag.sd))
            return 1;
        else if(tag.equals(SwbdDATag.b))
            return 2;
        else if(tag.equals(SwbdDATag.sv))
            return 3;
        else if(tag.equals(SwbdDATag.aa))
            return 4;
        else if(tag.equals(SwbdDATag.percentage_))
            return 5;
        else if(tag.equals(SwbdDATag.ba))
            return 6;
        else if(tag.equals(SwbdDATag.qy))
            return 7;
        else if(tag.equals(SwbdDATag.x))
            return 8;
        else if(tag.equals(SwbdDATag.ny))
            return 9;
        else if(tag.equals(SwbdDATag.fc))
            return 10;
        else if(tag.equals("percentage"))
            return 11;
        else if(tag.equals("qw"))
            return 12;
        else if(tag.equals("nn"))
            return 13;
        else if(tag.equals("bk"))
            return 14;
        else if(tag.equals("h"))
            return 15;
        else if(tag.equals("qy^d"))
            return 16;
        else if(tag.equals("o") || tag.equals("fo") || tag.equals("bc") || tag.equals("by") || tag.equals("fw"))
            return 17;
        else if(tag.equals("bh"))
            return 18;
        else if(tag.equals("^q"))
            return 19;
        else if(tag.equals("bf"))
            return 20;
        else if(tag.equals("na") || tag.equals("ny^e"))
            return 21;
        else if(tag.equals("ad"))
            return 22;
        else if(tag.equals("^2"))
            return 23;
        else if(tag.equals("b^m"))
            return 24;
        else if(tag.equals("qo"))
            return 25;
        else if(tag.equals("qh"))
            return 26;
        else if(tag.equals("^h"))
            return 27;
        else if(tag.equals("ar"))
            return 28;
        else if(tag.equals("ng") || tag.equals("nn^e"))
            return 29;
        else if(tag.equals("br"))
            return 30;
        else if(tag.equals("no"))
            return 31;
        else if(tag.equals("fp"))
            return 32;
        else if(tag.equals("qrr"))
            return 33;
        else if(tag.equals("arp") || tag.equals("nd"))
            return 34;
        else if(tag.equals("t3"))
            return 35;
        else if(tag.equals("oo") || tag.equals("cc") || tag.equals("co"))
            return 36;
        else if(tag.equals("t1"))
            return 37;
        else if(tag.equals("bd"))
            return 38;
        else if(tag.equals("aap/am"))
            return 39;
        else if(tag.equals("^g"))
            return 40;
        else if(tag.equals("qw^d"))
            return 41;
        else if(tag.equals("fa"))
            return 42;
        else if(tag.equals("ft"))
            return 43;
        else
            return 44;
    }

    public String getTagName(int alias){

        if(alias == 1)
            return "sd";
        else if(alias == 2)
            return "b";
        else if(alias == 3)
            return "sv";
        else if(alias == 4)
            return "aa";
        else if(alias == 5)
            return "percentage-";
        else if(alias == 6)
            return "ba";
        else if(alias == 7)
            return "qy";
        else if(alias == 8)
            return "x";
        else if(alias == 9)
            return "ny";
        else if(alias == 10)
            return "fc";
        else if(alias == 11)
            return "percentage";
        else if(alias == 12)
            return "qw";
        else if(alias == 13)
            return "nn";
        else if(alias == 14)
            return "bk";
        else if(alias == 15)
            return "h";
        else if(alias == 16)
            return "qy^d";
        else if(alias == 17)
            return "o";
        else if(alias == 18)
            return "bh";
        else if(alias == 19)
            return "^q";
        else if(alias == 20)
            return "bf";
        else if(alias == 21)
            return "na";
        else if(alias == 22)
            return "ad";
        else if(alias == 23)
            return "^2";
        else if(alias == 24)
            return "b^m";
        else if(alias == 25)
            return "qo";
        else if(alias == 26)
            return "qh";
        else if(alias == 27)
            return "^h";
        else if(alias == 28)
            return "ar";
        else if(alias == 29)
            return "ng";
        else if(alias == 30)
            return "br";
        else if(alias == 31)
            return "no";
        else if(alias == 32)
            return "fp";
        else if(alias == 33)
            return "qrr";
        else if(alias == 34)
            return "arp";
        else if(alias == 35)
            return "t3";
        else if(alias == 36)
            return "oo";
        else if(alias == 37)
            return "t1";
        else if(alias == 38)
            return "bd";
        else if(alias == 39)
            return "aap";
        else if(alias == 40)
            return "^g";
        else if(alias == 41)
            return "qw^d";
        else if(alias == 42)
            return "fa";
        else if(alias == 43)
            return "ft";
        else
            return "unknown";
    }

}
