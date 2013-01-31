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
import com.amlogic.tvutil.TVConfigValue;

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.util.List;
import java.util.ArrayList;

public class DTVScanDVBT extends DTVActivity{
	private static final String TAG="DTVScanDVBT";

	public static final int DTVSCANDVBT_SETTING_SCAN_MODE = 0;
	public static final int DTVSCANDVBT_SETTING_MANU_SCAN_MODE = 1;
	public static final int DTVSCANDVBT_SCAN_MODE = 2;

	/*in DTVSCANDVBT_SETTING_SCAN_MODE*/
	public static final int SETTINGS_AUTO_SCAN = 0;
	public static final int SETTINGS_MANU_SCAN = 1;
	public static final int SETTINGS_AREA = 2;
	public static final int SETTINGS_LCN = 3;
	public static final int SETTINGS_MAX = 4;

	/*in DTVSCANDVBT_SETTING_MANU_SCAN_MODE*/
	public static final int SETTINGS_MANU_SCANMODE = 0;
	public static final int SETTINGS_SCAN_BAND = 1;
	public static final int SETTINGS_CHNO = 2;
	public static final int SETTINGS_FREQUENCY = 3;
	public static final int SETTINGS_BANDWIDTH = 4;
	public static final int SETTINGS_SCAN = 5;
	public static final int SETTINGS_MANU_MAX = 6;

	/*dvbt area*/
	public static final int SETTINGS_AREA_UK = 0;
	public static final int SETTINGS_AREA_AUSTRALIA = 1;
	public static final int SETTINGS_AREA_ITALY = 2;
	public static final int SETTINGS_AREA_FRANCE = 3;
	public static final int SETTINGS_AREA_TAIWAN = 4;
	public static final int SETTINGS_AREA_MAX = 5;

	/*dvbt manual scan mode*/
	public static final int SETTINGS_MANU_SCANMODE_FREQ = 0;
	public static final int SETTINGS_MANU_SCANMODE_CHAN = 1;

	/*dvbt manual band*/
	public static final int SETTINGS_MANU_SCANBAND_VHF = 0;
	public static final int SETTINGS_MANU_SCANBAND_UHF = 1;
	public static final int SETTINGS_MANU_SCANBAND_MAX = 2;

	/*dvbt bandwidth*/
	public static final int SETTINGS_BANDWIDTH_8_MHZ = 0;
	public static final int SETTINGS_BANDWIDTH_7_MHZ = 1;
	public static final int SETTINGS_BANDWIDTH_6_MHZ = 2;	
	public static final int SETTINGS_BANDWIDTH_AUTO = 3;

	/*mode*/
	private int dtvscandvbt_scan_mode = DTVSCANDVBT_SETTING_SCAN_MODE;	

	/*setting ui control*/
	private TextView ui_dvbsandvbt_setting_title;
	private ListView ui_dvbsandvbt_setting_list;
	
	private DTVScanDVBT_SettingListAdapter ui_dvbsandvbt_setting_list_adapt;	
	private int ui_dvbsandvbt_setting_list_curitem = 0;
	private int ui_dvbsandvbt_setting_list_count = SETTINGS_MAX;

	private int dvbscandvbt_area = SETTINGS_AREA_UK;
	private boolean dvbscandvbt_lcn = true;
	
	private int dvbscandvbt_manu_scanmode = SETTINGS_MANU_SCANMODE_FREQ;
	private int dvbscandvbt_manu_scanband = SETTINGS_MANU_SCANBAND_VHF;
	private int dvbscandvbt_manu_chno = 0;
	/*unit KHz*/
	private int dvbscandvbt_manu_freq = 474000;
	/*unit M*/
	private int dvbscandvbt_manu_bandwidth = SETTINGS_BANDWIDTH_8_MHZ;

	/*for setting edit freq in manu scan*/
	AlertDialog.Builder dvbscandvbt_editfreqbuilder;

	/*scan ui control*/
	private TextView ui_dvbsandvbt_scan_title;
	private TextView ui_dvbsandvbt_scan_tvtitle;
	private TextView ui_dvbsandvbt_scan_radiotitle;	
	private ListView ui_dvbsandvbt_scantv_list;
	private ListView ui_dvbsandvbt_scanradio_list;
	private ProgressBar scanprogress;
	private TextView  scanprogresspercentage;
	private TextView  scanfreqinfo;	
	
	private DTVScanDVBT_ScanListAdapter ui_dvbsandvbt_scantv_list_adapter;
	private DTVScanDVBT_ScanListAdapter ui_dvbsandvbt_scanradio_list_adapter;

	private ArrayList<Object> ui_dvbsandvbt_scantv_srv_list;
	private ArrayList<Object> ui_dvbsandvbt_scanradio_srv_list;

	private static int ui_dvbsandvbt_scantv_srv_list_index = 0;
	private static int ui_dvbsandvbt_scanradio_srv_list_index = 0;

