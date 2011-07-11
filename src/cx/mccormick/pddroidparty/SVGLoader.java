package cx.mccormick.pddroidparty;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;

import android.util.Log;
import android.graphics.Picture;

import com.larvalabs.svgandroid.SVGParser;

public class SVGLoader {
	public static Picture getPicture(PdDroidPatchView parent, String name) {
		File f = new File(parent.app.getPatchFile().getParent() + "/" + name + ".svg");
		if (f.exists() && f.canRead() && f.isFile()) {
			StringBuilder svgfile = new StringBuilder();
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = br.readLine()) != null) {
					svgfile.append(line);
					svgfile.append('\n');
				}
				return SVGParser.getSVGFromString(svgfile.toString()).getPicture();
			} catch (IOException e) {
				Log.e("PdDroidParty.Widget.getPicture", e.toString());
			}
		}
		return null;
	}
}
