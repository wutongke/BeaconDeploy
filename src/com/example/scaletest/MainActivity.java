package com.example.scaletest;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sortListView.SortListActivity;
import sortListView.SortModel;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lef.DB.BeaconDatabase;
import com.lef.method.YunZiShow;
import com.lxr.overflot.OverlayIcon;
import com.lxr.overflot.OverlayIcon.IClickListener;
import com.lxr.overflot.OverlayLayout;
import com.lxr.overflot.OverlayLayout.ILoadingLayoutListener;
import com.wxq.draw.DrawConstants;
import com.wxq.draw.DrawDBTool;
import com.wxq.draw.MapControler;
import com.wxq.draw.MapControler.IMapClickListener;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	public enum RequestCode {
		SiteList, BarCode_Locate, Collect
	}

	// 位置点信息
	public final static String FLOORNAME = "floorName";
	public final static String SPOTID = "spotId";
	public final static String MALLDBPATH = "MallDBpath";
	public final static String X_COORD = "X_Coord";
	public final static String Y_COORD = "Y_Coord";

	protected String MallDBpath;
	protected MapControler mapLayout;
	protected String floorName, mallName = "";
	private OverlayLayout handlerYunzi;
	private OverlayLayout overlaylayout;
	private int handlerYunziId;
	protected Button facilityButton;// 公共设施按钮
	protected Button searchButton;// 查找按钮
	protected Button clearButton;// 清空按钮
	protected Button beaconshowButton;// 显示按钮

	protected ListView facilityListView;
	protected List<Map<String, Object>> wclist;
	SimpleAdapter facilityadapter;// 公共设施列表数据适配器

	OverlayIcon overlaypointer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		mapLayout = new MapControler(this, null);
		layout.addView(mapLayout);
		overlaypointer = new OverlayIcon(mapLayout, R.drawable.loc_pointer,
				0.8f, true, -1);
