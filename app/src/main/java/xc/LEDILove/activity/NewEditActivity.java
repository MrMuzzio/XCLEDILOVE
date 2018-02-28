package xc.LEDILove.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.internal.Utils;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import xc.LEDILove.R;
import xc.LEDILove.adapter.BrightAndModelRecyclerAdapter;
import xc.LEDILove.adapter.ListitemAdapter;
import xc.LEDILove.app.MyApplication;
import xc.LEDILove.db.UmsResultBean;
import xc.LEDILove.db.UmsResultHelper;
import xc.LEDILove.font.CmdConts;
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
public class NewEditActivity extends AppCompatActivity implements View.OnClickListener {

    //指定服务
    private static final String DATA_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可写
    private static final String TXD_CHARACT_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    //指定服务下的 可读
    private static final String RXD_CHARACT_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";

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

    private class Params {
        //文本
        public String str;
        //字体大小
        public int wordSize;
        //速度
        public String wordSpeed;

        public String wordType;
        public String wordEffect;
        public String languageType;
        public String bright = "1";
        //类型 上移 下移。。。
        public String model = "1";
        //序号
        public String switchValue = "1";
        //颜色
        public int color;
    }
    private boolean isInMain = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        tvSetting = (TextView) findViewById(R.id.tv_set);
        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                llContent.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                include.setVisibility(View.GONE);
                isInMain = false;
            }
        });

        //每隔50毫秒进行数据发送
        subscribe_auto = Observable.interval(50, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (0 == senddatas.size()) {
                            return;
                        }
                        Log.e("xxx", Helpful.MYBytearrayToString(senddatas.get(0)));
                        MyApplication.getInstance().mClient.write(mac, UUID.fromString(DATA_SERVICE_UUID), UUID.fromString(TXD_CHARACT_UUID),
                                senddatas.get(0), mWriteRsp);
                        senddatas.remove(0);


                        //命令发送完毕，开始计时，5s无回馈即视为失败
                        if (0 == senddatas.size()) {
                            if (subscriptionRxResponseStutas != null) {
                                subscriptionRxResponseStutas.unsubscribe();
                            }
                            subscriptionRxResponseStutas = RxCountDown.countdown(5)
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Integer>() {
                                        @Override
                                        public void call(Integer integer) {
                                            if (integer == 0) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (!isGettingResponse) {
                                                            pd.dismiss();
                                                            Log.e("超时>>>>","超时");
                                                            showYCDialog(getString(R.string.sendOperationTimeout));
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                        //TODO 测试代码
//                        if (0 == senddatas.size()) {
//                            if (mNotifyRsp != null ) {
//                                mNotifyRsp.onNotify(UUID.fromString(DATA_SERVICE_UUID), UUID.fromString(RXD_CHARACT_UUID), "S".getBytes());
//                            }
//                            return;
//                        }

                    }
                });
        intial();
        mac = getIntent().getStringExtra("mac");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().mClient.registerConnectStatusListener(mac, mConnectStatusListener);
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
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (null != subscribe_auto) {
            subscribe_auto.unsubscribe();
        }
        MyApplication.getInstance().mClient.disconnect(mac);
        MyApplication.getInstance().mClient.unregisterConnectStatusListener(mac, mConnectStatusListener);
        super.onDestroy();

    }
    private boolean only_connect = true;
    private boolean select_return_first = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            connect(data, requestCode);
            tv_device_name.setText(getTenChar(data.getExtras().getString("name")));
        }else {
            select_return_first = true;
            aSwitch.setChecked(false);
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
                this.requestCode = 1004;
                if (TextUtils.isEmpty(clearEditText.getText().toString())) {
                    showYCDialog(getString(R.string.pleaseEnterText));
                    return;
                }
                if (!mConnected) {
                    Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
                    startActivityForResult(intent, 1004);
                    return;
                }
                edit_byte = ledView.getTextByte();
                hasSendData = 0;
                pd.show();
                sendTxtData();
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

        sendCmd(sendcmd);
    }


    private List<byte[]> senddatas = new ArrayList<byte[]>();

    private void sendCmd(byte[] sendcmd) {
        Log.e("sendData->", Helpful.MYBytearrayToString(sendcmd) + " SIZE=" + sendcmd.length);
        pd.show();
        //把数据以20byte分隔
        int pre_index = 0;
        while (true) {
            if ((sendcmd.length - pre_index) > 20) {
                senddatas.add(Helpful.subByte(sendcmd, pre_index, 20));
                pre_index += 20;
            } else {
                senddatas.add(Helpful.subByte(sendcmd, pre_index, sendcmd.length
                        - pre_index));
                break;
            }
        }
    }

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
                    new AlertDialog.Builder(NewEditActivity.this)
                            .setTitle(getString(R.string.hint))
                            .setMessage(erroMsg)
