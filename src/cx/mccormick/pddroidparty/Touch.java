package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.RectF;

public class Touch extends Widget {
	private static final String TAG = "Touch";
	
	SVGRenderer on = null;
	SVGRenderer off = null;
	
	boolean down = false;
	int pid0 = -1; //pointer id when down
	
	public Touch(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		sendname = app.app.replaceDollarZero(atomline[7]);
		
		// try and load SVGs
		on = getSVG(TAG, "on", label);
		off = getSVG(TAG, "off", label);
	}
	
	public void draw(Canvas canvas) {
		if (down) {
			paint.setStrokeWidth(2);
		} else {
			paint.setStrokeWidth(1);
		}
		
		if (down ? drawPicture(canvas, on) : drawPicture(canvas, off)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1, dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom, paint);
		}
	}
	
	public void Sendxy(float x, float y)
	{
		send(((x - dRect.left) / dRect.width()) + " "
				+ ((y - dRect.top) / dRect.height()));
	}
	
	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {

			down = true;
			pid0 = pid;
			Sendxy(x,y);
			return true;
		}
		
		return false;
	}
	
	public boolean touchup(int pid, float x, float y)
	{
		if(pid == pid0) {
			down = false;
			pid0 = -1;
			send("-1 -1");
		}
		return false;
	}
	
	public boolean touchmove(int pid, float x, float y)
	{
		if(pid == pid0) {
			Sendxy(x,y);		
			return true;
		}
		return false;	
	}

}
