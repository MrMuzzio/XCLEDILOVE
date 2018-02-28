package xc.LEDILove.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import xc.LEDILove.R;
import xc.LEDILove.db.UmsResultBean;


/**
 */
public class HistoryRecordRecyclerAdapter extends RecyclerView.Adapter<HistoryRecordRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<UmsResultBean> list = new ArrayList<>();

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvIndex;
        TextView tvContent;
        LinearLayout llContent;

        public MyViewHolder(View itemView) {
            super(itemView);
            llContent = (LinearLayout) itemView.findViewById(R.id.ll_content);
            tvIndex = (TextView) itemView.findViewById(R.id.tvIndex);
            tvContent = (TextView) itemView.findViewById(R.id.tvContent);
        }
    }

    public interface MyCollectionRecordListener {

        void onclick(int position);

        void onLongClick(int position);

    }

    private MyCollectionRecordListener onClickListener;

    public HistoryRecordRecyclerAdapter(Context context, MyCollectionRecordListener onClickListener) {
        this.context = context;
        list = new ArrayList<>();
        this.onClickListener = onClickListener;
    }

    public void addList(ArrayList<UmsResultBean> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void clearList() {
        this.list.clear();
    }

    public ArrayList<UmsResultBean> getList() {
        return list == null ? new ArrayList<UmsResultBean>() : list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UmsResultBean umsResultBean = list.get(position);
        holder.tvContent.setText(umsResultBean.body);
        holder.tvIndex.setText((position + 1)+"");
        holder.llContent.setTag(position);
        holder.llContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onclick(Integer.parseInt(v.getTag().toString()));
            }
        });
        holder.llContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.onLongClick(Integer.parseInt(v.getTag().toString()));
                return true;
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

}