package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.File;

import xc.LEDILove.R;
import xc.LEDILove.adapter.GalleryGrideViewAdapter;
import xc.LEDILove.adapter.GalleryinlayGrideViewAdapter;
import xc.LEDILove.bluetooth.StaticDatas;

public class GalleryInlayFragment extends Fragment {
    private Context context;
    private GridView gv_gallery_inlay;
    private File[] files;
    private GalleryinlayGrideViewAdapter galleryinlayGrideViewAdapter;
    private String filepath;
    private InlayFragmentStatusCallback inlayFragmentStatusCallback;

    public void resetResources() {
        getFilePath();
        getPicFile();
        if (galleryinlayGrideViewAdapter!=null){
            galleryinlayGrideViewAdapter.refresh(files);
        }
    }
    private void getFilePath(){
        filepath =Environment.getExternalStorageDirectory()+"/LEDILOVE/bit/"+ StaticDatas.LEDHight+""+StaticDatas.LEDWidth+"/";
    }
    interface InlayFragmentStatusCallback {
        void onEditRequire (String path);
        void onSendRequire(String path);
    }
    public GalleryInlayFragment(){

    }
    public void setInlayFragmentStatusCallback(InlayFragmentStatusCallback inlayFragmentStatusCallback){
        this.inlayFragmentStatusCallback = inlayFragmentStatusCallback;
    }
    @SuppressLint("ValidFragment")
    public  GalleryInlayFragment(Context context){
        this.context = context;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getFilePath();
        getPicFile();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_gallery_inlay,null);
        findView(view);
        return view;
    }

    private void findView(View view) {
        gv_gallery_inlay= (GridView)view.findViewById(R.id.gv_gallery_inlay);
        if (files==null){
            return;
        }
        galleryinlayGrideViewAdapter= new GalleryinlayGrideViewAdapter(context,files);
        galleryinlayGrideViewAdapter.setGalleryGrideViewItemClickCallback(new GalleryinlayGrideViewAdapter.GalleryinlayGrideViewItemClickCallback() {
            @Override
            public void onItemSelected(int position) {//多选

            }

            @Override
            public void onEditSelected(int position) {//编辑
                inlayFragmentStatusCallback.onEditRequire(files[position].getPath());
            }

            @Override
            public void onSendSelected(int position) {//发送
                inlayFragmentStatusCallback.onSendRequire(files[position].getPath());
            }

            @Override
            public void onDeleteSelected(int position) {//删除
                files[position].delete();
                getPicFile();
                galleryinlayGrideViewAdapter.refresh(files);
            }
        });
        gv_gallery_inlay.setAdapter(galleryinlayGrideViewAdapter);
    }
    private void getPicFile() {
        File f= new File(filepath);
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
    }
}
