package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.lang.Math;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

import org.puredata.core.PdBase;

public class Bang extends Widget {
	private static final String TAG = "Bang";

	boolean bang = false;

	public Bang(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) / parent.patchwidth
				* screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight
				* screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth
				* screenwidth;
		float h = Float.parseFloat(atomline[5]) / parent.patchheight
				* screenheight;

		sendname = app.app.replaceDollarZero(atomline[9]);
		receivename = atomline[10];
		label = setLabel(atomline[11]);
		labelpos[0] = Float.parseFloat(atomline[12]) / parent.patchwidth
				* screenwidth;
		labelpos[1] = Float.parseFloat(atomline[13]) / parent.patchheight
				* screenheight;

		// listen out for floats from Pd
		setupreceive();

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w),
				Math.round(y + h));
	}

	public Bang(PdDroidPatchView app) {
		super(app);
	}

	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top,
				paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right,
				dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom,
				paint);
		canvas.drawLine(dRect.right, dRect.top + 1, dRect.right, dRect.bottom,
				paint);
		if (bang) {
			// TODO: only set this back after the set time
			bang = false;
			paint.setStyle(Paint.Style.FILL);
			parent.threadSafeInvalidate();
		} else {
			paint.setStyle(Paint.Style.STROKE);
		}
		canvas.drawCircle(dRect.centerX(), dRect.centerY(),
				Math.min(dRect.width(), dRect.height()) / 2, paint);
		drawLabel(canvas);
	}

	private void bang() {
		bang = true;
		PdBase.sendBang(sendname);
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

				bang();
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			index = event.findPointerIndex(pid);
			Log.d("BangBefore", index + "");
			index = (index == -1) ? 1 : index;
			Log.d("BangAfter", index + "");
			ex = event.getX(index);
			ey = event.getY(index);
			if (dRect.contains(ex, ey)) {

				bang();

			}
			break;

		}

	}

	public void receiveAny() {
		bang();
	}
}
