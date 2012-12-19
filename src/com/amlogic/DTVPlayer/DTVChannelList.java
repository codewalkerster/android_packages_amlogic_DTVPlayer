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
import android.graphics.Color;

public class DTVChannelList extends DTVActivity{
	private static final String TAG="DTVChannelList";
	ListView ListView_channel=null;
	private int cur_select_item=0;
	private IconAdapter myAdapter=null;
	private TVProgram[]  mTVProgramList=null;

	int db_id=-1;
	
	private void getListData(){
		mTVProgramList = TVProgram.selectByType(this,TVProgram.TYPE_TV,true);
	}


	private void DTVChannelListUIInit(){		
	    Bundle bundle = this.getIntent().getExtras();
		if(bundle!=null){
	    	db_id = bundle.getInt("db_id");
		}	
		Log.d(TAG,"db_id="+db_id);
		getListData();
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
		/*get list data*/
		DTVChannelListUIInit();
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
				DTVPlayerPlayById(db_id);
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
			holder.prono.setText(Integer.toString(mTVProgramList[position].getNumber().getNumber()));
			holder.text.setText(mTVProgramList[position].getName());

			if(db_id == mTVProgramList[position].getID()){  
				//convertView.setBackgroundColor(Color.RED);  
				holder.text.setTextColor(Color.YELLOW);
			}	
			else{
				//convertView.setBackgroundColor(Color.TRANSPARENT); 
				holder.text.setTextColor(Color.WHITE);
			}	
		
			//if(mTVProgramList[position].getLockStatus()){
			if(true){
				holder.icon.setBackgroundResource(R.drawable.dtvplayer_icon_lock); 
			}	
			else{
				holder.icon.setBackgroundResource(Color.TRANSPARENT);
			}

			//if(mTVProgramList[position].getLockStatus()){
			if(true){
				holder.icon_fav.setBackgroundResource(R.drawable.dtvplayer_icon_fav); 
			}	
			else{
				holder.icon_fav.setBackgroundResource(Color.TRANSPARENT);
			}	

			//if(mTVProgramList[position].getLockStatus()){
			if(true){
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
				
				//setFocusPosition();
				break;		
			case KeyEvent.KEYCODE_DPAD_RIGHT:	
				
				//setFocusPosition();
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:			
				if(cur_select_item== ListView_channel.getCount()-1)
			    	ListView_channel.setSelection(0); 			
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if(cur_select_item== 0)
					ListView_channel.setSelection(ListView_channel.getCount()-1); 
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				//showPvrDialog();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}	  
 
}

