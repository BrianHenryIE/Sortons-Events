package ie.sortons.events.client.presenter;

import ie.sortons.events.client.ClientDAO;
import ie.sortons.events.client.view.widgets.EventWidget;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.DiscoveredEventsResponse;
import ie.sortons.gwtfbplus.client.api.Canvas;

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class PageEventsPresenter implements Presenter {

	private ClientDAO rpcService;
	private FlowPanel container;

	public PageEventsPresenter(ClientDAO rpcService) {
		this.rpcService = rpcService;
	}

	private void getEvents() {
		rpcService.getEventsForPage(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					Serializer serializer = (Serializer) GWT.create(Serializer.class);
					DiscoveredEventsResponse deResponse = (DiscoveredEventsResponse) serializer.deSerialize(response.getText(),
							"ie.sortons.events.shared.DiscoveredEventsResponse");

					displayEvents(deResponse.getData());
				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") PageEventsPresenter.getEvents");
					// System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
					// System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
				}
			}
		});
	}

	private void displayEvents(List<DiscoveredEvent> upcomingEvents) {

		for (DiscoveredEvent event : upcomingEvents) {
			container.add(new EventWidget(event));
		}

		// Update the scrollbars
		Timer resizeCanvas = new Timer() {
			@Override
			public void run() {
				Canvas.setSize();
			}
		};

		// Schedule the timer to run once in 1 second.
		resizeCanvas.schedule(1500);
		resizeCanvas.schedule(3000);

	}

	@Override
	public void go(HasWidgets container) {
		this.container = (FlowPanel) container;

		getEvents();
	}

}
