package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import xc.LEDILove.Bean.TextColorSelctParams;
import xc.LEDILove.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ColorSelectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ColorSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorSelectFragment extends Fragment  {
    private String TAG = ColorSelectFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //背景颜色选择控件

    private RadioGroup rg_edit_dialog_font_color;
    private RadioGroup rg_edit_dialog_bg_color;
    //字体颜色选择控件

    private TextView tv_preview;
    private OnFragmentInteractionListener mListener;
    private TextColorSelctParams selctParams;
    private Context context;
    private ColorCallback colorCallback;
    private Button btn_auto_shift;

    private boolean isAutoColor = false;
    public ColorSelectFragment() {
        // Required empty public constructor
    }
    @SuppressLint("ValidFragment")
    public ColorSelectFragment(Context context) {
        // Required empty public constructor
        this.context = context;
    }
    public void setColorCallback(ColorCallback colorCallback){
        this.colorCallback = colorCallback;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ColorSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ColorSelectFragment newInstance(String param1, String param2) {
        ColorSelectFragment fragment = new ColorSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_color_select, null);
        initView(view);
        radioGroupEvent();
        return view;
    }
    private void radioGroupEvent() {
        rg_edit_dialog_bg_color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                translateBGColorById(i);
            }
        });
        rg_edit_dialog_font_color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                translateFontColorById(i);
            }
        });
    }
    private void translateFontColorById(int id) {
        switch (id){
            case R.id.gb_edit_dialog_font_black:
                if (0==selctParams.getColor_backdrop()){//判断用户点击的字体颜色是否与当前背景颜色相同
                    revocationFontSelected(selctParams.getColor_font());// 如果相同，需要撤销此次点击  即设置RadioGroup 为点击前的状态
                    return;
                }
                selctParams.setColor_font(0);
                break;
            case R.id.gb_edit_dialog_font_red:
                if (1==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(1);
                break;
            case R.id.gb_edit_dialog_font_yellow:
                if (2==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(2);
                break;
            case R.id.gb_edit_dialog_font_green:
                if (3==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(3);
                break;
            case R.id.gb_edit_dialog_font_cyan:
                if (4==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(4);
                break;
            case R.id.gb_edit_dialog_font_blue:
                if (5==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(5);
                break;
            case R.id.gb_edit_dialog_font_purple:
                if (6==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(6);
                break;
            case R.id.gb_edit_dialog_font_white:
                if (7==selctParams.getColor_backdrop()){
                    revocationFontSelected(selctParams.getColor_font());
                    return;
                }
                selctParams.setColor_font(7);
                break;
        }
        setText();
        colorCallback.OnColorSelected(selctParams);
    }

    private void revocationFontSelected(int i) {
        rg_edit_dialog_font_color.check(translateFontIdByColor(i));
    }
    private void revocationBGSelected(int i) {
        rg_edit_dialog_bg_color.check(translateBGIdByColor(i));
    }
    private int translateFontIdByColor(int i) {
        int id= R.id.gb_edit_dialog_font_black;
        switch (i){
            case 0:
                id = R.id.gb_edit_dialog_font_black;
                break;
            case 1:
                id = R.id.gb_edit_dialog_font_red;
                break;
            case 2:
                id = R.id.gb_edit_dialog_font_yellow;
                break;
            case 3:
                id = R.id.gb_edit_dialog_font_green;
                break;
            case 4:
                id = R.id.gb_edit_dialog_font_cyan;
                break;
            case 5:
                id = R.id.gb_edit_dialog_font_blue;
                break;
            case 6:
                id = R.id.gb_edit_dialog_font_purple;
                break;
            case 7:
                id = R.id.gb_edit_dialog_font_white;
                break;

        }
        return id;
    }
    private int translateBGIdByColor(int i) {
        int id = R.id.rb_edit_dialog_bg_black;
        switch (i){
            case 0:
                id = R.id.rb_edit_dialog_bg_black;
                break;
            case 1:
                id = R.id.rb_edit_dialog_bg_red;
                break;
            case 2:
                id = R.id.rb_edit_dialog_bg_yellow;
                break;
            case 3:
                id = R.id.rb_edit_dialog_bg_green;
                break;
            case 4:
                id = R.id.rb_edit_dialog_bg_cyan;
                break;
            case 5:
                id = R.id.rb_edit_dialog_bg_blue;
                break;
            case 6:
                id = R.id.rb_edit_dialog_bg_purple;
                break;
            case 7:
                id = R.id.rb_edit_dialog_bg_white;
                break;

        }
        return id;
    }
    private void translateBGColorById(int id) {
        switch (id){
            case R.id.rb_edit_dialog_bg_black:
                if (0==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(0);
                break;
            case R.id.rb_edit_dialog_bg_red:
                if (1==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(1);
                break;
            case R.id.rb_edit_dialog_bg_yellow:
                if (2==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(2);
                break;
            case R.id.rb_edit_dialog_bg_green:
                if (3==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(3);
                break;
            case R.id.rb_edit_dialog_bg_cyan:
                if (4==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(4);
                break;
            case R.id.rb_edit_dialog_bg_blue:
                if (5==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(5);
                break;
            case R.id.rb_edit_dialog_bg_purple:
                if (6==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(6);
                break;
            case R.id.rb_edit_dialog_bg_white:
                if (7==selctParams.getColor_font()){
                    revocationBGSelected(selctParams.getColor_backdrop());
                    return;
                }
                selctParams.setColor_backdrop(7);
                break;
        }
        setText();
        colorCallback.OnColorSelected(selctParams);
//        onColorChange();
    }
    private void initView(View view) {
        selctParams = new TextColorSelctParams();
        selctParams.setColor_backdrop(0);
        selctParams.setColor_font(1);
        rg_edit_dialog_font_color = (RadioGroup) view.findViewById(R.id.rg_edit_dialog_font_color);
        rg_edit_dialog_bg_color = (RadioGroup) view.findViewById(R.id.rg_edit_dialog_bg_color);
        tv_preview = (TextView) view.findViewById(R.id.tv_preview);
        tv_preview.setBackgroundColor(parseColor(selctParams.getColor_backdrop()));
        tv_preview.setTextColor(parseColor(selctParams.getColor_font()));
        btn_auto_shift = (Button) view.findViewById(R.id.btn_auto_shift);
        setAutoColorShift(1);//初始化
        btn_auto_shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAutoColor){
                    setAutoColorShift(1);
                    isAutoColor=false;
                }else {
                    setAutoColorShift(0);
                    isAutoColor=true;
                }
                colorCallback.OnColorModelChange(isAutoColor);
            }
        });
        rg_edit_dialog_font_color.check(R.id.gb_edit_dialog_font_red);
        rg_edit_dialog_bg_color.check(R.id.rb_edit_dialog_bg_black);
    }
    private void setAutoColorShift(int choose) {
        btn_auto_shift.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
        String str=getResources().getString(R.string.shift_auto);
        SpannableStringBuilder style=new SpannableStringBuilder(str);
        int index=str.indexOf("/");
        switch (choose){
            case 0://单行模式 “/” 前面字符显示绿色
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_color_yellow)),0,index, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                break;
            case 1://双行模式 “/” 后面字符显示绿色
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_color_yellow)),index+1,str.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                break;
        }
        btn_auto_shift.setText(style);
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
////            case R.id.btn_choose:
////                break;
//            //背景颜色
//            case R.id.iv_backdrop_black:
//                if (0==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(0);
//                break;
//            case R.id.iv_backdrop_red:
//                if (1==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(1);
//                break;
//            case R.id.iv_backdrop_yellow:
//                if (2==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(2);
//                break;
//            case R.id.iv_backdrop_green:
//                if (3==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(3);
//                break;
//            case R.id.iv_backdrop_cyan:
//                if (4==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(4);
//                break;
//            case R.id.iv_backdrop_blove:
//                if (5==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(5);
//                break;
//            case R.id.iv_backdrop_purple:
//                if (6==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(6);
//                break;
//            case R.id.iv_backdrop_white:
//                if (7==selctParams.getColor_font()){
//                    return;
//                }
//                selctParams.setColor_backdrop(7);
//                break;
//            //字体颜色
//            case R.id.iv_font_black:
//                if (0==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(0);
//                break;
//            case R.id.iv_font_red:
//                if (1==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(1);
//                break;
//            case R.id.iv_font_yellow:
//                if (2==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(2);
//                break;
//            case R.id.iv_font_green:
//                if (3==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(3);
//                break;
//            case R.id.iv_font_cyan:
//                if (4==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(4);
//                break;
//            case R.id.iv_font_blove:
//                if (5==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(5);
//                break;
//            case R.id.iv_font_purple:
//                if (6==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(6);
//                break;
//            case R.id.iv_font_white:
//                if (7==selctParams.getColor_backdrop()){
//                    return;
//                }
//                selctParams.setColor_font(7);
//                break;
//        }
////        Log.i("click",view.getId()+"");
//        setText();
//        colorCallback.OnColorSelected(selctParams);
//    }
    private void setText() {
        //设置预览颜色
        tv_preview.setBackgroundColor(parseColor(selctParams.getColor_backdrop()));
        tv_preview.setTextColor(parseColor(selctParams.getColor_font()));

    }

    private int parseColor(int position) {
        int color = -1;
        switch (position){
            case 0:
                color =  context.getResources().getColor(R.color.black);
                break;
            case 1:
                color = context.getResources().getColor(R.color.red);
                break;
            case 2:
                color = context.getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = context.getResources().getColor(R.color.green);
                break;
            case 4:
                color = context.getResources().getColor(R.color.cyan);
                break;
            case 5:
                color = context.getResources().getColor(R.color.blove);
                break;
            case 6:
                color = context.getResources().getColor(R.color.purple);
                break;
            case 7:
                color = context.getResources().getColor(R.color.white);
                break;
        }
        return color;
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public interface ColorCallback{
        void OnColorSelected(TextColorSelctParams selctParams);
        void OnColorModelChange(boolean isAuto);
    }
}
