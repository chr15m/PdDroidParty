package cx.mccormick.pddroidparty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.lang.Math;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.os.SystemClock;
import android.util.Log;

import org.puredata.core.PdBase;

public class Bang extends Widget {
	private static final String TAG = "Bang";

	boolean bang = false;
	long bangtime ;
	int interrpt,hold; //interrpt and hold time, in ms.
	
	public Bang(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[5]) ;
		
		hold = (int)Float.parseFloat(atomline[6]) ;
		interrpt = (int)Float.parseFloat(atomline[7]) ;
		init = (int)Float.parseFloat(atomline[8]) ;

		sendname = app.app.replaceDollarZero(atomline[9]);
		receivename = atomline[10];
		label = setLabel(atomline[11]);
		labelpos[0] = Float.parseFloat(atomline[12]) ;
		labelpos[1] = Float.parseFloat(atomline[13]) ;
		labelfont = Integer.parseInt(atomline[14]);
		labelsize = (int)(Float.parseFloat(atomline[15]));
		bgcolor = getColor(Integer.parseInt(atomline[16]));
		fgcolor = getColor(Integer.parseInt(atomline[17]));
		labelcolor = getColor(Integer.parseInt(atomline[18]));

		// listen out for floats from Pd
		setupreceive();

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
	}

	public Bang(PdDroidPatchView app) {
		super(app);
	}

	public void draw(Canvas canvas) {
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(bgcolor);
		canvas.drawRect(dRect, paint);

		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(1);
		canvas.drawLine(dRect.left /*+ 1*/, dRect.top, dRect.right, dRect.top, paint);
		canvas.drawLine(dRect.left + 0, dRect.bottom, dRect.right, dRect.bottom, paint);
		canvas.drawLine(dRect.left, dRect.top + 0, dRect.left, dRect.bottom, paint);
		canvas.drawLine(dRect.right, dRect.top + 0, dRect.right, dRect.bottom, paint);
		if (bang) {
			if((SystemClock.uptimeMillis()-bangtime)>hold) bang = false;
			//paint.setStyle(Paint.Style.FILL);
			
			parent.threadSafeInvalidate();
			canvas.drawCircle(dRect.centerX(), dRect.centerY(), Math.min(dRect.width(), dRect.height()) / 2, paint);
			paint.setColor(fgcolor);
			
		} /*else {
			paint.setStyle(Paint.Style.STROKE);
		}*/
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(dRect.centerX(), dRect.centerY(), Math.min(dRect.width(), dRect.height()) / 2, paint);
		drawLabel(canvas);
	}

	private void bang() {
		bang = true;
		bangtime = SystemClock.uptimeMillis();
		PdBase.sendBang(sendname);
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			bang();
			return true;
		}
		
		return false;
	}
	
	/*public void receiveAny() {
		bang();
	}*/
	
	public void receiveList(Object... args) {
		bang();
	}
	
	
	public void receiveSymbol(String symbol) {
		bang();
	}
	
	public void receiveFloat(float x) {
		bang();
	}
	
	public void receiveBang() {
		bang();
	}

	
	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol,args)) return;
		else bang();
	}

}
