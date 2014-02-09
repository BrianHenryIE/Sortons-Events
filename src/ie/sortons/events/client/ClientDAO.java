package ie.sortons.events.client;

import ie.sortons.events.client.presenter.AdminPresenter;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.DsFqlPage;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtfb.sdk.FBCore;
import com.kfuntak.gwt.json.serialization.client.HashMapSerializer;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class ClientDAO {

	// When it comes time to refactor:
	// https://code.google.com/p/google-apis-client-generator/wiki/TableOfContents

	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	private Long currentPageId;
	
	private FBCore fbCore;
	// private SimpleEventBus eventBus;

	private ClientPageData clientPageData;

	public ClientPageData getClientPageData() {
		return this.clientPageData;
	}

	public ClientDAO(SimpleEventBus eventBus) {
		// this.eventBus = eventBus;
	}

	public void setGwtFb(FBCore fbCore) {
		this.fbCore = fbCore;
	}
	
	
	{
		if(SignedRequest.getSignedRequestFromHTML() != null)
			currentPageId = Long.parseLong(((SignedRequest) serializer.deSerialize(new JSONObject(SignedRequest.getSignedRequestFromHTML()), "ie.sortons.gwtfbplus.shared.domain.SignedRequest")).getPage().getId());
		
	}

	public void getEventsForPage(RequestCallback callback) {

		// Must be https for cloud endpoints
		String jsonUrl = "https://sortonsevents.appspot.com/_ah/api/upcomingEvents/v1/discoveredeventsresponse/";

		// Check for dev mode
		if (!GWT.isProdMode() && GWT.isClient()){
			System.out.println("dev mode getevents");
			jsonUrl = "http://testbed.org.org:8888/_ah/api/upcomingEvents/v1/discoveredeventsresponse/";
		}
			

		String url = jsonUrl + currentPageId;
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

	public void refreshClientPageData(final AdminPresenter adminPresenter) {

		String jsonUrl = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/clientpagedata/";

		// Check for dev mode
		if (!GWT.isProdMode() && GWT.isClient()) {
			System.out.println("dev mode cpd");
			jsonUrl = "http://testbed.org.org:8888/_ah/api/clientdata/v1/clientpagedata/";
		}

		String url = URL.encode(jsonUrl + currentPageId);

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

						clientPageData = (ClientPageData) serializer.deSerialize(response.getText());

						adminPresenter.displayClientData(clientPageData);

					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") AdminPresenter.getClientPageData()");
						// System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() +
						// ") getClientPageData");
						// System.out.println("Couldn't retrieve JSON (" + response.getText() + ")" getClientPageData);
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("catch (RequestException e) Couldn't retrieve JSON : " + e.getMessage() + " getClientPageData()");
		}

	}

	public void addPage(FqlPage newPage, RequestCallback callback) {

		String addPageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/addPage/" + currentPageId;

		// Check for dev mode
		if (!GWT.isProdMode() && GWT.isClient()) {
			System.out.println("dev mode add page");
			addPageAPI = "http://testbed.org.org:8888/_ah/api/clientdata/v1/addPage/" + currentPageId;
		}

		RequestBuilder addPageBuilder = new RequestBuilder(RequestBuilder.POST, addPageAPI);

		addPageBuilder.setHeader("Content-Type", "application/json");

		clientPageData.getSuggestedPages().remove(newPage);

		try {
			@SuppressWarnings("unused")
			Request request = addPageBuilder.sendRequest(serializer.serialize(newPage), callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :addPage()");
		}

	}

	public void ignorePage(FqlPage page, RequestCallback callback) {

		clientPageData.getSuggestedPages().remove(page);

		String ignorePageAPI = "https://sortonsevents.appspot.com/_ah/api/clientdata/v1/ignorePage/" + currentPageId;

		// Check for dev mode
		if (!GWT.isProdMode() && GWT.isClient()) {
			System.out.println("dev mode ignore page");
			ignorePageAPI = "http://testbed.org.org:8888/_ah/api/clientdata/v1/ignorePage/" + currentPageId;
		}

		RequestBuilder ignorePageBuilder = new RequestBuilder(RequestBuilder.POST, ignorePageAPI);
		ignorePageBuilder.setHeader("Content-Type", "application/json");

		try {
			@SuppressWarnings("unused")
			Request request = ignorePageBuilder.sendRequest(serializer.serialize(page), callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :ignorePage()");
		}

	}

	public void graphCall(String graphPath, AsyncCallback<JavaScriptObject> callback) {
		fbCore.api(graphPath, callback);
	}

	public List<DsFqlPage> getSuggestions() {
		return clientPageData.getSuggestedPages();
	}

	public void getSuggestions(final AdminPresenter presenter) {

		System.out.println("getSuggestions()");
		
		if (clientPageData.getSuggestedPages() == null || clientPageData.getSuggestedPages().size() < 25) {

			List<Long> searchPagesList = new ArrayList<Long>();
			for (Long pageId : clientPageData.getIncludedPageIds()) {
				searchPagesList.add(pageId);
			}
			// http://blog.jonleonard.com/2012/10/gwt-collectionsshuffle-implementation.html
			for (int index = 0; index < searchPagesList.size(); index += 1) {
				Collections.swap(searchPagesList, index, Random.nextInt(searchPagesList.size()));
			}
			String searchPages = currentPageId + "," + Joiner.on(",").join(searchPagesList);

			String ignoredPages = Joiner.on(",").join(clientPageData.getIgnoredPageIds());
			String includedPages = Joiner.on(",").join(clientPageData.getIncludedPageIds());

			String excludePages = "";
			if (clientPageData.getIgnoredPageIds().size() > 0 && clientPageData.getIncludedPageIds().size() > 0) {
				excludePages = ignoredPages + "," + includedPages;
			} else {
				excludePages = (clientPageData.getIgnoredPageIds().size() > 0) ? ignoredPages : includedPages;
			}

			String fql = "SELECT page_id, name, page_url, location FROM page WHERE page_id IN (SELECT page_id FROM page_fan WHERE uid IN ("
					+ searchPages + ") AND NOT (page_id IN (" + excludePages + ")) LIMIT 250)";

			System.out.println(fql);

			String method = "fql.query";
			JSONObject query = new JSONObject();
			query.put("method", new JSONString(method));
			query.put("query", new JSONString(fql));

			System.out.println("fire fql");
			
			fbCore.api(query.getJavaScriptObject(), new AsyncCallback<JavaScriptObject>() {
				public void onSuccess(JavaScriptObject response) {

					HashMapSerializer hashMapSerializer = (HashMapSerializer) GWT.create(HashMapSerializer.class);

					System.out.println("deserialize");
					
					@SuppressWarnings("unchecked")
					HashMap<String, DsFqlPage> map = (HashMap<String, DsFqlPage>) hashMapSerializer.deSerialize(new JSONObject(response),
							"ie.sortons.events.shared.DsFqlPage");

					System.out.println("process");
					ArrayList<DsFqlPage> pages = new ArrayList<DsFqlPage>();
					for (DsFqlPage page : map.values()) {
						pages.add(page);
					}

					clientPageData.setSuggestedPages(pages);

					// Shuffle the list so it's not a bunch of similar suggestions consecutively
					// http://blog.jonleonard.com/2012/10/gwt-collectionsshuffle-implementation.html
					for (int index = 0; index < clientPageData.getSuggestedPages().size(); index += 1) {
						Collections.swap(clientPageData.getSuggestedPages(), index, Random.nextInt(clientPageData.getSuggestedPages().size()));
					}

					presenter.setSuggestions(clientPageData.getSuggestedPages());

				}

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
				}
			});

		} else {

			presenter.setSuggestions(clientPageData.getSuggestedPages());
		}
	}

}
