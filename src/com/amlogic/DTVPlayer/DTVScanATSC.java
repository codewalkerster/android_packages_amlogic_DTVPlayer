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

public class DTVScanATSC extends DTVActivity{
	private static final String TAG="DTVScanATSC";

	public static final int DTVSCANATSC_SETTING_SCAN_MODE = 0;
	public static final int DTVSCANATSC_SETTING_MANU_SCAN_MODE = 1;
	public static final int DTVSCANATSC_SCAN_MODE = 2;

	/*in DTVSCANATSC_SETTING_SCAN_MODE*/
	public static final int SETTINGS_AUTO_SCAN = 0;
	public static final int SETTINGS_MANU_SCAN = 1;
	public static final int SETTINGS_AREA = 2;
	public static final int SETTINGS_ANTENNA_POWER = 3;
	public static final int SETTINGS_SIGNAL_TYPE = 4;
	public static final int SETTINGS_MAX = 5;

	/*in DTVSCANATSC_SETTING_MANU_SCAN_MODE*/
	public static final int SETTINGS_MANU_SCANMODE = 0;
	public static final int SETTINGS_SCAN_BAND = 1;
	public static final int SETTINGS_CHNO = 2;
	public static final int SETTINGS_FREQUENCY = 3;
	/*not use now. if use atsc demod need set modulation, SETTINGS_MODULATION = 4;SETTINGS_SCAN = 5;SETTINGS_MANU_MAX = 6;*/
	public static final int SETTINGS_MODULATION = 6;
	public static final int SETTINGS_SCAN = 4;
	public static final int SETTINGS_MANU_MAX = 5;

	/*atsc area*/
	public static final int SETTINGS_AREA_USA = 0;
	public static final int SETTINGS_AREA_CANADA = 1;
	public static final int SETTINGS_AREA_MEXICO = 2;
	public static final int SETTINGS_AREA_MAX = 3;

	/*atsc signal type*/
	public static final int SETTINGS_SIGNAL_CABLE = 0;
	public static final int SETTINGS_SIGNAL_AIR = 1;

	/*atsc manual scan mode*/
	public static final int SETTINGS_MANU_SCANMODE_FREQ = 0;
	public static final int SETTINGS_MANU_SCANMODE_CHAN = 1;

	/*atsc manual band*/
	public static final int SETTINGS_MANU_SCANBAND_VHF = 0;
	public static final int SETTINGS_MANU_SCANBAND_UHF = 1;
	public static final int SETTINGS_MANU_SCANBAND_MAX = 2;

	/*atsc modulation*/
	public static final int SETTINGS_MODULATION_QAM_AUTO= 0;
	public static final int SETTINGS_MODULATION_QAM_16  = 1;
	public static final int SETTINGS_MODULATION_QAM_32  = 2;
	public static final int SETTINGS_MODULATION_QAM_64  = 3;
	public static final int SETTINGS_MODULATION_QAM_128 = 4;
	public static final int SETTINGS_MODULATION_QAM_256 = 5;
	public static final int SETTINGS_MODULATION_VSB_8   = 6;
	public static final int SETTINGS_MODULATION_VSB_16  = 7;

	/*mode*/
	private int dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_SCAN_MODE;	

	/*setting ui control*/
	private TextView ui_dvbsanatsc_setting_title;
	private ListView ui_dvbsanatsc_setting_list;
	
	private DTVScanATSC_SettingListAdapter ui_dvbsanatsc_setting_list_adapt;	
	private int ui_dvbsanatsc_setting_list_curitem = 0;
	private int ui_dvbsanatsc_setting_list_count = SETTINGS_MAX;

	private int dvbscanatsc_area = SETTINGS_AREA_USA;
	private boolean dvbscanatsc_antennapwoer = false;
	private int dfbscanatsc_signaltype = SETTINGS_SIGNAL_CABLE;
	
	private int dvbscanatsc_manu_scanmode = SETTINGS_MANU_SCANMODE_FREQ;
	private int dvbscanatsc_manu_scanband = SETTINGS_MANU_SCANBAND_VHF;
	private int dvbscanatsc_manu_chno = 0;
	/*unit KHz*/
	private int dvbscanatsc_manu_freq = 474000;
	private int dvbscanatsc_manu_modulation = SETTINGS_MODULATION_QAM_AUTO;

	/*for setting edit freq in manu scan*/
	AlertDialog.Builder dvbscanatsc_editfreqbuilder;

	/*scan ui control*/
	private TextView ui_dvbsanatsc_scan_title;
	private TextView ui_dvbsanatsc_scan_tvtitle;
	private TextView ui_dvbsanatsc_scan_radiotitle;	
	private ListView ui_dvbsanatsc_scantv_list;
	private ListView ui_dvbsanatsc_scanradio_list;
	private ProgressBar scanprogress;
	private TextView  scanprogresspercentage;
	private TextView  scanfreqinfo;	
	
	private DTVScanATSC_ScanListAdapter ui_dvbsanatsc_scantv_list_adapter;
	private DTVScanATSC_ScanListAdapter ui_dvbsanatsc_scanradio_list_adapter;

