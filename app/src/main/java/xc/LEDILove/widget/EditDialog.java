package xc.LEDILove.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import xc.LEDILove.Bean.Params;
import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.Bean.TextColorSelctParams;
import xc.LEDILove.R;
import xc.LEDILove.adapter.SelectorViewPagerAdapter;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.LangUtils;
import xc.LEDILove.utils.TimonLibary;

/**
 * Created by xcgd on 2018/5/16.
 */

public class EditDialog extends Dialog{
    private final String TAG = EditDialog.class.getSimpleName();
    private Context context;
    private FragmentManager manager;
    private SelectorViewPagerAdapter viewPagerAdapter;
    private boolean isSet = false;//是否是设置Text 引发的回调
    private  boolean isPaste = false;//是否粘贴事件
    private SpannableStringBuilder spannableStringBuilder;
    private LinearLayout ll_main_diy_popupwindow_font;
    private LinearLayout ll_main_diy_popupwindow_color;
    private ClearEditText clearEditText;
    private List<TextBean> textBeanList;
    private List<TextBean> textBeanList_backup;//切换自动颜色和自定义颜色 是作为textBeanList 的备份，避免切换自动颜色操作不可逆
    private int MAX_COUNT = 80;
    private Spinner sp_special;
    //字体大小
    private Spinner spinnerWordSize;
    //正 斜 粗
    private Spinner spinnerWordType;
    private Spinner spinner_alignment;
    private String[] wordSize;
    private String[] wordType;
    private String[] wordType_full;
    private String[] wordspecial;
    private String[] alignments;
    private RadioGroup rg_font_color_select;
    private TextColorSelctParams colorSelctParams;
    private int defaultFontColor = 1;
    private int defaultBGColor =0;
    private TextView tvTxtNumber;
    private boolean isSupportMarFullColor = true;
    private Params selectedParams = new Params();

    //背景颜色选择控件