//                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            })
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create().show();
                }
            });

        }
    }

    private int requestCode = -1;

    /***
     * 开启链接蓝牙
     * @param data
     */
    private void connect(Intent data, final int requestCode) {

        this.requestCode = requestCode;
        if (data != null) {
            if (TextUtils.isEmpty(mac)) {
                mac = data.getStringExtra("mac");
                if (TextUtils.isEmpty(mac)) {
                    CommonUtils.toast(" mac is empty !");
                    return;
                }
            }
            if (pd == null) {
                pd = ProgressDialog.show(NewEditActivity.this, "", "Please Waiting...",
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
            //打开通知
            MyApplication.getInstance().mClient.notify(mac, UUID.fromString(DATA_SERVICE_UUID), UUID.fromString(RXD_CHARACT_UUID), mNotifyRsp);
            isInterrupt=false;
        } else {
            CommonUtils.toast(" mac is empty! ");
        }
    }

    private boolean isInterrupt = false;
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
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.red));
            }
        });
        imageViewYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.yellow);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.yellow));
            }
        });
        imageViewGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.green);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.green));
            }
        });
        imageViewCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.cyan);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.cyan));
            }
        });
        imageViewBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.blue);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.blue));
            }
        });
        imageViewPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.purple);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
                clearEditText.setTextColor(getResources().getColor(R.color.purple));
            }
        });
        imageViewWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedParams.color = getResources().getColor(R.color.white);
                ledView.setMatrixTextWithColor(selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color);
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
//                if (null != subscribe_auto) {
//                    subscribe_auto.unsubscribe();
//                }
//                MyApplication.getInstance().mClient.clearRequest(mac,0);
//                MyApplication.getInstance().mClient.unnotify(mac, UUID.fromString(DATA_SERVICE_UUID), UUID.fromString(RXD_CHARACT_UUID), new BleUnnotifyResponse(){
///
//                    @Override
//                    public void onResponse(int code) {
//
//                    }
//                });
//                MyApplication.getInstance().mClient.disconnect(mac);
//                isInterrupt = true;
//                MyApplication.getInstance().mClient.unregisterConnectStatusListener(mac, mConnectStatusListener);
//                Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
//                startActivityForResult(intent, 1004);
                if (mConnected) {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(NewEditActivity.this);
                    builder.setMessage(getApplicationContext().getResources().getString(R.string.dialog_reset));
                    builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent reset = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            reset.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(reset);
//                            overridePendingTransition(R.anim.activity_top_to_bottom, 0);
                        }
                    });
                    builder.show();
                }

            }
        });
        tv_device_name = (TextView) findViewById(R.id.tv_device_name);
        tvTxtNumber = (TextView) findViewById(R.id.tvTxtNumber);
        clearEditText = (ClearEditText) findViewById(R.id.clearEditText);
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250)});//250字符限制
        clearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });
        aSwitch = (Switch) findViewById(R.id.switchTrack);
        //开关
