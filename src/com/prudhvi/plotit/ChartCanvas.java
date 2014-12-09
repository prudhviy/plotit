package com.prudhvi.plotit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class ChartCanvas extends SurfaceView implements SurfaceHolder.Callback{
	
	private static final String TAG = "PRUDHVI";
	private ChartCanvasThread thread;
	private Paint paintPie = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint paintPieStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Path pathPie = new Path();
	private float wWidth,wHeight;// window dimensions
	private float sWidth,sHeight;// screen dimensions
	private float vWidth,vHeight;// view component dimensions
	private final float[] pieP = {45.0f,30.0f,30.0f,40.0f,50.0f,10.0f,55.0f,25.0f,40.0f,20.0f,15.0f};
	private Region region;
	private Point point;
	private List<Region> bounds;
	private float touched_x;
	private float touched_y;

	public ChartCanvas(Context context) {
		super(context);
		init();
	}
	public ChartCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public ChartCanvas(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		bounds = new ArrayList<Region>();
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
		 
		//setFocusable(true); // make sure we get key events
		//setClickable(true);
		setBackgroundColor(Color.GRAY);
		//paintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		
		paintPie.setStyle(Paint.Style.FILL);
		paintPie.setColor(Color.RED);
		paintPieStroke.setStyle(Paint.Style.STROKE);
		paintPieStroke.setColor(Color.BLACK);
		//paintPie.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		
		final DisplayMetrics metrics = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		sWidth = (float)metrics.widthPixels;
		sHeight = (float)metrics.heightPixels;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) { }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread = new ChartCanvasThread(getHolder(), this);
		thread.setRunning(true);
		vWidth = (float)getWidth();
		vHeight = (float)getHeight();
		Log.i(TAG, "surfaceCreated ChartCanvas " + vWidth + " " + thread.getName());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		Log.i(TAG, "surfaceDestroyed ChartCanvas ");
		thread.setRunning(false);
		while(retry) {
			try {
				thread.join();
				retry = false;
			} 
			catch(InterruptedException e) {
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.restore();
		vWidth = (float)getWidth();
		vHeight = (float)getHeight();
		float total = 0.0f;
		RectF rectF = new RectF();
		RectF rect = new RectF(25.0f, 25.0f, vWidth -50.0f, vWidth -50.0f);
		for(int i = 0; i < pieP.length; i++) {
			pathPie.reset();
			paintPie.setAlpha((255/pieP.length)*(i+1));
			pathPie.moveTo(rect.centerX(), rect.centerY());
			pathPie.addArc(rect, total, pieP[i]);
			pathPie.lineTo(rect.centerX(), rect.centerY());
			pathPie.close();
			canvas.drawPath(pathPie, paintPie);
			canvas.drawPath(pathPie, paintPieStroke);
			total = total + pieP[i];
			
			pathPie.computeBounds(rectF, true);
			region = new Region();
			region.setPath(pathPie, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
			bounds.add(region);
		}
		//Log.i(TAG, "onDraw ChartCanvas " + bounds);
		
		//super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		wWidth = MeasureSpec.getSize(widthMeasureSpec);
	    wHeight = MeasureSpec.getSize(heightMeasureSpec);
	    Log.i(TAG, "onMeasure ChartCanvas " + wWidth + " " + wHeight);
		if (sWidth > 220.0f && sWidth < 250.0f){
			// resolution - 240x320
			this.setMeasuredDimension((int)wWidth, (int)(wWidth));
		} 
		else if(sWidth > 300.0f && sWidth < 350.0f){
			// resolution - 320x480
			this.setMeasuredDimension((int)wWidth, (int)(wWidth));
		}
		else if(sWidth > 450.0f && sWidth < 500.0f){
			// resolution - 480x800, 480x854
			this.setMeasuredDimension((int)wWidth, (int)(wWidth));
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		touched_x = event.getX();
		touched_y = event.getY();
	    
		int action = event.getAction();
		switch(action){
			case MotionEvent.ACTION_DOWN:
				Log.i(TAG, "downi touch ChartCanvas " + touched_x + " and " + touched_y);
				int i = 0;
				for(Region bound : bounds){
					if (bound.contains((int)touched_x, (int)touched_y)) {
							Log.i(TAG, "yes inside ChartCanvas " + i);
							Context context = ((Activity) getContext());
							CharSequence text = "Showing " + i + " pie chart";
							int duration = Toast.LENGTH_SHORT;

							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
					}
					i = i + 1;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				Log.i(TAG, "move touch ChartCanvas ");
				break;
			case MotionEvent.ACTION_UP:
				Log.i(TAG, "up touch ChartCanvas ");
				break;
			case MotionEvent.ACTION_CANCEL:
				Log.i(TAG, "cancel touch ChartCanvas ");
				break;
			case MotionEvent.ACTION_OUTSIDE:
				Log.i(TAG, "outside touch ChartCanvas ");
				break;
			default:
		}
		return true; //processed
	}
}