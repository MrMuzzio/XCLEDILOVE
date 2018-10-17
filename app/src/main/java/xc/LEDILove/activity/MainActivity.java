package xc.LEDILove.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xc.LEDILove.Bean.SendBean;
import xc.LEDILove.R;
import xc.LEDILove.adapter.SelectorViewPagerAdapter;
import xc.LEDILove.bluetooth.CommandHelper;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.font.CmdConts;
import xc.LEDILove.fragment.ColorSelectFragment;
import xc.LEDILove.fragment.ControllerFragment;
import xc.LEDILove.fragment.FontSelectFragment;
import xc.LEDILove.fragment.GalleryFragment;
import xc.LEDILove.fragment.GifFragment;
import xc.LEDILove.fragment.ImageFragment;
import xc.LEDILove.fragment.MusicBuiltinFragment;
import xc.LEDILove.fragment.MusicFragment;
import xc.LEDILove.fragment.MusicLocalFragment;
import xc.LEDILove.fragment.QuicklySendFragment;
import xc.LEDILove.fragment.TextFragment;
import xc.LEDILove.service.BleConnectService;
import xc.LEDILove.utils.AppVersionUpdate;
import xc.LEDILove.utils.CommonUtils;
import xc.LEDILove.utils.SPUtils;
import xc.LEDILove.view.NoScrollViewPager;
import xc.LEDILove.widget.HintDialog;
import xc.LEDILove.widget.LoadingDialog;

