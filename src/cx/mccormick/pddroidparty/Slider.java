package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Paint;

public class Slider extends Widget {
	private static final String TAG = "Slider";
	
	float min, max;
	int log;
	boolean jump;
	
	int pid0=-1;			// pointer id,
	float x0,y0,val0 ; 	// position of pointer, and value when pointer down.
	
	boolean orientation_horizontal = true;
	boolean down = false;
	
	SVGRenderer svg = null;
	SVGRenderer slider = null;
	
	RectF sRect = new RectF();
	
	public Slider(PdDroidPatchView app, String[] atomline, boolean horizontal) {
		super(app);
		
		orientation_horizontal = horizontal;
		
		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;
		
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		sendname = app.app.replaceDollarZero(atomline[11]);
		receivename = atomline[12];
		label = setLabel(atomline[13]);
		labelpos[0] = Float.parseFloat(atomline[14]) ;
		labelpos[1] = Float.parseFloat(atomline[15]) ;
		labelfont = Integer.parseInt(atomline[16]);
		labelsize = (int)(Float.parseFloat(atomline[17]));
		bgcolor = getColor(Integer.parseInt(atomline[18]));
		fgcolor = getColor(Integer.parseInt(atomline[19]));
		labelcolor = getColor(Integer.parseInt(atomline[20]));

		setval((float)(Float.parseFloat(atomline[21]) * 0.01 * (max - min) / ((horizontal ? Float.parseFloat(atomline[5]) : Float.parseFloat(atomline[6])) - 1) + min), min);
		jump = (Float.parseFloat(atomline[22])==0) ;
		// listen out for floats from Pd
		setupreceive();
		
		// send initial value if we have one
		initval();
		
		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		// load up the SVG to use and cache all positions
		if (horizontal) {
			svg = getSVG(TAG, "horizontal", label, sendname);
			slider = getSVG(TAG, "widget-horizontal", label, sendname);
			/*for (float sx = dRect.left; sx < dRect.left + dRect.width(); sx++) {
				// hit the cache for this value
				svg.interpolate("closed", "open", (sx - min) / (max - min));
			}*/
		} else {
			svg = getSVG(TAG, "vertical", label, sendname);
			slider = getSVG(TAG, "widget-vertical", label, sendname);
			/*for (float sy = dRect.top; sy < dRect.top + dRect.height(); sy++) {
				// hit the cache for this value
				svg.interpolate("closed", "open", (sy - min) / (max - min));
			}*/
		}
		
		if (svg != null && slider != null) {
			// create the slider rectangle thingy
			if (orientation_horizontal) {
				float ratio = Float.parseFloat(slider.getAttribute("height")) / h;
				int rel = (int)(Float.parseFloat(slider.getAttribute("width")) / ratio);
				sRect = new RectF(x, y, x + rel, y + h);
			} else {
				float ratio = Float.parseFloat(slider.getAttribute("width")) / w;
				int rel = (int)(Float.parseFloat(slider.getAttribute("height")) / ratio);
				sRect = new RectF(x, y, x + w, y + rel);
			}
		}
		
		slider_setval(val);
		
		// interpolate the svg paths with ID "open" and "closed"
		//if (svg != null) {
		//	svg.interpolate("closed", "open", 0.5);
		//}
	}
	
	public void draw(Canvas canvas) {
		if (drawPicture(canvas, svg)) {
			paint.setColor(bgcolor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawRect(dRect,paint);

			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawLine(dRect.left /*+ 1*/, dRect.top, dRect.right, dRect.top, paint);
			canvas.drawLine(dRect.left /*+ 1*/, dRect.bottom, dRect.right, dRect.bottom, paint);
			canvas.drawLine(dRect.left, dRect.top /*+ 1*/, dRect.left, dRect.bottom, paint);
			canvas.drawLine(dRect.right, dRect.top /*+ 1*/, dRect.right, dRect.bottom, paint);
			paint.setColor(fgcolor);
			paint.setStrokeWidth(3);
			if (orientation_horizontal) {
				canvas.drawLine(Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.top /*+ 2*/), Math.round(dRect.left + ((val - min) / (max - min)) * dRect.width()), Math.round(dRect.bottom /*- 2*/), paint);
			} else {
				canvas.drawLine(Math.round(dRect.left /*+ 2*/), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), Math.round(dRect.right /*- 2*/), Math.round(dRect.bottom - ((val - min) / (max - min)) * dRect.height()), paint);
			}

			drawLabel(canvas);
		} else if (slider != null) {
			if (orientation_horizontal) {
				sRect.offsetTo((val - min) / (max - min) * (dRect.width() - sRect.width()) + dRect.left, dRect.top);
			} else {
				sRect.offsetTo(dRect.left, (1 - (val - min) / (max - min)) * (dRect.height() - sRect.height()) + dRect.top);
			}
			drawPicture(canvas, slider, sRect);
		}
	}

	
	public void slider_setval(float v) {
		val = Math.min(max, Math.max(min, v));
		if (svg != null) {
			// Log.e("Slider", "" + ((val - min) / (max - min)));
			if (slider == null) {
				svg.interpolate("closed", "open", (val - min) / (max - min));
			}
		}
	}
	
	public float get_horizontal_val(float x) {
		return (((x - dRect.left) / dRect.width()) * (max - min) + min);
	}
	
	public float get_vertical_val(float y) {
		return (((dRect.height() - (y - dRect.top)) / dRect.height()) * (max - min) + min);
	}
	
	public boolean touchdown(int pid,float x,float y)
	{
		if (dRect.contains(x, y)) {
			val0=val;
			x0=x;
			y0=y;
			pid0=pid;
			if(jump) {
				if (orientation_horizontal) val = get_horizontal_val(x);
				else val = get_vertical_val(y);
			}
			send("" + val);
			return true;
		}
		return false;
	}

	public boolean touchup(int pid,float x,float y)
	{
		if(pid0 == pid) {
			pid0 = -1;
			//return true;
		}
		return false;
	}

	public boolean touchmove(int pid,float x,float y)
	{
		if(pid0 == pid) {
			if(jump) {
				if (orientation_horizontal) val = get_horizontal_val(x);
				else val = get_vertical_val(y);
			}
			else {
				if (orientation_horizontal) 
					val = val0 + get_horizontal_val(x) - get_horizontal_val(x0);
				else
					val = val0 + get_vertical_val(y) - get_vertical_val(y0);
			}
			// clamp the value
			slider_setval(val);
			// send the result to Pd
			send("" + val);
			return true;
		}
		return false;
	}
	
	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol,args)) return;
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
	
	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
	
	public void receiveFloat(float v) {
		slider_setval(v);
	}
}


