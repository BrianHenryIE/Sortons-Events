package ie.sortons.events.client.presenter;


import ie.sortons.events.client.ClientModel;
import ie.sortons.events.client.appevent.PageLikesReceivedEvent;
import ie.sortons.events.client.view.overlay.ClientPageDataOverlay;
import ie.sortons.events.client.view.overlay.FbGraphOverlay;
import ie.sortons.events.client.view.overlay.FbPageOverlay;
import ie.sortons.events.shared.DsFbPage;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.gwtfb.client.DataObject;


public class AdminPresenter implements Presenter {

	private final ClientModel rpcService;
	private final EventBus eventBus;
	private final Display display;  

	public interface Display {
		HasText getNewPage();
		HasClickHandlers getAddButton();
		void setIncludedPages(JsArray<FbPageOverlay> jsArray);
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
				processTextBox();
			}
		});
		
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
				System.out.println("Couldn't retrieve JSON getClientPageData");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {
					displayClientData(JsonUtils.safeEval(response.getText()));
					// System.out.println(response.getText());
				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") getClientPageData");
					//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ") getClientPageData");
					//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")" getClientPageData);
				}
			}
		});

	}


	protected void displayClientData(JavaScriptObject clientPageDetailsJSO) {

		ClientPageDataOverlay clientPageDetails = clientPageDetailsJSO.cast();

		System.out.println("returned: "+clientPageDetails.getClientPageId());
		
		display.setIncludedPages(clientPageDetails.getIncludedPages());
		

	}


	
	private void processTextBox() {


		// Get the text from the textbox
		// regex it to a page_id
		// get the page's basic details
		// send it to the server
		// add it to the included pages list
		// get the new page's likes to add to suggestions.
		
		
		System.out.println("Button clicked");

		// Get the text that has been entered and build the graph call
		
		String textEntered = display.getNewPage().getText();
		String graphPath = "/";
		
		// Sometimes http://www.facebook.com/pages/Randals-Rest-UCD/107542286070006		
		if( textEntered.matches(".*facebook\\.com/pages/[^/]*/\\d*/?") ) {
			
			graphPath += textEntered.split(".*facebook\\.com/pages/[^/]*/")[1].replace("/", "");
		
		// Sometimes facebook.com/PageName
		} else if( textEntered.matches(".*facebook\\.com/[^/]*/?") ) {
			
			graphPath += textEntered.split(".*facebook\\.com/")[1].replace("/", "");
	
		// Sometimes 107542286070006	
		} else if( textEntered.matches("\\d*") ) {
			
			graphPath += textEntered;
			
		}
		
		graphPath += "?fields=name,id,link";

		
		rpcService.graphCall(graphPath,  new AsyncCallback<JavaScriptObject>() {
			public void onSuccess(JavaScriptObject response) {

				FbGraphOverlay pageDetails = response.cast();

				DsFbPage newPage = new DsFbPage(pageDetails.getName(), pageDetails.getLink(), pageDetails.getId());
				
				addPage(newPage); 
				
				// now how to clean up after?
				
			}
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
		});	
		
	}
	
	public void addPage(DsFbPage newPage){
		
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




}