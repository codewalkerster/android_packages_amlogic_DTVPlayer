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

public class DTVSettingsUI extends DTVSettings{
	private static final String TAG="DTVSettingsUI";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvsettings);  //settings_main
		UIAnimationInit();
		DTVSettingUIInit();
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
	public static final int SETTINGS_PARENTAL_RATING_SET=8;
	public static final int SETTINGS_TTX_REGION=9;

	private String ttx_region_str=null;
	private static String[] ttx_region_str_arry=null;
	private int cur_select_item=0;

	private ListView ListView_settings=null;
	private IconAdapter myAdapter=null;
	
	private AlertDialog.Builder builder;
	private AlertDialog.Builder editBuilder=null;
	private AlertDialog.Builder editBuilder_password=null;

	private int timeshift_time_select_item=-1;
	private int audio_track_select_item=-1;
	private int ttx_region_select_item=-1;


	private void DTVSettingUIInit()
	{
		//if(atsc_or_dvb)	
		DATA = getResources().getStringArray(R.array.settings_content);
		//else
	
		ttx_region_str = DTVSettingsUI.super.getTeletextRegionName();
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
			//if(ListView_settings.hasFocus() == true){
				//reset_timer();
				if(v.isSelected())
				 	v.startAnimation(listItemFocus);
				else
					v.clearAnimation();
			//}
			cur_select_item = position;
		}
		
		public void onNothingSelected(AdapterView<?> parent){
		}
	}; 	

	class listOnKeyListener implements OnKeyListener{
		public boolean onKey(View v, int keyCode, KeyEvent event) {					
			switch(keyCode)
			{
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

			builder = new AlertDialog.Builder(DTVSettingsUI.this);
			
			switch(position)
			{
			    case SETTINGS_SUBTILE_SWITCH:
				{
					
			    	if(DTVSettingsUI.super.getSubtitleStatus()==false)
			    	{
			    		image_cur.setBackgroundResource(R.drawable.select_round_2);
			    		info_cur.setText(R.string.on);
			    		//DVBPlayer.getConnect().setSubtitleStatus(true);
						//DVBPlayer.getStaticSubtitle().startSubtitle();
			    	}
			    	else if(DTVSettingsUI.super.getSubtitleStatus())
			    	{
			    		image_cur.setBackgroundResource(R.drawable.select_round_1);
			    		info_cur.setText(R.string.off);
			    		//DVBPlayer.getConnect().setSubtitleStatus(false);
						//DVBPlayer.getStaticSubtitle().stop();
			    	}	
				}
			    break;
				case SETTINGS_SCREEN_TYPE:
					{
						int mode = DTVSettingsUI.super.getScreenMode();
						int pos = 0;

						if(mode==0)
						{
							pos = 2;
						}
						else  if(mode==2)
						{
							pos = 0;
						}
						else  if(mode==3)
						{
							pos = 1;
						}


					     builder.setTitle(R.string.screen_type);
						 builder.setIcon( android.R.drawable.ic_dialog_info);
						 //builder.setCancelable(false);
						 
						 builder.setSingleChoiceItems(new String[]  { "4:3","16:9","auto"}, pos, new DialogInterface.OnClickListener() {
								      public void onClick(DialogInterface dialog, int which) {
										
								    	  switch(which)
						 					{
						 						case 0:
						 							info_cur.setText(R.string.type_4_3);						
													DTVSettingsUI.super.setScreenMode(2);
						 							break;
						 						case 1:
						 							info_cur.setText(R.string.type_16_9);
													DTVSettingsUI.super.setScreenMode(3);
						 							break;
						 						case 2:
						 							info_cur.setText(R.string.auto);
													DTVSettingsUI.super.setScreenMode(0);
						 							break;	
						 					}
						 					
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
			
				 				public void onClick(DialogInterface arg0, int arg1) {
				 					// TODO Auto-generated method stub
				 					Log.d("#####","#####"+arg1);
									
									int  mode = DTVSettingsUI.super.getScreenMode();
		 							 if(mode==0)
		 							 {
										 DTVSettingsUI.super.setScreenMode(0);
										 
		 							 }
		 							 else  if(mode==2)
		 							 {
		 								DTVSettingsUI.super.setScreenMode(2);
		 							 }
		 							 else  if(mode==3)
		 							 {
		 								DTVSettingsUI.super.setScreenMode(3);
		 							 }
				 				}
				        	 });								
							
							 AlertDialog dialog = builder.create();
							 dialog.setOnShowListener(new DialogInterface.OnShowListener(){
									public void onShow(DialogInterface dialog) {
										//timer_handler.removeCallbacks(timer_runnable);		
										//settings_list_hide_count = 0;
										}         
										}); 	

			   		         dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
									public void onDismiss(DialogInterface dialog) {
										
									switch(DTVSettingsUI.super.getScreenMode())
									{
										case 0:
											info_cur.setText(R.string.auto);
											DTVSettingsUI.super.setScreenMode(0);
											break;
										case 2:
											info_cur.setText(R.string.type_4_3);						
											DTVSettingsUI.super.setScreenMode(2);
											break;
										case 3:
											info_cur.setText(R.string.type_16_9);
											DTVSettingsUI.super.setScreenMode(3);
											break;
									}	
									//timer_handler.postDelayed(timer_runnable, 1000);	
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
					break;
					
				case SETTINGS_AUDIO_TRACK:
					{
						int mode = getAudioTrack();
						int pos = 0;
						  if(mode==0)
						 {
							 pos = 0;
						 }
						 else  if(mode==1)
						 {
							pos = 1;
						 }
						 else  if(mode==2)
						 {
							pos = 2;
						 }
					

						builder.setTitle(R.string.audio_track);
						builder.setIcon( android.R.drawable.ic_dialog_info);
						builder.setSingleChoiceItems(new String[] { "Left", "Right","Stereo"}, pos, new DialogInterface.OnClickListener() {
							      public void onClick(DialogInterface dialog, int which) {	
									//audio_track_select_item	 = which;	
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
		
			 				public void onClick(DialogInterface arg0, int arg1) {
			 					// TODO Auto-generated method stub
								
								 switch(audio_track_select_item)
					 					{
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
			        	 });	
						
						 AlertDialog dialog = builder.create();
						 dialog.setOnShowListener(new DialogInterface.OnShowListener(){
									public void onShow(DialogInterface dialog) {
										//timer_handler.removeCallbacks(timer_runnable);		
										//settings_list_hide_count = 0;
										}         
										}); 	

			   		     dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
									public void onDismiss(DialogInterface dialog) {
									//timer_handler.postDelayed(timer_runnable, 1000);	
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
					break;
				
				case SETTINGS_START_SCAN:
					{
					
						DTVSetting_GotoDTVScanDvbt();
						
						DTVSettingsUI.this.finish();
					}	

						//if(DTV.getStaticSubtitle()!=null)
							//DVBPlayer.getStaticSubtitle().stop();
					
					break;
				case SETTINGS_DEFAULT:
					showPasswordDialog();
					break;
				case SETTINGS_SET_PASSWORD:
					showSetPasswordDialog();
					break;
				case SETTINGS_SELECT_STORAGE:
					Intent Intent_path_select = new Intent();
	        		Intent_path_select.setClass(DTVSettingsUI.this, DTVDeviceBrowser.class);
	        		startActivity(Intent_path_select);
					DTVSettingsUI.this.finish();
					break;
				 case SETTINGS_TIMESHIFT_TIME_SET:
				 	{
						int pos = 0;
						int time = getTimeShiftingDuration();
						switch(time)
						{
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
						 
					
						 builder.setTitle(R.string.timeshift_time_set);
						 builder.setIcon( android.R.drawable.ic_dialog_info);
						 builder.setSingleChoiceItems(new String[]  { "10 Min","30 Min","60 Min"}, pos, new DialogInterface.OnClickListener() {
								      public void onClick(DialogInterface dialog, int which) {
										
										timeshift_time_select_item = which;
								    	 
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
			
				 				public void onClick(DialogInterface arg0, int arg1) {
				 					// TODO Auto-generated method stub
				 					Log.d("#####","#####"+arg1);

									switch(timeshift_time_select_item)
				 					{
				 						//pos = which;
				 						case 0:
				 							info_cur.setText("10 Min");
											DTVSettingsUI.super.setTimeShiftingDuration(10*60);
				 							break;
				 						case 1:
				 							info_cur.setText("30 Min");
											DTVSettingsUI.super.setTimeShiftingDuration(30*60);
				 							break;
				 						case 2:
				 							info_cur.setText("60 Min");
											DTVSettingsUI.super.setTimeShiftingDuration(60*60);
				 							break;	
				 					}



									

				 				}
				        	 });	
							
							
							
							 AlertDialog dialog = builder.create();
							 dialog.setOnShowListener(new DialogInterface.OnShowListener(){
									public void onShow(DialogInterface dialog) {
										//timer_handler.removeCallbacks(timer_runnable);		
										//settings_list_hide_count = 0;
										}         
										}); 	

			   		         dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
									public void onDismiss(DialogInterface dialog) {
									//timer_handler.postDelayed(timer_runnable, 1000);	
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
					break;
				 case SETTINGS_PARENTAL_RATING_SET:
					showParentalRatingPasswordDialog(info_cur);
					break;
				case SETTINGS_TTX_REGION:
					{
						
						int pos = DTVSettingsUI.super.getTeletextRegion();
						
						 //View view = getLayoutInflater().inflate(R.layout.test, (ViewGroup)findViewById(R.id.queryBuilder));
						 //builder.setView(view);
						 builder.setTitle(R.string.ttx_region);
						 builder.setIcon( android.R.drawable.ic_dialog_info);
						 builder.setSingleChoiceItems(ttx_region_str_arry, pos, new DialogInterface.OnClickListener() {
								      public void onClick(DialogInterface dialog, int which) {
										ttx_region_select_item = which;
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
			
				 				public void onClick(DialogInterface arg0, int arg1) {
				 					// TODO Auto-generated method stub
				 					Log.d("#####","#####"+arg1);
				 					info_cur.setText(ttx_region_str_arry[ttx_region_select_item]);
									DTVSettingsUI.super.setTeletextRegion(ttx_region_select_item);
			
				 				}
				        	 });	
							
							
							
							 AlertDialog dialog = builder.create();
							 dialog.setOnShowListener(new DialogInterface.OnShowListener(){
									public void onShow(DialogInterface dialog) {
										//timer_handler.removeCallbacks(timer_runnable);		
										}         
										}); 	

			   		         dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
									public void onDismiss(DialogInterface dialog) {
									//timer_handler.postDelayed(timer_runnable, 1000);	
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
					break;
			}
		}
	};
	
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
			
			holder.info.setTextColor(Color.YELLOW);
			switch(position){
			case SETTINGS_SUBTILE_SWITCH:
				{
			    	 holder.icon.setImageBitmap(mIcon1);
					 holder.info.setVisibility(View.VISIBLE);
					 holder.icon1.setVisibility(View.VISIBLE);
			    	 if (getSubtitleStatus())
					   {			   
			    		 holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
			    		 
			    		 holder.info.setText(R.string.on);
					   }
					   else
					   {
						   holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
						   holder.info.setText(R.string.off);
					   }	  
				}
		    	break;
		     case SETTINGS_SCREEN_TYPE:
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.VISIBLE);
				 
		    	 holder.icon.setImageBitmap(mIcon2);
		    	 holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 

				 int mode= DTVSettingsUI.super.getScreenMode();		 
				 if(mode==0)
				 {
				 	 holder.info.setText(R.string.auto);
				 }
				 else  if(mode==2)
				 {
					holder.info.setText(R.string.type_4_3);
				 }
				 else  if(mode==3)
				 {
					holder.info.setText(R.string.type_16_9);
				 }	
				 
		    	 break;
			
		     case SETTINGS_AUDIO_TRACK:
			 	 holder.info.setVisibility(View.VISIBLE);
				 holder.icon1.setVisibility(View.VISIBLE);
				 
		    	 holder.icon.setImageBitmap(mIcon3);
		    	 holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				
				 int tmp= DTVSettingsUI.super.getAudioTrack();
			
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
				holder.icon1.setVisibility(View.VISIBLE);
				holder.icon.setImageBitmap(mIcon9);
				holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 

				int time = DTVSettingsUI.super.getTimeShiftingDuration();
				switch(time)
				{
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
			 	holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.VISIBLE);
				holder.icon.setImageBitmap(mIcon10);
				holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 

				int pr = DTVSettingsUI.super.getParentalRating();
				if(pr<=0){
					holder.info.setText(R.string.all);
				}else{
					holder.info.setText(pr+"");
				}
				break;
			
			case SETTINGS_TTX_REGION:
				holder.info.setVisibility(View.VISIBLE);
				holder.icon1.setVisibility(View.VISIBLE);
				holder.icon.setImageBitmap(mIcon12);
				holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
				
				int pos =  DTVSettingsUI.super.getTeletextRegion();

				holder.info.setSingleLine(true);
				holder.info.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));
				if(ttx_region_str_arry!=null)
					holder.info.setText(ttx_region_str_arry[pos]);

				break;
		  }

		  return convertView;
		}
	}	
	
	public void showFactorySureDialog()
	{
		builder = new AlertDialog.Builder(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(DTVSettingsUI.this); 
		builder.setMessage(R.string.sure_factory_set)
		.setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				//factory_reset();
				dialog.cancel();
			}        
		 })        
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();            
				}        
		 }); 
			AlertDialog alert = builder.create();
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
							//timer_handler.removeCallbacks(timer_runnable);		
							}         
							}); 	

			alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
						//timer_handler.postDelayed(timer_runnable, 1000);	
						}         
						});	
		alert.show();	

		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
	               lp.dimAmount=0.0f;
	               alert.getWindow().setAttributes(lp);
	               alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	public void showPasswordDialog()
	{
		  Log.d(TAG,"############showPasswordDialog");	
          	  AlertDialog alert_password=null;	
			 
		  editText = new EditText(this);
		  editText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
		  editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		  editBuilder_password.setTitle(R.string.enter_password);
		  editBuilder_password.setView(editText); 
 
		   alert_password = editBuilder_password.create();
		  
		   alert_password.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						String password = editText.getText().toString();

						 //cur_password = mLast.getString("PASSWORD",cur_password);
				
						if((cur_password==null))
						{
							if(password.equals("1234")||password.equals("0000"))
							{
								dialog.cancel();
								showFactorySureDialog();
							}
							else
							{
								editText.setText(null);

								toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_password,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
						}
						else
						{
							if(password.equals(cur_password)||password.equals("0000"))
							{
								dialog.cancel();
								showFactorySureDialog();							}
							else
							{
								editText.setText(null);

								toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_password,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
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


		
		alert_password.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				//timer_handler.removeCallbacks(timer_runnable);		
				}         
				}); 	

		alert_password.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
		
			//timer_handler.postDelayed(timer_runnable, 1000);	
			}         
			});	

		   
		  alert_password.show();

		  alert_password.getWindow().setLayout(500, -1);
		  	
		  WindowManager.LayoutParams lp=alert_password.getWindow().getAttributes();
		  lp.dimAmount=0.0f;
		  alert_password.getWindow().setAttributes(lp);
		  alert_password.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		  
	}


	void parentalrating_set(TextView v)
	{
		final TextView info_cur = v;
		
		builder = new AlertDialog.Builder(this);
		
		int pr = DTVSettingsUI.super.getParentalRating();
		int pos = 0;

		if(pr>=4 && pr<=18)
			pos = pr-3;
		else if(pr>18)
			pos = 15;
		builder.setTitle(R.string.parental_rating_set);
		builder.setIcon( android.R.drawable.ic_dialog_info);
		builder.setSingleChoiceItems(new String[]  {"all","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18"},
				pos,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if(which==0){
							info_cur.setText(R.string.all);
							DTVSettingsUI.super.setParentalRating(0);
						}else{
							info_cur.setText(which+3+"");
							DTVSettingsUI.super.setParentalRating(which+3);
						}
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
 				public void onClick(DialogInterface arg0, int arg1) {
 					// TODO Auto-generated method stub
 					Log.d(TAG,">>>>"+arg1);
					DTVSettingsUI.super.forceParentalRatingCheck();
 				}
        	});
		
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener(){
				public void onShow(DialogInterface dialog) {
					//timer_handler.removeCallbacks(timer_runnable);		
				}         
		}); 	

		        dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
				public void onDismiss(DialogInterface dialog) {
					//timer_handler.postDelayed(timer_runnable, 1000);	
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

	public void showParentalRatingPasswordDialog(TextView v)
	{
		final TextView info_cur = v;
		  Log.d(TAG,"############showParentalRatingPasswordDialog");	
          AlertDialog alert_password=null;	
			 
		  editText = new EditText(this);
		  editText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
		  editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		  editBuilder_password.setTitle(R.string.enter_password);
		  editBuilder_password.setView(editText); 
 
		   alert_password = editBuilder_password.create();
		  
		   alert_password.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_CENTER:
					//case KeyEvent.KEYCODE_ENTER:
						String password = editText.getText().toString();

						 //cur_password = mLast.getString("PASSWORD",null);
				
						if((cur_password==null))
						{
							if(password.equals("1234")||password.equals("0000"))
							{
								dialog.cancel();
								parentalrating_set(info_cur);
							}
							else
							{
								editText.setText(null);

								toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_password,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
						}
						else
						{
							if(password.equals(cur_password)||password.equals("0000"))
							{
								dialog.cancel();
								parentalrating_set(info_cur);							}
							else
							{
								editText.setText(null);

								toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_password,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
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

		   alert_password.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				//timer_handler.removeCallbacks(timer_runnable);		
				//settings_list_hide_count = 0;
				}         
				}); 	

		alert_password.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
		
			//timer_handler.postDelayed(timer_runnable, 1000);	
			}         
			});	
		  alert_password.show();

		  alert_password.getWindow().setLayout(500, -1);
		  	
		  WindowManager.LayoutParams lp=alert_password.getWindow().getAttributes();
		  lp.dimAmount=0.0f;
		  alert_password.getWindow().setAttributes(lp);
		  alert_password.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		  
	}	

    EditText editText_password_old;
    EditText editText;
    EditText editText1;
    String password_old = null;
    String password_new = null;
    String password_new_again =null;
    String cur_password = null;
	public void showSetPasswordDialog()

	{
		Log.d(TAG,"############showPasswordDialog");

		AlertDialog alert_password_set=null;	

		LinearLayout password_set_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dtvsettings_password_set, null);

		editText_password_old =(EditText)password_set_layout.findViewById(R.id.edittext_password_old);
		editText = (EditText)password_set_layout.findViewById(R.id.edittext_password1);
		editText1 = (EditText)password_set_layout.findViewById(R.id.edittext_password2);

		editText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
		editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		editText1.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
		editText1.setTransformationMethod(PasswordTransformationMethod.getInstance());

		editText_password_old.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(4)});
		editText_password_old.setTransformationMethod(PasswordTransformationMethod.getInstance());

		editBuilder.setTitle(R.string.password_set);
		editBuilder.setView(password_set_layout); 

		  editBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				 password_old = editText_password_old.getText().toString();
				 password_new = editText.getText().toString();
				 password_new_again = editText1.getText().toString();
				 //cur_password = mLast.getString("PASSWORD",cur_password);

				if(password_new.equals("")||password_new_again.equals("")||password_old.equals(""))
				{
				
					toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_input,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
					try
						{
						    Field field = dialog.getClass()
						            .getSuperclass().getDeclaredField("mShowing");
						    field.setAccessible(true);
						    field.set(dialog, false);
						    dialog.dismiss();
						}
						catch (Exception e)
						{
						}

						return;
				}

				if(cur_password!=null)
				{
					
					{
						if((password_new.equals(password_new_again))&&(password_old.equals(cur_password)||password_old.equals("0000")))
						{

							//mLast.edit().putString ("PASSWORD",password_new)
							    //.commit();
						}
						else
						{
					
							toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_input,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							try
								{
								    Field field = dialog.getClass()
								            .getSuperclass().getDeclaredField("mShowing");
								    field.setAccessible(true);
								    field.set(dialog, false);
								    dialog.dismiss();
								}
								catch (Exception e)
								{
								}

								return;
						}
							
					}
				}
				else
				{
					if((password_new.equals(password_new_again))&&(password_old.equals("1234")||password_old.equals("0000")))
					{

						//mLast.edit().putString ("PASSWORD",password_new)
						    //.commit();
					}
					else
						{	

							toast = Toast.makeText(
								DTVSettingsUI.this, 
					    		R.string.invalid_input,
					    		Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							try
								{
								    Field field = dialog.getClass()
								            .getSuperclass().getDeclaredField("mShowing");
								    field.setAccessible(true);
								    field.set(dialog, false);
								    dialog.dismiss();
								}
								catch (Exception e)
								{
								}

								return;
						}
					
				}

				try
						{
						    Field field = dialog.getClass()
						            .getSuperclass().getDeclaredField("mShowing");
						    field.setAccessible(true);
						    field.set(dialog, true);
						    dialog.dismiss();
						}
						catch (Exception e)
						{
						}
			}
		});

		editBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try
				{
				    Field field = dialog.getClass()
				            .getSuperclass().getDeclaredField("mShowing");
				    field.setAccessible(true);
				    field.set(dialog, true);
				    dialog.dismiss();
				}
				catch (Exception e)
				{
				}           
			}        
		}); 
		 
		  alert_password_set = editBuilder.create();
		  alert_password_set.setOnShowListener(new DialogInterface.OnShowListener(){
								public void onShow(DialogInterface dialog) {
									//timer_handler.removeCallbacks(timer_runnable);		
									}         
									}); 	

		   alert_password_set.setOnDismissListener(new DialogInterface.OnDismissListener(){
								public void onDismiss(DialogInterface dialog) {
								//timer_handler.postDelayed(timer_runnable, 1000);	
								}         
								});	

		  
		  alert_password_set.show();          
		  WindowManager m = getWindowManager();   
		  Display d = m.getDefaultDisplay();  	
		  WindowManager.LayoutParams lp=alert_password_set.getWindow().getAttributes();
		  lp.dimAmount=0.0f;
		  lp.height = (int) (d.getHeight() * 0.6);  
		  lp.width = (int) (d.getWidth() * 0.65);
		  alert_password_set.getWindow().setAttributes(lp);
		  alert_password_set.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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


	private void DTVSetting_GotoDTVScanDvbt()
	{
		Intent intent = new Intent();
		intent.setClass(this, DTVScanDVBT.class);
		
		startActivityForResult(intent, 1);	
	}
	
}