//        aSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(),"onclick",Toast.LENGTH_LONG).show();
//            }
//        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnOnOrOffCmd = CmdConts.ON_LED;
                } else {
                    turnOnOrOffCmd = CmdConts.OFF_LED;
                }
                requestCode = 1005;
                if (mConnected) {
                    sendCmd(turnOnOrOffCmd.getBytes());
                } else {
//                    if (!select_return_first){
                        Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
                        startActivityForResult(intent, 1005);
                        select_return_first = false;
//                    }
                }
            }
        });
        clearEditText.addTextChangedListener(mTextWatcher);
        frameLayout = (FrameLayout) findViewById(R.id.ledView);
        ledView = new LedView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ledView.setLayoutParams(params);
        frameLayout.addView(ledView);

        selectedParams.wordSize = 12;
        //字体大小
        spinnerWordSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedParams.wordSize = Integer.parseInt(wordSize[position]);
                if (!TextUtils.isEmpty(selectedParams.str)) {
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
                    requestCode = 1001;
                    sendCmd((CmdConts.WRITE_SPEED + tvSpeedNum.getText() + "<E>").getBytes());
                } else {
                    Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
                    startActivityForResult(intent, 1001);
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
                    requestCode = 1002;
                    sendCmd((CmdConts.WRITE_LIGHT + tvLightNum.getText() + "<E>").getBytes());
                } else {
                    Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
                    startActivityForResult(intent, 1002);
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
                    Toast.makeText(NewEditActivity.this, "no choice！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mConnected) {
                    requestCode = 1003;
                    sendCmd((CmdConts.LOOP_SHOW + waitSendIndexStr + "<E>").getBytes());
                } else {
                    Intent intent = new Intent(NewEditActivity.this, NewScanActivity.class);
                    startActivityForResult(intent, 1003);
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
            handler.postDelayed(delayRun, 400);
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

//        return calculateLength(clearEditText.getText().toString());//以字节数显示
        return clearEditText.getText().toString().length();//以字符数显示
    }


    /*********************************************************************************************************************************/


    /***
     * 蓝牙链接状态 监听
     */
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (pd != null) {
                pd.dismiss();
            }
            if (status == STATUS_CONNECTED) {
                mConnected = true;
                aSwitch.setChecked(mConnected);
            } else {
                mConnected = false;
                aSwitch.setChecked(mConnected);
                tv_device_name.setText(getString(R.string.connectstate));
//                CommonUtils.toast("disconnected");
//                MyApplication.getInstance().mClient.disconnect(mac);
            }
        }
    };


    /**
     * 监听通知
     */
    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {

        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {

            Log.e("xxx", "onNotify = " + new String(value));

            //接收指定服务
            if (service.equals(UUID.fromString(DATA_SERVICE_UUID)) && character.equals(UUID.fromString(RXD_CHARACT_UUID))) {

                isGettingResponse = true;

                if (mConnected) {

                    if (requestCode == 1004) {
                        //是发送文本，需要判断是否是大数据操作
                        //发送成功
                        if (new String(value).toUpperCase().contains("S")) {

                            //判断是否还需要发送
                            if (hasSendData < edit_byte.length) {
                                sendTxtData();
                            } else {
                                pd.dismiss();
                                CommonUtils.toast("control right");
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
                        } else {
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
                                    }
                                });
                            } else {
                                //错误次数在3次以内，重发
                                sendCmd(sendcmd);
                            }
                        }

                    } else {

                        //普通操作
                        if (pd != null) {
                            pd.dismiss();
                        }
                        if (new String(value).toUpperCase().equals("E")) {
                            CommonUtils.toast("control failed");
                        } else {
                            //发送成功添加到数据库
                            UmsResultBean umsResultBean = new UmsResultBean();
                            umsResultBean.type = selectedParams.model;
                            umsResultBean.color = selectedParams.color;
                            umsResultBean.numberIndex = Integer.parseInt(selectedParams.switchValue);
                            umsResultBean.body = clearEditText.getText().toString();
                            umsResultBean.speed = seekBarSpeed.getProgress();
                            umsResultBean.bright = seekBarLight.getProgress();
                            umsResultHelper.storeUmsReulst(umsResultBean);
                            CommonUtils.toast("control right");
                        }
                    }

                } else {
                    if (pd != null) {
                        pd.dismiss();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showYCDialog(getString(R.string.connectionCasBeenDisconnected));
                        }
                    });
                    //连接断开
                    CommonUtils.toast("Disconnect !");
                }
            }
        }

        @Override
        public void onResponse(int code) {
            if (pd != null) {
                pd.dismiss();
            }
            if (code == REQUEST_SUCCESS) {
                //打开通知成功
                CommonUtils.toast("open notify success ");
                mConnected = true;
                aSwitch.setChecked(mConnected);
                //连接成功
                if (requestCode == 1001) {
                    //速度
                    sendCmd((CmdConts.WRITE_SPEED + tvSpeedNum.getText() + "<E>").getBytes());
                } else if (requestCode == 1002) {
                    //亮度
                    sendCmd((CmdConts.WRITE_LIGHT + tvLightNum.getText() + "<E>").getBytes());
                } else if (requestCode == 1003) {
                    //发送列表
                    sendCmd((CmdConts.LOOP_SHOW + waitSendIndexStr + "<E>").getBytes());
                } else if (requestCode == 1004) {
                    //发送数据
                    edit_byte = ledView.getTextByte();
                    hasSendData = 0;
                    sendTxtData();
                } else if (requestCode == 1005) {
                    //发送开关
                    sendCmd(turnOnOrOffCmd.getBytes());
                }
            } else {
                CommonUtils.toast("open notify failed ");

                mConnected = false;
                aSwitch.setChecked(mConnected);
            }
        }
    };

    /***
     * 写数据监听
     */
    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
            Log.e("xxx", "BleWriteResponse code=" + code);
        }
    };


}
