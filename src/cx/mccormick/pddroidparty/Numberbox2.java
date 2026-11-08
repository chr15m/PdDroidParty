package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

public class Numberbox2 extends Numberbox {
	private static final String TAG = "Nbx";
	private float ascent = 0.0f;

	public Numberbox2(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float h = Float.parseFloat(atomline[6]) ;
		Rect tRect = new Rect();

		numwidth = Integer.parseInt(atomline[5]);

		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		init = Integer.parseInt(atomline[10]);
		initCommonArgs(app, atomline, 11);

		// calculate screen bounds for the numbers that can fit
		StringBuffer calclen = new StringBuffer();
		for (int s = 0; s < numwidth; s++) {
			calclen.append("0");
		}
		paint.setTextSize(labelsize);
		paint.getTextBounds(calclen.toString(), 0, calclen.length(), tRect);
		ascent = tRect.height();
		dRect.set(x, y, x + tRect.width() + h / 2.0f + 5, y + h);
		dRect.sort();

		// set the value to the init value if possible
		setval(Float.parseFloat(atomline[21]), 0);

		// listen out for floats from Pd
		setupreceive();

		// send initial value if we have one
		initval();
	}

	public void draw(Canvas canvas) {
		float corner = dRect.height() / 4.0f;
		Path path = new Path();
		path.moveTo(dRect.left, dRect.top);
		path.lineTo(dRect.right - corner, dRect.top);
		path.lineTo(dRect.right, dRect.top + corner);
		path.lineTo(dRect.right, dRect.bottom);
		path.lineTo(dRect.left, dRect.bottom);
		path.close();

		paint.setColor(bgcolor);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(path, paint);

		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawPath(path, paint);

		paint.setColor(fgcolor);
		paint.setStyle(Paint.Style.STROKE);
		float half = dRect.height() / 2.0f;
		path.reset();
		path.moveTo(dRect.left + 1, dRect.top + 1);
		path.lineTo(dRect.left + half, dRect.top + half);
		path.lineTo(dRect.left + 1, dRect.bottom - 1);
		canvas.drawPath(path, paint);

		paint.setStrokeWidth(0);
		paint.setColor(fgcolor);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(labelsize);
		canvas.drawText(formatNumber(val, numwidth),
			dRect.left + half + 2, dRect.centerY() + ascent / 2.0f, paint);
		drawLabel(canvas);
	}
}

