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
import com.amlogic.tvutil.TVGroup;

import java.text.SimpleDateFormat;
import java.util.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.util.DisplayMetrics;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import com.amlogic.widget.Rotate3D;
import com.amlogic.widget.CustomDialog;
import com.amlogic.widget.CustomDialog.ICustomDialog;

public class DTVChannelList extends DTVActivity{
	private static final String TAG="DTVChannelList";
	ListView ListView_channel=null;
	TextView Text_title=null;
	private int class_total=0;
	private int cur_class_no=-1;
	private int cur_select_item=0;
	private IconAdapter myAdapter=null;
	private TVProgram[]  mTVProgramList=null;
	private DTVSettings mDTVSettings=null;

	int db_id=-1;
	private int service_type=TVProgram.TYPE_TV;
	private boolean favor=false;

	private void getListData(int type){
		if(type==0)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,true);
		else if(type==1)
			mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_RADIO,true);
	}

	private void getListFavorite(){
		mTVProgramList=TVProgram.selectByFavorite(this,true);	
	}


	TVGroup[] mTVGroup=null;
	private int getListProgramClass(){
		mTVGroup=DTVProgramManagerGetGroupList();
		if(mTVGroup!=null)
			return mTVGroup.length;
		else
			return 0;
	}
	
	private void getClassData(int class_no){
		mTVProgramList=DTVProgramManagerGetProByGroup(class_no);
	}

	private LinearLayout LinearLayoutListView=null;
	private void DTVChannelListUIInit(){

		Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	db_id = bundle.getInt("db_id");
			service_type=getCurrentProgramType();
		}	

	    //LinearLayoutListView = (LinearLayout)findViewById(R.id.LinearLayoutListView);
		//initAnimation();

		Text_title=(TextView)findViewById(R.id.Text_title);
		Text_title.setTextColor(Color.YELLOW);
		class_total = getListProgramClass();
		if(service_type == TVProgram.TYPE_RADIO){
			getListData(1);
			Text_title.setText(R.string.radio);
		}	
		else{
			service_type = TVProgram.TYPE_TV;
			getListData(0);
			Text_title.setText(R.string.tv);
		}
		
		ListView_channel = (ListView) findViewById(R.id.ListView_channel);
		myAdapter = new IconAdapter(DTVChannelList.this,null);
		ListView_channel.setOnItemSelectedListener(mOnSelectedListener);
		ListView_channel.setOnScrollListener(new listOnScroll()); 
		ListView_channel.setOnItemClickListener(mOnItemClickListener);
		ListView_channel.setAdapter(myAdapter);
		setFocusPosition();
	}

	public void setFocusPosition(){
		int i = 0;
		if(mTVProgramList!=null){
			for(i=0;i<mTVProgramList.length;i++){
				if(db_id == mTVProgramList[i].getID()){
					ListView_channel.setFocusableInTouchMode(true);
	   			  	ListView_channel.requestFocus();
	   			  	ListView_channel.requestFocusFromTouch();
	        		ListView_channel.setSelection(i);
					cur_select_item = i;
					break;
				}
			}	
		}
	}
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvchannellist); 
		mDTVSettings = new DTVSettings(this);
		DTVChannelListUIInit();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		myAdapter.notifyDataSetChanged();
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
  
  	private AdapterView.OnItemSelectedListener mOnSelectedListener = new AdapterView.OnItemSelectedListener(){
		public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
			ListView_channel = (ListView) findViewById(R.id.ListView_channel);
			if(ListView_channel.hasFocus() == true){
			}
			cur_select_item = position;
		}
		public void onNothingSelected(AdapterView<?> parent){
		}
	};

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			int db_id=mTVProgramList[position].getID();	
			int serviceType = mTVProgramList[position].getType();
			/*
			if(serviceType==TVProgram.TYPE_RADIO){
				setProgramType(TVProgram.TYPE_RADIO);
				Log.d(TAG,"setProgramType(TVProgram.TYPE_RADIO)");
			}	
			else{
				setProgramType(TVProgram.TYPE_TV);
				Log.d(TAG,"setProgramType(TVProgram.TYPE_TV)");
			}
			*/
			Bundle bundle = new Bundle();
			bundle.putInt("db_id",db_id);
			bundle.putInt("service_type",serviceType);
			bundle.putString("activity_tag","DTVChannelList");
			Intent in = new Intent();
			in.setClass(DTVChannelList.this, DTVPlayer.class);
			in.putExtras(bundle);
			startActivity(in);
			DTVChannelList.this.finish();

		}
	};

	private class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context cont;
		private List<String> listItems;
		private int selectItem;
		
		class ViewHolder {
			TextView prono;
			TextView text;	
			ImageView icon_scrambled;
			ImageView icon_fav;
			ImageView icon;
		}
		
		public IconAdapter(Context context, List<String> list) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);			  
		}

		public int getCount() {
			if(mTVProgramList==null)
				return 0;
			else
				return mTVProgramList.length;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}

		public void setSelectItem(int position){
			this.selectItem = position;
		}
        
        public int getSelectItem(){
			return this.selectItem;
        }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;	
			if (convertView == null){    
				convertView = mInflater.inflate(R.layout.dtvchannellist_item, null);
				
				holder = new ViewHolder();
				holder.prono = (TextView)convertView.findViewById(R.id.prono);
				holder.text = (TextView) convertView.findViewById(R.id.ItemText);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.icon_scrambled = (ImageView)convertView.findViewById(R.id.icon_scrambled);
				holder.icon_fav = (ImageView)convertView.findViewById(R.id.icon_fav);
				convertView.setTag(holder);
			}
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
		
			// Bind the data efficiently with the holder.

			if(mDTVSettings.getScanRegion().contains("ATSC")==false){
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
			}
			else{
				holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber())+"-"+Integer.toString(mTVProgramList[position].getNumber().getMinor()));
			}
			
			holder.text.setText(mTVProgramList[position].getName());

			if(db_id == mTVProgramList[position].getID()){  
				//convertView.setBackgroundColor(Color.RED);  
				holder.text.setTextColor(Color.YELLOW);
			}	
			else{
				//convertView.setBackgroundColor(Color.TRANSPARENT); 
				holder.text.setTextColor(Color.WHITE);
			}	
		
			if(mTVProgramList[position].getLockFlag()){
				holder.icon.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
			}	
			else{
				holder.icon.setBackgroundResource(Color.TRANSPARENT);
			}

			if(mTVProgramList[position].getFavoriteFlag()){
				holder.icon_fav.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
			}	
			else{
				holder.icon_fav.setBackgroundResource(Color.TRANSPARENT);
			}	

			if(mTVProgramList[position].getScrambledFlag()){
				holder.icon_scrambled.setBackgroundResource(R.drawable.dtvplayer_icon_scrambled); 
			}	
			else{
				holder.icon_scrambled.setBackgroundResource(Color.TRANSPARENT);
			}			  
			return convertView;
		}
	}	
		
	class listOnScroll implements OnScrollListener{
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			//reset_timer();	
		}
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//reset_timer();
		}
    }	
	
	class MouseClick implements OnClickListener{    
		public void onClick(View v) {
				// TODO Auto-generated method stub	
		    	//reset_timer();
				switch (v.getId()) {
					case R.id.arrow_left:
						break;
				}
			}
	     }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//reset_timer();
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				DTVListDealLeftAndRightKey(0);
				break;		
			case KeyEvent.KEYCODE_DPAD_RIGHT:	
				DTVListDealLeftAndRightKey(1);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(cur_select_item== ListView_channel.getCount()-1){
			    	ListView_channel.setSelection(0); 	
					return true;
				}	
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(cur_select_item== 0){
					ListView_channel.setSelection(ListView_channel.getCount()-1); 
					return true;
				}	
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				showPvrTimeSetDialog(DTVChannelList.this);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}	  

	
	Rotate3D lQuest1Animation=null;		
	Rotate3D lQuest2Animation =null;	
 	Rotate3D rQuest1Animation=null;	
    Rotate3D rQuest2Animation=null;	
	public void initAnimation() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int mCenterX = dm.widthPixels / 2;
		int mCenterY = dm.heightPixels / 2;
		
		int duration = 300;
		lQuest1Animation = new Rotate3D(0, -90, mCenterX, mCenterY);	
		lQuest1Animation.setFillAfter(true);
		lQuest1Animation.setDuration(duration);

		lQuest2Animation = new Rotate3D(90, 0, mCenterX, mCenterY);		
		lQuest2Animation.setFillAfter(true);
		lQuest2Animation.setDuration(duration);

		rQuest1Animation = new Rotate3D(0, 90, mCenterX, mCenterY);		
		rQuest1Animation.setFillAfter(true);
		rQuest1Animation.setDuration(duration);

		rQuest2Animation = new Rotate3D(-90, 0, mCenterX, mCenterY);	
		rQuest2Animation.setFillAfter(true);
		rQuest2Animation.setDuration(duration);
	}



	private void DTVListDealLeftAndRightKey(int mode){
		switch(mode){
			case 0:  //left
				//LinearLayoutListView.startAnimation(lQuest1Animation); 
				if((service_type == TVProgram.TYPE_RADIO)&&(favor!=true)){
					getListData(0);
					Text_title.setText(R.string.tv);
					service_type = TVProgram.TYPE_TV;
					myAdapter.notifyDataSetChanged();
				}
				else if((service_type == TVProgram.TYPE_TV)&&(favor!=true)){
					Log.d(TAG,"##########"+class_total);
					if(class_total>0){	
						service_type = -1;
					   	cur_class_no = class_total-1;
						Text_title.setText(mTVGroup[cur_class_no].getName());
						getClassData(mTVGroup[cur_class_no].getID());
						myAdapter.notifyDataSetChanged();
					}
					else{					
						Text_title.setText(R.string.favorite);
						getListFavorite();
						myAdapter.notifyDataSetChanged();
						favor=true;
					}	
				}	
				else if((favor!=true)&&(service_type != TVProgram.TYPE_TV)&&(service_type != TVProgram.TYPE_RADIO)){
					if(cur_class_no>0&&class_total>0)
					{
						service_type = -1;
						cur_class_no --;
						Text_title.setText(mTVGroup[cur_class_no].getName());
						getClassData(mTVGroup[cur_class_no].getID());
						myAdapter.notifyDataSetChanged();
						
					}
					else
					{
						Text_title.setText(R.string.favorite);
						getListFavorite();
						myAdapter.notifyDataSetChanged();
						favor=true;
					}
				}
				else if(favor==true)
				{
					getListData(1);
					Text_title.setText(R.string.radio);
					service_type = TVProgram.TYPE_RADIO;
					myAdapter.notifyDataSetChanged();
					favor=false;
				}	
				setFocusPosition();
				//LinearLayoutListView.startAnimation(lQuest2Animation); 
			
				break;	
			case 1:
				//LinearLayoutListView.startAnimation(rQuest1Animation);  
				if(service_type == TVProgram.TYPE_TV)
				{
					getListData(1);
					Text_title.setText(R.string.radio);
					service_type = TVProgram.TYPE_RADIO;
					myAdapter.notifyDataSetChanged();
				}
				else if((service_type == TVProgram.TYPE_RADIO)&&(favor==false)){
					Text_title.setText(R.string.favorite);
					getListFavorite();
					myAdapter.notifyDataSetChanged();
					favor=true;
				}
				else if(favor==true)
				{
					Log.d(TAG,"##########"+class_total);
					if(class_total>0)
					{	
						service_type = -1;
					    cur_class_no = 0;
						Text_title.setText(mTVGroup[cur_class_no].getName());
						getClassData(mTVGroup[cur_class_no].getID());
						myAdapter.notifyDataSetChanged();
					}
					else
					{
						
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}	
					favor=false;
				}	
				else
				{
					if(cur_class_no<(class_total-1))
					{
						service_type = -1;
						cur_class_no ++;
						Text_title.setText(mTVGroup[cur_class_no].getName());
						getClassData(mTVGroup[cur_class_no].getID());
						myAdapter.notifyDataSetChanged();
					}
					else
					{
						getListData(0);
						Text_title.setText(R.string.tv);
						service_type = TVProgram.TYPE_TV;
						myAdapter.notifyDataSetChanged();
					}
					
				}	
				setFocusPosition();
				//LinearLayoutListView.startAnimation(rQuest2Animation);  
				break;
		}
	}


	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute; 
	private void pvr_time_init(){				
		Date date = new Date(getUTCTime());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss"); 
		String today = sdf.format(date); 
		Log.d(TAG,"####Today"+today);
		
		String[] para = today.split(" ", 6);
		if (para.length >= 6){
			mYear = Integer.valueOf(para[0]).intValue();
			mMonth= Integer.valueOf(para[1]).intValue()-1;
			mDay = Integer.valueOf(para[2]).intValue();
			mHour = Integer.valueOf(para[3]).intValue();
			mMinute = Integer.valueOf(para[4]).intValue();
		}
	}

	long time_test(int y,int m, int d,int h,int min,int s){		
		Calendar Calendar_sys=Calendar.getInstance();
		Calendar_sys.set( y, m,  d, h, min, s);
	    return Calendar_sys.getTime().getTime();
	}

	private void showPvrTimeSetDialog(Context context){
		final Context mContext = context;
		final CustomDialog mCustomDialog = new CustomDialog(mContext);
		mCustomDialog.showDialog(R.layout.dtv_pvr_time_set_dialog, new ICustomDialog(){
				public boolean onKeyDown(int keyCode, KeyEvent event){
					if(keyCode == KeyEvent.KEYCODE_BACK)
						mCustomDialog.dismissDialog();
					return false;
				}
				public void showWindowDetail(Window window){
					TextView title = (TextView)window.findViewById(R.id.title);
					title.setText(R.string.dtvplayer_pvr_duration_time);

					TimePicker tp;
  					DatePicker dp;	
					EditText editText;
					
					editText = (EditText)window.findViewById(R.id.edittext_time_duration);
					editText.setFilters(new  InputFilter[]{ new  InputFilter.LengthFilter(5)});

					dp=(DatePicker)window.findViewById(R.id.dPicker);
					pvr_time_init();
					dp.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener()    {
			 	  
					public void onDateChanged(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
							mYear=year;
							mMonth= monthOfYear;
							mDay=dayOfMonth;
						
						}
					});
		    
					tp=(TimePicker)window.findViewById(R.id.tPicker);
					tp.setIs24HourView(true);
					tp.setCurrentHour(mHour); 
					tp.setCurrentMinute(mMinute); 
					tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
						public void onTimeChanged(TimePicker view,int hourOfDay,int minute)      {
							mHour=hourOfDay;
							mMinute=minute;
						}    
					});	


					final int time_init = (int)(time_test(mYear,mMonth,mDay,mHour,mMinute,0)/1000);
					
					Button no = (Button)window.findViewById(R.id.no);
					no.setText(R.string.no);
					Button yes = (Button)window.findViewById(R.id.yes);
					yes.setText(R.string.yes);
					no.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							mCustomDialog.dismissDialog();
						}
					});	 
					yes.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {	
							//DTVPlayerStartRecording();
							//showPvrIcon();
							mCustomDialog.dismissDialog();
						}
					});	    
				}
			}	
		);		
	}

}

