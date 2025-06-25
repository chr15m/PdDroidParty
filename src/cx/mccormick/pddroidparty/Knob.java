package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Paint;
import android.util.Log;

public class Knob extends Widget {
	private static final String TAG = "Knob";

	float min, max;
	int log;
	float mouse;

	int pid0 = -1;			// pointer id,
	float x0, y0, val0 ; 	// position of pointer, and value when pointer down.
	float angle0; 			// angle of pointer when pointer down.

	boolean down = false;
	int steady = 1;

	WImage image = new WImage();

	public Knob(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;

		mouse = Float.parseFloat(atomline[6]) ;
		min = Float.parseFloat(atomline[7]);
		max = Float.parseFloat(atomline[8]);
		log = Integer.parseInt(atomline[9]);
		init = Integer.parseInt(atomline[10]);
		initCommonArgs(app, atomline, 11);
		steady = Integer.parseInt(atomline[22]);

		//setpos((float)(Float.parseFloat(atomline[21]) * 0.01 * (max - min) / ((orientation_horizontal ? Float.parseFloat(atomline[5]) : Float.parseFloat(atomline[6])) - 1) + min), min);

		// listen out for floats from Pd
		setupreceive();

		// send initial value if we have one
		//initval();

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + w));

		// load up the image to use
		image.load(TAG, null, label, sendname, receivename);
	}

	public float circlex(float radius, float angle)
	{
		return (float) (dRect.centerX() + dRect.width() / 2 * radius * Math.cos(Math.toRadians(-angle - 90)));
	}

	public float circley(float radius, float angle)
	{
		return (float) (dRect.centerY() - dRect.height() / 2 * radius * Math.sin(Math.toRadians(-angle - 90)));
	}

	public void draw(Canvas canvas) {
		float angle; // clockwise ; 0 degrees = down

		if(mouse >= 0) angle = val * 270 + 45;
		else angle = val * 360;

		if (image.none()) {
			float littlerad = 0.1F;
			float [] vert = new float[6];
			int [] cols = new int[6];

			paint.setColor(bgcolor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawOval(dRect, paint);

			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawOval(dRect, paint);

			paint.setStyle(Paint.Style.FILL);
			paint.setColor(fgcolor);
			paint.setStrokeWidth(1);

			cols[0] = cols[1] = cols[2] = cols[3] = cols[4] = cols[5] = fgcolor;
			vert[0] = circlex(littlerad, angle - 90);
			vert[1] = circley(littlerad, angle - 90);
			vert[2] = circlex(littlerad, angle + 90);
			vert[3] = circley(littlerad, angle + 90);
			vert[4] = circlex(1F, angle);
			vert[5] = circley(1F, angle);

			canvas.drawVertices(Canvas.VertexMode.TRIANGLES, 6, vert, 0, null, 0, cols, 0, null, 0, 0, paint);

			drawLabel(canvas);

		} else {
			canvas.save();
			canvas.rotate(angle, dRect.centerX(), dRect.centerY());
			image.draw(canvas);
			canvas.restore();
		}
	}

	public void setval(float v) {
		v = Math.min(max, Math.max(min, v));
		val = (v - min) / (max - min); // normalize to [0:1]
	}

	public void sendval() {
		sendFloat((max - min) * val + min);
	}

	public float get_circular_val(float x, float y) {
		float angle = (float) Math.toDegrees(Math.atan2(-y + dRect.centerY(), -x + dRect.centerX()));

		angle = angle + 90;
		if(angle > 360) angle -= 360;
		if(angle < 0) angle += 360;

		return angle / 360;
	}

	public float dobounds(float angle)
	{
		if(mouse == 0) {
			if(angle < 0) return 0;
			else if(angle > 1) return 1;
		}
		angle -= Math.floor(angle);

		return angle;
	}

	public boolean touchdown(int pid, float x, float y)
	{
		if (dRect.contains(x, y)) {
			val0 = val;
			x0 = x;
			y0 = y;
			pid0 = pid;
			if(mouse <= 0) {
				angle0 = get_circular_val(x, y);
				if(steady == 0) val = dobounds((float) (angle0 * 1.33));
			}
			sendval();
			return true;
		}
		return false;
	}

	public boolean touchup(int pid, float x, float y)
	{
		if(pid0 == pid) {
			pid0 = -1;
			//return true;
		}
		return false;
	}

	float fract(float f)
	{
		return (float) (f - Math.floor(f));
	}

	public boolean touchmove(int pid, float x, float y)
	{
		if(pid0 == pid) {
			if (mouse > 0) {
				float d, dx = x - x0, dy = y - y0;

				if (Math.abs(dy) > Math.abs(dx)) d = -dy;
				else d = dx;

				val = Math.min(1, Math.max(0, val0 + d / mouse));
			} else {
				float angle = get_circular_val(x, y);
				if(steady == 0) val = dobounds((float) (angle * 1.33)) ;
				else {
					float dangle = fract(angle - angle0);
					if(dangle >= 0.5) dangle -= 1;
					if(dangle < -0.5) dangle += 1;
					//Log.e(TAG,"dangle="+ dangle);
					if(mouse == 0) {
						val = (float) (val0 + dangle * 1.33) ;
						val = Math.max(Math.min(val, 1), 0);
					} else {
						val = fract(val0 + dangle);
					}
					angle0 = angle;
				}
			}
			// send the result to Pd
			sendval();
			val0 = val;
			x0 = x;
			y0 = y;
			return true;
		}
		return false;
	}

	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol, args)) return;
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
		setval(v);
		sendval();
	}
}
