package xc.LEDILove.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.adapter.DeviceListAdapter;
import xc.LEDILove.app.MyApplication;
import xc.LEDILove.utils.CommonUtils;
import xc.LEDILove.view.PullRefreshListView;
import xc.LEDILove.view.PullToRefreshFrameLayout;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/***
 * 搜索界面
 *  点击事件包含 链接
 */
public class NewScanActivity extends Activity {

    private final static int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 10001;
    private PullToRefreshFrameLayout mRefreshLayout;
    private PullRefreshListView mListView;
    private DeviceListAdapter mAdapter;
    private TextView mTvTitle;
    private List<SearchResult> mDevices;
    private String mac;
    private String device_name;
    private ProgressDialog pd;
    private ImageView iv_getback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDevices = new ArrayList<>();
        mTvTitle = (TextView) findViewById(R.id.title);
        iv_getback = (ImageView) findViewById(R.id.iv_getback);
        iv_getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mRefreshLayout = (PullToRefreshFrameLayout) findViewById(R.id.pulllayout);
        mListView = mRefreshLayout.getPullToRefreshListView();
        mAdapter = new DeviceListAdapter(this, new DeviceListAdapter.DeviceListOnClickLisener() {
            @Override
            public void onClickItem(final String mac, String name) {
                if (pd == null) {
                    pd = ProgressDialog.show(NewScanActivity.this, "", "connecting",
                            true, false);
                    pd.setCancelable(true);
                    pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            MyApplication.getInstance().mClient.disconnect(mac);
                            CommonUtils.toast("Cancel");
                        }
                    });
                } else {
                    pd.show();
                }
                //点击进行链接和打开服务操作
                NewScanActivity.this.mac = mac;
                NewScanActivity.this.device_name = name;
                connectDevice();
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(new PullRefreshListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                searchDevice();
            }

        });

        MyApplication.getInstance().mClient.registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                if (null != pd) {
                    pd.dismiss();
                }
                if (openOrClosed) {
                    CommonUtils.toast("Bluetooth is open.");
                } else {
                    CommonUtils.toast("Bluetooth is close.");
                }
            }
        });

        if (!MyApplication.getInstance().mClient.isBluetoothOpened()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(enableBtIntent, 0);
        }
        checkBluetoothPermission();

    }

    /*
           校验蓝牙权限
          */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(NewScanActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NewScanActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                //具有权限
                searchDevice();
                ;
            }
        } else {
            //系统不高于6.0直接执行
            searchDevice();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchDevice();
    }

    private BleConnectOptions options = new BleConnectOptions.Builder()
            .setConnectRetry(3)//重试3次
            .setConnectTimeout(20000)//20秒超时
            .setServiceDiscoverRetry(3)//发现服务三次
            .setServiceDiscoverTimeout(10000)//10秒超时
            .build();

    /***
     * 链接蓝牙
     */
    private void connectDevice() {
        if (TextUtils.isEmpty(mac)) {
            CommonUtils.toast("mac is empty!");
            return;
        }
//        Intent intent = new Intent();
//                    intent.putExtra("mac", mac);
//                    intent.putExtra("name", device_name);
//                    setResult(RESULT_OK, intent);
//                    finish();
        Log.e("xxx", "connect mac =" + mac);
        MyApplication.getInstance().mClient.connect(mac, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                pd.dismiss();
                mListView.setVisibility(View.VISIBLE);
                if (code == REQUEST_SUCCESS) {
                    //连接成功，跳转编辑界面
                    Intent intent = new Intent();
                    intent.putExtra("mac", mac);
                    intent.putExtra("name", device_name);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    CommonUtils.toast("connect failed.");
                }
            }
        });
    }


    /***
     * 开始搜索
     */
    private void searchDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SearchRequest request = new SearchRequest.Builder()
                        .searchBluetoothLeDevice(5000, 2).build();
                MyApplication.getInstance().mClient.search(request, mSearchResponse);
            }
        }).start();

    }

    /***
     * 搜索回调
     */
    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.string_refreshing);
            mDevices.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);
            }
            if (mDevices.size() > 0) {
                mRefreshLayout.showState(AppConstants.LIST);
            }
        }

        @Override
        public void onSearchStopped() {
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.devices);
        }

        @Override
        public void onSearchCanceled() {
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.devices);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.getInstance().mClient.stopSearch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                searchDevice();
            } else {
                // 权限拒绝
                // 下面的方法最好写一个跳转，可以直接跳转到权限设置页面，方便用户
                Toast.makeText(this, "bluetooth permission granted!", Toast.LENGTH_LONG).show();
            }
        }
    }

}
