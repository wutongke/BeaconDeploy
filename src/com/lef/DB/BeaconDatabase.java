package com.lef.DB;

import java.util.ArrayList;
import java.util.HashMap;

import com.lef.method.YunZiShow;
import com.lef.scanner.IBeacon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.util.Log;

public class BeaconDatabase {

	private static final String TAG = BeaconDatabase.class.getSimpleName();
	Object lockObject = new Object(); // 正常的增删改查的数据库操作是否在运行的锁

	// 定义数据库名
	private static final String DB_NAME = "BEACONDEPOY902.db";
	private static final int DB_VERSION = 1;
	// databaseHelp类
	private DatabaseHelper databaseHelper = null;
	private Context context = null;

	public SQLiteDatabase sqliteDatabase = null;

	// 表名
	public final static String BEACON_TABLE = "beacon_table";
	// column
	public static final String _ID = "_id";// 自然增长的序号
	public static final String ID = "id"; // beacon mac 可用来唯一标识beacon
	public static final String UUID = "uuid";// UUID
	public static final String MAJOR = "major";// beacon 横坐标
	public static final String MINOR = "minor";// beacon 纵坐标
	public static final String  BUILDING= "building";// 建筑信息
	public static final String FLOOR = "floor";	//楼层信息
	public static final String COVERAGE = "coverage";
	private static final String UPDATE_TIME = "update_time";//更新时间
	private static final String POWER ="power";//beacon功率
	private static final String FREQUENCY ="frequency";//beacon广播频率
	private static final String TEMPERATUREFREQUENCY ="temperaturefrequency";//温度采样频率
	private static final String LIGHTFREQUENCY ="lightfrequency";//光照采样频率
	private static final String ACCELERATE = "accelerate";//加速度灵敏度
	

	
	// 创建表
	private static final String createBeaconTable = "Create  TABLE [beacon_table] ("
			+ "[_id] integer PRIMARY KEY,"
			+ "[id] varchar(40) UNIQUE NOT NULL,"
			+ "[uuid] varchar(40) NOT NULL," 
			+ "[major] int NOT NULL," 
			+ "[minor] int NOT NULL," 
			+ "[building] varchar(40) NOT NULL," 
			+ "[floor] varchar(40) NOT NULL," 
			+ "[coverage] varchar(40) NOT NULL," 
			+ "[power] varchar(20) NOT NULL," 
			+ "[frequency] varchar(20) NOT NULL," 
			+ "[temperaturefrequency] varchar(20), " 
			+ "[lightfrequency] varchar(20)," 
			+ "[accelerate] varchar(20)," 
			+ "[update_time] bigint" 
			+ ");";
	
	/**
	 * 利用重写的openOrCreateDatabase方法，从SD卡读取数据
	 * 
	 * @author lief
	 * @param context
	 */
	public BeaconDatabase(Context context) {
		DatabaseContext dbContext = new DatabaseContext(context);
		this.context = dbContext;
	}

	// 打开数据库，返回数据库对象
	public void open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getReadableDatabase();
	}

	// 关闭数据库
	public void close() {
		databaseHelper.close();
	}
	//
	/*public ArrayList<PointF> getBeaconLocate(String Building,String Floor){
		Cursor cursor = sqliteDatabase.query(true, BEACON_TABLE, new String[]{MAJOR,MINOR}, 
				BUILDING+"=?"+" and "+FLOOR+"=?", new String[]{Building,Floor},null, null, null, null);
		
		ArrayList<PointF> beaconInfo = new ArrayList<PointF>(); 
		while(cursor.moveToNext()){
			beaconInfo.add(new PointF(Integer.valueOf(cursor.getString(0))/10,Integer.valueOf(cursor.getString(1))/10));
		}
		return beaconInfo;
	}*/
	
	public ArrayList<YunZiShow> getBeaconLocate(String Building,String Floor){
		Cursor cursor = sqliteDatabase.query(true, BEACON_TABLE, new String[]{_ID,MAJOR,MINOR,COVERAGE}, 
				BUILDING+"=?"+" and "+FLOOR+"=?", new String[]{Building,Floor},null, null, null, null);
		
		ArrayList<YunZiShow> beaconInfo = new ArrayList<YunZiShow>(); 
		while(cursor.moveToNext()){
			beaconInfo.add(new YunZiShow(cursor.getInt(0),new PointF(Integer.valueOf(cursor.getString(1))/10,Integer.valueOf(cursor.getString(2))/10), cursor.getString(3)) );
		}
		return beaconInfo;
	}
	public boolean deleteById(int beaconID){
		synchronized (lockObject) {
			//sql事物，处理失败则回滚
			sqliteDatabase.beginTransaction();

			long error = 0;
			try {
				Cursor cursor =sqliteDatabase.query(BEACON_TABLE, null, _ID+"=?", new String[] {String.valueOf(beaconID)}, null, null, null);
//				Cursor cursor =sqliteDatabase.query("select id from beacon_table where _id="+beacon);
				if(cursor.getCount()==0){
					return false;
				}else{
					error = sqliteDatabase.delete(BEACON_TABLE, _ID+"=?", new String[]{String.valueOf(String.valueOf(beaconID))});
				}
				
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}
	}
	public boolean isBeaconInDatabase(IBeacon beacon){
		Cursor cursor =sqliteDatabase.query(BEACON_TABLE, null, ID+"=?", new String[] {beacon.getBluetoothAddress()}, null, null, null);
		if(cursor.isNull(0)){
			return true;
		}else{
			return false;
		}
	}
	/*
	 * 插入数据
	 */
	public boolean addBeaconConfig(String mac, String uuid,int major,int minor,
			String building ,String floor,String coverage,
			String power ,String frequency,
			String temperaturefrequency,String lightfrequency,String accelerate,
			long updateTime) {

		
		synchronized (lockObject) {
			//sql事物，处理失败则回滚
			sqliteDatabase.beginTransaction();

			ContentValues contentValues = new ContentValues();
			contentValues.put(ID, mac);
			contentValues.put(UUID, uuid);
			contentValues.put(MAJOR, major);
			contentValues.put(MINOR, minor);
			contentValues.put(BUILDING, building);
			contentValues.put(FLOOR , floor);
			contentValues.put(POWER, power);
			contentValues.put(FREQUENCY, frequency);
			contentValues.put(TEMPERATUREFREQUENCY, temperaturefrequency);
			contentValues.put(LIGHTFREQUENCY, lightfrequency);
			contentValues.put(ACCELERATE, accelerate);
			contentValues.put(UPDATE_TIME, updateTime);
			contentValues.put(COVERAGE, coverage);

			long error = 0;
			try {
				Cursor cursor =sqliteDatabase.query(BEACON_TABLE, null, ID+"=?", new String[] {mac}, null, null, null);
				if(cursor.getCount()==0){
					error = sqliteDatabase.insertOrThrow(BEACON_TABLE,
							null, contentValues);
				}else{
					error = sqliteDatabase.update(BEACON_TABLE, contentValues, ID+"=?", new String[] {mac});
				}
				
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}

	}
	private class DatabaseHelper extends SQLiteOpenHelper {
		/* 构造函数-创建一个数据库 */
		DatabaseHelper(Context context) {
			// 当调用getWritableDatabase()
			// 或 getReadableDatabase()方法时
			// 则创建一个数据库

			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 数据库建表
			try {
				synchronized (lockObject) {
					db.execSQL(createBeaconTable);
				}

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}

		}

		/* 升级数据库 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				// db.execSQL(m_DelTB_BeaconConfig);
				// db.execSQL(m_DelTB_ZoneConfig);
				// onCreate(db);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}
}
