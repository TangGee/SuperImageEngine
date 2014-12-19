package com.ym.superimageengine.utils.current;

import android.graphics.Bitmap;

/**
 * 这个类用于ImageDownloader类下载图片后封装 该类包含了一个bitmap对象
 * 该类包含四个成员变量
 * position 用于记录listView 中要显示的position
 * bitmap是图片对象
 * url 是请求的地址 要给lrucache纪录作为键
 * tag 是标签 用于区分一个activity的多个listview 如果你只有一个控件需要填充图片
 * 这个就可以不用关系
 * 
 * 
 * @author s0ng
 *
 */
public class PositionBitMap {

	private int position;
	private Bitmap bitmap;
	private String url;
	private int tag;
	
	
	
	public PositionBitMap(int position,Bitmap bitmap,String url,int tag)
	{
		this.position=position;
		this.bitmap=bitmap;
		this.url=url;
		this.tag=tag;
		
	}
	
	
	public int getTag()
	{
		return tag;
	}
	
	
	
	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public int getPosition() {
		return position;
	}
	
	
	
	
	public void setPosition(int position) {
		this.position = position;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	
	
	
}
