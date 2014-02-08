package ie.sortons.events.client;

import ie.sortons.events.client.appevent.ResponseErrorEvent;
import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.client.presenter.PageEventsPresenter;
import ie.sortons.events.client.presenter.RecentPostsPresenter;
import ie.sortons.events.client.view.AdminView;
import ie.sortons.events.client.view.RecentPostsView;
import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.popups.ClickPopup;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.gwtfb.sdk.FBCore;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class AppController {

	// Courtesy of gwtfb.com
	private FBCore fbCore = GWT.create(FBCore.class);

	public String APPID = Config.getAppID();
	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;

	private ClientDAO rpcService;
	private SimpleEventBus eventBus;

	private HasWidgets container;

	interface MyEventBinder extends EventBinder<AppController> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	GwtFbPlusResources resources = GwtFbPlusResources.INSTANCE;

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	public AppController(ClientDAO rpcService, SimpleEventBus eventBus) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		eventBinder.bindEventHandlers(this, eventBus);

		resources.facebookStyles().ensureInjected();
		resources.css().ensureInjected();

		// Initialize the Facebook API
		fbCore.init(APPID, status, cookie, xfbml);

		@SuppressWarnings("unused")
		LoginController lc = new LoginController(eventBus);

		rpcService.setGwtFb(fbCore);

	}

	// TODO
	public static final native JavaScriptObject getSignedRequestFromHTML() /*-{
		return $wnd._sr_data;
	}-*/;

	public void go(final HasWidgets container) {
		this.container = container;

		// TODO : this crashes if not inside facebook
		SignedRequest sr = (SignedRequest) serializer.deSerialize(new JSONObject(getSignedRequestFromHTML()),
				"ie.sortons.gwtfbplus.shared.domain.SignedRequest");

		// Where are we?
		if (sr == null) {
			// Looks like we're operating outside Facebook

		} else if (sr.getPage() == null) {
			// Are we inside Facebook with no Page ID? Then we're the app...

			// Show friends events!

			// TODO The dev server is caching the signedrequest variable and polluting the output for GETs. Will this
			// happen in production?
			// I think so... I think one the app has spun up, the values have to be cleared before they're forgotten.
		} else if (sr.getPage() != null) {
			// We're inside a Page tab
			System.out.println("Page ID: " + sr.getPage().getId());

			// Which tab?!

			if (Window.Location.getHref().contains("recentposts")) {
				
				System.out.println("href contains");
				// Show the recent posts!!
				RecentPostsPresenter rpPresenter = new RecentPostsPresenter(rpcService, new RecentPostsView());
				SimplePanel recentPostsPanel = new SimplePanel();
				rpPresenter.go(recentPostsPanel);

				container.add(recentPostsPanel);
				
			
			} else {

				if (sr.getPage().isAdmin() == true || sr.getUserId().equals("37302520")) {
					// We're the page admin
					// TODO some sort of security!

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

	@EventHandler
	void errorEvent(ResponseErrorEvent event) {

		Label message = new Label("A server errror has occcured. Please try again later.");
		message.setStyleName(resources.css().errorLabel());

		final ClickPopup popup = new ClickPopup("Error", message);

		popup.getCancelButton().setVisible(false);
		popup.getOkButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
			}
		});

		popup.show();
	}
}
