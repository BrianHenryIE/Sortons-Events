package ie.sortons.events.client;

import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.client.overlay.FqlPageOverlay;
import ie.sortons.gwtfbplus.client.overlay.SignedRequest;
import ie.sortons.gwtfbplus.server.fql.FqlPage;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtfb.client.DataObject;
import com.gwtfb.sdk.FBCore;

public class ClientDAO {

	// When it comes time to refactor:
	// https://code.google.com/p/google-apis-client-generator/wiki/TableOfContents

	private String currentPageId = SignedRequest.parseSignedRequest().getPage().getId();
	private FBCore fbCore;
	private SimpleEventBus eventBus;

	private ClientPageData clientPageData;

	public ClientPageData getClientPageData() {
		return this.clientPageData;
	}

	public ClientDAO(SimpleEventBus eventBus) {
		this.eventBus = eventBus;

	}


	public void setGwtFb(FBCore fbCore) {
		this.fbCore = fbCore;
	}


	public void getEventsForPage(RequestCallback callback) {

		// Must be https for cloud endpoints
		String JSON_URL = "https://sortonsevents.appspot.com/_ah/api/upcomingEvents/v1/discoveredeventcollection/";
		//String JSON_URL = "http://testbed.org.org:8888/_ah/api/upcomingEvents/v1/fbeventcollection/";

		String url = JSON_URL + currentPageId;
		url = URL.encode(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :getEventsForPage()");
		}
	}

	public void refreshClientPageData(final AdminPresenter adminPresenter){

		String JSON_URL = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/clientpagedata/";
		// String JSON_URL = "http://testbed.org.org:8888/_ah/api/clientdata/v1/clientpagedata/";

		String url = URL.encode(JSON_URL + currentPageId);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			@SuppressWarnings("unused")
			Request request = builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON getClientPageData");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {

						ClientPageData.Overlay clientPageDetailsJS = JsonUtils.safeEval(response.getText()).cast();

						clientPageData = new ClientPageData(clientPageDetailsJS);

						adminPresenter.displayClientData(clientPageData);

					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") AdminPresenter.getClientPageData()");
						//System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ") getClientPageData");
						//System.out.println("Couldn't retrieve JSON (" + response.getText() + ")" getClientPageData);
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("catch (RequestException e) Couldn't retrieve JSON : " + e.getMessage() + " getClientPageData()");
		}

	}


	public void addPage(FbPage newPage, RequestCallback callback) {

		String addPageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/addPage/"+currentPageId;
		// String addPageAPI = "http://testbed.org.org:8888/_ah/api/clientdata/v1/addPage/"+currentPageId;

		RequestBuilder addPageBuilder = new RequestBuilder(RequestBuilder.POST, addPageAPI);

		addPageBuilder.setHeader("Content-Type", "application/json");

		try {
			@SuppressWarnings("unused")
			Request request = addPageBuilder.sendRequest(newPage.asJsonString(), callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :addPage()");
		}

	}

	public void ignorePage(FbPage page, RequestCallback callback) {

		String ignorePageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/ignorePage/"+currentPageId;

		RequestBuilder ignorePageBuilder = new RequestBuilder(RequestBuilder.POST, ignorePageAPI);

		ignorePageBuilder.setHeader("Content-Type", "application/json");

		try {
			@SuppressWarnings("unused")
			Request request = ignorePageBuilder.sendRequest(page.asJsonString(), callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :addPage()");
		}

	}


	public void graphCall(String graphPath, AsyncCallback<JavaScriptObject> callback) {

		fbCore.api(graphPath, callback);

	}

	public void getSuggestions(final AdminPresenter presenter) {


		String searchPages = currentPageId+","+Joiner.on(",").join(clientPageData.getIncludedPageIds());

		final Joiner joiner = Joiner.on(",").skipNulls();

		String ignoredPages = Joiner.on(",").join(clientPageData.getIgnoredPageIds());
		String includedPages = Joiner.on(",").join(clientPageData.getIncludedPageIds());

		String excludePages = "";
		if(clientPageData.getIgnoredPageIds().size()>0 && clientPageData.getIncludedPageIds().size()>0){
			excludePages = ignoredPages+","+includedPages;
		} else {
			excludePages = (clientPageData.getIgnoredPageIds().size()>0) ? ignoredPages : includedPages;
		}

		String fql = "SELECT page_id, name, page_url FROM page WHERE page_id IN (SELECT page_id FROM page_fan WHERE uid IN ("+searchPages+") AND NOT (page_id IN ("+excludePages+")) LIMIT 250)";

		System.out.println(fql);

		String method = "fql.query";
		JSONObject query = new JSONObject();
		query.put("method", new JSONString(method));
		query.put("query", new JSONString(fql));

		fbCore.api(query.getJavaScriptObject(),
				new AsyncCallback<JavaScriptObject>() {
			public void onSuccess(JavaScriptObject response) {

				JsArray<FqlPageOverlay> likesJs;	

				DataObject dataObject = response.cast();

				likesJs = dataObject.getData().cast();

				System.out.println("likes: " + likesJs.length());
				
				
				List<FbPage> likes = new ArrayList<FbPage>();

				for(int i = 0; i < likesJs.length(); i++){
					likes.add(new FbPage(likesJs.get(i).getName(), likesJs.get(i).getPageUrl(), likesJs.get(i).getPageId()));
				}

				presenter.setSuggestions(likes);

			}
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
		});	
	}








}
