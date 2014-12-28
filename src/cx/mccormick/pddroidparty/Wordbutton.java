package cx.mccormick.pddroidparty;

import org.puredata.core.PdBase;

import cx.mccormick.pddroidparty.Widget.WImage;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

public class Wordbutton extends Bang {
	private static final String TAG = "Wordbutton";

	Rect tRect = new Rect();

	WImage on = new WImage();
	WImage off = new WImage();

	boolean down = false;
	int pid0 = -1; //pointer id when down
	
	String spacereplace = null;

	public Wordbutton(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;

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

		// try and load images
		on.load(TAG, "on", label, sendname, receivename);
		off.load(TAG, "off", label, sendname, receivename);

		setTextParametersFromSVG(on.svg);
		setTextParametersFromSVG(off.svg);
	}

	public void draw(Canvas canvas) {
		if (down) {
			paint.setStrokeWidth(2);
		} else {
			paint.setStrokeWidth(1);
		}

		if (down ? on.draw(canvas) : off.draw(canvas)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top,
					paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right,
					dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left,
					dRect.bottom, paint);
			canvas.drawLine(dRect.right, dRect.top + 1, dRect.right,
					dRect.bottom, paint);
		}
		drawCenteredText(canvas, spacereplace);
	}

	public boolean touchdown(int pid,float x,float y)
	{
		if (dRect.contains(x, y)) {
			down = true;
			pid0 = pid;
			return true;
		}
		return false;
	}

	public boolean touchup(int pid,float x,float y)
	{
		if (pid == pid0) {
			if (dRect.contains(x, y)) {
				PdBase.sendBang(sendname);
			}
			down = false;
			pid0 = -1;
			//return true;
		}
		return false;
	}
	
	public void touch_(MotionEvent event) {

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
			Log.d("dwnBtnBefore", index + "");
			index = (index == -1) ? 1 : index;
			Log.d("dwnBtnAfter", index + "");
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
			Log.d("upBtnBefore", index + "");
			index = (index == -1) ? 1 : index;
			Log.d("upBtnAfter", index + "");
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
