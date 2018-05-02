package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import xc.LEDILove.R;
import xc.LEDILove.activity.MEditActivity;
import xc.LEDILove.view.HorizontalListView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FontSelectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FontSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FontSelectFragment extends Fragment implements View.OnClickListener {
    private String TAG = FontSelectFragment.class.getSimpleName();
    //字体大小
    private Spinner spinnerWordSize;
    //正 斜 粗
    private Spinner spinnerWordType;
    //字符计数
    private TextView tvTxtNumber;
    private HorizontalListView hl_unicode_image;
    private String[] unicode_strs;
    private TextView tv_unicode_1;
    private TextView tv_unicode_2;
    private TextView tv_unicode_3;
    private TextView tv_unicode_4;
    private TextView tv_unicode_5;
    private TextView tv_unicode_6;
    private TextView tv_unicode_7;
    private TextView tv_unicode_8;
    private TextView tv_unicode_9;
    private TextView tv_unicode_10;
    private TextView tv_unicode_11;
    private TextView tv_unicode_12;
    private TextView tv_unicode_13;
    private TextView tv_unicode_14;
    private TextView tv_unicode_15;
    private TextView tv_unicode_16;
    private TextView tv_unicode_17;
    private TextView tv_unicode_18;
    private TextView tv_unicode_19;
    private TextView tv_unicode_20;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FontCallback fontCallback;
    private OnFragmentInteractionListener mListener;
    private String charlegth = "";



    public interface FontCallback{
        void onWordSizeChange(String size);
        void onWordTypeChange(String type);
        void onViewCreate();
        void onImageSelected(String str);
    }
    public FontSelectFragment() {
        // Required empty public constructor
    }
    @SuppressLint("ValidFragment")
    public FontSelectFragment(Context context){
        this.context = context;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FontSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FontSelectFragment newInstance(String param1, String param2) {
        FontSelectFragment fragment = new FontSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public void setFontCallback(FontCallback fontCallback){
        this.fontCallback = fontCallback;
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
    private  View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_font_select, container, false);
        initView(view);
        fontCallback.onViewCreate();
        return view;
    }
    private String[] wordSize;
    private String[] wordType;
    private Context context;
    private void initView(View view) {
        spinnerWordSize = (Spinner) view.findViewById(R.id.spinnerWordSize);
        spinnerWordType = (Spinner) view.findViewById(R.id.spinnerWordType);
        tvTxtNumber = (TextView) view.findViewById(R.id.tvTxtNumber);
        wordSize = getResources().getStringArray(R.array.wordSize);
        ArrayAdapter<String> wordSizeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordSize);
        spinnerWordSize.setAdapter(wordSizeAdapter);

        wordType = getResources().getStringArray(R.array.wordType);
        ArrayAdapter<String> wordTypeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wordType);
        spinnerWordType.setAdapter(wordTypeAdapter);

        //正斜粗
//        selectedParams.wordType = wordType[0];
        spinnerWordType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fontCallback.onWordTypeChange(wordType[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        selectedParams.wordSize = 12;
        //字体大小
        spinnerWordSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fontCallback.onWordSizeChange(wordSize[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        unicode_strs = context.getResources().getStringArray(R.array.unicode_images);
//        hl_unicode_image =(HorizontalListView) view.findViewById(R.id.hl_unicode_image);
//        hlAdapter = new HLAdapter();
//        hl_unicode_image.setAdapter(hlAdapter);
        tv_unicode_1 = (TextView) view.findViewById(R.id.tv_unicode_1);
        tv_unicode_1.setText(unicode_strs[0]);
        tv_unicode_1.setOnClickListener(this);
        tv_unicode_2 = (TextView) view.findViewById(R.id.tv_unicode_2);
        tv_unicode_2.setText(unicode_strs[1]);
        tv_unicode_2.setOnClickListener(this);
        tv_unicode_3 = (TextView) view.findViewById(R.id.tv_unicode_3);
        tv_unicode_3.setText(unicode_strs[2]);
        tv_unicode_3.setOnClickListener(this);
        tv_unicode_4 = (TextView) view.findViewById(R.id.tv_unicode_4);
        tv_unicode_4.setText(unicode_strs[3]);
        tv_unicode_4.setOnClickListener(this);
        tv_unicode_5 = (TextView) view.findViewById(R.id.tv_unicode_5);
        tv_unicode_5.setText(unicode_strs[4]);
        tv_unicode_5.setOnClickListener(this);
        tv_unicode_6 = (TextView) view.findViewById(R.id.tv_unicode_6);
        tv_unicode_6.setText(unicode_strs[5]);
        tv_unicode_6.setOnClickListener(this);
        tv_unicode_7 = (TextView) view.findViewById(R.id.tv_unicode_7);
        tv_unicode_7.setText(unicode_strs[6]);
        tv_unicode_7.setOnClickListener(this);
        tv_unicode_8 = (TextView) view.findViewById(R.id.tv_unicode_8);
        tv_unicode_8.setText(unicode_strs[7]);
        tv_unicode_8.setOnClickListener(this);
        tv_unicode_9 = (TextView) view.findViewById(R.id.tv_unicode_9);
        tv_unicode_9.setText(unicode_strs[8]);
        tv_unicode_9.setOnClickListener(this);
        tv_unicode_10 = (TextView) view.findViewById(R.id.tv_unicode_10);
        tv_unicode_10.setText(unicode_strs[9]);
        tv_unicode_10.setOnClickListener(this);
        tv_unicode_11 = (TextView) view.findViewById(R.id.tv_unicode_11);
        tv_unicode_11.setText(unicode_strs[10]);
        tv_unicode_11.setOnClickListener(this);
        tv_unicode_12 = (TextView) view.findViewById(R.id.tv_unicode_12);
        tv_unicode_12.setText(unicode_strs[11]);
        tv_unicode_12.setOnClickListener(this);
        tv_unicode_13 = (TextView) view.findViewById(R.id.tv_unicode_13);
        tv_unicode_13.setText(unicode_strs[12]);
        tv_unicode_13.setOnClickListener(this);
        tv_unicode_14 = (TextView) view.findViewById(R.id.tv_unicode_14);
        tv_unicode_14.setText(unicode_strs[13]);
        tv_unicode_14.setOnClickListener(this);
        tv_unicode_15 = (TextView) view.findViewById(R.id.tv_unicode_15);
        tv_unicode_15.setText(unicode_strs[14]);
        tv_unicode_15.setOnClickListener(this);
        tv_unicode_16 = (TextView) view.findViewById(R.id.tv_unicode_16);
        tv_unicode_16.setText(unicode_strs[15]);
        tv_unicode_16.setOnClickListener(this);
        tv_unicode_17 = (TextView) view.findViewById(R.id.tv_unicode_17);
        tv_unicode_17.setText(unicode_strs[16]);
        tv_unicode_17.setOnClickListener(this);
        tv_unicode_18 = (TextView) view.findViewById(R.id.tv_unicode_18);
        tv_unicode_18.setText(unicode_strs[17]);
        tv_unicode_18.setOnClickListener(this);
        tv_unicode_19 = (TextView) view.findViewById(R.id.tv_unicode_19);
        tv_unicode_19.setText(unicode_strs[18]);
        tv_unicode_19.setOnClickListener(this);
        tv_unicode_20 = (TextView) view.findViewById(R.id.tv_unicode_20);
        tv_unicode_20.setText(unicode_strs[19]);
        tv_unicode_20.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        fontCallback.onImageSelected(((TextView)view).getText().toString());
//        switch (view.getId()){
//        }
    }
    public void setCharCount(String string){
        if (tvTxtNumber!=null){

            tvTxtNumber.setText(string);
        }
    }
    private HLAdapter hlAdapter ;
    private class HLAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return unicode_strs.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View contView, ViewGroup viewGroup) {
            View view ;
            ViewHolder viewHolder;
            if (contView!=null){
                view = contView;
                viewHolder = (ViewHolder) view.getTag();
            }else {
                view = View.inflate(context,R.layout.horizontallv_item,null);
                viewHolder = new ViewHolder();
                viewHolder.tv_unicode_char =(TextView) view.findViewById(R.id.tv_unicode_char);
                view.setTag(viewHolder);
            }
            viewHolder.tv_unicode_char.setText(unicode_strs[position]);
            return view;
        }
    }
    private class ViewHolder{
         TextView tv_unicode_char;
    }
    /***
     * 设置显示输入字符字数
     */
    private void setLeftCount() {
//        tvTxtNumber.setText(getInputCount() + "/" + MAX_COUNT + "");
    }

//    private long getInputCount() {
//
////        return clearEditText.getText().toString().length();//以字符数显示
//    }
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
}
