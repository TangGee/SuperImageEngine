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
 * 实现系统的LRCCACHE的子类 用于本地缓存图片 当内存中的缓存超过阀值就会去删除连标的最前边那个(访问最少的) 而且会调用
 * entryRemoved()方法 这个方法实际上什么都没有做 这里我们来实现它 将删除的图片保存在本地
 * 
 * create方法呢 是在调用lrucache的get方法时如果内存中没有该key对应的value 那么就会调用create方法
 * create方法也是什么没做 这里我们来实现它从文件系统取出数据
 * 
 * 
 * 在这里我设计一下本地缓存的管理 要怎么 初步设计 用两个文件夹 分别是current 增加个缓存清理功能么 缓存????? 开一个服务区管理???
 * 开启一个线程去遍历图片 下载日期超过十天的就删除
 * 
 * @author s0ng
 * 
 * @param <K>
 * @param <V>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MyLruCache extends LruCache<String, Bitmap> {

	/**
	 * 关于加载图片的一些优化 措施 尽量避免OOM
	 */
	BitmapFactory.Options options;

	/**
	 * 这个参数未用 打算实现 本地缓存大小达到伐值就不在 本地缓存 但是考虑到这绝对是受累不讨好的工作 所以没有实现
	 */
	private long local_Size;

	/**
	 * 要缓存图片的文件地址
	 */
	private File file;
	/**
	 * 这个值也没用 当初想 当内存剩余少于 这个值的时候就不去 缓存了 但是后来 在 应用初始化的时候 就确定了是否要缓存
	 */
	private static final long NOT_LOCAL_CACHE_SIZE = 50 * 1024 * 1024;

	/**
	 * 
	 * @param maxSize
	 *            缓存大小
	 * @param local_Size
	 *            本地缓存大小 这里还没有实现
	 * @param file_path
	 *            这个应该有一登陆的时候 判断有无存储卡 存储卡剩余空间 来决定 缓存路径
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
	 * 重写了 LruCache的create方法 当内存没有就去缓存地址去读图片 因为我们 使用url作为图片的本地名称 所以将/和冒号都替换掉
	 */
	@Override
	protected Bitmap create(String key) {
		// 这里从我们的文件系统中找到

		key = key.replace("/", "杠");
		key = key.replace(":", "冒");
		String path = file.getAbsolutePath() + "/" + key;
		return BitmapFactory.decodeFile(path, options);

	}

	/**
	 * 这里讲value添加到我们的 文件系统 但是必须保证 如果文件的内容超出 缓存指定大小
	 */
	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
			Bitmap newValue) {

		/**
		 * 如果开启本应用的时候内存小于50M 我们就不要本地缓存了
		 * 
		 */
		if (GlobalParams.CACHE_LOCAL.equals(CacheLocal.NONE)) {
			return;
		}

		key = key.replace("/", "杠");
		key = key.replace(":", "冒");

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
	 * 重写LruCache获取大小的方法
	 */
	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getByteCount();
	}

}
