package com.amlogic.widget;

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
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.graphics.Color;
import com.amlogic.DTVPlayer.R;
import com.amlogic.DTVPlayer.DTVSettings;
	
abstract public class PasswordSettingDialog {
	private static final String TAG="PasswordSettingDialog";
	Dialog mDialog = null;
	private Context mContext = null;
	private Toast toast=null;

	/* ImageButton pin0 ;
    ImageButton pin1 ;
    ImageButton pin2 ;
    ImageButton pin3 ; */
	TextView mPinTextView = null;

	/* ImageButton pin00 ;
    ImageButton pin11 ;
    ImageButton pin22 ;
    ImageButton pin33 ; */
	TextView mPinConfirmTextView = null;
       
    // private static int cur_button_position=0;
    
    /* private static String pin_char_0=null;
    private static String pin_char_1=null;
    private static String pin_char_2=null;
    private static String pin_char_3=null; */

	/* private static String pin_char_00=null;
    private static String pin_char_11=null;
    private static String pin_char_22=null;
    private static String pin_char_33=null; */
	
	public PasswordSettingDialog(Context context) {
		mContext = context;
		// cur_button_position=0;	
		mDialog = new Dialog(mContext, R.style.MyDialog){
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event){
				 switch (keyCode) {
					/* case KeyEvent.KEYCODE_0:
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
								pin00.requestFocus();
								break;	
							case 4:
								pin_char_00=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin00.setBackgroundResource(R.drawable.button_pin_in);
								pin11.requestFocus();
								break;
							case 5:
								pin_char_11=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin11.setBackgroundResource(R.drawable.button_pin_in);
								pin22.requestFocus();
								break;
							case 6:
								pin_char_22=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin22.setBackgroundResource(R.drawable.button_pin_in);
								pin33.requestFocus();
								break;
							case 7:
								pin_char_33=Integer.toString(keyCode - KeyEvent.KEYCODE_0);
								pin3.setBackgroundResource(R.drawable.button_pin_in);
								pin00.requestFocus();
								if(check_pin()){
									    dismissDialog();
										onCheckPasswordIsRight();
								}
								else{
									pin0.setBackgroundResource(R.drawable.button_pin_null);
									pin1.setBackgroundResource(R.drawable.button_pin_null);
									pin2.setBackgroundResource(R.drawable.button_pin_null);
									pin3.setBackgroundResource(R.drawable.button_pin_null);
									pin00.setBackgroundResource(R.drawable.button_pin_null);
									pin11.setBackgroundResource(R.drawable.button_pin_null);
									pin22.setBackgroundResource(R.drawable.button_pin_null);
									pin33.setBackgroundResource(R.drawable.button_pin_null);
	
									pin0.requestFocus();
									onCheckPasswordIsFalse();
									pin_char_0=null;
								    pin_char_1=null;
								    pin_char_2=null;
								    pin_char_3=null;
									pin_char_00=null;
								    pin_char_11=null;
								    pin_char_22=null;
								    pin_char_33=null;
								}	
								break;
						}
						return true; */
					case KeyEvent.KEYCODE_BACK:	
						dismissDialog();
						break;
					case KeyEvent.KEYCODE_DPAD_UP:	
					case KeyEvent.KEYCODE_DPAD_DOWN:
						if(onDealUpDownKey()){
							((Activity)mContext).onKeyDown(keyCode,event);
							dismissDialog();
							return true;
						}
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
		mDialog.setContentView(R.layout.password_setting);
		Window window = mDialog.getWindow();
		((PasswordSettingLayout)window.findViewById(R.id.password_setting_layout)).setPasswordSettingDialog(this);
		/* WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();
		lp.dimAmount=0.0f;
		mDialog.getWindow().setAttributes(lp);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); */
		pin_button_init(window);

	}

	private void pin_button_init(Window window ){
		/* pin0 = (ImageButton)window.findViewById(R.id.pin_button0);
		pin1 = (ImageButton)window.findViewById(R.id.pin_button1);
		pin2 = (ImageButton)window.findViewById(R.id.pin_button2);
		pin3 = (ImageButton)window.findViewById(R.id.pin_button3);

		pin00 = (ImageButton)window.findViewById(R.id.button0);
		pin11 = (ImageButton)window.findViewById(R.id.button1);
		pin22 = (ImageButton)window.findViewById(R.id.button2);
		pin33 = (ImageButton)window.findViewById(R.id.button3);
     	    	
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

		pin00.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
             public void onFocusChange(View v, boolean hasFocus) {   
                 ImageButton imageButton = (ImageButton) v;   
                 if (hasFocus) {   
                 	cur_button_position=4;
                      
                 } else {   
                     
                 }   
             }});   
    	
    	pin11.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
           public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=5;
                     
                } else {   
                    
                }   
            }});   
     	
     	pin22.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
            public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=6;
                     
                } else {   
                    
                }   
            }});  
     	
     	pin33.setOnFocusChangeListener(new ImageButton.OnFocusChangeListener(){   
            public void onFocusChange(View v, boolean hasFocus) {   
                ImageButton imageButton = (ImageButton) v;   
                if (hasFocus) {   
                    //imageButton.setImageResource(R.drawable.button_pin_in); 
                	cur_button_position=7;
                     
                } else {   
                    
                }   
            }}); */
		mPinTextView = (TextView)window.findViewById(R.id.pin_textview);
		mPinConfirmTextView = (TextView)window.findViewById(R.id.pin_confirm_textview);
		
		window.findViewById(R.id.ok_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkPassword();
			}
		});
		window.findViewById(R.id.return_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissDialog();
			}
		});
    }

	public void checkPassword() {
		if (check_pin()) {
			dismissDialog();
			onCheckPasswordIsRight();
		} else {
			onCheckPasswordIsFalse();
			if(toast!=null)
				toast.cancel(); 
			toast = Toast.makeText(	mContext, 
	    		R.string.invalid_password,
	    		Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	
    private boolean check_pin(){
    	/* String cur_pin = null;
		String confirm_pin = null;
    	cur_pin = pin_char_0+pin_char_1+pin_char_2+pin_char_3;
		confirm_pin = pin_char_00+pin_char_11+pin_char_22+pin_char_33;
		if(cur_pin.equals(confirm_pin)){
			if(pin_char_0==null||pin_char_1==null||pin_char_2==null||pin_char_3==null||
				pin_char_00==null||pin_char_11==null||pin_char_22==null||pin_char_33==null)
				return false;
			else{
				DTVSettings mDTVSettings = new DTVSettings(mContext);
				mDTVSettings.setPassWord(confirm_pin);
				return true;
			}	
		}	    
		else
    		return false; */
		String pin = mPinTextView.getText().toString();
		String pinConfirm = mPinConfirmTextView.getText().toString();
		if (pin.equals(pinConfirm)) {
			if (TextUtils.isEmpty(pin) || TextUtils.isEmpty(pinConfirm))
				return false;
			DTVSettings mDTVSettings = new DTVSettings(mContext);
			mDTVSettings.setPassWord(pinConfirm);
			return true;
		} else
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
	abstract public boolean onDealUpDownKey();
}

 
