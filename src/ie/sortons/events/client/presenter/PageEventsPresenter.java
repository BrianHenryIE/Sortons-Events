package ie.sortons.events.client.presenter;

import ie.sortons.events.client.ClientModel;
import ie.sortons.events.client.view.overlay.FbEventOverlay;
import ie.sortons.events.client.view.widgets.EventWidget;
import ie.sortons.gwtfbplus.client.api.Canvas;
import ie.sortons.gwtfbplus.client.overlay.DataObject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasWidgets;

public class PageEventsPresenter implements Presenter {

	private ClientModel rpcService;
	private HasWidgets container;
	

	public void bind(){

		
	}


	public PageEventsPresenter(ClientModel rpcService) {
		this.rpcService = rpcService;
	}


	private void getEvents(){
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


	private void displayEvents(JavaScriptObject response){
		// { "items" : [ array of EventOverlay objects

		DataObject dataObject = response.cast();

		JsArray<FbEventOverlay> upcoming = dataObject.getObject("items").cast();

		for (int i = 0; i < upcoming.length(); i++){

			container.add(new EventWidget(upcoming.get(i)));

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


	@Override
	public void go(HasWidgets container) {
		this.container = container;

		getEvents();
	}
	
}
