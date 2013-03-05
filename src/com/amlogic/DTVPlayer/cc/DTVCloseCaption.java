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
import com.amlogic.widget.SingleChoiseDialog;

public class DTVCloseCaption extends DTVActivity{
	private static final String TAG="DTVCloseCaption";
	
	ListView myView;
	Intent intent = new Intent();
	Bundle bundle=null;
	IconAdapter adapter=null;
	List<Map<String, Object>> list = null;
	
	static int ListItemTitle[]={R.string.cc_switch,R.string.cc_mode_basic,R.string.cc_mode_advance,R.string.cc_digital_options};

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvvchip_settings);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		super.onConnected();
		DTVCloseCaptionUIInit();
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

	private void DTVCloseCaptionUIInit(){
		myView = (ListView) findViewById(R.id.settings_list);
		myView.setItemsCanFocus(false);
		myView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new IconAdapter(this);
		myView.setOnItemClickListener(new listOnItemClick());
		myView.setAdapter(adapter);

		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.cc_title);
	}
	
	class listOnItemClick implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
    		Log.d(TAG,"id---->>" + arg2+"----position----->"+position);
			final TextView info_cur = (TextView)arg1.findViewById(R.id.info);
 			switch((int)position){
				
 			    case 0:
 			    	if(getCCSwitch()){
						info_cur.setText(R.string.cc_switch_off);
						setCCSwitch(false);
 			    	}	
					else{ 
						info_cur.setText(R.string.cc_switch_on);
						setCCSwitch(true);
					}
					adapter.notifyDataSetChanged();
 			    	break;
 				case 1:
 					showBasicSelectionDialog(info_cur);
 					break;
 				case 2:
 					showAdvanceSelectionDialog(info_cur);
 					break;
				case 3:
 					intent.setClass(DTVCloseCaption.this,DTVCaptionOptions.class);
 					startActivityForResult(intent, 14);	
					onHide();
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
	
	private class IconAdapter extends BaseAdapter {
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
			if(getCCSwitch()==false){
				if (position==1||position==2) {
					return false;
				}
			}	
		    return super.isEnabled(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG,"position------->"+position);	
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

			if((getCCSwitch()==false)){  
				if (position==1||position==2){
					holder.text.setTextColor(Color.GRAY);
					holder.text2.setTextColor(Color.GRAY);
				}
			}	
			else{
				//holder.icon.setImageBitmap(mIcon1);		
				//convertView.setBackgroundColor(Color.TRANSPARENT); 
				holder.text.setTextColor(Color.WHITE);
				holder.text2.setTextColor(Color.YELLOW);
			}	
		  	
			switch(position){
				case 0: 
					if(getCCSwitch())
						holder.text2.setText(R.string.cc_switch_on);
					else 
						holder.text2.setText(R.string.cc_switch_off);
					holder.text2.setVisibility(View.VISIBLE);
					break;
				case 1: 
					holder.text2.setText(getBasicSelection());
					holder.text2.setVisibility(View.VISIBLE);
					break;
				case 2:
					holder.text2.setText(getAdvanceSelection());
					holder.text2.setVisibility(View.VISIBLE);
					break;
				case 3:
					holder.text2.setText(null);
					break;
			}
		  
			return convertView;
		}
	}	
			
	protected void onListItemClick(ListView l, View v, int position, long id){
		Log.d(TAG,"onListItemClick----->>"+position);	
	}
		
	public void setCCSwitch(boolean c){
		setConfig("tv:atsc:cc:enable",c);		
	}

	public boolean getCCSwitch(){
		boolean mode=true;
		mode=getBooleanConfig("tv:atsc:cc:enable");
		return mode;
	}

	public void setBasicSelectionMode(int value){
		return;				
	}

	public String[] getBasicSelectionItems(){
		String[] items=new String[]{"CC1","CC2","CC3"};
		return items;
	}

	public int getBasicSelectionMode(){
		int mode=0;
		return mode;
	}
	public String getBasicSelection(){
		String value=" ";
		return value;
	}

	public void setAdvanceSelectionMode(int value){
		return;				
	}

	public String[] getAdvanceSelectionItems(){
		String[] items=new String[]{"Service1","Service2","Service3"};

		return items;
	}
	public int getAdvanceSelectionMode(){
		int mode=0;
		return mode;
	}

	public String getAdvanceSelection(){
		String value=" ";
		return value;
	}

	
	private void showBasicSelectionDialog(TextView v){
		final TextView info_cur = v;

		int pr = getBasicSelectionMode();
		int pos = pr;
		String items[] = getBasicSelectionItems();
		
		new SingleChoiseDialog(DTVCloseCaption.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.cc_mode_basic));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				setBasicSelectionMode(which);
				info_cur.setText(getBasicSelection());
			}
		};
	}	


	private void showAdvanceSelectionDialog(TextView v){
		final TextView info_cur = v;

		int pr = getAdvanceSelectionMode();
		int pos = pr;
		String items[] = getAdvanceSelectionItems();
		
		new SingleChoiseDialog(DTVCloseCaption.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.cc_mode_basic));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				setBasicSelectionMode(which);
				info_cur.setText(getBasicSelection());
			}
		};
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
				case 14:
					onShow();
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

	private  void onHide(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.INVISIBLE);
	} 
	
	private void onShow(){
		RelativeLayout RelativeLayoutParent = (RelativeLayout)findViewById(R.id.RelativeLayoutParent);
		RelativeLayoutParent.setVisibility(View.VISIBLE);
	}
}


