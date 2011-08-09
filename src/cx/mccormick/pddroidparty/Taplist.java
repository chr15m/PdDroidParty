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
	
	public Taplist(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		fontsize = (int)(h * 0.75);
		
		// get list atoms
		for (int a=9; a<atomline.length; a++) {
			atoms.add(atomline[a]);
		}
		
		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);
		
		sendname = atomline[8];
		receivename = atomline[7];
		
		setval(0, 0);
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		// listen out for floats from Pd
		setupreceive();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1, dRect.top, paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom - 1, paint);
		canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom, paint);
		canvas.drawText(atoms.get((int)val), dRect.left + dRect.width() / 2, (int)(dRect.top + dRect.height() * 0.75), paint);
	}
	
	private void doSend() {
		parent.app.send(sendname, atoms.get((int)val));
		PdBase.sendFloat(sendname + "/idx", val);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && dRect.contains(ex, ey)) {
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
