package com.amlogic.DTVPlayer;

import android.util.Log;
import android.os.*;
import com.amlogic.tvutil.TVMessage;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVProgram;
import com.amlogic.tvutil.TVProgramNumber;
import com.amlogic.tvactivity.TVActivity;
import com.amlogic.tvutil.TVChannelParams;
import com.amlogic.tvutil.TVScanParams;
import com.amlogic.tvutil.TVConst;
import com.amlogic.tvutil.TVRegion;

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
import android.os.SystemProperties;
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
	private boolean mMidUi = false;
	private Bundle bundle = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (SystemProperties.get("ro.product.brand", "").equals("MID")) {
			mMidUi = true;
			setContentView(R.layout.mid_dtv_settings_menu);
		} else {
			mMidUi = false;
			setContentView(R.layout.dtv_settings_menu);
		}
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
		bundle = this.getIntent().getExtras();
		if(bundle!=null){	
			if(bundle.containsKey("activity_tag")){
				if (bundle.getString("activity_tag").equals("play_program")){
					Log.d(TAG,">>>playValid<<<");
					playValid();
				}
			}
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

		findViewById(R.id.return_icon).setOnClickListener(
			new View.OnClickListener(){	  
				public void onClick(View v) {		
					// TODO Auto-generated method stub	
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					//setResult(RESULT_OK,null);
					DTVSettingsMenu.this.startActivity(in);	
					DTVSettingsMenu.this.finish();
				}
			}
		);
				
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
		
		Bundle bundle = this.getIntent().getExtras();
		int menuId = R.string.setting_menu_search;
		if (bundle != null)
			menuId = bundle.getInt("menu");
		switch (menuId) {
			case R.string.setting_menu_program:
				button_program.requestFocus();
				ProgramItem_Init();
				break;
			case R.string.setting_menu_search:
				button_search.requestFocus();
				SearchItem_Init();
				break;
			case R.string.setting_menu_system:
				button_system.requestFocus();
				SystemItem_Init();
				break;
			case R.string.setting_menu_av:
				button_av_setting.requestFocus();
				AVItem_Init();
				break;
			default:
				button_search.requestFocus();
				SearchItem_Init();
				break;	
		}
		
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
	private SearchDvbtItemAdapter mySearchDvbtItemAdapter=null;
	private SearchDvbcItemAdapter mySearchDvbcItemAdapter=null;
	private SearchATSCItemAdapter mySearchATSCItemAdapter=null;
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
		String region = null;
		
		
		try {
			region = mDTVSettings.getScanRegion();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		Log.d(TAG, "region = " + region);
		
		if(region.contains("DVB-T")||region.contains("ISDBT")){
			Log.d(TAG, "goto DTVScanDVBT");
			button_status = BUTTON_SEARCH;  
			DATA = getResources().getStringArray(R.array.search_settings_content_dvbt);
			//listview
			ListView_settings = (ListView)findViewById(R.id.settings_list);
			mySearchDvbtItemAdapter = new SearchDvbtItemAdapter(this,DATA);
			ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
			ListView_settings.setOnKeyListener(new listOnKeyListener());
			ListView_settings.setOnItemClickListener(mSearchDvbtOnItemClickListener);
			ListView_settings.setAdapter(mySearchDvbtItemAdapter);
		}
		else if(region.contains("ATSC")){
			Log.d(TAG, "goto DTVScanATSC");

			button_status = BUTTON_SEARCH;  
			DATA = getResources().getStringArray(R.array.search_settings_content_atsc);
			//listview
			ListView_settings = (ListView)findViewById(R.id.settings_list);
			mySearchATSCItemAdapter = new SearchATSCItemAdapter(this,DATA);
			ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
			ListView_settings.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				switch(keyCode){
					case KeyEvent.KEYCODE_BACK:
						if (event.getAction() == KeyEvent.ACTION_DOWN){
							if(dtvscanatsc_scan_mode==DTVSCANATSC_SETTING_MANU_SCAN_MODE){
								dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_SCAN_MODE;
								SearchItem_Init();
								return true;
							}
						}
				} 
				return false;
			}
		});
			ListView_settings.setOnItemClickListener(mSearchATSCOnItemClickListener);
			ListView_settings.setAdapter(mySearchATSCItemAdapter);
		}
		else if(region.contains("DVBS"))
		{
			Log.d(TAG, "goto DTVScanDVBS");
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
		else if(region.contains("DVB-C"))
		{
			Log.d(TAG, "goto DTVScanDVBC");
			button_status = BUTTON_SEARCH;  
			DATA = getResources().getStringArray(R.array.search_settings_content_dvbc);
			//listview
			ListView_settings = (ListView)findViewById(R.id.settings_list);
			mySearchDvbcItemAdapter = new SearchDvbcItemAdapter(this,DATA);
			ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
			ListView_settings.setOnKeyListener(new listOnKeyListener());
			ListView_settings.setOnItemClickListener(mSearchDvbcOnItemClickListener);
			ListView_settings.setAdapter(mySearchDvbcItemAdapter);			
		}	

		
		
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
			  	 //onShow();
				 switch(p){	
				    case 0:
				 	//onShow();   	
				    	break;
				    case 1:
				    	//onShow();
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

			if (mMidUi) {
				int count = ((AvItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);
					DTVSettingsMenu.this.finish();
					return;
				}
			}

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
				case 3:
					if(mDTVSettings.getBlackoutPolicyConfig()==1)
				    	{
				    		//image_cur.setBackgroundResource(R.drawable.select_round_2);
				    		info_cur.setText(R.string.on);
						mDTVSettings.setBlackoutPolicyConfig(0);
						setBlackoutPolicy(0);
				    	}
				    	else
				    	{
				    		//image_cur.setBackgroundResource(R.drawable.select_round_1);
				    		info_cur.setText(R.string.off);
						mDTVSettings.setBlackoutPolicyConfig(1);
						setBlackoutPolicy(1);
				    	}	
					break;
				case 4:
					showInforbarShowTimeDialog(info_cur);
					break;
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
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
				holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			holder.info.setVisibility(View.VISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			switch(position){
			
		     case 0://SETTINGS_SCREEN_TYPE:
				 //int mode= mDTVSettings.getScreenMode();	
				int mode = DTVGetScreenMode();
				int pos = 0;
				final int step = 5;
				if(mode==1){
					pos = 0;
				}
				else {
					pos = mode-step;
				}
				 
				 switch(pos){
					case 0:
						holder.info.setText(R.string.full_screen);						
						break;
					case 1:
						holder.info.setText(R.string.type_4_3_IGNORE);						
						break;
					case 2:
						holder.info.setText(R.string.type_4_3_LETTER_BOX);
					break;
					case 3:
						holder.info.setText(R.string.type_4_3_PAN_SCAN);
						break;	
					case 4:
						holder.info.setText(R.string.type_4_3_COMBINED);						
						break;
					case 5:
						holder.info.setText(R.string.type_16_9_IGNORE);						
						break;
					case 6:
						holder.info.setText(R.string.type_16_9_LETTER_BOX);						
						break;
					case 7:
						holder.info.setText(R.string.type_16_9_PAN_SCAN);						
						break;
					case 8:
						holder.info.setText(R.string.type_16_9_COMBINED);						
						break;
					
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
			case 3://blackout policy:
				{
					//holder.icon.setImageBitmap(mIcon1);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					if (mDTVSettings.getBlackoutPolicyConfig()==1){			   
						//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
						holder.info.setText(R.string.off);
					}
					else{
						//holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
						holder.info.setText(R.string.on);
					}	  
				}
		  		break; 
			case 4:
				{
					
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					holder.info.setText(String.valueOf(mDTVSettings.getInforBarShowTime())+"S");
				}
				break;
		  }

		  return convertView;
		}
	}	


	private AdapterView.OnItemClickListener mProgramOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);

			if (mMidUi) {
				int count = ((ProgramItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);	
					DTVSettingsMenu.this.finish();
					return;
				}
			}

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
					/*	
					case 2:
						showSortByDialog(info_cur);
						break;
					*/	
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
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
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

			if (mMidUi) {
				int count = ((SearchItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);
					DTVSettingsMenu.this.finish();
					return;
				}
			}

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

	/*****************DVBT SCAN***********************/

	private DvbtManualScanAdapter myDvbtManualScanAdapter=null;
	private void DTVDvbtManualScanConfig_UIInit(){
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myDvbtManualScanAdapter = new DvbtManualScanAdapter(this);
		ListView_settings.setAdapter(myDvbtManualScanAdapter);
		ListView_settings.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				switch(keyCode){
					case KeyEvent.KEYCODE_BACK:
						if (event.getAction() == KeyEvent.ACTION_DOWN)
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

				if (mMidUi) {
					int count = ((DvbtManualScanAdapter)((ListView)arg0).getAdapter()).getCount();
					if (arg2 == count - 1) {
						Intent in = new Intent();
						in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
						DTVSettingsMenu.this.startActivity(in);
						DTVSettingsMenu.this.finish();
						return;
					}
				}

				/*
				DVBUnicableSetting setting = getUnicableSetting();
				if (setting == null) {
					Log.d(TAG, "Cannot get unicable setting from service");
					return;
				}
				*/
				switch(arg2){
					case 0:    //scan mode
						showDvbtScanModeDialog(info);
						break;
					case 1: 	//scan band	
						if(myDvbtManualScanAdapter.isEnabled(arg2))
							showDvbtScanBandDialog(info);
						break;
					case 2:  //channle number
						if(myDvbtManualScanAdapter.isEnabled(arg2))
							showDvbtScanChannelNumberDialog(info);
						break;
					case 3:  //fre
						if(myDvbtManualScanAdapter.isEnabled(arg2))
							showDvbtEditFreDialog(info,0);
						break;
					case 4:  //bandwidth
						if(myDvbtManualScanAdapter.isEnabled(arg2))
							showDvbtScanBandwidthDialog(info);	
						break;
					case 5:
						showSignalCheckDialog();
						break;
					case 6:
						Intent intent_scan= new Intent();
						intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

						Bundle bundle_scan_dvbt = new Bundle();	
						bundle_scan_dvbt.putString("scan-mode","dvbt-manual-scan");
						bundle_scan_dvbt.putInt("scan-fre",mDTVSettings.getDvbtScanFrequency());
						bundle_scan_dvbt.putInt("scan-band-width",mDTVSettings.getDvbtScanBandwidth());
							
						intent_scan.putExtras(bundle_scan_dvbt);
						//startActivityForResult(intent_scan,1);	
						startActivity(intent_scan);	
						DTVSettingsMenu.this.finish();
						break;
				}
			}	
		});	
	}

	private DvbcManualScanAdapter myDvbcManualScanAdapter=null;
	private void DTVDvbcManualScanConfig_UIInit(){
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myDvbcManualScanAdapter = new DvbcManualScanAdapter(this);
		ListView_settings.setAdapter(myDvbcManualScanAdapter);
		ListView_settings.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {					
				switch(keyCode){
					case KeyEvent.KEYCODE_BACK:
						if (event.getAction() == KeyEvent.ACTION_DOWN)
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

				if (mMidUi) {
					int count = ((DvbcManualScanAdapter)((ListView)arg0).getAdapter()).getCount();
					if (arg2 == count - 1) {
						Intent in = new Intent();
						in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
						DTVSettingsMenu.this.startActivity(in);
						DTVSettingsMenu.this.finish();
						return;
					}
				}

				/*
				DVBUnicableSetting setting = getUnicableSetting();
				if (setting == null) {
					Log.d(TAG, "Cannot get unicable setting from service");
					return;
				}
				*/
				switch(arg2){
					case 0:    //scan mode
						showDvbtScanModeDialog(info);
						break;
					case 1: 	//scan band	
						if(myDvbcManualScanAdapter.isEnabled(arg2))
							showDvbtScanBandDialog(info);
						break;
					case 2:  //channle number
						if(myDvbcManualScanAdapter.isEnabled(arg2))
							showDvbtScanChannelNumberDialog(info);
						break;
					case 3:  //fre
						if(myDvbcManualScanAdapter.isEnabled(arg2))
							showDvbtEditFreDialog(info,0);
						break;
					case 4:  //bandwidth
						if(myDvbcManualScanAdapter.isEnabled(arg2))
							showDvbcScanModulationDialog(info);	
						break;
					case 5:   //symbol	
						if(myDvbcManualScanAdapter.isEnabled(arg2))
							showDvbcSymboleDialog(info,0);
						break;
					case 6:
						showSignalCheckDialog();
						break;
					case 7:
						Intent intent_scan= new Intent();
						intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

						Bundle bundle_scan_dvbc = new Bundle();	
						bundle_scan_dvbc.putString("scan-mode","dvbc-manual-scan");
						bundle_scan_dvbc.putInt("scan-fre",mDTVSettings.getDvbtScanFrequency());
						bundle_scan_dvbc.putInt("scan-modulation",mDTVSettings.getDvbcModulation());
						bundle_scan_dvbc.putInt("scan-symbole",mDTVSettings.getDvbcSymbole());	
						intent_scan.putExtras(bundle_scan_dvbc);
						//startActivityForResult(intent_scan,1);	
						startActivity(intent_scan);	
						DTVSettingsMenu.this.finish();
						break;
				}
			}	
		});	
	}


	TVChannelParams[] dvbsanatsc_channel_number_list=null; 
	public void DTVAtscManualScanConfig_Data_Init(){
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
			//region = mDTVSettings.getCurrentRegion();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		TVRegion mTVRegion = TVRegion.selectByName(this, region);
		if(mTVRegion == null){
			Log.d(TAG,"***mTVRegion is NULL***");
			return;
		}
		dvbsanatsc_channelallbandlist = mTVRegion.getChannelParams();

		if(dvbsanatsc_channelallbandlist != null)
		{
			mDTVSettings.setAtscScanFrequency(dvbsanatsc_channelallbandlist[0].frequency/1000);
						
			dvbsanatsc_channel_number_list = new TVChannelParams[dvbsanatsc_channelallbandlist.length];
			int temp = 0;
			for(int i=0;i<dvbsanatsc_channelallbandlist.length;i++){
				if(dvbsanatsc_channelallbandlist[i].frequency/1000!= 0){
					dvbsanatsc_channel_number_list[temp]=TVChannelParams.dvbtParams(dvbsanatsc_channelallbandlist[i].frequency, dvbsanatsc_channelallbandlist[i].modulation);			
					temp++;
				}
				
			}
			
		}
	}

	
	TVChannelParams[] dvbsandvbt_channelallbandlist=null;
	TVChannelParams[] dvbsandvbt_channel_number_list=null; 

	public void DTVDvbtManualScanConfig_Data_Init(){
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
			//region = mDTVSettings.getCurrentRegion();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		TVRegion mTVRegion = TVRegion.selectByName(this, region);
		if(mTVRegion == null){
			Log.d(TAG,"***mTVRegion is NULL***");
			return;
		}
		dvbsandvbt_channelallbandlist = mTVRegion.getChannelParams();

		if(dvbsandvbt_channelallbandlist != null)
		{
			/*
			mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[0].frequency/1000);
			mDTVSettings.setDvbtScanBandwidth(dvbsandvbt_channelallbandlist[0].bandwidth);
			*/

			mDTVSettings.setDvbtScanFrequency(mDTVSettings.getDvbtScanFrequency());
			mDTVSettings.setDvbcModulation(mDTVSettings.getDvbtScanBandwidth());
			mDTVSettings.setDvbtScanBand(0);
			
			int channel_count=0;
			//dvbsandvbt_channel_number_list = new String[dvbsandvbt_channelallbandlist.length];
			if(mDTVSettings.getDvbtScanBand()==0){
				
				for(int m=0;m<dvbsandvbt_channelallbandlist.length;m++){
					if(dvbsandvbt_channelallbandlist[m].frequency/1000<300000){
						channel_count++;
					}
				}
				if(channel_count==0){
					return;
				}
				
				dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
				int temp = 0;
				for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
					if(dvbsandvbt_channelallbandlist[i].frequency/1000<300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbtParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].bandwidth);			
					}
					
				}
			}
			else{
				channel_count=0;
				for(int n=0;n<dvbsandvbt_channelallbandlist.length;n++){
					if(dvbsandvbt_channelallbandlist[n].frequency/1000>=300000){
						channel_count++;
					}
				}

				if(channel_count==0){
					mDTVSettings.setDvbtScanBand(0);
					DTVDvbtManualScanConfig_Data_Init();
				}
				else{
					dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
					int temp = 0;
					for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
						if(dvbsandvbt_channelallbandlist[i].frequency/1000>=300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbtParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].bandwidth);			
						temp++;
						}					
					}
				}
			}	
			
		}
	}	

	public void DTVDvbcManualScanConfig_Data_Init(){
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
			//region = mDTVSettings.getCurrentRegion();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		TVRegion mTVRegion = TVRegion.selectByName(this, region);
		if(mTVRegion == null){
			Log.d(TAG,"***mTVRegion is NULL***");
			return;
		}
		dvbsandvbt_channelallbandlist = mTVRegion.getChannelParams();

		if(dvbsandvbt_channelallbandlist != null)
		{
			/*mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[0].frequency/1000);
			mDTVSettings.setDvbcModulation(dvbsandvbt_channelallbandlist[0].modulation);
			mDTVSettings.setDvbcSymbole(dvbsandvbt_channelallbandlist[0].symbolRate);
			*/
			mDTVSettings.setDvbtScanFrequency(mDTVSettings.getDvbtScanFrequency());
			mDTVSettings.setDvbcModulation(mDTVSettings.getDvbcModulation());
			mDTVSettings.setDvbcSymbole(mDTVSettings.getDvbcSymbole());
			
			mDTVSettings.setDvbtScanBand(0);
			
			int channel_count=0;
			//dvbsandvbt_channel_number_list = new String[dvbsandvbt_channelallbandlist.length];
			if(mDTVSettings.getDvbtScanBand()==0){
				
				for(int m=0;m<dvbsandvbt_channelallbandlist.length;m++){
					if(dvbsandvbt_channelallbandlist[m].frequency/1000<300000){
						channel_count++;
					}
				}
				if(channel_count==0){
					mDTVSettings.setDvbtScanBand(1);
					return;
				}
				
				dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
				int temp = 0;
				for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
					if(dvbsandvbt_channelallbandlist[i].frequency/1000<300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbcParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].modulation,dvbsandvbt_channelallbandlist[i].symbolRate);			
					}
					
				}
			}
			else{
				channel_count=0;
				for(int n=0;n<dvbsandvbt_channelallbandlist.length;n++){
					if(dvbsandvbt_channelallbandlist[n].frequency/1000>=300000){
						channel_count++;
					}
				}

				if(channel_count==0){
					mDTVSettings.setDvbtScanBand(0);
					DTVDvbcManualScanConfig_Data_Init();
				}
				else{
					dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
					int temp = 0;
					for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
						if(dvbsandvbt_channelallbandlist[i].frequency/1000>=300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbcParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].modulation,dvbsandvbt_channelallbandlist[i].symbolRate);			
						temp++;
						}					
					}
				}
			}	
			
		}
	}	


	private void DTVDvbtNitScan(){
		Intent intent_scan= new Intent();
		intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

		Bundle bundle_scan_dvbt = new Bundle();	
		bundle_scan_dvbt.putString("scan-mode","dvbt-nit-scan");
		bundle_scan_dvbt.putInt("scan-fre",mDTVSettings.getDvbtScanFrequency());
		bundle_scan_dvbt.putInt("scan-band-width",mDTVSettings.getDvbtScanBandwidth());
			
		intent_scan.putExtras(bundle_scan_dvbt);
		//startActivityForResult(intent_scan,1);	
		startActivity(intent_scan);	
		DTVSettingsMenu.this.finish();
	}

	private void DTVDvbcNitScan(){
		Intent intent_scan= new Intent();
		intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

		Bundle bundle_scan_dvbc = new Bundle();	
		bundle_scan_dvbc.putString("scan-mode","dvbc-nit-scan");
		bundle_scan_dvbc.putInt("scan-fre",mDTVSettings.getDvbtScanFrequency());
		bundle_scan_dvbc.putInt("scan-modulation",mDTVSettings.getDvbcModulation());
		bundle_scan_dvbc.putInt("scan-symbole",mDTVSettings.getDvbcSymbole());
			
		intent_scan.putExtras(bundle_scan_dvbc);
		//startActivityForResult(intent_scan,1);	
		startActivity(intent_scan);	
		DTVSettingsMenu.this.finish();
	}

	private AdapterView.OnItemClickListener mSearchDvbtOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);

			if (mMidUi) {
				int count = ((SearchDvbtItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);
					DTVSettingsMenu.this.finish();
					return;
				}
			}

			switch(position){
				case 0:   //auto scan
					{	
						Intent intent_scan= new Intent();
						intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

						Bundle bundle_scan_dvbs = new Bundle();	
						bundle_scan_dvbs.putString("scan-mode","dvbt-auto-scan");
						intent_scan.putExtras(bundle_scan_dvbs);
						//startActivityForResult(intent_scan,1);	
						startActivity(intent_scan);	
						DTVSettingsMenu.this.finish();
					}	
					break;
				case 1:   //manual scan
					{
						DTVDvbtManualScanConfig_UIInit();
						DTVDvbtManualScanConfig_Data_Init();
					}
					break;
				case 2: //NIT Scan
					DTVDvbtNitScan();
					break;
				case 3:  //area
					{
						showRegionsDialog(info_cur);
					}
					break;
				case 4:  //lcn
					if(mDTVSettings.getLCNStatus()==false)
				    	{
				    		image_cur.setBackgroundResource(R.drawable.select_round_2);
				    		info_cur.setText(R.string.on);
						mDTVSettings.setLCNStatus(true);
				    	}
				    	else
				    	{
				    		image_cur.setBackgroundResource(R.drawable.select_round_1);
				    		info_cur.setText(R.string.off);
						mDTVSettings.setLCNStatus(false);
				    	}		
					break;
			}
		}
	};

	private AdapterView.OnItemClickListener mSearchDvbcOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);

			if (mMidUi) {
				int count = ((SearchDvbcItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);
					DTVSettingsMenu.this.finish();
					return;
				}
			}

			switch(position){
				case 0:   //auto scan
					{	
						Intent intent_scan= new Intent();
						intent_scan.setClass(DTVSettingsMenu.this,DvbtScanResult.class);

						Bundle bundle_scan_dvbs = new Bundle();	
						bundle_scan_dvbs.putString("scan-mode","dvbt-auto-scan");
						intent_scan.putExtras(bundle_scan_dvbs);
						//startActivityForResult(intent_scan,1);	
						startActivity(intent_scan);
						DTVSettingsMenu.this.finish();
					}	
					break;
				case 1:   //manual scan
					{
						DTVDvbcManualScanConfig_UIInit();
						DTVDvbcManualScanConfig_Data_Init();
					}
					break;
				case 2: //NIT Scan
					DTVDvbcNitScan();
					break;
			}
		}
	};
	
	public void refreshAtscManualScanList(){
		((SearchATSCItemAdapter)ListView_settings.getAdapter()).notifyDataSetChanged();

	}
	
	

	public void refreshDvbtManualScanList(){
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}
		
		if(region.contains("DVB-T")){
			((DvbtManualScanAdapter)ListView_settings.getAdapter()).notifyDataSetChanged();
		}
		else if (region.contains("DVB-C")){
			((DvbcManualScanAdapter)ListView_settings.getAdapter()).notifyDataSetChanged();
		}
		
	}
	
	private class SearchDvbtItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
	
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public SearchDvbtItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			
		}

		public int getCount() {
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
				holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			switch(position){				
			     case 0:
				 	holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
					 holder.icon.setImageBitmap(mIcon1);
					 break;		 
			     case 1:
				 	 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
			    	 	break;	
			     case 2:
					 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
					break;
			     case 3:
					/////int n = DTVPlayergetRecallNumber();
					holder.icon.setImageBitmap(mIcon1);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
					//////////holder.info.setText(String.valueOf(n));
					String regions[]=mDTVSettings.getRegions();
					if(regions!=null){
						holder.info.setText(regions[mDTVSettings.getRegionsIndex()]);
					}
					break;
				case 4:
					{
						holder.icon.setImageBitmap(mIcon1);
						holder.info.setVisibility(View.VISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
						if (mDTVSettings.getLCNStatus()){			   
							//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
							holder.info.setText(R.string.on);
						}
						else{
							//holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
							holder.info.setText(R.string.off);
						}	  
					}
			    	break;	 
			  }

			return convertView;
		}

	}


	private class SearchDvbcItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
	
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public SearchDvbcItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			
		}

		public int getCount() {
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
				holder.text.setText(listItems[position]);
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);
			switch(position){				
			     case 0:
				 	holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
					 holder.icon.setImageBitmap(mIcon1);
					 break;		 
			     case 1:
				 	 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
			    	 	break;	
			     case 2:
					 holder.info.setVisibility(View.INVISIBLE);
					 holder.icon1.setVisibility(View.INVISIBLE);
					break;	 
			  }

			return convertView;
		}

	}


	/*******************************ATSC*********************************/
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
	//public static final int SETTINGS_SCAN_BAND = 1;
	public static final int SETTINGS_CHNO = 1;
	public static final int SETTINGS_FREQUENCY = 2;
	/*not use now. if use atsc demod need set modulation, SETTINGS_MODULATION = 4;SETTINGS_SCAN = 5;SETTINGS_MANU_MAX = 6;*/
	
	public static final int SETTINGS_SCAN = 3;
	public static final int SETTINGS_MANU_MAX = 5;
	public static final int SETTINGS_MODULATION = 6;

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
	
	private int dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_SCAN_MODE;	
	private int dvbscanatsc_area = SETTINGS_AREA_USA;
	private boolean dvbscanatsc_antennapwoer = false;
	private int dfbscanatsc_signaltype = SETTINGS_SIGNAL_CABLE;
	private int dvbscanatsc_manu_scanmode = SETTINGS_MANU_SCANMODE_FREQ;
	private int dvbscanatsc_manu_scanband = SETTINGS_MANU_SCANBAND_VHF;
	private int dvbscanatsc_manu_chno = 0;
	/*unit KHz*/
	private int dvbscanatsc_manu_freq = 474000;
	private int dvbscanatsc_manu_modulation = SETTINGS_MODULATION_QAM_AUTO;
	private int ui_dvbsanatsc_setting_list_count = SETTINGS_MAX;
	
	private AdapterView.OnItemClickListener mSearchATSCOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);
			final TextView info_cur = (TextView)v.findViewById(R.id.info);

			if (dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE) {
				if (mMidUi) {
					int count = ((SearchATSCItemAdapter)((ListView)parent).getAdapter()).getCount();
					if (position == count - 1) {
						Intent in = new Intent();
						in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
						DTVSettingsMenu.this.startActivity(in);
						DTVSettingsMenu.this.finish();
						return;
					}
				}
			}

			DTVScanATSC_SettingListItemClicked(info_cur,position);
		}
	};

	private void DTVScanATSC_SettingListItemClicked(TextView v,int position)
	{
		switch(dtvscanatsc_scan_mode)
		{
			case DTVSCANATSC_SETTING_SCAN_MODE:
				{
					switch(position)
					{
						case SETTINGS_AUTO_SCAN:
							DTVScanATSCUiScan();
							break;
						case SETTINGS_MANU_SCAN:
							DTVScanATSC_SettingListItemClickedManuScan();
							break;
						case SETTINGS_AREA:
							showATSCRegionsDialog(v);
							break;
						case SETTINGS_ANTENNA_POWER:
							break;
						case SETTINGS_SIGNAL_TYPE:
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
						/*
						case SETTINGS_MANU_SCANMODE:    //scan mode
							showAtscScanModeDialog(v);
							break;
						
						case SETTINGS_SCAN_BAND: 	//scan band	
							if(mySearchATSCItemAdapter.isEnabled(position))
								showDvbtScanBandDialog(v);
							break;
						*/	
						case 0:  //channle number
							if(mySearchATSCItemAdapter.isEnabled(position))
								showAtscScanChannelNumberDialog(v);
							break;
						case 1:  //manual scan
							DTVScanATSCUiScan();
							break;
						case 2:
							dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_SCAN_MODE;
							SearchItem_Init();
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

	private void DTVScanATSCUiScan(){
		Log.d(TAG, "DTVScanATSCUiScan");
		
		//DTVScanATSC_ScanList();
		
		if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_SCAN_MODE)
		{
			//ui_dvbsanatsc_scan_title.setText(R.string.dtvscan_scan_auto);

			DTVScanATSC_StartAutoScan();
		}
		else if(dtvscanatsc_scan_mode == DTVSCANATSC_SETTING_MANU_SCAN_MODE)
		{
			//ui_dvbsanatsc_scan_title.setText(R.string.dtvscan_scan_manual);

			DTVScanATSC_StartManuScan();
		}
	}	

	private void DTVScanATSC_StartAutoScan()
	{
		Log.d(TAG, "DTVScanATSC_StartAutoScan");
		
		Intent intent_scan= new Intent();
		intent_scan.setClass(DTVSettingsMenu.this,ATSCScanResult.class);

		Bundle bundle_scan_atsc = new Bundle();	
		bundle_scan_atsc.putString("scan-mode","atsc-auto-scan");
		intent_scan.putExtras(bundle_scan_atsc);
		startActivity(intent_scan);	
	}

	private void DTVScanATSC_StartManuScan()
	{
		Log.d(TAG, "DTVScanATSC_StartManuScan" + dvbscanatsc_manu_freq);
		
		Intent intent_scan= new Intent();
		intent_scan.setClass(DTVSettingsMenu.this,ATSCScanResult.class);
		Bundle bundle_scan_atsc = new Bundle();	
		bundle_scan_atsc.putString("scan-mode","atsc-manual-scan");
		bundle_scan_atsc.putInt("scan-fre",dvbscanatsc_manu_freq);
		bundle_scan_atsc.putInt("scan-atsc-mod",dvbscanatsc_manu_modulation);
		intent_scan.putExtras(bundle_scan_atsc);
		startActivity(intent_scan);	
	}
	
	private void DTVScanATSC_SettingListItemClickedManuFreqEdit()
	{
		LinearLayout dtvscanatsc_edit_freq_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dtvscan_edit_freq, null);      
		final EditText dtvscanatsc_edit_freq_text = (EditText)dtvscanatsc_edit_freq_layout.findViewById(R.id.dvbscan_edit_freq);

		/*  
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
		*/
	}
	
	private TVChannelParams[] dvbsanatsc_channelallbandlist = null;
	private void DTVScanATSCUiSettingManualScan(){
		Log.d(TAG, "DTVScanATSCUiSettingManualScan");
		
		dtvscanatsc_scan_mode = DTVSCANATSC_SETTING_MANU_SCAN_MODE;
		ui_dvbsanatsc_setting_list_count = SETTINGS_MANU_MAX;
		
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		TVRegion mTVRegion = TVRegion.selectByName(this, region);
		if(mTVRegion==null)
			return;

		dvbsanatsc_channelallbandlist = mTVRegion.getChannelParams();

		if(dvbsanatsc_channelallbandlist != null)
		{
			dvbscanatsc_manu_freq = dvbsanatsc_channelallbandlist[0].frequency/1000;
			dvbscanatsc_manu_modulation = dvbsanatsc_channelallbandlist[0].modulation;
			if(mDTVSettings!=null)
				mDTVSettings.setAtscScanFrequency(dvbscanatsc_manu_freq);
		}

		((SearchATSCItemAdapter)ListView_settings.getAdapter()).notifyDataSetChanged();
	}

	private void DTVScanATSC_SettingListItemClickedManuScan()
	{
		DTVScanATSCUiSettingManualScan();
	}	
	
	private class SearchATSCItemAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
	
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public SearchATSCItemAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			
		}

		public int getCount() {
			switch(dtvscanatsc_scan_mode)
			{
				case DTVSCANATSC_SETTING_SCAN_MODE:
					return mMidUi ? (listItems.length + 1) : listItems.length;
				case DTVSCANATSC_SETTING_MANU_SCAN_MODE:	
					return mMidUi ? 3 : 2;
				default:
					return mMidUi ? (listItems.length + 1) : listItems.length;

			}		
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
			holder.icon.setVisibility(View.INVISIBLE);
			holder.info.setTextColor(Color.YELLOW);

		displaytext(holder,position);
		if (mMidUi) {
			if (position == listItems.length) {
				holder.info.setVisibility(View.GONE);
				return convertView;
			}
		}
		
		switch(dtvscanatsc_scan_mode){
			case DTVSCANATSC_SETTING_SCAN_MODE:
			switch(position){
			
		     case 0:
			 	holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
				 holder.icon.setImageBitmap(mIcon1);
				 break;		 
		     case 1:
			 	 holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 break;	
			 case 2:
				/////int n = DTVPlayergetRecallNumber();
				holder.icon.setImageBitmap(mIcon1);
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.INVISIBLE);
				//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
				//////////holder.info.setText(String.valueOf(n));
				String regions[]=mDTVSettings.getATSCRegions();
				if(regions!=null){
					holder.info.setText(regions[mDTVSettings.getATSCRegionsIndex()]);
				}
				break;
			case 3:
				{
					holder.icon.setImageBitmap(mIcon1);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					
				}
		    	break;
		  }
			break;
		case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
				switch(position){
					/*
					case SETTINGS_MANU_SCANMODE:
						holder.info.setVisibility(View.VISIBLE);
						displayinfoscanmode(holder);
						break;
					
					case SETTINGS_SCAN_BAND:
						holder.info.setVisibility(View.VISIBLE);								
						displayinfoscanband(holder);

						if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
						{
							
							holder.info.setTextColor(Color.YELLOW);
						}
						else if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
						{
							holder.info.setTextColor(Color.DKGRAY);
						}
						break;
					*/	
					case 0:  //chann no
						holder.info.setVisibility(View.VISIBLE);
						dvbscanatsc_manu_freq=mDTVSettings.getAtscScanFrequency();
						displayinfoch(holder);
						holder.info.setTextColor(Color.WHITE);
						//holder.info.setTextColor(Color.DKGRAY);
						
						break;
					/*	
					case 1:  //fre
						holder.info.setVisibility(View.VISIBLE);
						//displayinfofreq(holder);
						dvbscanatsc_manu_freq=mDTVSettings.getAtscScanFrequency();
						holder.info.setText(dvbscanatsc_manu_freq + "");
						
						//if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
						//{
							//holder.info.setTextColor(Color.WHITE);
						//}
						//else if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
						{
							holder.info.setTextColor(Color.DKGRAY);
						}

						break;
						
					case SETTINGS_MODULATION:
						holder.info.setVisibility(View.VISIBLE);
						displayinfomodulation(holder);
							 
						if (dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_FREQ)
						{
							
							holder.info.setTextColor(Color.WHITE);
						}
						else if(dvbscanatsc_manu_scanmode == SETTINGS_MANU_SCANMODE_CHAN)
						{
							holder.info.setTextColor(Color.DKGRAY);
						}
						break;
					*/	
					case 1:  //mau scan
						holder.info.setVisibility(View.GONE);

						break;
					case 2:
						holder.info.setVisibility(View.GONE);
						break;
					default:
						break; 
				}
				break;
		   }

		  return convertView;
		}

		private void displaytext(ViewHolder vh, int position)
		{
			switch(dtvscanatsc_scan_mode)
			{
				case DTVSCANATSC_SETTING_SCAN_MODE:
					{
						if (mMidUi) {
							if (position != listItems.length) {
								switch(position)
								{
									case SETTINGS_AUTO_SCAN:
										vh.text.setText(R.string.dtvscan_scan_auto);
										break;
									case SETTINGS_MANU_SCAN:
										vh.text.setText(R.string.dtvscan_scan_manual);
										break;
									case SETTINGS_AREA:
										vh.text.setText(R.string.dtvscan_erea);
										break;
									/*	
									case SETTINGS_ANTENNA_POWER:
										vh.text.setText(R.string.dtvscanatsc_antennapower);
										break;
									case SETTINGS_SIGNAL_TYPE:
										vh.text.setText(R.string.dtvscanatsc_signaltype);
										break;	
									*/	
									default:
										break;
								}
							} else
								vh.text.setText(R.string.exit);
						} else {
							switch(position)
							{
								case SETTINGS_AUTO_SCAN:
									vh.text.setText(R.string.dtvscan_scan_auto);
									break;
								case SETTINGS_MANU_SCAN:
									vh.text.setText(R.string.dtvscan_scan_manual);
									break;
								case SETTINGS_AREA:
									vh.text.setText(R.string.dtvscan_erea);
									break;
								/*	
								case SETTINGS_ANTENNA_POWER:
									vh.text.setText(R.string.dtvscanatsc_antennapower);
									break;
								case SETTINGS_SIGNAL_TYPE:
									vh.text.setText(R.string.dtvscanatsc_signaltype);
									break;	
								*/	
								default:
									break;
							}
						}
					}
					break;
					
				case DTVSCANATSC_SETTING_MANU_SCAN_MODE:
					{
						switch(position)
						{
							/*
							case SETTINGS_MANU_SCANMODE:	
								vh.text.setText(R.string.dtvscan_scan_mode);
								break;
								
							case SETTINGS_SCAN_BAND:	
								vh.text.setText(R.string.dtvscan_scan_band);
								break;
							*/	
							case 0:  //chan no	
								vh.text.setText(R.string.dtvscan_channel_no);
								break;
							/*	
							case 1:	 //fre
								vh.text.setText(R.string.dtvscan_base_frequence);
								break;
								
							case SETTINGS_MODULATION:
								vh.text.setText(R.string.dtvscanatsc_modulation);
								break;
							*/	
							case 1:   //manual scan
								vh.text.setText(R.string.dtvscan_begain_search);
								break;
							case 2:
								vh.text.setText(R.string.dtvscan_scan_manual_back);
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


		private void displayinfoscanmode(ViewHolder vh){
		switch(dvbscanatsc_manu_scanmode)
		{
			case SETTINGS_MANU_SCANMODE_FREQ:	
				vh.info.setText(R.string.dtvscan_by_frequence);
				break;			
			
			case SETTINGS_MANU_SCANMODE_CHAN:	
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
			case SETTINGS_MANU_SCANBAND_VHF:	
				vh.info.setText(R.string.dtvscan_scan_band_vhf);
				break;			
			
			case SETTINGS_MANU_SCANBAND_UHF:	
				vh.info.setText(R.string.dtvscan_scan_band_uhf);
				break;

			default:
				break;
		}
	}	

	private void displayinfoch(ViewHolder vh){
		/*refresh dvbscanatsc_manu_chno and dvbscanatsc_manu_freq*/
		vh.info.setText( dvbscanatsc_manu_freq + "KHZ");
	}

	private void displayinfofreq(ViewHolder vh){			
		vh.info.setText(dvbscanatsc_manu_freq + "");
	}		

	private void displayinfomodulation(ViewHolder vh){
		switch(dvbscanatsc_manu_modulation)
		{
			case SETTINGS_MODULATION_QAM_AUTO:
				vh.info.setText(R.string.dtvscanatsc_modulation_qamauto);
				break;
			case SETTINGS_MODULATION_QAM_16:
				vh.info.setText(R.string.dtvscanatsc_modulation_qam16);
				break;
			case SETTINGS_MODULATION_QAM_32:
				vh.info.setText(R.string.dtvscanatsc_modulation_qam32);
				break;			
			case SETTINGS_MODULATION_QAM_64:
				vh.info.setText(R.string.dtvscanatsc_modulation_qam64);
				break;	
			case SETTINGS_MODULATION_QAM_128:
				vh.info.setText(R.string.dtvscanatsc_modulation_qam128);
				break;	
			case SETTINGS_MODULATION_QAM_256:
				vh.info.setText(R.string.dtvscanatsc_modulation_qam256);
				break;					
			case SETTINGS_MODULATION_VSB_8:	
				vh.info.setText(R.string.dtvscanatsc_modulation_vsb8);
				break;
			case SETTINGS_MODULATION_VSB_16:	
				vh.info.setText(R.string.dtvscanatsc_modulation_vsb16);
				break;
			default:
				break;
		}
		
	}
	}


    /*****************************************************************/
	
	
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
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
				holder.text.setText(listItems[position]);
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

			if (mMidUi) {
				int count = ((SystemItemAdapter)((ListView)parent).getAdapter()).getCount();
				if (position == count - 1) {
					Intent in = new Intent();
					in.setClass(DTVSettingsMenu.this, DTVPlayer.class);
					DTVSettingsMenu.this.startActivity(in);
					DTVSettingsMenu.this.finish();
					return;
				}
			}
			
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
						if(mDTVSettings.getScanRegion().contains("ATSC")) {
							DTVSetting_GotoDTVCC();
							onStop();
						} else {
							controlUpdate(1/*restart monitor*/, 1/*check*/, "");
						}
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
			return mMidUi ? (listItems.length + 1) : listItems.length;
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
			if (mMidUi) {
				if (position != listItems.length) {
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else
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
						if(((DvbsUnicableAdapter)ListView_settings.getAdapter()).isEnabled(arg2)){	
							int band = getUserBand();
							if(band>=0&&band<7){
								info.setText("LNB"+String.valueOf(band+2));
								setUserBand(band + 1);
							}
							else{
								info.setText("LNB1");
								setUserBand(0);
							}
						}
						break;
					case 2:
						if(((DvbsUnicableAdapter)ListView_settings.getAdapter()).isEnabled(arg2)){	
							showUnicableConfigEditDia();
						}	
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

	private class DvbtManualScanAdapter extends BaseAdapter {
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
	
		public DvbtManualScanAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
		    	listItems = context.getResources().getStringArray(R.array.search_settings_content_dvbt_manual);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
		}

		public int getCount() {
			//return listItems.length;
			return mMidUi ? (listItems.length + 1) : listItems.length;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			
			if (mDTVSettings.getDvbtScanMode()==0){    
				if (position==3||position==4) {
					return false;
				}
			}
			else if(mDTVSettings.getDvbtScanMode()==1){
				if (position==1||position==2) {
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

			if (mMidUi) {
				if (position != listItems.length) {
					holder.info.setTextColor(Color.YELLOW);
					holder.text.setTextColor(Color.WHITE);
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else {
				holder.info.setTextColor(Color.YELLOW);
				holder.text.setTextColor(Color.WHITE);
				holder.text.setText(listItems[position]);
			}

			switch(position){
				 case 0:    //scan mode
				 	holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);

					int mode = mDTVSettings.getDvbtScanMode();
					if(mode==0){
						holder.info.setText(R.string.dtvscan_by_channel);
					}
					else  if(mode==1){
						holder.info.setText(R.string.dtvscan_by_frequence);
					}
					
					break;
				case 1:  //scan band
					holder.icon.setImageBitmap(mIcon2);				
					int value = mDTVSettings.getDvbtScanBand();
					if(value==0){
						holder.info.setText(R.string.dtvscan_scan_band_vhf);
					}
					else  if(value==1){
						holder.info.setText(R.string.dtvscan_scan_band_uhf);
					}
					if(mDTVSettings.getDvbtScanMode()==1){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);	
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);	
					}	
					break;
				case 2:     //channel number
					holder.icon.setImageBitmap(mIcon3);				
					int fre = mDTVSettings.getDvbtScanFrequency();
					holder.info.setText(String.valueOf(fre)+"KHZ");
					if(mDTVSettings.getDvbtScanMode()==1){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);
					}	
						
					break;
				case 3: //frequency
					holder.icon.setImageBitmap(mIcon3);				
					int frequency=mDTVSettings.getDvbtScanFrequency();

					holder.info.setText(String.valueOf(frequency));
					if(mDTVSettings.getDvbtScanMode()==0){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);
					}	
					break;
				case 4:   //band width
					holder.icon.setImageBitmap(mIcon3);				
					holder.info.setVisibility(View.VISIBLE);
					int bandwidth=mDTVSettings.getDvbtScanBandwidth();
					switch(bandwidth){
						case TVChannelParams.BANDWIDTH_8_MHZ:
							holder.info.setText("8M");
							break;
						case TVChannelParams.BANDWIDTH_7_MHZ:
							holder.info.setText("7M");	
							break;
						case TVChannelParams.BANDWIDTH_6_MHZ:
							holder.info.setText("6M");	
							break;			
						case TVChannelParams.BANDWIDTH_AUTO:
							holder.info.setText("Auto");	
							break;
						default:
							holder.info.setText("Auto");	
							break;
					}

					if(mDTVSettings.getDvbtScanMode()==0){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
					}	
	
					break;
			}
			  
			return convertView;
		}
	}

	private class DvbcManualScanAdapter extends BaseAdapter {
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
	
		public DvbcManualScanAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
		    	listItems = context.getResources().getStringArray(R.array.search_settings_content_dvbc_manual);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
		}

		public int getCount() {
			//return listItems.length;
			return mMidUi ? (listItems.length + 1) : listItems.length;
		}

		public Object getItem(int position) {

			return position;
		}
	
		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			
			if (mDTVSettings.getDvbtScanMode()==0){    
				if (position==3||position==4||position==5) {
					return false;
				}
			}
			else if(mDTVSettings.getDvbtScanMode()==1){
				if (position==1||position==2) {
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

			if (mMidUi) {
				if (position != listItems.length) {
					holder.info.setTextColor(Color.YELLOW);
					holder.text.setTextColor(Color.WHITE);
					holder.text.setText(listItems[position]);
				} else {
					holder.text.setText(R.string.exit);
					holder.info.setVisibility(View.GONE);
					return convertView;
				}
			} else {
				holder.info.setTextColor(Color.YELLOW);
				holder.text.setTextColor(Color.WHITE);
				holder.text.setText(listItems[position]);
			}

			switch(position){
				 case 0:    //scan mode
				 	holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);

					int mode = mDTVSettings.getDvbtScanMode();
					if(mode==0){
						holder.info.setText(R.string.dtvscan_by_channel);
					}
					else  if(mode==1){
						holder.info.setText(R.string.dtvscan_by_frequence);
					}
					
					break;
				case 1:  //scan band
					holder.icon.setImageBitmap(mIcon2);				
					int value = mDTVSettings.getDvbtScanBand();
					if(value==0){
						holder.info.setText(R.string.dtvscan_scan_band_vhf);
					}
					else  if(value==1){
						holder.info.setText(R.string.dtvscan_scan_band_uhf);
					}
					if(mDTVSettings.getDvbtScanMode()==1){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);	
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);	
					}	
					break;
				case 2:     //channel number
					holder.icon.setImageBitmap(mIcon3);				
					int fre = mDTVSettings.getDvbtScanFrequency();
					holder.info.setText(String.valueOf(fre)+"KHZ");
					if(mDTVSettings.getDvbtScanMode()==1){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);
					}	
						
					break;
				case 3: //frequency
					holder.icon.setImageBitmap(mIcon3);				
					int frequency=mDTVSettings.getDvbtScanFrequency();

					holder.info.setText(String.valueOf(frequency));
					if(mDTVSettings.getDvbtScanMode()==0){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
						holder.info.setVisibility(View.INVISIBLE);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
						holder.info.setVisibility(View.VISIBLE);
					}	
					break;
				case 4:   //modulation
					holder.icon.setImageBitmap(mIcon3);				
					holder.info.setVisibility(View.VISIBLE);
					int mod=mDTVSettings.getDvbcModulation();
					switch(mod){
						case TVChannelParams.MODULATION_QAM_16:
							holder.info.setText("16QAM");
							break;
						case TVChannelParams.MODULATION_QAM_32:
							holder.info.setText("32QAM");	
							break;
						case TVChannelParams.MODULATION_QAM_64:
							holder.info.setText("64QAM");	
							break;			
						case TVChannelParams.MODULATION_QAM_128:
							holder.info.setText("128QAM");	
							break;
						case TVChannelParams.MODULATION_QAM_256:
							holder.info.setText("256QAM");	
							break;	
						default:
							holder.info.setText("Auto");	
							break;
					}

					if(mDTVSettings.getDvbtScanMode()==0){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
					}	
					break;
				case 5: //symbole
					holder.icon.setImageBitmap(mIcon3);	
					holder.info.setVisibility(View.VISIBLE);
					int sym=mDTVSettings.getDvbcSymbole();

					holder.info.setText(String.valueOf(sym));
					if(mDTVSettings.getDvbtScanMode()==0){
						holder.text.setTextColor(Color.DKGRAY);
						holder.info.setTextColor(Color.DKGRAY);
					}
					else{
						holder.text.setTextColor(Color.WHITE);
						holder.info.setTextColor(Color.YELLOW);
					}	
					break;
			}
			  
			return convertView;
		}
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

	public void showRegionsDialog(TextView v){
		final TextView info_cur = v;
		final String regions[] = mDTVSettings.getRegions();
		final int index = mDTVSettings.getRegionsIndex();
	
		new SingleChoiseDialog(DTVSettingsMenu.this,regions,index){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt)[2]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
					mDTVSettings.setRegion(regions[which]);
					mDTVSettings.setDvbtScanChannelIndex(0);
					info_cur.setText(regions[which]);
			}
		};						
	}

	public void showATSCRegionsDialog(TextView v){
		final TextView info_cur = v;
		final String regions[] = mDTVSettings.getATSCRegions();
		final int index = mDTVSettings.getATSCRegionsIndex();
	
		new SingleChoiseDialog(DTVSettingsMenu.this,regions,index){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt)[2]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
					mDTVSettings.setRegion(regions[which]);
					mDTVSettings.setDvbtScanChannelIndex(0);
					info_cur.setText(regions[which]);
			}
		};						
	}

	public void showAtscScanModeDialog(TextView v){
		final TextView info_cur = v;
		int mode = mDTVSettings.getAtscScanMode();
		int pos = 0;
		if(mode==0){
			pos = 0;
		}
		else {
			pos = 1;
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ getResources().getString(R.string.dtvscan_by_channel), getResources().getString(R.string.dtvscan_by_frequence)},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[0]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText(R.string.dtvscan_by_channel);
					mDTVSettings.setAtscScanMode(0);
					break;
				case 1:
					info_cur.setText(R.string.dtvscan_by_frequence);
					mDTVSettings.setAtscScanMode(1);
					break;
				
				}	
				refreshAtscManualScanList();
			}
		};						
	}							



	public void showDvbtScanModeDialog(TextView v){
		final TextView info_cur = v;
		int mode = mDTVSettings.getDvbtScanMode();
		int pos = 0;
		if(mode==0){
			pos = 0;
		}
		else {
			pos = 1;
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ getResources().getString(R.string.dtvscan_by_channel), getResources().getString(R.string.dtvscan_by_frequence)},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[0]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText(R.string.dtvscan_by_channel);
					mDTVSettings.setDvbtScanMode(0);
					break;
				case 1:
					info_cur.setText(R.string.dtvscan_by_frequence);
					mDTVSettings.setDvbtScanMode(1);
					break;
				
				}	
				refreshDvbtManualScanList();
			}
		};						
	}							

	public void showDvbtScanBandDialog(TextView v){
		final TextView info_cur = v;
		int mode = mDTVSettings.getDvbtScanBand();
		int pos = 0;
		if(mode==0){
			pos = 0;
		}
		else  if(mode==1){
			pos = 1;
		}

		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ getResources().getString(R.string.dtvscan_scan_band_vhf), getResources().getString(R.string.dtvscan_scan_band_uhf)},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[1]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText(R.string.dtvscan_scan_band_vhf);
					mDTVSettings.setDvbtScanBand(0);
					break;
				case 1:
					info_cur.setText(R.string.dtvscan_scan_band_uhf);
					mDTVSettings.setDvbtScanBand(1);
					break;
				}	
				
				String region;
				try {
					region = getConfig("tv:scan:dtv:region").getString();
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "Cannot read dtv region !!!");
					return;
				}
				if(region.contains("DVB-T")){
					DTVScanDVBT_UpdateChInfoByband(which);
				}
				else if (region.contains("DVB-C")){
					DTVScanDVBC_UpdateChInfoByband(which);
				}
				DTVScanDVBT_UpdateChInfoByband(which);
				refreshDvbtManualScanList();
			}
		};						
	}

	private boolean DTVScanDVBC_UpdateChInfoByband(int scanband)
	{
		boolean ret = false;

		if(dvbsandvbt_channelallbandlist == null)
			return ret;
		
		if(scanband == 0)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) < 300000)
				{
					mDTVSettings.setDvbtScanChannelIndex(i); 
					mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[i].frequency/1000);
					mDTVSettings.setDvbcModulation(dvbsandvbt_channelallbandlist[i].modulation);
					mDTVSettings.setDvbcSymbole(dvbsandvbt_channelallbandlist[i].symbolRate);
					ret = true;
					break;
				}				
			}	
		}
		else if(scanband == 1)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) >= 300000)
				{
					mDTVSettings.setDvbtScanChannelIndex(i); 
					mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[i].frequency/1000);
					mDTVSettings.setDvbcModulation(dvbsandvbt_channelallbandlist[i].modulation);
					mDTVSettings.setDvbcSymbole(dvbsandvbt_channelallbandlist[i].symbolRate);
					ret = true;
					break;
				}				
			}		
		}

		mDTVSettings.setDvbtScanChannelIndex(0);
	
		return ret;
	}

	private boolean DTVScanDVBT_UpdateChInfoByband(int scanband)
	{
		boolean ret = false;

		if(dvbsandvbt_channelallbandlist == null)
			return ret;
		
		if(scanband == 0)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) < 300000)
				{
					mDTVSettings.setDvbtScanChannelIndex(i); 
					mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[i].frequency/1000);
					mDTVSettings.setDvbtScanBandwidth(dvbsandvbt_channelallbandlist[i].bandwidth);
					ret = true;
					break;
				}				
			}	
		}
		else if(scanband == 1)
		{
			for (int i = 0; i < dvbsandvbt_channelallbandlist.length; i++)
			{
				if((dvbsandvbt_channelallbandlist[i].frequency/1000) >= 300000)
				{
					mDTVSettings.setDvbtScanChannelIndex(i); 
					mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channelallbandlist[i].frequency/1000);
					mDTVSettings.setDvbtScanBandwidth(dvbsandvbt_channelallbandlist[i].bandwidth);
					ret = true;
					break;
				}				
			}		
		}

		mDTVSettings.setDvbtScanChannelIndex(0);
	
		return ret;
	}

	public void DTVAtscManualScanChannel_Data_Init(){
		int channel_count=0;
		if(dvbsanatsc_channelallbandlist != null){
			for(int m=0;m<dvbsanatsc_channelallbandlist.length;m++){
				if(dvbsanatsc_channelallbandlist[m].frequency/1000<300000){
					channel_count++;
				}
			}
			dvbsanatsc_channelallbandlist = new TVChannelParams[channel_count];
			int temp = 0;
			for(int i=0;i<dvbsanatsc_channelallbandlist.length;i++){
				if(dvbsanatsc_channelallbandlist[i].frequency/1000<300000){
					dvbsanatsc_channelallbandlist[temp]=TVChannelParams.atscParams(dvbsanatsc_channelallbandlist[i].frequency, dvbsanatsc_channelallbandlist[i].modulation);			
					temp++;
				}
			}
		}

	}
	public void showAtscScanChannelNumberDialog(TextView v){
		final TextView info_cur = v;
		int pos = mDTVSettings.getAtscScanChannelIndex();
		DTVAtscManualScanConfig_Data_Init();

		String list[] = new String[dvbsanatsc_channelallbandlist.length];
		for(int i=0;i<dvbsanatsc_channelallbandlist.length;i++){
			list[i]= dvbsanatsc_channelallbandlist[i].frequency/1000+"KHZ";	
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,list,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[2]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				DTVScanATSC_UpdateChInfoByChNo(which);
				refreshAtscManualScanList();
			}
		};						
	}

	public void DTVDvbtManualScanChannel_Data_Init(){
		int channel_count=0;
		if(dvbsandvbt_channelallbandlist != null){
			if(mDTVSettings.getDvbtScanBand()==0){		
				for(int m=0;m<dvbsandvbt_channelallbandlist.length;m++){
					if(dvbsandvbt_channelallbandlist[m].frequency/1000<300000){
						channel_count++;
					}
				}
				dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
				int temp = 0;
				for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
					if(dvbsandvbt_channelallbandlist[i].frequency/1000<300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbtParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].bandwidth);			
						temp++;
					}
					
				}
			}
			else{
				channel_count = 0;
				for(int n=0;n<dvbsandvbt_channelallbandlist.length;n++){
					if(dvbsandvbt_channelallbandlist[n].frequency/1000>=300000){
						channel_count++;
					}
				}

				if(channel_count==0){
					mDTVSettings.setDvbtScanBand(0);
					DTVDvbtManualScanChannel_Data_Init();
				}
				else{
				
					dvbsandvbt_channel_number_list = new TVChannelParams[channel_count];
					int temp = 0;
					for(int i=0;i<dvbsandvbt_channelallbandlist.length;i++){
						if(dvbsandvbt_channelallbandlist[i].frequency/1000>=300000){
						dvbsandvbt_channel_number_list[temp]=TVChannelParams.dvbtParams(dvbsandvbt_channelallbandlist[i].frequency, dvbsandvbt_channelallbandlist[i].bandwidth);			
							temp++;
						}	
					}
				}	
			}	
			
		}

	}
			
	public void showDvbtScanChannelNumberDialog(TextView v){
		final TextView info_cur = v;
		int pos = mDTVSettings.getDvbtScanChannelIndex();
		DTVDvbtManualScanChannel_Data_Init();

		String list[] = new String[dvbsandvbt_channel_number_list.length];
		for(int i=0;i<dvbsandvbt_channel_number_list.length;i++){
			list[i]= dvbsandvbt_channel_number_list[i].frequency/1000+"KHZ";	
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,list,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[2]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				DTVScanDVBT_UpdateChInfoByChNo(which);
				refreshDvbtManualScanList();
			}

			public void onDismissEvent(){
			}
		};						
	}

	private Handler timer_signal_check_handler = new Handler();   

	private Runnable timer_signal_check_runnable = new Runnable() {
		public void run() {
			timer_signal_check_handler.postDelayed(timer_signal_check_runnable,500);  
			t.onSetupCmd(50,null);
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
			
			if(lock_status==false)
				checkboxStatus.setChecked(false);
			else
				checkboxStatus.setChecked(true);

		}
		
	}

	signalCheckThread t =null;
	private void showSignalCheckDialog(){
		String region;
		try {
			region = getConfig("tv:scan:dtv:region").getString();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Cannot read dtv region !!!");
			return;
		}

		t =new signalCheckThread();  
		t.start(); 
		
		mDialog = new AlertDialog(DTVSettingsMenu.this){
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

		mDialog.show();

		if (region.contains("DVB-C")){
			mDialog.setContentView(R.layout.dvbc_signal_check);
		}
		else
			mDialog.setContentView(R.layout.dvbt_signal_check);
		
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	
		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(DTVSettingsMenu.this.getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[5]);
	
		final TextView edittext_frequency= (TextView) window.findViewById(R.id.edittext_frequency);

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
		
		if(region.contains("DVB-T")){
			lock(TVChannelParams.dvbtParams(mDTVSettings.getDvbtScanFrequency()*1000,mDTVSettings.getDvbtScanBandwidth()));

			final  RadioGroup mRadioGroup = (RadioGroup) window.findViewById(R.id.radiogroup);
			mRadioGroup.setVisibility(View.VISIBLE);
			final  RadioButton mRadio1 = (RadioButton) window.findViewById(R.id.dvbt);
			mRadio1.setText("DVBT");
			final  RadioButton mRadio2 = (RadioButton) window.findViewById(R.id.dvbt2);
			mRadio2.setText("DVBT2");
			
			mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			  @Override
			  public void onCheckedChanged(RadioGroup group, int checkedId) {
			   if (checkedId == mRadio1.getId()) {
			    	lock(TVChannelParams.dvbtParams(mDTVSettings.getDvbtScanFrequency()*1000,mDTVSettings.getDvbtScanBandwidth()));
			   } else if (checkedId == mRadio2.getId()) {
			   	lock(TVChannelParams.dvbt2Params(mDTVSettings.getDvbtScanFrequency()*1000,mDTVSettings.getDvbtScanBandwidth()));
			   }
			  }
			 });

		}
		else if (region.contains("DVB-C")){
			lock(TVChannelParams.dvbcParams(mDTVSettings.getDvbtScanFrequency()*1000,mDTVSettings.getDvbcModulation(),mDTVSettings.getDvbcSymbole()));
		}
		else if(region.contains("ISDBT")){
			lock(TVChannelParams.isdbtParams(mDTVSettings.getDvbtScanFrequency()*1000,mDTVSettings.getDvbtScanBandwidth()));
		}

				
		edittext_frequency.setText(String.valueOf(mDTVSettings.getDvbtScanFrequency())+"KHZ");

		if((region.contains("ISDBT"))||(region.contains("DVB-T"))){
			String bandwidth=null;
			switch(mDTVSettings.getDvbtScanBandwidth()){
				case TVChannelParams.BANDWIDTH_8_MHZ:
					bandwidth= "8M";
					break;
				case TVChannelParams.BANDWIDTH_7_MHZ:
					bandwidth= "7M";
					break;
				case TVChannelParams.BANDWIDTH_6_MHZ:
					bandwidth= "6M";
					break;			
				case TVChannelParams.BANDWIDTH_AUTO:
					bandwidth= "Auto";
					break;
			}	
			final TextView edittext_band = (TextView) window.findViewById(R.id.band);
			edittext_band.setText(bandwidth);
		}
		else if(region.contains("DVB-C")){
			final TextView edittext_modulation = (TextView) window.findViewById(R.id.modulation);
			final TextView edittext_symbole = (TextView) window.findViewById(R.id.symbole);
			switch(mDTVSettings.getDvbcModulation()){
				case TVChannelParams.MODULATION_QAM_16:
					edittext_modulation.setText("16QAM");
					break;
				case TVChannelParams.MODULATION_QAM_32:
					edittext_modulation.setText("32QAM");
					break;
				case TVChannelParams.MODULATION_QAM_64:
					edittext_modulation.setText("64QAM");
					break;		
				case TVChannelParams.MODULATION_QAM_128:
					edittext_modulation.setText("128QAM");
					break;
				case TVChannelParams.MODULATION_QAM_256:
					edittext_modulation.setText("256QAM");
					break;		
				case TVChannelParams.MODULATION_QAM_AUTO:
					edittext_modulation.setText("Auto");
					break;	
			}	
			
			edittext_symbole.setText(String.valueOf(mDTVSettings.getDvbcSymbole())+"Kbps");
		}
		
		snr_value.setText("0%");
		agc_value.setText("0%");
		//ber_value.setText("0%");
		
		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
								//stopPlaying();
								t.onSetupCmd(50,null);
							}         
							}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							TVProgram mTVProgram=DTVPlayerGetDataByCurrentID();

							if(mTVProgram!=null){
								lock(mTVProgram.getChannel().getParams());
							}
							t.quitLoop();	
							timer_signal_check_handler.removeCallbacks(timer_signal_check_runnable);  
						}         
						});	

		timer_signal_check_handler.postDelayed(timer_signal_check_runnable, 500);
		
	}

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

	class signalCheckThread extends Thread {
		private Handler mHandler = null;

		private static final int ROTOR_CMD_STOP_MOVING		= 0;	//!< para: None
		private static final int ROTOR_CMD_DISABLE_LIMIT	= 1;	//!< para: None
		private static final int ROTOR_CMD_SET_ELIMIT		= 2;	//!< para: None
		
		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {

					switch (msg.what) { 
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
	

	private void DTVScanATSC_UpdateChInfoByChNo(int index)
	{
		if(dvbsanatsc_channelallbandlist == null)
			return;
		if(dvbsanatsc_channel_number_list.length==0)
			return;
		mDTVSettings.setAtscScanChannelIndex(index); 
		mDTVSettings.setAtscScanFrequency(dvbsanatsc_channel_number_list[index].frequency/1000);
		//mDTVSettings.setAtscScanBandwidth(dvbsandvbt_channel_number_list[index].modulation);
	}


	private void DTVScanDVBT_UpdateChInfoByChNo(int index)
	{
		if(dvbsandvbt_channelallbandlist == null)
			return;
		if(dvbsandvbt_channel_number_list.length==0)
			return;
		mDTVSettings.setDvbtScanChannelIndex(index); 
		mDTVSettings.setDvbtScanFrequency(dvbsandvbt_channel_number_list[index].frequency/1000);
		mDTVSettings.setDvbtScanBandwidth(dvbsandvbt_channel_number_list[index].bandwidth);
	}

	public void showAtscEditFreDialog(View v,int pos){
		final View view = v;
		final int position = pos;
		builder = new AlertDialog.Builder(this);	
		final EditText editText = new EditText(this);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		
		//editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		builder.setTitle(R.string.edit_title);
		TextView desFreText = (TextView) view;
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
								TextView des = (TextView)view;
								des.setText(editText.getText().toString());
								/*
								Log.d(TAG,"Old usf"+usfs[position]);
								usfs[position]= Integer.parseInt(editText.getText().toString());
								Log.d(TAG,"New usf"+usfs[position]);
								*/
								mDTVSettings.setDvbtScanFrequency(Integer.parseInt(editText.getText().toString()));
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
						
	public void showDvbtEditFreDialog(View v,int pos){
		final View view = v;
		final int position = pos;

		mDialog = new Dialog(this,R.style.MyDialog){
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

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.edit_number_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.edit_title));
		
		final EditText editText= (EditText) window.findViewById(R.id.edit);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		TextView desFreText = (TextView) view;
		editText.setText(desFreText.getText().toString());

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);

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
							if(Integer.parseInt(fre)==0||Integer.parseInt(fre)<51000||Integer.parseInt(fre)>858000){
								editText.setText(null);
								toast = Toast.makeText(
								DTVSettingsMenu.this, 
							    	R.string.invalid_input,
							    	Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
							else{
								TextView des = (TextView)view;
								des.setText(editText.getText().toString());
								/*
								Log.d(TAG,"Old usf"+usfs[position]);
								usfs[position]= Integer.parseInt(editText.getText().toString());
								Log.d(TAG,"New usf"+usfs[position]);
								*/
								mDTVSettings.setDvbtScanFrequency(Integer.parseInt(editText.getText().toString()));
							}	
						}
				 
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
	          }});


		
	}

	public void showDvbcSymboleDialog(View v,int pos){
		final View view = v;
		final int position = pos;

		mDialog = new Dialog(this,R.style.MyDialog){
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

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				
			}         
		}); 	
		mDialog.show();
		mDialog.setContentView(R.layout.edit_number_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


		TextView title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(getString(R.string.edit_title));
		
		final EditText editText= (EditText) window.findViewById(R.id.edit);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(6)});
		TextView desFreText = (TextView) view;
		editText.setText(desFreText.getText().toString());

		Button no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		Button yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);

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
								TextView des = (TextView)view;
								des.setText(editText.getText().toString());
								/*
								Log.d(TAG,"Old usf"+usfs[position]);
								usfs[position]= Integer.parseInt(editText.getText().toString());
								Log.d(TAG,"New usf"+usfs[position]);
								*/
								mDTVSettings.setDvbcSymbole(Integer.parseInt(editText.getText().toString()));
							}	
						}
				 
					if(mDialog!=null&& mDialog.isShowing()){
						mDialog.dismiss();
					}
	          }});


		
	}


	public void showDvbcScanModulationDialog(TextView v){
		final TextView info_cur = v;
		int pos = mDTVSettings.getDvbcModulation()-1;
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "16QAM", "32QAM","64QAM","128QAM","256QAM","Auto"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[4]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText("16QAM");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_16);
					break;
				case 1:
					info_cur.setText("32QAM");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_32);
					break;
				case 2:
					info_cur.setText("64QAM");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_64);
					break;	
				case 3:
					info_cur.setText("128QAM");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_128);
					break;
				case 4:
					info_cur.setText("256QAM");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_256);
					break;
				case 5:
					info_cur.setText("Auto");
					mDTVSettings.setDvbcModulation(TVChannelParams.MODULATION_QAM_AUTO);
					break;
					
				}	
			}
		};						
	}

	public void showDvbtScanBandwidthDialog(TextView v){
		final TextView info_cur = v;
		int pos = mDTVSettings.getDvbtScanBandwidth();
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "8M", "7M","6M","Auto"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getResources().getStringArray(R.array.search_settings_content_dvbt_manual)[4]);
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:
					info_cur.setText("8M");
					mDTVSettings.setDvbtScanBandwidth(TVChannelParams.BANDWIDTH_8_MHZ);
					break;
				case 1:
					info_cur.setText("7M");
					mDTVSettings.setDvbtScanBandwidth(TVChannelParams.BANDWIDTH_7_MHZ);
					break;
				case 2:
					info_cur.setText("6M");
					mDTVSettings.setDvbtScanBandwidth(TVChannelParams.BANDWIDTH_6_MHZ);
					break;	
				case 3:
					info_cur.setText("Auto");
					mDTVSettings.setDvbtScanBandwidth(TVChannelParams.BANDWIDTH_AUTO);
					break;
				default:
					info_cur.setText("8M");
					mDTVSettings.setDvbtScanBandwidth(TVChannelParams.BANDWIDTH_8_MHZ);
					break;
				}	
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

			
	public void showInforbarShowTimeDialog(TextView v){
		final TextView info_cur = v;
		int time = mDTVSettings.getInforBarShowTime();
		int pos = time -2;
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{"2s","3s","4s","5s","6s","7s","8s","9s","10s",},pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.screen_type));
			}
			
			public void onSetNegativeButton(){
				
			}

			public void onSetPositiveButton(int which){
				info_cur.setText(String.valueOf(which+2)+"S");						
				mDTVSettings.setInforBarShowTime(which+2);
			}
		};		
	}
	
	public void showScreenTypeDialog(TextView v){
		final TextView info_cur = v;
		//int mode = mDTVSettings.getScreenMode();
		int mode = DTVGetScreenMode();
		int pos = 0;
		final int step = 5;
		if(mode==1){
			pos = 0;
		}
		else {
			pos = mode-step;
		}
		
		/************************************
		VIDEO_WIDEOPTION_4_3_IGNORE       = 6,
		VIDEO_WIDEOPTION_4_3_LETTER_BOX   = 7,
		VIDEO_WIDEOPTION_4_3_PAN_SCAN     = 8,
		VIDEO_WIDEOPTION_4_3_COMBINED     = 9,
		VIDEO_WIDEOPTION_16_9_IGNORE      = 10,
		VIDEO_WIDEOPTION_16_9_LETTER_BOX  = 11,
		VIDEO_WIDEOPTION_16_9_PAN_SCAN    = 12,
		VIDEO_WIDEOPTION_16_9_COMBINED    = 13,
		***************************************/
		final String DATA[] = getResources().getStringArray(R.array.settings_screen_mode);
		new SingleChoiseDialog(DTVSettingsMenu.this,DATA,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.screen_type));
			}
			
			public void onSetNegativeButton(){
				
			}

			public void onSetPositiveButton(int which){
				switch(which){
					case 0:
						info_cur.setText(R.string.full_screen);						
						DTVSetScreenMode(1);
						break;
					case 1:
						info_cur.setText(R.string.type_4_3_IGNORE);						
						//mDTVSettings.setScreenMode(2);
						DTVSetScreenMode(which+step);
						break;
					case 2:
						info_cur.setText(R.string.type_4_3_LETTER_BOX);
						DTVSetScreenMode(which+step);
						break;
					case 3:
						info_cur.setText(R.string.type_4_3_PAN_SCAN);
						DTVSetScreenMode(which+step);
						break;	
					case 4:
						info_cur.setText(R.string.type_4_3_COMBINED);						
						DTVSetScreenMode(which+step);
						break;
					case 5:
						info_cur.setText(R.string.type_16_9_IGNORE);						
						DTVSetScreenMode(which+step);	
						break;
					case 6:
						info_cur.setText(R.string.type_16_9_LETTER_BOX);						
						DTVSetScreenMode(which+step);
						break;
					case 7:
						info_cur.setText(R.string.type_16_9_PAN_SCAN);						
						DTVSetScreenMode(which+step);
						break;
					case 8:
						info_cur.setText(R.string.type_16_9_COMBINED);						
						DTVSetScreenMode(which+step);						
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
				switchScreenType(0);
				mDTVSettings.factoryReset();

				Bundle bundle = new Bundle();	
				Intent pickerIntent = new Intent();
				bundle.putString("activity_tag", "factory_reset");
				pickerIntent.setClass(DTVSettingsMenu.this, DTVPlayer.class);
				pickerIntent.putExtras(bundle);
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
					stopPlaying();
					Intent Intent_pvr_manager = new Intent();
					Intent_pvr_manager.setClass(DTVSettingsMenu.this,DTVPvrManager.class);
					startActivity(Intent_pvr_manager);
					DTVSettingsMenu.this.finish();	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				
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
				((TextView)v).setText(getResources().getStringArray(R.array.Program_settings_content)[2]);
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


	private int debugMenuDialogShowFlag=0;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	    //reset_timer();
		if(!connected){
			return true;
			}
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_0:
			case KeyEvent.KEYCODE_1:
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_9:
				if(keyCode == KeyEvent.KEYCODE_2 && debugMenuDialogShowFlag == 0x0){
					debugMenuDialogShowFlag |= 0x1;
				}else if(keyCode == KeyEvent.KEYCODE_4 && debugMenuDialogShowFlag == 0x1){
					debugMenuDialogShowFlag |= 0x2;
				}else if(keyCode == KeyEvent.KEYCODE_6 && debugMenuDialogShowFlag == 0x3){
					debugMenuDialogShowFlag |= 0x4;
				}else if(keyCode == KeyEvent.KEYCODE_8 && debugMenuDialogShowFlag == 0x7){
					debugMenuDialogShowFlag |= 0x8;
				}else{
					debugMenuDialogShowFlag = 0x0;
				}
				return true;
			case DTVActivity.KEYCODE_YELLOW_BUTTON:
				if(debugMenuDialogShowFlag == 0xf){
					showMenuAndDemodDebugDialog();
				}
				debugMenuDialogShowFlag = 0x0;
				return true;	
			case KeyEvent.KEYCODE_DPAD_DOWN:	
				if(ListView_settings!=null){
					if(cur_select_item== ListView_settings.getCount()-1)
				    	ListView_settings.setSelection(0); 	
				}	
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(ListView_settings!=null){
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
		//onHide();	
	}
	private void DTVSetting_GotoDTVVChip(){
		new PasswordDialog(DTVSettingsMenu.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				DTVStartVChip();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				
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
		//onHide();
	}

	private  void onHide(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.INVISIBLE);
	} 
	
	private void onShow(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.VISIBLE);
	}


	private void showMenuAndDemodDebugDialog(){
		AlertDialog.Builder debugMenuDialog = new AlertDialog.Builder(this)
			.setTitle("Menu and demod tuner config")
			.setItems(new String[] {"Menu Config","Demod tuner config",""}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Log.d(TAG, "###Recive click"+which);
					switch (which) {
						case 0:
							showDvbModeMenuDebugDialog();
							break;
						case 1:
							showDemodAndDmxConfigMenuDebugDialog();
							break;
					}
				}
			});

		debugMenuDialog.create();
		debugMenuDialog.show();
	}

	public void showDvbModeMenuDebugDialog(){
		String mode = mDTVSettings.getDtvMode();
		int pos = 0;
		if(mode.equals("dvbs")){
			pos = 0;
		}
		else if(mode.equals("dvbt")){
			pos = 1;
		}
		else if(mode.equals("isdbt")){
			pos = 2;
		}
		else if(mode.equals("atsc")){
			pos = 3;
		}
		else if(mode.equals("dvbc")){
			pos = 4;
		}
		
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "DVBS", "DVBT/T2","ISDBT","ATSC","DVBC"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText("DTV Mode");
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:  //DVBS
					mDTVSettings.setDtvMode("dvbs");
					break;
				case 1:  //DVBT/T2
					mDTVSettings.setDtvMode("dvbt");
					break;
				case 2:  //ISDBT
					mDTVSettings.setDtvMode("isdbt");
					break;	
				case 3:  //ATSC
					mDTVSettings.setDtvMode("atsc");
					break;
				case 4:
					mDTVSettings.setDtvMode("dvbc");
					break;
				}	
			}
		};						
	}

	public void showDemodAndDmxConfigMenuDebugDialog(){
		//String mode = mDTVSettings.getDtvMode();
		int pos = 0;
	
		new SingleChoiseDialog(DTVSettingsMenu.this,new String[]{ "AVL6211(DVBS)", "MXL101(DVBT)","SI2168(DVBT/T2)","ITE9173(ISDBT)"},pos){
			public void onSetMessage(View v){
				((TextView)v).setText("DTV Mode");
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				switch(which){
				case 0:  //DVBS
					mDTVSettings.setDtvDemodAndDmxConfig("avl6211");
					break;
				case 1:  //DVBT/T2
					mDTVSettings.setDtvDemodAndDmxConfig("mxl101");
					break;
				case 2:  //ISDBT
					mDTVSettings.setDtvDemodAndDmxConfig("si2168");
					break;	
				case 3:  //ATSC
					
					break;
				}	

				
			}
			public void onDismissEvent(){
				FeConfigAndDmxConfig();
			}
		};						
	}
				
}
