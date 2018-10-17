package xc.LEDILove.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import xc.LEDILove.Bean.ResourceData;
import xc.LEDILove.R;

/**
 * Created by xcgd on 2018/5/31.
 */

public class ControllerRecyviewAdapter extends RecyclerView.Adapter<ControllerRecyviewAdapter.MyViewHolder>{
    private final String TAG = ControllerRecyviewAdapter.class.getSimpleName();
    private List<ResourceData> resourcedata;
    private Context context;
    private List<Boolean> chooses;
    public ControllerRecyviewAdapter(Context context,List<ResourceData> resourcedata){
        this.resourcedata = resourcedata;
        this.context = context;
        chooses = new ArrayList<>();
        for (int i=0;i<resourcedata.size();i++){
            chooses.add(false);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_controller_recyclerview,null);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ResourceData data = resourcedata.get(position);
        if (data.getDrawable()!=null){
            try {
                GifDrawable drawable = new GifDrawable(data.getDrawable());
                holder.gifimageview_image.setImageDrawable(drawable);
                holder.tv_text.setVisibility(View.GONE);
                holder.gifimageview_image.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            holder.tv_text.setText(data.getText());
            holder.tv_text.setVisibility(View.VISIBLE);
            holder.gifimageview_image.setVisibility(View.GONE);
        }

        holder.checkbox_controller_choose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                chooses.set(position,b);
            }
        });
        holder.iv_controller_replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: position>>"+position);
            }
        });
        holder.tv_controller_message_number.setText(position+1+"");
    }

    @Override
    public int getItemCount() {
        return resourcedata.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private GifImageView gifimageview_image;
        private TextView tv_text;
        private TextView tv_controller_message_number;
        private CheckBox checkbox_controller_choose;
        private ImageView iv_controller_replace;
        public MyViewHolder(View itemView) {
            super(itemView);
            gifimageview_image = (GifImageView) itemView.findViewById(R.id.gifimageview_image);
            tv_text = (TextView) itemView.findViewById(R.id.tv_text);
            tv_controller_message_number = (TextView) itemView.findViewById(R.id.tv_controller_message_number);
            checkbox_controller_choose = (CheckBox) itemView.findViewById(R.id.checkbox_controller_choose);
            iv_controller_replace = (ImageView) itemView.findViewById(R.id.iv_controller_replace);
        }
    }
}