package cx.mccormick.pddroidparty;

import android.util.Log;

import org.puredata.core.PdBase;

public class LoadSave extends Widget {
	private static final String TAG = "LoadSave";
	private PdDroidPatchView parent = null;
	private String filename = "";
	private String directory = ".";
	private String extension = "";
	private String sendreceive = null;
	
	public LoadSave(PdDroidPatchView app, String[] atomline) {
		super(app);
		parent = app;
		sendreceive = atomline[5];
		parent.app.registerReceiver(sendreceive, this);
	}
	
	public void receiveList(Object... args) {
		Log.e(TAG, args[0] + " " + args[1] + " " + args[2]);
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public void receiveMessage(String symbol, Object... args) {
		int type = 0;
		if (symbol.equals("save")) {
			type = PdDroidParty.DIALOG_SAVE;
		}
		directory = args.length > 0 ? (String)args[0] : ".";
		extension = args.length > 1 ? (String)args[1] : "";
		parent.app.launchDialog(this, type);
	}
	
	public void gotFilename(String type, String newname) {
		filename = newname;
		String root = parent.app.getPatchFile().getParent();
		if (directory.equals(".")) {
			directory = root;
		} else if (directory.charAt(0) != '/') {
			directory = root + "/" + directory;
		}
		PdBase.sendSymbol(sendreceive + "-" + type, directory + "/" + filename + "." + extension);
	}
}
