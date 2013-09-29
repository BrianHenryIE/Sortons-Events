package ie.sortons.events.client.view.overlay;

import com.google.gwt.core.client.JavaScriptObject;


// TODO... stop writing things twice


public class FbPageOverlay extends JavaScriptObject {
	protected FbPageOverlay() {}

	
	public final native String getPageId() /*-{ return this.pageId; }-*/;
	public final native String getName() /*-{ return this.name; }-*/;
	public final native String getPageUrl() /*-{ return this.pageUrl; }-*/;
	
}


