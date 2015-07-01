package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
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

// Mobile/desktop
//http://stackoverflow.com/questions/7038108/gwt-how-to-compile-mobile-permutations

public class PageEventsPresenter implements Presenter {

	private RpcService rpcService;
	private FlowPanel container;

	public PageEventsPresenter(RpcService rpcService) {
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
		resizeCanvas.schedule(2000);

	}

	@Override
	public void go(HasWidgets container) {
		this.container = (FlowPanel) container;

		getEvents();
	}

}