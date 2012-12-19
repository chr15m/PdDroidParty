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
	
	SVGRenderer on = null;
	SVGRenderer off = null;
	
	boolean down = false;
	
	public Touch(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		sendname = app.app.replaceDollarZero(atomline[7]);
		
		// try and load SVGs
		on = getSVG(TAG, "on", label);
		off = getSVG(TAG, "off", label);
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

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pid, index;
		float ex;
		float ey;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			ex = event.getX();
			ey = event.getY();
			if (dRect.contains(ex, ey)) {

				send(((ex - dRect.left) / dRect.width()) + " "
						+ ((ey - dRect.top) / dRect.height()));
				down = true;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			index = event.findPointerIndex(pid);
			Log.d("dwnTouchBefore", index+"");
			index=(index==-1)?1:index;
			Log.d("dwnTouchAfter", index+"");
			ex = event.getX(index);
			ey = event.getY(index);
			if (dRect.contains(ex, ey)) {

				send(((ex - dRect.left) / dRect.width()) + " "
						+ ((ey - dRect.top) / dRect.height()));
				down = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			for (int i = 0; i < event.getPointerCount(); i++) {
				ex = event.getX(i);
				ey = event.getY(i);
				if (dRect.contains(ex, ey)) {

					send(((ex - dRect.left) / dRect.width()) + " "
							+ ((ey - dRect.top) / dRect.height()));
					down = true;

				}
			}
			break;

		case MotionEvent.ACTION_UP:
			if (down) {
				ex = event.getX();
				ey = event.getY();
				if (dRect.contains(ex, ey)) {

					down = false;
				}
			}
			break;

		case MotionEvent.ACTION_POINTER_UP:
			if (down) {
				pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				index = event.findPointerIndex(pid);
				Log.d("upTouchBefore", index+"");
				index=(index==-1)?1:index;
				Log.d("dwnTouchAfter", index+"");
				ex = event.getX(index);
				ey = event.getY(index);
				if (dRect.contains(ex, ey)) {

					down = false;
				}
			}
			break;
		}
	}
}
