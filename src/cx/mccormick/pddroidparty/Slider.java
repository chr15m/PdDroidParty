package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Slider extends Widget {
	private static final String TAG = "Slider";

	float min, max;
	int log;

	int pid0 = -1;			// pointer id,
	float x0, y0, val0 ; 	// position of pointer, and value when pointer down.

	boolean orientation_horizontal = true;
	boolean down = false;
	int steady = 1;

	WImage bg = new WImage();
	WImage slider = new WImage();

	RectF sRect = new RectF();

	public Slider(PdDroidPatchView app, String[] atomline, boolean horizontal) {
		super(app);

		orientation_horizontal = horizontal;

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;

		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		initCommonArgs(app, atomline, 11);
		steady = Integer.parseInt(atomline[22]);

		setval((float)(Float.parseFloat(atomline[21]) * 0.01 * (max - min) / ((horizontal ? Float.parseFloat(atomline[5]) : Float.parseFloat(atomline[6])) - 1) + min), min);

		// listen out for floats from Pd
		setupreceive();

		// send initial value if we have one
		//initval();

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));

		// load up the images to use and cache all positions
		if (horizontal) {
			bg.load(TAG, "horizontal", label, sendname);
			slider.load(TAG, "widget-horizontal", label, sendname);
		} else {
			bg.load(TAG, "vertical", label, sendname);
			slider.load(TAG, "widget-vertical", label, sendname);
		}

		if ( (!bg.none()) && (!slider.none()) ) {
			// create the slider rectangle thingy
			if (orientation_horizontal) {
				float ratio = slider.getHeight() / h;
				int rel = (int)(slider.getWidth() / ratio);
				sRect = new RectF(x, y, x + rel, y + h);
			} else {
				float ratio = slider.getHeight() / w;
				int rel = (int)(slider.getHeight() / ratio);
				sRect = new RectF(x, y, x + w, y + rel);
			}
		}

		setval(val);
	}

	public void draw(Canvas canvas) {
		if (bg.draw(canvas)) {
			paint.setColor(bgcolor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRect(dRect, paint);

			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawLine(dRect.left /*+ 1*/, dRect.top, dRect.right, dRect.top, paint);
			canvas.drawLine(dRect.left /*+ 1*/, dRect.bottom, dRect.right, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top /*+ 1*/, dRect.left, dRect.bottom, paint);
			canvas.drawLine(dRect.right, dRect.top /*+ 1*/, dRect.right, dRect.bottom, paint);
			paint.setColor(fgcolor);
			paint.setStrokeWidth(3);
			if (orientation_horizontal) {
				canvas.drawLine(Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.top /*+ 2*/), Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.bottom /*- 2*/), paint);
			} else {
				canvas.drawLine(Math.round(dRect.left /*+ 2*/), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), Math.round(dRect.right /*- 2*/), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), paint);
			}

		} else if (!slider.none()) {
			if (orientation_horizontal) {
				sRect.offsetTo((val - min) / (max - min) * (dRect.width() - sRect.width()) + dRect.left, dRect.top);
			} else {
				sRect.offsetTo(dRect.left, (1 - (val - min) / (max - min)) * (dRect.height() - sRect.height()) + dRect.top);
			}
			slider.draw(canvas, sRect);
		}
		drawLabel(canvas);
	}

	public void setval(float v) {
		val = Math.min(max, Math.max(min, v));
		if (bg.svg != null) {
			// Log.e("Slider", "" + ((val - min) / (max - min)));
			if (slider.none()) {
				bg.svg.interpolate("closed", "open", (val - min) / (max - min));
			}
		}
	}

	public float get_horizontal_val(float x) {
		return (((x - dRect.left) / dRect.width()) * (max - min) + min);
	}

	public float get_vertical_val(float y) {
		return (((dRect.height() - (y - dRect.top)) / dRect.height()) * (max - min) + min);
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			val0 = val;
			x0 = x;
			y0 = y;
			pid0 = pid;
			if(steady == 0) {
				if (orientation_horizontal) val = get_horizontal_val(x);
				else val = get_vertical_val(y);
			}
			send("" + val);
			return true;
		}
		return false;
	}

	public boolean touchup(int pid, float x, float y)
	{
		if(pid0 == pid) {
			pid0 = -1;
			//return true;
		}
		return false;
	}

	public boolean touchmove(int pid, float x, float y)
	{
		if(pid0 == pid) {
			if (orientation_horizontal) {
				val = steady * val0 + get_horizontal_val(x) - get_horizontal_val(x0) * steady;
			} else {
				val = steady * val0 + get_vertical_val(y) - get_vertical_val(y0) * steady;
			}
			// clamp the value
			setval(val);
			// send the result to Pd
			send("" + val);
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
	}
}

