package xc.LEDILove.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;


public class StaticDatas {
	public static ExecutorService poolExecutor;
	private static StaticDatas object;
	public static boolean isSupportMarFullColor = true;
	public static int  LEDHight = 12;
	public static int  LEDWidth = 48;
	public static int  VersionFunction = 2;//第一位电量   第二位时间  第三位 灯控
	public static boolean isConnectAuto = true;
	public static int  LEDVersion =  0;
	public static int  BATTERY = -1;

	public static String PreferencesName = "PreferencesName";
	public static String LibDbName       = "LibDbName";
	public static int LibDbVersion       = 1;
	
	public static String SPEED_KEY       = "SPEED_KEY";
	public static String LIGHT_KEY       = "LIGHT_KEY";
	public static final String custom_path_3232 = Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/"+"3232/";
	public static final String custom_path_4048 = Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/"+"4048/";
	public static final String TYPE_SELCET[] = { "Banner", "M0", "M1",
		"M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9" };
	
	// 头帧
	public static final byte[] cms_start = new byte[]{0x42, 0x54, 0x30, 0x33, 0x31,0x32,0x30};//BT03120
	
	// 获取静态对象
	public static StaticDatas getInstance() {
		if (object == null) {
			object = new StaticDatas();
		}
		return object;
	}

	public List<MTBLEDevice> scandevice_list;     // 扫描到的设备列表
	
	public String[] edit_list = new String[11];
	public List<String> lib_list = new ArrayList<String>();  // 库列表
	
	// 保存编辑列表
	public void saveEditInf(Context context){
		SharedPreferences prefs = context.getSharedPreferences(
				StaticDatas.PreferencesName, Context.MODE_PRIVATE);
		
		Editor edit = prefs.edit();
		
		for(int i=0; i<TYPE_SELCET.length; i++){
			edit.putString(TYPE_SELCET[i], edit_list[i]);
		}
		
		edit.commit();
	}
	
	public int speed = 1;
	public int light = 1;

	/**
	 * 判断是否具有该功能
	 * @param index 第一位电量   第二位时间  第三位 灯控
	 * @return 返回对应位置是否具有此项功能
	 */
	public static boolean judgeFunctionByBit(int index){
		String a = Integer.toBinaryString(StaticDatas.VersionFunction);
		if (a.equals("0")){
			a = "000";
		}else if (a.equals("1")){
			a="001";
		}else if (a.equals("10")){
			a = "010";
		}
		Log.e("StaticDatas", "judgeFunctionByBit: "+a );
		if (a.length()<index+1){
			return false;
		}else {
			if (a.substring(index,index+1).equals("0")){
				return false;
			}else {
				return true;
			}
		}

	}
}
