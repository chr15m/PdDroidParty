package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

public class Widget {
	float x, y, w, h;
	
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



