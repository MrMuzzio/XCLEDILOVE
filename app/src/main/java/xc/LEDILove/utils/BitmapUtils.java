package xc.LEDILove.utils;

import android.content.res.Resources;
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
//        // 得到图片原始的高宽
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
        matrix.postScale(widthScale, heightScale);
//        matrix.postScale(heightScale, widthScale);
        // 设置图片的旋转角度
//        matrix.postRotate(-90);
        // 设置图片的倾斜
        //matrix.postSkew(0.1f, 0.1f);
        //将图片大小压缩
        //压缩后图片的宽和高以及kB大小均会变化
        Bitmap newBitmap;
        if (rawWidth>=rawHeight){
             newBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawWidth,rawHeight, matrix, true);
        }else {
            newBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawHeight,rawHeight, matrix, true);
        }
        return newBitmap;
    }
    public static Bitmap sizeCompres(String path, int rqsW, int rqsH) {
        // 用option设置返回的bitmap对象的一些属性参数
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 设置仅读取Bitmap的宽高而不读取内容
        BitmapFactory.decodeFile(path, options);// 获取到图片的宽高，放在option里边
        final int height = options.outHeight;//图片的高度放在option里的outHeight属性中
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (rqsW == 0 || rqsH == 0) {
            options.inSampleSize = 1;
        } else if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            options.inSampleSize = inSampleSize;
        }
        return BitmapFactory.decodeFile(path, options);// 主要通过option里的inSampleSize对原图片进行按比例压缩
    }
    public static Bitmap decodeBitmapFromResource(String resources,  int width , int height){

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(resources,options);
        //获取采样率
        options.inSampleSize = calculateInSampleSize(options,width,height);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(resources,options);

    }
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        int inSampleSize = 1;


        if (originalHeight > reqHeight || originalWidth > reqHeight){
            int halfHeight = originalHeight / 2;
            int halfWidth = originalWidth / 2;
            //压缩后的尺寸与所需的尺寸进行比较
            while ((halfWidth / inSampleSize) >= reqHeight && (halfHeight /inSampleSize)>=reqWidth){
                inSampleSize *= 2;
            }

        }

        return inSampleSize;



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
     * @param bit  bitmap对象
     * @param width  点阵宽
     * @param height 点阵高
     * @param type 点阵数据类型  这里处理32/48 读取手绘图片点阵时  排除第一列无效数据
     * @return
     */
    public static int[][] getPointData(Bitmap bit,int width,int height,int type){
        int rate_x = bit.getWidth()/width;
        int rate_y = bit.getHeight()/height;
        int[][] pointData = new int[width][height];
        for (int i = 0;i<width;i++){
            for (int j=0;j<height;j++){
                int clr;
                if (type==1){
                    if ((i+1)*rate_x<bit.getWidth()&&(j+1)*rate_y<bit.getHeight()){
                        clr= bit.getPixel((i+1)*rate_x,(j+1)*rate_y);
                    }else {
                        clr = bit.getPixel((i)*rate_x,(j)*rate_y);
                    }
                }else {
                    clr = bit.getPixel((i)*rate_x,(j)*rate_y);
                }
                Log.e("位图数据", clr+"" );
//                int clr = bit.getPixel((i)*rate_x,(j)*rate_y);
                int red  = (clr & 0x00ff0000) >> 16; //取高两位
                int green = (clr & 0x0000ff00) >> 8; //取中两位
                int blue = clr & 0x000000ff; //取低两位
                int color = paseRGBtoint(red,green,blue);
//                Log.e("位图数据", "颜色值>>>> "+color);
                Log.e("位图数据", "x>>>> "+i+" y>>>"+j+" color>>"+color);
                pointData[i][j] = color;
            }
        }
        return pointData;
    }
    /**
     * 获取Bitmap 点阵数据
     * */
    public static int[][] getBitmapPointData(Bitmap bit){
        int[][] pointData = new int[bit.getWidth()][bit.getHeight()];
//        int[][] pointData = new int[bit.getHeight()][bit.getWidth()];
//        int[] RGB;
        int[] pixels = new int[bit.getWidth()*bit.getHeight()];//保存所有的像素的数组，图片宽×高
        Log.e("读取图片", "getBitmapPointData: >>>"+pixels.length );
        bit.getPixels(pixels,0,bit.getWidth(),0,0,bit.getWidth(),bit.getHeight());
//        bit.getPixels(pixels,0,bit.getWidth(),0,0,bit.getWidth(),bit.getHeight());
//        int count = (bit.getWidth()>bit.getHeight()? bit.getWidth() :bit.getHeight()); //解决纵横大小不同导致的crash
        for (int i = 0;i<bit.getWidth();i++){
            for (int j=0;j<bit.getHeight();j++){
                int clr = bit.getPixel(i,j);
                int red  = (clr & 0x00ff0000) >> 16; //取高两位
                int green = (clr & 0x0000ff00) >> 8; //取中两位
                int blue = clr & 0x000000ff; //取低两位
                Log.e("位图数据", "showGif2: "+"r="+red+",g="+green+",b="+blue);
                int color = paseRGBtoint(red,green,blue);
                Log.e("位图数据", "颜色值>>>> "+color);
                Log.e("位图数据", "x>>>> "+j%bit.getWidth()+" y>>>"+j/bit.getWidth()+" color>>"+color);
                pointData[i][j] = color;
            }
        }
//        for(int j = 0; j < pixels.length; j++){
//            int clr = pixels[j];
//                int red  = (clr & 0x00ff0000) >> 16; //取高两位
//                int green = (clr & 0x0000ff00) >> 8; //取中两位
//                int blue = clr & 0x000000ff; //取低两位
//                Log.e("位图数据", "showGif2: "+"r="+red+",g="+green+",b="+blue);
//                int color = paseRGBtoint(red,green,blue);
//                Log.e("位图数据", "颜色值>>>> "+color);
//                Log.e("位图数据", "x>>>> "+j%bit.getWidth()+" y>>>"+j/bit.getWidth()+" color>>"+color);
//                pointData[j%bit.getWidth()][j/bit.getWidth()] = color;
//        }
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
