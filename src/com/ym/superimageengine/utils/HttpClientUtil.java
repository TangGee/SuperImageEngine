package com.ym.superimageengine.utils;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.ym.superimageengine.GlobalParams;

/**
 * http���󹤾� �� 
 * @author s0ng
 *
 */
public class HttpClientUtil {
	
	
	private HttpClient client;
	
	/**
	 * ��ʼ�� httpClient  ��������� ���� ����˵apn����� �����й�����Ҫ
	 * ���� GlobalParams�е�PROXY��PORT ��Ϣ ������NetUtil���ʼ�� 
	 * ��� PROXYΪ�� �Ǿ� ��Ҫ����
	 */
	public HttpClientUtil()
	{
		 
		client=new DefaultHttpClient();
	
		if(!TextUtils.isEmpty(GlobalParams.PROXY)) 
		{
			HttpHost host=new HttpHost(GlobalParams.PROXY, GlobalParams.PORT);
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, host);
		}
	}
	
	/**
	 * post���� 
	 * @param url ��������·�� 
	 * @param beans  ����Ҫ��ƴ��������� keyΪ������  valueΪ����ֵ 
	 * @return  
	 */
	public String doPost(String url,Map<String, String> beans)
	{
		HttpPost post=new HttpPost();
		List<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		if(beans!=null)
		for(Entry<String, String> enrty:beans.entrySet())
		{
			
			params.add(new BasicNameValuePair(enrty.getKey(), enrty.getValue()));
		}
		
		try {
			UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params, ConstParams.ENCODING);
			post.setEntity(entity);
			post.setURI(new URI(url));
			HttpResponse response=client.execute(post);
			if(response.getStatusLine().getStatusCode()==200)
			{
				return EntityUtils.toString(response.getEntity(), ConstParams.ENCODING);
			}
			
			
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	

	/**
	 * ��get����ȥ����  ����ͬ��
	 * @param url  
	 * @param beans
	 * @return
	 */
	public String doGet(String url,Map<String,String> beans)
	{
		
		if(beans!=null&&beans.size()>0)
		{
			StringBuilder builder=new StringBuilder();
			builder.append("?");
			for(Entry<String, String>entry : beans.entrySet())
			{
				builder.append(entry.getKey()+"="+entry.getValue()+"&");
			}
			
			url+=builder.substring(0, builder.length()-1).toString();
			
		}
	
		try {
			
			HttpGet get=new HttpGet(url);
			// ����ʱ
			HttpParams httpParams = new BasicHttpParams();
			httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
			HttpConnectionParams.setSoTimeout(httpParams, 8000);
			get.setParams(httpParams);
			
			HttpResponse response=client.execute(get);
			
			if(response.getStatusLine().getStatusCode()==200)
			{
				return EntityUtils.toString(response.getEntity(), ConstParams.ENCODING);
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		return "";
	}
	
	
	/**
	 * 
	 * ��������ͼƬ ������smart image��
	 * @param url
	 * @param beans
	 * @return
	 */
	public InputStream getImageUrl(String url)
	{
		
		try {
			HttpGet get=new HttpGet(url);
			// ����ʱ
			HttpParams httpParams = new BasicHttpParams();
			httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
			HttpConnectionParams.setSoTimeout(httpParams, 8000);
			get.setParams(httpParams);
			
			HttpResponse response=client.execute(get);
			
			if(response.getStatusLine().getStatusCode()==200)
			{
				return response.getEntity().getContent();
			}
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	
			
			
		
		return null;
	}
	
	/**
	 * 
	 * @param url
	 * @param connTimeout  ���ӳ�ʱ �������õ�Сһ�� 800
	 * @param soTimeout ��ȡ���ݳ�ʱ ��������Ϊ5s
	 * @return
	 */
	public InputStream getImageStream(String url,int connTimeout,int soTimeout)
	{
	
		try {
			
			HttpGet get=new HttpGet(url);
			// ����ʱ
			HttpParams httpParams = new BasicHttpParams();
			httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
			HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
			get.setParams(httpParams);
			
			HttpResponse response=client.execute(get);
			
			if(response.getStatusLine().getStatusCode()==200)
			{
				return response.getEntity().getContent();
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}
		return null;
			
	}
	
	
	
	
//	private static Header[] headers;
//	static {
//		headers = new BasicHeader[10];
//		headers[0] = new BasicHeader("Appkey", "12343");
//		headers[1] = new BasicHeader("Udid", "");// �ֻ�����
//		headers[2] = new BasicHeader("Os", "android");//
//		headers[3] = new BasicHeader("Osversion", "");//
//		headers[4] = new BasicHeader("Appversion", "");// 1.0
//		headers[5] = new BasicHeader("Sourceid", "");//
//		headers[6] = new BasicHeader("Ver", "");
//
//		headers[7] = new BasicHeader("Userid", "");
//		headers[8] = new BasicHeader("Usersession", "");
//
//		headers[9] = new BasicHeader("Unique", "");
//	}
//
//	public String sendGet(String uri) {
//	HttpGet	get = new HttpGet(uri);
//		get.setHeaders(headers);
//
//		// ����ʱ
//		HttpParams httpParams = new BasicHttpParams();
//		httpParams = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
//		HttpConnectionParams.setSoTimeout(httpParams, 8000);
//		get.setParams(httpParams);
//		
//		
//		try {
//			HttpResponse response = client.execute(get);
//			if (response.getStatusLine().getStatusCode() == 200) {
//				// ����Ӧ�����ݱ�Ϊ�ַ�������
//				return EntityUtils.toString(response.getEntity(),ConstParams.ENCODING);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	
//	
	
	
	
	
}