public class MainActivity extends FragmentActivity implements MusicLocalFragment.OnFragmentInteractionListener,
        MusicBuiltinFragment.OnFragmentInteractionListener,FontSelectFragment.OnFragmentInteractionListener,
        ColorSelectFragment.OnFragmentInteractionListener {
    private final String TAG = MainActivity.class.getSimpleName();
    //APP版本信息获取地址
    private final String service_url = "http://www.xn--led-f00fr20c.com/appVersion/getNewAppVersion?appName=LEDILOVE";
    private NoScrollViewPager vp_main_content;
    private RadioGroup rg_main_item_select;
    private RadioButton rb_gallery;
    private RadioButton rb_palette;
    private RadioButton rb_music;
    private RadioButton rb_edit_quick;
    private List<Fragment> fragments;
    //    private GifFragment gifFragment;
    private GalleryFragment galleryFragment;
    private ImageFragment imageFragment;
    private MusicFragment musicFragment;
    private TextFragment textFragment;
    private ControllerFragment controllerFragment;
    private QuicklySendFragment quicklySendFragment;
    private ImageView iv_more;
    private ImageButton iv_main_send;
    private LinearLayout ll_connect_state;
    private TextView tv_device_name;
    private LinearLayout ll_mode_send;
    private SelectorViewPagerAdapter viewPagerAdapter;
    //开关
    private Switch aSwitch;
    private FrameLayout fragment_main;

    private boolean isConnectIntime = false;//是否刚连接
    public static String connected_MAC = "";//当前连接设备的mac地址
    public static  String connected_name = "";
    private  final  int CMD_CONNTCTING =1001;
    private  final  int DISMISS_DIALOG =1002;
    private  final  int CMD_SENDING =1003;
    private  final  int CMD_TIMEOUT =1004;//操作超时
    private  final  int CMD_TIMEOUT_RECEIVE =1005;//接收反馈超时
    private  final  int CONNECTED_FAIL =1006;//连接失败-打开通知失败
    private  final  int ACTIVITY_LAUNCHER =1007;//连接失败-打开通知失败
    private  final  int REFRESH_VP =1008;//接收下位机返回数据后刷新界面
    private  final  int CONNECT_AUTO =1009;//自动连接上次连接过的设备 每2S连接一次
    private  final  int SEND_SEQUENCE =1010;//发送数据队列
    private int sendCmdType = -1;//发送数据类型
    private boolean power = false;//开关机命令
    private boolean isNeedSendData = false;
    //    private ProgressDialog pd;
    private LoadingDialog pd;
    private HintDialog hintDialog;
    private ServiceConnection serviceConnection;
    private BleConnectService.Mybinder serviceBinder;
    private boolean isStartFirst = true;
    private boolean isNeedShowDialog = true;
    private String turnOnOrOffCmd = "";
    private byte[] sendcmd;
    private List<SendBean> send_files;
    private int send_index = 0;
    //发送的数据大小
    private int hasSendData = 0;
    //发送的序号头00 01  02
    private int header = 0;
    //主页面LED尺寸记录，初始值与全局变量相同
    private int H = 32;
    private int V = 32;
    /**
     * 多彩标志位  是否支持多彩
     * */
//    private boolean isSupportMarFullColor = true;
    /**
     * 版本管理工具，
     * */
    private AppVersionUpdate versionUpdate;
    private  Call mcall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.activity_launch_enter,0);
        findView();//
        initData();
        interfaceListener();
        initViewAndListener();
        initService();
        checkVersionToUpData();
    }
    private void interfaceListener() {
        textFragment.setTextFragmentCallback(new TextFragment.TextFragmentCallback() {
            @Override
            public void onSendRequire(byte[] cmd) {
                if (checkConnectStatus()){
                    sendTextData(true,true,cmd);
                }
            }
        });
        imageFragment.setRealTimePreviewListener(new ImageFragment.RealTimePreviewListener() {
            @Override
            public void onStartPreview() {
                //发送进入预览模式命令
                serviceBinder.sendData("LOL".getBytes(),false);
            }

            @Override
            public void onStopPreview() {
                //发送退出预览模式命令
                serviceBinder.sendData("DNF".getBytes(),false);
            }

            @Override
            public void onStepRefresh() {
                if (!imageFragment.getPreviewStatus()){
                    return;
                }
                //检查链接状态
                if (checkConnectStatus()) {
                    //发送画板数据
                    sendCmdType=CommandHelper.dataType_data;
                    sendPaletteData(false, false, "",1,0);

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
    }

    @Override
    protected void onPause() {
        if (serviceBinder!=null){
            serviceBinder.removeServiceListener();
        }
        MEHandler.removeMessages(CONNECT_AUTO);
        super.onPause();
    }
    private long time_backPress=0;
    @Override
    public void onBackPressed() {
        long inedx = System.currentTimeMillis();
        if (inedx-time_backPress<2000){
            finish();
        }else {
            Toast.makeText(this,getString(R.string.toast_exit_app),Toast.LENGTH_SHORT).show();
            time_backPress=inedx;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
//    @Override
//    public void onBackPressed() {
////        if (isInMain){
////            Intent intent= new Intent(Intent.ACTION_MAIN);
////            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            intent.addCategory(Intent.CATEGORY_HOME);
////            startActivity(intent);
////        }else {
////            llContent.setVisibility(View.GONE);
////            scrollView.setVisibility(View.VISIBLE);
////            include.setVisibility(View.VISIBLE);
////            isInMain = true;
////        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        viewPagerAdapter.getItem(fragment_position).onActivityResult(requestCode, resultCode, data);
        sendCmdType = CommandHelper.dataType_data;
        if (requestCode ==1004&&resultCode == RESULT_OK) {
            if (data.getExtras().getString("MAC").equals("")){

            }else {
                StaticDatas.isConnectAuto =false;
                isNeedShowDialog = true;
                connected_MAC = data.getExtras().getString("MAC");
                connected_name = data.getExtras().getString("NAME");
                MEHandler.sendEmptyMessageDelayed(CMD_CONNTCTING,500);

            }
        }else {
            connected_MAC="";
            connected_name="";
            saveDevice();
            StaticDatas.isConnectAuto =true;
            MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,5000);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
//        refreshViewByColorModel();
        Log.e(TAG, "onResume: ");
        checkAndResetConnectStatus();
        checkAndResetFragmentByLEDSize();
    }

    private void checkAndResetConnectStatus() {
        if (serviceBinder!=null&&serviceListener!=null){//检查蓝牙是否已经连接，并做相应的初始化处理
            serviceBinder.setServiceListener(serviceListener);
            if(serviceBinder.getConnectedStatus()){//
//                isConnectIntime = true;
                switch_state_iscontrolauto=true;
                connected_name =serviceBinder.getConnectedMacName();
//                    mConnected = true;
                MEHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //设置打开开关,发送开机命令
//                        switch_state_iscontrolauto = true;
                        aSwitch.setChecked(true);
                        aSwitch.setVisibility(View.VISIBLE);

                        Log.e(TAG,"connected_name>>>>"+connected_name);
                        tv_device_name.setText(getTenChar(connected_name));
                    }

                });
            }else {
                MEHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                    CommonUtils.toast(getResources().getString(R.string.connectionCasBeenDisconnected));
//                        mConnected = false;
                        aSwitch.setVisibility(View.GONE);
                        hasSendData=0;
                        sendcmd=null;
                        switch_state_iscontrolauto = true;
                        aSwitch.setChecked(false);
                        isConnectIntime = false;
                        tv_device_name.setText(getResources().getString(R.string.connectstate));
                        if (pd!=null){
                            pd.dismiss();
                        }
                    }
                });
                if (StaticDatas.isConnectAuto){
                    MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,500);
                }
            }
        }
    }

    private void checkAndResetFragmentByLEDSize() {//根据当前LED高度设置显示模式
        Log.e(TAG, "checkAndResetFragmentByLEDSize: LEDHight>>"+StaticDatas.LEDHight+"_"+StaticDatas.LEDWidth);
        if (H!=StaticDatas.LEDHight||V!=StaticDatas.LEDWidth){
            switch (StaticDatas.LEDHight){
                case 12:
                    enterSingleFunction();
                    break;
                case 16:
                    enterSingleFunction();
                    break;
                case 32:
                    enterFullFunction();
                    break;
                case 40:
                    enterFullFunction();
                    break;
            }
        }
        H = StaticDatas.LEDHight;
        V = StaticDatas.LEDWidth;
    }

    private void enterSingleFunction() {
        checkRadioButton(4);
        ll_mode_send.setVisibility(View.GONE);
        rg_main_item_select.setVisibility(View.GONE);
        textFragment.refreshBySize();
    }

    private void enterFullFunction() {
        checkRadioButton(0);
        ll_mode_send.setVisibility(View.VISIBLE);
        rg_main_item_select.setVisibility(View.VISIBLE);
        //根据连接返回的LED尺寸 重新设置图库和画板布局
        resetGalleryAndPaletteLayout();
    }

    private void resetGalleryAndPaletteLayout() {
        if (galleryFragment!=null){
            galleryFragment.resetLayout();
        }
        if (imageFragment!=null){
            imageFragment.resetLayout();
        }
        if(quicklySendFragment!=null){
            quicklySendFragment.resetLayout();
        }
    }

    @Override
    protected void onDestroy() {
        serviceBinder.removeServiceListener();
        unbindService(serviceConnection);
        if (pd!=null){
            pd.dismiss();
            pd=null;
        }
        MEHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private String verName = "";
    private void checkVersionToUpData() {

        //发送网络请求
        OkHttpClient client = new OkHttpClient();

        Request.Builder builder = new Request.Builder().url(service_url);
        builder.method("GET",null);
        Request request = builder.build();
        mcall = client.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"fail",Toast.LENGTH_SHORT);
                    }
                });
