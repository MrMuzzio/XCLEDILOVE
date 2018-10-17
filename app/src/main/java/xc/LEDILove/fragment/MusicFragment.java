package xc.LEDILove.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xc.LEDILove.Bean.MusicBean;
import xc.LEDILove.R;
import xc.LEDILove.activity.MusicActivity;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.service.BleConnectService;
import xc.LEDILove.service.MusicPlayService;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.widget.LEDFrequencyView;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by xcgd on 2018/5/14.
 */

public class MusicFragment extends Fragment implements View.OnClickListener{
    private Context context;
    private List<Integer> resource_musics;
    private List<Integer> musicID;
    private FrameLayout LEDFrView_music;
    private LEDFrequencyView ledFrequencyView;
    private ServiceConnection serviceConnection;
    private BleConnectService.Mybinder serviceBinder;
    private MusicPlayService.myBinder musicBinder;
    private AssetManager assetManager;

    private ImageView iv_start_control;
    private ImageView iv_music_last;
    private ImageView iv_music_next;
    private SeekBar sb_music_progress;
    private TextView tv_current_music;
    private TextView tv_current_time;
    private TextView tv_max_time;
    private LinearLayout ll_back;
    private TextView scancount_txt;
    private TextView tv_head_left;
    private ViewPager vp_music_type;
    private RadioGroup rg_item_select;
    private String TAG;
    private ImageView iv_music_model;
    private ImageView iv_music_sync;
    private RadioButton rb_built;
    private RadioButton rb_local;
    private int play_model = 0;//0 循环    1 单曲循环   2 随机
    private MusicLocalFragment.LocalMusicCallback  localMusicCallback;
    private MusicBuiltinFragment.BuiltMusicCallback  builtMusicCallback;

    private MusicBuiltinFragment musicBuiltinFragment;
    private MusicLocalFragment musicLocalFragment;
    private List<MusicBean> musicBeans;
    private int currentMusicList = 0;// 0 内置  1 本地
//    private StaticDatas mStaticDatas;
    private MusicPlayService musicPlayService;
    private ServiceConnection music_serviceConnection;
    public MusicFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public MusicFragment (Context context){
        this.context=context;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_music,container,false);
        findView(view);
        initView();
        initService();
        return view;
    }

    @Override
    public void onDestroy() {
        if (music_serviceConnection!=null){
            context.unbindService(music_serviceConnection);
        }
        super.onDestroy();
    }

    private void initService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceBinder = (BleConnectService.Mybinder) iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent intent  = new Intent();
        intent.setClass(context,BleConnectService.class);
        context.bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        music_serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                musicBinder = (MusicPlayService.myBinder) iBinder;
                musicBinder.setBleServiceBinder(serviceBinder);
                //自动进入 音乐模式
