package cx.mccormick.pddroidparty;

import java.io.File;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Picture;
import android.view.MotionEvent;
import android.util.Log;
import android.text.StaticLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.Log;

import org.puredata.core.PdBase;

public class Widget {
	private static final String TAG = "Widget";
	int screenwidth=0;
	int screenheight=0;
	int WRAPWIDTH = 360;
	
	float x, y, w, h;
	
	Paint paint = new Paint();
	
	float val = 0;
	int init = 0;
	String sendname = null;
	String receivename = null;
	String label = null;
	float[] labelpos = new float[2];
	PdDroidPatchView parent = null;
	
	Typeface font = Typeface.create("Courier", Typeface.BOLD);
	int fontsize = 0;
	StaticLayout textLayout = null;
	
	public Widget(PdDroidPatchView app) {
		parent = app;
		screenwidth = parent.getWidth();
		screenheight = parent.getHeight();
		fontsize = (int)((float)parent.fontsize / parent.patchheight * screenheight) - 2;
		
		File f = new File(parent.app.getPatchFile().getParent() + "/font.ttf");
		if (f.exists() && f.canRead() && f.isFile()) {
			font = Typeface.createFromFile(f);
		}
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setTypeface(font);
		paint.setTextSize(fontsize);
	}
	
	public void send(String msg) {
		if (sendname != null && !sendname.equals("") && !sendname.equals("empty")) {
			parent.app.send(sendname, msg);
		}
	}
	
	public void sendFloat(float f) {
		if (sendname != null && !sendname.equals("") && !sendname.equals("empty")) {
			PdBase.sendFloat(sendname, f);
		}
	}

	public void setupreceive() {
		// listen out for floats from Pd
		if (receivename != null && !receivename.equals("") && !receivename.equals("empty")) {
			parent.app.registerReceiver(receivename, this);
		}
	}
	
	public void setval(float v, float alt) {
		if (init != 0) {
			val = v;
		} else {
			val = alt;
		}
	}
	
	public void initval() {
		if (init != 0) {
			send("" + val);
		}
	}
	
	public float getval() {
		return val;
	}
	
	/* Draw the label */	
	public void drawLabel(Canvas canvas) {
		if (label != null) {
			if (textLayout == null) {
				textLayout = new StaticLayout((CharSequence)label, new TextPaint(paint), (int)((float)WRAPWIDTH / parent.patchwidth * screenwidth), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
			}
			canvas.save();
			canvas.translate(x + labelpos[0] + 2, y + labelpos[1] + 2 - fontsize / 2);
			textLayout.draw(canvas);
			canvas.restore();
		}
	}
	
	/* Set the label (checking for special null values) */
	public String setLabel(String incoming) {
		if (incoming.equals("-") || incoming.equals("empty")) {
			return null;
		} else {
			return incoming;
		}
	}
	
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
		Log.e(TAG, "dropped list");
	}
	
	public void receiveMessage(String symbol, Object... args) {
		Log.e(TAG, "dropped message");
	}
	
	public void receiveSymbol(String symbol) {
		Log.e(TAG, "dropped symbol");
	}
	
	public void receiveFloat(float x) {
		Log.e(TAG, "dropped float");
	}
	
	public void receiveBang() {
		Log.e(TAG, "dropped bang");
	}
	
	public void receiveAny() {
	}
	
	public boolean inside(float ex, float ey) {
		return !(ex < x || ex > x + w || ey < y || ey > y + h);
	}
	
	public Picture getPicture(String name) {
		return SVGLoader.getPicture(parent, name);
	}
}
