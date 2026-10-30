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

public class FileDownloader extends AsyncTask<String, Float, String> {
	@Override
	protected String doInBackground(String... params) {
		int count;
		try {
			URL url = new URL(params[0]);
			String destinationPath = params[1];
			URLConnection conection = url.openConnection();
			conection.connect();
			// getting file length
			Float lenghtOfFile = new Float(conection.getContentLength());

			// input stream to read file - with 8k buffer
			InputStream input = new BufferedInputStream(url.openStream(), 8192);

			// Output stream to write file
			OutputStream output = new FileOutputStream(destinationPath);

			byte data[] = new byte[1024];

			long total = 0;

			while ((count = input.read(data)) != -1) {
				total += count;
				// publishing the progress....
				// After this onProgressUpdate will be called
				publishProgress(total / lenghtOfFile);

				// writing data to file
				output.write(data, 0, count);
			}

			// flushing output
			output.flush();

			// closing streams
			output.close();
			input.close();
			return destinationPath;
		} catch (Exception e) {
			Log.e("FileDownloader", e.getMessage());
		}
		return null;
	}
}

