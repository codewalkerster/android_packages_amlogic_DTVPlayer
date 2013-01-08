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
import android.widget.AdapterView.OnItemClickListener;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.text.method.*;
import java.lang.reflect.Field;

public class DTVVChip extends DTVActivity{
	private static final String TAG="DTVVChip";
	
	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvvchip_settings);
		DTVVChipUIInit();
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
	
	
	ListView myView;
	Intent intent = new Intent();
	Bundle bundle=null;
	IconAdapter adapter=null;
	static int ListItemTitle[]={
		R.string.parent_control_switch,
		R.string.parent_control_TV,
		R.string.parent_control_MPAA,
		R.string.parent_control_Canada_English,
		R.string.parent_control_Canada_French,
		R.string.parent_control_rrt_set,
		R.string.parent_control_rrt_reset
	};

	private void DTVVChipUIInit(){
		/*
		Window w = getWindow();
		WindowManager.LayoutParams wl = w.getAttributes();
		wl.x = -700;
		wl.y = -100;
		w.setAttributes(wl);
		*/
		myView = (ListView) findViewById(R.id.settings_list);
		myView.setItemsCanFocus(false);
		myView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new IconAdapter(this);
		myView.setOnItemClickListener(new listOnItemClick());
		myView.setAdapter(adapter);

		//title		
		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.parent_control_title);

	}


	class listOnItemClick implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
        	Log.d(TAG,"id---->>" + arg2+"----position----->"+position);
 			switch((int)position){
				case 0:
					if(getSwitch()){
						setSwitch(false);
					}	
					else{
						setSwitch(true);
					}
					updata_list();
 					break;
 				case 1:
 					intent.setClass(DTVVChip.this,DTVVchipTv.class);
 					startActivityForResult(intent, 111);
 					break;
 				case 2:
 					intent.setClass(DTVVChip.this,DTVVchipMpaa.class);
 					startActivityForResult(intent, 112);
 					break;
					
 				case 3:
 					intent.setClass(DTVVChip.this,VchipCanadaEnglishActivity.class);
 					startActivityForResult(intent, 113);
 					break;
 				case 4:
 					intent.setClass(DTVVChip.this,VchipCanadaFrenchActivity.class);
 					startActivityForResult(intent, 114);
 					break;
				case 5:
					intent.setClass(DTVVChip.this,RRTDimensions.class);
 					startActivityForResult(intent, 115);
 					break;
 				
 			}
			
			/*
			int version = Integer.valueOf(android.os.Build.VERSION.SDK);
			if (version >= 5) {
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout); 
			}
        	*/   	
			
        }      	
    }
	
	private  class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;

		private Context cont;
		private List<Map<String, Object>> listItems;

		class ViewHolder {
			TextView text;
		    TextView   text2; 		
		}

		public IconAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
		}

		public int getCount() {
			return ListItemTitle.length;
		}

		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public boolean isEnabled(int position) {
			if(getSwitch()==false){
				if (position>=1) {
				     return false;
				}
			}	
		    return super.isEnabled(position);
		}


		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG,"position--->>"+position);	
			ViewHolder holder;
			if (convertView == null) {
			   convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
			   holder = new ViewHolder();
			   holder.text = (TextView) convertView.findViewById(R.id.text);
			   holder.text2 = (TextView) convertView.findViewById(R.id.info);
			   //holder.iboolean = (ImageButton)convertView.findViewById(R.id.iboolean);
			   //holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
			   convertView.setTag(holder);
			}else {
			  // Get the ViewHolder back to get fast access to the TextView
			  // and the ImageView.
			  holder = (ViewHolder) convertView.getTag();
			  }
			
			  // Bind the data efficiently with the holder.
			  holder.text.setText(ListItemTitle[position]);
			  holder.text2.setText(null);
			  
			  if(getSwitch()==false){  
				  if (position>=1)
					 holder.text.setTextColor(Color.GRAY);
			  }	
			  else{
				   //holder.icon.setImageBitmap(mIcon1);		
				   //convertView.setBackgroundColor(Color.TRANSPARENT); 
					holder.text.setTextColor(Color.WHITE);
			  }	
			  holder.text2.setTextColor(Color.YELLOW);
			  switch(position){
			  	case 0: 
			  		if(getSwitch()){
						holder.text2.setVisibility(View.VISIBLE);
			  			holder.text2.setText(R.string.parent_control_switch_on);
						holder.text.setTextColor(Color.WHITE);
			  		}	
					else {
						holder.text2.setVisibility(View.VISIBLE);
						holder.text2.setText(R.string.parent_control_switch_off);
					}	
				break;
			  }
			  
			  return convertView;
		}
	}	
			
	protected void onListItemClick(ListView l, View v, int position, long id){
		Log.d("#####","#####################"+position);	
	}
		
	public void setSwitch(boolean c){
		setConfig("tv:vchip:enable", c);
		return;				
	}

	public boolean getSwitch(){
		boolean mode=true;
		mode = getBooleanConfig("tv:vchip:enable");
		return mode;
	}
	
	void updata_list(){			 
		 adapter.notifyDataSetChanged();
	}	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onActivityResult");
		int p=-1;
		
		if(data!=null)
		{
			Bundle bundle =data.getExtras();
			p = bundle.getInt("position");			
		}		
		
		if(resultCode == RESULT_OK)
		{
			switch(requestCode)
			{
			  case 11:
				 switch(p)
				  {	
				    case 0:
				    	setSwitch(true);
				    	updata_list();
				    	break;
				    case 1:
				    	setSwitch(false);
				    	updata_list();
				    	break;
				  }
				  break;
				  
			}
		}	
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			Log.d(TAG,"KEYCODE_DPAD_LEFT");
			break;		
		case KeyEvent.KEYCODE_DPAD_RIGHT:	
			Log.d(TAG,"KEYCODE_DPAD_RIGHT");
			break;
		case KeyEvent.KEYCODE_BACK:	
			setResult(RESULT_OK,null);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}	  

}

