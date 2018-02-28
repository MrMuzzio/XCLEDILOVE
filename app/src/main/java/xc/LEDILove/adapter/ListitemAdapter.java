package xc.LEDILove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.R;
import xc.LEDILove.db.UmsResultBean;

/**
 * craete by YuChang on 2017/11/23 17:19
 */

public class ListitemAdapter extends BaseAdapter {

    public List<HistoryListItem> getHistoryListItemList() {
        return historyListItemList;
    }

    private List<HistoryListItem> historyListItemList;
    private HistoryListItem historyListItem;

    public class HistoryListItem {
        public UmsResultBean umsResultBean;
        public Boolean isChecked;
    }

    public interface MyCollectionRecordListener {
        void onclick(int selectSize);
    }

    private MyCollectionRecordListener onClickListener;
    private Context mContext;
    private LayoutInflater mInflater;

    public ListitemAdapter(Context context, List<UmsResultBean> list, MyCollectionRecordListener onClickListener) {
        historyListItemList = new ArrayList<>();
        for (UmsResultBean umsResultBean : list) {
            historyListItem = new HistoryListItem();
            historyListItem.umsResultBean = umsResultBean;
            historyListItem.isChecked = false;
            historyListItemList.add(historyListItem);
        }
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.onClickListener = onClickListener;
    }

    @Override
    public int getCount() {
        return historyListItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyListItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.check_list_item, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.title);
            holder.mCb = (CheckBox) convertView.findViewById(R.id.cb);
            holder.mContent = (LinearLayout) convertView.findViewById(R.id.ll_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitle.setText(historyListItemList.get(position).umsResultBean.body);
        holder.mTitle.setTextColor(historyListItemList.get(position).umsResultBean.color);
        holder.mCb.setChecked(historyListItemList.get(position).isChecked);
        holder.mContent.setTag(position + "");
        holder.mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = Integer.parseInt(v.getTag().toString());
                historyListItemList.get(index).isChecked = !historyListItemList.get(index).isChecked;
                int size = 0;
                for (ListitemAdapter.HistoryListItem historyListItem : historyListItemList) {
                    if (historyListItem.isChecked) {
                        size++;
                    }
                }
                onClickListener.onclick(size);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void refresh(List<UmsResultBean> list) {
        historyListItemList = new ArrayList<>();
        for (UmsResultBean umsResultBean : list) {
            historyListItem = new HistoryListItem();
            historyListItem.umsResultBean = umsResultBean;
            historyListItem.isChecked = false;
            historyListItemList.add(historyListItem);
        }
        notifyDataSetChanged();
    }
}