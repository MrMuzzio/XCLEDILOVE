package xc.LEDILove.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.warkiz.widget.IndicatorSeekBar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscription;
import xc.LEDILove.Bean.Params;
import xc.LEDILove.Bean.PopupItem;
import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.Bean.TextColorSelctParams;
import xc.LEDILove.R;
import xc.LEDILove.adapter.BrightAndModelRecyclerAdapter;
import xc.LEDILove.adapter.PopupwindowAdapter;
import xc.LEDILove.adapter.SelectorViewPagerAdapter;
import xc.LEDILove.bluetooth.CommandHelper;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.db.UmsResultBean;
import xc.LEDILove.db.UmsResultHelper;
import xc.LEDILove.font.CmdConts;
import xc.LEDILove.fragment.ColorSelectFragment;
import xc.LEDILove.fragment.FontSelectFragment;
import xc.LEDILove.service.BleConnectService;
import xc.LEDILove.utils.AppVersionUpdate;
import xc.LEDILove.utils.CommonUtils;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.SPUtils;
import xc.LEDILove.widget.ClearEditText;
import xc.LEDILove.widget.LedView;



/***
 * LED编辑界面
 */
public class MEditActivity extends BaseActivity implements View.OnClickListener,ColorSelectFragment.OnFragmentInteractionListener,FontSelectFragment.OnFragmentInteractionListener {

    //指定服务
    private static final String DATA_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可写
    private static final String TXD_CHARACT_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可读
    private static final String RXD_CHARACT_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    //APP版本信息获取地址
    private final String service_url = "http://www.xn--led-f00fr20c.com/appVersion/getNewAppVersion?appName=LEDILOVE";
    //region 编辑界面view

    private TextView tvSetting;
    private LedView ledView;
    private ScrollView scrollView;
    //发送
    private Button btnOk;

    //历史数据序号
    private BrightAndModelRecyclerAdapter numberRecyclerAdapter;
    private RecyclerView recyclerViewBrightness;
    //模式  上下移位..
    private RecyclerView recyclerViewModel;
    private BrightAndModelRecyclerAdapter modelRecyclerAdapter;
//    private ListView listView;
//    private ListitemAdapter listitemAdapter;


    //endregion

    //region 设置界面view
    //速度
    private IndicatorSeekBar seekBarSpeed;
    //亮度
    private IndicatorSeekBar seekBarLight;
    //序号1-8
    //private EditText editTextRunList;
    private Button btnRunList;
    private TextView tvSpeedNum;
    private TextView tvLightNum;
    private TextView tv_device_name;
    private ImageView ivBack;
    private LinearLayout ll_connect_state;
    private ClearEditText clearEditText;
    //开关
    private Switch aSwitch;
    //包裹led
    private FrameLayout frameLayout;
    //设置界面view
    private LinearLayout llContent;
    private View include;
    //endregion

    //region 变量
    // 连接线程
    private ProgressDialog pd;
    //发送的数据大小
    private int hasSendData = 0;
    //发送的序号头00 01  02
    private int header = 0;
    private byte[] edit_byte;
    private String[] wordSize;
    private String[] wordType;
    private String[] bright;
    private String[] model;
    private int MAX_COUNT = 250;
    //是否已经连接
//    private boolean mConnected = false;
    private String mac;
    private Params selectedParams = new Params();
    private List<UmsResultBean> umsResultBeanList;
    //数据库操作类
    private UmsResultHelper umsResultHelper;

    private String waitSendIndexStr = "";
    private Subscription subscribe_auto;
    private Subscription subscriptionRxResponseStutas;
    /***
     * 发送命令错误次数
     */
    private int erroCount = 0;
    private String turnOnOrOffCmd = "";
    private byte[] sendcmd;
    //是否获得硬件反馈
    private Boolean isGettingResponse = false;
    //endregion

    private  Button btn_progressbar;
    private TextView tv_head_left;
    private ImageView iv_more;
    private boolean isInMain = true;
    private AppVersionUpdate versionUpdate;
    /**
     * viewPager 参数
     * */
    private ViewPager vp_selector;
    private SelectorViewPagerAdapter  viewPagerAdapter;
    private android.support.v4.app.FragmentManager fragmentManager;
    private List<Fragment> fragments;
    private ColorSelectFragment colorSelectFragment;
    private FontSelectFragment fontSelectFragment;
    private String TAG = MEditActivity.class.getSimpleName();
    private BleConnectService.Mybinder serviceBinder;

    private ImageView  mIvGuideRedPoint ;
    private RelativeLayout rl_launcher;
    private TextView tv_version;
    private String vername;
    private StaticDatas mStaticDatas;
    private TextView tv_navigation_index;
    private ImageView iv_navigation_left;
    private ImageView iv_navigation_right;
    private RadioGroup rg_font_color_select;
    private RadioButton rb_font;
    private RadioButton rb_color;
    /**
     * 多彩标志位  是否支持多彩
     * */
    private boolean isSupportMarFullColor = true;
    /**
     * 多彩下 是否自动颜色
     * */
    private boolean isAutoColor = false;
    private String head = "BT03120";
    private String dataType = "1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        setContentView(R.layout.activity_edit);
        setSwipeBackEnable(false);
        overridePendingTransition(R.anim.activity_launch_enter,0);
        tvSetting = (TextView) findViewById(R.id.tv_set);
        mIvGuideRedPoint = (ImageView)findViewById(R.id.iv_guide_redPoint);
        linLay_guide_pointContainer = (LinearLayout) findViewById(R.id.linLay_guide_pointContainer);
        btn_progressbar = (Button) findViewById(R.id.btn_progressbar);
        iv_more = (ImageView) findViewById(R.id.iv_more);
        tv_head_left = (TextView) findViewById(R.id.tv_head_left);
        vp_selector = (ViewPager) findViewById(R.id.vp_selector);
        tv_navigation_index = (TextView) findViewById(R.id.tv_navigation_index);
        iv_navigation_left = (ImageView) findViewById(R.id.iv_navigation_left);
        iv_navigation_right = (ImageView) findViewById(R.id.iv_navigation_right);
        rg_font_color_select = (RadioGroup) findViewById(R.id.rg_font_color_select);
        rb_font = (RadioButton) findViewById(R.id.rb_font);
        rb_color = (RadioButton) findViewById(R.id.rb_color);
        rg_font_color_select.check(R.id.rb_font);
        rb_font.setTextColor(getResources().getColor(R.color.white));
        rb_color.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
        rg_font_color_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_font:
                        vp_selector.setCurrentItem(0);
                        break;
                    case R.id.rb_color:
                        vp_selector.setCurrentItem(1);
                        break;
                }
            }
        });
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showPopupWindow(iv_more);
                Intent mintent = new Intent();
                mintent.setClass(MEditActivity.this,OtherMessageActivity.class);
                startActivity(mintent);
