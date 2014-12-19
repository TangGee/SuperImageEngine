package com.ym.superimageengine.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.ym.superimageengine.GlobalParams;

/**
 * ���Ǽ������Ĺ����� ͬʱ�ж��Ƿ���apn ��������ʼ��������Ϣ ������GlobaParam��
 * @author s0ng
 *
 */
public class NetUtils {
	//�������
	
	public static boolean cheakUtils(Context context)
	{
		ConnectivityManager cm=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo wifiInfo=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI );
		
		if(wifiInfo.isConnected())
		{
			return true;
		}else{
			NetworkInfo mobileInfo=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if(mobileInfo.isConnected())
			{
				//��׿4.0��֧�� ���������  
				//readAPN(context);
				return true;
			}
			
		}
			
		return false;
	}
	
	
	/**
	 * APN��ѡ��,�Ĵ�����Ϣ�Ƿ������ݣ������wap��ʽ
	 * 
	 * @param context
	 */
	private static void readAPN(Context context) {
		Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");//4.0ģ�������ε���Ȩ��

		// ������ϵ������
		ContentResolver resolver = context.getContentResolver();
		// �ж����ĸ�APN��ѡ����
		Cursor cursor = resolver.query(PREFERRED_APN_URI, null, null, null, null);
		
		if(cursor!=null&&cursor.moveToFirst())
		{
			GlobalParams.PROXY=cursor.getString(cursor.getColumnIndex("proxy"));
			GlobalParams.PORT=cursor.getInt(cursor.getColumnIndex("port"));
		}
		

	}


}
