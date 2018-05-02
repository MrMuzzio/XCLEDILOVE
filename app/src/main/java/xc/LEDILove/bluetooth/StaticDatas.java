package xc.LEDILove.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
	public static int  VersionFunction = 0;
	public static int  LEDVersion =  0;

	public static String PreferencesName = "PreferencesName";
	public static String LibDbName       = "LibDbName";
	public static int LibDbVersion       = 1;
	
	public static String SPEED_KEY       = "SPEED_KEY";
	public static String LIGHT_KEY       = "LIGHT_KEY";
	
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
}
