package ie.sortons.events.server;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import ie.sortons.gwtfbplus.shared.domain.graph.GraphEvent;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphFeedItem;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphFields;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPage;

public class Facebook {

	private static final Logger LOG = Logger.getLogger(Facebook.class.getName());

	// Call stub
	private String fbGraphCallStub = "https://graph.facebook.com/";

	// The app's fb access token. Never to be used client-side.
	private String serverAccessToken;
	private String fbApiVersion;

	/**
	 * Used for building the graph query for multiple objects
	 */
	private Joiner joiner = Joiner.on(",").skipNulls();

	public Facebook(String serverAccessToken, String fbApiVersion) {
		this.serverAccessToken = serverAccessToken;
		this.fbApiVersion = fbApiVersion;
	}

	List<GraphFeedItem> posts = new ArrayList<GraphFeedItem>();

	public List<GraphFeedItem> getPosts() {
		return posts;
	}

	public void nullPosts() {
		posts = new ArrayList<GraphFeedItem>();
	}

	// Shorter dates seem to be a thing of the past - is there a standard parser
	// that could be used instead?
	private Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
		@Override
		public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			try {
				return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(json.getAsString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}).create();

	public GraphPage getSinglePage(String pageId) {

		String query = pageId + "?fields=about,phone,name,id,link,location";

		List<String> queries = Arrays.asList(query);

		List<String> jsonResponses = batchAsyncUrlFetch(queries);

		GraphPage page = gson.fromJson(jsonResponses.get(0), GraphPage.class);

		return page;
	}

	/**
	 * The master method
	 * 
	 * @param pageIds
	 * @return
	 */
	public Map<GraphEvent, Set<String>> getGraphEventsForPageIds(List<List<String>> pageIds) {

		// Map<PageId, Set<EventId>>
		Map<String, Set<String>> eventIdsAndPages = getEventsAndTheirPageIds(pageIds);

		Set<String> eventIds = eventIdsAndPages.keySet();

		Map<String, GraphEvent> eventsDetails = getGraphEventsFromEventIds(eventIds);

		Map<GraphEvent, Set<String>> graphEventsAndPages = new HashMap<GraphEvent, Set<String>>();

		for (Entry<String, Set<String>> eventIdAndPages : eventIdsAndPages.entrySet()) {

			GraphEvent graphEvent = eventsDetails.get(eventIdAndPages.getKey());
			Set<String> eventPages = eventIdAndPages.getValue();

			graphEventsAndPages.put(graphEvent, eventPages);
		}

		return graphEventsAndPages;
	}

	/**
	 * 
	 * @param list
	 *            of event Ids
	 * @return list of GraphEvent objects
	 */
	public Map<String, GraphEvent> getGraphEventsFromEventIds(Set<String> eventIds) {

		// TODO: how many can we query at once before it starts failing? There's
		// a GET length limit
		// TODO: how do we notify of a failure? Prem op

		List<String> queries = new ArrayList<String>();

		List<String> eventIdsList = new ArrayList<String>(eventIds);
		List<List<String>> partitionedEventIds = Lists.partition(eventIdsList, 50);

		// default fields: description,end_time,name,place,start_time
		for (List<String> eventIdsPartition : partitionedEventIds) {
			String query = "?ids=" + joiner.join(eventIdsPartition);
			queries.add(query);
		}

		List<String> jsonResponseEvents = batchAsyncUrlFetch(queries);

		Type stringGraphEventMap = new TypeToken<Map<String, GraphEvent>>() {
		}.getType();

		Map<String, GraphEvent> events = new HashMap<String, GraphEvent>();
		for (String jsonResponse : jsonResponseEvents) {
			Map<String, GraphEvent> batch = gson.fromJson(jsonResponse, stringGraphEventMap);
			events.putAll(batch);
		}
		return events;
	}

	/**
	 * <EventId, <FbPageId>>
	 * 
	 * @param pageIds
	 * @return a list of event ids and the page ids the events were found on
	 */
	public Map<String, Set<String>> getEventsAndTheirPageIds(List<List<String>> pageIds) {

		Map<String, Set<String>> eventsAndTheirPages = new HashMap<String, Set<String>>();

		// <FbPageId, <EventId>>
		Map<String, Set<String>> pagesAndTheirEvents = getEventIdsPerPage(pageIds);

		for (Entry<String, Set<String>> entry : pagesAndTheirEvents.entrySet()) {

			String fbPageId = entry.getKey();
			Set<String> eventIds = entry.getValue();

			for (String eventId : eventIds) {
				if (!eventsAndTheirPages.containsKey(eventId))
					eventsAndTheirPages.put(eventId, new HashSet<String>());
				eventsAndTheirPages.get(eventId).add(fbPageId);
			}
		}

		return eventsAndTheirPages;
	}

