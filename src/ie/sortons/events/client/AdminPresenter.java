package ie.sortons.events.client;


import ie.sortons.events.client.overlay.ClientPageDataOverlay;
import ie.sortons.events.shared.FbPage;
import ie.sortons.events.shared.FbPageI;
import ie.sortons.gwtfbplus.client.overlay.SignedRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;


public class AdminPresenter {

	
	// Declare the factory type
	interface MyFactory extends AutoBeanFactory {
		AutoBean<FbPageI> fbPage();
	}

	// Instantiate the factory
	MyFactory factory = GWT.create(MyFactory.class);

	String serializeToJson(FbPage person) {
		// Retrieve the AutoBean controller
		AutoBean<FbPage> bean = AutoBeanUtils.getAutoBean(person);

		return AutoBeanCodex.encode(bean).getPayload();
	}
	
	private String currentPageId;

	private Button button = new Button("click");
	
	@SuppressWarnings("unused")
	private HasWidgets view;

	private static final String JSON_URL = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/clientpagedata/";
	// private static final String JSON_URL = "http://testbed.org.org:8888/_ah/api/clientdata/v1/clientpagedata/";

	public AdminPresenter() {
	  
		currentPageId = SignedRequest.parseSignedRequest().getPage().getId();


		getClientPageData(currentPageId);
		
		
		ClickHandler handler = new ClickHandler() {
		        public void onClick(ClickEvent event) {
		        	System.out.println("Button clicked");
		          addPage();
		        }
		      };
		button.addClickHandler(handler);
		

		// Get the likes for the pages
		// Display as suggestions

	}
	
	
	private void getClientPageData(String pageId){

		String url = JSON_URL + pageId; 
		System.out.println(url);
		url = URL.encode(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, new RequestCallback() {
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
		} catch (RequestException e) {
			System.out.println("catch (RequestException e) Couldn't retrieve JSON : " + e.getMessage() + " ap");
		}
		
	}
	

	
	


	private void addPage() {

		String addPageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/addPage/"+currentPageId;
		// String addPageAPI = "http://testbed.org.org:8888/_ah/api/clientdata/v1/addPage/"+currentPageId;
		
		RequestBuilder addPageBuilder = new RequestBuilder(RequestBuilder.POST, addPageAPI);

		//addPageBuilder.setHeader("Content-type", "application/x-www-form-urlencoded");
		addPageBuilder.setHeader("Content-Type", "application/json");
		
		
		//TODO
				String newPage = "{\n" +
				"\"name\":\"UCD Cycling Club\",\n" +
				"\"pageId\":\"282209558476898\",\n" +
				"\"pageUrl\":\"http://www.facebook.com/pages/UCD-Cycling-Club/282209558476898\"\n" +
				"}";
		

				
				
		try {
			@SuppressWarnings("unused")
			Request request = addPageBuilder.sendRequest(newPage, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						//displayClientData(JsonUtils.safeEval(response.getText()));
						System.out.println(response.getText());
					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
						//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage());
		}


	}
	

	protected void displayClientData(JavaScriptObject clientPageDetailsJSO) {

		ClientPageDataOverlay clientPageDetails = clientPageDetailsJSO.cast();

		System.out.println("returned: "+clientPageDetails.getClientPageId());
		
	}


	private TextBox addNewPageBox = new TextBox();
	private Button addNewPageButton = new Button("Add");

	void setView(HasWidgets view) {
		this.view = view;

		// Pretty sure the V in MVP means this shouldn't be here		
		FlowPanel panel = new FlowPanel();

		panel.add(new InlineHTML("from:"));
		panel.add(addNewPageBox);
		panel.add(addNewPageButton);
		
		panel.add(button);

		view.add(panel);	
	}

}