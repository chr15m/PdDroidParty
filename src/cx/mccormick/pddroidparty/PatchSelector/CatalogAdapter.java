package cx.mccormick.pddroidparty.PatchSelector;

import java.util.List;

import android.util.Log;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.os.Handler;

import cx.mccormick.pddroidparty.R;
import cx.mccormick.pddroidparty.PatchSelector.Nodes.*;

class CatalogAdapter extends ArrayAdapter<Node> {

	private int resourceLayout;
	private Context mContext;
	public boolean firstIsBackNode = false;

	public CatalogAdapter(Context context, int resource, List<Node> items) {
		super(context, resource, items);
		this.resourceLayout = resource;
		this.mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		boolean isBackNode = (position == 0 && firstIsBackNode);
		if(convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
		}
		Node n = getItem(position);

		TextView textView = (TextView) convertView.findViewById(R.id.name);
		if (textView != null) {
			textView.setText(n.name);
		}
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
		if (imageView != null) {
			if(isBackNode) {
				imageView.setImageResource(R.drawable.ic_menu_back);
			} else {
				if(n instanceof Group) {
					if(n.link != null && n.link.fromLinksTxt == true)
						imageView.setImageResource(R.drawable.ic_menu_archive_online);
					else imageView.setImageResource(R.drawable.ic_menu_archive);
				}
				else if(! n.file.exists()) imageView.setImageResource(R.drawable.download);
				else imageView.setImageResource(R.drawable.icon);
			}
		}
		TextView pathText = (TextView) convertView.findViewById(R.id.path);
		if(pathText != null) {
			if(isBackNode && n.parent != null) {
				pathText.setText(n.parent.getPath() + "/");
				pathText.setVisibility(View.VISIBLE);
			} else pathText.setVisibility(View.GONE);
		}
		return convertView;
	}

	void notify(boolean fibn) {
		firstIsBackNode = fibn;
		notifyDataSetChanged();
	}
}


