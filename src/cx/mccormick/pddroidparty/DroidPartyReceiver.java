package cx.mccormick.pddroidparty;

import java.util.ArrayList;

import org.puredata.core.PdListener;

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
	
	public final PdListener listener = new PdListener.Adapter() {
		@Override public void receiveList(String source, Object... args) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveList(args);
					widget.receiveAny();
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveMessage(String source, String symbol, Object... args) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveMessage(symbol, args);
					widget.receiveAny();
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveSymbol(String source, String symbol) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveSymbol(symbol);
					widget.receiveAny();
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveFloat(String source, float x) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveFloat(x);
					widget.receiveAny();
				}
			}
			patchview.threadSafeInvalidate();
		}
		
		@Override public void receiveBang(String source) {
			if (widgets != null) {
				for (Widget widget: widgets) {
					widget.receiveBang();
					widget.receiveAny();
				}
			}
			patchview.threadSafeInvalidate();
		}
	};
}
