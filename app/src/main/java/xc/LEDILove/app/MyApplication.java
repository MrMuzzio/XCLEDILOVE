package xc.LEDILove.app;

import android.app.Application;

import com.inuker.bluetooth.library.BluetoothClient;


/**
 * craete by YuChang on 2017/11/28 10:59
 */

public class MyApplication extends Application {
    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    public BluetoothClient mClient;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        mClient = new BluetoothClient(myApplication);
    }
}
