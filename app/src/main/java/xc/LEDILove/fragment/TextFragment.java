package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import rx.Subscription;
import xc.LEDILove.Bean.Params;
import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.Bean.TextColorSelctParams;
import xc.LEDILove.R;
import xc.LEDILove.activity.MEditActivity;
import xc.LEDILove.activity.MainActivity;
import xc.LEDILove.activity.OtherMessageActivity;
import xc.LEDILove.activity.SelectDeviceActivity;
import xc.LEDILove.adapter.BrightAndModelRecyclerAdapter;
import xc.LEDILove.adapter.SelectorViewPagerAdapter;
import xc.LEDILove.bluetooth.CommandHelper;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.db.UmsResultBean;
import xc.LEDILove.db.UmsResultHelper;
import xc.LEDILove.font.CmdConts;
import xc.LEDILove.service.BleConnectService;
import xc.LEDILove.utils.AppVersionUpdate;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.LangUtils;
import xc.LEDILove.utils.TimonLibary;
import xc.LEDILove.widget.ClearEditText;
import xc.LEDILove.widget.LedView;

/**
 * Created by xcgd on 2018/5/14.
 */

public class TextFragment extends Fragment{
    private Context context;
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
    private SelectorViewPagerAdapter viewPagerAdapter;
    private android.support.v4.app.FragmentManager fragmentManager;
    private List<Fragment> fragments;
    private ColorSelectFragment colorSelectFragment;
    private FontSelectFragment fontSelectFragment;
    private String TAG = TextFragment.class.getSimpleName();
    private BleConnectService.Mybinder serviceBinder;

    private ImageView mIvGuideRedPoint ;
    private RelativeLayout rl_launcher;
    private TextView tv_version;
    private String vername;
    //    private StaticDatas mStaticDatas;
    private TextView tv_navigation_index;
    private ImageView iv_navigation_left;
    private ImageView iv_navigation_right;
    private RadioGroup rg_font_color_select;
    private RadioButton rb_font;
    private RadioButton rb_color;
    /**
     * 多彩标志位  是否支持多彩
     * */
//    private boolean isSupportMarFullColor = true;
    /**
     * 多彩下 是否自动颜色
     * */
    private boolean isAutoColor = false;
    private String head = "BT03120";
    private String dataType = "1";
    private int requestCode = -1;

    private boolean isInterrupt = false;
    private boolean isComplete = false;
    private ColorSelectFragment.ColorCallback colorCallback;
    private FontSelectFragment.FontCallback fontCallback;
    private TextColorSelctParams colorSelctParams;
    private ServiceConnection serviceConnection;
    private LinearLayout linLay_guide_pointContainer;
    private Button btn_fragment_text_send;
    private int mPointWidth;
    private int defaultFontColor = 1;
    private int defaultBGColor =0;
    private boolean needAutoConnected = true;
    private boolean isSet = false;//是否是设置Text 引发的回调
    private  boolean isPaste = false;//是否粘贴事件
    private SpannableStringBuilder spannableStringBuilder;
    private List<TextBean> textBeanList;
    private List<TextBean> textBeanList_backup;
    private boolean isCreated = false;
    private boolean isLensImage = false;
    private int wordEffectType = 0;
    private TextFragmentCallback textFragmentCallback;
    public TextFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public TextFragment(Context context){
        this.context =context;
    }

