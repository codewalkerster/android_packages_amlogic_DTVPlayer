package com.amlogic.DTVPlayer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import android.os.Bundle;
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
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;

import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.*;
import android.graphics.Color;
import android.content.*;
import android.util.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.view.View.OnLongClickListener; 

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation.AnimationListener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class DTVScanDvbsConfig  extends DTVActivity {
	/** Called when the activity is first created. */
	private final static String TAG = "DTVScanDvbsConfig";
	private static  SharedPreferences mLast = null;
	private SatAdapter mySatAdapter=null;
	private TsAdapter myTsAdapter=null;
	private LnbSetAdapter myLnbSetAdapter=null; 
	
	private List<DbSat> satInfoList=null;
	private List<DbTransponder> tsInfoList=null;

	public static  List<DbSat> ScanSatAndtsInfoList = null;
	private ListView sat_list;
	private ListView sat_lnb_list;
	private int  ListStatus = 0;
	private int list_cur_pos=0;

	private boolean sat_list_focus_flag=true;
	private static int gobal_sat_cur_pos=0;
	private static int gobal_ts_cur_pos=0; 

	AlertDialog.Builder builder;

	LinearLayout tsInfoTitleLayout = null;
	LinearLayout satInfoTitleLayout = null;

	LinearLayout set_limit = null;
	LinearLayout set_location = null;

	private RelativeLayout bufferLayout;
	static Context mContext=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dish_setup_main);
		mContext=this;
		Context otherAppsContext = null;
		try{
			otherAppsContext = createPackageContext(
			"com.amlogic.DTVPlayer", Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
		}
		catch (NameNotFoundException e){
		}
		mLast= PreferenceManager.getDefaultSharedPreferences(otherAppsContext);
		mTVSatellite = new TVSatellite(this);
		DTVScanDvbsConfig_UIInit();
	}
	
	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVScanDvbsConfig_UIInit();

		//DVBControl mControl = new DVBControl("/sys/class/video/screen_mode");
		//mControl.setValue(Integer.toString(0));		
		//mLockDvb.stopPlaying(true);	
	}

	public void onDisconnected(){
		Log.d(TAG, "disconnected");
		super.onDisconnected();
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

	private void DTVScanDvbsConfig_UIInit(){
		sat_list = (ListView)findViewById(R.id.sat_list);

		tsInfoTitleLayout = (LinearLayout)findViewById(R.id.ts_info_title_layout);
		satInfoTitleLayout = (LinearLayout)findViewById(R.id.sat_info_title_layout);

		sat_lnb_list = (ListView)findViewById(R.id.sat_lnb_list);	
		sat_lnb_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
	        {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				ImageView image_cur = (ImageView)arg1.findViewById(R.id.icon1);
				final TextView info_cur = (TextView)arg1.findViewById(R.id.info);	
				switch(arg2){
					case DISHSETUP_LNB_TYPE:
						showSatLnbTypeDia();
						break;
				       case DISHSETUP_LNB_POWER:
					 	showLnbPowerDia(info_cur);	
				    		break;
					case DISHSETUP_LNB_22K:
						showLnb22kDia(info_cur);	
						break; 
			     	case DISHSETUP_LNB_TONEBURST:
						showLnbToneBurstDia(info_cur);
						break;
					/*		 
					case DISHSETUP_LNB_DISEQC:
						 showLnbDiseqcModeDia();
						break;
					case DISHSETUP_LNB_COMMITTED_COMMAND:
						showLnbDiseqcCommittedDia();
						break;	
						
					case DISHSETUP_LNB_DISEQC_REPEATS:
						dealDiseqcRepeatItem(arg1);
						break;
					case DISHSETUP_LNB_DISEQC_SEQUENCE:
						showLnbDiseqcSequence();
						break;
					case DISHSETUP_LNB_UNCOMMITTED_COMMAND:
						showLnbDiseqcUncommittedDia();
						break;
					case DISHSETUP_LNB_FAST_DISEQC:
						dealFastDiseqcItem(arg1);
						break;
					case DISHSETUP_LNB_MOTO_NO:
						showMotoNoDia();
						break;	
					*/
					case DISHSETUP_DISEQC1_0:
						showLnbDiseqcCommittedDia(info_cur);
						break;
					case DISHSETUP_DISEQC1_1:
						showLnbDiseqcUncommittedDia(info_cur);
						break;
					case DISHSETUP_DISEQC_MOTOR:
						showLnbDiseqcModeDia(info_cur);
						break;
				}
			}	        	   
	        });

		/*get data*/
		ScanSatAndtsInfoList = getScanListSatAndTsDataFromBuildDB();

		sat_list.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
		int position, long id) {
			Log.d(TAG,"sat_list setOnItemSelectedListener " + position);

			list_cur_pos = position;
			if(ListStatus==0){
				gobal_ts_cur_pos = 0;
				gobal_sat_cur_pos = list_cur_pos;
				TextView sat_name = (TextView) findViewById(R.id.sat_name);
				sat_name.setTextColor(Color.YELLOW);
				sat_name.setText(ScanSatAndtsInfoList.get(list_cur_pos).getName());
				if(myLnbSetAdapter!=null)
					myLnbSetAdapter.notifyDataSetChanged();
			}	
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
		});

		sat_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				ImageView image_cur = (ImageView)arg1.findViewById(R.id.icon);
				if(ListStatus ==0){					
					if(((DbSat)ScanSatAndtsInfoList.get(arg2)).getSelectedFlag()==false){
						((DbSat)ScanSatAndtsInfoList.get(arg2)).setSelectedFlag(true);
						image_cur.setBackgroundResource(R.drawable.selected);
					}	
					else{
						((DbSat)ScanSatAndtsInfoList.get(arg2)).setSelectedFlag(false);
						image_cur.setBackgroundResource(0);
					}
				}
				else{

					if(((DbTransponder)tsInfoList.get(arg2)).getSelectedFlag()==false){
						((DbTransponder)tsInfoList.get(arg2)).setSelectedFlag(true);
						image_cur.setBackgroundResource(R.drawable.selected);
					}	
					else{
						((DbTransponder)tsInfoList.get(arg2)).setSelectedFlag(false);
						image_cur.setBackgroundResource(0);
					}
				}
			}
	        });
		/*
		new Thread(){
	            @Override
	            public void run(){
	            	
	
			Message message=new Message();
			message.what=500;
			
			EventHandler ha =new EventHandler(Looper.getMainLooper());  
			ha.sendMessage(message);
			
	            }
	        }.start();

		*/
		if(ScanSatAndtsInfoList==null)
			ScanSatAndtsInfoList = new ArrayList<DbSat>();
		satInfoList = ScanSatAndtsInfoList;	   
		
		show_no_satellites_info();
		
		if (null == mySatAdapter){
			mySatAdapter = new SatAdapter(DTVScanDvbsConfig.this,ScanSatAndtsInfoList);
		}

		sat_list.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean isFocused)
			{
				// TODO Auto-generated method stub
				if (isFocused==true){
					sat_list_focus_flag=true;
					Log.d(TAG,"focus on"+v.getId());
				}
				else { 
					sat_list_focus_flag=false;
				}
				if(ListStatus==0)
					mySatAdapter.notifyDataSetChanged();
				else
					myTsAdapter.notifyDataSetChanged();
			}
		});


		sat_list.setAdapter(mySatAdapter);
		mySatAdapter.notifyDataSetChanged();
		ListStatus = 0;

		//sat lnb
		if (null == myLnbSetAdapter){
			myLnbSetAdapter = new LnbSetAdapter(DTVScanDvbsConfig.this);
		}
		sat_lnb_list.setAdapter(myLnbSetAdapter);
		myLnbSetAdapter.notifyDataSetChanged();

		sat_lnb_list.setOnKeyListener( new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_LEFT:
						if(ScanSatAndtsInfoList.size()>0){

							sat_list.requestFocus();
							sat_list.setSelection(list_cur_pos);
						}			
						return true;
				}
				return false;
            }
			
		});	

		
		//ts
		if(getTsData(gobal_sat_cur_pos)!=null){
			if (null == myTsAdapter)
		       {
		    	   myTsAdapter = new TsAdapter(DTVScanDvbsConfig.this,tsInfoList);
		       }   
		}
	
		sat_list.setOnItemLongClickListener(new OnItemLongClickListener() {		
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view,
								int position, long id) {
							Log.d(TAG,"long Click");
							list_cur_pos = position;
							if(ListStatus==0){
								gobal_ts_cur_pos = 0;
								gobal_sat_cur_pos = list_cur_pos;
							}	
							createMenuChoiceDialog();
							return true;
						}
					});
		sat_list.setOnLongClickListener (new OnLongClickListener () {
						@Override
						public boolean onLongClick(View v) {
							Log.d(TAG,"long Click");
							if(ListStatus==1)
								showTsAddDia();
							return false;
						}
					});
		
		TextView test = (TextView)this.findViewById(R.id.no_data_info);
		
		test.setOnLongClickListener (new OnLongClickListener () {
			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG,"long Click");
				if(ListStatus ==0){
					showSatAddDia();
				}
				else{
					showTsAddDia();
				}
				return false;
			}
		});
	}

	protected void onStart(){
		super.onStart();
	}

	private void createMenuChoiceDialog(){
	
		String[] itemChoices = {getString(R.string.dish_setup_conf_button_des1_info),getString(R.string.dish_setup_conf_button_des2_info),getString(R.string.dish_setup_conf_button_des3_info),getString(R.string.dish_setup_conf_button_des4_info),getString(R.string.dish_setup_conf_button_des6_info),getString(R.string.dish_setup_conf_button_des8_info)};
		String[] itemChoices1 = {getString(R.string.dish_setup_conf_button_des1_info),getString(R.string.dish_setup_conf_button_des2_info),getString(R.string.dish_setup_conf_button_des3_info),getString(R.string.dish_setup_conf_button_des4_info),getString(R.string.dish_setup_conf_button_des6_info),getString(R.string.dish_setup_conf_button_des8_info),getString(R.string.dish_setup_conf_button_des7_info)};
		
		AlertDialog mMenuChoiceDialog = new AlertDialog.Builder(this)
						   .setItems(ListStatus ==0?itemChoices:itemChoices1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
				if(ListStatus ==0&&ScanSatAndtsInfoList.size()>0){
					ListStatus =1;
					gobal_sat_cur_pos = list_cur_pos;
					getTsData(gobal_sat_cur_pos);
					myTsAdapter = new TsAdapter(DTVScanDvbsConfig.this,tsInfoList);
					sat_list.setAdapter(myTsAdapter);
					myTsAdapter.notifyDataSetChanged();
					sat_list.requestFocus();
					//sat_list.setSelection(gobal_ts_cur_pos);

					satInfoTitleLayout.setVisibility(View.INVISIBLE);
					satInfoTitleLayout.setVisibility(View.GONE);
					tsInfoTitleLayout.setVisibility(View.VISIBLE);
					show_bottom_set_limit();
					
					show_no_satellites_info();
				}
				else{
					ListStatus=0;
					gobal_ts_cur_pos = list_cur_pos;
					sat_list.setAdapter(mySatAdapter);
					mySatAdapter.notifyDataSetChanged();
					sat_list.requestFocus();
					sat_list.setSelection(gobal_sat_cur_pos);
					
					tsInfoTitleLayout.setVisibility(View.INVISIBLE);
					tsInfoTitleLayout.setVisibility(View.GONE);
					satInfoTitleLayout.setVisibility(View.VISIBLE);
					hide_bottom_set_limit();
					show_no_satellites_info();
				}		
				break;
			case 1:
				if(ListStatus ==0){
					showSatAddDia();
				}
				else{
					showTsAddDia();
				}
				
				break;
			case 2:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_REPEAT");
				if(ListStatus ==0){
					showSatEditDia();
				}
				else{
					showTsEditDia();
				}
				break;
			case 3: 
				Log.d(TAG,"KEY:KEYCODE_MEDIA_INFO");
				showDeleteDia();
				break;
			case 4:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_RATIO");
				if(ScanSatAndtsInfoList.size()>0)
					showScanConfigDia();
				break;
			case 5:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_AUDIO");
				showSetLocationDia();
				break;	
			case 6:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_SUB_T");
				if(ListStatus==1)
					showSetLimitAndPositionDia();
				break;
			
			default:
				break;
			}

			}
			}).create();
		
			mMenuChoiceDialog.show();
			WindowManager.LayoutParams lp=mMenuChoiceDialog.getWindow().getAttributes();
			lp.dimAmount=0.0f;
			mMenuChoiceDialog.getWindow().setAttributes(lp);
			mMenuChoiceDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	protected void onStop(){
		Log.d(TAG,"onStop");	
		super.onStop();
	}

	protected void onDestory(){
		super.onDestroy();
	}

	private void getUnicableSetting() {
		
	}
	/*
	private DVBUnicableSetting getUnicableSetting() {
		DVBUnicableSetting set = null;
		
		if (mLockDvb != null) {
			set = new DVBUnicableSetting();
			mLockDvb.getUnicableSetting(set);
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
	
	private void show_no_satellites_info(){
		 TextView no_data_info = (TextView)this.findViewById(R.id.no_data_info);
		if(ListStatus==0&&ScanSatAndtsInfoList.size()==0){
			 no_data_info.setText(getString(R.string.no_scan_list));
			 no_data_info.setVisibility(View.VISIBLE);
			 
			 if(myLnbSetAdapter!=null)
					myLnbSetAdapter.notifyDataSetChanged();
			 TextView sat_name = (TextView) findViewById(R.id.sat_name);
			 sat_name.setText("");
		}
		else if(ListStatus==1&&(ScanSatAndtsInfoList.get(gobal_sat_cur_pos).transponder==null||(ScanSatAndtsInfoList.get(gobal_sat_cur_pos).transponder!=null&&ScanSatAndtsInfoList.get(gobal_sat_cur_pos).transponder.size()==0))){
			no_data_info.setText("No TP information,please add");
			no_data_info.setVisibility(View.VISIBLE);
		}
		else{
			 no_data_info.setVisibility(View.INVISIBLE);
			 no_data_info.setVisibility(View.GONE);
		}
	}

	private void show_bottom_set_limit(){
		set_limit = (LinearLayout)findViewById(R.id.editProgramOpDes07);
		set_limit.setVisibility(View.VISIBLE);
	}

	private void show_bottom_set_location(){
		set_location = (LinearLayout)findViewById(R.id.editProgramOpDes08);
		set_location.setVisibility(View.VISIBLE);
	}

	private void hide_bottom_set_limit(){
		set_limit = (LinearLayout)findViewById(R.id.editProgramOpDes07);
		set_limit.setVisibility(View.INVISIBLE);
		set_limit.setVisibility(View.GONE);
	}

	private void hide_bottom_set_location(){
		set_location = (LinearLayout)findViewById(R.id.editProgramOpDes08);
		set_location.setVisibility(View.INVISIBLE);
		set_location.setVisibility(View.GONE);
	}

	private TVSatellite mTVSatellite=null;
	private TVSatellite[] list_sat=null;
	private  List<DbSat>  getScanListSatAndTsDataFromBuildDB(){
		int n=0;
		int numColumn=0;
		int sat_no=0;

		List<DbSat> DbSat = new ArrayList<DbSat>();

	 	list_sat = mTVSatellite.tvSatelliteList(mContext);
		if(list_sat!=null){
			n = list_sat.length;
			for(int i=0;i<n;i++){
				DbSat satInfoNode = new DbSat();	
				TVSatellite temp = list_sat[i];
				
				int  sat_id = temp.getSatelliteId();				
				//Log.d(TAG,"sat_id"+sat_id);			
				satInfoNode.setSatId(sat_id);
							
				double sat_longitude = temp.tv_satparams.getSatelliteLongitude();		
				//Log.d(TAG,"sat_longitude"+sat_longitude);			
				satInfoNode.setSatLongitude(sat_longitude);
				
				//selected	
				satInfoNode.setFlags(0);						

				int  pos_num = temp.tv_satparams.getMotorPositionNum();				
				//Log.d(TAG,"pos_num"+pos_num);			
				satInfoNode.setPositionNumber(pos_num);	

					
				String name = temp.getSatelliteName();				
				//Log.d(TAG,"sat_name"+name);				
				satInfoNode.setName(name);		

				satInfoNode.setLNBType(0);	

				int  lof_lo = temp.tv_satparams.getSatelliteLnbLofLo();		
				//Log.d(TAG,"lof_lo"+lof_lo);			
				satInfoNode.setLoLOF(lof_lo);	

				int  lof_hi= temp.tv_satparams.getSatelliteLnbLofhi();			
				//Log.d(TAG,"lof_hi"+lof_hi);			
				satInfoNode.setHiLOF(lof_hi);

				int  lof_threshold= temp.tv_satparams.getSatelliteLnbLofthreadhold();			
				//Log.d(TAG,"lof_threshold"+lof_threshold);			
				satInfoNode.setLofThreshold(lof_threshold);
				
				int  voltage = temp.tv_satparams.getSecVoltage();  		
				//Log.d(TAG,"voltage"+voltage);			
				satInfoNode.setLNBPwrOnOff(voltage);	

				int  signal_22khz = temp.tv_satparams.getSec22k();  	  		
				//Log.d(TAG,"signal_22khz"+signal_22khz);			
				satInfoNode.set22KOnOff(signal_22khz);	

				int  tone_burst = temp.tv_satparams.getSecToneBurst();  	 		
				//Log.d(TAG,"tone_burst"+tone_burst);			
				satInfoNode.setToneburstType(tone_burst);	
				
				int  diseqc_mode = temp.tv_satparams.getDiseqcMode();  	
				//Log.d(TAG,"diseqc_mode"+diseqc_mode);				
				satInfoNode.setSwtPort(diseqc_mode);

				int  lnb_diseqc_mode_config10 = temp.tv_satparams.getDiseqcCommitted();  	 		
				//Log.d(TAG,"lnb_diseqc_mode_config1.0"+lnb_diseqc_mode_config10);			
				satInfoNode.setLnbConfig10(lnb_diseqc_mode_config10);	

				int  lnb_diseqc_mode_config11 = temp.tv_satparams.getDiseqcUncommitted(); 		
				//Log.d(TAG,"lnb_diseqc_mode_config1.1"+lnb_diseqc_mode_config11);			
				satInfoNode.setLnbConfig11(lnb_diseqc_mode_config11);	
			
				satInfoNode.setMotMode(0);

				int  moto_no = temp.tv_satparams.getMotorNum(); 	 		
				//Log.d(TAG,"motor_num"+moto_no);			
				satInfoNode.setMotoNo(moto_no);

				int  fast_diseqc = temp.tv_satparams.getDiseqcFast();   		
				//Log.d(TAG,"fast_diseqc"+fast_diseqc);			
				satInfoNode.setFastDiseqc(fast_diseqc);

				int  sequence_repeat = temp.tv_satparams.getDiseqcSequenceRepeat();   		
				//Log.d(TAG,"sequence_repeat"+sequence_repeat);			
				satInfoNode.setDiseqcRepeat(sequence_repeat);

				int  cmd_order = temp.tv_satparams.getDiseqcOrder();    		
				//Log.d(TAG,"diseqc_sequence"+cmd_order);			
				satInfoNode.setDiseqcSequence(cmd_order);

				double  longitude = temp.tv_satparams.getSatelliteRecLocalLongitude();   		
				//Log.d(TAG,"longitude"+longitude);			
				satInfoNode.setLongitude(longitude);

				double  latitude = temp.tv_satparams.getSatelliteRecLocalLatitude();  	
				//Log.d(TAG,"latitude"+latitude);			
				satInfoNode.setLatitude(latitude);
				
				DbSat.add(satInfoNode);	
			}
		}
		
		return DbSat;		
	}


		
	private static TVChannel[] list_ts=null;	
	static List<DbTransponder> tsInfoArrayList=null;
	static List<DbTransponder> getScanTsList(DbSat satInfoNode){
		int n=0,numColumn=0;
		if(satInfoNode.transponder==null){
			int sat_id = satInfoNode.getSatId();

			list_ts = TVChannel.tvChannelList(mContext,sat_id);
			if(list_ts!=null){
				n = list_ts.length;
				tsInfoArrayList = new ArrayList<DbTransponder>();
				for(int i=0;i<n;i++){
					DbTransponder tsInfoNode = new DbTransponder();
					TVChannel temp = list_ts[i];

					int  ts_sat_id = sat_id;
					//Log.d(TAG,"ts_scan_id"+ts_scan_id);
					tsInfoNode.setSatId(ts_sat_id);

					int  db_id = temp.getID();
					//Log.d(TAG,"db_id"+db_id);
					tsInfoNode.setDbId(db_id);

					int  ts_id =  temp.getDVBTSID();
					//Log.d(TAG,"ts_id"+ts_id);
					tsInfoNode.setId(ts_id);

					int  frequency = temp.params.getFrequency();
					//Log.d(TAG,"frequency"+frequency);
					tsInfoNode.setFrequency(frequency);

					int  polarization = temp.params.getPolarisation();
					//Log.d(TAG,"polarization"+polarization);
					tsInfoNode.setPolarization(polarization);	

					int symbol = temp.params.getSymbolRate();
					//Log.d(TAG,"symbol"+symbol);	
					tsInfoNode.setSymbol(symbol);
					
					tsInfoArrayList.add(tsInfoNode);	
				}
				satInfoNode.transponder=tsInfoArrayList;
			}	
		}
		
		return satInfoNode.transponder;			
	}


	 @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	   switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:	
				if((list_cur_pos== sat_list.getCount()-1)&&sat_list_focus_flag)
				    	sat_list.setSelection(0); 			
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				  if(list_cur_pos== 0&&sat_list_focus_flag)
					sat_list.setSelection(sat_list.getCount()-1); 
				  break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				if(ListStatus ==0&&ScanSatAndtsInfoList.size()>0){
					ListStatus =1;
					gobal_sat_cur_pos = list_cur_pos;
					
					getTsData(gobal_sat_cur_pos);
					myTsAdapter = new TsAdapter(DTVScanDvbsConfig.this,tsInfoList);
					sat_list.setAdapter(myTsAdapter);
					myTsAdapter.notifyDataSetChanged();
					sat_list.requestFocus();
					//sat_list.setSelection(gobal_ts_cur_pos);

					satInfoTitleLayout.setVisibility(View.INVISIBLE);
					satInfoTitleLayout.setVisibility(View.GONE);
					tsInfoTitleLayout.setVisibility(View.VISIBLE);
					show_bottom_set_limit();
					show_no_satellites_info();
				}
				else{
					ListStatus=0;
					gobal_ts_cur_pos = list_cur_pos;
					sat_list.setAdapter(mySatAdapter);
					mySatAdapter.notifyDataSetChanged();
					sat_list.requestFocus();
					sat_list.setSelection(gobal_sat_cur_pos);
					
					tsInfoTitleLayout.setVisibility(View.INVISIBLE);
					tsInfoTitleLayout.setVisibility(View.GONE);
					satInfoTitleLayout.setVisibility(View.VISIBLE);
					hide_bottom_set_limit();
					show_no_satellites_info();
				}		
				return true;
			case KeyEvent.KEYCODE_ZOOM_OUT:
				if(ListStatus ==0){
					showSatAddDia();
				}
				else{
					showTsAddDia();
				}
				break;
			case KeyEvent.KEYCODE_TV_REPEAT:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_REPEAT");
				if(ListStatus ==0){
					showSatEditDia();
				}
				else{
					showTsEditDia();
				}
				break;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_REWIND");
				if(ListStatus==1)
					showTransponderSignalInfoDialog();
				break;
			case KeyEvent.KEYCODE_TAB: 
				Log.d(TAG,"KEY:KEYCODE_MEDIA_INFO");
				showDeleteDia();
				break;
			case KeyEvent.KEYCODE_TV_SWITCH: 
				Log.d(TAG,"KEY:KEYCODE_TV_SYSTEM");
				break;
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_DISPAYMODE:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_RATIO");
				
				/*if(mLockDvb.isRecording()){
					Toast toast = Toast.makeText(
					DTVScanDvbsConfig.this, 
			    		R.string.pvr_is_running,
			    		Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show(); 	
				}
				else
				*/{
					if(ScanSatAndtsInfoList.size()>0)
						showScanConfigDia();
				}
				
				break;
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_FAST_FORWARD");
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_NEXT");
				break;
			
			case KeyEvent.KEYCODE_TV_SHORTCUTKEY_VOICEMODE:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_AUDIO");
				showSetLocationDia();
				break;
			case KeyEvent.KEYCODE_TV_SUBTITLE:
				Log.d(TAG,"KEY:KEYCODE_MEDIA_SUB_T");
				if(ListStatus==1)
					showSetLimitAndPositionDia();
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				
				break;
			case KeyEvent.KEYCODE_BACK:
				if(ListStatus ==1){
					ListStatus=0;
					gobal_ts_cur_pos = list_cur_pos;
					sat_list.setAdapter(mySatAdapter);
					mySatAdapter.notifyDataSetChanged();
					sat_list.requestFocus();
					sat_list.setSelection(gobal_sat_cur_pos);
					
					tsInfoTitleLayout.setVisibility(View.INVISIBLE);
					tsInfoTitleLayout.setVisibility(View.GONE);
					satInfoTitleLayout.setVisibility(View.VISIBLE);
					hide_bottom_set_limit();
					show_no_satellites_info();
					return true;
				}
				else{
					returnSettings();
					DTVScanDvbsConfig.this.finish();
				}	
				break;
		}
		
		return super.onKeyDown(keyCode, event);
	}	

	private void returnSettings(){
		Intent intent = new Intent();
		intent.setClass(DTVScanDvbsConfig.this, DTVSettingsMenu.class);
		startActivity(intent);
	}


	public void dealEnterKey(){
		if(ListStatus ==0){
		}
		else{
		
		}	
	}
	
	private DbTransponder  queryTsData(int cur_pos){
		return getTsInfoByPostion(cur_pos);	 
	}


	private boolean deleteSelectedSatData(){
		boolean selected_flag=false;
		if(ScanSatAndtsInfoList.size()>0){
			Log.d(TAG,"sat ts para size=="+ScanSatAndtsInfoList.size());
			int size=ScanSatAndtsInfoList.size();
			int i=0;
			while(i<ScanSatAndtsInfoList.size()){
				DbSat sat_temp = ScanSatAndtsInfoList.get(i);		
				if(sat_temp.getSelectedFlag()){
					//add to default list	
					deleteSatData(i);
					ScanSatAndtsInfoList.remove(i);
					selected_flag=true;
					i--;
					Log.d(TAG,"------delete>>"+i);
				}
				i++;
			}
		}
		return selected_flag;
	}

	private TVSatellite[] removeSatFromList(TVSatellite[] a,int index){
	    int len=a.length;
	    if(index<0||index>=len){
	        return a;
	    }
	    TVSatellite[] result=new TVSatellite[len-1];
	    System.arraycopy(a,0,result,0,index);
	    System.arraycopy(a,index+1,result,index,len-index-1);
	    return result;
	}

	private TVChannel[] removeTsFromList(TVChannel[] a,int index){
	    int len=a.length;
	    if(index<0||index>=len){
	        return a;
	    }
	    TVChannel[] result=new TVChannel[len-1];
	    System.arraycopy(a,0,result,0,index);
	    System.arraycopy(a,index+1,result,index,len-index-1);
	    return result;
	}
	
	private void deleteSatData(int cur_pos){
		DbSat sat_node = getSatInfoByPostion(cur_pos);
		int sat_id = sat_node.getSatId();
		Log.d(TAG,"sat_id"+sat_id);
		mTVSatellite.tvSatelliteDel(this,sat_id);

		list_sat = mTVSatellite.tvSatelliteList(mContext);
		if(list_sat!=null){
			if(cur_pos<list_sat.length){
				list_sat[cur_pos].tvSatelliteDel(this,sat_id);
				list_sat = removeSatFromList(list_sat,cur_pos);
			}
		}
	}

	private boolean deleteSelectedTsData(int cur_sat_pos){
		boolean selected_flag=false;
		
		if(tsInfoList.size()>0){
			Log.d(TAG,"tp size=="+tsInfoList.size());
			int i=0;
			while(i<tsInfoList.size()){
				DbTransponder ts_node = tsInfoList.get(i);			
				if(ts_node.getSelectedFlag()){
					//add to default list	
					deleteTsData(cur_sat_pos,i);
					tsInfoList.remove(i);
					selected_flag=true;
					i--;
				}
				i++;
			}
		}
		return selected_flag;
	}

	
	private void  deleteTsData(int cur_sat_pos, int cur_pos){
		DbTransponder ts_node = getTsInfoByPostion(cur_pos);
		int db_id = ts_node.getDbId();
		
		DbSat sat_node = getSatInfoByPostion(cur_sat_pos);
		int sat_id  = sat_node.getSatId();

		list_ts = TVChannel.tvChannelList(mContext,ts_node.getSatId());
		if(cur_pos<list_ts.length){
			list_ts[cur_pos].tvChannelDel(mContext);
			list_ts = removeTsFromList(list_ts,cur_pos);
		}	
	}

	private boolean getConflictTp(int scan_id, int fre, int sym, int polarization){
		int n=0;
		/*
		Cursor cur_ts =this.getContentResolver().query(DVBClient.TABLE_TS, null,"db_sat_para_id="+scan_id+" and freq="+fre+" and symb="+sym +" and polar="+polarization, null, null);	
		if(cur_ts!=null){
			n = cur_ts.getCount();
			cur_ts.close();
			if(n>0){	
				return true;
			}				
			else	{					
				return false;
			}										
		}	
		*/
		return false;
	}

	private boolean getConflictSat(int scan_id, double angle){
		int n=0;
		/*
		Cursor cur =this.getContentResolver().query(DVBClient.TABLE_SAT_PARA, null,"sat_longitude="+angle, null, null);	
		if(cur!=null){
			n = cur.getCount();
			cur.close();
			if(n>0){	
				return true;
			}				
			else	{					
				return false;
			}								
		}	
		*/
		return false;		
	}
	
	private int getSatDbIdByAngle(int angle) {
		int db_id = -1;
		/*
		Cursor cur =this.getContentResolver().query(DVBClient.TABLE_SAT_PARA, 
			new String[]{"db_id"},"sat_longitude="+angle, null, null);	
		if(cur != null){
			if (cur.moveToFirst()) {
				db_id = cur.getInt(0);
			}
			cur.close();						
		}	
		*/
		return db_id;
	}
	
	private int getTsDbId(int db_sat_id, int freq, int sym, int polar) {
		int db_id = -1;
		/*
		Cursor cur =this.getContentResolver().query(DVBClient.TABLE_TS, 
		new String[]{"db_id"},
		"db_sat_para_id="+db_sat_id+" and freq="+freq+" and symb="+sym +" and polar="+polar, null, null);	
		if(cur!=null){
			if (cur.moveToFirst()) {
				db_id = cur.getInt(0);
			}
			cur.close();						
		}	
		*/
		return db_id;
	}

	private void editSatData(int sat_id,ContentValues values,String mode){
		
		TVSatellite temp_TVSatellite = TVSatellite.tvSatelliteSelect(mContext,sat_id);

		if(mode.equals("sat")){
			String name = (String)(Object)values.get("sat_name");
			temp_TVSatellite.setSatelliteName(name);
			int sat_longitude = (int)values.getAsInteger("sat_longitude");
			temp_TVSatellite.setSatelliteLongitude((double)sat_longitude);
		}
		else if(mode.equals("tone_burst")){
			int tone_burst = (int)values.getAsInteger("tone_burst");
			temp_TVSatellite.setSecToneBurst(tone_burst);
		}
		else if(mode.equals("motor_num")){
			int motor_num = (int)values.getAsInteger("motor_num");
			temp_TVSatellite.setMotorNum(motor_num);
		}
		else if(mode.equals("voltage")){
			int voltage = (int)values.getAsInteger("voltage");
			temp_TVSatellite.setSecVoltage(voltage);
		}
		else if(mode.equals("signal_22khz")){
			int signal_22khz = (int)values.getAsInteger("signal_22khz");
			temp_TVSatellite.setSec22k(signal_22khz);
		}
		
		else if(mode.equals("lof")){
			int lof_lo = (int)values.getAsInteger("lof_lo");
			int lof_hi = (int)values.getAsInteger("lof_hi");
			int lof_threshold = (int)values.getAsInteger("lof_threshold");
			temp_TVSatellite.setSatelliteLnb(0,lof_hi,lof_lo,lof_threshold);
		}
		else if(mode.equals("diseqc_mode")){
			int diseqc_mode = (int)values.getAsInteger("diseqc_mode");
			temp_TVSatellite.setDiseqcMode(diseqc_mode);
		}
		else if(mode.equals("committed_cmd")){
			int committed_cmd = (int)values.getAsInteger("committed_cmd");
			temp_TVSatellite.setDiseqcCommitted(committed_cmd);
		}
		else if(mode.equals("uncommitted_cmd")){
			int uncommitted_cmd = (int)values.getAsInteger("uncommitted_cmd");
			temp_TVSatellite.setDiseqcUncommitted(uncommitted_cmd);
		}
		else if(mode.equals("cmd_order")){
			int uncommitted_cmd = (int)values.getAsInteger("uncommitted_cmd");
			temp_TVSatellite.setDiseqcOrder(uncommitted_cmd);
		}
		else if(mode.equals("sequence_repeat")){
			int uncommitted_cmd = (int)values.getAsInteger("uncommitted_cmd");
			temp_TVSatellite.setDiseqcSequenceRepeat(uncommitted_cmd);
		}
		else if(mode.equals("fast_diseqc")){
			int uncommitted_cmd = (int)values.getAsInteger("fast_diseqc");
			temp_TVSatellite.setDiseqcFast(uncommitted_cmd);
		}
		else if(mode.equals("pos_num")){
			int pos_num =  (int)values.getAsInteger("pos_num");
			temp_TVSatellite.setMotorPositionNum(pos_num);
		}
		else if(mode.equals("local_longitude_latitude")){
			double longitude =  (double)values.getAsDouble("longitude");			
			double latitude =  (double)values.getAsDouble("latitude");
			temp_TVSatellite.setSatelliteRecLocal((double)longitude,(double)latitude);
		}
		
	}

	private void addSatData(String name,double angle){
		DbSat sat_node  = new DbSat();
		//ContentValues values;

		TVSatellite temp_TVSatellite = new TVSatellite(mContext,name,(double)angle);
		temp_TVSatellite.setDiseqcMode(DbSat.LNB_DISEQC_11);
		/*
		values.put("sat_name", name);
		values.put("sat_longitude",angle);
		values.put("lnb_num",0);
		values.put("lof_hi",10600);
		values.put("lof_lo",9750);
		values.put("lof_threshold",11700);
		values.put("signal_22khz",2);
		values.put("voltage",3);
		values.put("motor_num",0);
		values.put("pos_num",0);
		
		values.put("lo_direction",0);
		values.put("la_direction",0);
		values.put("longitude",0);
		values.put("latitude",0);
		
		values.put("diseqc_mode",0);
		values.put("tone_burst",0);
		values.put("committed_cmd",0);
		values.put("uncommitted_cmd",11700);
		values.put("repeat_count",3);
		values.put("sequence_repeat",0);
		values.put("fast_diseqc",0);
		values.put("cmd_order",0);
		*/

		Log.d(TAG,"add sat sat_id==="+temp_TVSatellite.getSatelliteId());
		sat_node.setSatId(temp_TVSatellite.getSatelliteId());
		sat_node.setName(name);
		sat_node.setSatLongitude(angle);	
		sat_node.setPositionNumber(0);	
		sat_node.setLnbNo(0);
		sat_node.setLoLOF(9750*1000);	
		sat_node.setHiLOF(10600*1000);
		sat_node.setLofThreshold(11700*1000);
		sat_node.setLNBPwrOnOff(3);	
		sat_node.set22KOnOff(2);	
		sat_node.setToneburstType(0);	
		sat_node.setSwtPort(DbSat.LNB_DISEQC_11);
		sat_node.setLnbConfig10(4);	//None
		sat_node.setLnbConfig11(0);	
		sat_node.setMotoNo(0);
		sat_node.setFastDiseqc(0);
		sat_node.setDiseqcRepeat(0);
		sat_node.setDiseqcSequence(0);
		sat_node.setLongitude(0);
		sat_node.setLatitude(0);
		sat_node.setScanId(getSatDbIdByAngle((int)angle));

		ScanSatAndtsInfoList.add(sat_node);
	}
	

	private void editTsData(int cur_sat_pos, int cur_pos,ContentValues values){
		
		DbTransponder ts_node = getTsInfoByPostion(cur_pos);
		int db_id  = ts_node.getDbId();
		Log.d(TAG,"db_id"+db_id);

		DbSat sat_node = getSatInfoByPostion(cur_sat_pos);
		int sat_id = sat_node.getSatId();

		int fre = (int)values.getAsInteger("freq");
		int sym = (int)values.getAsInteger("symb");
		int polar = (int)values.getAsInteger("polar");

		TVChannel.selectByID(this, db_id).setFrequency(fre);
		TVChannel.selectByID(this, db_id).setSymbolRate(sym);
		TVChannel.selectByID(this, db_id).setPolarisation(polar);
	}

	private void addTsData(int cur_sat_pos, ContentValues values, DbTransponder ts){
		
		DbSat sat_node = getSatInfoByPostion(cur_sat_pos);
		int sat_id = sat_node.getSatId();
		
		values.put("db_sat_para_id",sat_id);
		values.put("ts_id",0xffff);

		//values.put("src",DVBClient.SCAN_DVBS>>8);
		values.put("db_net_id",-1);
		values.put("mod",0);
		values.put("bw",0);
		values.put("snr",0);
		values.put("ber",0);
		values.put("strength",0);
		values.put("dvbt_flag",0);

		Log.d(TAG,"fre="+ts.getFrequency()+"sym="+ts.getSymbol()+"sat id="+sat_id+"polar+"+ts.getPolarization());
		//TVChannelParams temp = new TVChannelParams(TVChannelParams.MODE_QPSK);
		TVChannelParams temp = TVChannelParams.dvbsParams(mContext,ts.getFrequency(), ts.getSymbol(), sat_id,ts.getPolarization());
		TVChannel TVChannel_temp = new TVChannel(mContext,temp);

		Log.d(TAG,"tp id="+TVChannel_temp.getID());
		ts.setDbId(TVChannel_temp.getID());
		
		if(tsInfoList!=null)
			tsInfoList.add(ts);
		else{
			List<DbTransponder> tsInfoList = new ArrayList<DbTransponder>();
			tsInfoList.add(ts);
			sat_node.transponder = tsInfoList;
		}	
		
	}

	/*
	private void showDeleteDia(){
		if(ScanSatAndtsInfoList==null||ScanSatAndtsInfoList.size()==0)
			return;
		else{
			if((ListStatus==1)&&(tsInfoList==null)){
				return;
			}
		}
		
		builder = new AlertDialog.Builder(DTVScanDvbsConfig.this);
		builder.setMessage(R.string.sure_delete)
		    .setCancelable(false)
		    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
				
				if(ListStatus==0){
					Log.d(TAG,"list_cur_pos = " + list_cur_pos);
					if(deleteSelectedSatData()==false){
				    		deleteSatData(gobal_sat_cur_pos);
						ScanSatAndtsInfoList.remove(gobal_sat_cur_pos);
					}
					mySatAdapter.notifyDataSetChanged();
				}
				else{
					if(deleteSelectedTsData(gobal_sat_cur_pos)==false){	
						deleteTsData(gobal_sat_cur_pos,list_cur_pos);
						tsInfoList.remove(list_cur_pos);
					}
					myTsAdapter.notifyDataSetChanged();
					show_no_satellites_info();
				}
				
				
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
									show_no_satellites_info();
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

	private void showDeleteDia(){
		if(ScanSatAndtsInfoList==null||ScanSatAndtsInfoList.size()==0)
			return;
		else{
			if((ListStatus==1)&&(tsInfoList==null)){
				return;
			}
		}
	
		new SureDialog(DTVScanDvbsConfig.this,true){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.sure_delete));
			}

			public void onSetNegativeButton(){
	  
			}
			public void onSetPositiveButton(){
				if(ListStatus==0){
					Log.d(TAG,"list_cur_pos = " + list_cur_pos);
					if(deleteSelectedSatData()==false){
				    		deleteSatData(gobal_sat_cur_pos);
						ScanSatAndtsInfoList.remove(gobal_sat_cur_pos);
					}
					mySatAdapter.notifyDataSetChanged();
				}
				else{
					if(deleteSelectedTsData(gobal_sat_cur_pos)==false){	
						deleteTsData(gobal_sat_cur_pos,list_cur_pos);
						tsInfoList.remove(list_cur_pos);
					}
					myTsAdapter.notifyDataSetChanged();
					
				}
				show_no_satellites_info();
			}
		};
	}

	
	public void showMotoNoDia(){
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		pos = SatInfo.getMotoNo();
		LnbSettingItemSelected = pos;

		String[] ItemData = new String[32];
		for(int i=0;i<32;i++){
			ItemData[i] = String.valueOf(i+1);
		}	

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_moto_number);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	
			LnbSettingItemSelected= which;

			ContentValues values = new ContentValues();
			values.put("motor_num", LnbSettingItemSelected);
			SatInfo.setMotoNo(LnbSettingItemSelected);
			int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"motor_num");		
			myLnbSetAdapter.notifyDataSetChanged();
		}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	
	private AlertDialog.Builder lnbTypeBuilder;
	private View dvbs_lnb_type;
	/*
	private void showSatLnbTypeDia(){
		if(ScanSatAndtsInfoList==null)
			return;
		
		ContentValues values=null;
		lnbTypeBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_lnb_type = layoutInflater.inflate(R.layout.dvbs_lnb_type_dia, null); 
		lnbTypeBuilder.setTitle(R.string.edit_title);

		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		
		final EditText edittext_low = (EditText) dvbs_lnb_type.findViewById(R.id.edittext_lnb_low);
		final EditText edittext_high = (EditText) dvbs_lnb_type.findViewById(R.id.edittext_lnb_high); 
		final EditText edittext_threshold = (EditText)dvbs_lnb_type.findViewById(R.id.edittext_lnb_threshold);
		edittext_low.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		edittext_high.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		edittext_threshold.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});

		final CheckBox checkboxSwitch = (CheckBox)dvbs_lnb_type.findViewById(R.id.checkSwitch);
		final LinearLayout user_mode_layout = (LinearLayout)dvbs_lnb_type.findViewById(R.id.user_mode_layout);
		final LinearLayout lof_layout = (LinearLayout)dvbs_lnb_type.findViewById(R.id.lof_layout);

		final Button lof_mode = (Button)dvbs_lnb_type.findViewById(R.id.lnb_lof_fre); 
		final TextView text_info = (TextView)dvbs_lnb_type.findViewById(R.id.text_info);

		lof_mode.setTextSize(22);

		
		text_info.setTextColor(Color.YELLOW);
		
		lof_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(lof_mode.getText().equals("5150")){
					lof_mode.setText("5750");
				}
				else if(lof_mode.getText().equals("5750")){
					lof_mode.setText("9750");
				}
				else if(lof_mode.getText().equals("9750")){
					lof_mode.setText("10050");
				}
				else if(lof_mode.getText().equals("10050")){
					lof_mode.setText("11300");
				}
				else if(lof_mode.getText().equals("11300")){
					//lof_mode.setText("5150-5750");
				//}
				//else if(lof_mode.getText().equals("5150-5750")){
					lof_mode.setText("9750-10600");
				}
				else if(lof_mode.getText().equals("9750-10600")){
					lof_mode.setText("9750-10750");
				}
				else if(lof_mode.getText().equals("9750-10750")){
					lof_mode.setText("5150");
				}
				
			}
		});

		checkboxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
				if(arg1){
					user_mode_layout.setVisibility(View.VISIBLE);	
					lof_layout.setVisibility(View.INVISIBLE);
					lof_layout.setVisibility(View.GONE);
					
				}
				else{
					user_mode_layout.setVisibility(View.INVISIBLE);
					user_mode_layout.setVisibility(View.GONE);
					lof_layout.setVisibility(View.VISIBLE);
				}
			}
		});

		if(SatInfo.getLNBType()!=0){
			checkboxSwitch.setChecked(true);	
		}
		else{
			checkboxSwitch.setChecked(false);	
			lof_layout.setVisibility(View.VISIBLE);
		}
			if(SatInfo.getLoLOF()==0&&SatInfo.getHiLOF()==0){
				
				text_info.setText("0"+" MHz");
			}	
			else if(SatInfo.getLoLOF()!=SatInfo.getHiLOF()){
				text_info.setText(String.valueOf(SatInfo.getLoLOF())+"-"+String.valueOf(SatInfo.getHiLOF())+" MHz");
			}
			else if(SatInfo.getLoLOF()==SatInfo.getHiLOF()){
				text_info.setText(String.valueOf(SatInfo.getLoLOF())+" MHz");
			}
			//lof_mode.setText("5150");

			if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5150*1000)){
					lof_mode.setText("5150");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5750*1000)){
					lof_mode.setText("5750");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)){
					lof_mode.setText("9750");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==10050*1000)){
					lof_mode.setText("10050");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==11300*1000)){
					lof_mode.setText("11300");
				}
				//else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5150)&&(SatInfo.getHiLOF()==5750)){
					//lof_mode.setText("5150-5750");
				//}
				else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)&&(SatInfo.getHiLOF()==10600*1000)){
					lof_mode.setText("9750-10600");
				}
				else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)&&(SatInfo.getHiLOF()==10750*1000)){
					lof_mode.setText("9750-10750");
				}
				else{
					lof_mode.setText("5150");
				}
		
		
		if(SatInfo.getLoLOF()==0)
			edittext_low.setText("0");
		else
			edittext_low.setText(String.valueOf(SatInfo.getLoLOF()));

		if(SatInfo.getLoLOF()==0)
			edittext_high.setText("0");
		else
			edittext_high.setText(String.valueOf(SatInfo.getHiLOF()));

		if(SatInfo.getLofThreshold()==0)
			edittext_threshold.setText("11700");
		else
			edittext_threshold.setText(String.valueOf(SatInfo.getLofThreshold()));
		
		lnbTypeBuilder.setView(dvbs_lnb_type);

		lnbTypeBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				ContentValues values = new ContentValues();

				if(checkboxSwitch.isChecked()){
					//values.put("lnb_type",1);
					SatInfo.setLNBType(1);

					if(((edittext_low.getText().toString()) == null)
						|| ((edittext_low.getText().toString()).equals(""))){
						SatInfo.setLoLOF(5150*1000);
						values.put("lof_lo",5150*1000);
					}else{
						SatInfo.setLoLOF(Integer.parseInt(edittext_low.getText().toString()));
						values.put("lof_lo",Integer.parseInt(edittext_low.getText().toString()));						
					}

					if(((edittext_high.getText().toString()) == null)
						|| ((edittext_high.getText().toString()).equals(""))){
						SatInfo.setHiLOF(5150*1000);
						values.put("lof_hi",5150*1000);
					}else{
						SatInfo.setHiLOF(Integer.parseInt(edittext_high.getText().toString()));
						values.put("lof_hi",Integer.parseInt(edittext_high.getText().toString()));
					}					
					
					if(((edittext_threshold.getText().toString()) == null)
						|| ((edittext_threshold.getText().toString()).equals(""))){
						SatInfo.setLofThreshold(5150*1000);
						values.put("lof_threshold",5150*1000);
					}else{
						SatInfo.setLofThreshold(Integer.parseInt(edittext_threshold.getText().toString()));
						values.put("lof_threshold",Integer.parseInt(edittext_threshold.getText().toString()));
					}

				}	
				else{	
					//values.put("lnb_type",0);
					SatInfo.setLNBType(0);
					int threshold = 0;
					
					if(lof_mode.getText().equals("5150")){
						SatInfo.setLoLOF(5150*1000);
						SatInfo.setHiLOF(5150*1000);
						values.put("lof_lo",5150*1000);
						values.put("lof_hi",5150*1000);
						threshold = 5150*1000;
					}
					else if(lof_mode.getText().equals("5750")){
						SatInfo.setLoLOF(5750*1000);
						SatInfo.setHiLOF(5750*1000);
						values.put("lof_lo",5750*1000);
						values.put("lof_hi",5750*1000);
						threshold = 5150*1000;
					}
					else if(lof_mode.getText().equals("9750")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(9750*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",9750*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("10050")){
						SatInfo.setLoLOF(10050*1000);
						SatInfo.setHiLOF(10050*1000);
						values.put("lof_lo",10050*1000);
						values.put("lof_hi",10050*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("11300")){
						SatInfo.setLoLOF(11300*1000);
						SatInfo.setHiLOF(11300*1000);
						values.put("lof_lo",11300*1000);
						values.put("lof_hi",11300*1000);
						threshold = 11700*1000;
					}
					//else if(lof_mode.getText().equals("5150-5750")){
						//SatInfo.setLoLOF(5150);
						//SatInfo.setHiLOF(5750);
						//values.put("lof_lo",5150);
						//values.put("lof_hi",5750);
						//threshold = 5150;
					//}
					else if(lof_mode.getText().equals("9750-10600")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(10600*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",10600*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("9750-10750")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(10750*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",10750*1000);
						threshold = 11700*1000;
					}

					if (threshold != 0)  {
						SatInfo.setLofThreshold(threshold);
						values.put("lof_threshold",threshold);
					}
				}
				
				int sat_id = SatInfo.getSatId();
				editSatData(sat_id,values,"lof");	
				myLnbSetAdapter.notifyDataSetChanged();
				dialog.dismiss();
                          }
                      });
               lnbTypeBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = lnbTypeBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								}         
								});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);

		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
	}
	*/

	private void showSatLnbTypeDia(){
		if(ScanSatAndtsInfoList==null)
			return;
		
		ContentValues values=null;
		
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_lnb_type_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.edit_title));

		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		
		final EditText edittext_low = (EditText) window.findViewById(R.id.edittext_lnb_low);
		final EditText edittext_high = (EditText) window.findViewById(R.id.edittext_lnb_high); 
		final EditText edittext_threshold = (EditText)window.findViewById(R.id.edittext_lnb_threshold);
		edittext_low.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		edittext_high.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		edittext_threshold.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});

		final CheckBox checkboxSwitch = (CheckBox)window.findViewById(R.id.checkSwitch);
		final LinearLayout user_mode_layout = (LinearLayout)window.findViewById(R.id.user_mode_layout);
		final LinearLayout lof_layout = (LinearLayout)window.findViewById(R.id.lof_layout);

		final Button lof_mode = (Button)window.findViewById(R.id.lnb_lof_fre); 
		final TextView text_info = (TextView)window.findViewById(R.id.text_info);

		lof_mode.setTextSize(22);

		
		text_info.setTextColor(Color.YELLOW);
		
		lof_mode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(lof_mode.getText().equals("5150")){
					lof_mode.setText("5750");
				}
				else if(lof_mode.getText().equals("5750")){
					lof_mode.setText("9750");
				}
				else if(lof_mode.getText().equals("9750")){
					lof_mode.setText("10050");
				}
				else if(lof_mode.getText().equals("10050")){
					lof_mode.setText("11300");
				}
				else if(lof_mode.getText().equals("11300")){
					//lof_mode.setText("5150-5750");
				//}
				//else if(lof_mode.getText().equals("5150-5750")){
					lof_mode.setText("9750-10600");
				}
				else if(lof_mode.getText().equals("9750-10600")){
					lof_mode.setText("9750-10750");
				}
				else if(lof_mode.getText().equals("9750-10750")){
					lof_mode.setText("5150");
				}
				
			}
		});

		checkboxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
				if(arg1){
					user_mode_layout.setVisibility(View.VISIBLE);	
					lof_layout.setVisibility(View.INVISIBLE);
					lof_layout.setVisibility(View.GONE);
					
				}
				else{
					user_mode_layout.setVisibility(View.INVISIBLE);
					user_mode_layout.setVisibility(View.GONE);
					lof_layout.setVisibility(View.VISIBLE);
				}
			}
		});

		if(SatInfo.getLNBType()!=0){
			checkboxSwitch.setChecked(true);	
		}
		else{
			checkboxSwitch.setChecked(false);	
			lof_layout.setVisibility(View.VISIBLE);
		}
			if(SatInfo.getLoLOF()==0&&SatInfo.getHiLOF()==0){
				
				text_info.setText("0"+" MHz");
			}	
			else if(SatInfo.getLoLOF()!=SatInfo.getHiLOF()){
				text_info.setText(String.valueOf(SatInfo.getLoLOF())+"-"+String.valueOf(SatInfo.getHiLOF())+" MHz");
			}
			else if(SatInfo.getLoLOF()==SatInfo.getHiLOF()){
				text_info.setText(String.valueOf(SatInfo.getLoLOF())+" MHz");
			}
			//lof_mode.setText("5150");

			if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5150*1000)){
					lof_mode.setText("5150");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5750*1000)){
					lof_mode.setText("5750");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)){
					lof_mode.setText("9750");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==10050*1000)){
					lof_mode.setText("10050");
				}
				else if((SatInfo.getLoLOF()==SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==11300*1000)){
					lof_mode.setText("11300");
				}
				//else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==5150)&&(SatInfo.getHiLOF()==5750)){
					//lof_mode.setText("5150-5750");
				//}
				else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)&&(SatInfo.getHiLOF()==10600*1000)){
					lof_mode.setText("9750-10600");
				}
				else if((SatInfo.getLoLOF()!=SatInfo.getHiLOF())&&(SatInfo.getLoLOF()==9750*1000)&&(SatInfo.getHiLOF()==10750*1000)){
					lof_mode.setText("9750-10750");
				}
				else{
					lof_mode.setText("5150");
				}
		
		
		if(SatInfo.getLoLOF()==0)
			edittext_low.setText("0");
		else
			edittext_low.setText(String.valueOf(SatInfo.getLoLOF()));

		if(SatInfo.getLoLOF()==0)
			edittext_high.setText("0");
		else
			edittext_high.setText(String.valueOf(SatInfo.getHiLOF()));

		if(SatInfo.getLofThreshold()==0)
			edittext_threshold.setText("11700");
		else
			edittext_threshold.setText(String.valueOf(SatInfo.getLofThreshold()));
		

		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
				ContentValues values = new ContentValues();

				if(checkboxSwitch.isChecked()){
					//values.put("lnb_type",1);
					SatInfo.setLNBType(1);

					if(((edittext_low.getText().toString()) == null)
						|| ((edittext_low.getText().toString()).equals(""))){
						SatInfo.setLoLOF(5150*1000);
						values.put("lof_lo",5150*1000);
					}else{
						SatInfo.setLoLOF(Integer.parseInt(edittext_low.getText().toString()));
						values.put("lof_lo",Integer.parseInt(edittext_low.getText().toString()));						
					}

					if(((edittext_high.getText().toString()) == null)
						|| ((edittext_high.getText().toString()).equals(""))){
						SatInfo.setHiLOF(5150*1000);
						values.put("lof_hi",5150*1000);
					}else{
						SatInfo.setHiLOF(Integer.parseInt(edittext_high.getText().toString()));
						values.put("lof_hi",Integer.parseInt(edittext_high.getText().toString()));
					}					
					
					if(((edittext_threshold.getText().toString()) == null)
						|| ((edittext_threshold.getText().toString()).equals(""))){
						SatInfo.setLofThreshold(5150*1000);
						values.put("lof_threshold",5150*1000);
					}else{
						SatInfo.setLofThreshold(Integer.parseInt(edittext_threshold.getText().toString()));
						values.put("lof_threshold",Integer.parseInt(edittext_threshold.getText().toString()));
					}

				}	
				else{	
					//values.put("lnb_type",0);
					SatInfo.setLNBType(0);
					int threshold = 0;
					
					if(lof_mode.getText().equals("5150")){
						SatInfo.setLoLOF(5150*1000);
						SatInfo.setHiLOF(5150*1000);
						values.put("lof_lo",5150*1000);
						values.put("lof_hi",5150*1000);
						threshold = 5150*1000;
					}
					else if(lof_mode.getText().equals("5750")){
						SatInfo.setLoLOF(5750*1000);
						SatInfo.setHiLOF(5750*1000);
						values.put("lof_lo",5750*1000);
						values.put("lof_hi",5750*1000);
						threshold = 5150*1000;
					}
					else if(lof_mode.getText().equals("9750")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(9750*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",9750*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("10050")){
						SatInfo.setLoLOF(10050*1000);
						SatInfo.setHiLOF(10050*1000);
						values.put("lof_lo",10050*1000);
						values.put("lof_hi",10050*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("11300")){
						SatInfo.setLoLOF(11300*1000);
						SatInfo.setHiLOF(11300*1000);
						values.put("lof_lo",11300*1000);
						values.put("lof_hi",11300*1000);
						threshold = 11700*1000;
					}
					//else if(lof_mode.getText().equals("5150-5750")){
						//SatInfo.setLoLOF(5150);
						//SatInfo.setHiLOF(5750);
						//values.put("lof_lo",5150);
						//values.put("lof_hi",5750);
						//threshold = 5150;
					//}
					else if(lof_mode.getText().equals("9750-10600")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(10600*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",10600*1000);
						threshold = 11700*1000;
					}
					else if(lof_mode.getText().equals("9750-10750")){
						SatInfo.setLoLOF(9750*1000);
						SatInfo.setHiLOF(10750*1000);
						values.put("lof_lo",9750*1000);
						values.put("lof_hi",10750*1000);
						threshold = 11700*1000;
					}

					if (threshold != 0)  {
						SatInfo.setLofThreshold(threshold);
						values.put("lof_threshold",threshold);
					}
				}
				
				int sat_id = SatInfo.getSatId();
				editSatData(sat_id,values,"lof");	
				myLnbSetAdapter.notifyDataSetChanged();
					
					

					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							
						}         
						});	
	
		
	}


	private int LnbSettingItemSelected=0;
	/*
	public void showLnbPowerDia(){
		 
		int pos = 0;

		t =new getFrontEndInfoThread();  
                t.start(); 
				
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getLNBPwrOnOff()){
			case 0:
				pos=0;
				break;
			case 1:
				pos=1;
				break;
			case 2 :
				pos=2;
				break;
			case 3:
				pos=3;
				break;
		}

		builder = new AlertDialog.Builder(DTVScanDvbsConfig.this);	
		builder.setTitle(R.string.dish_setup_lnb_power);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(new String[] { "13V", "18V","Off","13v/18v"}, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	
			LnbSettingItemSelected = which; 
			}	
		     });
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){
			//@Override
			public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				ContentValues values = new ContentValues();
				
				switch(LnbSettingItemSelected){
					
					case 0:
						values.put("voltage", 0);
						SatInfo.setLNBPwrOnOff(0);
						//t.onSetupCmd(t.LNB_CONTROL_CMD_13V,null);
						break;
					case 1:
						values.put("voltage",1);
						SatInfo.setLNBPwrOnOff(1);
						//t.onSetupCmd(t.LNB_CONTROL_CMD_18V,null);
						break;	
					case 2:
						values.put("voltage", 2);
						SatInfo.setLNBPwrOnOff(2);
						//t.onSetupCmd(t.LNB_CONTROL_CMD_POWER_OFF,null);
						break;
					case 3:
						values.put("voltage",3);
						SatInfo.setLNBPwrOnOff(3);
						break;
					
			}

			int sat_id  = SatInfo.getSatId();
			editSatData(sat_id,values,"voltage");	

			getTsData(gobal_sat_cur_pos);
			final cmdParams my_cmdParams = new cmdParams();
			if(tsInfoList!=null){
				int size=tsInfoList.size();
				
				if(size>0){
					final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
					//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

					TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
					
					TVChannelParams mTVChannelParams = mTVChannel.getParams();
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_POWER_AUTO,(Object)my_cmdParams);
				}
			}
			else{
				TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
				my_cmdParams.channel = mTVChannelParams;
				t.onSetupCmd(t.LNB_CONTROL_CMD_POWER_AUTO,(Object)my_cmdParams);
			}

			
			myLnbSetAdapter.notifyDataSetChanged();
			dialog.dismiss();

			}
		});	
		
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		 dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					t.quitLoop();	
				}         
		});	
		 
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		//lp.height = (int) (d.getHeight() * 0.6);  
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/
	
	public void showLnbPowerDia(TextView v){
		final TextView info_cur = v;
		int pos = 0;

		t =new getFrontEndInfoThread();  
                t.start(); 
				
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getLNBPwrOnOff()){
			case 0:
				pos=0;
				break;
			case 1:
				pos=1;
				break;
			case 2 :
				pos=2;
				break;
			case 3:
				pos=3;
				break;
		}
			
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,new String[]{ "13V", "18V","Off","13v/18v"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_lnb_power));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				ContentValues values = new ContentValues();
				
				switch(which){				
					case 0:
						values.put("voltage", 0);
						SatInfo.setLNBPwrOnOff(0);
						break;
					case 1:
						values.put("voltage",1);
						SatInfo.setLNBPwrOnOff(1);
						break;	
					case 2:
						values.put("voltage", 2);
						SatInfo.setLNBPwrOnOff(2);
						break;
					case 3:
						values.put("voltage",3);
						SatInfo.setLNBPwrOnOff(3);
						break;
					
				}

				int sat_id  = SatInfo.getSatId();
				editSatData(sat_id,values,"voltage");	

				getTsData(gobal_sat_cur_pos);
				final cmdParams my_cmdParams = new cmdParams();
				if(tsInfoList!=null){
					int size=tsInfoList.size();
					
					if(size>0){
						final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
						//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

						TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
						
						TVChannelParams mTVChannelParams = mTVChannel.getParams();
						my_cmdParams.channel = mTVChannelParams;
						t.onSetupCmd(t.LNB_CONTROL_CMD_POWER_AUTO,(Object)my_cmdParams);
					}
				}
				else{
					TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_POWER_AUTO,(Object)my_cmdParams);
				}
				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					t.quitLoop();		
				}         
		});	
		
	}

	/*
	public void showLnb22kDia(){
		int pos = 0;

		t =new getFrontEndInfoThread();  
                t.start(); 
		
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.get22KOnOff()){
			case DbSat.LNB_22K_ON:
				pos=0;
				break;
			case DbSat.LNB_22K_OFF:
				pos=1;
				break;
			case DbSat.LNB_22K_LOHI:
				pos=2;
				break;	
		}

		String[] ItemData = new String[3];
		
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_on);
		ItemData[1] =this.getResources().getString(R.string.dish_setup_conf_off);
		ItemData[2] = this.getResources().getString(R.string.dish_setup_conf_auto);

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_22K);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	

			LnbSettingItemSelected= which;
		}
		});
						
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				}
		});
		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
			
				switch(LnbSettingItemSelected){
					case 0:
						values.put("signal_22khz", DbSat.LNB_22K_ON);
						SatInfo.set22KOnOff(DbSat.LNB_22K_ON);
						break;
					case 1:
						values.put("signal_22khz",DbSat.LNB_22K_OFF);
						SatInfo.set22KOnOff(DbSat.LNB_22K_OFF);
						break;
					case 2:
						values.put("signal_22khz", DbSat.LNB_22K_LOHI);
						SatInfo.set22KOnOff(DbSat.LNB_22K_LOHI);		
						break;	
			}		
			int sat_id  = SatInfo.getSatId();
			editSatData(sat_id,values,"signal_22khz");	
			getTsData(gobal_sat_cur_pos);
			final cmdParams my_cmdParams = new cmdParams();
			if(tsInfoList!=null){
				int size=tsInfoList.size();
				
				if(size>0){
					final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
					//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

					TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());					
					TVChannelParams mTVChannelParams = mTVChannel.getParams();

					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
			}
			else{
				TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
				my_cmdParams.channel = mTVChannelParams;
				t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
			}

			myLnbSetAdapter.notifyDataSetChanged();
			}
		});	

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
				t.quitLoop();
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/

	public void showLnb22kDia(TextView v){
		final TextView info_cur = v;
		int pos = 0;

		t =new getFrontEndInfoThread();  
                t.start(); 
		
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.get22KOnOff()){
			case DbSat.LNB_22K_ON:
				pos=0;
				break;
			case DbSat.LNB_22K_OFF:
				pos=1;
				break;
			case DbSat.LNB_22K_LOHI:
				pos=2;
				break;	
		}

		String[] ItemData = new String[3];
		
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_on);
		ItemData[1] =this.getResources().getString(R.string.dish_setup_conf_off);
		ItemData[2] = this.getResources().getString(R.string.dish_setup_conf_auto);

			
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,ItemData,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_22K));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				// TODO Auto-generated method stub
				LnbSettingItemSelected= which;
				ContentValues values = new ContentValues();
				
					switch(LnbSettingItemSelected){
						case 0:
							values.put("signal_22khz", DbSat.LNB_22K_ON);
							SatInfo.set22KOnOff(DbSat.LNB_22K_ON);
							break;
						case 1:
							values.put("signal_22khz",DbSat.LNB_22K_OFF);
							SatInfo.set22KOnOff(DbSat.LNB_22K_OFF);
							break;
						case 2:
							values.put("signal_22khz", DbSat.LNB_22K_LOHI);
							SatInfo.set22KOnOff(DbSat.LNB_22K_LOHI);		
							break;	
				}		
				int sat_id  = SatInfo.getSatId();
				editSatData(sat_id,values,"signal_22khz");	
				getTsData(gobal_sat_cur_pos);
				final cmdParams my_cmdParams = new cmdParams();
				if(tsInfoList!=null){
					int size=tsInfoList.size();
					
					if(size>0){
						final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
						//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

						TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());					
						TVChannelParams mTVChannelParams = mTVChannel.getParams();

						my_cmdParams.channel = mTVChannelParams;
						t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
					}
				}
				else{
					TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}

				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					t.quitLoop();		
				}         
		});	
		
	}

	/*
	public void showLnbToneBurstDia(){
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getToneburstType()){
			case DbSat.LNB_TONEBURST_NONE:
				pos=0;
				break;
			case DbSat.LNB_TONEBURST_A:
				pos=1;
				break;
			case DbSat.LNB_TONEBURST_B: 
				pos=2;
				break;
		}

		String[] ItemData = new String[3];
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
		ItemData[1] = "A";
		ItemData[2] ="B";
		builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.dish_setup_lnb_toneburst);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	

			LnbSettingItemSelected= which;
		}
		});
						
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				}
		});
		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
		
				switch(LnbSettingItemSelected){
					case 0:
						values.put("tone_burst", DbSat.LNB_TONEBURST_NONE);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_NONE);
						break;
					case 1:
						values.put("tone_burst", DbSat.LNB_TONEBURST_A);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_A);
						break;
					case 2:
						values.put("tone_burst",DbSat.LNB_TONEBURST_B);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_B);
						
						break;
			}
			int sat_id  = SatInfo.getSatId();
			editSatData(sat_id,values,"tone_burst");	
			getTsData(gobal_sat_cur_pos);
			final cmdParams my_cmdParams = new cmdParams();
			if(tsInfoList!=null){
				int size=tsInfoList.size();
				
				if(size>0){
					final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
					//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

					TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());					
					TVChannelParams mTVChannelParams = mTVChannel.getParams();

					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
			}
			else{
				TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
				my_cmdParams.channel = mTVChannelParams;
				t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
			}		
			myLnbSetAdapter.notifyDataSetChanged();
			}
		});	

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}	
	*/

	public void showLnbToneBurstDia(TextView v){
		final TextView info_cur = v;
		
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getToneburstType()){
			case DbSat.LNB_TONEBURST_NONE:
				pos=0;
				break;
			case DbSat.LNB_TONEBURST_A:
				pos=1;
				break;
			case DbSat.LNB_TONEBURST_B: 
				pos=2;
				break;
		}

		String[] ItemData = new String[3];
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
		ItemData[1] = "A";
		ItemData[2] ="B";
		
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,ItemData,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_lnb_toneburst));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				// TODO Auto-generated method stub
				LnbSettingItemSelected= which;
				ContentValues values = new ContentValues();
		
				switch(LnbSettingItemSelected){
					case 0:
						values.put("tone_burst", DbSat.LNB_TONEBURST_NONE);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_NONE);
						break;
					case 1:
						values.put("tone_burst", DbSat.LNB_TONEBURST_A);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_A);
						break;
					case 2:
						values.put("tone_burst",DbSat.LNB_TONEBURST_B);
						SatInfo.setToneburstType(DbSat.LNB_TONEBURST_B);
						
						break;
				}
				int sat_id  = SatInfo.getSatId();
				editSatData(sat_id,values,"tone_burst");	
				getTsData(gobal_sat_cur_pos);
				final cmdParams my_cmdParams = new cmdParams();
				if(tsInfoList!=null){
					int size=tsInfoList.size();
					
					if(size>0){
						final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
						//final DbTransponder TsInfo  = queryTsData(list_cur_pos);

						TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());					
						TVChannelParams mTVChannelParams = mTVChannel.getParams();

						my_cmdParams.channel = mTVChannelParams;
						t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
					}
				}
				else{
					TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}		
				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
				}         
		});	
		
	}
	
	/*
	public void showLnbDiseqcModeDia(){

		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getSwtPort()){
			case DbSat.LNB_DISEQC_NONE:
			case DbSat.LNB_DISEQC_10:	
			case DbSat.LNB_DISEQC_11: 	
				pos=0;
				break;
			case DbSat.LNB_DISEQC_12: 
				pos=1;
				break;	
			case DbSat.LNB_DISEQC_13: 
				pos=2;
				break;		
			default:
				pos=0;
				break;
		}
		LnbSettingItemSelected=pos;

		String[] ItemData = new String[3];
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
		//ItemData[1] = this.getResources().getString(R.string.dish_setup_diseqc0);
		//ItemData[2] = this.getResources().getString(R.string.dish_setup_diseqc1);
		ItemData[1] = this.getResources().getString(R.string.dish_setup_diseqc2);
		ItemData[2] = this.getResources().getString(R.string.dish_setup_diseqc3);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_diseqc);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	

			LnbSettingItemSelected= which;
		}
		});
						
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				}
		});
		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
				switch(LnbSettingItemSelected){
					case 0:
						values.put("diseqc_mode", DbSat.LNB_DISEQC_NONE);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_11);
						break;
					case 1:
						values.put("diseqc_mode",DbSat.LNB_DISEQC_12);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_12);
						break;
					case 2:
						values.put("diseqc_mode",DbSat.LNB_DISEQC_13);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_13);	
						break;
			}
			int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"diseqc_mode");
			
			myLnbSetAdapter.notifyDataSetChanged();
			
			}
		});	

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/

	public void showLnbDiseqcModeDia(TextView v){
		final TextView info_cur = v;
		
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		switch(SatInfo.getSwtPort()){
			case DbSat.LNB_DISEQC_NONE:
			case DbSat.LNB_DISEQC_10:	
			case DbSat.LNB_DISEQC_11: 	
				pos=0;
				break;
			case DbSat.LNB_DISEQC_12: 
				pos=1;
				break;	
			case DbSat.LNB_DISEQC_13: 
				pos=2;
				break;		
			default:
				pos=0;
				break;
		}
		LnbSettingItemSelected=pos;

		String[] ItemData = new String[3];
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
		//ItemData[1] = this.getResources().getString(R.string.dish_setup_diseqc0);
		//ItemData[2] = this.getResources().getString(R.string.dish_setup_diseqc1);
		ItemData[1] = this.getResources().getString(R.string.dish_setup_diseqc2);
		ItemData[2] = this.getResources().getString(R.string.dish_setup_diseqc3);
		
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,ItemData,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_diseqc));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				// TODO Auto-generated method stub
				LnbSettingItemSelected= which;
				ContentValues values = new ContentValues();
				switch(LnbSettingItemSelected){
					case 0:
						values.put("diseqc_mode", DbSat.LNB_DISEQC_NONE);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_11);
						break;
					case 1:
						values.put("diseqc_mode",DbSat.LNB_DISEQC_12);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_12);
						break;
					case 2:
						values.put("diseqc_mode",DbSat.LNB_DISEQC_13);
						SatInfo.setSwtPort(DbSat.LNB_DISEQC_13);	
							break;
				}
				int sat_id = SatInfo.getSatId();
				editSatData(sat_id,values,"diseqc_mode");
				
				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
				}         
		});	
		
	}


	/*
	public void showLnbDiseqcCommittedDia(){

		String[] ItemData=null;
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		
		t =new getFrontEndInfoThread();  
		t.start(); 

		final int diseqc_mode = SatInfo.getSwtPort();
		//if(diseqc_mode == DbSat.LNB_DISEQC_NONE)
			//return;
		
		ItemData = new String[5];

		/*
		ItemData[0]="AA";
		ItemData[1]="AB";
		ItemData[2]="BA";
		ItemData[3]="BB";
		*/
		/*
		ItemData[0]="LNB1";
		ItemData[1]="LNB2";
		ItemData[2]="LNB3";
		ItemData[3]="LNB4";
		ItemData[4] = this.getResources().getString(R.string.dish_setup_conf_none);
		
		pos = SatInfo.getLnbConfig10();
		LnbSettingItemSelected=pos;

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_committed_command);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	
			LnbSettingItemSelected= which;
		}
		});
						
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				}
		});
		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
			values.put("committed_cmd", LnbSettingItemSelected);
			SatInfo.setLnbConfig10(LnbSettingItemSelected);
			int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"committed_cmd");	
			getTsData(gobal_sat_cur_pos);
			final cmdParams my_cmdParams = new cmdParams();
			if(tsInfoList!=null){
				int size=tsInfoList.size();
				if(size>0){
					final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
					//final DbTransponder TsInfo  = queryTsData(list_cur_pos);
					TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
					TVChannelParams mTVChannelParams = mTVChannel.getParams();
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
			}
			else{
				TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
				my_cmdParams.channel = mTVChannelParams;
				t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
			}
			
			myLnbSetAdapter.notifyDataSetChanged();
			
			}
		});	

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
				t.quitLoop();	
			}         
		});	

		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/
		
	public void showLnbDiseqcCommittedDia(TextView v){
		final TextView info_cur = v;
		String[] ItemData=null;
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;
		
		t =new getFrontEndInfoThread();  
		t.start(); 

		final int diseqc_mode = SatInfo.getSwtPort();
		//if(diseqc_mode == DbSat.LNB_DISEQC_NONE)
			//return;
		
		ItemData = new String[5];

		/*
		ItemData[0]="AA";
		ItemData[1]="AB";
		ItemData[2]="BA";
		ItemData[3]="BB";
		*/
		
		ItemData[0]="LNB1";
		ItemData[1]="LNB2";
		ItemData[2]="LNB3";
		ItemData[3]="LNB4";
		ItemData[4] = this.getResources().getString(R.string.dish_setup_conf_none);
		
		pos = SatInfo.getLnbConfig10();
		LnbSettingItemSelected=pos;
			
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,ItemData,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_uncommitted_command));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				// TODO Auto-generated method stub
				LnbSettingItemSelected= which;
				ContentValues values = new ContentValues();
				values.put("committed_cmd", LnbSettingItemSelected);
				SatInfo.setLnbConfig10(LnbSettingItemSelected);
				int sat_id = SatInfo.getSatId();
				editSatData(sat_id,values,"committed_cmd");	
				getTsData(gobal_sat_cur_pos);
				final cmdParams my_cmdParams = new cmdParams();
				if(tsInfoList!=null){
					int size=tsInfoList.size();
					if(size>0){
						final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
						//final DbTransponder TsInfo  = queryTsData(list_cur_pos);
						TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
						TVChannelParams mTVChannelParams = mTVChannel.getParams();
						my_cmdParams.channel = mTVChannelParams;
						t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
					}
				}
				else{
					TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
				
				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					t.quitLoop();		
				}         
		});	
		
	}


	/*
	public void showLnbDiseqcUncommittedDia(){
		
		String[] ItemData=null;
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;

		t =new getFrontEndInfoThread();  
		t.start(); 


		final int diseqc_mode = SatInfo.getSwtPort();
		//if(diseqc_mode == DbSat.LNB_DISEQC_NONE)
			//return;
		//if(diseqc_mode >= DbSat.LNB_DISEQC_11){
			ItemData = new String[17];
			ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
			for(int j=1;j<17;j++){
				ItemData[j]="LNB"+String.valueOf(j);
			}
			
			if(SatInfo.getLnbConfig11()==0||SatInfo.getLnbConfig11()==4){
				pos = 0;	
			}
			else if(SatInfo.getLnbConfig11()>=0xf0){
				pos = SatInfo.getLnbConfig11()-0xf0+1;
			}
		//}
		LnbSettingItemSelected=pos;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_uncommitted_command);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	
			LnbSettingItemSelected= which;
			
			ContentValues values = new ContentValues();
			if(diseqc_mode >= DbSat.LNB_DISEQC_11){

				int temp=4;
				if(LnbSettingItemSelected==0){
					temp=4;
				}
				else if(LnbSettingItemSelected>=1){
					temp = 0xf0+LnbSettingItemSelected-1;
				}
		
				values.put("uncommitted_cmd", temp);
				SatInfo.setLnbConfig11(temp);
			}		
			int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"uncommitted_cmd");	
			getTsData(gobal_sat_cur_pos);
			final cmdParams my_cmdParams = new cmdParams();
			if(tsInfoList!=null){
				int size=tsInfoList.size();
				
				if(size>0){
					final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
					//final DbTransponder TsInfo  = queryTsData(list_cur_pos);
					TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
					TVChannelParams mTVChannelParams = mTVChannel.getParams();
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
			}
			else{
				TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
				my_cmdParams.channel = mTVChannelParams;
				t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
			}
		
			myLnbSetAdapter.notifyDataSetChanged();
		}
		});

		/*				
		builder.setNegativeButton(R.string.cancel, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				}
		});
		builder.setPositiveButton(R.string.ok, new  DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
			if(diseqc_mode == DbSat.LNB_DISEQC_10){
				values.put("lnb_diseqc_mode_config10", LnbSettingItemSelected);
				SatInfo.setLnbConfig10(LnbSettingItemSelected);
				
			}
			else if(diseqc_mode == DbSat.LNB_DISEQC_11){
				values.put("lnb_diseqc_mode_config11", LnbSettingItemSelected);
				SatInfo.setLnbConfig11(LnbSettingItemSelected);
			}		
			int scan_id = SatInfo.getScanId();
			int sat_id  = SatInfo.getId();
			Log.d(TAG,"sat_id"+sat_id);	
			editSatData(scan_id,sat_id,values);		
			myLnbSetAdapter.notifyDataSetChanged();
			
			}
		});	
		*/
		/*
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
				t.quitLoop();	
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		lp.width = (int) (d.getWidth() * 0.50);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/

	public void showLnbDiseqcUncommittedDia(TextView v){
		final TextView info_cur = v;
		String[] ItemData=null;
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;

		t =new getFrontEndInfoThread();  
		t.start(); 


		final int diseqc_mode = SatInfo.getSwtPort();

		ItemData = new String[17];
		ItemData[0] = this.getResources().getString(R.string.dish_setup_conf_none);
		for(int j=1;j<17;j++){
			ItemData[j]="LNB"+String.valueOf(j);
		}
		
		if(SatInfo.getLnbConfig11()==0||SatInfo.getLnbConfig11()==4){
			pos = 0;	
		}
		else if(SatInfo.getLnbConfig11()>=0xf0){
			pos = SatInfo.getLnbConfig11()-0xf0+1;
		}
		
		LnbSettingItemSelected=pos;	
			
		SingleChoiseDialog mSingleChoiseDialog = new SingleChoiseDialog(DTVScanDvbsConfig.this,ItemData,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.dish_setup_uncommitted_command));
			}
			
			public void onSetNegativeButton(){
			}
			public void onSetPositiveButton(int which){
				// TODO Auto-generated method stub
				LnbSettingItemSelected= which;
				ContentValues values = new ContentValues();
				if(diseqc_mode >= DbSat.LNB_DISEQC_11){

					int temp=4;
					if(LnbSettingItemSelected==0){
						temp=4;
					}
					else if(LnbSettingItemSelected>=1){
						temp = 0xf0+LnbSettingItemSelected-1;
					}
			
					values.put("uncommitted_cmd", temp);
					SatInfo.setLnbConfig11(temp);
				}		
				int sat_id = SatInfo.getSatId();
				editSatData(sat_id,values,"uncommitted_cmd");	
				getTsData(gobal_sat_cur_pos);
				final cmdParams my_cmdParams = new cmdParams();
				if(tsInfoList!=null){
					int size=tsInfoList.size();
					
					if(size>0){
						final DbTransponder TsInfo  = (DbTransponder)tsInfoList.get(0);
						//final DbTransponder TsInfo  = queryTsData(list_cur_pos);
						TVChannel mTVChannel = TVChannel.selectByID(DTVScanDvbsConfig.this,TsInfo.getDbId());
						TVChannelParams mTVChannelParams = mTVChannel.getParams();
						my_cmdParams.channel = mTVChannelParams;
						t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
					}
				}
				else{
					TVChannelParams mTVChannelParams = TVChannelParams.dvbsParams(DTVScanDvbsConfig.this, 0,0,sat_id, 0);
					my_cmdParams.channel = mTVChannelParams;
					t.onSetupCmd(t.LNB_CONTROL_CMD_22K_AUTO,(Object)my_cmdParams);
				}
			
				myLnbSetAdapter.notifyDataSetChanged();
			}
		};	

		mSingleChoiseDialog.mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					t.quitLoop();		
				}         
		});	
		
	}

	public void showLnbDiseqcSequence(){
		int pos = 0;
		final DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;

		final int diseqc_mode = SatInfo.getSwtPort();
		if(diseqc_mode == DbSat.LNB_DISEQC_NONE)
			return;

		/*
		diseqc 1.0)
			0) commited, toneburst
			1) toneburst, committed
		diseqc > 1.0)
			2) committed, uncommitted, toneburst
			3) toneburst, committed, uncommitted
			4) uncommitted, committed, toneburst
			5) toneburst, uncommitted, committed 
		*/
		
		String[] ItemData=null;

		if(diseqc_mode == DbSat.LNB_DISEQC_10){
			ItemData = new String[2];
			ItemData[0] ="0    commited, toneburst";
			ItemData[1] ="1    toneburst, commited";

			pos = SatInfo.getDiseqcSequence()&0x000F;
			if(pos>=2)
				pos=0;
			
		}
		else if(diseqc_mode >= DbSat.LNB_DISEQC_11){
			ItemData = new String[4];
			ItemData[0] ="2    committed, uncommitted, toneburst";
			ItemData[1] ="3    toneburst, committed, uncommitted";
			ItemData[2] ="4    uncommitted, committed, toneburst";
			ItemData[3] ="5    toneburst, uncommitted, committed";

			pos = (SatInfo.getDiseqcSequence()>>4)&0x000F;
			if(pos>=2){
				pos = pos-2;
			}
			else
				pos=0;
		}	

		builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dish_setup_diseqc_sequence);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(ItemData, pos, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {	
			LnbSettingItemSelected= which;

			ContentValues values = new ContentValues();


			if(diseqc_mode == DbSat.LNB_DISEQC_10){
				int value0 = (SatInfo.getDiseqcSequence()&0x00F0)|(LnbSettingItemSelected&0x000F);
				values.put("cmd_order", value0);
				SatInfo.setDiseqcSequence(value0);
			}
			else if(diseqc_mode >= DbSat.LNB_DISEQC_11){
				int value1 = (SatInfo.getDiseqcSequence()&0x000F)|((LnbSettingItemSelected+2)<<4);
				values.put("cmd_order", value1);
				SatInfo.setDiseqcSequence(value1);
			}
		
			int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"cmd_order");		
			myLnbSetAdapter.notifyDataSetChanged();
		}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
			}         
		}); 	

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
			}         
		});	
		
		dialog.show();
		WindowManager m = getWindowManager();   
		Display d = m.getDefaultDisplay();  	
		WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		
		lp.width = (int) (d.getWidth() * 0.50);
		 if(diseqc_mode == DbSat.LNB_DISEQC_11)
		 	lp.width = (int) (d.getWidth() * 0.80);
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	public void dealDiseqcRepeatItem(View v){

		final ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
		final TextView info_cur = (TextView)v.findViewById(R.id.info);	

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;

		int diseqc_repeat_mode = SatInfo.getDiseqcRepeat();
		ContentValues values = new ContentValues();
		if(diseqc_repeat_mode == 0){
			values.put("sequence_repeat", 1);
			SatInfo.setDiseqcRepeat(1);
			image_cur.setBackgroundResource(R.drawable.select_round_2); 
		    	info_cur.setText(R.string.on);
		}
		else{
			values.put("sequence_repeat", 0);
			SatInfo.setDiseqcRepeat(0);
			image_cur.setBackgroundResource(R.drawable.select_round_1); 
		    	info_cur.setText(R.string.off);
		}
		
		int sat_id = SatInfo.getSatId();
		editSatData(sat_id,values,"sequence_repeat");		
		
	}

	public void dealFastDiseqcItem(View v){

		final ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
		final TextView info_cur = (TextView)v.findViewById(R.id.info);	

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		if(SatInfo==null)
			return;

		int fast_diseqc_mode = SatInfo.getFastDiseqc();
		ContentValues values = new ContentValues();
		if(fast_diseqc_mode == 0){
			values.put("fast_diseqc", 1);
			SatInfo.setFastDiseqc(1);
			image_cur.setBackgroundResource(R.drawable.select_round_2); 
		    	info_cur.setText(R.string.on);
		}
		else{
			values.put("fast_diseqc", 0);
			SatInfo.setFastDiseqc(0);
			image_cur.setBackgroundResource(R.drawable.select_round_1); 
		    	info_cur.setText(R.string.off);
		}
		
		int sat_id = SatInfo.getSatId();
			editSatData(sat_id,values,"fast_diseqc");		
		
	}


	
	private AlertDialog.Builder setLocationBuilder;
	private View dvbs_set_location;
	/*
	private void showSetLocationDia(){

		 DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);

		if(SatInfo==null||SatInfo.getSwtPort()!=DbSat.LNB_DISEQC_13){
			return;
		}
		
		final int sat_id = SatInfo.getSatId();
			
		String longitude_direction = mLast.getString("longitude_direction","East");
		String latitude_direction = mLast.getString("latitude_direction","North");

		final int longitude_angle = mLast.getInt("longitude_angle",0);
		final int latitude_angle = mLast.getInt("latitude_angle",0);
		
			
		ContentValues values=null;
		setLocationBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_set_location = layoutInflater.inflate(R.layout.dvbs_set_location_dia, null); 
		setLocationBuilder.setTitle(R.string.dish_setup_conf_button_des8_info);

		final Button edit_longitude_direction = (Button)dvbs_set_location.findViewById(R.id.edit_longitude_direction); 
		final EditText edittext_angle0 = (EditText) dvbs_set_location.findViewById(R.id.angle0); 
		final EditText edittext_angle1 = (EditText) dvbs_set_location.findViewById(R.id.angle1);

		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		
		final Button edit_latitude_direction = (Button)dvbs_set_location.findViewById(R.id.edit_latitude_direction); 
		final EditText edittext_latitude_angle0 = (EditText) dvbs_set_location.findViewById(R.id.latitude_angle0); 
		final EditText edittext_latitude_angle1 = (EditText) dvbs_set_location.findViewById(R.id.latitude_angle1);
		edittext_latitude_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		edittext_latitude_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
			
		if(longitude_direction.equals("East")){
			edit_longitude_direction.setText("East");
		}
		else{
			edit_longitude_direction.setText("West");
		}	
		edit_longitude_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(edit_longitude_direction.getText().equals("East")){
					edit_longitude_direction.setText("West");
				}
				else{
					edit_longitude_direction.setText("East");
				}
			}
		});
		edittext_angle0.setText(String.valueOf(Math.abs(longitude_angle)/10));
		edittext_angle1.setText(String.valueOf(Math.abs(longitude_angle%10)));

		
		if(latitude_direction.equals("North")){
			edit_latitude_direction.setText("North");
		}
		else{
			edit_latitude_direction.setText("South");
		}
		edit_latitude_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub
				if(edit_latitude_direction.getText().equals("North")){
					edit_latitude_direction.setText("South");				
				}
				else{
					edit_latitude_direction.setText("North");
				}
			}
		});
		edittext_latitude_angle0.setText(String.valueOf(Math.abs(latitude_angle)/10));
		edittext_latitude_angle1.setText(String.valueOf(Math.abs(latitude_angle%10)));

		
		setLocationBuilder.setView(dvbs_set_location);

		setLocationBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				ContentValues values= new ContentValues();
				
				if(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals("")||edittext_latitude_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals("")){
					
					Toast toast = Toast.makeText(
					DTVScanDvbsConfig.this, 
					R.string.lo_la_input_invalid,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show(); 
					try{
					    Field field = dialog.getClass()
					            .getSuperclass().getDeclaredField("mShowing");
					    field.setAccessible(true);
					    field.set(dialog, false);
					    dialog.dismiss();
					}
					catch (Exception e){
					}
					return;	
				}
				

				int temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
				if(temp>1800){
					Toast toast = Toast.makeText(
					DTVScanDvbsConfig.this, 
					R.string.lo_la_input_invalid,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show(); 
					try{
					    Field field = dialog.getClass()
					            .getSuperclass().getDeclaredField("mShowing");
					    field.setAccessible(true);
					    field.set(dialog, false);
					    dialog.dismiss();
					}
					catch (Exception e){
					}
					return;	
				}
				
				temp=Integer.parseInt(edittext_latitude_angle0.getText().toString())*10+Integer.parseInt(edittext_latitude_angle1.getText().toString());
				if(temp>900){
					Toast toast = Toast.makeText(
					DTVScanDvbsConfig.this, 
					R.string.lo_la_input_invalid,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show(); 	

					try{
					    Field field = dialog.getClass()
					            .getSuperclass().getDeclaredField("mShowing");
					    field.setAccessible(true);
					    field.set(dialog, false);
					    dialog.dismiss();
					}
					catch (Exception e){
					}
					return;	
				}
			
				String tmp = edit_longitude_direction.getText().toString();
				mLast.edit().putString ("longitude_direction",tmp).commit();
				if(tmp.equals("North"))
					values.put("la_direction",0);
				else 
					values.put("la_direction",0);
				
				tmp = edit_latitude_direction.getText().toString();
				mLast.edit().putString ("latitude_direction",tmp).commit();	
				if(tmp.equals("East"))
					values.put("lo_direction",0);
				else 
					values.put("lo_direction",0);
				
				temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
				mLast.edit().putInt ("longitude_angle",temp).commit();
				
				double longitude_direction = (double)temp;
				longitude_direction = longitude_direction/10;	
				if(mLast.getString ("longitude_direction","East").equals("East")){
					values.put("longitude",longitude_direction);
				}	
				else 
					values.put("longitude",0-longitude_direction);
				
				temp=Integer.parseInt(edittext_latitude_angle0.getText().toString())*10+Integer.parseInt(edittext_latitude_angle1.getText().toString());
				mLast.edit().putInt("latitude_angle",temp).commit();

				double latitude_direction = (double)temp;
				latitude_direction = latitude_direction/10;	
				if(mLast.getString ("latitude_direction","North").equals("North")){				
					values.put("latitude",latitude_direction);
				}	
				else 
					values.put("latitude",0-latitude_direction);
				/*
				DTVScanDvbsConfig.this.getContentResolver().update(DVBClient.TABLE_SAT_PARA, values,null, null);

				if(mLockDvb!=null)
					mLockDvb.syncDatabase(DVBClient.TABLE_SAT_PARA, -1);
				else
					Log.d(TAG,">>>>>>>>>>>>>>>>DVBPlayer.getConnect().syncDatabase fail<<<<<<<<<<<<<<<");
				*/
				/*
				editSatData(sat_id, values, "local_longitude_latitude");
				
				try{
					    Field field = dialog.getClass()
					            .getSuperclass().getDeclaredField("mShowing");
					    field.setAccessible(true);
					    field.set(dialog, true);
					    dialog.dismiss();
					}
					catch (Exception e){
					}
                          }
                      });
               setLocationBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				try{
				    Field field = dialog.getClass()
				            .getSuperclass().getDeclaredField("mShowing");
				    field.setAccessible(true);
				    field.set(dialog, true);
				    dialog.dismiss();
				}
				catch (Exception e)	{
				}           
                          }
                      });

		AlertDialog alert = setLocationBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								}         
								});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);

		//alert.getWindow().setLayout(displayMetrics.widthPixels /2,-1);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f; 
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	*/

	private void showSetLocationDia(){
		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);

		if(SatInfo==null||SatInfo.getSwtPort()!=DbSat.LNB_DISEQC_13){
			return;
		}

		final int sat_id = SatInfo.getSatId();

		String longitude_direction = mLast.getString("longitude_direction","East");
		String latitude_direction = mLast.getString("latitude_direction","North");

		final int longitude_angle = mLast.getInt("longitude_angle",0);
		final int latitude_angle = mLast.getInt("latitude_angle",0);

		ContentValues values=null;
	  	mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_set_location_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.dish_setup_conf_button_des8_info));


		final Button edit_longitude_direction = (Button)window.findViewById(R.id.edit_longitude_direction); 
		final EditText edittext_angle0 = (EditText) window.findViewById(R.id.angle0); 
		final EditText edittext_angle1 = (EditText) window.findViewById(R.id.angle1);

		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		
		final Button edit_latitude_direction = (Button)window.findViewById(R.id.edit_latitude_direction); 
		final EditText edittext_latitude_angle0 = (EditText) window.findViewById(R.id.latitude_angle0); 
		final EditText edittext_latitude_angle1 = (EditText) window.findViewById(R.id.latitude_angle1);
		edittext_latitude_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		edittext_latitude_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
			
		if(longitude_direction.equals("East")){
			edit_longitude_direction.setText("East");
		}
		else{
			edit_longitude_direction.setText("West");
		}	
		edit_longitude_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(edit_longitude_direction.getText().equals("East")){
					edit_longitude_direction.setText("West");
				}
				else{
					edit_longitude_direction.setText("East");
				}
			}
		});
		edittext_angle0.setText(String.valueOf(Math.abs(longitude_angle)/10));
		edittext_angle1.setText(String.valueOf(Math.abs(longitude_angle%10)));

		
		if(latitude_direction.equals("North")){
			edit_latitude_direction.setText("North");
		}
		else{
			edit_latitude_direction.setText("South");
		}
		edit_latitude_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub
				if(edit_latitude_direction.getText().equals("North")){
					edit_latitude_direction.setText("South");				
				}
				else{
					edit_latitude_direction.setText("North");
				}
			}
		});
		edittext_latitude_angle0.setText(String.valueOf(Math.abs(latitude_angle)/10));
		edittext_latitude_angle1.setText(String.valueOf(Math.abs(latitude_angle%10)));


		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
					ContentValues values= new ContentValues();
				
					if(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals("")||edittext_latitude_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals("")){
						
						Toast toast = Toast.makeText(
						DTVScanDvbsConfig.this, 
						R.string.lo_la_input_invalid,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show(); 
						
						return;	
					}
					

					int temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
					if(temp>1800){
						Toast toast = Toast.makeText(
						DTVScanDvbsConfig.this, 
						R.string.lo_la_input_invalid,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show(); 
						
						return;	
					}
					
					temp=Integer.parseInt(edittext_latitude_angle0.getText().toString())*10+Integer.parseInt(edittext_latitude_angle1.getText().toString());
					if(temp>900){
						Toast toast = Toast.makeText(
						DTVScanDvbsConfig.this, 
						R.string.lo_la_input_invalid,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show(); 	

						
						return;	
					}
				
					String tmp = edit_longitude_direction.getText().toString();
					mLast.edit().putString ("longitude_direction",tmp).commit();
					if(tmp.equals("North"))
						values.put("la_direction",0);
					else 
						values.put("la_direction",0);
					
					tmp = edit_latitude_direction.getText().toString();
					mLast.edit().putString ("latitude_direction",tmp).commit();	
					if(tmp.equals("East"))
						values.put("lo_direction",0);
					else 
						values.put("lo_direction",0);
					
					temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
					mLast.edit().putInt ("longitude_angle",temp).commit();
					
					double longitude_direction = (double)temp;
					longitude_direction = longitude_direction/10;	
					if(mLast.getString ("longitude_direction","East").equals("East")){
						values.put("longitude",longitude_direction);
					}	
					else 
						values.put("longitude",0-longitude_direction);
					
					temp=Integer.parseInt(edittext_latitude_angle0.getText().toString())*10+Integer.parseInt(edittext_latitude_angle1.getText().toString());
					mLast.edit().putInt("latitude_angle",temp).commit();

					double latitude_direction = (double)temp;
					latitude_direction = latitude_direction/10;	
					if(mLast.getString ("latitude_direction","North").equals("North")){				
						values.put("latitude",latitude_direction);
					}	
					else 
						values.put("latitude",0-latitude_direction);
					/*
					DTVScanDvbsConfig.this.getContentResolver().update(DVBClient.TABLE_SAT_PARA, values,null, null);

					if(mLockDvb!=null)
						mLockDvb.syncDatabase(DVBClient.TABLE_SAT_PARA, -1);
					else
						Log.d(TAG,">>>>>>>>>>>>>>>>DVBPlayer.getConnect().syncDatabase fail<<<<<<<<<<<<<<<");
					*/
					editSatData(sat_id, values, "local_longitude_latitude");
					
					

					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							
						}         
						});	
		
	}
	
	private int getPositionNumberFromScanList(int sat_id,int moto_no,int cur_pos){
	    int pos=1;
		TVSatellite temp_TVSatellite = TVSatellite.tvSatelliteSelect(mContext,sat_id);
		pos=temp_TVSatellite.getValidMotorPositionNum(0,cur_pos);
		return pos;
	}

	private void storePosition(Object cmd){
		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		int sat_id = SatInfo.getSatId();
		int moto_no = SatInfo.getMotoNo();
		int position_no = 1;
		
		//Log.d(TAG,"scan_id--"+scan_id+"sat_id---"+sat_id+"moto_no--"+moto_no+"pos="+SatInfo.getPositionNumber());

		//alloc a valide position number on this moto_no
		position_no = getPositionNumberFromScanList(sat_id,SatInfo.getMotoNo(),SatInfo.getPositionNumber());
			
		Log.d(TAG,"getPositionNumber=="+position_no);

		SatInfo.setPositionNumber(position_no);

		ContentValues values = new ContentValues();
		values.put("pos_num", SatInfo.getPositionNumber());

		editSatData(sat_id, values,"pos_num");
		//client storeposition function
		t.onSetupCmd(t.ROTOR_CMD_STORE_POSITION,cmd);

		return;
	}

	private void gotoPosition(Object cmd){
		//client storeposition function
		t.onSetupCmd(t.ROTOR_CMD_GOTO_POSITION,(Object)cmd);
		return;
	}

	private void gotoX(Object cmd){
		//public static final int ROTOR_CMD_GOTOX= 8;	//!< para: "local_longitude local_latitude satellite_longitude"
		int longitude=0;
		int latitude=0;
		double sat_position=0;

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		sat_position = SatInfo.getPosition();
		//client storeposition function
		if(mLast!=null){
			if(mLast.getString("longitude_direction","East").equals("East"))
				longitude = mLast.getInt("longitude_angle",0);
			else
				longitude = 0 - mLast.getInt("longitude_angle",0);
			
			//!< latitude direction GO_NORTH, GO_SOUTH
			if(mLast.getString("latitude_direction","North").equals("North"))
				latitude = mLast.getInt("latitude_angle",0);
			else
				latitude = 0 - mLast.getInt("latitude_angle",0);
		}

		Log.d(TAG,"Para String="+String.valueOf(longitude)+" "+String.valueOf(latitude)+" "+String.valueOf(sat_position));	
		//t.onSetupCmd(t.ROTOR_CMD_GOTOX,(Object)new String(String.valueOf(longitude)+" "+String.valueOf(latitude)+" "+String.valueOf(sat_position)));
		t.onSetupCmd(t.ROTOR_CMD_GOTOX,cmd);
		return;
	}


	private void showSureDia(Object cmd){
		final Object tcmd=cmd;
		builder = new AlertDialog.Builder(DTVScanDvbsConfig.this);
		builder.setMessage(R.string.sure)
		    .setCancelable(true)
		    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
		    	storePosition(tcmd);	
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
									show_no_satellites_info();
								}         
								});	
		alert.show();

		//alert.getWindow().setLayout(500,-1);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

				
	}


	void showMoveIcon(ImageView v){
		final Animation animation = new AlphaAnimation(0, 1); // Change alpha from fully visible to invisible
		animation.setDuration(500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // 
		v.setAnimation(animation);
	}

	void showMoveIcon1(ImageView v,TextView text){
		final TextView t=text;
		final Animation animation = new AlphaAnimation(0, 1); // Change alpha from fully visible to invisible
		animation.setDuration(500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(2); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // 
		v.setAnimation(animation);

		animation.setAnimationListener(new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			t.setText("Stop");
		}
		});
	}

	void hideMoveIcon(ImageView v){
		v.setAnimation(null);
	}


	private Handler timer_position_adjust_ts_signal_info_handler = new Handler();   

	private Runnable timer_position_adjust_ts_signal_info_runnable = new Runnable() {

		public void run() {
		 
			timer_position_adjust_ts_signal_info_handler.postDelayed(timer_position_adjust_ts_signal_info_runnable,500);  
			t.onSetupCmd(50,null);
		}   
	};



	public class EventHandler extends Handler {  

		public EventHandler(Looper looper) {  
		     super(looper);  
		}  

		@Override  

		public void handleMessage(Message msg) {  
			   switch (msg.what) { 
					case 50: 
			   		 	updataSignalInfo((DVBFrontendSignalInfo)msg.obj);
					break;
				}  
		}
	}  

	class cmdParams{
		TVChannelParams channel;
		int				unit;
	} 
  
	class getFrontEndInfoThread extends Thread {
		private Handler mHandler = null;

		private static final int ROTOR_CMD_STOP_MOVING		= 0;	//!< para: None
		private static final int ROTOR_CMD_DISABLE_LIMIT	= 1;	//!< para: None
		private static final int ROTOR_CMD_SET_ELIMIT		= 2;	//!< para: None
		private static final int ROTOR_CMD_SET_WLIMIT		= 3;	//!< para: None
		private static final int ROTOR_CMD_MOVE_EAST		= 4;	//!< para: "move_unit"
		private static final int ROTOR_CMD_MOVE_WEST		= 5;	//!< para: "move_unit"
		private static final int ROTOR_CMD_STORE_POSITION	= 6;	//!< para: "position_number"
		private static final int ROTOR_CMD_GOTO_POSITION	= 7;	//!< para: "position_number"
		private static final int ROTOR_CMD_GOTOX		        = 8;

		private static final int LNB_CONTROL_CMD_13V = 9;
		private static final int LNB_CONTROL_CMD_18V = 10;
		
		private static final int LNB_CONTROL_CMD_POWER_OFF = 11;
		private static final int LNB_CONTROL_CMD_POWER_AUTO = 12;

		private static final int LNB_CONTROL_CMD_22K_ON = 13;
		private static final int LNB_CONTROL_CMD_22K_OFF = 14;
		private static final int LNB_CONTROL_CMD_22K_AUTO = 15;
		
		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {

					Log.d(TAG,"------------------------getFrontEndInfoThread---------------");
					switch (msg.what) { 
						case ROTOR_CMD_STOP_MOVING: {													
								diseqcPositionerStopMoving();
							}
							break;
						case ROTOR_CMD_DISABLE_LIMIT:
							{							
								diseqcPositionerDisableLimit();
							}
							break;
						case ROTOR_CMD_SET_ELIMIT:	//!< para: None
							{	Log.d(TAG,">>>>>>>ROTOR_CMD_SET_ELIMIT<<<<<<");						
								diseqcPositionerSetEastLimit();
							}
							break;
						case ROTOR_CMD_SET_WLIMIT:	//!< para: None
							{							
								
								//Log.d(TAG,">>>>>>>ROTOR_CMD_SET_WLIMIT<<<<<<");
								diseqcPositionerSetWestLimit();
							}
							break;
						case ROTOR_CMD_MOVE_EAST:	//!< para: "move_unit"
							diseqcPositionerMoveEast(((cmdParams)msg.obj).channel,((cmdParams)msg.obj).unit);
							break;
						case ROTOR_CMD_MOVE_WEST:	//!< para: "move_unit"
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_MOVE_WEST,(String)msg.obj);
							diseqcPositionerMoveWest(((cmdParams)msg.obj).channel,((cmdParams)msg.obj).unit);
							break;
						case ROTOR_CMD_STORE_POSITION:	//!< para: "position_number"
							{							
								diseqcPositionerStorePosition(((cmdParams)msg.obj).channel);
								//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STORE_POSITION,(String)msg.obj);
							}
							break;
						
						case ROTOR_CMD_GOTO_POSITION	://!< para: "position_number"
							{							
								//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_GOTO_POSITION,(String)msg.obj);
								diseqcPositionerGotoPosition(((cmdParams)msg.obj).channel);
							}
							break;
						case ROTOR_CMD_GOTOX	://!< para: "position_number"
							{							
								//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_GOTOX,(String)msg.obj);
								diseqcPositionerGotoX(((cmdParams)msg.obj).channel);
							}
							break;	
						case 50:
							{		
								
								DVBFrontendSignalInfo info = new DVBFrontendSignalInfo();

								info.ber = getFrontendBER();
								info.snr = getFrontendSNR();
								info.strength = getFrontendSignalStrength();
								if(getFrontendStatus()==32)
								
								 info.lock_status = false;
								else
									info.lock_status=true;

								Log.d(TAG, "report : ber:" + info.ber + " snr:" + info.snr + " strength:" + info.strength+"lock status:"+info.lock_status);

								Message message=new Message();
								message.what=50;
								message.obj= (Object)info;
			
								EventHandler ha =new EventHandler(Looper.getMainLooper());  
								ha.sendMessage(message);
							}
							break;
						case LNB_CONTROL_CMD_13V:
						case LNB_CONTROL_CMD_18V:
							sec_setLnbsSwitchCfgValid(((cmdParams)msg.obj).channel);		
							break;
						case  LNB_CONTROL_CMD_POWER_OFF:
							
							sec_setLnbsSwitchCfgValid(((cmdParams)msg.obj).channel);		
							break;
						case LNB_CONTROL_CMD_POWER_AUTO:
							
							sec_setLnbsSwitchCfgValid(((cmdParams)msg.obj).channel);		
							break;
						case  LNB_CONTROL_CMD_22K_ON:
							sec_setLnbsSwitchCfgValid(((cmdParams)msg.obj).channel);		
							break;
						case LNB_CONTROL_CMD_22K_OFF:
						case LNB_CONTROL_CMD_22K_AUTO:	
							sec_setLnbsSwitchCfgValid(((cmdParams)msg.obj).channel);		
							break;
							
						default:
							break;
					}  
				}
			};

			Looper.loop();
			Log.d(TAG, "work thread will now exit.");
		}

		public void quitLoop() {
			if (mHandler != null && mHandler.getLooper() != null) {
				mHandler.getLooper().quit();
			}
		}
		
		public void onSetupCmd(int cmd, Object para ) {
			if (mHandler != null){
				mHandler.sendMessage(mHandler.obtainMessage(cmd,para));	
			}	
		}

		

	}

	private AlertDialog.Builder diaBuilder;
	private AlertDialog alert; 
	private int SetLimitItemSelected = 0;
	View dvbs_set_limit_list;
	private getFrontEndInfoThread t = null;
	private boolean resetrotorstatuscache = false;

	/*
	private void showSetLimitAndPositionDia(){
		ContentValues values=null;

		t =new getFrontEndInfoThread();  
		t.start();  

		diaBuilder = new AlertDialog.Builder(this);
	    LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_set_limit_list = layoutInflater.inflate(R.layout.dvbs_set_limit_dia, null); 
		diaBuilder.setTitle(R.string.dish_setup_conf_button_des7_info);

		//List<String> dataList = getUnicableUserDefinedData();
		final ListView LimitListView = (ListView)dvbs_set_limit_list.findViewById(R.id.set_limit_list); 

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		
		if(SatInfo.getSwtPort()==DbSat.LNB_DISEQC_13){
			LimitListView.setAdapter(new DvbsSetLimitAdapter(this,1));
		}
		else if(SatInfo.getSwtPort()==DbSat.LNB_DISEQC_12){
			LimitListView.setAdapter(new DvbsSetLimitAdapter(this,0));		
		}


		if(tsInfoList==null||tsInfoList.size()==0){
			return;
		}

		Log.d(TAG,"list_cur_pos="+list_cur_pos);
		final DbTransponder TsInfo  = queryTsData(list_cur_pos);
		Log.d(TAG,"ts db-id="+TsInfo.getDbId());
		Log.d(TAG,"fre="+TsInfo.getFrequency()+"symbol="+TsInfo.getSymbol());
		TVChannel mTVChannel = TVChannel.selectByID(this,TsInfo.getDbId());
		
		TVChannelParams mTVChannelParams = mTVChannel.getParams();

		final cmdParams my_cmdParams = new cmdParams();

		my_cmdParams.channel = mTVChannelParams;
		
		if(mTVChannelParams!=null)
			lock(mTVChannelParams);
	
		final TextView edittext_frequency= (TextView) dvbs_set_limit_list.findViewById(R.id.edittext_frequency);
		final TextView edittext_symbol = (TextView) dvbs_set_limit_list.findViewById(R.id.edittext_symbol);
		final TextView polarization = (TextView) dvbs_set_limit_list.findViewById(R.id.polarization);

    		final ProgressBar ProgressBarSNR = (ProgressBar)dvbs_set_limit_list.findViewById(R.id.ProgressBarSNR);
    		ProgressBarSNR.setMax(100);
    		
		final ProgressBar ProgressBarAGC = (ProgressBar)dvbs_set_limit_list.findViewById(R.id.ProgressBarAGC);
    		ProgressBarAGC.setMax(100);

		//final ProgressBar ProgressBarBER = (ProgressBar)dvbs_set_limit_list.findViewById(R.id.ProgressBarBER);
    		//ProgressBarBER.setMax(100);

		final TextView snr_value = (TextView) dvbs_set_limit_list.findViewById(R.id.snr_value);
		final TextView agc_value = (TextView) dvbs_set_limit_list.findViewById(R.id.agc_value);
		//final TextView ber_value = (TextView) dvbs_set_limit_list.findViewById(R.id.ber_value);	

		
		if(TsInfo!=null){
			edittext_frequency.setText(String.valueOf(TsInfo.getFrequency()));
			edittext_symbol.setText(String.valueOf(TsInfo.getSymbol()));

			if(TsInfo.getPolarization()==1)
				polarization.setText("V");
			else
				polarization.setText("H");

			snr_value.setText("0%");
			agc_value.setText("0%");
			//ber_value.setText("0%");
		}
		
		LimitListView.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
				Log.d(TAG,"sat_list setOnItemSelectedListener " + position);

				SetLimitItemSelected = position;
				
			}

			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG,"<<sat_list onNothingSelected>> ");
			}
		});
		LimitListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
	        {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
					final TextView text =(TextView) arg1.findViewById(R.id.info);
					final ImageView icon=(ImageView)arg1.findViewById(R.id.icon);	
					final ImageView icon1=(ImageView)arg1.findViewById(R.id.icon1);
					
					
					// TODO Auto-generated method stub
					System.out.println("onItemSelected arg0 " + arg0);
					System.out.println("onItemSelected arg1 " + arg1);
					System.out.println("onItemSelected arg2 " + arg2);
					System.out.println("onItemSelected arg3 " + arg3);

					/** Rotor commands for Satellite source, para: para string format in sendRotorCmd */
					//public static final int ROTOR_CMD_STOP_MOVING		= 0;	//!< para: None
					//public static final int ROTOR_CMD_DISABLE_LIMIT	= 1;	//!< para: None
					//public static final int ROTOR_CMD_SET_ELIMIT		= 2;	//!< para: None
					//public static final int ROTOR_CMD_SET_WLIMIT		= 3;	//!< para: None
					//public static final int ROTOR_CMD_MOVE_EAST		= 4;	//!< para: "move_unit"
					//public static final int ROTOR_CMD_MOVE_WEST		= 5;	//!< para: "move_unit"
					//public static final int ROTOR_CMD_STORE_POSITION	= 6;	//!< para: "position_number"
					//public static final int ROTOR_CMD_GOTO_POSITION	= 7;	//!< para: "position_number"
	/*
					switch(arg2){
						case 0:  //move continue
							text.setText("Stop");	
							hideMoveIcon(icon);
							hideMoveIcon(icon1);
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STOP_MOVING,null);
							 t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);
							break;
						case 1:  //move one step
							text.setText("Stop");
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STOP_MOVING,null);
							 t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);
							break;
						case 2://set east limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_SET_ELIMIT,null);
							 t.onSetupCmd(t.ROTOR_CMD_SET_ELIMIT,null);
							break;
						case 3: //set west limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_SET_WLIMIT,null);
							 t.onSetupCmd(t.ROTOR_CMD_SET_WLIMIT,null);
							break;
						case 4://disable limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_DISABLE_LIMIT,null);
							t.onSetupCmd(t.ROTOR_CMD_DISABLE_LIMIT,null);
							break;
						case 5: //store position
							showSureDia(my_cmdParams);
							break;
						case 6 ://goto position
							gotoPosition(my_cmdParams);
							resetrotorstatuscache = true;
							break;
						case 7: //gotoX
							gotoX(my_cmdParams);
							resetrotorstatuscache = true;
							break;
					}
				}
	        	    
	        });

		LimitListView.setOnKeyListener( new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				int pos=0;	
					TextView text=null;	
					ImageView icon =null;
					ImageView icon1=null;
					ListView listView = (ListView) v;
					DvbsSetLimitAdapter adapter = null;
					if (listView.getSelectedView() != null) {
					        // (cast if necessary) and use selected view
						//pos = listView.getSelected();
						View view = listView.getChildAt(SetLimitItemSelected);
						text =(TextView) view.findViewById(R.id.info);
						icon = (ImageView)  view.findViewById(R.id.icon);
						icon1 = (ImageView)  view.findViewById(R.id.icon1);
						adapter = (DvbsSetLimitAdapter)listView.getAdapter();
				}
				
 				if (event.getAction() == KeyEvent.ACTION_UP) {
 	
	        			
		
					// TODO Auto-generated method stub
					switch(keyCode)
					{	
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=0;
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								hideMoveIcon(icon);
								showMoveIcon(icon1);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								showMoveIcon1(icon1,text);	
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							return true;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=0;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								hideMoveIcon(icon1);
								showMoveIcon(icon);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								showMoveIcon1(icon,text);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
								
							}
							return true;
					}
 				}	
				else if(event.getAction() == KeyEvent.ACTION_DOWN){
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_DOWN:
							Log.d(TAG,"----continue is stop-----"+SetLimitItemSelected);
							if(SetLimitItemSelected==0)
							{
								hideMoveIcon(icon);
								hideMoveIcon(icon1);
								text.setText("Stop");
								t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);						
							}
							break;
					}	
				}	
				return false;
	            }
			
		});	
		
				
		diaBuilder.setView(dvbs_set_limit_list);
	
		alert = diaBuilder.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
									t.onSetupCmd(50,null);
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							t.quitLoop();	
							timer_position_adjust_ts_signal_info_handler.removeCallbacks(timer_position_adjust_ts_signal_info_runnable);  
							if(resetrotorstatuscache){
								resetrotorstatuscache = false;
								//mLockDvb.resetrotorstatuscache();
							}
							}         
							});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		//alert.getWindow().setLayout(displayMetrics.widthPixels / 2, -1);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.00f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		timer_position_adjust_ts_signal_info_handler.postDelayed(timer_position_adjust_ts_signal_info_runnable, 500);
	}

	*/
	
	private void showSetLimitAndPositionDia(){
		ContentValues values=null;

		t =new getFrontEndInfoThread();  
		t.start(); 

		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_set_limit_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.dish_setup_conf_button_des7_info));

		//List<String> dataList = getUnicableUserDefinedData();
		final ListView LimitListView = (ListView)window.findViewById(R.id.set_limit_list); 

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		
		if(SatInfo.getSwtPort()==DbSat.LNB_DISEQC_13){
			LimitListView.setAdapter(new DvbsSetLimitAdapter(this,1));
		}
		else if(SatInfo.getSwtPort()==DbSat.LNB_DISEQC_12){
			LimitListView.setAdapter(new DvbsSetLimitAdapter(this,0));		
		}


		if(tsInfoList==null||tsInfoList.size()==0){
			return;
		}

		Log.d(TAG,"list_cur_pos="+list_cur_pos);
		final DbTransponder TsInfo  = queryTsData(list_cur_pos);
		Log.d(TAG,"ts db-id="+TsInfo.getDbId());
		Log.d(TAG,"fre="+TsInfo.getFrequency()+"symbol="+TsInfo.getSymbol());
		TVChannel mTVChannel = TVChannel.selectByID(this,TsInfo.getDbId());
		
		TVChannelParams mTVChannelParams = mTVChannel.getParams();

		final cmdParams my_cmdParams = new cmdParams();

		my_cmdParams.channel = mTVChannelParams;
		
		if(mTVChannelParams!=null)
			lock(mTVChannelParams);
	
		final TextView edittext_frequency= (TextView) window.findViewById(R.id.edittext_frequency);
		final TextView edittext_symbol = (TextView) window.findViewById(R.id.edittext_symbol);
		final TextView polarization = (TextView) window.findViewById(R.id.polarization);

    		final ProgressBar ProgressBarSNR = (ProgressBar)window.findViewById(R.id.ProgressBarSNR);
    		ProgressBarSNR.setMax(100);
    		
		final ProgressBar ProgressBarAGC = (ProgressBar)window.findViewById(R.id.ProgressBarAGC);
    		ProgressBarAGC.setMax(100);

		//final ProgressBar ProgressBarBER = (ProgressBar)dvbs_set_limit_list.findViewById(R.id.ProgressBarBER);
    		//ProgressBarBER.setMax(100);

		final TextView snr_value = (TextView) window.findViewById(R.id.snr_value);
		final TextView agc_value = (TextView) window.findViewById(R.id.agc_value);
		//final TextView ber_value = (TextView) dvbs_set_limit_list.findViewById(R.id.ber_value);	
		final CheckBox checkboxStatus = (CheckBox)window.findViewById(R.id.checkStatus);

		checkboxStatus.setFocusable(false);   
			
		if(TsInfo!=null){
			edittext_frequency.setText(String.valueOf(TsInfo.getFrequency()));
			edittext_symbol.setText(String.valueOf(TsInfo.getSymbol()));

			if(TsInfo.getPolarization()==1)
				polarization.setText("V");
			else
				polarization.setText("H");

			snr_value.setText("0%");
			agc_value.setText("0%");
			//ber_value.setText("0%");
		}
		
		LimitListView.setOnItemSelectedListener(new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view,
			int position, long id) {
				Log.d(TAG,"sat_list setOnItemSelectedListener " + position);

				SetLimitItemSelected = position;
				
			}

			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG,"<<sat_list onNothingSelected>> ");
			}
		});
		LimitListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
	        {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
					final TextView text =(TextView) arg1.findViewById(R.id.info);
					final ImageView icon=(ImageView)arg1.findViewById(R.id.icon);	
					final ImageView icon1=(ImageView)arg1.findViewById(R.id.icon1);
					
					
					// TODO Auto-generated method stub
					System.out.println("onItemSelected arg0 " + arg0);
					System.out.println("onItemSelected arg1 " + arg1);
					System.out.println("onItemSelected arg2 " + arg2);
					System.out.println("onItemSelected arg3 " + arg3);

					/** Rotor commands for Satellite source, para: para string format in sendRotorCmd */
					//public static final int ROTOR_CMD_STOP_MOVING		= 0;	//!< para: None
					//public static final int ROTOR_CMD_DISABLE_LIMIT	= 1;	//!< para: None
					//public static final int ROTOR_CMD_SET_ELIMIT		= 2;	//!< para: None
					//public static final int ROTOR_CMD_SET_WLIMIT		= 3;	//!< para: None
					//public static final int ROTOR_CMD_MOVE_EAST		= 4;	//!< para: "move_unit"
					//public static final int ROTOR_CMD_MOVE_WEST		= 5;	//!< para: "move_unit"
					//public static final int ROTOR_CMD_STORE_POSITION	= 6;	//!< para: "position_number"
					//public static final int ROTOR_CMD_GOTO_POSITION	= 7;	//!< para: "position_number"
	
					switch(arg2){
						case 0:  //move continue
							text.setText("Stop");	
							hideMoveIcon(icon);
							hideMoveIcon(icon1);
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STOP_MOVING,null);
							 t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);
							break;
						case 1:  //move one step
							text.setText("Stop");
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_STOP_MOVING,null);
							 t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);
							break;
						case 2://set east limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_SET_ELIMIT,null);
							 t.onSetupCmd(t.ROTOR_CMD_SET_ELIMIT,null);
							break;
						case 3: //set west limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_SET_WLIMIT,null);
							 t.onSetupCmd(t.ROTOR_CMD_SET_WLIMIT,null);
							break;
						case 4://disable limit
							//mLockDvb.sendRotorCommand(mLockDvb.ROTOR_CMD_DISABLE_LIMIT,null);
							t.onSetupCmd(t.ROTOR_CMD_DISABLE_LIMIT,null);
							break;
						case 5: //store position
							showSureDia(my_cmdParams);
							break;
						case 6 ://goto position
							gotoPosition(my_cmdParams);
							resetrotorstatuscache = true;
							break;
						case 7: //gotoX
							gotoX(my_cmdParams);
							resetrotorstatuscache = true;
							break;
					}
				}
	        	    
	        });

		LimitListView.setOnKeyListener( new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				int pos=0;	
					TextView text=null;	
					ImageView icon =null;
					ImageView icon1=null;
					ListView listView = (ListView) v;
					DvbsSetLimitAdapter adapter = null;
					if (listView.getSelectedView() != null) {
					        // (cast if necessary) and use selected view
						//pos = listView.getSelected();
						View view = listView.getChildAt(SetLimitItemSelected);
						text =(TextView) view.findViewById(R.id.info);
						icon = (ImageView)  view.findViewById(R.id.icon);
						icon1 = (ImageView)  view.findViewById(R.id.icon1);
						adapter = (DvbsSetLimitAdapter)listView.getAdapter();
				}
				
 				if (event.getAction() == KeyEvent.ACTION_UP) {
 	
	        			
		
					// TODO Auto-generated method stub
					switch(keyCode)
					{	
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=0;
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								hideMoveIcon(icon);
								showMoveIcon(icon1);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_RIGHT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_EAST,(Object)my_cmdParams);
								text.setText("East");
								showMoveIcon1(icon1,text);	
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							return true;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if(SetLimitItemSelected==0){
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=0;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								hideMoveIcon(icon1);
								showMoveIcon(icon);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
							}
							else if(SetLimitItemSelected==1) {
								Log.d(TAG,"KEYCODE_DPAD_LEFT"+SetLimitItemSelected);
								my_cmdParams.unit=1;	
								t.onSetupCmd(t.ROTOR_CMD_MOVE_WEST,(Object)my_cmdParams);
								text.setText("West");
								showMoveIcon1(icon,text);
								adapter.notifyDataSetChanged();
								resetrotorstatuscache = true;
								
							}
							return true;
					}
 				}	
				else if(event.getAction() == KeyEvent.ACTION_DOWN){
					switch(keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_DOWN:
							Log.d(TAG,"----continue is stop-----"+SetLimitItemSelected);
							if(SetLimitItemSelected==0)
							{
								hideMoveIcon(icon);
								hideMoveIcon(icon1);
								text.setText("Stop");
								t.onSetupCmd(t.ROTOR_CMD_STOP_MOVING,null);						
							}
							break;
					}	
				}	
				return false;
	            }
			
		});	
		

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
								t.onSetupCmd(50,null);
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							t.quitLoop();	
							timer_position_adjust_ts_signal_info_handler.removeCallbacks(timer_position_adjust_ts_signal_info_runnable);  
							if(resetrotorstatuscache){
								resetrotorstatuscache = false;
								//mLockDvb.resetrotorstatuscache();
							}
						}         
						});	

		timer_position_adjust_ts_signal_info_handler.postDelayed(timer_position_adjust_ts_signal_info_runnable, 500);
		
	}

	public static class DefaultList{
		int scan_id;
		int sat_id;
		//DVBSatelliteParameter sat;
		//ArrayList<DVBFrontEndPara> ts_list=null;
		TVSatellite sat;
		ArrayList<TVChannelParams> ts_list=null;
	}
	
	private static ArrayList<DefaultList> default_list=null;

	private static  ArrayList<DefaultList> getScanParaList(){
		if(default_list==null)
			default_list = new ArrayList<DefaultList>();

		default_list.clear();
		
		if(ScanSatAndtsInfoList.size()>0){
			Log.d(TAG,"sat ts para size=="+ScanSatAndtsInfoList.size());
			for(int i=0;i<ScanSatAndtsInfoList.size();i++){
				boolean ts_node_sel_flag = false;
					
				DbSat sat_temp = ScanSatAndtsInfoList.get(i);
				//check ts info
				List<DbTransponder> ts_list_temp = sat_temp.transponder;
				
				 DefaultList dvb_sat_para = new DefaultList();
				if(sat_temp.getSelectedFlag()){
					//add to default list	
					
					if(ts_list_temp!=null&&ts_list_temp.size()>0){
						for(int j=0;j<ts_list_temp.size();j++){
							DbTransponder ts_temp = ts_list_temp.get(j);
							if(ts_temp.getSelectedFlag()){
								//add ts info into default list
								ts_node_sel_flag=true;
								break;
							}
						}
					}
					

					if(ts_node_sel_flag){
						for(int k=0;k<ts_list_temp.size();k++){
							DbTransponder ts_temp_node = ts_list_temp.get(k);
							if(ts_temp_node.getSelectedFlag()){
								//add ts info into default list
								if(dvb_sat_para.ts_list==null)
									dvb_sat_para.ts_list = new ArrayList<TVChannelParams>();

								TVChannelParams ts_node = TVChannelParams.dvbsParams(mContext, ts_temp_node.getFrequency(),
																											ts_temp_node.getSymbol(), 
																											ts_temp_node.getSatId(), 
																											ts_temp_node.getPolarization());
								
				
								dvb_sat_para.ts_list.add(ts_node);
							}
						}
					}
					else{
						//add all
						if(ts_list_temp==null){
							ScanSatAndtsInfoList.get(i).transponder= getScanTsList(ScanSatAndtsInfoList.get(i));
							ts_list_temp = ScanSatAndtsInfoList.get(i).transponder;
						}	
						
						if(ts_list_temp!=null&&ts_list_temp.size()>0){
							for(int m=0;m<ts_list_temp.size();m++){
								DbTransponder ts_temp_node_1 = ts_list_temp.get(m);
								
								//add ts info into default list
								if(dvb_sat_para.ts_list==null)
									dvb_sat_para.ts_list = new ArrayList<TVChannelParams>();
								
								TVChannelParams ts_node_1 = TVChannelParams.dvbsParams(mContext, ts_temp_node_1.getFrequency(),
																											ts_temp_node_1.getSymbol(), 
																											ts_temp_node_1.getSatId(), 
																											ts_temp_node_1.getPolarization());
								
								dvb_sat_para.ts_list.add(ts_node_1);
								
							}
						}	
					}

					dvb_sat_para.sat = new TVSatellite();
					dvb_sat_para.sat.tv_satparams = new TVSatelliteParams();
					//dvb_sat_para.sat.lnb_num = sat_temp.getLnbNo();//lnb No.
					dvb_sat_para.sat.sat_name = sat_temp.getName(); //satellite name
					
					dvb_sat_para.sat.tv_satparams.sat_longitude = (double)(sat_temp.getSatLongitude());//satellite longitude
					dvb_sat_para.sat.tv_satparams.lnb_lof_lo = sat_temp.getLoLOF(); //lof_low
					dvb_sat_para.sat.tv_satparams.lnb_lof_hi = sat_temp.getHiLOF(); //lof_hi
					dvb_sat_para.sat.tv_satparams.lnb_lof_threadhold = sat_temp.getLofThreshold(); //threshold
					dvb_sat_para.sat.tv_satparams.sec_voltage_status = sat_temp.getLNBPwrOnOff();//voltage
					
					dvb_sat_para.sat.tv_satparams.sec_22k_status = sat_temp.get22KOnOff(); //22k
					dvb_sat_para.sat.tv_satparams.sec_tone_burst = sat_temp.getToneburstType();
					
					dvb_sat_para.sat.tv_satparams.diseqc_sequence_repeat = sat_temp.getDiseqcRepeat();
				
					dvb_sat_para.sat.tv_satparams.diseqc_committed = sat_temp.getLnbConfig10();
					dvb_sat_para.sat.tv_satparams.diseqc_uncommitted = sat_temp.getLnbConfig11();
					dvb_sat_para.sat.tv_satparams.diseqc_order = sat_temp.getDiseqcSequence();
					dvb_sat_para.sat.tv_satparams.diseqc_fast = sat_temp.getFastDiseqc();

					if(sat_temp.getSwtPort()==DbSat.LNB_DISEQC_12){
						dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_2;
						dvb_sat_para.sat.tv_satparams.motor_position_num = sat_temp.getPositionNumber();
					}
					else if(sat_temp.getSwtPort()==DbSat.LNB_DISEQC_13){
						dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_3;
						dvb_sat_para.sat.tv_satparams.motor_position_num = sat_temp.getPositionNumber();
					}
					else{
						dvb_sat_para.sat.tv_satparams.diseqc_mode = sat_temp.getSwtPort();
					}

					/*
					//!< longitude direction GO_EAST, GO_WEST
					if(mLast.getString("longitude_direction","East").equals("East"))
						dvb_sat_para.sat.lo_direction = TVSatelliteParams.GO_EAST;
					else
						dvb_sat_para.sat.lo_direction = TVSatelliteParams.GO_WEST;
					
					//!< latitude direction GO_NORTH, GO_SOUTH
					if(mLast.getString("latitude_direction","North").equals("North"))
						dvb_sat_para.sat.la_direction = TVSatelliteParams.GO_NORTH;
					else
						dvb_sat_para.sat.la_direction = TVSatelliteParams.GO_SOUTH;

					dvb_sat_para.sat.longitude = mLast.getInt("longitude_angle",0);
					dvb_sat_para.sat.latitude = mLast.getInt("latitude_angle",0);
					*/
					dvb_sat_para.sat_id = sat_temp.getSatId();	
					
					default_list.add(dvb_sat_para);
				}
				else{
					if(ts_list_temp!= null&&ts_list_temp.size()>0){
						for(int h=0;h<ts_list_temp.size();h++){
							DbTransponder ts_temp = ts_list_temp.get(h);
							if(ts_temp.getSelectedFlag()){
								ts_node_sel_flag = true;
								//add ts info into default list
								if(dvb_sat_para.ts_list==null)
									dvb_sat_para.ts_list = new ArrayList<TVChannelParams>();

								TVChannelParams ts_node_2 = TVChannelParams.dvbsParams(mContext, ts_temp.getFrequency(),
																											ts_temp.getSymbol(), 
																											ts_temp.getSatId(), 
																											ts_temp.getPolarization());
								
								dvb_sat_para.ts_list.add(ts_node_2);
							}
						}
					}
					

					if(ts_node_sel_flag){

						dvb_sat_para.sat = new TVSatellite();
						dvb_sat_para.sat.tv_satparams = new TVSatelliteParams();
						//dvb_sat_para.sat.lnb_num = sat_temp.getLnbNo();//lnb No.
						dvb_sat_para.sat.sat_name = sat_temp.getName(); //satellite name
						
						dvb_sat_para.sat.tv_satparams.sat_longitude = (double)(sat_temp.getSatLongitude());//satellite longitude
						dvb_sat_para.sat.tv_satparams.lnb_lof_lo = sat_temp.getLoLOF(); //lof_low
						dvb_sat_para.sat.tv_satparams.lnb_lof_hi = sat_temp.getHiLOF(); //lof_hi
						dvb_sat_para.sat.tv_satparams.lnb_lof_threadhold = sat_temp.getLofThreshold(); //threshold
						dvb_sat_para.sat.tv_satparams.sec_voltage_status = sat_temp.getLNBPwrOnOff();//voltage
						
						dvb_sat_para.sat.tv_satparams.sec_22k_status = sat_temp.get22KOnOff(); //22k
						dvb_sat_para.sat.tv_satparams.sec_tone_burst = sat_temp.getToneburstType();
						
						dvb_sat_para.sat.tv_satparams.diseqc_sequence_repeat = sat_temp.getDiseqcRepeat();
					
						dvb_sat_para.sat.tv_satparams.diseqc_committed = sat_temp.getLnbConfig10();
						dvb_sat_para.sat.tv_satparams.diseqc_uncommitted = sat_temp.getLnbConfig11();
						dvb_sat_para.sat.tv_satparams.diseqc_order = sat_temp.getDiseqcSequence();
						dvb_sat_para.sat.tv_satparams.diseqc_fast = sat_temp.getFastDiseqc();

						if(sat_temp.getSwtPort()==DbSat.LNB_DISEQC_12){
							dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_2;
							dvb_sat_para.sat.tv_satparams.motor_position_num = sat_temp.getPositionNumber();
						}
						else if(sat_temp.getSwtPort()==DbSat.LNB_DISEQC_13){
							dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_3;
							dvb_sat_para.sat.tv_satparams.motor_position_num = sat_temp.getPositionNumber();
						}
						else{
							dvb_sat_para.sat.tv_satparams.diseqc_mode = sat_temp.getSwtPort();
						}
						dvb_sat_para.sat_id = sat_temp.getSatId();	
						default_list.add(dvb_sat_para);
					}
				}

			}
			
		}

		if(default_list.size()==0){
			DbSat sat_node = ScanSatAndtsInfoList.get(gobal_sat_cur_pos);
			DefaultList dvb_sat_para = new DefaultList();	

			List<DbTransponder> ts_list = sat_node.transponder;

			if(ts_list==null){
				ScanSatAndtsInfoList.get(gobal_sat_cur_pos).transponder= getScanTsList(ScanSatAndtsInfoList.get(gobal_sat_cur_pos));
				ts_list = ScanSatAndtsInfoList.get(gobal_sat_cur_pos).transponder;
			}	

			if(ts_list!= null&&ts_list.size()>0){
				for(int y=0;y<ts_list.size();y++){
					DbTransponder ts_node_temp = ts_list.get(y);
					
					//add ts info into default list
					if(dvb_sat_para.ts_list==null)
						dvb_sat_para.ts_list = new ArrayList<TVChannelParams>();

					TVChannelParams ts_node = TVChannelParams.dvbsParams(mContext, ts_node_temp.getFrequency(),
																								ts_node_temp.getSymbol(), 
																								ts_node_temp.getSatId(), 
																								ts_node_temp.getPolarization());
					
					dvb_sat_para.ts_list.add(ts_node);
				}
			}

			dvb_sat_para.sat = new TVSatellite();			
			dvb_sat_para.sat.tv_satparams = new TVSatelliteParams();
			//dvb_sat_para.sat.lnb_num = sat_temp.getLnbNo();//lnb No.
			dvb_sat_para.sat.sat_name = sat_node.getName(); //satellite name
			dvb_sat_para.sat.tv_satparams.sat_longitude =(double)(sat_node.getSatLongitude());//satellite longitude
			dvb_sat_para.sat.tv_satparams.lnb_lof_lo = sat_node.getLoLOF(); //lof_low
			dvb_sat_para.sat.tv_satparams.lnb_lof_hi = sat_node.getHiLOF(); //lof_hi
			dvb_sat_para.sat.tv_satparams.lnb_lof_threadhold = sat_node.getLofThreshold(); //threshold
			dvb_sat_para.sat.tv_satparams.sec_voltage_status = sat_node.getLNBPwrOnOff();//voltage
			
			dvb_sat_para.sat.tv_satparams.sec_22k_status = sat_node.get22KOnOff(); //22k
			dvb_sat_para.sat.tv_satparams.sec_tone_burst = sat_node.getToneburstType();
			
			dvb_sat_para.sat.tv_satparams.diseqc_sequence_repeat = sat_node.getDiseqcRepeat();
		
			dvb_sat_para.sat.tv_satparams.diseqc_committed = sat_node.getLnbConfig10();
			dvb_sat_para.sat.tv_satparams.diseqc_uncommitted = sat_node.getLnbConfig11();
			dvb_sat_para.sat.tv_satparams.diseqc_order = sat_node.getDiseqcSequence();
			dvb_sat_para.sat.tv_satparams.diseqc_fast = sat_node.getFastDiseqc();

			if(sat_node.getSwtPort()==DbSat.LNB_DISEQC_12){
				dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_2;
				dvb_sat_para.sat.tv_satparams.motor_position_num = sat_node.getPositionNumber();
			}
			else if(sat_node.getSwtPort()==DbSat.LNB_DISEQC_13){
				dvb_sat_para.sat.tv_satparams.diseqc_mode = TVSatelliteParams.DISEQC_MODE_V1_3;
				dvb_sat_para.sat.tv_satparams.motor_position_num = sat_node.getPositionNumber();
			}
			else{
				dvb_sat_para.sat.tv_satparams.diseqc_mode = sat_node.getSwtPort();
			}
			
			dvb_sat_para.sat_id = sat_node.getSatId();		
			default_list.add(dvb_sat_para);	
		}
		
		return default_list;
	}
	
	

	private boolean checkHasTpInfo(String mode){
		int count = 0;
		if(!(mode.equals("blind"))){
			for(int i=0;i<DTVScanDvbsConfig.getDefaultList().size();i++){
				if(DTVScanDvbsConfig.getDefaultList().get(i).ts_list!=null){
					count = count + DTVScanDvbsConfig.getDefaultList().get(i).ts_list.size();
				}
				else 
					continue;
			}			
			
			if(count>0)
				return true;
			else 
				return false;
		}

		return true;
	}


	private AlertDialog.Builder scanBuilder;
	ScanModeAdapter mScanModeAdapter = null;
	/*
	private void showScanConfigDia(){
		
		ContentValues values=null;
		scanBuilder = new AlertDialog.Builder(this);
	        scanBuilder.setTitle(R.string.scan_mode);
		ListView mList = new ListView(this);

		if(mScanModeAdapter==null)
			mScanModeAdapter = new ScanModeAdapter(DTVScanDvbsConfig.this);
		mList.setAdapter(mScanModeAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub

					TextView info = (TextView)arg1.findViewById(R.id.info);
					switch(arg2){
							
						case 0:
							String mode = mLast.getString("scan_mode","default");
							if (isUnicableOn()==false){							
								
								if(mode.equals("default")){
									info.setText(R.string.scan_mode_blind);
									mLast.edit().putString ("scan_mode","blind").commit();
								}	
								else if(mode.equals("blind")){
									info.setText(R.string.scan_mode_network);
									mLast.edit().putString ("scan_mode","network").commit();
								}
								else{
									info.setText(R.string.scan_mode_default);
									mLast.edit().putString ("scan_mode","default").commit();
								}
							}
							else{
								if(mode.equals("default")){
									info.setText(R.string.scan_mode_network);
									mLast.edit().putString ("scan_mode","network").commit();
								}				
								else{
									info.setText(R.string.scan_mode_default);
									mLast.edit().putString ("scan_mode","default").commit();
								}
							}
							break;
						case 1:
							String crypted = mLast.getString("scan_mode_crypted","all");
							if(crypted.equals("all")){
								info.setText(R.string.scan_crypted_fta);
								mLast.edit().putString ("scan_mode_crypted","fta").commit();
							}	
							else if(crypted.equals("fta")){
								info.setText(R.string.scan_crypted_all);
								mLast.edit().putString ("scan_mode_crypted","all").commit();
								
							}
							
							break;
							
						case 2:
							String service_mode = mLast.getString("scan_service_mode","all");
							if(service_mode.equals("all")){
								info.setText(R.string.scan_service_tv);
								mLast.edit().putString ("scan_service_mode","tv").commit();
							}	
							else if(service_mode.equals("tv")){
								info.setText(R.string.scan_service_radio);
								mLast.edit().putString ("scan_service_mode","radio").commit();
							}
							else{
								info.setText(R.string.scan_service_all);
								mLast.edit().putString ("scan_service_mode","all").commit();
							}
							break;
						
					}
				}	
		 });
		
		scanBuilder.setView(mList);

		scanBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {

				String mode = mLast.getString("scan_mode","default");
				String crypted = mLast.getString("scan_mode_crypted","all");
				String service_mode = mLast.getString("scan_service_mode","all");
				Log.d(TAG,"@mode="+mode+"@crypted="+crypted+"@service_mode="+service_mode);
				getScanParaList();

				if(checkHasTpInfo(mode)){
				
					Intent intent_scan= new Intent();
					intent_scan.setClass(DTVScanDvbsConfig.this,DvbsScanResult.class);

					Bundle bundle_scan_dvbs = new Bundle();	
					bundle_scan_dvbs.putString("scan_mode", mode);
					intent_scan.putExtras(bundle_scan_dvbs);

					//startActivityForResult(intent_scan,1);	
					startActivity(intent_scan);	
					DTVScanDvbsConfig.this.finish();
				}
				else{
					Toast toast = Toast.makeText(
						DTVScanDvbsConfig.this, 
				    		R.string.can_not_search,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show(); 	
				}
				
				dialog.dismiss();
                          }
                      });
               scanBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = scanBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								}         
								});	

		alert.show();	

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);
		
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.00f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
	}
	*/

	private void showScanConfigDia(){
		ContentValues values=null;
	  	mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_set_scan_config_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.scan_mode));
		
		ListView mList = (ListView)window.findViewById(R.id.set_list);

		if(mScanModeAdapter==null)
			mScanModeAdapter = new ScanModeAdapter(DTVScanDvbsConfig.this);
		mList.setAdapter(mScanModeAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub

					TextView info = (TextView)arg1.findViewById(R.id.info);
					switch(arg2){
							
						case 0:
							String mode = mLast.getString("scan_mode","default");
							if (isUnicableOn()==false){							
								
								if(mode.equals("default")){
									info.setText(R.string.scan_mode_blind);
									mLast.edit().putString ("scan_mode","blind").commit();
								}	
								else if(mode.equals("blind")){
									info.setText(R.string.scan_mode_network);
									mLast.edit().putString ("scan_mode","network").commit();
								}
								else{
									info.setText(R.string.scan_mode_default);
									mLast.edit().putString ("scan_mode","default").commit();
								}
							}
							else{
								if(mode.equals("default")){
									info.setText(R.string.scan_mode_network);
									mLast.edit().putString ("scan_mode","network").commit();
								}				
								else{
									info.setText(R.string.scan_mode_default);
									mLast.edit().putString ("scan_mode","default").commit();
								}
							}
							break;
						case 1:
							String crypted = mLast.getString("scan_mode_crypted","all");
							if(crypted.equals("all")){
								info.setText(R.string.scan_crypted_fta);
								mLast.edit().putString ("scan_mode_crypted","fta").commit();
							}	
							else if(crypted.equals("fta")){
								info.setText(R.string.scan_crypted_all);
								mLast.edit().putString ("scan_mode_crypted","all").commit();
								
							}
							
							break;
							
						case 2:
							String service_mode = mLast.getString("scan_service_mode","all");
							if(service_mode.equals("all")){
								info.setText(R.string.scan_service_tv);
								mLast.edit().putString ("scan_service_mode","tv").commit();
							}	
							else if(service_mode.equals("tv")){
								info.setText(R.string.scan_service_radio);
								mLast.edit().putString ("scan_service_mode","radio").commit();
							}
							else{
								info.setText(R.string.scan_service_all);
								mLast.edit().putString ("scan_service_mode","all").commit();
							}
							break;
						
					}
				}	
		 });

		no.setFocusable(true);     
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
					String mode = mLast.getString("scan_mode","default");
					String crypted = mLast.getString("scan_mode_crypted","all");
					String service_mode = mLast.getString("scan_service_mode","all");
					Log.d(TAG,"@mode="+mode+"@crypted="+crypted+"@service_mode="+service_mode);
					getScanParaList();

					if(checkHasTpInfo(mode)){
					
						Intent intent_scan= new Intent();
						intent_scan.setClass(DTVScanDvbsConfig.this,DvbsScanResult.class);

						Bundle bundle_scan_dvbs = new Bundle();	
						bundle_scan_dvbs.putString("scan_mode", mode);
						intent_scan.putExtras(bundle_scan_dvbs);

						//startActivityForResult(intent_scan,1);	
						startActivity(intent_scan);	
						DTVScanDvbsConfig.this.finish();
					}
					else{
						Toast toast = Toast.makeText(
							DTVScanDvbsConfig.this, 
					    		R.string.can_not_search,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show(); 	
					}

					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							
						}         
						});	

	}


	private AlertDialog.Builder satEditBuilder;
	private View dvbs_sat_edit;
	/*
	private void showSatEditDia(){
		if(ScanSatAndtsInfoList==null||ScanSatAndtsInfoList.size()==0)
			return;
		
		ContentValues values=null;
		satEditBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_sat_edit = layoutInflater.inflate(R.layout.dvbs_sat_edit_dia, null); 
		satEditBuilder.setTitle(R.string.edit_title);

		DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		
		final EditText edittext_satname = (EditText) dvbs_sat_edit.findViewById(R.id.edittext_sat_name);
		final Button button_sat_direction = (Button)dvbs_sat_edit.findViewById(R.id.edit_direction); 
		final EditText edittext_angle0 = (EditText) dvbs_sat_edit.findViewById(R.id.angle0); 
		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		final EditText edittext_angle1 = (EditText) dvbs_sat_edit.findViewById(R.id.angle1);
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		final double direction = SatInfo.getSatLongitude();

		final TextView sat_no = (TextView)dvbs_sat_edit.findViewById(R.id.sat_number);
		sat_no.setText(String.valueOf(gobal_sat_cur_pos+1));
		
		edittext_satname.setText(SatInfo.getName());
		
		if(direction>0){
			button_sat_direction.setText("East");
		}
		else{
			button_sat_direction.setText("West");
		}
		button_sat_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(button_sat_direction.getText().equals("East")){
					button_sat_direction.setText("West");
				}
				else{
					button_sat_direction.setText("East");
				}
			}
		});
		
		edittext_angle0.setText(String.valueOf(Math.abs((int)SatInfo.getSatLongitude())/10));
		edittext_angle1.setText(String.valueOf((Math.abs(SatInfo.getSatLongitude()))%10));
		
		satEditBuilder.setView(dvbs_sat_edit);

		satEditBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				ContentValues values = new ContentValues();
				values.put("sat_name", edittext_satname.getText().toString());

				DbSat sat_node = getSatInfoByPostion(gobal_sat_cur_pos);
				sat_node.setName(edittext_satname.getText().toString());
				int temp=0;
				if(button_sat_direction.getText().equals("East")){
					if(!(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals(""))){
						temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
						
						values.put("sat_longitude",temp);
					}
					else
						return;
				}else{	
					if(!(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals(""))){
						temp=0-(Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString()));
						values.put("sat_longitude",temp);
					}	
					else
						return;
				}

				int sat_id = sat_node.getSatId();

				if(getConflictSat(sat_node.getSatId(),temp)){
					
					Toast toast = Toast.makeText(
										DTVScanDvbsConfig.this, 
										"Satellite angle is already exist",
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
					return;				
				}
				editSatData(sat_id,values,"sat");	
				sat_node.setSatLongitude((double)temp);
				TextView sat_name = (TextView) findViewById(R.id.sat_name);
				sat_name.setTextColor(Color.YELLOW);
				sat_name.setText(edittext_satname.getText().toString());
	
				mySatAdapter.notifyDataSetChanged();
				dialog.dismiss();
				
                          }
                      });
               satEditBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = satEditBuilder.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
								
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							}         
							});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);


		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

	}

	*/
	
	Dialog mDialog = null;
	private void showSatEditDia(){
		if(ScanSatAndtsInfoList==null||ScanSatAndtsInfoList.size()==0)
			return;
		
		ContentValues values=null;

		mDialog = new Dialog(mContext,R.style.MyDialog){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		//mDialog.setCancelable(false);
		//mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_sat_edit_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		//mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.edit_title));

	    DbSat SatInfo  = getSatInfoByPostion(gobal_sat_cur_pos);
		
		final EditText edittext_satname = (EditText) window.findViewById(R.id.edittext_sat_name);

		final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(edittext_satname,0);
		final Button button_sat_direction = (Button)window.findViewById(R.id.edit_direction); 
		final EditText edittext_angle0 = (EditText) window.findViewById(R.id.angle0); 
		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		final EditText edittext_angle1 = (EditText) window.findViewById(R.id.angle1);
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		final double direction = SatInfo.getSatLongitude();

		final TextView sat_no = (TextView)window.findViewById(R.id.sat_number);
		sat_no.setText(String.valueOf(gobal_sat_cur_pos+1));
		
		edittext_satname.setText(SatInfo.getName());
		
		if(direction>0){
			button_sat_direction.setText("East");
		}
		else{
			button_sat_direction.setText("West");
		}
		button_sat_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(button_sat_direction.getText().equals("East")){
					button_sat_direction.setText("West");
				}
				else{
					button_sat_direction.setText("East");
					//imm.showSoftInput(edittext_satname, 0);
					//imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);  
				}
			}
		});
		
		edittext_angle0.setText(String.valueOf(Math.abs((int)SatInfo.getSatLongitude())/10));
		edittext_angle1.setText(String.valueOf((Math.abs(SatInfo.getSatLongitude()))%10));

		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
				 //onSetPositiveButton(cur_choise_index);
				 ContentValues values = new ContentValues();
				values.put("sat_name", edittext_satname.getText().toString());

				DbSat sat_node = getSatInfoByPostion(gobal_sat_cur_pos);
				sat_node.setName(edittext_satname.getText().toString());
				int temp=0;
				if(button_sat_direction.getText().equals("East")){
					if(!(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals(""))){
						temp=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
						
						values.put("sat_longitude",temp);
					}
					else
						return;
				}else{	
					if(!(edittext_angle0.getText().toString().equals("")||edittext_angle1.getText().toString().equals(""))){
						temp=0-(Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString()));
						values.put("sat_longitude",temp);
					}	
					else
						return;
				}

				int sat_id = sat_node.getSatId();

				if(getConflictSat(sat_node.getSatId(),temp)){
					
					Toast toast = Toast.makeText(
										DTVScanDvbsConfig.this, 
										"Satellite angle is already exist",
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
					return;				
				}
				editSatData(sat_id,values,"sat");	
				sat_node.setSatLongitude((double)temp);
				TextView sat_name = (TextView) findViewById(R.id.sat_name);
				sat_name.setTextColor(Color.YELLOW);
				sat_name.setText(edittext_satname.getText().toString());
	
				mySatAdapter.notifyDataSetChanged();
				
				 
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
	          }});
	
	}

	/*
	private void showSatAddDia(){

		ContentValues values=null;
		satEditBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_sat_edit = layoutInflater.inflate(R.layout.dvbs_sat_edit_dia, null); 
		satEditBuilder.setTitle(R.string.add_title);
		
		final EditText edittext_satname = (EditText) dvbs_sat_edit.findViewById(R.id.edittext_sat_name);
		final Button button_sat_direction = (Button)dvbs_sat_edit.findViewById(R.id.edit_direction); 
		final EditText edittext_angle0 = (EditText) dvbs_sat_edit.findViewById(R.id.angle0); 
		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		final EditText edittext_angle1 = (EditText) dvbs_sat_edit.findViewById(R.id.angle1);
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		
		final TextView sat_no = (TextView)dvbs_sat_edit.findViewById(R.id.sat_number);

		if(ScanSatAndtsInfoList!=null)
			sat_no.setText(String.valueOf(ScanSatAndtsInfoList.size()+1));
		else
			sat_no.setText(String.valueOf(1));
		
		edittext_satname.setText("");
		edittext_angle0.setText("");
		edittext_angle1.setText("");
		button_sat_direction.setText("East");
	
		button_sat_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(button_sat_direction.getText().equals("East")){
					button_sat_direction.setText("West");
				}
				else{
					button_sat_direction.setText("East");
				}
			}
		});
		
		
		
		satEditBuilder.setView(dvbs_sat_edit);

		satEditBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				int angle;
				
				if((edittext_angle0.getText().toString().equals(""))||(edittext_angle0.getText().toString().equals(null))||
						(edittext_angle1.getText().toString().equals(""))||(edittext_angle1.getText().toString().equals(null)))
					{
						dialog.dismiss();
						return;
					}
				
					if(button_sat_direction.getText().equals("East")){
						angle=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
					}else{	
						angle=0-(Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString()));
					}
					
					addSatData(edittext_satname.getText().toString(),(double)angle);

					mySatAdapter = new SatAdapter(DTVScanDvbsConfig.this,ScanSatAndtsInfoList);
					sat_list.setAdapter(mySatAdapter);
		
					mySatAdapter.notifyDataSetChanged();
					sat_list.setSelection(sat_list.getCount()-1);
					dialog.dismiss();
				}
                          
                      });
               satEditBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = satEditBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
									show_no_satellites_info();
								}         
								});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);

		/*
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/

	private void showSatAddDia(){
		ContentValues values=null;

		mDialog = new Dialog(mContext,R.style.MyDialog){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_sat_edit_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.add_title));


		final EditText edittext_satname = (EditText) window.findViewById(R.id.edittext_sat_name);
		final Button button_sat_direction = (Button)window.findViewById(R.id.edit_direction); 
		final EditText edittext_angle0 = (EditText) window.findViewById(R.id.angle0); 
		edittext_angle0.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(3)});
		final EditText edittext_angle1 = (EditText) window.findViewById(R.id.angle1);
		edittext_angle1.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(1)});
		
		final TextView sat_no = (TextView)window.findViewById(R.id.sat_number);

		if(ScanSatAndtsInfoList!=null)
			sat_no.setText(String.valueOf(ScanSatAndtsInfoList.size()+1));
		else
			sat_no.setText(String.valueOf(1));
		
		edittext_satname.setText("");
		edittext_angle0.setText("");
		edittext_angle1.setText("");
		button_sat_direction.setText("East");
	
		button_sat_direction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(button_sat_direction.getText().equals("East")){
					button_sat_direction.setText("West");
				}
				else{
					button_sat_direction.setText("East");
				}
			}
		});

		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
				int angle;
				
				if((edittext_angle0.getText().toString().equals(""))||(edittext_angle0.getText().toString().equals(null))||
					(edittext_angle1.getText().toString().equals(""))||(edittext_angle1.getText().toString().equals(null)))
					{
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						return;
					}
				
					if(button_sat_direction.getText().equals("East")){
						angle=Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString());
					}else{	
						angle=0-(Integer.parseInt(edittext_angle0.getText().toString())*10+Integer.parseInt(edittext_angle1.getText().toString()));
					}
					
					addSatData(edittext_satname.getText().toString(),(double)angle);

					mySatAdapter = new SatAdapter(DTVScanDvbsConfig.this,ScanSatAndtsInfoList);
					sat_list.setAdapter(mySatAdapter);
		
					mySatAdapter.notifyDataSetChanged();
					sat_list.setSelection(sat_list.getCount()-1);

					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							show_no_satellites_info();
						}         
						});	
			
	}	
		
	
	private AlertDialog.Builder tsEditBuilder;
	private View dvbs_ts_edit;
	/*
	private void showTsEditDia(){

		if(tsInfoList==null||tsInfoList.size()==0){
			return;
		}
		
		ContentValues values=null;
		tsEditBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_ts_edit = layoutInflater.inflate(R.layout.dvbs_ts_edit_dia, null); 
		tsEditBuilder.setTitle(R.string.edit_title);

		final DbTransponder TsInfo  = queryTsData(list_cur_pos);
		
		final EditText edittext_frequency= (EditText) dvbs_ts_edit.findViewById(R.id.edittext_frequency);
		final EditText edittext_symbol = (EditText) dvbs_ts_edit.findViewById(R.id.edittext_symbol);
		edittext_frequency.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});
		final TextView ts_number = (TextView) dvbs_ts_edit.findViewById(R.id.item_no);
		edittext_symbol.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});

		final Button polarization = (Button) dvbs_ts_edit.findViewById(R.id.polarization);

		if(TsInfo!=null){
			ts_number.setText(String.valueOf(list_cur_pos+1));
			edittext_frequency.setText(String.valueOf(TsInfo.getFrequency()/1000));
			edittext_symbol.setText(String.valueOf(TsInfo.getSymbol()/1000));

			if(TsInfo.getPolarization()==1)
				polarization.setText("V");
			else
				polarization.setText("H");
		}

		polarization.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(polarization.getText().equals("V")){
					polarization.setText("H");
				}
				else{
					polarization.setText("V");
				}
			}
		});
		
		tsEditBuilder.setView(dvbs_ts_edit);

		tsEditBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {

				ContentValues values = new ContentValues();
				if(edittext_frequency.getText().toString().equals("")==false){
					values.put("freq", Integer.parseInt(edittext_frequency.getText().toString())*1000);
				}	
				else{
					return;
				}	
				if(edittext_symbol.getText().toString().equals("")==false){
					values.put("symb", Integer.parseInt(edittext_symbol.getText().toString())*1000);
				}
				else{
					return ;
				}

				if(getConflictTp(TsInfo.getSatId(),(Integer.parseInt(edittext_frequency.getText().toString())*1000),(Integer.parseInt(edittext_symbol.getText().toString())*1000),(polarization.getText().toString().equals("H"))==true?0:1)){
					
					Toast toast = Toast.makeText(
										DTVScanDvbsConfig.this, 
										"Transponder is already exist",
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
									return;
				}

				TsInfo.setFrequency(Integer.parseInt(edittext_frequency.getText().toString())*1000);
				TsInfo.setSymbol(Integer.parseInt(edittext_symbol.getText().toString())*1000);

				if(polarization.getText().toString().equals("H")){
					values.put("polar",0);
					TsInfo.setPolarization(0);
				}
				else{
					values.put("polar",1);
					TsInfo.setPolarization(1);
				}
				editTsData(gobal_sat_cur_pos,list_cur_pos,values);	
				myTsAdapter.notifyDataSetChanged();
				dialog.dismiss();
				
                          }
                      });
               tsEditBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = tsEditBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								}         
								});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);

		//alert.getWindow().setLayout(displayMetrics.widthPixels / 2, /*displayMetrics.heightPixels * 2 / 3*///-1);
		/*
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

	}
	*/

	private void showTsEditDia(){
		if(tsInfoList==null||tsInfoList.size()==0){
			return;
		}
		
		
		ContentValues values=null;

		
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		//mDialog.setCancelable(false);
		//mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_ts_edit_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.edit_title));

	    final DbTransponder TsInfo  = queryTsData(list_cur_pos);
		
		final EditText edittext_frequency= (EditText) window.findViewById(R.id.edittext_frequency);
		final EditText edittext_symbol = (EditText) window.findViewById(R.id.edittext_symbol);
		edittext_frequency.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});
		final TextView ts_number = (TextView) window.findViewById(R.id.item_no);
		edittext_symbol.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});

		final Button polarization = (Button) window.findViewById(R.id.polarization);

		if(TsInfo!=null){
			ts_number.setText(String.valueOf(list_cur_pos+1));
			edittext_frequency.setText(String.valueOf(TsInfo.getFrequency()/1000));
			edittext_symbol.setText(String.valueOf(TsInfo.getSymbol()/1000));

			if(TsInfo.getPolarization()==1)
				polarization.setText("V");
			else
				polarization.setText("H");
		}

		polarization.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(polarization.getText().equals("V")){
					polarization.setText("H");
				}
				else{
					polarization.setText("V");
				}
			}
		});

		
		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
				 //onSetPositiveButton(cur_choise_index);
				ContentValues values = new ContentValues();
				if(edittext_frequency.getText().toString().equals("")==false){
					values.put("freq", Integer.parseInt(edittext_frequency.getText().toString())*1000);
				}	
				else{
					return;
				}	
				if(edittext_symbol.getText().toString().equals("")==false){
					values.put("symb", Integer.parseInt(edittext_symbol.getText().toString())*1000);
				}
				else{
					return ;
				}

				if(getConflictTp(TsInfo.getSatId(),(Integer.parseInt(edittext_frequency.getText().toString())*1000),(Integer.parseInt(edittext_symbol.getText().toString())*1000),(polarization.getText().toString().equals("H"))==true?0:1)){
					
					Toast toast = Toast.makeText(
										DTVScanDvbsConfig.this, 
										"Transponder is already exist",
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
									return;
				}

				TsInfo.setFrequency(Integer.parseInt(edittext_frequency.getText().toString())*1000);
				TsInfo.setSymbol(Integer.parseInt(edittext_symbol.getText().toString())*1000);

				if(polarization.getText().toString().equals("H")){
					values.put("polar",0);
					TsInfo.setPolarization(0);
				}
				else{
					values.put("polar",1);
					TsInfo.setPolarization(1);
				}
				editTsData(gobal_sat_cur_pos,list_cur_pos,values);	
				myTsAdapter.notifyDataSetChanged();
				 
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
	          }});
			  
	}	


	private Handler timer_ts_signal_info_handler = new Handler();   

	private Runnable timer_ts_signal_info_runnable = new Runnable() {

		public void run() {

			timer_ts_signal_info_handler.postDelayed(timer_ts_signal_info_runnable,500);  
		}   
	};

	class DVBFrontendSignalInfo{
		int ber = 0;
		int snr = 0;
		int strength = 0;
		boolean lock_status = false;
	}

	private void updataSignalInfo(DVBFrontendSignalInfo info ){
		
		int ber = info.ber;
		int snr =  info.snr;
		int strength =  info.strength;
		boolean lock_status = info.lock_status;

		if(mDialog!=null){
			Window window = mDialog.getWindow();
			final CheckBox checkboxStatus = (CheckBox)window.findViewById(R.id.checkStatus);
			
			final ProgressBar ProgressBarSNR = (ProgressBar)window.findViewById(R.id.ProgressBarSNR);
	    		ProgressBarSNR.setMax(100);
	    		
			final ProgressBar ProgressBarAGC = (ProgressBar)window.findViewById(R.id.ProgressBarAGC);
	    		ProgressBarAGC.setMax(100);

			//final ProgressBar ProgressBarBER = (ProgressBar)dvbs_set_limit_list.findViewById(R.id.ProgressBarBER);
	    		//ProgressBarBER.setMax(100);

			final TextView snr_value = (TextView) window.findViewById(R.id.snr_value);
			final TextView agc_value = (TextView) window.findViewById(R.id.agc_value);
			//final TextView ber_value = (TextView) window.findViewById(R.id.ber_value);	

			if(strength>100)
				strength=0;
			
			ProgressBarSNR.setProgress(strength);
			ProgressBarAGC.setProgress(snr);
			//ProgressBarBER.setProgress(info.getBER());

			snr_value.setText(Integer.toString((strength>100)?100:strength));
			agc_value.setText(Integer.toString((snr>100)?100:snr));
			//ber_value.setText(Integer.toString((info.getBER()>100)?100:info.getBER()));

			//if(info.getLockStat()==32)
			
			if(lock_status==false)
				checkboxStatus.setChecked(false);
			else
				checkboxStatus.setChecked(true);
		}
		
	}


	private AlertDialog ts_signal_info_alert; 
	private void showTransponderSignalInfoDialog(){

		
		if(tsInfoList==null||tsInfoList.size()==0){
			return;
		}

		ContentValues values=null;
		tsEditBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_ts_edit = layoutInflater.inflate(R.layout.dvbs_ts_info_dia, null); 
		tsEditBuilder.setTitle(R.string.sat_signal_info);

		final DbTransponder TsInfo  = queryTsData(list_cur_pos);
		
		final TextView edittext_frequency= (TextView) dvbs_ts_edit.findViewById(R.id.edittext_frequency);
		final TextView edittext_symbol = (TextView) dvbs_ts_edit.findViewById(R.id.edittext_symbol);
		final TextView ts_number = (TextView) dvbs_ts_edit.findViewById(R.id.item_no);
		final TextView polarization = (TextView) dvbs_ts_edit.findViewById(R.id.polarization);

    		final ProgressBar ProgressBarSNR = (ProgressBar)dvbs_ts_edit.findViewById(R.id.ProgressBarSNR);
    		ProgressBarSNR.setMax(100);
    		
		final ProgressBar ProgressBarAGC = (ProgressBar)dvbs_ts_edit.findViewById(R.id.ProgressBarAGC);
    		ProgressBarAGC.setMax(100);

		//final ProgressBar ProgressBarBER = (ProgressBar)dvbs_ts_edit.findViewById(R.id.ProgressBarBER);
    		//ProgressBarBER.setMax(100);

		final TextView snr_value = (TextView) dvbs_ts_edit.findViewById(R.id.snr_value);
		final TextView agc_value = (TextView) dvbs_ts_edit.findViewById(R.id.agc_value);
		//final TextView ber_value = (TextView) dvbs_ts_edit.findViewById(R.id.ber_value);	

		if(TsInfo!=null){
			ts_number.setText(String.valueOf(list_cur_pos+1));
			edittext_frequency.setText(String.valueOf(TsInfo.getFrequency()));
			edittext_symbol.setText(String.valueOf(TsInfo.getSymbol()));

			if(TsInfo.getPolarization()==1)
				polarization.setText("V");
			else
				polarization.setText("H");

			snr_value.setText("0%");
			agc_value.setText("0%");
			//ber_value.setText("0%");
			
		}
				
		tsEditBuilder.setView(dvbs_ts_edit);

		ts_signal_info_alert = tsEditBuilder.create();

		ts_signal_info_alert.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case  KeyEvent.KEYCODE_BACK:
						dialog.cancel();
						return true;
				}
				return false;
			}
		});

		 ts_signal_info_alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		ts_signal_info_alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								timer_ts_signal_info_handler.removeCallbacks(timer_ts_signal_info_runnable);  	
								}         
								});	

		ts_signal_info_alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		//ts_signal_info_alert.getWindow().setLayout(displayMetrics.widthPixels / 2,-1);

		WindowManager.LayoutParams lp=ts_signal_info_alert.getWindow().getAttributes();
		lp.dimAmount=0.00f;
		ts_signal_info_alert.getWindow().setAttributes(lp);
		ts_signal_info_alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		timer_ts_signal_info_handler.postDelayed(timer_ts_signal_info_runnable, 500);
		
	}


	/*
	private void showTsAddDia(){
		ContentValues values=null;
		tsEditBuilder = new AlertDialog.Builder(this);
	        LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_ts_edit = layoutInflater.inflate(R.layout.dvbs_ts_edit_dia, null); 
		tsEditBuilder.setTitle(R.string.add_title);

		//DbTransponder TsInfo  = queryTsData(list_cur_pos);
		
		final EditText edittext_frequency= (EditText) dvbs_ts_edit.findViewById(R.id.edittext_frequency);
		final EditText edittext_symbol = (EditText) dvbs_ts_edit.findViewById(R.id.edittext_symbol);
		edittext_frequency.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});
		final TextView ts_number = (TextView) dvbs_ts_edit.findViewById(R.id.item_no);
		edittext_symbol.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});

		final Button polarization = (Button) dvbs_ts_edit.findViewById(R.id.polarization);

		if(tsInfoList==null)
			ts_number.setText(String.valueOf(1));	
		else	
			ts_number.setText(String.valueOf(tsInfoList.size()+1));
		edittext_frequency.setText("");
		edittext_symbol.setText("");
		polarization.setText("V");
			
		polarization.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(polarization.getText().equals("V")){
					polarization.setText("H");
				}
				else{
					polarization.setText("V");
				}
			}
		});

		tsEditBuilder.setView(dvbs_ts_edit);

		tsEditBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {

				DbTransponder TsInfo  = new DbTransponder();
				ContentValues values = new ContentValues();

				int freq = 0, symb = 0, polar;
				if(edittext_frequency.getText().toString().equals("")==false){
					freq = Integer.parseInt(edittext_frequency.getText().toString())*1000;
					values.put("freq", freq);
					TsInfo.setFrequency(freq);
				}	
				if(edittext_symbol.getText().toString().equals("")==false){
					symb = Integer.parseInt(edittext_symbol.getText().toString())*1000;
					values.put("symb", symb);
					TsInfo.setSymbol(symb);
				}
				
				if(polarization.getText().toString().equals("H")){
					values.put("polar",0);
					TsInfo.setPolarization(0);
					polar = 0;
				}
				else{
					values.put("polar",1);
					TsInfo.setPolarization(1);
					polar = 1;
				}
				
				if (freq <= 0 || symb <= 0) {
					Toast toast = Toast.makeText(
								DTVScanDvbsConfig.this, 
								"Invalid value",
								Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
					return;			
				}
				DbSat sat_node = getSatInfoByPostion(gobal_sat_cur_pos);
				int sat_id = sat_node.getSatId();
				if(getConflictTp(sat_id,TsInfo.getFrequency(),TsInfo.getSymbol(),TsInfo.getPolarization())){	
					Toast toast = Toast.makeText(
										DTVScanDvbsConfig.this, 
										"Transponder angle is already exist",
										Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
						return;			
				}else{
					TsInfo.setSatId(sat_id);
					addTsData(gobal_sat_cur_pos,values,TsInfo);
					//TsInfo.setDbId(getTsDbId(sat_id, freq, symb, polar));
					getTsData(gobal_sat_cur_pos);
					myTsAdapter = new TsAdapter(DTVScanDvbsConfig.this,tsInfoList);
					sat_list.setAdapter(myTsAdapter);
					myTsAdapter.notifyDataSetChanged();
					sat_list.setSelection(sat_list.getCount()-1);
					show_no_satellites_info();
					dialog.dismiss();
				}
                          }
                      });
               tsEditBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
                          }
                      });

		AlertDialog alert = tsEditBuilder.create();

		 alert.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									
									}         
									}); 	

		   alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								}         
								});	

		alert.show();	
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		System.out.println("H = " + displayMetrics.heightPixels);
		System.out.println("W = " + displayMetrics.widthPixels);

		//alert.getWindow().setLayout(displayMetrics.widthPixels / 2, displayMetrics.heightPixels * 2 / 3);

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}
	*/
	
	private void showTsAddDia(){
		ContentValues values=null;

		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(mDialog!=null&& mDialog.isShowing()){
							mDialog.dismiss();
						}
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.dvbs_ts_edit_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.5f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.add_title));


		final EditText edittext_frequency= (EditText) window.findViewById(R.id.edittext_frequency);
		final EditText edittext_symbol = (EditText) window.findViewById(R.id.edittext_symbol);
		edittext_frequency.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});
		final TextView ts_number = (TextView) window.findViewById(R.id.item_no);
		edittext_symbol.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(5)});

		final Button polarization = (Button) window.findViewById(R.id.polarization);

		if(tsInfoList==null)
			ts_number.setText(String.valueOf(1));	
		else	
			ts_number.setText(String.valueOf(tsInfoList.size()+1));
		edittext_frequency.setText("");
		edittext_symbol.setText("");
		polarization.setText("V");
			
		polarization.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
			// TODO Auto-generated method stub

				if(polarization.getText().equals("V")){
					polarization.setText("H");
				}
				else{
					polarization.setText("V");
				}
			}
		});

		no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 //onSetNegativeButton();
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
	          public void onClick(View v) {
					DbTransponder TsInfo  = new DbTransponder();
					ContentValues values = new ContentValues();

					int freq = 0, symb = 0, polar;
					if(edittext_frequency.getText().toString().equals("")==false){
						freq = Integer.parseInt(edittext_frequency.getText().toString())*1000;
						values.put("freq", freq);
						TsInfo.setFrequency(freq);
					}	
					if(edittext_symbol.getText().toString().equals("")==false){
						symb = Integer.parseInt(edittext_symbol.getText().toString())*1000;
						values.put("symb", symb);
						TsInfo.setSymbol(symb);
					}
					
					if(polarization.getText().toString().equals("H")){
						values.put("polar",0);
						TsInfo.setPolarization(0);
						polar = 0;
					}
					else{
						values.put("polar",1);
						TsInfo.setPolarization(1);
						polar = 1;
					}
					
					if (freq <= 0 || symb <= 0) {
						Toast toast = Toast.makeText(
									DTVScanDvbsConfig.this, 
									"Invalid value",
									Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
						return;			
					}
					DbSat sat_node = getSatInfoByPostion(gobal_sat_cur_pos);
					int sat_id = sat_node.getSatId();
					if(getConflictTp(sat_id,TsInfo.getFrequency(),TsInfo.getSymbol(),TsInfo.getPolarization())){	
						Toast toast = Toast.makeText(
											DTVScanDvbsConfig.this, 
											"Transponder angle is already exist",
											Toast.LENGTH_SHORT);
										toast.setGravity(Gravity.CENTER, 0, 0);
										toast.show();
							return;			
					}else{
						TsInfo.setSatId(sat_id);
						addTsData(gobal_sat_cur_pos,values,TsInfo);
						//TsInfo.setDbId(getTsDbId(sat_id, freq, symb, polar));
						getTsData(gobal_sat_cur_pos);
						myTsAdapter = new TsAdapter(DTVScanDvbsConfig.this,tsInfoList);
						sat_list.setAdapter(myTsAdapter);
						myTsAdapter.notifyDataSetChanged();
						sat_list.setSelection(sat_list.getCount()-1);
						show_no_satellites_info();
					}
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
			}});
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							show_no_satellites_info();
						}         
						});	
	
	}
	




	private void refresh_sat_data(int pos){
		//satInfoList.remove(pos);
		mySatAdapter.notifyDataSetChanged();
	}


	private List<DbTransponder> getTsData(int cur_pos){
		DbSat satInfoNode =  getSatInfoByPostion(cur_pos);
		
		if(satInfoNode!= null){
			tsInfoList = getScanTsList(satInfoNode);
		}

		return tsInfoList;
	}

	/*
	private int satellite_id=0;
	private int transponder_id=0;
 	private List<DbSat> readDefaultXML() {
		InputStream inputStream=null;

		List<DbSat> DbSat = new ArrayList<DbSat>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			inputStream = this.getResources().getAssets().open("satellites.xml");
			
			Document dom = builder.parse(inputStream);

			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName("sat");
			Log.d(TAG,"satellites  "+items.getLength());
			for (int i = 0; i < items.getLength(); i++) {
				satellite_id = i;
				DbSat DbSat_node = new DbSat();
				DbSat_node.setId(satellite_id);
				Element element_node = (Element) items.item(i);
				 //Log.d("name=",element_node.getAttribute("name"));
				 DbSat_node.setName(element_node.getAttribute("name"));
				 
				 //Log.d("flags=",element_node.getAttribute("flags"));
				 if(element_node.getAttribute("flags").equals("")==false)
				  	DbSat_node.setFlags(Integer.parseInt(element_node.getAttribute("flags")));
				  
				// Log.d("position=",element_node.getAttribute("position"));
				if(element_node.getAttribute("position").equals("")==false)
				  DbSat_node.setPosition(Integer.parseInt(element_node.getAttribute("position")));


				//NodeList childsNodes = element_node.getChildNodes();
				NodeList childsNodes = element_node.getElementsByTagName("transponder");
				Log.d(TAG,"childsNodes getLength==================="+childsNodes.getLength());
				List<DbTransponder> DbTransponder = new ArrayList<DbTransponder>();
				for (int j = 0; j < childsNodes.getLength(); j++) {
					transponder_id = j;
					
					DbTransponder DbTransponder_node = new DbTransponder();
					DbTransponder_node.setId(transponder_id);
					DbTransponder_node.setSatId(satellite_id);
					
					Node node = (Node) childsNodes.item(j); 

					if(node.getNodeType() == Node.ELEMENT_NODE){   
					
						Element childNode = (Element) node;
						//Log.d(TAG,"childNode.getNodeName()"+childNode.getNodeName());								

						if ("transponder".equals(childNode.getNodeName())) {
							 //Log.d("frequency=",childNode.getAttribute("frequency"));
							 if(childNode.getAttribute("frequency").equals("")==false)
							 DbTransponder_node.setFrequency(Integer.parseInt(childNode.getAttribute("frequency")));
							 
							 //Log.d("symbol_rate=",childNode.getAttribute("symbol_rate"));
							  if(childNode.getAttribute("symbol_rate").equals("")==false)
							  DbTransponder_node.setSymbol(Integer.parseInt(childNode.getAttribute("symbol_rate")));
							  
							 //Log.d("polarization=",childNode.getAttribute("polarization"));
							 if(childNode.getAttribute("polarization").equals("")==false)
							  DbTransponder_node.setPolarization(Integer.parseInt(childNode.getAttribute("polarization")));
							 
							// Log.d("fec_inner",childNode.getAttribute("fec_inner"));
							if(childNode.getAttribute("fec_inner").equals("")==false)
							 DbTransponder_node.setFecInner(Integer.parseInt(childNode.getAttribute("fec_inner")));
							 
							 //Log.d(TAG,"---------------------------");
						}
						
					}
					DbTransponder.add(DbTransponder_node);
				}
				DbSat_node.transponder = DbTransponder;
				
				DbSat.add(DbSat_node);
				}

				inputStream.close();
			} catch (Exception e) {
			e.printStackTrace();
		}
				
		return DbSat;
	}
	*/

	/*
	void addSatInfoToDB( List<DbSat> value){
				
		Log.d(TAG,"addSatInfoToDB==="+value.size());
			
		if(value.size()==0)
			return;

		SQLiteDatabase db = mLockDvbsHelper.getWritableDatabase();
		db.beginTransaction();	
		
		db.delete(mLockDvbsHelper.DB_TABLE_TS_INFO, null, null);	
		db.delete(mLockDvbsHelper.DB_TABLE_SAT_INFO, null, null);	
		
		for(int i=0;i<value.size();i++){
			int id = value.get(i).getId();
			Log.d(TAG,"ID==="+id);

			Log.d(TAG,"****"+value.get(i).getName());				
			Log.d(TAG,"****"+value.get(i).getFlags());
			Log.d(TAG,"****"+value.get(i).getPosition());

			ContentValues values_sat = new ContentValues();
			values_sat.put("sat_id", value.get(i).getId());
			values_sat.put("name", value.get(i).getName());
			values_sat.put("flags", value.get(i).getFlags());
			values_sat.put("position", value.get(i).getPosition());
			db.insert(mLockDvbsHelper.DB_TABLE_SAT_INFO, null, values_sat);

			for(int j=0;j<value.get(i).transponder.size();j++){
				Log.d(TAG,"TS ID==="+value.get(i).transponder.get(j).getId());
				Log.d(TAG,"TS Sat ID="+value.get(i).transponder.get(j).getSatId());
				Log.d(TAG,"TS FRE="+value.get(i).transponder.get(j).getFrequency());
				Log.d(TAG,"TS SYM="+value.get(i).transponder.get(j).getSymbol());
				Log.d(TAG,"TS Porlarization="+value.get(i).transponder.get(j).getPolarization());
				Log.d(TAG,"TS getFecInner="+value.get(i).transponder.get(j).getFecInner());


				ContentValues values_ts = new ContentValues();
				values_ts.put("ts_id", value.get(i).transponder.get(j).getId());
				values_ts.put("sat_id", value.get(i).getId());
				values_ts.put("frequency", value.get(i).transponder.get(j).getFrequency());
				values_ts.put("symbol", value.get(i).transponder.get(j).getSymbol());
				values_ts.put("polarization", value.get(i).transponder.get(j).getPolarization());
				values_ts.put("fec_inner", value.get(i).transponder.get(j).getFecInner());
				//getMyDBHelper().insertTsInfo(values_ts);	
				db.insert(mLockDvbsHelper.DB_TABLE_TS_INFO, null, values_ts);

			}
			
		}

		db.setTransactionSuccessful(); 
		db.endTransaction();
		db.close();
		
	  }
	  */
	public DbSat getSatInfoByPostion(int position){
		if(ScanSatAndtsInfoList == null)
			return null;
		if (position >= ScanSatAndtsInfoList.size())
	    	{
	    		return null;
	    	}
	    	
	    	return (DbSat) ScanSatAndtsInfoList.get(position);
	    }

	 class SatAdapter extends BaseAdapter{
    		private LayoutInflater mInflater;
    		private Context cont;
    		private List<DbSat> listItems;
    		private int selectItem;
    		
    		class ViewHolder {
				ImageView icon;
				TextView     sat_id;
				TextView     direction;
    			TextView     position;
				TextView     name;		      		
				TextView 	   band;
				
    			}
    		
    		public SatAdapter(Context context, List<DbSat> list) {
    			super();
    			cont = context;
    			listItems = list;
    			mInflater=LayoutInflater.from(context);			  
    		}

    		public int getCount() {
    			if(listItems==null)
				return 0;
			else
				return listItems.size();
    		}

    		public Object getItem(int position) {
    		
    			return position;
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
    		
		private void fillData(ViewHolder mv, int position){
			if (null != mv){
				DbSat satinfo = getSatInfoByPostion(position);
				if (null != satinfo)
				{					  									   
					mv.name.setText(satinfo.getName()); 
					//mv.sat_id.setText(String.valueOf(satinfo.getSatNo()+1));  
					mv.sat_id.setText(String.valueOf(position+1));
					if(satinfo.getSatLongitude()>0){
						mv.direction.setText("East");
					}
					else{
						mv.direction.setText("West");
					}
					float angle_float = Math.abs((float)satinfo.getSatLongitude());	
					//Log.d(TAG,"angle"+angle);
					
					angle_float=angle_float/10;
					//Log.d(TAG,"angle"+angle_float);
					mv.position.setText(String.valueOf(angle_float));  
					if(satinfo.getSelectedFlag()==true)
						mv.icon.setBackgroundResource(R.drawable.selected);
					else 
						mv.icon.setBackgroundResource(0);

					if(sat_list_focus_flag){
							mv.sat_id.setTextColor(Color.WHITE);
							mv.name.setTextColor(Color.WHITE);
							mv.position.setTextColor(Color.WHITE);
							mv.direction.setTextColor(Color.WHITE);
						}	
						else{
							if(position==gobal_sat_cur_pos){
								Log.d(TAG,"position="+position+"###gobal_sat_cur_pos"+gobal_sat_cur_pos);	

								mv.sat_id.setTextColor(Color.YELLOW);
								mv.name.setTextColor(Color.YELLOW);
								mv.position.setTextColor(Color.YELLOW);
								mv.direction.setTextColor(Color.YELLOW);
							}
							else{
								mv.sat_id.setTextColor(Color.WHITE);
								mv.name.setTextColor(Color.WHITE);
								mv.position.setTextColor(Color.WHITE);
								mv.direction.setTextColor(Color.WHITE);
							}
						}
				}
			}
		}
				
			
    		public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;	
    		if (convertView == null) { 
    			 convertView = mInflater.inflate(R.layout.list_dvbs_sat, null);
			holder = new ViewHolder();

			holder.sat_id = (TextView) convertView.findViewById(R.id.sat_id);
			holder.name= (TextView) convertView.findViewById(R.id.name);
			holder.direction = (TextView)convertView.findViewById(R.id.direction);
			holder.position=(TextView) convertView.findViewById(R.id.position);		      		
			//holder.band = (TextView) convertView.findViewById(R.id.band);	
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
    		}else {
    		  // Get the ViewHolder back to get fast access to the TextView
    		  // and the ImageView.
    		  holder = (ViewHolder) convertView.getTag();
    		  }
			
    		  // Bind the data efficiently with the holder.
			  
    		  fillData(holder, position);
    		  
    		  return convertView;
    		}
    	  }

	 public DbTransponder getTsInfoByPostion(int position)
	    {
	        if(tsInfoList==null)
			return null;	

		if (position >= tsInfoList.size())
	    	{
			Log.d(TAG,"TsInfo is null!");
			return null;
	    	}
	    	
	    	return (DbTransponder) tsInfoList.get(position);
	    }

	class TsAdapter extends BaseAdapter{
    		private LayoutInflater mInflater;
  
    		private Context cont;
    		private List<DbTransponder> listItems=null;
    		private int selectItem;
			
    		class ViewHolder {
				ImageView icon;
				TextView     ts_no;
				TextView     frequency;
    				TextView     polarity;
				TextView     symbol;		      	
				
    			}
    		
    		public TsAdapter(Context context, List<DbTransponder> list) {
    			super();
    			cont = context;
    			listItems = list;
    			mInflater=LayoutInflater.from(context);			  
    		}

    		public int getCount() {
  
    			if(listItems==null){
				Log.d(TAG,"TS INFO IS NULL");
				return 0;
    			}	
			else
				return listItems.size();
    		}

    		public Object getItem(int position) {
    		
    			return position;
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
    		
		private void fillData(ViewHolder mv, int position){
			if (null != mv){
				DbTransponder tsinfo = getTsInfoByPostion(position);
				if (null != tsinfo)
				{			
					//mv.ts_no.setText(String.valueOf(tsinfo.getTsNo()+1));
					mv.ts_no.setText(String.valueOf(position+1));
					mv.frequency.setText(String.valueOf(tsinfo.getFrequency()/1000)+"  Mhz");
					if(tsinfo.getPolarization()==1)
						mv.polarity.setText("V");  
					else
						mv.polarity.setText("H");  
					mv.symbol.setText(String.valueOf(tsinfo.getSymbol()/1000)+"  KS/s");  

					if(tsinfo.getSelectedFlag()==true)
						mv.icon.setBackgroundResource(R.drawable.selected);
					else 
						mv.icon.setBackgroundResource(0);

					
						if(sat_list_focus_flag){
							mv.ts_no.setTextColor(Color.WHITE);
							mv.frequency.setTextColor(Color.WHITE);
							mv.polarity.setTextColor(Color.WHITE);
							mv.symbol.setTextColor(Color.WHITE);
						}	
						else{
							if(position==list_cur_pos){
								mv.ts_no.setTextColor(Color.YELLOW);
								mv.frequency.setTextColor(Color.YELLOW);
								mv.polarity.setTextColor(Color.YELLOW);
								mv.symbol.setTextColor(Color.YELLOW);
							}	
						}
					
				}
			}
		}
				
			
    		public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;	
    		if (convertView == null) { 
    			 convertView = mInflater.inflate(R.layout.list_dvbs_ts, null);
			holder = new ViewHolder();
			holder.ts_no = (TextView) convertView.findViewById(R.id.ts_no);
			holder.frequency= (TextView) convertView.findViewById(R.id.frequency);
			holder.polarity=(TextView) convertView.findViewById(R.id.polarity);		      		
			holder.symbol = (TextView) convertView.findViewById(R.id.symbol);	
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
    		}else {
    		  // Get the ViewHolder back to get fast access to the TextView
    		  // and the ImageView.
    		  holder = (ViewHolder) convertView.getTag();
    		  }
			
    		  // Bind the data efficiently with the holder.
    		  fillData(holder, position);
    		  
    		  return convertView;
    		}
    	  }


	//public static final int DISHSETUP_LNB_NO = 0;
	public static final int DISHSETUP_LNB_TYPE = 0;
	
	public static final int DISHSETUP_LNB_POWER = 1;
	public static final int DISHSETUP_LNB_22K = 2;
	public static final int DISHSETUP_LNB_TONEBURST = 3;
	public static final int DISHSETUP_DISEQC1_0 = 4;
	public static final int DISHSETUP_DISEQC1_1 = 5;
	public static final int DISHSETUP_DISEQC_MOTOR = 6;
	
	/*
	public static final int DISHSETUP_LNB_DISEQC = 4;
	//public static final int DISHSETUP_LNB_DISEQC_CONFIG = 5;
	public static final int DISHSETUP_LNB_COMMITTED_COMMAND = 5;
	public static final int DISHSETUP_LNB_DISEQC_REPEATS = 6;
	public static final int DISHSETUP_LNB_DISEQC_SEQUENCE = 8;
	public static final int DISHSETUP_LNB_UNCOMMITTED_COMMAND = 7;
	public static final int DISHSETUP_LNB_FAST_DISEQC = 9;
	//public static final int DISHSETUP_LNB_MOTO = 10;
	public static final int DISHSETUP_LNB_MOTO_NO = 10;
	*/

	
	private  class LnbSetAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon0;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		private Bitmap mIcon4;
		private Bitmap mIcon5;
		private Bitmap mIcon6;
		private Bitmap mIcon7;
		private Bitmap mIcon8;
		private Bitmap mIcon9;
		private Bitmap mIcon10;
		private Bitmap mIcon11;
		private Bitmap mIcon12;
		
		private Context cont;
		private String[] listItems;
		private List<DbSat> lnb_list;
		private DbSat SatLnbInfo;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
			TextView   info; 
			ImageButton  iboolean;
			ImageView icon1;
		}
	
		public LnbSetAdapter(Context context) {
			super();
			cont = context;

			listItems = new String[7];

			//listItems[DISHSETUP_LNB_NO]= cont.getResources().getString(R.string.dish_setup_lnb_no);
			listItems[DISHSETUP_LNB_TYPE]= cont.getResources().getString(R.string.dish_setup_lnb_type);
			listItems[DISHSETUP_LNB_POWER]= cont.getResources().getString(R.string.dish_setup_lnb_power);
			listItems[DISHSETUP_LNB_22K]= cont.getResources().getString(R.string.dish_setup_22K);
			listItems[DISHSETUP_LNB_TONEBURST]= cont.getResources().getString(R.string.dish_setup_lnb_toneburst);
			/*
			listItems[DISHSETUP_LNB_DISEQC]= cont.getResources().getString(R.string.dish_setup_diseqc);
			//listItems[5]= cont.getResources().getString(R.string.dish_setup_conf_lnb);
			
			listItems[DISHSETUP_LNB_COMMITTED_COMMAND]= cont.getResources().getString(R.string.dish_setup_committed_command);
			listItems[DISHSETUP_LNB_DISEQC_REPEATS]= cont.getResources().getString(R.string.dsh_setup_diseqc_repeat);
			listItems[DISHSETUP_LNB_DISEQC_SEQUENCE]= cont.getResources().getString(R.string.dish_setup_diseqc_sequence);
			listItems[DISHSETUP_LNB_UNCOMMITTED_COMMAND]= cont.getResources().getString(R.string.dish_setup_uncommitted_command);
			listItems[DISHSETUP_LNB_FAST_DISEQC]= cont.getResources().getString(R.string.dish_setup_fast_diseqc);
			//listItems[DISHSETUP_LNB_MOTO]= cont.getResources().getString(R.string.dish_setup_moto);
			listItems[DISHSETUP_LNB_MOTO_NO]= cont.getResources().getString(R.string.dish_setup_moto_number);
			*/
			listItems[DISHSETUP_DISEQC1_0]= cont.getResources().getString(R.string.dish_setup_diseqc0);
			//listItems[5]= cont.getResources().getString(R.string.dish_setup_conf_lnb);
			
			listItems[DISHSETUP_DISEQC1_1]= cont.getResources().getString(R.string.dish_setup_diseqc1);
			listItems[DISHSETUP_DISEQC_MOTOR]= "Motor Mode";

			mInflater=LayoutInflater.from(context);
			/*
			mIcon0 =  BitmapFactory.decodeResource(context.getResources(), R.drawable.set_channel_search);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_subtitle);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_audio_track);
			mIcon4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_channel_search);
			mIcon5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_factory_data_reset);
			 mIcon6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_password);

			
			mIcon7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_subtitle);
			mIcon9 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_picture_size);
			mIcon8 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_audio_track);
			mIcon10 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_channel_search);
			mIcon11 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_factory_data_reset);
			*/
			//mIcon12 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_password);

		}

		public int getCount() {

			return listItems.length;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			if (isUnicableOn()==true)
			{
				if ((position>=DISHSETUP_DISEQC1_0)&&(position<=DISHSETUP_DISEQC_MOTOR)) {
					return false;
				}
			}	

			DbSat SatLnbInfo=getSatInfoByPostion(gobal_sat_cur_pos);

			if((SatLnbInfo==null))
				return false;

			/*
			switch(position){
				case DISHSETUP_LNB_COMMITTED_COMMAND:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
							return false;
						}
					}
					break;
				case DISHSETUP_LNB_UNCOMMITTED_COMMAND:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
							return false;
						}
					}	
					break;
				case DISHSETUP_LNB_FAST_DISEQC:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
							return false;
						}
					}
					break;
				case DISHSETUP_LNB_DISEQC_REPEATS:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
							return false;
						}
					}	
					break;
				case DISHSETUP_LNB_DISEQC_SEQUENCE:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
							return false;
						}
					}
					break;
				case DISHSETUP_LNB_MOTO_NO:
					if(SatLnbInfo!=null){
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_11){
							return false;
						}
					}

			}
			*/
			return super.isEnabled(position);
		}
	
		public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;

		if (convertView == null) {
		   convertView = mInflater.inflate(R.layout.lnb_set_xml, null);
		   holder = new ViewHolder();
		   
		   holder.text = (TextView) convertView.findViewById(R.id.text);
		   holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		   holder.info = (TextView)convertView.findViewById(R.id.info);
		   holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
		   convertView.setTag(holder);
		}else {
		// Get the ViewHolder back to get fast access to the TextView
		// and the ImageView.
		holder = (ViewHolder) convertView.getTag();
		}

		//if(ListStatus==0)
		Log.d(TAG,"gobal_sat_cur_pos"+gobal_sat_cur_pos);
		 	SatLnbInfo=getSatInfoByPostion(gobal_sat_cur_pos);

		// Bind the data efficiently with the holder.
		holder.text.setText(listItems[position]);
		holder.info.setTextColor(Color.YELLOW);
		holder.text.setTextColor(Color.WHITE);

		if(SatLnbInfo== null||ScanSatAndtsInfoList.size()==0){	
			holder.icon.setVisibility(View.INVISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			holder.info.setVisibility(View.INVISIBLE);
			holder.text.setTextColor(Color.DKGRAY);
			return convertView;
		}	

		
	
		switch(position){
			case DISHSETUP_LNB_TYPE:
				holder.icon.setVisibility(View.VISIBLE);
				holder.info.setVisibility(View.VISIBLE);
				holder.info.setSingleLine(true);
				holder.info.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));
				holder.icon1.setVisibility(View.VISIBLE);
				holder.icon.setImageBitmap(mIcon1);
				holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 

				if (SatLnbInfo!=null){	
				   	Log.d(TAG,"DISHSETUP_LNB_TYPE low:"+SatLnbInfo.getLoLOF()+"high:"+SatLnbInfo.getHiLOF());

					if((SatLnbInfo.getLoLOF()==SatLnbInfo.getHiLOF())&&(SatLnbInfo.getLoLOF()!=0))			
		    		 		holder.info.setText(String.valueOf(SatLnbInfo.getLoLOF()/1000));
					 else
						holder.info.setText(String.valueOf(SatLnbInfo.getLoLOF()/1000)+"/"+String.valueOf(SatLnbInfo.getHiLOF()/1000));	 
				}
			 
				break;
			case DISHSETUP_LNB_POWER:
			 	 holder.icon.setVisibility(View.VISIBLE);
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.VISIBLE);
			    	 holder.icon.setImageBitmap(mIcon2);
			    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
				
				if(SatLnbInfo!=null){
					  Log.d(TAG,"DISHSETUP_LNB_POWER"+SatLnbInfo.getLNBPwrOnOff());
					 if(SatLnbInfo.getLNBPwrOnOff()==DbSat.LNB_POWER_OFF){
						holder.info.setText(R.string.dish_setup_conf_off);
					 }
					 else if(SatLnbInfo.getLNBPwrOnOff()==DbSat.LNB_POWER_13V){
						holder.info.setText("13V");
					 }
					 else if(SatLnbInfo.getLNBPwrOnOff()==DbSat.LNB_POWER_18V){
						holder.info.setText("18V");
					 }
					 else if(SatLnbInfo.getLNBPwrOnOff()==DbSat.LNB_POWER_13V18V){
						holder.info.setText("13/18V");
					 }
				}
				break;
			
			case DISHSETUP_LNB_22K:
				holder.icon.setVisibility(View.VISIBLE);
			        holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.VISIBLE);
			    	 holder.icon.setImageBitmap(mIcon3);
			    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
				 if(SatLnbInfo!= null){	
			    	 	Log.d(TAG,"DISHSETUP_LNB_22K"+SatLnbInfo.getLNBType());	
					if(SatLnbInfo.get22KOnOff()==DbSat.LNB_22K_LOHI){
						holder.info.setText(R.string.dish_setup_conf_auto);
					 }
					 else if(SatLnbInfo.get22KOnOff()==DbSat.LNB_22K_ON){
						holder.info.setText(R.string.dish_setup_conf_on);
					 }
					 else if(SatLnbInfo.get22KOnOff()==DbSat.LNB_22K_OFF){
						holder.info.setText(R.string.dish_setup_conf_off);
					 }
				 }
		    		 break; 
			case DISHSETUP_LNB_TONEBURST:
				 holder.icon.setVisibility(View.VISIBLE);
				 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.VISIBLE);
			    	 holder.icon.setImageBitmap(mIcon4);
			    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
				 if(SatLnbInfo!= null){	
			    	 	Log.d(TAG,"DISHSETUP_LNB_TONEBURST"+SatLnbInfo.getToneburstType());	
					if(SatLnbInfo.getToneburstType()==DbSat.LNB_TONEBURST_NONE){
						holder.info.setText(R.string.dish_setup_conf_none);
					 }
					 else if(SatLnbInfo.getToneburstType()==DbSat.LNB_TONEBURST_A){
						holder.info.setText("A");
					 }
					 else if(SatLnbInfo.getToneburstType()==DbSat.LNB_TONEBURST_B){
						holder.info.setText("B");
					 }
				 }
				break;
			case DISHSETUP_DISEQC1_0:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}
				else{
					if(SatLnbInfo== null){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);						
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
					}
					else{
						{
							holder.info.setVisibility(View.VISIBLE);
					 		holder.icon1.setVisibility(View.VISIBLE);
							holder.icon.setVisibility(View.VISIBLE);
				    			holder.icon.setImageBitmap(mIcon6);
				    	 		holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC_CONFIG"+SatLnbInfo.getSwtPort());	
										
							switch(SatLnbInfo.getLnbConfig10()){
								
								case 0:
									holder.info.setText("LNB1");
									break;
								case 1:
									holder.info.setText("LNB2");
									break;
								case 2:
									holder.info.setText("LNB3");
									break;
								case 3:
									holder.info.setText("LNB4");
									break;	
								case 4:
									holder.info.setText(R.string.dish_setup_conf_none);
									break;	
							}
									
						 }	
					}	 
				}	
				break;
			case DISHSETUP_DISEQC1_1:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
					holder.info.setVisibility(View.INVISIBLE);
				}
				else{
					if(SatLnbInfo== null){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						{
							holder.icon.setVisibility(View.VISIBLE);
							holder.info.setVisibility(View.VISIBLE);
					 		holder.icon1.setVisibility(View.VISIBLE);
				    			holder.icon.setImageBitmap(mIcon10);
				    	 		holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC_CONFIG"+SatLnbInfo.getSwtPort());	
							//if(SatLnbInfo.getSwtPort()>=DbSat.LNB_DISEQC_11){
								if(SatLnbInfo.getLnbConfig11()==4||SatLnbInfo.getLnbConfig11()==0)
									holder.info.setText(R.string.dish_setup_conf_none);
								else
									holder.info.setText("LNB"+String.valueOf(SatLnbInfo.getLnbConfig11()-0xf0+1));
								 
							//}
						 }	
					}	 
				}	
				break;
			case DISHSETUP_DISEQC_MOTOR:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
					holder.info.setVisibility(View.INVISIBLE);
				}
				else{
					 holder.icon.setVisibility(View.VISIBLE);
			 	 	 holder.info.setVisibility(View.VISIBLE);
					 holder.icon1.setVisibility(View.VISIBLE);
				    	 holder.icon.setImageBitmap(mIcon5);
				    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					 if(SatLnbInfo!= null){	
				    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC"+SatLnbInfo.getSwtPort());	
						if(SatLnbInfo.getSwtPort()==0||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||
							SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_11
							){
							holder.info.setText(R.string.dish_setup_conf_none);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_12){
							holder.info.setText(R.string.dish_setup_diseqc2);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_13){
							holder.info.setText(R.string.dish_setup_diseqc3);
							show_bottom_set_location();
						 }
					 }
				}	
				break;
				
			/*	
			case DISHSETUP_LNB_DISEQC:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
					holder.info.setVisibility(View.INVISIBLE);
				}
				else{
					 holder.icon.setVisibility(View.VISIBLE);
			 	 	 holder.info.setVisibility(View.VISIBLE);
					 holder.icon1.setVisibility(View.VISIBLE);
				    	 holder.icon.setImageBitmap(mIcon5);
				    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					 if(SatLnbInfo!= null){	
				    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC"+SatLnbInfo.getSwtPort());	
						if(SatLnbInfo.getSwtPort()==0||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
							holder.info.setText(R.string.dish_setup_conf_none);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
							holder.info.setText(R.string.dish_setup_diseqc0);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_11){
							holder.info.setText(R.string.dish_setup_diseqc1);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_12){
							holder.info.setText(R.string.dish_setup_diseqc2);
							hide_bottom_set_location();
						 }
						 else if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_13){
							holder.info.setText(R.string.dish_setup_diseqc3);
							show_bottom_set_location();
						 }
					 }
				}	
				 break;
			
			case DISHSETUP_LNB_COMMITTED_COMMAND:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}
				else{
					if(SatLnbInfo== null){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);						
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
					}
					else{
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
							holder.icon.setVisibility(View.INVISIBLE);
						 	holder.icon1.setVisibility(View.INVISIBLE);
							holder.info.setVisibility(View.INVISIBLE);
							holder.text.setTextColor(Color.DKGRAY);
						}	
						else{
							holder.info.setVisibility(View.VISIBLE);
					 		holder.icon1.setVisibility(View.VISIBLE);
							holder.icon.setVisibility(View.VISIBLE);
				    			holder.icon.setImageBitmap(mIcon6);
				    	 		holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC_CONFIG"+SatLnbInfo.getSwtPort());	
										
							switch(SatLnbInfo.getLnbConfig10()){
								
								case 0:
									holder.info.setText("AA");
									break;
								case 1:
									holder.info.setText("AB");
									break;
								case 2:
									holder.info.setText("BA");
									break;
								case 3:
									holder.info.setText("BB");
									break;	
								case 4:
									holder.info.setText(R.string.dish_setup_conf_none);
									break;	
							}
									
						 }	
					}	 
				}	
				break;
			case DISHSETUP_LNB_FAST_DISEQC:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}

				else if(SatLnbInfo!=null){
					if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
				
					}
					else{
						holder.icon.setVisibility(View.VISIBLE);
					        holder.info.setVisibility(View.VISIBLE);
						holder.icon1.setVisibility(View.VISIBLE);
					    	holder.icon.setImageBitmap(mIcon7);
							
						if(SatLnbInfo.getFastDiseqc()==0){
							holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
				    		 	holder.info.setText(R.string.off);
						}
						else{
						 	holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
				    		 	holder.info.setText(R.string.on);
						}
					}
				}
				break;
			case DISHSETUP_LNB_DISEQC_REPEATS:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}

				else if(SatLnbInfo!=null){
					if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
				
					}
					else{
						holder.icon.setVisibility(View.VISIBLE);
					        holder.info.setVisibility(View.VISIBLE);
						holder.icon1.setVisibility(View.VISIBLE);
					    	holder.icon.setImageBitmap(mIcon8);
							
						if(SatLnbInfo.getDiseqcRepeat()==0){
							holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
				    		 	holder.info.setText(R.string.off);
						}
						else{
						 	holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
				    		 	holder.info.setText(R.string.on);
						}
					}
				}
				
				break;
			case DISHSETUP_LNB_DISEQC_SEQUENCE:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}
				else if(SatLnbInfo!=null){
					if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
				
					}
					else{
						holder.icon.setVisibility(View.VISIBLE);
					        holder.info.setVisibility(View.VISIBLE);
						holder.icon1.setVisibility(View.VISIBLE);
					    	holder.icon.setImageBitmap(mIcon9);
							
						holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
							int values = SatLnbInfo.getDiseqcSequence()&0x000F;
							if(values>=2){
								values=0;
							}	
							holder.info.setText(String.valueOf(values));
						}
						else if(SatLnbInfo.getSwtPort()>=DbSat.LNB_DISEQC_11){
							int values = (SatLnbInfo.getDiseqcSequence()>>4)&0x000F;
							if(values<2){
								values=2;
							}	
							holder.info.setText(String.valueOf(values));
						}
				    		
					}
				}
				break;
			case DISHSETUP_LNB_UNCOMMITTED_COMMAND:
				if (isUnicableOn()==true){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
					holder.info.setVisibility(View.INVISIBLE);
				}
				else{
					if(SatLnbInfo== null){
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						if(SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_NONE||SatLnbInfo.getSwtPort()==DbSat.LNB_DISEQC_10){
							holder.icon.setVisibility(View.INVISIBLE);
						 	holder.icon1.setVisibility(View.INVISIBLE);
							holder.text.setTextColor(Color.DKGRAY);
							holder.info.setVisibility(View.INVISIBLE);
						}	
						else{
							holder.icon.setVisibility(View.VISIBLE);
							holder.info.setVisibility(View.VISIBLE);
					 		holder.icon1.setVisibility(View.VISIBLE);
				    			holder.icon.setImageBitmap(mIcon10);
				    	 		holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 
					    	 	Log.d(TAG,"DISHSETUP_LNB_DISEQC_CONFIG"+SatLnbInfo.getSwtPort());	
							if(SatLnbInfo.getSwtPort()>=DbSat.LNB_DISEQC_11){
								if(SatLnbInfo.getLnbConfig11()==4||SatLnbInfo.getLnbConfig11()==0)
									holder.info.setText(R.string.dish_setup_conf_none);
								else
									holder.info.setText("LNB"+String.valueOf(SatLnbInfo.getLnbConfig11()-0xf0+1));
								 
							}
						 }	
					}	 
				}	
				break;
			 case DISHSETUP_LNB_MOTO_NO:

				//if (isUnicableOn()==true){
				if(false){
					holder.icon.setVisibility(View.INVISIBLE);
				 	holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.INVISIBLE);
					holder.text.setTextColor(Color.DKGRAY);
				}
				else if(SatLnbInfo!=null){
					if(SatLnbInfo.getSwtPort()>=DbSat.LNB_DISEQC_12){
						holder.icon.setVisibility(View.VISIBLE);
						holder.info.setVisibility(View.VISIBLE);
						holder.icon1.setVisibility(View.VISIBLE);
						holder.icon.setImageBitmap(mIcon12);
						holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 	
						holder.info.setText(String.valueOf(SatLnbInfo.getMotoNo()+1));
					}
					else{
						holder.icon.setVisibility(View.INVISIBLE);
					 	holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.INVISIBLE);
						holder.text.setTextColor(Color.DKGRAY);
					}
				}
				break;
				*/
		  }

		  holder.icon.setVisibility(View.INVISIBLE);
		  
		  return convertView;
		}
	}

	private  class ScanModeAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		
		private Context cont;
		private String[] listItems;
		private List<DbSat> lnb_list;
		private DbSat SatLnbInfo;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
			TextView   info; 
			ImageButton  iboolean;
			ImageView icon1;
		}
	
		public ScanModeAdapter(Context context) {
			super();
			cont = context;

			listItems = new String[3];
			
			listItems[0]= cont.getResources().getString(R.string.scan_mode);
			listItems[1]= cont.getResources().getString(R.string.scan_crypted);
			listItems[2]= cont.getResources().getString(R.string.scan_service_type);
			
			
			mInflater=LayoutInflater.from(context);
			/*
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_subtitle);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.set_audio_track);
			*/
		}

		public int getCount() {

			return listItems.length;
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
		   convertView = mInflater.inflate(R.layout.lnb_set_xml, null);
		   holder = new ViewHolder();
		   
		   holder.text = (TextView) convertView.findViewById(R.id.text);
		   holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		   holder.info = (TextView)convertView.findViewById(R.id.info);
		   holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
		   convertView.setTag(holder);
		}else {
		  // Get the ViewHolder back to get fast access to the TextView
		  // and the ImageView.
		  holder = (ViewHolder) convertView.getTag();
		  }


		holder.text.setText(listItems[position]);
		 holder.info.setVisibility(View.VISIBLE);
		holder.info.setTextColor(Color.YELLOW);

		holder.icon.setVisibility(View.VISIBLE);
		 holder.icon1.setVisibility(View.VISIBLE);
	    	 holder.icon1.setBackgroundResource(R.drawable.arrow_down2); 

		switch(position){
			case 0:
				String mode = mLast.getString("scan_mode","default");
				holder.icon.setImageBitmap(mIcon1); 
				if (isUnicableOn()==false){
					if(mode.equals("blind")){
						holder.info.setText(R.string.scan_mode_blind);
					}	
					else if(mode.equals("network")){
						holder.info.setText(R.string.scan_mode_network);
						
					}
					else{
						holder.info.setText(R.string.scan_mode_default);		
					}
				}
				else{
					if(mode.equals("blind")){
						mLast.edit().putString ("scan_mode","default").commit();
						holder.info.setText(R.string.scan_mode_default);
						
					}
					else if(mode.equals("network")){
						holder.info.setText(R.string.scan_mode_network);
					}
					else{
						holder.info.setText(R.string.scan_mode_default);		
					}
				}
				break;
			case 1:
				String crypted = mLast.getString("scan_mode_crypted","all");
				holder.icon.setImageBitmap(mIcon2);
				if(crypted.equals("all")){
					holder.info.setText(R.string.scan_crypted_all);
				}	
				else if(crypted.equals("fta")){
					holder.info.setText(R.string.scan_crypted_fta);
					
				}
				break;
				
			case 2:
				String service_mode = mLast.getString("scan_service_mode","all");
				holder.icon.setImageBitmap(mIcon3);
				if(service_mode.equals("all")){
					holder.info.setText(R.string.scan_service_all);
				}	
				else if(service_mode.equals("tv")){
					holder.info.setText(R.string.scan_service_tv);
					
				}
				else{
					holder.info.setText(R.string.scan_service_radio);		
				}
				break;
				
		}

		return convertView;
		}
	}

	private  class DvbsSetLimitAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		
		private Context cont;
		private int Mode;
		private String[] DATA;

	 	class ViewHolder {
			
			TextView text;
			ImageView icon;
			TextView  info;
			ImageView icon1;
		}
	
		public DvbsSetLimitAdapter(Context context,int mode) {
			super();
			cont = context;
			Mode = mode;
			mInflater=LayoutInflater.from(context);

			if(Mode==1){
				DATA = new String[8];
				DATA[0]= cont.getResources().getString(R.string.set_limit_move_continue);			
				DATA[1]= cont.getResources().getString(R.string.set_limit_move_step);
				DATA[2]= cont.getResources().getString(R.string.set_limit_set_east);
				DATA[3]= cont.getResources().getString(R.string.set_limit_set_west);
				//DATA[4]= cont.getResources().getString(R.string.set_limit_goto_reference);
				DATA[4]= cont.getResources().getString(R.string.set_limit_disable_limit);
				DATA[5]= cont.getResources().getString(R.string.set_position_store_position);
				DATA[6]= cont.getResources().getString(R.string.set_position_goto_position);
				DATA[7]= cont.getResources().getString(R.string.set_position_goto_xx);
			}
			else{
				DATA = new String[7];
				DATA[0]= cont.getResources().getString(R.string.set_limit_move_continue);			
				DATA[1]= cont.getResources().getString(R.string.set_limit_move_step);
				DATA[2]= cont.getResources().getString(R.string.set_limit_set_east);
				DATA[3]= cont.getResources().getString(R.string.set_limit_set_west);
				//DATA[4]= cont.getResources().getString(R.string.set_limit_goto_reference);
				DATA[4]= cont.getResources().getString(R.string.set_limit_disable_limit);
				DATA[5]= cont.getResources().getString(R.string.set_position_store_position);
				DATA[6]= cont.getResources().getString(R.string.set_position_goto_position);
			}
		}

		public int getCount() {	
			return DATA.length;
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
				convertView = mInflater.inflate(R.layout.dvbs_set_limit_list_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				holder.icon = (ImageView)convertView.findViewById(R.id.icon);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			
			holder.text.setText(DATA[position]);
			if(position==0||position==1){
				holder.info.setVisibility(View.VISIBLE);
				holder.icon.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.VISIBLE);
				holder.info.setTextColor(Color.YELLOW);	

				String mode = holder.info.getText().toString();
				if(mode.equals("")||mode.equals(null))
					holder.info.setText("Stop");
				else
					holder.info.setText(mode);	
			} 
			
			return convertView;
		}
	}

	public static ArrayList<DefaultList> getDefaultList(){
		return DTVScanDvbsConfig.default_list;
	}

	@Override
	protected void onResume(){
		Log.d(TAG,"onResume");
		super.onResume();
		show_no_satellites_info();	
		if(ListStatus==0){
			mySatAdapter.notifyDataSetChanged();
			myLnbSetAdapter.notifyDataSetChanged();
		}
		else{
			myTsAdapter.notifyDataSetChanged();
			myLnbSetAdapter.notifyDataSetChanged();
		}	
	}

}
