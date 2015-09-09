package ie.sortons.events.server.cron;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.ObjectifyService;

import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.events.shared.WallPost;
import ie.sortons.gwtfbplus.shared.domain.FbResponse;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventDatesAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenue;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenueAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEventMember;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream.FqlStreamItemAttachment;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream.FqlStreamItemAttachmentAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream.FqlStreamItemAttachmentMediaItem;

/**
 * Servlet which polls Facebook for events created by a list of ids and events posted on their walls by them.
 * 
 * To be run regularly as a cron job.
 * 
 * 
 * @author brianhenry
 * 
 */
@SuppressWarnings("serial")
public class CollectorCron extends HttpServlet {

	private static final Logger log = Logger.getLogger(CollectorCron.class.getName());

	static {
		ObjectifyService.register(SourcePage.class);
		ObjectifyService.register(DiscoveredEvent.class);
		ObjectifyService.register(WallPost.class);
	}

	// For logging
	PrintWriter out;

	// The app's fb access token. Never to be used client-side.
	private String access_token = Config.getAppAccessTokenServer();

	// FQL call pieces
	private String fqlcallstub = "https://graph.facebook.com/fql?q=";

	// Adapter added because of differences in structure between when there is
	// an attachment or not in stream items.
	// Not an issue in newer FB SDK
	private Gson gson = new GsonBuilder()
			.registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter())
			.registerTypeAdapter(FqlEventVenue.class, new FqlEventVenueAdapter())
			.registerTypeAdapter(Date.class, new FqlEventDatesAdapter()).create();

	// For testing/mocking: could be removed via reflection?
	void setPrintWriter(PrintWriter out) {
		this.out = out;
	}

	// TODO caching
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		out = response.getWriter();

		// Get the list of source pages from the datastore
		List<SourcePage> sourcePages = ofy().load().type(SourcePage.class).list();

		// Fix for out of memory error on App Engine!
		List<List<SourcePage>> partitionedSourcePages = Lists.partition(sourcePages, 1000);

		// terrible fix for execution time exceeded!
		// Collections.shuffle(partitionedSourcePages);

		int wallsWithPostsCount = 0;
		int postedEventsCount = 0;
		int createdEventsCount = 0;
		int mergedEventsCount = 0;
		int futureEventsCount = 0;
		int wallPostsCount = 0;

		for (List<SourcePage> sourcePagePartition : partitionedSourcePages) {

			// Get the calls for reading their walls
			Map<SourcePage, String> fqlCalls = new HashMap<SourcePage, String>();

			for (SourcePage sourcePage : sourcePagePartition)
				fqlCalls.put(sourcePage, getFqlStreamCall(sourcePage));

			Map<SourcePage, String> jsonWalls = asyncFqlCall(fqlCalls);

			partitionedSourcePages = null;

			Map<SourcePage, List<FqlStream>> parsedWalls = parseJsonWalls(jsonWalls);

			wallsWithPostsCount += parsedWalls.size();

			List<DiscoveredEvent> eventsPosted = findEventsInStreams(parsedWalls);

			postedEventsCount += eventsPosted.size();

			List<DiscoveredEvent> eventsCreated = findCreatedEventsByPages(sourcePages);

			createdEventsCount += eventsCreated.size();

			// Now we have many lists of events including some duplicates and many
			// nulls. Merge them.

			List<DiscoveredEvent> mergedLists = new ArrayList<DiscoveredEvent>();
			mergedLists.addAll(eventsPosted);
			mergedLists.addAll(eventsCreated);
			// mergedLists.removeAll(Collections.singleton(null));

			List<DiscoveredEvent> allEvents = mergeEvents(mergedLists);

			mergedEventsCount += allEvents.size();

			// Some of the events don't have info, i.e. from the event_member table
			// Some will be in the past – the fql finding details will filter them
			// out

			List<DiscoveredEvent> eventsReady = findEventDetails(allEvents);

			futureEventsCount += eventsReady.size();

			// Check if there are changes from the datastore's existing events
			// Only save new/edited events

			List<DiscoveredEvent> datastoreEvents = ofy().load().type(DiscoveredEvent.class)
					.filter("startTime >", getHoursAgoOrToday(12)).order("startTime").list();

			Map<String, DiscoveredEvent> datastoreEventsMap = buildSearchMapDatastoreIds(datastoreEvents);

			for (DiscoveredEvent readyEvent : eventsReady)
				if (datastoreEventsMap.containsKey(readyEvent.getId())) {
					DiscoveredEvent datastoreEvent = datastoreEventsMap.get(readyEvent.getId());
					// Only save it if something has changed
					if (!readyEvent.equals(datastoreEvent))
						ofy().save().entity(readyEvent).now();
				} else
					// If it's not in the datastore already
					ofy().save().entity(readyEvent).now();

			// Get wall posts
			List<WallPost> wallPosts = findWallPostsToSave(parsedWalls);

			ofy().save().entities(wallPosts).now();

			wallPostsCount += wallPosts.size();

		}

		log.info(sourcePages.size() + " SourcePages in datastore.\n" + wallsWithPostsCount + " pages with wall posts.\n"
				+ postedEventsCount + " events posted on walls.\n" + createdEventsCount + " events created.\n"
				+ mergedEventsCount + " total events (duplicates merged).\n" + futureEventsCount + " future events.\n"
				+ wallPostsCount + " wall posts saved.\n");

		out.write(sourcePages.size() + " SourcePages in datastore.\n" + wallsWithPostsCount
				+ " pages with wall posts.\n" + postedEventsCount + " events posted on walls.\n" + createdEventsCount
				+ " events created.\n" + mergedEventsCount + " total events (duplicates merged).\n" + futureEventsCount
				+ " future events.\n" + wallPostsCount + " wall posts saved.\n");

	}

	// put getFqlStreamCall method here

	/**
	 * Makes FB API calls in parallel and returns a map of the <SourcePage,json>
	 * 
	 * Created because MalformeddUrlException was being thrown when URLs were too long due to too many ids.
	 * 
	 * @see http ://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app -engine/
	 * @param fqlCalls
	 * @return
	 */
	Map<SourcePage, String> asyncFqlCall(Map<SourcePage, String> fqlCalls) {

		Map<SourcePage, String> jsonList = new HashMap<SourcePage, String>();

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		Map<SourcePage, Future<HTTPResponse>> asyncResponses = new HashMap<SourcePage, Future<HTTPResponse>>();

		for (Map.Entry<SourcePage, String> entry : fqlCalls.entrySet()) {
			SourcePage sourcePage = entry.getKey();
			String fql = entry.getValue();

			try {
				URL graphcall = new URL(fqlcallstub + fql + "&access_token=" + access_token);

				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.put(sourcePage, responseFuture);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		for (Map.Entry<SourcePage, Future<HTTPResponse>> entry : asyncResponses.entrySet()) {
			SourcePage sourcePage = entry.getKey();
			Future<HTTPResponse> future = entry.getValue();

			try {
				// response = future.get();
				HTTPResponse response = future.get();

				jsonList.put(sourcePage, new String(response.getContent()));

			} catch (InterruptedException e) {
				System.out.println("InterruptedException: " + e);
			} catch (ExecutionException e) {
				System.out.println("ExecutionException: " + e);
			}
		}

		return jsonList;
	}

	Map<SourcePage, List<FqlStream>> parseJsonWalls(Map<SourcePage, String> jsonWalls) {

		Map<SourcePage, List<FqlStream>> streams = new HashMap<SourcePage, List<FqlStream>>();

		for (Map.Entry<SourcePage, String> entry : jsonWalls.entrySet()) {
			SourcePage sourcePage = entry.getKey();
			String jsonWall = entry.getValue();

			List<FqlStream> wall = parseJsonWall(jsonWall);
			if (wall.size() > 0)
				streams.put(sourcePage, wall);
		}

		return streams;
	}

	Type fqlStreamType = new TypeToken<FbResponse<FqlStream>>() {
	}.getType();

	private List<FqlStream> parseJsonWall(String jsonWall) {

		List<FqlStream> pagesOwnPosts = new ArrayList<FqlStream>();

		FbResponse<FqlStream> response = gson.fromJson(jsonWall, fqlStreamType);

		// TODO make it never be null here.
		if (response != null && response.getData() != null && response.getData().size() > 0) {

			List<FqlStream> pageStream = response.getData();

			pagesOwnPosts = findPostsByPageInStream(pageStream);
		}

		return pagesOwnPosts;
	}

	/**
	 * Filters a stream of posts from a page down to only those posted by the page TODO Maybe the FQL call could do this
	 * for us
	 * 
	 * @return
	 */
	List<FqlStream> findPostsByPageInStream(List<FqlStream> stream) {

		List<FqlStream> posts = new ArrayList<FqlStream>();

		for (FqlStream item : stream)
			if (item.getActorId().equals(item.getSourceId()))
				posts.add(item);

		return posts;
	}

	List<DiscoveredEvent> findEventsInStreams(Map<SourcePage, List<FqlStream>> walls) {

		List<DiscoveredEvent> justFoundEvents = new ArrayList<DiscoveredEvent>();

		for (Map.Entry<SourcePage, List<FqlStream>> entry : walls.entrySet()) {
			SourcePage sourcePage = entry.getKey();
			List<FqlStream> pagesOwnPosts = entry.getValue();

			if (pagesOwnPosts.size() > 0) {

				List<DiscoveredEvent> events = findEventsInStreamPosts(sourcePage, pagesOwnPosts);

				justFoundEvents.addAll(events);
			}
		}

		return justFoundEvents;
	}

	/**
	 * Searches post text and post attachments for events
	 * 
	 * @param Stream
	 *            of a single page's own stream posts
	 * @return A list of event ids
	 */
	List<DiscoveredEvent> findEventsInStreamPosts(SourcePage sourcePage, List<FqlStream> stream) {

		List<DiscoveredEvent> discoveredEvents = new ArrayList<DiscoveredEvent>();

		List<Long> foundEvents = findEventIdsInStreamPosts(stream);

		// Create DiscoveredEvents
		for (Long eventId : foundEvents) {
			DiscoveredEvent newDiscoveredEvent = new DiscoveredEvent(eventId, sourcePage);

			discoveredEvents.add(newDiscoveredEvent);

		}

		return discoveredEvents;
	}

	/**
	 * We're querying via event_member table. As a page cannot be invited to an event, if it is a member then it was the
	 * creator.
	 * 
	 * Even using the app access token, we can query all ids at once.
	 * 
	 * If a page creates an event rsvp_status = "" If a profile does, odds are it will be attending
	 * 
	 * @param ids
	 * @return
	 */
	List<DiscoveredEvent> findCreatedEventsByPages(List<SourcePage> sourcePages) {

		List<DiscoveredEvent> createdEvents = new ArrayList<DiscoveredEvent>();

		List<String> fqlCalls = new ArrayList<String>();

		Set<Long> pageIds = getIdsFromSourcePagesList(sourcePages);

		for (String idList : getBrokenIdsLists(pageIds))
			// fqlCalls.add("SELECT%20uid%2C%20eid%2C%20start_time%20FROM%20event_member%20WHERE%20start_time%20%3E%20now()%20AND%20uid%20IN%20("
			// + idList + ")");
			fqlCalls.add("SELECT%20eid%20FROM%20event_member%20WHERE%20start_time%20%3E%20now()%20AND%20uid%20IN%20("
					+ idList + ")");

		List<String> jsons = asyncFqlCall(fqlCalls);

		Type fqlEventMemberType = new TypeToken<FbResponse<FqlEventMember>>() {
		}.getType();

		Set<Long> eids = new HashSet<Long>();

		for (String json : jsons) {

			FbResponse<FqlEventMember> response = gson.fromJson(json, fqlEventMemberType);

			for (FqlEventMember item : response.getData())
				eids.add(item.getEid());

		}

		List<FqlEvent> fqlEvents = findEventDetailsById(eids);

		for (FqlEvent fqlEvent : fqlEvents)
			for (SourcePage sourcePage : sourcePages)
				if (fqlEvent.getCreator().equals(sourcePage.getPageId())) {
					DiscoveredEvent newEvent = new DiscoveredEvent(fqlEvent.getEid(), sourcePage);
					createdEvents.add(newEvent);
				}

		return createdEvents;
	}

	private Set<Long> getIdsFromSourcePagesList(List<SourcePage> sourcePages) {
		Set<Long> ids = new HashSet<Long>();
		for (SourcePage sourcePage : sourcePages)
			ids.add(sourcePage.getPageId());
		return ids;
	}

	/**
	 * Merges two lists of events, i.e. merges the event's SourcePages list
	 * 
	 * It searches to see if the event is found later in the list, if so it adds the SourcePage and continues, otherwise
	 * it adds the event to the list to be returned
	 * 
	 * @param list
	 * @return
	 */
	List<DiscoveredEvent> mergeEvents(List<DiscoveredEvent> list) {

		List<DiscoveredEvent> events = new ArrayList<DiscoveredEvent>();

		mainLoop: for (int i = 0; i < list.size(); i++) {
			for (int j = i + 1; j < list.size(); j++)
				if (list.get(i).getId().equals(list.get(j).getId())) {
					list.get(j).addSourcePages(list.get(i).getSourcePages());
					continue mainLoop;
				}
			events.add(list.get(i));
		}

		return events;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest ,
	 * javax.servlet.http.HttpServletResponse)
	 * 
	 * In case doPost is called, somehow.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		log.warning("Someone posted to CollectorCron servlet");
	}

	/**
	 * App Engine URLFetch was throwing a MalformedUrlException because the URLs were too long This method takes a full
	 * list of ids and splits them into comma separated strings with max 75 in each.
	 * 
	 * @param set
	 * @return
	 */
	List<String> getBrokenIdsLists(Set<Long> set) {
		// MalformerlException was being throw when the url was too long. This
		// is keeping it short.
		int i = 0;
		List<String> nextIds = new ArrayList<String>();
		List<String> idLists = new ArrayList<String>();
		for (Long s : set) {
			nextIds.add(s.toString());
			i++;
			if (i % 75 == 0) {
				idLists.add(Joiner.on(",").join(nextIds));
				nextIds = new ArrayList<String>();
			}
		}
		idLists.add(Joiner.on(",").join(nextIds));
		return idLists;
	}

	/**
	 * Created because MalformeddUrlException was being thrown when URLs were too long due to too many ids.
	 * 
	 * @see http ://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app -engine/
	 * @param fqlCalls
	 * @return
	 */
	List<String> asyncFqlCall(List<String> fqlCalls) {

		List<String> jsonList = new ArrayList<String>();

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		List<Future<HTTPResponse>> asyncResponses = new ArrayList<Future<HTTPResponse>>();

		for (String fql : fqlCalls) {
			try {
				URL graphcall = new URL(fqlcallstub + fql + "&access_token=" + access_token);

				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.add(responseFuture);

			} catch (MalformedURLException e) {
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

		return jsonList;
	}

	private String streamCallStub = "SELECT%20source_id%2C%20post_id%2C%20permalink%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment.media%2C%20created_time%2C%20type%20FROM%20stream%20WHERE%20source_id%20%3D%20"; // &access_token="+access_token;

	/**
	 * @param sourcePage
	 * @return FQL to retrieve last ~month of wall posts
	 */
	String getFqlStreamCall(SourcePage sourcePage) {
		String build = streamCallStub + sourcePage.getPageId() + "%20AND%20actor_id%20=%20" + sourcePage.getPageId()
				+ "%20AND%20created_time%20%3E%20" + ((new DateTime().getMillis() / 1000) - (30 * 24 * 60 * 60));
		// log.info(build);
		return build;
	}

	private List<WallPost> findWallPostsToSave(Map<SourcePage, List<FqlStream>> walls) {

		List<WallPost> wallPosts = new ArrayList<WallPost>();

		for (Map.Entry<SourcePage, List<FqlStream>> entry : walls.entrySet()) {
			SourcePage sourcePage = entry.getKey();
			List<FqlStream> pagesOwnPosts = entry.getValue();

			List<FqlStream> postsToSave = findPostsByPageToSave(pagesOwnPosts);

			for (FqlStream post : postsToSave) {

				WallPost wallPost = new WallPost(sourcePage.getClientId(), post.getPostId(), post.getCreatedTime(),
						post.getPermalink());

				wallPosts.add(wallPost);

			}
		}

		return wallPosts;
	}

	/**
	 * Filter down to post types 46, 66 or 80 that were made in the last 15 minutes (the assumed cron run frequency)
	 * i.e. remove events and photos we already have events photos don't display properly in Embedded Posts
	 * 
	 * @param pagePosts
	 * @param client
	 * @return
	 */
	List<FqlStream> findPostsByPageToSave(List<FqlStream> pagePosts) {

		List<FqlStream> theWallPosts = new ArrayList<FqlStream>();

		for (FqlStream item : pagePosts) {

			if ((item.getType() == 46 || item.getType() == 66 || item.getType() == 80)
					&& item.getCreatedTime() > (((new Date().getTime()) / 1000) - (60 * 16))
					&& item.getActorId() != item.getTargetId())
				theWallPosts.add(item);
		}

		return theWallPosts;
	}

	/**
	 * Searches post text and post attachments for events
	 * 
	 * @param Stream
	 *            of a single page's own stream posts
	 * @return A list of event ids
	 */
	List<Long> findEventIdsInStreamPosts(List<FqlStream> stream) {

		List<Long> foundEvents = new ArrayList<Long>();

		// Regex for extracting the url from wall posts
		Pattern pattern = Pattern.compile("facebook.com/events/[0-9]*");
		Matcher matcher;

		for (FqlStream item : stream) {

			// Check the post text
			matcher = pattern.matcher(item.getMessage());
			while (matcher.find())
				foundEvents.add(Long.parseLong(matcher.group().substring(20)));

			// Check the post's attachments
			if ((item.getAttachment() != null) && (item.getAttachment().getMedia().length > 0)) {

				for (FqlStreamItemAttachmentMediaItem mediaitem : item.getAttachment().getMedia())

					if (mediaitem.getHref() != null) {

						matcher = pattern.matcher(mediaitem.getHref());

						while (matcher.find())
							foundEvents.add(Long.parseLong(matcher.group().substring(20)));

					}
			}

		}

		return foundEvents;
	}

	/**
	 * Takes the list of DiscoveredEvents and gets the name, location etc. of those in the future.
	 * 
	 * @param discoveredEvents
	 * @return
	 */
	List<DiscoveredEvent> findEventDetails(List<DiscoveredEvent> discoveredEvents) {

		Set<Long> eventIds = getEventIdsFromDiscoveredEvents(discoveredEvents);

		List<FqlEvent> eventsDetails = findEventDetailsById(eventIds);

		List<DiscoveredEvent> futureEvents = new ArrayList<DiscoveredEvent>();

		// These must be in the future (filter in the FQL)
		for (FqlEvent ei : eventsDetails)
			for (DiscoveredEvent de : discoveredEvents)
				if (de.getEventId().equals(ei.getEid())) {
					de.setEvent(ei);
					futureEvents.add(de);
				}

		return futureEvents;
	}

	/**
	 * Queries Facebook (using FQL) for the event details The query filters to only events starting in the future.
	 * 
	 * @param eventIds
	 * @return
	 */
	private List<FqlEvent> findEventDetailsById(Set<Long> eventIds) {

		List<FqlEvent> eventsDetails = new ArrayList<FqlEvent>();

		List<String> fqlCalls = new ArrayList<String>();
		for (String idList : getBrokenIdsLists(eventIds))
			fqlCalls.add(
					"SELECT%20creator%2C%20eid%2C%20name%2C%20location%2C%20venue%2C%20start_time%2C%20end_time%2C%20is_date_only%20FROM%20event%20WHERE%20eid%20IN%20("
							+ idList + ")%20AND%20start_time%3Enow()");

		List<String> jsons = asyncFqlCall(fqlCalls);

		Type fooType = new TypeToken<FbResponse<FqlEvent>>() {
		}.getType();

		for (String json : jsons) {

			FbResponse<FqlEvent> response = gson.fromJson(json, fooType);
			eventsDetails.addAll(response.getData());

		}

		return eventsDetails;

	}

	private Map<String, DiscoveredEvent> buildSearchMapDatastoreIds(List<DiscoveredEvent> discoveredEvents) {

		Map<String, DiscoveredEvent> searchMap = new HashMap<String, DiscoveredEvent>();
		for (DiscoveredEvent event : discoveredEvents)
			searchMap.put(event.getId(), event);
		return searchMap;
	}

	private Set<Long> getEventIdsFromDiscoveredEvents(List<DiscoveredEvent> list) {
		Set<Long> ids = new HashSet<Long>();
		for (DiscoveredEvent event : list)
			ids.add(event.getEventId());
		return ids;
	}

	// TODO : get this out of here... it's a double of the
	// UpcomingEventsEndpoint method
	private Date getHoursAgoOrToday(int hours) {

		Calendar calvar = Calendar.getInstance();

		int today = calvar.get(Calendar.DAY_OF_YEAR);

		// Subtract the specified number of hours
		calvar.add(Calendar.HOUR_OF_DAY, -hours);

		// Keep subtracting hours until we get to last night
		while (calvar.get(Calendar.DAY_OF_YEAR) == today)
			calvar.add(Calendar.HOUR_OF_DAY, -1);

		Date ago = calvar.getTime();

		return ago;
	}

}