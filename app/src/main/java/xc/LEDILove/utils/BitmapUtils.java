package xc.LEDILove.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xcgd on 2018/5/8.
 */

public class BitmapUtils {
    /**
     * 根据文件路径 获取Bitmap
     * */
    public static Bitmap getBitmap(String path){
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }
    /**
     * Bitmap缩放
     * */
    public static Bitmap scaleBitmap(Bitmap rawBitmap,int width,int hight) {
        // 得到图片原始的高宽
        int rawHeight = rawBitmap.getHeight();
        int rawWidth = rawBitmap.getWidth();
        // 设定图片新的高宽
        int newHeight = hight;
        int newWidth = width;
        // 计算缩放因子
        float heightScale = ((float) newHeight) / rawHeight;
        float widthScale = ((float) newWidth) / rawWidth;
        // 新建立矩阵
        Matrix matrix = new Matrix();
        matrix.postScale(heightScale, widthScale);
        // 设置图片的旋转角度
        //matrix.postRotate(-30);
        // 设置图片的倾斜
        //matrix.postSkew(0.1f, 0.1f);
        //将图片大小压缩
        //压缩后图片的宽和高以及kB大小均会变化
        Bitmap newBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawWidth,rawWidth, matrix, true);
        return newBitmap;
    }
    /**
     * 获取Bitmap 位图数据
     * */
    public static List<int []> getBitmapData(Bitmap bit){
        List<int []> BitData = new ArrayList<>();
        int[] RGB;
        int[] pixels = new int[bit.getWidth()*bit.getHeight()];//保存所有的像素的数组，图片宽×高
        bit.getPixels(pixels,0,bit.getWidth(),0,0,bit.getWidth(),bit.getHeight());
        for(int j = 0; j < pixels.length; j++){
            RGB= new int [3];
            int clr = pixels[j];
            RGB[0] = (clr & 0x00ff0000) >> 16; //取高两位
            RGB[1] = (clr & 0x0000ff00) >> 8; //取中两位
            RGB[2] = clr & 0x000000ff; //取低两位
//                Log.e(TAG, "drawPoint: R>>>"+RGB[0]);
//                Log.e(TAG, "drawPoint: G>>>"+RGB[1]);
//                Log.e(TAG, "drawPoint: B>>>"+RGB[2]);
//                int red  = (clr & 0x00ff0000) >> 16; //取高两位
//                int green = (clr & 0x0000ff00) >> 8; //取中两位
//                int blue = clr & 0x000000ff; //取低两位

//                Log.e("位图数据", "showGif2: "+"r="+red+",g="+green+",b="+blue);
//                int color = paseRGBtoint(red,green,blue);
//                Log.e(TAG, "颜色值>>>> "+color);
//                pointData[j%pointCount][j/pointCount] = color;
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
            BitData.add(RGB);
        }
        return BitData;
    }
    /**
     * 获取Bitmap 点阵数据
     * */
    public static int[][] getBitmapPointData(Bitmap bit){
        int[][] pointData = new int[bit.getWidth()][bit.getHeight()];
//        int[] RGB;
        int[] pixels = new int[bit.getWidth()*bit.getHeight()];//保存所有的像素的数组，图片宽×高
        bit.getPixels(pixels,0,bit.getWidth(),0,0,bit.getWidth(),bit.getHeight());
        for(int j = 0; j < pixels.length; j++){
//            RGB= new int [3];
            int clr = pixels[j];
//            RGB[0] = (clr & 0x00ff0000) >> 16; //取高两位
//            RGB[1] = (clr & 0x0000ff00) >> 8; //取中两位
//            RGB[2] = clr & 0x000000ff; //取低两位
//                Log.e(TAG, "drawPoint: R>>>"+RGB[0]);
//                Log.e(TAG, "drawPoint: G>>>"+RGB[1]);
//                Log.e(TAG, "drawPoint: B>>>"+RGB[2]);
                int red  = (clr & 0x00ff0000) >> 16; //取高两位
                int green = (clr & 0x0000ff00) >> 8; //取中两位
                int blue = clr & 0x000000ff; //取低两位

//                Log.e("位图数据", "showGif2: "+"r="+red+",g="+green+",b="+blue);
                int color = paseRGBtoint(red,green,blue);
//                Log.e(TAG, "颜色值>>>> "+color);
                pointData[j%bit.getWidth()][j/bit.getHeight()] = color;
//            BitData.add(RGB);
        }
        return pointData;
    }
    public void saveBitmap(Bitmap bitmap,String bitName) throws IOException
    {
//        File file =new File(Environment.getExternalStorageDirectory()+"/Android/"+bitName+".jpg");
        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+".jpg");
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 90, out))
            {
                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static int paseRGBtoint(int red, int green, int blue) {
        int Red = castColor(red);
        int Green = castColor(green);
        int Blue = castColor(blue);
        switch (Red){
            case 0:
                switch (Green){
                    case 0:
                        switch (Blue){
                            case 0:
                                return 0;//000
                            case 1:
                                return 5;//001
                        }
                    case 1:
                        switch (Blue){
                            case 0:
                                return 3;
                            case 1:
                                return 4;
                        }
                }
            case 1:
                switch (Green){
                    case 0:
                        switch (Blue){
                            case 0:
                                return 1;
                            case 1:
                                return 6;
                        }
                    case 1:
                        switch (Blue){
                            case 0:
                                return 2;
                            case 1:
                                return 7;
                        }
                }
        }
        return 0;
    }
    public static int castColor(int red) {
        if (red<=127){
            return 0;
        }else {
            return 1;
        }
    }
}
