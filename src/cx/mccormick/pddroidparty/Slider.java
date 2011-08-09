package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

public class Slider extends Widget {
	private static final String TAG = "Slider";
	
	float min, max;
	int log;
	
	boolean orientation_horizontal = true;
	boolean down = false;
	
	public Slider(PdDroidPatchView app, String[] atomline, boolean horizontal) {
		super(app);
		
		orientation_horizontal = horizontal;
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		sendname = atomline[11];
		receivename = atomline[12];
		label = setLabel(atomline[13]);
		labelpos[0] = Float.parseFloat(atomline[14]) / parent.patchwidth * screenwidth;
		labelpos[1] = Float.parseFloat(atomline[15]) / parent.patchheight * screenheight;
		
		setval((float)(Float.parseFloat(atomline[21]) * 0.01 * (max - min) / ((horizontal ? Float.parseFloat(atomline[5]) : Float.parseFloat(atomline[6])) - 1) + min), min);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top, paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom, paint);
		canvas.drawLine(dRect.right, dRect.top + 1, dRect.right, dRect.bottom, paint);
		if (orientation_horizontal) {
			canvas.drawLine(Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.top + 2), Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.bottom - 2), paint);
		} else {
			canvas.drawLine(Math.round(dRect.left + 2), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), Math.round(dRect.right - 2), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), paint);
		}
		drawLabel(canvas);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && dRect.contains(ex, ey)) {
			down = true;
		}
		
		if (down) {
			//Log.e(TAG, "touch:" + val);
			if (event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) {
				// calculate the new value based on touch
				if (orientation_horizontal) {
					val = (((ex - dRect.left) / dRect.width()) * (max - min) + min);
				} else {
					val = (((dRect.height() - (ey - dRect.top)) / dRect.height()) * (max - min) + min);
				}
				// clamp the value
				val = Math.min(max, Math.max(min, val));
				// send the result to Pd
				send("" + val);
			} else if (event.getAction() == event.ACTION_UP) {
				down = false;
			}
		}
	}
	
	public void receiveMessage(String symbol, Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
	
	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
	
	public void receiveFloat(float v) {
		val = Math.min(max, Math.max(v, min));
	}
}


