package com.amlogic.DTVPlayer;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.content.ComponentName;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.Display;
import android.widget.AdapterView;
import android.widget.EditText;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;
import android.view.Gravity;
import android.os.SystemProperties;
import android.os.SystemClock;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.widget.*;
import android.media.AudioManager;
import android.view.View.OnKeyListener;

import java.io.IOException;
import java.io.File;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.app.ActivityManager;

import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannel;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVEvent;
import com.amlogic.tvutil.DTVPlaybackParams;
import com.amlogic.tvutil.DTVRecordParams;
import com.amlogic.tvutil.TVSatellite;
import com.amlogic.tvutil.TVSatelliteParams;
import com.amlogic.widget.SureDialog;

public class DvbsScanResult extends DTVActivity{

	private final String TAG = "DvbsScanResult";
	private static final int DVB_SEARCH_MODULES= 18;
	private static  SharedPreferences mLast = null;
	private ListView listview;
	private static String[] DATA = null;

	private Toast toast=null;
	private View dvbs_dbm_settings;
	ProgressDialog  m_pDialog;

	TextView sat_info=null;
	TextView ts_info=null;
	ProgressBar progressBar=null;
	TextView progress_value=null;

	TextView tv_title=null;
	TextView radio_title=null;

	private ListView tvlistview;
	private ListView radiolistview;

	private ScanResultAdapter mTvListAdapter=null;
	private ScanResultAdapter mRadioListAdapter=null;

	private ArrayList<Object> tp_list=null;
	private ArrayList<Object> tv_list=null;
	private ArrayList<Object> tv_list_temp=null;
	private ArrayList<Object> radio_list=null;

	private int mSatScanCount=0;
	boolean scan_ok_flag = false;
	boolean canplay_flag = false;
	boolean sync_flag=false;
	
