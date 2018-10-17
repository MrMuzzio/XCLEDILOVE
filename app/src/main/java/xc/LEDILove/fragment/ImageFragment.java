package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.R;
import xc.LEDILove.activity.PicPreViewActivity;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.db.UmsResultBean;
import xc.LEDILove.db.UmsResultHelper;
import xc.LEDILove.utils.BitmapUtils;
import xc.LEDILove.utils.DataTypeUtils;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.utils.TimonLibary;
import xc.LEDILove.widget.DispatchScrollView;
import xc.LEDILove.widget.EditDialog;
import xc.LEDILove.widget.LEDPaintView;
import xc.LEDILove.widget.MsgNumberDialog;

/**
 * Created by timon on 2018/5/14.
 */

public class ImageFragment extends Fragment implements View.OnClickListener{
    private final String TAG = ImageFragment.class.getSimpleName();
    private Context context;
    private SpannableStringBuilder spannableStringBuilder;
    private List<TextBean> textBeanList;
    private LEDPaintView ledPaintView;
    private DispatchScrollView dispatch_scroll_view;
    private RadioGroup rg_palette_color_select;
    private RadioGroup rg_palette_control;
    private RadioGroup rg_paint_tools;
    private RadioButton rb_pen;
    private int paintColor = 1;
    private int BGColor = 0;
    private int control_state = 0;//0 画笔 1 橡皮擦 2 油漆桶 3 pic
    private ImageButton ibtn_clear;
    private ImageButton btn_undo;
    private ImageButton btn_do_next;
    private ImageButton btn_save;
    private ImageButton ib_pic;
    private ImageButton btn_preview;
    private ImageButton btn_msg_number;
    private ImageButton ib_text;
    private ImageButton ib_tools_visible;
    private TextView tv_palette_page_current;
    private TextView tv_palette_page_max;
    private TextView tv_msg_number;
    private SeekBar seekBar_palette_control_move;
    private LinearLayout ll_palette_page_control;
    private  int pointCount = 32;
    private int data_h =32;
    private int data_v = 32;
    //    private  int pointCount = 48;
//    private int data_h =48;
//    private int data_v = 40;
    private TextView tv_dimensions;
    private  LinearLayout ll_pattle;
    private boolean isSupportMarFullColor ;
    private RealTimePreviewListener realTimePreviewListener;
    private boolean isrealtime = false;//是否处于同步预览模式
    private boolean isshowtool = false;//画图工具是否可视
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ImageFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public ImageFragment (Context context){
        this.context=context;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        ledPaintView = new LEDPaintView(context);
        View view = inflater.inflate(R.layout.fragment_palettle,container,false);
        findView(view);
        initData();
        interfaceListener();
        viewEvent();
        initSQLData();
        return view;
    }
    /**
     * 根据下位机屏幕高度刷新画板
     */
//    public void refreshView(){
//        data_v = StaticDatas.LEDWidth;
//        data_h = StaticDatas.LEDHight;
//        Log.e(TAG, "refreshView: >>"+data_h+"*"+data_v );
//        pointCount = data_v;
//        initPaletteSize(false);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler!=null){
            handler.removeMessages(SYNC_PALETTLE);
        }
    }

    private final int SYNC_PALETTLE = 1001;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==SYNC_PALETTLE){
                realTimePreviewListener.onStepRefresh();
            }
        }
    };
    private void interfaceListener() {
        ledPaintView.setPaintListener(new LEDPaintView.PaintListener() {
            @Override
            public void onPointPaint() {
                //按下，一步操作开始
                handler.sendEmptyMessageDelayed(SYNC_PALETTLE,1500);
            }

            @Override
            public void onStepPaint() {
                //抬手，一步操作完成
                handler.removeMessages(SYNC_PALETTLE);
                realTimePreviewListener.onStepRefresh();
            }
        });
    }

    private boolean isSetColor= false;
    private void findView(View view) {
        ledPaintView = (LEDPaintView) view.findViewById(R.id.sfv_paint);
        dispatch_scroll_view = view.findViewById(R.id.dispatch_scroll_view);
//        fl_palette = (LinearLayout) view.findViewById(R.id.fl_palette);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ledPaintView.setLayoutParams(params);
//        fl_palette.addView(ledPaintView);
        rg_palette_color_select = (RadioGroup) view.findViewById(R.id.rg_palette_color_select);
        rg_palette_control = (RadioGroup) view.findViewById(R.id.rg_palette_control);
        rg_paint_tools = (RadioGroup) view.findViewById(R.id.rg_paint_tools);
        rb_pen = (RadioButton)view.findViewById(R.id.rb_pen);
        ibtn_clear = (ImageButton) view.findViewById(R.id.ibtn_clear);
        btn_undo = (ImageButton) view.findViewById(R.id.btn_undo);
        btn_do_next = (ImageButton) view.findViewById(R.id.btn_do_next);
        btn_save = (ImageButton) view.findViewById(R.id.btn_save);
        ib_pic = (ImageButton) view.findViewById(R.id.ib_pic);
        ibtn_clear.setOnClickListener(this);
        rb_pen.setOnClickListener(this);
        btn_preview = (ImageButton) view.findViewById(R.id.btn_preview);
        btn_preview.setOnClickListener(this);
        btn_msg_number = (ImageButton) view.findViewById(R.id.btn_msg_number);
        btn_msg_number.setOnClickListener(this);
        ib_text = (ImageButton) view.findViewById(R.id.ib_text);
        ib_text.setOnClickListener(this);
        ib_tools_visible = (ImageButton) view.findViewById(R.id.ib_tools_visible);
        ib_tools_visible.setOnClickListener(this);
        ib_pic.setOnClickListener(this);
        btn_undo.setOnClickListener(this);
        btn_do_next.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        tv_dimensions = (TextView) view.findViewById(R.id.tv_dimensions);
        ll_pattle = (LinearLayout)view.findViewById(R.id.ll_pattle);
        ll_palette_page_control = (LinearLayout)view.findViewById(R.id.ll_palette_page_control);
        tv_palette_page_current = (TextView)view.findViewById(R.id.tv_palette_page_current);
        tv_palette_page_max = (TextView)view.findViewById(R.id.tv_palette_page_max);
        tv_msg_number = (TextView)view.findViewById(R.id.tv_msg_number);
        seekBar_palette_control_move = (SeekBar)view.findViewById(R.id.seekBar_palette_control_move);
    }
    private void initData() {
        ledPaintView.init(context,pointCount);
        if (StaticDatas.isSupportMarFullColor){
            isSupportMarFullColor = true;
        }else {
            isSupportMarFullColor = false;
        }
//        data_h = 32;
//        data_v = 16;
        initPaletteSize(false);
    }
    private void viewEvent() {
        ledPaintView.init(context,pointCount);
        tv_dimensions.setText(pointCount+" x "+pointCount);
        ledPaintView.setFocuseable(true);
        int [] s= new int[2];
        ledPaintView.getLocationOnScreen(s);
        dispatch_scroll_view.setPaintViewSize(ledPaintView.getX(),ledPaintView.getY());
        Log.e(TAG, "onCreate: "+s[0] +"   "+s[1]);
        rg_palette_color_select.check(R.id.rb_red);
        rg_palette_control.check(R.id.rb_pen);
        rg_paint_tools.check(R.id.rb_tool_pen);
        rg_palette_color_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged: "+ i);
                if (isSetColor){
                    isSetColor = false;
                    return;
                }
                switch (control_state){
                    case 0:
                        paintColor = translateIDtoColorValues(i);
                        Log.e(TAG, "onCheckedChanged: paintColor"+paintColor);
                        ledPaintView.setPaintColor(paintColor);
                        break;
                    case 1:
                        break;
                    case 2:
                        BGColor = translateIDtoColorValues(i);
                        Log.e(TAG, "onCheckedChanged: bgcolor>>>>"+BGColor);
                        ledPaintView.setBGColor(BGColor);
                        break;
                    case 3:
                        break;
                }

            }
        });
        rg_palette_control.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged: "+i );
                switch (i){
                    case R.id.rb_pen:
                        //显示图形工具
                        isshowtool = true;
                        rg_paint_tools.setVisibility(View.VISIBLE);
                        ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_sel_1));

                        control_state = 0;
                        ledPaintView.setPaintColor(paintColor);
                        isSetColor = true;
                        rg_palette_color_select.check(translateColorValuesToId(paintColor));
                        break;
                    case R.id.rb_rubber:
                        //隐藏图形工具
                        isshowtool = false;
                        rg_paint_tools.setVisibility(View.GONE);
                        ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_nor_1));

                        control_state = 1;
                        ledPaintView.setPaintColor(BGColor);
                        break;
                    case R.id.rb_bucket:
                        //隐藏图形工具
                        isshowtool = false;
                        rg_paint_tools.setVisibility(View.GONE);
                        ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_nor_1));

                        control_state = 2;
                        isSetColor = true;
                        rg_palette_color_select.check(translateColorValuesToId(BGColor));
                        break;
                }
            }
        });
        rg_paint_tools.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_tool_pen:
                        ledPaintView.setTools_mode(0);
                        rb_pen.setBackgroundResource(R.drawable.selector_palette_pen);
                        break;
                    case R.id.rb_tool_line:
                        ledPaintView.setTools_mode(1);
                        rb_pen.setBackgroundResource(R.drawable.selector_palette_line);
                        break;
                    case R.id.rb_tool_ring:
                        ledPaintView.setTools_mode(2);
                        rb_pen.setBackgroundResource(R.drawable.selector_palette_ring);
                        break;
                    case R.id.rb_tool_rec:
                        ledPaintView.setTools_mode(3);
                        rb_pen.setBackgroundResource(R.drawable.selector_palette_rec);
                        break;
                }
            }
        });
        seekBar_palette_control_move.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.e(TAG, "onProgressChanged: position>>>"+i );
                tv_palette_page_max.setText(""+(i+data_v));
                ledPaintView.setPosition(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * 设置画板尺寸
     * @param isForCharData 是否是字符数据变化
     */
    private void initPaletteSize(boolean isForCharData){
        Log.e(TAG, "initPaletteSize: data_h>>"+data_h+" _pointcount>>"+pointCount );
        if (data_h>pointCount){
            ll_palette_page_control.setVisibility(View.VISIBLE);
            seekBar_palette_control_move.setMax(data_h-data_v);
            tv_palette_page_max.setText((data_v)+"");
//            seekBar_palette_control_move.setMax(data_h-32);
//            tv_palette_page_max.setText((data_h-32)+"");
            ledPaintView.setCustomSize(data_h,data_v,isForCharData);
        }else {
            ll_palette_page_control.setVisibility(View.GONE);
        }

    }
    private String path = "";
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: "+requestCode );
        if (requestCode==1001&&resultCode==1001) {
//            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.face);
//            Bitmap bitmap= drawable.getBitmap();
//            ledPaintView.setData(BitmapUtils.getBitmapPointData(BitmapUtils.scaleBitmap(bitmap, pointCount, pointCount)));
            path = data.getStringExtra("file");
            if (path!=null&&!path.equals("")){
                setPlateByPath(path,1);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtn_clear:
                path="";//清除文件路径
                seekBar_palette_control_move.setProgress(0);
//                ll_palette_page_control.setVisibility(View.GONE);
                ledPaintView.clearCanvas();
                data_v=StaticDatas.LEDHight;
                data_h = StaticDatas.LEDWidth;
                initPaletteSize(false);
//                paintColor = 1;
//                BGColor = 0;
                break;
            case R.id.btn_undo:
                ledPaintView.unDo();
                break;
            case R.id.btn_do_next:
                ledPaintView.nextDo();
                break;
            case R.id.btn_save:
                if (path.equals("")){
                    ledPaintView.save();
                }else {
                    showSaveSelectedDialog();
                }
                break;
            case R.id.ib_pic:
//                Intent picintent = new Intent(context,GalleryActivity.class);
                Intent picintent = new Intent(context,PicPreViewActivity.class);
                this.startActivityForResult(picintent,1001);
                break;
            case R.id.btn_preview:
                if (isrealtime){//关闭同步功能
                    handler.removeMessages(SYNC_PALETTLE);
                    isrealtime =false;
                    btn_preview.setBackground(getResources().getDrawable(R.drawable.btn_preview_nor));
                    Toast.makeText(context,getResources().getString(R.string.toast_exit_preview),Toast.LENGTH_SHORT).show();
                    realTimePreviewListener.onStopPreview();
                }else {//打开同步功能
                    isrealtime = true;
                    btn_preview.setBackground(getResources().getDrawable(R.drawable.btn_preview_sel));
                    Toast.makeText(context,getResources().getString(R.string.toast_enter_preview),Toast.LENGTH_SHORT).show();
                    realTimePreviewListener.onStartPreview();
                }
                break;
            case R.id.ib_text:
//                showTextPopupWindow();
                showTextDialog();
                break;
            case R.id.btn_msg_number:
                showMsgNumberDialog();
                break;
            case R.id.ib_tools_visible:
                Log.e(TAG, "onClick: tools>>"+isshowtool);
//                if (isshowtool){
//                    isshowtool = false;
//                    rg_paint_tools.setVisibility(View.GONE);
//                    ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_nor_1));
//                }else {
//                    isshowtool = true;
//                    rg_paint_tools.setVisibility(View.VISIBLE);
//                    ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_sel_1));
//                }
                break;
            case R.id.rb_pen:
                if (isshowtool){
                    isshowtool = false;
                    rg_paint_tools.setVisibility(View.GONE);
                    ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_nor_1));
                }else {
                    isshowtool = true;
                    rg_paint_tools.setVisibility(View.VISIBLE);
                    ib_tools_visible.setBackground(getResources().getDrawable(R.drawable.btn_tools_sel_1));
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showSaveSelectedDialog() {
        View view  = LayoutInflater.from(context).inflate(R.layout.dialog_platte_save_selected,null,false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.setView(view).create();

        view.findViewById(R.id.btn_dialog_save_as).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ledPaintView.save();
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_dialog_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ledPaintView.save(path);
                dialog.dismiss();
            }
        });
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4
//        dialog.getWindow().setLayout((TimonLibary.getScreenWidth(context)/4*3),LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private MsgNumberDialog msgNumberDialog;
    private int index_msg = -1;
    private void showMsgNumberDialog() {
        if (msgNumberDialog==null){
            msgNumberDialog = new MsgNumberDialog(context, new MsgNumberDialog.MsgSelectedListener() {
                @Override
                public void onSelected(int number) {
                    if (index_msg+1==number){//选择的信息号与当前信息号相同，不做任何处理
                        return;
                    }
                    index_msg = number-1;
                    if (msgNumberDialog!=null){
                        if (number==0){
                            tv_msg_number.setText("--");
                            umsResultBean_index = null;
                        }else {
                            tv_msg_number.setText(number+"");
                            umsResultBean_index = umsResultBeanList.get(number-1);
                        }
                        msgNumberDialog.dismiss();
                    }
                    refreshPlatteByMsgNumber(index_msg);
//                    if (umsResultBeanList==null){
//
//                    }
                }
            });
        }
        msgNumberDialog.show();
    }

    /**
     * 根据所选信息号
     * 读取对应的点阵数据
     * 刷新画板
     * @param index_msg 当前信息号
     */
    private void refreshPlatteByMsgNumber(int index_msg) {
        if (index_msg!=-1&&umsResultBean_index.layerDiyByte!=null&&umsResultBean_index.layerCharByte!=null){
            if (umsResultBean_index.layerCharByte.length<umsResultBean_index.layerDiyByte.length){
                data_h =umsResultBean_index.layerDiyByte.length;
            }else {
                data_h =umsResultBean_index.layerCharByte.length;
            }
            initPaletteSize(false);
            ledPaintView.setLayerDiyData(umsResultBean_index.layerDiyByte);
            ledPaintView.setCharLayerData(umsResultBean_index.layerCharByte);
        }
    }

    //数据库操作类
    private UmsResultHelper umsResultHelper;
    private List<UmsResultBean> umsResultBeanList;
    private UmsResultBean umsResultBean_index;
    private void initSQLData(){
        umsResultHelper = new UmsResultHelper(context);
        //初始化查看本地有没有历史数据
        umsResultBeanList = umsResultHelper.getUmsReulstList();
        if (umsResultBeanList.size() < 8) {
            //判填充到8个值，MESSAGE1...MESSAGE8
            for (int position = umsResultBeanList.size(); position < 8; position++) {
                UmsResultBean umsResultBean = new UmsResultBean();
                umsResultBean.type = "1";
                umsResultBean.color = getResources().getColor(R.color.red);
                umsResultBean.speed = 1;
                umsResultBean.bright = 1;
                umsResultBean.numberIndex = position + 1;
                umsResultBean.body = "MESSAGE" + (position + 1);
                umsResultBean.beanList = getDefultTextBeanList(umsResultBean.body);
                umsResultBean.layerBg = 0;
                umsResultBean.layerCharByte = ledPaintView.getPointData();
                umsResultBean.layerDiyByte = ledPaintView.getPointData();
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
    public boolean getPreviewStatus(){
        return isrealtime;
    }

    public void resetLayout() {
        if (StaticDatas.LEDHight==data_v){
//            return;
        }else {

        }
        data_v = StaticDatas.LEDHight;
        data_h = StaticDatas.LEDWidth;
        pointCount = StaticDatas.LEDWidth;
//        ledPaintView.resetSize(ledPaintView.getDisplay().getWidth());
        ledPaintView.resetSize(TimonLibary.getScreenWidth(context));
    }

    /**
     * 打开同步功能
     */
    public void openPreView(){
        if (!isrealtime){//打开同步功能
            isrealtime = true;
            btn_preview.setBackground(getResources().getDrawable(R.drawable.btn_preview_sel));
            Toast.makeText(context,getResources().getString(R.string.toast_enter_preview),Toast.LENGTH_SHORT).show();
            realTimePreviewListener.onStartPreview();
        }

    }

    /**
     * 关闭同步功能
     */
    public void closePreView(){
        if (isrealtime){
            handler.removeMessages(SYNC_PALETTLE);
            isrealtime =false;
            btn_preview.setBackground(getResources().getDrawable(R.drawable.btn_preview_nor));
            Toast.makeText(context,getResources().getString(R.string.toast_exit_preview),Toast.LENGTH_SHORT).show();
            realTimePreviewListener.onStopPreview();
        }
    }

    /**
     * @return 是否处于同步状态
     */
    public boolean getPreViewStatus(){
        return isrealtime;
    }
    public interface RealTimePreviewListener{
        void onStartPreview();
        void onStopPreview();
        void onStepRefresh();
    }
    public void setRealTimePreviewListener(RealTimePreviewListener realTimePreviewListener){
        this.realTimePreviewListener = realTimePreviewListener;
    }
    public void setPlateByPath(String path,int type){
        if (!(path.equals(""))){
            Log.e(TAG, "onActivityResult: >>>>path:"+path );
            Log.e(TAG, "onActivityResult: >>>>size :"+data_v+"_"+data_h );
            Bitmap bitmap = BitmapUtils.getBitmap(path);
            ledPaintView.setData(BitmapUtils.getPointData(bitmap, data_h, data_v,type));
        }
    }
    private int[][] editGgbData;
    private List<Integer> wordWilds;
    private EditDialog editDialog;
    private int screenWild;
    private void showTextDialog() {
        editDialog = new EditDialog(context,spannableStringBuilder,textBeanList);

        // 设置dialog位置
        Window window = editDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
//        lp.x = 10;
//        lp.y = 10;
//        lp.width = screenWild;
//        lp.height = screenWild;
//        lp.alpha = 0.8f;
        window.setAttributes(lp);

        editDialog.show();
        editDialog.setDialogResultCallback(new EditDialog.DialogResultCallback() {
            @Override
            public void onResut(boolean isOk, int alignment, int dots, SpannableStringBuilder spannableStringBuilder,List<TextBean> textBeanList) {

                ImageFragment.this.spannableStringBuilder = spannableStringBuilder;
                ImageFragment.this.textBeanList = textBeanList;
                if (isOk){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //自动识别软键盘状态并转换状态(显示/隐藏) 此处为隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    //获取输入字符的GRB点阵数据
                    editGgbData = editDialog.getRgbData();
                    //获取输入字符的字库集合
                    wordWilds = editDialog.getWordWilds();
                    //处理点阵数据
                    adjustRgbData(alignment,dots);
//                    sendcmd = editDialog.getEdit_byte();
                }
                editDialog.dismiss();
                TimonLibary.hideKeyboard(dispatch_scroll_view);
            }

        });
    }
    /**
     * 点阵字符层数据处理  规则：
     * 1.判断点阵是否可以分行放入 画板点阵中 如可行 则分行按照一屏处理 整体居中
     * 2.如果不可行  居中显示
     * */
    int[][] adjustedData = new int[data_h][data_v];
    private void adjustRgbData(int alignment,int dots) {
        if (editGgbData==null){
            return;
        }
        int dataHigh = editGgbData.length;

        int row = data_h/dataHigh;
        //总字体宽
        int sum_wordWild = 0;
        for (int i=0;i<wordWilds.size();i++){
            sum_wordWild = sum_wordWild+wordWilds.get(i);
        }
        Log.e(TAG, "adjustRgbData: datahigh>>"+editGgbData[0].length );
        Log.e(TAG, "adjustRgbData: width>>"+sum_wordWild );
        if (sum_wordWild>row*pointCount){//总字宽大于 初始画板宽*可显示的行数
            //不可一屏显示 需要单行显示 增加画板宽度 上下居中显示
            Log.e(TAG, "adjustRgbData: 超过一屏");
            adjustedData = new int[sum_wordWild][data_v];
            for (int j=0;j<sum_wordWild;j++){
                for (int k=0;k<data_v;k++){
                    adjustedData[j][k] =0;
                }
            }
            int index_y = 0;
            if (alignment==0){//左上对齐

            }else if (alignment==1){//居中对齐
                index_y = (data_v-dataHigh)/2;
            }
            combinationDataSingle(index_y);
            //重设画板宽度
            data_h = sum_wordWild;
            initPaletteSize(true);
        }else {
            //可以一屏显示  换行
            adjustedData = new int[data_h][data_v];
            for (int j=0;j<data_h;j++){
                for (int k=0;k<data_v;k++){
                    adjustedData[j][k] =0;
                }
            }
            //以字符宽度为单位 重组数组
            int singleWild=0;//当前行已经重组的宽度
            int sum = 0;
            int high = 0;
            int offset_x = 0;
            int offset_y = 0;
            int cloum =0;//行数
            int maxwild = 0;
            if (alignment==0){//左上对齐

            }else if (alignment==1){//居中对齐
                cloum=1;
                for (int n =0;n<wordWilds.size();n++){
                    int wild = wordWilds.get(n);

                    if (singleWild+wild>data_h){//超过一行了  需要换行
                        singleWild=0;
                        cloum+=1;
                    }
                    singleWild+=wild;
                    if (maxwild<singleWild){
                        maxwild=singleWild;
                    }
                }
                offset_x = (data_h-maxwild)/2;
                offset_y = (data_v-(cloum*dots))/2;
                Log.e(TAG, "adjustRgbData: offset>>>"+offset_x+"_"+offset_y);
                singleWild=0;
                cloum=0;
            }
            for (int s=0;s<wordWilds.size();s++){
                int wild  = wordWilds.get(s);
                if (singleWild+wild>data_h){//超过一行了  需要换行
                    singleWild=0;
                    high+= dataHigh;
                    if (dots==5||dots==7){//五号字体上下无空余点 行之间需要增加行偏移量
                        cloum+=1;
                    }
                }
                if (s==wordWilds.size()-1){//最后一个字符宽度需要减1
                    wild = wild-1;
                }
                combinationData(singleWild,high,wild,dataHigh,sum,offset_x,offset_y+cloum);//
                singleWild = singleWild+wild;
                sum=sum+wild;
            }
        }
        //给画板设置数据
        ledPaintView.setCharLayerData(adjustedData);
    }

    private void combinationDataSingle(int index_y) {
        Log.e(TAG, "combinationDataSingle: >>"+editGgbData[0].length);
        Log.e(TAG, "combinationDataSingle: >>"+adjustedData.length);
        for (int s=0;s<editGgbData[0].length;s++){
            for (int l =0;l<editGgbData.length;l++){
                Log.e(TAG, "combinationDataSingle: position>>>"+s+"-"+(index_y+l));
                adjustedData[s][index_y+l] = editGgbData[l][s];
            }
        }
    }

    /**
     * @param index_x 横向添加 初始位置
     * @param index_y 纵向添加  初始位置
     * @param wild 添加字符宽度
     * @param dataHigh 添加字符高度
     * @param sumWild  添加的总宽度
     * @param x        x方向偏移量
     * @param y         y方向偏移量
     */
    private void combinationData(int index_x, int index_y, Integer wild,int dataHigh,int sumWild,int x,int y) {
        Log.e(TAG, "combinationData: wild>>>"+wild);
        Log.e(TAG, "combinationData: dataWild>>>"+editGgbData.length);
        for (int a=0;a<wild;a++){
            for (int b=0;b<dataHigh;b++){
//                Log.e(TAG, "combinationData: b>>"+b);
                if (sumWild+a<editGgbData[0].length){
                    adjustedData[index_x+a+x][index_y+b+y] = editGgbData[b][sumWild+a];
                }
            }
        }
    }

    private int CMD_SENDING =1001;
    @SuppressLint("HandlerLeak")
    private Handler myhander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==CMD_SENDING){
//                if (serviceBinder.getConnectedStatus()){
//                }else {
//                    Toast.makeText(context,getString(R.string.connectstate),Toast.LENGTH_SHORT).show();
//                }

            }
        }
    };
    private int translateIDtoColorValues(int i) {
        switch (i){
            case R.id.rb_back:
                return 0;
            case R.id.rb_red:
                return 1;
            case R.id.rb_yellow:
                return 2;
            case R.id.rb_green:
                return 3;
            case R.id.rb_cyan:
                return 4;
            case R.id.rb_blue:
                return 5;
            case R.id.rb_purple:
                return 6;
            case R.id.rb_white:
                return 7;
        }
        return 0;
    }
    private int translateColorValuesToId(int color) {
        switch (color){
            case 0:
                return R.id.rb_back;
            case 1:
                return(R.id.rb_red);
            case 2:
                return(R.id.rb_yellow);
            case 3:
                return(R.id.rb_green);
            case 4:
                return(R.id.rb_cyan);
            case 5:
                return(R.id.rb_blue);
            case 6:
                return(R.id.rb_purple);
            case 7:
                return (R.id.rb_white);
        }
        return 0;
    }
    private byte[] edit_byte;
    //发送的数据大小
    private int hasSendData = 0;
    //发送的序号头00 01  02
    private int header = 0;
    private byte[] sendcmd;
    public byte[] getDiy_byte(String path,int number,int type) {
        if (path!=null&&!path.equals("")){
            Bitmap bitmap = BitmapUtils.getBitmap(path);
            edit_byte = getRGBData(BitmapUtils.getPointData(bitmap, data_h, data_v,type));
        }else {
            edit_byte = getRGBData(ledPaintView.getPointData());
        }
        hasSendData = 0;
        List<byte[]> bytes = new ArrayList<>();
        while (hasSendData<edit_byte.length){
            bytes.add(getFrameData(number,ledPaintView.getPointData().length));
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
//            Log.e(TAG, "getEdit_byte: "+Helpful.MYBytearrayToString(result));
        }
        return result;
    }

    /***
     * 执行发送数据事件
     */
    private byte[] getFrameData(int number,int width) {
        String cmdHeader;
        byte[] waitSendData;
        byte[] cmdHeaderbyte ;
        if (hasSendData == 0) {
            //需要转换为16进制
//            Log.e(TAG, "sendTxtData: dataType>>>>"+dataType);
//            cmdHeader = head+ selectedParams.wordSize + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            if (isrealtime){//
                cmdHeaderbyte = new byte[1];
                cmdHeader = "P";
                Helpful.catByte(cmdHeader.getBytes(), 0, cmdHeaderbyte, 0);
            }else {
                cmdHeaderbyte = new byte[22];
                cmdHeader = "BT03120" //命令头
                        + StaticDatas.LEDHight; //点阵高度
                if (width>99){
                    cmdHeader = cmdHeader+
                            "0"+width;//点阵数据宽度
                } else {
                    cmdHeader = cmdHeader+
                            "00"+width;//点阵数据宽度
                }
                cmdHeader = cmdHeader + "3"//颜色代码
                        + number//信息号
                        +"3";//移动方式
//                        +"1fff" //
//                        +"00";
                Helpful.catByte(cmdHeader.getBytes(), 0, cmdHeaderbyte, 0);
                cmdHeaderbyte[16] = 0x00;
                cmdHeaderbyte[17] = 0x00;
                cmdHeaderbyte[18] = 0x00;
                cmdHeaderbyte[19] = 0x00;
                cmdHeaderbyte[20] = 0x30;
                cmdHeaderbyte[21] = 0x30;
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
        byte[] sendData;
        if (!isrealtime){//实时预览不需要校验
            //在最后一位增加校验位
            byte checkByte = 0;
            for (int position = 0; position < waitSendData.length; position++) {
                checkByte = (byte) (checkByte ^ waitSendData[position]);
            }
            sendData= new byte[waitSendData.length + 1];
//        byte[] sendData = new byte[waitSendData.length];
            Helpful.catByte(waitSendData, 0, sendData, 0);
            sendData[waitSendData.length] = checkByte;
        }else {
            sendData = new byte[waitSendData.length ];
            Helpful.catByte(waitSendData, 0, sendData, 0);
        }


        //拼接命令头和数据
        sendcmd = new byte[sendData.length + cmdHeaderbyte.length];

        Helpful.catByte(cmdHeaderbyte, 0, sendcmd, 0);
        Helpful.catByte(sendData, 0, sendcmd, cmdHeaderbyte.length);
        return sendcmd;
    }
    private byte[] getRGBData(int [][] latticeColors ){
        int x=latticeColors[0].length;
        int y = latticeColors.length;
        boolean[][] R_booleanArry = new boolean[x][y];
        boolean[][] G_booleanArry = new boolean[x][y];
        boolean[][] B_booleanArry = new boolean[x][y];
        /**
         * 根据记录的颜色值(int) 用三个boolean数组分别记录RGB颜色
         * */
        for (int j = 0;j<y;j++){
            for (int i = 0;i<x;i++){
                switch (latticeColors[j][i]){
                    case 0://黑 000
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = false;
                        break;
                    case 1://红 100
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = false;
                        break;
                    case 2://黄 110
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = false;
                        break;
                    case 3://绿 010
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = false;
                        break;
                    case 4://青 011
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = true;
                        break;
                    case 5://蓝 001
                        R_booleanArry[i][j] = false;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = true;
                        break;
                    case 6://紫 101
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = false;
                        B_booleanArry[i][j] = true;
                        break;
                    case 7://白 111
                        R_booleanArry[i][j] = true;
                        G_booleanArry[i][j] = true;
                        B_booleanArry[i][j] = true;
                        break;
                }
            }
        }
        /**
         * boolean 二维数组转化成 byte 数组
         * */
        byte[] R_byteArry = DataTypeUtils.getAdjustedData(R_booleanArry);
        byte[] G_byteArry  = DataTypeUtils.getAdjustedData(G_booleanArry);
        byte[] B_byteArry  = DataTypeUtils.getAdjustedData(B_booleanArry);
        byte[] reult = new byte[R_byteArry.length*3];
        /**
         * 拼接三个数组
         * */
        Helpful.catByte(R_byteArry,0,reult,0);
        Helpful.catByte(G_byteArry,0,reult,R_byteArry.length);
        Helpful.catByte(B_byteArry,0,reult,R_byteArry.length*2);

        return reult;
    }
}