//                if (isSupportMarFullColor){
//                    isSupportMarFullColor=false;
//                    dataType="1";
//                }else {
//                    isSupportMarFullColor=true;
//                    dataType = "3";
//                }
//                refreshViewPager();
            }
        });
        mStaticDatas= StaticDatas.getInstance();
        if (mStaticDatas.isSupportMarFullColor){
            isSupportMarFullColor = true;
        }else {
            isSupportMarFullColor = false;
        }
//        poolExecutor = mStaticDatas.poolExecutor;
//        poolExecutor = new ThreadPoolExecutor(3, 5,
//                10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(128));
        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendCmd("+++".getBytes());
//                refresh();
                llContent.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                include.setVisibility(View.GONE);
                isInMain = false;
            }
        });
        textBeanList = new ArrayList<>();
        textBeanList_backup = new ArrayList<>();
        intial();
        //读数据库
        initSQLData();
        initViewData();
        versionUpdate = new AppVersionUpdate();
        //检测版本更新
        checkVersionToUpData();
        //绑定创建蓝牙service
        initService();
//        mac = getIntent().getStringExtra("mac");

    }
    private void refreshViewByColorModel(){
        if (mStaticDatas!=null){
            if (mStaticDatas.isSupportMarFullColor){
                isSupportMarFullColor = true;
                dataType="3";
            }else {
                isSupportMarFullColor = false;
                dataType = "1";
            }
            refreshViewPager();
        }
    }
    /**
     * 刷新ViewPager
     * */
    private void refreshViewPager() {
        fragments.clear();
        fragments.add(fontSelectFragment);
        if (isSupportMarFullColor){
            fragments.add(colorSelectFragment);
            rb_color.setVisibility(View.VISIBLE);
        }else {
            rb_color.setVisibility(View.GONE);
        }
        viewPagerAdapter.notifyDataSetChanged();
        rg_font_color_select.check(R.id.rb_font);
        setEditTextFilter(selectedParams.wordSize);
        setTextSpan(clearEditText.getText().toString());
        ledView.setMatrixTextWithColor(isSupportMarFullColor,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);
        colorSelctParams.setColor_backdrop(defaultBGColor);
        colorSelctParams.setColor_font(defaultFontColor);

    }

    private boolean isStartFirst = true;
    private boolean isNeedShowDialog = true;
    private void connectLastDevice() {
        connected_MAC = (String) SPUtils.get(this,"MAC","");
        connected_name = (String) SPUtils.get(this,"NAME","");
        if (connected_MAC!=null&&connected_name!=null&!connected_MAC.equals("")){
//            serviceBinder.connected(connected_MAC,connected_name);
            isNeedShowDialog = false;
            connectDevice();
        }
    }

    private void initService() {

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceBinder = (BleConnectService.Mybinder) iBinder;
                serviceBinder.setServiceListener(serviceListener);
                if (isStartFirst){
//                    MEHandler.sendEmptyMessageDelayed(CMD_CONNTCTING,500);
//                    connectLastDevice();
                    isStartFirst = false;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent  = new Intent();
        intent.setClass(this,BleConnectService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }

    private void checkVersionToUpData() {
        getServiceVersionCode(service_url);
    }
    Call mcall;
    private void getServiceVersionCode(String url) {
        //发送网络请求
        OkHttpClient client = new OkHttpClient();

        Request.Builder builder = new Request.Builder().url(url);
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
                Log.i("onFailure", e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i("onResponse", "cache---" + str);
                } else {
                    String jsonData = response.body().string();
                    int code =  versionUpdate.parseJson(jsonData);
                    if (code==-1){
                        return;
                    }
                    String str = response.networkResponse().toString();
                    Log.i("onResponse", "network---" + str);
                    //服务器版本大于已安装版本
                    if (code>versionUpdate.getVersionCode(getApplicationContext())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultDialog(getString(R.string.app_update),true);
                            }
                        });
                    }else {

                    }
                }
            }
        });
    }
    private void showResultDialog(String message, final boolean isUpdate) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MEditActivity.this);
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
    private void showPopupWindow(View view) {
        final View popupwindow = View.inflate(MEditActivity.this,R.layout.popupwindow,null);
        ListView lv_popupitem =(ListView) popupwindow.findViewById(R.id.lv_popupitem);
        String[] popup_messages = getResources().getStringArray(R.array.popup_message);
        int[] popup_image_id = {R.mipmap.ic_seting_1,R.mipmap.ic_about_3};
        List<PopupItem> popupItems = new ArrayList<>();
        for (int i=0;i<popup_messages.length;i++){
            PopupItem item = new PopupItem();
            item.setMessage(popup_messages[i]);
            item.setImageId(popup_image_id[i]);
            popupItems.add(item);
            item=null;
        }
        PopupwindowAdapter popupwindowAdapter = new PopupwindowAdapter(MEditActivity.this,popupItems);
        lv_popupitem.setAdapter(popupwindowAdapter);
        //参数一，view对象；参数二 宽度；参数三 高度；参数四是否获取焦点
        final PopupWindow window = new PopupWindow(popupwindow,350,180,true);
        window.setBackgroundDrawable(new ColorDrawable(Color.rgb(100,196,246)));
        window.setOutsideTouchable(true);
        window.setTouchable(true);
        window.showAsDropDown(view,100,2);
        lv_popupitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("onItemClick",i+"");
                window.dismiss();
                switch (i){
                    case 0:
//                        refresh();
                        llContent.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                        include.setVisibility(View.GONE);
                        isInMain = false;
                        break;
                    case 1:
                        Intent mintent = new Intent();
                        mintent.setClass(MEditActivity.this,OtherMessageActivity.class);
                        startActivity(mintent);
                        break;
                }
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onResume: ");

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshViewByColorModel();
        Log.e(TAG, "onResume: ");
        if (serviceBinder!=null){
            serviceBinder.setServiceListener(serviceListener);
            if(serviceBinder.getConnectedStatus()){
//                isConnectIntime = true;
                switch_state_iscontrolauto=true;
//                    mConnected = true;
                MEHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //设置打开开关,发送开机命令
//                        switch_state_iscontrolauto = true;
                        aSwitch.setChecked(true);
                        aSwitch.setVisibility(View.VISIBLE);
                        connected_name =serviceBinder.getConnectedMacName();
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
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
    }
    @Override
    protected void onStop() {
        super.onStop();
        serviceBinder.removeServiceListener();
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
    protected void onDestroy() {
        SPUtils.put(this,"MAC",connected_MAC);
        SPUtils.put(this,"NAME",connected_name);
        unbindService(serviceConnection);
        if (pd!=null){
            pd.dismiss();
            pd=null;
        }
        MEHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private boolean only_connect = true;
    private boolean select_return_first = false;
    private boolean switch_state_iscontrolauto = false;//是否自动操作
    private boolean switch_state = false;//开关状态  flase 为 关
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sendCmdType = CommandHelper.dataType_data;
        if (requestCode ==1004&&resultCode == RESULT_OK) {
            if (data.getExtras().getString("MAC").equals("")){

            }else {
                isNeedShowDialog = true;
                connected_MAC = data.getExtras().getString("MAC");
                connected_name = data.getExtras().getString("NAME");
                MEHandler.sendEmptyMessageDelayed(CMD_CONNTCTING,500);

            }
        }else {
            select_return_first = true;
        }
    }
    private String getTenChar(String str){
        if (str.length()<15){
            return str;
        }else {
            return str.substring(0,14)+"…";
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 发送数据
             */
            case R.id.btnOk:
//                this.requestCode = 1004;
                isNeedSendData = true;
                sendCmdType = CommandHelper.dataType_data;
                //如果输入为空 返回
                if (TextUtils.isEmpty(clearEditText.getText().toString())) {
                    showYCDialog(getString(R.string.pleaseEnterText));
                    return;
                }
                //如果无连接 进入扫描页面
                if (!serviceBinder.getConnectedStatus()) {
                    Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                    return;
                    //有连接 但处于关机状态
                }
                else if (!switch_state){
                    showYCDialog(getString(R.string.pleasepoweron));
                    return;
                }

                //数据未处理完成
                if (!isComplete){

                    showYCDialog(getString(R.string.please_wait));
                    return;
                }
                edit_byte = ledView.getTextByte(isSupportMarFullColor);
                hasSendData = 0;
//                pd.show();
                MEHandler.sendEmptyMessage(CMD_SENDING);
        }
    }


    /***
     * 执行发送数据事件
     */
    private void sendTxtData() {
        String cmdHeader;
        byte[] waitSendData;
        if (hasSendData == 0) {
            //需要转换为16进制
            String ledWidthCmd = ledView.getLEDWidget(isSupportMarFullColor) + "";
            Log.e(TAG, "sendTxtData: dataType>>>>"+dataType);
            if (ledWidthCmd.length() == 1) {
                cmdHeader = head + selectedParams.wordSize + "000" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else if (ledWidthCmd.length() == 2) {
                cmdHeader = head + selectedParams.wordSize + "00" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else if (ledWidthCmd.length() == 3) {
                cmdHeader = head + selectedParams.wordSize + "0" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else {
                cmdHeader = head+ selectedParams.wordSize + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            }
            //第一帧
            if (edit_byte.length > 500) {
                hasSendData = 500;
                waitSendData = Helpful.subByte(edit_byte, 0, hasSendData);
            } else {
                hasSendData = edit_byte.length;
                waitSendData = edit_byte;
            }
        } else {
            //后续帧
            header = header + 1;
            if (header > 9) {
                cmdHeader = "" + header;
            } else {
                cmdHeader = "0" + header;
            }
            if ((edit_byte.length - hasSendData) > 512) {
                waitSendData = Helpful.subByte(edit_byte, hasSendData, 512);
                hasSendData = hasSendData + 512;
            } else {
                waitSendData = Helpful.subByte(edit_byte, hasSendData, (edit_byte.length - hasSendData));
                hasSendData = hasSendData + (edit_byte.length - hasSendData);
                //一段命令发送完毕 序号重置为0
                header = 0;
            }
        }


        //在最后一位增加校验位
        byte checkByte = 0;
        for (int position = 0; position < waitSendData.length; position++) {
            checkByte = (byte) (checkByte ^ waitSendData[position]);
        }
        byte[] sendData = new byte[waitSendData.length + 1];
        Helpful.catByte(waitSendData, 0, sendData, 0);
        sendData[waitSendData.length] = checkByte;

        //命令头
        byte[] cmdHeaderbyte = cmdHeader.getBytes();

        //拼接命令头和数据
        sendcmd = new byte[sendData.length + cmdHeaderbyte.length];

        Helpful.catByte(cmdHeaderbyte, 0, sendcmd, 0);
        Helpful.catByte(sendData, 0, sendcmd, cmdHeaderbyte.length);

//        sendCmd(sendcmd);
    }


    private List<byte[]> senddatas = new ArrayList<byte[]>();

    /***
     * 颜色值转换为命令
     * @param colorValues
     * @return
     */
    public String color2Cmd(int colorValues) {
        String result = "1fff";
        if (colorValues == getResources().getColor(R.color.red)) {
            result = "1fff";
        }
        if (colorValues == getResources().getColor(R.color.yellow)) {
            result = "2fff";
        }
        if (colorValues == getResources().getColor(R.color.green)) {
            result = "3fff";
        }
        if (colorValues == getResources().getColor(R.color.cyan)) {
            result = "4fff";
        }
        if (colorValues == getResources().getColor(R.color.blue)) {
            result = "5fff";
        }
        if (colorValues == getResources().getColor(R.color.purple)) {
            result = "6fff";
        }
        if (colorValues == getResources().getColor(R.color.white)) {
            result = "7fff";
        }
        return result;
    }

    /***
     * 显示对话框
     * @param erroMsg
     */
    private void showYCDialog(final String erroMsg) {
        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MEditActivity.this)
                            .setTitle(getString(R.string.hint))
                            .setMessage(erroMsg)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            //提示按钮
//                            .setNegativeButton(getResources().getString(R.string.help), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    Intent mIntent = new Intent();
//                                    mIntent.setClass(MEditActivity.this,HelpActivity.class);
//                                    startActivity(mIntent);
//                                }
//                            })
                            .create().show();
                }
            });

        }
    }

    private int requestCode = -1;

    private boolean isInterrupt = false;
    private boolean isComplete = false;
    private ColorSelectFragment.ColorCallback colorCallback;
    private FontSelectFragment.FontCallback fontCallback;
    private TextColorSelctParams colorSelctParams;
    private ServiceConnection serviceConnection;
    private LinearLayout linLay_guide_pointContainer;
    private int mPointWidth;
    private int defaultFontColor = 1;
    private int defaultBGColor =0;
    /***
     * 初始化
     */
    private void intial() {
        colorSelctParams = new TextColorSelctParams();
        colorSelctParams.setColor_backdrop(defaultBGColor);
        colorSelctParams.setColor_font(defaultFontColor);
        colorCallback = new ColorSelectFragment.ColorCallback() {
            @Override
            public void OnColorSelected(TextColorSelctParams selctParams) {
                colorSelctParams= selctParams;
                Log.d("colorSelctParams", colorSelctParams.toString());
                if (textSelected()){
                    int start = clearEditText.getSelectionStart();
                    int end = clearEditText.getSelectionEnd();
                    for (int k=0;k<end-start;k++){
                        textBeanList.get(start+k).setBackdrop((selctParams.getColor_backdrop()));
                        textBeanList.get(start+k).setFont((selctParams.getColor_font()));
                    }
                    setTextSpan(clearEditText.getText().toString());
//            etMsg.setSelection(start,end);
                }
            }

            @Override
            public void OnColorModelChange(boolean isAuto) {
                if (isSupportMarFullColor){
                    isAutoColor = isAuto;
                    if (isAutoColor){
                        setTextAutoColor(clearEditText.getText().toString());
                    }else {
                        if (textBeanList_backup.size()==textBeanList.size()){
                            try {
                                textBeanList = deepCopy(textBeanList_backup);
                                textBeanList_backup.clear();
                                setTextSpan(clearEditText.getText().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        };
        fontCallback = new FontSelectFragment.FontCallback() {
            @Override
            public void onWordSizeChange(String size) {
                Log.e(TAG, "onWordSizeChange:"+size );
                selectedParams.wordSize = Integer.parseInt(size);
                if (!TextUtils.isEmpty(selectedParams.str)) {
                    //重新设置EditText 字符控制
                    setEditTextFilter(selectedParams.wordSize);
                    if (fontSelectFragment!=null){
                        fontSelectFragment.setCharCount(textBeanList.size() + "/"+MAX_COUNT);
                        Log.e(TAG, "onTextChanged: "+MAX_COUNT);
                    }
                    ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType,textBeanList);
                }
            }

            @Override
            public void onWordTypeChange(String type) {
                selectedParams.wordType = type;
                if (!TextUtils.isEmpty(selectedParams.str)) {

                    ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType,textBeanList);
                }
            }

            @Override
            public void onViewCreate() {
                fontSelectFragment.setCharCount(textBeanList.size() + "/"+MAX_COUNT);
            }

            @Override
            public void onImageSelected(String str) {
                int index = clearEditText.getSelectionStart();
                Editable editable = clearEditText.getText();
                editable.insert(index,str);
//                int index = clearEditText.getSelectionEnd();
//                String msg = clearEditText.getText().toString();
//                char[] chars = str.toCharArray();
//                for (int i=0;i<str.length();i++){
//                    TextBean bean = new TextBean();
//                    bean.setCharacter(chars[i]);
//                    bean.setBackdrop(selctParams.getColor_backdrop());
//                    bean.setFont(selctParams.getColor_font());
//                    textBeanList.add(index+i,bean);
//                }
//                setTextSpan(msg.substring(0,index)+str+msg.substring(index,msg.length()));
            }
        };
        colorSelectFragment = new ColorSelectFragment(this);
        colorSelectFragment.setColorCallback(colorCallback);
        fontSelectFragment = new FontSelectFragment(this);
        fontSelectFragment.setFontCallback(fontCallback);
        fragments = new ArrayList<>();
        fragments.add(fontSelectFragment);
        fragments.add(colorSelectFragment);
        fragmentManager = getSupportFragmentManager();
        umsResultHelper = new UmsResultHelper(this);
        viewPagerAdapter = new SelectorViewPagerAdapter(fragmentManager,fragments);
        vp_selector.setAdapter(viewPagerAdapter);
        vp_selector.setCurrentItem(0);
        iv_navigation_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 0 ");
                vp_selector.setCurrentItem(1);
            }
        });
        iv_navigation_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 1 ");
                vp_selector.setCurrentItem(0);
            }
        });
