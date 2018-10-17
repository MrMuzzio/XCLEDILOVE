package xc.LEDILove.fragment;

import android.annotation.SuppressLint;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.Bean.ResourceData;
import xc.LEDILove.R;
import xc.LEDILove.adapter.ControllerRecyviewAdapter;
import xc.LEDILove.adapter.GridSpacingItemDecoration;
import xc.LEDILove.utils.TimonLibary;

/**
 * Created by xcgd on 2018/5/31.
 */

@SuppressLint("ValidFragment")
public class ControllerFragment extends Fragment {
    private final String TAG = ControllerFragment.class.getSimpleName();
    public ControllerFragment(Context context) {
        this.context = context;
    }
    public ControllerFragment(){

    }
    private ControllerRecyviewAdapter recyviewAdapter;
    private RecyclerView rv_controller_message;
    private Context context;
    private List<ResourceData> resourceDataList;
    private ImageView iv_remote_bg_1;
    private ImageView iv_remote_bg_2;
    private ImageView iv_remote_bg_3;
    private ImageView iv_remote_bg_4;
    private ImageView iv_remote_bg_5;
    private ImageView iv_remote_bg_6;
    private ImageView iv_remote_bg_7;
    private ImageView iv_remote_bg_8;
    private ImageView iv_eye_1;
    private ImageView iv_eye_2;
    private ImageView iv_eye_3;
    private ImageView iv_eye_4;
    private ImageView iv_eye_5;
    private ImageView iv_eye_6;
    private ImageView iv_eye_7;
    private ImageView iv_eye_8;
    private ImageView iv_remote_edit_1;
    private ImageView iv_remote_edit_2;
    private ImageView iv_remote_edit_3;
    private ImageView iv_remote_edit_4;
    private ImageView iv_remote_edit_5;
    private ImageView iv_remote_edit_6;
    private ImageView iv_remote_edit_7;
    private ImageView iv_remote_edit_8;
    private ImageView iv_remote_more_1;
    private ImageView iv_remote_more_2;
    private ImageView iv_remote_more_3;
    private ImageView iv_remote_more_4;
    private ImageView iv_remote_more_5;
    private ImageView iv_remote_more_6;
    private ImageView iv_remote_more_7;
    private ImageView iv_remote_more_8;
    private ImageView iv_msg_1;
    private ImageView iv_remote_select_all_1;
    private ImageView iv_remote_select_all_2;
    private LinearLayout ll_remote_1;
    private RemoteBGListener remoteBGListener;
    private RemoteEditListener remoteEditListener;
    private RemoteBGLongClickListener remoteBGLongClickListener;
    private MoreEditListener moreEditListener;
    private EyeListener eyeListener;
    private String filepath;
    private File[] files;
    private int image_width;
    private boolean[] MsgBGSelecteds;//用布尔数组来记录当前信息的选择情况 ,最后两位 用来表示两个全选按钮的状态，true 位选择 false 未选择
    private boolean[] editVisiables;//记录信息长按出现的编辑状态
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller,container,false);
        remoteBGListener = new RemoteBGListener();
        remoteEditListener = new RemoteEditListener();
        eyeListener = new EyeListener();
        remoteBGLongClickListener = new RemoteBGLongClickListener();
        moreEditListener = new MoreEditListener();
        findView(view);
        initView();