//                Log.i("onFailure", e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i("onResponse", "cache---" + str);
                } else {
                    String jsonData = response.body().string();
                    int code =  versionUpdate.parseJsonForCode(jsonData);
                    if (code==-1){
                        return;
                    }
                    String str = response.networkResponse().toString();
                    Log.i("onResponse", "network---" + str);
                    //服务器版本大于已安装版本
                    if (code>versionUpdate.getVersionCode(getApplicationContext())){
                        verName = versionUpdate.parseJsonForName(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultDialog(getString(R.string.app_update)+"\n"+"version:"+verName,true);
                            }
                        });
                    }else {

                    }
                }
            }
        });
    }

    private void initService() {

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.e(TAG, "onServiceConnected: >>");
                serviceBinder = (BleConnectService.Mybinder) iBinder;
                serviceBinder.setServiceListener(serviceListener);
                if (isStartFirst){
                    MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,500);
//                    connectLastDevice();
                    isStartFirst = false;
                }
                checkAndResetConnectStatus();
                MEHandler.sendEmptyMessageDelayed(REFRESH_VP,1000);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent  = new Intent();
        intent.setClass(this,BleConnectService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }
    private int fragment_position = 0;
    private byte[] waitSendData;
    private void initViewAndListener() {
//        vp_main_content.setNoScroll(false);
//        vp_main_content.setAdapter(viewPagerAdapter);
        iv_main_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果无连接 进入扫描页面
                if (checkConnectStatus()) {
                    switch (fragment_position){
                        case 0://画板
                            sendCmdType=CommandHelper.dataType_data;
                            sendPaletteData(true,true,"",1,0);
                            break;
                        case 1://图库

                            break;
                        case 2://音乐
                            musicFragment.openSync();
                            break;
                        case 3://快速发送
//                        if ((sendcmd = textFragment.getEdit_byte())!=null){
//                            sendCmdType=CommandHelper.dataType_data;
//                            isNeedShowDialog=true;
//                            MEHandler.sendEmptyMessage(CMD_SENDING);
//                        }
                            send_files=quicklySendFragment.getSend_files();
                            send_index = 0;
                            if (send_files.size()>0){
                                sendCmdType  = CommandHelper.dataType_list;
                                sendPaletteData(true,true,send_files.get(send_index).getPath(),send_files.get(send_index).getMsg_number(),0);
                            }
                            break;
                        case 4://文字输入
                            break;
                    }
                }
            }
        });
        rg_main_item_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged: id"+i );
                setViewPager(i);
            }
        });
        checkRadioButton(0);
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showPopupWindow(iv_more);
                Intent mintent = new Intent();
                mintent.setClass(MainActivity.this,OtherMessageActivity.class);
                startActivity(mintent);
            }
        });
        ll_connect_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticDatas.isConnectAuto = false;
                Log.e(TAG,"ll_connect_state>>onClick");
                isNeedSendData = false;
                if (serviceBinder.getConnectedStatus()){
                    serviceBinder.disConnected();
                }
                MEHandler.removeMessages(CONNECT_AUTO);
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivityForResult(intent, 1004);
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("isChecked",isChecked+"");
                switch_state = isChecked;
                if (switch_state_iscontrolauto){
                    switch_state_iscontrolauto = false;
                    return;
                }
                if (isChecked) {
                    turnOnOrOffCmd = CmdConts.ON_LED;
                } else {
                    turnOnOrOffCmd = CmdConts.OFF_LED;
                }
                sendCmdType = CommandHelper.dataType_power;
                if (serviceBinder.getConnectedStatus()) {
                    MEHandler.sendEmptyMessage(CMD_SENDING);
                } else {
                    Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                }
            }
        });
    }

    private boolean checkConnectStatus() {
        if (!serviceBinder.getConnectedStatus()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,getResources().getString(R.string.connect_device),Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivityForResult(intent, 1004);
            return false;
            //有连接 但处于关机状态
        }
        else if (!switch_state){
            showYCDialog(getString(R.string.pleasepoweron));
            return false;
        }
        else {
            return true;
        }
    }
    private  void sendTextData(boolean iswaitretrun,boolean isshowdialog,byte[] cmd){
        if (imageFragment.getPreViewStatus()){
            imageFragment.closePreView();
        }
        if (musicFragment.getSyncStatus()){
            musicFragment.closeSync();
        }
        if ((sendcmd=cmd)!=null){
            sendCmdType=CommandHelper.dataType_data;
            if (isshowdialog&&iswaitretrun){
                isNeedShowDialog = isshowdialog;
                MEHandler.sendEmptyMessage(CMD_SENDING);
            }else {
                serviceBinder.sendData(sendcmd,iswaitretrun);
            }
        }
    }

    /**
     * @param iswaitretrun  是否等待返回值 实时模式下不需要
     * @param isshowdialog  是否显示发送框  实时模式下不需要
     * @param path  发送图片时的文件路径
     * @param number   发送图片的信息号
     */
    private void sendPaletteData(boolean iswaitretrun,boolean isshowdialog,String path,int number,int type) {//发送画板数据
        if (path!=null&&!path.equals("")){//发送图片 此时需要检查 是否处在 实时预览模式下 如果是 需要先退出该模式
            if (imageFragment.getPreViewStatus()){
                imageFragment.closePreView();
            }
        }
        if (musicFragment.getSyncStatus()){
            musicFragment.closeSync();
        }
        if ((sendcmd = imageFragment.getDiy_byte(path,number,type))!=null){
//            sendCmdType=CommandHelper.dataType_data;
//            isNeedShowDialog=b;
//            MEHandler.sendEmptyMessage(CMD_SENDING);

            if (isshowdialog&&iswaitretrun){
                isNeedShowDialog = isshowdialog;
                MEHandler.sendEmptyMessage(CMD_SENDING);
            }else {
                serviceBinder.sendData(sendcmd,iswaitretrun);
            }
        }
    }

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private void replaceFragment(int i) {
        if (fragmentManager==null){
            fragmentManager = getSupportFragmentManager();
        }
        transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        transaction.show(fragments.get(i));
        transaction.commit();
    }
    // 当fragment已被实例化，就隐藏起来
    public void hideFragments(FragmentTransaction ft) {
        if (fragments.get(0) != null)
            ft.hide(fragments.get(0));
        if (fragments.get(1) != null)
            ft.hide(fragments.get(1));
        if (fragments.get(2) != null)
            ft.hide(fragments.get(2));
        if (fragments.get(3) != null)
            ft.hide(fragments.get(3));
        if (fragments.get(4) != null)
            ft.hide(fragments.get(4));
    }
    private void setViewPager(int id){
        switch (id){
            case R.id.rb_palette:
                fragment_position = 0;
//                vp_main_content.setCurrentItem(1);
                rb_palette.setTextColor(getResources().getColor(R.color.text_color_yellow));
                rb_gallery.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_music.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_edit_quick.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                replaceFragment(0);
                break;
            case R.id.rb_gallery:
                fragment_position = 1;
//                vp_main_content.setCurrentItem(0);
                rb_palette.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_gallery.setTextColor(getResources().getColor(R.color.text_color_yellow));
                rb_music.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_edit_quick.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                replaceFragment(1);
                break;
            case R.id.rb_music:
                fragment_position = 2;
//                vp_main_content.setCurrentItem(2);
                rb_palette.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_gallery.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_music.setTextColor(getResources().getColor(R.color.text_color_yellow));
                rb_edit_quick.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                replaceFragment(2);
                break;
            case R.id.rb_edit_quick:
                fragment_position = 3;
//                vp_main_content.setCurrentItem(3);
                rb_palette.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_gallery.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_music.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_edit_quick.setTextColor(getResources().getColor(R.color.text_color_yellow));
                replaceFragment(3);
                break;
        }

    }
    private void checkRadioButton(int index){
        switch (index){
            case 0:
                rg_main_item_select.check(R.id.rb_palette);
                rb_palette.setTextColor(getResources().getColor(R.color.text_color_yellow));
                rb_gallery.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_music.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                rb_edit_quick.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                fragment_position = 0;
                replaceFragment(0);
                break;
            case 4:
                fragment_position = 4;
                replaceFragment(4);
                break;
        }
    }
    private void initData() {
        fragments = new ArrayList<>();
//        gifFragment = new GifFragment(this);
        galleryFragment = new GalleryFragment(0,this);
        galleryFragment.setGalleryFragmentStatusCallback(new GalleryFragment.GalleryFragmentStatusCallback() {
            @Override
            public void onInlayEdit(String path) {
                rg_main_item_select.check(R.id.rb_palette);
                imageFragment.setPlateByPath(path,0);
            }

            @Override
            public void onSendRequire(String path, int type) {
                if (checkConnectStatus()) {
                    sendCmdType=CommandHelper.dataType_data;
                    sendPaletteData(true, true, path,1,type);
                }
            }
        });
        imageFragment = new ImageFragment(this);
        musicFragment = new MusicFragment(this);
        textFragment = new TextFragment(this);
//        controllerFragment = new ControllerFragment(this);
        quicklySendFragment = new QuicklySendFragment(this);
        quicklySendFragment.SetQuicklyCallback(new QuicklySendFragment.QuicklyCallback() {
            @Override
            public void onItemClick(String path) {
                if (checkConnectStatus()) {
                    sendCmdType=CommandHelper.dataType_data;
                    sendPaletteData(true, true, path,1,0);
                }
            }

            @Override
            public void onEditClick(int number) {

            }
        });
        fragments.add(imageFragment);
        fragments.add(galleryFragment);
        fragments.add(musicFragment);
        fragments.add(quicklySendFragment);
//        fragments.add(controllerFragment);
        fragments.add(textFragment);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_main,fragments.get(0));
        transaction.add(R.id.fragment_main,fragments.get(1));
        transaction.add(R.id.fragment_main,fragments.get(2));
        transaction.add(R.id.fragment_main,fragments.get(3));
        transaction.add(R.id.fragment_main,fragments.get(4));
        transaction.commit();
        aSwitch.setVisibility(View.GONE);
        versionUpdate = new AppVersionUpdate();
    }

    private void findView() {
        rg_main_item_select = (RadioGroup) findViewById(R.id.rg_main_item_select);
        rb_gallery = (RadioButton) findViewById(R.id.rb_gallery);
        rb_palette = (RadioButton) findViewById(R.id.rb_palette);
        rb_music = (RadioButton) findViewById(R.id.rb_music);
        rb_edit_quick = (RadioButton) findViewById(R.id.rb_edit_quick);
        iv_more = (ImageView) findViewById(R.id.iv_more);
        iv_main_send = (ImageButton) findViewById(R.id.iv_main_send);
        ll_connect_state = (LinearLayout) findViewById(R.id.ll_connect_state);
        tv_device_name = (TextView) findViewById(R.id.tv_device_name);
        aSwitch = (Switch) findViewById(R.id.switchTrack);
        fragment_main = (FrameLayout) findViewById(R.id.fragment_main);
        ll_mode_send = (LinearLayout) findViewById(R.id.ll_mode_send);
    }

    @SuppressLint("HandlerLeak")
    public Handler MEHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEND_SEQUENCE:
                    sendPaletteData(true,true,send_files.get(send_index).getPath(),send_files.get(send_index).getMsg_number(),0);
                    break;
                case CONNECT_AUTO:
                    Log.e(TAG, "handleMessage: CONNECT_AUTO" );
                    MEHandler.removeMessages(CONNECT_AUTO);
                    connectLastDevice();