//        addGrayPoint(viewPagerAdapter.getCount());
//        measureGrayPointGap();
        vp_selector.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                int leftMargin = (int)
//                        (mPointWidth * positionOffset + mPointWidth * position);
//                //重新修改布局参数
//                RelativeLayout.LayoutParams params =
//                        (RelativeLayout.LayoutParams)  mIvGuideRedPoint.getLayoutParams();
//                params.leftMargin = leftMargin;
//                mIvGuideRedPoint.setLayoutParams(params);
                if (position==0){
                    tv_navigation_index.setText(getString(R.string.pre));

                    iv_navigation_right.setVisibility(View.VISIBLE);
                    iv_navigation_left.setVisibility(View.GONE);
                }else if (position==1){
                    tv_navigation_index.setText(getString(R.string.after));
                    iv_navigation_right.setVisibility(View.GONE);
                    iv_navigation_left.setVisibility(View.VISIBLE);
                }
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onPageSelected(int position) {
                if (position==0){
                    rg_font_color_select.check(R.id.rb_font);
                    rb_font.setTextColor(getResources().getColor(R.color.white));
                    rb_color.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                }else if (position==1){
                    rg_font_color_select.check(R.id.rb_color);
                    rb_font.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
                    rb_color.setTextColor(getResources().getColor(R.color.white));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        recyclerViewBrightness = (RecyclerView) findViewById(R.id.recyclerview_brightness);
        recyclerViewBrightness.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerViewBrightness.setItemAnimator(new DefaultItemAnimator());

        //序号
        numberRecyclerAdapter = new BrightAndModelRecyclerAdapter(this, new BrightAndModelRecyclerAdapter.MyCollectionRecordListener() {
            @Override
            public void onclick(String keyName) {

                selectedParams.switchValue = keyName;
                //响应点击序号事件，点击哪一个显示哪一个的历史数据，没有就不显示
                UmsResultBean umsResultBean = umsResultHelper.getUmsReulsByIndex(Integer.parseInt(keyName));
                if (null != umsResultBean) {
                    selectedParams.color = umsResultBean.color;
                    selectedParams.model = umsResultBean.type;
                    textBeanList = umsResultBean.beanList;
                    if (isAutoColor){
                        setTextAutoColor(umsResultBean.body);
                    }else {
                        setTextSpan(umsResultBean.body);
                    }
                    modelRecyclerAdapter.setSelected(Integer.parseInt(umsResultBean.type) - 1);
                }
            }
        });
        bright = getResources().getStringArray(R.array.bright);
        numberRecyclerAdapter.addList(Arrays.asList(bright));
        recyclerViewBrightness.setAdapter(numberRecyclerAdapter);

        recyclerViewModel = (RecyclerView) findViewById(R.id.recyclerview_model);
        recyclerViewModel.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerViewModel.setItemAnimator(new DefaultItemAnimator());
        //特效
        modelRecyclerAdapter = new BrightAndModelRecyclerAdapter(this, new BrightAndModelRecyclerAdapter.MyCollectionRecordListener() {
            @Override
            public void onclick(String keyName) {
                selectedParams.model = keyName;
            }
        });
        model = getResources().getStringArray(R.array.model);
        modelRecyclerAdapter.addList(Arrays.asList(model));
        recyclerViewModel.setAdapter(modelRecyclerAdapter);

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        ll_connect_state = (LinearLayout) findViewById(R.id.ll_connect_state);
        ll_connect_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG,"ll_connect_state>>onClick");
                isNeedSendData = false;
                if (serviceBinder.getConnectedStatus()){
                    serviceBinder.disConnected();
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//
////                                ble_admin.disConnect();
//                        }
//                    };
//                    poolExecutor.execute(runnable);

                }
                Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                startActivityForResult(intent, 1004);
            }
        });
        tv_device_name = (TextView) findViewById(R.id.tv_device_name);

        clearEditText = (ClearEditText) findViewById(R.id.clearEditText);
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
        clearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });
        aSwitch = (Switch) findViewById(R.id.switchTrack);
        aSwitch.setVisibility(View.GONE);
        //开关
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
                    Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                    select_return_first = false;
                }
            }
        });
        clearEditText.addTextChangedListener(mTextWatcher);
        clearEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent arg0) {
                if (i==KeyEvent.KEYCODE_DEL&& arg0.getAction()==KeyEvent.ACTION_DOWN){//如果单独判定按键值，if内容会执行两次！(包含按下 松开 两次事件)
                    if (textBeanList.size()>clearEditText.getSelectionStart()-1&&clearEditText.getSelectionStart()>0){
//                        Log.e(TAG, "onKey: delete" + textBeanList.size());
                        textBeanList.remove(clearEditText.getSelectionEnd()-1);
//                        Log.e(TAG, "onKey: delete" + textBeanList.size());
                    }
                }
                return false;//这里返回true 系统就不会再做删除反馈了
            }
        });
        clearEditText.setCutAndPastCallback(new ClearEditText.CutAndPastCallback() {
            @Override
            public void onCut(String string) {
                int start  = clearEditText.getSelectionStart();
                int end = clearEditText.getSelectionEnd();
                int count = end-start;
                for (int i=0;i<count;i++){
                    //这里移除数据的时候不能 从低位开始，因为从低位开始移除  移除后集合长度变小了  下次再移除 不再是最低位  而是最低位+1  最后出现数组越界
                    //eg :例如集合长度为4（1 2 3 4 ）  count 也是4  第一次 i=0 移除0（1）  第二次 i=1  移除的1(此时集合为234) 移除的便是3  而非2
                    textBeanList.remove(count-i-1);
                }
            }

            @Override
            public void onPast(String string) {
                isPaste = true;
                int start  = clearEditText.getSelectionStart();
                int end = clearEditText.getSelectionEnd();
                char[] chars = string.toCharArray();
                for (int i=0;i<chars.length;i++){
                    TextBean bean = new TextBean();
                    bean.setCharacter(chars[chars.length-i-1]);
                    bean.setBackdrop(colorSelctParams.getColor_backdrop());
                    bean.setFont(colorSelctParams.getColor_font());
                    textBeanList.add(start,bean);
                }
            }

            @Override
            public void onDelete() {
                textBeanList.clear();
                setTextSpan("");
            }
        });
        frameLayout = (FrameLayout) findViewById(R.id.ledView);
        ledView = new LedView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ledView.setLayoutParams(params);
        frameLayout.addView(ledView);
        ledView.setCompleteCallback(new LedView.completeCallback() {
            @Override
            public void onComplete(boolean result) {
                isComplete = result;
            }
        });


    }
    /**
     *
     * 全彩下，自动颜色
     * */
    private void setTextAutoColor(String str) {
        try {
            textBeanList_backup = deepCopy(textBeanList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        textBeanList.clear();
//        String string = clearEditText.getText().toString();
        String string = str;
        char[] chars = string.toCharArray();
        TextBean bean;
        for (int i = 0;i<chars.length;i++){
            bean = new TextBean();
            bean.setFont((i%7)+1);//字体颜色值在1-7循环
            bean.setBackdrop(0);//自动颜色，背景设为黑色
            bean.setCharacter(chars[i]);
            textBeanList.add(bean);
        }
        setTextSpan(string);
        clearEditText.setSelection(clearEditText.getText().length());
    }

    private void initViewData(){
        if (null != umsResultBeanList && umsResultBeanList.size() > 0) {
            numberRecyclerAdapter.setSelected(umsResultBeanList.get(0).numberIndex - 1);
            selectedParams.color = umsResultBeanList.get(0).color;
            selectedParams.model = umsResultBeanList.get(0).type;
            textBeanList = umsResultBeanList.get(0).beanList;
            /**
             * clearEditText 输入框数据初始化
             * */
            if (textBeanList==null){
                textBeanList = getDefultTextBeanList(umsResultBeanList.get(0).body);
                setTextSpan(umsResultBeanList.get(0).body);
            }else {
                setTextSpan(umsResultBeanList.get(0).body);
            }
            /**
             * recyclerViewModel 初始化选中项
             * */
            modelRecyclerAdapter.setSelected(Integer.parseInt(umsResultBeanList.get(0).type) - 1);

        }
    }
    private void initSQLData(){

        //初始化查看本地有没有历史数据
        umsResultBeanList = umsResultHelper.getUmsReulstList();
        if (umsResultBeanList.size() < 8) {
            //判填充到8个值，MESSAGE1...MESSAGE8
            for (int position = umsResultBeanList.size(); position < 8; position++) {
                UmsResultBean umsResultBean = new UmsResultBean();
                umsResultBean.type = selectedParams.model;
                umsResultBean.color = getResources().getColor(R.color.red);
                umsResultBean.speed = 1;
                umsResultBean.bright = 1;
                umsResultBean.numberIndex = position + 1;
                umsResultBean.body = "MESSAGE" + (position + 1);
                umsResultBean.beanList = getDefultTextBeanList(umsResultBean.body);
                umsResultHelper.storeUmsReulst(umsResultBean);
            }
            umsResultBeanList = umsResultHelper.getUmsReulstList();
        }

    }
    private void measureGrayPointGap(){
        // meaure->layout->draw(必须在onCreate执行结束之后才开始绘制),
        // 所以不能直接在onCreate中获取位置相关信息
        // 监听layout执行结束事件, 结束之后再去获取位置信息,计算圆点间距
        // 获取视图树,hierarchyviewer.bat
        mIvGuideRedPoint.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    // layout方法执行结束之后,回调此方法
                    @Override
                    public void onGlobalLayout() {
                        mPointWidth = linLay_guide_pointContainer
                                .getChildAt(1)
                                .getLeft() -
                                linLay_guide_pointContainer
                                        .getChildAt(0)
                                        .getLeft();
                    }
                });
    }
    private void addGrayPoint(int count){
        //初始化灰色小圆点
        for (int i=0; i<count; i++){
            ImageView point = new ImageView(this);
            point.setBackgroundResource(R.drawable.shape_point_gray);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0){
                params.leftMargin = 100;//设置左边距
            }
            point.setLayoutParams(params);
            linLay_guide_pointContainer.addView(point);
        }
    }
    private List<TextBean> getDefultTextBeanList(String body) {
        char [] chars = body.toCharArray();
        List<TextBean> result = new ArrayList<>();
        for (int i=0;i<chars.length;i++){
            TextBean bean = new TextBean();
            bean.setFont(1);
            bean.setBackdrop(0);
            bean.setCharacter(chars[i]);
            result.add(bean);
        }
        return result;
    }
    private void setEditTextFilter(int wordSize) {
        if (isSupportMarFullColor){
            if (wordSize==12){
                MAX_COUNT = 80;
            }else if (wordSize == 16){
                MAX_COUNT = 50;

            }
        }else {
            if (wordSize==12){
                MAX_COUNT = 250;
            }else if (wordSize == 16){
                MAX_COUNT = 128;
            }
        }
        if (clearEditText.getText().toString().length()>=MAX_COUNT){
            textBeanList.clear();
//            setTextSpan(clearEditText.getText().toString().substring(0,MAX_COUNT));
            clearEditText.setText(clearEditText.getText().toString().substring(0,MAX_COUNT));
//            textBeanList.subList(0,MAX_COUNT);
            clearEditText.setSelection(MAX_COUNT);
            Toast.makeText(getApplication(),getResources().getString(R.string.out_of_max),Toast.LENGTH_SHORT).show();
        }
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
//        setLeftCount();
    }


    /**
     * 刷新列表
     */