    @Override
    public void onResume() {
//        Log.e(TAG, "onResume: ");
//        if (!isCreated){
//            viewEvent();
//            intial();
//            //读数据库
//            initSQLData();
//            initViewData();
//            isCreated = false;
//        }
        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_edit,container,false);
        findView(view);
        viewEvent();
        intial();
        //读数据库
        initSQLData();
        initViewData();
        isCreated = true;
        return view;
    }
    public void setTextFragmentCallback (TextFragmentCallback textFragmentCallback){
        this.textFragmentCallback = textFragmentCallback;
    }
    private void viewEvent() {
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
        btn_fragment_text_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textFragmentCallback.onSendRequire(getEdit_byte());
            }
        });
    }

    public void refreshBySize() {
        fontSelectFragment.refresh();
    }

    public interface TextFragmentCallback{
        void onSendRequire(byte[] cmd);
    }
    private void initViewData(){
        textBeanList = new ArrayList<>();
        textBeanList_backup = new ArrayList<>();
        rg_font_color_select.check(R.id.rb_font);
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
        refreshViewPager();
    }
    private void findView(View view) {
        btn_fragment_text_send = (Button)view.findViewById(R.id.btn_fragment_text_send);
        mIvGuideRedPoint = (ImageView)view.findViewById(R.id.iv_guide_redPoint);
        linLay_guide_pointContainer = (LinearLayout) view.findViewById(R.id.linLay_guide_pointContainer);
        btn_progressbar = (Button) view.findViewById(R.id.btn_progressbar);
        iv_more = (ImageView) view.findViewById(R.id.iv_more);
        tv_head_left = (TextView) view.findViewById(R.id.tv_head_left);
        vp_selector = (ViewPager) view.findViewById(R.id.vp_selector);
        tv_navigation_index = (TextView) view.findViewById(R.id.tv_navigation_index);
        iv_navigation_left = (ImageView) view.findViewById(R.id.iv_navigation_left);
        iv_navigation_right = (ImageView) view.findViewById(R.id.iv_navigation_right);
        rg_font_color_select = (RadioGroup) view.findViewById(R.id.rg_font_color_select);
        rb_font = (RadioButton) view.findViewById(R.id.rb_font);
        rb_color = (RadioButton) view.findViewById(R.id.rb_color);
        rg_font_color_select.check(R.id.rb_font);
        rb_font.setTextColor(getResources().getColor(R.color.white));
        rb_color.setTextColor(getResources().getColor(R.color.loading_bar_text_color));

//        if (mStaticDatas.isSupportMarFullColor){
//            isSupportMarFullColor = true;
//        }else {
//            isSupportMarFullColor = false;
//        }

        frameLayout = (FrameLayout) view.findViewById(R.id.ledView);
        recyclerViewModel = (RecyclerView) view.findViewById(R.id.recyclerview_model);
        clearEditText = (ClearEditText) view.findViewById(R.id.clearEditText);
        recyclerViewBrightness = (RecyclerView) view.findViewById(R.id.recyclerview_brightness);
    }
    //    private void pressSend() {
