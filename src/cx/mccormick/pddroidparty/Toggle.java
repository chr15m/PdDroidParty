package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.util.Log;

public class Toggle extends Widget {
	private static final String TAG = "Toggle";
	
	float toggleval = 1;
	SVGRenderer on = null;
	SVGRenderer off = null;
	
	public Toggle(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[5]) / parent.patchheight * screenheight;
		
		toggleval = Float.parseFloat(atomline[18]);
		
		init = Integer.parseInt(atomline[6]);
		sendname = atomline[7];
		receivename = atomline[8];
		label = setLabel(atomline[9]);
		labelpos[0] = Float.parseFloat(atomline[10]) / parent.patchwidth * screenwidth;
		labelpos[1] = Float.parseFloat(atomline[11]) / parent.patchheight * screenheight;
		
		setval(Float.parseFloat(atomline[17]), 0);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		// try and load SVGs
		on = getSVG(TAG, "on", label, sendname);
		off = getSVG(TAG, "off", label, sendname);
	}
	
	public void draw(Canvas canvas) {
		if (drawPicture(canvas, off)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom, paint);
			canvas.drawLine(dRect.right, dRect.top + 1, dRect.right, dRect.bottom, paint);
		}
		
		if (val != 0) {
			if (drawPicture(canvas, on)) {
				canvas.drawLine(dRect.left + 2, dRect.top + 2, dRect.right - 2, dRect.bottom - 2, paint);
				canvas.drawLine(dRect.left + 2, dRect.bottom - 2, dRect.right - 2, dRect.top + 2, paint);
			}
		}
		drawLabel(canvas);
	}
	
	public void toggle() {
		if (val == 0) {
			val = toggleval;
		} else {
			val = 0;
		}
	}
	
	public void initval() {
		if (init != 0) {
			sendFloat(val);
		}
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && dRect.contains(ex, ey)) {
			toggle();
			sendFloat(val);
		}
	}
	
	public void receiveMessage(String dest, Object... args) {
		// set message sets value without sending
		if (dest.equals("set") && args.length > 0) {
			val = (Float)args[0];
		}
	}
	
	public void receiveList(Object... args) {
		if (args.length > 0) {
			if (args[0].getClass().equals(Float.class)) {
				// if we receive a float, pass it through, setting our value
				receiveFloat((Float)args[0]);
			} else if (args[0].getClass().equals(String.class)) {
				// if we receive a set message
				if (args[0].equals("set")) {
					// set our value to the float supplied but don't pass it through
					if (args.length > 1 && args[1].getClass().equals(Float.class)) {
						val = (Float)args[1];
					}
				} else if (args[0].equals("bang")) {
					// if we receive a bang, do that
					receiveBang();
				}
			}
		}
	}
	
	public void receiveFloat(float v) {
		val = v;
		sendFloat(val);
	}
	
	public void receiveBang() {
		toggle();
		sendFloat(val);
	}
}
