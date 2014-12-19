package com.ym.superimageengine.simple;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ym.superimageengine.R;
import com.ym.superimageengine.utils.MyLruCache;
import com.ym.superimageengine.utils.current.ImageDownloader;
import com.ym.superimageengine.utils.current.ImageDownloader.OnDownImageFinish;
import com.ym.superimageengine.utils.current.PositionBitMap;

public class Simple extends Activity{

	/**
	 * 每次加载个  
	 */
	private int page=0;
	private int pageNum=10;
	private static final int LV_TAG = 0;
	private MyLruCache cache;
	private ListView lv;
	private ImageDownloader downloader;
	private List<Bean> adapterList;
	private MyAdapter adapter;
	/**
	 * 判断是否还有更多数据 如果为false表示再拉动也不会加载更多数据了
	 */
	private boolean hasMore=true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * 设置2个线程
		 */
		downloader=new ImageDownloader(2);

		lv=(ListView) findViewById(R.id.lv);
		cache=new MyLruCache(2*1024*1024, 1000l, Environment.getExternalStorageDirectory().getAbsolutePath()+"/test");
		
		adapterList=new ArrayList<Bean>();
		adapter=new MyAdapter();
		lv.setAdapter(adapter);
		
		setListener();
		
		getDateFromNet();
		
	
		
	}
	
	
	
	public void setListener()
	{
		
		
		downloader.setNnDownImageFinishListener(new OnDownImageFinish() {
			
			@Override
			public void callBack(PositionBitMap pb) {
				synchronized (this) {
					cache.put(pb.getUrl(), pb.getBitmap());
					
					switch (pb.getTag()) {
					case LV_TAG:
						ImageView iv=(ImageView) lv.findViewWithTag(pb.getPosition());
						iv.setImageBitmap(pb.getBitmap());
						break;

					default:
						break;
					}
					
				}
				
				
			
				
				
			}
		});
		
		
		lv.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			
				if(hasMore)
				{
					if(lv.getLastVisiblePosition()>=(page-1)*pageNum-1)
					{
						getDateFromNet();
					}
				}
				
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void getDateFromNet()
	{
		//这里模拟加载到10条数据
		if(page*pageNum+pageNum>list.length)
			hasMore=false;
		
		for(int i=page*pageNum;i<page*pageNum+pageNum;i++)
		{
			if(i<list.length){
			Bean b=new Bean();
			b.url=list[i];
				b.name=i+"";
				adapterList.add(b);
			}
		}
		
		adapter.notifyDataSetChanged();
		
		//获取十条数据后 就可以下载图片
		Map<String, Integer> urls=new HashMap<String, Integer>();
		for(int i=page*pageNum;i<adapterList.size();i++)
		{
			if(cache.get(adapterList.get(i).url)!=null)
			{
				continue;
			}
			
			urls.put(adapterList.get(i).url, i);
			
		}
		page++;
		downloader.downImage(urls, 8000, 5000, LV_TAG);
	}
	
	
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return adapterList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			View v;
			Holder h;
			
			if(convertView!=null)
			{
				v=convertView;
				h=(Holder) v.getTag();
			}else{
				v=View.inflate(Simple.this, R.layout.lv_item, null);
				h=new Holder();
				v.setTag(h);
				h.iv=(ImageView) v.findViewById(R.id.iv);
				h.name=(TextView) v.findViewById(R.id.name);
			}
			
			h.name.setText(adapterList.get(position).name);
			h.iv.setTag(position);
			if(cache.get(adapterList.get(position).url)!=null)
			{
				
				h.iv.setImageBitmap(cache.get(adapterList.get(position).url));
			}
			
		
			
			return v;
		}
		
		
	}
	
	class Holder{
		TextView name;
		ImageView iv;
	}
	
	
	private class Bean{
		String name;
		String url;
	}
	/**
	 * 120 多张图片 
	 */
	private String[] list={
		"http://img1.imgtn.bdimg.com/it/u=3523220886,881949335&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=3684610274,2744906073&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=3232737637,1519026289&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3129628661,3734455477&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=642411846,3567124587&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=742285469,2652882407&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=3095740640,1409058133&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2139548076,1901533372&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=323561353,2264204692&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2015365735,1773884189&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=2441017954,3864301040&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2372115625,1848895251&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=149134494,3498780640&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=2625778401,1719214400&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2604603518,2673352484&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2407247982,3235949379&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=1118746248,1654924701&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=1977768959,2714856436&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=3449295510,3688965015&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=2272101223,1202858670&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=730913833,54121875&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=3320233258,3085081821&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2160858985,544543679&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=777819074,826141302&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=3626668618,4121239399&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=2252749316,186403830&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=678168118,2909510647&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2737673885,4139879944&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2361517975,1192352140&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2381895387,1244728669&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3614565509,3825662179&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=4102242134,1274923616&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=1789599760,938689816&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2302038083,2406294273&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=310036661,1012389079&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=4040962600,4223129733&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=3549967563,3105240580&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=1618972266,1720415767&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=3594547672,1913067504&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2920743823,2802550622&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2662801057,3520412289&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=62764155,2749483625&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=153710717,2400411738&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=640468503,1719585868&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2358286549,3512447199&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=1839753494,2548054359&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=802861827,3780912632&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=4167198230,2840569088&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3220812924,2697520542&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=1745039071,2905928481&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=32003136,1665732172&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=1795509190,3402425462&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=2575227605,2898892958&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=4129769738,1391496019&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=648916614,2814651223&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=2129136949,679661942&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=921241765,1709893154&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=168165524,2353498517&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2201994020,1815232526&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=1306991356,3023188962&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=3523220886,881949335&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=3684610274,2744906073&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=3232737637,1519026289&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3129628661,3734455477&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=642411846,3567124587&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=742285469,2652882407&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=3095740640,1409058133&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2139548076,1901533372&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=323561353,2264204692&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2015365735,1773884189&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=2441017954,3864301040&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2372115625,1848895251&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=149134494,3498780640&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=2625778401,1719214400&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2604603518,2673352484&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2407247982,3235949379&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=1118746248,1654924701&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=1977768959,2714856436&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=3449295510,3688965015&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=2272101223,1202858670&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=730913833,54121875&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=3320233258,3085081821&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2160858985,544543679&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=777819074,826141302&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=3626668618,4121239399&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=2252749316,186403830&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=678168118,2909510647&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2737673885,4139879944&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2361517975,1192352140&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2381895387,1244728669&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3614565509,3825662179&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=4102242134,1274923616&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=1789599760,938689816&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2302038083,2406294273&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=310036661,1012389079&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=4040962600,4223129733&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=3549967563,3105240580&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=1618972266,1720415767&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=3594547672,1913067504&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=2920743823,2802550622&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2662801057,3520412289&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=62764155,2749483625&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=153710717,2400411738&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=640468503,1719585868&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=2358286549,3512447199&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=1839753494,2548054359&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=802861827,3780912632&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=4167198230,2840569088&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=3220812924,2697520542&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=1745039071,2905928481&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=32003136,1665732172&fm=21&gp=0.jpg",
		"http://img2.imgtn.bdimg.com/it/u=1795509190,3402425462&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=2575227605,2898892958&fm=21&gp=0.jpg",
		"http://img5.imgtn.bdimg.com/it/u=4129769738,1391496019&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=648916614,2814651223&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=2129136949,679661942&fm=21&gp=0.jpg",
		"http://img3.imgtn.bdimg.com/it/u=921241765,1709893154&fm=21&gp=0.jpg",
		"http://img1.imgtn.bdimg.com/it/u=168165524,2353498517&fm=21&gp=0.jpg",
		"http://img4.imgtn.bdimg.com/it/u=2201994020,1815232526&fm=21&gp=0.jpg",
		"http://img0.imgtn.bdimg.com/it/u=1306991356,3023188962&fm=21&gp=0.jpg"
		
	};
	
}
