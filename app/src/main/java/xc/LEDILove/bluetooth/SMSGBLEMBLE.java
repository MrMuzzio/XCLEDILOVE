package xc.LEDILove.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by SF on 2017/12/5.
 */

@SuppressLint("NewApi")
public class SMSGBLEMBLE {
    private String TAG = SMSGBLEMBLE.class.getSimpleName();
    private Context context ;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private   BluetoothGatt mBluetoothGatt;

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    private BluetoothDevice device;
//    private static SMSGBLEMBLE single =null;
//    private SMSGBLEMBLE(){};
//    public static SMSGBLEMBLE getInstance(){
//        if (single==null){
//            single = new SMSGBLEMBLE();
//            return single;
//        }else {
//            return single;
//        }
//    }
    private static final int DELAY_TIME = 15; // 操作间要有至少15ms的间隔

    public  SMSGBLEMBLE(Context context, BluetoothManager mBluetoothManager,
                       BluetoothAdapter mBluetoothAdapter) {
        this.context = context;
        this.mBluetoothManager = mBluetoothManager;
        this.mBluetoothAdapter = mBluetoothAdapter;

        handl.sendEmptyMessageDelayed(SEND_DATAS, 40);
    }

    // 连接设备
    private String last_mac;
    private boolean connect_flag = false;
    private static final String DATA_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String TXD_CHARACT_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    private static final String RXD_CHARACT_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic txd_charact;
    private BluetoothGattCharacteristic rxd_charact;

    public void connect(String mac, int sectime, int reset_times) {
        Log.e(TAG,"connect>>000");
        if (!mBluetoothAdapter.isEnabled()) { // 没有打开蓝牙
            return ;
        }
//            disConnect();
        for (int i = 0; i < reset_times; i++) {
            initTimeFlag(WORK_onServicesDiscovered);
            Log.e(TAG,"connect>>开始连接");
            System.out.println("开始连接");
            if ((mBluetoothGatt != null) && mac.equals(last_mac)) {
//                    if (connect_flag == true) { // 当前已经连接好了
//                        return true;
//                    }
                System.out.println("重连");
                mBluetoothGatt.close();
//                    mBluetoothGatt.connect();
            }
//                else {
                System.out.println("新连接");
//                    disConnect(); // 新设备进行连接
                device = mBluetoothAdapter.getRemoteDevice(mac);
                if (device == null) {
                    System.out.println("device == null");
                    return ;
                }
                handl.sendEmptyMessageDelayed(CONNECT_TIMEOUT,sectime);
                mBluetoothGatt = device.connectGatt(context, false,
                        mGattCallback);
//                }

//                if (startTimeOut(sectime)) { // 连接超时
//                    Log.e(TAG,"connect>>连接超时");
//                    System.out.println("连接超时");
////                    if((i+1)==reset_times){
////                    }
//                    callback.onTimeOut(0);
////                    disConnect();
//                    continue;
//                }

//                connect_flag = true;

//                txd_charact = getCharact(DATA_SERVICE_UUID, TXD_CHARACT_UUID);
//                rxd_charact = getCharact(DATA_SERVICE_UUID, RXD_CHARACT_UUID);
//
//                if ((txd_charact == null) || (rxd_charact == null)) {
//                    // System.out.println("获取服务失败");
//                    return false;
//                }

//                Thread.sleep(100);
//                setNotifyACK(rxd_charact, 1000);
//               boolean isok =  enableData(true);
//                Log.e("使能数据接收",isok+"");
            last_mac = mac;
//                NewMsgActivity.isConnected = true;
//                NewMsgActivity.connected_MAC = mac;
        }
    }

    // 获取特征值
    public BluetoothGattCharacteristic getCharact(String service_uuid_str,
                                                  String charact_uuid_str) {
        if (!mBluetoothAdapter.isEnabled()) { // 没有打开蓝牙
            return null;
        }

        if (!isConnected()) {
            return null;
        }
        if(mBluetoothGatt==null){
            return null;
        }
        BluetoothGattService ble_service = mBluetoothGatt.getService(UUID
                .fromString(service_uuid_str));

        if (ble_service == null) { // 获取服务失败
            System.out.println("获取服务失败");
            return null;
        }

        BluetoothGattCharacteristic data_char = ble_service
                .getCharacteristic(UUID.fromString(charact_uuid_str));

        if (data_char == null) { // 获取特征值失败
            System.out.println("data_char == null");
            return null;
        }

        return data_char;
    }