//        initData();
        return view;
    }

    private void initData() {
        int width = TimonLibary.getScreenWidth(context);
        image_width = TimonLibary.dp2px(context,(width-ll_remote_1.getWidth()-2)/2);
        filepath = Environment.getExternalStorageDirectory()+"/LEDILOVE/bit/";
        getPicFile();
        Log.e(TAG, "initData: path>>"+files[0]);
//        iv_msg_1.setMinimumHeight(image_width);
//        iv_msg_1.setMinimumWidth(image_width);
        iv_msg_1.setImageURI(Uri.fromFile(files[0]));
    }
    private void getPicFile() {
        File f= new File(filepath);
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
    }
    private void initView() {
        MsgBGSelecteds = new boolean[]{false,false,false,false,false,false,false,false,false,false};//初始状态为 未选择任何信息,共10位
        editVisiables = new boolean[]{false,false,false,false,false,false,false,false};//初始状态为 未选择任何信息,共10位
        resourceDataList = new ArrayList<>();
        getGifFiles();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
        rv_controller_message.setLayoutManager(gridLayoutManager);
        recyviewAdapter = new ControllerRecyviewAdapter(context,resourceDataList);
                int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = 8;//每一个矩形的间距
        boolean includeEdge = false;//如果设置成false那边缘地带就没有间距s
        //设置每个item间距
        rv_controller_message.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        rv_controller_message.setAdapter(recyviewAdapter);
    }

    private void findView(View view) {
        ll_remote_1 = (LinearLayout) view.findViewById(R.id.ll_remote_1);
        rv_controller_message = (RecyclerView) view.findViewById(R.id.rv_controller_message);
        iv_remote_bg_1 =  (ImageView) view.findViewById(R.id.iv_remote_bg_1);
        iv_remote_bg_2 =  (ImageView) view.findViewById(R.id.iv_remote_bg_2);
        iv_remote_bg_3 =  (ImageView) view.findViewById(R.id.iv_remote_bg_3);
        iv_remote_bg_4 =  (ImageView) view.findViewById(R.id.iv_remote_bg_4);
        iv_remote_bg_5 =  (ImageView) view.findViewById(R.id.iv_remote_bg_5);
        iv_remote_bg_6 =  (ImageView) view.findViewById(R.id.iv_remote_bg_6);
        iv_remote_bg_7 =  (ImageView) view.findViewById(R.id.iv_remote_bg_7);
        iv_remote_bg_8 =  (ImageView) view.findViewById(R.id.iv_remote_bg_8);
        iv_eye_1 =  (ImageView) view.findViewById(R.id.iv_eye_1);
        iv_eye_2 =  (ImageView) view.findViewById(R.id.iv_eye_2);
        iv_eye_3 =  (ImageView) view.findViewById(R.id.iv_eye_3);
        iv_eye_4 =  (ImageView) view.findViewById(R.id.iv_eye_4);
        iv_eye_5 =  (ImageView) view.findViewById(R.id.iv_eye_5);
        iv_eye_6 =  (ImageView) view.findViewById(R.id.iv_eye_6);
        iv_eye_7 =  (ImageView) view.findViewById(R.id.iv_eye_7);
        iv_eye_8 =  (ImageView) view.findViewById(R.id.iv_eye_8);
        iv_remote_more_1 =  (ImageView) view.findViewById(R.id.iv_remote_more_1);
        iv_remote_more_2 =  (ImageView) view.findViewById(R.id.iv_remote_more_2);
        iv_remote_more_3 =  (ImageView) view.findViewById(R.id.iv_remote_more_3);
        iv_remote_more_4 =  (ImageView) view.findViewById(R.id.iv_remote_more_4);
        iv_remote_more_5 =  (ImageView) view.findViewById(R.id.iv_remote_more_5);
        iv_remote_more_6 =  (ImageView) view.findViewById(R.id.iv_remote_more_6);
        iv_remote_more_7 =  (ImageView) view.findViewById(R.id.iv_remote_more_7);
        iv_remote_more_8 =  (ImageView) view.findViewById(R.id.iv_remote_more_8);
        iv_remote_more_1.setOnClickListener(moreEditListener);
        iv_remote_more_2.setOnClickListener(moreEditListener);
        iv_remote_more_3.setOnClickListener(moreEditListener);
        iv_remote_more_4.setOnClickListener(moreEditListener);
        iv_remote_more_5.setOnClickListener(moreEditListener);
        iv_remote_more_6.setOnClickListener(moreEditListener);
        iv_remote_more_7.setOnClickListener(moreEditListener);
        iv_remote_more_8.setOnClickListener(moreEditListener);
        iv_eye_1.setVisibility(View.GONE);
        iv_eye_2.setVisibility(View.GONE);
        iv_eye_3.setVisibility(View.GONE);
        iv_eye_4.setVisibility(View.GONE);
        iv_eye_5.setVisibility(View.GONE);
        iv_eye_6.setVisibility(View.GONE);
        iv_eye_7.setVisibility(View.GONE);
        iv_eye_8.setVisibility(View.GONE);
        iv_remote_edit_1 =  (ImageView) view.findViewById(R.id.iv_remote_edit_1);
        iv_remote_edit_2 =  (ImageView) view.findViewById(R.id.iv_remote_edit_2);
        iv_remote_edit_3 =  (ImageView) view.findViewById(R.id.iv_remote_edit_3);
        iv_remote_edit_4 =  (ImageView) view.findViewById(R.id.iv_remote_edit_4);
        iv_remote_edit_5 =  (ImageView) view.findViewById(R.id.iv_remote_edit_5);
        iv_remote_edit_6 =  (ImageView) view.findViewById(R.id.iv_remote_edit_6);
        iv_remote_edit_7 =  (ImageView) view.findViewById(R.id.iv_remote_edit_7);
        iv_remote_edit_8 =  (ImageView) view.findViewById(R.id.iv_remote_edit_8);
        iv_remote_edit_1.setVisibility(View.GONE);
        iv_remote_edit_2.setVisibility(View.GONE);
        iv_remote_edit_3.setVisibility(View.GONE);
        iv_remote_edit_4.setVisibility(View.GONE);
        iv_remote_edit_5.setVisibility(View.GONE);
        iv_remote_edit_6.setVisibility(View.GONE);
        iv_remote_edit_7.setVisibility(View.GONE);
        iv_remote_edit_8.setVisibility(View.GONE);
        iv_remote_bg_1.setOnClickListener(remoteBGListener);
        iv_remote_bg_2.setOnClickListener(remoteBGListener);
        iv_remote_bg_3.setOnClickListener(remoteBGListener);
        iv_remote_bg_4.setOnClickListener(remoteBGListener);
        iv_remote_bg_5.setOnClickListener(remoteBGListener);
        iv_remote_bg_6.setOnClickListener(remoteBGListener);
        iv_remote_bg_7.setOnClickListener(remoteBGListener);
        iv_remote_bg_8.setOnClickListener(remoteBGListener);
        iv_remote_bg_1.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_2.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_3.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_4.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_5.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_6.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_7.setOnLongClickListener(remoteBGLongClickListener);
        iv_remote_bg_8.setOnLongClickListener(remoteBGLongClickListener);
        iv_eye_1.setOnClickListener(eyeListener);
        iv_eye_2.setOnClickListener(eyeListener);
        iv_eye_3.setOnClickListener(eyeListener);
        iv_eye_4.setOnClickListener(eyeListener);
        iv_eye_5.setOnClickListener(eyeListener);
        iv_eye_6.setOnClickListener(eyeListener);
        iv_eye_7.setOnClickListener(eyeListener);
        iv_eye_8.setOnClickListener(eyeListener);
        iv_remote_edit_1.setOnClickListener(remoteEditListener);
        iv_remote_edit_2.setOnClickListener(remoteEditListener);
        iv_remote_edit_3.setOnClickListener(remoteEditListener);
        iv_remote_edit_4.setOnClickListener(remoteEditListener);
        iv_remote_edit_5.setOnClickListener(remoteEditListener);
        iv_remote_edit_6.setOnClickListener(remoteEditListener);
        iv_remote_edit_7.setOnClickListener(remoteEditListener);
        iv_remote_edit_8.setOnClickListener(remoteEditListener);
        iv_remote_select_all_1 =  (ImageView) view.findViewById(R.id.iv_remote_select_all_1);
        iv_remote_select_all_1.setOnClickListener(remoteBGListener);
        iv_remote_select_all_2 =  (ImageView) view.findViewById(R.id.iv_remote_select_all_2);
        iv_remote_select_all_2.setOnClickListener(remoteBGListener);

        iv_msg_1 =  (ImageView) view.findViewById(R.id.iv_msg_1);

    }
    private void getGifFiles() {
        File[] files;
        File f= new File(Environment.getExternalStorageDirectory()+"/LEDILOVE/gif/");
        if (!f.exists()) {//判断路径是否存在
            return ;
        }
        files = f.listFiles();
        if (files.length>8){
            for (int i=0;i<8;i++){
                ResourceData data = new ResourceData();
                data.setDrawable(files[i]);
                resourceDataList.add(data);
            }
        }
    }
    class MoreEditListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_remote_more_1:
                    showOrHideEditor(1);
                    break;
                case R.id.iv_remote_more_2:
                    showOrHideEditor(2);
                    break;
                case R.id.iv_remote_more_3:
                    showOrHideEditor(3);
                    break;
                case R.id.iv_remote_more_4:
                    showOrHideEditor(4);
                    break;
                case R.id.iv_remote_more_5:
                    showOrHideEditor(5);
                    break;
                case R.id.iv_remote_more_6:
                    showOrHideEditor(6);
                    break;
                case R.id.iv_remote_more_7:
                    showOrHideEditor(7);
                    break;
                case R.id.iv_remote_more_8:
                    showOrHideEditor(8);
                    break;
            }
        }
    }

    private void showOrHideEditor(int i) {
        switch (i){
            case 1:
                if (editVisiables[0]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[0] = false;
                    iv_eye_1.setVisibility(View.GONE);
                    iv_remote_edit_1.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[0] = true;
                    iv_eye_1.setVisibility(View.VISIBLE);
                    iv_remote_edit_1.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                if (editVisiables[1]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[1] = false;
                    iv_eye_2.setVisibility(View.GONE);
                    iv_remote_edit_2.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[1] = true;
                    iv_eye_2.setVisibility(View.VISIBLE);
                    iv_remote_edit_2.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                if (editVisiables[2]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[2] = false;
                    iv_eye_3.setVisibility(View.GONE);
                    iv_remote_edit_3.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[2] = true;
                    iv_eye_3.setVisibility(View.VISIBLE);
                    iv_remote_edit_3.setVisibility(View.VISIBLE);
                }
                break;
            case 4:
                if (editVisiables[3]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[3] = false;
                    iv_eye_4.setVisibility(View.GONE);
                    iv_remote_edit_4.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[3] = true;
                    iv_eye_4.setVisibility(View.VISIBLE);
                    iv_remote_edit_4.setVisibility(View.VISIBLE);
                }
                break;
            case 5:
                if (editVisiables[4]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[4] = false;
                    iv_eye_5.setVisibility(View.GONE);
                    iv_remote_edit_5.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[4] = true;
                    iv_eye_5.setVisibility(View.VISIBLE);
                    iv_remote_edit_5.setVisibility(View.VISIBLE);
                }
                break;
            case 6:
                if (editVisiables[5]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[5] = false;
                    iv_eye_6.setVisibility(View.GONE);
                    iv_remote_edit_6.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[5] = true;
                    iv_eye_6.setVisibility(View.VISIBLE);
                    iv_remote_edit_6.setVisibility(View.VISIBLE);
                }
                break;
            case 7:
                if (editVisiables[6]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[6] = false;
                    iv_eye_7.setVisibility(View.GONE);
                    iv_remote_edit_7.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[6] = true;
                    iv_eye_7.setVisibility(View.VISIBLE);
                    iv_remote_edit_7.setVisibility(View.VISIBLE);
                }
                break;
            case 8:
                if (editVisiables[7]){//已经是编辑状态 改变成非编辑状态
                    editVisiables[7] = false;
                    iv_eye_8.setVisibility(View.GONE);
                    iv_remote_edit_8.setVisibility(View.GONE);
                }else {//否则 改变成编辑状态
                    editVisiables[7] = true;
                    iv_eye_8.setVisibility(View.VISIBLE);
                    iv_remote_edit_8.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    class RemoteBGLongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()){
                case R.id.iv_remote_bg_1:
                   showOrHideEditor(1);
                    break;
                case R.id.iv_remote_bg_2:
                   showOrHideEditor(2);
                    break;
                case R.id.iv_remote_bg_3:
                   showOrHideEditor(3);
                    break;
                case R.id.iv_remote_bg_4:
                   showOrHideEditor(4);
                    break;
                case R.id.iv_remote_bg_5:
                   showOrHideEditor(5);
                    break;
                case R.id.iv_remote_bg_6:
                   showOrHideEditor(6);
                    break;
                case R.id.iv_remote_bg_7:
                   showOrHideEditor(7);
                    break;
                case R.id.iv_remote_bg_8:
                   showOrHideEditor(8);
                    break;
            }
            return true;
        }
    }
    /**
     * 信息背景点击事件监听
     */
    class RemoteBGListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.iv_remote_bg_1:
                    if (MsgBGSelecteds[0]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_1.setBackgroundResource(R.drawable.ic_remote_left_up_1);
                        MsgBGSelecteds[0] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_1.setBackgroundResource(R.drawable.ic_remote_left_up_sel_1);
                        MsgBGSelecteds[0] =true;
                    }
                    break;
                case R.id.iv_remote_bg_2:
                    if (MsgBGSelecteds[1]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_2.setBackgroundResource(R.drawable.ic_remote_right_up_2);
                        MsgBGSelecteds[1] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_2.setBackgroundResource(R.drawable.ic_remote_right_up_sel_2);
                        MsgBGSelecteds[1] =true;
                    }
                    break;
                case R.id.iv_remote_bg_3:
                    if (MsgBGSelecteds[2]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_3.setBackgroundResource(R.drawable.ic_remote_left_down_3);
                        MsgBGSelecteds[2] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_3.setBackgroundResource(R.drawable.ic_remote_left_down_sel_3);
                        MsgBGSelecteds[2] =true;
                    }
                    break;
                case R.id.iv_remote_bg_4:
                    if (MsgBGSelecteds[3]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_4.setBackgroundResource(R.drawable.ic_remote_right_down_4);
                        MsgBGSelecteds[3] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_4.setBackgroundResource(R.drawable.ic_remote_right_down_sel_4);
                        MsgBGSelecteds[3] =true;
                    }
                    break;
                case R.id.iv_remote_bg_5:
                    if (MsgBGSelecteds[4]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_5.setBackgroundResource(R.drawable.ic_remote_left_up_5);
                        MsgBGSelecteds[4] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_5.setBackgroundResource(R.drawable.ic_remote_left_up_sel_5);
                        MsgBGSelecteds[4] =true;
                    }
                    break;
                case R.id.iv_remote_bg_6:
                    if (MsgBGSelecteds[5]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_6.setBackgroundResource(R.drawable.ic_remote_right_up_6);
                        MsgBGSelecteds[5] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_6.setBackgroundResource(R.drawable.ic_remote_right_up_sel_6);
                        MsgBGSelecteds[5] =true;
                    }
                    break;
                case R.id.iv_remote_bg_7:
                    if (MsgBGSelecteds[6]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_7.setBackgroundResource(R.drawable.ic_remote_left_down_7);
                        MsgBGSelecteds[6] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_7.setBackgroundResource(R.drawable.ic_remote_left_down_sel_7);
                        MsgBGSelecteds[6] =true;
                    }
                    break;
                case R.id.iv_remote_bg_8:
                    if (MsgBGSelecteds[7]){//如果已经为选择状态 改为未选择状态
                        iv_remote_bg_8.setBackgroundResource(R.drawable.ic_remote_right_down_8);
                        MsgBGSelecteds[7] =false;
                    }else {//否则 改为选择状态
                        iv_remote_bg_8.setBackgroundResource(R.drawable.ic_remote_right_down_sel_8);
                        MsgBGSelecteds[7] =true;
                    }
                    break;
                case R.id.iv_remote_select_all_1:
                    if (MsgBGSelecteds[8]){//如果已经为选择状态 改为未选择状态
                        iv_remote_select_all_1.setBackgroundResource(R.drawable.ic_remote_ring_centrol);
                        MsgBGSelecteds[8] =false;
                    }else {//否则 改为选择状态
                        iv_remote_select_all_1.setBackgroundResource(R.drawable.ic_remote_ring_centrol_sel);
                        MsgBGSelecteds[8] =true;
                    }
                    refreshBGSelectStatus(0,MsgBGSelecteds[8]);
                    break;
                case R.id.iv_remote_select_all_2:
                    if (MsgBGSelecteds[9]){//如果已经为选择状态 改为未选择状态
                        iv_remote_select_all_2.setBackgroundResource(R.drawable.ic_remote_ring_centrol);
                        MsgBGSelecteds[9] =false;
                    }else {//否则 改为选择状态
                        iv_remote_select_all_2.setBackgroundResource(R.drawable.ic_remote_ring_centrol_sel);
                        MsgBGSelecteds[9] =true;
                    }
                    refreshBGSelectStatus(1,MsgBGSelecteds[9]);
                    break;
            }
            if (MsgBGSelecteds[0]&&MsgBGSelecteds[1]&&MsgBGSelecteds[2]&&MsgBGSelecteds[3]){
                iv_remote_select_all_1.setBackgroundResource(R.drawable.ic_remote_ring_centrol_sel);
                MsgBGSelecteds[8] =true;
            }else {
                iv_remote_select_all_1.setBackgroundResource(R.drawable.ic_remote_ring_centrol);
                MsgBGSelecteds[8] =false;
            }
            if (MsgBGSelecteds[4]&&MsgBGSelecteds[5]&&MsgBGSelecteds[6]&&MsgBGSelecteds[7]){
                iv_remote_select_all_2.setBackgroundResource(R.drawable.ic_remote_ring_centrol_sel);
                MsgBGSelecteds[9] =true;
            }else {
                iv_remote_select_all_2.setBackgroundResource(R.drawable.ic_remote_ring_centrol);
                MsgBGSelecteds[9] =false;
            }
        }

    }

    /**
     * @param index 刷新的位置 0 为1号  1位2号
     * @param isselect 状态
     */
    private void refreshBGSelectStatus(int index,boolean isselect) {
        switch (index){
            case 0:
                MsgBGSelecteds[0] = isselect;
                MsgBGSelecteds[1] = isselect;
                MsgBGSelecteds[2] = isselect;
                MsgBGSelecteds[3] = isselect;
                if (isselect){
                    iv_remote_bg_1.setBackgroundResource(R.drawable.ic_remote_left_up_sel_1);
                    iv_remote_bg_2.setBackgroundResource(R.drawable.ic_remote_right_up_sel_2);
                    iv_remote_bg_3.setBackgroundResource(R.drawable.ic_remote_left_down_sel_3);
                    iv_remote_bg_4.setBackgroundResource(R.drawable.ic_remote_right_down_sel_4);
                }else {
                    iv_remote_bg_1.setBackgroundResource(R.drawable.ic_remote_left_up_1);
                    iv_remote_bg_2.setBackgroundResource(R.drawable.ic_remote_right_up_2);
                    iv_remote_bg_3.setBackgroundResource(R.drawable.ic_remote_left_down_3);
                    iv_remote_bg_4.setBackgroundResource(R.drawable.ic_remote_right_down_4);
                }
                break;
            case 1:
                MsgBGSelecteds[4] = isselect;
                MsgBGSelecteds[5] = isselect;
                MsgBGSelecteds[6] = isselect;
                MsgBGSelecteds[7] = isselect;
                if (isselect){
                    iv_remote_bg_5.setBackgroundResource(R.drawable.ic_remote_left_up_sel_5);
                    iv_remote_bg_6.setBackgroundResource(R.drawable.ic_remote_right_up_sel_6);
                    iv_remote_bg_7.setBackgroundResource(R.drawable.ic_remote_left_down_sel_7);
                    iv_remote_bg_8.setBackgroundResource(R.drawable.ic_remote_right_down_sel_8);
                }else {
                    iv_remote_bg_5.setBackgroundResource(R.drawable.ic_remote_left_up_5);
                    iv_remote_bg_6.setBackgroundResource(R.drawable.ic_remote_right_up_6);
                    iv_remote_bg_7.setBackgroundResource(R.drawable.ic_remote_left_down_7);
                    iv_remote_bg_8.setBackgroundResource(R.drawable.ic_remote_right_down_8);
                }
                break;
        }
    }

    class EyeListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            int index = 0;
            switch (view.getId()){
                case R.id.iv_eye_1:
                    index = 1;
                    break;
                case R.id.iv_eye_2:
                    index = 2;
                    break;
                case R.id.iv_eye_3:
                    index = 3;
                    break;
                case R.id.iv_eye_4:
                    index = 4;
                    break;
                case R.id.iv_eye_5:
                    index = 5;
                    break;
                case R.id.iv_eye_6:
                    index = 6;
                    break;
                case R.id.iv_eye_7:
                    index = 7;
                    break;
                case R.id.iv_eye_8:
                    index = 8;
                    break;
            }
        }
    }
    class RemoteEditListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            int index = 0;
            switch (view.getId()){
                case R.id.iv_remote_edit_1:
                    index = 1;
                    break;
                case R.id.iv_remote_edit_2:
                    index = 2;
                    break;
                case R.id.iv_remote_edit_3:
                    index = 3;
                    break;
                case R.id.iv_remote_edit_4:
                    index = 4;
                    break;
                case R.id.iv_remote_edit_5:
                    index = 5;
                    break;
                case R.id.iv_remote_edit_6:
                    index = 6;
                    break;
                case R.id.iv_remote_edit_7:
                    index = 7;
                    break;
                case R.id.iv_remote_edit_8:
                    index = 8;
                    break;
            }
        }
    }
}
