package xc.LEDILove.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.bluetooth.CommandHelper;
import xc.LEDILove.font.FontUtils;
import xc.LEDILove.service.BleConnectService;
import xc.LEDILove.utils.BitmapUtils;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.widget.LEDFrequencyView;
import xc.LEDILove.widget.LEDPaintView;
import xc.LEDILove.widget.LEDSurfaceView;
/**
 * ━━━━ Code is far away from ━━━━━━
 * 　　   () 　　　 ()
 * 　　  ( ) 　　　( )
 * 　 　 ( ) 　　　( )
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　┻　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━ bug with the more protecting ━━━
 * */
public class PaletteActivity extends Activity implements View.OnClickListener {
    private final String TAG = PaletteActivity.class.getSimpleName();
    private LEDPaintView ledPaintView;
    private RadioGroup ll_color_select;
    private RadioGroup ll_control;
    private int paintColor = 1;
    private int BGColor = 0;
    private int control_state = 0;//0 画笔 1 橡皮擦 2 油漆桶 3 pic
    private ImageButton ibtn_clear;
    private ImageButton btn_undo;
    private ImageButton btn_save;
    private ImageButton ib_pic;
    private ImageButton btn_preview;
    private LinearLayout ll_back;
    private TextView scancount_txt;
    private TextView tv_head_left;
    private final int pointCount = 32;
    private TextView tv_dimensions;
    private ServiceConnection serviceConnection;
    private BleConnectService.Mybinder serviceBinder;
    private ProgressDialog pd;

