package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.client.view.widgets.EventWidget;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.gwtfbplus.client.api.Canvas;

import java.util.List;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;

// Mobile/desktop
//http://stackoverflow.com/questions/7038108/gwt-how-to-compile-mobile-permutations

public class PageEventsPresenter implements Presenter {

	private RpcService rpcService;
	private FlowPanel container;

	public PageEventsPresenter(RpcService rpcService) {
		this.rpcService = rpcService;
	}

	private void getEvents() {
		rpcService.getEventsForPage(new AsyncCallback<List<DiscoveredEvent>>(){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(List<DiscoveredEvent> result) {
				displayEvents(result);
			}});
		
		
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
