package cx.mccormick.pddroidparty;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.content.res.AssetManager;
import android.app.Activity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.app.AlertDialog;
import android.widget.TextView;

import org.puredata.core.PdBase;

public class About {
	public static void open(Activity activity) {
		// load in the about dialog contents from assets/about.html
		StringBuffer sb = new StringBuffer();
		try {
			AssetManager assets = activity.getAssets();
			InputStreamReader reader = new InputStreamReader(assets.open("about.html"), "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line = br.readLine();
			while(line != null) {
				sb.append(line + "\n");
				line = br.readLine();
			}
			sb.append("Pure Data version: " + PdBase.versionString());
		} catch (IOException e) {
			sb.append("Copyright Chris McCormick, 2011");
		}

		// convert the string to HTML for the about dialog
		final SpannableString s = new SpannableString(Html.fromHtml(sb.toString()));
		Linkify.addLinks(s, Linkify.ALL);

		AlertDialog ab = new AlertDialog.Builder(activity)
		.setTitle("About")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(s)
		.setPositiveButton("ok", null)
		.create();
		ab.show();
		// make the links clickable
		((TextView)ab.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
