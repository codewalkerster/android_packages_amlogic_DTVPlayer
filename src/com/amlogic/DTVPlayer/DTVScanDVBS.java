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

public class DTVScanDVBS extends DTVActivity{
	private static final String TAG="DTVScanDVBS";
	private ListView mListView=null;
	private DvbsScanSubmenuAdapter myAdapter=null;
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvsettings);
		DTVScanDVBS_UIInit();
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
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

	private void DTVScanDVBS_UIInit(){ 
		//title		
		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.scan_program);

		//listview
		mListView = (ListView)findViewById(R.id.settings_list);
		myAdapter = new DvbsScanSubmenuAdapter(this,null);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
 			public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3){
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);
				
				switch(arg2){
					case 0:
						{	
							Intent intent_dish_setup = new Intent();
							intent_dish_setup.setClass(DTVScanDVBS.this, DTVScanDvbsConfig.class);
 							startActivity(intent_dish_setup);
							DTVScanDVBS.this.finish();
						}	
						break;
					case 1:     //DB management
						{
							Intent intent_db_management = new Intent();
							intent_db_management.setClass(DTVScanDVBS.this, DTVScanDvbsDBManagement.class);
							startActivityForResult(intent_db_management,2);
						}
						break;
					case 2:
						{
							Intent intent_unicable_config = new Intent();
							intent_unicable_config.setClass(DTVScanDVBS.this,DTVScanDvbsUnicableConfig.class);
							startActivityForResult(intent_unicable_config,3);
						}
						break;
				}
			}
        	    
        });

		mListView.setAdapter(myAdapter);
		mListView.requestFocus();
		
	}


	/*disable HOME key*/
	@Override 
	public void onAttachedToWindow()  {  
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);  
		super.onAttachedToWindow();  
	} 

	private static class DvbsScanSubmenuAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		
		private Context cont;
		private String[] listItems;

	 	static class ViewHolder {
			TextView text;
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}
	
		public DvbsScanSubmenuAdapter(Context context, String[] list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_subtitle);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_subtitle);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_subtitle);

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

			switch(position){
				case 0:
					holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_dish_setup);
					break;
				case 1:
					holder.icon.setImageBitmap(mIcon2);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_db_management);
					break;
				case 2:
					holder.icon.setImageBitmap(mIcon3);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_unicable_config);
					break;						
			}
			  
			return convertView;
		}
		
	}

	public boolean onKeyDown(int keyCode, KeyEvent event){	
		if(!connected){
			return true;
			}
		switch (keyCode){     
			case KeyEvent.KEYCODE_BACK:
		 
				break;
		}
	    
		return super.onKeyDown(keyCode, event);
	}


}