//                if (serviceBinder!=null&&serviceBinder.getConnectedStatus()){
//                    musicBinder.setSync(true);
//                    iv_music_sync.setImageResource(R.mipmap.ic_sync_white_24dp);
//                    Toast.makeText(context, getString(R.string.sync_enable), Toast.LENGTH_SHORT).show();
//                    //音乐模式进入命令  ，此模式下 不等待判断返回值
//                    serviceBinder.sendData("LOL".getBytes(), false);
//                    musicBinder.setSync(true);
//                }
                checkRadioButton(1);
                musicBuiltinFragment.setSelectedIndex(0);
                musicLocalFragment.setSelectedIndex(-1);
                playMusic(0);
                stopMusic();
                musicBinder.setMusicPlayLinstener(new MusicPlayService.MusicPlayLinstener() {
                    @Override
                    public void onComplete() {
                        playMusic(index_next);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                musicBinder.release();
            }
        };
        context.bindService(new Intent(context,MusicPlayService.class),music_serviceConnection,BIND_AUTO_CREATE);
    }
    public void refreshLedview(Context context){
//        ledFrequencyView = new LEDFrequencyView(context);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        ledFrequencyView.setLayoutParams(params);
//        LEDFrView_music.addView(ledFrequencyView);
        ledFrequencyView.init();
        musicBinder.initData(ledFrequencyView,false);
    }
    private void findView(View view) {
        LEDFrView_music = (FrameLayout) view.findViewById(R.id.LEDFrView_music);
        iv_start_control = (ImageView) view.findViewById(R.id.iv_start_control);
        iv_start_control.setOnClickListener(this);
        iv_music_last = (ImageView) view.findViewById(R.id.iv_music_last);
        iv_music_last.setOnClickListener(this);
        iv_music_next = (ImageView) view.findViewById(R.id.iv_music_next);
        iv_music_next.setOnClickListener(this);
        sb_music_progress = (SeekBar) view.findViewById(R.id.sb_music_progress);
        tv_current_music =(TextView) view.findViewById(R.id.tv_current_music);
        tv_current_time =(TextView) view.findViewById(R.id.tv_current_time);
        tv_max_time =(TextView) view.findViewById(R.id.tv_max_time);
        iv_music_model =(ImageView) view.findViewById(R.id.iv_music_model);
        iv_music_model.setOnClickListener(this);
        iv_music_sync =(ImageView) view.findViewById(R.id.iv_music_sync);
        iv_music_sync.setOnClickListener(this);
        vp_music_type = (ViewPager) view.findViewById(R.id.vp_music_type);
        rg_item_select = (RadioGroup) view.findViewById(R.id.rg_item_select);
        rb_built = (RadioButton) view.findViewById(R.id.rb_built);
        rb_local = (RadioButton) view.findViewById(R.id.rb_local);
        askPermission();
        assetManager = context.getAssets();
    }
    private int currentMusic_index = -1;
    private int sb_index = 0;
    private String[] resource_music_name;
    private void initView() {
//        mStaticDatas= StaticDatas.getInstance();
        resource_musics = new ArrayList<>();
        resource_musics.add(R.raw.croatian_rhapsody);
        resource_musics.add(R.raw.musicdemo2);
        resource_musics.add(R.raw.musicdemo4);
        resource_music_name = getResources().getStringArray(R.array.resource_music_name);
        musicID = new ArrayList<>();
        musicID.add(R.raw.musicdemo2);
        musicBuiltinFragment= new MusicBuiltinFragment(context,resource_music_name);
        musicLocalFragment = new MusicLocalFragment(context);
        ledFrequencyView = new LEDFrequencyView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ledFrequencyView.setLayoutParams(params);
        LEDFrView_music.addView(ledFrequencyView);
        sb_music_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                Log.e(TAG, "onProgressChanged:"+i );
                sb_index = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicBinder.seekTo(sb_index);
            }
        });
        vp_music_type.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position==0){
                    return musicBuiltinFragment;
                }else {
                    return musicLocalFragment;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        vp_music_type.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected:"+position );
                checkRadioButton(position+1);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        rg_item_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e(TAG, "onCheckedChanged:"+i);
//                vp_music_type.setCurrentItem(i-1);
                setViewPager(i);
            }
        });
        checkRadioButton(1);
        musicBeans = new ArrayList<>();
        localMusicCallback = new MusicLocalFragment.LocalMusicCallback() {
            @Override
            public void onItemSelected(int i) {
                currentMusicList = 1;
                playMusic(i);
            }

            @Override
            public void onRefreshed() {
                musicBeans = musicLocalFragment.getMusicBeans();
            }
        };
        musicLocalFragment.setLocalMusicCallback(localMusicCallback);
        builtMusicCallback = new MusicBuiltinFragment.BuiltMusicCallback() {
            @Override
            public void onItemSedected(int i) {
                currentMusicList = 0;
                playMusic(i);
            }
        };
        musicBuiltinFragment.setBuiltMusicCallback(builtMusicCallback);
    }
    private void setViewPager(int id){
        switch (id){
            case R.id.rb_built:
                vp_music_type.setCurrentItem(0);
                break;
            case R.id.rb_local:
                vp_music_type.setCurrentItem(1);
                break;
        }
    }
    private void checkRadioButton(int index){
        switch (index){
            case 1:
                rg_item_select.check(R.id.rb_built);
                break;
            case 2:
                rg_item_select.check(R.id.rb_local);
                break;
        }
    }
    private int max = 0;//进度条最大值
    private int index = 0;//播放进度条 进度
    private void playMusic(int index){
        currentMusic_index = index;
        switch (currentMusicList){
            case 0:
                musicBuiltinFragment.setSelectedIndex(currentMusic_index);
                musicLocalFragment.setSelectedIndex(-1);
                playMusicById(currentMusic_index);
                break;
            case 1:
                musicLocalFragment.setSelectedIndex(currentMusic_index);
                musicBuiltinFragment.setSelectedIndex(-1);
                playMusicByPath(currentMusic_index);
                break;
        }
    }
    private void playMusicById(int index){
        musicBinder.playById(context,resource_musics.get(index),resource_music_name[currentMusic_index]);
        musicBinder.initData(ledFrequencyView,true);
        max = musicBinder.getDuration();
        sb_music_progress.setMax(max);
        tv_max_time.setText(getDateTimeFromMillisecond((long) max));

        refreshPathMessage();
        startMusic();
    }
    private void playMusicByPath(int index){

        String path = musicBeans.get(index).getPath();
        musicBinder.playByPath(context,path,musicBeans.get(currentMusic_index).getName()+"-"+musicBeans.get(currentMusic_index).getSinger());
        musicBinder.initData(ledFrequencyView,true);
        max = musicBinder.getDuration();
        sb_music_progress.setMax(max);
        tv_max_time.setText(getDateTimeFromMillisecond((long) max));
        refreshPathMessage();
        startMusic();
//        mediaPlayer = MediaPlayer.create(this,resource_musics.get(index));
    }
    private void refreshPathMessage(){
        index_next = getNextMusic();
        if (currentMusicList==1){
            MusicBean bean = musicBeans.get(currentMusic_index);
            tv_current_music.setText(getString(R.string.playing_music)+bean.getName()+"  "+
                    bean.getSinger()+"-"+bean.getAlbum()+"    "+getString(R.string.playing_music_next)+musicBeans.get(index_next).getName());
        }else {
            tv_current_music.setText(getString(R.string.playing_music)+resource_music_name[currentMusic_index]+"  "+
                    getString(R.string.playing_music_next)+resource_music_name[index_next]);
        }
    }