    @Override
    protected void onPause() {
        serviceBinder.removepaletteListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (serviceBinder!=null&&paletteListener!=null){
            serviceBinder.setPaletteListener(paletteListener);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (serviceConnection!=null){
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        scrollToFinishActivity();//左滑退出activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledPaintView = new LEDPaintView(PaletteActivity.this);

        setContentView(R.layout.activity_palettle);
        findById();
        initViewState();
        initService();
    }
    private void initService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceBinder = (BleConnectService.Mybinder) iBinder;
                serviceBinder.setPaletteListener(paletteListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent  = new Intent();
        intent.setClass(this,BleConnectService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }
    private int CMD_SENDING =1001;
    @SuppressLint("HandlerLeak")
    private Handler myhander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==CMD_SENDING){
                if (serviceBinder.getConnectedStatus()){
//                    Log.e(TAG, "handleMessage:  "+sendcmd.length);
                    showPressDialog(getString(R.string.dialog_sending));

                    sendData();
                }else {
                    Toast.makeText(PaletteActivity.this,getString(R.string.connectstate),Toast.LENGTH_SHORT).show();
                }
            }
//            switch (msg.what){
//                case CMD_SENDING:
//
//                break;
//            }
        }
    };
    private BleConnectService.PaletteListener paletteListener = new BleConnectService.PaletteListener() {
        @Override
        public void onSendData(int code) {
            switch (code){
                case BleConnectService.ServiceListener.sendData_fail:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            if (pd != null) {
//                                pd.dismiss();
//                            }
                            if (pd!=null){
                                pd.dismiss();
                            }
                            showYCDialog(getString(R.string.failedOperation));
                            serviceBinder.disConnected();
                        }
                    });
                    break;
                case BleConnectService.ServiceListener.sendData_succeed:
                    Log.e(TAG, "onSendData: succeed");
//                    if (CommandHelper.dataType_data==sendCmdType){//如果是发数据包  判断是否还有数据需要发送
                    //判断是否还有剩余帧需要发送
                    if (hasSendData < edit_byte.length) {

                        myhander.sendEmptyMessage(CMD_SENDING);
                    }
                    else {
//                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                        //发送成功添加到数据库
//                        savaData();
                        myhander.post(new Runnable() {

                            @Override
                            public void run() {
                                if (pd!=null){
                                    pd.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sendsuccess),
                                        Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                    break;
//                    else {
////                        MEHandler.sendEmptyMessage(DISMISS_DIALOG);
//                        //发送成功添加到数据库
//                        savaData();
//                        if (!isConnectIntime){//首次连接 发送开机命令不 提示
//
//                            MEHandler.post(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.successfulOperation),
//                                            Toast.LENGTH_SHORT).show();
//                                }
//
//                            });
//                        }
////                        //如果是刚连接成功 并且成功发送开机命令    再发送数据
////                        if (sendCmdType==CommandHelper.dataType_power&&isConnectIntime){
////                            mStaticDatas.isSupportMarFullColor = false;
////                            MEHandler.sendEmptyMessage(REFRESH_VP);
////                            isConnectIntime = false;
////                            if (isNeedSendData){
////                                sendCmdType=CommandHelper.dataType_data;
////                                //发送数据
////                                edit_byte = ledView.getTextByte(isSupportMarFullColor);
////                                hasSendData = 0;
////                                MEHandler.sendEmptyMessage(CMD_SENDING);
////                            }
////                        }
//                    }

//                case BleConnectService.ServiceListener.sendData_timeOut:
//                    MEHandler.sendEmptyMessageDelayed(CMD_TIMEOUT_RECEIVE,0);
//                    break;
            }
        }
    };
    private void initViewState() {
        ledPaintView.init(this,pointCount);
        tv_dimensions.setText(pointCount+" x "+pointCount);
        ledPaintView.setFocuseable(true);
        int [] s= new int[2];
        ledPaintView.getLocationOnScreen(s);
        Log.e(TAG, "onCreate: "+s[0] +"   "+s[1]);
        ll_color_select.check(R.id.rb_red);
        ll_control.check(R.id.rb_pen);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1001&&resultCode==1001){
            String path;
            if (!(path=data.getStringExtra("file")).equals("")){
                Bitmap bitmap =  BitmapUtils.getBitmap(path);
                ledPaintView.setData(BitmapUtils.getBitmapPointData(BitmapUtils.scaleBitmap(bitmap,pointCount,pointCount)));
            }
        }
    }

    private void findById() {
        ledPaintView = (LEDPaintView) findViewById(R.id.sfv_paint);
        ll_color_select = (RadioGroup) findViewById(R.id.ll_color_select);
        ll_color_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged: "+ i);
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
                        ledPaintView.setBGColor(BGColor);
                        break;
                    case 3:
                        break;
                }

            }
        });
        ll_control = (RadioGroup) findViewById(R.id.ll_control);
        ll_control.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged: "+i );
                switch (i){
                    case R.id.rb_pen:
                        control_state = 0;
                        ledPaintView.setPaintColor(paintColor);
                        ll_color_select.check(translateColorValuesToId(paintColor));
                        break;
                    case R.id.rb_rubber:
                        control_state = 1;
                        ledPaintView.setPaintColor(BGColor);
                        break;
                    case R.id.rb_bucket:
                        control_state = 2;
                        ll_color_select.check(translateColorValuesToId(BGColor));
                        break;
                }
            }
        });
        ibtn_clear = (ImageButton) findViewById(R.id.ibtn_clear);
        btn_undo = (ImageButton) findViewById(R.id.btn_undo);
        btn_save = (ImageButton) findViewById(R.id.btn_save);
        ib_pic = (ImageButton) findViewById(R.id.ib_pic);
        ibtn_clear.setOnClickListener(this);
        btn_preview = (ImageButton) findViewById(R.id.btn_preview);
        btn_preview.setOnClickListener(this);
        ib_pic.setOnClickListener(this);
        btn_undo.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(this);
        scancount_txt = (TextView) findViewById(R.id.scancount_txt);
        scancount_txt.setText(getString(R.string.palte_simplename));
        tv_head_left = (TextView) findViewById(R.id.tv_head_left);
        tv_head_left.setText(getString(R.string.set));
        tv_dimensions = (TextView) findViewById(R.id.tv_dimensions);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtn_clear:
                ledPaintView.clearCanvas();
                paintColor = 1;
                BGColor = 0;
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.btn_undo:
                ledPaintView.unDo();
                break;
            case R.id.btn_save:
                ledPaintView.save();
                break;
            case R.id.ib_pic:
                Intent picintent = new Intent(PaletteActivity.this,PicPreViewActivity.class);
                startActivityForResult(picintent,1001);
                break;
            case R.id.btn_preview:
                hasSendData = 0;
                edit_byte = getRGBData(ledPaintView.getPointData());
                myhander.sendEmptyMessage(CMD_SENDING);
                break;
        }
    }
    private int hasSendData=0;
    private byte[] edit_byte;
    private int header = 0;
    private byte[] sendcmd;
    /***
     * 执行发送数据事件
     */
    private void sendData() {
        String cmdHeader;
        byte[] waitSendData;
        if (hasSendData == 0) {
            //需要转换为16进制
//            Log.e(TAG, "sendTxtData: dataType>>>>"+dataType);
//            cmdHeader = head+ selectedParams.wordSize + ledWidthCmd + dataType + selectedParams.switchValue + selectedParams.model + color2Cmd(selectedParams.color) + "00";
            cmdHeader = "BT03120" //命令头
                    + "32" //点阵高度
                    + "0032"//点阵数据宽度
                    + "3"//颜色代码
                    + "1"//信息号
                    +"3"//移动方式
                    +"1fff" //
                    +"00";

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
//        Log.e(TAG, "sendData: "+Helpful.MYBytearrayToString(sendcmd) );
        serviceBinder.sendData(sendcmd,true);
//        sendCmd(sendcmd);
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
        byte[] R_byteArry = getAdjustedData(R_booleanArry);
        byte[] G_byteArry  = getAdjustedData(G_booleanArry);
        byte[] B_byteArry  = getAdjustedData(B_booleanArry);
        byte[] reult = new byte[R_byteArry.length*3];
        /**
         * 拼接三个数组
         * */
        Helpful.catByte(R_byteArry,0,reult,0);
        Helpful.catByte(G_byteArry,0,reult,R_byteArry.length);
        Helpful.catByte(B_byteArry,0,reult,R_byteArry.length*2);

        return reult;
    }
    private void showPressDialog(String message){
        if (pd==null){
            pd = new ProgressDialog(PaletteActivity.this);
            pd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(),getString(R.string.cancelOperation),Toast.LENGTH_SHORT).show();
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
    /***
     * 显示对话框
     * @param erroMsg
     */
    private void showYCDialog(final String erroMsg) {
        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new android.app.AlertDialog.Builder(PaletteActivity.this)
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
    /**
     *
     * 将布尔型二维数组转化成一维十六进制数组  作为最终数据
     *
     */
    public byte[] getAdjustedData(boolean[][] datass){
        List<Byte> results = new ArrayList<>();
//        Log.e("datass.length",datass.length+"");
//        Log.e("datass[0].length",datass[0].length+"");
//        realLEDWidget = datass[0].length;
        for (int i=0;i<datass.length;i++){
            //用一个最大的十六进制数 与二进制进行|运算 最低位参与运算  如果是0 最低位变成1 否则为0
            //然后再将新的十六进制数 左移一位
            byte bite = 0x00;
            int position = 0;
            int m = 0;//行尾补齐计数
            for (int j=0 ;j<datass[0].length;j++){
                byte boolea_bite = 0b0;

                if (datass[i][j]){
                    boolea_bite=0b1;
                }
                bite= (byte) (( bite) <<1);//全部左移一位  最高位去掉 最低位0补齐
                bite= (byte) (( bite|boolea_bite));//最低位（0）或上boolean数据  即将boolean数据插入到最低位
                position+=1;
                if (position==8){//放满了 添加到集合中
//                    Log.e("bite",(bite&0xff)+"");
//                    Log.e("bite",(bite)+"");
//                    results.add((byte) (bite&0xff));
                    results.add((byte) (bite));
                    bite=0x00;
                    position=0;
                }
                if ((j+1==datass[0].length)&m==0) {//位图末尾  需要换行
                    if ((datass[0].length%8)==0){//如果是八的倍数就不需要做末尾补齐处理，
                        // 如果没有这个判断，当位图数量是八的倍数时，在每行后面会添加八位的空字节  2018/01/01

                    }else {

                        bite = (byte) (bite<<(8-position));
//                        Log.e("bite_end>>>>"+m,bite+"");
                        m+=1;
                        results.add(bite);
//                    results.add((byte) (bite&0xff));
                        bite=0x00;
                        position=0;
                    }
                }
                m=0;
            }
        }
        byte[] data =new byte[results.size()];
        for (int k = 0;k<results.size();k++){
            data[k] =  (results.get(k));
        }
        return  data;
    }
}
