package com.ym.superimageengine.utils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 这个 获取一些 手机参数  跟本类无关 喜欢的拿去用
 * @author s0ng
 *
 */
public class InitUtil {

	
	public static void init(Context context)
	{
		saveUdid(context);
		saveOSVersion(context);
		saveAppVersion(context);
		
		
	}
	
	//第一次启动 获取手机的一些信息
	
	/**
	 * 获取收集串号 
	 * @param context
	 */
	public static void saveUdid(Context context)
	{
		 
		TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String udid=telephonemanage.getDeviceId();
		context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("udid", udid).commit();

	}
	
	/**
	 * 获取系统版本号
	 */
	public static void saveOSVersion(Context context)
	{
		context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("osversion", android.os.Build.VERSION.RELEASE).commit();
		
	}
	
	/**
	 * 获取appversion
	 */
	public static void saveAppVersion(Context context)
	{
		PackageManager pm=context.getPackageManager();
		try {
			PackageInfo pi=pm.getPackageInfo(context.getPackageName(), 0);
			context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("appversion",pi.versionName).commit();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
