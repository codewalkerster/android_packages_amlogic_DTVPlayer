package com.amlogic.DTVPlayer;

import android.util.Log;
import android.os.Bundle;
import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;

import java.util.*;
import java.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;

import java.io.InputStream;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NameList;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.app.ProgressDialog;
import android.os.Looper;
import android.os.Message;

import com.amlogic.DTVPlayer.R;
import com.amlogic.widget.CheckUsbdevice;

public class DTVScanDvbsDBManagement extends DTVActivity {
	private final String TAG = "DTVScanDvbsDBManagement";
	private ListView mListView=null;
	private static String[] DATA = null;
	private Toast toast=null;
	private View dvbs_dbm_settings;
	int m_count = 0;
	ProgressDialog  m_pDialog;

	@Override
  	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.dtvsettings); 
		DTVScanDvbsDBManagement_UIInit();
	}
	
	public void onConnected(){
		Log.d(TAG, "connected");
		DTVScanDvbsDBManagement_UIInit();
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
	}

	public void onMessage(TVMessage msg){
		Log.d(TAG, "message "+msg.getType());
		switch (msg.getType()) {
			case TVMessage.TYPE_SCAN_PROGRESS:
				
				break;
			case TVMessage.TYPE_SCAN_STORE_BEGIN:
				Log.d(TAG, "Storing ...");
				break;
			case TVMessage.TYPE_SCAN_STORE_END:
				Log.d(TAG, "Store Done !");
				
				break;
			case TVMessage.TYPE_SCAN_END:
				Log.d(TAG, "Scan End");
				break;
			default:
				break;
	
		}
	}

	private void DTVScanDvbsDBManagement_UIInit(){
		TextView title  = (TextView)this.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(R.string.dvbs_unicable_config);

		mListView = (ListView)this.findViewById(R.id.settings_list);
		//list.setOnItemSelectedListener(mOnSelectedListener);
		mListView.setAdapter(new DvbsDBMAdapter(this,null));

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				switch(arg2){
					case 0:
						dealLoadSatelliteXML();	
						break;
					case 1:     //DB management
						dealBackupSatelliteXML();
						break;
				}
			}
        });
		
		mListView.requestFocus();
		//mDvbsHelper = getMyDBHelper();
		m_pDialog = new ProgressDialog(DTVScanDvbsDBManagement.this);	
	}


	/*
	private  DvbsDBHelper mDvbsHelper = null;
	private DvbsDBHelper getMyDBHelper(){
		Context context;
		if(mDvbsHelper==null){
	            mDvbsHelper = new DvbsDBHelper(getApplicationContext());
		}
		return mDvbsHelper;
	}
	*/

	
	private AlertDialog.Builder satXmlBuilder;
	private View dvbs_satxml;
	private void showSatellitesDB(List<String> list){
		final List<String> filelist = list;	
	
		satXmlBuilder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);  

		dvbs_satxml = layoutInflater.inflate(R.layout.dtvsettings, null); 
		satXmlBuilder.setTitle(R.string.dvbs_dbm_load);

		ListView mListView =(ListView)dvbs_satxml.findViewById(R.id.settings_list);
		mListView.setAdapter(new DvbsDBXmlAdapter(this,filelist));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				final String path = filelist.get(arg2);
				/*
				getContentResolver().query(DVBClient.IMPORT_DB,
					null, path, null, null);
				*/
				toast = Toast.makeText(
						DTVScanDvbsDBManagement.this,
						R.string.xml_load_success,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
				
			}
        	    
        });
			
		mListView.requestFocus();
		
		satXmlBuilder.setView(dvbs_satxml);

		AlertDialog alert = satXmlBuilder.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
								
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							
							}         
							});	

		alert.show();	
		
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
	}

	private void dealLoadSatelliteXML(){
		
		CheckUsbdevice Usbdevice = new CheckUsbdevice(this);

		if(Usbdevice.getDevice()==null)
		{
			toast = Toast.makeText(
			DTVScanDvbsDBManagement.this,
			R.string.check_usb_device,
			Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		else
		{
			//read satellite.amdb 

			List<String> filenameList = Usbdevice.getSatellitesDBFileList();
			if(filenameList == null){
			}
				
			showSatellitesDB(filenameList);	
			
		}	

	}

	private void dealBackupSatelliteXML(){
		
		CheckUsbdevice Usbdevice = new CheckUsbdevice(this);
		String satellites_db = null;

		satellites_db = Usbdevice.getDevice();
		
		if(satellites_db==null){
			toast = Toast.makeText(
			DTVScanDvbsDBManagement.this,
			R.string.check_usb_device,
			Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return;
		}
		else{
			Date date = new Date(getUTCTime()*1000 ); 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); 
			String today = sdf.format(date); 

			//this.getContentResolver().query(DVBClient.EXPORT_DB,
							//null, satellites_db+"/"+"satellites_"+today+".amdb", null, null);
							
			toast = Toast.makeText(
				DTVScanDvbsDBManagement.this,
				R.string.xml_generator_success,
				Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
		}	

	}

	private static class DvbsDBMAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		
		private Context cont;
		private String[] listItems;

	 	static class ViewHolder {
			ImageView icon;
			TextView text;
		}
	
		public DvbsDBMAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
		
		}

		public int getCount() {

			//return listItems.length;
			return 2;
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
			   convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);

			   holder = new ViewHolder();
			   
			   holder.text = (TextView) convertView.findViewById(R.id.text);
			   holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			 
			   convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			switch(position){
				case 0:
					//holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_dbm_load);
					break;
				case 1:
					// holder.icon.setImageBitmap(mIcon2);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_dbm_backup);
					break;	
			}
			  
			return convertView;
		}
	}


	private  class DvbsDBXmlAdapter extends BaseAdapter {
		private LayoutInflater mInflater;		
		private Context cont;
		private List<String> listItems;

		class ViewHolder {
			ImageView icon;
			TextView text;
		}

		public DvbsDBXmlAdapter(Context context, List<String> list) {
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);		   
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(listItems.get(position));
			return convertView;
		}
	}

	private void showProgressDialog(){
		m_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_pDialog.setTitle("Please wait...");
		m_pDialog.setMessage("Please wait...");
		m_pDialog.setIcon(R.drawable.icon);
		m_pDialog.setProgress(0);
		m_pDialog.setMax(100);
		m_pDialog.setIndeterminate(false);
		m_pDialog.setCancelable(true);
		m_pDialog.show();
		
		new Thread(){
			public void run(){
				while(m_count <=100){
					m_pDialog.setProgress(m_count++);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				m_pDialog.cancel();
			}
		}.start();
	}

}

  
