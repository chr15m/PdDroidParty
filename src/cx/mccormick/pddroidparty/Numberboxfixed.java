package cx.mccormick.pddroidparty;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Numberboxfixed extends Numberbox {
	private static final String TAG = "Numberbox";
	
	SVGRenderer on = null;
	SVGRenderer off = null;
	
	public Numberboxfixed(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;
		
		fontsize = (int)(h * 0.75);
		
		// calculate screen bounds for the numbers that can fit
		numwidth = 3;
		StringBuffer calclen = new StringBuffer();
		for (int s=0; s<numwidth; s++) {
			if (s == 1) {
				calclen.append(".");
			} else {
				calclen.append("#");
			}
		}
		fmt = new DecimalFormat(calclen.toString());
		
		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);
		
		min = Float.parseFloat(atomline[9]);
		max = Float.parseFloat(atomline[10]);
		init = 1;
		sendname = app.app.replaceDollarZero(atomline[8]);
		receivename = atomline[7];
		
		// set the value to the init value if possible
		setval(Float.parseFloat(atomline[11]), 0);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
		
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		// try and load SVGs
		on = getSVG(TAG, "on", sendname, receivename);
		off = getSVG(TAG, "off", sendname, receivename);
		
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
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1, dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom, paint);
		}
		canvas.drawText(fmt.format(val), dRect.left + dRect.width() / 2, (int)(dRect.top + dRect.height() * 0.75), paint);
	}
}

