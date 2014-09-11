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

abstract public class SingleChoiseDialog {
	private static final String TAG="SingleChoiseDialog";
	public Dialog mDialog = null;
	private Context mContext = null;

	TextView title;
	ListView list_item;
	Button no ;
	Button yes ;
	String[] item_string=null;
	int cur_choise_index=0;
	
	public SingleChoiseDialog(Context context,String[] item,int pos) {
		mContext = context;
		item_string = item;
		cur_choise_index = pos;
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						dismissDialog();
						return true;
					case KeyEvent.KEYCODE_DPAD_LEFT:
						if(no!=null)
							no.requestFocus();
						break;
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						if(yes!=null)
							yes.requestFocus();
						break;
						
				}
				return super.onKeyDown(keyCode, event);
			}
			
		};
		
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);

		if(mDialog == null){
			return;
		}

		mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			public void onShow(DialogInterface dialog) {
				onShowEvent();
			}         
		}); 	

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
						public void onDismiss(DialogInterface dialog) {
							onDismissEvent();
						}         
						});	
		
		mDialog.show();
		mDialog.setContentView(R.layout.single_choise_dialog);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialogInit(window,item,pos);
		
	}

	public void onShowEvent(){}
	public void onDismissEvent(){}
	
	private void dialogInit(Window window,String[] item,int pos){
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
		list_item.setChoiceMode(ListView.CHOICE_MODE_SINGLE);		
		SimpleAdapter adapter = new SimpleAdapter(mContext, platform(),
				R.layout.list_menu_check_item, new String[] { "myMenuItemId" },
				new int[] { R.id.myMenuItemId });
		
		list_item.setAdapter(adapter);

		list_item.setSelection(pos);
		list_item.setItemChecked(pos, true);
		list_item.setOnItemClickListener(mOnItemClickListener);
		
		
		// no.setFocusable(true);   
		//no.requestFocus();   
		// no.setFocusableInTouchMode(true);   
		no.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {				  	 
		        	 onSetNegativeButton();
					 dismissDialog();
		          }});	 
		yes.setOnClickListener(new OnClickListener(){
		          public void onClick(View v) {
					 onSetPositiveButton(cur_choise_index);
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

	private AdapterView.OnItemClickListener mOnItemClickListener =new AdapterView.OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			cur_choise_index = position;  
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

	public void updateDialog(String[] item,int pos){
		Window window = mDialog.getWindow();
		this.item_string=item;
		list_item = (ListView)window.findViewById(R.id.list_item);
		list_item.setItemsCanFocus(false);
		list_item.setChoiceMode(ListView.CHOICE_MODE_SINGLE);		
		SimpleAdapter adapter = new SimpleAdapter(mContext, platform(),
				R.layout.list_menu_check_item, new String[] { "myMenuItemId" },
				new int[] { R.id.myMenuItemId });
		
		list_item.setAdapter(adapter);	
	}

	
	abstract public void onSetMessage(View v);
	abstract public void onSetNegativeButton();
	abstract public void onSetPositiveButton(int which);
}

 
