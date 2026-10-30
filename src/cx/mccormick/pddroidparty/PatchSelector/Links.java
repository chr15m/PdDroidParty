package cx.mccormick.pddroidparty.PatchSelector;

import android.util.Log;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONArray;

import cx.mccormick.pddroidparty.PatchSelector.Nodes.*;

public class Links {
	static final String TAG = "Catalog";

	public static class Link {
		public String name;
		public URL url = null;
		boolean fromLinksTxt = false;
		boolean scanned = false;
		Link(String name, URL url) {
			this.name = name;
			this.url = url;
		}
		public void scan(Group group) {}
	}

	public static class PdzLink extends Link {
		PdzLink(String name, URL url) {
			super(name, url);
		}
	}

	public static class LinkLink extends Link {
		LinkLink(String name, URL url) {
			super(name, url);
		}

		@Override
		public void scan(Group group) {
			if(scanned) return;
			scanned = true;
			processLinksFileUrl(url, group);
		}
	}

	public static class GhLink extends Link {
		GhLink(String name, URL url) {
			super(name, url);
		}

		private void parseJSONArray(JSONArray json, Group group) {
			for(int i = 0; i < json.length(); i++) {
				try {
						JSONObject object = json.getJSONObject(i);
						String objType = object.optString("type", "?");
						String objName = object.getString("name");
						if(objType.equals("dir")) {
							Group n = group.getGroup(objName);
							if(n == null) continue;
							n.link = new GhLink(name, new URL(object.getString("url")));
						} else {
							String downloadUrl = object.optString("download_url", object.optString("browser_download_url", null));
							if(downloadUrl == null) continue;
							URL objUrl = new URL(downloadUrl);
							if(objName.endsWith(".pdz")) {
								String patchName = objName.substring(0, objName.length() - 4);
								Item n = group.get(patchName);
								n.link = new PdzLink(patchName, objUrl);
							} else if(objName.equals("links.txt")) {
								processLinksFileUrl(objUrl, group);
							}
						}
					} catch(Exception e) {
					Log.d(TAG, "GhLink.parseJSONArray() exception: " + e.toString());
				}
			}
		}

		@Override
		public void scan(Group group) {
			if(scanned) return;
			scanned = true;
			new StringDownloader(){
				@Override
				protected void onPostExecute(String downloadedString) {
					Log.d(TAG, "json of link " + group.getPath() + " downloaded, parsing it.");
					try {
						JSONArray json = new JSONArray(downloadedString);
						parseJSONArray(json, group);
					} catch(Exception e) {}
					try {
						JSONObject json = new JSONObject(downloadedString);
						JSONArray assetsJson = json.optJSONArray("assets");
						if(assetsJson != null) {
							//Log.d(TAG, "GhLink.scan() got assets");
							parseJSONArray(assetsJson, group);
						}
						String body = json.optString("body", null);
						if(body != null) {
							//Log.d(TAG, "GhLink.scan() got body: " + body);
							processLinksString(body, group);
						}
					} catch(Exception e) {}
				}
			}.execute(url);
		}
	}

	private static Link buildLink(String linksLine) {
		String[] words = linksLine.split(" ");
		if(words.length < 3) return null;
		try {
			URL url = new URL(words[2]);
			if(words[0].equals("gh")) return new GhLink(words[1], url);
			else if(words[0].equals("pdz")) return new PdzLink(words[1], url);
			else if(words[0].equals("links")) return new LinkLink(words[1], url);
		} catch(Exception e) { return null;}
		return null;
	}

	public static void processLinkLine(String linksLine, Group parent) {
		Link link = buildLink(linksLine);
		if(link != null) {
			link.fromLinksTxt = true;
			Node n;
			if(link instanceof PdzLink) n = parent.get(link.name);
			else n = parent.getGroup(link.name);
			if(n != null && n.link == null) {
				n.link = link;
				parent.getRoot().refresh();
				String linktype = "none";
				if(link instanceof PdzLink) linktype = "pdz";
				else if(link instanceof GhLink) linktype = "gh";
				else if(link instanceof LinkLink) linktype = "link";
				Log.d(TAG, "add '" + linktype + "' link: '" + link.name + "' to " + parent.getPath());
			}
		}
	}

	private static void processLinksString(String linksString, Group group) {
		if(linksString == null || group == null) return;
		String[] lines = linksString.split("[\\r\\n]+");
		int firstLine = 0;
		int lastLine = lines.length - 1;
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].startsWith("# LINKS")) {
				firstLine = i + 1;
			}
			else if(lines[i].startsWith("# ENDLINKS")) {
				lastLine = i - 1;
			}
		}
		if(firstLine < lines.length && lastLine >= 0 && firstLine <= lastLine)
			for(int i = firstLine; i <= lastLine; i++) {
				processLinkLine(lines[i], group);
			}
	}

	private static void processLinksFileUrl(URL url, Group group) {
		new StringDownloader(){
			@Override
			protected void onPostExecute(String downloadedString) {
				processLinksString(downloadedString, group);
			}
		}.execute(url);
	}
}