    // 校验密码
    public boolean checkPwd(String pwd) {

        return true;
    }
    public boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
//                Log.e("清除缓存", "清除缓存");
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod(
                        "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(
                            localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                Log.e("清除缓存", "An exception occured while refreshing device");
            }
        }
        Log.e("refreshDeviceCache", "false" );
        return false;
    }
    // 断开连接
    public synchronized  boolean disConnect() {
        if (mBluetoothGatt != null) {
            System.out.println("断开连接");
            mBluetoothGatt.disconnect();
//            mBluetoothGatt.close();
//            mBluetoothGatt.close();
//            mBluetoothGatt = null;
//            connect_flag = false;
//            NewMsgActivity.isConnected = false;
            return true;
        }
        return false;
    }

    // 销毁连接
    public void close() {
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    // 查看连接状态
    public boolean isConnected() {
        return connect_flag;
    }

    // BLE回调操作
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);
//            System.out.println(newState);
//            Log.e("ConnectionStateChange->",newState+"");
            if (newState == BluetoothProfile.STATE_CONNECTED) { // 连接成功
                Log.e(TAG,"STATE_CONNECTED");
                System.out.println("STATE_CONNECTED");
                if (work_witch == WORK_onConnectionStateChange) {
                    work_ok_flag = true;
                }
                mBluetoothGatt.discoverServices();
                connect_flag = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 断开连接
                Log.e(TAG,"STATE_DISCONNECTED");
                if (refreshDeviceCache()){
                    mBluetoothGatt.close();
//                    mBluetoothGatt = null;
                }
                if (connect_flag) { // 如果外部已经主动调用了断开连接的话
                    connect_flag = false;
                    if (callback != null) {
                        callback.onDisconnect();
                    }
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("onServicesDiscovered");
                txd_charact = getCharact(DATA_SERVICE_UUID, TXD_CHARACT_UUID);
                rxd_charact = getCharact(DATA_SERVICE_UUID, RXD_CHARACT_UUID);

                if ((txd_charact == null) || (rxd_charact == null)) {
                    // System.out.println("获取服务失败");
                }else {
                    setNotifyACK(rxd_charact, 1000);
                }

//                Thread.sleep(100);

                if (work_witch == WORK_onServicesDiscovered) {
                    work_ok_flag = true;
                }
            } else {
                System.out.println("onServicesDiscovered fail-->" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (work_witch == WORK_onCharacteristicRead) {
                    work_ok_flag = true;
                }
            }
        }
        //接收消息回调接口
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data = characteristic
                    .getValue();
//            //也可以先打印出来看看
            String str = "";
            for (int i = 0; i < data.length; i++) {
                str += MYConvertHexByteToString(data[i]) + " ";
            }
            Log.e("收消息回调",str);
//            Toast.makeText(context,"回调",Toast.LENGTH_LONG).show();
//            if (work_witch != WORK_onCharacteristicChanged){
//                return;
//            }
//            Toast.makeText(context,"Changed->"+Helpful.MYBytearrayToString(characteristic.getValue()),Toast.LENGTH_LONG).show();

			Log.e("Changed->",new String(characteristic.getValue()));
            handl.removeMessages(RECIVE_TIMEOUT);
            handl.sendEmptyMessageDelayed(RECIVE_TIMEOUT, 400);
            callback.onDataReturn(data);
            Helpful.catByte(characteristic.getValue(), 0, reciveDatas,
                    reciveLength);
            reciveLength += characteristic.getValue().length;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                handl.sendEmptyMessage(SEND_DATAS);
				System.out.println("发送成功");
				callback.onWrited();
                if (work_witch == WORK_onCharacteristicWrite) {
                    work_ok_flag = true;
                }
            } else {
                System.out.println("write fail->" + status);
            }

            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            handl.removeMessages(CONNECT_TIMEOUT);
            callback.onNotification(status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG,"打开通知成功");
                if (work_witch == WORK_onDescriptorWrite) {
                    work_ok_flag = true;
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (work_witch == WORK_onReadRemoteRssi) {
                    work_ok_flag = true;
                }
                rssi_value = (rssi_value + rssi) / 2;
                // rssi_value = rssi;
            }
        }
    };
    public  String MYConvertHexByteToString(byte byteToConvert) {
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
    public List<BluetoothGattService> getServiceList() {
        if (!mBluetoothAdapter.isEnabled()) { // 没有打开蓝牙
            return null;
        }

        if (!isConnected()) {
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    // 设置可通知
    public boolean setNotifyACK(BluetoothGattCharacteristic data_char,
                                int milsec) {
        Log.e(TAG, "setNotifyACK: 打开通知……");
        if (exit_flag) {
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) { // 没有打开蓝牙
            return false;
        }

        if (data_char == null) {
            return false;
        }

        if (!isConnected()) {
            return false;
        }

        initTimeFlag(WORK_onDescriptorWrite);

        if ((0 != (data_char.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                || (0 != (data_char.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE))) { // 查看是否带有可通知属性
            mBluetoothGatt.setCharacteristicNotification(data_char, true);
            BluetoothGattDescriptor descriptor = data_char.getDescriptor(UUID
                    .fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (startTimeOut(milsec)) {
            System.out.println("startTimeOut");
            return false;
        }

        try { // 发送数据一定要有一些延迟
            Thread.sleep(DELAY_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    // 发送大量
    private List<byte[]> senddatas = new ArrayList<byte[]>();

    public boolean sendDatas(byte[] value) {
        Log.e("sendDatas", value.length+"");
        if (!isConnected()) {
            return false;
        }
        int pre_index = 0;

        while (true) {
            if ((value.length - pre_index) > 20) {
                senddatas.add(Helpful.subByte(value, pre_index, 20));
                pre_index += 20;
            } else {
                senddatas.add(Helpful.subByte(value, pre_index, value.length
                        - pre_index));
                break;
            }
        }
        return true;
    }

    // 发送命令
    public byte[] sendCmd(byte[] value, int milsec) {
        if (!sendDatas(value)) {
            System.out.println("发送命令失败");
            return null;
        }
        initTimeFlag(WORK_onCharacteristicChanged);
        if (startTimeOut(milsec)) {
            return null;
        }
        byte[] result = Helpful.subByte(reciveDatas, 0, reciveLength);
        reciveLength = 0;
        return result;
    }

    // 使能数据接收
    public boolean enableData(boolean enable) {
       return mBluetoothGatt.setCharacteristicNotification(rxd_charact, enable);
    }

    // 读取
    private int rssi_value;

    public int getRssi(int milsec) {
        if (!mBluetoothAdapter.isEnabled()) { // 没有打开蓝牙
            return 0;
        }
        initTimeFlag(WORK_onReadRemoteRssi);

        mBluetoothGatt.readRemoteRssi();

        if (startTimeOut(milsec)) {
            return 0;
        }

        return rssi_value;
    }

    // 回调方法
    private CallBack callback;

    public interface CallBack {
         void onDisconnect();
        void onNotification(int code);
        void onDataReturn(byte[] values);
        void onTimeOut(int type);
        void onWrited();
    }

    // 设置回调
    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    // 初始化定时变量
    private int work_witch = 0;
    private final int WORK_onConnectionStateChange = 1;
    private final int WORK_onServicesDiscovered = 2;
    private final int WORK_onCharacteristicRead = 4;
    private final int WORK_onCharacteristicChanged = 5;
    private final int WORK_onCharacteristicWrite = 6;
    private final int WORK_onDescriptorWrite = 7;
    private final int WORK_onReadRemoteRssi = 8;

    private void initTimeFlag(int work_index) {
        work_witch = work_index;
        timeout_flag = false;
        work_ok_flag = false;
    }

    // 开始计时
    private boolean startTimeOut(int minsec) {
        handl.sendEmptyMessageDelayed(HANDLE_TIMEOUT, minsec);
        while (!work_ok_flag) {
            if (exit_flag) {
                return true;
            }
            if (timeout_flag) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        handl.removeMessages(HANDLE_TIMEOUT);

        return false;
    }

    // 强制退出
    private boolean exit_flag = false; // 强制退出

    public void exit() {
        disConnect();
        handl.removeMessages(HANDLE_TIMEOUT);
        handl.removeMessages(SEND_DATAS);
        exit_flag = true;
    }

    // 事件处理
    private static final int HANDLE_TIMEOUT = 0;
    private static final int SEND_DATAS = 1;
    private static final int RECIVE_TIMEOUT = 2;
    private static final int CONNECT_TIMEOUT = 3;
    private boolean timeout_flag = false;
    private boolean work_ok_flag = false;
    private byte[] reciveDatas = new byte[2046];
    private int reciveLength = 0;
    @SuppressLint("HandlerLeak")
    private Handler handl = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what ==CONNECT_TIMEOUT ){
                callback.onTimeOut(0);
            }
            if (msg.what == HANDLE_TIMEOUT) {
                System.out.println("超时");
                timeout_flag = true;
                return;
            }

            if (msg.what == SEND_DATAS) {
                //每帧数据延时，下位机模块 理论上最高接收速度为20ms 实测25ms 以上100% 丢失  这里给50
                handl.sendEmptyMessageDelayed(SEND_DATAS, 50);

                if (!isConnected()) {
                    return;
                }
                if (0 == senddatas.size()) {
                    return;
                }
                txd_charact.setValue(senddatas.get(0)); // 发送掉这个数据
                mBluetoothGatt.writeCharacteristic(txd_charact);
                senddatas.remove(0);
            }

            if (msg.what == RECIVE_TIMEOUT) {
//				if (callback == null) {
//					return;
//				}

                if (work_witch == WORK_onCharacteristicChanged) {
                    work_ok_flag = true;
                }
//				callback.onReviceDatas(Helpful.subByte(reciveDatas, 0,
//						reciveLength));

                return;
            }

        }
    };

}
