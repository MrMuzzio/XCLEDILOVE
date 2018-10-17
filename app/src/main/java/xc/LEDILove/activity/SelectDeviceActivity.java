package xc.LEDILove.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.bluetooth.MTBLEDevice;
import xc.LEDILove.bluetooth.MTBLEManager;
import xc.LEDILove.bluetooth.SMSGBLEMBLE;
import xc.LEDILove.bluetooth.StaticDatas;


public class SelectDeviceActivity extends BaseActivity {
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scrollToFinishActivity();//左滑退出activity
    }

    private final static String TAG = "SelectDeviceActivity";

    private MTBLEManager mMTBLEManager;
    private StaticDatas mStaticDatas;
    private Handler handl;
    private MTBLEDevice select_device;
//    public SMSGBLEMBLE mBle;
    private ImageView iv_getback;

    //
    private BAdapter mAdapter;
//    private SelectParams mDataBean;
    private XRecyclerView rv_device_list;
    private LinearLayout ll_back;
    private TextView scancount_txt;
    final int requestPermission = 100;

//    @PermissionYes(requestPermission)
//    private void getPermissionYes(List<String> grantedPermissions) {
//        init();
//    }
//
//    @PermissionNo(requestPermission)
//    private void getPermissionNo(List<String> deniedPermissions) {
//        // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
//        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
//            // 第一种：用默认的提示语。
//            AndPermission.defaultSettingDialog(this, 101).show();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectdevice);
        rv_device_list = (XRecyclerView) findViewById(R.id.rv_device_list);
        ll_back = (LinearLayout)findViewById(R.id.ll_back);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        scancount_txt= (TextView)findViewById(R.id.scancount_txt);
        scancount_txt.setText(getString(R.string.devices));
        askPermission();
        init();

    }
    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            AndPermission.with(this)
                    .requestCode(100)
                    .permission(
                            // 多个权限，以数组的形式传入。
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.READ_EXTERNAL_STORAGE
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
//                            isAllow = true;
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
//                            isAllow = false;
                        }
                    })
                    .start();
            // 没有权限，申请权限。
        }else{
            // 有权限了，去放肆吧。
//            isAllow =true;
        }
    }
    private void init() {
//        try {
//
//        } catch (Exception e) {
//            Log.d(TAG, "onCreate->" + e.toString());
//        }
//        if (SMSGBLEMBLE.mBluetoothGatt!=null){
//            Log.e(TAG, "init: mBluetoothGatt");
//            SMSGBLEMBLE.mBluetoothGatt.disconnect();
//            SMSGBLEMBLE.mBluetoothGatt =null;
//        }
        mMTBLEManager = MTBLEManager.getInstance();
        mStaticDatas = StaticDatas.getInstance();
        mMTBLEManager.init(this);

//        mBle = new SMSGBLEMBLE(getApplicationContext(),
//                mMTBLEManager.mBluetoothManager,
//                mMTBLEManager.mBluetoothAdapter);

//        mBle.setCallback(bleCallback);
        handl = new Handler();

        mStaticDatas.scandevice_list = new ArrayList<MTBLEDevice>();

        if (!mMTBLEManager.isEnable()) {
            startActivityForResult(new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
        }
        initView();
    }


    // 连接线程
    private ProgressDialog pd;

    private boolean isChoose =false;
    private void initView() {

//        mDataBean = (SelectParams) getIntent().getSerializableExtra("DataBean");
        mAdapter = new BAdapter(this, mStaticDatas.scandevice_list);
        mAdapter.setbAdapterInterface(new BAdapter.BAdapterInterface() {
            @Override
            public void onClick(MTBLEDevice device) {
                select_device = device;
                Intent intent = new Intent();
                intent.putExtra("MAC",select_device.getMac());
                intent.putExtra("NAME",select_device.getName());
                SelectDeviceActivity.this.setResult(RESULT_OK,intent);
                stopScan();
                isChoose = true;
                finish();
//                new ConnectThread().start();
            }
        });
        rv_device_list.setLayoutManager(new LinearLayoutManager(this));
        rv_device_list.setAdapter(mAdapter);
        rv_device_list.setLoadingMoreEnabled(false);
        rv_device_list.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                //手机蓝牙多次关闭-打开  存在ScanCallback方法不执行的BUG 就会出现设备列表无数据
                //解决：在刷新时 实际只清除 list中的数据 不调用系统蓝牙的stop start 而因为从OnResume 开始一直在扫描
                //无需担心 扫不到设备  只是存在一定的延迟
                if (!isFresh){
                    isFresh = true;
                    mStaticDatas.scandevice_list.clear();
                    mAdapter.notifyDataSetChanged();
                    startScan();
                }
                Log.e("onRefresh","onRefresh");
                mStaticDatas.scandevice_list.clear();
                mAdapter.notifyDataSetChanged();
//                stopScan();
                scaning = true;
                handl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rv_device_list.refreshComplete();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore() {

            }
        });
        rv_device_list.refresh();
    }


    // 开始扫描
    private MTBLEManager.MTScanCallback lescancallback = new MTBLEManager.MTScanCallback() {

        @Override
        public void onScan(final MTBLEDevice device) {
            Log.d("onScan", device.getMac() );
            Log.d("onScan", mStaticDatas.scandevice_list.size()+"" );
            for (final MTBLEDevice mdevice : mStaticDatas.scandevice_list) { // 将设备储存进扫描列表
                if (mdevice.getMac().equals(device.getMac())) {
                    Log.d("same>>>", "same" );
                    handl.post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                mdevice.reflashInf(device);
                                mdevice.setNoscancount(0); // 清空没扫描到统计
                                mAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.d(TAG, "list_adapter.notifyDataSetChanged->" + e.toString());

                            }
                        }
                    });

                    continue;
                }
            }

                handl.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!getIsContain(device)) {
                                mStaticDatas.scandevice_list.add(device);
                                Log.d("add", device.getMac());
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "notifyDataSetChanged2->" + e.toString());
                        }
                    }
                    });
            if (mStaticDatas.scandevice_list.size()==0){
                mStaticDatas.scandevice_list.add(device);
            }
        }
    };

    private boolean getIsContain(MTBLEDevice device) {
        for (MTBLEDevice mtbleDevice:mStaticDatas.scandevice_list){
            if (mtbleDevice.getMac().equals(device.getMac())){
                return true;
            }
        }
        return false;
    }

    private boolean scaning = false;
    private boolean isFresh = false;
    private void startScan() {
        try {
            if (!mMTBLEManager.isEnable()) {
                scaning = false;
                return;
            }

            if (scaning) {
                return;
            }
            scaning = true;
            mAdapter.notifyDataSetChanged();
            mMTBLEManager.startScan(lescancallback);
            handl.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "startScan->" + e.toString());
        }
    }

    // 停止扫描
    private void stopScan() {
        try {
            if (!mMTBLEManager.isEnable()) {
                scaning = false;
                return;
            }

            if (!scaning) {
                return;
            }
            scaning = false;
            mMTBLEManager.stopScan();
        } catch (Exception e) {
            Log.d(TAG, "stopScan->" + e.toString());
        }
    }

