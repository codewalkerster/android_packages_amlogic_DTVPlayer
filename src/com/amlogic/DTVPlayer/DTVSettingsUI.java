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

public class DTVSettingsUI extends DTVActivity{
	private static final String TAG="DTVSettingsUI";
	DTVSettings mDTVSettings = null;
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvsettings);  //settings_main
		//UIAnimationInit();
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
		DTVSettingUIInit();
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

	private static String[] DATA = null;
	private Toast toast=null;
	public static final int SETTINGS_START_SCAN = 0;
	public static final int SETTINGS_SUBTILE_SWITCH = 1;
	public static final int SETTINGS_SCREEN_TYPE = 2;
	public static final int SETTINGS_AUDIO_TRACK = 3;
	public static final int SETTINGS_DEFAULT = 4;
	public static final int SETTINGS_SET_PASSWORD = 5;
	public static final int SETTINGS_SELECT_STORAGE=6;
	public static final int SETTINGS_TIMESHIFT_TIME_SET=7;
	public static final int SETTINGS_PARENTAL_RATING_SET=8; //and vchip

	public static final int SETTINGS_TTX_REGION=9;
	public static final int SETTINGS_CC=10;
	public static final int SETTINGS_ANTENNA_SOURCE=11;
		
	private String ttx_region_str=null;
	private static String[] ttx_region_str_arry=null;
	private int cur_select_item=0;

	private ListView ListView_settings=null;
	private IconAdapter myAdapter=null;
	
	private AlertDialog.Builder editBuilder=null;
	private AlertDialog.Builder editBuilder_password=null;

	private int timeshift_time_select_item=-1;
	private int audio_track_select_item=-1;
	private int ttx_region_select_item=-1;

	private void DTVSettingUIInit(){
		Log.d(TAG,"scan region="+mDTVSettings.getScanRegion());
		if(mDTVSettings.getScanRegion().contains("ATSC"))	
			DATA = getResources().getStringArray(R.array.settings_content_atsc);
		else
			DATA = getResources().getStringArray(R.array.settings_content);
	
		ttx_region_str = mDTVSettings.getTeletextRegionName();
		if(ttx_region_str!=null)
			ttx_region_str_arry = ttx_region_str.split(" "); 

		//listview
		ListView_settings = (ListView)findViewById(R.id.settings_list);
		myAdapter = new IconAdapter(this,DATA);
		ListView_settings.setOnItemSelectedListener(mOnSelectedListener);
		ListView_settings.setOnKeyListener(new listOnKeyListener());
		ListView_settings.setOnItemClickListener(mOnItemClickListener);
		ListView_settings.setAdapter(myAdapter);
		ListView_settings.requestFocus();

		//title		
		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.settings_title);

		//dialog
		editBuilder = new AlertDialog.Builder(this);
		editBuilder_password = editBuilder;
		
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

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			
			ImageView image_cur = (ImageView)v.findViewById(R.id.icon1);

			final TextView info_cur = (TextView)v.findViewById(R.id.info);
			
			switch(position){
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
				case SETTINGS_SCREEN_TYPE:
					showScreenTypeDialog(info_cur);		
					break;
					
				case SETTINGS_AUDIO_TRACK:
					showAudioTrackDialog(info_cur);	
					break;
				
				case SETTINGS_START_SCAN:
					{
						DTVSetting_GotoDTVScan();
						DTVSettingsUI.this.finish();
					}	
					//if(DTV.getStaticSubtitle()!=null)
						//DVBPlayer.getStaticSubtitle().stop();
					break;
				case SETTINGS_DEFAULT:
					showPasswordDialog();
					break;
				case SETTINGS_SET_PASSWORD:
					showEnterPasswordSettingDialog();
					break;
				case SETTINGS_SELECT_STORAGE:
					Intent Intent_path_select = new Intent();
	        		Intent_path_select.setClass(DTVSettingsUI.this, DTVDeviceBrowser.class);
	        		startActivity(Intent_path_select);
					DTVSettingsUI.this.finish();
					break;
				 case SETTINGS_TIMESHIFT_TIME_SET:
				 	showTimeshiftingTimeSettingDialog(info_cur);
					break;
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

	
	private class IconAdapter extends BaseAdapter {
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
	
		public IconAdapter(Context context, String[] list) {
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
		     case SETTINGS_SCREEN_TYPE:
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 holder.icon.setImageBitmap(mIcon2);
		    	 //holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 

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
		     case SETTINGS_AUDIO_TRACK:
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 holder.icon.setImageBitmap(mIcon3);
		    	 holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				
				 int tmp= mDTVSettings.getAudioTrack();
			
				 if(tmp == 2)
				 	holder.info.setText(R.string.stereo);
				 else if(tmp == 1)
					holder.info.setText(R.string.right);
				 else if(tmp == 0)
				 	holder.info.setText(R.string.left);
		    	 break;
				 
		     case SETTINGS_START_SCAN:
			 	holder.info.setVisibility(View.INVISIBLE);
				 holder.icon1.setVisibility(View.INVISIBLE);
		    	 holder.icon.setImageBitmap(mIcon4);
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
				
				int pos =  mDTVSettings.getTeletextRegion();

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

	public void showTTXRegionDialog(TextView v){
		final TextView info_cur = v;
		int pos = mDTVSettings.getTeletextRegion();
		new SingleChoiseDialog(DTVSettingsUI.this,ttx_region_str_arry,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.ttx_region));
			}
			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				info_cur.setText(ttx_region_str_arry[which]);
				mDTVSettings.setTeletextRegion(which);
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

		new SingleChoiseDialog(DTVSettingsUI.this,new String[]{ "Left", "Right","Stereo"},pos){
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
		
		new SingleChoiseDialog(DTVSettingsUI.this,new String[]{ "4:3","16:9","auto"},pos){
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
		new SureDialog(DTVSettingsUI.this){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.sure_factory_set));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(){
				mDTVSettings.factoryReset();
				Intent pickerIntent = new Intent();
				pickerIntent.setClass(DTVSettingsUI.this, DTVPlayer.class);
 		        startActivity(pickerIntent);
				DTVSettingsUI.this.finish();
			}
		};
	}

	private void showPasswordDialog(){
		new PasswordDialog(DTVSettingsUI.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				showFactorySureDialog();	
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsUI.this, 
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
		
		new SingleChoiseDialog(DTVSettingsUI.this,new String[]{"all","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18"},pos){
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

		new SingleChoiseDialog(DTVSettingsUI.this,new String[]{ "10 Min","30 Min","60 Min"},pos){
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
		new PasswordDialog(DTVSettingsUI.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				parentalrating_set(info_cur);		
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
							DTVSettingsUI.this, 
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

	private void showSetPasswordDialog(){
		new PasswordSettingDialog(DTVSettingsUI.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsUI.this, 
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
		new PasswordDialog(DTVSettingsUI.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				showSetPasswordDialog();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
								DTVSettingsUI.this, 
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
			
		new SingleChoiseDialog(DTVSettingsUI.this,new String[]{ "Air","Cable"},pos){
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
				break;
			case KeyEvent.KEYCODE_BACK:	
				Intent in = new Intent();
				in.setClass(DTVSettingsUI.this, DTVPlayer.class);
				//setResult(RESULT_OK,null);
				DTVSettingsUI.this.startActivity(in);	
				DTVSettingsUI.this.finish();
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
			Intent_scan.setClass(DTVSettingsUI.this, DTVScanDVBT.class);
		}
		else if(region.contains("ATSC")){
			Log.d(TAG, "goto DTVScanATSC");
			Intent_scan.setClass(DTVSettingsUI.this, DTVScanATSC.class);
		}
		else if(region.contains("DVBS"))
		{
			Log.d(TAG, "goto DTVScanDVBS");
			Intent_scan.setClass(DTVSettingsUI.this, DTVScanDVBS.class);
		}	
		else if(region.contains("DVB-C"))
		{
			Log.d(TAG, "goto DTVScanDVBC");
			Intent_scan.setClass(DTVSettingsUI.this, DTVScanDVBC.class);				
		}		
		
		startActivityForResult(Intent_scan, 1);	
		
	}

	private void DTVStartVChip(){
		Intent intent = new Intent();
		intent.setClass(this, DTVVChip.class);
		startActivityForResult(intent, 11);	
		onHide();	
	}
	private void DTVSetting_GotoDTVVChip(){
		new PasswordDialog(DTVSettingsUI.this){
			public void onCheckPasswordIsRight(){
				Log.d(TAG,">>>>>PASSWORD IS RIGHT!<<<<<");
				DTVStartVChip();
			}
			public void onCheckPasswordIsFalse(){
				Log.d(TAG,">>>>>PASSWORD IS False!<<<<<");
				toast = Toast.makeText(
				DTVSettingsUI.this, 
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

