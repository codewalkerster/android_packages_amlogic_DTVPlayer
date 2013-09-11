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
import java.lang.reflect.Field;

import com.amlogic.widget.PasswordDialog;
import com.amlogic.widget.SureDialog;
import com.amlogic.widget.SingleChoiseDialog;
import com.amlogic.widget.MutipleChoiseDialog;
import com.amlogic.widget.PasswordSettingDialog;
import com.amlogic.widget.CheckUsbdevice;

public class DTVSettingsMenu extends DTVActivity {
	private static final String TAG="DTVSettingsMenu";
	DTVSettings mDTVSettings = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtv_settings_menu);
	}

	@Override
	protected void onStop(){
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		mDTVSettings = new DTVSettings(this);
		DTVSettingsMenu_UI_Init();
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


	ImageButton button_program=null;
	ImageButton button_search=null;
	ImageButton button_system=null;
	ImageButton button_av_setting=null;

	public static final int BUTTON_PROGRAM = 0;
	public static final int BUTTON_SEARCH = 1;	
	public static final int BUTTON_SYSTEM = 2;
	public static final int BUTTON_AV_SETTING = 3;
	int button_status = BUTTON_SEARCH;

	void DTVSettingsMenu_UI_Init(){
	
		//curr_content_layout =  (LinearLayout)findViewById(R.id.linearLayout_vod_home);		
		//VodHomeLayout=(RelativeLayout)findViewById(R.id.VodHomeLayout);		
		//bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();	
			
		button_program = (ImageButton)findViewById(R.id.button_program);	
		button_search = (ImageButton)findViewById(R.id.button_search);	
		button_system = (ImageButton)findViewById(R.id.button_system);	
		button_av_setting = (ImageButton)findViewById(R.id.button_av);	
				
		button_program.setOnClickListener(new ButtonClick()); 	
		button_search.setOnClickListener(new ButtonClick()); 	
		button_system.setOnClickListener(new ButtonClick()); 	
		button_av_setting.setOnClickListener(new ButtonClick()); 	

		button_program.setOnFocusChangeListener(new OnFocusChangeListener()
		    {
		      public void onFocusChange(View v, boolean isFocused)
		      {
		        if (isFocused==true){
					button_program.setBackgroundResource(R.drawable.button_program);
					ProgramItem_Init();
		        }
				/*
		        else {
		        	if(button_status == BUTTON_PROGRAM)
						button_program.setBackgroundResource(R.drawable.program3);
		        }
		        */
		      }
		});

		button_program.setOnKeyListener(new buttonOnKeyListener());

		button_search.setOnFocusChangeListener(new OnFocusChangeListener()
		    {
		      public void onFocusChange(View v, boolean isFocused)
		      {
		        if (isFocused==true){
					button_search.setBackgroundResource(R.drawable.button_search);
					SearchItem_Init();
		        }
				/*
		        else {
		        	if(button_status == BUTTON_SEARCH)
						button_search.setBackgroundResource(R.drawable.search3);
		        }
		        */
		      }
		});
		button_search.setOnKeyListener(new buttonOnKeyListener());
		button_system.setOnFocusChangeListener(new OnFocusChangeListener()
		    {
		      public void onFocusChange(View v, boolean isFocused)
		      {
		        if (isFocused==true){
					button_system.setBackgroundResource(R.drawable.button_system);
					SystemItem_Init();
		        }
				/*
		        else {
		        	if(button_status == BUTTON_SYSTEM)
						button_system.setBackgroundResource(R.drawable.system3);
		        }
		        */
		      }
		});
		button_system.setOnKeyListener(new buttonOnKeyListener());
		button_av_setting.setOnFocusChangeListener(new OnFocusChangeListener()
		    {
		      public void onFocusChange(View v, boolean isFocused)
		      {
		        if (isFocused==true){
					button_av_setting.setBackgroundResource(R.drawable.button_av);
					AVItem_Init();
		        }
				/*
		        else {
		        	if(button_status == BUTTON_AV_SETTING)
						button_av_setting.setBackgroundResource(R.drawable.av3);
		        }
		        */
		      }
		});
		button_av_setting.setOnKeyListener(new buttonOnKeyListener());
		//button_search.setBackgroundResource(R.drawable.search2);	
		button_search.requestFocus();
		SearchItem_Init();
		
		//dialog
		editBuilder = new AlertDialog.Builder(this);
		editBuilder_password = editBuilder;
	}

	class ButtonClick  implements android.view.View.OnClickListener{	  
		public void onClick(View v) {		
			// TODO Auto-generated method stub		
			switch (v.getId()) {			
				case R.id.button_program:		
					/*			
					button_program.setBackgroundResource(R.drawable.program3);		
					button_search.setBackgroundResource(R.drawable.button_search);		
					button_system.setBackgroundResource(R.drawable.button_system);
					button_av_setting.setBackgroundResource(R.drawable.button_av);	
					*/
					
					//get data	
					ProgramItem_Init();
					break;
				case R.id.button_search:	
					/*
					button_program.setBackgroundResource(R.drawable.button_program);		
					button_search.setBackgroundResource(R.drawable.search3);		
					button_system.setBackgroundResource(R.drawable.button_system);
					button_av_setting.setBackgroundResource(R.drawable.button_av);	
					*/
					
					//get data	
					SearchItem_Init();
					break;
				case R.id.button_system:	
					/*
					button_program.setBackgroundResource(R.drawable.button_program);		
					button_search.setBackgroundResource(R.drawable.button_search);		
					button_system.setBackgroundResource(R.drawable.system3);
					button_av_setting.setBackgroundResource(R.drawable.button_av);	
					*/
					 
					//get data	
					SystemItem_Init();
					break; 	
				case R.id.button_av:
					/*
					button_program.setBackgroundResource(R.drawable.button_program);		
					button_search.setBackgroundResource(R.drawable.button_search);		
					button_system.setBackgroundResource(R.drawable.button_system);
					button_av_setting.setBackgroundResource(R.drawable.av3);	
					*/
					 
					//get data
					AVItem_Init();
					break;	
			}		

		}	
	}

	private static String[] DATA = null;
	private Toast toast=null;
	public static final int SETTINGS_RECALL_LIST=0;
	public static final int SETTINGS_SUBTILE_SWITCH = 1;
	public static final int SETTINGS_DEFAULT = 2;
	public static final int SETTINGS_SET_PASSWORD = 3;

	/*
	public static final int SETTINGS_SELECT_STORAGE=4;
	public static final int SETTINGS_TIMESHIFT_TIME_SET=5;
	*/
	public static final int SETTINGS_PARENTAL_RATING_SET=4; //and vchip
	public static final int SETTINGS_TTX_REGION=5;
	public static final int SETTINGS_CC=6;
	public static final int SETTINGS_ANTENNA_SOURCE=7;
		
	private String ttx_region_str=null;
	private static String[] ttx_region_str_arry=null;
	private int cur_select_item=0;

	private ListView ListView_settings=null;
	private ProgramItemAdapter myProgramAdapter=null;
	private SearchItemAdapter mySearchAdapter=null;
	private SystemItemAdapter mySystemAdapter=null;
	private AvItemAdapter myAvAdapter=null;
	
	private AlertDialog.Builder editBuilder=null;
	private AlertDialog.Builder editBuilder_password=null;

	private int timeshift_time_select_item=-1;
	private int audio_track_select_item=-1;
	private int ttx_region_select_item=-1;

	private void SystemItem_Init(){
		Log.d(TAG,"scan region="+mDTVSettings.getScanRegion());
		button_status = BUTTON_SYSTEM;  
		if(mDTVSettings.getScanRegion().contains("ATSC"))	
			DATA = getResources().getStringArray(R.array.system_settings_content_atsc);
		else
			DATA = getResources().getStringArray(R.array.system_settings_content);
	
		//ttx_region_str = mDTVSettings.getTeletextRegionName();
		//if(ttx_region_str!=null)
			//ttx_region_str_arry = ttx_region_str.split(" "); 
		ttx_region_str_arry = getResources().getStringArray(R.array.teletext_region_lauguage);

		//listview
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		mySystemAdapter = new SystemItemAdapter(this,DATA);
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new listOnKeyListener());
		ListView_settings.setOnItemClickListener(mSystemOnItemClickListener);
		ListView_settings.setAdapter(mySystemAdapter);
		mySystemAdapter.notifyDataSetChanged();
		//ListView_settings.requestFocus();
		
	}

	private void ProgramItem_Init(){
		button_status = BUTTON_PROGRAM; 
		if(mDTVSettings.getScanRegion().contains("DVBS")){
			DATA = getResources().getStringArray(R.array.Program_settings_content_dvbs);
		}
		else
			DATA = getResources().getStringArray(R.array.Program_settings_content);

		//listview
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myProgramAdapter = new ProgramItemAdapter(this,DATA);
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new listOnKeyListener());
		ListView_settings.setOnItemClickListener(mProgramOnItemClickListener);
		ListView_settings.setAdapter(myProgramAdapter);
		//ListView_settings.requestFocus();

		/*
		//title		
		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.settings_title);
		*/
		
	}

	private void SearchItem_Init(){
		Log.d(TAG,"scan region="+mDTVSettings.getScanRegion());
		button_status = BUTTON_SEARCH;  
		DATA = getResources().getStringArray(R.array.search_settings_content_dvbs);

		//listview
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		mySearchAdapter = new SearchItemAdapter(this,DATA);
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new listOnKeyListener());
		ListView_settings.setOnItemClickListener(mSearchOnItemClickListener);
		ListView_settings.setAdapter(mySearchAdapter);
		//ListView_settings.requestFocus();
	}
	private void AVItem_Init(){
		Log.d(TAG,"scan region="+mDTVSettings.getScanRegion());
		button_status = BUTTON_AV_SETTING;  
		DATA = getResources().getStringArray(R.array.av_settings_content);

		//listview
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myAvAdapter = new AvItemAdapter(this,DATA);
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new listOnKeyListener());
		ListView_settings.setOnItemClickListener(mAvOnItemClickListener);
		ListView_settings.setAdapter(myAvAdapter);
		//ListView_settings.requestFocus();

	}

		
	private AnimationSet listItemFocus ;
	private void UIAnimationInit(){
		listItemFocus = new AnimationSet(true);
	    ScaleAnimation  scale  = new ScaleAnimation(1.01f, 0.99f, 1.02f, 0.99f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
	     listItemFocus.addAnimation(scale);
	     scale.setDuration(300);
	     scale.setRepeatCount(0);
		 listItemFocus.addAnimation(scale);
	}
		
	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
			ListView_settings = (ListView)findViewById(R.id.settings_list);
			/*
				if(v.isSelected())
				 	v.startAnimation(listItemFocus);
				else
					v.clearAnimation();
			*/
			cur_select_item = position;
		}
		
		public void onNothingSelected(AdapterView<?> parent){
		}
	}; 	

	class listOnKeyListener implements OnKeyListener{
		public boolean onKey(View v, int keyCode, KeyEvent event) {					
			switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:	
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						//v.clearAnimation();
					}
					break;
			} 
			return false;
		}
	}

	class buttonOnKeyListener implements OnKeyListener{
		public boolean onKey(View v, int keyCode, KeyEvent event) {					
			switch (v.getId()) {			
				case R.id.button_program:		
				case R.id.button_search:	
				case R.id.button_system:					
				case R.id.button_av:
					{
						switch(keyCode){
							case KeyEvent.KEYCODE_DPAD_DOWN:
								if (event.getAction() == KeyEvent.ACTION_DOWN) {
									if(button_status == BUTTON_PROGRAM)
										button_program.setBackgroundResource(R.drawable.program3);
						        	else if(button_status == BUTTON_SEARCH)
										button_search.setBackgroundResource(R.drawable.search3);
						        	else if(button_status == BUTTON_SYSTEM)
										button_system.setBackgroundResource(R.drawable.system3);
						        	else if(button_status == BUTTON_AV_SETTING)
										button_av_setting.setBackgroundResource(R.drawable.av3);
		    
								}
								break;
							} 
					}
					break;	
			}	
			
			return false;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onActivityResult");
		int p=-1;
		
		if(data!=null){
			Bundle bundle =data.getExtras();
			p = bundle.getInt("position");			
		}		

		if(resultCode == RESULT_OK){
			switch(requestCode){
			  case 11:
			  	 onShow();
				 switch(p){	
				    case 0:
				 		onShow();   	
				    	break;
				    case 1:
				    	onShow();
				    	break;
				  }
				  break;
			}
		}	
	}

	private AdapterView.OnItemClickListener mAvOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			switch(position){
				case 0:
					showScreenTypeDialog(info_cur);		
					break;
				case 1: //subtitle language
					showSubtitleLanguageDialog(info_cur);
					break;
				case 2:  //audio language	
					showAudioLanguageDialog(info_cur);
					break;
				//case 3:
					//showAudioTrackDialog(info_cur);	
					//break;
			}
		}
	};
	
	private class AvItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public AvItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
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

			// Bind the data efficiently with the holder.
			holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			holder.info.setVisibility(View.VISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			switch(position){
			
		     case 0://SETTINGS_SCREEN_TYPE:
			 	

				 int mode= mDTVSettings.getScreenMode();		 
				 if(mode==0){
				 	 holder.info.setText(R.string.auto);
				 }
				 else  if(mode==2){
					holder.info.setText(R.string.type_4_3);
				 }
				 else  if(mode==3){
					holder.info.setText(R.string.type_16_9);
				 }	
		    	 break;
			case 1: //subtitle language
				String language = getLanguage(getIndexLanguage(mDTVSettings.getSubtitleLanguage()));
				holder.info.setText(language);
				break;
			case 2:  //audio language
				String lan = getLanguage(getIndexLanguage(mDTVSettings.getAudLanguage()));
				holder.info.setText(lan);
				break;
			/*
			case 3://SETTINGS_AUDIO_TRACK:
			 	
		    	 holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				
				 int tmp= mDTVSettings.getAudioTrack();
			
				 if(tmp == 2)
				 	holder.info.setText(R.string.stereo);
				 else if(tmp == 1)
					holder.info.setText(R.string.right);
				 else if(tmp == 0)
				 	holder.info.setText(R.string.left);
		    	 break;
		    */	 
		  }

		  return convertView;
		}
	}	


	private AdapterView.OnItemClickListener mProgramOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			if(mDTVSettings.getScanRegion().contains("DVBS")){
				switch(position){
					case 0:
						if(isHavePragram()){
								
							showProgramManagerPasswordDialog();
							
						}
						else{
							toast = Toast.makeText(
							DTVSettingsMenu.this,
							R.string.dtvplayer_no_program,
							Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						break;
					case 1:						
						if(isHavePragram()){
							DTVStartEPG();
							DTVSettingsMenu.this.finish();
						}
						else{
							toast = Toast.makeText(
							DTVSettingsMenu.this,
							R.string.dtvplayer_no_program,
							Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						break;
					case 2:
						Intent Intent_record_device = new Intent();
		        		Intent_record_device.setClass(DTVSettingsMenu.this, DTVRecordDevice.class);
		        		startActivityForResult(Intent_record_device,12);
						//DTVSettingsMenu.this.finish();
						break;
					case 3:
						showPvrManagerPasswordDialog();
						break;
					case 4:
						if(isHavePragram()){
							if(mDTVSettings.getCheckProgramLock()==false){					
								if(isHaveExternalStorage()){
									Intent Intent_timeshifting = new Intent();
									Intent_timeshifting.setClass(DTVSettingsMenu.this,DTVTimeshifting.class);
									startActivity(Intent_timeshifting);
									DTVSettingsMenu.this.finish();
								}
								else{
									if(toast!=null)
										toast.cancel(); 
									toast = Toast.makeText(
									DTVSettingsMenu.this,
									R.string.check_usb_device,
									Toast.LENGTH_SHORT);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();
								}	
							}
							else{
								showTimeshiftingPasswordDialog();
							}
						}else{
							toast = Toast.makeText(
							DTVSettingsMenu.this,
							R.string.dtvplayer_no_program,
							Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						break;
				}
			}
			else{
				switch(position){
					case 0:
						DTVStartProgramManager();
						DTVSettingsMenu.this.finish();
						break;
					case 1:
						DTVStartEPG();
						DTVSettingsMenu.this.finish();
						break;
					case 2:
						showSortByDialog(info_cur);
						break;
					case 3:
						Intent Intent_record_device = new Intent();
		        		Intent_record_device.setClass(DTVSettingsMenu.this, DTVRecordDevice.class);
		        		startActivityForResult(Intent_record_device,12);
						//DTVSettingsMenu.this.finish();
						break;
					case 4:
						Intent Intent_pvr_manager = new Intent();
						Intent_pvr_manager.setClass(DTVSettingsMenu.this,DTVPvrManager.class);
						startActivity(Intent_pvr_manager);
						DTVSettingsMenu.this.finish();
						break;
					case 5:
						Intent Intent_timeshifting = new Intent();
						Intent_timeshifting.setClass(DTVSettingsMenu.this,DTVTimeshifting.class);
						startActivity(Intent_timeshifting);
						DTVSettingsMenu.this.finish();
						break;
				}
			}
		}
	};
	
	private class ProgramItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public ProgramItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
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

			// Bind the data efficiently with the holder.
			holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			holder.info.setVisibility(View.INVISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			switch(position){
				case 2: 
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
				 	break;	
			}

		  return convertView;
		}
	}	

	private AdapterView.OnItemClickListener mSearchOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			switch(position){
				case 0:
					{	
						Intent intent_dish_setup = new Intent();
						intent_dish_setup.setClass(DTVSettingsMenu.this, DTVScanDvbsConfig.class);
							startActivity(intent_dish_setup);
						DTVSettingsMenu.this.finish();
					}	
					break;
				case 1:     //DB management
					{
						/*
						Intent intent_db_management = new Intent();
						intent_db_management.setClass(DTVSettingsMenu.this, DTVScanDvbsDBManagement.class);
						startActivityForResult(intent_db_management,2);
						*/
						DBMInit();
					}
					break;
				case 2:
					{
						/*
						Intent intent_unicable_config = new Intent();
						intent_unicable_config.setClass(DTVSettingsMenu.this,DTVSettingsMenu.class);
						startActivityForResult(intent_unicable_config,3);
						*/
						DTVDvbsUnicableConfig_UIInit();
					}
					break;
			}
		}
	};
	
	private class SearchItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public SearchItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
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

			// Bind the data efficiently with the holder.
			holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			switch(position){
			
		     
			 case 0:
			 	 holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 
		    	 break;	 
		     case 1:
			 	holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    
		    	 break;
			 case 2: 
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 /*
		    	 holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				
				 int tmp= mDTVSettings.getAudioTrack();
			
				 if(tmp == 2)
				 	holder.info.setText(R.string.stereo);
				 else if(tmp == 1)
					holder.info.setText(R.string.right);
				 else if(tmp == 0)
				 	holder.info.setText(R.string.left);
				 */	
		    	 break;	 
		  }

		  return convertView;
		}
	}	

	private AdapterView.OnItemClickListener mSystemOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){			
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);

			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			
			switch(position){
				case SETTINGS_RECALL_LIST:
					showRcallSetDialog(info_cur);
					break;
			    case SETTINGS_SUBTILE_SWITCH:{	
			    	if(mDTVSettings.getSubtitleStatus()==false)
			    	{
			    		image_cur.setBackgroundResource(R.drawable.select_round_2);
			    		info_cur.setText(R.string.on);
						mDTVSettings.setSubtitleStatus(true);
			    	}
			    	else if(mDTVSettings.getSubtitleStatus())
			    	{
			    		image_cur.setBackgroundResource(R.drawable.select_round_1);
			    		info_cur.setText(R.string.off);
						mDTVSettings.setSubtitleStatus(false);
			    	}	
				}
			    break;
				
				case SETTINGS_DEFAULT:
					showPasswordDialog();
					break;
				case SETTINGS_SET_PASSWORD:
					showEnterPasswordSettingDialog();
					break;
				/*	
				case SETTINGS_SELECT_STORAGE:
					
					break;
				 case SETTINGS_TIMESHIFT_TIME_SET:
				 	//showTimeshiftingTimeSettingDialog(info_cur);
					break;
				*/	
				 case SETTINGS_PARENTAL_RATING_SET:
				 	if(mDTVSettings.getScanRegion().contains("ATSC")){
						//DTVVChip
						DTVSetting_GotoDTVVChip();
					}
					else
						showParentalRatingPasswordDialog(info_cur);
					break;
				case SETTINGS_TTX_REGION:
					showTTXRegionDialog(info_cur);
					break;
				case SETTINGS_CC:
					{
						DTVSetting_GotoDTVCC();
						onStop();
					}	
					break;	
				case SETTINGS_ANTENNA_SOURCE:
					showAntennaSourceSettingDialog(info_cur);
					break;	
			}
		}
	};
	
	private class SystemItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
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

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public SystemItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_update_sys_time);
			mIcon4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_subtitle);
			mIcon5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_factory_data_reset);
			mIcon10 =
			mIcon6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_password);
			mIcon7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_update_sys_time);
		  	mIcon8 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_select_storage);
			mIcon9 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_timeshifting_buffering);
			mIcon11 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_subtitle);
			
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
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
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

			// Bind the data efficiently with the holder.
			holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			switch(position){
			case SETTINGS_RECALL_LIST:
				int n = DTVPlayergetRecallNumber();
				holder.icon.setImageBitmap(mIcon1);
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
				holder.info.setText(String.valueOf(n));
				break;
			case SETTINGS_SUBTILE_SWITCH:
				{
					holder.icon.setImageBitmap(mIcon1);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					if (mDTVSettings.getSubtitleStatus()){			   
						//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
						holder.info.setText(R.string.on);
					}
					else{
						//holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
						holder.info.setText(R.string.off);
					}	  
				}
		    	break;
		     case SETTINGS_DEFAULT:
			 	holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
				 holder.icon.setImageBitmap(mIcon5);
				 break;
		 
		     case SETTINGS_SET_PASSWORD:
			 	 holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 holder.icon.setImageBitmap(mIcon6);
		    	 break;	
			 /*  	 
		     case SETTINGS_SELECT_STORAGE:
			 	 holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 holder.icon.setImageBitmap(mIcon8);
		    	 break;	 
		     case SETTINGS_TIMESHIFT_TIME_SET:
			 	holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				holder.icon.setImageBitmap(mIcon9);
				holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 

				int time = mDTVSettings.getTimeShiftingDuration();
				switch(time){
					case 600:
						holder.info.setText("10 Min");
						break;
					case 30*60:
						holder.info.setText("30 Min");
						break;
					case 60*60:
						holder.info.setText("60 Min");
						break;	
				}
				break;
			*/	
		     case SETTINGS_PARENTAL_RATING_SET:
			 	if(mDTVSettings.getScanRegion().contains("ATSC")){
					 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
			    	 holder.icon.setImageBitmap(mIcon4);
				}
				else{
				 	holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					holder.icon.setImageBitmap(mIcon10);

					int pr = mDTVSettings.getParentalRating();
					if(pr<=0){
						holder.info.setText(R.string.all);
					}else{
						holder.info.setText(pr+"");
					}
				}
				break;
			case SETTINGS_TTX_REGION:
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				holder.icon.setImageBitmap(mIcon12);
				
				String region = mDTVSettings.getTeletextRegion(); 
				int pos = getTTXRegionIndex(region);

				holder.info.setSingleLine(true);
				holder.info.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));
				if(ttx_region_str_arry!=null)
					holder.info.setText(ttx_region_str_arry[pos]);

				break;
			case SETTINGS_CC:
				if(mDTVSettings.getScanRegion().contains("ATSC")){
					 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
			    	 holder.icon.setImageBitmap(mIcon4);
				}
				break;
			case SETTINGS_ANTENNA_SOURCE:
				if(mDTVSettings.getScanRegion().contains("ATSC")){
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					holder.icon.setImageBitmap(mIcon4);

					String source = mDTVSettings.getAtscAntennaSource();
					if(source.equals("air")){
						holder.info.setText("Air");
					}
					else{
						holder.info.setText("Cable");
					}
				}
				break;
		  }

		  return convertView;
		}
	}	


	private AlertDialog.Builder satXmlBuilder;
	private View dvbs_satxml;
	private void showSatellitesDB(List<String> list){
		final List<String> filelist = list;	

		final Dialog mDialog = new AlertDialog(this){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						dismiss();
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

		mDialog.show();
		mDialog.setContentView(R.layout.dtv_list_dialog);

		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		TextView title = (TextView)window.findViewById(R.id.title);
		title.setText(getString(R.string.dvbs_dbm_load));
		title.setTextColor(Color.YELLOW);
		
		ListView mListView =(ListView)window.findViewById(R.id.settings_list);
		DvbsDBXmlAdapter mDvbsDBXmlAdapter = new DvbsDBXmlAdapter(this,filelist);
		mListView.setAdapter(mDvbsDBXmlAdapter);
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				final String path = filelist.get(arg2);
				importDatabase(path);
				toast = Toast.makeText(
						DTVSettingsMenu.this,
						R.string.xml_load_success,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
				
			}
        	    
        });
			
		mListView.requestFocus();
		mDvbsDBXmlAdapter.notifyDataSetChanged();
		

		/*
		satXmlBuilder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);  

		dvbs_satxml = layoutInflater.inflate(R.layout.dtv_list_dialog, null); 
		satXmlBuilder.setTitle(R.string.dvbs_dbm_load);

		ListView mListView =(ListView)dvbs_satxml.findViewById(R.id.settings_list);
		DvbsDBXmlAdapter mDvbsDBXmlAdapter = new DvbsDBXmlAdapter(this,filelist);
		mListView.setAdapter(mDvbsDBXmlAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				final String path = filelist.get(arg2);
				importDatabase(path);
				toast = Toast.makeText(
						DTVSettingsMenu.this,
						R.string.xml_load_success,
						Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
				
			}
        	    
        });
			
		mListView.requestFocus();
		mDvbsDBXmlAdapter.notifyDataSetChanged();
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
		*/
	}

	private void dealLoadSatelliteXML(){
		
		CheckUsbdevice Usbdevice = new CheckUsbdevice(this);

		if(Usbdevice.getDevice()==null)
		{
			toast = Toast.makeText(
			DTVSettingsMenu.this,
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
			DTVSettingsMenu.this,
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

			exportDatabase(satellites_db+"/"+"satellites_"+today+".xml");
			
			toast = Toast.makeText(
				DTVSettingsMenu.this,
				R.string.xml_generator_success,
				Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
		}	

	}


	private AdapterView.OnItemClickListener mDBMOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			switch(position){
				case 0:
					dealLoadSatelliteXML();	
					break;
				case 1:     //DB management
					dealBackupSatelliteXML();
					break;
			}
		}
	};

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
			TextView 	 filename;
			TextView     Time;
			ImageView icon;
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

				convertView = mInflater.inflate(R.layout.pvr_manager_item, null);
				holder = new ViewHolder();
				
		      	holder.Time=(TextView) convertView.findViewById(R.id.pvr_time);
				holder.filename=(TextView) convertView.findViewById(R.id.filename);			   
			    convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			// Bind the data efficiently with the holder.
			
			holder.filename.setText(listItems.get(position));
			return convertView;
		}
	}

	private DvbsDBMAdapter myDBMAdapter=null;
	private void DBMInit(){
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myDBMAdapter = new DvbsDBMAdapter(this,null);
	
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				switch(keyCode){
					case KeyEvent.KEYCODE_BACK:
						SearchItem_Init();
						return true;
				} 
				return false;
			}
		});
		ListView_settings.setOnItemClickListener(mDBMOnItemClickListener);
		ListView_settings.setAdapter(myDBMAdapter);
		ListView_settings.requestFocus();
		
	}


	private void DTVDvbsUnicableConfig_UIInit(){
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		
		ListView_settings.setAdapter(new DvbsUnicableAdapter(this));
		ListView_settings.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				switch(keyCode){
					case KeyEvent.KEYCODE_BACK:
						SearchItem_Init();
						return true;
				} 
				return false;
			}
		});
		ListView_settings.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				TextView info = (TextView)arg1.findViewById(R.id.info);

				System.out.println("onItemSelected arg3 " + arg3);

				/*
				DVBUnicableSetting setting = getUnicableSetting();
				if (setting == null) {
					Log.d(TAG, "Cannot get unicable setting from service");
					return;
				}
				*/
				switch(arg2){
					case 0:
						setUnicableSwitchStatus(!getUnicableSwitchStatus());
						refreshListData();
						break;
					case 1: 					
						int band = getUserBand();
						if(band>=0&&band<7){
							info.setText("LNB"+String.valueOf(band+2));
							setUserBand(band + 1);
						}
						else{
							info.setText("LNB1");
							setUserBand(0);
						}
						
						break;
					case 2:
						showUnicableConfigEditDia();
						break;
				}
			}	
		});	
	}
	 
	private void setUnicableUserDefinedData(){
		//usfs[index]=fre;
		//setConfig("tv:dtv:unicableusebandfreq",usfs[index]);
			
		setConfig("tv:dtv:unicableuseband0freq",usfs[0]);
		setConfig("tv:dtv:unicableuseband1freq",usfs[1]);
		setConfig("tv:dtv:unicableuseband2freq",usfs[2]);
		setConfig("tv:dtv:unicableuseband3freq",usfs[3]);
		setConfig("tv:dtv:unicableuseband4freq",usfs[4]);
		setConfig("tv:dtv:unicableuseband5freq",usfs[5]);;
		setConfig("tv:dtv:unicableuseband6freq",usfs[6]);
		setConfig("tv:dtv:unicableuseband7freq",usfs[7]);
	}

	private List<String> getUnicableUserDefinedData(){

		List<String> dataList = new ArrayList<String>();
		
		usfs= new int[8];

		usfs[0] = getIntConfig("tv:dtv:unicableuseband0freq");
		usfs[1] = getIntConfig("tv:dtv:unicableuseband1freq");
		usfs[2] = getIntConfig("tv:dtv:unicableuseband2freq");
		usfs[3] = getIntConfig("tv:dtv:unicableuseband3freq");
		usfs[4] = getIntConfig("tv:dtv:unicableuseband4freq");
		usfs[5] = getIntConfig("tv:dtv:unicableuseband5freq");
		usfs[6] = getIntConfig("tv:dtv:unicableuseband6freq");
		usfs[7] = getIntConfig("tv:dtv:unicableuseband7freq");

		for (int i = 0; i<8; i++) {
			dataList.add(""+usfs[i]);
		}
		
		return dataList;
	}

	private boolean getUnicableSwitchStatus(){
		return getBooleanConfig("tv:dtv:unicable_switch");
	}

	private void setUnicableSwitchStatus(boolean b){
		setConfig("tv:dtv:unicable_switch", b);
	}

	private int getUserBand(){
		int band = 0;
		band = getIntConfig("tv:dtv:unicableuseband");

		return band;
	}

	private void setUserBand(int band){
		setConfig("tv:dtv:unicableuseband",band);
	}
	
	private AlertDialog.Builder builder;
	public void showEditUnicableFreDialog(View v,int pos){
		final View view = v;
		final int position = pos;
		builder = new AlertDialog.Builder(this);	
		final EditText editText = new EditText(this);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(4)});
		
		//editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		builder.setTitle(R.string.edit_title);
		TextView desFreText =(TextView) view.findViewById(R.id.edit_fre);
		editText.setText(desFreText.getText().toString());
		builder.setView(editText); 

		AlertDialog alert = builder.create();

		alert.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						String fre = editText.getText().toString();
						if(fre==null||fre.equals("")){
							editText.setText(null);
							toast = Toast.makeText(
							DTVSettingsMenu.this, 
						    	R.string.invalid_input,
						    	Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						else{
							if(Integer.parseInt(fre)==0){
								editText.setText(null);
								toast = Toast.makeText(
								DTVSettingsMenu.this, 
							    	R.string.invalid_input,
							    	Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
							else{
								TextView des = (TextView)view.findViewById(R.id.edit_fre);
								des.setText(editText.getText().toString());
								Log.d(TAG,"Old usf"+usfs[position]);
								usfs[position]= Integer.parseInt(editText.getText().toString());
								Log.d(TAG,"New usf"+usfs[position]);
								dialog.cancel();
							}	
						}
						return true;
					case  KeyEvent.KEYCODE_BACK:
						dialog.cancel();
						return true;
				}
				
				return false;
			}
		});	

		
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
			
							}         
							}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {	
						}         
						});	
		alert.show();	
		alert.getWindow().setLayout(500, -200);
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}


	private AlertDialog.Builder diaBuilder;
	private View dvbs_unicable_user_list;
	int[] usfs = null;
	/*
	private void showUnicableConfigEditDia(){
		//final DVBUnicableSetting unicable_setting = setting;
		ContentValues values=null;
		diaBuilder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_unicable_user_list = layoutInflater.inflate(R.layout.dvbs_unicable_list_dia, null); 
		diaBuilder.setTitle(R.string.dvbs_unicable_user_define);
		usfs = new int[8];
		List<String> dataList = getUnicableUserDefinedData();
		ListView UnicableListView = (ListView)dvbs_unicable_user_list.findViewById(R.id.dvbs_sub_list); 
		
		UnicableListView.setAdapter(new DvbsUnicableUserDefineAdapter(this,dataList));

		UnicableListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				showEditUnicableFreDialog(arg1,arg2);
			}
        	    
        });
		
		diaBuilder.setView(dvbs_unicable_user_list);
		diaBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
				setUnicableUserDefinedData();
				/*
				unicable_setting.setUbFreqs(usfs);
				mDvb.setUnicableSetting(unicable_setting);
				*/
				/*
				dialog.dismiss();
              }
          });
       diaBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
                  }
              });

		AlertDialog alert = diaBuilder.create();
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
								
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							}         
							});	

		alert.show();	
		
	}
	*/

	private Dialog mDialog=null;
	private void showUnicableConfigEditDia(){
		ContentValues values=null;
	  	mDialog = new AlertDialog(this){
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
		mDialog.setContentView(R.layout.dvbs_unicable_list_dia);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.dvbs_unicable_user_define));

		usfs = new int[8];
		List<String> dataList = getUnicableUserDefinedData();
		ListView UnicableListView = (ListView)window.findViewById(R.id.dvbs_sub_list); 
		
		UnicableListView.setAdapter(new DvbsUnicableUserDefineAdapter(this,dataList));

		UnicableListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				showEditUnicableFreDialog(arg1,arg2);
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
					setUnicableUserDefinedData();
					/*
					unicable_setting.setUbFreqs(usfs);
					mDvb.setUnicableSetting(unicable_setting);
					*/
				
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


	public void refreshListData(){
		((DvbsUnicableAdapter)ListView_settings.getAdapter()).notifyDataSetChanged();
	}

	private class DvbsUnicableAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			
			ImageView icon;
			TextView text;
			ImageButton  iboolean;
			ImageView icon1;
			TextView   info; 
		}
	
		public DvbsUnicableAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
		}

		public int getCount() {
			//return listItems.length;
			return 3;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			if (!getUnicableSwitchStatus()){
				if (position>=1) {
					return false;
				}
			}	
			return super.isEnabled(position);
		}
	
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			holder.info.setTextColor(Color.YELLOW);
			/*
			DVBUnicableSetting setting = getUnicableSetting();
			if (setting == null) {
				Log.d(TAG, "Cannot get unicable setting from service");
				return convertView;
			}
			*/
			boolean unicable_on = getUnicableSwitchStatus();
			
			if(unicable_on==false){  
				if (position>=1){
					holder.text.setTextColor(Color.DKGRAY);
					holder.icon1.setVisibility(View.INVISIBLE); 
				}	
			}	
			else{
				holder.text.setTextColor(Color.WHITE);
			}		

			switch(position){
				 case 0:
				 	holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_unicable_switch);
					if (unicable_on==true){			   
						//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
						holder.info.setText(R.string.on);
					}
					else
					{
						//holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
						holder.info.setText(R.string.off);
					}	  
					
					break;
				case 1:
					holder.icon.setImageBitmap(mIcon2);				
					holder.info.setVisibility(View.INVISIBLE);	
					if (unicable_on==true){	
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.VISIBLE);	
						//holder.icon1.setBackgroundResource(R.drawable.pull_right_1); 
						
						int band = getUserBand();
						if(band>=0&&band<=7){
							holder.info.setText("LNB"+String.valueOf(band+1));
						}
						else
							holder.info.setText("LNB1");
					}
					else{
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
					}
					
					holder.text.setText(R.string.dvbs_unicable_user_band);
					break;
				case 2:
					holder.icon.setImageBitmap(mIcon3);				
					holder.info.setVisibility(View.INVISIBLE);	
					if (unicable_on==true){	
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
						//holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
					}
					else{
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
					}
					holder.text.setText(R.string.dvbs_unicable_user_define);
					break;
			}
			  
			return convertView;
		}
	}


	private static class DvbsUnicableUserDefineAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private Context cont;
		private List<String> listItems;
		private String[] DATA;

	 	static class ViewHolder {			
			TextView text;
			TextView  fre;
		}
	
		public DvbsUnicableUserDefineAdapter(Context context,List<String> list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);

			DATA = new String[8];
			DATA[0]= cont.getResources().getString(R.string.dish_setup_conf_lnb_1);			
			DATA[1]= cont.getResources().getString(R.string.dish_setup_conf_lnb_2);
			DATA[2]= cont.getResources().getString(R.string.dish_setup_conf_lnb_3);
			DATA[3]= cont.getResources().getString(R.string.dish_setup_conf_lnb_4);
			DATA[4]= cont.getResources().getString(R.string.dish_setup_conf_lnb_5);
			DATA[5]= cont.getResources().getString(R.string.dish_setup_conf_lnb_6);
			DATA[6]= cont.getResources().getString(R.string.dish_setup_conf_lnb_7);
			DATA[7]= cont.getResources().getString(R.string.dish_setup_conf_lnb_8);
			
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
				convertView = mInflater.inflate(R.layout.dvbs_unicable_list_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.fre = (TextView)convertView.findViewById(R.id.edit_fre);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			
			holder.text.setText(DATA[position]);
			holder.fre.setTextColor(Color.YELLOW);
			holder.fre.setText(listItems.get(position));
			  
			return convertView;
		}
	}

	private int getTTXRegionIndex(String region){
		String  ttx_region_str_arry[] = getResources().getStringArray(R.array.teletext_region_lauguage);
		for(int i=0;i<ttx_region_str_arry.length;i++){
			if(region.equals(ttx_region_str_arry[i])){
				return i;
			}	
		}

		return 0; 
	}


	public void showTTXRegionDialog(TextView v){
		final TextView info_cur = v;
		String region = mDTVSettings.getTeletextRegion(); 
		int pos = getTTXRegionIndex(region);
		new SingleChoiseDialog(DTVSettingsMenu.this,ttx_region_str_arry,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.ttx_region));
			}
			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				info_cur.setText(ttx_region_str_arry[which]);
				mDTVSettings.setTeletextRegion(ttx_region_str_arry[which]);
			}
		};
	}

	public void showAudioTrackDialog(TextView v){
		final TextView info_cur = v;
		int mode = mDTVSettings.getAudioTrack();
		int pos = 0;
		if(mode==0){
			pos = 0;
		}
		else  if(mode==1){
			pos = 1;
		}
		else  if(mode==2){
			pos = 2;
		}

		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "Left", "Right","Stereo"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.audio_track));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText(R.string.left);
					//setAudioTrack("l");
					break;
				case 1:
					info_cur.setText(R.string.right);
					//setAudioTrack("r");
					break;
				case 2:
					info_cur.setText(R.string.stereo);
					//setAudioTrack("s");
					break;	
				}	
			}
		};						
	}

	private int getIndexLanguage(String lan){
		String item_639_2[] = getResources().getStringArray(R.array.lauguage_iso_639_2);
		for(int i=0;i<item_639_2.length;i++){
			if(lan.equals(item_639_2[i]))
				return i;
		}
		
		return 0;
	}

	private String getLanguage(int index){
		String item[] = getResources().getStringArray(R.array.settings_lauguage);
		return item[index];
	}

	public void showAudioLanguageDialog(TextView v){
		final TextView info_cur = v;
		String lan = mDTVSettings.getAudLanguage();
		final String item[] = getResources().getStringArray(R.array.settings_lauguage);
		final String item_639_2[] = getResources().getStringArray(R.array.lauguage_iso_639_2);
		int pos = getIndexLanguage(lan);
	
		new SingleChoiseDialog(DTVSettingsMenu.this,item,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.av_settings_content)[2]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
					mDTVSettings.setAudLanguage(item_639_2[which]);
					info_cur.setText(item[which]);
			}
		};						
	}
	
	public void showSubtitleLanguageDialog(TextView v){
		final TextView info_cur = v;
		String lan = mDTVSettings.getSubtitleLanguage();
		final String item[] = getResources().getStringArray(R.array.settings_lauguage);
		final String item_639_2[] = getResources().getStringArray(R.array.lauguage_iso_639_2);
		int pos = getIndexLanguage(lan);
	
		new SingleChoiseDialog(DTVSettingsMenu.this,item,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.av_settings_content)[1]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
					mDTVSettings.setSubtitleLanguage(item_639_2[which]);
					info_cur.setText(item[which]);
			}
		};						
	}
	
	public void showScreenTypeDialog(TextView v){
		final TextView info_cur = v;
		int mode = mDTVSettings.getScreenMode();
		int pos = 0;
		if(mode==0){
			pos = 2;
		}
		else  if(mode==2){
			pos = 0;
		}
		else  if(mode==3){
			pos = 1;
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "4:3","16:9","auto"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.screen_type));
			}
			
			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					case 0:
						info_cur.setText(R.string.type_4_3);						
						mDTVSettings.setScreenMode(2);
						break;
					case 1:
						info_cur.setText(R.string.type_16_9);
						mDTVSettings.setScreenMode(3);
						break;
					case 2:
						info_cur.setText(R.string.auto);
						mDTVSettings.setScreenMode(0);
						break;	
				}
			}
		};		
	}
	
	public void showFactorySureDialog(){
		new SureDialog(DTVSettingsMenu.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.sure_factory_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(){
				mDTVSettings.factoryReset();
				Intent pickerIntent = new Intent();
				pickerIntent.setClass(DTVSettingsMenu.this, DTVPlayer.class);
 		        startActivity(pickerIntent);
				DTVSettingsMenu.this.finish();
			}
		};
	}

	private void showProgramManagerPasswordDialog(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				DTVStartProgramManager();
				DTVSettingsMenu.this.finish();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}


	private void showPasswordDialog(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				showFactorySureDialog();	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}

	private void showPvrManagerPasswordDialog(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
					Intent Intent_pvr_manager = new Intent();
					Intent_pvr_manager.setClass(DTVSettingsMenu.this,DTVPvrManager.class);
					startActivity(Intent_pvr_manager);
					DTVSettingsMenu.this.finish();	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};

	}


	private void showTimeshiftingPasswordDialog(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				if(isHaveExternalStorage()){
					Intent Intent_timeshifting = new Intent();
					Intent_timeshifting.setClass(DTVSettingsMenu.this,DTVTimeshifting.class);
					startActivity(Intent_timeshifting);
					DTVSettingsMenu.this.finish();
				}
				else{
					if(toast!=null)
						toast.cancel(); 
					toast = Toast.makeText(
					DTVSettingsMenu.this,
					R.string.check_usb_device,
					Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}
	

	private void parentalrating_set(TextView v){
		final TextView info_cur = v;
		
		int pr = mDTVSettings.getParentalRating();
		int pos = 0;

		if(pr>=4 && pr<=18)
			pos = pr-3;
		else if(pr>18)
			pos = 15;
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{"all","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.parental_rating_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				if(which==0){
					info_cur.setText(R.string.all);
					mDTVSettings.setParentalRating(0);
				}else{
					info_cur.setText(which+3+"");
					mDTVSettings.setParentalRating(which+3);
				}
				mDTVSettings.forceParentalRatingCheck();
			}
		};		
	}

	public void showTimeshiftingTimeSettingDialog(TextView v){
		final TextView info_cur = v;
		int pos = 0;
		int time = mDTVSettings.getTimeShiftingDuration();
		switch(time){
			case 600:
				pos=0;
				break;
			case 30*60:
				pos=1;
				break;
			case 60*60:
				pos=2;
				break;	
		}

		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "10 Min","30 Min","60 Min"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshift_time_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					//pos = which;
					case 0:
						info_cur.setText("10 Min");
						mDTVSettings.setTimeShiftingDuration(10*60);
						break;
					case 1:
						info_cur.setText("30 Min");
						mDTVSettings.setTimeShiftingDuration(30*60);
						break;
					case 2:
						info_cur.setText("60 Min");
						mDTVSettings.setTimeShiftingDuration(60*60);
						break;	
				}
			}
		};				
	}

	public void showParentalRatingPasswordDialog(TextView v){
		final TextView info_cur = v;
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				parentalrating_set(info_cur);		
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
							DTVSettingsMenu.this, 
				    		R.string.invalid_password,
				    		Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};

	}	
	
	public void showRcallSetDialog(TextView v){
		final TextView info_cur = v;
		int pos = 0;
		int n = DTVPlayergetRecallNumber();
		switch(n){
			case 1:
				pos=0;
				break;
			case 3:
				pos=1;
				break;
			case 5:
				pos=2;
				break;	
		}

		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "1","3","5"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.system_settings_content)[0]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					//pos = which;
					case 0:
						info_cur.setText("1");
						DTVPlayerSetRecallNumber(1);
						break;
					case 1:
						info_cur.setText("3");
						DTVPlayerSetRecallNumber(3);
						break;
					case 2:
						info_cur.setText("5");
						DTVPlayerSetRecallNumber(5);
						break;	
				}
			}
		};				
	}

	private void showSetPasswordDialog(){
		new PasswordSettingDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}

	public void showEnterPasswordSettingDialog(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				showSetPasswordDialog();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
								DTVSettingsMenu.this, 
					    		R.string.invalid_input,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
			}
			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}

	public void showAntennaSourceSettingDialog(TextView v){
		final TextView info_cur = v;
		int pos = 0;
		String antenna_source = mDTVSettings.getAtscAntennaSource();
		if(antenna_source.equals("air")){
			pos = 0;
		}
		else{
			pos = 1;
		} 
			
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "Air","Cable"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.timeshift_time_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					case 0:
						info_cur.setText("Air");
						mDTVSettings.setAtscAntennaSource("air");
						break;
					case 1:
						info_cur.setText("Cable");
						mDTVSettings.setAtscAntennaSource("cable");
						break;
				}
			}
		};				
	}

	public void showSortByDialog(TextView v){
		final TextView info_cur = v;
		
		int mode = 0;// mDTVSettings.getScreenMode();
		int pos = 0;
		if(mode==0){
			pos = 2;
		}
		else  if(mode==2){
			pos = 0;
		}
		else  if(mode==3){
			pos = 1;
		}

		final String DATA[] = getResources().getStringArray(R.array.sort_by_content);
		
		new SingleChoiseDialog(DTVSettingsMenu.this,DATA,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.screen_type));
			}
			
			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
					case 0:
						info_cur.setText(DATA[0]);						
						//mDTVSettings.setScreenMode(2);
						break;
					case 1:
						info_cur.setText(DATA[1]);
						//mDTVSettings.setScreenMode(3);
						break;
					case 2:
						info_cur.setText(DATA[2]);
						//mDTVSettings.setScreenMode(0);
						break;	
					case 3:
						info_cur.setText(DATA[4]);
						break;
				}
			}
		};		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	    //reset_timer();
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_DPAD_DOWN:			
				if(cur_select_item== ListView_settings.getCount()-1)
			    	ListView_settings.setSelection(0); 			
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			  if(cur_select_item== 0)
				ListView_settings.setSelection(ListView_settings.getCount()-1); 
					switch(button_status){
						case BUTTON_PROGRAM:
							//button_search.setBackgroundResource(R.drawable.search2);	
							button_program.requestFocus();
							break;
						case BUTTON_SEARCH:	
							button_search.requestFocus();
							break;
						case BUTTON_SYSTEM:
							button_system.requestFocus();
							break;
						case BUTTON_AV_SETTING:
							button_av_setting.requestFocus();
							break;
					}
					
				break;
			case KeyEvent.KEYCODE_BACK:	
				Intent in = new Intent();
				in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
				//setResult(RESULT_OK,null);
				DTVSettingsMenu.this.startActivity(in);	
				DTVSettingsMenu.this.finish();
				break;
		}
		return super.onKeyDown(keyCode, event);
	}	


	private void DTVSetting_GotoDTVScan(){
		Intent Intent_scan = new Intent();

		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		Log.d(TAG, "region = " + region);
		
		if(region.contains("DVB-T")){
			Log.d(TAG, "goto DTVScanDVBT");
			Intent_scan.setClass(DTVSettingsMenu.this, DTVScanDVBT.class);
		}
		else if(region.contains("ATSC")){
			Log.d(TAG, "goto DTVScanATSC");
			Intent_scan.setClass(DTVSettingsMenu.this, DTVScanATSC.class);
		}
		else if(region.contains("DVBS"))
		{
			Log.d(TAG, "goto DTVScanDVBS");
			Intent_scan.setClass(DTVSettingsMenu.this, DTVScanDVBS.class);
		}	
		else if(region.contains("DVB-C"))
		{
			Log.d(TAG, "goto DTVScanDVBC");
			Intent_scan.setClass(DTVSettingsMenu.this, DTVScanDVBC.class);				
		}		
		
		startActivityForResult(Intent_scan, 1);	
		
	}

	private void DTVStartEPG(){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("db_id", DTVPlayerGetCurrentProgramID());
		intent.putExtras(bundle);
		intent.setClass(this, DTVEpg.class);
		startActivityForResult(intent, 11);	
		onHide();	
	}

	private void DTVStartProgramManager(){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("db_id", DTVPlayerGetCurrentProgramID());
		intent.putExtras(bundle);
		intent.setClass(this, DTVProgramEdit.class);
		startActivityForResult(intent, 11);	
		onHide();	
	}

	private void DTVStartVChip(){
		Intent intent = new Intent();
		intent.setClass(this, DTVVChip.class);
		startActivityForResult(intent, 11);	
		onHide();	
	}
	private void DTVSetting_GotoDTVVChip(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				DTVStartVChip();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsMenu.this, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

			public boolean onDealUpDownKey(){
				return false;
			}
		};
	}

	private void DTVSetting_GotoDTVCC(){
		Intent intent = new Intent();
		intent.setClass(this, DTVCloseCaption.class);
		startActivityForResult(intent, 11);	
		onHide();
	}

	private  void onHide(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.INVISIBLE);
	} 
	
	private void onShow(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.VISIBLE);
	}
				
}
