package cx.mccormick.pddroidparty;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import org.puredata.core.PdBase;

public class MenuBang {
	private static final String TAG = "Widget";
	
	// list of all known menubangs so we can find them when we are told a menu item has been pressed
	private static Map<MenuItem, MenuBang> all = new HashMap<MenuItem, MenuBang>();
	// have to use name->this store because the menu is not available when our list if first built
	private static Map<String, MenuBang> names = new HashMap<String, MenuBang>();
	
	private PdDroidPatchView parent = null;
	private String name = null;
	private String sendname = null;
	private Bitmap bitmap = null;
	
	public MenuBang(PdDroidPatchView app, String[] atomline) {
		parent = app;
		name = atomline[5];
		sendname = "menubang-" + name;
		names.put(name, this);
	}
	
	public void clear() {
		all.clear();
	}
	
	public void send() {
		PdBase.sendBang(sendname);
	}
	
	// called as a classmethod to add all MenuBangs to the actual menu
	public static void setMenu(Menu menu) {
		for(String key: names.keySet()) {
			MenuItem i = menu.add(0, Menu.FIRST + menu.size(), 0, key);
			// TODO: if there is a bitmap, use it
			// java.io.File
			// File f = new File("/blah/blah/blah");
			// f.exists() f.canRead() f.isFile()
			
			// Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
			// BitmapDrawable(Resources res, Bitmap bitmap)
			// i.setIcon(Drawable icon);
			all.put(i, names.get(key));
		}
	}
	
	// called as a classmethod to find the specific MenuBang which was pressed
	public static void hit(MenuItem which) {
		MenuBang m = all.get(which);
		m.send();
	}
}
