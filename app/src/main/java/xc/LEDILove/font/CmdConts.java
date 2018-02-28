package xc.LEDILove.font;

/**
 * craete by YuChang on 2017/6/12 11:31
 * <p>
 * 命令集合
 */

public class CmdConts {

    //开机
    public static final String ON_LED = "BT03111<E>";
    //关机
    public static final String OFF_LED = "BT03110<E>";

    //可读回显参数
    public static final String READ_LED = "BT03100";

    /**
     * 运行列表（多条信息循环运行）
     * 1)	发送BT03130xxxxxxx<E>  x为1-8个信息编号（1-8），可以是一个，也可以最多是8个
     */
    public static final String LOOP_SHOW = "BT03130";

    //速度 1-8
    public static final String WRITE_SPEED = "BT03140";

    //亮度 1-8
    public static final String WRITE_LIGHT = "BT03150";
}