	/**
	 * @param pageIds
	 * @return <FbPageId, <EventId>>
	 */
	public Map<String, Set<String>> getEventIdsPerPage(List<List<String>> pageIds) {

		Map<String, Set<String>> pagesAndTheirEvents = new HashMap<String, Set<String>>();

		List<String> graphQueriesToPerform = new ArrayList<String>();

		for (List<String> pageIdsList : pageIds) {
			String query = buildGraphQueryStringForFeedAndEventsForMultiplePages(pageIdsList);
			graphQueriesToPerform.add(query);
		}

		List<String> jsonList = batchAsyncUrlFetch(graphQueriesToPerform);

		Type stringGraphFieldsMap = new TypeToken<Map<String, GraphFields>>() {
		}.getType();

		Map<String, GraphFields> gmi = new HashMap<String, GraphFields>();
		for (String json : jsonList) {
			Map<String, GraphFields> batch = gson.fromJson(json, stringGraphFieldsMap);
			gmi.putAll(batch);
		}

		for (Entry<String, GraphFields> gfr : gmi.entrySet()) {

			Set<String> allEventsForPage = new HashSet<String>();

			if (gfr != null && gfr.getValue() != null && gfr.getValue().getPosts() != null
					&& gfr.getValue().getPosts().getData() != null) {
				// Posts
				posts.addAll(gfr.getValue().getPosts().getData());

				// Events
				Set<String> eventsPostedByPage = findEventsInFeed(gfr.getValue().getPosts().getData());
				allEventsForPage.addAll(eventsPostedByPage);
			}

			if (gfr != null && gfr.getValue() != null && gfr.getValue().getEvents() != null
					&& gfr.getValue().getEvents().getData() != null) {
				Set<String> eventsCreatedByPage = mapGraphEventsToEventIds(gfr.getValue().getEvents().getData());
				allEventsForPage.addAll(eventsCreatedByPage);
			}

			pagesAndTheirEvents.put(gfr.getKey(), allEventsForPage);
		}

		return pagesAndTheirEvents;
	}

	/**
	 * Get the event ids from event objects
	 * 
	 * @return event ids
	 */
	Set<String> mapGraphEventsToEventIds(List<GraphEvent> events) {

		// Should be unique anyway
		Set<String> foundEvents = new TreeSet<String>();

		for (GraphEvent nextEvent : events) {
			foundEvents.add(nextEvent.getId());
		}

		return foundEvents;
	}

	/**
	 * Takes a /feed or /posts and searches for events attached or events linked
	 * in the post test
	 * 
	 * @param feed
	 * @return event ids
	 */
	Set<String> findEventsInFeed(List<GraphFeedItem> feed) {

		Set<String> foundEvents = new TreeSet<String>();

		// Regex for extracting the url from wall posts
		Pattern pattern = Pattern.compile("facebook.com/events/[0-9]*");
		Matcher matcher;

		for (GraphFeedItem item : feed) {

			// Check the post text
			if (item.getMessage() != null) {
				matcher = pattern.matcher(item.getMessage());
				while (matcher.find())
					foundEvents.add(matcher.group().substring(20));
			}

			// Check the post's attachments
			if (item.getLink() != null) {
				matcher = pattern.matcher(item.getLink());
				while (matcher.find())
					foundEvents.add(matcher.group().substring(20));
			}
		}

		return foundEvents;
	}

	/**
	 * 
	 * ?ids=131737309490,139957459378369&fields=events,feed
	 * 
	 * Multiple ID Read Requests
	 * 
	 * @see https://developers.facebook.com/docs/graph-api/using-graph-api/
	 * 
	 * @param ids
	 * @return a single Graph call query string without access token
	 */
	String buildGraphQueryStringForFeedAndEventsForMultiplePages(List<String> ids) {

		String query = null;
		try {
			query = "?ids=" + joiner.join(ids) + "&fields="
					+ URLEncoder.encode("posts{message,link,created_time},events{id}", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query;
	}

	/**
	 * Makes FB API calls in parallel
	 * 
	 * Created because MalformeddUrlException was being thrown when URLs were
	 * too long due to too many ids.
	 * 
	 * @see http
	 *      ://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app
	 *      -engine/
	 * @param ids
	 * @return
	 */
	List<String> batchAsyncUrlFetch(List<String> queries) {

		// TODO:
		// https://developers.facebook.com/docs/graph-api/making-multiple-requests/

		// GAE has a max_concurrent_requests option in appengine-web.xml
		// https://cloud.google.com/appengine/docs/java/config/appref
		List<List<String>> batches = Lists.partition(queries, 40);

		List<String> jsonList = new ArrayList<String>();

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		for (List<String> batch : batches) {

			List<Future<HTTPResponse>> asyncResponses = new ArrayList<Future<HTTPResponse>>();

			for (String query : batch) {

				try {
					String graphCall = fbGraphCallStub + fbApiVersion + "/" + query + "&access_token="
							+ serverAccessToken;

					URL graphcall = new URL(graphCall);

					Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
					asyncResponses.add(responseFuture);

				} catch (Exception e) {
					// MalformedURLException e, UnsupportedEncodingException e1
					e.printStackTrace();
				}
			}

			for (Future<HTTPResponse> future : asyncResponses) {

				try {
					// response = future.get();
					HTTPResponse response = future.get();

					jsonList.add(new String(response.getContent()));

				} catch (InterruptedException e) {
					System.out.println("InterruptedException: " + e);
				} catch (ExecutionException e) {
					System.out.println("ExecutionException: " + e);
				}
			}
		}

		return jsonList;
	}

}
