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

import xc.LEDILove.R;

public class PicPreViewActivity extends BaseActivity {
    private final String TAG = PicPreViewActivity.class.getSimpleName();
    private GridView gv_preview;
    private File[] files;
    private int width;
    private int imageWidth;
    private GridViewAdapter gridViewAdapter;
    private LinearLayout ll_back;
    private TextView tv_head_left;
    private TextView scancount_txt;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setActivityResult("");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scrollToFinishActivity();//左滑退出activity
    }
    private void setActivityResult(String msg) {
        Intent intent = new Intent();
        intent.putExtra("file",msg);
        setResult(1001,intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_pre_view);
        gv_preview =(GridView) findViewById(R.id.gv_preview);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_head_left = (TextView)findViewById(R.id.tv_head_left);
        tv_head_left.setText(getString(R.string.palte_simplename));
        scancount_txt = (TextView)findViewById(R.id.scancount_txt);
        scancount_txt.setText(getString(R.string.PhotoGallery));
        getScreenWidth();
        getPicFile();
        if (files!=null&&files.length>0){
             gridViewAdapter = new GridViewAdapter();
            gv_preview.setAdapter(gridViewAdapter);
        }
        gv_preview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "onItemClick: "+i);
            }
        });
    }

    private void getScreenWidth() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
       float density =  metrics.density;
       width = metrics.widthPixels;
        imageWidth = (width-dp2px(PicPreViewActivity.this,5*2+2))/2;
    }

    private void getPicFile() {
        File f= new File(Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/");
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
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
                viewHolder.blur_layout = (BlurLayout) view.findViewById(R.id.blur_layout);
                view.setTag(viewHolder);
            }
            viewHolder.iv_pic.setMinimumWidth(imageWidth);
            viewHolder.iv_pic.setMinimumHeight(imageWidth);
            viewHolder.iv_pic.setImageURI(Uri.fromFile(files[i]));
            View hover = LayoutInflater.from(PicPreViewActivity.this).inflate(R.layout.hover_sample, null);
            hover.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YoYo.with(Techniques.Tada)
                            .duration(450)
                            .playOn(v);
                    setActivityResult(files[i].getAbsolutePath());
                }
            });
            hover.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YoYo.with(Techniques.Swing)
                            .duration(450)
                            .playOn(v);
                }
            });
            hover.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YoYo.with(Techniques.Swing)
                            .duration(450)
                            .playOn(v);
                    files[i].delete();

                    refresh();
                }
            });
            viewHolder.blur_layout.setHoverView(hover);
            viewHolder.blur_layout.setBlurDuration(550);
            viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.edit, Techniques.FlipInX, 450, 0);
            viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.share, Techniques.FlipInX, 450, 250);
            viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.delete, Techniques.FlipInX, 450, 500);

            viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.edit, Techniques.FlipOutX, 450, 500);
            viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.share, Techniques.FlipOutX, 450, 250);
            viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.delete, Techniques.FlipOutX, 450, 0);

            viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.description, Techniques.FadeInUp);
            viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.description, Techniques.FadeOutDown);
            return view;
        }
    }

    private void refresh() {
        getPicFile();
        gridViewAdapter.notifyDataSetChanged();
    }

    class viewHolder{
        com.daimajia.androidviewhover.BlurLayout blur_layout;
        ImageView iv_pic;
    }
    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
}
