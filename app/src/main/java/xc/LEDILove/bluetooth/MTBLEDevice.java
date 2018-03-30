package xc.LEDILove.bluetooth;

import java.util.HashMap;
import java.util.Map;

public class MTBLEDevice {

	// 设备类型
	private int devicetype; // 设备类型
	public static final int MTBeacon1 = 1;
	public static final int MTBeacon2 = 2;
	public static final int MTBeacon3 = 5;
	public static final int IBeacon = 4;
	public static final int MTBLE = 3;
	private static Map<Integer, String> typeName = new HashMap<Integer, String>();
	static {
		typeName.put(new Integer(MTBeacon1), "MTBeacon1");
		typeName.put(new Integer(MTBeacon2), "MTBeacon2");
		typeName.put(new Integer(MTBLE), "馒头透传模块");
	}

	// BLE
	private byte[] scanRecord; // 广播数据
	private Map<Byte, byte[]> record_map;
	private String name; // 设备名称
	private String mac; // 设备mac
	private int current_rssi = 0; // 当前rssi
	private double current_distance = 0; // 当前实际距离

	// IBeacon
	private int major;
	private int minor;
	private int txpower;
	private String beacon_uuid;

	// MTBLE
	private static Map<Character, String> setLvName = new HashMap<Character, String>();
	static {
		setLvName.put(new Character('A'), "可连接");
		setLvName.put(new Character('B'), "密码连接");
		setLvName.put(new Character('C'), "临时部署");
		setLvName.put(new Character('D'), "永久部署");
	}

	// MTBeacon1
	private int battery; // 电池电量

	// MTBeacon2
	private char setlev; // 部署等级

	public MTBLEDevice(String name, String mac, byte[] scanRecord,
			int current_rssi) {
		reflashInf(name, mac, scanRecord, current_rssi);
	}

	public void reflashInf(String name, String mac, byte[] scanRecord,
			int current_rssi) {
		this.name = name;
		this.mac = mac;
		this.scanRecord = scanRecord;
		if (this.current_rssi == 0) {
			this.current_rssi = current_rssi;
		} else {
			this.current_rssi = (this.current_rssi + current_rssi) / 2;
		}

		this.record_map = analysisRecord(scanRecord); // 把广播数据分别提取出来

		this.devicetype = analysisType(record_map); // 分析设备类型

		if (this.devicetype == MTBeacon1) { // 是beacon
			analysisBeacon();
			analysisMTBeacon1();
			return;
		}

		if (this.devicetype == MTBeacon2) {
			analysisBeacon();
			analysisMTBeacon2();
			return;
		}
		
		if (this.devicetype == MTBeacon3) {
			analysisBeacon();
			analysisMTBeacon3();
			return;
		}
		
		if(this.devicetype == IBeacon){
			analysisBeacon();
			return;
		}

		if (this.devicetype == MTBLE) {
			analysisMTBLE();
			return;
		}
	}
	
	public void reflashInf(MTBLEDevice device) {
		reflashInf(device.getName(), device.getMac(), device.getScanRecord(), device.getCurrent_rssi());
	}

	// 防抖处理
	private int noscancount = 0;

	public int getNoscancount() {
		return noscancount;
	}

	public void setNoscancount(int noscancount) {
		this.noscancount = noscancount;
	}

	public int getDevicetype() {
		return devicetype;
	}

	public byte[] getScanRecord() {
		return scanRecord;
	}

	public Map<Byte, byte[]> getRecord_map() {
		return record_map;
	}

	public String getName() {
		return name;
	}

	public String getMac() {
		return mac;
	}

	public int getCurrent_rssi() {
		return current_rssi;
	}