	private TVChannelParams[] dvbsandvbt_channelallbandlist = null;
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		DTVScanDVBTUiSettingInit();

	}

	public void onConnected(){
		Log.d(TAG, "connected");
		
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
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

				scanprogress.setProgress(msg.getScanProgress());
				scanprogresspercentage.setText(msg.getScanProgress()+"%");
				scanfreqinfo.setText(msg.getScanCurChanParams().getFrequency()/1000+"KHz");
				
				if (msg.getScanProgramName() != null) {
					Log.d(TAG, "Scan update: new program >> "+ msg.getScanProgramName());
					DTVScanDVBT_ScanListAddSvr(msg.getScanProgramType(), msg.getScanProgramName());
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
				scanprogress.setProgress(100);
				scanprogresspercentage.setText("100%");				
				Log.d(TAG, "stopScan End");				
				break;
			default:
				break;
	
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{    	
		boolean ret = false;
		
		switch (keyCode)
		{     
			case KeyEvent.KEYCODE_DPAD_DOWN:	
				if((dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
					|| (dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE))
				{
					if(ui_dvbsandvbt_setting_list_curitem == ui_dvbsandvbt_setting_list.getCount() - 1)
						ui_dvbsandvbt_setting_list.setSelection(0); 
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if((dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
					|| (dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE))
				{				
					if(ui_dvbsandvbt_setting_list_curitem == 0)
						ui_dvbsandvbt_setting_list.setSelection(ui_dvbsandvbt_setting_list.getCount() - 1); 
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if((dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
					|| (dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE))
				{				
					DTVScanDVBT_SettingListItemLeftArrowHandle();
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if((dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
					|| (dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE))
				{				
					DTVScanDVBT_SettingListItemRightArrowHandle();
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_BACK:	
				if(dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
				{
					DTVScanDVBT_GotoDTVPlayer();
					ret = true;				
				}
				else if(dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE)
				{
					DTVScanDVBTUiSettingInit();
					ret = true;				
				}				
				else if(dtvscandvbt_scan_mode == DTVSCANDVBT_SCAN_MODE)
				{			
					stopScan(false);
				
					DTVScanDVBTUiSettingInit();
					ret = true;
				}
				break;	
			case KeyEvent.KEYCODE_DPAD_CENTER:	
				if(dtvscandvbt_scan_mode == DTVSCANDVBT_SCAN_MODE)
				{			
					stopScan(false);
				
					DTVScanDVBT_GotoDTVPlayer();
					ret = true;
				}
				break;
				
			default:
				break;
		}
        
		if(ret==false){
			ret = super.onKeyDown(keyCode, event);
		}

		return ret;
	}	

	private void DTVScanDVBTUiSettingInit(){
		Log.d(TAG, "DTVScanDVBTUiSettingInit");
		
		setContentView(R.layout.dtvscan_setting);

		ui_dvbsandvbt_setting_title = (TextView)findViewById(R.id.DtvscanTitleText);
		ui_dvbsandvbt_setting_list = (ListView) findViewById(R.id.DtvscanSettingList);            

		if (null == ui_dvbsandvbt_setting_list_adapt){
			ui_dvbsandvbt_setting_list_adapt = new DTVScanDVBT_SettingListAdapter(this);
		}
		ui_dvbsandvbt_setting_list.setAdapter(ui_dvbsandvbt_setting_list_adapt);
        
		ui_dvbsandvbt_setting_list.setItemsCanFocus(false);
		ui_dvbsandvbt_setting_list.setClickable(true);
		ui_dvbsandvbt_setting_list.setFocusable(true);
		ui_dvbsandvbt_setting_list.setOnItemSelectedListener(mOnSelectedListener);
		ui_dvbsandvbt_setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DTVScanDVBT_SettingListItemClicked(position);
			}
		});			
		ui_dvbsandvbt_setting_list.requestFocus();

		DTVScanDVBTUiSettingScan();

		dvbscandvbt_editfreqbuilder = new AlertDialog.Builder(this);
	}

	private void DTVScanDVBTUiScanInit(){
		Log.d(TAG, "DTVScanDVBTUiScanInit");
		
		setContentView(R.layout.dtvscan_scan);

		ui_dvbsandvbt_scan_title = (TextView)findViewById(R.id.dvbscanscan_title);
		ui_dvbsandvbt_scan_tvtitle = (TextView)findViewById(R.id.dvbscanscan_tvtitle);
		ui_dvbsandvbt_scan_radiotitle = (TextView)findViewById(R.id.dvbscanscan_radiotitle);

		ui_dvbsandvbt_scan_tvtitle.setText(R.string.dtvscan_scan_tv);
		ui_dvbsandvbt_scan_radiotitle.setText(R.string.dtvscan_scan_audio);		

		scanprogress = (ProgressBar)findViewById(R.id.dvbscanprogressbar);
		scanprogress.setProgressDrawable(getResources().getDrawable(R.drawable.dtvscan_progress_bg));
		scanprogress.setProgress(0);

		scanprogresspercentage = (TextView)findViewById(R.id.dvbscan_progresspercentage);
		scanprogresspercentage.setText(0+"%");

		scanfreqinfo = (TextView)findViewById(R.id.dvbscan_scanfreqinfo);
		         		
		ui_dvbsandvbt_scantv_list = (ListView)findViewById(R.id.dvbscanscan_listtv);
		
		if (null == ui_dvbsandvbt_scantv_srv_list)
		{
			ui_dvbsandvbt_scantv_srv_list = new ArrayList<Object>();             

			if (null == ui_dvbsandvbt_scantv_list_adapter)
			{
				ui_dvbsandvbt_scantv_list_adapter = new DTVScanDVBT_ScanListAdapter(DTVScanDVBT.this, ui_dvbsandvbt_scantv_srv_list);
				ui_dvbsandvbt_scantv_list_adapter.setServiceType(serviceInfo.SERVICE_TYPE_TV);
			}
			ui_dvbsandvbt_scantv_list.setAdapter(ui_dvbsandvbt_scantv_list_adapter);
		}  
		else
		{
			ui_dvbsandvbt_scantv_list.setAdapter(ui_dvbsandvbt_scantv_list_adapter);
		}

		ui_dvbsandvbt_scanradio_list = (ListView)findViewById(R.id.dvbscanscan_listradio);
		
		if (null == ui_dvbsandvbt_scanradio_srv_list)
		{
			ui_dvbsandvbt_scanradio_srv_list = new ArrayList<Object>();             

			if (null == ui_dvbsandvbt_scanradio_list_adapter)
			{
				ui_dvbsandvbt_scanradio_list_adapter = new DTVScanDVBT_ScanListAdapter(DTVScanDVBT.this, ui_dvbsandvbt_scanradio_srv_list);
				ui_dvbsandvbt_scanradio_list_adapter.setServiceType(serviceInfo.SERVICE_TYPE_RADIO);
			}
			ui_dvbsandvbt_scanradio_list.setAdapter(ui_dvbsandvbt_scanradio_list_adapter);
		}
		else
		{
			ui_dvbsandvbt_scanradio_list.setAdapter(ui_dvbsandvbt_scanradio_list_adapter);
		}	
         
		ui_dvbsandvbt_scantv_list.setItemsCanFocus(false);
		ui_dvbsandvbt_scantv_list.setClickable(false);
		ui_dvbsandvbt_scantv_list.setFocusable(false);

		ui_dvbsandvbt_scanradio_list.setItemsCanFocus(false);
		ui_dvbsandvbt_scanradio_list.setClickable(false);
		ui_dvbsandvbt_scanradio_list.setFocusable(false);

		DTVScanDVBTUiScan();
	}	

	private void DTVScanDVBTUiSettingScan(){
		Log.d(TAG, "DTVScanDVBTUiSettingScan");
		
		dtvscandvbt_scan_mode = DTVSCANDVBT_SETTING_SCAN_MODE;
		ui_dvbsandvbt_setting_list_count = SETTINGS_MAX;
		
		ui_dvbsandvbt_setting_title.setText(R.string.dtvscan_scansettingtitle);

		DTVScanDVBT_SettingList();
	}

	private void DTVScanDVBTUiSettingManualScan(){
		Log.d(TAG, "DTVScanDVBTUiSettingManualScan");
		
		dtvscandvbt_scan_mode = DTVSCANDVBT_SETTING_MANU_SCAN_MODE;
		ui_dvbsandvbt_setting_list_count = SETTINGS_MANU_MAX;
		
		ui_dvbsandvbt_setting_title.setText(R.string.dtvscan_manualscansettingtitle);

		DTVScanDVBT_SettingList();

		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		dvbsandvbt_channelallbandlist = TVChannelParams.channelCurAllbandParams(this, region, TVChannelParams.MODE_OFDM);

		if(dvbsandvbt_channelallbandlist != null)
		{
			dvbscandvbt_manu_freq = dvbsandvbt_channelallbandlist[0].frequency/1000;
			dvbscandvbt_manu_bandwidth = dvbsandvbt_channelallbandlist[0].bandwidth;
			DTVScanDVBT_UpdateScanBand(dvbscandvbt_manu_freq);
		}
	}

	private void DTVScanDVBTUiScan(){
		Log.d(TAG, "DTVScanDVBTUiScan");
		
		DTVScanDVBT_ScanList();
		
		if(dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_SCAN_MODE)
		{
			ui_dvbsandvbt_scan_title.setText(R.string.dtvscan_scan_auto);

			DTVScanDVBT_StartAutoScan();
		}
		else if(dtvscandvbt_scan_mode == DTVSCANDVBT_SETTING_MANU_SCAN_MODE)
		{
			ui_dvbsandvbt_scan_title.setText(R.string.dtvscan_scan_manual);

			DTVScanDVBT_StartManuScan();
		}

		dtvscandvbt_scan_mode = DTVSCANDVBT_SCAN_MODE;
		
	}	

	private void DTVScanDVBT_SettingList(){
		ui_dvbsandvbt_setting_list.setSelection(0);
		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
	}	

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			if (ui_dvbsandvbt_setting_list_curitem != position)
			{
				ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
			}

			ui_dvbsandvbt_setting_list_curitem = position;
		}

		public void onNothingSelected(AdapterView<?> parent)
		{
			;
		}
	};

	private void DTVScanDVBT_SettingListItemClicked(int position)
	{
		switch(dtvscandvbt_scan_mode)
		{
			case DTVSCANDVBT_SETTING_SCAN_MODE:
				{
					switch(position)
					{
						case DTVScanDVBT.SETTINGS_AUTO_SCAN:
							DTVScanDVBT_SettingListItemClickedAutoScanStart();
							break;
						case DTVScanDVBT.SETTINGS_MANU_SCAN:
							DTVScanDVBT_SettingListItemClickedManuScan();
							break;
						default:
							break;
					}
				}
				break;
			case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
				{
					switch(position)
					{
						case DTVScanDVBT.SETTINGS_FREQUENCY:
							DTVScanDVBT_SettingListItemClickedManuFreqEdit();
							break;

						case DTVScanDVBT.SETTINGS_SCAN:
							DTVScanDVBT_SettingListItemClickedManuScanStart();
							break;
							
						default:
							break;
					}						
				}
				break;
			default:
				break;
		}		
	}

	private void DTVScanDVBT_SettingListItemClickedAutoScanStart()
	{
		DTVScanDVBTUiScanInit();
	}

	private void DTVScanDVBT_SettingListItemClickedManuScan()
	{
		DTVScanDVBTUiSettingManualScan();
	}	

	private void DTVScanDVBT_SettingListItemClickedManuScanStart()
	{
		DTVScanDVBTUiScanInit();	
	}

	private void DTVScanDVBT_SettingListItemClickedManuFreqEdit()
	{
		LinearLayout dtvscandvbt_edit_freq_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dtvscan_edit_freq, null);      
		final EditText dtvscandvbt_edit_freq_text = (EditText)dtvscandvbt_edit_freq_layout.findViewById(R.id.dvbscan_edit_freq);
		  
		dvbscandvbt_editfreqbuilder.setTitle(R.string.dtvscan_edit_freq);
		dvbscandvbt_editfreqbuilder.setView(dtvscandvbt_edit_freq_layout);

		dtvscandvbt_edit_freq_text.setText("");
		dvbscandvbt_editfreqbuilder.setPositiveButton(R.string.dtvscan_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (!dtvscandvbt_edit_freq_text.getText().toString().equals(""))
				{
					dvbscandvbt_manu_freq = Integer.valueOf(dtvscandvbt_edit_freq_text.getText().toString());
					ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
				}
			}
		});
		   	
		dvbscandvbt_editfreqbuilder.setNegativeButton(R.string.dtvscan_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			}
		});

		AlertDialog alert = dvbscandvbt_editfreqbuilder.create();
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
		WindowManager.LayoutParams lp = alert.getWindow().getAttributes();
		lp.dimAmount = 0.0f;
		lp.width = (int) (d.getWidth() * 0.65);
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	private void DTVScanDVBT_SettingListItemLeftArrowHandle()
	{
		switch(dtvscandvbt_scan_mode)
		{
			case DTVSCANDVBT_SETTING_SCAN_MODE:
				{
					switch(ui_dvbsandvbt_setting_list_curitem)
					{
						case DTVScanDVBT.SETTINGS_AREA:
							DTVScanDVBT_SettingListItemLeftArrowArea();
							break;
						case DTVScanDVBT.SETTINGS_LCN:
							DTVScanDVBT_SettingListItemArrowLcn();
							break;
						default:
							break;
					}
				}
				break;
				
			case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
				{
					switch(ui_dvbsandvbt_setting_list_curitem)
					{
						case SETTINGS_MANU_SCANMODE:
							DTVScanDVBT_SettingListItemArrowScanMode();
							break;

						case SETTINGS_SCAN_BAND:
							DTVScanDVBT_SettingListItemArrowScanBand();
							break;

						case SETTINGS_CHNO:
							DTVScanDVBT_SettingListItemLeftArrowChNo();
							break;
							
						case DTVScanDVBT.SETTINGS_BANDWIDTH:
							DTVScanDVBT_SettingListItemLeftArrowBandw();
							break;

						default:
							break;
					}					
				}
				break;
			
			default:
				break;
		}
	
		
	}
 
	private void DTVScanDVBT_SettingListItemRightArrowHandle()
	{
		switch(dtvscandvbt_scan_mode)
		{
			case DTVSCANDVBT_SETTING_SCAN_MODE:
				{
					switch(ui_dvbsandvbt_setting_list_curitem)
					{
						case DTVScanDVBT.SETTINGS_AREA:
							DTVScanDVBT_SettingListItemRightArrowArea();
							break;
						case DTVScanDVBT.SETTINGS_LCN:
							DTVScanDVBT_SettingListItemArrowLcn();
							break;
						default:
							break;
					}
				}
				break;

			case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
				{
					switch(ui_dvbsandvbt_setting_list_curitem)
					{
						case SETTINGS_MANU_SCANMODE:
							DTVScanDVBT_SettingListItemArrowScanMode();
							break;

						case SETTINGS_SCAN_BAND:
							DTVScanDVBT_SettingListItemArrowScanBand();
							break;

						case SETTINGS_CHNO:
							DTVScanDVBT_SettingListItemRightArrowChNo();
							break;
					
						case DTVScanDVBT.SETTINGS_BANDWIDTH:
							DTVScanDVBT_SettingListItemRightArrowBandw();
							break;

						default:
							break;
					}					
				}
				break;
				
			default:
				break;
		}	
	}

	private void DTVScanDVBT_SettingListItemLeftArrowArea()
	{
		if (dvbscandvbt_area > SETTINGS_AREA_UK)
		{
			dvbscandvbt_area = dvbscandvbt_area - 1;
		}
		else
		{
			dvbscandvbt_area = SETTINGS_AREA_MAX - 1;
		}	

		dvbscandvbt_manu_chno = 0;

		DTVScanDVBT_SettingUpdateArea();

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanDVBT_SettingListItemRightArrowArea()
	{
		if (dvbscandvbt_area < (SETTINGS_AREA_MAX - 1))
		{
			dvbscandvbt_area = dvbscandvbt_area + 1;		
		}
		else
		{
			dvbscandvbt_area = SETTINGS_AREA_UK;		
		}	

		dvbscandvbt_manu_chno = 0;

		DTVScanDVBT_SettingUpdateArea();

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanDVBT_SettingUpdateArea()
	{
		String region = "Default DVB-T UK";

		switch(dvbscandvbt_area)
		{
			case DTVScanDVBT.SETTINGS_AREA_UK:	
				region = "Default DVB-T UK";
				break;
			case DTVScanDVBT.SETTINGS_AREA_AUSTRALIA:	
				region = "Default DVB-T AUSTRALIA";
				break;
			case DTVScanDVBT.SETTINGS_AREA_ITALY:	
				region = "Default DVB-T ITALY";
				break;
			case DTVScanDVBT.SETTINGS_AREA_FRANCE:	
				region = "Default DVB-T FRANCE";
				break;
			case DTVScanDVBT.SETTINGS_AREA_TAIWAN:
				region = "Default DVB-T TAIWAN";
				break;
			default:
				break;
		}
		
		setConfig("tv:scan:dtv:region", region);
	}

	private void DTVScanDVBT_SettingListItemArrowLcn()
	{
		if(dvbscandvbt_lcn)
		{
			dvbscandvbt_lcn = false;
		}else
		{
			dvbscandvbt_lcn = true;
		}

		
		
		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanDVBT_SettingListItemLeftArrowBandw()
	{
		if (dvbscandvbt_manu_bandwidth > SETTINGS_BANDWIDTH_8_MHZ)
		{
			dvbscandvbt_manu_bandwidth = dvbscandvbt_manu_bandwidth - 1;
		}
		else
		{
			dvbscandvbt_manu_bandwidth = SETTINGS_BANDWIDTH_AUTO;
		}	

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanDVBT_SettingListItemRightArrowBandw()
	{
		if (dvbscandvbt_manu_bandwidth < (SETTINGS_BANDWIDTH_AUTO))
		{
			dvbscandvbt_manu_bandwidth = dvbscandvbt_manu_bandwidth + 1;		
		}
		else
		{
			dvbscandvbt_manu_bandwidth = SETTINGS_BANDWIDTH_8_MHZ;
		}	

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanDVBT_SettingListItemArrowScanMode()
	{
		if(dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
		{
			dvbscandvbt_manu_scanmode = SETTINGS_MANU_SCANMODE_CHAN;
		}else if(dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
		{
			dvbscandvbt_manu_scanmode = SETTINGS_MANU_SCANMODE_FREQ;
		}

		
		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanDVBT_SettingListItemArrowScanBand()
	{
		dvbscandvbt_manu_scanband = (dvbscandvbt_manu_scanband + 1)%SETTINGS_MANU_SCANBAND_MAX;

		if(DTVScanDVBT_UpdateChInfoByband(dvbscandvbt_manu_scanband))
		{
			ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
		}
		else
		{
			dvbscandvbt_manu_scanband = (dvbscandvbt_manu_scanband + 1)%SETTINGS_MANU_SCANBAND_MAX;
		}
	}

	private void DTVScanDVBT_SettingListItemLeftArrowChNo()
	{
		if(dvbsandvbt_channelallbandlist == null)
			return;
		
		if (dvbscandvbt_manu_chno > 0)
		{
			dvbscandvbt_manu_chno = dvbscandvbt_manu_chno - 1;
		}
		else
		{
			dvbscandvbt_manu_chno = dvbsandvbt_channelallbandlist.length - 1;
		}

		dvbscandvbt_manu_freq = dvbsandvbt_channelallbandlist[dvbscandvbt_manu_chno].frequency/1000;
		dvbscandvbt_manu_bandwidth = dvbsandvbt_channelallbandlist[dvbscandvbt_manu_chno].bandwidth;		

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanDVBT_SettingListItemRightArrowChNo()
	{
		if(dvbsandvbt_channelallbandlist == null)
			return;
		
		if (dvbscandvbt_manu_chno < (dvbsandvbt_channelallbandlist.length - 1))
		{
			dvbscandvbt_manu_chno = dvbscandvbt_manu_chno + 1;		
		}
		else
		{
			dvbscandvbt_manu_chno = 0;
		}	

		dvbscandvbt_manu_freq = dvbsandvbt_channelallbandlist[dvbscandvbt_manu_chno].frequency/1000;
		dvbscandvbt_manu_bandwidth = dvbsandvbt_channelallbandlist[dvbscandvbt_manu_chno].bandwidth;	

		ui_dvbsandvbt_setting_list_adapt.notifyDataSetChanged();	
	}	

	private class DTVScanDVBT_SettingListAdapter extends BaseAdapter {
		private Context cont;
		private LayoutInflater mInflater;
		
		private Bitmap mIcon_setting;
		private Bitmap mIcon_scan;

	 	class ViewHolder {
			ImageView icon;
			TextView text;
			ImageView icon1;
			TextView info; 			
			ImageView icon2;
		}
	
		public DTVScanDVBT_SettingListAdapter(Context context) {
			super();
			this.cont = context;
			mInflater=LayoutInflater.from(context);
			/*pic need design*/
			mIcon_setting = BitmapFactory.decodeResource(context.getResources(), R.drawable.dtvscan_setting);
			mIcon_scan = BitmapFactory.decodeResource(context.getResources(), R.drawable.dtvscan_scan);
		}

		public int getCount() {
			return ui_dvbsandvbt_setting_list_count;
		}

		public Object getItem(int position) {
			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dtvscan_settings_list, null);

				holder = new ViewHolder();
		   
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				holder.icon2 = (ImageView)convertView.findViewById(R.id.icon2);
				
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.
			displaytext(holder, position);
		  
			switch(dtvscandvbt_scan_mode)
			{
				case DTVSCANDVBT_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanDVBT.SETTINGS_AUTO_SCAN:
								holder.info.setVisibility(View.GONE);
								displayicon(holder, position, false);
								break;
								
							case DTVScanDVBT.SETTINGS_MANU_SCAN:
								holder.info.setVisibility(View.GONE);
								displayicon(holder, position, false);
								break;
								
							case DTVScanDVBT.SETTINGS_AREA:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoarea(holder);

								holder.info.setTextColor(Color.WHITE);
								
								if (ui_dvbsandvbt_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;
								
							case DTVScanDVBT.SETTINGS_LCN:
								holder.info.setVisibility(View.VISIBLE);
								displayinfolcn(holder);

								holder.info.setTextColor(Color.WHITE);

								if (ui_dvbsandvbt_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;
								
							default:
								break;
						}
					}
					break;

				case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
					{
						  switch(position)
						  {
							case DTVScanDVBT.SETTINGS_MANU_SCANMODE:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoscanmode(holder);

								if (ui_dvbsandvbt_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;

							case DTVScanDVBT.SETTINGS_SCAN_BAND:
								holder.info.setVisibility(View.VISIBLE);								
								displayinfoscanband(holder);

								if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									if (ui_dvbsandvbt_setting_list_curitem == position)
									{
										displayicon(holder, position, true);
										//setlistarrowclicklistener(holder);
									}
									else
									{
										displayicon(holder, position, false);
									}
									holder.info.setTextColor(Color.WHITE);
								}
								else if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									holder.info.setTextColor(Color.DKGRAY);
									displayicon(holder, position, false);
								}
								break;
							case DTVScanDVBT.SETTINGS_CHNO:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoch(holder);

								if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									if (ui_dvbsandvbt_setting_list_curitem == position)
									{
										displayicon(holder, position, true);
										//setlistarrowclicklistener(holder);
									}
									else
									{
										displayicon(holder, position, false);
									}
									holder.info.setTextColor(Color.WHITE);
								}
								else if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									displayicon(holder, position, false);
									holder.info.setTextColor(Color.DKGRAY);
								}
								break;
							case DTVScanDVBT.SETTINGS_FREQUENCY:
								holder.info.setVisibility(View.VISIBLE);
								displayinfofreq(holder);
								
								if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									holder.info.setTextColor(Color.WHITE);
								}
								else if(dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									holder.info.setTextColor(Color.DKGRAY);
								}

								displayicon(holder, position, false);
								break;
							case DTVScanDVBT.SETTINGS_BANDWIDTH:
								holder.info.setVisibility(View.VISIBLE);
								displayinfobandwidth(holder);
									 
								if (dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									if (ui_dvbsandvbt_setting_list_curitem == position)
									{
										displayicon(holder, position, true);
										//setlistarrowclicklistener(holder);
									}
									else
									{
										displayicon(holder, position, false);
									}
									holder.info.setTextColor(Color.WHITE);
								}
								else if(dvbscandvbt_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									displayicon(holder, position, false);
									holder.info.setTextColor(Color.DKGRAY);
								}
								break;
							case DTVScanDVBT.SETTINGS_SCAN:
								holder.info.setVisibility(View.GONE);
								displayicon(holder, position, false);
								break;
							default:
								break;
						}
					}					
					break;
				default:
					break;
			}			
		  
			return convertView;
		}

		private void displayicon(ViewHolder vh, int position, boolean display_arrow)
		{

			switch(dtvscandvbt_scan_mode)
			{
				case DTVSCANDVBT_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanDVBT.SETTINGS_AUTO_SCAN:
								vh.icon.setImageBitmap(mIcon_scan);
								break;			
							case DTVScanDVBT.SETTINGS_MANU_SCAN:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_AREA:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_LCN:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							default:
								break;					
						}
					}
					break;
				case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanDVBT.SETTINGS_MANU_SCANMODE:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_SCAN_BAND:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_CHNO:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_FREQUENCY:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_BANDWIDTH:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanDVBT.SETTINGS_SCAN:
								vh.icon.setImageBitmap(mIcon_scan);
								break;
							default:
								break;
						}
					}
					break;

				default:
					break;
			}
			
			if (display_arrow)
			{
				vh.icon1.setVisibility(View.VISIBLE);
				vh.icon1.setBackgroundResource(R.drawable.dtvscan_setting_arrow_left);
				vh.icon2.setVisibility(View.VISIBLE);
				vh.icon2.setBackgroundResource(R.drawable.dtvscan_setting_arrow_right);
			}
			else
			{
				vh.icon1.setVisibility(View.VISIBLE);
				vh.icon1.setBackgroundResource(R.drawable.dtvscan_setting_blank);
				vh.icon2.setVisibility(View.VISIBLE);
				vh.icon2.setBackgroundResource(R.drawable.dtvscan_setting_blank);
			}
		}

		private void displaytext(ViewHolder vh, int position)
		{
			switch(dtvscandvbt_scan_mode)
			{
				case DTVSCANDVBT_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanDVBT.SETTINGS_AUTO_SCAN:
								vh.text.setText(R.string.dtvscan_scan_auto);
								break;
							case DTVScanDVBT.SETTINGS_MANU_SCAN:
								vh.text.setText(R.string.dtvscan_scan_manual);
								break;
							case DTVScanDVBT.SETTINGS_AREA:
								vh.text.setText(R.string.dtvscan_erea);
								break;
							case DTVScanDVBT.SETTINGS_LCN:
								vh.text.setText(R.string.dtvscan_lcn);
								break;
							default:
								break;
						}
					}
					break;
					
				case DTVSCANDVBT_SETTING_MANU_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanDVBT.SETTINGS_MANU_SCANMODE:	
								vh.text.setText(R.string.dtvscan_scan_mode);
								break;
							case DTVScanDVBT.SETTINGS_SCAN_BAND:	
								vh.text.setText(R.string.dtvscan_scan_band);
								break;
							case DTVScanDVBT.SETTINGS_CHNO:	
								vh.text.setText(R.string.dtvscan_channel_no);
								break;
							case DTVScanDVBT.SETTINGS_FREQUENCY:	
								vh.text.setText(R.string.dtvscan_base_frequence);
								break;
							case DTVScanDVBT.SETTINGS_BANDWIDTH:
								vh.text.setText(R.string.dtvscandvbt_bandwidth);
								break;
							case DTVScanDVBT.SETTINGS_SCAN:
								vh.text.setText(R.string.dtvscan_begain_search);
								break;
							default:
								break;
						}						
					}
					break;
					
				default:
					break;
			}
					
		}

		/*
		private void setlistarrowclicklistener(ViewHolder vh){
			vh.icon1.setOnClickListener(new OnClickListener(){          
				public void onClick(View v) {
					//DTVScanDVBT_SettingListItemLeftArrowHandle();
				}
			});

			vh.icon2.setOnClickListener(new OnClickListener(){          
				public void onClick(View v) {
					//DTVScanDVBT_SettingListItemRightArrowHandle();
				}
			});			
		}
		*/
		
		private void displayinfoarea(ViewHolder vh){
			switch(dvbscandvbt_area)
			{
				case DTVScanDVBT.SETTINGS_AREA_UK:	
					vh.info.setText(R.string.dtvscandvbt_erea_uk);
					break;
				case DTVScanDVBT.SETTINGS_AREA_AUSTRALIA:	
					vh.info.setText(R.string.dtvscandvbt_erea_australia);
					break;
				case DTVScanDVBT.SETTINGS_AREA_ITALY:	
					vh.info.setText(R.string.dtvscandvbt_erea_italy);
					break;
				case DTVScanDVBT.SETTINGS_AREA_FRANCE:	
					vh.info.setText(R.string.dtvscandvbt_erea_france);
					break;
				case DTVScanDVBT.SETTINGS_AREA_TAIWAN:
					vh.info.setText(R.string.dtvscandvbt_erea_taiwan);
					break;
				default:
					break;
			}				
		}	

		private void displayinfolcn(ViewHolder vh){
			if(dvbscandvbt_lcn)
			{
				vh.info.setText(R.string.dtvscan_lcn_on);
			}else
			{
				vh.info.setText(R.string.dtvscan_lcn_off);
			}
		}	

		private void displayinfoscanmode(ViewHolder vh){
			switch(dvbscandvbt_manu_scanmode)
			{
				case DTVScanDVBT.SETTINGS_MANU_SCANMODE_FREQ:	
					vh.info.setText(R.string.dtvscan_by_frequence);
					break;			
				
				case DTVScanDVBT.SETTINGS_MANU_SCANMODE_CHAN:	
					vh.info.setText(R.string.dtvscan_by_channel);
					break;

				default:
					break;
			}
		}

		private void displayinfoscanband(ViewHolder vh){
			/*refresh dvbscandvbt_manu_scanband*/
			
			switch(dvbscandvbt_manu_scanband)
			{
				case DTVScanDVBT.SETTINGS_MANU_SCANBAND_VHF:	
					vh.info.setText(R.string.dtvscan_scan_band_vhf);
					break;			
				
				case DTVScanDVBT.SETTINGS_MANU_SCANBAND_UHF:	
					vh.info.setText(R.string.dtvscan_scan_band_uhf);
					break;

				default:
					break;
			}
		}	

		private void displayinfoch(ViewHolder vh){
			/*refresh dvbscandvbt_manu_chno and dvbscandvbt_manu_freq*/
			
			vh.info.setText("CH" + dvbscandvbt_manu_chno + "(" + dvbscandvbt_manu_freq + "KHZ)");
		}

		private void displayinfofreq(ViewHolder vh){			
			vh.info.setText(dvbscandvbt_manu_freq + "");
		}		

		private void displayinfobandwidth(ViewHolder vh){
			switch(dvbscandvbt_manu_bandwidth)
			{
				case DTVScanDVBT.SETTINGS_BANDWIDTH_8_MHZ:
				case DTVScanDVBT.SETTINGS_BANDWIDTH_7_MHZ:
				case DTVScanDVBT.SETTINGS_BANDWIDTH_6_MHZ:
					vh.info.setText((8 - dvbscandvbt_manu_bandwidth) + "");
					break;			
				
				case DTVScanDVBT.SETTINGS_BANDWIDTH_AUTO:	
					vh.info.setText(R.string.dtvscandvbt_scan_bandwidthauto);
					break;

				default:
					break;
			}
			
		}
		
	}

	private void DTVScanDVBT_UpdateScanBand(int chfreq)
	{
		if(chfreq < 300000)
		{
			dvbscandvbt_manu_scanband = SETTINGS_MANU_SCANBAND_VHF;
		}
		else
		{
			dvbscandvbt_manu_scanband = SETTINGS_MANU_SCANBAND_UHF;
		}
	}

	private boolean DTVScanDVBT_UpdateChInfoByband(int scanband)
	{
		boolean ret = false;

		if(dvbsandvbt_channelallbandlist == null)
			return ret;
		
		if(scanband == SETTINGS_MANU_SCANBAND_VHF)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) < 300000)
				{
					dvbscandvbt_manu_chno = i; 
					dvbscandvbt_manu_freq = dvbsandvbt_channelallbandlist[i].frequency/1000;
					dvbscandvbt_manu_bandwidth = dvbsandvbt_channelallbandlist[i].bandwidth;
					ret = true;
					break;
				}				
			}	
		}
		else if(scanband == SETTINGS_MANU_SCANBAND_UHF)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) >= 300000)
				{
					dvbscandvbt_manu_chno = i; 
					dvbscandvbt_manu_freq = dvbsandvbt_channelallbandlist[i].frequency/1000;
					dvbscandvbt_manu_bandwidth = dvbsandvbt_channelallbandlist[i].bandwidth;
					ret = true;
					break;
				}				
			}		
		}

		return ret;
	}	

	private serviceInfo getServiceInfoByPos(int pos, int serviceType)
	{
		serviceInfo si = null;

		if (serviceType == serviceInfo.SERVICE_TYPE_TV)
		{
			if (pos > ui_dvbsandvbt_scantv_srv_list.size() || ui_dvbsandvbt_scantv_srv_list.size() <= 0)
			{
				return null;
			}

			si = (serviceInfo) ui_dvbsandvbt_scantv_srv_list.get(pos);
		}
		else if (serviceType == serviceInfo.SERVICE_TYPE_RADIO)
		{
			if (pos > ui_dvbsandvbt_scanradio_srv_list.size() || ui_dvbsandvbt_scanradio_srv_list.size() <= 0)
			{
				return null;
			}

			si = (serviceInfo) ui_dvbsandvbt_scanradio_srv_list.get(pos);
		}
		
		return si;
	}	

	private class serviceInfo {
		static final int SERVICE_TYPE_TV = 0x01;
		static final int SERVICE_TYPE_RADIO = 0x02;

		private int serviceType;
		private int id;
		private String name;

		public int getServiceType() {
			return serviceType;
		}
		public void setServiceType(int serviceType) {
			this.serviceType = serviceType;
		}

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

	
	private void DTVScanDVBT_ScanList(){
		ui_dvbsandvbt_scantv_srv_list.clear();
		ui_dvbsandvbt_scanradio_srv_list.clear();
		ui_dvbsandvbt_scantv_srv_list_index = 0;
		ui_dvbsandvbt_scanradio_srv_list_index = 0;
		ui_dvbsandvbt_scantv_list_adapter.notifyDataSetChanged();
		ui_dvbsandvbt_scanradio_list_adapter.notifyDataSetChanged();
	}

	private void DTVScanDVBT_ScanListAddSvr(int service_type, String service_name)
	{
		if ((service_type != serviceInfo.SERVICE_TYPE_TV) && (service_type != serviceInfo.SERVICE_TYPE_RADIO))
		{
			return ;
		}
		
		serviceInfo serviceinfo = new serviceInfo();
		serviceinfo.setServiceType(service_type);
		serviceinfo.setName(service_name);

		if (service_type == serviceInfo.SERVICE_TYPE_TV)
		{
			serviceinfo.setId(++ui_dvbsandvbt_scantv_srv_list_index);
			ui_dvbsandvbt_scantv_srv_list.add(serviceinfo); 
			ui_dvbsandvbt_scantv_list.setSelection(ui_dvbsandvbt_scantv_srv_list_index - 1);
			ui_dvbsandvbt_scantv_list_adapter.notifyDataSetChanged();
		}
		else
		{
			serviceinfo.setId(++ui_dvbsandvbt_scanradio_srv_list_index);
			ui_dvbsandvbt_scanradio_srv_list.add(serviceinfo); 
			ui_dvbsandvbt_scanradio_list.setSelection(ui_dvbsandvbt_scanradio_srv_list_index - 1);
			ui_dvbsandvbt_scanradio_list_adapter.notifyDataSetChanged();
		}
	}	

	private class DTVScanDVBT_ScanListAdapter extends BaseAdapter{
		Context context;
		List<Object> titles;    
		LayoutInflater lif;
		private int selectItem;
		private int serviceType;

		private class ViewHolder{
			/*is index now*/
			TextView srv_id;
			TextView srv_name;
		}		
     
		public DTVScanDVBT_ScanListAdapter(Context context,List<Object> titles){
			this.context = context;
			this.titles = titles;
		}

		public void setServiceType(int serviceType)
		{
			this.serviceType = serviceType;
		}
		
		public int getCount() {
			return titles.size();
		}
     
		public Object getItem(int position) {
			return titles.get(position);
		}
    
		public long getItemId(int position) {
			return position;
		}
     
		public void setSelectItem(int position)
		{
			this.selectItem = position;
		}
		
		public int getSelectItem()
		{
			return this.selectItem;
		}
    
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mv;
			final int pos = position;

			if(convertView == null){
				mv = new ViewHolder();
				lif = LayoutInflater.from(context);

				convertView = lif.inflate(R.layout.dtvscan_scan_list, null);

				//convertView = lif.inflate(R.layout.listlayout, null);
				mv.srv_id = (TextView)convertView.findViewById(R.id.dtvscan_scanlist_srv_id_text);
				mv.srv_name = (TextView)convertView.findViewById(R.id.dtvscan_scanlist_srv_name_text);

				convertView.setTag(mv);
			}else{
				mv = (ViewHolder)convertView.getTag();
			}        

			fillData(mv, position);

			return convertView;
		}  

		private void fillData(ViewHolder mv, int position)
		{
			if (null != mv)
			{
				serviceInfo serviceinfo = getServiceInfoByPos(position, this.serviceType);
				if (null != serviceinfo)
				{
					mv.srv_id.setText(serviceinfo.getId() + "");
					mv.srv_name.setText(serviceinfo.getName());	
				}
			}
		}
		
	}

	private void DTVScanDVBT_StartAutoScan()
	{
		Log.d(TAG, "DTVScanDVBT_StartAutoScan");

		TVScanParams sp;	

		sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_OFDM);

		startScan(sp);
	}

	private void DTVScanDVBT_StartManuScan()
	{
		Log.d(TAG, "DTVScanDVBT_StartManuScan");
	
		TVScanParams sp;	

		switch(dvbscandvbt_manu_bandwidth)
		{
			case DTVScanDVBT.SETTINGS_BANDWIDTH_8_MHZ:
				sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.dvbtParams(dvbscandvbt_manu_freq * 1000, TVChannelParams.BANDWIDTH_8_MHZ));
				break;
				
			case DTVScanDVBT.SETTINGS_BANDWIDTH_7_MHZ:
				sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.dvbtParams(dvbscandvbt_manu_freq * 1000, TVChannelParams.BANDWIDTH_7_MHZ));
				break;
				
			case DTVScanDVBT.SETTINGS_BANDWIDTH_6_MHZ:
				sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.dvbtParams(dvbscandvbt_manu_freq * 1000, TVChannelParams.BANDWIDTH_6_MHZ));
				break;			
			
			case DTVScanDVBT.SETTINGS_BANDWIDTH_AUTO:	
				sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.dvbtParams(dvbscandvbt_manu_freq * 1000, TVChannelParams.BANDWIDTH_AUTO));
				break;

			default:
				sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.dvbtParams(dvbscandvbt_manu_freq * 1000, TVChannelParams.BANDWIDTH_8_MHZ));
				break;
		}
		
		startScan(sp);
	}

	private void DTVScanDVBT_GotoDTVPlayer()
	{	
		Intent intent = new Intent();
		intent.setClass(this, DTVPlayer.class);
		
		startActivityForResult(intent, 1);

		finish();
	}
	
}

