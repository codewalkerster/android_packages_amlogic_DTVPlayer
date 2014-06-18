package com.amlogic.widget;

import android.content.Context;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.util.Log;

public class PasswordLayout extends LinearLayout {
	private static final String TAG = "PasswordLayout";
	private Context mContext = null;
	private boolean mEnterPreActionDown = false;
	private PasswordDialog mPasswordDialog = null;

	public PasswordLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setPasswordDialog(PasswordDialog dialog) {
		mPasswordDialog = dialog;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
			if (action == KeyEvent.ACTION_DOWN)
				mEnterPreActionDown = true;
			if (action == KeyEvent.ACTION_UP && mEnterPreActionDown) {
				mEnterPreActionDown = false;
				if (mPasswordDialog != null)
					mPasswordDialog.checkPassword();
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
