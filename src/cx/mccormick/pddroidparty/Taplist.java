package cx.mccormick.pddroidparty;

import java.util.ArrayList;

import org.puredata.core.PdBase;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Taplist extends Widget {
	private static final String TAG = "Taplist";

	String longest = null;
	ArrayList<String> atoms = new ArrayList<String>();

	SVGRenderer on = null;
	SVGRenderer off = null;

	boolean down = false;
	int pid0 = -1; //pointer id when down
	
	public Taplist(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;

		fontsize = (int) (h * 0.75);

		// get list atoms
		for (int a = 9; a < atomline.length; a++) {
			atoms.add(atomline[a]);
		}

		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);

		sendname = app.app.replaceDollarZero(atomline[8]);
		receivename = atomline[7];

		setval(0, 0);

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w),
				Math.round(y + h));

		// listen out for floats from Pd
		setupreceive();

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
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1,
					dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1,
					dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left,
					dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom,
					paint);
		}
		drawCenteredText(canvas, atoms.get((int) val));
	}

	private void doSend() {
		parent.app.send(sendname, atoms.get((int) val));
		PdBase.sendFloat(sendname + "/idx", val);
	}

	public boolean touchdown(int pid, float x,float y) {
		if (dRect.contains(x, y)) {
			val = (val + 1) % atoms.size();
			doSend();
			down = true;
			pid0 = pid;
			return true;
		}
		return false;
	}
	
	public boolean touchup(int pid, float x,float y) {
		if (pid == pid0) {
			down = false;
			pid0 = -1;
			return true;
		}
		return false;
	}
	
	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float) args[0]);
		}
	}

	public void receiveFloat(float v) {
		val = v % atoms.size();
		doSend();
	}
}
