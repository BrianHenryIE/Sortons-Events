package ie.sortons.events.client.view.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;


// TODO... stop writing things twice


public class ClientPageDataOverlay extends JavaScriptObject {
	
	protected ClientPageDataOverlay() {} 
	
	// TODO should this line be gone? It's mainly for datastore indexing...
	public final native String getClientPageId() /*-{ return this.clientPageId; }-*/;
	
	public final native FbPage getClientPage() /*-{ return this.clientPageIds; }-*/;

	private final native JsArray<FbPage> getIncludedPages() /*-{ return this.includedPages; }-*/;
	private final native JsArray<FbPage> getExcludedPages() /*-{ return this.excludedPages; }-*/;


	public static class FbPage extends JavaScriptObject {
		protected FbPage() {}
		
		public final native String getPageId() /*-{ return this.pageId; }-*/;
		public final native String getName() /*-{ return this.name; }-*/;
		public final native String getPageUrl() /*-{ return this.pageUrl; }-*/;
	}
    
}
