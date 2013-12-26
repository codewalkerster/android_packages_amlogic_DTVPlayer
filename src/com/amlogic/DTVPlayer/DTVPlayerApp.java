package com.amlogic.DTVPlayer;

import android.os.*;
import android.app.*;
import android.content.*;
import android.app.Application;
import android.app.Activity;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.amlogic.tvclient.TVClient;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVMessage;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.Process; 

public class DTVPlayerApp extends Application {
	private static final String TAG="DTVPlayerApp";
	private List<Activity> activities = new ArrayList<Activity>();  
		
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,"------DTV Application create-----");
		//dealHomeKey();
	}

	public void addActivity(Activity activity) {  
		activities.add(activity);  
	}  

	@Override  
    	public void onTerminate() {  
    		Log.d(TAG,"------DTV Application onTerminate-----");
	        for (Activity activity : activities) {  
	            activity.finish();  
	        }  

		 super.onTerminate();
    	}  

	public void dealHomeKey(){
		new Thread(new Runnable(){
			@Override
			public void run()
			{
			
			BufferedReader bufferedReader = null;
			try
			{
			
				Process logcatProcess = Runtime.getRuntime().exec(new String[] {"logcat","ActivityManager:I *:S"});
				bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));

				String line;

				while ((line = bufferedReader.readLine()) != null)
				{
					if (line.indexOf("cat=[android.intent.category.HOME]") > 0)
					{
						Runtime.getRuntime().exec("logcat -c"); 
						Log.d(TAG,"HOME KEY DEAL");
						onTerminate();						
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			}
		}).start();
	}

}