//        isNeedSendData = true;
//        sendCmdType = CommandHelper.dataType_data;
//        //如果输入为空 返回
//        if (TextUtils.isEmpty(clearEditText.getText().toString())) {
//            showYCDialog(getString(R.string.pleaseEnterText));
//            return;
//        }
//
//        //数据未处理完成
//        if (!isComplete){
//
//            showYCDialog(getString(R.string.please_wait));
//            return;
//        }
//        edit_byte = ledView.getTextByte(isSupportMarFullColor);
//        hasSendData = 0;
////                pd.show();
//        MEHandler.sendEmptyMessage(CMD_SENDING);
//    }
    public byte[] getEdit_byte(){
        if (!isComplete){
            return null;
        }
        edit_byte = ledView.getTextByte(StaticDatas.isSupportMarFullColor);
        if (edit_byte.length==0){
            return null;
        }

        hasSendData = 0;
        List<byte[]> bytes = new ArrayList<>();
        while (hasSendData<edit_byte.length){
            bytes.add(getFrameData());
        }
        int length=0;
        for (int i=0;i<bytes.size();i++){
            length= bytes.get(i).length+length;
        }
        byte[] result = new byte[length];
        int hasCat = 0;
        Log.e(TAG, "getEdit_byte: "+result.length);
        for (int i=0;i<bytes.size();i++){
            Helpful.catByte(bytes.get(i), 0, result, hasCat);
            hasCat = hasCat+bytes.get(i).length;
            Log.e(TAG, "getEdit_byte: "+Helpful.MYBytearrayToString(result));
        }
        return result;
    }


    /***
     * 执行发送数据事件
     */
    private byte[] getFrameData() {
        String cmdHeader;
        //命令头
        byte[] cmdHeaderbyte ;
        byte[] waitSendData;
        if (hasSendData == 0) {
            cmdHeaderbyte = new byte[22];
            //需要转换为16进制
            String ledWidthCmd = ledView.getLEDWidget(StaticDatas.isSupportMarFullColor) + "";
            Log.e(TAG, "sendTxtData: dataType>>>>"+dataType);
            if (ledWidthCmd.length() == 1) {
                cmdHeader = head + selectedParams.wordSize + "000" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model ;
            } else if (ledWidthCmd.length() == 2) {
                cmdHeader = head + selectedParams.wordSize + "00" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model ;
            } else if (ledWidthCmd.length() == 3) {
                cmdHeader = head + selectedParams.wordSize + "0" + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model ;
            } else {
                cmdHeader = head+ selectedParams.wordSize + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model ;
            }
            Helpful.catByte(cmdHeader.getBytes(), 0, cmdHeaderbyte, 0);
            cmdHeaderbyte[16] = 0x00;
            cmdHeaderbyte[17] = 0x00;
            cmdHeaderbyte[18] = 0x00;
            cmdHeaderbyte[19] = 0x00;
            cmdHeaderbyte[20] = 0x30;
            cmdHeaderbyte[21] = 0x30;
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
                cmdHeaderbyte = new byte[2];
            } else {
                cmdHeader = "0" + header;
                cmdHeaderbyte = new byte[2];
            }
            Helpful.catByte(cmdHeader.getBytes(), 0, cmdHeaderbyte, 0);
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



        //拼接命令头和数据
        sendcmd = new byte[sendData.length + cmdHeaderbyte.length];

        Helpful.catByte(cmdHeaderbyte, 0, sendcmd, 0);
        Helpful.catByte(sendData, 0, sendcmd, cmdHeaderbyte.length);
        return sendcmd;
//        sendCmd(sendcmd);
    }
    public void setBinder(BleConnectService.Mybinder serviceBinder){
        this.serviceBinder = serviceBinder;
    }
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
                }
            }

            @Override
            public void OnColorModelChange(boolean isAuto) {
                if (StaticDatas.isSupportMarFullColor){
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
                setWordSize(size);
                Log.e(TAG, "onWordSizeChange:"+size );

            }

            @Override
            public void onWordTypeChange(String type) {
                selectedParams.wordType = type;
                if (!TextUtils.isEmpty(selectedParams.str)) {

                    ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType,textBeanList);
                }
            }

            @Override
            public void onFontEffectChange(String effect) {
                wordEffectType = Integer.parseInt(effect);
//                if (Integer.parseInt(effect)==1){
//                    isLensImage = true;
//                    //刷新页面
//                }else {
//                    isLensImage=false;
//                }
                handler.postDelayed(delayRun, 100);
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
            }
        };
        colorSelectFragment = new ColorSelectFragment(context);
        colorSelectFragment.setColorCallback(colorCallback);
        fontSelectFragment = new FontSelectFragment(context);
        fontSelectFragment.setFontCallback(fontCallback);
        fragments = new ArrayList<>();
        fragments.add(fontSelectFragment);
        fragments.add(colorSelectFragment);
        fragmentManager = getChildFragmentManager();
        umsResultHelper = new UmsResultHelper(context);
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


        recyclerViewBrightness.setLayoutManager(new GridLayoutManager(context, 4));
        recyclerViewBrightness.setItemAnimator(new DefaultItemAnimator());

        //序号
        numberRecyclerAdapter = new BrightAndModelRecyclerAdapter(context, new BrightAndModelRecyclerAdapter.MyCollectionRecordListener() {
            @Override
            public void onclick(String keyName) {

                selectedParams.switchValue = keyName;
                //响应点击序号事件，点击哪一个显示哪一个的历史数据，没有就不显示
                UmsResultBean umsResultBean = umsResultHelper.getUmsReulsByIndex(Integer.parseInt(keyName));
                if (null != umsResultBean) {
                    selectedParams.color = umsResultBean.color;
                    selectedParams.model = umsResultBean.type;
                    textBeanList = umsResultBean.beanList;
                    if (StaticDatas.isSupportMarFullColor){

                        if (isAutoColor){
                            setTextAutoColor(umsResultBean.body);
                        }else {
                            setTextSpan(umsResultBean.body);
                        }
                    }else {
                        textBeanList = getDefultTextBeanList(umsResultBean.body);
                        setTextSpan(umsResultBean.body);
                    }
                    modelRecyclerAdapter.setSelected(Integer.parseInt(umsResultBean.type) - 1);
                }
            }
        });
        bright = getResources().getStringArray(R.array.bright);
        numberRecyclerAdapter.addList(Arrays.asList(bright));
        recyclerViewBrightness.setAdapter(numberRecyclerAdapter);


        recyclerViewModel.setLayoutManager(new GridLayoutManager(context, 4));
        recyclerViewModel.setItemAnimator(new DefaultItemAnimator());
        //特效
        modelRecyclerAdapter = new BrightAndModelRecyclerAdapter(context, new BrightAndModelRecyclerAdapter.MyCollectionRecordListener() {
            @Override
            public void onclick(String keyName) {
                selectedParams.model = keyName;
            }
        });
        model = getResources().getStringArray(R.array.model);
        modelRecyclerAdapter.addList(Arrays.asList(model));
        recyclerViewModel.setAdapter(modelRecyclerAdapter);


        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
        clearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });
        clearEditText.addTextChangedListener(mTextWatcher);
        clearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if (i== EditorInfo.IME_ACTION_SEND||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    Log.e(TAG, "onEditorAction: search");
                    TimonLibary.hideKeyboard(clearEditText);
                    pressSend();
                    return true;
                }
                return false;
            }
        });
        clearEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent arg0) {
                Log.e(TAG, "onKey: >>>>>>>>>>>>>>"+arg0.getCharacters());
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

        ledView = new LedView(context);
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

    private void setWordSize(String size) {
        selectedParams.wordSize = Integer.parseInt(size);
        if (!TextUtils.isEmpty(selectedParams.str)) {
            //重新设置EditText 字符控制
            setEditTextFilter(selectedParams.wordSize);
            if (fontSelectFragment!=null){
                fontSelectFragment.setCharCount(textBeanList.size() + "/"+MAX_COUNT);
//                        Log.e(TAG, "onTextChanged: "+MAX_COUNT);
            }
            ledView.setMatrixText(selectedParams.str, selectedParams.wordSize, selectedParams.wordType,textBeanList);
        }
    }

    public static long lastTextChangeTime;
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
    public void savaData() {
        //发送成功添加到数据库
        UmsResultBean umsResultBean = new UmsResultBean();
        umsResultBean.type = selectedParams.model;
        umsResultBean.color = selectedParams.color;
        umsResultBean.numberIndex = Integer.parseInt(selectedParams.switchValue);
        umsResultBean.body = clearEditText.getText().toString();
        umsResultBean.beanList = textBeanList;
        umsResultHelper.storeUmsReulst(umsResultBean);
    }
    private Handler handler = new Handler();
    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {
        @Override
        public void run() {
            //
            Log.e("delayRun: ", textBeanList.size()+"");
            ledView.setMatrixTextWithColor(StaticDatas.isSupportMarFullColor,wordEffectType,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        boolean running =false;
        int editStart;
        int editEnd;
        int selection = 0;
        int charCount = 0;
        String contxt ="";
        String lastContext = "";
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
            String text = s.toString();
            //增加running判断可防止堆栈溢出
            if (!running){
                running = true;
                //非代码设置 产生的回调
                if (!isSet){
                    Log.e(TAG, "afterTextChanged: "+text);
                    //韩文/日文 无法输入无法触发字符拼接解决 方案
                    // 触发的条件是：光标之前的字符（也就是正处于拼接状态的字符）不能经过任何处理
                    // 包括设置spanner 或者editable 替换
                    if (LangUtils.isJapanese(text) || LangUtils.isKorean(text)){//如果含日文/韩文

                        selection = clearEditText.getSelectionEnd();
                        Log.e(TAG, "afterTextChanged: selection>>>"+selection );
                        if (text.length()>0&&selection>0){//判空
                            if (selection==text.length()){//光标处于字符末端
                                /**
                                 * 这里处理在onTextChanged回调中已经将每次输入的字符加入到textBeanList中 ，
                                 * 但是英文韩文/日文的字符拼接 最终需要的只是最后拼接的字符 之前的字符数据需要移除掉
                                 * 判断条件：EditText中字符长度小于 textBeanList的长度
                                 * */
                                if (text.length()<textBeanList.size()&&!isPaste){
                                    while (text.length()<textBeanList.size()){
                                        /**
                                         * 移除多余的数据
                                         * */
                                        textBeanList.remove(textBeanList.size()-1-1);
                                    }
                                }
                                /**
                                 * 这里设置EditText中字符颜色 但是不能包括光标前的字符
                                 * */
                                s.replace(0,text.length()-1,getTextSpan(text.substring(0,text.length()-1)));
                            }else if (selection>1&&selection!=text.length()&&!isPaste){//光标处于中间位置
                                if (text.length()<textBeanList.size()){
                                    while (text.length()<textBeanList.size()){
                                        textBeanList.remove(selection-1);
                                    }
                                }
                                s.replace(0,selection-1,getTextSpan(text.substring(0,selection-1)));
                                s.replace(selection,text.length(),getTextSpan(text.substring(selection,text.length())));
                            }else if (selection==1&&!isPaste){//光标处于位置1  这里单独拿出来 主要是 防止 selection-1=0，replace (0,0)会报错
                                if (text.length()<textBeanList.size()){
                                    while (text.length()<textBeanList.size()){
                                        textBeanList.remove(0);
                                    }
                                }
                                s.replace(1,text.length(),getTextSpan(text.substring(1,text.length())));
                            }
                        }
                    }else {//非韩文/日文  直接设置颜色
                        s.replace(0,text.length(),getTextSpan(text));

                    }
//
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
                running = false;
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        /**
         * 对多彩字符的设置 ，原则上 在onTextChanged 回调中记录颜色数据
         * 然后在afterTextChanged通过改变Editable属性增加颜色
         *
         * **/
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before,
                                  int count) {

            if (fontSelectFragment!=null){//更新字符计数
                fontSelectFragment.setCharCount(charSequence.length() + "/"+MAX_COUNT);
            }
            char[] chars = charSequence.toString().toCharArray();
            if (!isSet&&!running){
                Log.e(TAG, "onTextChanged: running"+charSequence);
                running=true;
                //没选
                if (!textSelected()){
                    selection = clearEditText.getSelectionEnd();
                    if (isAutoColor){
                        getTextBeanListAutoColor(charSequence.toString());
                    }else {
                        if (!isPaste){
                            for (int i=0;i<count;i++){
                                TextBean bean = new TextBean();
                                bean.setCharacter(chars[start+i]);
                                bean.setBackdrop(colorSelctParams.getColor_backdrop());
                                bean.setFont(colorSelctParams.getColor_font());
                                if (selection<charSequence.length()&&selection>0&&clearEditText.getSelectionEnd()-1<textBeanList.size()){
                                    textBeanList.add(clearEditText.getSelectionEnd()-1,bean);
                                    bean=null;
                                }else {
                                    textBeanList.add(bean);
                                    bean=null;
                                }
                            }
//                            }
                        }else {
                            isPaste = false;
                        }
                    }
                    for (int i=0;i<textBeanList.size();i++){
                        Log.e(TAG, "onTextChanged:字符 "+textBeanList.get(i).getCharacter());
                        Log.e(TAG, "onTextChanged:字体色 "+textBeanList.get(i).getFont());
                        Log.e(TAG, "onTextChanged:背景色 "+textBeanList.get(i).getBackdrop());
                    }
                    clearEditText.requestFocus();
                    clearEditText.setCursorVisible(true);
                }
                running = false;
            }else {
                isSet=false;
            }

        }

    };
    /**
     *
     * 全彩下，自动颜色
     * */
    private void getTextBeanListAutoColor(String str) {
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
//        setTextSpan(string);
//        clearEditText.setSelection(clearEditText.getText().length());
    }
    private SpannableStringBuilder getTextSpan(String text) {
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
//                    if (j!=text.length()-1){
//                    }
                    spannableString.setSpan(new BackgroundColorSpan(parseColor(textBeanList.get(j).getBackdrop())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(parseColor(textBeanList.get(j).getFont())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.append(spannableString);
                    spannableString = null;
                }
            }
        }
        return spannableStringBuilder;
    }
    private void setTextSpan(String text) {
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
//                    if (j!=text.length()-1){
//                    }
                    spannableString.setSpan(new BackgroundColorSpan(parseColor(textBeanList.get(j).getBackdrop())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(parseColor(textBeanList.get(j).getFont())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.append(spannableString);
                    spannableString = null;
                }
            }
        }
        isSet=true;
        if (StaticDatas.isSupportMarFullColor){
            clearEditText.setText(spannableStringBuilder);
        }else {
            clearEditText.setText(text);
        }
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
    private void pressSend() {

    }
    private void setEditTextFilter(int wordSize) {
        if (StaticDatas.isSupportMarFullColor){
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
            Toast.makeText(context,getResources().getString(R.string.out_of_max),Toast.LENGTH_SHORT).show();
        }
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
//        setLeftCount();
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
    public List<TextBean> deepCopy(List<TextBean> list) throws IOException, ClassNotFoundException{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(list);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in =new ObjectInputStream(byteIn);
        List dest = (List)in.readObject();
        return dest;
    }

    public void refreshData(boolean isSupportMarFullColor,String s) {
        dataType=s;
        refreshViewPager();
    }
    public boolean getStatue(){
        return isCreated;
    }
    private void refreshViewPager() {
        if (fragments!=null){
            fragments.clear();
        }else {
            fragments = new ArrayList<>();
        }
        fragments.add(fontSelectFragment);
        if (StaticDatas.isSupportMarFullColor){
            fragments.add(colorSelectFragment);
            rb_color.setVisibility(View.VISIBLE);
        }else {
            rb_color.setVisibility(View.GONE);
        }
        viewPagerAdapter.notifyDataSetChanged();
        rg_font_color_select.check(R.id.rb_font);
        setEditTextFilter(selectedParams.wordSize);
        setTextSpan(clearEditText.getText().toString());
        ledView.setMatrixTextWithColor(StaticDatas.isSupportMarFullColor,wordEffectType,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);
        colorSelctParams.setColor_backdrop(defaultBGColor);
        colorSelctParams.setColor_font(defaultFontColor);
        if (!StaticDatas.isSupportMarFullColor){
            textBeanList= getDefultTextBeanList(clearEditText.getText().toString());
        }
        setTextSpan(clearEditText.getText().toString());
    }
}
