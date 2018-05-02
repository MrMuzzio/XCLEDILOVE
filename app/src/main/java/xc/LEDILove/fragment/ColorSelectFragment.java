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
public class ColorSelectFragment extends Fragment implements View.OnClickListener {
    private String TAG = ColorSelectFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //背景颜色选择控件
    private ImageView iv_backdrop_black;
    private ImageView iv_backdrop_red;
    private ImageView iv_backdrop_yellow;
    private ImageView iv_backdrop_green;
    private ImageView iv_backdrop_cyan;
    private ImageView iv_backdrop_blove;
    private ImageView iv_backdrop_purple;
    private ImageView iv_backdrop_white;
    //字体颜色选择控件
    private ImageView iv_font_black;
    private ImageView iv_font_red;
    private ImageView iv_font_yellow;
    private ImageView iv_font_green;
    private ImageView iv_font_cyan;
    private ImageView iv_font_blove;
    private ImageView iv_font_purple;
    private ImageView iv_font_white;

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
        View view = inflater.inflate(R.layout.fragment_color_select, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        selctParams = new TextColorSelctParams();
        selctParams.setColor_backdrop(0);
        selctParams.setColor_font(1);
        tv_preview = (TextView) view.findViewById(R.id.tv_preview);
        iv_backdrop_black = (ImageView) view.findViewById(R.id.iv_backdrop_black);
        iv_backdrop_red = (ImageView) view.findViewById(R.id.iv_backdrop_red);
        iv_backdrop_yellow = (ImageView) view.findViewById(R.id.iv_backdrop_yellow);
        iv_backdrop_green = (ImageView) view.findViewById(R.id.iv_backdrop_green);
        iv_backdrop_cyan = (ImageView) view.findViewById(R.id.iv_backdrop_cyan);
        iv_backdrop_blove = (ImageView) view.findViewById(R.id.iv_backdrop_blove);
        iv_backdrop_purple = (ImageView) view.findViewById(R.id.iv_backdrop_purple);
        iv_backdrop_white = (ImageView) view.findViewById(R.id.iv_backdrop_white);
        iv_backdrop_black.setOnClickListener(this);
        iv_backdrop_red.setOnClickListener(this);
        iv_backdrop_yellow.setOnClickListener(this);
        iv_backdrop_green.setOnClickListener(this);
        iv_backdrop_cyan.setOnClickListener(this);
        iv_backdrop_blove.setOnClickListener(this);
        iv_backdrop_purple.setOnClickListener(this);
        iv_backdrop_white.setOnClickListener(this);

        iv_font_black = (ImageView) view.findViewById(R.id.iv_font_black);
        iv_font_red = (ImageView) view.findViewById(R.id.iv_font_red);
        iv_font_yellow = (ImageView) view.findViewById(R.id.iv_font_yellow);
        iv_font_green = (ImageView) view.findViewById(R.id.iv_font_green);
        iv_font_cyan = (ImageView) view.findViewById(R.id.iv_font_cyan);
        iv_font_blove = (ImageView) view.findViewById(R.id.iv_font_blove);
        iv_font_purple = (ImageView) view.findViewById(R.id.iv_font_purple);
        iv_font_white = (ImageView) view.findViewById(R.id.iv_font_white);

        iv_font_black.setOnClickListener(this);
        iv_font_red.setOnClickListener(this);
        iv_font_yellow.setOnClickListener(this);
        iv_font_green.setOnClickListener(this);
        iv_font_cyan.setOnClickListener(this);
        iv_font_blove.setOnClickListener(this);
        iv_font_purple.setOnClickListener(this);
        iv_font_white.setOnClickListener(this);
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
//                selectedParams.color = getResources().getColor(R.color.white);
//                ledView.setTextColorChange( selectedParams.color);
//                clearEditText.setTextColor(getResources().getColor(R.color.white));
    }
    private void setAutoColorShift(int choose) {
        btn_auto_shift.setTextColor(getResources().getColor(R.color.loading_bar_text_color));
        String str=getResources().getString(R.string.shift_auto);
        SpannableStringBuilder style=new SpannableStringBuilder(str);
        int index=str.indexOf("/");
        switch (choose){
            case 0://单行模式 “/” 前面字符显示绿色
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)),0,index, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                break;
            case 1://双行模式 “/” 后面字符显示绿色
                style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)),index+1,str.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
//            case R.id.btn_choose:
//                break;
            //背景颜色
            case R.id.iv_backdrop_black:
                if (0==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(0);
                break;
            case R.id.iv_backdrop_red:
                if (1==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(1);
                break;
            case R.id.iv_backdrop_yellow:
                if (2==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(2);
                break;
            case R.id.iv_backdrop_green:
                if (3==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(3);
                break;
            case R.id.iv_backdrop_cyan:
                if (4==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(4);
                break;
            case R.id.iv_backdrop_blove:
                if (5==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(5);
                break;
            case R.id.iv_backdrop_purple:
                if (6==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(6);
                break;
            case R.id.iv_backdrop_white:
                if (7==selctParams.getColor_font()){
                    return;
                }
                selctParams.setColor_backdrop(7);
                break;
            //字体颜色
            case R.id.iv_font_black:
                if (0==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(0);
                break;
            case R.id.iv_font_red:
                if (1==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(1);
                break;
            case R.id.iv_font_yellow:
                if (2==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(2);
                break;
            case R.id.iv_font_green:
                if (3==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(3);
                break;
            case R.id.iv_font_cyan:
                if (4==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(4);
                break;
            case R.id.iv_font_blove:
                if (5==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(5);
                break;
            case R.id.iv_font_purple:
                if (6==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(6);
                break;
            case R.id.iv_font_white:
                if (7==selctParams.getColor_backdrop()){
                    return;
                }
                selctParams.setColor_font(7);
                break;
        }
//        Log.i("click",view.getId()+"");
        setText();
        colorCallback.OnColorSelected(selctParams);
//        if (textSelected()){
//            int start = et_display.getSelectionStart();
//            int end = et_display.getSelectionEnd();
//            for (int k=0;k<end-start;k++){
//                textBeanList.get(start+k).setBackdrop((selctParams.getColor_backdrop()));
//                textBeanList.get(start+k).setFont((selctParams.getColor_font()));
//            }
//            setTextSpan(et_display.getText().toString());
////            et_display.setSelection(start,end);
//        }
    }
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
                color = context.getResources().getColor(R.color.dark_green);
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