//    public void refresh() {
//        umsResultBeanList = umsResultHelper.getUmsReulstList();
//        listitemAdapter.refresh(umsResultBeanList);
//    }
    public static long lastTextChangeTime;
    private Handler handler = new Handler();

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {
        @Override
        public void run() {
            //
            Log.e("delayRun: ", textBeanList.size()+"");
            ledView.setMatrixTextWithColor(isSupportMarFullColor,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);
        }
    };
    /***
     * 监听文本输入
     */
    private boolean isSet = false;//是否是设置Text 引发的回调
    private  boolean isPaste = false;//是否粘贴事件
    private SpannableStringBuilder spannableStringBuilder;
    private List<TextBean> textBeanList;
    private List<TextBean> textBeanList_backup;//切换自动颜色和自定义颜色 是作为textBeanList 的备份，避免切换自动颜色操作不可逆
    private TextCountListener countListener;
    public interface TextCountListener {
        void onTextCountChange(String count);
    }
    public void AddTextCountListener(TextCountListener countListener){
        this.countListener = countListener;
    }
    private TextWatcher mTextWatcher = new TextWatcher() {

        private int editStart;

        private int editEnd;
        public  boolean isFastDoubleClick() {
            long time = System.currentTimeMillis();
            long timeD = time - lastTextChangeTime;
            if ( 0 < timeD && timeD < 500) {       //100毫秒内按钮无效，这样可以控制快速点击，自己调整频率
                return true;
            }
            lastTextChangeTime = time;
            return false;
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (!isSet){
                if(delayRun!=null){
                    //每次editText有变化的时候，则移除上次发出的延迟线程
                    handler.removeCallbacks(delayRun);
                }
                editStart = clearEditText.getSelectionStart();
                editEnd = clearEditText.getSelectionEnd();

                clearEditText.removeTextChangedListener(mTextWatcher);

                // 这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度
                // 因为是中英文混合，单个字符而言，calculateLength函数都会返回1
                while (calculateLength(s.toString()) > MAX_COUNT) { // 当输入字符个数超过限制的大小时，进行截断操作
                    s.delete(editStart - 1, editEnd);
                    editStart--;
                    editEnd--;
                }
                // mEditText.setText(s);将这行代码注释掉就不会出现后面所说的输入法在数字界面自动跳转回主界面的问题了，
                clearEditText.setSelection(editStart);

                // 恢复监听器
                clearEditText.addTextChangedListener(mTextWatcher);

//            setLeftCount();

                // if (!TextUtils.isEmpty(s.toString())) {
                selectedParams.str = s.toString();
//            ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
//            //  }
                //延迟400ms，如果不再输入字符，则执行该线程的run方法
                if (s.toString().length()<50){
                    handler.postDelayed(delayRun, 100);
                }else if(s.toString().length()>50&&s.toString().length()<200){
                    handler.postDelayed(delayRun, 300);
                }else {
                    handler.postDelayed(delayRun, 400);
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
//            if (isFastDoubleClick()) {
//                return;
//            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before,
                                  int count) {
            Log.e(TAG, "onTextChanged: "+charSequence);
            if (fontSelectFragment!=null){//更新字符计数
                fontSelectFragment.setCharCount(charSequence.length() + "/"+MAX_COUNT);
            }
            char[] chars = charSequence.toString().toCharArray();
//            if (charSequence.length()>0){
//                iv_edit_delete.setVisibility(View.VISIBLE);
//            }
            if (!isSet){
//                    textBeanList.clear();
                //没选
                if (!textSelected()){
//                    textBeanList.clear();
                    int selection = clearEditText.getSelectionEnd();
                    if (isAutoColor){
                        setTextAutoColor(charSequence.toString());
                    }else {
                        if (!isPaste){
                            for (int i=0;i<count;i++){
                                TextBean bean = new TextBean();
                                bean.setCharacter(chars[start+i]);
                                bean.setBackdrop(colorSelctParams.getColor_backdrop());
                                bean.setFont(colorSelctParams.getColor_font());
                                if (selection<charSequence.length()&&selection>0){
                                    textBeanList.add(clearEditText.getSelectionEnd()-1,bean);
                                    bean=null;
                                }else {
                                    textBeanList.add(bean);
                                    bean=null;
                                }
                            }
                            setTextSpan(charSequence.toString());
                        }else {
                            isPaste = false;
                        }
                    }
                    for (int i=0;i<textBeanList.size();i++){
                        Log.e(TAG, "onTextChanged:字符 "+textBeanList.get(i).getCharacter());
                        Log.e(TAG, "onTextChanged:字体色 "+textBeanList.get(i).getFont());
                        Log.e(TAG, "onTextChanged:背景色 "+textBeanList.get(i).getBackdrop());
                    }
                    if (selection<charSequence.length()){
                        clearEditText.setSelection(selection);
                    }else if (selection==charSequence.length()){
                        clearEditText.setSelection(clearEditText.getText().length());
                    }
                    clearEditText.requestFocus();
                    clearEditText.setCursorVisible(true);
                }
            }else {
                isSet=false;

            }

        }

    };
    private void setTextSpan(String text) {
//        if (text.length()>MAX_COUNT||textBeanList.size()>MAX_COUNT){
//            textBeanList.subList(0,MAX_COUNT);
//            clearEditText.setSelection(MAX_COUNT);
//            Toast.makeText(getApplication(),getResources().getString(R.string.out_of_max),Toast.LENGTH_SHORT).show();
//        }
        Log.e("textBeanList",textBeanList.size()+"");
        if (spannableStringBuilder!=null){
            spannableStringBuilder.clear();
        }
        spannableStringBuilder = new SpannableStringBuilder("");
        if (text.equals("")){

        }else {
            if (textBeanList==null){
                textBeanList = getDefultTextBeanList(text);
            }
            //这里使用字符信息对象集合尺寸做上限，而不是 text长度，理论上是一样的数值，但这样可以避免下面取的时候 超集合最大值(不知道怎么发生的)
            for (int j= 0;j<textBeanList.size();j++){
                if (j<text.length()){
                    SpannableString spannableString = new SpannableString(text.substring(j,j+1));
                    spannableString.setSpan(new BackgroundColorSpan(parseColor(textBeanList.get(j).getBackdrop())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(parseColor(textBeanList.get(j).getFont())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.append(spannableString);
                    spannableString = null;
                }
            }
        }
        isSet=true;
        if (isSupportMarFullColor){
            clearEditText.setText(spannableStringBuilder);
        }else {
            clearEditText.setText(text);
        }
//        etMsg.setSelection(text.length());
    }
    private int parseColor(int position) {
        int color = -1;
        switch (position){
            case 0:
                color =  this.getResources().getColor(R.color.black);
                break;
            case 1:
                color = this.getResources().getColor(R.color.red);
                break;
            case 2:
                color = this.getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = this.getResources().getColor(R.color.dark_green);
                break;
            case 4:
                color = this.getResources().getColor(R.color.cyan);
                break;
            case 5:
                color = this.getResources().getColor(R.color.blove);
                break;
            case 6:
                color = this.getResources().getColor(R.color.purple);
                break;
            case 7:
                color = this.getResources().getColor(R.color.white);
                break;
        }
        return color;
    }
    private boolean textSelected(){
        return clearEditText.getSelectionEnd()-clearEditText.getSelectionStart()>0;
    }
    /***
     * 计算输入字符
     * @param c
     * @return
     */
    private long calculateLength(CharSequence c) {
        double len = 0;
        for (int i = 0; i < c.length(); i++) {
            int tmp = (int) c.charAt(i);
            // 0到127是英文
            if (tmp > 0 && tmp < 127) {
                len += 0.5;
            } else {
                len++;
            }
        }
        return Math.round(len);
    }




    /*********************************************************************************************************************************/



    public static String connected_MAC = "";//当前连接设备的mac地址
    public static  String connected_name = "";
    //    public static SMSGBLEMBLE ble_admin;
//    private MTBLEManager mMTBLEManager;
//    private ThreadPoolExecutor poolExecutor;
    private  final  int CMD_CONNTCTING =1001;
    private  final  int DISMISS_DIALOG =1002;
    private  final  int CMD_SENDING =1003;
    private  final  int CMD_TIMEOUT =1004;//操作超时
    private  final  int CMD_TIMEOUT_RECEIVE =1005;//接收反馈超时
    private  final  int CONNECTED_FAIL =1006;//连接失败-打开通知失败
    private  final  int ACTIVITY_LAUNCHER =1007;//连接失败-打开通知失败
    private  final  int REFRESH_VP =1008;//接收下位机返回数据后刷新界面
    private int sendCmdType = -1;//发送数据类型
    private boolean power = false;//开关机命令
    private boolean isNeedSendData = false;
    @SuppressLint("HandlerLeak")
    public Handler MEHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH_VP:
                    refreshViewByColorModel();
                    break;
                case ACTIVITY_LAUNCHER:
                    rl_launcher.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    include.setVisibility(View.VISIBLE);
                    break;
                case CONNECTED_FAIL:
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
                    sendDataToDevice();
                    break;
            }
        }
    };

    private void showDialog(String message){
        if (pd==null){
            pd = new ProgressDialog(MEditActivity.this);
            pd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(),getString(R.string.cancelOperation),Toast.LENGTH_SHORT).show();
                    MEHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                    serviceBinder.disConnected();
                }
            });
