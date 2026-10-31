package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.RectF;
import java.util.Arrays;

public class Touch extends Widget {
	private static final String TAG = "Touch";

	WImage on = new WImage();
	WImage off = new WImage();

	boolean multi = false;
	int[] pids = new int[20]; //pointer ids in use

	public Touch(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));

		sendname = app.app.replaceDollarZero(atomline[7]);

		int i = 8;
		while(atomline.length > i) {
			switch(atomline[i]) {
				case "multi": multi = true;
			}
			i++;
		}
		// try and load images
		on.load(TAG, "on", sendname);
		off.load(TAG, "off", sendname);
		Arrays.fill(pids, -1);
	}

	private int getPidIndex(int pid) {
		for(int i = 0; i < pids.length; i++) {
			if(pids[i] == pid) return i;
		}
		return -1;
	}

	private int takePid(int pid) {
		int i0 = getPidIndex(pid);
		if(i0 != -1) return i0; // shouldn't happen
		for(int i = 0; i < pids.length; i++) {
			if(pids[i] == -1) {
				pids[i] = pid;
				return i;
			}
		}
		return -1;
	}

	private void releasePid(int pid) {
		int index = getPidIndex(pid);
		if(index != -1) pids[index] = -1;
	}

	private void releasePidIndex(int index) {
		if(index != -1) pids[index] = -1;
	}

	private int countPids() {
		int count = 0;
		for(int i = 0; i < pids.length; i++) {
			if(pids[i] != -1) count++;
		}
		return count;
	}

	public void draw(Canvas canvas) {
		boolean down = (countPids() > 0);
		paint.setStrokeWidth(down ? 2 : 1);

		if (down ? on.draw(canvas) : off.draw(canvas)) {
			canvas.drawLine(dRect.left + 1, dRect.top, dRect.right - 1, dRect.top, paint);
			canvas.drawLine(dRect.left + 1, dRect.bottom, dRect.right - 1, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top + 1, dRect.left, dRect.bottom - 1, paint);
			canvas.drawLine(dRect.right, dRect.top, dRect.right, dRect.bottom, paint);
		}
	}

	public void Sendxy(int index, float x, float y)
	{
		float xnorm = (x - dRect.left) / dRect.width();
		float ynorm = (y - dRect.top) / dRect.height();
		if(multi) send(xnorm + " " + ynorm + " " + index);
		else send(xnorm + " " + ynorm);
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			int index = takePid(pid);
			if(index == -1) return false;
			Sendxy(index, x, y);
			return true;
		}
		return false;
	}

	public boolean touchup(int pid, float x, float y)
	{
		int index = getPidIndex(pid);
		if(index != -1) {
			releasePidIndex(index);
			send("-1 -1 " + index);
			return true;
		}
		return false;
	}

	public boolean touchmove(int pid, float x, float y)
	{
		int index = getPidIndex(pid);
		if(index != -1) {
			Sendxy(index, x, y);
			return true;
		}
		return false;
	}
}
