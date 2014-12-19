package com.ym.superimageengine.utils.select;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;

import com.ym.superimageengine.GlobalParams;
import com.ym.superimageengine.utils.smart.SmartImage;

/***
 * 作废  有兴趣读下注释 
 * 
 * 本类 是采用多IO复用技术完成图片下载的类
 * 这个类的功能没有实现  本人尝试了很长时间 但是使用多路复用技术 会导致很多无法
 * 预计的bug
 * 可能是因为本人写的方法不对  若是您有什么好的方法 或者改写了该类 请一定与我
 * 分享 那么我会非常感谢您的
 * 
 * @author s0ng
 * 
 */
public abstract class WebImageU implements SmartImage {

	private MyMap<SocketChannel, String> channelMap = new MyMap<SocketChannel, String>();
	private Map<MySockAddr, String> mapping;

	

	/**
	 * 使用代理请求
	 * 
	 * @param urls
	 *            请求的路径列表
	 * @param obj
	 *            view对象吧
	 * @param proxy
	 *            代理主机
	 * @param port
	 *            代理端口
	 */
	public void loadImagesUseSelectAndSetProxy(List<String> urls, View view,
			String proxy, String port, Context context) {
		if (!TextUtils.isEmpty(GlobalParams.PROXY)) {
			String proxyHost = proxy;
			String proxyPort = port;
			System.getProperties().put("socksProxySet ", "true ");
			System.getProperties().put("socksProxyHost ", proxyHost);
			System.getProperties().put("socksProxyPort ", proxyPort);
		}

		loadImagesUseSelect(urls, view, context);

	}

