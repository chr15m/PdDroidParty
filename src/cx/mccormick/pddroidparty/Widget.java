package cx.mccormick.pddroidparty;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Picture;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.text.StaticLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import org.puredata.core.PdBase;

public class Widget {
	private static final String TAG = "Widget";
	int screenwidth=0;
	int screenheight=0;
	int WRAPWIDTH = 360;
	
	RectF dRect = new RectF();
	
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
		
		// set an aliased font
		File f = new File(parent.app.getPatchFile().getParent() + "/font.ttf");
		if (f.exists() && f.canRead() && f.isFile()) {
			font = Typeface.createFromFile(f);
		}
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
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
			canvas.translate(dRect.left + labelpos[0] + 2, dRect.top + labelpos[1] + 2 - fontsize / 2);
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
	
	/***** Special SVG GUI drawing stuff *****/
	
	public Picture getPicture(String prefix, String suffix, Object... args) {
		// split the string into parts on underscore, so we test on e.g.
		// blah_x_y, blah, blah_x, blah_x_y
		ArrayList<String> testnames = new ArrayList<String>();
		ArrayList<String> parts = new ArrayList<String>();
		if (prefix != null)
			parts.add(prefix);
		if (suffix != null)
			parts.add(suffix);
		testnames.add(TextUtils.join("-", parts));
		
		for (int a=0; a<args.length; a++) {
			String teststring = (String)args[a];
			if (teststring != null && !teststring.equals("null") && !teststring.equals("empty") && !teststring.equals("-")) {
				String[] tries = teststring.split("_");
				ArrayList<String> buffer = new ArrayList<String>();
				for (int p=0; p<tries.length; p++) {
					parts.clear();
					buffer.add(tries[p]);
					if (prefix != null)
						parts.add(prefix);
					parts.add(TextUtils.join("_", buffer));
					if (suffix != null)
						parts.add(suffix);
					testnames.add(TextUtils.join("-", parts));
				}
				parts.clear();
			}
		}
		
		// now test every combination we have come up with
		// we want to check from most specific to least specific
		for (int s = testnames.size() - 1; s >= 0; s--) {
			SVGRenderer svg = SVGRenderer.getSVGRenderer(parent, testnames.get(s));
			if (svg != null) {
				return svg.getPicture();
			}
		}
		
		return null;
	}
	
	public boolean drawPicture(Canvas c, Picture p) {
		if (p != null) {
			c.drawPicture(p, dRect);
			return false;
		} else {
			return true;
		}
	}
}
