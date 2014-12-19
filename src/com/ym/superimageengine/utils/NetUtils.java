package com.ym.superimageengine.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.ym.superimageengine.GlobalParams;

/**
 * 这是检查网络的工具类 同时判断是否是apn 如果是则初始化代理信息 保存在GlobaParam下
 * @author s0ng
 *
 */
public class NetUtils {
	//检测网络
	
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
				//安卓4.0后不支持 但真机可以  
				//readAPN(context);
				return true;
			}
			
		}
			
		return false;
	}
	
	
	/**
	 * APN被选中,的代理信息是否有内容，如果有wap方式
	 * 
	 * @param context
	 */
	private static void readAPN(Context context) {
		Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");//4.0模拟器屏蔽掉该权限

		// 操作联系人类似
		ContentResolver resolver = context.getContentResolver();
		// 判断是哪个APN被选中了
		Cursor cursor = resolver.query(PREFERRED_APN_URI, null, null, null, null);
		
		if(cursor!=null&&cursor.moveToFirst())
		{
			GlobalParams.PROXY=cursor.getString(cursor.getColumnIndex("proxy"));
			GlobalParams.PORT=cursor.getInt(cursor.getColumnIndex("port"));
		}
		

	}


}