	/**
	 * 
	 * @param urls
	 *            需要下载的链接 都存放在 urls中
	 * @param obj
	 */
	@SuppressLint("NewApi")
	public void loadImagesUseSelect(List<String> urls, View view,
			Context context) {

		try {
			mapping = urlToSockAddress(urls);
			Selector selector = Selector.open();

			for (MySockAddr mAddr : mapping.keySet()) {
				register(selector, mAddr);

			}
			int finished = 0, total = mapping.size();

			ByteBuffer buffer = ByteBuffer.allocate(32 * 1024);

			while (finished < total) {
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys()
						.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (key.isValid() && key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();

						buffer.clear();
						/***
						 * 在这里遇到了问题 如何解析bitmap? 应该利用BitmapFactory获取
						 * 这里最好是使用inputstream获取 如果使用数组 会加大内存的压力阿
						 * 但是inputstream是native方法 要查看源代码看看底层到底是如何拼装bitmap的
						 * 也许压力的时间很短吧 先驶下如何 底层肯定也是循环的去拼装bitmap
						 * 
						 * 这个问题暂时可以不解决也可以用 我打算以后自己写一个 InputStream的子类在这里 给BitmapFactory使用
						 */
						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						boolean firstRead = true;
						int read = 0;
						boolean readed = false;
						
						/**
						 * 问题就出在这里  如果我传进来一个Url就毫无问题  但是如果同时传进来多个URL就出错了 read总是=0  或者直接为-1
						 * 我很不理解这是为什么呢 我觉得每个SocketChannel的端口都不一样  内核不至于搞混啊 
						 */
						while ((read = channel.read(buffer)) != -1) {
							if (read == 0 && readed) {
								break;
							} else if (read == 0) {
								continue;
							}

							buffer.flip();// flip方法在读缓冲区字节操作之前调用。
							// bos.write(buffer.array());
							if (firstRead) {

								firstRead = false;
								String temp[] = new String(buffer.array())
										.split("\r\n\r\n");

								bos.write(temp[1].getBytes());

							} else {
								bos.write(buffer.array());
							}

							buffer.clear();// 清空缓冲
							readed = true;
						}

						if (read == -1) {
							finished++;
							key.cancel();

							Bitmap bitmap = BitmapFactory.decodeByteArray(
									bos.toByteArray(), 0,
									bos.toByteArray().length);

							int position = channelMap.getPosition(channel);
							option(bitmap, view);
							bos.close();
						}
					}
					if (key.isValid() && key.isConnectable()) {

						SocketChannel channel = ((SocketChannel) key.channel());

						boolean success = channel.finishConnect();
						if (!success) {
							finished++;
							key.cancel();
						} else {
							// 需要根据它获取path这就不好办了 这个address是我们的阿ddress么
							InetSocketAddress address = (InetSocketAddress) channel
									.socket().getRemoteSocketAddress();

							// 这里肯定不对
							String path = channelMap.get(channel);
							
				
							StringBuilder temp=new StringBuilder();
							
							StringBuilder request = new StringBuilder();
							request.append("GET " + path + " HTTP/1.1\r\n");
//							request.append("Host: " +  address.getHostString();
							request.append("\r\n");

							ByteBuffer header = ByteBuffer.wrap(request
									.toString().getBytes());
							
							channel.write(header);

						}
					}
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自己定义的方法 具体的操作类 把byte转换为图片传入view
	 * 
	 * 这里必须有个标记哦
	 * 
	 * @param byteArray
	 * @param view
	 */
	protected abstract void option(Bitmap bitmap, View view);

	/**
	 * 注册select感兴趣事件
	 * 同时封装MyMap<K, V>  主键是SocketChannel  value是Path
	 * 
	 * @param selector
	 * @param url2
	 * @throws IOException
	 */
	private void register(Selector selector, MySockAddr address)
			throws IOException {

		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(address.getSocketAddress());
		channel.register(selector, SelectionKey.OP_CONNECT
				| SelectionKey.OP_READ);

		channelMap.put(channel, mapping.get(address), address.getPosition());

	}

	/**
	 * 根据url拼装socketaddress
	 * 
	 * 把Url插接为SocketAddress 和path 并且保存下标
	 * 
	 * @param urls
	 *            urls的集合
	 * @return 存放地址和请求路径的map集合
	 * @throws MalformedURLException
	 */
	private Map<MySockAddr, String> urlToSockAddress(List<String> urls)
			throws MalformedURLException {

		Map<MySockAddr, String> mapping = new HashMap<MySockAddr, String>();
		for (int i = 0; i < urls.size(); i++) {
			String s = urls.get(i);
			URL url = new URL(s); // 这里可能需要优化 将url提取出去 每次改变路径 不知道是否可行
									// 我还没有开始看URL的源码
			int port = url.getPort() != -1 ? url.getPort() : url
					.getDefaultPort();
			MySockAddr address = new MySockAddr(url.getHost(), port, i);
			String path = url.getPath();
			mapping.put(address, path);

		}

		return mapping;
	}

	/**
	 * 这个内部类 封装了一个SocketAddress 同时保存一个下标 用于将来把这个下标存放在下面的MyMap<K, V>类中 作为position
	 * @author s0ng
	 *
	 */
	private class MySockAddr {

		private SocketAddress socketAddress;
		private int position;

		public SocketAddress getSocketAddress() {
			return socketAddress;
		}

		public Integer getPosition() {
			return position;
		}

		public MySockAddr(String host, int port, int position) {
			socketAddress = new InetSocketAddress(host, port);
			this.position = position;
		}

	}

	/**
	 * //这个类在这里我主键我想用SocketChannel类型  value 类型是图片的请求路径  并且保存传进来的url的index 
	 *  这个index就是我们的listView的下标  
	 * @author s0ng
	 *
	 * @param <K>
	 * @param <V>
	 */
	private class MyMap<K, V> extends HashMap<K, V> {

		//用于保存一个编号 这个编号我是想把它作为ListView的下标使用呢  
		
		private HashMap<K, Integer> hashMap = new HashMap<K, Integer>();

		public V put(K key, V value, Integer position) {
			hashMap.put(key, position);
			return super.put(key, value);
		}

		public Integer getPosition(K key) {
			return hashMap.get(key);
		}

	}

}
