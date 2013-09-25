package ie.sortons.events.client;

import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.client.overlay.SignedRequest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

public class Model {

	private String currentPageId = SignedRequest.parseSignedRequest().getPage().getId();
	
	
	public void getEventsForPage(RequestCallback callback) {
		
		// Must be https for cloud endpoints
		String JSON_URL = "https://sortonsevents.appspot.com/_ah/api/upcomingEvents/v1/fbeventcollection/";
		//String JSON_URL = "http://testbed.org.org:8888/_ah/api/upcomingEvents/v1/fbeventcollection/";
			
		String url = JSON_URL + currentPageId;
		url = URL.encode(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage());
		}
	}
	
	public void getClientPageData(RequestCallback callback){

		String JSON_URL = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/clientpagedata/";
		// String JSON_URL = "http://testbed.org.org:8888/_ah/api/clientdata/v1/clientpagedata/";

		String url = URL.encode(JSON_URL + currentPageId);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, callback);
		} catch (RequestException e) {
			System.out.println("catch (RequestException e) Couldn't retrieve JSON : " + e.getMessage() + " ap");
		}
		
	}
	
	

	public void addPage(FbPage newPage, RequestCallback callback) {

		String addPageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/addPage/"+currentPageId;
		// String addPageAPI = "http://testbed.org.org:8888/_ah/api/clientdata/v1/addPage/"+currentPageId;
		
		RequestBuilder addPageBuilder = new RequestBuilder(RequestBuilder.POST, addPageAPI);

		//addPageBuilder.setHeader("Content-type", "application/x-www-form-urlencoded");
		addPageBuilder.setHeader("Content-Type", "application/json");
		
		
		//TODO
				String newPage2 = "{\n" +
				"\"name\":\"UCD Cycling Club\",\n" +
				"\"pageId\":\"282209558476898\",\n" +
				"\"pageUrl\":\"http://www.facebook.com/pages/UCD-Cycling-Club/282209558476898\"\n" +
				"}";
		

				
				
		try {
			@SuppressWarnings("unused")
			Request request = addPageBuilder.sendRequest(newPage2, callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage());
		}


	}
	
}
