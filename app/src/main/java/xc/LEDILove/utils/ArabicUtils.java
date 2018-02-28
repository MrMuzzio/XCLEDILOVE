package xc.LEDILove.utils;


import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xcgd on 2018/1/5.
 */

public class ArabicUtils {
    private static ArabicUtils single = null;
    private ArabicUtils(){};
    public static ArabicUtils getInstance(){
        if (single==null){
            single = new ArabicUtils();
            return single;
        }else {
            return single;
        }
    }
    public static void main(String[] args){
        //下面写你要测试的方法，如：

//        getArbicResult("مرحباً");
        System.out.println("مرحباً");
        System.out.println(getArbicResult("مرحباً"));
    }

    //阿拉伯文中需要变形字符的unicode码 0x621-0x64a 集合中对应不同位置变形后的unicode码
    static final int[][] Arbic_Position = //former first, last, middle, alone
            {

                    {0x621, 0xfe80, 0xfe80, 0xfe80, 0xfe80},    // 0x621

                    {0x622, 0xfe82, 0xfe81, 0xfe82, 0xfe81},

                    { 0x623,0xfe84, 0xfe83, 0xfe84, 0xfe83},

                    { 0x624,0xfe86, 0xfe85, 0xfe86, 0xfe85},

                    {0x625, 0xfe88, 0xfe87, 0xfe88, 0xfe87},

                    { 0x626,0xfe8a, 0xfe8b, 0xfe8c, 0xfe89},

                    {0x627, 0xfe8e, 0xfe8d, 0xfe8e, 0xfe8d},

                    {0x628, 0xfe90, 0xfe91, 0xfe92, 0xfe8f},   // 0x628

                    { 0x629,0xfe94, 0xfe93, 0xfe94, 0xfe93},

                    {0x62a, 0xfe96, 0xfe97, 0xfe98, 0xfe95},   // 0x62A

                    {0x62b, 0xfe9a, 0xfe9b, 0xfe9c, 0xfe99},

                    {0x62c, 0xfe9e, 0xfe9f, 0xfea0, 0xfe9d},

                    {0x62d, 0xfea2, 0xfea3, 0xfea4, 0xfea1},

                    { 0x62e,0xfea6, 0xfea7, 0xfea8, 0xfea5},

                    {0x62f, 0xfeaa, 0xfea9, 0xfeaa, 0xfea9},

                    {0x630, 0xfeac, 0xfeab, 0xfeac, 0xfeab},   // 0x630

                    {0x631, 0xfeae, 0xfead, 0xfeae, 0xfead},

                    { 0x632,0xfeb0, 0xfeaf, 0xfeb0, 0xfeaf},

                    {0x633, 0xfeb2, 0xfeb3, 0xfeb4, 0xfeb1},

                    {0x634, 0xfeb6, 0xfeb7, 0xfeb8, 0xfeb5},

                    { 0x635,0xfeba, 0xfebb, 0xfebc, 0xfeb9},

                    {0x636, 0xfebe, 0xfebf, 0xfec0, 0xfebd},

                    {0x637, 0xfec2, 0xfec3, 0xfec4, 0xfec1},

                    {0x638, 0xfec6, 0xfec7, 0xfec8, 0xfec5},  // 0x638

                    {0x639, 0xfeca, 0xfecb, 0xfecc, 0xfec9},

                    { 0x63a,0xfece, 0xfecf, 0xfed0, 0xfecd},  //0x63A

                    {0x63b, 0x63b, 0x63b, 0x63b, 0x63b},

                    {0x63c, 0x63c, 0x63c, 0x63c, 0x63c},

                    {0x63d, 0x63d, 0x63d, 0x63d, 0x63d},

                    {0x63e, 0x63e, 0x63e, 0x63e, 0x63e},

                    {0x63f, 0x63f, 0x63f, 0x63f, 0x63f},

                    { 0x640,0x640, 0x640, 0x640, 0x640},   // 0x640

                    {0x641, 0xfed2, 0xfed3, 0xfed4, 0xfed1},

                    { 0x642,0xfed6, 0xfed7, 0xfed8, 0xfed5},

                    {0x643, 0xfeda, 0xfedb, 0xfedc, 0xfed9},

                    { 0x644,0xfede, 0xfedf, 0xfee0, 0xfedd},

                    {0x645, 0xfee2, 0xfee3, 0xfee4, 0xfee1},

                    {0x646, 0xfee6, 0xfee7, 0xfee8, 0xfee5},

                    { 0x647,0xfeea, 0xfeeb, 0xfeec, 0xfee9},

                    { 0x648,0xfeee, 0xfeed, 0xfeee, 0xfeed},   // 0x648

                    {0x649, 0xfef0, 0xfef3, 0xfef4, 0xfeef},

                    {0x64a,0xfef2, 0xfef3, 0xfef4, 0xfef1},   // 0x64A

            };
    //前连集合
    //判断是否是连接前面的,采用判断该字符前一个字符的判定方法,方法是,看前一个字符是否在集合set1中。如果在,则是有连接前面的
    static final int[] theSet1={
            0x62c, 0x62d, 0x62e, 0x647, 0x639, 0x63a, 0x641, 0x642,
            0x62b, 0x635, 0x636, 0x637, 0x643, 0x645, 0x646, 0x62a,
            0x644, 0x628, 0x64a, 0x633, 0x634, 0x638, 0x626, 0x640};  // 0x640 新增
    //后连集合
    //判断是否是连接后面的,采用判断该字符后一个字符的判定方法,方法是,看后一个字符是否在集合set2中。如果在,则是有连接后面的
    static final  int[] theSet2={
            0x62c, 0x62d, 0x62e, 0x647, 0x639, 0x63a, 0x641, 0x642,
            0x62b, 0x635, 0x636, 0x637, 0x643, 0x645, 0x646, 0x62a,
            0x644, 0x628, 0x64a, 0x633, 0x634, 0x638, 0x626,
            0x627, 0x623, 0x625, 0x622, 0x62f, 0x630, 0x631, 0x632,
            0x648, 0x624, 0x629, 0x649, 0x640};   // 0x640 新增

