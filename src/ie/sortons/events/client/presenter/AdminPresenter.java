package ie.sortons.events.client.presenter;


import ie.sortons.events.client.ClientDAO;
import ie.sortons.events.client.appevent.PageLikesReceivedEvent;
import ie.sortons.events.client.view.overlay.ClientPageDataOverlay;
import ie.sortons.events.client.view.overlay.FbGraphOverlay;
import ie.sortons.events.client.view.overlay.FbPageOverlay;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.FbPage;

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

	private final ClientDAO rpcService;
	private final EventBus eventBus;
	private final Display display;
	private ClientPageData clientPageDetails;  

	public interface Display {
		HasText getNewPage();
		HasClickHandlers getAddButton();
		void setIncludedPages(List<FbPage> includedList);
		void setSuggestedPages(List<FbPage> suggestionsList);

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




	public AdminPresenter(EventBus eventBus, final ClientDAO rpcService, Display view) {
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

					ClientPageDataOverlay clientPageDetailsJS = JsonUtils.safeEval(response.getText()).cast();

					clientPageDetails = new ClientPageData(clientPageDetailsJS);
					
					displayClientData();

				} else {
					System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") getClientPageData");
					//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ") getClientPageData");
					//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")" getClientPageData);
				}
			}
		});

	}


	private void displayClientData(){

		display.setIncludedPages(clientPageDetails.getIncludedPages());

	}


	private void getSuggestions() {

		// Get the page's likes
		// filter out the included and ignored pages

		// Get the included pages' likes.

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

		//TODO get rid of anything after ?
		// http://www.facebook.com/ISS.UCD?ref=stream

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
		// TODO
		// Give feedback when it doesn't match

		graphPath += "?fields=name,id,link";

		rpcService.graphCall(graphPath,  new AsyncCallback<JavaScriptObject>() {
			public void onSuccess(JavaScriptObject response) {

				FbGraphOverlay pageDetails = response.cast();

				FbPage newPage = new FbPage(pageDetails.getName(), pageDetails.getLink(), pageDetails.getId());

				addPage(newPage); 

			}
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
		});	

	}

	public void addPage(FbPage newPage){

		rpcService.addPage(newPage, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				System.out.println("Couldn't retrieve JSON");
			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

					System.out.println(response.getText());

					FbPageOverlay pageJs = JsonUtils.safeEval(response.getText()).cast();

					FbPage page = new FbPage(pageJs);
					
					System.out.println(page.getName() + " ... clientPageDetails.getIncludedPages().contains(page): " + clientPageDetails.getIncludedPages().contains(page));

					if(!clientPageDetails.getIncludedPages().contains(page)){
						clientPageDetails.addPage(page);
					}

					if(clientPageDetails.getIgnoredPages().contains(page)){
						clientPageDetails.ignorePage(page);
					}

					System.out.println(page.getName() + " ... clientPageDetails.getIncludedPages().contains(page): " + clientPageDetails.getIncludedPages().contains(page));
					
					// then update UI
					displayClientData();


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