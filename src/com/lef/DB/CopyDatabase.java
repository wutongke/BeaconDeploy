package com.lef.DB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
/**
 * 将asset中的数据装载
 * @author lief
 *
 */
public class CopyDatabase {
	private static String DATABASE_PATH = null;
	private static String DATABASE_NAME = null;
/**
 * 	
 * @param context 
 * @param dbPath 需要装载的数据库路径
 * @param dbFileName 装载的文件名称
 */
	//-----------装载数据库------------------------------------------
	public static void setupDatabase(Context context, String dbPath,
			String dbFileName) {
		DATABASE_PATH = dbPath;
		DATABASE_NAME = dbFileName;
		String fileFullName = DATABASE_PATH + DATABASE_NAME;
		File dbFile = new File(fileFullName);
		//调试时，每次都创建新的数据库
		if(Debug.Debug){
			// 创建目标文件
			CreateFile(fileFullName);
			copyFileFromAssets(context, DATABASE_NAME, dbFile);
		}else{
			if (!dbFile.exists()) {
				// 创建目标文件
				CreateFile(fileFullName);
				copyFileFromAssets(context, DATABASE_NAME, dbFile);
				Log.d("wxq", "setup database");
			} else {
				Log.d("wxq", "database already existed!");
			}
		}
	}
	public static void copyFileFromAssets(Context context,String fileInAssets,File outFile) {
    	AssetManager am = context.getAssets();
        InputStream is;
        FileOutputStream fos = null;
		try 
		{
			is = am.open(fileInAssets);
			fos = new FileOutputStream(outFile);
			byte [] buffer = new byte[8192];
			
			int count = 0;
			while((count = is.read(buffer))>=0)
			{
				fos.write(buffer, 0, count);
			}
			fos.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("wxq", "Can NOT copy the file:"+outFile.getName());
			e.printStackTrace();
		}
	}
	public static boolean CreateFile(String destFileName) {
	    File file = new File(destFileName);
	    if (!Debug.Debug) {
			if (file.exists()) {
				System.out.println("创建单个文件" + destFileName + "失败，目标文件已存在！");
				return false;
			}
			if (destFileName.endsWith(File.separator)) {
				System.out.println("创建单个文件" + destFileName + "失败，目标不能是目录！");
				return false;
			}
			if (!file.getParentFile().exists()) {
				System.out.println("目标文件所在路径不存在，准备创建。。。");
				if (!file.getParentFile().mkdirs()) {
					System.out.println("创建目录文件所在的目录失败！");
					return false;
				}
			}
		}
		// 创建目标文件
	    try {
	     if (file.createNewFile()) {
	    	 System.out.println("创建单个文件" + destFileName + "成功！");
	    	 return true;
	     } 
	     else {
	    	 System.out.println("创建单个文件" + destFileName + "失败！");
	    	 return false;
	     }
	    } 
	    catch (IOException e) {
	    	e.printStackTrace();
	    	System.out.println("创建单个文件" + destFileName + "失败！");
	    	return false;
	    }
	}
}
