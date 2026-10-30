package cx.mccormick.pddroidparty.PatchSelector;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;
import android.content.Context;
import android.view.View;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import cx.mccormick.pddroidparty.R;
import cx.mccormick.pddroidparty.PatchSelector.Nodes.*;

public class CatalogView {
	private static final String TAG = "CatalogView";
	private Group root;
	private Group current;
	private final Context context;
	private final ListView view;
	private final List<Node> displayedList = new ArrayList<Node>();
	private final CatalogAdapter adapter;
	private final int REFRESH_DELAY_MS = 200;
	private final String rootPath;

	CatalogView(AppCompatActivity activity, String rootPath) {
		this.rootPath = rootPath;
		context = activity;

		view = (ListView) activity.findViewById(R.id.patch_selector);
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				Node n = displayedList.get(position);
				if(n == null) return;
				// return to parent directory
				if(n == current) {
					gotoNode(current.parent);
					return;
				}
				// enter directory
				if(n instanceof Group) {
					gotoNode((Group)n);
					return;
				}
				else {
					// run or install
					if(n.file.exists()) {
						runPatchDir(n.file.getPath());
					} else {
						if(n.link != null && n.parent != null) {
							installPdz(n.name, n.parent.getPath(), n.link.url.toString());
						}
					}
					return;
				}
			}
		});
		view.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
				Node n = displayedList.get(position);
				Log.d(TAG, "long click on: " + n.getPath());
				showMenu(n);
				return true;
			}
		});

		adapter = new CatalogAdapter(context, R.layout.catalog_item, displayedList);
		view.setAdapter(adapter);

		activity.getOnBackPressedDispatcher().addCallback(backPressedCallback);

		initRoot();
	}

	private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
		@Override
		public void handleOnBackPressed() {
			gotoNode(current.parent);
		}
	};

	public void initRoot() {
		current = root = new Group("", null) {
			@Override
			public void refresh() {
				scheduleRefresh();
			}
		};
		root.file = new File(rootPath);
		root.scan(true);
	}
	
	public void scan() {
		current.scan(false);
	}

	public void print() {
		Log.d(TAG, "catalog: \n" + root.print(""));
	}

	public void gotoNode(Group group) {
		if(group == null) return;
		Log.d(TAG, "goto: " + group.getPath());
		current = group;
		refresh();
	}

	private static void sortNodeList(List<? extends Node> list) {
		Collections.sort(list, new Comparator<Node>() {
			public int compare(Node a, Node b) {
				return a.name.toLowerCase().compareTo(b.name.toLowerCase());
			}
		});
	}

	private void refresh() {
		boolean showBackNode = (current.parent != null);
		Log.d(TAG, "refreshing " + current.getPath());
		current.scan(false);
		displayedList.clear();
		// if not root, add 'go back' item
		if(showBackNode) displayedList.add(current);
		// patches first
		sortNodeList(current.items);
		displayedList.addAll(current.items);
		// then non-empty dirs
		sortNodeList(current.groups);
		for(Group n: current.groups) {
			if(n.containsItems()) displayedList.add(n);
		}
		adapter.notify(showBackNode);
		backPressedCallback.setEnabled(showBackNode);
		if(! showBackNode) print();
	}

	private class RefreshScheduler {
		private Object refreshToken = new Object();
		private Handler handler = new Handler();
		public void refresh() {
			handler.removeCallbacksAndMessages(refreshToken);
			handler.postDelayed(() -> CatalogView.this.refresh(), refreshToken, REFRESH_DELAY_MS);
		}
	}
	private RefreshScheduler refreshScheduler = new RefreshScheduler();

	public void scheduleRefresh() {
		refreshScheduler.refresh();
	}

	private void showMenu(Node node) {
		PopupMenu popup = new PopupMenu(context, view);
		popup.getMenuInflater().inflate(R.menu.catalog_context, popup.getMenu());
		if(node instanceof Item) {
			if(node.file.exists()) {
				popup.getMenu().findItem(R.id.delete).setVisible(true);
				popup.getMenu().findItem(R.id.check_updates).setVisible(true);
			} else {
				popup.getMenu().findItem(R.id.install).setVisible(true);
			}
		}
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				if(id == R.id.install) {
					if(node instanceof Item && node.link != null) {
						Log.d(TAG, "install " + node.getPath());
						installPdz(node.name, node.parent.getPath(), node.link.url.toString());
					}
					return true;
				} else if(id == R.id.check_updates) {
					if(node instanceof Item && node.link != null) {
						Log.d(TAG, "check updates " + node.getPath());
						installPdz(node.name, node.parent.getPath(), node.link.url.toString());
					}
					return true;
				} else if(id == R.id.delete) {
					Log.d(TAG, "delete " + node.getPath());
					if(node instanceof Item && node.file.exists()) {
						PatchUtils.deleteDirectory(node.file);
						refresh();
					}
					return true;
				}
				return false;
			}
		});
		if(popup.getMenu().hasVisibleItems()) popup.show();
	}

	public void runPatchDir(String patchDir) {}
	public void installPdz(String name, String path, String url) {}
}