//            pd = ProgressDialog.show(MEditActivity.this, "", message,
//                    true, false);
            pd.setCancelable(true);
//            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialogInterface) {
//                    ble_admin.disConnect();
//                }
//            });
            pd.setMessage(message);
            pd.setCanceledOnTouchOutside(false);
            pd.show();
//            pd.setButton("cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    ble_admin.disConnect();
//                }
//            });
        }else {
            pd.setMessage(message);
            pd.show();
        }
    }
    private boolean isConnectIntime = false;//是否刚连接
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
                    break;
                case BleConnectService.ServiceListener.connected_timeOut:
                    MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                    MEHandler.sendEmptyMessage(CMD_TIMEOUT);
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
                    Log.e(TAG, "onSendData: succeed");
                    if (CommandHelper.dataType_data==sendCmdType){//如果是发数据包  判断是否还有数据需要发送
                        //判断是否还有剩余帧需要发送
                        if (hasSendData < edit_byte.length) {

                            MEHandler.sendEmptyMessage(CMD_SENDING);
                        } else {
                            MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                            //发送成功添加到数据库
                            savaData();
                            MEHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sendsuccess),
                                            Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    }else {
                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
                        //发送成功添加到数据库
                        savaData();
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
                            mStaticDatas.isSupportMarFullColor = false;
                            MEHandler.sendEmptyMessage(REFRESH_VP);
                            isConnectIntime = false;
                            if (isNeedSendData){
                                sendCmdType=CommandHelper.dataType_data;
                                //发送数据
                                edit_byte = ledView.getTextByte(isSupportMarFullColor);
                                hasSendData = 0;
                                MEHandler.sendEmptyMessage(CMD_SENDING);
                            }
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
                        hasSendData=0;
                        sendcmd=null;
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
                    edit_byte = ledView.getTextByte(isSupportMarFullColor);
                    hasSendData = 0;
                    MEHandler.sendEmptyMessage(CMD_SENDING);
                }
            }
        }
    };
    // BLE回调信息
