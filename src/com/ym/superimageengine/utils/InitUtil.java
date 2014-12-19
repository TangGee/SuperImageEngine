package com.ym.superimageengine.utils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * ��� ��ȡһЩ �ֻ�����  �������޹� ϲ������ȥ��
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
	
	//��һ������ ��ȡ�ֻ���һЩ��Ϣ
	
	/**
	 * ��ȡ�ռ����� 
	 * @param context
	 */
	public static void saveUdid(Context context)
	{
		 
		TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String udid=telephonemanage.getDeviceId();
		context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("udid", udid).commit();

	}
	
	/**
	 * ��ȡϵͳ�汾��
	 */
	public static void saveOSVersion(Context context)
	{
		context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("osversion", android.os.Build.VERSION.RELEASE).commit();
		
	}
	
	/**
	 * ��ȡappversion
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
