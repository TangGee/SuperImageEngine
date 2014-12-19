package com.ym.superimageengine.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * �첽�����ĳ�����
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
	  * execute�Ĵ�����ÿ�����󶼻��ж�����
	  * @param params
	  * @return
	  */
	public final AsyncTask<Params, Progress, Result> executeProxy(Params... params) {
		 if(NetUtils.cheakUtils(context))
		 {
			return super.execute(params);
			 
		 }else{
			 Toast.makeText(context, "û������", 0).show();
		 }
		 return  null;
	    
	 }

}

