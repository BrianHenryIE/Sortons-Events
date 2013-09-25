package ie.sortons.events.client;

import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.client.view.AdminView;
import ie.sortons.events.client.view.overlay.FbEventOverlay;
import ie.sortons.events.client.view.widgets.EventWidget;
import ie.sortons.events.shared.FbConfig;
import ie.sortons.gwtfbplus.client.api.Canvas;
import ie.sortons.gwtfbplus.client.newresources.Resources;
import ie.sortons.gwtfbplus.client.overlay.DataObject;
import ie.sortons.gwtfbplus.client.overlay.SignedRequest;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtfb.sdk.FBCore;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Sortonsevents implements EntryPoint {
	
	
															
	// Courtesy of gwtfb.com
	private FBCore fbCore = GWT.create(FBCore.class);

	public String APPID = FbConfig.getAppID();	
	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;
	

	// TODO : Gin
	// "Create the object graph - a real application would use Gin"
    final SimpleEventBus eventBus = new SimpleEventBus();
    
    
	// We'll add this to the rootpanel
	// TODO A panel that autogrows (and fires an event for never ending scrolling) and which
	// has the default styles aplied would be nice.
	FlowPanel view = new FlowPanel();

	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		System.out.println("Entrypoint");

		SimpleEventBus eventBus = new SimpleEventBus();
		Model rpcService = new Model();
		
		// Nothing has been written yet.
				
		// Inject the GwtFB+ stylesheet which cascades Facebook styles through the document. 
		GWT.<Resources>create(Resources.class).newCss().ensureInjected();
		 
		// Initialize the Facebook API
		fbCore.init(APPID, status, cookie, xfbml);

		RootPanel.get("gwt").add(view);
		
		
		// Where are we?
		if (SignedRequest.parseSignedRequest() == null) {
			// Looks like we're operating outside Facebook
		} else if (SignedRequest.parseSignedRequest().getPage() == null) {
			// Are we inside Facebook with no Page ID? Then we're the app... 
			
			// Show friends events!
			
			// TODO The dev server is caching the signedrequest variable and polluting the output for GETs. Will this happen in production?
		} else if (SignedRequest.parseSignedRequest().getPage() != null) {
			// We're inside a Page tab
			System.out.println("Page ID: " + SignedRequest.parseSignedRequest().getPage().getId());



			if (SignedRequest.parseSignedRequest().getPage().getAdmin() == true) {
				// We're the page admin
				//TODO some sort of security!
				
				// Check we're logged in
				// Show the login button
				
				// Show the admin panel
				AdminPresenter adminPresenter = new AdminPresenter(eventBus, rpcService, new AdminView());
				SimplePanel adminPanel = new SimplePanel();
			    adminPresenter.go(adminPanel);

				view.add(adminPanel);
			}
			
			
			rpcService.getEventsForPage(new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {

						displayEvents(JsonUtils.safeEval(response.getText()));
						//System.out.println(response.getText());
					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
					}
				}
			});
			
			
		}

		
		
		
		
		
		
	}
	

	
	private void displayEvents(JavaScriptObject response){
		// { "items" : [ array of EventOverlay objects
				
		DataObject dataObject = response.cast();
		
		JsArray<FbEventOverlay> upcoming = dataObject.getObject("items").cast();
		
		for (int i = 0; i < upcoming.length(); i++){
			
			view.add(new EventWidget(upcoming.get(i)));
			
		}

		// Update the scrollbars
		Timer t = new Timer() {
			@Override
			public void run() {
				Canvas.setSize();
			}
		};

		// Schedule the timer to run once in 1 second.
		t.schedule(1500);

	}
}
