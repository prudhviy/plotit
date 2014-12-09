package com.prudhvi.plotit;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class ChartCanvasThread extends Thread {
	private static final String TAG = "PRUDHVI";
	private SurfaceHolder myThreadSurfaceHolder;
	private ChartCanvas myThreadSurfaceView;
	private boolean myThreadRun = false;
	
	public ChartCanvasThread(SurfaceHolder surfaceHolder, ChartCanvas surfaceView) {
		myThreadSurfaceHolder = surfaceHolder;
		myThreadSurfaceView = surfaceView;
	}
	
	public void setRunning(boolean b) {
		myThreadRun = b;
	}

	@Override
	public void run() {
		while(myThreadRun) {
			Canvas c = null;
			try {
				c = myThreadSurfaceHolder.lockCanvas(null);
				synchronized (myThreadSurfaceHolder) {
					myThreadSurfaceView.onDraw(c);
				}
				Log.i(TAG, "SLEEP ChartCanvasThread");
				sleep(5000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					myThreadSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}