//		overlaylayout = new OverlayLayout(maplayoutt, layout, id)
		overlaylayout = new OverlayLayout(mapLayout,
				R.layout.navigation_overlay_modify,2);
		initView();
		setAllButton();
	}

	@SuppressLint("NewApi")
	private void initView() {
		facilityButton = (Button) findViewById(R.id.wc);
		searchButton = (Button) findViewById(R.id.menu);
		clearButton = (Button) findViewById(R.id.removeall);
		beaconshowButton = (Button) findViewById(R.id.beaconshow);

		// mapLayout = (MapControler) findViewById(R.id.mapLayout);
		handlerYunzi = new OverlayLayout(mapLayout,
				R.layout.navigation_overlay_deletebeacon, 1);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().penaltyLog()
				.penaltyDeath().build());
	}

	private void setAllButton() {
		// 添加公共设施列表
		addFacilitylist();
		//图层
		/**
		 * 设置浮层加载时的响应事件.结构设置很奇葩
		 */
		overlaylayout.setOnLoadingListener(new ILoadingLayoutListener(){

			@Override
			public void loading(PointF arg0) {
				// TODO Auto-generated method stub
				TextView titleView = (TextView) overlaylayout.layoutView
						.findViewById(R.id.locationInfo);
				Button btn = (Button) overlaylayout.layoutView
						.findViewById(R.id.gotoDetailMap);

				String spotName = mapLayout.getSpotInfoAtMapCoord(overlaylayout.MapCoord);

				String  currentSpotId = mapLayout.getCurrentSpotID();
				DrawDBTool dbTool = new DrawDBTool(MainActivity.this);
				dbTool.setDBName(MallDBpath);
				String ff = dbTool.getunityid(currentSpotId);

				File dbFile = new File(android.os.Environment
						.getExternalStorageDirectory()
						+ DrawConstants.DBFolder
						+ ff + ".db");
				if (!dbFile.exists()) {
					btn.setVisibility(View.GONE);
				} else {
					btn.setVisibility(View.VISIBLE);
				}
				if (!TextUtils.isEmpty(spotName)) {
					titleView.setText(spotName);
				} else {
					titleView.setText("未定义地点");
				}
				addAddSpotButton();
//				addSpotDetailButton();
			}
			
		});
//		overlaylayout.setLoading(new OverlayLayout.LoadingLayout() {
//			@Override
//			public void loading() {
//			}
//			@Override
//			public void loading(PointF pF) {
//				TextView titleView = (TextView) overlaylayout.layoutView
//						.findViewById(R.id.locationInfo);
//				
//				Button btn = (Button) overlaylayout.layoutView
//						.findViewById(R.id.gotoDetailMap);
//
//				spotName = getSpotInfoAtMapCoord(overlaylayout.MapCoord);
//
//				currentSpotId = getCurrentSpotID();
//				DrawDBTool dbTool = new DrawDBTool(mycontext);
//				dbTool.setDBpath(dbfile);
//				String ff = dbTool.getunityid(currentSpotId);
//
//				File dbFile = new File(android.os.Environment
//						.getExternalStorageDirectory()
//						+ DrawConstants.DBFolder
//						+ ff + ".db");
//				if (!dbFile.exists()) {
//					btn.setVisibility(View.GONE);
//				} else {
//					btn.setVisibility(View.VISIBLE);
//				}
//				if (!TextUtils.isEmpty(spotName)) {
//					titleView.setText(spotName);
//				} else {
//					titleView.setText("未定义地点");
//				}
//				addAddSpotButton();
////				addSpotDetailButton();
//			}
//
//		});
		// 点击地图显示事件
		mapLayout.setMapclicklistener(new IMapClickListener() {

			@Override
			public void mapClicked(float arg0, float arg1) {
				// TODO Auto-generated method stub
				overlaypointer.pinAtMapWithScreenCoord(new PointF(arg0, arg1));
				overlaylayout.pinAtMapWithScreenCoord(new PointF(arg0, arg1));
			}

			@Override
			public void mapChanged() {
				// TODO Auto-generated method stub
				// String newdb=mapLayout.getDbfile();
				// if(MallDBpath!=null && MallDBpath.equals(newdb))
				// return ;
				// MallDBpath=newdb;
				// DrawDBTool dbTool = new DrawDBTool(MainActivity.this);
				// dbTool.setDBName(MallDBpath);
				// mallName = dbTool.getMallName();
				// setTitle(mallName);
			}
		});
		// TODO Auto-generated method stub
		handlerYunzi
				.setOnLoadingListener(new OverlayLayout.ILoadingLayoutListener() {

					@Override
					public void loading(PointF arg0) {
						// TODO Auto-generated method stub

						Button updatebtn = (Button) handlerYunzi.layoutView
								.findViewById(R.id.update_beacon_deploy);
						Button deletebtn = (Button) handlerYunzi.layoutView
								.findViewById(R.id.delete_beacon_deploy);

						updatebtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent newIntent = new Intent();
								newIntent.setClass(MainActivity.this,
										ListActivity.class);
								newIntent.putExtra("floorName",
										mapLayout.getFloor());
								newIntent.putExtra("spotId",
										mapLayout.getCurrentSpotID());
								newIntent.putExtra("MallDBpath",
										mapLayout.getDbfile());
								newIntent.putExtra("X_Coord",
										overlaypointer.getMapCoord().x);
								newIntent.putExtra("Y_Coord",
										overlaypointer.getMapCoord().y);
								MainActivity.this.startActivity(newIntent);
							}
						});
						deletebtn.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								new AlertDialog.Builder(MainActivity.this)
										.setIcon(R.drawable.title_bar_menu)
										.setTitle("确定删除部署的云子吗")
										.setPositiveButton(
												"确定",
												new android.content.DialogInterface.OnClickListener() {

													@SuppressLint("ShowToast")
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														// TODO Auto-generated
														// method
														// stub
														BeaconDatabase db = new BeaconDatabase(
																MainActivity.this);
														db.open();
														boolean deleteSucceed = db
																.deleteById(handlerYunziId);
														db.close();
														if (deleteSucceed) {
															Toast.makeText(
																	MainActivity.this,
																	"删除成功",
																	1000)
																	.show();
															clearAll();
															showDeployYunZi();

														} else {
															Toast.makeText(
																	MainActivity.this,
																	"删除出错",
																	1000)
																	.show();
														}
													}
												})
										.setNegativeButton("取消", null).create()
										.show();
							}
						});

					}
				});

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SortListActivity.class);

				intent.putExtra("dataList", (Serializable) getListData());

				startActivityForResult(intent, RequestCode.SiteList.ordinal());
			}
		});

		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearAll();
			}
		});

		facilityButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (facilityListView.getVisibility() == View.VISIBLE) {
					facilityListView.setVisibility(View.INVISIBLE);
				} else {
					changeFacilitylist();
					facilityListView.setVisibility(View.VISIBLE);
				}
			}
		});
		beaconshowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeployYunZi();
			}
		});

	}

	protected void showDeployYunZi() {
		// TODO Auto-generated method stub
		ArrayList<YunZiShow> beaconPointList = new ArrayList<YunZiShow>();
		BeaconDatabase db = new BeaconDatabase(MainActivity.this);
		db.open();
		beaconPointList = db.getBeaconLocate(mapLayout.getDbfile(),
				mapLayout.getFloor());
		db.close();
		if (beaconPointList.size() > 0) {
			Paint paintEmpty = new Paint();
			paintEmpty.setStyle(Style.FILL);
			paintEmpty.setColor(Color.BLUE);
			paintEmpty.setAlpha(20);

			Paint paintCricle = new Paint();
			paintCricle.setStyle(Style.STROKE);
			paintCricle.setColor(Color.BLUE);

			for (int i = 0; i < beaconPointList.size(); i++) {
				if (beaconPointList.get(i) != null) {
					final PointF currentPointF = new PointF(beaconPointList
							.get(i).getLocate().x, beaconPointList.get(i)
							.getLocate().y);
					mapLayout.getFloatView()
							.addCircle(String.valueOf(i),
									beaconPointList.get(i).getLocate().x,
									beaconPointList.get(i).getLocate().y,
									beaconPointList.get(i).getCoverageInt(),
									paintEmpty);
					mapLayout.getFloatView().addCircle(String.valueOf(i),
							beaconPointList.get(i).getLocate().x,
							beaconPointList.get(i).getLocate().y,
							beaconPointList.get(i).getCoverageInt(),
							paintCricle);
					OverlayIcon ico = new OverlayIcon(mapLayout,
							R.drawable.da_marker_red, 0.8f, false, i);
					ico.setOnclickListener(new IClickListener() {

						@Override
						public void onClick(int id) {
							// TODO Auto-generated method stub
							handlerYunziId = id;
							handlerYunzi.pinAtMapWithMapCoord(currentPointF);
							new OverlayIcon(mapLayout, R.drawable.loc_pointer,
									false, id)
									.pinAtMapWithMapCoord(currentPointF);
						}
					});
					ico.pinAtMapWithMapCoord(currentPointF);
				}
			}

		} else {
			Toast.makeText(MainActivity.this, "该地没有beacon", Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 不显示各种图标
	 */
	protected void clearAll() {
		// TODO Auto-generated method stub
		// 清除云子显示
		mapLayout.getFloatView().clear();
		// from1 = false; // 消除起点终点标记
		// goto1 = false;
		// queryRoute = false;
		mapLayout.hideAll();
		handlerYunzi.hideOverlay();
		// mapLayout.routeView.hideRoute();
		// mapLayout.routeView.clearRoute();
		facilityListView.setVisibility(View.INVISIBLE);
	}

	private void addFacilitylist() {
		facilityListView = (ListView) findViewById(R.id.facilityListView);
		facilityListView.setVisibility(View.INVISIBLE);
		changeFacilitylist();// 获得适配器数据
		facilityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DrawDBTool dbTool;
				dbTool = new DrawDBTool(MainActivity.this);
				dbTool.setDBName(MallDBpath);
				List<Map<String, Object>> mywcList = dbTool.getwcNUM(floorName,
						wclist.get(position).get("Type").toString());
				mapLayout.showPopuArray(mywcList, "Coord_X", "Coord_Y",
						R.drawable.da_marker_red);
				// ((Object) mapLayout).showPopuArray(mywcList,
				// R.drawable.da_marker_red);
				facilityListView.setVisibility(View.INVISIBLE);
				dbTool.close();
			}
		});
	}

	private void changeFacilitylist() {
		getDataList();
		facilityadapter = new SimpleAdapter(MainActivity.this, wclist,
				R.layout.wc_list_item, new String[] { "Type", "num" },
				new int[] { R.id.Type, R.id.num });
		facilityListView.setAdapter(facilityadapter);
		facilityadapter.notifyDataSetChanged();
	}

	public List<SortModel> getListData() {
		List<SortModel> ld = new ArrayList<SortModel>();
		DrawDBTool dbTool = new DrawDBTool(MainActivity.this);
		dbTool.setDBName(MallDBpath);
		List<Map<String, Object>> nameList = dbTool.getSiteNames(mapLayout
				.getFloor());
		for (Map<String, Object> map : nameList) {
			SortModel sortModel = new SortModel();
			sortModel.setName(map.get("CompanyName") + "");
			sortModel.setExtraInfo(map.get("Coord_X") + ","
					+ map.get("Coord_Y"));
			ld.add(sortModel);
		}
		return ld;
	}

	/**
	 * 获取公共设施列表
	 */
	private void getDataList() {
		DrawDBTool dbTool = new DrawDBTool(MainActivity.this);
		dbTool.setDBName(MallDBpath);
		wclist = dbTool.getwclist(MallDBpath, mapLayout.getFloor());
		dbTool.close();
	}
	private void addAddSpotButton() {
		Button btn = (Button) overlaylayout.layoutView
				.findViewById(R.id.btn_beacondeploy);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent newIntent = new Intent();
				newIntent.setClass(MainActivity.this, ListActivity.class);
				newIntent.putExtra("floorName", mapLayout.getFloor());
				newIntent.putExtra("spotId", mapLayout.getCurrentSpotID());
				newIntent.putExtra("MallDBpath", mapLayout.getDbfile());
				newIntent.putExtra("X_Coord", overlaylayout.MapCoord.x);
				newIntent.putExtra("Y_Coord", overlaylayout.MapCoord.y);
				MainActivity.this.startActivity(newIntent);
			}
		});
	}
}
