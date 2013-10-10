package ie.sortons.events.client;

import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.client.presenter.PageEventsPresenter;
import ie.sortons.events.client.view.AdminView;
import ie.sortons.events.shared.FbConfig;
import ie.sortons.gwtfbplus.client.newresources.Resources;
import ie.sortons.gwtfbplus.client.overlay.SignedRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtfb.sdk.FBCore;

public class AppController {


	// Courtesy of gwtfb.com
	private FBCore fbCore = GWT.create(FBCore.class);

	public String APPID = FbConfig.getAppID();	
	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;


	private ClientDAO rpcService;
	private SimpleEventBus eventBus;
	private HasWidgets container;

	
	public AppController(ClientDAO rpcService, SimpleEventBus eventBus) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		

		// Inject the GwtFB+ stylesheet which cascades Facebook styles through the document. 
		GWT.<Resources>create(Resources.class).newCss().ensureInjected();

		// Initialize the Facebook API
		fbCore.init(APPID, status, cookie, xfbml);
		
		rpcService.setGwtFb(fbCore);

	}

	
	public void go(final HasWidgets container) {
		this.container = container;


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

				container.add(adminPanel);
			}


			
			PageEventsPresenter pep = new PageEventsPresenter(rpcService);
			
			FlowPanel pepPanel = new FlowPanel(); // this was SimplePanel ... WHY? TODO!
			pep.go(pepPanel);

			container.add(pepPanel);

		}
	}

}