    //连字符是以0x644开头,后面跟的是0x622,0x623,0x625,0x627,并根据情况取下面的字符数组0或1,如果0x644前一个字符是在集合1（同上面的集合1）中间,那么取数组1,否则取数组0
    static final int[][] arabic_specs=
            {
                    {0xFEF5,0xFEF6},//0x622
                    {0xFEF7,0xFEF8},//0x623
                    {0xFEF9,0xFEFA},//0x625
                    {0xFEFB,0xFEFC},//0x627
            };
    //阿拉伯文上下标字符 unicode
    static final int[] ArabicSup_Subs = {0x64b,0x64c,0x64d,0x64e,0x64f,0x650,0x651,0x652,0x653,0x654,0x655,0x656,0x657,0x658,0x659,0x65a,0x65b,0x65c,0x65d,0x65e,
                                    0x6d6,0x6d7,0x6d8,0x6d9,0x6da,0x6db,0x6dc,
                                    0x6df,0x6e0,0x6e1,0x6e2,0x6e3,0x6e4,
                                    0x6e7,0x6e8,0x6ea,0x6eb,0x6ec
    };
    //印地文上下标字符 unicode
    static final int[] HindiSup_Subs = {0x901,0x902,0x903,0x93c,0x941,0x942,0x943,0x944,0x945,0x946,0x947,0x948,0x94d,
                                    0x951,0x952,0x953,0x954,0x962,0x963,
    };
    //希伯来文上下标字符 unicode
    static final int[] HebrewSup_Subs = {0x591,0x592,0x593,0x594,0x595,0x596,0x597,0x598,0x599,0x59a,0x59b,0x59c,0x59d,0x59e,0x59f,0x5a0,
            0x5a1,0x5a2,0x5a3,0x5a4,0x5a5,0x5a6,0x5a7,0x5a8,0x5a9,0x5aa,0x5ab,0x5ac,0x5ad,0x5ae,0x5af,0x5b0,0x5b1,0x5b2,
            0x5b3,0x5b4,0x5b5,0x5b6,0x5b7,0x5b8,
            0x5bb,0x5bd,0x5bf,0x5c1,0x5c2,0x5c4,0x5c5,0x5c7
    };
    //泰文 上下标字符 unicode
    static final int[] ThaiSup_Subs = {0x0e31,0x0e34,0x0e35,0x0e36,0x0e37,0x0e38,0x0e39,0x0e3a,
                                        0x0e47,0x0e48,0x0e49,0x0e4a,0x0e4b,0x0e4c,0x0e4d,0x0e4e
    };
    //左右结构字符 unicode
    static final int[] CRLH = {0x903,0x93e,
    };