	public double getCurrent_distance() {
		return current_distance;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getTxpower() {
		return txpower;
	}

	public String getBeacon_uuid() {
		return beacon_uuid;
	}

	public int getBattery() {
		return battery;
	}

	public char getSetlev() {
		return setlev;
	}

	public String getSetlevName() {
		return setLvName.get(new Character(setlev));
	}

	public String gettypeName() {
		return typeName.get(new Integer(devicetype));
	}

	// 解析数据
	private void analysisBeacon() {
		byte[] value = record_map.get(new Byte((byte) (0xff & 0xff)));
		if (value.length != 25) {
			return;
		}
		major = ((0xFF & value[20]) * 256 + (0xFF & value[21]));
		minor = ((0xFF & value[22]) * 256 + (0xFF & value[23]));
		txpower = value[24];

		beacon_uuid = "";
		for (int j = 4; j < 20; j++) { // uuid

			if ((j == 8) || (j == 10) || (j == 12) || (j == 14))
				beacon_uuid += '-';
			beacon_uuid += Helpful.MYConvertHexByteToString(value[j]);
		}
		beacon_uuid = beacon_uuid.toUpperCase();

		current_distance = CalculateDistance(current_rssi, txpower); // 计算距离
	}

	// 获取MTBeacon1的数据
	private boolean analysisMTBeacon1() {
		byte[] value = record_map.get(new Byte((byte) (0xAA & 0xFF))); // 查看是否为MTBeacon第一代

		if (value == null) {
			return false;
		}
		if (value.length != 2) {
			return false;
		}
		this.battery = value[1]; // 获取电量
		return true;
	}

	// 获取MTBeacon2的数据
	private boolean analysisMTBeacon2() {
		byte[] value = record_map.get(new Byte((byte) (0xAA & 0xFF))); // 查看是否为MTBeacon第一代

		if (value == null) {
			return false;
		}
		if (value.length != 3) {
			return false;
		}
		this.battery = value[1]; // 获取电量
		this.setlev = (char) value[2]; // 部署等级

		return true;
	}

	// 获取MTBeacon3的数据
	private boolean analysisMTBeacon3(){
		
		byte[] value = record_map.get(new Byte((byte) (0x06 & 0xFF)));
		if((value == null) || (value.length != 16)){
			return false;
		}
		
		this.battery = value[2];  // 电池电量
		this.setlev = (char) value[8]; // 部署等级
		
		return true;
	}
	
	// 获取MTBLE的数据
	private boolean analysisMTBLE() {
		byte[] value = record_map.get(new Byte((byte) (0x06 & 0xFF))); // 查看是否为MT产品
		if (value != null) {
			battery = value[2];
		}
		return true;
	}
	
	// 分析类型
	private static int analysisType(Map<Byte, byte[]> record_map) {

		byte[] value = record_map.get(new Byte((byte) (0xFF & 0xFF))); // 判断厂商数据
		if (value != null) {
			if ((value.length == 25)
					&& (value[0] == (byte) (0x4C & 0xFF)) // 判断是否是ibeacon格式
					&& (value[1] == (byte) (0x00 & 0xFF))
					&& (value[2] == (byte) (0x02 & 0xFF))) {
				value = record_map.get(new Byte((byte) (0xAA & 0xFF)));
				if (value != null) {
					if ((value.length == 2) && (value[0] == 'M')) {  // 第一代MTBeacon
						return MTBeacon1;
					}
					if ((value.length == 3) && (value[0] == 'M')) {   // 第二代MTBeacon
						return MTBeacon2;
					}
				}
//				return IBeacon;    // 返回普通ibeacon类型
			}
		}

		value = record_map.get(new Byte((byte) (0x06 & 0xFF))); // 查看是否为MT产品
		if (value != null) {
			byte lrc = 0;
			for (int i = 0; i < value.length; i++) {
				lrc += value[i];
			}
			if (lrc != 0) {
				return 0;
			}
			
			switch(value[1]){
			case 0x02: return MTBeacon3;
			default:break;
			}
			
			return MTBLE;
		}

		return 0;
	}

	// 解析数据
	private static Map<Byte, byte[]> analysisRecord(byte[] scanRecord) {
		byte[] values;
		Map<Byte, byte[]> record_map = new HashMap<Byte, byte[]>();
		for (int i = 0; i < scanRecord.length;) {

			if ((scanRecord[i] + i) >= scanRecord.length) { // 复制前先查看是否有这么长
				break;
			}
			if (scanRecord[i]<=0){
				break;
			}
			values = new byte[scanRecord[i] - 1];
			System.arraycopy(scanRecord, i + 2, values, 0, scanRecord[i] - 1);
			if (values != null) {
				record_map.put(new Byte(scanRecord[i + 1]), values);
			}

			i += (scanRecord[i] + 1);

			if ((i >= (scanRecord.length)) || (0x00 == scanRecord[i])) {
				break;
			}
		}

		return record_map;
	}

	// 计算距离
	private static double CalculateDistance(int rssi, int txpower) {
		double distance = 0;
		double ratio = rssi * 1.0 / txpower; // 计算距离
		if (ratio < 1.0) {
			distance = Math.pow(ratio, 10);
		} else {
			distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
		}

		return distance;
	}
}
