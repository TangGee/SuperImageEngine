package com.ym.superimageengine.utils;
import java.io.File;

import android.util.Log;

/**
 * 这个类 是用于 查看linux文件系统中file 的一些文件 时间属性 
 * 本项目 采用该类 意在删除定期未被访问的文件 
 * @author s0ng
 *
 */
public class FileUtil {

	/**
	 * 获取图片上次 访问 时间  
	 * @param path 路径 
	 * @return 以秒为单位 举例 unix元年的时间 
	 */
	public static native long getFileAccessTime(String path);
	
	/**
	 * 获取图片多久未访问 访问 时间  
	 * @param path 路径 
	 * @return 以秒为单位  
	 */
	public static native long howmanyTimeUnAccess(String path);
	
	/**
	 * 获取 当前举例unix元年 时间 单位秒  
	 * @return
	 */
	public static native long data();
	
	static{
		System.loadLibrary("fileutil");
	}
	
	
	/**
	 * 这里由于比较好费cpu时间 所以单开一个线程 如果想细水长流也可以睡眠秒再去递归
	 * 定义删除某个文件夹下面 多久未访问的文件 
	 * @param time  多久未访问会被删除
	 * @param dir 要扫描的文件夹 
	 */
	public static void clearLongTimeUnAccess(final long time, String dir)
	{
		final File root=new File(dir);
		if(root.exists()&&root.isDirectory())
		{
			new Thread(){
				public void run()
				{
					deleFile(root, time);
				}
			}.start();
		}
		
	}
	
	/**
	 * 递归遍历删除所有访问时间超过指定时间的文件
	 * @param root  文件加
	 * @param time  时间
	 */
	public static void deleFile(File root,long time)
	{
		File[] files=root.listFiles();
		for(File file:files)
		{
			if(file.isDirectory())
			{
				//这里由于比较好费cpu时间 所以单开一个线程 如果想细水长流也可以睡眠秒再去递归
				deleFile(file,time);
			}else{
				long whattime=howmanyTimeUnAccess(file.getAbsolutePath());
				if(whattime>time)
				{
					file.delete();
					
				}
				
			}
		}
	}
	
}
