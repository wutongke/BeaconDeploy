package com.example.scaletest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.lef.DB.BeaconDatabase;
import com.lef.method.YunziCoverage;
import com.lef.scanner.BaseSettings;
import com.lef.scanner.BeaconConnection;
import com.lef.scanner.BeaconConnectionCallback;
import com.lef.scanner.IBeacon;
import com.wxq.draw.DrawDBTool;
import com.wxq.draw.MapControler;

@SuppressLint("NewApi")
public class BeaconDeploy extends Activity implements BeaconConnectionCallback {

	// view界面
	private ImageView smartSetButton;
	private ImageView smartSaveButton;

	private Spinner smartPowerSpinner;
	private Spinner smartFrequencySpinner;

	private TextView buildingInfoView;
	private TextView floorInfoView;
	private TextView coordinateInfoView;
	private TextView yunziParam;
	private TextView coverageInfo;
	private LinearLayout llView;
	private ImageView moreSetButton;
	private ProgressBar waitProgressBar;
	// 用于获取连接的beacon
	private IBeacon contextBeacon;

	private BeaconConnection beaconConnection;
	// 功率列表
	Map<Object, String> yunZiPower = new HashMap<Object, String>();
	ArrayList<BaseSettings.TransmitPower> YunziPowerList = new ArrayList<BaseSettings.TransmitPower>();
	// 广播频率列表
	Map<Object, String> yunZiAdvertisingInterval = new HashMap<Object, String>();
	ArrayList<BaseSettings.AdvertisingInterval> yunZiAdvertisingIntervalList = new ArrayList<BaseSettings.AdvertisingInterval>();
	// 位置信息

	private String BUILDING = null;
	private String FLOOR = null;
	private float FLOOR_X;
	private float FLOOR_Y;

	// 获取建筑信息工具
	DrawDBTool getBuildName = null;

