package xc.LEDILove.utils;

import android.text.Editable;
import android.widget.Button;

import com.handmark.pulltorefresh.library.internal.Utils;

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
}
