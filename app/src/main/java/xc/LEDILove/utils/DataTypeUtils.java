package xc.LEDILove.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xcgd on 2018/5/22.
 */

public class DataTypeUtils {
    /**
     *
     * 将布尔型二维数组转化成一维十六进制数组  作为最终数据
     *
     */
    public static byte[] getAdjustedData(boolean[][] datass){
        List<Byte> results = new ArrayList<>();
//        Log.e("datass.length",datass.length+"");
//        Log.e("datass[0].length",datass[0].length+"");
//        realLEDWidget = datass[0].length;
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
        byte[] data =new byte[results.size()];
        for (int k = 0;k<results.size();k++){
            data[k] =  (results.get(k));
        }
        return  data;
    }
}
