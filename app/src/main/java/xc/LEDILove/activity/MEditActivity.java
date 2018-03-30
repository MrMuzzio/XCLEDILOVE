package xc.LEDILove.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import xc.LEDILove.Bean.Params;
import xc.LEDILove.Bean.PopupItem;
import xc.LEDILove.R;
import xc.LEDILove.adapter.BrightAndModelRecyclerAdapter;
import xc.LEDILove.adapter.ListitemAdapter;
import xc.LEDILove.adapter.PopupwindowAdapter;
import xc.LEDILove.app.MyApplication;
import xc.LEDILove.bluetooth.CommandHelper;
import xc.LEDILove.bluetooth.MTBLEManager;
import xc.LEDILove.bluetooth.SMSGBLEMBLE;
import xc.LEDILove.db.UmsResultBean;
import xc.LEDILove.db.UmsResultHelper;
import xc.LEDILove.font.CmdConts;
import xc.LEDILove.utils.AppVersionUpdate;
import xc.LEDILove.utils.CommonUtils;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.RxCountDown;
import xc.LEDILove.widget.ClearEditText;
import xc.LEDILove.widget.LedView;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;


/***
 * LED编辑界面
 */
public class MEditActivity extends AppCompatActivity implements View.OnClickListener {

    //指定服务
    private static final String DATA_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可写
    private static final String TXD_CHARACT_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可读
    private static final String RXD_CHARACT_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    //APP版本信息获取地址
    private final String service_url = "http://www.xn--led-f00fr20c.com/appVersion/getNewAppVersion?appName=LEDILOVE";
    //region 编辑界面view
    //字体大小
    private Spinner spinnerWordSize;
    //正 斜 粗
    private Spinner spinnerWordType;
    private TextView tvSetting;
    private LedView ledView;
    private ScrollView scrollView;
    //发送
    private Button btnOk;
    private TextView tvTxtNumber;
    //历史数据序号
    private BrightAndModelRecyclerAdapter numberRecyclerAdapter;
    private RecyclerView recyclerViewBrightness;
    //模式  上下移位..
    private RecyclerView recyclerViewModel;
    private BrightAndModelRecyclerAdapter modelRecyclerAdapter;
    private ListView listView;
    private ListitemAdapter listitemAdapter;
    private ImageView imageViewRed;
    private ImageView imageViewYellow;
    private ImageView imageViewGreen;
    private ImageView imageViewCyan;
    private ImageView imageViewBlue;
    private ImageView imageViewPurple;
    private ImageView imageViewWhite;

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
    private boolean mConnected = false;
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
    private byte[] sendcmd;
    private String turnOnOrOffCmd = "";
    //是否获得硬件反馈
    private Boolean isGettingResponse = false;
    //endregion

