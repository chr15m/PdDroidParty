package cx.mccormick.pddroidparty;

import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Picture;
import android.util.Log;

public class Canvasrect extends Widget {
	private static final String TAG = "Canvas";
	
	SVGRenderer vis = null;
	
	private static int IEM_GUI_MAX_COLOR = 30;
	private static int iemgui_color_hex[] = {
		16579836, 10526880, 4210752, 16572640, 16572608,
		16579784, 14220504, 14220540, 14476540, 16308476,
		14737632, 8158332, 2105376, 16525352, 16559172,
		15263784, 1370132, 2684148, 3952892, 16003312,
		12369084, 6316128, 0, 9177096, 5779456,
		7874580, 2641940, 17488, 5256, 5767248
	};
	
	public Canvasrect(PdDroidPatchView app, String[] atomline) {
		super(app);
		
		float x = Float.parseFloat(atomline[2]) / parent.patchwidth * screenwidth;
		float y = Float.parseFloat(atomline[3]) / parent.patchheight * screenheight;
		float w = Float.parseFloat(atomline[6]) / parent.patchwidth * screenwidth;
		float h = Float.parseFloat(atomline[7]) / parent.patchheight * screenheight;
		
		receivename = atomline[9];
		setupreceive();
		
		// TODO: calculate and set fill colour
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
		
		int iemcolor = Integer.parseInt(atomline[15]);
		Log.e("ORIGINAL COLOR", "" + iemcolor);
		int color = 0;
		
		if(iemcolor < 0)
		{
			iemcolor = -1 - iemcolor;
			paint.setARGB(0xFF, (iemcolor & 0x3f000) >> 10, (iemcolor & 0xfc0) >> 4, iemcolor & 0x3f << 2);
		}
		else
		{
			iemcolor = iemgui_modulo_color(iemcolor);
			color = iemgui_color_hex[iemcolor] << 8 | 0xFF;
			paint.setColor(color);
		}
		
		Log.e("COLOR", "" + color);
		//paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		//paint.setStyle(Paint.Style.STROKE);
		//r.sort();
		vis = getSVG(TAG, null, receivename);
	}
	
	private int iemgui_modulo_color(int col) {
		while (col >= IEM_GUI_MAX_COLOR)
			col -= IEM_GUI_MAX_COLOR;
		while (col < 0)
			col += IEM_GUI_MAX_COLOR;
		return col;
	}
		
	public void draw(Canvas canvas) {
		if (drawPicture(canvas, vis)) {
			canvas.drawRect(dRect.left, dRect.top, dRect.right, dRect.bottom, paint); 
		}
	}
	
	public void receiveMessage(String symbol, Object... args) {
		if (symbol.equals("pos")) {
			if (args.length == 2) {
				float w = dRect.width();
				float h = dRect.height();
				//Log.e("POS", args[0].toString() + ", " + args[1].toString());
				dRect.left = Float.parseFloat(args[0].toString()) / parent.patchwidth * screenwidth;
				dRect.top = Float.parseFloat(args[1].toString()) / parent.patchwidth * screenwidth;
				dRect.right = dRect.left + w;
				dRect.bottom = dRect.top + h;
				//r.sort();
			}
		}
	}
}
