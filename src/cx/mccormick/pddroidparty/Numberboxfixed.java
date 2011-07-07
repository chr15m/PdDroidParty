package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.view.MotionEvent;
import android.util.Log;

public class Numberboxfixed extends Numberbox {
	private static final String TAG = "Numberboxfixed";
	
	public Numberboxfixed(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		fontsize = (int)(h * 0.8);
		
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
		sendname = atomline[8];
		receivename = atomline[7];
		
		// set the value to the init value if possible
		setval(Float.parseFloat(atomline[11]), 0);
		
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(x + 1, y, x + w - 1, y, paint);
		canvas.drawLine(x + 1, y + h, x + w - 1, y + h, paint);
		canvas.drawLine(x, y + 1, x, y + h - 1, paint);
		canvas.drawLine(x + w, y, x + w, y + h, paint);
		canvas.drawText(fmt.format(val), x + w / 2, (int)(y + h * 0.8), paint);
	}
}

