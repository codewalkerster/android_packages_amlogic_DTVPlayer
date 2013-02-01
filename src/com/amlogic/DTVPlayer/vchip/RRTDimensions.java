package com.amlogic.DTVPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.SystemProperties;

import com.amlogic.tvutil.TVDimension;

public class RRTDimensions extends Activity {
	private final static String TAG="RRTDimensions";
	public  static final int RRT_REGION = 1;
	ListView myView;
	Intent intent = new Intent();
	Bundle bundle=null;
	private static SharedPreferences mLast = null;
	IconAdapter adapter=null;
	List<Map<String, Object>> list = null;
	
	private String[] ListItemTitle=null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dimensions);

		Window w = getWindow();
		WindowManager.LayoutParams wl = w.getAttributes();
		wl.x = -200;
		wl.y = -100;
		w.setAttributes(wl);

		getData();

		if (ListItemTitle == null) {
			Log.d(TAG, "No dimension data of this region, will not display!");
			finish();
		}

		myView = (ListView) findViewById(R.id.sub_list_dimensions);
		myView.setItemsCanFocus(false);
		myView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new IconAdapter(this);
		myView.setOnItemClickListener(new listOnItemClick());
		myView.setAdapter(adapter);
		
	}

	public static TVDimension[] mTVDimension=null;

	public static TVDimension[] getTVDimension(){
		return mTVDimension;
	}
	
	private void getData(){
		mTVDimension = TVDimension.selectUSDownloadable(this);
		if(mTVDimension!=null){
			ListItemTitle = new String[mTVDimension.length];
			for(int i=0;i<mTVDimension.length;i++){
				ListItemTitle[i]= mTVDimension[i].getName();
			}
		}
	}


	class listOnItemClick implements OnItemClickListener{
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long position) {   
	            
			System.out.println("id---------" + arg2);
			Log.d("#####","#####################"+position);
			bundle = new Bundle();
			bundle.putString("Dimension", ListItemTitle[(int)position]);
			bundle.putInt("Index",position);
			intent.putExtras(bundle);
			intent.setClass(RRTDimensions.this,RRTDimensionsEnter.class);
			startActivityForResult(intent,30);		   
	    }

	 }
	private static class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;

		private Context cont;
		private List<Map<String, Object>> listItems;

		static class ViewHolder {
			TextView text;
			TextView   text2; 
			//ImageView icon;
			//ImageButton  iboolean;
			//ImageView icon1;
		}

		public IconAdapter(Context context) {
			super();
			cont = context;
			//listItems = list;
			mInflater=LayoutInflater.from(context);
			//mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.p1);
		}

		public int getCount() {
			if (ListItemTitle != null)
				return ListItemTitle.length;
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
			ViewHolder holder;
			if (convertView == null) {
			   convertView = mInflater.inflate(R.layout.list_item, null);
			   holder = new ViewHolder();
			   holder.text = (TextView) convertView.findViewById(R.id.title);
			   holder.text2 = (TextView) convertView.findViewById(R.id.text2);
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
			//holder.text2.setText(ListItemTitle[position]);

			//holder.icon.setImageBitmap(mIcon1);		
			//convertView.setBackgroundColor(Color.TRANSPARENT); 
			holder.text.setTextColor(Color.WHITE);
			holder.text2.setTextColor(Color.WHITE);

			return convertView;
		}
	}	
			
	protected void onListItemClick(ListView l, View v, int position, long id){
		Log.d("#####","#####################"+position);	
	}

	void setText(int position,int res_id){
		/*
		 Map<String, Object> map;
		 map = list.get(position); 
		 map = list.get(position);
		 map.remove("myMenuItemId1");
		 map.put("myMenuItemId1", this.getString(res_id) );
		 list.remove(position);
		 list.add(position,map);
		 */
		 
		 adapter.notifyDataSetChanged();
	}	
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.d("#################","#######onActivityResult");
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
			Log.d(TAG,"#######################KEYCODE_DPAD_LEFT");
			break;		
		case KeyEvent.KEYCODE_DPAD_RIGHT:	
			Log.d(TAG,"#######################KEYCODE_DPAD_RIGHT");
			break;
		case KeyEvent.KEYCODE_BACK:	
			//this.onBack();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}	  

	
	
}
	 
