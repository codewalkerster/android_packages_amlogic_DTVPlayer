package com.amlogic.widget;

import java.util.*;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;


import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;
import com.amlogic.DTVPlayer.R;
import com.amlogic.DTVPlayer.DTVActivity;

abstract public class MutipleChoiseDialog {
	private static final String TAG="SingleChoiseDialog";
	Dialog mDialog = null;
	private Context mContext = null;

	TextView title;
	ListView list_item;
	Button no ;
    Button yes ;
	String[] item_string=null;
	boolean[] isChecked=null;
    int cur_choise_index=0;
	
	public MutipleChoiseDialog(Context context,String[] item,boolean[] b,int pos) {
		mContext = context;
		item_string = item;
		isChecked = b;
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						dismissDialog();
						break;
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);

		if(mDialog == null){
			return;
		}

		mDialog.show();
		mDialog.setContentView(R.layout.single_choise_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialogInit(window,item,b,pos);

	}

	private void dialogInit(Window window,String[] item,boolean[] b,int pos){
		no = (Button)window.findViewById(R.id.no);
		no.setText(R.string.no);
		yes = (Button)window.findViewById(R.id.yes);
		yes.setText(R.string.yes);
		title = (TextView)window.findViewById(R.id.title);
		title.setTextColor(Color.YELLOW);
		onSetMessage(title);

		/*ListView Item*/
		list_item = (ListView)window.findViewById(R.id.list_item);
		list_item.setItemsCanFocus(false);
		list_item.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);	
		
		SimpleAdapter adapter = new SimpleAdapter(mContext, platform(),
				R.layout.list_menu_check_item, new String[] { "myMenuItemId" },
				new int[] { R.id.myMenuItemId });
		/*
	    MyAdapter adapter = new MyAdapter(mContext, platform(),b,
				R.layout.list_menu_check_item, new String[] { "myMenuItemId" },
				new int[] { R.id.myMenuItemId });
		*/
		list_item.setAdapter(adapter);
		list_item.setSelection(pos);
		if(isChecked!=null){
			for(int i =0;i<isChecked.length;i++){
				list_item.setItemChecked(i, isChecked[i]);
			}
		}	
		list_item.setOnItemClickListener(mOnItemClickListener);
		
		
     	no.setFocusable(true);   
     	//no.requestFocus();   
     	no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 onSetNegativeButton();
					 dismissDialog();
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {
					 onSetPositiveButton(cur_choise_index,isChecked);
					 dismissDialog();
		          }});	    
	}

	private List<Map<String, Object>> platform() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		for (int i=0;i<item_string.length;i++) {
			map = new HashMap<String, Object>();
			map.put("myMenuItemId", item_string[i]);
			list.add(map);
		}
		
		return list;
	}

	public class MyAdapter extends SimpleAdapter {

		Map<Integer, Boolean> map; 
		LayoutInflater mInflater;
		private List<? extends Map<String, ?>> mList;

		public MyAdapter(Context context, List<Map<String, Object>> data,boolean[] b,int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			map = new HashMap<Integer, Boolean>();
			mInflater = LayoutInflater.from(context);
			mList = data;
			for(int i = 0; i < data.size(); i++) {
				map.put(i, b[i]);
			} 
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.list_menu_check_item, null);
			}
			TextViewWithCheck item = (TextViewWithCheck) convertView.findViewById(R.id.myMenuItemId);
			item.setChecked(map.get(position)); 
			return convertView;
		}
	}

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			cur_choise_index = position;  
			if(isChecked!=null)
			isChecked[position]=((TextViewWithCheck)v).isChecked();
			Log.d(TAG,"postion "+cur_choise_index+"is "+isChecked[position]);
		}
	};
		
	
	public void dismissDialog(){
		if(mDialog!=null&& mDialog.isShowing()){
			mDialog.dismiss();
		}
	}

	public boolean isShowing(){
		if(mDialog!=null&&mDialog.isShowing()){
			return mDialog.isShowing();
		}
		return false;
	}

	abstract public void onSetMessage(View v);
	abstract public void onSetNegativeButton();
	abstract public void onSetPositiveButton(int which,boolean[] b);
}

 
