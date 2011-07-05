package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.util.Log;

public class Wordbutton extends Bang {
	private static final String TAG = "Wordbutton";
	
	RectF dRect;
	Rect tRect = new Rect();

	public Wordbutton(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		w = Float.parseFloat(atomline[5]) / parent.patchwidth * screenwidth;
		h = Float.parseFloat(atomline[6]) / parent.patchheight * screenheight;
		
		sendname = "wordbutton-" + atomline[7];
		label = setLabel(atomline[7]);
		paint.getTextBounds(label, 0, label.length(), tRect);
		labelpos[0] = w / 2 - tRect.width() / 2;
		labelpos[1] = h / 2 - tRect.height() / 2;
		
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
	}
	
	public void draw(Canvas canvas) {
		canvas.drawLine(dRect.left + 1, dRect.top, dRect.right, dRect.top, paint);
		canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom, paint);
		canvas.drawLine(dRect.right, dRect.top + 1, dRect.right, dRect.bottom, paint);
		drawLabel(canvas);
	}
	
	public void touch(MotionEvent event) {
		float ex = event.getX();
		float ey = event.getY();
		if (event.getAction() == event.ACTION_DOWN && inside(ex, ey)) {
			send("bang");
		}
	}
}
