package cx.mccormick.pddroidparty;

import java.io.File;
import java.util.ArrayList;

import org.puredata.core.PdBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

public class Widget {
	private static final String TAG = "Widget";
	
 	RectF dRect = new RectF();
	
	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	float val = 0;
	int init = 0;
	String sendname = null;
	String receivename = null;
	String label = null;
	float[] labelpos = new float[2];
	int labelfont=0;
	int labelsize=14;
	Typeface font = Typeface.create("Courier", Typeface.BOLD);
	int fontsize = 0;
	float[] textoffset = new float[2];
	
	int bgcolor=0xFFFFFFFF, fgcolor=0xFF000000, labelcolor=0xFF000000;
	
	PdDroidPatchView parent = null;
	
	private static int IEM_GUI_MAX_COLOR = 30;
	private static int iemgui_color_hex[] = {
		16579836, 10526880, 4210752, 16572640, 16572608,
		16579784, 14220504, 14220540, 14476540, 16308476,
		14737632, 8158332, 2105376, 16525352, 16559172,
		15263784, 1370132, 2684148, 3952892, 16003312,
		12369084, 6316128, 0, 9177096, 5779456,
		7874580, 2641940, 17488, 5256, 5767248
	};

	
	public Widget(PdDroidPatchView app) {
		parent = app;
		fontsize = (int)((float)parent.fontsize);
		
		File f = null;
		
		// set an aliased font
		f = new File(parent.app.getPatchFile().getParent() + "/font.ttf");
		if (f.exists() && f.canRead() && f.isFile()) {
			font = Typeface.createFromFile(f);
		} else {
			// set an anti-aliased font
			f = new File(parent.app.getPatchFile().getParent() + "/font-antialiased.ttf");
			if (f.exists() && f.canRead() && f.isFile()) {
				font = Typeface.createFromFile(f);
				paint.setAntiAlias(true);
			}
		}
		
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTypeface(font);
		paint.setTextSize(fontsize);
		textoffset[0] = 0.0f;
		textoffset[1] = 0.0f;
	}
	
	public static int getColor(int iemcolor) {
		//Log.e("ORIGINAL COLOR", "" + iemcolor);
		int color = 0;		

		if(iemcolor < 0)
		{
			iemcolor = -1 - iemcolor;
			color = ((iemcolor & 0x3f000) << 6 )
					+ ((iemcolor & 0xfc0) << 4 )
					+ ((iemcolor & 0x3f) << 2 )
					+ 0xFF000000;
					//(iemcolor & 0xffffff) + 0xFF000000;
		}
		else
		{
			color = (iemgui_color_hex[iemcolor%IEM_GUI_MAX_COLOR] & 0xFFFFFF) | 0xFF000000;
		}
		
		//Log.e("COLOR", "" + color);
		return color;
	}
		
	public static int getColor24(int iemcolor) {
		//Log.e("ORIGINAL COLOR", "" + iemcolor);
		int color = 0;		

		if(iemcolor < 0)
		{
			iemcolor = -1 - iemcolor;
			color = (iemcolor & 0xffffff) + 0xFF000000;
		}
		else
		{
			color = (iemgui_color_hex[iemcolor%IEM_GUI_MAX_COLOR] & 0xFFFFFF) | 0xFF000000;
		}
		
		//Log.e("COLOR", "" + color);
		return color;
	}
	