    //阿拉伯文 28个字母unicode范围 ：0x060C--0x06FE
    /**
    * 阿拉伯文排版
    * **/
    @NonNull
    public  static  String getArbicResult(String str){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0;i<str.length();i++){
            //取连续的三个字符判断
            String substr = str.substring(i,i+1);
            String pre_sub ;
            String for_sub ;
            if (i==0){
                pre_sub = "";
            }else {
                pre_sub = str.substring(i-1,i);
            }

            if (i==str.length()-1){
                for_sub = "";
            }else {
                for_sub = str.substring(i+1,i+2);
            }
            if (isArbic(substr)){ //如果当前字符是阿拉伯文
                boolean ispreconnect = false ;
                boolean isforconnect  = false;
                //排版规则1:
                // 1.判断是否前连
                if (isArbic(pre_sub)&&!pre_sub.equals("")){//如果前一个字符是阿拉伯文，判断是否前连
                    ispreconnect = getIsPreConnect(pre_sub);

                }else{//不需要判断是否前连
                }
                //2.判断是否后连
                if (isArbic(for_sub)&&!for_sub.equals("")){//如果前一个字符是阿拉伯文，判断是否后连
                    isforconnect =  getIsForConnect(for_sub);
                }else{//不需要判断是否后连
                }
                //排版规则2：
                //以0x644开头，后面跟的是0x622，0x623，0x625，0x627
                if (Integer.parseInt(gbEncoding(substr),16)==0x0644&&!for_sub.equals("")) {//是0x0644
                    int fors = Integer.parseInt(gbEncoding(for_sub),16);
                    if (fors==0x0622||fors==0x0623||fors==0x0625||fors==0x0627){//后面接0x622，0x623，0x625，0x627
                        //这种情况处理后 两个字符合并成一个字符
                        //判断0x0644前一个字符是否前连
                        int temp = 0;
                        if (ispreconnect){//是前连 取arabic_specs数组 1
                            temp = 1;
                        }else{//不是 取arabic_specs数组 0
                            temp = 0;
                        }
                        switch (fors){
                            case 0x0622:
                                substr = arabic_specs[0][temp]+"";
                                break;
                            case 0x0623:
                                substr = arabic_specs[1][temp]+"";
                                break;
                            case 0x0625:
                                substr = arabic_specs[2][temp]+"";
                                break;
                            case 0x0627:
                                substr = arabic_specs[3][temp]+"";
                                break;
                        }
                        substr = getStrFromUniCode(substr);
                        i+=1;
                    }
                }else if (isNeedChange(substr)){//不是0x0644,并且在需要变形的数组中
                    int index = 0;
                    if(!isforconnect&&ispreconnect){//前连
                        index = 1;
                    }
                    if (isforconnect&&!ispreconnect){//后连
                        index = 2;
                    }

                    if (isforconnect&&ispreconnect){//中间
                        index = 3;
                    }
                    if (!isforconnect&&!ispreconnect){//独立
                        index = 4;
                    }
                        substr = getChangeReturn(substr,index);
                    substr = getStrFromUniCode(substr);

                }
            }else{//不是阿拉伯文

            }
            stringBuffer.append(substr);
        }
        return stringBuffer.toString();
    }
    /**
     *返回重排后的字符
     * */
    private static String getChangeReturn(String substr,int index) {
        int subunicode = Integer.parseInt(gbEncoding(substr),16);
        for (int i=0;i<Arbic_Position.length;i++){
            if (Arbic_Position[i][0]==subunicode){
                substr = "\\u"+Integer.toHexString(Arbic_Position[i][index]);
            }
    }
        return substr;
    }
    //阿拉伯文 当前字符是否需要重排
    private static boolean isNeedChange(String substr) {
        int subunicode = Integer.parseInt(gbEncoding(substr),16);
        for (int i=0;i<Arbic_Position.length;i++){
           if (Arbic_Position[i][0]==subunicode){
               return true;
           }
        }
        return false;
    }
    //后连
    private static boolean getIsForConnect(String for_sub) {
        int subunicode = Integer.parseInt(gbEncoding(for_sub),16);
        for (int i=0;i<theSet2.length;i++){
            if (theSet2[i]==subunicode){
                return true;
            }
        }
        return false;
    }
    //前连
    private static boolean getIsPreConnect(String pre_sub) {
        int subunicode = Integer.parseInt(gbEncoding(pre_sub),16);
        for (int i=0;i<theSet1.length;i++){
            if (theSet1[i]==subunicode){
                return true;
            }
        }
        return false;
    }
    //阿拉伯文上下标处理
    public  static byte[] adminSup_SubArabic(byte[] str_byte,byte[] follow_byte,int dots){
        byte[] resultbyte= follow_byte;
        if (dots==12){//字体为12时 上下标字符 实体占第1-4，11-12行  虚体占5-10行
            for (int i=4*2;i<10*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }else if (dots==16){//字体为12时 上下标字符 实体占第1-6，14-16行  7-13虚体占行
            for (int i=6*2;i<12*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }
        for (int k=0;k<str_byte.length;k++){
            resultbyte[k]= (byte) (str_byte[k]|follow_byte[k]);
        }
        return  resultbyte;
    }
    //是否为需要处理的上下标特殊字符
    public static boolean isSup_SubArabic(String str){
       int subunicode = Integer.parseInt(gbEncoding(str),16);
        for (int i=0;i<ArabicSup_Subs.length;i++){
            if (ArabicSup_Subs[i]==subunicode){
                return  true;
            }
        }
        return false;
    }
    //判断字符是否是阿拉伯文
    public static  boolean isArbic (String sub){
        for (int j=0;j<sub.length();j++){
            String substr = sub.substring(j,j+1);
                if (substr.equals("")){
                    return false;
                }
                int subunicode = 0x00;
                subunicode = Integer.parseInt(gbEncoding(substr),16);
                if (((subunicode>0x0600)&&(subunicode<0x06ff))||//0600-06FF：阿拉伯文 (Arabic)
                        ((subunicode>0xfb50)&&(subunicode<0xfdff))||// FB50-FDFF：阿拉伯表達形式A (Arabic Presentation Form-A)
                        ((subunicode>0xfe70)&&(subunicode<0xfeff))){//FE70-FEFF：阿拉伯表達形式B (Arabic Presentation Form-B)
                    return  true;
                }else {
                    return false;
                }
            }

        return false;
    }

    //泰文上下标处理
    public  static byte[] adminSup_SubThai(byte[] str_byte,byte[] follow_byte,int dots){
        byte[] resultbyte= follow_byte;
        if (dots==12){//字体为12时 上下标字符 实体占第1-4，11-12行  虚体占5-10行
            for (int i=5*2;i<10*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }else if (dots==16){//字体为12时 上下标字符 实体占第1-6，14-16行  7-13虚体占行
            for (int i=6*2;i<12*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }
        for (int k=0;k<str_byte.length;k++){
            resultbyte[k]= (byte) (str_byte[k]|follow_byte[k]);
        }
        return  resultbyte;
    }
    //是否为需要处理的上下标特殊字符
    public static boolean isSup_SubThai(String str){
        int subunicode = Integer.parseInt(gbEncoding(str),16);
        for (int i=0;i<ThaiSup_Subs.length;i++){
            if (ThaiSup_Subs[i]==subunicode){
                return  true;
            }
        }
        return false;
    }
    //判断字符是否是泰文
    public static  boolean isThai (String sub){
        for (int j=0;j<sub.length();j++){
            String substr = sub.substring(j,j+1);
            if (substr.equals("")){
                return false;
            }
            int subunicode = 0x00;
            subunicode = Integer.parseInt(gbEncoding(substr),16);
            //泰文编码范围0E00-0E3a，0E3f-0E5b，
            if (((subunicode>0x0e00)&&(subunicode<0x0e3a))||
                    ((subunicode>0x0e3f)&&(subunicode<0x0e5b))){
                return  true;
            }else {
                return false;
            }
        }

        return false;
    }


    //希伯来文上下标处理
    public  static byte[] adminSup_SubHebrew(byte[] str_byte,byte[] follow_byte,int dots){
        byte[] resultbyte= follow_byte;
        if (dots==12){//字体为12时 上下标字符 实体占第1-4，11-12行  虚体占5-10行
            for (int i=4*2;i<10*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }else if (dots==16){//字体为16时 上下标字符 实体占第1-6，14-16行  7-13虚体占行
            for (int i=6*2;i<13*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }
        for (int k=0;k<str_byte.length;k++){
            resultbyte[k]= (byte) (str_byte[k]|follow_byte[k]);
        }
        return  resultbyte;
    }
    //是否为需要处理的上下标特殊字符


    public static boolean isSup_SubHebrew(String str){
        int subunicode = Integer.parseInt(gbEncoding(str),16);
        for (int i=0;i<HebrewSup_Subs.length;i++){
            if (HebrewSup_Subs[i]==subunicode){
                return  true;
            }
        }
        return false;
    }
    //判断字符是否是希伯来文
    public static  boolean isHebrew (String sub){
        for (int j=0;j<sub.length();j++){
            String substr = sub.substring(j,j+1);
            if (substr.equals("")){
                return false;
            }
            int subunicode = 0x00;
            subunicode = Integer.parseInt(gbEncoding(substr),16);
            //希伯来文编码范围：0590-05ff
            if (((subunicode>0x0590)&&(subunicode<0x05ff))
                    ){
                return  true;
            }else {
                return false;
            }
        }

        return false;
    }


    //印地文上下标处理
    public  static byte[] adminSup_SubHindi(byte[] str_byte,byte[] follow_byte,int dots){
        byte[] resultbyte= follow_byte;
        if (dots==12){//字体为12时 上下标字符 实体占第1-5，12行  虚体占6-11行
            for (int i=5*2;i<11*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }else if (dots==16){//字体为16时 上下标字符 实体占第1-6，13-16行  7-12虚体占行
            for (int i=7*2;i<13*2;i++){//每行两个字节
                str_byte[i]=0x00;//将虚体部分清除
            }
        }
        for (int k=0;k<str_byte.length;k++){
            resultbyte[k]= (byte) (str_byte[k]|follow_byte[k]);
        }
        return  resultbyte;
    }


    //是否为需要处理的印地文上下标特殊字符


    public static boolean isSup_SubHindi(String str){
        int subunicode = Integer.parseInt(gbEncoding(str),16);
        for (int i=0;i<HindiSup_Subs.length;i++){
            if (HindiSup_Subs[i]==subunicode){
                return  true;
            }
        }
        return false;
    }
    //判断字符是否是印地文
    public static  boolean isHindi (String sub){
        for (int j=0;j<sub.length();j++){
            String substr = sub.substring(j,j+1);
            if (substr.equals("")){
                return false;
            }
            int subunicode = 0x00;
            subunicode = Integer.parseInt(gbEncoding(substr),16);
            //印地文编码范围：0900-097f
            if (((subunicode>0x0900)&&(subunicode<0x097f))
                    ){
                return  true;
            }else {
                return false;
            }
        }

        return false;
    }
    /*
  * 根据字符转unicode码
  * */
    private  static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
            String hexB = Integer.toHexString(utfBytes[byteIndex]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
//            unicodeBytes = unicodeBytes + "\\u" + hexB;
            unicodeBytes = unicodeBytes  + hexB;
        }
//        System.out.println("unicodeBytes is: " + unicodeBytes);
        return unicodeBytes;
    }
    /*
    * 根据unicode转字符
    * */
    @NonNull
    private static String getStrFromUniCode(String unicode){
        StringBuffer string = new StringBuffer();

        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {

            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);

            // 追加成string
            string.append((char) data);
        }
        String s = string.toString();
        return string.toString();
    }
    public static String replaceUnicode(String sourceStr)
    {
        String regEx= "["+
                "\u0000-\u001F"+//：C0控制符及基本拉丁文 (C0 Control and Basic Latin)
                "\u007F-\u00A0" +// ：特殊 (Specials);
//                "\u0600-\u06FF"+// 阿拉伯文
                "\u064b-\u064b"+// 阿拉伯文
//                "\u0E00-\u0E7F"+//：泰文 (Thai)
                "]";
//                "\u4E00-\u9FBF"+//：CJK 统一表意符号 (CJK Unified Ideographs)
//                "\u4DC0-\u4DFF"+//：易经六十四卦符号 (Yijing Hexagrams Symbols)
//                "\u0000-\u007F"+//：C0控制符及基本拉丁文 (C0 Control and Basic Latin)
//                "\u0080-\u00FF"+//：C1控制符及拉丁：补充-1 (C1 Control and Latin 1 Supplement)
//                "\u0100-\u017F"+//：拉丁文扩展-A (Latin Extended-A)
//                "\u0180-\u024F"+//：拉丁文扩展-B (Latin Extended-B)
//                "\u0250-\u02AF"+//：国际音标扩展 (IPA Extensions)
//                "\u02B0-\u02FF"+//：空白修饰字母 (Spacing Modifiers)
//                "\u0300-\u036F"+//：结合用读音符号 (Combining Diacritics Marks)
//                "\u0370-\u03FF"+//：希腊文及科普特文 (Greek and Coptic)
//                "\u0400-\u04FF"+//：西里尔字母 (Cyrillic)
//                "\u0500-\u052F"+//：西里尔字母补充 (Cyrillic Supplement)
//                "\u0530-\u058F"+//：亚美尼亚语 (Armenian)
//                "\u0590-\u05FF"+//：希伯来文 (Hebrew)
//                "\u0600-\u06FF"+//：阿拉伯文 (Arabic)
//                "\u0700-\u074F"+//：叙利亚文 (Syriac)
//                "\u0750-\u077F"+//：阿拉伯文补充 (Arabic Supplement)
//                "\u0780-\u07BF"+//：马尔代夫语 (Thaana)
//                //"\u07C0-\u077F"+//：西非书面语言 (N'Ko)
//                "\u0800-\u085F"+//：阿维斯塔语及巴列维语 (Avestan and Pahlavi)
//                "\u0860-\u087F"+//：Mandaic
//                "\u0880-\u08AF"+//：撒马利亚语 (Samaritan)
//                "\u0900-\u097F"+//：天城文书 (Devanagari)
//                "\u0980-\u09FF"+//：孟加拉语 (Bengali)
//                "\u0A00-\u0A7F"+//：锡克教文 (Gurmukhi)
//                "\u0A80-\u0AFF"+//：古吉拉特文 (Gujarati)
//                "\u0B00-\u0B7F"+//：奥里亚文 (Oriya)
//                "\u0B80-\u0BFF"+//：泰米尔文 (Tamil)
//                "\u0C00-\u0C7F"+//：泰卢固文 (Telugu)
//                "\u0C80-\u0CFF"+//：卡纳达文 (Kannada)
//                "\u0D00-\u0D7F"+//：德拉维族语 (Malayalam)
//                "\u0D80-\u0DFF"+//：僧伽罗语 (Sinhala)
//                "\u0E00-\u0E7F"+//：泰文 (Thai)
//                "\u0E80-\u0EFF"+//：老挝文 (Lao)
//                "\u0F00-\u0FFF"+//：藏文 (Tibetan)
//                "\u1000-\u109F"+//：缅甸语 (Myanmar)
//                "\u10A0-\u10FF"+//：格鲁吉亚语 (Georgian)
//                "\u1100-\u11FF"+//：朝鲜文 (Hangul Jamo)
//                "\u1200-\u137F"+//：埃塞俄比亚语 (Ethiopic)
//                "\u1380-\u139F"+//：埃塞俄比亚语补充 (Ethiopic Supplement)
//                "\u13A0-\u13FF"+//：切罗基语 (Cherokee)
//                "\u1400-\u167F"+//：统一加拿大土著语音节 (Unified Canadian Aboriginal Syllabics)
//                "\u1680-\u169F"+//：欧甘字母 (Ogham)
//                "\u16A0-\u16FF"+//：如尼文 (Runic)
//                "\u1700-\u171F"+//：塔加拉语 (Tagalog)
//                "\u1720-\u173F"+//：Hanunóo
//                "\u1740-\u175F"+//：Buhid
//                "\u1760-\u177F"+//：Tagbanwa
//                "\u1780-\u17FF"+//：高棉语 (Khmer)
//                "\u1800-\u18AF"+//：蒙古文 (Mongolian)
//                "\u18B0-\u18FF"+//：Cham
//                "\u1900-\u194F"+//：Limbu
//                "\u1950-\u197F"+//：德宏泰语 (Tai Le)
//                "\u1980-\u19DF"+//：新傣仂语 (New Tai Lue)
//                "\u19E0-\u19FF"+//：高棉语记号 (Kmer Symbols)
//                "\u1A00-\u1A1F"+//：Buginese
//                "\u1A20-\u1A5F"+//：Batak
//                "\u1A80-\u1AEF"+//：Lanna
//                "\u1B00-\u1B7F"+//：巴厘语 (Balinese)
//                "\u1B80-\u1BB0"+//：巽他语 (Sundanese)
//                "\u1BC0-\u1BFF"+//：Pahawh Hmong
//                "\u1C00-\u1C4F"+//：雷布查语(Lepcha)
//                "\u1C50-\u1C7F"+//：Ol Chiki
//                "\u1C80-\u1CDF"+//：曼尼普尔语 (Meithei/Manipuri)
//                "\u1D00-\u1D7F"+//：语音学扩展 (Phone tic Extensions)
//                "\u1D80-\u1DBF"+//：语音学扩展补充 (Phonetic Extensions Supplement)
//                "\u1DC0-\u1DFF"+//结合用读音符号补充 (Combining Diacritics Marks Supplement)
//                "\u1E00-\u1EFF"+//：拉丁文扩充附加 (Latin Extended Additional)
//                "\u1F00-\u1FFF"+//：希腊语扩充 (Greek Extended)
//                "\u2000-\u206F"+//：常用标点 (General Punctuation)
//                "\u2070-\u209F"+//：上标及下标 (Superscripts and Subscripts)
//                "\u20A0-\u20CF"+//：货币符号 (Currency Symbols)
//                "\u20D0-\u20FF"+//：组合用记号 (Combining Diacritics Marks for Symbols)
//                "\u2100-\u214F"+//：字母式符号 (Letterlike Symbols)
//                "\u2150-\u218F"+//：数字形式 (Number Form)
//                "\u2190-\u21FF"+//：箭头 (Arrows)
//                "\u2200-\u22FF"+//：数学运算符 (Mathematical Operator)
//                "\u2300-\u23FF"+//：杂项工业符号 (Miscellaneous Technical)
//                "\u2400-\u243F"+//：控制图片 (Control Pictures)
//                "\u2440-\u245F"+//：光学识别符 (Optical Character Recognition)
//                "\u2460-\u24FF"+//：封闭式字母数字 (Enclosed Alphanumerics)
//                "\u2500-\u257F"+//：制表符 (Box Drawing)
//                "\u2580-\u259F"+//：方块元素 (Block Element)
//                "\u25A0-\u25FF"+//：几何图形 (Geometric Shapes)
//                "\u2600-\u26FF"+//：杂项符号 (Miscellaneous Symbols)
//                "\u2700-\u27BF"+//：印刷符号 (Dingbats)
//                "\u27C0-\u27EF"+//：杂项数学符号-A (Miscellaneous Mathematical Symbols-A)
//                "\u27F0-\u27FF"+//：追加箭头-A (Supplemental Arrows-A)
//                "\u2800-\u28FF"+//：盲文点字模型 (Braille Patterns)
//                "\u2900-\u297F"+//：追加箭头-B (Supplemental Arrows-B)
//                "\u2980-\u29FF"+//：杂项数学符号-B (Miscellaneous Mathematical Symbols-B)
//                "\u2A00-\u2AFF"+//：追加数学运算符 (Supplemental Mathematical Operator)
//                "\u2B00-\u2BFF"+//：杂项符号和箭头 (Miscellaneous Symbols and Arrows)
//                "\u2C00-\u2C5F"+//：格拉哥里字母 (Glagolitic)
//                "\u2C60-\u2C7F"+//：拉丁文扩展-C (Latin Extended-C)
//                "\u2C80-\u2CFF"+//：古埃及语 (Coptic)
//                "\u2D00-\u2D2F"+//：格鲁吉亚语补充 (Georgian Supplement)
//                "\u2D30-\u2D7F"+//：提非纳文 (Tifinagh)
//                "\u2D80-\u2DDF"+//：埃塞俄比亚语扩展 (Ethiopic Extended)
//                "\u2E00-\u2E7F"+//：追加标点 (Supplemental Punctuation)
//                "\u2E80-\u2EFF"+//：CJK 部首补充 (CJK Radicals Supplement)
//                "\u2F00-\u2FDF"+//：康熙字典部首 (Kangxi Radicals)
//                "\u2FF0-\u2FFF"+//：表意文字描述符 (Ideographic Description Characters)
//                "\u3000-\u303F"+//：CJK 符号和标点 (CJK Symbols and Punctuation)
//                "\u3040-\u309F"+//：日文平假名 (Hiragana)
//                "\u30A0-\u30FF"+//：日文片假名 (Katakana)
//                "\u3100-\u312F"+//：注音字母 (Bopomofo)
//                "\u3130-\u318F"+//：朝鲜文兼容字母 (Hangul Compatibility Jamo)
//                "\u3190-\u319F"+//：象形字注释标志 (Kanbun)
//                "\u31A0-\u31BF"+//：注音字母扩展 (Bopomofo Extended)
//                "\u31C0-\u31EF"+//：CJK 笔画 (CJK Strokes)
//                "\u31F0-\u31FF"+//：日文片假名语音扩展 (Katakana Phonetic Extensions)
//                "\u3200-\u32FF"+//：封闭式 CJK 文字和月份 (Enclosed CJK Letters and Months)
//                "\u3300-\u33FF"+//：CJK 兼容 (CJK Compatibility)
//                "\u3400-\u4DBF"+//：CJK 统一表意符号扩展 A (CJK Unified Ideographs Extension A)
//                "\u4DC0-\u4DFF"+//：易经六十四卦符号 (Yijing Hexagrams Symbols)
//                "\u4E00-\u9FBF"+//：CJK 统一表意符号 (CJK Unified Ideographs)
//                "\uA000-\uA48F"+//：彝文音节 (Yi Syllables)
//                "\uA490-\uA4CF"+//：彝文字根 (Yi Radicals)
//                "\uA500-\uA61F"+//：Vai
//                "\uA660-\uA6FF"+//：统一加拿大土著语音节补充 (Unified Canadian Aboriginal Syllabics Supplement)
//                "\uA700-\uA71F"+//：声调修饰字母 (Modifier Tone Letters)
//                "\uA720-\uA7FF"+//：拉丁文扩展-D (Latin Extended-D)
//                "\uA800-\uA82F"+//：Syloti Nagri
//                "\uA840-\uA87F"+//：八思巴字 (Phags-pa)
//                "\uA880-\uA8DF"+//：Saurashtra
//                "\uA900-\uA97F"+//：爪哇语 (Javanese)
//                "\uA980-\uA9DF"+//：Chakma
//                "\uAA00-\uAA3F"+//：Varang Kshiti
//                "\uAA40-\uAA6F"+//：Sorang Sompeng
//                "\uAA80-\uAADF"+//：Newari
//                "\uAB00-\uAB5F"+//：越南傣语 (Vi?t Thái)
//                "\uAB80-\uABA0"+//：Kayah Li
//                "\uAC00-\uD7AF"+//：朝鲜文音节 (Hangul Syllables)
//                //"\uD800-\uDBFF"+//：High-half zone of UTF-16
//                //"\uDC00-\uDFFF"+//：Low-half zone of UTF-16
//                "\uE000-\uF8FF"+//：自行使用区域 (Private Use Zone)
//                "\uF900-\uFAFF"+//：CJK 兼容象形文字 (CJK Compatibility Ideographs)
//                "\uFB00-\uFB4F"+//：字母表达形式 (Alphabetic Presentation Form)
//                "\uFB50-\uFDFF"+//：阿拉伯表达形式A (Arabic Presentation Form-A)
//                "\uFE00-\uFE0F"+//：变量选择符 (Variation Selector)
//                "\uFE10-\uFE1F"+//：竖排形式 (Vertical Forms)
//                "\uFE20-\uFE2F"+//：组合用半符号 (Combining Half Marks)
//                "\uFE30-\uFE4F"+//：CJK 兼容形式 (CJK Compatibility Forms)
//                "\uFE50-\uFE6F"+//：小型变体形式 (Small Form Variants)
//                "\uFE70-\uFEFF"+//：阿拉伯表达形式B (Arabic Presentation Form-B)
//                "\uFF00-\uFFEF"+//：半型及全型形式 (Halfwidth and Fullwidth Form)
//                "\uFFF0-\uFFFF]";//：特殊 (Specials);
        Pattern pattern= Pattern.compile(regEx);
        Matcher matcher=pattern.matcher(sourceStr);
        return matcher.replaceAll("");
    }
}
