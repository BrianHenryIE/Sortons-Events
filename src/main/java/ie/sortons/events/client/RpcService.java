package ie.sortons.events.client;

import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.PageList;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.events.shared.WallPost;
import ie.sortons.events.shared.dto.ClientPageDataResponse;
import ie.sortons.events.shared.dto.DiscoveredEventsResponse;
import ie.sortons.events.shared.dto.PagesListResponse;
import ie.sortons.events.shared.dto.RecentPostsResponse;
import ie.sortons.gwtfbplus.client.api.FBCore;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.kfuntak.gwt.json.serialization.client.HashMapSerializer;
import com.kfuntak.gwt.json.serialization.client.Serializer;

public class RpcService {

	// When it comes time to refactor:
	// https://code.google.com/p/google-apis-client-generator/wiki/TableOfContents

	private static final Logger log = Logger.getLogger(RpcService.class.getName());
	
	Serializer serializer = (Serializer) GWT.create(Serializer.class);

	// Must be https for cloud endpoints
	private String apiBase = Config.getApiBase();
	
	private Long currentPageId;

	private FBCore fbCore;
	// private SimpleEventBus eventBus;

	private ClientPageData clientPageData;

	public ClientPageData getClientPageData() {
		return this.clientPageData;
	}

	public RpcService(SimpleEventBus eventBus) {
		// this.eventBus = eventBus;
	}

	public void setGwtFb(FBCore fbCore) {
		this.fbCore = fbCore;
	}

	{
		if (SignedRequest.getSignedRequestFromHTML() != null)
			currentPageId = Long.parseLong(((SignedRequest) serializer.deSerialize(
					new JSONObject(SignedRequest.getSignedRequestFromHTML()), "ie.sortons.gwtfbplus.shared.domain.SignedRequest"))
					.getPage().getId());
	}

	public void getEventsForPage(final AsyncCallback<List<DiscoveredEvent>> callback) {

		log.info("\n\ngetEventsForPage()");
		
		// Must be https for cloud endpoints
		String jsonUrl = apiBase + "upcomingEvents/v1/discoveredeventsresponse/";

		String url = jsonUrl + currentPageId;
		url = URL.encode(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {

						log.info("about to deserialize");;
						
						DiscoveredEventsResponse deResponse = (DiscoveredEventsResponse) serializer.deSerialize(response.getText(),
								"ie.sortons.events.shared.DiscoveredEventsResponse");

						log.info("deserialized");
						
						callback.onSuccess(deResponse.getData());
					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") PageEventsPresenter.getEvents");
						// System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
						// System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :getEventsForPage()");
		}
	}

	public void getWallPostsForPage(final AsyncCallback<List<WallPost>> callback) {

		// Must be https for cloud endpoints
		String jsonUrl = apiBase + "recentPosts/v1/recentpostsresponse/";

		String url = jsonUrl + currentPageId;
		url = URL.encode(url);

		System.out.println(url);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request, Response response) {

					System.out.println("response: " + response.getText());

					RecentPostsResponse deResponse = (RecentPostsResponse) serializer.deSerialize(response.getText(),
							"ie.sortons.events.shared.RecentPostsResponse");
					callback.onSuccess(deResponse.getData());
				}

