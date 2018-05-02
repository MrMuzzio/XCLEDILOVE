package xc.LEDILove.font;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;

import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.utils.ArabicUtils;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.LangUtils;


/**
 * craete by YuChang on 2017/3/6 09:34
 * <p>
 * 字库帮助类
 * 目前支持 12  16
 */
public class FontUtils {

    private Context context;

    private boolean hasChinese = false;
    private boolean hasJapanese = false;
    private boolean hasKorean = false;
    private boolean hasWestern = false;
    private List<Integer> wordWilds;
    //英文的  12点位高 占12字节宽8位，16点位高占16字节 宽8位
    private int asciiwordByteByDots = 12;

    /*
     * 字库名
     */
    public String dotMatrixFont = "";

    /*
     * 字库用几个点表示(行) 点阵字库高度
     */
    public int dots = -1;

    /***
     * 正斜粗 字库的 通用后缀 默认 Z 正体
     */
    public String zlx = "Z";

    /***
     * 列
     */
    public int line = 16;

    /*
     * 一个字用点表示需要多少字节，12X16的字体需要24个字节 16x16需要32字节 ASCII 12*8  12字节  16*8 16字节
     */
    public int wordByteByDots = 24;

    private boolean[][] matrix;
    public byte[] dataResult;
    private int totalByte = 0;
    private String str;
    private int realLEDWidget = 0;
    private int lineTotalByte = 0;
    private boolean isReadBC =false;
    private Thread word_stork;
    private List<TextBean> beanList;
    public FontUtils(Context context, String zlcs, int pix) {
        this.context = context;
//        word_stork = new Thread(new)
        if (zlcs!=null){

            this.zlx = zlcs;
        }
        setPix(pix);
    }
    public List<Integer> getWordWilds(){
        return wordWilds;
    }
    public List<TextBean> getTextBeanList(){
        return beanList;
    }
    /**
     * 获取字库信息
     */
    public synchronized boolean[][] getWordsInfo(String str, List<TextBean> beanList) {
        this.beanList = beanList;
        //由于个别字符显示问题 在读字库前先做处理
//        String inversod = inverso(str);//字符反序
        String inversod = str;
        inversod = ArabicUtils.getArbicResult(inversod);//阿拉伯文重排
        //针对阿拉伯文和希伯来文 需要反序处理
        if (ArabicUtils.isArbic(inversod)||ArabicUtils.isHebrew(inversod)){
//            Log.e("befor>>>>>>",inversod.substring(inversod.length()-1,inversod.length()));
            Log.e("befor>>>>>>",inversod);
            inversod = adminInverso(inversod);//字符反序
//            inversod = inverso(inversod);
//            Log.e("after>>>>>>",inversod.substring(inversod.length()-1,inversod.length()));
            Log.e("after>>>>>>",inversod);
        }
//        for (int i=0;i<inversod.length();i++){
//            Log.e("after",gbEncoding(inversod.substring(i,i+1)));
//        }
        inversod = ArabicUtils.replaceUnicode(inversod);//unicode不可见字符屏蔽
//        getWordsByte(adjustStr(inversod));
        getzimodata(adjustStr(inversod));
        return matrix;
    }
    /**
     * 含阿拉伯/希伯来文字符串 重新排序
     * 问题：阿拉伯/希伯来文字符串从右至左读取，一般来说直接全部反序就行
     * 当时用String.sub()方法逐一取字符时（从右向左）  在遇到连续的非阿拉伯/希伯来文时，java默认从连续字符串的左边读取
     *那么问题是：如果不区分开 在读取的时候就乱了
     * 思路：按照正常字符串的方式 从左向右读取，第一个(也就是字符串的最后一个字符)必须是正常的字符(如果不是添加“1”)
     * 从左至右 遇到正常的字符集 反序
     * */
    private String adminInverso(String str){
        String result =str;
        boolean isEndOfSpecial = false;//阿拉伯字符结尾
        boolean isendofspace =false;//空格结尾
        String endstr = str.substring(str.length()-1,str.length());
        if (ArabicUtils.isArbic(endstr)||ArabicUtils.isHebrew(endstr)){//字符串以阿拉伯文/希伯来结尾，这种情况整个字符串从右至左
            Log.e("isArbic>>>>>>",str.substring(str.length()-1,str.length()));
            isEndOfSpecial = true;
            str = str+"1";
//            result =   inverso(str);//整个字符串反序
        }
//        else if (endstr.equals(" ")){
//            isendofspace = true;
//            str = str+"1";
//        }
//        if (!str.substring(str.length()-1,str.length()).equals(" "))
//        {//字符串不以阿拉伯文结尾，从右至左解码，解码时最后剩余的非阿拉伯字符为左到右
        Log.e("isnomal>>>>>>",str.substring(str.length()-1,str.length()));
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer laststrs = new StringBuffer();
        StringBuffer arabicstrs = new StringBuffer();
        for (int i=str.length();i>0;i--){
            String sub = str.substring(i-1,i);
            String follow = "";
            if (i>1){
                follow = str.substring(i-2,i-1);
            }
            if (!ArabicUtils.isArbic(sub)&&!ArabicUtils.isHebrew(sub)){
                laststrs.append(sub);
                if ((!follow.equals(""))&&(ArabicUtils.isArbic(follow)||ArabicUtils.isHebrew(follow))){
                    stringBuffer.append(inverso(laststrs.toString()));
                    laststrs.delete(0,laststrs.length());
                }

            }
            else {
                stringBuffer.append(sub);
            }
        }
        if (isEndOfSpecial){
            stringBuffer.delete(0,1);
        }
        result =   stringBuffer.toString();
        return  result;
    }
    //字符串反序
    private String inverso (String str){

        StringBuffer stringBuffer = new StringBuffer();
        List<String> list = new ArrayList<>();
        for (int i=0;i<str.length();i++){
            String sub = str.substring(i,i+1);
            list.add(sub);
        }
        for (int j=0;j<list.size();j++){
            stringBuffer.append(list.get(list.size()-j-1));
        }
        return stringBuffer.toString();
    }
    //替换字符中英文状态下 - 字符显示错误问题
    private String adjustStr(String str){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0;i<str.length();i++){
            String subStr = str.substring(i,i+1);
            Log.e(subStr+"unicode>>>>",gbEncoding(subStr));
            if (gbEncoding(subStr).equals("\\u2013")){
                subStr = getStrFromUniCode("\\uff0d");
//                Log.e("unicode>>>>","shift");
            }
            stringBuffer.append(subStr);
        }
        return  stringBuffer.toString();
    }