    private  Button btn_progressbar;
    private TextView tv_head_left;
    private ImageView iv_more;
    private boolean isInMain = true;
    private AppVersionUpdate versionUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        tvSetting = (TextView) findViewById(R.id.tv_set);
        btn_progressbar = (Button) findViewById(R.id.btn_progressbar);
        iv_more = (ImageView) findViewById(R.id.iv_more);
        tv_head_left = (TextView) findViewById(R.id.tv_head_left);
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(iv_more);
//                //创建弹出式菜单对象（最低版本11）
//                     PopupMenu popup = new PopupMenu(MEditActivity.this, iv_more);//第二个参数是绑定的那个view
//                      //获取菜单填充器
//                     MenuInflater inflater = popup.getMenuInflater();
//                     //填充菜单
//                      inflater.inflate(R.menu.menu_mian, popup.getMenu());
//                     //绑定菜单项的点击事件
//                     popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                         @Override
//                         public boolean onMenuItemClick(MenuItem menuItem) {
//
//                             return false;
//                         }
//                     });
//                      popup.show(); //这一行代码不要忘记了
            }
        });
        initBle();
        poolExecutor = new ThreadPoolExecutor(3, 5,
                10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(128));
        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendCmd("+++".getBytes());
                refresh();
                llContent.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                include.setVisibility(View.GONE);
                isInMain = false;
            }
        });
        intial();
        versionUpdate = new AppVersionUpdate();
        //检测版本更新
        checkVersionToUpData();
        mac = getIntent().getStringExtra("mac");
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
                        refresh();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (isInMain){
            Intent intent= new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }else {
            llContent.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            include.setVisibility(View.VISIBLE);
            isInMain = true;
        }
    }

    @Override
    protected void onDestroy() {
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
                connected_MAC = data.getExtras().getString("MAC");
                connected_name = data.getExtras().getString("NAME");

                messgeHandler.sendEmptyMessageDelayed(CMD_CONNTCTING,0);

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
                if (!mConnected) {
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
                edit_byte = ledView.getTextByte();
                hasSendData = 0;
//                pd.show();
                messgeHandler.sendEmptyMessage(CMD_SENDING);
//            case R.id.ll_connect_state:
//                    Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
//                    startActivityForResult(intent, 1004);
//                break;
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
            String ledWidthCmd = ledView.getLEDWidget() + "";
            if (ledWidthCmd.length() == 1) {
                cmdHeader = "BT03120" + selectedParams.wordSize + "000" + ledWidthCmd + "1" + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else if (ledWidthCmd.length() == 2) {
                cmdHeader = "BT03120" + selectedParams.wordSize + "00" + ledWidthCmd + "1" + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else if (ledWidthCmd.length() == 3) {
                cmdHeader = "BT03120" + selectedParams.wordSize + "0" + ledWidthCmd + "1" + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            } else {
                cmdHeader = "BT03120" + selectedParams.wordSize + ledWidthCmd + "1" + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
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

//    private void sendCmd(byte[] sendcmd) {
//        Log.e("sendData->", Helpful.MYBytearrayToString(sendcmd) + " SIZE=" + sendcmd.length);
//        pd.show();
//        //把数据以20byte分隔
//        int pre_index = 0;
//        while (true) {
//            if ((sendcmd.length - pre_index) > 20) {
//                senddatas.add(Helpful.subByte(sendcmd, pre_index, 20));
//                pre_index += 20;
//            } else {
//                senddatas.add(Helpful.subByte(sendcmd, pre_index, sendcmd.length
//                        - pre_index));
//                break;
//            }
//        }
//    }

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
    /***
     * 初始化
     */
    private void intial() {

        umsResultHelper = new UmsResultHelper(this);
        imageViewRed = (ImageView) findViewById(R.id.ivred);
        imageViewYellow = (ImageView) findViewById(R.id.ivyellow);
        imageViewGreen = (ImageView) findViewById(R.id.ivgreen);
        imageViewCyan = (ImageView) findViewById(R.id.ivcyan);
        imageViewBlue = (ImageView) findViewById(R.id.ivblue);
        imageViewPurple = (ImageView) findViewById(R.id.ivpurple);
        imageViewWhite = (ImageView) findViewById(R.id.ivwhite);
        imageViewRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.red);
//                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.red));
            }
        });
        imageViewYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.yellow);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.yellow));
            }
        });
        imageViewGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.green);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.green));
            }
        });
        imageViewCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.cyan);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.cyan));
            }
        });
        imageViewBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.blue);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.blue));
            }
        });
        imageViewPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.purple);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.purple));
            }
        });
        imageViewWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.white);
                ledView.setTextColorChange( selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.white));
            }
        });

        listView = (ListView) findViewById(R.id.list_item);
        spinnerWordSize = (Spinner) findViewById(R.id.spinnerWordSize);
        spinnerWordType = (Spinner) findViewById(R.id.spinnerWordType);

        wordSize = getResources().getStringArray(R.array.wordSize);
        ArrayAdapter<String> wordSizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wordSize);
        spinnerWordSize.setAdapter(wordSizeAdapter);

        wordType = getResources().getStringArray(R.array.wordType);
        ArrayAdapter<String> wordTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wordType);
        spinnerWordType.setAdapter(wordTypeAdapter);

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
                    clearEditText.setText(umsResultBean.body);
                    clearEditText.setTextColor(umsResultBean.color);
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
                isNeedSendData = false;
                if (mConnected){
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if ((mConnected)){//未连接
                                ble_admin.disConnect();
                            }
                        }
                    };
                    poolExecutor.execute(runnable);

                }
                Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                startActivityForResult(intent, 1004);
            }
        });
        tv_device_name = (TextView) findViewById(R.id.tv_device_name);
        tvTxtNumber = (TextView) findViewById(R.id.tvTxtNumber);
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
                if (mConnected) {
                    messgeHandler.sendEmptyMessage(CMD_SENDING);
                } else {
                        Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                        startActivityForResult(intent, 1004);
                        select_return_first = false;
                }
            }
        });
        clearEditText.addTextChangedListener(mTextWatcher);
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
        selectedParams.wordSize = 12;
        //字体大小
        spinnerWordSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedParams.wordSize = Integer.parseInt(wordSize[position]);
                if (!TextUtils.isEmpty(selectedParams.str)) {
                    //重新设置EditText 字符控制
                    setEditTextFilter(selectedParams.wordSize);
                    ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //正斜粗
        selectedParams.wordType = wordType[0];
        spinnerWordType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedParams.wordType = wordType[position];
                if (!TextUtils.isEmpty(selectedParams.str)) {
                    ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        llContent = (LinearLayout) findViewById(R.id.ll_content);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        include = findViewById(R.id.include);

        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        llContent.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        include.setVisibility(View.VISIBLE);
                        isInMain = true;
                    }
                }
        );
        tv_head_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llContent.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                include.setVisibility(View.VISIBLE);
                isInMain = true;
            }
        });
        tvSpeedNum = (TextView) findViewById(R.id.tv_speed_num);
        seekBarSpeed = (IndicatorSeekBar) findViewById(R.id.seekBar);
        //速度
        seekBarSpeed.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar indicatorSeekBar, int progress, float v, boolean b) {
                selectedParams.wordSpeed = (progress + 1) + "";
                tvSpeedNum.setText((progress + 1) + "");
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar indicatorSeekBar, int i, String s, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar, int i) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
                if (mConnected) {
                    sendCmdType = CommandHelper.dataType_speed;
                    messgeHandler.sendEmptyMessage(CMD_SENDING);
                } else {
                    Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                }
            }
        });


        tvLightNum = (TextView) findViewById(R.id.tv_light_num);
        //亮度
        seekBarLight = (IndicatorSeekBar) findViewById(R.id.seekBar2);
        seekBarLight.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar indicatorSeekBar, int progress, float v, boolean b) {
                selectedParams.bright = (progress + 1) + "";
                tvLightNum.setText((progress + 1) + "");
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar indicatorSeekBar, int i, String s, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar, int i) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
                if (mConnected) {
                    sendCmdType = CommandHelper.dataType_light;
                    messgeHandler.sendEmptyMessage(CMD_SENDING);
                } else {
                    Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                }
            }
        });


        btnRunList = (Button) findViewById(R.id.btn_send_run_list);
        //发送列表
        btnRunList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ListitemAdapter.HistoryListItem> historyListItemList = listitemAdapter.getHistoryListItemList();
                waitSendIndexStr = "";
                for (ListitemAdapter.HistoryListItem historyListItem : historyListItemList) {
                    if (historyListItem.isChecked) {
                        waitSendIndexStr = waitSendIndexStr + historyListItem.umsResultBean.numberIndex;
                    }
                }
                if (TextUtils.isEmpty(waitSendIndexStr)) {
                    Toast.makeText(MEditActivity.this, "no choice！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mConnected) {
                    sendCmdType= CommandHelper.dataType_mesList;
                    messgeHandler.sendEmptyMessage(CMD_SENDING);
                } else {
                    Intent intent = new Intent(MEditActivity.this, SelectDeviceActivity.class);
                    startActivityForResult(intent, 1004);
                }
            }
        });

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
                umsResultHelper.storeUmsReulst(umsResultBean);
            }
            umsResultBeanList = umsResultHelper.getUmsReulstList();
        }
        if (null != umsResultBeanList && umsResultBeanList.size() > 0) {

            numberRecyclerAdapter.setSelected(umsResultBeanList.get(0).numberIndex - 1);
            selectedParams.color = umsResultBeanList.get(0).color;
            selectedParams.model = umsResultBeanList.get(0).type;
            clearEditText.setText(umsResultBeanList.get(0).body);
            clearEditText.setTextColor(umsResultBeanList.get(0).color);

            seekBarSpeed.setProgress(umsResultBeanList.get(0).speed-1);
            seekBarLight.setProgress(umsResultBeanList.get(0).bright-1);

            modelRecyclerAdapter.setSelected(Integer.parseInt(umsResultBeanList.get(0).type) - 1);

            //填充List
            listitemAdapter = new ListitemAdapter(this, umsResultBeanList, new ListitemAdapter.MyCollectionRecordListener() {
                @Override
                public void onclick(int selectSize) {
                    if (selectSize > 0) {
                        btnRunList.setEnabled(true);
                        btnRunList.setClickable(true);
                        btnRunList.setBackgroundResource(R.drawable.shape_button);
                    } else {
                        btnRunList.setEnabled(false);
                        btnRunList.setClickable(false);
                        btnRunList.setBackgroundResource(R.drawable.shape_button_unablecheck);
                    }
                }
            });
            listView.setAdapter(listitemAdapter);

        }

    }

    private void setEditTextFilter(int wordSize) {
        if (wordSize==12){
            MAX_COUNT = 250;
        }else if (wordSize == 16){
            MAX_COUNT = 128;
            if (clearEditText.getText().toString().length()>=128){
                clearEditText.setText(clearEditText.getText().toString().substring(0,128));
                clearEditText.setSelection(128);
                Toast.makeText(getApplication(),getResources().getString(R.string.out_of_max),Toast.LENGTH_SHORT).show();
            }
        }
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
        setLeftCount();
    }


    /**
     * 刷新列表
     */
    public void refresh() {
        umsResultBeanList = umsResultHelper.getUmsReulstList();
        listitemAdapter.refresh(umsResultBeanList);
    }
    public static long lastTextChangeTime;
    private Handler handler = new Handler();

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {
        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
        }
    };
    /***
     * 监听文本输入
     */
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

            setLeftCount();

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

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
//            if (isFastDoubleClick()) {
//                return;
//            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }

    };

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

    /***
     * 设置显示输入字符字数
     */
    private void setLeftCount() {
        tvTxtNumber.setText(getInputCount() + "/" + MAX_COUNT + "");
    }

    private long getInputCount() {

        return clearEditText.getText().toString().length();//以字符数显示
    }


    /*********************************************************************************************************************************/



    public static String connected_MAC = "";//当前连接设备的mac地址
    public static  String connected_name = "";
    public static SMSGBLEMBLE ble_admin;
    private MTBLEManager mMTBLEManager;
    private ThreadPoolExecutor poolExecutor;
    private void initBle() {
        mMTBLEManager = MTBLEManager.getInstance();
        mMTBLEManager.init(this);
        ble_admin = new SMSGBLEMBLE(getApplicationContext(),
                mMTBLEManager.mBluetoothManager,
                mMTBLEManager.mBluetoothAdapter);
//        connectThread =new ConnectThread();
        ble_admin.setCallback(bleCallback);
    }
    private  final  int CMD_CONNTCTING =1001;
    private  final  int DISMISS_DIALOG =1002;
    private  final  int CMD_SENDING =1003;
    private  final  int CMD_TIMEOUT =1004;//操作超时
    private  final  int CMD_TIMEOUT_RECEIVE =1005;//接收反馈超时
    private  final  int CONNECTED_FAIL =1006;//连接失败-打开通知失败
    private int sendCmdType = -1;//发送数据类型
    private boolean power = false;//开关机命令
    private boolean isNeedSendData = false;
    @SuppressLint("HandlerLeak")
    public Handler messgeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONNECTED_FAIL:
                    showYCDialog(getResources().getString(R.string.connectedfail));
                    break;
                case CMD_CONNTCTING:
                    showDialog(getResources().getString(R.string.connecting));
                    connectDevice();
                    break;
                case CMD_TIMEOUT:
                    showYCDialog(getResources().getString(R.string.connectionTimedOut));
