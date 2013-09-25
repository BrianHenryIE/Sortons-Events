package ie.sortons.events.client.presenter;


import ie.sortons.events.client.Model;
import ie.sortons.events.client.view.overlay.ClientPageDataOverlay;
import ie.sortons.events.shared.FbPage;

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
import com.google.web.bindery.event.shared.EventBus;


public class AdminPresenter implements Presenter {


	public interface Display {
		HasClickHandlers getAddButton();
		//HasClickHandlers getDeleteButton();
		//HasClickHandlers getList();
		//void setData(List<String> data);
		//int getClickedRow(ClickEvent event);
		//List<Integer> getSelectedRows();
		Widget asWidget();
	}


	private final Model rpcService;
	private final EventBus eventBus;
	private final Display display;  

	public void bind() {
		display.getAddButton().addClickHandler(new ClickHandler() {   
			public void onClick(ClickEvent event) {
				// eventBus.fireEvent(new AddContactEvent());
				addPage();
			}
		});
	}

	public AdminPresenter(EventBus eventBus, final Model rpcService, Display view) {
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




	// Get the likes for the pages
	// Display as suggestions



	protected void displayClientData(JavaScriptObject clientPageDetailsJSO) {

		ClientPageDataOverlay clientPageDetails = clientPageDetailsJSO.cast();

		System.out.println("returned: "+clientPageDetails.getClientPageId());

	}



}