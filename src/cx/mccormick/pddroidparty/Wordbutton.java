package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Picture;
import android.view.MotionEvent;
import android.util.Log;

import org.puredata.core.PdBase;

public class Wordbutton extends Bang {
	private static final String TAG = "Wordbutton";

	Rect tRect = new Rect();

	SVGRenderer on = null;
	SVGRenderer off = null;

	boolean down = false;

	String spacereplace = null;

	public Wordbutton(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) / parent.patchwidth
				* screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight
				* screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth
				* screenwidth;
		float h = Float.parseFloat(atomline[6]) / parent.patchheight
				* screenheight;

		sendname = "wordbutton-" + app.app.replaceDollarZero(atomline[7]);
		label = setLabel(atomline[7]);
		spacereplace = label.replace("_", " ");
		paint.setTextSize(Math.round(h * 0.75));
		paint.setTextAlign(Paint.Align.CENTER);
		paint.getTextBounds(spacereplace, 0, label.length(), tRect);
		labelpos[0] = w / 2 - tRect.width() / 2;
		labelpos[1] = h / 2 - tRect.height() / 2;

		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w),
				Math.round(y + h));

		// try and load SVGs
		on = getSVG(TAG, "on", label);
		off = getSVG(TAG, "off", label);

		setTextParametersFromSVG(on);
		setTextParametersFromSVG(off);
	}

	public void draw(Canvas canvas) {
		if (down) {
			paint.setStrokeWidth(2);
		} else {
			paint.setStrokeWidth(1);
		}

		if (down ? drawPicture(canvas, on) : drawPicture(canvas, off)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top,
					paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right,
					dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left,
					dRect.bottom, paint);
			canvas.drawLine(dRect.right, dRect.top + 1, dRect.right,
					dRect.bottom, paint);
		}
		canvas.drawText(spacereplace, dRect.left + dRect.width() / 2,
				(int) (dRect.top + dRect.height() * 0.75), paint);
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

				down = true;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			index = event.findPointerIndex(pid);
			Log.d("dwnBtnBefore", index+"");
			index=(index==-1)?1:index;
			Log.d("dwnBtnAfter", index+"");
			ex = event.getX(index);
			ey = event.getY(index);
			if (dRect.contains(ex, ey)) {

				down = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			ex = event.getX();
			ey = event.getY();
			if (dRect.contains(ex, ey)) {

				PdBase.sendBang(sendname);
			}
			down = false;
			break;

		case MotionEvent.ACTION_POINTER_UP:
			pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			index = event.findPointerIndex(pid);
			Log.d("upBtnBefore", index+"");
			index=(index==-1)?1:index;
			Log.d("upBtnAfter", index+"");
			ex = event.getX(index);
			ey = event.getY(index);
			if (dRect.contains(ex, ey)) {

				PdBase.sendBang(sendname);
			}
			down = false;
			break;

		}

	}
}
