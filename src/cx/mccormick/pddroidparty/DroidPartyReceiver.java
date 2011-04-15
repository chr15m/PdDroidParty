package cx.mccormick.pddroidparty;

import org.puredata.core.utils.PdListener;

import java.util.ArrayList;

public class DroidPartyReceiver {
	ArrayList<Widget> widgets = new ArrayList<Widget>();
	PdDroidPatchView patchview = null;
	
	public DroidPartyReceiver(PdDroidPatchView view, Widget w) {
		addWidget(w);
		patchview = view;
	}
	
	public void addWidget(Widget w) {
		widgets.add(w);
	}
	
	public final PdListener listener = new PdListener() {
		@Override
		public void receiveList(Object... args) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveList(args);
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		// the remaining methods will never be called
		@Override
		public void receiveMessage(String symbol, Object... args) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveMessage(symbol, args);
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveSymbol(String symbol) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveSymbol(symbol);
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveFloat(float x) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveFloat(x);
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveBang() {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveBang();
				}
			}
			patchview.threadSafeInvalidate();
		}
	};
}
