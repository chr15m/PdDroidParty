package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Rect;

public class Comment extends Widget {
	private static final String TAG = "Comment";

	private static boolean isNumeric(String str) { 
		try {
			Double.parseDouble(str);
			return true;
		} catch(NumberFormatException e){
			return false;
		}
	}

	public Comment(PdDroidPatchView app, String[] atomline) {
		super(app);

		Rect tRect = new Rect();
		paint.getTextBounds("0y", 0, 2, tRect);

		// remove trailing "width" sequence, if any:
		int stopAt = atomline.length - 1;
		if(atomline.length >= 3
				&& atomline[atomline.length - 2].equals("f")
				&& atomline[atomline.length - 3].endsWith(",")
				&& isNumeric(atomline[atomline.length - 1])) {
			stopAt = atomline.length - 3;
			String lastAtom = atomline[atomline.length - 3];
			atomline[atomline.length - 3] = lastAtom.substring(0, lastAtom.length() - 1);
		}
		// create the comment string
		StringBuffer buffer = new StringBuffer();
		for (int i = 4; i <= stopAt; i++) {
			buffer.append(atomline[i]);
			if (i < stopAt) {
				buffer.append(" ");
			}
		}

		label = buffer.toString();
		labelpos[0] = Float.parseFloat(atomline[2]) + 2;
		labelpos[1] = Float.parseFloat(atomline[3]) + tRect.height();
	}

	public void draw(Canvas canvas) {
		drawLabel(canvas);
	}
}
