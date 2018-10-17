package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import xc.LEDILove.R;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.picture.GifImageDecoder;
import xc.LEDILove.thirdparty.WenXinShare;
import xc.LEDILove.utils.BitmapUtils;
import xc.LEDILove.widget.LEDPaintView;

/**
 * Created by xcgd on 2018/5/14.
 */

public class GifFragment extends Fragment{
    private Context context;
    private final String TAG = GifFragment.class.getSimpleName();
    private LinearLayout ll_gif;
    //    GifFrame gifFrame = null;00
    private GifImageDecoder gifDecoder;
    private int pointCount = 32;
    private int [][] pointData;
    private List<int []> BitData;
    private GridView gv_list_gif;
    private RelativeLayout rl_select_mode;
    private TextView tv_current_select;
    private ImageView iv_exit_selectMode;
    private ImageView iv_delete;
    private ImageView iv_share;
    private File[] files;
    private GifFragment.GifAdapter gifAdapter;
    private int width;
    private int imageWidth;
    private RelativeLayout ll_gif_activity;
    private LinearLayout ll_back;
    private List<Integer> chooses;
    private boolean isSelectedMoudle = false;
    private String filePath ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif,container,false);
        findView(view);
        getFilePath();
        getGifFiles();
        getScreenWidth();
        selectItems = new ArrayList<>();
        gifAdapter = new GifFragment.GifAdapter();
        gv_list_gif.setAdapter(gifAdapter);
        pointData = new int[pointCount][pointCount];
        widgetListener();
        return view;
    }

    private void widgetListener() {
        iv_exit_selectMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelectedMoudle=false;
                selectItems.clear();
                gifAdapter.notifyDataSetChanged();
            }
        });
        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectItems!=null){
                    for (int i=0;i<selectItems.size();i++){
                        files[selectItems.get(i)].delete();
                    }
                    getGifFiles();
                    selectItems.clear();
                    gifAdapter.notifyDataSetChanged();
                }
            }
        });
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WenXinShare wenXinShare = new WenXinShare(context);
                wenXinShare.showSharePopupwindow("picture",ll_gif_activity, new WenXinShare.ShareListener() {
                    @Override
                    public void onTypeSelected(int scene) {
                        wenXinShare.sharePicByFile(files[selectItems.get(0)],scene,"001");
                    }

                });
                for (int i=0;i<selectItems.size();i++){

                }
            }
        });
    }


    private void findView(View view) {
        gv_list_gif = (GridView) view.findViewById(R.id.gv_list_gif);
        ll_gif_activity = (RelativeLayout)view.findViewById(R.id.ll_gif_activity);
        ll_back = (LinearLayout) view.findViewById(R.id.ll_back);
        tv_current_select = (TextView) view.findViewById(R.id.tv_current_select);
        iv_exit_selectMode = (ImageView) view.findViewById(R.id.iv_exit_selectMode);
        iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
        iv_share = (ImageView) view.findViewById(R.id.iv_share);
        rl_select_mode = (RelativeLayout) view.findViewById(R.id.rl_select_mode);
        rl_select_mode.setVisibility(View.GONE);
    }
    private void getGifFiles() {
        File f= new File(filePath);
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
    }
    private void getScreenWidth() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density =  metrics.density;
        width = metrics.widthPixels;
        imageWidth = (width-dp2px(context,5*2+2))/4;
    }
    private final int REFRESHGIF = 101;
    @SuppressLint("HandlerLeak")
    private Handler mhander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESHGIF:
                    refreshGif();
                    break;
            }
        }
    };
    private int frameCount = 0;
    private int currentIndex = 0;
    private int  timeGap = 0;
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final Bitmap.CompressFormat format) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }
    @SuppressLint("ResourceType")
    void showGif2(File file)
    {
        gifDecoder = new GifImageDecoder();
        try {
            gifDecoder.read(new BufferedInputStream(new FileInputStream(file)));
            frameCount =gifDecoder.getFrameCount();
            timeGap = gifDuration/frameCount;
            Bitmap bit = gifDecoder.getFrame(0);
            Log.e(TAG, "showGif2: >>byte size"+bitmap2Bytes(bit, Bitmap.CompressFormat.PNG).length );
            refreshGif();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshGif() {
        if (BitData==null){
            BitData = new ArrayList<>();
        }else {
            BitData.clear();
        }
        Bitmap bit = gifDecoder.getFrame(currentIndex);

        bit = BitmapUtils.scaleBitmap(bit,pointCount,pointCount);
        BitData = BitmapUtils.getBitmapData(bit);
        ledpv_gif.set8BitData(BitData);
        currentIndex++;
        if (currentIndex==frameCount){
            currentIndex=0;
        }
        mhander.sendEmptyMessageDelayed(REFRESHGIF,timeGap);
    }
    private List<Integer> selectItems;

    public void resetResources() {
        getFilePath();
        getGifFiles();
        if (gifAdapter!=null){
            gifAdapter.notifyDataSetChanged();
        }
    }
    private void getFilePath(){
        filePath =Environment.getExternalStorageDirectory()+"/LEDILOVE/gif/"+ StaticDatas.LEDHight+""+StaticDatas.LEDWidth+"/";
    }
    class GifAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public Object getItem(int i) {
            return files[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            View view;
            final GifFragment.GifVh gifVh;
            if (convertView!=null){
                view = convertView;
                gifVh = (GifFragment.GifVh) view.getTag();
            }else {
                view = View.inflate(context,R.layout.item_gridview_gif,null);
                gifVh = new GifFragment.GifVh();
                gifVh.giv_gif = (GifImageView) view.findViewById(R.id.giv_gif);
                gifVh.iv_selected = (ImageView) view.findViewById(R.id.iv_selected);
                view.setTag(gifVh);
            }
            tv_current_select.setText(selectItems.size()+"/8");
            if (isSelectedMoudle){
                rl_select_mode.setVisibility(View.VISIBLE);
            }else {
                rl_select_mode.setVisibility(View.GONE);
            }
            if (isInlist(i,selectItems)!=-1){
                gifVh.iv_selected.setVisibility(View.VISIBLE);
            }else {
                gifVh.iv_selected.setVisibility(View.GONE);
            }
            gifVh.giv_gif.setMinimumWidth(imageWidth);
            if (StaticDatas.LEDWidth==32){
                gifVh.giv_gif.setMinimumHeight(imageWidth);//3232
            }else if (StaticDatas.LEDWidth==48){
                gifVh.giv_gif.setMinimumHeight((int) (imageWidth*0.83));//4048
            }else {
                gifVh.giv_gif.setMinimumHeight(imageWidth);//默认正方形
            }
            try {
                final GifDrawable gifFromFile = new GifDrawable(files[i]);
                gifVh.giv_gif.setImageDrawable(gifFromFile);
//                gifFromFile.seekToFrame(0);
//                gifFromFile.stop();
                gifVh.giv_gif.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick:>>>"+ gifFromFile.getDuration());
//                        gifFromFile.getDuration();
                        if (isSelectedMoudle){
                            int posotion = 0;
                            if ((posotion=isInlist(i,selectItems))==-1){
                                if (selectItems.size()<8){
                                    selectItems.add(i);
                                }
                            }else {
                                selectItems.remove(posotion);
                                if (selectItems.size()==0){
                                    isSelectedMoudle=false;
                                }
                            }
                            notifyDataSetChanged();
                        }else if (!isSelectedMoudle){
//                            showPopuwindow(files[i],gifFromFile.getDuration());//动画点阵预览
                        }
                    }
                });
                gifVh.giv_gif.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (isSelectedMoudle){
                            return true;
                        }
                        if (!isSelectedMoudle){
                            isSelectedMoudle = true;
                            selectItems.add(i);

                            notifyDataSetChanged();
                        }
                        return true;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return view;
        }
    }

    private int isInlist(int i, List<Integer> selectItems) {
        for (int j=0;j<selectItems.size();j++){
            if (i==selectItems.get(j)){
                return j;
            }
        }
        return -1;
    }

    private PopupWindow popupWindow;
    private LEDPaintView ledpv_gif;
    private int gifDuration;
    private Button btn_send_gif;
    private void showPopuwindow(File file,int duration) {
        if(popupWindow!=null){
            popupWindow.dismiss();
            popupWindow=null;
        }
        gifDuration = duration;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View conterView = inflater.inflate(R.layout.popuwindow_gif,null);
        ledpv_gif = (LEDPaintView) conterView.findViewById(R.id.ledpv_gif);
        btn_send_gif = (Button) conterView.findViewById(R.id.btn_send_gif);
        btn_send_gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        popupWindow = new PopupWindow(conterView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow.showAtLocation(ll_gif_activity, Gravity.CENTER,0,0);
        ledpv_gif.init(context,32);
        ledpv_gif.setFocuseable(false);
        showGif2(file);
    }

    class GifVh {
        GifImageView giv_gif;
        ImageView iv_selected;
    }

    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    public GifFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public GifFragment (Context context){
        this.context=context;
    }
}
