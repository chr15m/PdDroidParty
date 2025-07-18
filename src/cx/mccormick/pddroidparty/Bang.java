package cx.mccormick.pddroidparty;

import org.puredata.core.PdBase;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;

public class Bang extends Widget {
	private static final String TAG = "Bang";

	boolean bang = false;
	long bangtime ;
	int interrpt, hold; //interrpt and hold time, in ms.

	WImage on = new WImage();
	WImage off = new WImage();

	public Bang(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[5]) ;

		hold = (int)Float.parseFloat(atomline[6]) ;
		interrpt = (int)Float.parseFloat(atomline[7]) ;
		init = (int)Float.parseFloat(atomline[8]) ;

		initCommonArgs(app, atomline, 9);

		// listen out for floats from Pd
		setupreceive();

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));

		// try and load images
		on.load(TAG, "on", label, sendname, receivename);
		off.load(TAG, "off", label, sendname, receivename);
	}

	public Bang(PdDroidPatchView app) {
		super(app);
	}

	public void draw(Canvas canvas) {
		if (off.draw(canvas)) {
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(bgcolor);
			canvas.drawRect(dRect, paint);

			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			canvas.drawRect(dRect, paint);
			if (bang && on.draw(canvas)) {
				if((SystemClock.uptimeMillis() - bangtime) > hold) bang = false;
				paint.setStyle(Paint.Style.FILL);
				parent.threadSafeInvalidate();
				canvas.drawCircle(dRect.centerX(), dRect.centerY(), Math.min(dRect.width(), dRect.height()) / 2, paint);
				paint.setColor(fgcolor);

			}
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawCircle(dRect.centerX(), dRect.centerY(), Math.min(dRect.width(), dRect.height()) / 2, paint);
			drawLabel(canvas);
		}
	}

	// visual bang :
	private void bang() {
		bang = true;
		bangtime = SystemClock.uptimeMillis();
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			bang();
			PdBase.sendBang(sendname);
			return true;
		}
		return false;
	}

	/*public void receiveAny() {
		bang();
	}*/

	public void receiveList(Object... args) {
		bang();
	}

	public void receiveSymbol(String symbol) {
		bang();
	}

	public void receiveFloat(float x) {
		bang();
	}

	public void receiveBang() {
		bang();
	}

	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol, args)) return;
		else bang();
	}
}