//                    ble_admin.disConnect();
                    break;
                case CMD_TIMEOUT_RECEIVE:
//                    if (erroCount>3){
                        showYCDialog(getResources().getString(R.string.sendOperationTimeout));
                        messgeHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                        ble_admin.disConnect();
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

                        showDialog(getResources().getString(R.string.dialog_sending));
//                    }
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
                    messgeHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                    messgeHandler.removeMessages(CMD_TIMEOUT);
                    ble_admin.disConnect();
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
    // BLE回调信息
    private SMSGBLEMBLE.CallBack bleCallback = new SMSGBLEMBLE.CallBack() {
        //发送数据成功回调
        @Override
        public void onWrited() {
            //发送延时消息，如果3s内没有接收到 回馈 即 视为 发送超时
            messgeHandler.sendEmptyMessageDelayed(CMD_TIMEOUT_RECEIVE,3000);
        }

        @Override
        public void onTimeOut(int type) {
            //连接超时
            if (type==0){
                messgeHandler.sendEmptyMessage(DISMISS_DIALOG);
                messgeHandler.sendEmptyMessage(CMD_TIMEOUT);
                ble_admin.disConnect();

            }
        }
        //接收数据回调
        @Override
        public void onDataReturn(String values) {
            //收到回馈 移除 超时消息
            messgeHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
            if (values.contains("S")){//成功
                if (CommandHelper.dataType_data==sendCmdType){//如果是发数据包  判断是否还有数据需要发送
                    //判断是否还有剩余帧需要发送
                    if (hasSendData < edit_byte.length) {

                        messgeHandler.sendEmptyMessage(CMD_SENDING);
                    } else {
                        messgeHandler.sendEmptyMessage(DISMISS_DIALOG);
                        //发送成功添加到数据库
                        savaData();
                        messgeHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sendsuccess),
                                        Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                }else {
                    messgeHandler.sendEmptyMessage(DISMISS_DIALOG);
                    //发送成功添加到数据库
                    savaData();
                    if (!isConnectIntime){//首次连接 发送开机命令不 提示

                        messgeHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.successfulOperation),
                                        Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                    //如果是刚连接成功 并且成功发送开机命令    再发送数据
                    if (sendCmdType==CommandHelper.dataType_power&&isConnectIntime&&isNeedSendData){
                        isConnectIntime = false;
                        sendCmdType=CommandHelper.dataType_data;
                            //发送数据
                            edit_byte = ledView.getTextByte();
                            hasSendData = 0;
//                        messgeHandler.sendEmptyMessage(DISMISS_DIALOG);
                        messgeHandler.sendEmptyMessage(CMD_SENDING);
                    }
                }
            }else if (values.contains("E")){//失败
                //发送失败
                erroCount++;
                Log.e("xxx", "erroCount=" + erroCount);
                if (erroCount > 3) {
                    erroCount = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pd != null) {
                                pd.dismiss();
                            }
                            showYCDialog(getString(R.string.failedOperation));
                            ble_admin.disConnect();
                        }
                    });
                } else {
                    //错误次数在3次以内，重发
                    ble_admin.sendDatas(sendcmd);
                }
            }
        }
        //打开通知回调
        @Override
        public void onNotification(int code) {

            if (code== BluetoothGatt.GATT_SUCCESS){//打开通知成功  连接真正成功
                //打开通知成功
                mConnected = true;
                isConnectIntime = true;

                messgeHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //设置打开开关,发送开机命令
//                        switch_state_iscontrolauto = true;
                        aSwitch.setChecked(true);
                        aSwitch.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connecttingSuccessful),
                                Toast.LENGTH_SHORT).show();
                        tv_device_name.setText(getTenChar(connected_name));
                    }

                });


            }else {//如果打开通知失败
                messgeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        messgeHandler.sendEmptyMessage(CONNECTED_FAIL);
//                        CommonUtils.toast("open notify failed ");
                        ble_admin.disConnect();
                    }
                });
