package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.MotionEvent;
import android.util.Log;

public class Slider extends Widget {
	private static final String TAG = "Slider";
	private PdDroidPatchView parent;
	private int screenwidth;
	private int screenheight;
	
	float min, max, val;
	int log, init;
	String send, recv, labl;
	
	RectF dRect;
	Paint paint = new Paint();
	
	public Slider(PdDroidPatchView app, String[] atomline) {
		parent = app;
		
		screenwidth = parent.getWidth();
		screenheight = parent.getHeight();
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		send = atomline[11];
		recv = atomline[12];
		labl = atomline[13];
		val = (Float.parseFloat(atomline[21]) / 100) / w;
		
		// graphics setup
		
		dRect = new RectF(x, y, x + w, y + h);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
	}
	
	public void draw(Canvas canvas) {
		Log.e(TAG, "rectangle: " + dRect.toString());
		canvas.drawRoundRect(dRect, 2, 2, paint);
		canvas.drawLine(x + val * w, y + 2, x + val * w, y + h - 2, paint);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (inside(ex, ey)) {
			val = (((ex - x) / w) * (max - min) + min);
			Log.e(TAG, "touch:" + val);
			if (event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) {
				parent.app.send(send, "" + val);
			} else if (event.getAction() == event.ACTION_UP) {
			}
		}
	}
}


