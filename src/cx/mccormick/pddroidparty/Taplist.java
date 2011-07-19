package cx.mccormick.pddroidparty;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.view.MotionEvent;
import android.util.Log;

import org.puredata.core.PdBase;

public class Taplist extends Widget {
	private static final String TAG = "Taplist";
	
	String longest = null;
	ArrayList<String> atoms = new ArrayList<String>();
	
	Rect dRect = new Rect();
	
	public Taplist(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		fontsize = (int)(h * 0.8);

		// get list atoms
		for (int a=9; a<atomline.length; a++) {
			atoms.add(atomline[a]);
			paint.getTextBounds(atomline[a], 0, atomline[a].length(), dRect);
			Log.e("RECT", dRect.toString());
		}
		
		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);
		
		sendname = atomline[8];
		receivename = atomline[7];
		
		setval(0, 0);
		
		// listen out for floats from Pd
		setupreceive();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(x + 1, y, x + w - 1, y, paint);
		canvas.drawLine(x + 1, y + h, x + w - 1, y + h, paint);
		canvas.drawLine(x, y + 1, x, y + h - 1, paint);
		canvas.drawLine(x + w, y, x + w, y + h, paint);
		canvas.drawText(atoms.get((int)val), x + w / 2, (int)(y + h * 0.8), paint);
	}
	
	private void doSend() {
		parent.app.send(sendname, atoms.get((int)val));
		PdBase.sendFloat(sendname + "/idx", val);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && inside(ex, ey)) {
			// go to the next item in our list
			val = (val + 1) % atoms.size();
			doSend();
		}
	}
	
	public void receiveFloat(float v) {
		val = v % atoms.size();
		doSend();
	}
}
