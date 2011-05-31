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
	
	RectF dRect;
	boolean orientation_horizontal = true;
	boolean down = false;
	
	public Slider(PdDroidPatchView app, String[] atomline, boolean horizontal) {
		super(app);
		
		orientation_horizontal = horizontal;
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		sendname = atomline[11];
		receivename = atomline[12];
		label = atomline[13];
		labelpos[0] = Float.parseFloat(atomline[14]);
		labelpos[1] = Float.parseFloat(atomline[15]);
		
		setval((Float.parseFloat(atomline[21]) / 100) / w, min);
		
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
			canvas.drawLine(Math.round(x + ((val - min) / (max - min)) * w), Math.round(y + 2), Math.round(x + ((val - min) / (max - min)) * w), Math.round(y + h - 2), paint);
		} else {
			canvas.drawLine(Math.round(x + 2), Math.round((y + h) - ((val - min) / (max - min)) * h), Math.round(x + w - 2), Math.round((y + h) - ((val - min) / (max - min)) * h), paint);
		}
		drawLabel(canvas);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && inside(ex, ey)) {
			down = true;
		}
		
		if (down) {
			//Log.e(TAG, "touch:" + val);
			if (event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) {
				// calculate the new value based on touch
				if (orientation_horizontal) {
					val = (((ex - x) / w) * (max - min) + min);
				} else {
					val = (((h - (ey - y)) / h) * (max - min) + min);
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
	
	public void receiveFloat(float v) {
		val = Math.min(max, Math.max(v, min));
	}
}


