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
 * ����  ����Ȥ����ע�� 
 * 
 * ���� �ǲ��ö�IO���ü������ͼƬ���ص���
 * �����Ĺ���û��ʵ��  ���˳����˺ܳ�ʱ�� ����ʹ�ö�·���ü��� �ᵼ�ºܶ��޷�
 * Ԥ�Ƶ�bug
 * ��������Ϊ����д�ķ�������  ��������ʲô�õķ��� ���߸�д�˸��� ��һ������
 * ���� ��ô�һ�ǳ���л����
 * 
 * @author s0ng
 * 
 */
public abstract class WebImageU implements SmartImage {

	private MyMap<SocketChannel, String> channelMap = new MyMap<SocketChannel, String>();
	private Map<MySockAddr, String> mapping;

	

	/**
	 * ʹ�ô�������
	 * 
	 * @param urls
	 *            �����·���б�
	 * @param obj
	 *            view�����
	 * @param proxy
	 *            ��������
	 * @param port
	 *            ����˿�
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
	 *            ��Ҫ���ص����� ������� urls��
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
						 * ���������������� ��ν���bitmap? Ӧ������BitmapFactory��ȡ
						 * ���������ʹ��inputstream��ȡ ���ʹ������ ��Ӵ��ڴ��ѹ����
						 * ����inputstream��native���� Ҫ�鿴Դ���뿴���ײ㵽�������ƴװbitmap��
						 * Ҳ��ѹ����ʱ��̰ܶ� ��ʻ����� �ײ�϶�Ҳ��ѭ����ȥƴװbitmap
						 * 
						 * ���������ʱ���Բ����Ҳ������ �Ҵ����Ժ��Լ�дһ�� InputStream������������ ��BitmapFactoryʹ��
						 */
						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						boolean firstRead = true;
						int read = 0;
						boolean readed = false;
						
						/**
						 * ����ͳ�������  ����Ҵ�����һ��Url�ͺ�������  �������ͬʱ���������URL�ͳ����� read����=0  ����ֱ��Ϊ-1
						 * �Һܲ��������Ϊʲô�� �Ҿ���ÿ��SocketChannel�Ķ˿ڶ���һ��  �ں˲����ڸ�찡 
						 */
						while ((read = channel.read(buffer)) != -1) {
							if (read == 0 && readed) {
								break;
							} else if (read == 0) {
								continue;
							}

							buffer.flip();// flip�����ڶ��������ֽڲ���֮ǰ���á�
							// bos.write(buffer.array());
							if (firstRead) {

								firstRead = false;
								String temp[] = new String(buffer.array())
										.split("\r\n\r\n");

								bos.write(temp[1].getBytes());

							} else {
								bos.write(buffer.array());
							}

							buffer.clear();// ��ջ���
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
							// ��Ҫ��������ȡpath��Ͳ��ð��� ���address�����ǵİ�ddressô
							InetSocketAddress address = (InetSocketAddress) channel
									.socket().getRemoteSocketAddress();

							// ����϶�����
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
	 * �Լ�����ķ��� ����Ĳ����� ��byteת��ΪͼƬ����view
	 * 
	 * ��������и����Ŷ
	 * 
	 * @param byteArray
	 * @param view
	 */
	protected abstract void option(Bitmap bitmap, View view);

	/**
	 * ע��select����Ȥ�¼�
	 * ͬʱ��װMyMap<K, V>  ������SocketChannel  value��Path
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
	 * ����urlƴװsocketaddress
	 * 
	 * ��Url���ΪSocketAddress ��path ���ұ����±�
	 * 
	 * @param urls
	 *            urls�ļ���
	 * @return ��ŵ�ַ������·����map����
	 * @throws MalformedURLException
	 */
	private Map<MySockAddr, String> urlToSockAddress(List<String> urls)
			throws MalformedURLException {

		Map<MySockAddr, String> mapping = new HashMap<MySockAddr, String>();
		for (int i = 0; i < urls.size(); i++) {
			String s = urls.get(i);
			URL url = new URL(s); // ���������Ҫ�Ż� ��url��ȡ��ȥ ÿ�θı�·�� ��֪���Ƿ����
									// �һ�û�п�ʼ��URL��Դ��
			int port = url.getPort() != -1 ? url.getPort() : url
					.getDefaultPort();
			MySockAddr address = new MySockAddr(url.getHost(), port, i);
			String path = url.getPath();
			mapping.put(address, path);

		}

		return mapping;
	}

	/**
	 * ����ڲ��� ��װ��һ��SocketAddress ͬʱ����һ���±� ���ڽ���������±����������MyMap<K, V>���� ��Ϊposition
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
	 * //�����������������������SocketChannel����  value ������ͼƬ������·��  ���ұ��洫������url��index 
	 *  ���index�������ǵ�listView���±�  
	 * @author s0ng
	 *
	 * @param <K>
	 * @param <V>
	 */
	private class MyMap<K, V> extends HashMap<K, V> {

		//���ڱ���һ����� �����������������ΪListView���±�ʹ����  
		
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
