package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Radio extends Widget {
	private static final String TAG = "Radio";

	int nCells = 8;

	boolean orientation_horizontal = true;

	WImage bg = new WImage();
	WImage slider = new WImage();
	RectF sRect = new RectF();

	public Radio(PdDroidPatchView app, String[] atomline, boolean horizontal) {
		super(app);

		orientation_horizontal = horizontal;

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		//float h = Float.parseFloat(atomline[6]) ;

		init = Integer.parseInt(atomline[7]);
		nCells = Integer.parseInt(atomline[8]);
		initCommonArgs(app, atomline, 9);
		setval((float)Float.parseFloat(atomline[19]));

		// listen out for floats from Pd
		setupreceive();

		// graphics setup

		// load up the images to use and cache all positions
		if (horizontal) {
			dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w * nCells), Math.round(y + w));
			bg.load(TAG, "horizontal", label, sendname);
			slider.load(TAG, "widget-horizontal", label, sendname);
		} else {
			dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + w * nCells));
			bg.load(TAG, "vertical", label, sendname);
			slider.load(TAG, "widget-vertical", label, sendname);
		}
	}

	public void draw(Canvas canvas) {
		if (orientation_horizontal) {
			float x = dRect.left + dRect.height() * (val + 0.25f);
			float y = dRect.top + dRect.height() * 0.25f;
			float w = dRect.height() * 0.5f;
			sRect.set(x, y, x + w, y + w);
		} else {
			float x = dRect.left + dRect.width() * 0.25f;
			float y = dRect.top + dRect.width() *(val + 0.25f);
			float w = dRect.width() * 0.5f;
			sRect.set(x, y, x + w, y + w);
		}
		if (bg.draw(canvas)) {
			paint.setColor(bgcolor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRect(dRect, paint);

			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawRect(dRect, paint);
			for(int i = 1; i < nCells; i++) {
				if (orientation_horizontal) {
					float x = dRect.left + i * dRect.height();
					canvas.drawLine(x, dRect.top, x, dRect.top + dRect.height(), paint);
				} else {
					float y = dRect.top + i * dRect.width();
					canvas.drawLine(dRect.left, y, dRect.left + dRect.width(), y, paint);
				}
			}
			paint.setColor(fgcolor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRect(sRect, paint);

		} else if (!slider.none()) {
			slider.draw(canvas, sRect);
		}
		drawLabel(canvas);
	}

	public void setval(float v) {
		val = Math.min(nCells - 1, Math.max(0, Math.round(v)));
	}

	public float get_horizontal_val(float x) {
		return Math.round(((x - dRect.left) / dRect.width()) * nCells - 0.5);
	}

	public float get_vertical_val(float y) {
		return Math.round(((y - dRect.top) / dRect.height()) * nCells - 0.5);
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			if (orientation_horizontal) setval(get_horizontal_val(x));
			else setval(get_vertical_val(y));
			sendFloat(val);
			return true;
		}
		return false;
	}

	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol, args)) return;
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}

	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}

	public void receiveFloat(float v) {
		setval(v);
		if (! sendname.equals(receivename)) {
			sendFloat(val);
		}
	}
}

