package xc.LEDILove.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import xc.LEDILove.R;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public class DeviceListAdapter extends BaseAdapter implements Comparator<SearchResult> {

    private Context mContext;
    private DeviceListOnClickLisener mDeviceListOnClickLisener;
    public interface DeviceListOnClickLisener {
        void onClickItem(String mac,String name);
    }

    private List<SearchResult> mDataList;

    public DeviceListAdapter(Context context,DeviceListOnClickLisener deviceListOnClickLisener) {
        mContext = context;
        mDataList = new ArrayList<SearchResult>();
        mDeviceListOnClickLisener = deviceListOnClickLisener;
    }

    public void setDataList(List<SearchResult> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
        Collections.sort(mDataList, this);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int compare(SearchResult lhs, SearchResult rhs) {
        return rhs.rssi - lhs.rssi;
    }

    private static class ViewHolder {
        TextView name;
        TextView mac;
        TextView rssi;
        TextView adv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.device_list_item, null, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.mac = (TextView) convertView.findViewById(R.id.mac);
            holder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            holder.adv = (TextView) convertView.findViewById(R.id.adv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SearchResult result = (SearchResult) getItem(position);

        holder.name.setText(result.getName());
        holder.mac.setText(result.getAddress());
        holder.rssi.setText(String.format("Rssi: %d", result.rssi));

        Beacon beacon = new Beacon(result.scanRecord);
        holder.adv.setText(beacon.toString());

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDeviceListOnClickLisener.onClickItem(result.getAddress(),result.getName());
//                Intent intent = new Intent();
//                intent.setClass(mContext, NewEditActivity.class);
//                intent.putExtra("mac", result.getAddress());
//                mContext.startActivity(intent);
            }
        });

        return convertView;
    }
}
