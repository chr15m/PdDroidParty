package cx.mccormick.pddroidparty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.puredata.core.PdBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

public class MenuBang {
	private static final String TAG = "Widget";

	// list of all known menubangs so we can find them when we are told a menu item has been pressed
	private static Map<MenuItem, MenuBang> all = new HashMap<MenuItem, MenuBang>();
	// have to use name->this store because the menu is not available when our list if first built
	private static Map<String, MenuBang> names = new HashMap<String, MenuBang>();

	private PdDroidPatchView parent = null;
	private String name = null;
	private String sendname = null;
	private BitmapDrawable icon = null;

	public MenuBang(PdDroidPatchView app, String[] atomline) {
		parent = app;
		name = atomline[5];
		sendname = "menubang-" + name;
		// find an icon for us
		File f = new File(parent.app.getPatchFile().getParent() + "/menubang-" + name + ".png");
		if (f.exists() && f.canRead() && f.isFile()) {
			//    36x36 for low-density
			//    48x48 for medium-density
			//    72x72 for high-density
			//    96x96 for extra high-density
			Bitmap b = BitmapFactory.decodeFile(f.toString());
			// set the density according to the size of bitmap they have used
			if (b.getWidth() <= 36) {
				b.setDensity(DisplayMetrics.DENSITY_LOW);
			} else if (b.getWidth() <= 48) {
				b.setDensity(DisplayMetrics.DENSITY_MEDIUM);
			} else {
				b.setDensity(DisplayMetrics.DENSITY_HIGH);
			}
			//} else {
			//	b.setDensity(DisplayMetrics.DENSITY_XHIGH);
			//}
			icon = new BitmapDrawable(parent.app.getResources(), b);
		}
		// remember a list of MenuBangs
		names.put(name, this);
	}

	public BitmapDrawable getIcon() {
		return icon;
	}

	public void send() {
		PdBase.sendBang(sendname);
	}

	// forget all menubangs we have created and start afresh
	public static void clear() {
		all.clear();
		names.clear();
	}

	// called as a classmethod to add all MenuBangs to the actual menu
	public static void setMenu(Menu menu) {
		for(String key : names.keySet()) {
			MenuItem i = menu.add(0, Menu.FIRST + menu.size(), 0, key.replace("_", " "));
			MenuBang mb = names.get(key);
			// if there is a bitmap, use it
			if (mb.getIcon() != null) {
				i.setIcon(mb.getIcon());
			}
			// add this to our list of all known MenuBangs
			all.put(i, mb);
		}
	}

	// called as a classmethod to find the specific MenuBang which was pressed
	public static void hit(MenuItem which) {
		MenuBang m = all.get(which);
		m.send();
	}
}