//    private SMSGBLEMBLE.CallBack bleCallback = new SMSGBLEMBLE.CallBack() {
//        //发送数据成功回调
//        @Override
//        public void onWrited() {
//            //发送延时消息，如果3s内没有接收到 回馈 即 视为 发送超时
//            MEHandler.sendEmptyMessageDelayed(CMD_TIMEOUT_RECEIVE,3000);
//        }
//
//        @Override
//        public void onTimeOut(int type) {
//            //连接超时
//            if (type==0){
//                MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                MEHandler.sendEmptyMessage(CMD_TIMEOUT);
////                ble_admin.disConnect();
//
//            }
//        }
//        //接收数据回调
//        @Override
//        public void onDataReturn(String values) {
//            //收到回馈 移除 超时消息
//            MEHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
//            if (values.contains("S")){//成功
//                if (CommandHelper.dataType_data==sendCmdType){//如果是发数据包  判断是否还有数据需要发送
//                    //判断是否还有剩余帧需要发送
//                    if (hasSendData < edit_byte.length) {
//
//                        MEHandler.sendEmptyMessage(CMD_SENDING);
//                    } else {
//                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                        //发送成功添加到数据库
//                        savaData();
//                        MEHandler.post(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sendsuccess),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//
//                        });
//                    }
//                }else {
//                    MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                    //发送成功添加到数据库
//                    savaData();
//                    if (!isConnectIntime){//首次连接 发送开机命令不 提示
//
//                        MEHandler.post(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.successfulOperation),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//
//                        });
//                    }
//                    //如果是刚连接成功 并且成功发送开机命令    再发送数据
//                    if (sendCmdType==CommandHelper.dataType_power&&isConnectIntime&&isNeedSendData){
//                        isConnectIntime = false;
//                        sendCmdType=CommandHelper.dataType_data;
//                        //发送数据
//                        edit_byte = ledView.getTextByte();
//                        hasSendData = 0;
////                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                        MEHandler.sendEmptyMessage(CMD_SENDING);
//                    }
//                }
//            }else if (values.contains("E")){//失败
//                //发送失败
//                erroCount++;
//                Log.e("xxx", "erroCount=" + erroCount);
//                if (erroCount > 3) {
//                    erroCount = 0;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (pd != null) {
//                                pd.dismiss();
//                            }
//                            showYCDialog(getString(R.string.failedOperation));
//                            ble_admin.disConnect();
//                        }
//                    });
//                } else {
//                    //错误次数在3次以内，重发
//                    ble_admin.sendDatas(sendcmd);
//                }
//            }
//        }
//        //打开通知回调
//        @Override
//        public void onNotification(int code) {
//
//            if (code== BluetoothGatt.GATT_SUCCESS){//打开通知成功  连接真正成功
//                //打开通知成功
//                mConnected = true;
//                isConnectIntime = true;
//
//                MEHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        //设置打开开关,发送开机命令
////                        switch_state_iscontrolauto = true;
//                        aSwitch.setChecked(true);
//                        aSwitch.setVisibility(View.VISIBLE);
//                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connecttingSuccessful),
//                                Toast.LENGTH_SHORT).show();
//                        tv_device_name.setText(getTenChar(connected_name));
//                    }
//
//                });
//
//
//            }else {//如果打开通知失败
//                MEHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        MEHandler.sendEmptyMessage(CONNECTED_FAIL);
////                        CommonUtils.toast("open notify failed ");
//                        ble_admin.disConnect();
//                    }
//                });
////                mConnected = false;
////                switch_state_iscontrolauto = true;
////                aSwitch.setChecked(mConnected);
////                tv_device_name.setText(getString(R.string.connectstate));
//
//            }
//        }
//        //断开连接回调
//        @Override
//        public void onDisconnect() {
//            MEHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    CommonUtils.toast(getResources().getString(R.string.connectionCasBeenDisconnected));
////                    MEHandler.removeMessages();
////                    MEHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
//                    mConnected = false;
//                    aSwitch.setVisibility(View.GONE);
//                    hasSendData=0;
//                    sendcmd=null;
//                    switch_state_iscontrolauto = true;
//                    aSwitch.setChecked(mConnected);
//                    isConnectIntime = false;
//                    tv_device_name.setText(getResources().getString(R.string.connectstate));
//                    connected_MAC="";
//                    connected_name = "";
//                    if (pd!=null){
//                        pd.dismiss();
//                    }
//                }
//            });
//        }
//    };



    private void savaData() {
        //发送成功添加到数据库
        UmsResultBean umsResultBean = new UmsResultBean();
        umsResultBean.type = selectedParams.model;
        umsResultBean.color = selectedParams.color;
        umsResultBean.numberIndex = Integer.parseInt(selectedParams.switchValue);
        umsResultBean.body = clearEditText.getText().toString();
//        umsResultBean.speed = seekBarSpeed.getProgress();
//        umsResultBean.bright = seekBarLight.getProgress();
        umsResultBean.beanList = textBeanList;

        umsResultHelper.storeUmsReulst(umsResultBean);
//        refresh();
    }

    private void connectDevice(){
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if ((!serviceBinder.getConnectedStatus())&&(!connected_MAC.equals(""))){//未连接
//
//                }
//            }
//        };
//        poolExecutor.execute(runnable);
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
                    if (sendCmdType==CommandHelper.dataType_data){
                        sendTxtData();
                    }else if (sendCmdType == CommandHelper.dataType_light){
                        sendcmd = ((CmdConts.WRITE_LIGHT + tvLightNum.getText() + "<E>").getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_mesList){
                        sendcmd = ((CmdConts.LOOP_SHOW + waitSendIndexStr + "<E>").getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_power){
                        sendcmd = (turnOnOrOffCmd.getBytes());
                    }else if (sendCmdType == CommandHelper.dataType_speed){
                        sendcmd = ((CmdConts.WRITE_SPEED + tvSpeedNum.getText() + "<E>").getBytes());
                    }
                    serviceBinder.sendData(sendcmd,true);
                }
            }
        };
        mStaticDatas.poolExecutor.execute(ad);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    public List<TextBean> deepCopy(List<TextBean> list) throws IOException, ClassNotFoundException{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(list);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in =new ObjectInputStream(byteIn);
        List dest = (List)in.readObject();
        return dest;
    }
}
