package cx.mccormick.pddroidparty;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class Numberbox2 extends Numberbox {
	private static final String TAG = "Nbx";
	
	public Numberbox2(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		Rect tRect = new Rect();
		
		// calculate screen bounds for the numbers that can fit
		numwidth = Integer.parseInt(atomline[5]);
		StringBuffer calclen = new StringBuffer();
		for (int s=0; s<numwidth; s++) {
			if (s == 1) {
				calclen.append(".");
			} else {
				calclen.append("#");
			}
		}
		fmt = new DecimalFormat(calclen.toString());
		paint.getTextBounds(">" + calclen.toString(), 0, calclen.length() + 1, tRect);
		dRect.set(tRect);
		dRect.sort();
		dRect.offset((int)x, (int)y);
		dRect.top -= 3;
		dRect.bottom += 3;
		dRect.left -= 3;
		dRect.right += 3;

		float h = Float.parseFloat(atomline[6]) ;
		float diff = h - dRect.height();
		if (diff > 0) {
			dRect.bottom += diff / 2;
			dRect.top -= diff / 2;
		}
		
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		init = Integer.parseInt(atomline[10]);
		sendname = app.app.replaceDollarZero(atomline[11]);
		receivename = atomline[12];
		label = setLabel(atomline[13]);
		labelpos[0] = Float.parseFloat(atomline[14]) ;
		labelpos[1] = Float.parseFloat(atomline[15]) ;
		
		// set the value to the init value if possible
		setval(Float.parseFloat(atomline[21]), 0);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 5, dRect.top, paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom, paint);
		canvas.drawLine(dRect.right, dRect.top + 5, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.right - 5, dRect.top, dRect.right, dRect.top + 5, paint);
		canvas.drawText(">" + fmt.format(val), dRect.left + 3, dRect.centerY() + dRect.height() * (float)0.25, paint);
	}
}