	private ArrayList<Object> ui_dvbsanatsc_scantv_srv_list;
	private ArrayList<Object> ui_dvbsanatsc_scanradio_srv_list;

	private static int ui_dvbsanatsc_scantv_srv_list_index = 0;
	private static int ui_dvbsanatsc_scanradio_srv_list_index = 0;

	private TVChannelParams[] dvbsanatsc_channelallbandlist = null;
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		DTVScanATSCUiSettingInit();

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
					Log.d(TAG, "Scan update: new program >> "+ msg.getScanProgramType() + " "+ msg.getScanProgramName());
					DTVScanATSC_ScanListAddSvr(msg.getScanProgramType(), msg.getScanProgramName());
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
				if((dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
					|| (dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE))
				{
					if(ui_dvbsanatsc_setting_list_curitem == ui_dvbsanatsc_setting_list.getCount() - 1)
						ui_dvbsanatsc_setting_list.setSelection(0); 
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if((dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
					|| (dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE))
				{				
					if(ui_dvbsanatsc_setting_list_curitem == 0)
						ui_dvbsanatsc_setting_list.setSelection(ui_dvbsanatsc_setting_list.getCount() - 1); 
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if((dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
					|| (dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE))
				{				
					DTVScanATSC_SettingListItemLeftArrowHandle();
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if((dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
					|| (dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE))
				{				
					DTVScanATSC_SettingListItemRightArrowHandle();
					ret = true;
				}
				break;
			case KeyEvent.KEYCODE_BACK:	
				if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
				{
					DTVScanATSC_GotoDTVPlayer();
					ret = true;				
				}
				else if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE)
				{
					DTVScanATSCUiSettingInit();
					ret = true;				
				}				
				else if(dtvscanatsc_scan_mode == DTVSCANATSC_SCAN_MODE)
				{				
					DTVScanATSCUiSettingInit();
					ret = true;
				}
				break;	
			case KeyEvent.KEYCODE_DPAD_CENTER:	
				if(dtvscanatsc_scan_mode == DTVSCANATSC_SCAN_MODE)
				{				
					DTVScanATSC_GotoDTVPlayer();
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

	private void DTVScanATSCUiSettingInit(){
		Log.d(TAG, "DTVScanATSCUiSettingInit");
		
		setContentView(R.layout.dtvscan_setting);

		ui_dvbsanatsc_setting_title = (TextView)findViewById(R.id.DtvscanTitleText);
		ui_dvbsanatsc_setting_list = (ListView) findViewById(R.id.DtvscanSettingList);            

		if (null == ui_dvbsanatsc_setting_list_adapt){
			ui_dvbsanatsc_setting_list_adapt = new DTVScanATSC_SettingListAdapter(this);
		}
		ui_dvbsanatsc_setting_list.setAdapter(ui_dvbsanatsc_setting_list_adapt);
        
		ui_dvbsanatsc_setting_list.setItemsCanFocus(false);
		ui_dvbsanatsc_setting_list.setClickable(true);
		ui_dvbsanatsc_setting_list.setFocusable(true);
		ui_dvbsanatsc_setting_list.setOnItemSelectedListener(mOnSelectedListener);
		ui_dvbsanatsc_setting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DTVScanATSC_SettingListItemClicked(position);
			}
		});			
		ui_dvbsanatsc_setting_list.requestFocus();

		DTVScanATSCUiSettingScan();

		dvbscanatsc_editfreqbuilder = new AlertDialog.Builder(this);
	}

	private void DTVScanATSCUiScanInit(){
		Log.d(TAG, "DTVScanATSCUiScanInit");
		
		setContentView(R.layout.dtvscan_scan);

		ui_dvbsanatsc_scan_title = (TextView)findViewById(R.id.dvbscanscan_title);
		ui_dvbsanatsc_scan_tvtitle = (TextView)findViewById(R.id.dvbscanscan_tvtitle);
		ui_dvbsanatsc_scan_radiotitle = (TextView)findViewById(R.id.dvbscanscan_radiotitle);

		ui_dvbsanatsc_scan_tvtitle.setText(R.string.dtvscan_scan_tv);
		ui_dvbsanatsc_scan_radiotitle.setText(R.string.dtvscan_scan_audio);		

		scanprogress = (ProgressBar)findViewById(R.id.dvbscanprogressbar);
		scanprogress.setProgressDrawable(getResources().getDrawable(R.drawable.dtvscan_progress_bg));
		scanprogress.setProgress(0);

		scanprogresspercentage = (TextView)findViewById(R.id.dvbscan_progresspercentage);
		scanprogresspercentage.setText(0+"%");

		scanfreqinfo = (TextView)findViewById(R.id.dvbscan_scanfreqinfo);
		         		
		ui_dvbsanatsc_scantv_list = (ListView)findViewById(R.id.dvbscanscan_listtv);
		
		if (null == ui_dvbsanatsc_scantv_srv_list)
		{
			ui_dvbsanatsc_scantv_srv_list = new ArrayList<Object>();             

			if (null == ui_dvbsanatsc_scantv_list_adapter)
			{
				ui_dvbsanatsc_scantv_list_adapter = new DTVScanATSC_ScanListAdapter(DTVScanATSC.this, ui_dvbsanatsc_scantv_srv_list);
				ui_dvbsanatsc_scantv_list_adapter.setServiceType(serviceInfo.SERVICE_TYPE_TV);
			}
			ui_dvbsanatsc_scantv_list.setAdapter(ui_dvbsanatsc_scantv_list_adapter);
		}  

		ui_dvbsanatsc_scanradio_list = (ListView)findViewById(R.id.dvbscanscan_listradio);
		
		if (null == ui_dvbsanatsc_scanradio_srv_list)
		{
			ui_dvbsanatsc_scanradio_srv_list = new ArrayList<Object>();             

			if (null == ui_dvbsanatsc_scanradio_list_adapter)
			{
				ui_dvbsanatsc_scanradio_list_adapter = new DTVScanATSC_ScanListAdapter(DTVScanATSC.this, ui_dvbsanatsc_scanradio_srv_list);
				ui_dvbsanatsc_scanradio_list_adapter.setServiceType(serviceInfo.SERVICE_TYPE_RADIO);
			}
			ui_dvbsanatsc_scanradio_list.setAdapter(ui_dvbsanatsc_scanradio_list_adapter);
		}
         
		ui_dvbsanatsc_scantv_list.setItemsCanFocus(false);
		ui_dvbsanatsc_scantv_list.setClickable(false);
		ui_dvbsanatsc_scantv_list.setFocusable(false);

		ui_dvbsanatsc_scanradio_list.setItemsCanFocus(false);
		ui_dvbsanatsc_scanradio_list.setClickable(false);
		ui_dvbsanatsc_scanradio_list.setFocusable(false);

		DTVScanATSCUiScan();
	}	

	private void DTVScanATSCUiSettingScan(){
		Log.d(TAG, "DTVScanATSCUiSettingScan");
		
		dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_SCAN_MODE;
		ui_dvbsanatsc_setting_list_count = SETTINGS_MAX;
		
		ui_dvbsanatsc_setting_title.setText(R.string.dtvscan_scansettingtitle);

		DTVScanATSC_SettingList();
	}

	private void DTVScanATSCUiSettingManualScan(){
		Log.d(TAG, "DTVScanATSCUiSettingManualScan");
		
		dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_MANU_SCAN_MODE;
		ui_dvbsanatsc_setting_list_count = SETTINGS_MANU_MAX;
		
		ui_dvbsanatsc_setting_title.setText(R.string.dtvscan_manualscansettingtitle);

		DTVScanATSC_SettingList();

		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		dvbsanatsc_channelallbandlist = TVChannelParams.channelCurAllbandParams(this, region, TVChannelParams.MODE_ATSC);

		if(dvbsanatsc_channelallbandlist != null)
		{
			dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[0].frequency/1000;
			dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[0].modulation;
			DTVScanATSC_UpdateScanBand(dvbscanatsc_manu_freq);
		}
	}

	private void DTVScanATSCUiScan(){
		Log.d(TAG, "DTVScanATSCUiScan");
		
		DTVScanATSC_ScanList();
		
		if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
		{
			ui_dvbsanatsc_scan_title.setText(R.string.dtvscan_scan_auto);

			DTVScanATSC_StartAutoScan();
		}
		else if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE)
		{
			ui_dvbsanatsc_scan_title.setText(R.string.dtvscan_scan_manual);

			DTVScanATSC_StartManuScan();
		}

		dtvscanatsc_scan_mode = DTVSCANATSC_SCAN_MODE;
		
	}	

	private void DTVScanATSC_SettingList(){
		ui_dvbsanatsc_setting_list.setSelection(0);
		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
	}	

	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
		{
			if (ui_dvbsanatsc_setting_list_curitem != position)
			{
				ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
			}

			ui_dvbsanatsc_setting_list_curitem = position;
		}

		public void onNothingSelected(AdapterView<?> parent)
		{
			;
		}
	};

	private void DTVScanATSC_SettingListItemClicked(int position)
	{
		switch(dtvscanatsc_scan_mode)
		{
			case DTVSCANATSC_SETTING_SCAN_MODE:
				{
					switch(position)
					{
						case DTVScanATSC.SETTINGS_AUTO_SCAN:
							DTVScanATSC_SettingListItemClickedAutoScanStart();
							break;
						case DTVScanATSC.SETTINGS_MANU_SCAN:
							DTVScanATSC_SettingListItemClickedManuScan();
							break;
						default:
							break;
					}
				}
				break;
			case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
				{
					switch(position)
					{
						case DTVScanATSC.SETTINGS_FREQUENCY:
							DTVScanATSC_SettingListItemClickedManuFreqEdit();
							break;

						case DTVScanATSC.SETTINGS_SCAN:
							DTVScanATSC_SettingListItemClickedManuScanStart();
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

	private void DTVScanATSC_SettingListItemClickedAutoScanStart()
	{
		DTVScanATSCUiScanInit();
	}

	private void DTVScanATSC_SettingListItemClickedManuScan()
	{
		DTVScanATSCUiSettingManualScan();
	}	

	private void DTVScanATSC_SettingListItemClickedManuScanStart()
	{
		DTVScanATSCUiScanInit();	
	}

	private void DTVScanATSC_SettingListItemClickedManuFreqEdit()
	{
		LinearLayout dtvscanatsc_edit_freq_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dtvscan_edit_freq, null);      
		final EditText dtvscanatsc_edit_freq_text = (EditText)dtvscanatsc_edit_freq_layout.findViewById(R.id.dvbscan_edit_freq);
		  
		dvbscanatsc_editfreqbuilder.setTitle(R.string.dtvscan_edit_freq);
		dvbscanatsc_editfreqbuilder.setView(dtvscanatsc_edit_freq_layout);

		dtvscanatsc_edit_freq_text.setText("");
		dvbscanatsc_editfreqbuilder.setPositiveButton(R.string.dtvscan_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (!dtvscanatsc_edit_freq_text.getText().toString().equals(""))
				{
					dvbscanatsc_manu_freq = Integer.valueOf(dtvscanatsc_edit_freq_text.getText().toString());
					ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
				}
			}
		});
		   	
		dvbscanatsc_editfreqbuilder.setNegativeButton(R.string.dtvscan_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			}
		});

		AlertDialog alert = dvbscanatsc_editfreqbuilder.create();
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

	private void DTVScanATSC_SettingListItemLeftArrowHandle()
	{
		switch(dtvscanatsc_scan_mode)
		{
			case DTVSCANATSC_SETTING_SCAN_MODE:
				{
					switch(ui_dvbsanatsc_setting_list_curitem)
					{
						case DTVScanATSC.SETTINGS_AREA:
							DTVScanATSC_SettingListItemLeftArrowArea();
							break;
						case DTVScanATSC.SETTINGS_ANTENNA_POWER:
							DTVScanATSC_SettingListItemArrowAntennapwoer();
							break;
						case DTVScanATSC.SETTINGS_SIGNAL_TYPE:
							DTVScanATSC_SettingListItemArrowSignaltype();
							break;
							
						default:
							break;
					}
				}
				break;
				
			case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
				{
					switch(ui_dvbsanatsc_setting_list_curitem)
					{
						case SETTINGS_MANU_SCANMODE:
							DTVScanATSC_SettingListItemArrowScanMode();
							break;

						case SETTINGS_SCAN_BAND:
							DTVScanATSC_SettingListItemArrowScanBand();
							break;

						case SETTINGS_CHNO:
							DTVScanATSC_SettingListItemLeftArrowChNo();
							break;
							
						case DTVScanATSC.SETTINGS_MODULATION:
							DTVScanATSC_SettingListItemLeftArrowModulation();
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
 
	private void DTVScanATSC_SettingListItemRightArrowHandle()
	{
		switch(dtvscanatsc_scan_mode)
		{
			case DTVSCANATSC_SETTING_SCAN_MODE:
				{
					switch(ui_dvbsanatsc_setting_list_curitem)
					{
						case DTVScanATSC.SETTINGS_AREA:
							DTVScanATSC_SettingListItemRightArrowArea();
							break;
						case DTVScanATSC.SETTINGS_ANTENNA_POWER:
							DTVScanATSC_SettingListItemArrowAntennapwoer();
							break;
						case DTVScanATSC.SETTINGS_SIGNAL_TYPE:
							DTVScanATSC_SettingListItemArrowSignaltype();
							break;
							
						default:
							break;
					}
				}
				break;

			case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
				{
					switch(ui_dvbsanatsc_setting_list_curitem)
					{
						case SETTINGS_MANU_SCANMODE:
							DTVScanATSC_SettingListItemArrowScanMode();
							break;

						case SETTINGS_SCAN_BAND:
							DTVScanATSC_SettingListItemArrowScanBand();
							break;

						case SETTINGS_CHNO:
							DTVScanATSC_SettingListItemRightArrowChNo();
							break;
					
						case DTVScanATSC.SETTINGS_MODULATION:
							DTVScanATSC_SettingListItemRightArrowModulation();
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

	private void DTVScanATSC_SettingListItemLeftArrowArea()
	{
		if (dvbscanatsc_area > SETTINGS_AREA_USA)
		{
			dvbscanatsc_area = dvbscanatsc_area - 1;
		}
		else
		{
			dvbscanatsc_area = SETTINGS_AREA_MAX - 1;
		}	

		dvbscanatsc_manu_chno = 0;

		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanATSC_SettingListItemRightArrowArea()
	{
		if (dvbscanatsc_area < (SETTINGS_AREA_MAX - 1))
		{
			dvbscanatsc_area = dvbscanatsc_area + 1;		
		}
		else
		{
			dvbscanatsc_area = SETTINGS_AREA_USA;		
		}	

		dvbscanatsc_manu_chno = 0;

		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanATSC_SettingListItemArrowAntennapwoer()
	{
		if(dvbscanatsc_antennapwoer)
		{
			dvbscanatsc_antennapwoer = false;
		}else
		{
			dvbscanatsc_antennapwoer = true;
		}

		
		
		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanATSC_SettingListItemArrowSignaltype()
	{
		if(dfbscanatsc_signaltype == SETTINGS_SIGNAL_CABLE)
		{
			dfbscanatsc_signaltype = SETTINGS_SIGNAL_AIR;
			dvbscanatsc_manu_modulation = SETTINGS_MODULATION_VSB_8;
		}else
		{
			dfbscanatsc_signaltype = SETTINGS_SIGNAL_CABLE;
			dvbscanatsc_manu_modulation = SETTINGS_MODULATION_QAM_AUTO;
		}
		
		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}	

	private void DTVScanATSC_SettingListItemLeftArrowModulation()
	{
		if(dfbscanatsc_signaltype == SETTINGS_SIGNAL_CABLE)
		{
			if (dvbscanatsc_manu_modulation > SETTINGS_MODULATION_QAM_AUTO)
			{
				dvbscanatsc_manu_modulation = dvbscanatsc_manu_modulation - 1;
			}
			else
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_QAM_256;
			}	
		}else
		{
			if (dvbscanatsc_manu_modulation == SETTINGS_MODULATION_VSB_8)
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_VSB_16;
			}
			else
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_VSB_8;
			}
		}

		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanATSC_SettingListItemRightArrowModulation()
	{
		if(dfbscanatsc_signaltype == SETTINGS_SIGNAL_CABLE)
		{
			if (dvbscanatsc_manu_modulation < (SETTINGS_MODULATION_QAM_256))
			{
				dvbscanatsc_manu_modulation = dvbscanatsc_manu_modulation + 1;		
			}
			else
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_QAM_AUTO;
			}
		}else
		{
			if (dvbscanatsc_manu_modulation == SETTINGS_MODULATION_VSB_8)
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_VSB_16;
			}
			else
			{
				dvbscanatsc_manu_modulation = SETTINGS_MODULATION_VSB_8;
			}
		}
	
		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanATSC_SettingListItemArrowScanMode()
	{
		if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
		{
			dvbscanatsc_manu_scanmode = SETTINGS_MANU_SCANMODE_CHAN;
		}else if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
		{
			dvbscanatsc_manu_scanmode = SETTINGS_MANU_SCANMODE_FREQ;
		}

		
		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}

	private void DTVScanATSC_SettingListItemArrowScanBand()
	{
		dvbscanatsc_manu_scanband = (dvbscanatsc_manu_scanband + 1)%SETTINGS_MANU_SCANBAND_MAX;

		if(DTVScanATSC_UpdateChInfoByband(dvbscanatsc_manu_scanband))
		{
			ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
		}
		else
		{
			dvbscanatsc_manu_scanband = (dvbscanatsc_manu_scanband + 1)%SETTINGS_MANU_SCANBAND_MAX;
		}
	}

	private void DTVScanATSC_SettingListItemLeftArrowChNo()
	{
		if(dvbsanatsc_channelallbandlist == null)
			return;
		
		if (dvbscanatsc_manu_chno > 0)
		{
			dvbscanatsc_manu_chno = dvbscanatsc_manu_chno - 1;
		}
		else
		{
			dvbscanatsc_manu_chno = dvbsanatsc_channelallbandlist.length - 1;
		}

		dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[dvbscanatsc_manu_chno].frequency/1000;
		dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[dvbscanatsc_manu_chno].bandwidth;		

		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();
	}		

	private void DTVScanATSC_SettingListItemRightArrowChNo()
	{
		if(dvbsanatsc_channelallbandlist == null)
			return;
		
		if (dvbscanatsc_manu_chno < (dvbsanatsc_channelallbandlist.length - 1))
		{
			dvbscanatsc_manu_chno = dvbscanatsc_manu_chno + 1;		
		}
		else
		{
			dvbscanatsc_manu_chno = 0;
		}	

		dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[dvbscanatsc_manu_chno].frequency/1000;
		dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[dvbscanatsc_manu_chno].bandwidth;	

		ui_dvbsanatsc_setting_list_adapt.notifyDataSetChanged();	
	}	

	private class DTVScanATSC_SettingListAdapter extends BaseAdapter {
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
	
		public DTVScanATSC_SettingListAdapter(Context context) {
			super();
			this.cont = context;
			mInflater=LayoutInflater.from(context);
			/*pic need design*/
			mIcon_setting = BitmapFactory.decodeResource(context.getResources(), R.drawable.dtvscan_setting);
			mIcon_scan = BitmapFactory.decodeResource(context.getResources(), R.drawable.dtvscan_scan);
		}

		public int getCount() {
			return ui_dvbsanatsc_setting_list_count;
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
		  
			switch(dtvscanatsc_scan_mode)
			{
				case DTVSCANATSC_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanATSC.SETTINGS_AUTO_SCAN:
								holder.info.setVisibility(View.GONE);
								displayicon(holder, position, false);
								break;
								
							case DTVScanATSC.SETTINGS_MANU_SCAN:
								holder.info.setVisibility(View.GONE);
								displayicon(holder, position, false);
								break;
								
							case DTVScanATSC.SETTINGS_AREA:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoarea(holder);

								holder.info.setTextColor(Color.WHITE);
								
								if (ui_dvbsanatsc_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;
								
							case DTVScanATSC.SETTINGS_ANTENNA_POWER:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoantennapower(holder);

								holder.info.setTextColor(Color.WHITE);

								if (ui_dvbsanatsc_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;
								
							case DTVScanATSC.SETTINGS_SIGNAL_TYPE:
								holder.info.setVisibility(View.VISIBLE);
								displayinfosignaltype(holder);

								holder.info.setTextColor(Color.WHITE);

								if (ui_dvbsanatsc_setting_list_curitem == position)
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

				case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
					{
						  switch(position)
						  {
							case DTVScanATSC.SETTINGS_MANU_SCANMODE:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoscanmode(holder);

								if (ui_dvbsanatsc_setting_list_curitem == position)
								{
									displayicon(holder, position, true);
									//setlistarrowclicklistener(holder);
								}
								else
								{
									displayicon(holder, position, false);
								}
								break;

							case DTVScanATSC.SETTINGS_SCAN_BAND:
								holder.info.setVisibility(View.VISIBLE);								
								displayinfoscanband(holder);

								if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									if (ui_dvbsanatsc_setting_list_curitem == position)
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
								else if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									holder.info.setTextColor(Color.DKGRAY);
									displayicon(holder, position, false);
								}
								break;
							case DTVScanATSC.SETTINGS_CHNO:
								holder.info.setVisibility(View.VISIBLE);
								displayinfoch(holder);

								if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									if (ui_dvbsanatsc_setting_list_curitem == position)
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
								else if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									displayicon(holder, position, false);
									holder.info.setTextColor(Color.DKGRAY);
								}
								break;
							case DTVScanATSC.SETTINGS_FREQUENCY:
								holder.info.setVisibility(View.VISIBLE);
								displayinfofreq(holder);
								
								if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									holder.info.setTextColor(Color.WHITE);
								}
								else if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									holder.info.setTextColor(Color.DKGRAY);
								}

								displayicon(holder, position, false);
								break;
							case DTVScanATSC.SETTINGS_MODULATION:
								holder.info.setVisibility(View.VISIBLE);
								displayinfomodulation(holder);
									 
								if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
								{
									if (ui_dvbsanatsc_setting_list_curitem == position)
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
								else if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
								{
									displayicon(holder, position, false);
									holder.info.setTextColor(Color.DKGRAY);
								}
								break;
							case DTVScanATSC.SETTINGS_SCAN:
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

			switch(dtvscanatsc_scan_mode)
			{
				case DTVSCANATSC_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanATSC.SETTINGS_AUTO_SCAN:
								vh.icon.setImageBitmap(mIcon_scan);
								break;			
							case DTVScanATSC.SETTINGS_MANU_SCAN:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_AREA:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_ANTENNA_POWER:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_SIGNAL_TYPE:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
								
							default:
								break;					
						}
					}
					break;
				case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanATSC.SETTINGS_MANU_SCANMODE:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_SCAN_BAND:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_CHNO:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_FREQUENCY:	
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_MODULATION:
								vh.icon.setImageBitmap(mIcon_setting);
								break;
							case DTVScanATSC.SETTINGS_SCAN:
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
			switch(dtvscanatsc_scan_mode)
			{
				case DTVSCANATSC_SETTING_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanATSC.SETTINGS_AUTO_SCAN:
								vh.text.setText(R.string.dtvscan_scan_auto);
								break;
							case DTVScanATSC.SETTINGS_MANU_SCAN:
								vh.text.setText(R.string.dtvscan_scan_manual);
								break;
							case DTVScanATSC.SETTINGS_AREA:
								vh.text.setText(R.string.dtvscan_erea);
								break;
							case DTVScanATSC.SETTINGS_ANTENNA_POWER:
								vh.text.setText(R.string.dtvscanatsc_antennapower);
								break;
							case DTVScanATSC.SETTINGS_SIGNAL_TYPE:
								vh.text.setText(R.string.dtvscanatsc_signaltype);
								break;								
							default:
								break;
						}
					}
					break;
					
				case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
					{
						switch(position)
						{
							case DTVScanATSC.SETTINGS_MANU_SCANMODE:	
								vh.text.setText(R.string.dtvscan_scan_mode);
								break;
							case DTVScanATSC.SETTINGS_SCAN_BAND:	
								vh.text.setText(R.string.dtvscan_scan_band);
								break;
							case DTVScanATSC.SETTINGS_CHNO:	
								vh.text.setText(R.string.dtvscan_channel_no);
								break;
							case DTVScanATSC.SETTINGS_FREQUENCY:	
								vh.text.setText(R.string.dtvscan_base_frequence);
								break;
							case DTVScanATSC.SETTINGS_MODULATION:
								vh.text.setText(R.string.dtvscanatsc_modulation);
								break;
							case DTVScanATSC.SETTINGS_SCAN:
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
					//DTVScanATSC_SettingListItemLeftArrowHandle();
				}
			});

			vh.icon2.setOnClickListener(new OnClickListener(){          
				public void onClick(View v) {
					//DTVScanATSC_SettingListItemRightArrowHandle();
				}
			});			
		}
		*/
		
		private void displayinfoarea(ViewHolder vh){
			switch(dvbscanatsc_area)
			{
				case DTVScanATSC.SETTINGS_AREA_USA:	
					vh.info.setText(R.string.dtvscanatsc_erea_usa);
					break;
				case DTVScanATSC.SETTINGS_AREA_CANADA:	
					vh.info.setText(R.string.dtvscanatsc_erea_canada);
					break;
				case DTVScanATSC.SETTINGS_AREA_MEXICO:	
					vh.info.setText(R.string.dtvscanatsc_erea_mexico);
					break;
				default:
					break;
			}				
		}	

		private void displayinfoantennapower(ViewHolder vh){
			if(dvbscanatsc_antennapwoer)
			{
				vh.info.setText(R.string.dtvscanatsc_antennapower_on);
			}else
			{
				vh.info.setText(R.string.dtvscanatsc_antennapower_off);
			}
		}	

		private void displayinfosignaltype(ViewHolder vh){
			if(dfbscanatsc_signaltype == SETTINGS_SIGNAL_CABLE)
			{
				vh.info.setText(R.string.dtvscanatsc_signaltype_cable);
			}else
			{
				vh.info.setText(R.string.dtvscanatsc_signaltype_air);
			}
		}		

		private void displayinfoscanmode(ViewHolder vh){
			switch(dvbscanatsc_manu_scanmode)
			{
				case DTVScanATSC.SETTINGS_MANU_SCANMODE_FREQ:	
					vh.info.setText(R.string.dtvscan_by_frequence);
					break;			
				
				case DTVScanATSC.SETTINGS_MANU_SCANMODE_CHAN:	
					vh.info.setText(R.string.dtvscan_by_channel);
					break;

				default:
					break;
			}
		}

		private void displayinfoscanband(ViewHolder vh){
			/*refresh dvbscanatsc_manu_scanband*/
			
			switch(dvbscanatsc_manu_scanband)
			{
				case DTVScanATSC.SETTINGS_MANU_SCANBAND_VHF:	
					vh.info.setText(R.string.dtvscan_scan_band_vhf);
					break;			
				
				case DTVScanATSC.SETTINGS_MANU_SCANBAND_UHF:	
					vh.info.setText(R.string.dtvscan_scan_band_uhf);
					break;

				default:
					break;
			}
		}	

		private void displayinfoch(ViewHolder vh){
			/*refresh dvbscanatsc_manu_chno and dvbscanatsc_manu_freq*/
			
			vh.info.setText("CH" + dvbscanatsc_manu_chno + "(" + dvbscanatsc_manu_freq + "KHZ)");
		}

		private void displayinfofreq(ViewHolder vh){			
			vh.info.setText(dvbscanatsc_manu_freq + "");
		}		

		private void displayinfomodulation(ViewHolder vh){
			switch(dvbscanatsc_manu_modulation)
			{
				case DTVScanATSC.SETTINGS_MODULATION_QAM_AUTO:
					vh.info.setText(R.string.dtvscanatsc_modulation_qamauto);
					break;
				case DTVScanATSC.SETTINGS_MODULATION_QAM_16:
					vh.info.setText(R.string.dtvscanatsc_modulation_qam16);
					break;
				case DTVScanATSC.SETTINGS_MODULATION_QAM_32:
					vh.info.setText(R.string.dtvscanatsc_modulation_qam32);
					break;			
				case DTVScanATSC.SETTINGS_MODULATION_QAM_64:
					vh.info.setText(R.string.dtvscanatsc_modulation_qam64);
					break;	
				case DTVScanATSC.SETTINGS_MODULATION_QAM_128:
					vh.info.setText(R.string.dtvscanatsc_modulation_qam128);
					break;	
				case DTVScanATSC.SETTINGS_MODULATION_QAM_256:
					vh.info.setText(R.string.dtvscanatsc_modulation_qam256);
					break;					
				case DTVScanATSC.SETTINGS_MODULATION_VSB_8:	
					vh.info.setText(R.string.dtvscanatsc_modulation_vsb8);
					break;
				case DTVScanATSC.SETTINGS_MODULATION_VSB_16:	
					vh.info.setText(R.string.dtvscanatsc_modulation_vsb16);
					break;
				default:
					break;
			}
			
		}
		
	}

	private void DTVScanATSC_UpdateScanBand(int chfreq)
	{
		if(chfreq < 300000)
		{
			dvbscanatsc_manu_scanband = SETTINGS_MANU_SCANBAND_VHF;
		}
		else
		{
			dvbscanatsc_manu_scanband = SETTINGS_MANU_SCANBAND_UHF;
		}
	}

	private boolean DTVScanATSC_UpdateChInfoByband(int scanband)
	{
		boolean ret = false;

		if(dvbsanatsc_channelallbandlist == null)
			return ret;
		
		if(scanband == SETTINGS_MANU_SCANBAND_VHF)
		{
			for (int i = 0; i < dvbsanatsc_channelallbandlist.length; i++)
			{
				if((dvbsanatsc_channelallbandlist[i].frequency/1000) < 300000)
				{
					dvbscanatsc_manu_chno = i; 
					dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[i].frequency/1000;
					dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[i].bandwidth;
					ret = true;
					break;
				}				
			}	
		}
		else if(scanband == SETTINGS_MANU_SCANBAND_UHF)
		{
			for (int i = 0; i < dvbsanatsc_channelallbandlist.length; i++)
			{
				if((dvbsanatsc_channelallbandlist[i].frequency/1000) >= 300000)
				{
					dvbscanatsc_manu_chno = i; 
					dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[i].frequency/1000;
					dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[i].bandwidth;
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
			if (pos > ui_dvbsanatsc_scantv_srv_list.size() || ui_dvbsanatsc_scantv_srv_list.size() <= 0)
			{
				return null;
			}

			si = (serviceInfo) ui_dvbsanatsc_scantv_srv_list.get(pos);
		}
		else if (serviceType == serviceInfo.SERVICE_TYPE_RADIO)
		{
			if (pos > ui_dvbsanatsc_scanradio_srv_list.size() || ui_dvbsanatsc_scanradio_srv_list.size() <= 0)
			{
				return null;
			}

			si = (serviceInfo) ui_dvbsanatsc_scanradio_srv_list.get(pos);
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

	
	private void DTVScanATSC_ScanList(){
		ui_dvbsanatsc_scantv_srv_list.clear();
		ui_dvbsanatsc_scanradio_srv_list.clear();
		ui_dvbsanatsc_scantv_srv_list_index = 0;
		ui_dvbsanatsc_scanradio_srv_list_index = 0;
		ui_dvbsanatsc_scantv_list_adapter.notifyDataSetChanged();
		ui_dvbsanatsc_scanradio_list_adapter.notifyDataSetChanged();
	}

	private void DTVScanATSC_ScanListAddSvr(int service_type, String service_name)
	{
		if ((service_type != serviceInfo.SERVICE_TYPE_TV) && (service_type != serviceInfo.SERVICE_TYPE_RADIO))
		{
			return ;
		}
		
		serviceInfo serviceinfo = new serviceInfo();
		serviceinfo.setServiceType(service_type);
		serviceinfo.setName(service_name);

		Log.d(TAG, "DTVScanATSC_ScanListAddSvr "+ service_name);

		if (service_type == serviceInfo.SERVICE_TYPE_TV)
		{
			serviceinfo.setId(++ui_dvbsanatsc_scantv_srv_list_index);
			ui_dvbsanatsc_scantv_srv_list.add(serviceinfo); 
			ui_dvbsanatsc_scantv_list.setSelection(ui_dvbsanatsc_scantv_srv_list_index - 1);
			ui_dvbsanatsc_scantv_list_adapter.notifyDataSetChanged();
		}
		else
		{
			serviceinfo.setId(++ui_dvbsanatsc_scanradio_srv_list_index);
			ui_dvbsanatsc_scanradio_srv_list.add(serviceinfo); 
			ui_dvbsanatsc_scanradio_list.setSelection(ui_dvbsanatsc_scanradio_srv_list_index - 1);
			ui_dvbsanatsc_scanradio_list_adapter.notifyDataSetChanged();
		}
	}	

	private class DTVScanATSC_ScanListAdapter extends BaseAdapter{
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
     
		public DTVScanATSC_ScanListAdapter(Context context,List<Object> titles){
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

			Log.d(TAG, "getView "+ position);

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
					Log.d(TAG, "fillData "+ serviceinfo.getName());
					mv.srv_id.setText(serviceinfo.getId() + "");
					mv.srv_name.setText(serviceinfo.getName());	
				}
			}
		}
		
	}

	private void DTVScanATSC_StartAutoScan()
	{
		Log.d(TAG, "DTVScanATSC_StartAutoScan");

		TVScanParams sp;	

		sp = TVScanParams.dtvAllbandScanParams(0, TVChannelParams.MODE_ATSC);

		startScan(sp);
	}

	private void DTVScanATSC_StartManuScan()
	{
		Log.d(TAG, "DTVScanATSC_StartManuScan" + dvbscanatsc_manu_freq);
	
		TVScanParams sp;	
	
		switch(dvbscanatsc_manu_modulation)
		{
				case DTVScanATSC.SETTINGS_MODULATION_QAM_AUTO:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;
				case DTVScanATSC.SETTINGS_MODULATION_QAM_16:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;
				case DTVScanATSC.SETTINGS_MODULATION_QAM_32:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;			
				case DTVScanATSC.SETTINGS_MODULATION_QAM_64:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;	
				case DTVScanATSC.SETTINGS_MODULATION_QAM_128:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;	
				case DTVScanATSC.SETTINGS_MODULATION_QAM_256:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;					
				case DTVScanATSC.SETTINGS_MODULATION_VSB_8:	
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;
				case DTVScanATSC.SETTINGS_MODULATION_VSB_16:	
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;
				default:
					sp = TVScanParams.dtvManualScanParams(0, TVChannelParams.atscParams(dvbscanatsc_manu_freq * 1000));
					break;
		}
		
		startScan(sp);
	}

	private void DTVScanATSC_GotoDTVPlayer()
	{	
		Intent intent = new Intent();
		intent.setClass(this, DTVPlayer.class);
		
		startActivityForResult(intent, 1);

		finish();
	}
	
}

