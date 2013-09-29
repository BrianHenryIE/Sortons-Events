package ie.sortons.events.client.view.overlay;

import com.google.gwt.core.client.JavaScriptObject;


// TODO... stop writing things twice


public class FbGraphOverlay extends JavaScriptObject {
	protected FbGraphOverlay() {}

	
	public final native String getId() /*-{ return this.id; }-*/;
	public final native String getName() /*-{ return this.name; }-*/;
	public final native String getLink() /*-{ return this.link; }-*/;
	
}


