package ie.sortons.events.client.view.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;


// TODO... stop writing things twice


public class ClientPageDataOverlay extends JavaScriptObject {

	protected ClientPageDataOverlay() {} 

	// TODO should this line be gone? It's mainly for datastore indexing...
	public final native String getClientPageId() /*-{ return this.clientPageId; }-*/;

	public final native FbPageOverlay getClientPage() /*-{ return this.clientPageIds; }-*/;

	public final native JsArray<FbPageOverlay> getIncludedPages() /*-{ return this.includedPages; }-*/;
	public final native JsArray<FbPageOverlay> getIgnoredPages() /*-{ return this.ignoredPages; }-*/;



}
