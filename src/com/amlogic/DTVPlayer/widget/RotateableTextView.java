package com.amlogic.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class RotateableTextView extends TextView{

	public RotateableTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public RotateableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RotateableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.widget.TextView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.translate(getHeight()*5/6, 0);
		canvas.rotate(90);
		super.onDraw(canvas);
	}

	/* (non-Javadoc)
	 * @see android.widget.TextView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);		
		setMeasuredDimension(200, 200);
	}

}