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

public class Comment extends Widget {
	private static final String TAG = "Comment";
	
	public Comment(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		// create the comment string
		StringBuffer buffer = new StringBuffer();
		for (int i=4; i<atomline.length; i++) {
			buffer.append(atomline[i]);
			if (i < atomline.length - 1) {
				buffer.append(" ");
			}
		}
		
		label = buffer.toString();
		labelpos[0] = Float.parseFloat(atomline[2]) ;
		labelpos[1] = Float.parseFloat(atomline[3]) ;
	}
	
	public void draw(Canvas canvas) {
		drawLabel(canvas);
	}
}
