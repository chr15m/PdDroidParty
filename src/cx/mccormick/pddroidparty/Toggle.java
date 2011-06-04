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

public class Toggle extends Widget {
	private static final String TAG = "Toggle";
	
	RectF dRect;
	float toggleval = 1;
	
	public Toggle(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[5]) / parent.patchheight * screenheight;
		
		toggleval = Float.parseFloat(atomline[18]);
		
		init = Integer.parseInt(atomline[6]);
		sendname = atomline[7];
		receivename = atomline[8];
		label = atomline[9];
		labelpos[0] = Float.parseFloat(atomline[10]) / parent.patchwidth * screenwidth;
		labelpos[1] = Float.parseFloat(atomline[11]) / parent.patchheight * screenheight;

		setval(Float.parseFloat(atomline[17]), 0);
		
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
		if (val != 0) {
			canvas.drawLine(Math.round(x + 2), Math.round(y + 2), Math.round(x + w - 2), Math.round(y + h - 2), paint);
			canvas.drawLine(Math.round(x + 2), Math.round(y + h - 2), Math.round(x + w - 2), Math.round(y + 2), paint);
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
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && inside(ex, ey)) {
			toggle();
			send("" + val);
		}
	}
	
	public void receiveFloat(float v) {
		toggle();
		send("" + val);
	}
	
	public void receiveBang() {
		toggle();
		send("" + val);
	}
}
