package xc.LEDILove.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;


/**
 */
public class BrightAndModelRecyclerAdapter extends RecyclerView.Adapter<BrightAndModelRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<BrightAndModelInfo> list = new ArrayList<>();

    private static class BrightAndModelInfo {
        public String index;
        public String name;
        //是否选中
        public Boolean isSelected;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.name);
        }
    }

    public interface MyCollectionRecordListener {
        void onclick(String keyName);
    }

    private MyCollectionRecordListener onClickListener;

    public BrightAndModelRecyclerAdapter(Context context, MyCollectionRecordListener onClickListener) {
        this.context = context;
        list = new ArrayList<>();
        this.onClickListener = onClickListener;
    }

    public void addList(List<String> list) {
        ArrayList<BrightAndModelInfo> brightAndModelInfoArrayList = new ArrayList<>();
        int position = 1;
        for (String name : list) {
            BrightAndModelInfo innerCarInfo = new BrightAndModelInfo();
            innerCarInfo.index = position + "";
            innerCarInfo.name = name;
            if (position == 1) {
                innerCarInfo.isSelected = true;
            } else {
                innerCarInfo.isSelected = false;
            }
            position++;
            brightAndModelInfoArrayList.add(innerCarInfo);
        }
        this.list.addAll(brightAndModelInfoArrayList);
        notifyDataSetChanged();
    }

    public void clearList() {
        this.list.clear();
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bright_model_recycview_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BrightAndModelInfo body = list.get(position);
        holder.tvName.setText(body.name);
        holder.tvName.setTag(position);
        holder.tvName.setEnabled(!body.isSelected);
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelected(v);
            }
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void onSelected(View view) {
        onClickListener.onclick(list.get(Integer.parseInt(view.getTag().toString())).index);
        for (BrightAndModelInfo innerCarInfo : list) {
            innerCarInfo.isSelected = false;
        }
        list.get(Integer.parseInt(view.getTag().toString())).isSelected = true;
        notifyDataSetChanged();
    }

    /***
     * 设置被选中item
     * @param index
     */
    public void setSelected(int index) {
        for (BrightAndModelInfo innerCarInfo : list) {
            innerCarInfo.isSelected = false;
        }
        list.get(index).isSelected = true;
        notifyDataSetChanged();
    }
}