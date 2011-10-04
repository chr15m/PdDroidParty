package cx.mccormick.pddroidparty;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PdDroidPatchView extends View implements OnTouchListener {
	private static final String TAG = "PdDroidPatchView";
	
	Paint paint = new Paint();
	public int patchwidth;
	public int patchheight;
	public int fontsize;
	ArrayList<Widget> widgets = new ArrayList<Widget>();
	public PdDroidParty app;
	private Picture background = null;
	
	public PdDroidPatchView(Context context, PdDroidParty parent) {
		super(context);
		
		app = parent;
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		this.setOnTouchListener(this);
		
		// default background color settings
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		
		// load the background image
		SVGRenderer renderer = SVGRenderer.getSVGRenderer(this, "background");
		if (renderer != null) {
			background = renderer.getPicture();
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawPaint(paint);
		if (background != null) {
			canvas.save();
			canvas.scale((float)this.getWidth() / background.getWidth(), (float)this.getHeight() / background.getHeight());
			canvas.drawPicture(background);
			canvas.restore();
		}
		// draw all widgets
		if (widgets != null) {
			for (Widget widget: widgets) {
				widget.draw(canvas);
			}
		}
	}
	
	public boolean onTouch(View view, MotionEvent event) {
		// if(event.getAction() != MotionEvent.ACTION_DOWN)
		// return super.onTouchEvent(event);
		if (widgets != null) {
			for (Widget widget: widgets) {
				widget.touch(event);
			}
		}
		invalidate();
		//Log.d(TAG, "touch: " + event.getX() + " " + event.getY());
		return true;
	}
	
	/** Lets us invalidate this view from the audio thread */
	public void threadSafeInvalidate() {
		final PdDroidPatchView me = this;
		app.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				me.invalidate();
			}
		});
	}
	
	/** Main patch is done loading, now we should change the background from the splash. **/
	public void loaded() {
		
	}
	
	/** build a user interface using the lines of atoms found in the patch by the pd file parser */
	public void buildUI(PdParser p, ArrayList<String[]> atomlines) {
		//ArrayList<String> canvases = new ArrayList<String>();
		int level = 0;
		
		for (String[] line: atomlines) {
			if (line.length >= 4) {
				// find canvas begin and end lines
				if (line[1].equals("canvas")) {
					/*if (canvases.length == 0) {
						canvases.add(0, "self");
					} else {
						canvases.add(0, line[6]);
					}*/
					level += 1;
					if (level == 1) {
						patchwidth = Integer.parseInt(line[4]);
						patchheight = Integer.parseInt(line[5]);
						fontsize = Integer.parseInt(line[6]);
					}
				} else if (line[1].equals("restore")) {
					//canvases.remove(0);
					level -= 1;
				// find different types of UI element in the top level patch
				} else if (level == 1) {
					if (line.length >= 2) {
						// builtin pd things
						if (line[1].equals("text")) {
							widgets.add(new Comment(this, line));
						} else if (line[1].equals("floatatom")) {
							widgets.add(new Numberbox(this, line));
						} else if (line.length >= 5) {
							// pd objects
							if (line[4].equals("vsl")) {
								widgets.add(new Slider(this, line, false));
							} else if (line[4].equals("hsl")) {
								widgets.add(new Slider(this, line, true));
							} else if (line[4].equals("tgl")) {
								widgets.add(new Toggle(this, line));
							} else if (line[4].equals("bng")) {
								widgets.add(new Bang(this, line));
							} else if (line[4].equals("nbx")) {
								widgets.add(new Numberbox2(this, line));
							} else if (line[4].equals("cnv")) {
								widgets.add(new Canvasrect(this, line));
							// special PdDroidParty abstractions
							} else if (line[4].equals("wordbutton")) {
								widgets.add(new Wordbutton(this, line));
							} else if (line[4].equals("numberbox")) {
								widgets.add(new Numberboxfixed(this, line));
							} else if (line[4].equals("taplist")) {
								widgets.add(new Taplist(this, line));
							} else if (line[4].equals("touch")) {
								widgets.add(new Touch(this, line));
							// things that aren't widgets
							} else if (line[4].equals("menubang")) {
								new MenuBang(this, line);
							} else if (line[4].equals("loadsave")) {
								new LoadSave(this, line);
							}
						}
					}
				}
			}
		}
		threadSafeInvalidate();
	}
}