    private RadioGroup rg_edit_dialog_font_color;
    private RadioGroup rg_edit_dialog_bg_color;
    //字体颜色选择控件
    //包裹led
    private FrameLayout frameLayout;
    private LedView ledView;
    private TextView tv_preview;
    private TextColorSelctParams selctParams;
    private Button btn_auto_shift;
    private Button btn_dialog_cancel;
    private Button btn_dialog_ok;
    private boolean isAutoColor = false;
    private boolean isLensImage = false;
    private int wordEffectType = 0;
    private int alignment = 0;
    private ArrayAdapter<String> wordTypeAdapter;
    private boolean isLarger = false;
    public interface DialogResultCallback{
        void onResut(boolean isOk,int alignment,int dots,SpannableStringBuilder spannableStringBuilder,List<TextBean> textBeanList);
    }
    private DialogResultCallback dialogResultCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popuwindow_pattle_text);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        findView();
        initView();
        viewEvent();
    }

    private void viewEvent() {
        radioGroupEvent();
        rg_font_color_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_font:
                        ll_main_diy_popupwindow_font.setVisibility(View.VISIBLE);
                        ll_main_diy_popupwindow_color.setVisibility(View.GONE);
                        break;
                    case R.id.rb_color:
                        ll_main_diy_popupwindow_font.setVisibility(View.GONE);
                        ll_main_diy_popupwindow_color.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        spinnerWordType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (wordType.length>position){
                    selectedParams.wordType= wordType[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //字体
//        selectedParams.wordType = wordType[0];
        sp_special.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wordEffectType = position;
//                    if (position==0){
//                        isLensImage =false;
//                    }else {
//                        isLensImage=true;
//                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //字体大小
        spinnerWordSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onWordSizeChange:"+wordSize[position] );
                selectedParams.wordSize = Integer.parseInt(wordSize[position]);
                if ((selectedParams.wordSize==24||selectedParams.wordSize==32)&&!isLarger){
                    isLarger = true;
                    wordTypeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordType_full);
                    spinnerWordType.setAdapter(wordTypeAdapter);

                }else if (!(selectedParams.wordSize==24||selectedParams.wordSize==32)&&isLarger){
                    isLarger = false;
                    wordTypeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordType);
                    spinnerWordType.setAdapter(wordTypeAdapter);
                }
                setEditTextFilter(selectedParams.wordSize);
                tvTxtNumber.setText(textBeanList.size() + "/"+MAX_COUNT);
                if (!TextUtils.isEmpty(selectedParams.str)) {
                    //重新设置EditText 字符控制
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //对齐方式
        spinner_alignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alignment=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogResultCallback.onResut(false,alignment,selectedParams.wordSize,spannableStringBuilder,textBeanList);
            }
        });
        btn_dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedParams.str=clearEditText.getText().toString();
                ledView.setMatrixTextWithColor(isSupportMarFullColor,wordEffectType,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);

                Log.e(TAG, "onClick: >>>"+isComplete);
                dialogResultCallback.onResut(true,alignment,selectedParams.wordSize,spannableStringBuilder,textBeanList);
            }
        });
        textEvent();
    }

    private void radioGroupEvent() {
        rg_edit_dialog_bg_color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                translateBGColorById(i);
            }
        });
        rg_edit_dialog_font_color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                translateFontColorById(i);
            }
        });
    }

    private void translateFontColorById(int id) {
        switch (id){
            case R.id.gb_edit_dialog_font_black:
                if (0==selctParams.getColor_backdrop()){//判断用户点击的字体颜色是否与当前背景颜色相同
                    revocationFontSelected(selctParams.getColor_font());// 如果相同，需要撤销此次点击  即设置RadioGroup 为点击前的状态
                    return;
                }
                selctParams.setColor_font(0);
                break;
            case R.id.gb_edit_dialog_font_red:
                if (1==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(1);
                break;
            case R.id.gb_edit_dialog_font_yellow:
                if (2==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(2);
                break;
            case R.id.gb_edit_dialog_font_green:
                if (3==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(3);
                break;
            case R.id.gb_edit_dialog_font_cyan:
                if (4==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(4);
                break;
            case R.id.gb_edit_dialog_font_blue:
                if (5==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(5);
                break;
            case R.id.gb_edit_dialog_font_purple:
                if (6==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(6);
                break;
            case R.id.gb_edit_dialog_font_white:
                if (7==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(7);
                break;
        }
        onColorChange();
    }

    private void revocationFontSelected(int i) {
        rg_edit_dialog_font_color.check(translateFontIdByColor(i));
    }
    private void revocationBGSelected(int i) {
        rg_edit_dialog_bg_color.check(translateBGIdByColor(i));
    }

    private int translateFontIdByColor(int i) {
        int id= R.id.gb_edit_dialog_font_black;
        switch (i){
            case 0:
                id = R.id.gb_edit_dialog_font_black;
                break;
            case 1:
                id = R.id.gb_edit_dialog_font_red;
                break;
            case 2:
                id = R.id.gb_edit_dialog_font_yellow;
                break;
            case 3:
                id = R.id.gb_edit_dialog_font_green;
                break;
            case 4:
                id = R.id.gb_edit_dialog_font_cyan;
                break;
            case 5:
                id = R.id.gb_edit_dialog_font_blue;
                break;
            case 6:
                id = R.id.gb_edit_dialog_font_purple;
                break;
            case 7:
                id = R.id.gb_edit_dialog_font_white;
                break;

        }
        return id;
    }
    private int translateBGIdByColor(int i) {
        int id = R.id.rb_edit_dialog_bg_black;
        switch (i){
            case 0:
                id = R.id.rb_edit_dialog_bg_black;
                break;
            case 1:
                id = R.id.rb_edit_dialog_bg_red;
                break;
            case 2:
                id = R.id.rb_edit_dialog_bg_yellow;
                break;
            case 3:
                id = R.id.rb_edit_dialog_bg_green;
                break;
            case 4:
                id = R.id.rb_edit_dialog_bg_cyan;
                break;
            case 5:
                id = R.id.rb_edit_dialog_bg_blue;
                break;
            case 6:
                id = R.id.rb_edit_dialog_bg_purple;
                break;
            case 7:
                id = R.id.rb_edit_dialog_bg_white;
                break;

        }
        return id;
    }

    private void translateBGColorById(int id) {
        switch (id){
            case R.id.rb_edit_dialog_bg_black:
                if (0==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(0);
                break;
            case R.id.rb_edit_dialog_bg_red:
                if (1==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(1);
                break;
            case R.id.rb_edit_dialog_bg_yellow:
                if (2==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(2);
                break;
            case R.id.rb_edit_dialog_bg_green:
                if (3==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(3);
                break;
            case R.id.rb_edit_dialog_bg_cyan:
                if (4==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(4);
                break;
            case R.id.rb_edit_dialog_bg_blue:
                if (5==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(5);
                break;
            case R.id.rb_edit_dialog_bg_purple:
                if (6==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(6);
                break;
            case R.id.rb_edit_dialog_bg_white:
                if (7==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(7);
                break;
        }
        onColorChange();
    }
    private void onColorChange(){
        setText();
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
    public int[][] getRgbData(){

        ledView.getDarwText();
        return ledView.getRgbPointData();
    }
    public List<Integer> getWordWilds(){
        return ledView.getWordWildLists();
    }
    public byte[] getEdit_byte(){
        if (!isComplete){
            return null;
        }
        edit_byte = ledView.getTextByte(isSupportMarFullColor);
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

    private String head = "BT03120";
    private boolean isComplete = false;
    private byte[] edit_byte;
    private byte[] sendcmd;
    //发送的数据大小
    private int hasSendData = 0;
    //发送的序号头00 01  02
    private int header = 0;
    /***
     * 执行发送数据事件
     */
    private byte[] getFrameData() {
        String dataType = "3";
        String cmdHeader;
        byte[] cmdHeaderbyte ;
        byte[] waitSendData;
        if (hasSendData == 0) {
            cmdHeaderbyte = new byte[22];
            //需要转换为16进制
            String ledWidthCmd = ledView.getLEDWidget(isSupportMarFullColor) + "";
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
                cmdHeaderbyte = new byte[1];
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

        //命令头

        //拼接命令头和数据
        sendcmd = new byte[sendData.length + cmdHeaderbyte.length];

        Helpful.catByte(cmdHeaderbyte, 0, sendcmd, 0);
        Helpful.catByte(sendData, 0, sendcmd, cmdHeaderbyte.length);
        return sendcmd;
//        sendCmd(sendcmd);
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
            clearEditText.setText(clearEditText.getText().toString().substring(0,MAX_COUNT));
            clearEditText.setSelection(MAX_COUNT);
            Toast.makeText(context,context.getResources().getString(R.string.out_of_max),Toast.LENGTH_SHORT).show();
        }
        clearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_COUNT)});//250字符限制
//        setLeftCount();
    }
    private void textEvent() {
        clearEditText.addTextChangedListener(mTextWatcher);
        clearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if (i == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    Log.e(TAG, "onEditorAction: search");
                    TimonLibary.hideKeyboard(clearEditText);
//                    pressSend();
                    return true;
                }
                return false;
            }
        });
        clearEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent arg0) {
                Log.e(TAG, "onKey: >>>>>>>>>>>>>>" + arg0.getCharacters());
                if (i == KeyEvent.KEYCODE_DEL && arg0.getAction() == KeyEvent.ACTION_DOWN) {//如果单独判定按键值，if内容会执行两次！(包含按下 松开 两次事件)
                    if (textBeanList.size() > clearEditText.getSelectionStart() - 1 && clearEditText.getSelectionStart() > 0) {
                        textBeanList.remove(clearEditText.getSelectionEnd() - 1);
                    }
                }
                return false;//这里返回true 系统就不会再做删除反馈了
            }
        });
        clearEditText.setCutAndPastCallback(new ClearEditText.CutAndPastCallback() {
            @Override
            public void onCut(String string) {
                int start = clearEditText.getSelectionStart();
                int end = clearEditText.getSelectionEnd();
                int count = end - start;
                for (int i = 0; i < count; i++) {
                    //这里移除数据的时候不能 从低位开始，因为从低位开始移除  移除后集合长度变小了  下次再移除 不再是最低位  而是最低位+1  最后出现数组越界
                    //eg :例如集合长度为4（1 2 3 4 ）  count 也是4  第一次 i=0 移除0（1）  第二次 i=1  移除的1(此时集合为234) 移除的便是3  而非2
                    textBeanList.remove(count - i - 1);
                }
            }

            @Override
            public void onPast(String string) {
                isPaste = true;
                int start = clearEditText.getSelectionStart();
                int end = clearEditText.getSelectionEnd();
                char[] chars = string.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    TextBean bean = new TextBean();
                    bean.setCharacter(chars[chars.length - i - 1]);
                    bean.setBackdrop(colorSelctParams.getColor_backdrop());
                    bean.setFont(colorSelctParams.getColor_font());
                    textBeanList.add(start, bean);
                }
            }

            @Override
            public void onDelete() {
                textBeanList.clear();
                setTextSpan("");
            }
        });
    }
    public void setDialogResultCallback(DialogResultCallback dialogResultCallback){
        this.dialogResultCallback = dialogResultCallback;
    }
    private void initView() {
        rg_edit_dialog_font_color.check(R.id.gb_edit_dialog_font_red);
        rg_edit_dialog_bg_color.check(R.id.rb_edit_dialog_bg_black);
        if (textBeanList==null){
            textBeanList = new ArrayList<>();
        }
        textBeanList_backup = new ArrayList<>();
        if (StaticDatas.isSupportMarFullColor){
            isSupportMarFullColor = true;
        }else {
            isSupportMarFullColor = false;
        }
        colorSelctParams = new TextColorSelctParams();
        colorSelctParams.setColor_backdrop(defaultBGColor);
        colorSelctParams.setColor_font(defaultFontColor);
        rg_font_color_select.check(R.id.rb_font);

        //字体大小
        wordSize = context.getResources().getStringArray(R.array.asciiWordSize);
        ArrayAdapter<String> wordSizeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordSize);
        spinnerWordSize.setAdapter(wordSizeAdapter);

        //字体类型
        wordType = context.getResources().getStringArray(R.array.Typeface);
        wordType_full = context.getResources().getStringArray(R.array.Typeface_full);
         wordTypeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordType);
        spinnerWordType.setAdapter(wordTypeAdapter);

        //特殊效果
        wordspecial = context.getResources().getStringArray(R.array.wordspecial);
        ArrayAdapter<String> wordspecialAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordspecial);
        sp_special.setAdapter(wordspecialAdapter);

        //对齐方式
        alignments = context.getResources().getStringArray(R.array.alignments);
        ArrayAdapter<String> alignmentAlAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, alignments);
        spinner_alignment.setAdapter(alignmentAlAdapter);
        if (spannableStringBuilder!=null&&textBeanList!=null){
//            setTextSpan(spannableStringBuilder.toString());
            clearEditText.setText(spannableStringBuilder);
            Log.e(TAG, "initView: textBeanList>>>"+textBeanList.size() );
        }
//
    }
    private void findView() {
        frameLayout = (FrameLayout) findViewById(R.id.ledView);
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
        rg_font_color_select = (RadioGroup) findViewById(R.id.rg_font_color_select);
        ll_main_diy_popupwindow_color = (LinearLayout) findViewById(R.id.ll_main_diy_popupwindow_color);
        ll_main_diy_popupwindow_font = (LinearLayout)findViewById(R.id.ll_main_diy_popupwindow_font);
        spinnerWordSize = (Spinner) findViewById(R.id.spinnerWordSize);
        sp_special = (Spinner) findViewById(R.id.sp_special);
        spinnerWordType = (Spinner) findViewById(R.id.spinnerWordType);
        spinner_alignment = (Spinner) findViewById(R.id.spinner_alignment);
        clearEditText= (ClearEditText) findViewById(R.id.clearEditText);
        tvTxtNumber = (TextView) findViewById(R.id.tvTxtNumber);
        btn_dialog_cancel = (Button)findViewById(R.id.btn_dialog_cancel);
        btn_dialog_ok = (Button)findViewById(R.id.btn_dialog_ok);


        selctParams = new TextColorSelctParams();
        selctParams.setColor_backdrop(0);
        selctParams.setColor_font(1);
        tv_preview = (TextView) findViewById(R.id.tv_preview);

        rg_edit_dialog_font_color = (RadioGroup) findViewById(R.id.rg_edit_dialog_font_color);
        rg_edit_dialog_bg_color = (RadioGroup) findViewById(R.id.rg_edit_dialog_bg_color);

        tv_preview.setBackgroundColor(parseColor(selctParams.getColor_backdrop()));
        tv_preview.setTextColor(parseColor(selctParams.getColor_font()));
        btn_auto_shift = (Button) findViewById(R.id.btn_auto_shift);
        setAutoColorShift(1);//初始化
        btn_auto_shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAutoColor){
                    setAutoColorShift(1);
                    isAutoColor=false;
                }else {
                    setAutoColorShift(0);
                    isAutoColor=true;
                }
                if (isSupportMarFullColor){
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
        });
    }
    private void setAutoColorShift(int choose) {
        btn_auto_shift.setTextColor(context.getResources().getColor(R.color.white));
        String str=context.getResources().getString(R.string.shift_auto);
        SpannableStringBuilder style=new SpannableStringBuilder(str);
        int index=str.indexOf("/");
        switch (choose){
            case 0://单行模式 “/” 前面字符显示绿色
                style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_color_yellow)),0,index, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                break;
            case 1://双行模式 “/” 后面字符显示绿色
                style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.text_color_yellow)),index+1,str.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                break;
        }
        btn_auto_shift.setText(style);
    }
    public EditDialog(@NonNull Context context,SpannableStringBuilder spannableStringBuilder,List<TextBean> textBeanList) {
        super(context);
        this.context=context;
        this.spannableStringBuilder = spannableStringBuilder;
        this.textBeanList = textBeanList;
        if (textBeanList!=null){
            Log.e(TAG, "EditDialog: textBeanList>>>"+textBeanList.size() );
        }
    }
    public EditDialog(Context context, FragmentManager manager){
        super(context);
        this.manager = manager;
    }
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
//            ledView.setMatrixTextWithColor(isSupportMarFullColor,selectedParams.str, selectedParams.wordSize, selectedParams.wordType, selectedParams.color,textBeanList);
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
                    if (!LangUtils.isAscii(text)){//英文之外的 字符 需要读取特殊的字库 这里做个识别
                        reSetWordSizeAndType();
                    }
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
//                    selectedParams.str = s.toString();
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

