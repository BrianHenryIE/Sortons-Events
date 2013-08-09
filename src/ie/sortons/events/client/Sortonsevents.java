package ie.sortons.events.client;

import ie.sortons.events.client.widgets.EventWidget;
import ie.sortons.gwtfbplus.client.overlay.DataObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Sortonsevents implements EntryPoint {

	// Must be https
	//private static final String JSON_URL = "https://ucdfbevents.appspot.com/_ah/api/upcomingevents/v1/fbeventcollection/";
	
	
	private static final String JSON_URL = "http://testbed.org.org:8888/_ah/api/upcomingevents/v1/fbeventcollection/";
	
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		System.out.println("Entrypoint");

		// Nothing has been written yet.

		String url = JSON_URL + "1";
		url = URL.encode(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						displayEvents(JsonUtils.safeEval(response.getText()));
						// System.out.println(response.getText());
					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage());
		}
	}
	

	
	private void displayEvents(JavaScriptObject response){
		// { "items" : [ array of EventOverlay objects
				
		DataObject dataObject = response.cast();
		
		JsArray<EventOverlay> upcoming = dataObject.getObject("items").cast();

		FlowPanel view = new FlowPanel();
		
		RootPanel.get("gwt").add(view);
		
		for (int i = 0; i < upcoming.length(); i++){
			
			view.add(new EventWidget(upcoming.get(i)));
			
		}

			
		
	}
}
