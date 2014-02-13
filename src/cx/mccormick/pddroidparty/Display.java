package cx.mccormick.pddroidparty;

import java.util.ArrayList;

import org.puredata.core.PdBase;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Display extends Widget {
	private static final String TAG = "Display";

	SVGRenderer svg = null;

	String text = null;
	
	public Display(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;

		fontsize = (int) (h * 0.75);

		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);

		receivename = atomline[7];

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w),
				Math.round(y + h));

		// listen out for symbols from Pd
		setupreceive();

		// try and load SVGs
		svg = getSVG(TAG, "", sendname, receivename);

		setTextParametersFromSVG(svg);
	}

	public void draw(Canvas canvas) {
		if (drawPicture(canvas, svg)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1,
					dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1,
					dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left,
					dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom,
					paint);
		}
		if (text != null) {
			canvas.drawText(text, dRect.left + dRect.width() / 2, (int) (dRect.top + dRect.height() * 0.75), paint);
		}
	}
	
	public void receiveList(Object... args) {
		String sep = "";
		String result = "";
		for (Object s: args) {
			result += sep + s.toString();
			sep = " ";
		}
		text = result;
	}
	
	public void receiveMessage(String symbol, Object... args) {
		String result = "" + symbol;
		for (Object s: args) {
			result += " " + s.toString();
		}
		text = result;
	}
	
	public void receiveSymbol(String v) {
		text = "" + v;
	}
}
