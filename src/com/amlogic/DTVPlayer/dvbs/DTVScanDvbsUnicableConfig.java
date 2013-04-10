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

public class DTVScanDvbsUnicableConfig extends DTVActivity{
	private final String TAG = "DTVScanDvbsUnicableConfig";
	private ListView listview;
	private static String[] DATA = null;
	private Toast toast=null;
	private View dvbs_dbm_settings;
	int m_count = 0;
	ProgressDialog  m_pDialog;
	
	private ListView mListView=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
  		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtvsettings);
	}

	public void onConnected(){
		Log.d(TAG, "connected");
		DTVScanDvbsUnicableConfig_UIInit();
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

	private void DTVScanDvbsUnicableConfig_UIInit(){
		TextView title  = (TextView)this.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		title.setText(R.string.dvbs_unicable_config);

		mListView = (ListView)this.findViewById(R.id.settings_list);
		mListView.setAdapter(new DvbsUnicableAdapter(this));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				TextView info = (TextView)arg1.findViewById(R.id.info);

				/*
				DVBUnicableSetting setting = getUnicableSetting();
				if (setting == null) {
					Log.d(TAG, "Cannot get unicable setting from service");
					return;
				}
				*/
				switch(arg2){
					case 0:
						setUnicableSwitchStatus(!getUnicableSwitchStatus());
						refreshListData();
						break;
					case 1: 
						
						int band = getUserBand();
						if(band>=0&&band<7){
							info.setText("LNB"+String.valueOf(band+2));
							setUserBand(band + 1);
						}
						else if(band>=7){
							info.setText("LNB1");
							setUserBand(0);
						}
						
						break;
					case 2:
						showUnicableConfigEditDia();
						break;
				}
			}	
		});	
	}
	 
	protected void onStop(){
		Log.d(TAG,"onStop");	
		super.onStop();
	}

	private List<String> getUnicableUserDefinedData(){

		List<String> dataList = new ArrayList<String>();
		/*
		int[] ubf = setting.getUbFreqs();
		usfs = setting.getUbFreqs();
		for (int i = 0; i<ubf.length; i++) {
			dataList.add(""+ubf[i]);
		}
		*/
		return dataList;
	}

	private boolean getUnicableSwitchStatus(){
		return true;
	}

	private void setUnicableSwitchStatus(boolean b){
		
	}

	private int getUserBand(){
		int band = 0;
		return band;
	}

	private void setUserBand(int band){
	
	}

	private AlertDialog.Builder builder;
	public void showEditUnicableFreDialog(View v,int pos){
		final View view = v;
		final int position = pos;
		builder = new AlertDialog.Builder(this);	
		final EditText editText = new EditText(this);
		editText.setFilters(new  android.text.InputFilter[]{ new  android.text.InputFilter.LengthFilter(4)});
		//editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		builder.setTitle(R.string.edit_title);
		TextView desFreText =(TextView) view.findViewById(R.id.edit_fre);
		editText.setText(desFreText.getText().toString());
		builder.setView(editText); 

		AlertDialog alert = builder.create();

		alert.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode)
				{	
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						String fre = editText.getText().toString();
						if(fre==null||fre.equals("")){
							editText.setText(null);
							toast = Toast.makeText(
							DTVScanDvbsUnicableConfig.this, 
						    	R.string.invalid_input,
						    	Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
						else{
							if(Integer.parseInt(fre)==0){
								editText.setText(null);
								toast = Toast.makeText(
								DTVScanDvbsUnicableConfig.this, 
							    	R.string.invalid_input,
							    	Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
							else{
								TextView des = (TextView)view.findViewById(R.id.edit_fre);
								des.setText(editText.getText().toString());
								Log.d(TAG,"Old usf"+usfs[position]);
								//usfs[position]= Integer.parseInt(editText.getText().toString());
								//Log.d(TAG,"New usf"+usfs[position]);
								dialog.cancel();
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

		
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
						public void onShow(DialogInterface dialog) {
			
							}         
							}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {	
						}         
						});	
		alert.show();	
		alert.getWindow().setLayout(500, -200);
		WindowManager.LayoutParams lp=alert.getWindow().getAttributes();
		lp.dimAmount=0.5f;
		alert.getWindow().setAttributes(lp);
		alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}


	private AlertDialog.Builder diaBuilder;
	private View dvbs_unicable_user_list;
	int[] usfs = null;
	private void showUnicableConfigEditDia(){
		//final DVBUnicableSetting unicable_setting = setting;
		ContentValues values=null;
		diaBuilder = new AlertDialog.Builder(this);
		LayoutInflater layoutInflater = LayoutInflater.from(this);  
		
 	 	dvbs_unicable_user_list = layoutInflater.inflate(R.layout.dvbs_unicable_list_dia, null); 
		diaBuilder.setTitle(R.string.dvbs_unicable_user_define);
		usfs = new int[8];
		List<String> dataList = getUnicableUserDefinedData();
		ListView UnicableListView = (ListView)dvbs_unicable_user_list.findViewById(R.id.dvbs_sub_list); 
		
		UnicableListView.setAdapter(new DvbsUnicableUserDefineAdapter(this,dataList));

		UnicableListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				System.out.println("onItemSelected arg0 " + arg0);
				System.out.println("onItemSelected arg1 " + arg1);
				System.out.println("onItemSelected arg2 " + arg2);
				System.out.println("onItemSelected arg3 " + arg3);

				showEditUnicableFreDialog(arg1,arg2);
			}
        	    
        });
		
		diaBuilder.setView(dvbs_unicable_user_list);
		diaBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
			  	/*
				unicable_setting.setUbFreqs(usfs);
				mDvb.setUnicableSetting(unicable_setting);
				*/
				dialog.dismiss();
              }
          });
       diaBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
                  }
              });

		AlertDialog alert = diaBuilder.create();
		alert.setOnShowListener(new DialogInterface.OnShowListener(){
							public void onShow(DialogInterface dialog) {
								
								}         
								}); 	

		alert.setOnDismissListener(new DialogInterface.OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
							}         
							});	

		alert.show();	
		
	}

	/*
	private DVBUnicableSetting getUnicableSetting() {
		DVBUnicableSetting set = null;
		
		if (mDvb != null) {
			set = new DVBUnicableSetting();
			mDvb.getUnicableSetting(set);
		}
		return set;
	}
	*/

	public void refreshListData(){
		((DvbsUnicableAdapter)mListView.getAdapter()).notifyDataSetChanged();
	}

	private class DvbsUnicableAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		
		private Context cont;
		private String[] listItems;

	 	class ViewHolder {
			
			ImageView icon;
			TextView text;
			ImageButton  iboolean;
			ImageView icon1;
			TextView   info; 
		}
	
		public DvbsUnicableAdapter(Context context) {
			super();
			cont = context;
			mInflater=LayoutInflater.from(context);
			mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_picture_size);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.settings_channel_search);
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
		
		public boolean isEnabled(int position) {
			if (!getUnicableSwitchStatus()){
				if (position>=1) {
					return false;
				}
			}	
			return super.isEnabled(position);
		}
	
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dtvsettings_list_item, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon1 = (ImageView)convertView.findViewById(R.id.icon1);
				holder.info = (TextView)convertView.findViewById(R.id.info);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			/*
			DVBUnicableSetting setting = getUnicableSetting();
			if (setting == null) {
				Log.d(TAG, "Cannot get unicable setting from service");
				return convertView;
			}
			*/
			boolean unicable_on = getUnicableSwitchStatus();
			
			if(unicable_on==false){  
				if (position>=1){
					holder.text.setTextColor(Color.DKGRAY);
					holder.icon1.setVisibility(View.INVISIBLE); 
				}	
			}	
			else{
				holder.text.setTextColor(Color.WHITE);
			}		

			switch(position){
				 case 0:
				 	holder.icon.setImageBitmap(mIcon1);
					holder.icon.setVisibility(View.INVISIBLE);
					holder.info.setVisibility(View.VISIBLE);
					holder.icon1.setVisibility(View.INVISIBLE);
					holder.text.setText(R.string.dvbs_unicable_switch);
					if (unicable_on==true){			   
						//holder.icon1.setBackgroundResource(R.drawable.select_round_2); 
						holder.info.setText(R.string.on);
					}
					else
					{
						//holder.icon1.setBackgroundResource(R.drawable.select_round_1); 
						holder.info.setText(R.string.off);
					}	  
					
					break;
				case 1:
					holder.icon.setImageBitmap(mIcon2);				
					holder.info.setVisibility(View.INVISIBLE);	
					if (unicable_on==true){	
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
						holder.info.setVisibility(View.VISIBLE);	
						//holder.icon1.setBackgroundResource(R.drawable.pull_right_1); 
						
						int band = getUserBand();
						if(band>=0&&band<=7){
							holder.info.setText("LNB"+String.valueOf(band+1));
						}
					}
					else{
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
					}
					
					holder.text.setText(R.string.dvbs_unicable_user_band);
					break;
				case 2:
					holder.icon.setImageBitmap(mIcon3);				
					holder.info.setVisibility(View.INVISIBLE);	
					if (unicable_on==true){	
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
						//holder.icon1.setBackgroundResource(R.drawable.pull_down_1); 
					}
					else{
						holder.icon.setVisibility(View.INVISIBLE);
						holder.icon1.setVisibility(View.INVISIBLE);
					}
					holder.text.setText(R.string.dvbs_unicable_user_define);
					break;
			}
			  
			return convertView;
		}
	}


	private static class DvbsUnicableUserDefineAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private Context cont;
		private List<String> listItems;
		private String[] DATA;

	 	static class ViewHolder {			
			TextView text;
			TextView  fre;
		}
	
		public DvbsUnicableUserDefineAdapter(Context context,List<String> list) {
			super();
			cont = context;
			listItems = list;
			mInflater=LayoutInflater.from(context);

			DATA = new String[8];
			DATA[0]= cont.getResources().getString(R.string.dish_setup_conf_lnb_1);			
			DATA[1]= cont.getResources().getString(R.string.dish_setup_conf_lnb_2);
			DATA[2]= cont.getResources().getString(R.string.dish_setup_conf_lnb_3);
			DATA[3]= cont.getResources().getString(R.string.dish_setup_conf_lnb_4);
			DATA[4]= cont.getResources().getString(R.string.dish_setup_conf_lnb_5);
			DATA[5]= cont.getResources().getString(R.string.dish_setup_conf_lnb_6);
			DATA[6]= cont.getResources().getString(R.string.dish_setup_conf_lnb_7);
			DATA[7]= cont.getResources().getString(R.string.dish_setup_conf_lnb_8);
			
		}

		public int getCount() {
			if(listItems!=null)
				return listItems.size();
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
			ViewHolder holder=null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dvbs_unicable_list_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.fre = (TextView)convertView.findViewById(R.id.edit_fre);
				convertView.setTag(holder);
			}else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			
			holder.text.setText(DATA[position]);
			holder.fre.setTextColor(Color.YELLOW);
			holder.fre.setText(listItems.get(position));
			  
			return convertView;
		}
	}


}

  
