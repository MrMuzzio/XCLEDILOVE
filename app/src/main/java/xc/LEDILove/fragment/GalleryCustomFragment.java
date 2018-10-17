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
import xc.LEDILove.bluetooth.StaticDatas;

public class GalleryCustomFragment extends Fragment {
    private Context context;
    private GridView gv_gallery_custom;
    private File[] files;
    private GalleryGrideViewAdapter galleryGrideViewAdapter;
    private String filepath;
    private CustomFragmentStatusCallback customFragmentStatusCallback;

    /**
     *
     */
    public void resetResources() {
        getFilePath();
        getPicFile();
        if (galleryGrideViewAdapter!=null){
            galleryGrideViewAdapter.refresh(files);
        }
    }
    private void getFilePath(){
        filepath =Environment.getExternalStorageDirectory()+"/LEDILOVE/pic/"+ StaticDatas.LEDHight+""+StaticDatas.LEDWidth+"/";
    }
    public interface CustomFragmentStatusCallback{
        void onEditRequire (String path);
        void onSendRequire(String path);
    }
    public void setCustomFragmentStatusCallback (CustomFragmentStatusCallback customFragmentStatusCallback){
        this.customFragmentStatusCallback = customFragmentStatusCallback;
    }
    public GalleryCustomFragment(){

    }
    @SuppressLint("ValidFragment")
    public  GalleryCustomFragment(Context context){
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
        View view  = inflater.inflate(R.layout.fragment_gallery_custom,null);
        findView(view);
        return view;
    }

    private void findView(View view) {
        gv_gallery_custom= (GridView)view.findViewById(R.id.gv_gallery_custom);
        galleryGrideViewAdapter= new GalleryGrideViewAdapter(context,files);
        galleryGrideViewAdapter.setGalleryGrideViewItemClickCallback(new GalleryGrideViewAdapter.GalleryGrideViewItemClickCallback() {
            @Override
            public void onItemSelected(int position) {
                customFragmentStatusCallback.onSendRequire(files[position].getPath());
            }

            @Override
            public void onEditSelected(int position) {
                customFragmentStatusCallback.onEditRequire(files[position].getPath());
            }

            @Override
            public void onDeleteSelected(int position) {
                files[position].delete();
                getPicFile();
                galleryGrideViewAdapter.refresh(files);
            }
        });
        gv_gallery_custom.setAdapter(galleryGrideViewAdapter);
    }

    private void getPicFile() {
        File f= new File(filepath);
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
    }
}
