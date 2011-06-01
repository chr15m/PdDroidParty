package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.util.Log;

public class Widget {
	private static final String TAG = "Widget";
	int screenwidth=0;
	int screenheight=0;
	
	float x, y, w, h;
	
	Paint paint = new Paint();
	
	float val = 0;
	int init = 0;
	String sendname = null;
	String receivename = null;
	String label = null;
	float[] labelpos = new float[2];
	PdDroidPatchView parent=null;
	Typeface font = Typeface.create("Courier", Typeface.BOLD);
	
	public Widget(PdDroidPatchView app) {
		parent = app;
		screenwidth = parent.getWidth();
		screenheight = parent.getHeight();
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setTypeface(font);
		paint.setTextSize((int)((float)parent.fontsize / parent.patchheight * screenheight) - 2);
	}
	
	public void send(String msg) {
		if (sendname != null && !sendname.equals("") && !sendname.equals("empty")) {
			parent.app.send(sendname, msg);
		}
	}

	public void setupreceive() {
		// listen out for floats from Pd
		if (receivename != null && !receivename.equals("") && !receivename.equals("empty")) {
			parent.app.registerReceiver(receivename, this);
		}
	}
	
	public void setval(float v, float alt) {
		if (init != 0) {
			val = v;
		} else {
			val = alt;
		}
	}
	
	public void initval() {
		if (init != 0) {
			Log.e(TAG, "SENT: " + val);
			send("" + val);
		}
	}
	
	/* Draw the label */	
	public void drawLabel(Canvas canvas) {
		if (label != null) {
			canvas.drawText(label, x + labelpos[0] + 2, y + labelpos[1] + 4, paint);
		}
	}
	
	/**
	 * Generic draw method for all widgets.
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
	}
	
	/**
	 * Generic touch method for a hit test.
	 * @param event
	 */	
	public void touch(MotionEvent event) {
	}
	
	public void receiveList(Object... args) {
		Log.e(TAG, "dropped list");
	}
	
	public void receiveMessage(String symbol, Object... args) {
		Log.e(TAG, "dropped message");
	}
	
	public void receiveSymbol(String symbol) {
		Log.e(TAG, "dropped symbol");
	}
	
	public void receiveFloat(float x) {
		Log.e(TAG, "dropped float");
	}
	
	public void receiveBang() {
		Log.e(TAG, "dropped bang");
	}
	
	public boolean inside(float ex, float ey) {
		return !(ex < x || ex > x + w || ey < y || ey > y + h);
	}
}



