package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.adapter.SelectorViewPagerAdapter;

public class GalleryFragment extends Fragment {
    private Context context;
    private int type = 0;
    private RadioGroup rg_gallery;
    private RadioButton rb_gallery_inlay;
    private RadioButton rb_gallery_custom;
    private RadioButton rb_gallery_gif;
    private GalleryCustomFragment galleryCustomFragment;
    private GalleryInlayFragment galleryInlayFragment;
    private GifFragment gifFragment;
    private GalleryTextViewListener galleryTextViewListener;
    private ViewPager vp_gallery_frame;
    private SelectorViewPagerAdapter selectorViewPagerAdapter;
    private List<Fragment> fragments;
    private GalleryFragmentStatusCallback galleryFragmentStatusCallback;
    public GalleryFragment(){

    }

    /**
     * 当LED参数发生变化 需要根据LED尺寸重新设置显示图片的资源文件，包含gif
     */
    public void resetLayout() {
        if (galleryCustomFragment!=null){
            galleryCustomFragment.resetResources();
        }
        if (galleryInlayFragment!=null){
            galleryInlayFragment.resetResources();
        }
        if (gifFragment!=null){
            gifFragment.resetResources();
        }
    }

    public interface GalleryFragmentStatusCallback{
        void onInlayEdit(String path);
        void onSendRequire(String path,int type);
    }
    public void setGalleryFragmentStatusCallback(GalleryFragmentStatusCallback galleryFragmentStatusCallback){
        this.galleryFragmentStatusCallback = galleryFragmentStatusCallback;
    }
    @SuppressLint("ValidFragment")
    public  GalleryFragment (int type, Context context){
        this.context = context;
        this.type = type;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        galleryTextViewListener = new GalleryTextViewListener();
        fragments = new ArrayList<>();

        File file_3232 = new File(Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/3232/");
        if (!file_3232.exists()){
            file_3232.mkdirs();
        }
        File file_4048 = new File(Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/4048/");
        if (!file_4048.exists()){
            file_4048.mkdirs();
        }
        galleryCustomFragment = new GalleryCustomFragment(context);
        galleryInlayFragment = new GalleryInlayFragment(context);
        if (type==1){
            gifFragment = new GifFragment(context);
        }
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_gallery,null);
        findViewAndSetListener(view);
        initView();
        ViewEventListener();
        return view;
    }

    private void ViewEventListener() {
        vp_gallery_frame.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                
            }

            @Override
            public void onPageSelected(int position) {
                selectRadioGroup(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void selectRadioGroup(int position) {
        switch (position){
            case 0:
                rg_gallery.check(R.id.rb_gallery_inlay);
                rb_gallery_inlay.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
                rb_gallery_custom.setTextColor(context.getResources().getColor(R.color.white));
                rb_gallery_gif.setTextColor(context.getResources().getColor(R.color.white));
                break;
            case 1:
                rg_gallery.check(R.id.rb_gallery_custom);
                rb_gallery_inlay.setTextColor(context.getResources().getColor(R.color.white));
                rb_gallery_custom.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
                rb_gallery_gif.setTextColor(context.getResources().getColor(R.color.white));
                break;
            case 2:
                rg_gallery.check(R.id.rb_gallery_gif);
                rb_gallery_inlay.setTextColor(context.getResources().getColor(R.color.white));
                rb_gallery_custom.setTextColor(context.getResources().getColor(R.color.white));
                rb_gallery_gif.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
                break;
        }
    }

    private void initView() {
        fragments.add(galleryInlayFragment);
        fragments.add(galleryCustomFragment);
        if (type==1&&gifFragment!=null){
            fragments.add(gifFragment);
        }else {
            rb_gallery_gif.setVisibility(View.GONE);
        }
        selectorViewPagerAdapter = new SelectorViewPagerAdapter(getFragmentManager(),fragments);
        vp_gallery_frame.setAdapter(selectorViewPagerAdapter);
        selectRadioGroup(0);
        galleryInlayFragment.setInlayFragmentStatusCallback(new GalleryInlayFragment.InlayFragmentStatusCallback() {
            @Override
            public void onEditRequire(String path) {
                galleryFragmentStatusCallback.onInlayEdit(path);
            }

            @Override
            public void onSendRequire(String path) {
                galleryFragmentStatusCallback.onSendRequire(path,0);
            }
        });
        galleryCustomFragment.setCustomFragmentStatusCallback(new GalleryCustomFragment.CustomFragmentStatusCallback() {
            @Override
            public void onEditRequire(String path) {
                galleryFragmentStatusCallback.onInlayEdit(path);
            }

            @Override
            public void onSendRequire(String path) {
                galleryFragmentStatusCallback.onSendRequire(path,1);
            }
        });
    }

    private void findViewAndSetListener(View view) {
        rg_gallery = (RadioGroup)view.findViewById(R.id.rg_gallery);
        rb_gallery_inlay = (RadioButton)view.findViewById(R.id.rb_gallery_inlay);
        rb_gallery_custom = (RadioButton)view.findViewById(R.id.rb_gallery_custom);
        rb_gallery_gif = (RadioButton)view.findViewById(R.id.rb_gallery_gif);
        rb_gallery_inlay.setOnClickListener(galleryTextViewListener);
        rb_gallery_custom.setOnClickListener(galleryTextViewListener);
        rb_gallery_gif.setOnClickListener(galleryTextViewListener);
        vp_gallery_frame = (ViewPager)view.findViewById(R.id.vp_gallery_frame);
    }

    class GalleryTextViewListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            int index = 0;
            switch (view.getId()){
                case R.id.rb_gallery_inlay:
                    index = 0;
                    break;
                case R.id.rb_gallery_custom:
                    index = 1;
                    break;
                case R.id.rb_gallery_gif:
                    index = 2;
                    break;
            }
            selectedViewPager(index);
        }
    }
    private void selectedViewPager(int index) {
        vp_gallery_frame.setCurrentItem(index);
    }
}