//    private void initData() {
//        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
//        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
//        Log.e("CaptureSizeRange",Visualizer.getCaptureSizeRange()[1]+"");//0为128；1为1024
//        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener(){
//
//            @Override
//            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
//                data=ledFrequencyView.refreshView(waveform);
//                if (isSync&&serviceBinder.getConnectedStatus()){
//                    Log.e("sendData000->", Helpful.MYBytearrayToString(data) + " SIZE=" + data.length);
//                    serviceBinder.sendData(data,false);
//                }
//            }
//
//            @Override
//            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
////                Log.e("onFftDataCapture","调用了！");
//                byte[] model = new byte[fft.length / 2 + 1];
//                model[0] = (byte) Math.abs(fft[1]);
//                int j = 1;
//
//                for (int i = 2; i < 18;) {
//                    model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
//                    i += 2;
//                    j++;
//                }
//
//                ledFrequencyView.refreshView(model);
////                visView_music.updateVisualizer(model);
//            }
//        } , Visualizer.getMaxCaptureRate()/2, true, false);
//        Log.e("采样频率",Visualizer.getMaxCaptureRate()/2+"");//10000mHz=10Hz
////        visualizer.setEnabled(true);//这个设置必须在参数设置之后，表示开始采样
//        visualizer.setEnabled(true);
//    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==001){
                index=musicBinder.getCurrentPosition();
                tv_current_time.setText(getDateTimeFromMillisecond((long) index));
                sb_music_progress.setProgress(index);
                handler.sendEmptyMessageDelayed(001,1000);
            }
        }
    };
    private void stopMusic(){
        musicBinder.pause();
        iv_start_control.setImageResource(R.mipmap.ic_fragment_music_pause);
//        visualizer.setEnabled(false);
        handler.removeMessages(001);
    }
    private void startMusic(){
        musicBinder.play(0);
        iv_start_control.setImageResource(R.mipmap.ic_fragment_music_play);
        handler.sendEmptyMessageDelayed(001,1000);
    }
    private boolean isSync = false;
    private int index_next=-1;
    private int getNextMusic(){
        int result = 0;
        switch (play_model){
            //列表循环
            case 0:
                if (currentMusicList==0){
                    if (currentMusic_index==resource_musics.size()-1){
                        result =  0;
                    }else{
                        result =  currentMusic_index+1;
                    }
                }else if (currentMusicList==1){
                    if (currentMusic_index==musicBeans.size()-1){
                        result =  0;
                    }else{
                        result =  currentMusic_index+1;
                    }
                }

                break;
            //单曲循环
            case 1:
                result =  currentMusic_index;
                break;

            case 2:
                if (currentMusicList==0){
                    result =  (int)((Math.random()*(resource_musics.size())));
                }else if (currentMusicList ==1){
                    result =  (int)((Math.random()*(musicBeans.size())));
                }

                break;
            default:
                result =  0;
                break;
        }
        return  result;
    }
    private int getLastMusic(){
        int result = 0;
        switch (play_model){
            //列表循环
            case 0:
                if (currentMusicList==0){//内置列表
                    if (currentMusic_index==0){
                        result =  resource_musics.size()-1;
                    }else{
                        result =  currentMusic_index-1;
                    }
                } else if (currentMusicList==1) {//本地列表
                    if (currentMusic_index==0){
                        result =  musicBeans.size()-1;
                    }else{
                        result =  currentMusic_index-1;
                    }
                }
                break;
            //单曲循环
            case 1:
                result =  currentMusic_index;
                break;
            case 2:
                if (currentMusicList==0){
                    result =  (int)((Math.random()*(resource_musics.size())));
                }else if (currentMusicList==1){
                    result =  (int)((Math.random()*(musicBeans.size())));
                }
                break;
            default:
                result =  0;
                break;
        }
        return  result;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                result = result && grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
            if (!result) {

                Toast.makeText(context, "授权结果（至少有一项没有授权），result="+result, Toast.LENGTH_LONG).show();
                // askPermission();
            } else {
                //授权成功
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_start_control:

                if (currentMusic_index == -1) {
                    playMusic(0);
                    return;
                }
                if (musicBinder.getMediaplayStatue()) {
                    closeSync();
                    iv_start_control.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                    stopMusic();
                } else {
                    openSync();
                    iv_start_control.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                    startMusic();
                }
                break;
            case R.id.iv_music_last:
                playMusic(getLastMusic());
                break;
            case R.id.iv_music_next:
                playMusic(index_next);
                break;
            case R.id.iv_music_model:
                switch (play_model) {
                    case 1:
                        Toast.makeText(context, getString(R.string.play_model_shuffle), Toast.LENGTH_SHORT).show();
                        iv_music_model.setImageResource(R.mipmap.ic_shuffle_white_24dp);
                        play_model = 2;
                        break;
                    case 0:
                        Toast.makeText(context, getString(R.string.play_model_repeat_one), Toast.LENGTH_SHORT).show();
                        iv_music_model.setImageResource(R.mipmap.ic_repeat_one_white_24dp);
                        play_model = 1;
                        break;
                    case 2:
                        Toast.makeText(context, getString(R.string.play_model_repeat), Toast.LENGTH_SHORT).show();
                        iv_music_model.setImageResource(R.mipmap.ic_repeat_white_24dp);
                        play_model = 0;
                        break;
                }
                refreshPathMessage();
                break;
//            case R.id.iv_music_sync:
//                if (serviceBinder.getConnectedStatus()) {
//                    if (!musicBinder.getSync()) {
//
//                        if (StaticDatas.LEDVersion>0){
//
//                        }
//                        iv_music_sync.setImageResource(R.mipmap.ic_sync_white_24dp);
//                        Toast.makeText(context, getString(R.string.sync_enable), Toast.LENGTH_SHORT).show();
//                        //音乐模式进入命令  ，此模式下 不等待判断返回值
//                        serviceBinder.sendData("LOL".getBytes(), false);
//                        musicBinder.setSync(true);
//                        isSync = true;
//                    } else {
//                        iv_music_sync.setImageResource(R.mipmap.ic_sync_disabled_white_24dp);
//                        Toast.makeText(context, getString(R.string.sync_disenable), Toast.LENGTH_SHORT).show();
//                        //普通模式进入命令  ，等待判断返回值
//                        serviceBinder.sendData("DNF".getBytes(), false);
//                        musicBinder.setSync(false);
//                        isSync = false;
//                    }
//                } else {
//                    Toast.makeText(context, getString(R.string.unconnect_device), Toast.LENGTH_SHORT).show();
//                }
//                break;
        }
    }
    List<String> permissions = new ArrayList<String>();
    private boolean askPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int RECORD_AUDIO = context.checkSelfPermission( Manifest.permission.RECORD_AUDIO );
            if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
            } else
                return false;
        } else
            return false;
        return true;

    }
    /**
     * 将毫秒转化成固定格式的时间
     * 时间格式: yyyy-MM-dd HH:mm:ss
     *
     * @param millisecond
     * @return
     */
    public  String getDateTimeFromMillisecond(Long millisecond){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        Date date = new Date(millisecond);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    public void openSync() {
        if (!musicBinder.getSync()){
//            iv_music_sync.setImageResource(R.mipmap.ic_sync_white_24dp);
//            Toast.makeText(context, getString(R.string.sync_enable), Toast.LENGTH_SHORT).show();
            //音乐模式进入命令  ，此模式下 不等待判断返回值
            serviceBinder.sendData("LOL".getBytes(), false);
            musicBinder.setSync(true);
            isSync = true;
        }
    }
    public void closeSync() {
        if (musicBinder.getSync()){
//            iv_music_sync.setImageResource(R.mipmap.ic_sync_disabled_white_24dp);
//            Toast.makeText(context, getString(R.string.sync_disenable), Toast.LENGTH_SHORT).show();
            //普通模式进入命令  ，等待判断返回值
            serviceBinder.sendData("DNF".getBytes(), false);
            musicBinder.setSync(false);
            isSync = false;
        }
        if (musicBinder.getMediaplayStatue()){
            iv_start_control.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
            stopMusic();
        }
    }
    public boolean getSyncStatus(){
        return isSync;
    }
}
