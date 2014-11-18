package com.lef.method;

import android.graphics.PointF;

/**
 * @author lief
 * 展示云子时用到的封装类
 */
public class YunZiShow {
	
	private int ID;
	private PointF locate;
	private String coverage;
	
	/**
	 * 构造方法
	 * @param ID
	 * @param locate
	 * @param coverage
	 */
	public YunZiShow(int ID,PointF locate,String coverage){
		this.setID(ID);
		this.coverage = coverage;
		this.locate = locate;
	}
	/**
	 * 获取云子的位置
	 * @return
	 */
	public PointF getLocate() {
		return locate;
	}
	public String getCoverage() {
		return coverage;
	}
	/**
	 * 获取云子的覆盖范围
	 * @return
	 */
	public int getCoverageInt(){
		return Integer.valueOf(coverage.substring(3,5));
	}
	public void setLocate(PointF locate) {
		this.locate = locate;
	}
	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	
}
