package com.ym.superimageengine.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.ym.superimageengine.GlobalParams;

/**
 * ʵ��ϵͳ��LRCCACHE������ ���ڱ��ػ���ͼƬ ���ڴ��еĻ��泬����ֵ�ͻ�ȥɾ���������ǰ���Ǹ�(�������ٵ�) ���һ����
 * entryRemoved()���� �������ʵ����ʲô��û���� ����������ʵ���� ��ɾ����ͼƬ�����ڱ���
 * 
 * create������ ���ڵ���lrucache��get����ʱ����ڴ���û�и�key��Ӧ��value ��ô�ͻ����create����
 * create����Ҳ��ʲôû�� ����������ʵ�������ļ�ϵͳȡ������
 * 
 * 
 * �����������һ�±��ػ���Ĺ��� Ҫ��ô ������� �������ļ��� �ֱ���current ���Ӹ�����������ô ����????? ��һ������������???
 * ����һ���߳�ȥ����ͼƬ �������ڳ���ʮ��ľ�ɾ��
 * 
 * @author s0ng
 * 
 * @param <K>
 * @param <V>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MyLruCache extends LruCache<String, Bitmap> {

	/**
	 * ���ڼ���ͼƬ��һЩ�Ż� ��ʩ ��������OOM
	 */
	BitmapFactory.Options options;

	/**
	 * �������δ�� ����ʵ�� ���ػ����С�ﵽ��ֵ�Ͳ��� ���ػ��� ���ǿ��ǵ�����������۲��ֺõĹ��� ����û��ʵ��
	 */
	private long local_Size;

	/**
	 * Ҫ����ͼƬ���ļ���ַ
	 */
	private File file;
	/**
	 * ���ֵҲû�� ������ ���ڴ�ʣ������ ���ֵ��ʱ��Ͳ�ȥ ������ ���Ǻ��� �� Ӧ�ó�ʼ����ʱ�� ��ȷ�����Ƿ�Ҫ����
	 */
	private static final long NOT_LOCAL_CACHE_SIZE = 50 * 1024 * 1024;

	/**
	 * 
	 * @param maxSize
	 *            �����С
	 * @param local_Size
	 *            ���ػ����С ���ﻹû��ʵ��
	 * @param file_path
	 *            ���Ӧ����һ��½��ʱ�� �ж����޴洢�� �洢��ʣ��ռ� ������ ����·��
	 */
	public MyLruCache(int maxSize, long local_Size, String file_path) {
		super(maxSize);
		this.local_Size = local_Size;
		file = new File(file_path);
		if (file.exists()) {
			if (!file.isDirectory()) {
				throw new IllegalArgumentException("maxSize <= 0");

			}
		} else {
			file.mkdirs();
		}

		options = new Options();

		options.inPreferredConfig = Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;

	}

	/**
	 * ��д�� LruCache��create���� ���ڴ�û�о�ȥ�����ַȥ��ͼƬ ��Ϊ���� ʹ��url��ΪͼƬ�ı������� ���Խ�/��ð�Ŷ��滻��
	 */
	@Override
	protected Bitmap create(String key) {
		// ��������ǵ��ļ�ϵͳ���ҵ�

		key = key.replace("/", "��");
		key = key.replace(":", "ð");
		String path = file.getAbsolutePath() + "/" + key;
		return BitmapFactory.decodeFile(path, options);

	}

	/**
	 * ���ｲvalue��ӵ����ǵ� �ļ�ϵͳ ���Ǳ��뱣֤ ����ļ������ݳ��� ����ָ����С
	 */
	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
			Bitmap newValue) {

		/**
		 * ���������Ӧ�õ�ʱ���ڴ�С��50M ���ǾͲ�Ҫ���ػ�����
		 * 
		 */
		if (GlobalParams.CACHE_LOCAL.equals(CacheLocal.NONE)) {
			return;
		}

		key = key.replace("/", "��");
		key = key.replace(":", "ð");

		File bm = (new File(file, key));
		if (bm.exists()) {
			return;
		}

		try {
			bm.createNewFile();
			OutputStream outStream = new FileOutputStream(bm);
			oldValue.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			Log.w("ImageFileCache", "FileNotFoundException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.w("ImageFileCache", "IOException");
		}

	}

	/**
	 * ��дLruCache��ȡ��С�ķ���
	 */
	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getByteCount();
	}

}
