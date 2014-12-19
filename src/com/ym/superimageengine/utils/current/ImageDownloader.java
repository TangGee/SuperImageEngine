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
	 * ���е�������ɻٵ������ļ�����
	 */
	private OnDownImageFinish onDownImageFinishListener;
	private final ExecutorService executorService;

	/**
	 * ����ͼƬ ���ҷ����ʺ�viewPagerʹ�õ���
	 * 
	 * @param urls
	 *            ���ص�ַ�ļ���
	 * @param timeout
	 *            ��ʱʱ��
	 * @param   ��ǩ ���ֲ�ͬ��Viewʹ�õ�ͼƬ ��Ϊ������д��Ŀ��ʱ�� ��һ��
	 * �������������� ListView ͬʱ��Ҫ����ͼƬ ���Ա���������Tag��ǩ ���������Բ�ͬ
	 * ListView��ҳ�� 
	 * 
	 * @param soTime ����Http�����soTime��ʱʱ�� 
	 * @param timeout  ���� Http����� connecion�ĳ�ʱʱ�� 
	 * @param width ����ͼƬ�Ŀ�� ���С��ԭͼ�ͻ�ѹ��  ����ԭͼ�򲻲���
	 * @param height ͼƬ�Ŀ��
	 * 
	 * ������������ѹ�����ܵ� ����ͼƬ�ķ��� 
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
						 * ����Ӧ��ѹ������ͼƬ
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
							Log.i("AAAA", "������ֱ��������"+dy);
							scase = dy;

						}

						if (dx > dy && dy > 1) {
							Log.i("AAAA", "����ˮƽ��������"+dx);
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
	 * ��ͬ��һ������ ���������� �÷������ṩ ѹ������ 
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
	 * ����������downImage�лص��ķ��� ��activity��ʵ��
	 * @author s0ng
	 *
	 */
	public interface OnDownImageFinish {
		void callBack(PositionBitMap pb);
	}

	/**
	 * ���ûٵ�����
	 * @param onDownImageFinish
	 */
	public void setNnDownImageFinishListener(OnDownImageFinish onDownImageFinish) {
		this.onDownImageFinishListener = onDownImageFinish;

	}

	// private final ExecutorService
	// executorService2=Executors.newScheduledThreadPool(2);

	
	/**
	 * �����������   �� ͨ��completionService�������߳����ص� ���������̫���� ���������߲�Ҫʹ�� ��������ʲô�Ľ�
	 * ����������� 
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
	 * �ر� �̳߳�
	 */
	public void shutdownNow() {
		executorService.shutdownNow();
	}

	/**
	 * ���ŵĹر� �ο�jdk�ĵ�
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
