package cx.mccormick.pddroidparty;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;

public class Widget {
	
	float x, y, w, h;
	
	/**
	 * Generic draw method for all widgets.
	 * @param gl
	 */
	public void draw(GL10 gl) {
	}
	
	/**
	 * Generic touch method for a hit test.
	 * @param gl
	 */	
	public void touch(MotionEvent event) {
	}
	
	public boolean inside(float ex, float ey) {
		return !(ex < x || ex > x + w || ey < y || ey > y + h);
	}
}



