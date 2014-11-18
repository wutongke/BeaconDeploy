package com.example.scaletest;

import java.util.ArrayList;
import java.util.Collection;

import com.lef.scanner.BeaconConnection;
import com.lef.scanner.IBeacon;
import com.lef.scanner.IBeaconData;
import com.lef.scanner.IBeaconManager;
import com.lef.scanner.MonitorNotifier;
import com.lef.scanner.RangeNotifier;
import com.lef.scanner.Region;
import com.wxq.draw.MapControler;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class ListActivity extends Activity implements
		com.lef.scanner.IBeaconConsumer {
	// 启动蓝牙请求码
	protected static final int BLUTETOOTH = 1;
	private IBeaconManager iBeaconManager;
	private ListView beaconListView;
	/**
	 * 数据更新
	 */
	private ArrayList<IBeacon> beaconDataListA = new ArrayList<IBeacon>();
	/**
	 * UI数据
	 */
	private ArrayList<IBeacon> beaconDataListB = new ArrayList<IBeacon>();

	// 云子数据Adapter
	private BeaconAdapter beaconListAdapter;
	// 数据常量
	private static final int UPDATEUI = 1;
	private static final int PROGRESSBARGONE = 2;
	private static final int CLICKTOAST = 3;
	//
	Handler UIHandler = new Handler();
	// 开始扫描时使用ProgressBar
	private ProgressBar progressScan;
	private TextView progressScanTextView;
	private boolean ProgressBarVisibile = true;


	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATEUI:
				beaconDataListB.clear();
				beaconDataListB.addAll(beaconDataListA);
				beaconListAdapter.notifyDataSetChanged();
				break;
			case PROGRESSBARGONE:
				progressScan.setVisibility(TextView.GONE);
				progressScanTextView.setVisibility(TextView.GONE);
				ProgressBarVisibile = false;
				break;
			case CLICKTOAST:
				Toast.makeText(ListActivity.this, "请设置beacon部署模式",
						Toast.LENGTH_SHORT).show();
			default:
				break;
			}
		};
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beaconlist);
		// 设置名称
		ActionBar mainBar = getActionBar();
		mainBar.setLogo(R.drawable.ic_list);
		mainBar.setTitle(R.string.ibeacon_list);


		iBeaconManager = IBeaconManager.getInstanceForApplication(this);

		// 获取View
		beaconListView = (ListView) findViewById(R.id.yunzilist);
		progressScan = (ProgressBar) findViewById(R.id.progressScan);
		progressScanTextView = (TextView) findViewById(R.id.progressScantext);

		beaconListAdapter = new BeaconAdapter(this, beaconDataListB);
		beaconListView.setAdapter(beaconListAdapter);
		
		
		
		beaconListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent preintent = getIntent();
				// TODO Auto-generated method stub
				if (beaconDataListB.get(position).isCanBeConnected()) {
					// 连接之前先设置密码
					BeaconConnection.setPASSWORD(ListActivity.this
							.getResources().getString(R.string.beaconPassword));
					Intent mintent = new Intent(ListActivity.this,
							BeaconDeploy.class);
					// 需要把IBeacon类型封装成IBeacon，以方便在intent传输
					mintent.putExtra("beacon",
							new IBeaconData(beaconDataListB.get(position)));
					mintent.putExtra(MainActivity.MALLDBPATH, preintent.getStringExtra(MainActivity.MALLDBPATH));
					mintent.putExtra(MainActivity.FLOORNAME, preintent.getStringExtra(MainActivity.FLOORNAME));
					mintent.putExtra(MainActivity.X_COORD, preintent.getFloatExtra(MainActivity.X_COORD, -1f));
					mintent.putExtra(MainActivity.Y_COORD, preintent.getFloatExtra(MainActivity.Y_COORD,-1f));
					startActivity(mintent);
					if (iBeaconManager != null
							&& iBeaconManager.isBound(ListActivity.this)) {
						iBeaconManager.unBind(ListActivity.this);
					}
				} else {
					handler.sendEmptyMessage(CLICKTOAST);
				}
				ListActivity.this.finish();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (iBeaconManager != null && !iBeaconManager.isBound(this)) {
			if (beaconDataListA.size() > 0) {
				beaconDataListA.clear();
			}
			// 蓝牙dialog
			initBluetooth();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// if (iBeaconManager.isBound(this)) {
		// iBeaconManager.unBind(this);
		// }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (iBeaconManager != null && iBeaconManager.isBound(this)) {
			iBeaconManager.unBind(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 请求打开蓝牙，如果不打开，将退出程序
	 */
	private void initBluetooth() {
		// TODO Auto-generated method stub
		final BluetoothAdapter blueToothEable = BluetoothAdapter
				.getDefaultAdapter();
		if (!blueToothEable.isEnabled()) {
			new AlertDialog.Builder(ListActivity.this)
					.setIcon(R.drawable.title_bar_menu).setTitle("蓝牙开启")
					.setMessage("云子配置需要开启蓝牙").setCancelable(false)
					.setPositiveButton("开启", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							blueToothEable.enable();
							iBeaconManager.bind(ListActivity.this);
						}
					}).setNegativeButton("退出", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							ListActivity.this.finish();
						}
					}).create().show();
		} else {
			iBeaconManager.setForegroundScanPeriod(800);
			iBeaconManager.bind(this);
		}
	}


	@Override
	public void onIBeaconServiceConnect() {
		// TODO Auto-generated method stub
		// 启动Range服务
		iBeaconManager.setRangeNotifier(new RangeNotifier() {

			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				if (ProgressBarVisibile) {
					handler.sendEmptyMessage(PROGRESSBARGONE);
				}
				// java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				// while (iterator.hasNext()) {
				// IBeacon temp = iterator.next();
				// if (beaconDataListA.contains(temp)) {
				// beaconDataListA.set(beaconDataListA.indexOf(temp), temp);
				// handler.sendEmptyMessage(UPDATEUI);
				// } else {
				// beaconDataListA.add(temp);
				// handler.sendEmptyMessage(UPDATEUI);
				// }
				//
				// }

			}

			@Override
			public void onNewBeacons(Collection<IBeacon> iBeacons, Region region) {
				// TODO Auto-generated method stub
				// beaconDataListA.addAll(iBeacons);
				// handler.sendEmptyMessage(UPDATEUI);
				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				while (iterator.hasNext()) {
					IBeacon temp = iterator.next();
					if (!beaconDataListA.contains(temp)) {
						beaconDataListA.add(temp);
					}
					handler.sendEmptyMessage(UPDATEUI);
				}
			}

			@Override
			public void onGoneBeacons(Collection<IBeacon> iBeacons,
					Region region) {
				// TODO Auto-generated method stub
				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				while (iterator.hasNext()) {
					IBeacon temp = iterator.next();
					if (beaconDataListA.contains(temp)) {
						beaconDataListA.remove(temp);
					}
					handler.sendEmptyMessage(UPDATEUI);
				}
			}

			@Override
			public void onUpdateBeacon(Collection<IBeacon> iBeacons,
					Region region) {
				// TODO Auto-generated method stub
				java.util.Iterator<IBeacon> iterator = iBeacons.iterator();
				while (iterator.hasNext()) {
					IBeacon temp = iterator.next();
					if (beaconDataListA.contains(temp)) {
						beaconDataListA.set(beaconDataListA.indexOf(temp), temp);
					}
					handler.sendEmptyMessage(UPDATEUI);
				}
			}

		});
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {

			@Override
			public void didExitRegion(Region region) {
				// TODO Auto-generated method stub
			}

			@Override
			public void didEnterRegion(Region region) {
				// TODO Auto-generated method stub

			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				// TODO Auto-generated method stub

			}
		});
		try {
			Region myRegion = new Region("myRangingUniqueId", null, null, null);
			iBeaconManager.startRangingBeaconsInRegion(myRegion);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
