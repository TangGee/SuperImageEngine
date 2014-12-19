package com.ym.superimageengine.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * 异步操作的抽象类
 * 
 */
public abstract class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
	private Context context;
	
	
	
	
	
	 public MyAsyncTask(Context context) {
		 super();
		 this.context=context;
	}





	 /**
	  * execute的代理方法每次请求都会判断网络
	  * @param params
	  * @return
	  */
	public final AsyncTask<Params, Progress, Result> executeProxy(Params... params) {
		 if(NetUtils.cheakUtils(context))
		 {
			return super.execute(params);
			 
		 }else{
			 Toast.makeText(context, "没有网络", 0).show();
		 }
		 return  null;
	    
	 }

}

