package ie.sortons.events.client.presenter;


import ie.sortons.events.client.ClientModel;
import ie.sortons.events.client.appevent.PageLikesReceivedEvent;
import ie.sortons.events.client.view.overlay.ClientPageDataOverlay;
import ie.sortons.events.client.view.overlay.FbPageOverlay;
import ie.sortons.events.shared.FbPage;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;


public class AdminPresenter implements Presenter {

	private final ClientModel rpcService;
	private final EventBus eventBus;
	private final Display display;  

	public interface Display {
		HasClickHandlers getAddButton();
		void setIncludedPages(List<FbPageOverlay> includedPagesList);
		void setSuggestedPages(List<FbPageOverlay> suggestionsList);
		void setIgnoredPages(List<FbPageOverlay> ignoredPagesList);
		//HasClickHandlers getDeleteButton();
		//HasClickHandlers getList();

		//int getClickedRow(ClickEvent event);
		//List<Integer> getSelectedRows();
		Widget asWidget();
	}

	//interface MyEventBinder extends EventBinder<AdminPresenter> {}
	//private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	public void bind() {

		display.getAddButton().addClickHandler(new ClickHandler() {   
			public void onClick(ClickEvent event) {
				// eventBus.fireEvent(new AddContactEvent());
				addPage();
			}
		});
	}

	@EventHandler
	void onLoginEvent(PageLikesReceivedEvent event) {
		// Check the likes aren't already in the included or excluded lists
		// Add the likes to the suggestions list.
		// event.getLikes()
		// display.setSuggestions(suggestionsList)
	}



	public AdminPresenter(EventBus eventBus, final ClientModel rpcService, Display view) {
		//eventBinder.bindEventHandlers(this, eventBus);
		
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		this.display = view;		
	}


	@Override
	public void go(HasWidgets container) {
		bind();
		container.clear();
		container.add(display.asWidget());
		getClientPageData();
		rpcService.getPageLikes();
	}



	private void getClientPageData() {
		rpcService.getClientPageData(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON ap");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {
					displayClientData(JsonUtils.safeEval(response.getText()));
					//System.out.println(response.getText());
				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") ap");
					//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
					//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
				}
			}
		});

	}

	private void addPage() {

		System.out.println("Button clicked");

		FbPage newPage = new FbPage("name", "pageId", "pageUrl");

		rpcService.addPage(newPage, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					System.out.println(response.getText());
				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ")");
					//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
					//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
				}
			}
		});
	}




	// Display as suggestions



	protected void displayClientData(JavaScriptObject clientPageDetailsJSO) {

		ClientPageDataOverlay clientPageDetails = clientPageDetailsJSO.cast();

		System.out.println("returned: "+clientPageDetails.getClientPageId());

	}



}