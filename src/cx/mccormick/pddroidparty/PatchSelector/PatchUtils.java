package cx.mccormick.pddroidparty.PatchSelector;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;
import android.app.ProgressDialog;
import android.app.Activity;
import android.os.AsyncTask;

import org.puredata.core.utils.IoUtils;

public class PatchUtils {
	private static final String TAG = "PatchUtils";

	public static String getPatchVersion(String patchPath) {
		List<File> listpdMain = IoUtils.find(new File(patchPath), ".*droidparty_main\\.pd$");
		String version = null;
		if (listpdMain.size() != 0) {
			for (File f : listpdMain) {
				Log.d(TAG, "disk file:" + f.getAbsolutePath());
				try {
					InputStream is = new FileInputStream(f);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.contains(" version: ")) {
							Log.d(TAG, "VersionLine:" + line);
							version = line.substring(line.lastIndexOf(":") + 1, line.length() - 1);
							break;
						}
					}
					if(version == null) version = "0";
					reader.close();
					Log.d(TAG, "disk version: " + version);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return version;
	}

	public static File getMainPatchPath(String patchDir) {
		List<File> listMain = IoUtils.find(new File(patchDir), ".*droidparty_main\\.pd$");
		if (listMain.size() != 0) return listMain.get(0);
		else return null;
	}

	public static String extractZipTo(String zipPath, String destinationPath) {
		try {
			List<File> listMain = IoUtils.extractZipResource(new FileInputStream(zipPath), new File(destinationPath), true);
			if (listMain.size() != 0) {
				for (File f : listMain) {
					if(f.isDirectory()) return f.getName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	public static class DownloadFileFromURL extends AsyncTask<String, String, String> {
		private ProgressDialog progressDialog;
		private Activity activity;
		private String destinationPath;

		DownloadFileFromURL(Activity act, String dest) {
			activity = act;
			destinationPath = dest;
		}
		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage("Downloading file. Please wait...");
			progressDialog.setIndeterminate(false);
			progressDialog.setMax(100);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				int lenghtOfFile = conection.getContentLength();

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
					publishProgress("" + (int)((total * 100) / lenghtOfFile));

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
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			progressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}
