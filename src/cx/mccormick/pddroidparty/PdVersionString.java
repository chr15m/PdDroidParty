package cx.mccormick.pddroidparty;

import org.puredata.core.PdBase;
import java.lang.reflect.Method;
import android.util.Log;

public class PdVersionString {

	private static String versionString = null;

	public static String get() {
		if(versionString == null) {
			versionString = "0.53.1"; // fallback value
			Class PdBaseClass = PdBase.class;
			try {
				Method m = PdBaseClass.getMethod("versionString");
				versionString = String.valueOf(m.invoke(null));
			} catch(Exception e) {
				Log.d("PdVersionString", "impossible to get PdBase.versionString(): " + e.toString());
			}
		}
		return versionString;
	}
}
