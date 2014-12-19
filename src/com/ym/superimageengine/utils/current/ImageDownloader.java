package com.ym.superimageengine.utils.current;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import com.ym.superimageengine.utils.HttpClientUtil;

@SuppressLint("NewApi")
public class ImageDownloader {

	private int threadCount;
	public ImageDownloader(int threadCount) 
	{
		executorService=  Executors
				.newFixedThreadPool(threadCount);
	}
	
	/**
	 * 持有的下载完成毁掉函数的监听者
	 */
	private OnDownImageFinish onDownImageFinishListener;
	private final ExecutorService executorService;

	/**
	 * 下载图片 并且返回适合viewPager使用的类
	 * 
	 * @param urls
	 *            下载地址的集合
	 * @param timeout
	 *            超时时间
	 * @param   标签 区分不同的View使用的图片 因为本人在写项目的时候 有一个
	 * 界面里面有两个 ListView 同时需要下载图片 所以本身设置了Tag标签 来区分来自不同
	 * ListView的页面 
	 * 
	 * @param soTime 这是Http请求的soTime超时时间 
	 * @param timeout  这是 Http请求的 connecion的超时时间 
	 * @param width 下载图片的宽度 如果小于原图就会压缩  大于原图则不操作
	 * @param height 图片的宽度
	 * 
	 * 这个方法是需带压缩功能的 下载图片的方法 
	 * 
	 */
	public void downImage(final Map<String, Integer> urls, final int timeout,
			final int soTimeout, final int tag, final int width,
			final int height) {
		for (final String url : urls.keySet()) {
			if (!executorService.isShutdown())
				executorService.submit(new Runnable() {

					PositionBitMap pb;

					@Override
					public void run() {

						Log.i("AAAA", "downimage threeadid :"
								+ Thread.currentThread().getId());

						/**
						 * 这里应该压缩以下图片
						 */

						HttpClientUtil clientUtil = new HttpClientUtil();

						BitmapFactory.Options opts = new Options();
						opts.inJustDecodeBounds = true;

						BitmapFactory.decodeStream(clientUtil.getImageStream(
								url, timeout, soTimeout), null, opts);

						int bitMapWeight = opts.outWidth;
						int bitMapHeight = opts.outHeight;

						int dx = bitMapWeight / width;
						int dy = bitMapHeight / height;
						int scase = 1;

						if (dy > dx && dx > 1) {
							Log.i("AAAA", "按照竖直方向缩放"+dy);
							scase = dy;

						}

						if (dx > dy && dy > 1) {
							Log.i("AAAA", "按照水平方向缩放"+dx);
							scase = dx;
						}

						opts.inJustDecodeBounds = false;

						pb = new PositionBitMap(urls.get(url), BitmapFactory
								.decodeStream(clientUtil.getImageStream(url,
										timeout, soTimeout), null, opts), url,
								tag);
						onDownImageFinishListener.callBack(pb);

					}
				});

		}

	}

	/**
	 * 这同上一个方法 的区别在于 该方法不提供 压缩功能 
	 * @param urls
	 * @param timeout
	 * @param soTimeout
	 * @param tag
	 */
	public void downImage(final Map<String, Integer> urls, final int timeout,
			final int soTimeout, final int tag) {
		for (final String url : urls.keySet()) {
			if (!executorService.isShutdown())
				executorService.submit(new Runnable() {

					PositionBitMap pb;

					@Override
					public void run() {

						Log.i("AAAA", "downimage threeadid :"
								+ Thread.currentThread().getId());

						HttpClientUtil clientUtil = new HttpClientUtil();

						InputStream is=clientUtil.getImageStream(url,
								timeout, soTimeout);
						Bitmap b=BitmapFactory
						.decodeStream(is);
						pb = new PositionBitMap(urls.get(url), b, url, tag);
						onDownImageFinishListener.callBack(pb);

					}
				});

		}

	}
	
	/**
	 * 这是在两个downImage中回调的方法 在activity中实现
	 * @author s0ng
	 *
	 */
	public interface OnDownImageFinish {
		void callBack(PositionBitMap pb);
	}

	/**
	 * 设置毁掉机制
	 * @param onDownImageFinish
	 */
	public void setNnDownImageFinishListener(OnDownImageFinish onDownImageFinish) {
		this.onDownImageFinishListener = onDownImageFinish;

	}

	// private final ExecutorService
	// executorService2=Executors.newScheduledThreadPool(2);

	
	/**
	 * 这个方法更简单   是 通过completionService来管理线程下载的 这个方法不太好用 建议下载者不要使用 若是您有什么改进
	 * 方案请告诉我 
	 * @param urls
	 * @param timeout
	 * @param tag
	 */
	public void allDownload(Map<URL, Integer> urls, long timeout, int tag) {

		CompletionService<PositionBitMap> completionService = new ExecutorCompletionService<PositionBitMap>(
				executorService);

		for (URL url : urls.keySet()) {

			Holder holder = new Holder(urls.get(url), url);
			completionService.submit(new CallHandler(holder, tag));

		}

		for (int i = 0; i < urls.size(); i++) {
			Future<PositionBitMap> future = null;
			try {
				future = completionService.take();
				PositionBitMap pb = future.get(timeout, TimeUnit.SECONDS);

				onDownImageFinishListener.callBack(pb);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				future.cancel(true);
			}
		}

	}

	class CallHandler implements Callable {

		Holder holder;
		int tag;

		public CallHandler(Holder holder, int tag) {
			this.holder = holder;
			this.tag = tag;
		}

		@Override
		public PositionBitMap call() throws Exception {

			InputStream is = holder.url.openStream();

			Bitmap bitmap = BitmapFactory.decodeStream(is);
			return new PositionBitMap(holder.position, bitmap,
					holder.url.toString(), tag);
		}

	}

	/**
	 * 关闭 线程池
	 */
	public void shutdownNow() {
		executorService.shutdownNow();
	}

	/**
	 * 优雅的关闭 参考jdk文档
	 */
	public void close() {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(3, TimeUnit.MINUTES)) {
				executorService.shutdown();
				executorService.awaitTermination(3, TimeUnit.MINUTES);
			}
		} catch (InterruptedException e) {
			executorService.shutdown();
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * 
	 * @author s0ng
	 *
	 */
	class Holder {
		int position;
		URL url;

		Holder(int position, URL url) {
			this.position = position;
			this.url = url;
		}

	}

	

}