//                mConnected = false;
//                switch_state_iscontrolauto = true;
//                aSwitch.setChecked(mConnected);
//                tv_device_name.setText(getString(R.string.connectstate));

            }
        }
        //断开连接回调
        @Override
        public void onDisconnect() {
            messgeHandler.post(new Runnable() {
                @Override
                public void run() {
                    CommonUtils.toast(getResources().getString(R.string.connectionCasBeenDisconnected));
//                    messgeHandler.removeMessages();
//                    messgeHandler.removeMessages(CMD_TIMEOUT_RECEIVE);
                    mConnected = false;
                    aSwitch.setVisibility(View.GONE);
                    hasSendData=0;
                    sendcmd=null;
                    switch_state_iscontrolauto = true;
                    aSwitch.setChecked(mConnected);
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
    };

    private void savaData() {
        //发送成功添加到数据库
        UmsResultBean umsResultBean = new UmsResultBean();
        umsResultBean.type = selectedParams.model;
        umsResultBean.color = selectedParams.color;
        umsResultBean.numberIndex = Integer.parseInt(selectedParams.switchValue);
        umsResultBean.body = clearEditText.getText().toString();
        umsResultBean.speed = seekBarSpeed.getProgress();
        umsResultBean.bright = seekBarLight.getProgress();
        umsResultHelper.storeUmsReulst(umsResultBean);
    }

    private void connectDevice(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if ((!mConnected)&&(!connected_MAC.equals(""))){//未连接
                    mConnected = ble_admin.connect(connected_MAC, 8000, 1);//10s 超时  重试两次

                }
            }
        };
        poolExecutor.execute(runnable);
    }
    public   void sendDataToDevice(){

        //发送数据
        Runnable ad = new Runnable() {
            @Override
            public void run() {
                if ( mConnected) {
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
                    ble_admin.sendDatas(sendcmd);
                }
            }
        };
        poolExecutor.execute(ad);
    }

}