				@Override
				public void onError(Request request, Throwable exception) {
					// TODO Auto-generated method stub

				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :getEventsForPage()");
		}
	}

	public void refreshClientPageData(final AsyncCallback<ClientPageData> callback) {

		String jsonUrl = apiBase + "clientdata/v1/clientpagedata/";

		String url = URL.encode(jsonUrl + currentPageId);

		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					System.out.println("Couldn't retrieve JSON getClientPageData");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {

						clientPageData = serializer.deSerialize(response.getText(), ClientPageData.class);

						callback.onSuccess(clientPageData);

					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText()
								+ ") rpcservice refreshClientPageData");
						// System.out.println("Couldn't retrieve JSON (" +
						// response.getStatusCode() +
						// ") getClientPageData");
						// System.out.println("Couldn't retrieve JSON (" +
						// response.getText() + ")" getClientPageData);
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("catch (RequestException e) Couldn't retrieve JSON : " + e.getMessage() + " getClientPageData()");
		}

	}

	
	public void addPage(SourcePage newPage, final AsyncCallback<SourcePage> callback) {

		log.info("addPage");		
		
		String addPageAPI = apiBase + "clientdata/v1/addPage/" + currentPageId;

		RequestBuilder addPageBuilder = new RequestBuilder(RequestBuilder.POST, addPageAPI);

		addPageBuilder.setHeader("Content-Type", "application/json");

		// temporary to build request cookie header
		StringBuilder sb = new StringBuilder();
		
		for(String cookieName : Cookies.getCookieNames()){
			if (sb.length() > 0) {
	            sb.append("; ");
	        }
			String value = cookieName+"="+Cookies.getCookie(cookieName);
	        sb.append(value);
		}
		String cookieHeader = sb.toString();

		addPageBuilder.setHeader("Cookie", cookieHeader);
		
		log.info("cookieHeader " + cookieHeader);
		
		clientPageData.getSuggestedPages().remove(newPage);

		try {
			addPageBuilder.sendRequest(serializer.serialize(newPage), new RequestCallback() {
				public void onError(Request request, Throwable exception) {

					// TODO Set the suggestbox item to xable
					System.out.println("Couldn't retrieve JSON");
				}

				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {

						
						SourcePage page = (SourcePage) serializer.deSerialize(response.getText(),
								"ie.sortons.events.shared.FqlPageSearchable");

						// TODO return a real error message
						if (page.getPageId() != null) {
							clientPageData.addPage(page);

							callback.onSuccess(page);
							

						} else {
							// TODO Fire error message
							// was page already included?
							// or serious error?
						}

					} else {
						System.out.println("Couldn't retrieve JSON (" + response.getStatusText() + ") AdminPresenter.addPage()");
						System.out.println("Couldn't retrieve JSON (" + response.getStatusCode() + ")");
						System.out.println("Couldn't retrieve JSON (" + response.getText() + ")");

						// TODO: How to know what type of error it is?
//						eventBus.fireEvent(new ResponseErrorEvent(response));

						
					}
				}
			});
			
			log.info("post request cookie: " + addPageBuilder.getHeader("Cookie"));
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :addPage()");
		}

	}

	public void addPagesList(String pagesList, final AsyncCallback<List<SourcePage>> asyncCallback) {

		System.out.println(pagesList);
		String addPagesListAPI = apiBase + "clientdata/v1/addPagesList/" + currentPageId;

		RequestBuilder addPagesListBuilder = new RequestBuilder(RequestBuilder.POST, addPagesListAPI);

		addPagesListBuilder.setHeader("Content-Type", "application/json");

		try {
			@SuppressWarnings("unused")
			Request request = addPagesListBuilder.sendRequest(serializer.serialize(new PageList(pagesList)), new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					System.out.println(response.getText());
					PagesListResponse addedPages = (PagesListResponse) serializer.deSerialize(response.getText(),
							"ie.sortons.events.shared.PagesListResponse");
					System.out.println(addedPages.getData());

					asyncCallback.onSuccess(addedPages.getData());

				}

				@Override
				public void onError(Request request, Throwable exception) {
					// TODO Auto-generated method stub

					asyncCallback.onFailure(null);

				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :addPage()");
		}

	}

	public void removePage(SourcePage page, RequestCallback callback) {

		clientPageData.getSuggestedPages().remove(page);

		String removePageAPI = apiBase + "clientdata/v1/removePage/" + currentPageId;

		RequestBuilder removePageBuilder = new RequestBuilder(RequestBuilder.POST, removePageAPI);
		removePageBuilder.setHeader("Content-Type", "application/json");

		try {
			@SuppressWarnings("unused")
			Request request = removePageBuilder.sendRequest(serializer.serialize(page), callback);
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :removePage()");
		}

	}

	// TODO make private to keep powers separate
	public void graphCall(String graphPath, AsyncCallback<JavaScriptObject> callback) {
		fbCore.api(graphPath, callback);
	}

	private boolean alreadyFailed = false;

	/**
	 * Uses FQL
	 * 
	 * @param callback
	 */
	public void getSuggestions(final AsyncCallback<List<SourcePage>> callback) {

		// TODO shouldn't included existing included pages

		java.util.logging.Logger logger = Logger.getLogger(this.getClass().getSimpleName());
		logger.log(Level.INFO, "getSuggestions()!");
		
		
		// Uncaught com.google.gwt.event.shared.UmbrellaException: Exception caught: (TypeError) : Cannot read property 'ie_sortons_events_shared_ClientPageData_includedPages' of undefined
		// Have to wait for the clientpagedata here.
		List<Long> searchPagesList = new ArrayList<Long>();
		if(clientPageData==null)
			return;
		
		for (Long pageId : clientPageData.getIncludedPageIds())
			searchPagesList.add(pageId);

		System.out.println("searchPagesList " + searchPagesList.size());

		// http://blog.jonleonard.com/2012/10/gwt-collectionsshuffle-implementation.html
		for (int index = 0; index < searchPagesList.size(); index += 1) {
			Collections.swap(searchPagesList, index, Random.nextInt(searchPagesList.size()));
		}
		String searchPages = currentPageId + "," + Joiner.on(",").join(searchPagesList);

		System.out.println("searchPagesList shuffled");

		// TODO... someday (soon) this will get too big
		String fql = "SELECT page_id, name, page_url, location FROM page WHERE page_id IN (SELECT page_id FROM page_fan WHERE uid IN ("
				+ searchPages + ") ) AND NOT is_community_page = 'true'";
		// TODO Remove LIMIT 250 before deploying!

		System.out.println(fql);

		String method = "fql.query";
		JSONObject query = new JSONObject();
		query.put("method", new JSONString(method));
		query.put("query", new JSONString(fql));

		System.out.println("fire fql");

		fbCore.api(query.getJavaScriptObject(), new AsyncCallback<JavaScriptObject>() {
			@SuppressWarnings("unchecked")
			public void onSuccess(JavaScriptObject response) {

				HashMapSerializer hashMapSerializer = (HashMapSerializer) GWT.create(HashMapSerializer.class);

				System.out.println("deserialize");

				JSONObject responseJson = new JSONObject(response);

				// TODO check how slow this is compared to overlays
				HashMap<String, FqlPage> map = new HashMap<String, FqlPage>();
				try {
					// TODO stop using a string here
					map = (HashMap<String, FqlPage>) hashMapSerializer.deSerialize(responseJson,
							"ie.sortons.gwtfbplus.shared.domain.fql.FqlPage");
				} catch (Exception e) {

					System.out.println(responseJson);
					// If it's failed here, it's possibly because we've tried
					// this before the sdk has initialized with
					// its access token
					// JsFqlError error = (JsFqlError)
					// serializer.deSerialize(responseJson,
					// "ie.sortons.events.shared.JsFqlError");
					// System.out.println(error.getErrorMsg());
					if (!alreadyFailed) {
						alreadyFailed = true;
						Timer t = new Timer() {
							@Override
							public void run() {
								getSuggestions(callback);
							}
						};
						t.schedule(1000);
					}

				}
				System.out.println("process");
				ArrayList<SourcePage> pages = new ArrayList<SourcePage>();
				for (FqlPage page : map.values()) {
					if (!clientPageData.getIncludedPageIds().contains(page.getPageId()))
						pages.add(new SourcePage(page));
				}

				clientPageData.setSuggestedPages(pages);

				System.out.println("pages.size() " + pages.size());

				// Shuffle the list so it's not a bunch of similar suggestions
				// consecutively
				// http://blog.jonleonard.com/2012/10/gwt-collectionsshuffle-implementation.html
				for (int index = 0; index < clientPageData.getSuggestedPages().size(); index += 1) {
					Collections.swap(clientPageData.getSuggestedPages(), index,
							Random.nextInt(clientPageData.getSuggestedPages().size()));
				}

				callback.onSuccess(clientPageData.getSuggestedPages());

			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
		});

	}

	/**
	 * This is for the sortonsadmin view
	 * 
	 * 
	 * 
	 * @param asyncCallback
	 */
	public void getAllClients(final AsyncCallback<List<ClientPageData>> asyncCallback) {

		String getAllClientsAPI = apiBase + "clientdata/v1/clientpagedataresponse/";

		RequestBuilder getAllClientsBuilder = new RequestBuilder(RequestBuilder.GET, getAllClientsAPI);
		getAllClientsBuilder.setHeader("Content-Type", "application/json");

		try {
			getAllClientsBuilder.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request, Response response) {
					System.out.println(response.getText());
					Serializer serializer = (Serializer) GWT.create(Serializer.class);

					List<ClientPageData> clients = ((ClientPageDataResponse) serializer.deSerialize(response.getText(),
							"ie.sortons.events.shared.ClientPageDataResponse")).getData();

					asyncCallback.onSuccess(clients);
				}

				@Override
				public void onError(Request request, Throwable exception) {
					// TODO Auto-generated method stub

				}
			});
		} catch (RequestException e) {
			System.out.println("Couldn't retrieve JSON : " + e.getMessage() + " :removePage()");
		}
	}

}
