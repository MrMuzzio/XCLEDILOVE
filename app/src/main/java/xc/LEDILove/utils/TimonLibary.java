package xc.LEDILove.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xcgd on 2018/2/28.
 *
 */

public class TimonLibary {
    /**
     * button点击频率控制
     */
    private static long lastClickTime;//上次点击的时间值
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();//获取当前时间值
        long timeD = time - lastClickTime;//时间差
        if ( 0 < timeD && timeD < 500) {       //500毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }
    //调用：事件监听中
//     if (TimonLibary.isFastDoubleClick()) {
//
//        return;
//    }
    //弹出软键盘
    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }
    //隐藏软键盘
    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    //切换软键盘状态(隐藏/弹出)
    public static void  toggleSoftInput(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(0,0);
        }
    }
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density =  metrics.density;
        return metrics.widthPixels;
    }
    public static int getScreenHigh(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density =  metrics.density;
        return metrics.heightPixels;
    }
    /**
     * dp转换成px
     */
    public static  int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
    public static byte[]intToBytes(int n){
        String s=String.valueOf(n);
        return s.getBytes();
    }
    /**
     * 将十进制整数转为十六进制数，并补位
     * */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }
    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    /**
     * 将从系统中取出的日期校正成标准的两位 如9/3  09/03
     * @param data 校正之前的日期
     * @return 返回校正后的日期
     */
    public static String adjustData(String data){
        StringBuffer stringBuffer = new StringBuffer();
        String[] strings = data.split("/");
        for (String str: strings){
            if (str.length()==1){
                str = "0"+str;
            }
            stringBuffer.append(str);
            stringBuffer.append("/");
        }
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        return stringBuffer.toString();
    }

    /**
     * 将从系统中取出的时间校正成标准的两位 如5:3  05：03
     * @param time
     * @return
     */
    public static String adjustTime(String time){
        StringBuffer stringBuffer = new StringBuffer();
        String[] strings = time.split(":");
        for (String str: strings){
            if (str.length()==1){
                str = "0"+str;
            }
            stringBuffer.append(str);
            stringBuffer.append(":");
        }
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        return stringBuffer.toString();
    }
}
