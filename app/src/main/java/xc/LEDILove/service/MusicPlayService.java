package xc.LEDILove.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.activity.MainActivity;
import xc.LEDILove.activity.MusicActivity;
import xc.LEDILove.utils.Helpful;
import xc.LEDILove.widget.LEDFrequencyView;

/**
 * Created by xcgd on 2018/5/30.
 */

public class MusicPlayService extends Service{
    private final String TAG  = MusicPlayService.class.getSimpleName();
    private static BleConnectService.Mybinder binder;
    private myBinder musicbinder;
    private MediaPlayer mediaPlayer;
    private Notification notification;
    public interface MusicPlayLinstener{
        void onComplete();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer!=null){
            mediaPlayer.release();
//            mediaPlayer.reset();
        }
        musicbinder.clearNotification();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
//        notification = new Notification();
//        notification.
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        musicbinder = new myBinder();
        return musicbinder;
    }
    private String songname = "";
    public   class myBinder extends Binder{
        private Visualizer visualizer;
        private boolean isSync;
        private NotificationManager notifyManager;
        private LEDFrequencyView ledFrequencyView;
        private int currentMusic_index = -1;
        private List<Integer> resource_musics;
        private MusicPlayLinstener musicPlayLinstener;
        private Context context;

        public void setMusicPlayLinstener(MusicPlayLinstener musicPlayLinstener){
            this.musicPlayLinstener = musicPlayLinstener;
        }
        public void playById(Context context, int id,String name){
            this.context =context;
            if (mediaPlayer!=null){
                mediaPlayer.release();
            }
            if (visualizer!=null){
                visualizer.setEnabled(false);
                visualizer.release();
            }
            mediaPlayer = MediaPlayer.create(context,id);
            songname = name;
        }
        public void playByPath(Context context,String path,String name){
            this.context =context;
            if (mediaPlayer!=null){
                mediaPlayer.reset();
            }
            if (visualizer!=null){
                visualizer.setEnabled(false);
                visualizer.release();
            }
            try {
                mediaPlayer.setDataSource(context, Uri.parse(path));
                mediaPlayer.prepare();
                songname=name;
//                sendSimplestNotificationWithAction(context,name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void setSync(boolean sync){
            this.isSync = sync;
        }
        public boolean getSync(){
            return isSync;
        }

        /**
         * @param type 状态0 为MainActivity 1 为MusicActivity
         */
        public void play(int type){
            mediaPlayer.start();
            sendSimplestNotificationWithAction(context,songname,type);
            visualizer.setEnabled(true);
        }
        public void stop(){
            mediaPlayer.stop();
            visualizer.setEnabled(false);
        }
        public int getDuration(){
            return mediaPlayer.getDuration();
        }
        public int getCurrentPosition(){
            return mediaPlayer.getCurrentPosition();
        }
        public void pause(){
            mediaPlayer.pause();
            visualizer.setEnabled(false);
            musicbinder.clearNotification();
        }
        public void seekTo(int position){
            mediaPlayer.seekTo(position);
        }
        public void release(){
            mediaPlayer.stop();
            mediaPlayer.release();
            if (visualizer!=null){
                visualizer.setEnabled(false);
            }
        }
        public boolean getMediaplayStatue(){
            boolean result  =false;
            try {
                result =  mediaPlayer.isPlaying();
            } catch (IllegalStateException  e) {
                mediaPlayer = null;
                mediaPlayer = new MediaPlayer();
            }
            return result;
        }
        public void setBleServiceBinder(BleConnectService.Mybinder binder){
            MusicPlayService.binder = binder;
        }
        private byte[] data;
        public void initData( final LEDFrequencyView ledFrequencyView,boolean enable) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    musicPlayLinstener.onComplete();
                }
            });
            this.ledFrequencyView = ledFrequencyView;
            if (visualizer!=null){
                visualizer=null;
            }
            visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            visualizer.setEnabled(false);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            Log.e("CaptureSizeRange",Visualizer.getCaptureSizeRange()[1]+"");//0为128；1为1024
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener(){

                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    data=ledFrequencyView.refreshView(waveform);
                    if (isSync&&binder.getConnectedStatus()){
                        Log.e("sendData000->", Helpful.MYBytearrayToString(data) + " SIZE=" + data.length);
                        binder.sendData(data,false);
                    }
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//                Log.e("onFftDataCapture","调用了！");
                    byte[] model = new byte[fft.length / 2 + 1];
                    model[0] = (byte) Math.abs(fft[1]);
                    int j = 1;

                    for (int i = 2; i < 18;) {
                        model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                        i += 2;
                        j++;
                    }

                    ledFrequencyView.refreshView(model);
//                visView_music.updateVisualizer(model);
                }
            } , Visualizer.getMaxCaptureRate()/2, true, false);
            Log.e("采样频率",Visualizer.getMaxCaptureRate()/2+"");//10000mHz=10Hz
//        visualizer.setEnabled(true);//这个设置必须在参数设置之后，表示开始采样
            visualizer.setEnabled(enable);
        }
        /**
         * 发送一个点击跳转到MainActivity的消息
         * @param context
         * @param name
         * @param type  状态0 为MainActivity 1 为MusicActivity
         */
        private void sendSimplestNotificationWithAction(Context context,String name,int type) {
            notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //获取PendingIntent
            Intent mainIntent;
            if (type==0){
                mainIntent = new Intent(context, MainActivity.class);
            }else {
                mainIntent = new Intent(context, MusicActivity.class);
            }
//            Intent mainIntent = new Intent(context, MainActivity.class);
            PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //创建 Notification.Builder 对象
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    //点击通知后自动清除
//                    .setAutoCancel(true)
                    .setContentTitle(context.getResources().getString(R.string.app_name)+" "+context.getResources().getString(R.string.notification_playing))
                    .setContentText(name)
                    .setContentIntent(mainPendingIntent);
            //发送通知
            notifyManager.notify(3, builder.build());

        }
        public void clearNotification(){
            if (notifyManager!=null){
                notifyManager.cancel(3);
            }
        }
    }
}
