package com.ym.superimageengine.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;

 /**
  * 这是一个 用 软引用构建的高速缓存类 与该项目无关 喜欢的尽管拿去用 
  * @author s0ng
  *
  * @param <K>
  * @param <V>
  */
public class CacheMap<K,V> extends HashMap<K, V>{
	
	private HashMap<K,SoftValue> cache=new HashMap<K,SoftValue>();
	private ReferenceQueue<V> queue=new ReferenceQueue<V>();
	
	
	
	public void clearSr() {

		SoftValue<K, V>   s=(SoftValue<K, V> ) queue.poll();

		while(s!=null)
		{
			cache.remove(s);
			s=(SoftValue<K, V> ) queue.poll();;
		}
	}
	
	
	@Override
	public V put(K key, V value) {

		SoftValue<K, V>  sf=new SoftValue(key, value, queue);
		cache.put(key, sf);
		
		return null;
	}
	
	@Override
	public V get(Object key) {
		
		clearSr();
		
		SoftValue<K, V> sr = cache.get(key);
		if (sr != null) {
			// 垃圾回收器清除，则此方法将返回 null。
			return sr.get();
		}

		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return get(key)!=null;
	}
	
	@Override
	public V remove(Object key) {
		return super.remove(key);
	}
	
	
	
	
	private class SoftValue<K, V>  extends SoftReference<V>
	{
		public SoftValue(K key,V r, ReferenceQueue<? super V> q) {
			super(r, q);
			this.key=key;
			
		}

		public Object key;
	}

}
