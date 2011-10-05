package cx.mccormick.pddroidparty;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

import android.util.Log;
import android.graphics.Picture;

import com.larvalabs.svgandroid.SVGParser;

public class SVGRenderer {
	private static final String TAG = "SVGRenderer";
	// used for modification (e.g. interpolation) of the original svg
	SVGManipulator original = null;
	// cached image so we don't keep regenerating it every frame
	Picture cached = null;
	// my SVG filename
	String svgfile = null;
	HashMap<Integer, Picture> interpolated_cache = new HashMap<Integer, Picture>();
	// class shared static hashmap of all cached SVG images
	private static HashMap<String, Picture> cache = new HashMap<String, Picture>();
	
	public SVGRenderer(File f) {
		svgfile = f.toString();
		Log.e(TAG, "Loading: " + svgfile);
		original = new SVGManipulator(f);
		// cache it the first time
		if (!cache.containsKey(svgfile)) {
			cache.put(svgfile, SVGParser.getSVGFromString(original.toString()).getPicture());
			Log.e(TAG, "(cache store)");
		}
	}
	
	// only create an SVGRenderer if we can load the file name asked for
	public static SVGRenderer getSVGRenderer(PdDroidPatchView parent, String name) {
		// reads the SVG string from a file if the file with with name exists
		// returns null if the file with name does not exist	
		File f = new File(parent.app.getPatchFile().getParent() + "/" + name + ".svg");
		if (f.exists() && f.canRead() && f.isFile()) {
			return new SVGRenderer(f);
		}
		return null;
	}
	
	// get a rendered version of the svg
	public Picture getPicture() {
		if (cached != null) {
			return cached;
		}
		if (!cache.containsKey(svgfile)) {
			cache.put(svgfile, SVGParser.getSVGFromString(original.toString()).getPicture());
		}
		return cache.get(svgfile);
	}
	
	// proxy to SVGManipulator to get attributes of the SVG
	public String getAttribute(String s) {
		return original.getAttribute(s);
	}
	
	// interpolate between two paths in the SVG, making the second one invisible
	public SVGRenderer interpolate(String startid, String endid, double amount) {
		if (interpolated_cache.containsKey((int)(amount * 1000)) ) {
			cached = interpolated_cache.get((int)(amount * 1000));
			Log.e("SVGRenderer", "cache hit: " + ((int)(amount * 1000)));
		} else {
			original.interpolate(startid, endid, amount);
			Picture tmp = getPicture();
			interpolated_cache.put((int)(amount * 1000), tmp);
			cached = tmp;
			Log.e("SVGRenderer", "cached: " + ((int)(amount * 1000)));
		}
		return this;
	}
}
