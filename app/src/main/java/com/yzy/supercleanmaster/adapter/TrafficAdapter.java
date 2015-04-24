package com.yzy.supercleanmaster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yzy.supercleanmaster.R;
import com.yzy.supercleanmaster.model.TrafficInfo;
import com.yzy.supercleanmaster.utils.TrafficUtils;

import java.util.List;

/**
 * Created by apple on 15/4/3.
 */
public class TrafficAdapter extends BaseAdapter {
    public List<TrafficInfo> mTrafficInfo;
    LayoutInflater infater = null;
    private Context mContext;

    public TrafficAdapter(Context context, List<TrafficInfo> flowinfos) {
        infater = LayoutInflater.from(context);
        mContext = context;
        this.mTrafficInfo = flowinfos;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mTrafficInfo.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mTrafficInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null)
        {
            convertView = infater.inflate(R.layout.listview_traffic_manage, null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.gprsUpload = (TextView) convertView.findViewById(R.id.gprs_upload);
            holder.gprDown = (TextView) convertView.findViewById(R.id.gprs_down);
            holder.wifiUpload = (TextView) convertView.findViewById(R.id.wifi_upload);
            holder.wifiDown = (TextView) convertView.findViewById(R.id.wifi_down);
            holder.gprsChoice = (RadioButton) convertView.findViewById(R.id.gprs_choice);
            holder.wifiChoice = (RadioButton) convertView.findViewById(R.id.wifi_choice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final TrafficInfo trafficInfo = (TrafficInfo) getItem(position);
        holder.appName.setText(trafficInfo.getAppName());
        holder.appIcon.setImageDrawable(trafficInfo.getAppIcon());
        holder.gprsUpload.setText(TrafficUtils.refreshTraffic(trafficInfo.getMobileRx()));
        holder.gprDown.setText(TrafficUtils.refreshTraffic(trafficInfo.getMobileTx()));
        holder.wifiUpload.setText(TrafficUtils.refreshTraffic(trafficInfo.getWifiRx()));
        holder.wifiDown.setText(TrafficUtils.refreshTraffic(trafficInfo.getWifiTx()));
        holder.gprsChoice.setChecked(true);
        holder.wifiChoice.setChecked(true);
        holder.gprsChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(false);
                notifyDataSetChanged();
            }
        });
        holder.wifiChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(false);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView gprsUpload;
        TextView gprDown;
        TextView wifiUpload;
        TextView wifiDown;
        RadioButton gprsChoice;
        RadioButton wifiChoice;
    }

}