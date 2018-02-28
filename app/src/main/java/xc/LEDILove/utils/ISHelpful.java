package xc.LEDILove.utils;

public class ISHelpful {
	public static byte getLrc(byte[] datas, int from, int length){
		byte lrc = 0;
		for(int i=from; i<from+length; i++){
			lrc ^= datas[i];
		}
		
		return lrc;
	}
	
	public static boolean checkLrc(byte[] datas, int from, int length){
		if(datas[datas.length-1] != getLrc(datas, from, length)){
			return false;
		}
		
		return true;
	}
}
