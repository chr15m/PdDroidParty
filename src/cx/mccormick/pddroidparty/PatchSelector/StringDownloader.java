package cx.mccormick.pddroidparty.PatchSelector;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;
import android.os.AsyncTask;

public class StringDownloader extends AsyncTask<URL, Float, String> {
	@Override
	protected String doInBackground(URL... params) {
		int count;
		try {
			URL url = params[0];
			URLConnection conection = url.openConnection();
			conection.connect();
			Float lenghtOfFile = new Float(conection.getContentLength());

			InputStream input = url.openStream();
			StringBuilder output = new StringBuilder();
			byte data[] = new byte[8 * 1024];
			long total = 0;
			while ((count = input.read(data)) != -1) {
				total += count;
				publishProgress(total / lenghtOfFile); // onProgressUpdate(0.0-1.0)
				output.append(new String(data, 0, count));
			}
			return output.toString();
		} catch (Exception e) {
			Log.e("JsonDownloader", e.getMessage());
		}
		return null;
	}
}

