package cx.mccormick.pddroidparty;

import android.graphics.Canvas;

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
