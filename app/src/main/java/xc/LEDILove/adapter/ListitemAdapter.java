package xc.LEDILove.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xc.LEDILove.Bean.TextBean;
import xc.LEDILove.R;
import xc.LEDILove.bluetooth.StaticDatas;
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
    private boolean isMatixColor = false;
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
        if (StaticDatas.getInstance().isSupportMarFullColor) isMatixColor = true;
        else isMatixColor = false;
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
        if (isMatixColor){
            holder.mTitle.setText(setTextSpan(historyListItemList.get(position).umsResultBean.beanList,historyListItemList.get(position).umsResultBean.body));
        }else {
            holder.mTitle.setText(historyListItemList.get(position).umsResultBean.body);
        }
//        holder.mTitle.setTextColor(historyListItemList.get(position).umsResultBean.color);
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
    private SpannableStringBuilder setTextSpan(List<TextBean> textBeanList, String text) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("");
        //这里使用字符信息对象集合尺寸做上限，而不是 text长度，理论上是一样的数值，但这样可以避免下面取的时候 超集合最大值(不知道怎么发生的)
        if (textBeanList==null){
            textBeanList = getDefultTextBeanList(text);
        }
        for (int j= 0;j<textBeanList.size();j++){
            if (j<text.length()){
                SpannableString spannableString = new SpannableString(text.substring(j,j+1));
                spannableString.setSpan(new BackgroundColorSpan(parseColor(textBeanList.get(j).getBackdrop())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(parseColor(textBeanList.get(j).getFont())),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.append(spannableString);
                spannableString = null;
            }
        }
        return spannableStringBuilder;
    }
    private int parseColor(int position) {
        int color = -1;
        switch (position){
            case 0:
                color =  mContext.getResources().getColor(R.color.black);
                break;
            case 1:
                color = mContext.getResources().getColor(R.color.red);
                break;
            case 2:
                color = mContext.getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = mContext.getResources().getColor(R.color.dark_green);
                break;
            case 4:
                color = mContext.getResources().getColor(R.color.cyan);
                break;
            case 5:
                color = mContext.getResources().getColor(R.color.blove);
                break;
            case 6:
                color = mContext.getResources().getColor(R.color.purple);
                break;
            case 7:
                color = mContext.getResources().getColor(R.color.white);
                break;
        }
        return color;
    }
    private List<TextBean> getDefultTextBeanList(String body) {
        char [] chars = body.toCharArray();
        List<TextBean> result = new ArrayList<>();
        for (int i=0;i<chars.length;i++){
            TextBean bean = new TextBean();
            bean.setFont(1);
            bean.setBackdrop(0);
            bean.setCharacter(chars[i]);
            result.add(bean);
        }
        return result;
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