	Handler uiHandler;
	Context mcontext;
	// 连接成功指示
	private boolean isConnected = false;
	/**
	 * handler信息列表 1：保存成功 2. 连接失败 3. 密码为空 4. 两次密码输入不一致 5.失去链接，靠近重连
	 */
	private final static int SAVE_SUCCESS = 1;// 保存成功
	private final static int CONNECTION_FAILURE = 2;// 连接失败
	private final static int CONNECTION_SUCCESS = 19;// 连接失败
	private final static int PASSWORD_EMPTY = 3;// 密码为空
	private final static int PASSWORD_ERROR = 4;// 密码不一致
	private final static int CONNECTION_ERROR = 5;// 失去连接
	private final static int RESETTOFACTORY = 6;// 恢复出厂设置
	private final static int PASSWORD_ERROR2 = 7;// 密码验证出错
	private final static int SAVE_TO_DATABASE_SUCCEED = 8;// 保存到数据库成功
	private final static int SAVE_TO_DATABASE_FAILURE = 9;// 保存到数据库成功
	private final static int SAVE_FAILURE = 10;
	private final static int GETLOCATE_FAILURE = 11;
	private final static int SET_MAJOR_MINOR_SUCCESS = 12;
	private final static int SET_MAJOR_MINOR_FAILURE = 13;
	private final static int SET_UUID_SUCCESS = 14;
	private final static int SET_UUID_FAILURE = 15;
	private final static int SET_BASE_SUCCESS = 16;
	private final static int SET_BASE_FAILURE = 17;
	private final static int GET_FLOOR_ERROR = 18;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_yun_zi_set);
		mcontext = this;
		// 设置名称
		ActionBar mainBar = getActionBar();
		mainBar.setLogo(R.drawable.ic_setting);
		mainBar.setTitle(R.string.title_activity_yunzi_modify);
		// 获取intent中的数据
		Intent configIntent = getIntent();
		contextBeacon = configIntent.getParcelableExtra("beacon");
		FLOOR = configIntent.getStringExtra(MainActivity.FLOORNAME);
		BUILDING = configIntent.getStringExtra(MainActivity.MALLDBPATH);
		FLOOR_X = configIntent.getFloatExtra(MainActivity.X_COORD, -1f);
		FLOOR_Y = configIntent.getFloatExtra(MainActivity.Y_COORD, -1f);

		beaconConnection = new BeaconConnection(this, contextBeacon, this);
		getBuildName = new DrawDBTool(mcontext);

		initFindViewById();
		initOnCLickListener();
		initSpinnerOnCLickListener();
		initMap();
		uiHandler = new Handler() {

			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					Toast.makeText(mcontext, "保存成功", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(mcontext, "连接云子失败，需重新连接", Toast.LENGTH_SHORT)
							.show();
					break;
				case 3:
					Toast.makeText(mcontext, "密码不能为空", Toast.LENGTH_SHORT)
							.show();
					break;
				case 4:
					Toast.makeText(mcontext, "两次密码输入不一致，请重新设置密码",
							Toast.LENGTH_SHORT).show();
					break;
				case 5:
					Toast.makeText(mcontext, "云子失去连接，请重新连接", Toast.LENGTH_SHORT)
							.show();
					break;
				case 6:
					Toast.makeText(mcontext, "恢复出厂设置成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case 7:
					Toast.makeText(mcontext, "密码输入错误，请重新输入", Toast.LENGTH_SHORT)
							.show();
					break;
				case 8:
					Toast.makeText(mcontext, "保存到数据库成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case 9:
					Toast.makeText(mcontext, "保存到数据库失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case 10:
					Toast.makeText(mcontext, "设置失败", Toast.LENGTH_SHORT).show();
					break;
				case 11:
					Toast.makeText(mcontext, "获取位置信息失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case 12:
					Toast.makeText(mcontext, "设置MM成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case 13:
					Toast.makeText(mcontext, "设置MM失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case 14:
					Toast.makeText(mcontext, "设置UUID成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case 15:
					Toast.makeText(mcontext, "设置UUID失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case 16:
					Toast.makeText(mcontext, "设置云子参数成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case 17:
					Toast.makeText(mcontext, "设置云子参数失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case 18:
					Toast.makeText(mcontext, "获取楼层信息失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case CONNECTION_SUCCESS:
					Toast.makeText(mcontext, "连接成功", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}

		};
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isConnected) {
			beaconConnection.connect();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 无需在这里判断是否连接成功，disconnect中会判断，所以只要connection不为空，就应该dis，
		// 否则在未连接成功时则不dis，会出现错误
		if (beaconConnection != null) {
			beaconConnection.disConnect();
		}
		this.finish();
	}

	private void initFindViewById() {
		// TODO Auto-generated method stub
		smartSetButton = (ImageView) findViewById(R.id.smartset);
		smartSaveButton = (ImageView) findViewById(R.id.smartsave);
		smartPowerSpinner = (Spinner) findViewById(R.id.smartpowerset);
		smartFrequencySpinner = (Spinner) findViewById(R.id.smartfrequencyset);
		buildingInfoView = (TextView) findViewById(R.id.buildinginfo);
		floorInfoView = (TextView) findViewById(R.id.floorinfo);
		coordinateInfoView = (TextView) findViewById(R.id.cordinateinfo);
		coverageInfo = (TextView) findViewById(R.id.coverageinfo);
		yunziParam = (TextView) findViewById(R.id.yunziParam);
		waitProgressBar = (ProgressBar) findViewById(R.id.progerssBar);
		llView = (LinearLayout) findViewById(R.id.yunzilayout);
	}

	private Runnable onClickGUIRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (beaconConnection != null) {
				getBuildName.setDBName(BUILDING);
				buildingInfoView.setText(getBuildName.getMallName());
				floorInfoView.setText(FLOOR);
				coverageInfo.setText(getCoverageInfo(contextBeacon));
				coordinateInfoView.setText("横坐标："
						+ (float) (contextBeacon.getMajor()) / 10 + "\n纵坐标： "
						+ (float) (contextBeacon.getMinor()) / 10);
				yunziParam.setText("Beacon功率："
						+ yunZiPower.get(contextBeacon.getBaseSettings()
								.getTransmitPower())
						+ "\nBeacon频率："
						+ yunZiAdvertisingInterval.get(contextBeacon
								.getBaseSettings().getAdvertisingInterval()));
			}
		}
	};
	// 界面显示

	private Runnable updateGUIrunnable = new Runnable() {

		@Override
		public void run() {
			if (isConnected) {
				// TODO Auto-generated method stub
				waitProgressBar.setVisibility(ProgressBar.GONE);
				llView.setVisibility(LinearLayout.VISIBLE);
			}
			if (beaconConnection != null) {
				buildingInfoView.setText("未设置");
				floorInfoView.setText("未设置");
				coordinateInfoView.setText("未设置");
				coverageInfo.setText("未设置");
				yunziParam.setText("未设置");
			}
		}
	};

	private void initMap() {
		// TODO Auto-generated method stub
		yunZiPower.put(BaseSettings.TransmitPower.MIN, "最低（-20db）");
		yunZiPower.put(BaseSettings.TransmitPower.VERYLOW, "很低（-16db）");
		yunZiPower.put(BaseSettings.TransmitPower.LOW, "低（-12db）");
		yunZiPower.put(BaseSettings.TransmitPower.MEDIUM, "中等（-8db）");
		yunZiPower.put(BaseSettings.TransmitPower.HIGH, "高（-4db）");
		yunZiPower.put(BaseSettings.TransmitPower.VERYHIGH, "很高（0db）");
		yunZiPower.put(BaseSettings.TransmitPower.MAX, "最高（+4db）");
		yunZiPower.put(BaseSettings.TransmitPower.UNKNOWN, "未知）");

		YunziPowerList.add(BaseSettings.TransmitPower.MIN);
		YunziPowerList.add(BaseSettings.TransmitPower.VERYLOW);
		YunziPowerList.add(BaseSettings.TransmitPower.LOW);
		YunziPowerList.add(BaseSettings.TransmitPower.MEDIUM);
		YunziPowerList.add(BaseSettings.TransmitPower.HIGH);
		YunziPowerList.add(BaseSettings.TransmitPower.VERYHIGH);
		YunziPowerList.add(BaseSettings.TransmitPower.MAX);
		YunziPowerList.add(BaseSettings.TransmitPower.UNKNOWN);

		yunZiAdvertisingIntervalList.add(BaseSettings.AdvertisingInterval.MIN);
		yunZiAdvertisingIntervalList
				.add(BaseSettings.AdvertisingInterval.VERYLOW);
		yunZiAdvertisingIntervalList.add(BaseSettings.AdvertisingInterval.LOW);
		yunZiAdvertisingIntervalList
				.add(BaseSettings.AdvertisingInterval.MEDIUM);
		yunZiAdvertisingIntervalList.add(BaseSettings.AdvertisingInterval.HIGH);
		yunZiAdvertisingIntervalList
				.add(BaseSettings.AdvertisingInterval.VERYHIGH);
		yunZiAdvertisingIntervalList
				.add(BaseSettings.AdvertisingInterval.UNKNOWN);

		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.MIN,
				"最低（5000ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.VERYLOW,
				"较低（2000ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.LOW,
				"中等（1000ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.MEDIUM,
				"较高（500ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.HIGH,
				"很高（300ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.VERYHIGH,
				"最高（100ms）");
		yunZiAdvertisingInterval.put(BaseSettings.AdvertisingInterval.UNKNOWN,
				"未知");
	}

	private void initOnCLickListener() {
		smartSaveButton
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (contextBeacon != null
								&& beaconConnection.isConnection()) {
							BeaconDatabase bdasebase = new BeaconDatabase(
									mcontext);
							bdasebase.open();
							boolean saveSucceed = false;
							if (FLOOR != null && BUILDING != null) {
								saveSucceed = bdasebase.addBeaconConfig(
										contextBeacon.getBluetoothAddress(),
										contextBeacon.getProximityUuid(),
										contextBeacon.getMajor(),
										contextBeacon.getMinor(),
										BUILDING,
										FLOOR,
										getCoverageDatabaseInfo(contextBeacon),
										yunZiPower.get(contextBeacon
												.getBaseSettings()
												.getTransmitPower()),
										yunZiAdvertisingInterval
												.get(contextBeacon
														.getBaseSettings()
														.getAdvertisingInterval()),
										"", "", "", new Date().getTime());
							} else {
								uiHandler.sendEmptyMessage(GETLOCATE_FAILURE);
							}
							bdasebase.close();
							if (saveSucceed) {
								uiHandler
										.sendEmptyMessage(SAVE_TO_DATABASE_SUCCEED);
							} else {
								uiHandler
										.sendEmptyMessage(SAVE_TO_DATABASE_FAILURE);
							}
						} else {
							uiHandler.sendEmptyMessage(CONNECTION_ERROR);
						}
					}
				});
		smartSetButton
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						beaconConnection.setUUID(contextBeacon
								.getProximityUuid().substring(0, 19)
								+ getFloorString(FLOOR)
								+ BUILDING.substring(23));
						// 休眠100ms后设置防止命令发送失败
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						beaconConnection.setMajorMinor((int) (FLOOR_X * 10),
								(int) (FLOOR_Y * 10));
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						beaconConnection
								.setAdvertisingInterval(BaseSettings.AdvertisingInterval.VERYLOW);
					}
				});
	}

	// spinner监听
	private void initSpinnerOnCLickListener() {
		smartPowerSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position != 0) {
							// TODO Auto-generated method stub
							beaconConnection.setTransmitPower(YunziPowerList
									.get(position - 1));
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});

		smartFrequencySpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position != 0) {
							// TODO Auto-generated method stub
							beaconConnection
									.setAdvertisingInterval(yunZiAdvertisingIntervalList
											.get(position - 1));
						} else if (position == 0) {

						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
	}

	// 存储数据库覆盖半径
	protected String getCoverageDatabaseInfo(IBeacon beacon) {
		// TODO Auto-generated method stub

		if (beacon.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(0).toString())) {
			return YunziCoverage.coverage.Mi_03.toString();
		} else if (beacon.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(1).toString())) {
			return YunziCoverage.coverage.Mi_20.toString();
		} else if (beacon.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(2).toString())) {
			return YunziCoverage.coverage.Mi_30.toString();
		} else if (beacon.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(3).toString())) {
			return YunziCoverage.coverage.Mi_60.toString();
		} else if (beacon.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(4).toString())) {
			return YunziCoverage.coverage.Mi_60BIG.toString();
		} else {
			return YunziCoverage.coverage.Mi_00UNKNOW.toString();
		}
	}

	// 获取云子覆盖半径
	protected String getCoverageInfo(IBeacon contextBeacon2) {
		// TODO Auto-generated method stub

		if (contextBeacon2.getBaseSettings().getTransmitPower().toString()
				.equals(YunziPowerList.get(0).toString())) {
			return "3米左右";
		} else if (contextBeacon2.getBaseSettings().getTransmitPower()
				.toString().equals(YunziPowerList.get(1).toString())) {
			return "20米左右";
		} else if (contextBeacon2.getBaseSettings().getTransmitPower()
				.toString().equals(YunziPowerList.get(2).toString())) {
			return "30米左右";
		} else if (contextBeacon2.getBaseSettings().getTransmitPower()
				.toString().equals(YunziPowerList.get(3).toString())) {
			return "60米左右";
		} else if (contextBeacon2.getBaseSettings().getTransmitPower()
				.toString().equals(YunziPowerList.get(4).toString())) {
			return "超过60米";
		} else {
			return "未知";
		}
	}

	/**
	 * 存储楼层信息，保存在UUID的
	 * 
	 * @param Floor
	 * @return
	 */
	private String getFloorString(String Floor) {
		if (!Floor.equals("") && Floor.charAt(5) != 'B') {
			if (Integer.valueOf(Floor.substring(5)) < 10) {
				return "000" + Floor.substring(5);
			} else if (Integer.valueOf(Floor.substring(5)) < 100) {
				return "00" + Floor.substring(5);
			} else if (Integer.valueOf(Floor.substring(5)) < 1000) {
				return "0" + Floor.substring(5);
			} else {
				return "0000";
			}
		} else if (!Floor.equals("") && Floor.charAt(5) == 'B') {
			if (Integer.valueOf(Floor.substring(6)) < 10) {
				return "100" + Floor.substring(6);
			} else if (Integer.valueOf(Floor.substring(6)) < 100) {
				return "10" + Floor.substring(6);
			} else if (Integer.valueOf(Floor.substring(6)) < 1000) {
				return "1" + Floor.substring(6);
			} else {
				return "0000";
			}
		} else {
			uiHandler.sendEmptyMessage(GET_FLOOR_ERROR);
			return "0000";
		}
	}

	@Override
	public void onConnectedState(IBeacon arg0, int arg1) {
		// TODO Auto-generated method stub
		switch (arg1) {
		case BeaconConnection.CONNECTED:
			if (!isConnected) {
				contextBeacon = arg0;
				uiHandler.sendEmptyMessage(CONNECTION_SUCCESS);
				isConnected = true;
				uiHandler.post(updateGUIrunnable);
			}
			break;
		case BeaconConnection.DISCONNECTED:
			uiHandler.sendEmptyMessage(CONNECTION_FAILURE);
			break;
		case BeaconConnection.CONNECTING:
		case BeaconConnection.DISCONNECTTING:
		default:
			break;
		}
	}

	@Override
	public void onSetBaseSetting(IBeacon arg0, int arg1) {
		// TODO Auto-generated method stub
		contextBeacon = arg0;
		switch (arg1) {
		case BeaconConnection.SUCCESS:
			uiHandler.sendEmptyMessage(SET_BASE_SUCCESS);
			uiHandler.post(onClickGUIRunnable);
			break;
		case BeaconConnection.FAILURE:
			uiHandler.sendEmptyMessage(SET_BASE_FAILURE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSetCalRssi(IBeacon arg0, int arg1) {
		// TODO Auto-generated method stub
		contextBeacon = arg0;
	}

	@Override
	public void onSetMajoMinor(IBeacon arg0, int arg1) {
		// TODO Auto-generated method stub
		contextBeacon = arg0;
		switch (arg1) {
		case BeaconConnection.SUCCESS:
			uiHandler.post(onClickGUIRunnable);
			uiHandler.sendEmptyMessage(SET_MAJOR_MINOR_SUCCESS);
			Log.v("sssssssssssssss", "major设置");
			break;
		case BeaconConnection.FAILURE:
			uiHandler.sendEmptyMessage(SET_MAJOR_MINOR_FAILURE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSetProximityUUID(IBeacon arg0, int arg1) {
		// TODO Auto-generated method stub
		contextBeacon = arg0;
		switch (arg1) {
		case BeaconConnection.SUCCESS:
			uiHandler.sendEmptyMessage(SET_UUID_SUCCESS);
			Log.v("sssssssssssssss", "uuid设置");
			uiHandler.post(onClickGUIRunnable);
			break;
		case BeaconConnection.FAILURE:
			uiHandler.sendEmptyMessage(SET_UUID_FAILURE);
			break;
		default:
			break;
		}
	}

}