//                    if (!serviceBinder.getConnectedStatus()){
//                        MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,5000);
//                    }
                    break;
                case REFRESH_VP:
                    refreshViewByColorModel();
                    break;
                case CONNECTED_FAIL:
                    MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                    showYCDialog(getResources().getString(R.string.connectedfail));
                    break;
                case CMD_CONNTCTING:
                    if (isNeedShowDialog){
                        showDialog(getResources().getString(R.string.connecting));
                    }
                    connectDevice();
                    break;
                case CMD_TIMEOUT:
                    if (isNeedShowDialog){
                        showYCDialog(getResources().getString(R.string.connectionTimedOut));
                    }
                    MEHandler.removeMessages(CMD_TIMEOUT);
//                    ble_admin.disConnect();
                    serviceBinder.disConnected();
                    break;
                case CMD_TIMEOUT_RECEIVE:
//                    if (erroCount>3){
                    showYCDialog(getResources().getString(R.string.sendOperationTimeout));
                    MEHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                    serviceBinder.disConnected();
//                    }
                    break;
                case DISMISS_DIALOG:
                    if (pd!=null){
                        pd.dismiss();
//                        pd=null;
                    }
                    break;
                case CMD_SENDING:
//                    if (!isConnectIntime){//连接成功发送开机  不显示提示
                    if (isNeedShowDialog){
                        showDialog(getResources().getString(R.string.dialog_sending));
                    }
                    if (imageFragment.getPreViewStatus()){
                        imageFragment.closePreView();
                    }
                    if (musicFragment.getSyncStatus()){
                        musicFragment.closeSync();
                    }
                    sendDataToDevice();
                    break;
            }
        }
    };
    private synchronized void connectLastDevice() {
        if (serviceBinder.getConnectedStatus()){
            return;
        }
        if (!StaticDatas.isConnectAuto){
            return;
        }
        String mac = (String) SPUtils.get(this,SPUtils.DEVICE,"MAC","");
        String name  = (String) SPUtils.get(this,SPUtils.DEVICE,"NAME","");
        Log.e(TAG, "connectLastDevice: "+mac+"_"+name );
        if (mac.equals("")){
            return;
        }
        if (name.equals("")){
            return;
        }
        connected_MAC = mac;
        connected_name = name;
        if (connected_MAC!=null&&connected_name!=null&&!connected_MAC.equals("")&&!connected_name.equals("")){

            isNeedShowDialog = false;
            connectDevice();
        }
    }
    private void refreshViewByColorModel(){
        checkAndResetFragmentByLEDSize();
        String type="";
        if (StaticDatas.isSupportMarFullColor){
//                isSupportMarFullColor = true;
            type="3";
//                if (textFragment.getStatue()){
//                    textFragment.refreshData(true,"3");
//                }
        }else {
//                isSupportMarFullColor = false;
            type = "1";
        }
        if (textFragment.getStatue()){
            textFragment.refreshData(StaticDatas.isSupportMarFullColor,type);
        }
        musicFragment.refreshLedview(this);
//            imageFragment.refreshView();
    }
    private void connectDevice(){
        serviceBinder.connected(connected_MAC,connected_name);
    }
    public   void sendDataToDevice(){

        //发送数据
        Runnable ad = new Runnable() {
            @Override
            public void run() {
                if ( serviceBinder.getConnectedStatus()) {
                    byte[] cmd = new byte[0];
                    Log.e("信息类型", sendCmdType + "");
                    if (sendCmdType==CommandHelper.dataType_data||sendCmdType==CommandHelper.dataType_list){
//                        sendTxtData();
                    }else if (sendCmdType == CommandHelper.dataType_light){
//                        sendcmd = ((CmdConts.WRITE_LIGHT + tvLightNum.getText() + "<E>").getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_mesList){
//                        sendcmd = ((CmdConts.LOOP_SHOW + waitSendIndexStr + "<E>").getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_power){
                        sendcmd = (turnOnOrOffCmd.getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_speed){
//                        sendcmd = ((CmdConts.WRITE_SPEED + tvSpeedNum.getText() + "<E>").getBytes());
                    }
//                    Log.e(TAG, "run: "+ Helpful.MYBytearrayToString(sendcmd) );
                    serviceBinder.sendData(sendcmd,true);
                }
            }
        };
        StaticDatas.poolExecutor.execute(ad);
    }
    //    private void sendTxtData() {
//        String cmdHeader;
//        byte[] waitSendData;
//        if (hasSendData == 0) {
//            //需要转换为16进制
//            String ledWidthCmd = ledView.getLEDWidget(isSupportMarFullColor) + "";
//            Log.e(TAG, "sendTxtData: dataType>>>>"+dataType);
//            if (ledWidthCmd.length() == 1) {
//                cmdHeader = head + selectedParams.wordSize + "000" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + "1fff" + "00";
//            } else if (ledWidthCmd.length() == 2) {
//                cmdHeader = head + selectedParams.wordSize + "00" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + "1fff" + "00";
//            } else if (ledWidthCmd.length() == 3) {
//                cmdHeader = head + selectedParams.wordSize + "0" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + "1fff" + "00";
//            } else {
//                cmdHeader = head+ selectedParams.wordSize + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + "1fff" + "00";
//            }
//            //第一帧
//            if (edit_byte.length > 500) {
//                hasSendData = 500;
//                waitSendData = Helpful.subByte(edit_byte, 0, hasSendData);
//            } else {
//                hasSendData = edit_byte.length;
//                waitSendData = edit_byte;
//            }
//        } else {
//            //后续帧
//            header = header + 1;
//            if (header > 9) {
//                cmdHeader = "" + header;
//            } else {
//                cmdHeader = "0" + header;
//            }
//            if ((edit_byte.length - hasSendData) > 512) {
//                waitSendData = Helpful.subByte(edit_byte, hasSendData, 512);
//                hasSendData = hasSendData + 512;
//            } else {
//                waitSendData = Helpful.subByte(edit_byte, hasSendData, (edit_byte.length - hasSendData));
//                hasSendData = hasSendData + (edit_byte.length - hasSendData);
//                //一段命令发送完毕 序号重置为0
//                header = 0;
//            }
//        }
//
//
//        //在最后一位增加校验位
//        byte checkByte = 0;
//        for (int position = 0; position < waitSendData.length; position++) {
//            checkByte = (byte) (checkByte ^ waitSendData[position]);
//        }
//        byte[] sendData = new byte[waitSendData.length + 1];
//        Helpful.catByte(waitSendData, 0, sendData, 0);
//        sendData[waitSendData.length] = checkByte;
//
//        //命令头
//        byte[] cmdHeaderbyte = cmdHeader.getBytes();
//
//        //拼接命令头和数据
//        sendcmd = new byte[sendData.length + cmdHeaderbyte.length];
//
//        Helpful.catByte(cmdHeaderbyte, 0, sendcmd, 0);
//        Helpful.catByte(sendData, 0, sendcmd, cmdHeaderbyte.length);
//
////        sendCmd(sendcmd);
//    }
    private boolean switch_state_iscontrolauto = false;//是否自动操作
    private boolean switch_state = false;//开关状态  flase 为 关
    private BleConnectService.ServiceListener serviceListener = new BleConnectService.ServiceListener(){

        @Override
        public void onConnected(int code) {
            switch (code){
                case BleConnectService.ServiceListener.connected_fail:
                    MEHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MEHandler.sendEmptyMessage(CONNECTED_FAIL);
//                        CommonUtils.toast("open notify failed ");
                            serviceBinder.disConnected();
                        }
                    });
                    break;
                case BleConnectService.ServiceListener.connected_succeed:
                    isConnectIntime = true;
//                    mConnected = true;
                    MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                    MEHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            //设置打开开关,发送开机命令
                            switch_state_iscontrolauto = false;
                            aSwitch.setChecked(true);
                            aSwitch.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.connecttingSuccessful),
                                    Toast.LENGTH_SHORT).show();
                            tv_device_name.setText(getTenChar(connected_name));

                        }

                    });
                    saveDevice();
                    break;
                case BleConnectService.ServiceListener.connected_timeOut:
                    MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                    MEHandler.sendEmptyMessage(CMD_TIMEOUT);
                    StaticDatas.isConnectAuto = true;
                    MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,1000);
                    Log.e(TAG, "onConnected time out: " );
