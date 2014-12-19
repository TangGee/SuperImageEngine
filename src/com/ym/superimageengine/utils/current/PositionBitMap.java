package com.ym.superimageengine.utils.current;

import android.graphics.Bitmap;

/**
 * ���������ImageDownloader������ͼƬ���װ ���������һ��bitmap����
 * ��������ĸ���Ա����
 * position ���ڼ�¼listView ��Ҫ��ʾ��position
 * bitmap��ͼƬ����
 * url ������ĵ�ַ Ҫ��lrucache��¼��Ϊ��
 * tag �Ǳ�ǩ ��������һ��activity�Ķ��listview �����ֻ��һ���ؼ���Ҫ���ͼƬ
 * ����Ϳ��Բ��ù�ϵ
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