//            if (fontSelectFragment!=null){//更新字符计数
//                fontSelectFragment.setCharCount(charSequence.length() + "/"+MAX_COUNT);
//            }
            tvTxtNumber.setText(charSequence.length() + "/"+MAX_COUNT);
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
                                if (selection<charSequence.length()&&selection>0){
                                    textBeanList.add(clearEditText.getSelectionEnd()-1,bean);
                                    bean=null;
                                }else {
                                    textBeanList.add(bean);
                                    bean=null;
                                }
                            }
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

    private void reSetWordSizeAndType() {//存在Ascii码之外的字符
        // 而选择的字体大小所对应的字库不存在这些字符 需要自动跳转到有这些字符的字体大小上去
        //如果当前的字体大小不存在 Ascii码之外的字符
        if (selectedParams.wordSize==5||
                selectedParams.wordSize==7||
                selectedParams.wordSize==10){
            spinnerWordSize.setSelection(3);
            selectedParams.wordSize=12;
        }
    }

    private boolean textSelected(){
        return clearEditText.getSelectionEnd()-clearEditText.getSelectionStart()>0;
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



    public List<TextBean> deepCopy(List<TextBean> list) throws IOException, ClassNotFoundException{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(list);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in =new ObjectInputStream(byteIn);
        List dest = (List)in.readObject();
        return dest;
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
    private int parseColor(int position) {
        int color = -1;
        switch (position){
            case 0:
                color =  context.getResources().getColor(R.color.black);
                break;
            case 1:
                color = context.getResources().getColor(R.color.red);
                break;
            case 2:
                color = context.getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = context.getResources().getColor(R.color.dark_green);
                break;
            case 4:
                color = context.getResources().getColor(R.color.cyan);
                break;
            case 5:
                color = context.getResources().getColor(R.color.blove);
                break;
            case 6:
                color = context.getResources().getColor(R.color.purple);
                break;
            case 7:
                color = context.getResources().getColor(R.color.white);
                break;
        }
        return color;
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

    private void setText() {
        //设置预览颜色
        tv_preview.setBackgroundColor(parseColor(selctParams.getColor_backdrop()));
        tv_preview.setTextColor(parseColor(selctParams.getColor_font()));

    }
}
