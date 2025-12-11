package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

public class Numberbox2 extends Numberbox {
	private static final String TAG = "Nbx";

	public Numberbox2(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float h = Float.parseFloat(atomline[6]) ;
		Rect tRect = new Rect();

		// calculate screen bounds for the numbers that can fit
		numwidth = Integer.parseInt(atomline[5]);
		StringBuffer calclen = new StringBuffer();
		for (int s = 0; s < numwidth; s++) {
			calclen.append("0");
		}
		paint.getTextBounds(">" + calclen.toString(), 0, calclen.length() + 1, tRect);
		
		dRect.set(x, y, x + tRect.width() + 8, y + h);
		dRect.sort();

		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		init = Integer.parseInt(atomline[10]);
		initCommonArgs(app, atomline, 11);

		// set the value to the init value if possible
		setval(Float.parseFloat(atomline[21]), 0);

		// listen out for floats from Pd
		setupreceive();

		// send initial value if we have one
		initval();
	}

	public void draw(Canvas canvas) {
		Path path = new Path();
		path.moveTo(dRect.left, dRect.top);
		path.lineTo(dRect.right - 5, dRect.top);
		path.lineTo(dRect.right, dRect.top + 5);
		path.lineTo(dRect.right, dRect.bottom);
		path.lineTo(dRect.left, dRect.bottom);
		path.close();
		path.lineTo(dRect.left + 6, dRect.top + dRect.height() / 2);
		path.lineTo(dRect.left, dRect.bottom);

		paint.setColor(bgcolor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(path, paint);

		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawPath(path, paint);

		paint.setStrokeWidth(0);
		paint.setColor(fgcolor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawText(formatNumber(val, numwidth), dRect.left + 8, dRect.centerY() + dRect.height() * (float)0.25, paint);
		drawLabel(canvas);
	}
}

