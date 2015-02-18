package ie.sortons.events.client;

import ie.sortons.events.client.appevent.LoginAuthResponseEvent;
import ie.sortons.events.client.appevent.ResponseErrorEvent;
import ie.sortons.events.client.presenter.DirectoryPresenter;
import ie.sortons.events.client.presenter.PageAdminPresenter;
import ie.sortons.events.client.presenter.PageEventsPresenter;
import ie.sortons.events.client.presenter.RecentPostsPresenter;
import ie.sortons.events.client.presenter.SortonsAdminPresenter;
import ie.sortons.events.client.resources.Resources;
import ie.sortons.events.client.view.DirectoryView;
import ie.sortons.events.client.view.MyEventsView;
import ie.sortons.events.client.view.PageAdminView;
import ie.sortons.events.client.view.RecentPostsView;
import ie.sortons.events.client.view.SortonsAdminView;
import ie.sortons.gwtfbplus.client.api.FBCore;
import ie.sortons.gwtfbplus.client.overlay.AuthResponse;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.popups.ClickPopup;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEventMember;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class AppController {

	// Courtesy of gwtfb.com
	private FBCore fbCore = GWT.create(FBCore.class);

	public String APPID = "251403644880972"; // Config.getAppID();
	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;

	private RpcService rpcService;
	private SimpleEventBus eventBus;

	private HasWidgets container;

	interface MyEventBinder extends EventBinder<AppController> {
	}

	private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	GwtFbPlusResources resources = GwtFbPlusResources.INSTANCE;
	Resources res = Resources.INSTANCE;

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	public AppController(RpcService rpcService, SimpleEventBus eventBus) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		eventBinder.bindEventHandlers(this, eventBus);

		resources.facebookStyles().ensureInjected();
		resources.css().ensureInjected();

		res.css().ensureInjected();

		// Initialize the Facebook API
		fbCore.init(APPID, status, cookie, xfbml);

		// TODO Move to isadmin()
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

		SignedRequest sr = null;
		if (getSignedRequestFromHTML() != null) {
			sr = (SignedRequest) serializer.deSerialize(new JSONObject(getSignedRequestFromHTML()),
					"ie.sortons.gwtfbplus.shared.domain.SignedRequest");
		}

		// Where are we?
		if (sr == null) {
			// Looks like we're operating outside Facebook

		} else if (sr.getAppData() != null && sr.getAppData().contains("sortonsadmin") && sr.getUserId().equals("37302520")) {

			SortonsAdminPresenter saPresenter = new SortonsAdminPresenter(rpcService, new SortonsAdminView());
			SimplePanel sortonsAdminPanel = new SimplePanel();
			saPresenter.go(sortonsAdminPanel);

			container.add(sortonsAdminPanel);

		} else if (sr.getPage() == null) {
			// Are we inside Facebook with no Page ID? Then we're the app...

			// Show friends events!

			// TODO The dev server is caching the signedrequest variable and polluting the output for GETs. Will this
			// happen in production?
			// I think so... I think one the app has spun up, the values have to be cleared before they're forgotten.
		} else if (sr.getPage() != null) {
			// We're inside a Page tab
			System.out.println("Page ID: " + sr.getPage().getId());

			if (sr.getPage().isAdmin() == true || sr.getUserId().equals("37302520")) {

				PageAdminView adminView = new PageAdminView();
				PageAdminPresenter adminPresenter = new PageAdminPresenter(eventBus, rpcService, adminView);
				final SimplePanel adminPanel = new SimplePanel();
				adminPresenter.go(adminPanel);

				final PopupPanel adminPopup = new PopupPanel();
				adminPopup.add(adminPanel);
				adminPopup.setGlassEnabled(true);
				adminPopup.setStyleName("");
				adminPopup.setGlassStyleName(res.css().adminGlass());

				adminView.getCloseButton().addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						adminPopup.hide();
					}
				});

				Label apLink = new Label("Admin Panel");
				apLink.setStyleName(res.css().adminPanelButton());
				container.add(apLink);

				if (sr.getOauthToken() == null) {
					apLink.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							fbCore.login(new AsyncCallback<JavaScriptObject>() {
								@Override
								public void onFailure(Throwable caught) {
								}

								@Override
								public void onSuccess(JavaScriptObject result) {
									System.out.println(new JSONObject(result).toString());
									AuthResponse auth = result.cast();
									if (auth.getStatus().equals("not_authorized")) {
										Label authMessage = new Label("You must authorise the application in order to configure it.");
										authMessage.setStyleName(resources.css().infoLabel());
										final ClickPopup noAuth = new ClickPopup("App Authorisation", authMessage);
										noAuth.getCancelButton().removeFromParent();
										noAuth.getOkButton().addClickHandler(new ClickHandler() {
											@Override
											public void onClick(ClickEvent event) {
												noAuth.hide();
											}
										});
										noAuth.show();
									} else {
										eventBus.fireEvent(new LoginAuthResponseEvent(result));
										//Cookies.setCookie("accessToken", auth.getAccessToken(),new Date(new Date().getTime() + auth.getExpiresIn()));
										//Cookies.setCookie("userId", auth.getUserId(), new Date(new Date().getTime() + auth.getExpiresIn()));
										System.out.println("show popup");
										adminPopup.show();
										adminPopup.setPopupPosition(135, 40);
									}

								}
							});

						}
					});
				} else {
					apLink.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							adminPopup.show();
							adminPopup.setPopupPosition(135, 40);
						}
					});
				}

			}
			
			
			// Which tab?!

			if (Window.Location.getHref().contains("recentposts") || (sr.getAppData() != null && sr.getAppData().contains("recentposts"))) {

				System.out.println("recentposts");
				// Show the recent posts!!
				RecentPostsPresenter rpPresenter = new RecentPostsPresenter(rpcService, new RecentPostsView());
				SimplePanel recentPostsPanel = new SimplePanel();
				rpPresenter.go(recentPostsPanel);

				container.add(recentPostsPanel);

			} else if (Window.Location.getHref().contains("directory") || (sr.getAppData() != null && sr.getAppData().contains("directory"))) {

				System.out.println("directory");
			
				DirectoryPresenter dPresenter = new DirectoryPresenter(rpcService, new DirectoryView());
				SimplePanel dPanel = new SimplePanel();
				dPresenter.go(dPanel);

				container.add(dPanel);
				
			}else if (Window.Location.getHref().contains("myevents") || (sr.getAppData() != null && sr.getAppData().contains("myevents"))) {

					System.out.println("myevents");

					final MyEventsView mev = new MyEventsView();
					rpcService.getMyEvents(new AsyncCallback<List<FqlEvent>>(){
						@Override
						public void onFailure(Throwable caught) {}

						@Override
						public void onSuccess(List<FqlEvent> events) {
							mev.setEvents(events);
							
							rpcService.getInvitees(events, new AsyncCallback<List<FqlEventMember>>(){
								@Override
								public void onFailure(Throwable caught) {}

								@Override
								public void onSuccess(List<FqlEventMember> events) {
									mev.setInvitees(events);
									
									
								}});
							
						}});

					container.add(mev);

			} else {

			

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
