package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Path;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

public class Numberbox extends Widget {
	private static final String TAG = "Floatatom";
	private static final int LABEL_LEFT   = 0;
	private static final int LABEL_RIGHT  = 1;
	private static final int LABEL_TOP    = 2;
	private static final int LABEL_BOTTOM = 3;

	float min, max;
	int numwidth;
	int label_pos = LABEL_LEFT;
	float font_height;

	boolean down = false;
	int pid0 = -1; //pointer id when down
	float y0, val0 ; // position of pointer, and value when pointer down.
	boolean moved = false;

	public Numberbox(PdDroidPatchView app, String[] atomline) {
		super(app);

		Rect tRect = new Rect();
		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;

		fontsize = Integer.parseInt(atomline[11]);
		if(fontsize == 0) fontsize = 12;
		labelsize = fontsize;
		paint.setTextSize(fontsize);

		// calculate screen bounds for the numbers that can fit
		numwidth = Integer.parseInt(atomline[4]);
		StringBuffer calclen = new StringBuffer();
		for (int s = 0; s < numwidth; s++) {
			calclen.append("0");
		}
		paint.getTextBounds(calclen.toString(), 0, calclen.length(), tRect);
		font_height = tRect.height();
		dRect.set(x, y, x + tRect.width() + 4, y + tRect.height() * 1.75f);
		dRect.sort();

		min = Float.parseFloat(atomline[5]);
		max = Float.parseFloat(atomline[6]);
		sendname = app.app.replaceDollarZero(atomline[10]);
		receivename = app.app.replaceDollarZero(atomline[9]);
		label = setLabel(atomline[8]);

		label_pos = Integer.parseInt(atomline[7]);
		updateLabelPos();

		setval(0, 0);

		// listen out for floats from Pd
		setupreceive();
	}

	public Numberbox(PdDroidPatchView app) {
		super(app);
	}

	public void setval(float v) {
		val = v;
		if (min != 0 || max != 0) {
			val = Math.min(max, Math.max(v, min));
		}
	}

	public void updateLabelPos() {
		switch (label_pos) {
		case LABEL_LEFT:
			Rect tRect = new Rect();
			paint.getTextBounds(label, 0, label.length(), tRect);
			labelpos[0] = -tRect.width() - 2;
			labelpos[1] = dRect.height() / 2;
			break;
		case LABEL_RIGHT:
			labelpos[0] = dRect.width() + 2;
			labelpos[1] = dRect.height() / 2;
			break;
		case LABEL_TOP:
			labelpos[0] = 0;
			labelpos[1] = -font_height / 2 - 2;
			break;
		case LABEL_BOTTOM:
			labelpos[0] = 0;
			labelpos[1] = dRect.height() + font_height / 2 + 2;
		}
	}

	public void draw(Canvas canvas) {
		Path path = new Path();
		path.moveTo(dRect.left, dRect.top);
		path.lineTo(dRect.right - 5, dRect.top);
		path.lineTo(dRect.right, dRect.top + 5);
		path.lineTo(dRect.right, dRect.bottom);
		path.lineTo(dRect.left, dRect.bottom);
		path.close();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawPath(path, paint);
		paint.setStrokeWidth(0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawText(formatNumber(val, numwidth), dRect.left + 2, dRect.centerY() + font_height / 2, paint);
		drawLabel(canvas);
	}

	public boolean touchdown(int pid, float x, float y) {
		if (dRect.contains(x, y)) {
			down = true;
			pid0 = pid;
			val0 = val;
			y0 = y;
			moved = false;
			return true;
		}
		return false;
	}

	public boolean touchup(int pid, float x, float y) {
		if (pid == pid0) {
			if (! moved) {
				parent.app.launchDialog(this, PdDroidParty.DIALOG_NUMBERBOX);
			}
			down = false;
			pid0 = -1;
			return true;
		}
		return false;
	}

	public boolean touchmove(int pid, float x, float y)
	{
		if(pid0 == pid) {
			float dv = Math.round((y - y0) / 2);
			if(dv != 0) {
				val = val0 - Math.round((y - y0) / 2);
				// clamp the value
				setval(val);
				// send the result to Pd
				sendFloat(val);
				moved =true;
			}
			return true;
		}
		return false;
	}

	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}

	public void receiveFloat(float v) {
		if (min != 0 || max != 0) {
			val = Math.min(max, Math.max(v, min));
		} else {
			val = v;
		}
		if (! sendname.equals(receivename)) {
			sendFloat(val);
		}

	}

	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol, args)) {
			updateLabelPos();
			return;
		}
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
}

