package xc.LEDILove.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidviewhover.BlurLayout;

import java.io.File;

import xc.LEDILove.R;
import xc.LEDILove.activity.PicPreViewActivity;
import xc.LEDILove.bluetooth.StaticDatas;

public class GalleryGrideViewAdapter extends BaseAdapter{
    private final String TAG  = GalleryGrideViewAdapter.class.getSimpleName();
    private Context context;
    private File[] files;
    private  int imageWidth;
    private GalleryGrideViewItemClickCallback galleryGrideViewItemClickCallback;


    public  GalleryGrideViewAdapter(Context context, File[] files){
        this.context = context;
        this.files = files;
        getScreenWidth();
    }
    public void setGalleryGrideViewItemClickCallback(GalleryGrideViewItemClickCallback galleryGrideViewItemClickCallback){
        this.galleryGrideViewItemClickCallback = galleryGrideViewItemClickCallback;
    }
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
    public void refresh(File[] files){
        this.files = files;
        notifyDataSetChanged();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View view;
        viewHolder viewHolder;
        if (convertView!=null){
            view=convertView;
            viewHolder= (GalleryGrideViewAdapter.viewHolder) view.getTag();
        }else {
            view = View.inflate(context, R.layout.item_grildeview_gallery,null);
            viewHolder = new viewHolder();
            viewHolder.iv_pic = (ImageView) view.findViewById(R.id.iv_pic);
            viewHolder.iv_grid_edit = (ImageView) view.findViewById(R.id.iv_grid_edit);
            viewHolder.iv_grid_del = (ImageView) view.findViewById(R.id.iv_grid_del);
//            viewHolder.blur_layout = (BlurLayout) view.findViewById(R.id.blur_layout);
            view.setTag(viewHolder);
        }
        viewHolder.iv_pic.setMinimumWidth(imageWidth);
        if (StaticDatas.LEDWidth==32){
            viewHolder.iv_pic.setMinimumHeight(imageWidth);//3232

        }else if (StaticDatas.LEDWidth==48){
            viewHolder.iv_pic.setMinimumHeight((int) (imageWidth*0.83));//4048
        }else {
            viewHolder.iv_pic.setMinimumHeight(imageWidth);//默认正方形
        }
        viewHolder.iv_pic.setImageURI(Uri.fromFile(files[position]));
        Log.e(TAG, "getView:width>>>"+viewHolder.iv_pic.getWidth());
        Log.e(TAG, "getView:height>>>"+viewHolder.iv_pic.getHeight());
        viewHolder.iv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryGrideViewItemClickCallback.onItemSelected(position);
            }
        });
        viewHolder.iv_grid_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryGrideViewItemClickCallback.onEditSelected(position);
            }
        });
        viewHolder.iv_grid_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryGrideViewItemClickCallback.onDeleteSelected(position);
            }
        });

//        viewHolder.iv_pic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("adapter", "onClick: >>>"+position );
//                galleryGrideViewItemClickCallback.onItemSelected(position);
//            }
//        });
//        View hover = LayoutInflater.from(context).inflate(R.layout.hover_sample_custom, null);
//        hover.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                YoYo.with(Techniques.Tada)
//                        .duration(450)
//                        .playOn(v);
//                galleryGrideViewItemClickCallback.onEditSelected(position);
////                setActivityResult(files[position].getAbsolutePath());
//            }
//        });
//        hover.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                YoYo.with(Techniques.Swing)
//                        .duration(450)
//                        .playOn(v);
//                galleryGrideViewItemClickCallback.onSendSelected(position);
//            }
//        });
//        hover.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                YoYo.with(Techniques.Swing)
//                        .duration(450)
//                        .playOn(v);
//                galleryGrideViewItemClickCallback.onDeleteSelected(position);
////                files[position].delete();
//            }
//        });
//        viewHolder.blur_layout.setHoverView(hover);
//        viewHolder.blur_layout.setBlurDuration(550);
//        viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.edit, Techniques.FlipInX, 450, 0);
//        viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.share, Techniques.FlipInX, 450, 250);
//        viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.delete, Techniques.FlipInX, 450, 500);
//
//        viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.edit, Techniques.FlipOutX, 450, 500);
//        viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.share, Techniques.FlipOutX, 450, 250);
//        viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.delete, Techniques.FlipOutX, 450, 0);

//            viewHolder.blur_layout.addChildAppearAnimator(hover, R.id.description, Techniques.FadeInUp);
//            viewHolder.blur_layout.addChildDisappearAnimator(hover, R.id.description, Techniques.FadeOutDown);
        return view;
    }
    private void getScreenWidth() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density =  metrics.density;
        imageWidth = (metrics.widthPixels-dp2px(context,5*2+2))/4;
    }
    /**
     * dp转换成px
     */
    private int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
    class viewHolder{
        com.daimajia.androidviewhover.BlurLayout blur_layout;
        ImageView iv_pic;
        ImageView iv_grid_edit;
        ImageView iv_grid_del;
    }
    public interface GalleryGrideViewItemClickCallback{
        void onItemSelected(int position);
        void onEditSelected(int position);
        void onDeleteSelected(int position);
    }
}
