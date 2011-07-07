package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

public class Touch extends Widget {
	private static final String TAG = "Touch";
	
	boolean down = false;
	
	public Touch(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		sendname = atomline[7];
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(x + 1, y, x + w - 1, y, paint);
		canvas.drawLine(x + 1, y + h, x + w - 1, y + h, paint);
		canvas.drawLine(x, y + 1, x, y + h - 1, paint);
		canvas.drawLine(x + w, y, x + w, y + h, paint);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if ((event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE) && inside(ex, ey)) {
			send(((ex - x) / w) + " " + ((ey - y) / h));
			down = true;
		}
		if (event.getAction() == event.ACTION_UP && down) {
			send(0 + " " + 0);
		}
	}
}
