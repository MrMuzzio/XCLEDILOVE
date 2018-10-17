package xc.LEDILove.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import xc.LEDILove.R;

public class MsgNumberDialog extends Dialog {
    private Context context;
    private RadioGroup rg_msg_number_selected;
    private MsgSelectedListener msgSelectedListener;
    public MsgNumberDialog(@NonNull Context context,MsgSelectedListener msgSelectedListener) {
        super(context);
        this.msgSelectedListener =msgSelectedListener;
        this.context = context;
    }
    public MsgNumberDialog(Context context){
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_msg_number, null);
        setContentView(view);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
        rg_msg_number_selected = (RadioGroup) view.findViewById(R.id.rg_msg_number_selected);
        rg_msg_number_selected.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                int select = -1;
                switch (id){
                    case R.id.rb_msg_number_null:
                        select = 0;
                        break;
                    case R.id.rb_msg_number_1:
                        select = 1;
                        break;
                    case R.id.rb_msg_number_2:
                        select = 2;
                        break;
                    case R.id.rb_msg_number_3:
                        select = 3;
                        break;
                    case R.id.rb_msg_number_4:
                        select = 4;
                        break;
                    case R.id.rb_msg_number_5:
                        select = 5;
                        break;
                    case R.id.rb_msg_number_6:
                        select = 6;
                        break;
                    case R.id.rb_msg_number_7:
                        select = 7;
                        break;
                    case R.id.rb_msg_number_8:
                        select = 8;
                        break;
                }
                msgSelectedListener.onSelected(select);
            }
        });
    }
    public interface MsgSelectedListener{
        void onSelected(int number);
    }
}
