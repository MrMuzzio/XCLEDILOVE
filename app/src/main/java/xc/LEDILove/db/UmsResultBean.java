package xc.LEDILove.db;

/**
 * Created by yuchang on 2016/11/25.
 */

public class UmsResultBean {

    public static final String TABLE_NAME = "UmsResult";

    /***
     * 序号
     */
    public static String NUMBERINDEX = "numberIndex";

    public static String ID = "_id";
    /***
     * 数据报文主体
     */
    public static String BODY = "body";
    /***
     * 上移下移
     */
    public static String TYPE = "type";
    /***
     * 颜色
     */
    public static String COLOR = "color";


    /***
     * 速度
     */
    public static String SPEED = "speed";

    /***
     * 亮度
     */
    public static String BRIGHT = "bright";
    public int id;

    public int numberIndex;

    public String body;

    public String type;

    public int color;

    public int speed;

    public int bright;


}
