package com.amlogic.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;

import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;
import com.amlogic.DTVPlayer.R;
import com.amlogic.DTVPlayer.DTVActivity;
	
abstract public class PasswordDialog {
	Dialog mDialog = null;
	private Context mContext = null;

	ImageButton pin0 ;
    ImageButton pin1 ;
    ImageButton pin2 ;
    ImageButton pin3 ;
       
    private static int cur_button_position=0;
    
    private static String pin_char_0;
    private static String pin_char_1;
    private static String pin_char_2;
    private static String pin_char_3;
	
	public PasswordDialog(Context context) {
		mContext = context;
		cur_button_position=0;
		mDialog = new AlertDialog(mContext){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					case KeyEvent.KEYCODE_0:
					case KeyEvent.KEYCODE_1:
					case KeyEvent.KEYCODE_2:
					case KeyEvent.KEYCODE_3:
					case KeyEvent.KEYCODE_4:
					case KeyEvent.KEYCODE_5:
					case KeyEvent.KEYCODE_6:
					case KeyEvent.KEYCODE_7:
					case KeyEvent.KEYCODE_8:
					case KeyEvent.KEYCODE_9:
						switch(cur_button_position){
							case 0:
								pin_char_0=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin0.setBackgroundResource(R.drawable.button_pin_in);
								pin1.requestFocus();
								break;
							case 1:
								pin_char_1=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin1.setBackgroundResource(R.drawable.button_pin_in);
								pin2.requestFocus();
								break;
							case 2:
								pin_char_2=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin2.setBackgroundResource(R.drawable.button_pin_in);
								pin3.requestFocus();
								break;
							case 3:
								pin_char_3=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin3.setBackgroundResource(R.drawable.button_pin_in);
								if(check_pin()){
									    dismissDialog();
										onCheckPasswordIsRight();
								}
								else{
									pin0.setBackgroundResource(R.drawable.button_pin_null);
									pin1.setBackgroundResource(R.drawable.button_pin_null);
									pin2.setBackgroundResource(R.drawable.button_pin_null);
									pin3.setBackgroundResource(R.drawable.button_pin_null);
									pin0.requestFocus();
									onCheckPasswordIsFalse();
								}	
								break;	
						}
						return true;
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
		mDialog.setContentView(R.layout.vchip_pin);
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		pin_button_init(window);

	}

	private void pin_button_init(Window window ){
		pin0 = (ImageButton)window.findViewById(R.id.pin_button0);
		pin1 = (ImageButton)window.findViewById(R.id.pin_button1);
		pin2 = (ImageButton)window.findViewById(R.id.pin_button2);
		pin3 = (ImageButton)window.findViewById(R.id.pin_button3);
     	    	
     	pin0.setFocusable(true);   
     	pin0.requestFocus();   
     	pin0.setFocusableInTouchMode(true);   
            
     	pin0.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
             public void onFocusChange(View v, boolean hasFocus) {   
                 ImageButton imageButton = (ImageButton) v;   
                 if (hasFocus) {   
                 	cur_button_position=0;
                      
                 } else {   
                     
                 }   
             }});   
    	
    	pin1.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
           public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=1;
                     
                } else {   
                    
                }   
            }});   
     	
     	pin2.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
            public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=2;
                     
                } else {   
                    
                }   
            }});  
     	
     	pin3.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
            public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=3;
                     
                } else {   
                    
                }   
            }});   
    	
    }

	
    private boolean check_pin()
    {
    	String cur_pin = null;
    	cur_pin=pin_char_0+pin_char_1+pin_char_2+pin_char_3;
    	String database_pin = "0000";
		database_pin=((DTVActivity)mContext).DTVPlayerGetPassword();
    	if(database_pin==null){
    		if(cur_pin.equals("0000"))
    			return true;
    	}
    	else{
    		if(cur_pin.equals(database_pin)){
    			return true;
    		}	
    	}	
    	return false;
    }
	
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

	abstract public void onCheckPasswordIsRight();
	abstract public void onCheckPasswordIsFalse();
}

 
