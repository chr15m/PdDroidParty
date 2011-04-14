package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public class Widget {
	
	float x, y, w, h;
	
	/**
	 * Generic draw method for all widgets.
	 * @param canvas
	 */
	public void draw(Canvas canvas, Paint paint) {
	}
	
	/**
	 * Generic touch method for a hit test.
	 * @param event
	 */	
	public void touch(MotionEvent event) {
	}
	
	public boolean inside(float ex, float ey) {
		return !(ex < x || ex > x + w || ey < y || ey > y + h);
	}
}



