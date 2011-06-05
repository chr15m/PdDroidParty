package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.view.MotionEvent;
import android.util.Log;

public class Numberbox extends Widget {
	private static final String TAG = "Numberbox";
	
	float min, max;
	int numwidth;
	
	Rect dRect = new Rect();
	Paint paint = new Paint();
	boolean down = false;
	StaticLayout numLayout = null;
	DecimalFormat fmt = null;
	
	public Numberbox(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		
		// calculate screen bounds for the numbers that can fit
		numwidth = Integer.parseInt(atomline[4]);
		StringBuffer calclen = new StringBuffer();
		for (int s=0; s<numwidth; s++) {
			if (s == 1) {
				calclen.append(".");
			} else {
				calclen.append("#");
			}
		}
		fmt = new DecimalFormat(calclen.toString());
		paint.getTextBounds(calclen.toString(), 0, calclen.length(), dRect);
		dRect.sort();
		dRect.offset((int)x, (int)y + fontsize);
		dRect.top -= 3;
		dRect.bottom += 3;
		dRect.left -= 3;
		dRect.right += 3;
		
		x = dRect.left;
		y = dRect.top;
		w = dRect.width();
		h = dRect.height();
		
		//Log.e("RECT", dRect.toString());
		// Rect(1, -9 - 30, 0)
		//w = Float.parseFloat(atomline[4]) * ;
		//h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		min = Float.parseFloat(atomline[5]);
		max = Float.parseFloat(atomline[6]);
		sendname = atomline[10];
		receivename = atomline[9];
		label = setLabel(atomline[8]);
		labelpos[0] = x;
		labelpos[1] = y;
		
		//setval((Float.parseFloat(atomline[21]) / 100) / (dRect.right - dRect.left), min);
		setval(0, 0);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		//initval();
		
		// graphics setup
		//dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 5, dRect.top, paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom, paint);
		canvas.drawLine(dRect.right, dRect.top + 5, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.right - 5, dRect.top, dRect.right, dRect.top + 5, paint);
		canvas.drawText(fmt.format(val), x + 3, y + fontsize + 3, paint);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_UP && inside(ex, ey)) {
			down = false;
			//parent.
			parent.app.launchNumberboxDialog();
		}
		
		/*if (down) {
			//Log.e(TAG, "touch:" + val);
			if (event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) {
				// calculate the new valimport com.csipsimple.R;ue based on touch
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
		}*/
	}
	
	public void receiveFloat(float v) {
		if (min != 0 || max != 0) {
			val = Math.min(max, Math.max(v, min));
		} else {
			val = v;
		}
	}
}

