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

public class DTVCaptionOptions extends DTVActivity {
	private final static String TAG="###DigitalCaptionOptions###";
	ListView myView;
	Intent intent = new Intent();
	Bundle bundle=null;
	private static SharedPreferences mLast = null;
	IconAdapter adapter=null;
	List<Map<String, Object>> list = null;
	
	int ListItemTitle[]={
		R.string.cc_digital_options_size,
		R.string.cc_digital_options_font_sytle,
		R.string.cc_digital_options_foreground_color,
		R.string.cc_digital_options_foreground_opacity,
		R.string.cc_digital_options_background_color,
		R.string.cc_digital_options_background_opacity,
		R.string.cc_digital_options_retune_default
	};

	public void onCreate(Bundle savedInstanceState){
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvvchip_settings);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		DTVCaptionOptionsUIInit();
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

	private void DTVCaptionOptionsUIInit(){
		myView = (ListView) findViewById(R.id.settings_list);
		myView.setItemsCanFocus(false);
		myView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new IconAdapter(this);
		myView.setOnItemClickListener(new listOnItemClick());
		myView.setAdapter(adapter);

		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.cc_digital_options);
	}

	class listOnItemClick implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
			Log.d(TAG,"id---->>" + arg2+"----position----->"+position);
 			switch((int)position){
				
 				//FontSize
 			    case 0:  
 			    	showFontSizeDialog(null);
 			    	break;
				/*	
				//FontStyle	
				 case 1:
 			    	bundle = new Bundle();
 					bundle.putInt("FontStyle",0);
 					intent.putExtras(bundle);
 					intent.setClass(DigitalCaptionOptions.this,FontStyle.class);
 					startActivityForResult(intent, 21);					
 			    	break;
				//ForegroundColor	
				 case 2:
 			    	bundle = new Bundle();
 					bundle.putInt("ForegroundColor",0);
 					intent.putExtras(bundle);
 					intent.setClass(DigitalCaptionOptions.this,ColorMode.class);
 					startActivityForResult(intent, 22);
 			    	break;
				//ForegroundOpacity	
				 case 3:
 	
 			    	bundle = new Bundle();
 					bundle.putInt("ForegroundOpacity",0);
 					intent.putExtras(bundle);
 					intent.setClass(DigitalCaptionOptions.this,OpacityMode.class);
 					startActivityForResult(intent, 23);
 					
 			    	break;
				//BackgroundColor	
				 case 4:
 			    
 			    	bundle = new Bundle();
 					bundle.putInt("BackgroundColor", 0);
 					intent.putExtras(bundle);
 					intent.setClass(DigitalCaptionOptions.this,ColorMode.class);
 					startActivityForResult(intent, 24);
 			    	break;
				//BackgroundOpacity	
				 case 5:
 			   
 			    	bundle = new Bundle();
 					bundle.putInt("BackgroundOpacity", 0);
 					intent.putExtras(bundle);
 					intent.setClass(DigitalCaptionOptions.this,OpacityMode.class);
 					startActivityForResult(intent, 25);
 			    	break;
				//ReturnToDefault	
				 case 6:
				 	
 			    	break;
				*/	
 					
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
		    //ImageView icon;
		    //ImageButton  iboolean;
		}

		public IconAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
			//mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.p1);  
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
			
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.text2 = (TextView) convertView.findViewById(R.id.info);
					convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Bind the data efficiently with the holder.
			holder.text.setText(ListItemTitle[position]);

			//holder.icon.setImageBitmap(mIcon1);		
			//convertView.setBackgroundColor(Color.TRANSPARENT); 
			holder.text.setTextColor(Color.WHITE);
			holder.text2.setTextColor(Color.WHITE);
			holder.text2.setTextColor(Color.YELLOW);
			switch(position){
				//FontSize
 			    case 0:  
					holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getFontSize());
 			    	break;
				//FontStyle	
				 case 1:
				 	holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getFontStyle());			
 			    	break;
				//ForegroundColor	
				 case 2:
				 	holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getForegroundColor());	
 			    	break;
				//ForegroundOpacity	
				 case 3:
 					holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getForegroundOpacity());	
 			    	break;
				//BackgroundColor	
				 case 4:
 			    	holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getBackgroundColor());	
 			    	break;
				//BackgroundOpacity	
				 case 5:
 			    	holder.text2.setVisibility(View.VISIBLE);
 			    	holder.text2.setText(getBackgroundOpacity());	
 			    	break;
	
			}
			  
			return convertView;
		}
	}	
			
	protected void onListItemClick(ListView l, View v, int position, long id){
		Log.d("#####","#####################"+position);	
	}
	  
	void setText(int position,int res_id){
		 adapter.notifyDataSetChanged();
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
			switch(requestCode)
			{
			  case 11:
			  /*
				 switch(p)
				  {	
				    case 0:
				    	setScanMode("air");
				    	setText(0,R.string.cc_switch_on);

				    	break;
				    case 1:
				    	 setScanMode("cable");
				    	 setText(0,R.string.cc_switch_off);
				    	break;
				  }
				  */
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

	int FontSize_menu_items[] = {
		R.string.cc_digital_options_size_standard,
		R.string.cc_digital_options_size_small,
		R.string.cc_digital_options_size_large
	};

	int FontStyle_menu_items[] = {
		R.string.cc_digital_options_font_sytle0,
		R.string.cc_digital_options_font_sytle1,
		R.string.cc_digital_options_font_sytle2,
		R.string.cc_digital_options_font_sytle3,
		R.string.cc_digital_options_font_sytle4,
		R.string.cc_digital_options_font_sytle5,
		R.string.cc_digital_options_font_sytle6,
		R.string.cc_digital_options_font_sytle7 
	};

	int ColorMode_menu_items[] = {
		R.string.cc_digital_options_color_black,
		R.string.cc_digital_options_color_red,
		R.string.cc_digital_options_color_green,
		R.string.cc_digital_options_color_blue,
		R.string.cc_digital_options_color_yellow,
		R.string.cc_digital_options_color_magenta,
		R.string.cc_digital_options_color_cyan
	 };
	
	int OpacityMode_menu_items[] = {
		R.string.cc_digital_options_opacity_default,
		R.string.cc_digital_options_opacity_transparent,
		R.string.cc_digital_options_opacity_translucent,
		R.string.cc_digital_options_opacity_solid,
		R.string.cc_digital_options_opacity_flashing
	};

	private int getFontSizeMode(){
		int mode =0;
		mode = getIntConfig("tv:atsc:cc:fontsize");
		return mode;
	}

	private String getFontSize(){
		int mode =0;
		mode = getIntConfig("tv:atsc:cc:fontsize");
		return getString(FontSize_menu_items[mode]);
	}

	private void setFontSize(int mode){
		setConfig("tv:atsc:cc:fontsize",mode);
	}
	
	private String getFontStyle(){
		int mode = 0;
		mode = getIntConfig("tv:atsc:cc:fontstyle");
		return getString(FontStyle_menu_items[mode]);
	}	

	private void setFontStyle(int mode){
		setConfig("tv:atsc:cc:fontstyle",mode);
	}
	private String getForegroundColor(){
		int mode = getIntConfig("tv:atsc:cc:foregroundcolor");
		return getString(ColorMode_menu_items[mode]);
	}

	private void setForegroundColor(int mode){
		setConfig("tv:atsc:cc:foregroundcolor",mode);
	}
	
	private String getForegroundOpacity(){
		int mode = getIntConfig("tv:atsc:cc:foregroundopacity");
		return getString(OpacityMode_menu_items[mode]);
	}	

	private void setForegroundOpacity(int mode){
		setConfig("tv:atsc:cc:foregroundopacity",mode);
	}
	private String getBackgroundColor	(){
		int mode = getIntConfig("tv:atsc:cc:backgroundcolor");
		return getString(ColorMode_menu_items[mode]);
	}
	private void setBackgroundColor(int mode){
		setConfig("tv:atsc:cc:backgroundcolor",mode);
	}
	
	private String getBackgroundOpacity	(){
		int mode = getIntConfig("tv:atsc:cc:backgroundopacity");
		return getString(OpacityMode_menu_items[mode]);
	}

	private void setBackgroundOpacity(int mode){
		setConfig("tv:atsc:cc:backgroundopacity",mode);
	}


	private void showFontSizeDialog(TextView v){
		final TextView info_cur = v;

		int pr = getFontSizeMode();
		int pos = pr;
		String items[] = new String[FontSize_menu_items.length];
		for(int i=0;i<FontSize_menu_items.length;i++){
			items[i]=getString(FontSize_menu_items[i]);
		}
		
		new SingleChoiseDialog(DTVCaptionOptions.this,items,pos){
			public void onSetMessage(View v){
				((TextView)v).setText(getString(R.string.cc_digital_options_size));
			}

			public void onSetNegativeButton(){
				
			}
			public void onSetPositiveButton(int which){
				setFontSize(which);
				info_cur.setText(getFontSize());
			}
		};
	}	
	
}

