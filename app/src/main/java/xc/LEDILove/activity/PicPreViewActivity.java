package xc.LEDILove.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidviewhover.BlurLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.bluetooth.StaticDatas;
import xc.LEDILove.utils.TimonLibary;

public class PicPreViewActivity extends BaseActivity {
    private final String TAG = PicPreViewActivity.class.getSimpleName();
    private GridView gv_preview;
    private GridView gv_preview_add;
    private TextView tv_no_data;
    private int width;
    private int imageWidth;
    private GridViewAdapter gridViewAdapter;
    private GridViewAdapterAdd gridViewAdapterAdd;
    private LinearLayout ll_back;
    private TextView tv_head_left;
    private TextView scancount_txt;
    private String filepath;
    private String filepath_bit;
    private File[] files;
    private File[] files_add;
    private int require_type = 0;//0 画板请求（只需要加载自定义的图片文件）  1 快速发送请求（需要加载当前尺寸所有图片文件）
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        filePaths.clear();
//        setActivityResult("");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scrollToFinishActivity();//左滑退出activity
    }
    private void setActivityResult(String msg) {
        Log.e(TAG, "setActivityResult: >>>"+msg );
        Intent intent = new Intent();
        intent.putExtra("file",msg);
        if (require_type==0){
            setResult(1001,intent);
        }else {
            setResult(1002,intent);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_pre_view);
        gv_preview =(GridView) findViewById(R.id.gv_preview);
        gv_preview_add =(GridView) findViewById(R.id.gv_preview_add);
        tv_no_data = (TextView) findViewById(R.id.tv_no_data);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_head_left = (TextView)findViewById(R.id.tv_head_left);
        scancount_txt = (TextView)findViewById(R.id.scancount_txt);
        scancount_txt.setText(getString(R.string.gallery));
        require_type = getIntent().getIntExtra("TYPE",0);
        Log.e(TAG, "onCreate: require_type>>>"+require_type );
        getScreenWidth();
        getFilePath();
        getPicFile();
        if (require_type==0){
            tv_head_left.setText(getString(R.string.PhotoGallery));
            if (files!=null&&files.length>0){
                gridViewAdapter = new GridViewAdapter();
                gv_preview.setAdapter(gridViewAdapter);
                tv_no_data.setVisibility(View.GONE);
                gv_preview_add.setVisibility(View.GONE);
            }
        }else if (require_type==1){
            if (files!=null&&files.length>0){
                gridViewAdapter = new GridViewAdapter();
                gv_preview.setAdapter(gridViewAdapter);
                tv_no_data.setVisibility(View.GONE);
                gv_preview_add.setVisibility(View.GONE);
            }
            gv_preview_add.setVisibility(View.VISIBLE);
            tv_head_left.setText(getString(R.string.back));
            if (files_add!=null&&files_add.length>0){
                gridViewAdapterAdd = new GridViewAdapterAdd();
                gv_preview_add.setAdapter(gridViewAdapterAdd);
            }
        }
        gv_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: "+i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gridViewAdapter!=null){
            getFilePath();
            getPicFile();
            gridViewAdapter.notifyDataSetChanged();
        }
    }

    private void getScreenWidth() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density =  metrics.density;
        width = metrics.widthPixels;
//        imageWidth = (width-dp2px(PicPreViewActivity.this,5*2+2))/4;
        imageWidth = (metrics.widthPixels- TimonLibary.dp2px(this,2))/3;
    }

    private void getPicFile() {
//        File f= new File(Environment.getExternalStorageDirectory()+"/LEDILOVE/bit/");
        File f= new File(filepath);
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
        if (require_type==1){
            File file = new File(filepath_bit);
            if (!file.exists()){
                return;
            }
            files_add = file.listFiles();
        }
//        if (filePaths!=null){
//            filePaths.clear();
//        }
//        for (int i =0;i<files.length;i++){
//            filePaths.add(files[i].getAbsolutePath());
//        }
//        if (require_type==1){
//            File f_bit= new File(filepath_bit);
//            if (!f_bit.exists()) {//判断路径是否存在
//                return ;
//            }
//            files = f_bit.listFiles();
//            for (int j =0;j<files.length;j++){
//                filePaths.add(files[j].getAbsolutePath());
//            }
//        }
    }
    private void getFilePath(){
        filepath =Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/"+ StaticDatas.LEDHight+""+StaticDatas.LEDWidth+"/";
        filepath_bit =Environment.getExternalStorageDirectory()+"/LEDILOVE/bit/"+ StaticDatas.LEDHight+""+StaticDatas.LEDWidth+"/";
    }
    class GridViewAdapter extends BaseAdapter{

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
            viewHolder viewHolder;
            if (convertView!=null){
                view=convertView;
                viewHolder= (PicPreViewActivity.viewHolder) view.getTag();
            }else {
                view = View.inflate(PicPreViewActivity.this,R.layout.item_grildeview,null);
                viewHolder = new viewHolder();
                viewHolder.iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
                viewHolder.iv_grid_function = (ImageView) view.findViewById(R.id.iv_grid_function);
                view.setTag(viewHolder);
            }
            viewHolder.iv_pic.setMinimumWidth(imageWidth);
            if (StaticDatas.LEDHight==40){
                viewHolder.iv_pic.setMinimumHeight((int) (imageWidth*0.83));
            }else {
                viewHolder.iv_pic.setMinimumHeight(imageWidth);
            }
            viewHolder.iv_pic.setImageURI(Uri.fromFile(files[i]));
            viewHolder.iv_grid_function.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    files[i].delete();
                   files[i].delete();
                    refresh(0);
                }
            });
            viewHolder.iv_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setActivityResult(files[i].getAbsolutePath());
                }
            });
            return view;
        }
    }
    class GridViewAdapterAdd extends BaseAdapter{

        @Override
        public int getCount() {
            return files_add.length;
        }

        @Override
        public Object getItem(int i) {
            return files_add[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            View view;
            viewHolder viewHolder;
            if (convertView!=null){
                view=convertView;
                viewHolder= (PicPreViewActivity.viewHolder) view.getTag();
            }else {
                view = View.inflate(PicPreViewActivity.this,R.layout.item_grildeview_add,null);
                viewHolder = new viewHolder();
                viewHolder.iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
                viewHolder.iv_grid_function = (ImageView) view.findViewById(R.id.iv_grid_function);
                view.setTag(viewHolder);
            }
            viewHolder.iv_pic.setMinimumWidth(imageWidth);
            if (StaticDatas.LEDHight==40){
                viewHolder.iv_pic.setMinimumHeight((int) (imageWidth*0.83));
            }else {
                viewHolder.iv_pic.setMinimumHeight(imageWidth);
            }
            viewHolder.iv_pic.setImageURI(Uri.fromFile(files_add[i]));
            viewHolder.iv_grid_function.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    files[i].delete();
                   files_add[i].delete();
                    refresh(1);
                }
            });
            viewHolder.iv_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setActivityResult(files_add[i].getAbsolutePath());
                }
            });
            return view;
        }
    }

    private void refresh(int type) {
//        getFilePath();
        getPicFile();
        switch (type){
            case 0:
                gridViewAdapter.notifyDataSetChanged();
                break;
            case 1:
                gridViewAdapterAdd.notifyDataSetChanged();
                break;
        }

    }

    class viewHolder{
        com.daimajia.androidviewhover.BlurLayout blur_layout;
        ImageView iv_pic;
        ImageView iv_grid_function;

    }
    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
}