	public void setTextParametersFromSVG(SVGRenderer svg) {
		if (svg != null) {
			if (svg.getAttribute("textFont") != null) {
				File f = new File(parent.app.getPatchFile().getParent() + "/" + svg.getAttribute("textFont") + ".ttf");
				if (f.exists() && f.canRead() && f.isFile()) {
					font = Typeface.createFromFile(f);
				} else {
					Log.e("PdDroidParty", "Bad font file: " + svg.getAttribute("textFont"));
				}
				paint.setTypeface(font);
			}
			if (svg.getAttribute("textColor") != null) {
				try {
					paint.setColor(Color.parseColor(svg.getAttribute("textColor")));
				} catch (Exception e) {
					// badly formatted color string - who cares?
					Log.e("PdDroidParty", "Bad text color: " + svg.getAttribute("textColor"));
				}
			}
			if (svg.getAttribute("textAntialias") != null) {
				paint.setAntiAlias(true);
			}
			if (svg.getAttribute("textOffset") != null) {
				try {
					String[] xy = svg.getAttribute("textOffset").split(" ");
					textoffset[0] = Float.parseFloat(xy[0]);
					textoffset[1] = Float.parseFloat(xy[1]);
				} catch (Exception e) {
					Log.e("PdDroidParty", "Bad text offset: " + svg.getAttribute("textOffset"));
					Log.e("PdDroidParty", e.toString());
				}
			}
		}
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
	
	public void drawLabel(Canvas canvas) {
		if (label != null) {
			paint.setStrokeWidth(0);
			paint.setColor(labelcolor);
			paint.setTextSize(labelsize);
			canvas.drawText(label, dRect.left + labelpos[0], dRect.top + labelpos[1] + labelsize / 3, paint);
			paint.setTextSize(fontsize);
		}
		paint.setColor(Color.BLACK);
	}
	
	public void drawCenteredText(Canvas canvas, String text) {
		paint.setStrokeWidth(0);
		if (text != null) {
			canvas.drawText(text, dRect.left + dRect.width() / 2 + dRect.width() * textoffset[0], (int) (dRect.top + dRect.height() * 0.75 + dRect.height() * textoffset[1]), paint);
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

	/**
	 * Generic touch methods : pid=pointer id
	 **/	
	public boolean touchdown(int pid, float x, float y) {
		return false;
	}
	public boolean touchmove(int pid, float x, float y) {
		return false;
	}
	public boolean touchup(int pid, float x, float y) {
		return false;
	}

	/**
	 * Generic setval method
	 **/	
	public void setval(float v) {
		val=v;
	}

	public boolean widgetreceiveSymbol(String symbol, Object... args) {
		if( symbol.equals("label")
		&& args.length > 0 && args[0].getClass().equals(String.class)
		) {
			label = setLabel((String)args[0]);
			return true;
		}

		if( symbol.equals("label_pos")
		&& args.length > 1 && args[0].getClass().equals(Float.class)
		&& args[1].getClass().equals(Float.class)
		) {
			labelpos[0]= (Float)args[0] ;
			labelpos[1]= (Float)args[1] ;
			return true;
		}

		if( symbol.equals("pos")
		&& args.length > 1 && args[0].getClass().equals(Float.class)
		&& args[1].getClass().equals(Float.class)
		) {
			dRect.offsetTo((Float)args[0] , (Float)args[1]);
			return true;
		}

		if( symbol.equals("color")
		&& args.length > 2 && args[0].getClass().equals(Float.class)
		&& args[1].getClass().equals(Float.class)
		&& args[2].getClass().equals(Float.class)
		) {
			bgcolor = getColor24((int)(float)(Float)args[0]);
			fgcolor = getColor24((int)(float)(Float)args[1]);
			labelcolor = getColor24((int)(float)(Float)args[2]);
			//Log.e(TAG, "msg bgcolor = "+(int)(float)(Float)args[0]+", bgcolor = "+bgcolor);
			return true;
		} 

		if( symbol.equals("label_font")
		&& args.length > 1 && args[0].getClass().equals(Float.class)
		&& args[1].getClass().equals(Float.class)
		) {
			labelfont = (Integer)args[0];
			labelsize = (Integer)args[1];
			return true;
		}

		if( symbol.equals("set")
		&& args.length > 0 && args[0].getClass().equals(Float.class)
		) {
			setval((Float)args[0]);
			return true;
		}
		
		return false;

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
	public class WImage {
		
		SVGRenderer svg = null;
		Bitmap bitmap = null;
		
		public WImage(){
			svg = null;
			bitmap = null;
		}
		
		public boolean none(){
			return (svg == null && bitmap == null);
		}
		
		// ***** Loading :
		
		public SVGRenderer getSVG(String prefix, String suffix, Object... args) {
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
					return svg;
				}
			}
			
			return null;
		}
		
		public Bitmap getBitmap(String prefix, String suffix, Object... args) {
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
				File f = new File(parent.app.getPatchFile().getParent() + "/" + testnames.get(s) + ".png");
				if (f.exists() && f.canRead() && f.isFile()) {
					return BitmapFactory.decodeFile(f.getAbsolutePath() );
				}
			}
			
			return null;
		}
		
		public void load(String prefix, String suffix, Object... args) {	
			svg = getSVG(prefix, suffix, args);
			if (svg == null) bitmap = getBitmap(prefix, suffix, args);
		}
		
		// ***** Get attributes
		
		public float getWidth() {
			if(svg != null) return Float.parseFloat(svg.getAttribute("width"));
			else if(bitmap != null) return bitmap.getWidth();
			else return 0;
		}
		
		
		public float getHeight() {
			if(svg != null) return Float.parseFloat(svg.getAttribute("height"));
			else if(bitmap != null) return bitmap.getHeight();
			else return 0;
		}
		
		// ***** Drawing :
		
		public boolean draw(Canvas c) {
			return draw(c, dRect);
		}
		
		public boolean draw(Canvas c, RectF rect) {
			if(svg != null) c.drawPicture(svg.getPicture(), rect);
			else if(bitmap != null) c.drawBitmap(bitmap, null, rect, paint);
			else return true;
			
			return false;
		}	

	} // end of class WImage
	
}
