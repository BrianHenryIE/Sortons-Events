package ie.sortons.events.client.view.overlay;

import com.google.gwt.core.client.JavaScriptObject;


// TODO... stop writing things twice


public class FbPageOverlay extends JavaScriptObject {
	protected FbPageOverlay() {}

	public final native String getId() /*-{ return this.id; }-*/;
	public final native String getName() /*-{ return this.name; }-*/;
	public final native String getLink() /*-{ return this.link; }-*/;
}