	@Override
  	public void onCreate(Bundle savedInstanceState) {
	  		super.onCreate(savedInstanceState);
			setContentView(R.layout.dvbs_scan_result_main);
			
			Context otherAppsContext = null;
			try
			{
				otherAppsContext = createPackageContext(
				"com.amlogic.DTVPlayer", Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
			}
			catch (NameNotFoundException e)
			{
			}
			mLast= PreferenceManager.getDefaultSharedPreferences(otherAppsContext);

			tv_title= (TextView)this.findViewById(R.id.tv);
			radio_title=(TextView)this.findViewById(R.id.radio);

			Bundle bundle = this.getIntent().getExtras();
			if(bundle!=null)
			{
		    		if(bundle.getString("scan_mode").equals("blind")){
					tv_title.setText(R.string.scan_ts);	
					tv_title.setVisibility(View.VISIBLE);
					radio_title.setVisibility(View.INVISIBLE);
				}		
			}	

			sat_info = (TextView)this.findViewById(R.id.sat_info);
			ts_info = (TextView)this.findViewById(R.id.ts_info);
			progressBar = (ProgressBar)this.findViewById(R.id.ProgressBar);
			progress_value = (TextView)this.findViewById(R.id.progress_value);
			
			tvlistview = (ListView)this.findViewById(R.id.tv_list);
	 		radiolistview =  (ListView)this.findViewById(R.id.radio_list);

			if(tv_list==null){
				tv_list = new ArrayList<Object>();
				tv_list_temp = tv_list;
			}

			if(tp_list==null){
				tp_list = new ArrayList<Object>();
			}	

			if(radio_list==null)
				radio_list = new ArrayList<Object>();

			mTvListAdapter = new ScanResultAdapter(this,tv_list_temp);
			mRadioListAdapter = new ScanResultAdapter(this,radio_list);

			tvlistview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					System.out.println("onItemSelected arg0 " + arg0);
					System.out.println("onItemSelected arg1 " + arg1);
					System.out.println("onItemSelected arg2 " + arg2);
					System.out.println("onItemSelected arg3 " + arg3);
					/*	
					if(((DbSat)tv_list.get(arg2)).getSelectedFlag()==false){
						((DbSat)satAndtsInfoList.get(arg2)).setSelectedFlag(true);
						image_cur.setBackgroundResource(R.drawable.set_channel_search);
				
					}	
					else{
						((DbSat)satAndtsInfoList.get(arg2)).setSelectedFlag(false);
						image_cur.setBackgroundResource(0);
					}
					*/
					if(canplay_flag==true)
						playProgram(1,arg2);
					
				}
	        	    
	        	});

			radiolistview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					System.out.println("onItemSelected arg0 " + arg0);
					System.out.println("onItemSelected arg1 " + arg1);
					System.out.println("onItemSelected arg2 " + arg2);
					System.out.println("onItemSelected arg3 " + arg3);
					if(canplay_flag==true)
						playProgram(2,arg2);
				}
	        	    
	        	});	
			tvlistview.setAdapter(mTvListAdapter);
			radiolistview.setAdapter(mRadioListAdapter);

			mSatScanCount = 0;

 	 }


	private class serviceInfo {
		private String name;
		private int id;
		private int serviceType;

		private int serviceId;
		private int tsId;
		private int frequence;
		private String satelliteName;

		private String getSatName() {
			return satelliteName;
		}
		private void setSatName(String name) {
			this.satelliteName = name;
		}
			
		private int getServiceId() {
			return this.serviceId;
		}
		private void setServiceId(int serviceId) {
			this.serviceId = serviceId;
		}

		private int getServiceType() {
			return serviceType;
		}
		private void setServiceType(int serviceType) {
			this.serviceType = serviceType;
		}
			
		private String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
		
	}

	private int service_get_dbId(String name, int serviceid, int serviceType){
		int dbId = -1;

		Log.d(TAG,"name="+name+"---serviceid="+serviceid+"-----serviceType="+serviceType);
		TVProgram mTVProgram = TVProgram.selectByNameAndServiceId(DvbsScanResult.this,name,serviceid,serviceType);

		if(mTVProgram!=null)
			return mTVProgram.getID();
		else
			return dbId;
	}

	
	private void playProgram(int type,int pos){
		serviceInfo serviceinfo=null;	
		if(type==1)	
			serviceinfo = (serviceInfo)tv_list.get(pos);
		else if(type ==2)
			serviceinfo = (serviceInfo)radio_list.get(pos);
		
		int db_id=-1;

		System.out.println("enter playProgram!!!!!!!");
		if(serviceinfo!=null)
			db_id = service_get_dbId(serviceinfo.getName(), serviceinfo.getServiceId(),serviceinfo.getServiceType());

		if (db_id == -1||((serviceinfo.getServiceType() != 2) && (serviceinfo.getServiceType() != 1))){
			Log.d(TAG,"enter playProgram error!");
			playValid();
		}

		Bundle bundle1 = new Bundle();  
		bundle1.putInt("service_type", serviceinfo.getServiceType());	            	      
		
		bundle1.putInt("db_id", db_id);
		bundle1.putString("activity_tag","DTVChannelList");

		Intent in = new Intent(Intent.ACTION_MAIN);  
		in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		in.putExtras(bundle1);
		ComponentName comp = new ComponentName("com.amlogic.DTVPlayer","com.amlogic.DTVPlayer.DTVPlayer");
		in.setComponent(comp);
		startActivity(in);  
		this.finish();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();

		if(DTVScanDvbsConfig.getDefaultList()!=null){
			Log.d(TAG,"####"+DTVScanDvbsConfig.getDefaultList().size());	
			for(int i=0;i<DTVScanDvbsConfig.getDefaultList().size();i++){
				TVSatellite sat_para = DTVScanDvbsConfig.getDefaultList().get(i).sat;
				Log.d(TAG,"sat name:"+DTVScanDvbsConfig.getDefaultList().get(i).sat.sat_name);

				ArrayList<TVChannelParams> ts_list=DTVScanDvbsConfig.getDefaultList().get(i).ts_list;	

				if(ts_list!=null){
					Log.d(TAG,"ts number:"+ts_list.size());
					for(int j=0;j<ts_list.size();j++){
						TVChannelParams ts_node = ts_list.get(j);
						Log.d(TAG,"ts node fre:"+ts_node.frequency);
					}
				}		
			}
		}

		String mode = mLast.getString("scan_mode","default");
		String crypted = mLast.getString("scan_mode_crypted","all");
		String service_mode = mLast.getString("scan_service_mode","all");
		Log.d(TAG,"@mode="+mode+"@crypted="+crypted+"@service_mode="+service_mode);

		if(mLast.getString("scan_mode","default").equals("default")){
			
			if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
				while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
						if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
							break;
						else
							mSatScanCount++;
					}
				
				if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){										
					TVScanParams sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_QPSK, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list);
					sp.setDtvOptions(getScanOptions());
					
					
					startScan(sp);						 
					sat_info.setText(String.valueOf(mSatScanCount+1)+"/"+String.valueOf(DTVScanDvbsConfig.getDefaultList().size())+"						"+DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.sat_name);
				}
			}	
		}
		else if(mLast.getString("scan_mode","default").equals("network")){
			if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()) {
				while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
						if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
							break;
						else
							mSatScanCount++;
					}

				if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
					
					TVScanParams sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_QPSK, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list);
					sp.setDtvOptions(getScanOptions());
					startScan(sp);		
					sat_info.setText(String.valueOf(mSatScanCount+1)+"/"+String.valueOf(DTVScanDvbsConfig.getDefaultList().size())+"						"+DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.sat_name);
					}
				}
		}
		else if(mLast.getString("scan_mode","default").equals("blind")){
			if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()) {
				TVScanParams sp = TVScanParams.dtvBlindScanParams(0, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.getParams(), TVChannelParams.MODE_QPSK);
				Log.d(TAG, "Start Scan...dvb-s dtvManualScanParams");
				sp.setDtvOptions(getScanOptions());
				startScan(sp);		

				tv_list_temp = tp_list;
				mTvListAdapter = new ScanResultAdapter(DvbsScanResult.this,tv_list_temp);
				tvlistview.setAdapter(mTvListAdapter);
			}
			sat_info.setText(String.valueOf(mSatScanCount+1)+"/"+String.valueOf(DTVScanDvbsConfig.getDefaultList().size())+"	"+DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.sat_name);	
		}
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
	}

	public void onMessage(TVMessage msg){
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_SCAN_PROGRESS:
				process_event_scan_service(msg);
				break;
			case TVMessage.TYPE_SCAN_STORE_BEGIN:
				Log.d(TAG, "Storing ...");
				break;
			case TVMessage.TYPE_SCAN_STORE_END:
				
				Log.d(TAG, "Store Done !");
				break;
			case TVMessage.TYPE_SCAN_END:
				Log.d(TAG, "Scan End");
				Log.d(TAG, "stopScan");
				stopScan(true);
				
				progressBar.setProgress(100);
				progress_value.setText(String.valueOf(100)+"%");
				ts_info.setText(getString(R.string.tv)+" : "+String.valueOf(tv_list.size())+"        "+getString(R.string.radio)+" : "+String.valueOf(radio_list.size()));	
			
				mSatScanCount++;
				if(mSatScanCount <DTVScanDvbsConfig.getDefaultList().size()){
					
					 progressBar.setProgress(0);
				
					 if(mLast.getString("scan_mode","default").equals("blind")){	
						TVScanParams sp = TVScanParams.dtvBlindScanParams(0, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.getParams(), TVChannelParams.MODE_QPSK);	
						sp.setDtvOptions(getScanOptions());
						startScan(sp);		

						tv_list_temp = tp_list;
						mTvListAdapter = new ScanResultAdapter(this,tv_list_temp);
						tvlistview.setAdapter(mTvListAdapter);
						mTvListAdapter.notifyDataSetChanged();
						tv_title.setText(R.string.scan_ts);	
						tv_title.setVisibility(View.VISIBLE);
						radio_title.setVisibility(View.INVISIBLE);
						radiolistview.setVisibility(View.INVISIBLE);
					 }
					else if(mLast.getString("scan_mode","default").equals("default")){
						while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
							if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
								break;
							else
								mSatScanCount++;
						}

						if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
							TVScanParams sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_QPSK, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list);
							sp.setDtvOptions(getScanOptions());
							startScan(sp);		
						}
						
					}
					else if(mLast.getString("scan_mode","default").equals("network")){
						while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
							if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
								break;
							else
								mSatScanCount++;
						}

						if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
							TVScanParams sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_QPSK,DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list);
							sp.setDtvOptions(getScanOptions());
							startScan(sp);	
						}
					}
					sat_info.setText(String.valueOf(mSatScanCount+1)+"/"+String.valueOf(DTVScanDvbsConfig.getDefaultList().size())+"	              "+DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.sat_name);
				}
				
				scan_ok_flag = true;
				canplay_flag = true;
				break;
			case TVMessage.TYPE_BLINDSCAN_PROGRESS:
				Log.d(TAG, "Blind Scan Progress:" + msg.getScanProgress() + " Blind freq polar lof:" + msg.getScanMsg());
				progressBar.setProgress(msg.getScanProgress());
				Log.d(TAG,""+msg.getScanProgress());
				progress_value.setText(String.valueOf(msg.getScanProgress())+"%");
				ts_info.setText(msg.getScanMsg());
				break;				
			case TVMessage.TYPE_BLINDSCAN_NEWCHANNEL:
				Log.d(TAG, "Blind Scan New Channel: frequency " + msg.getScanCurChanParams().getFrequency() + " symb " + 
				msg.getScanCurChanParams().getSymbolRate() + " polar " + msg.getScanCurChanParams().getPolarisation());

				
				String strMsg = "" + msg.getScanCurChanParams().getFrequency()/1000 + "MHz    ";
				strMsg += "" + msg.getScanCurChanParams().getSymbolRate()/1000 + "kS/s    ";
				strMsg += msg.getScanCurChanParams().getPolarisation()==0 ? "H" : "V";

				serviceInfo serviceinfo = new serviceInfo();
				serviceinfo.setName(strMsg);	
				tp_list.add(serviceinfo);
				tvlistview.setSelection(tp_list.size() -1);							
				mTvListAdapter.notifyDataSetChanged();

				break;
			case TVMessage.TYPE_BLINDSCAN_END:
				Log.d(TAG, "Blind Scan End");
				tv_list_temp=tv_list;
				mTvListAdapter = new ScanResultAdapter(DvbsScanResult.this,tv_list);
				tvlistview.setAdapter(mTvListAdapter);
				mTvListAdapter.notifyDataSetChanged();
				radiolistview.setVisibility(View.VISIBLE);
				mRadioListAdapter.notifyDataSetChanged();
				tv_title.setText(R.string.tv);
				tv_title.setVisibility(View.VISIBLE);
				radio_title.setVisibility(View.VISIBLE);
				break;			
			default:
				break;
	
		}
	}

	private int getScanOptions(){
		int options = 0;
	 	if (mLast.getString("scan_mode_crypted","all").equals("fta")) {
	 		options |= TVScanParams.DTV_OPTION_FTA;
	 	}

		Log.d(TAG, "scan service mode " + mLast.getString("scan_service_mode","all"));
	 	if (mLast.getString("scan_service_mode","all").equals("tv")) {
	 		options |= TVScanParams.DTV_OPTION_NO_RADIO;
	 	} else if (mLast.getString("scan_service_mode","all").equals("radio")) {
	 		options |= TVScanParams.DTV_OPTION_NO_TV;
	 	}

		Log.d(TAG, "options "+options);
		return options;
	}

	/*
	private void ResultOnEvent(DVBEvent evt) {
		Log.d( TAG , "EVT: " + Integer.toString(evt.type,10) + "msg" + evt.msg);
		switch(evt.type){
			case DVBEvent.EVENT_SCAN_SERVICE:
				  
				break;
			case DVBEvent.EVENT_SCAN_PROGRESS:
				progressBar.setProgress(evt.value);
				Log.d(TAG,""+evt.msg);
				progress_value.setText(String.valueOf(evt.value)+"%");
				ts_info.setText(evt.msg);
				if(evt.value==100){
					ts_info.setText(getString(R.string.tv)+" : "+String.valueOf(tv_list.size())+"        "+getString(R.string.radio)+" : "+String.valueOf(radio_list.size()));
						
				}
				break;

          		case DVBEvent.EVENT_SCAN_SIGNAL_INFO:	
			  	{
					System.out.println("ResultOnEvent msg " + evt.msg);
					String s = evt.msg;
					String array[]=s.split(" ");
					System.out.println("ResultOnEvent msg array " + array[3]);
					System.out.println("ResultOnEvent msg int " + Integer.parseInt(array[3]));
					int fre = Integer.parseInt(array[3]);
					//process_event_scan_signal_info(evt);
	          		}
          			break;
			case DVBEvent.EVENT_SCAN_ERROR:	
			case DVBEvent.EVENT_SCAN_OK:
				
           			mScanDvb.stopScan(true);
				mSatScanCount++;
				if(mSatScanCount <DTVScanDvbsConfig.getDefaultList().size()){
					
						progressBar.setProgress(0);
					
						 if(mLast.getString("scan_mode","default").equals("blind")){	
						 	int smode = DVBClient.SCAN_SAT_BLIND|DVBClient.SCAN_DVBS;
						 	if (mLast.getString("scan_mode_crypted","all").equals("fta")) {
						 		smode |= DVBClient.SCAN_FTA;
						 	}				
						 	if (mLast.getString("scan_service_mode","all").equals("tv")) {
						 		smode |= DVBClient.SCAN_TV;
						 	} else if (mLast.getString("scan_service_mode","all").equals("radio")) {
						 		smode |= DVBClient.SCAN_RADIO;
						 	}
							mScanDvb.startScan(DVBClient.SCAN_STD_DVB, smode,DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list, false,DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat);

							tv_list_temp = tp_list;
							//mTvListAdapter = new ScanResultAdapter(this,tv_list_temp);
							mTvListAdapter = new ScanResultAdapter(this,tv_list_temp);
							tvlistview.setAdapter(mTvListAdapter);
							mTvListAdapter.notifyDataSetChanged();
							tv_title.setText(R.string.scan_ts);	
							tv_title.setVisibility(View.VISIBLE);
							radio_title.setVisibility(View.INVISIBLE);
							radiolistview.setVisibility(View.INVISIBLE);
						 }
						else if(mLast.getString("scan_mode","default").equals("default")){
							/*
							int smode = DVBClient.SCAN_DVBS| DVBClient.SCAN_ALLBAND|(isUnicableOn()?DVBClient.SCAN_SAT_UNICABLE:0);
						 	if (mLast.getString("scan_mode_crypted","all").equals("fta")) {
						 		smode |= DVBClient.SCAN_FTA;
						 	}
						 	if (mLast.getString("scan_service_mode","all").equals("tv")) {
						 		smode |= DVBClient.SCAN_TV;
						 	} else if (mLast.getString("scan_service_mode","all").equals("radio")) {
						 		smode |= DVBClient.SCAN_RADIO;
						 	}
						 	*/
						 	/*
							while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
								if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
									break;
								else
									mSatScanCount++;
							}

							if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size())
								mScanDvb.startScan(DVBClient.SCAN_STD_DVB, smode, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list, false,DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat);
							
						}
						else if(mLast.getString("scan_mode","default").equals("network")){
							/*
							int smode = DVBClient.SCAN_DVBS|DVBClient.SCAN_AUTO|(isUnicableOn()?DVBClient.SCAN_SAT_UNICABLE:0);
						 	if (mLast.getString("scan_mode_crypted","all").equals("fta")) {
						 		smode |= DVBClient.SCAN_FTA;
						 	}
						 	if (mLast.getString("scan_service_mode","all").equals("tv")) {
						 		smode |= DVBClient.SCAN_TV;
						 	} else if (mLast.getString("scan_service_mode","all").equals("radio")) {
						 		smode |= DVBClient.SCAN_RADIO;
						 	}
						 	*/
						 	/*
							while(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size()){
								if(DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list!=null)
									break;
								else
									mSatScanCount++;
							}

							if(mSatScanCount<DTVScanDvbsConfig.getDefaultList().size())
								mScanDvb.startScan(DVBClient.SCAN_STD_DVB, smode, DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).ts_list, false,DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat);
						}
						sat_info.setText(String.valueOf(mSatScanCount+1)+"/"+String.valueOf(DTVScanDvbsConfig.getDefaultList().size())+"	              "+DTVScanDvbsConfig.getDefaultList().get(mSatScanCount).sat.name);
				}
				else{
										
				}
				
				scan_ok_flag = true;
				canplay_flag = true;
				
				break;
			case DVBEvent.EVENT_SCAN_BS_END:
				tv_list_temp=tv_list;
				mTvListAdapter = new ScanResultAdapter(DvbsScanResult.this,tv_list);
				tvlistview.setAdapter(mTvListAdapter);
				mTvListAdapter.notifyDataSetChanged();
				radiolistview.setVisibility(View.VISIBLE);
				mRadioListAdapter.notifyDataSetChanged();
				tv_title.setText(R.string.tv);
				tv_title.setVisibility(View.VISIBLE);
				radio_title.setVisibility(View.VISIBLE);
				break;
			case DVBEvent.EVENT_SCAN_BS_NEW_TP:
				System.out.println("ResultOnEvent msg " + evt.msg);
				String s = evt.msg;
				String array[]=s.split(" ", 3);
				if (array.length >= 3) {
					String strMsg = "" + Integer.parseInt(array[0])/1000 + "MHz    ";
					strMsg += "" + Integer.parseInt(array[1])/1000 + "kS/s    ";
					strMsg += Integer.parseInt(array[2])==0 ? "H" : "V";

					serviceInfo serviceinfo = new serviceInfo();
					serviceinfo.setName(strMsg);	
					tp_list.add(serviceinfo);
				}
				
				tvlistview.setSelection(tp_list.size() -1);
							
				mTvListAdapter.notifyDataSetChanged();
				break;
			case DVBEvent.EVENT_FEND_ROTOR_MOVING:
				ShowInformation(evt.msg);
				break;
			case DVBEvent.EVENT_FEND_ROTOR_STOP:
				HideInformation();
				break;		
		}
	}
	*/

	protected void onStart(){
		super.onStart();
		sync_flag=false;
	}

	/*
	private DVBUnicableSetting getUnicableSetting() {
		DVBUnicableSetting set = null;
		
		if (mScanDvb != null) {
			set = new DVBUnicableSetting();
			mScanDvb.getUnicableSetting(set);
		}
		return set;
	}
	*/
	
	private boolean isUnicableOn() {
		/*
		DVBUnicableSetting setting = getUnicableSetting();
		if (setting != null && setting.getUnicableOn())
			return true;
		*/	
		return false;
	}	
	
	private void process_event_scan_service(TVMessage msg){

		String locked;
		if (msg.getScanCurChanLockStatus()!=0) {
			locked = "Locked!";
		} else {
			locked = "Unlocked!";
		}

		Log.d(TAG, "Scan update: frequency "+msg.getScanCurChanParams().getFrequency()+ " " + locked +
			", Channel "+(msg.getScanCurChanNo()+1)+"/"+msg.getScanTotalChanCount()+
			", Percent:"+msg.getScanProgress()+"%");
		
		String name = 	msg.getScanProgramName();
		int service_type = msg.getScanProgramType();
		int service_id = 0;

		Log.d(TAG,"name="+name+"service_type="+service_type+"service_id"+service_id);

		if (service_type != 0x01 && service_type != 0x02){
			return ;
		}

		if(name!=null){
			serviceInfo serviceinfo = new serviceInfo();
			serviceinfo.setName(name);	
			serviceinfo.setServiceType(service_type);
			serviceinfo.setServiceId(service_id);
		
			if(service_type==1){			
				tv_list.add(serviceinfo);
				mTvListAdapter = new ScanResultAdapter(DvbsScanResult.this,tv_list);
				tvlistview.setAdapter(mTvListAdapter);
				mTvListAdapter.notifyDataSetChanged();
				tvlistview.setSelection(tv_list.size() -1);
			}
			else if(service_type==2){
				radio_list.add(serviceinfo);	
				radiolistview.setVisibility(View.VISIBLE);
				mRadioListAdapter.notifyDataSetChanged();
				radiolistview.setSelection(radio_list.size() -1);
			}
		}
		progressBar.setProgress(msg.getScanProgress());
		Log.d(TAG,""+msg.getScanProgress());
		progress_value.setText(String.valueOf(msg.getScanProgress())+"%");
		ts_info.setText(msg.getScanCurChanParams().getFrequency()/1000+"KHz"+ " " + locked);

	}
	
	
	private void process_event_scan_signal_info(TVMessage msg){
		/*
		String[] para = evt.msg.split(" ", 3);
		if (para.length >= 3){
			//sigalQuality = Integer.valueOf(para[0]).intValue();
			//ber = Integer.valueOf(para[1]).intValue();
			//strenth = Integer.valueOf(para[2]).intValue();
		}
		*/
	}

	private boolean showInformation_flag=false;
	void ShowInformation(String text)
	{		 
		if(showInformation_flag==false)
		{
			RelativeLayout layout =  (RelativeLayout)findViewById(R.id.information);
	
			TextView information_text = (TextView)findViewById(R.id.information_text);
			information_text.setText(text);
			layout.setVisibility(View.VISIBLE);
			showInformation_flag=true;
		}	
		else {
			TextView information_text = (TextView)findViewById(R.id.information_text);
			information_text.setText(text);
		}
	}

	void HideInformation()
	{
		if(showInformation_flag==true)
		{
			RelativeLayout layout =  (RelativeLayout)findViewById(R.id.information);
			layout.setVisibility(View.INVISIBLE);
			layout.setVisibility(View.GONE);
			showInformation_flag=false;
		}	
	}
	
	private int getTsId(int scan_id, int sat_id){
		/*
		Cursor cur=null;
		int n=0;
		DvbsDBHelper mDvbsHelper = new DvbsDBHelper(getApplicationContext());
		SQLiteDatabase db = mDvbsHelper.getReadableDatabase();
		Cursor cur_ts =db.query(mDvbsHelper.DB_TABLE_SCANLIST_TS_INFO, null, "sat_id="+sat_id+" and scan_id="+scan_id , null, null, null, "ts_id asc");	
		if(cur_ts==null){
			cur_ts.close();						
		}	
		
		n = cur_ts.getCount();
			
		if(n>0){	
			cur_ts.moveToLast();	
			int numColumn = cur_ts.getColumnIndex("ts_id");
			int  ts_id = cur_ts.getInt(numColumn);
			Log.d(TAG,"ts_id"+ts_id);
			cur_ts.close();
			db.close();
			return  ts_id+1;
		}				
		else	{
			cur_ts.close();
			db.close();
			return 0;
		}				
		*/
		return 0 ;
	}

	protected void onStop(){
		Log.d(TAG,"onStop");	
	
			stopScan(false);
		
		if(sync_flag==false){
			//mScanDvb.syncProgram(); 
			sync_flag=true;
		}	
		super.onStop();
	}

	private  class ScanResultAdapter extends BaseAdapter {
		private LayoutInflater mInflater;		
		private Context cont;
		private ArrayList<Object> listItems;

		class ViewHolder {
			TextView no;
			TextView text;
		}

		public ScanResultAdapter(Context context, ArrayList<Object> list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
		}

		public int getCount() {

			if(listItems!=null)
				return listItems.size();
			else
				return 0;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
	
	
		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		if (convertView == null) {
		   convertView = mInflater.inflate(R.layout.dvbs_channel_scan_item, null);
		   
		   holder = new ViewHolder();
		   
		   holder.text = (TextView) convertView.findViewById(R.id.text);
		   holder.no = (TextView) convertView.findViewById(R.id.number);
		 
		   convertView.setTag(holder);
		}else {
		  // Get the ViewHolder back to get fast access to the TextView
		  // and the ImageView.
		  holder = (ViewHolder) convertView.getTag();
		  }

		   holder.text.setText(((serviceInfo)(listItems.get(position))).getName());
		   holder.no.setText(String.valueOf(position+1));	
		  return convertView;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,">>>onDestroy<<<");
		super.onDestroy();
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		
		  case KeyEvent.KEYCODE_BACK:
		  	//android.os.Process.killProcess(android.os.Process.myPid());
                	//System.exit(0);
			if(scan_ok_flag==false)
				showReturnDia();
			else
				returnSettings();
			return true;
		  }
	
		return super.onKeyDown(keyCode, event);
	}


	/*
	private void showReturnDia(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(DvbsScanResult.this);
		builder.setMessage("Stop scan?")
		    .setCancelable(false)
		    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
				
			stopScan(false);
			
			if(sync_flag==false){
				//mScanDvb.syncProgram(); 
				sync_flag=true;
			}	
			canplay_flag = true;
			scan_ok_flag = true;
			returnSettings();
	            	dialog.dismiss();
	        }
	    })
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	        	 dialog.dismiss();
	        }
	    });
		AlertDialog alert = builder.create();
		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
									
								}         
								});	
		alert.show();

		//alert.getWindow().setLayout(500,-1);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

				
	}
	*/

	private void showReturnDia(){
		new SureDialog(DvbsScanResult.this,true){
			public void onSetMessage(View v){
				((TextView)v).setText("Stop scan?");
			}
			public void onSetNegativeButton(){
	  
			}
			public void onSetPositiveButton(){
				stopScan(false);
			
				if(sync_flag==false){
					//mScanDvb.syncProgram(); 
					sync_flag=true;
				}	
				canplay_flag = true;
				scan_ok_flag = true;
				returnSettings();
			}
		};
	}

	private void returnSettings(){
		Intent intent = new Intent();
		intent.setClass(DvbsScanResult.this, DTVScanDvbsConfig.class);
		startActivity(intent);
		DvbsScanResult.this.finish();
	}
}

	
