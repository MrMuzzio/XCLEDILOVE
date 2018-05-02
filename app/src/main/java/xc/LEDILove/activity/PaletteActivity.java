package xc.LEDILove.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xc.LEDILove.R;

public class PaletteActivity extends BaseActivity {
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scrollToFinishActivity();//左滑退出activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palettle);
    }
}
