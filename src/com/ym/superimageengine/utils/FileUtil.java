package com.ym.superimageengine.utils;
import java.io.File;

import android.util.Log;

/**
 * ����� ������ �鿴linux�ļ�ϵͳ��file ��һЩ�ļ� ʱ������ 
 * ����Ŀ ���ø��� ����ɾ������δ�����ʵ��ļ� 
 * @author s0ng
 *
 */
public class FileUtil {

	/**
	 * ��ȡͼƬ�ϴ� ���� ʱ��  
	 * @param path ·�� 
	 * @return ����Ϊ��λ ���� unixԪ���ʱ�� 
	 */
	public static native long getFileAccessTime(String path);
	
	/**
	 * ��ȡͼƬ���δ���� ���� ʱ��  
	 * @param path ·�� 
	 * @return ����Ϊ��λ  
	 */
	public static native long howmanyTimeUnAccess(String path);
	
	/**
	 * ��ȡ ��ǰ����unixԪ�� ʱ�� ��λ��  
	 * @return
	 */
	public static native long data();
	
	static{
		System.loadLibrary("fileutil");
	}
	
	
	/**
	 * �������ڱȽϺ÷�cpuʱ�� ���Ե���һ���߳� �����ϸˮ����Ҳ����˯������ȥ�ݹ�
	 * ����ɾ��ĳ���ļ������� ���δ���ʵ��ļ� 
	 * @param time  ���δ���ʻᱻɾ��
	 * @param dir Ҫɨ����ļ��� 
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
	 * �ݹ����ɾ�����з���ʱ�䳬��ָ��ʱ����ļ�
	 * @param root  �ļ���
	 * @param time  ʱ��
	 */
	public static void deleFile(File root,long time)
	{
		File[] files=root.listFiles();
		for(File file:files)
		{
			if(file.isDirectory())
			{
				//�������ڱȽϺ÷�cpuʱ�� ���Ե���һ���߳� �����ϸˮ����Ҳ����˯������ȥ�ݹ�
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