//    public static void startActivity(Context ctx, SelectParams dataBean) {
//        ctx.startActivity(new Intent().setClass(ctx, SelectDeviceActivity.class).putExtra("DataBean", dataBean));
//    }


    @Override
    public void onResume() {
        super.onResume();
        startScan();
        isFresh = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (!isChoose){
//            Intent intent = new Intent();
//            intent.putExtra("MAC","");
//            intent.putExtra("NAME","");
//            SelectDeviceActivity.this.setResult(10010,intent);
//        }
        mStaticDatas.scandevice_list.clear();
    }

    public static class BAdapter extends RecyclerView.Adapter {

        Context mContext;
        List mList;

        public interface BAdapterInterface {
            void onClick(MTBLEDevice device);
        }

        private BAdapterInterface bAdapterInterface;

        public BAdapterInterface getbAdapterInterface() {
            return bAdapterInterface;
        }

        public void setbAdapterInterface(BAdapterInterface bAdapterInterface) {
            this.bAdapterInterface = bAdapterInterface;
        }

        public BAdapter(Context ctx, List l) {
            mContext = ctx;
            mList = l;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_bluetooth_parameter, parent, false);
//            View convertView = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ViewHolder vh = (ViewHolder) holder;
            final MTBLEDevice device = (MTBLEDevice) mList.get(position);

            vh.tvName.setText(device.getName());
            vh.tvRssi.setText("Rssi: " + device.getCurrent_rssi()
                    + "");
            vh.tvMac.setText("Mac: " + device.getMac());

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "onClick: >>>"+position);
                    if (bAdapterInterface != null)
                        bAdapterInterface.onClick(device);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mList == null)
                return 0;
            else
                return mList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName;
            public TextView tvRssi;
            public TextView tvMac;

            ViewHolder(View view) {
                super(view);
//                tvName = (TextView) view.findViewById(R.id.name);
//                tvRssi = (TextView) view.findViewById(R.id.rssi);
//                tvMac = (TextView) view.findViewById(R.id.mac);
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvRssi = (TextView) view.findViewById(R.id.tvRssi);
                tvMac = (TextView) view.findViewById(R.id.tvMac);
            }
        }
    }
}
