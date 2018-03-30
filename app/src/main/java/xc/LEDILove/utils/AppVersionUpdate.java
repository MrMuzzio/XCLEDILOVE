package xc.LEDILove.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xcgd on 2018/3/29.
 */

public class AppVersionUpdate {

    public  int parseJson(String jsonData) {
        int code = -1;
        try {
            JSONObject json = new JSONObject(jsonData);
            String status = json.getString("status");
            if (status.equals("successs")){
                String name = json.getString("app");
                int  versioncode = json.getInt("code");
                String versionName = json.getString("version");
                code = versioncode;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * @param mContext
     * @return APP版本号
     */
    public  int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * @param context
     * @return APP版本名称
     */
    public  String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
