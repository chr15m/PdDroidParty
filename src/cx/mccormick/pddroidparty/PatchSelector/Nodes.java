package cx.mccormick.pddroidparty.PatchSelector;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import java.nio.file.Files;
import java.nio.file.Paths;

import cx.mccormick.pddroidparty.PatchSelector.Links.*;

public class Nodes {
	static final String TAG = "Catalog";

	public static abstract class Node {
		public String name;
		public Group parent;
		public File file = null;
		public Link link = null;

		public String getPath(String separator) {
			if(parent == null) return name;
			else return parent.getPath(separator) + separator + name;
		}

		public String getPath() {
			return getPath("/");
		}

		Node(String name, Group parent) {
			this.name = name;
			this.parent = parent;
			if(parent != null) file = new File(parent.file, name);
		}
		public abstract String prefix();
	}

	public static class Item extends Node {
		Item(String name, Group parent) {
			super(name, parent);
			Log.d(TAG, "creating item: " + name + " in group: " + parent.getPath());
		}
		
		public String prefix() {
			String p = "*  ";
			if(link != null) {
				if(file.exists()) p = "+  ";
				else p = "-  ";
			}
			return p;
		}

	}

	public static class Group extends Node {
		public static final int MAX_DEPTH = 100;
		public List<Item> items = new ArrayList<Item>();
		public List<Group> groups = new ArrayList<Group>();

		Group(String name, Group parent) {
			super(name, parent);
			if(parent == null) Log.d(TAG, "creating root group");
			else Log.d(TAG, "creating group: " + name + " in group: " + parent.getPath());
		}

		@Override
		public String prefix() {
			if(link != null && link.fromLinksTxt == true) {
				return "=> ";
			}
			return "-> ";
		}

		public boolean containsItems() {
			if(link != null && ! link.scanned) return true;
			if(items.size() != 0) return true;
			else for(Group n: groups) {
				if(n.containsItems()) return true;
			}
			return false;
		}

		public int depth() {
			int i = 0;
			Group g = this;
			while(g.parent != null) {
				g = g.parent;
				i++;
			}
			return i;
		}

		public Group getRoot() {
			Group g = this;
			while(g.parent != null) g = g.parent;
			return g;
		}

		private static boolean isPatchDir(File dir) {
			File[] files = dir.listFiles();
			for (File f: files) {
				if(f.getName().equals("droidparty_main.pd")) return true;
			}
			return false;
		}

		public Item find(String name) {
			for(Item n: items) {
				if(name.equals(n.name)) return n;
			}
			return null;
		}

		public Item get(String name) {
			Item n = find(name);
			if(n == null) {
				n = new Item(name, this);
				items.add(n);
				getRoot().refresh();
			}
			return n;
		}

		public Group findGroup(String name) {
			for(Group n: groups) {
				if(name.equals(n.name)) return n;
			}
			return null;
		}

		public Group getGroup(String name) {
			if(depth() >= Group.MAX_DEPTH) return null;
			Group n = findGroup(name);
			if(n == null) {
				n = new Group(name, this);
				groups.add(n);
				getRoot().refresh();
			}
			return n;
		}

		private void processLinksFile(File f) {
			Log.d(TAG, "in " + getPath() + "/, found links.txt, parsing it...");
			try {
				List<String> lines = Files.readAllLines(Paths.get(f.getPath()));
				for(String line: lines) {
					Links.processLinkLine(line, this);
				}
			} catch(Exception e) {}
		}

		public void scan(boolean recursive) {
			if(link != null && ! recursive) link.scan(this);
			if(!file.isDirectory()) return;
			File[] files = file.listFiles();
			for (File f: files) {
				if(f.isDirectory()) {
					if(isPatchDir(f)) {
						Item n = get(f.getName());
					} else {
						Group n = getGroup(f.getName());
						if(n != null && recursive) n.scan(true);
					}
				} else if(f.getName().equals("links.txt")) {
					processLinksFile(f);
				}
			}
		}

		public String print(String prefix) {
			String s = "";
			for(Item n: items) {
				s += prefix + n.prefix() + n.name + "\n";
			}
			for(Group n: groups) {
				s += prefix + n.prefix() + n.name + "\n";
				s += n.print(prefix + "    ");
			}
			return s;
		}

		public void refresh() {}
	}
}
