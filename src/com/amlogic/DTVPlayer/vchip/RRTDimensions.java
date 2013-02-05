package com.amlogic.DTVPlayer;

import android.util.Log;
import android.os.Bundle;
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
	
	private  static String[] ListItemTitle=null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.dtvsettings);
		RRTDimensionsUIInit();
	}

	private void RRTDimensionsUIInit(){
		getData();

		if (ListItemTitle == null) {
			Log.d(TAG, "No dimension data of this region, will not display!");
			finish();
		}

		//title		
		TextView Title=(TextView)findViewById(R.id.title);
		Title.setTextColor(Color.YELLOW);
		Title.setText(R.string.settings_title);

		//listview
		myView = (ListView) findViewById(R.id.settings_list);
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
			bundle = new Bundle();
			bundle.putString("Dimension", ListItemTitle[(int)position]);
			bundle.putInt("Index",(int)position);
			intent.putExtras(bundle);
			intent.setClass(RRTDimensions.this,RRTDimensionsEnter.class);
			startActivityForResult(intent,30);
			onHide();
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
			ImageView icon;
		    TextView   info; 
		    ImageButton  iboolean;
		    ImageView icon1;
		}

		public IconAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
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
			holder.info.setVisibility(View.INVISIBLE);
			holder.icon1.setVisibility(View.INVISIBLE);
			holder.text.setText(ListItemTitle[position]);
			holder.text.setTextColor(Color.WHITE);
			//holder.icon.setImageBitmap(mIcon1);		

			return convertView;
		}
	}	
			
	protected void onListItemClick(ListView l, View v, int position, long id){
		Log.d(TAG,"onListItemClick---"+position);	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		int p=-1;		
		if(data!=null){
			Bundle bundle =data.getExtras();
			p = bundle.getInt("position");			
		}		
		
		if(resultCode == RESULT_OK){
			switch(requestCode)
			{
			  case 30:
			 	  onShow();
				  break; 
			}
		}	
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

	   switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			break;		
		case KeyEvent.KEYCODE_DPAD_RIGHT:	
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
	 
