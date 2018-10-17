package xc.LEDILove.bluetooth;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;

import xc.LEDILove.Bean.Params;


/**
 * Created by TIMON on 01/24/2018.
 */
public class CommandHelper {
    //以帧格式发送:帧头+帧类型+地址+数据长度+数据+校验
    //数据包
    public static int dataType_data= 1004;
    //亮度
    public static int dataType_light= 1002;
    //速度
    public static int dataType_speed= 1001;
    //发送信息列表
    public static int dataType_mesList= 1003;
    //开关机
    public static int dataType_power= 1005;
    //电量查询
    public static int dataType_battery= 1006;
    //更改名称
    public static int dataType_rename= 1007;
    //批量发送数据
    public static int dataType_list= 1008;
    public static byte[] getCmd(Params dataBean,int datatype,boolean power) throws UnsupportedEncodingException {

        byte[] send_cmd = null;
        if (datatype==dataType_data){
//            send_cmd =  getDataPackage(dataBean);
        }else if (datatype==dataType_light){
        }else if (datatype==dataType_speed){
        }else if (datatype==dataType_mesList){
        }else if (datatype==dataType_power){
//            send_cmd =  getDataPower(dataBean,power);
        }
        return send_cmd;
    }
    public static byte[] getTestData() throws UnsupportedEncodingException {
        String body = "+++";
        byte[] edit_byte = body.getBytes("unicode");//消息体
        return edit_byte;
    }
    public static byte[] getDatamegList(Params dataBean, List<Integer> selectors) {
        byte[] send_cmd = null;
        send_cmd = new byte[10+selectors.size()];
        int point = 0;
        Helpful.catByte(StaticDatas.cms_start, 0, send_cmd, point); // 帧头
        point += StaticDatas.cms_start.length;
        send_cmd[point++] = 0x04; // 类型
        send_cmd[point++] = 0x00; // 地址
        send_cmd[point++] = 0x00; // 数据长度
        send_cmd[point++] = (byte) selectors.size(); // 数据长度
        Log.e("Selector>>>",selectors.size()+"");
        for (int i=0;i<selectors.size();i++){
            send_cmd[point++]= (byte) (selectors.get(i)+0x41);
        }
        // 检验和
        send_cmd[send_cmd.length - 1] = ISHelpful.getLrc(send_cmd,
                9,
                selectors.size());
        return send_cmd;
    }

    public static byte[] getDataSpeed(Params dataBean,int speed) {
        byte[] send_cmd = null;
        send_cmd = new byte[12];
        int point = 0;
        Helpful.catByte(StaticDatas.cms_start, 0, send_cmd, point); // 帧头
        point += StaticDatas.cms_start.length;
        send_cmd[point++] = 0x03; // 类型
        send_cmd[point++] = 0x00; // 地址
        send_cmd[point++] = 0x00; // 数据长度
        send_cmd[point++] = 0x02; // 数据长度
        send_cmd[point++] = (byte) 0xaa; // 固定位
        send_cmd[point++] = (byte) (speed+0x30); // 亮度（0x31-0x38）
        // 检验和
        send_cmd[send_cmd.length - 1] = ISHelpful.getLrc(send_cmd,
                9,
                2);
        return send_cmd;
    }

    public static byte[] getDataLight(Params dataBean,int light) {
        byte[] send_cmd = null;
        send_cmd = new byte[12];
        int point = 0;
        Helpful.catByte(StaticDatas.cms_start, 0, send_cmd, point); // 帧头
        point += StaticDatas.cms_start.length;
        send_cmd[point++] = 0x02; // 类型
        send_cmd[point++] = 0x00; // 地址
        send_cmd[point++] = 0x00; // 数据长度
        send_cmd[point++] = 0x02; // 数据长度
        send_cmd[point++] = 0x55; // 固定位
        send_cmd[point++] = (byte) (light+0x30); // 亮度（0x31-0x38）
        // 检验和
        send_cmd[send_cmd.length - 1] = ISHelpful.getLrc(send_cmd,
                9,
                2);
        return send_cmd;
    }

