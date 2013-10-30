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

public class ATSCScanResult extends DTVActivity{

	private final String TAG = "ATSCScanResult";
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
		TVProgram mTVProgram = TVProgram.selectByNameAndServiceId(ATSCScanResult.this,name,serviceid,serviceType);

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

		
		Bundle bundle = this.getIntent().getExtras(); 
		if(bundle!=null){
	    		if(bundle.getString("scan-mode").equals("atsc-auto-scan")){
				DTVScanATSC_StartAutoScan();
			}	
			else if(bundle.getString("scan-mode").equals("atsc-manual-scan")){
				DTVScanATSC_StartManuScan(bundle.getInt("scan-atsc-mod",TVChannelParams.MODULATION_VSB_8),bundle.getInt("scan-fre",474000));
			}
		}	
		
	}

	private void DTVScanATSC_StartAutoScan(){
		TVScanParams sp;	
		if(DTVGetATSCAtvEnable())
			sp = TVScanParams.adtvScanParams(0, TVChannelParams.MODE_ATSC);
		else
			sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_ATSC);
		startScan(sp);
	}

	private void DTVScanATSC_StartManuScan(int  dvbscanatsc_manu_modulation,int dvbscanatsc_manu_freq){
		TVScanParams sp;	
	
		switch(dvbscanatsc_manu_modulation)
		{
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_AUTO:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_AUTO));
					break;
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_16:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_16));
					break;
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_32:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_32));
					break;			
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_64:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_64));
					break;	
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_128:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_128));
					break;	
				case DTVSettingsMenu.SETTINGS_MODULATION_QAM_256:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_QAM_256));
					break;					
				case DTVSettingsMenu.SETTINGS_MODULATION_VSB_8:	
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_VSB_8));
					break;
				case DTVSettingsMenu.SETTINGS_MODULATION_VSB_16:	
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_VSB_16));
					break;
				default:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000, TVChannelParams.MODULATION_VSB_8));
					break;
		}
		
		startScan(sp);
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
	}

	public void onMessage(TVMessage msg){
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_SCAN_PROGRESS:
				String locked;
				if (msg.getScanCurChanLockStatus()!=0) {
					locked = "Locked!";
				} else {
					locked = "Unlocked!";
				}

				Log.d(TAG, "Scan update: frequency "+msg.getScanCurChanParams().getFrequency()+ " " + locked +
					", Channel "+(msg.getScanCurChanNo()+1)+"/"+msg.getScanTotalChanCount()+
					", Percent:"+msg.getScanProgress()+"%");

				progressBar.setProgress(msg.getScanProgress());
				progress_value.setText(msg.getScanProgress()+"%");
				ts_info.setText(msg.getScanCurChanParams().getFrequency()/1000+"KHz");
				
				if (msg.getScanProgramName() != null) {
					Log.d(TAG, "Scan update: new program >> "+ msg.getScanProgramName());
					process_event_scan_service(msg);
				}
				
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
				progress_value.setText("100%");				
				Log.d(TAG, "stopScan End");				
				break;
			default:
				break;
	
		}
	}

	protected void onStart(){
		super.onStart();
		sync_flag=false;
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
				mTvListAdapter = new ScanResultAdapter(ATSCScanResult.this,tv_list);
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

	private void showReturnDia(){
		new SureDialog(ATSCScanResult.this,true){
			public void onSetMessage(View v){
				((TextView)v).setText("Stop scan?");
			}
			public void onSetNegativeButton(){
	  
			}
			public void onSetPositiveButton(){
				stopScan(true);
			
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
		//Intent intent = new Intent();
		//intent.setClass(ATSCScanResult.this, DTVSettingsMenu.class);
		//startActivity(intent);
		ATSCScanResult.this.finish();
	}
}