//    private boolean[] isReadBCs;
    /**
     * 读取字模数据
     * */
    public byte[] getzimodata(String str){
        this.str = str;
        dataResult = new byte[str.length()*dots*2];
        if (wordWilds!=null){
            wordWilds.clear();
        }else {
            wordWilds = new ArrayList<>();//字符宽度记录
        }
        int hasDealByte = 0;
        for (int index = 0; index < str.length(); index++) {
            wordWilds.add(16);
            String subjectStr = str.substring(index, index + 1);
            String followStr ="";
            if (index<str.length()-1){
                followStr = str.substring(index+1,index+2);
            }
            byte[] data = readAllZiMo(subjectStr);
            byte[] replacedata =new byte[dots*2];
            for (int r =0;r<replacedata.length;r++){
                replacedata[r]=0x00;
            }
            if (data != null) {
                byte[] data_follow = readAllZiMo(followStr);
//                特殊国别上下标处理
                if (!followStr.equals("")){//后续字符不为空
                    //阿拉伯文上下标处理
                    if (ArabicUtils.isArbic(subjectStr)&ArabicUtils.isArbic(followStr)){//都为阿拉伯字符
                        if (ArabicUtils.isSup_SubArabic(subjectStr)){//当前字符是否为上下标特殊字符  因为阿拉伯文输入方向为左到右  所以判断当前是否为下一字符的上下标
                            if(data_follow!=null){//后续字符数据不为空
                                data = ArabicUtils.adminSup_SubArabic(data,data_follow,dots);//将当前字符数据叠加到后面字符数据中
                                index+=1;
                                System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);//当前字符的数据用空数据代替
                                hasDealByte = hasDealByte + replacedata.length;
                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                hasDealByte = hasDealByte + data.length;

                            }
                        }else {
                            System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                            hasDealByte = hasDealByte + data.length;
                        }
                    }else
                        //希伯来文上下标处理
                        if (ArabicUtils.isHebrew(subjectStr)&ArabicUtils.isHebrew(followStr)){//都为希伯来字符
                            if (ArabicUtils.isSup_SubHebrew(subjectStr)){//当前字符是否为上下标特殊字符  因为希伯来文输入方向为左到右  所以判断当前是否为下一字符的上下标
                                if(data_follow!=null){//后续字符数据不为空
                                    data = ArabicUtils.adminSup_SubHebrew(data,data_follow,dots);//将当前字符数据叠加到后面字符数据中
                                    index+=1;
                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
                                    hasDealByte = hasDealByte + replacedata.length;
                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                    hasDealByte = hasDealByte + data.length;
                                }
                            }else {
                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                hasDealByte = hasDealByte + data.length;
                            }

                        }else if (ArabicUtils.isHindi(subjectStr)&ArabicUtils.isHindi(followStr)){//都为印地文
                            if(data_follow!=null&&ArabicUtils.isSup_SubHindi(followStr)){//后续字符数据不为空,且为上下标字符
                                data = ArabicUtils.adminSup_SubHindi(data_follow,data,dots);//将后面字符数据叠加到当前字符数据中
                                index+=1;
                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                hasDealByte = hasDealByte + data.length;
                                System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
                                hasDealByte = hasDealByte + replacedata.length;
                            }
                        else {
                            System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                            hasDealByte = hasDealByte + data.length;
                        }

                        }else
                            //泰文上下标处理
                            if (ArabicUtils.isThai(subjectStr)&ArabicUtils.isThai(followStr)){//都为泰文字符
                                String follow2str = "";
                                if(index<str.length()-2){
                                    follow2str = str.substring(index+2,index+3);//泰文存在上下标同时存在的情况
                                }
                                if (!follow2str.equals("")&&ArabicUtils.isSup_SubThai(follow2str)&&ArabicUtils.isSup_SubThai(followStr)){
                                    byte[] data_follow2 = readAllZiMo(follow2str);
                                    if(data_follow2!=null){//后续字符数据不为空
                                        data = ArabicUtils.adminSup_SubThai(data_follow,data,dots);//将后面字符数据叠加到当前字符数据中
                                        data = ArabicUtils.adminSup_SubThai(data_follow2,data,dots);//将后面字符数据叠加到当前字符数据中
                                        index+=2;
                                        System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                        hasDealByte = hasDealByte + data.length;
                                        System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
                                        hasDealByte = hasDealByte + replacedata.length;
                                        System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
                                        hasDealByte = hasDealByte + replacedata.length;
                                    }
                                }else if (ArabicUtils.isSup_SubThai(followStr)){//后面字符是否为上下标特殊字符
                                    if(data_follow!=null){//后续字符数据不为空
                                        data = ArabicUtils.adminSup_SubThai(data_follow,data,dots);//将后面字符数据叠加到当前字符数据中
                                        index+=1;
                                        System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                        hasDealByte = hasDealByte + data.length;
                                        System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
                                        hasDealByte = hasDealByte + replacedata.length;
                                    }
                                }else {
                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                    hasDealByte = hasDealByte + data.length;
                                }
                            }else {
                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                                hasDealByte = hasDealByte + data.length;
                            }
                }else {
                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                    hasDealByte = hasDealByte + data.length;
                }
            }
        }


        getbooleandata(dataResult);//将读取的byte数组数据转化成boolean二维数组
        //字模读取出来的数据为十六进制，
