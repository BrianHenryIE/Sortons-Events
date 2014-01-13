package ie.sortons.events.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JpsArray extends JavaScriptObject {

	protected JpsArray() {
	}

	public final native JavaScriptObject index() /*-{
		return this[0];
	}-*/;

}
