package xc.LEDILove.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.util.List;

/**
 * Created by SF on 2017/12/5.
 */

public class MTBLEManager {

    private static MTBLEManager mMTBLEManager;
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;

    private Scan5_0 mScan5_0;
    private Scan4_3 mScan4_3;

    /*
     * 初始化
     */
    public void init(Context context) {
        mBluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT < 21) {
            mScan4_3 = new Scan4_3(this.mBluetoothAdapter);
        } else {
            mScan5_0 = new Scan5_0(this.mBluetoothAdapter);
        }
    }

    /*
     * 是否可以工作
     */
    public boolean canWork() {
        if ((mBluetoothManager == null) || (null == mBluetoothAdapter)) {
            return false;
        }
        return true;
    }

    /*
     * 强行设置蓝牙开关
     */
    public void setEnable(boolean flag) {
        if (flag) {
            mBluetoothAdapter.enable();
        } else {
            mBluetoothAdapter.disable();
        }
    }

    // 获取开关状态
    public boolean isEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    // 获取自身mac
    public String getSelfMac() {
        if (mBluetoothAdapter == null) {
            return null;
        }
        return mBluetoothAdapter.getAddress();
    }

    /*
     * 扫描结果接口
     */
    public interface ScanCallback {
        public void onScan(MTBLEDevice device);
    }

    /*
     * 扫描结果回调
     */
    private BluetoothAdapter.LeScanCallback lescancallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            if (scanCallback == null) {
                return;
            }

            final MTBLEDevice mMTBLEDevice = new MTBLEDevice(device.getName(),
                    device.getAddress(), scanRecord, rssi);
            scanCallback.onScan(mMTBLEDevice);

            return;
        }
    };


    /*
     * 扫描蓝牙
     */
    private MTScanCallback scanCallback;
    private boolean scan_flag = false;

    public boolean startScan(MTScanCallback scanCallback) {

        if (!mMTBLEManager.isEnable()) {
            scan_flag = false;
            return false;
        }

        if (scan_flag) { // 如果正在扫描
            stopScan();
        }

        scan_flag = true;
        this.scanCallback = scanCallback;

        __startScan();

//		mBluetoothAdapter.startLeScan(lescancallback);

        // UUID.fromString("");
        // mBluetoothAdapter.startLeScan(new
        // UUID[]{UUID.fromString("0AFF6461-7265-706D-6574-8EDE6F4C99B4")},
        // lescancallback);
        // mBluetoothAdapter.startLeScan(new
        // UUID[]{UUID.fromString("F000F2F0-0451-4000-B000-000000000000")},
        // lescancallback);

        return true;
    }

    private boolean __startScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mScan4_3.startScan(scanCallback);
        } else {
            mScan5_0.startScan(scanCallback);
        }

        return true;
    }

    /*
     * 停止扫描
     */
    public boolean stopScan() {

        if (!mMTBLEManager.isEnable()) {
            scan_flag = false;
            return false;
        }

        if (scan_flag) {
            __stopScan();
        }
        scan_flag = false;

        return true;
    }

    private boolean __stopScan() {
        if (Build.VERSION.SDK_INT < 21) {
            System.out.println("mScan4_3.stopScan");
            mScan4_3.stopScan();
        } else {
            mScan5_0.stopScan();
        }

        return true;
    }

    /*
     * 查看是否正在扫描
     */
    public boolean isScaning() {
        return scan_flag;
    }

    /*
     * 清空缓存, conected后调用，再discoverservices
     */
//    public boolean refreshGattStructure(BluetoothGatt gatt) {
//        BluetoothGatt localGatt = gatt;
//        try {
//            Method localMethod = localGatt.getClass().getMethod("refresh",
//                    new Class[0]);
//            if (localMethod != null) {
//                boolean result = ((Boolean) localMethod.invoke(localGatt,
//                        new Class[0])).booleanValue();
//                return result;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /*
     * 获取静态对象
     */
    public static MTBLEManager getInstance() {
        if (mMTBLEManager == null) {
            mMTBLEManager = new MTBLEManager();
        }
        return mMTBLEManager;
    }

    // 扫描回调方法
    public interface MTScanCallback {
        public void onScan(MTBLEDevice device);
    }

    // 安卓5.0+系统扫描方式
    private Handler handl = new Handler();
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class Scan5_0 {
        private BluetoothAdapter mBluetoothAdapter;
        private MTScanCallback scanCallback;

        public Scan5_0(BluetoothAdapter mBluetoothAdapter) {
            this.mBluetoothAdapter = mBluetoothAdapter;
        }

        // 5.0+系统扫描回调方法
        private android.bluetooth.le.ScanCallback mScanCallback = new android.bluetooth.le.ScanCallback() {

            @Override
            public void onScanResult(final int callbackType, final ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result == null) {
                    return;
                }
                handl.post(new Runnable() {

                    @Override
                    public void run() {
                        scanCallback.onScan(new MTBLEDevice(result.getDevice().getName(), result.getDevice().getAddress(),
                                result.getScanRecord().getBytes(), result.getRssi()));
                    }
                });
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }

        };

        public boolean startScan(MTScanCallback scanCallback) {
            System.out.println("5.0 scan");

            BluetoothLeScanner scaner = mBluetoothAdapter.getBluetoothLeScanner();
            if(scaner == null){
                return false;
            }

            this.scanCallback = scanCallback;

            scaner.startScan(mScanCallback);

            return true;
        }

        public boolean stopScan() {

            if(!isEnable()){
                return true;
            }

            BluetoothLeScanner scaner = mBluetoothAdapter.getBluetoothLeScanner();

            if(scaner == null){
                return true;
            }

            scaner.stopScan(mScanCallback);

            return true;
        }
    }

    // 安卓4.3+系统扫描方式
    private class Scan4_3 {
        private BluetoothAdapter mBluetoothAdapter;
        private MTScanCallback scanCallback;

        // 4.3-5.0系统扫描回调方法
        private BluetoothAdapter.LeScanCallback lescancallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                handl.post(new Runnable() {

                    @Override
                    public void run() {
                        scanCallback.onScan(new MTBLEDevice(device.getName(), device.getAddress(), scanRecord, rssi));
                    }
                });

                return;
            }
        };

        public Scan4_3(BluetoothAdapter mBluetoothAdapter) {
            this.mBluetoothAdapter = mBluetoothAdapter;
        }

        public boolean startScan(MTScanCallback scanCallback) {
            System.out.println("4.3 scan");
            this.scanCallback = scanCallback;

            this.mBluetoothAdapter.startLeScan(lescancallback);
            return true;
        }

        public boolean stopScan() {
//				this.scanCallback = null;
            this.mBluetoothAdapter.stopLeScan(lescancallback);
            return true;
        }
    }


}