//        fillMatrix(lineTotalByte);
        inserAemptyData();//为解决某些字库 字模数据之间连在一起没有间隔 所以在消除多余间隔前在所有字符字模数据之间插入一排空值
        fillMatrixEmpty();//字符间隔判定-解决字符间隔过大的情况

        return dataResult;
    }

    /***
     * 1.
     * @param str
     * @return
     */
    /*public byte[] getWordsByte(String str) {
        List<byte[]> databytes = new ArrayList<>();

        this.str = str;
        //预先遍历，确认总的字节数，因为不同字库12点阵字模的 宽度和字节都不一样
        totalByte = 0;
        for (int index = 0; index < str.length(); index++) {

            String subjectStr = str.substring(index, index + 1);
//            totalByte = totalByte + wordByteByDots;
            if (LangUtils.isChinese(subjectStr)) {
                totalByte = totalByte + wordByteByDots;
            } else if (LangUtils.isJapanese(subjectStr)) {
                totalByte = totalByte + wordByteByDots;
            } else if (LangUtils.isKorean(subjectStr)) {
                totalByte = totalByte + wordByteByDots;
            } else {
                if ((int) subjectStr.charAt(0) < 128) {
                    //都不是，就按照 英文处理
                    if (dots == 12) {
                        totalByte = totalByte + 12;
                    }
                    if (dots == 16) {
                        totalByte = totalByte + 16;
                    }
                } else {
//                    //特殊字符
                    totalByte = totalByte + wordByteByDots;
                }
            }
        }

        //初始化总的大小

        dataResult = new byte[totalByte];
//        isReadBCs = new boolean[str.length()];//记录读取的字库文件
        //依次读取字模信息
        //已经处理的字节
        int hasDealByte = 0;
        for (int index = 0; index < str.length(); index++) {
            //判断是 中 英 韩 日 标点符号
            String subjectStr = str.substring(index, index + 1);
            String followStr = "";
            if (index<str.length()-1){
                followStr = str.substring(index+1,index+2);
            }
//            byte[] data = readAllZiMo(subjectStr);
//            System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//            hasDealByte = hasDealByte + data.length;

            if (LangUtils.isChinese(subjectStr)) {
                hasChinese = true;
                byte[] data = readChineseZiMo(subjectStr);
//                databytes.add(data);
                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                hasDealByte = hasDealByte + data.length;
            } else if (LangUtils.isJapanese(subjectStr)) {
                hasJapanese = true;
                byte[] data = readJapaneseZiMo(subjectStr);
//                databytes.add(data);
                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                hasDealByte = hasDealByte + data.length;
            } else if (LangUtils.isKorean(subjectStr)) {
                hasKorean = true;
                byte[] data = readKoreanZiMo(subjectStr);
//                databytes.add(data);
                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                hasDealByte = hasDealByte + data.length;
            }
//                {
            //标点符号

            else if ((int) subjectStr.charAt(0) < 128) {
                byte[] data = readAsciiZiMo(subjectStr);
                if (data != null) {
//                        databytes.add(data);
                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                    hasDealByte = hasDealByte + data.length;
                }
            }
            else {
                hasWestern = true;
                byte[] replacedata =new byte[24];
                for (int r =0;r<replacedata.length;r++){
                    replacedata[r]=0x00;
                }
                byte[] data = readTSZiMo(subjectStr);
                if (data != null) {
                    byte[] data_follow = readTSZiMo(followStr);
                    if (!followStr.equals("")){//后续字符不为空
                        //阿拉伯文上下标处理
                        if (ArabicUtils.isArbic(subjectStr)&ArabicUtils.isArbic(followStr)){//都为阿拉伯字符
                            if (ArabicUtils.isSup_SubArabic(subjectStr)){//当前字符是否为上下标特殊字符  因为阿拉伯文输入方向为左到右  所以判断当前是否为下一字符的上下标
                                if(data_follow!=null){//后续字符数据不为空
                                    data = ArabicUtils.adminSup_SubArabic(data,data_follow,dots);//将当前字符数据叠加到后面字符数据中
                                    index+=1;

//                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                    hasDealByte = hasDealByte + data.length;
//                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
//                                    hasDealByte = hasDealByte + replacedata.length;
                                }
                            }else {
//                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                hasDealByte = hasDealByte + data.length;
                            }
                        }
                        //希伯来文上下标处理
                        if (ArabicUtils.isHebrew(subjectStr)&ArabicUtils.isHebrew(followStr)){//都为希伯来字符
                            if (ArabicUtils.isSup_SubHebrew(subjectStr)){//当前字符是否为上下标特殊字符  因为希伯来文输入方向为左到右  所以判断当前是否为下一字符的上下标
                                if(data_follow!=null){//后续字符数据不为空
                                    data = ArabicUtils.adminSup_SubHebrew(data,data_follow,dots);//将当前字符数据叠加到后面字符数据中
                                    index+=1;
//                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                    hasDealByte = hasDealByte + data.length;
//                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
//                                    hasDealByte = hasDealByte + replacedata.length;
                                }
                            }else {
//                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                hasDealByte = hasDealByte + data.length;
                            }

                        }
                        //泰文上下标处理
                        if (ArabicUtils.isThai(subjectStr)&ArabicUtils.isThai(followStr)){//都为泰文字符
                            String follow2str = "";
                            if(index<str.length()-2){
                                follow2str = str.substring(index+2,index+3);//泰文存在上下标同时存在的情况
                            }
                            if (!follow2str.equals("")&&ArabicUtils.isSup_SubThai(follow2str)&&ArabicUtils.isSup_SubThai(followStr)){
                                byte[] data_follow2 = readTSZiMo(follow2str);
                                if(data_follow2!=null){//后续字符数据不为空
                                    data = ArabicUtils.adminSup_SubThai(data_follow,data,dots);//将后面字符数据叠加到当前字符数据中
                                    data = ArabicUtils.adminSup_SubThai(data_follow2,data,dots);//将后面字符数据叠加到当前字符数据中
                                    index+=2;
//                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                    hasDealByte = hasDealByte + data.length;
//                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
//                                    hasDealByte = hasDealByte + replacedata.length;
//                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
//                                    hasDealByte = hasDealByte + replacedata.length;
                                }
                            }else
                            if (ArabicUtils.isSup_SubThai(followStr)){//后面字符是否为上下标特殊字符
                                if(data_follow!=null){//后续字符数据不为空
                                    data = ArabicUtils.adminSup_SubThai(data_follow,data,dots);//将后面字符数据叠加到当前字符数据中
                                    index+=1;
//                                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                    hasDealByte = hasDealByte + data.length;
//                                    System.arraycopy(replacedata, 0, dataResult, hasDealByte, replacedata.length);
//                                    hasDealByte = hasDealByte + replacedata.length;
                                }
                            }else {
//                                System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
//                                hasDealByte = hasDealByte + data.length;
                            }
                        }
                    }
//                        databytes.add(data);
                    System.arraycopy(data, 0, dataResult, hasDealByte, data.length);
                    hasDealByte = hasDealByte + data.length;
                }
//                }

            }
//            isReadBCs[index] = isReadBC;
        }

//
//        List<Byte> byteList =new ArrayList<>();
//
//        for (int k=0;k<databytes.size();k++){
//            byte[] index =databytes.get(k);
//            for (int j=0;j<index.length;j++){
//                byteList.add(index[j]);
//            }
//        }
//        dataResult = new byte[byteList.size()];
//        for (int s = 0;s<byteList.size();s++){
//            dataResult[s] = byteList.get(s);
//        }



        dataResult = getResultWordsByte(dataResult);
//        getbooleandata(dataResult);
        //字模读取出来的数据为十六进制，
        fillMatrix(lineTotalByte);
        insertEmptyData();//为解决某些字库 字模数据之间连在一起没有间隔 所以在消除多余间隔前在所有字符字模数据之间插入一排空值
        fillMatrixEmpty();//字符间隔判定-解决字符间隔过大的情况

        return dataResult;
    }*/
    private List<Integer> spaceIndexs;//空格所在占有
    private List<Integer> ArabicIndexs;//阿拉伯文所在位置
    private List<Integer> HindiIndexs;//印地文所在位置
    private List<Integer> CRLHIndexs;//左右结构字符所在位置

    public int getLEDWidget() {
        return realLEDWidget;
    }
    /**
     * 在字符间插入一位空格
     * */
    private void inserAemptyData(){
        Log.e("matrix",matrix[0].length+"");
        //初始化空值数组
        empty_data = new boolean[dots];
        for (int i=0;i<dots;i++) {
            empty_data[i] = false;
        }
        ArrayList<boolean[]> tem = new ArrayList<>();
        int position = -1;//更改后的数组当前下标
        int matrix_index=-1;//更改前的数组当前下标
        spaceIndexs = new ArrayList<>();
        ArabicIndexs = new ArrayList<>();
        HindiIndexs = new ArrayList<>();
        CRLHIndexs = new ArrayList<>();

        for (int i=0;i<str.length();i++){
            String indexstr = str.substring(i, i + 1);
            String followstr = "";
            if (i<str.length()-1){
                followstr = str.substring(i+1,i+2);
            }
            boolean isSpace = indexstr.equals(" ");
            boolean isArabic = (!followstr.equals(""))&&ArabicUtils.isArbic(followstr)&&ArabicUtils.isArbic(indexstr);
            boolean isHindi = (!followstr.equals(""))&&ArabicUtils.isHindi(followstr)&&ArabicUtils.isHindi(indexstr);
            if (i==0&&str.length()>1){
                for (int K=0;K<17;K++){//无论是12还是16字体 横排都是两个字节 16位
                    position=position+1;
                    matrix_index+=1;
                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                    if (isArabic){
                        ArabicIndexs.add(position);
                    }
                    if (isSpace){
                        spaceIndexs.add(position);
                    }
                    if (isHindi){
                        HindiIndexs.add(position);
                    }
                    tem.add(position,indx);
                    indx =null;
                }
            }else if (i==str.length()-1&&str.length()>1){
//                matrix_index-=1;
                for (int K=0;K<15;K++){//无论是12还是16字体 横排都是两个字节 16位
                    position=position+1;
                    matrix_index+=1;
                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                    if (isArabic){
                        ArabicIndexs.add(position);
                    }
                    if (isSpace){
                        spaceIndexs.add(position);
                    }
                    if (isHindi){
                        HindiIndexs.add(position);
                    }
                    tem.add(position,indx);
                    indx =null;
                }
//                tem.add(position+1,getstrbycolumn(matrix,matrix[0].length));
            }else {
                Log.e("indexstr>>>>",indexstr+"");
                for (int j=0;j<16;j++){//无论是12还是16字体 横排都是两个字节 16位
                    position=position+1;
                    matrix_index+=1;
                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                    if (isArabic){
                        ArabicIndexs.add(position);
                    }
                    if (isSpace){
                        spaceIndexs.add(position);
                    }
                    if (isHindi){
                        HindiIndexs.add(position);
                    }
                    tem.add(position,indx);
                    indx =null;
                }
            }

//            连续的两个阿拉伯字符之间不需要插入空格
            if ((!followstr.equals(""))&&ArabicUtils.isArbic(followstr)&&ArabicUtils.isArbic(indexstr)){
                //            连续的两个印地文字符之间不需要插入空格
            }else if ((!followstr.equals(""))&&ArabicUtils.isHindi(followstr)&&ArabicUtils.isHindi(indexstr)) {

            }else {
                Log.e("insert",position+"");
                tem.add(position,empty_data);
                wordWilds.set(i,wordWilds.get(i)+1);
                position+=1;
            }
        }

        boolean[][] temps1 = new boolean[matrix.length][tem.size()];//12*n
        for (int i = 0;i<tem.size();i++){
            boolean[] pos = tem.get(i);
            for (int j=0;j<matrix.length;j++){
                temps1[j][i] = pos[j];
            }
        }
        matrix = temps1;
        Log.e("matrix>>>",matrix[0].length+"");
    }/*
    private void insertEmptyData(){//无论12*8 还是16*8 矩阵 横排都是八位  可以在每隔八位插入 中文除外
        //初始化空值数组
        empty_data = new boolean[dots];
        for (int i=0;i<dots;i++) {
            empty_data[i] = false;
        }
        Log.e("empty_data>>>>",empty_data.length+"");
        ArrayList<boolean[]> tem = new ArrayList<>();
        int position = 0;//更改后的数组当前下标
        int matrix_index=0;//更改前的数组当前下标
        spaceIndexs = new ArrayList<>();
        ArabicIndexs = new ArrayList<>();
        for (int i=0;i<str.length();i++){//因为中文位12位或者16位 其他为8位   用字符判断是否是中文
            String indexstr = str.substring(i, i + 1);
            String followstr = "";
            if (i<str.length()-1){
                followstr = str.substring(i+1,i+2);
            }
            boolean isSpace = indexstr.equals(" ");
            boolean isArabic = (!followstr.equals(""))&&ArabicUtils.isArbic(followstr)&&ArabicUtils.isArbic(indexstr);
            Log.e("indexstr>>>>",indexstr+"");
//            isReadBC = isReadBCs[i];
//            ArrayList<boolean[]> chararrs = new ArrayList<>();


//            if (dots==12){//如果是为12字体中文
//                for (int j=0;j<12;j++){//取12位
//                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
//                    tem.add(position,indx);
//                    position=position+1;
//                    matrix_index=matrix_index+1;
//                    indx =null;
//                }
//
//            }else {//如果是16字体中文
//                for (int j=0;j<16;j++){//取16位
//                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
//                    tem.add(position,indx);
//                    position=position+1;
//                    matrix_index=matrix_index+1;
//                    indx =null;
//                }
//            }
//            if (isSpace){
//                Log.e("字符>>>>","space");
//                for (int j=0;j<8;j++){//取12位
//                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
//                    tem.add(position,indx);
//                    position=position+1;
//                    matrix_index+=1;
//                    spaceIndexs.add(matrix_index);
//                    indx =null;
//                }
//            } else
            if (LangUtils.isChinese(indexstr) || LangUtils.isJapanese(indexstr) || LangUtils.isKorean(indexstr) || ((int) indexstr.charAt(0) > 128)){//如果是中文
                Log.e("字符>>>>","chinese");
                if (dots==12){//如果是为12字体中文
                    for (int j=0;j<12;j++){//取12位
                        boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                        if (isArabic){
                            ArabicIndexs.add(position);
                        }
                        tem.add(position,indx);
                        position=position+1;
                        matrix_index+=1;
                        indx =null;
                    }

                }else {//如果是16字体中文
                    for (int j=0;j<16;j++){//取16位
                        boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                        tem.add(position,indx);
                        position=position+1;
                        matrix_index+=1;
                        indx =null;
                    }
                }

            } else if (isReadBC){//特殊字符
//                if (isSpechars(indexstr)){//如果是特殊字符 ❤形  ❤形在字库中是按照英文处理的  但实际上字模宽度比一般英文大 12字体为12位
                //16字体为16位  所以要在英文处理中 单独判读字模宽
                Log.e("字符>>>>","Spechars");
                if (dots==12){
                    for (int j=0;j<12;j++){//取12位
                        boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                        tem.add(position,indx);
                        position=position+1;
                        matrix_index+=1;
                        indx =null;
                    }
                }else {
                    for (int j=0;j<16;j++){//取16位
                        boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
                        tem.add(position,indx);
                        position=position+1;
                        matrix_index+=1;
                        indx =null;
                    }
                }

            }else {
                Log.e("字符>>>>","other");
                for (int k=0;k<8;k++){//取8位
                    boolean[] indx = getstrbycolumn(matrix,matrix_index);//取一竖排
//                        Log.e("position>>>>",position+"");
                    tem.add(position,indx);
                    if (isSpace){
                        spaceIndexs.add(position);
                    }
                    position =position+1;
                    matrix_index+=1;
                    indx =null;
                }

            }
            //连续的两个阿拉伯字符之间不需要插入空格
            if ((!followstr.equals(""))&&ArabicUtils.isArbic(followstr)&&ArabicUtils.isArbic(indexstr)){

            }else{
                Log.e("insert","1");
                tem.add(position,empty_data);
                position+=1;
            }
        }
//        if (matrix[0].length>=16){//两个字符以上才需要增加
//
//            for (int i=0;i<matrix[0].length;i++){
//                boolean[] indx = getstrbycolumn(matrix,i);//取一竖排
//                tem.add(indx);
//
//                if (LangUtils.isChinese(subjectStr)){
//                    if ((i+1)%12==0){
//                        Log.e("添加空格>>>>","+1");
//                        tem.add(empty_data);
//                    }
//                }else {
//                    if ((i+1)%8==0){
//                        Log.e("添加空格>>>>","+1");
//                        tem.add(empty_data);
//                    }
//                }
//                indx=null;
//            }

        boolean[][] temps1 = new boolean[matrix.length][tem.size()];//12*n
        for (int i = 0;i<tem.size();i++){
            boolean[] pos = tem.get(i);
            for (int j=0;j<matrix.length;j++){
                temps1[j][i] = pos[j];
            }
        }
        matrix = temps1;

    }*/
    private final String[] specstrsunicode={"\\u2764","\\u00a5","\\u20ac","\\ufe49","\\u2665","\\u2661"};
    private boolean isSpechars(String str){
        boolean result =false;
        for (int i=0;i<specstrsunicode.length;i++){
            if (gbEncoding(str).equals(specstrsunicode[i])){
                result=true;
                break;
            }
        }
        return  result;
    }
    /*
    * 根据字符转unicode码
    * */
    private   String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
            String hexB = Integer.toHexString(utfBytes[byteIndex]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
//        System.out.println("unicodeBytes is: " + unicodeBytes);
        return unicodeBytes;
    }
    /*
    * 根据unicode转字符
    * */
    private String getStrFromUniCode(String unicode){
        StringBuffer string = new StringBuffer();

        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {

            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);

            // 追加成string
            string.append((char) data);
        }

        return string.toString();
    }
    private boolean[] empty_data  ;
    private boolean isSpaceVaules(int index){
        boolean result=false;
        if (spaceIndexs!=null&&spaceIndexs.size()>0){
            for (int i=0;i<spaceIndexs.size();i++){
                if (spaceIndexs.get(i)==index){
                    result =true;
                    break;
                }
            }
        }else {
            return false;
        }
        return  result;
    }
    private boolean isArabicVaules(int index){
        boolean result=false;
        if (ArabicIndexs!=null&&ArabicIndexs.size()>0){
            for (int i=0;i<ArabicIndexs.size();i++){
                if (ArabicIndexs.get(i)==index){
                    result =true;
                    break;
                }
            }
        }else {
            return false;
        }
        return  result;
    }
    private boolean isHindiVaules(int index){
        boolean result=false;
        if (HindiIndexs!=null&&HindiIndexs.size()>0){
            for (int i=0;i<HindiIndexs.size();i++){
                if (HindiIndexs.get(i)==index){
                    result =true;
                    break;
                }
            }
        }else {
            return false;
        }
        return  result;
    }
    /*
    * 2017.12.25 添加
    * 消除多余空格
    * */
    private void fillMatrixEmpty(){
        //原则：判断boolean二维数组竖排是否出现连续为Flase的情况 如果是 便舍弃一个  否则添加到新的数组中
        //
        ArrayList<boolean[]> tem = new ArrayList<>();
        int space_number=0;
        for (int i = 0;i<matrix[0].length-1;i++){
            boolean[] indx = getstrbycolumn(matrix,i);//取一竖排
            boolean[] indy = getstrbycolumn(matrix,i+1);
            if (i==matrix[0].length-1&&!Arrays.equals(empty_data,indy)){//最后一排加进去
//                Log.e(i+">>>>","last_data");
                tem.add(indy);
            } else if (isSpaceVaules(i)&&isSpaceVaules(i+1)){//如果是空格的位置
                space_number+=1;

//                Log.e(i+">>>>","空格");
                if (space_number<5){//空格位置过长  只取4个点作为空格
                    tem.add(indx);
                }
                if (space_number==16){
                    space_number=0;
                }
                if (i%17>0){
                    wordWilds.set(i/17,5);
                }
            }
            else if (!isSpaceVaules(i)&&Arrays.equals(empty_data,indx)&&Arrays.equals(empty_data,indy)){//如果相邻两列都为空 不保存
//                Log.e(i+">>>>","empty_data");
                if (i%17>0){
                    wordWilds.set(i/17,wordWilds.get(i/17)-1);
                }
            }
            else if (isArabicVaules(i)&&!isSpaceVaules(i)&&Arrays.equals(empty_data,indx)){//阿拉伯文 清除所有空格

            }else if (isHindiVaules(i)&&!isSpaceVaules(i)&&Arrays.equals(empty_data,indx)){//印地文 清除所有空格

            }
            else{
                //否则保存
//                Log.e(i+">>>>","data");
                tem.add(indx);
            }
            indx = null;
            indy = null;

        }
        boolean[][] temps1 = new boolean[matrix.length][tem.size()];//12*n
        for (int i = 0;i<tem.size();i++){
            boolean[] pos = tem.get(i);
            for (int j=0;j<matrix.length;j++){
                temps1[j][i] = pos[j];
            }
        }
        spaceIndexs=null;
        ArabicIndexs =null;
        HindiIndexs =null;
        matrix = temps1;
//        Log.e("matrix>>>>",matrix[0].length+"");
//        Log.e("tem>>>>",tem.size()+"");
//        Log.e("temps1>>>>",temps1[0].length+"");

    }
    /**
     * 取某一竖排值
     * */
    public boolean[] getstrbycolumn(boolean[][] strarray, int column){
        int columnlength = strarray.length;
        boolean[] result = new boolean[strarray.length];
        for(int i=0;i<columnlength;i++) {
            result[i] = strarray[i][column];
        }
        return result;
    }
    /**
     * 将读取出来的字模数据 按byte转成二维数组
     * 再转成boolean数组
     * */
    private void getbooleandata (byte[] data){
        int s = 0;
        matrix = new boolean[dots][str.length()*8*2];
        byte[][] tem = new byte[dots][str.length()*2];
        for (int i=0;i<str.length();i++){
            for (int j=0;j<dots;j++){
                tem[j][i*2] = data[s];
                tem[j][i*2+1] = data[s+1];
                s+=2;
            }
        }
        for (int m = 0; m < dots; m++) {
            for (int line = 0; line < tem[0].length; line++) {
                byte tmp = tem[m][line];
                for (int j2 = 0; j2 < 8; j2++) {
                    if (((tmp >> (7 - j2)) & 1) == 1) {
                        matrix[m][line * 8 + j2] = true;
                    } else {
                        matrix[m][line * 8 + j2] = false;
                    }

                }
            }
        }
    }
    /***
     *
     * @param totalLinesByte 一行多少字节
     */
    private void fillMatrix(int totalLinesByte) {
        matrix = new boolean[dots][totalLinesByte * 8];
        for (int i = 0; i < dots; i++) {
            for (int line = 0; line < totalLinesByte; line++) {
                byte tmp = dataResult[totalLinesByte * i + line];
                for (int j2 = 0; j2 < 8; j2++) {
                    if (((tmp >> (7 - j2)) & 1) == 1) {
                        matrix[i][line * 8 + j2] = true;
                    } else {
                        matrix[i][line * 8 + j2] = false;
                    }

                }
            }
        }
    }
    /**
     *
     * 将布尔型二维数组转化成一维十六进制数组  作为最终数据
     *
     */
    public byte[] getAdjustedData(boolean[][] datass){
        List<Byte> results = new ArrayList<>();
        Log.e("datass.length",datass.length+"");
        Log.e("datass[0].length",datass[0].length+"");
        realLEDWidget = datass[0].length;
        for (int i=0;i<datass.length;i++){
            //用一个最大的十六进制数 与二进制进行|运算 最低位参与运算  如果是0 最低位变成1 否则为0
            //然后再将新的十六进制数 左移一位
            byte bite = 0x00;
            int position = 0;
            int m = 0;//行尾补齐计数
            for (int j=0 ;j<datass[0].length;j++){
                byte boolea_bite = 0b0;

                if (datass[i][j]){
                    boolea_bite=0b1;
                }
                bite= (byte) (( bite) <<1);//全部左移一位  最高位去掉 最低位0补齐
                bite= (byte) (( bite|boolea_bite));//最低位（0）或上boolean数据  即将boolean数据插入到最低位
                position+=1;
                if (position==8){//放满了 添加到集合中
//                    Log.e("bite",(bite&0xff)+"");
//                    Log.e("bite",(bite)+"");
//                    results.add((byte) (bite&0xff));
                    results.add((byte) (bite));
                    bite=0x00;
                    position=0;
                }
                if ((j+1==datass[0].length)&m==0) {//位图末尾  需要换行
                    if ((datass[0].length%8)==0){//如果是八的倍数就不需要做末尾补齐处理，
                        // 如果没有这个判断，当位图数量是八的倍数时，在每行后面会添加八位的空字节  2018/01/01

                    }else {

                        bite = (byte) (bite<<(8-position));
//                        Log.e("bite_end>>>>"+m,bite+"");
                        m+=1;
                        results.add(bite);
//                    results.add((byte) (bite&0xff));
                        bite=0x00;
                        position=0;
                    }
                }
                m=0;
            }
        }
//            short bite = 0x00;
//            int position = 0;
//            for (int j=0 ;j<datass[0].length;j++){
//                short boolea_bite = 0x00;
//
//                if (datass[i][j]){
//                    boolea_bite=0x01;
//                }
//                bite= (short) (( bite|boolea_bite) <<1);
//                position+=1;
//                if (position==15){//放满了 添加到集合中
//                    Log.e("bite",bite+"");
//                    results.add(bite);
//                    bite=0x00;
//                    position=0;
//                }
//                if (j+1==datass[0].length) {//位图末尾  需要换行
//                    bite = (short) (bite<<(15-position));
//                    Log.e("bite_end",bite+"");
//                    results.add(bite);
//                    bite=0x00;
//                }
//            }
//        }
        byte[] data =new byte[results.size()];
        for (int k = 0;k<results.size();k++){
//            data =   intToBytes2(results.get(k));
//            data[k] = 0x10;
//            int s =0x00;
//            s=results.get(k);
            data[k] =  (results.get(k));
//            Helpful.catByte(data,0,intToBytes2(results.get(k)),data.length);
        }
        return  data;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value)
    {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16)& 0xFF);
        src[2] = (byte) ((value>>8)&0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static byte[] int2BytesArray(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }

    public byte[] getResultWordsByte(byte[] preByte) {
        byte[] dataCmdResult = preByte;
        //横向取模
        byte[] result = new byte[dataCmdResult.length];
        boolean currentStrIsEnglish = false;
        int preIndex = 0;
        boolean preStrIsEnglish = false;
        int totalByteSize = 0;
        for (int line = 0; line < dots; line++) {
            int hasDealByte = 0;
            realLEDWidget = 0;
            lineTotalByte = 0;
            totalByteSize = 0;
            for (int index = 0; index < str.length(); index++) {

                String subjectStr = str.substring(index, index + 1);
                int bytewei = 0;
//                bytewei = this.line;
                if (LangUtils.isChinese(subjectStr) || LangUtils.isJapanese(subjectStr) || LangUtils.isKorean(subjectStr) || ((int) subjectStr.charAt(0) > 128)) {
                    bytewei = this.line;
                    currentStrIsEnglish = false;
                } else {
                    bytewei = 8;
                    currentStrIsEnglish = true;
                }
                //  index >0  总长度是8的倍数就不要位移
                if (realLEDWidget % 8 != 0 && dots == 12) {

                    //需要进行位移
                    if (currentStrIsEnglish) {

                        byte preByte1 = result[preIndex - 1];
                        byte preByte2 = dataCmdResult[hasDealByte + 0 + line * (bytewei / 8)];
                        result[preIndex - 1] = (byte) (((preByte1 & 0xff)) | ((preByte2 & 0xff) >>> 4));
                        result[preIndex] = (byte) (dataCmdResult[hasDealByte + 0 + line * (bytewei / 8)] << 4);
                        preIndex++;
                        hasDealByte = hasDealByte + asciiwordByteByDots;
                        totalByteSize = totalByteSize + 12;
                        realLEDWidget = realLEDWidget + 8;
                        lineTotalByte = lineTotalByte + 1;
                    }
                    else {
                        for (int position = 0; position < 2; position++) {
                            //preIndex-1 是上一个字节
                            if (position == 0) {
                                byte preByte1 = result[preIndex - 1];
                                byte preByte2 = dataCmdResult[hasDealByte + 0 + line * (bytewei / 8)];
                                result[preIndex - 1] = (byte) (((preByte1 & 0xff)) | ((preByte2 & 0xff) >>> 4));
                            } else {
                                //上一个字节
                                byte preByte1 = dataCmdResult[hasDealByte + 0 + line * (bytewei / 8)];
                                byte preByte2 = dataCmdResult[hasDealByte + 1 + line * (bytewei / 8)];
                                result[preIndex] = (byte) (((preByte1 & 0xff) << 4) | ((preByte2 & 0xff) >>> 4));
                                preIndex++;
                            }

                        }
                        hasDealByte = hasDealByte + wordByteByDots;
                        totalByteSize = totalByteSize + 12;
                        realLEDWidget = realLEDWidget + 12;
                        lineTotalByte = lineTotalByte + 1;
                    }


                } else {
                    //不需要进行位移
//                    bytewei = 16;
                    if (currentStrIsEnglish) {
                        bytewei = 8;
                    } else {
                        bytewei = 16;
                    }
                    for (int position = 0; position < (bytewei / 8); position++) {
                        result[preIndex] = dataCmdResult[hasDealByte + position + line * (bytewei / 8)];
                        preIndex++;
                    }
                    if (LangUtils.isChinese(subjectStr) || LangUtils.isJapanese(subjectStr) || LangUtils.isKorean(subjectStr)|| ((int) subjectStr.charAt(0) > 128)) {
                        hasDealByte = hasDealByte + wordByteByDots;
                        totalByteSize = totalByteSize + wordByteByDots;
                        realLEDWidget = realLEDWidget + 12;
                        lineTotalByte = lineTotalByte + 2;
                    } else {
                        hasDealByte = hasDealByte + asciiwordByteByDots;
                        totalByteSize = totalByteSize + asciiwordByteByDots;
                        realLEDWidget = realLEDWidget + 8;
                        lineTotalByte = lineTotalByte + 1;
                    }
                }
                preStrIsEnglish = currentStrIsEnglish;
            }
        }
        result = Helpful.subByte(result, 0, totalByteSize);
        return result;

    }

    /**
     * @param str 单个字符
     * @return 读取韩文字模信息
     */
    protected byte[] readKoreanZiMo(String str) {
        Log.e("readKoreanZiMo",str);
        byte[] data = new byte[wordByteByDots];
        try {
            if (str.charAt(0) >= 0xAC00) {
                dotMatrixFont = wordName("HZK", "K", dots, zlx);
                AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
                in.skip(((int) str.charAt(0) - 0xAC00) * wordByteByDots);
                in.read(data, 0, wordByteByDots);
                in.close();
            } else {
                dotMatrixFont = wordNameBC("HZK", "R", dots, zlx);
                AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
                in.skip(((int) str.charAt(0)) * wordByteByDots);
                in.read(data, 0, wordByteByDots);
                in.close();
            }
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * @param str 单个字符
     * @return 读取中文字模信息
     * 12 点中文寻址 [（GHH-0xa1)*94+GLL-0xa1]*24
     */
    protected byte[] readChineseZiMo(String str) {
        Log.e("readChineseZiMo",str);
        byte[] data = new byte[wordByteByDots];
        try {
            dotMatrixFont = wordName("HZK", "S", dots, zlx);
            AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
            in.skip((((str.getBytes("GB2312")[0] < 0 ? 256 + str.getBytes("GB2312")[0] : str.getBytes("GB2312")[0]) - 0xa1) * 94 + (str.getBytes("GB2312")[1] < 0 ? 256 + str.getBytes("GB2312")[1] : str.getBytes("GB2312")[1]) - 0xa1) * wordByteByDots);
            in.read(data, 0, wordByteByDots);
            in.close();
            if (data.length == 0) {
                dotMatrixFont = wordNameBC("HZK", "R", dots, zlx);
                in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
                in.skip((((str.getBytes("GB2312")[0] < 0 ? 256 + str.getBytes("GB2312")[0] : str.getBytes("GB2312")[0]) - 0xa1) * 94 + (str.getBytes("GB2312")[1] < 0 ? 256 + str.getBytes("GB2312")[1] : str.getBytes("GB2312")[1]) - 0xa1) * wordByteByDots);
                in.read(data, 0, wordByteByDots);
                in.close();
            }
        } catch (Exception ex) {

        }
        return data;
    }

    /**
     * @param str 单个字符
     * @return 读取日文字模信息
     * [(Jhh-0X80)*189+JLL-0X40]*24
     */
    protected byte[] readJapaneseZiMo(String str) {
        Log.e("readJapaneseZiMo",str);
        byte[] data = new byte[wordByteByDots];

        try {
            dotMatrixFont = wordNameBC("HZK", "R", dots, zlx);
            AssetManager.AssetInputStream  in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
            in.skip(str.charAt(0)*wordByteByDots);
            in.read(data, 0, wordByteByDots);
            in.close();

        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * @param str 单个字符
     * @return 读取Ascii字模信息
     */
    protected byte[] readAsciiZiMo(String str) {
        Log.e("readAsciiZiMo",str);
        byte[] data = null;
        try {
//            dotMatrixFont = wordName("AS", "C", dots, zlx);
            dotMatrixFont = wordName("HZK", "LD1", dots, zlx);
//            dotMatrixFont = wordNameBC("HZK", "LD1", dots, zlx);
            //header + languge + type + wordSize + ".DZK"
            data = new byte[asciiwordByteByDots];
            Log.e("charAt",(int) str.charAt(0)+"");
            if ((int) str.charAt(0) < 128) {

                //英文字符
                AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
                in.skip((int) str.charAt(0) * asciiwordByteByDots);
                in.read(data, 0, asciiwordByteByDots);
                in.close();
            }
        } catch (Exception ex) {
        }

        return data;
    }

    protected byte[] readTSZiMo(String str) {
        Log.e("readTSZiMo",str);
        byte[] data = null;
        try {
            data = new byte[wordByteByDots];
            dotMatrixFont = wordNameBC("HZK", "C", dots, zlx);
            AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
            in.skip(((int) str.charAt(0)) * wordByteByDots);
            in.read(data, 0, wordByteByDots);
            in.close();

        } catch (Exception ex) {
            Log.e("readTSZiMo","read fail");
        }
        return data;
    }

    public void setPix(int pix) {
        if (pix == 12) {
            dots = 12;
            wordByteByDots = 24;
            line = 16;
            asciiwordByteByDots = 12;
        }
        if (pix == 16) {
            dots = 16;
            line = 16;
            wordByteByDots = 32;
            asciiwordByteByDots = 16;
        }
    }
    protected byte[] readAllZiMo(String str) {
//        Log.e("readAllZiMo",str);
        byte[] data = null;
        try {
            data = new byte[wordByteByDots];
            dotMatrixFont = wordNameOveral(zlx,dots);
            AssetManager.AssetInputStream in = (AssetManager.AssetInputStream) context.getResources().getAssets().open(dotMatrixFont);
            in.skip(((int) str.charAt(0)) * wordByteByDots);
            in.read(data, 0, wordByteByDots);
            in.close();

        } catch (Exception ex) {
        }
        return data;
    }
    private String wordName(String header, String languge, int wordSize, String wordType) {
        isReadBC =false;
        String type = "Z";
        if (wordType.equals("正体") || wordType.equals("Normall")) {
            type = "Z";
        }
        if (wordType.equals("斜体") || wordType.equals("Italic")) {
            type = "L";
        }
        if (wordType.equals("粗体") || wordType.equals("Bold")) {
            type = "C";
        }
        Log.e("wordName>>>",header + languge + type + wordSize + ".DZK");
        return header + languge + type + wordSize + ".DZK";
    }

    /***
     * 补充
     *  上面的字库没有读到，用这个里面的继续读取。
     * @param wordSize
     * @param wordType
     * @return
     */
    private String wordNameOveral(String wordType,int  wordSize){
        String type = "Z";
        if (wordType.equals("正体") || wordType.equals("Normall")) {
            type = wordSize + "" + wordSize + "";
        }
        if (wordType.equals("斜体") || wordType.equals("Italic")) {
            type = wordSize + "" + wordSize + "L";
        }
        if (wordType.equals("粗体") || wordType.equals("Bold")) {
            type = wordSize + "" + wordSize + "C";
        }
        return type + ".DZK";
    }
    private String wordNameBC(String header, String languge, int wordSize, String wordType) {
        isReadBC=true;
        String type = "Z";
        if (wordType.equals("正体") || wordType.equals("Normall")) {
            type = wordSize + "" + wordSize + "";
        }
        if (wordType.equals("斜体") || wordType.equals("Italic")) {
            type = wordSize+ "" + wordSize + "L";
        }
        if (wordType.equals("粗体") || wordType.equals("Bold")) {
            type = wordSize+ "" + wordSize + "C";
        }
        Log.e("wordNameBC>>>",type + ".DZK");
        return type + ".DZK";
    }
}