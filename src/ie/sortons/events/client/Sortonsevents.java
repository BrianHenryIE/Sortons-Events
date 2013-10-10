package ie.sortons.events.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class Sortonsevents implements EntryPoint {

	public void onModuleLoad() {

		System.out.println("Entrypoint");

		SimpleEventBus eventBus = new SimpleEventBus();
		ClientDAO rpcService = new ClientDAO(eventBus);
		AppController appViewer = new AppController(rpcService, eventBus);
		
		FlowPanel fp = new FlowPanel();
		RootPanel.get("gwt").add(fp);
		appViewer.go(fp);
		
	}
	
}
