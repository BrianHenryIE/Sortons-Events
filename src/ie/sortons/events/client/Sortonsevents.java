package ie.sortons.events.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RootPanel;

public class Sortonsevents implements EntryPoint {

	public void onModuleLoad() {

		System.out.println("Entrypoint");

		SimpleEventBus eventBus = new SimpleEventBus();
		ClientModel rpcService = new ClientModel(eventBus);
		AppController appViewer = new AppController(rpcService, eventBus);
		appViewer.go(RootPanel.get("gwt"));
		
	}
	
}
