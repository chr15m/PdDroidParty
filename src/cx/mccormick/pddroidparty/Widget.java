package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

public class Widget {
	int screenwidth=0;
	int screenheight=0;
	
	float x, y, w, h;
	
	int init = 0;
	String sendname = null;
	String receivename = null;
	String label = null;
	PdDroidPatchView parent=null;
	
	public Widget(PdDroidPatchView app) {
		parent = app;
		screenwidth = parent.getWidth();
		screenheight = parent.getHeight();
	}
	
	public void send(String msg) {
		if (sendname != null && !sendname.equals("")) {
			parent.app.send(sendname, msg);
		}
	}
	
	/**
	 * Generic draw method for all widgets.
	 * @param canvas
	 */
	public void draw(Canvas canvas) {
	}
	
	/**
	 * Generic touch method for a hit test.
	 * @param event
	 */	
	public void touch(MotionEvent event) {
	}
	
	public void receiveList(Object... args) {
	}
	
	public void receiveMessage(String symbol, Object... args) {
	}
	
	public void receiveSymbol(String symbol) {
	}
	
	public void receiveFloat(float x) {
	}
	
	public void receiveBang() {
	}
	
	public boolean inside(float ex, float ey) {
		return !(ex < x || ex > x + w || ey < y || ey > y + h);
	}
}



