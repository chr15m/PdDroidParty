package cx.mccormick.pddroidparty.PatchSelector;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import cx.mccormick.pddroidparty.R;
import cx.mccormick.pddroidparty.PatchSelector.Nodes.*;

public class NodeMenu {
	private final PopupWindow popupWindow;
	
	private void setupButton(View v, int id, boolean visible) {
		Button button = (Button)v.findViewById(id);
		button.setVisibility(visible ? View.VISIBLE : View.GONE);
		if(visible) button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClicked(id);
				popupWindow.dismiss();
			}
		});
	}

	private void setupText(View v, int id, String text) {
		TextView textView = (TextView)v.findViewById(id);
		textView.setVisibility(text != null ? View.VISIBLE : View.GONE);
		if(text != null) textView.setText(text);
	}

	private static long folderSize(File directory) {
		long length = 0;
		if(directory == null | ! directory.isDirectory()) return 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	private static String formatSize(long size) {
		if(size < 1024) return "" + size + "B";
		else if(size < 1024 * 1024) return "" + (Math.round(10 * (size / 1024.0)) / 10.0) + "kB";
		else return "" + (Math.round(10 * (size / (1024.0 * 1024.0))) / 10.0) + "MB";
	}

	private static int countItems(Group group) { 
		int count = 0;
		for(Item i: group.items) if(i.file.exists()) count++;
		for(Group g: group.groups) count += countItems(g);
		return count;
	}

	public NodeMenu(Context context, View itemView, ViewGroup parentViewGroup, Node node) {
		popupWindow = new PopupWindow(context);
		View layout = new View(context).inflate(context, R.layout.node_menu, null);

		popupWindow.setContentView(layout);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

		if(node instanceof Item) {
			setupText(layout, R.id.name, node.name);
			if(node.file.exists()) {
				setupText(layout, R.id.size, formatSize(folderSize(node.file)));
				setupText(layout, R.id.patches, null);
				setupButton(layout, R.id.install, false);
				setupButton(layout, R.id.delete, true);
				setupButton(layout, R.id.check_updates, node.link != null);
			} else {
				setupText(layout, R.id.size, null);
				setupText(layout, R.id.patches, null);
				setupButton(layout, R.id.install, true);
				setupButton(layout, R.id.delete, false);
				setupButton(layout, R.id.check_updates, false);
			}
		} else {
			setupText(layout, R.id.name, node.name + "/");
			long size = folderSize(node.file);
			int itemCount = countItems((Group)node);
			setupText(layout, R.id.size, formatSize(folderSize(node.file)));
			setupText(layout, R.id.patches, itemCount + " installed " + (itemCount > 1 ? "patches" : "patch"));
			setupButton(layout, R.id.install, false);
			setupButton(layout, R.id.delete, size != 0);
			setupButton(layout, R.id.check_updates, false);
		}
		popupWindow.setFocusable(true);
		popupWindow.setTouchModal(true);
		popupWindow.showAsDropDown(itemView);
	}

	public void onClicked(int id) {}
}