    private static byte[] getDataPower(Params dataBean,boolean power) {
        byte[] send_cmd = null;
        send_cmd = new byte[12];
        int point = 0;
        Helpful.catByte(StaticDatas.cms_start, 0, send_cmd, point); // 帧头
        point += StaticDatas.cms_start.length;
        send_cmd[point++] = 0x05; // 类型
        send_cmd[point++] = 0x00; // 地址
        send_cmd[point++] = 0x00; // 数据长度
        send_cmd[point++] = 0x02; // 数据长度
        send_cmd[point++] = 0x00; // 固定位
        if (power){
            send_cmd[point++] = 0x01; // 开关机 00为关 01为开
        }else {
            send_cmd[point++] = 0x00; // 开关机 00为关 01为开
        }
        // 检验和
        send_cmd[send_cmd.length - 1] = ISHelpful.getLrc(send_cmd,
                9,
                2);
        return send_cmd;
    }
    /**
     * 发送数据
     * */
//    private static byte[] getDataPackage(Params dataBean) throws UnsupportedEncodingException {
//        byte[] send_cmd = null;
////        char[] s = dataBean.editext.toCharArray();
////        byte[] edit_byte = new byte[s.length];
////        for (int j=0;j<s.length;j++){
////            edit_byte[j]= (byte) s[j];
////        }
//        String body = "";
//        byte[] edit_byte = body.getBytes("unicode");//消息体
//        send_cmd = new byte[14 + edit_byte.length - 2];
////        send_cmd = new byte[14 + edit_byte.length ];
//
//        int point = 0;
//        Helpful.catByte(StaticDatas.cms_start, 0, send_cmd, point); // 帧头
//        point += StaticDatas.cms_start.length;
//        send_cmd[point++] = 0x01; // 类型
//        send_cmd[point++] = 0x00; // 地址
//        send_cmd[point++] = (byte) ((4 + edit_byte.length-2) / 256 & 0x00FF); // 数据长度
//        send_cmd[point++] = (byte) ((4 + edit_byte.length-2) % 256 & 0x00FF); // 数据长度
////        Log.e("信息号",dataBean.mess_index-1+0x41+"");
//        send_cmd[point++] = (byte) (dataBean.mess_index-1+0x41); // 信息号(A-K)
//
//        byte style = 0x31; //
//        if (dataBean.wordfont==0){
//            style = 0x31;
//        }else if(dataBean.wordfont==1){
//            style = 0x32;
//        }else if(dataBean.wordfont==2){
//            style = 0x33;
//        }else {
//            style = 0x31;
//        }
////            switch (dataBean.wordfont) {
////                case 1:
////                    style = 0x02;
////                    break;
////                case DataBean.style_italics:
////                    style = 0x03;
////                    break;
////                case DataBean.style_standard:
////                    style = 0x01;
////                    break;
////                default:
////
////                    break;
////            }
//        byte anim = 0x38; // 默认为none
////            switch (dataBean.getAnimation()) {
////                case DataBean.animation_left:
////                    anim = 0x01;
////                    break;
////                case DataBean.animation_right:
////                    anim = 0x02;
////                    break;
////                case DataBean.animation_up:
////                    anim = 0x03;
////                    break;
////                case DataBean.animation_down:
////                    anim = 0x04;
////                    break;
////                case DataBean.animation_snow:
////                    anim = 0x05;
////                    break;
////                case DataBean.animation_open:
////                    anim = 0x06;
////                    break;
////                case DataBean.animation_close:
////                    anim = 0x07;
////                    break;
////                case DataBean.animation_none:
////                    anim = 0x08;
////                    break;
////                default:
////                    anim = 0x08;
////                    break;
////            }
//        switch (dataBean.model+1) {
//            case 1:
//                anim = 0x31;
//                break;
//            case 2:
//                anim = 0x32;
//                break;
//            case 3:
//                anim = 0x33;
//                break;
//            case 4:
//                anim = 0x34;
//                break;
//            case 5:
//                anim = 0x35;
//                break;
//            case 6:
//                anim = 0x36;
//                break;
//            case 7:
//                anim = 0x37;
//                break;
//            case 8:
//                anim = 0x38;
//                break;
//            case 9:
//                anim = 0x39;
//                break;
//            case 10:
//                anim = 0x3a;
//                break;
//            default:
//                anim = 0x38;
//                break;
//        }
//        byte size = 0x31; //
//        if (dataBean.wordsize==0){
//            size = 0x31;
//        }else if(dataBean.wordsize==1){
//            size = 0x32;
//        }else if(dataBean.wordsize==2){
//            size = 0x33;
//        }else {
//            size = 0x31;
//        }
//        send_cmd[point++] = style; // 字体类型
//        send_cmd[point++] = anim; // 动画类型
//        send_cmd[point++] = size; // 字号
//
//        // 处理信息byte
//        for (int i = 1; i < edit_byte.length / 2; i++) {
//            byte tmp = edit_byte[i * 2 + 1];
//            edit_byte[i * 2 + 1] = edit_byte[i * 2];
//            edit_byte[i * 2] = tmp;
//        }
//
//        Helpful.catByte(edit_byte, 2, send_cmd, point);
//
//        // 检验和
//        send_cmd[send_cmd.length - 1] = (byte) (ISHelpful.getLrc(send_cmd,
//                        9,
//                        edit_byte.length + 4-2));
//        Log.e("send_cmd",MYBytearrayToString(send_cmd));
//        return send_cmd;
//    }
    /**
     * byte数组转十六进制字符串
     * */
    public static String MYBytearrayToString(byte[] data) {
        String str = "";
        for (int i = 0; i < data.length; i++) {
            str += MYConvertHexByteToString(data[i]) + " ";
        }

        return str;
    }
    /**
     * byte转十六进制字符
     * */
    public static String MYConvertHexByteToString(byte byteToConvert) {
        String ConvertedByte = "";
        if (byteToConvert < 0) {
            ConvertedByte += Integer.toString(byteToConvert + 256, 16);
        } else if (byteToConvert <= 15) {
            ConvertedByte += "0" + Integer.toString(byteToConvert, 16);
        } else {
            ConvertedByte += Integer.toString(byteToConvert, 16);
        }

        return ConvertedByte;
    }

}
