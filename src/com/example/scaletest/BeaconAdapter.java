package com.example.scaletest;

import java.util.ArrayList;
import java.util.List;

import com.lef.scanner.IBeacon;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.scaletest.R;

public class BeaconAdapter extends BaseAdapter{
	private List<IBeacon> beaconList;
	private LayoutInflater layoutInflater = null;
    private ViewHolder holder = null;
	public BeaconAdapter(Context context,List<IBeacon> beaconList){
		this.beaconList = beaconList;
		layoutInflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return beaconList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return beaconList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.beaconadapter, parent, false);
            holder = new ViewHolder();
            holder.majorTextView = (TextView) convertView.findViewById(R.id.major);
            holder.minorTextView = (TextView) convertView.findViewById(R.id.minor);
            holder.rssiImageView = (ImageView) convertView.findViewById(R.id.rssi);
            holder.IDTextView = (TextView) convertView.findViewById(R.id.ID);
            holder.distanceTextView = (TextView)convertView.findViewById(R.id.distance);
            holder.canBeConnectedView = (ImageView) convertView.findViewById(R.id.canbeconnected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
		if (beaconList.size()>0) {
			holder.majorTextView.setText(String.valueOf(beaconList.get(position).getMajor()));
			holder.minorTextView.setText(String.valueOf(beaconList.get(position).getMinor()));
			holder.IDTextView.setText(beaconList.get(position)
					.getBluetoothAddress());
			holder.distanceTextView.setText(getDistance(beaconList.get(position)));
			holder.rssiImageView.setBackgroundResource(getRSSIView(beaconList
					.get(position)));
			holder.canBeConnectedView
					.setBackgroundResource(getConnectionView(beaconList
							.get(position)));
		}
		return convertView;
	}
	private int getRSSIView(IBeacon beacon) {
		if (beacon.getRssi() <=-110) {
			return R.drawable.icon_rssi1;
		} else if (beacon.getRssi() <= -100) {
			return R.drawable.icon_rssi2;
		} else if (beacon.getRssi() <= -90) {
			return R.drawable.icon_rssi3;
		} else if (beacon.getRssi() <= -80) {
			return R.drawable.icon_rssi4;
		} else if (beacon.getRssi() <= -70) {
			return R.drawable.icon_rssi5;
		} else if (beacon.getRssi() > -70) {
			return R.drawable.icon_rssi6;
		}
		return R.drawable.icon_rssi1;
	}
	private int getConnectionView(IBeacon beacon){
		if (beacon.isCanBeConnected()){
			return R.drawable.conn;
		}else{
			return R.drawable.disconn;
		}
	}
	private String getDistance(IBeacon beacon){
		if(String.valueOf(beacon.getAccuracy()).length()>=7){
			return String.valueOf(beacon.getAccuracy()).substring(0, 7);
		}else{
			return String.valueOf(beacon.getAccuracy());
		}
	}
	class ViewHolder {
        private TextView majorTextView = null;
        private TextView minorTextView = null;
        private ImageView rssiImageView = null;
        private TextView distanceTextView = null;
        private TextView IDTextView = null;
        private ImageView canBeConnectedView = null;
    }
}
