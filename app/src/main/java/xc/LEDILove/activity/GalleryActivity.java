package xc.LEDILove.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xc.LEDILove.R;
import xc.LEDILove.fragment.GalleryFragment;

public class GalleryActivity extends AppCompatActivity {
    private GalleryFragment galleryFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        galleryFragment = new GalleryFragment(0,this);
        addFragment();
    }

    private void addFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction =  fragmentManager.beginTransaction();
        transaction.add(R.id.fl_gallery,galleryFragment);
        transaction.commit();
    }
}
