package cx.mccormick.pddroidparty;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Canvasrect extends Widget {
	private static final String TAG = "Canvas";

	WImage image = new WImage();
	boolean isViewPort = false;
	boolean viewPortRatioEqual = false;

	public Canvasrect(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[6]) ;
		float h = Float.parseFloat(atomline[7]) ;
		initCommonArgs(app, atomline, 8, true);
		setupreceive();
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		image.load(TAG, null, receivename);

		isViewPort = receivename.startsWith("ViewPort");
		viewPortRatioEqual = isViewPort && receivename.contains("Equal");
		
		if (isViewPort) {
			parent.setDimensions((int)dRect.left, (int)dRect.top, (int)dRect.width(), (int)dRect.height(), viewPortRatioEqual);
		}
	}

	public void draw(Canvas canvas) {
		if(receivename.equals("ViewPort")) return;
		if (image.draw(canvas)) {
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(bgcolor);
			canvas.drawRect(dRect.left, dRect.top, dRect.right, dRect.bottom, paint);
			drawLabel(canvas);
		}
	}

	public void receiveMessage(String symbol, Object... args) {
		if (symbol.equals("vis_size") && args.length > 1 && args[0].getClass().equals(Float.class) && args[1].getClass().equals(Float.class)) {
			float w = Float.parseFloat(args[0].toString());
			float h = Float.parseFloat(args[1].toString());

			dRect.right = dRect.left + w;
			dRect.bottom = dRect.top + h;
		}
		else widgetreceiveSymbol(symbol, args);

		if(isViewPort) {
			if (symbol.equals("vis_size") || symbol.equals("pos") ) {
				parent.setDimensions((int)dRect.left, (int)dRect.top, (int)dRect.width(), (int)dRect.height(), viewPortRatioEqual);
			}
		}
	}
}
