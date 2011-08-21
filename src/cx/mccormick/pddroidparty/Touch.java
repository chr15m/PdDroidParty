package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Picture;
import android.view.MotionEvent;
import android.util.Log;

public class Touch extends Widget {
	private static final String TAG = "Touch";
	
	Picture on = null;
	Picture off = null;
	
	boolean down = false;
	
	public Touch(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		sendname = atomline[7];
		
		// try and load SVGs
		on = getPicture(TAG, "on", label);
		off = getPicture(TAG, "off", label);
	}
	
	public void draw(Canvas canvas) {
		if (down) {
			paint.setStrokeWidth(2);
		} else {
			paint.setStrokeWidth(1);
		}
		
		if (down ? drawPicture(canvas, on) : drawPicture(canvas, off)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1, dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom, paint);
		}
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if ((event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) && dRect.contains(ex, ey)) {
			send(((ex - dRect.left) / dRect.width()) + " " + ((ey - dRect.top) / dRect.height()));
			down = true;
		}
		
		if (event.getAction() == event.ACTION_UP && down) {
			send(0 + " " + 0);
			down = false;
		}
	}
}