//                    serviceBinder.disConnected();
                    break;
            }
        }

        @Override
        public void onSendData(int code) {
            switch (code){
                case BleConnectService.ServiceListener.sendData_fail:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pd != null) {
                                pd.dismiss();
                            }
                            showYCDialog(getString(R.string.failedOperation));
                            serviceBinder.disConnected();
                        }
                    });
                    break;
                case BleConnectService.ServiceListener.sendData_succeed:
                    Log.e(TAG, "onSendData: sendCmdType>>>>"+sendCmdType);
                    if ( sendCmdType  == CommandHelper.dataType_list&&send_index<send_files.size()-1){
                        Log.e(TAG, "onSendData: hasSend>>>"+ send_index +"/"+send_files.size());
                        send_index++;
                        MEHandler.sendEmptyMessageDelayed(SEND_SEQUENCE,300);

                    }else {
                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                        //发送成功添加到数据库
                        saveData();
                    }
                    if (!isConnectIntime){//首次连接 发送开机命令不 提示

                        MEHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.successfulOperation),
                                        Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                    //如果是刚连接成功 并且成功发送开机命令    再发送数据
                    if (sendCmdType==CommandHelper.dataType_power&&isConnectIntime){
                        //低版本  没有查询返回值  默认单色 12点  如果当前参数与
                        if (StaticDatas.isSupportMarFullColor||StaticDatas.LEDHight!=12){
                            StaticDatas.isSupportMarFullColor = false;
                            StaticDatas.LEDHight=12;
                            MEHandler.sendEmptyMessage(REFRESH_VP);
                        }
                        isConnectIntime = false;
                        if (isNeedSendData){
                            sendCmdType=CommandHelper.dataType_data;
                            //发送数据

//                                edit_byte = ledView.getTextByte(isSupportMarFullColor);
//                                hasSendData = 0;
                            MEHandler.sendEmptyMessage(CMD_SENDING);
                        }
                    }

                    break;
                case BleConnectService.ServiceListener.sendData_timeOut:
                    MEHandler.sendEmptyMessageDelayed(CMD_TIMEOUT_RECEIVE,0);
                    break;
            }
        }

        @Override
        public void onConnectStatueChange(int statue) {
            if (BleConnectService.ServiceListener.statue_Disconnected==statue){
                MEHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtils.toast(getResources().getString(R.string.connectionCasBeenDisconnected));
//                        mConnected = false;
                        aSwitch.setVisibility(View.GONE);
//                        hasSendData=0;
//                        sendcmd=null;
                        switch_state_iscontrolauto = true;
                        aSwitch.setChecked(false);
                        isConnectIntime = false;
                        tv_device_name.setText(getResources().getString(R.string.connectstate));
                        connected_MAC="";
                        connected_name = "";
                        if (pd!=null){
                            pd.dismiss();
                        }
                    }
                });
                Log.e(TAG, "onConnectStatueChange: disconnected");
                StaticDatas.isConnectAuto = true;
                MEHandler.sendEmptyMessageDelayed(CONNECT_AUTO,2000);
            }
        }

        @Override
        public void onReceivedLEDSet() {
            Log.e(TAG, "onReceivedLEDSet: " );
            MEHandler.sendEmptyMessage(DISMISS_DIALOG);
            //如果是刚连接成功 并且成功发送开机命令    再发送数据
            if (sendCmdType==CommandHelper.dataType_power&&isConnectIntime){
//                mStaticDatas.isSupportMarFullColor = true;
                MEHandler.sendEmptyMessage(REFRESH_VP);
                isConnectIntime = false;
                if (isNeedSendData){
                    sendCmdType=CommandHelper.dataType_data;
                    //发送数据
//                    edit_byte = ledView.getTextByte(isSupportMarFullColor);
//                    hasSendData = 0;
                    MEHandler.sendEmptyMessage(CMD_SENDING);
                }
            }
        }
    };

    private void saveData() {
        switch (fragment_position){
            case 4:
                textFragment.savaData();
                break;
        }
    }

    private void showDialog(String message){
        if (pd==null){
            pd = new LoadingDialog(MainActivity.this, new LoadingDialog.OnLoadingListener() {
                @Override
                public void onCancel() {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(),getString(R.string.cancelOperation),Toast.LENGTH_SHORT).show();
                    MEHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                    serviceBinder.clearConnectTimeOut();
                    serviceBinder.disConnected();
                }
            });
            pd.setCancelable(true);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            pd.setMessage(message);
        }else {
            pd.setMessage(message);
            pd.show();
        }
    }

    private void showResultDialog(String message, final boolean isUpdate) {

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setTitle(getString(R.string.hint));
        if (isUpdate){
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (isUpdate){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_download)));
                        startActivity(intent);
                        Log.e("result","更新");
                    }else {
                        Log.e("result","最新了");
                    }
                }
            });
        }
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create();
        builder.show();
    }
    /***
     * 显示对话框
     * @param message
     */
    private void showYCDialog(final String message) {
        if (hintDialog==null){
            hintDialog = new HintDialog(MainActivity.this, new HintDialog.OnSureListener() {
                @Override
                public void onSure() {
                    hintDialog.dismiss();
                }
            });
            hintDialog.setCancelable(true);
            hintDialog.show();
            hintDialog.setMessage(message);
        }else {
            hintDialog.setMessage(message);
            hintDialog.show();
        }
    }
    private void saveDevice(){
        SPUtils.put(this,SPUtils.DEVICE,"MAC",connected_MAC);
        SPUtils.put(this,SPUtils.DEVICE,"NAME",connected_name);
    }
    private String getTenChar(String str){
        if (str.length()<15){
            return str;
        }else {
            return str.substring(0,14)+"…";
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
