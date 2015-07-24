package ie.sortons.events.server.servlet;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.ObjectifyService;

/**
 * Servlet which polls Facebook for events created by a list of ids and events
 * posted on their walls by them.
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
		ObjectifyService.register(ClientPageData.class);
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

	// For testing/mocking
	void setPrintWriter(PrintWriter out) {
		this.out = out;
	}

	private List<WallPost> wallPosts = new ArrayList<WallPost>();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// TODO: memcache
		List<ClientPageData> clients = ofy().load().type(ClientPageData.class).list();

		out = response.getWriter();

		out.println("<pre>");

		log.info("started");

		out.println(clients.size() + " clients");

		for (ClientPageData client : clients) {

			out.print(client.getName());

			Map<Long, DiscoveredEvent> createdEvents = findCreatedEventsForClient(client);

			Map<Long, DiscoveredEvent> postedEvents = findPostedEventsForClient(client);

			Map<Long, DiscoveredEvent> discoveredEvents = mergeEventMaps(postedEvents, createdEvents);

			if (discoveredEvents.size() > 0) {

				Map<Long, DiscoveredEvent> detailedEvents = findEventDetails(discoveredEvents);

				List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class)
						.filter("startTime >", getHoursAgoOrToday(12)).order("startTime").list();

				out.println("Datastore events : " + dsEvents.size());

				for (DiscoveredEvent dsEvent : dsEvents) {

					if (detailedEvents.containsKey(dsEvent.getEid())) {

						// Add the datastore info to the discovered events list
						// if it changes resave, if it doesn't discard.

						if (dsEvent.getSourceLists() == null || dsEvent.getSourceLists().size() == 0) {
							log.warning("NPE for dsevent " + dsEvent.getEid() + " CollectorCron ~129.");
							log.warning(gson.toJson(dsEvent));
							log.warning("dsEvent.getSourceLists().size() " + dsEvent.getSourceLists().size());
						}

						if (dsEvent.getSourcePages() == null || dsEvent.getSourcePages().size() == 0)
							log.warning("dsEvent.getSourcePages() " + dsEvent.getSourcePages());

						// If adding anything to the datastore's record would
						// change it,
						// merge the new record and the datastore one and save
						// it,
						// otherwise drop it from the list to be saved
						if (dsEvent.addSourceLists(detailedEvents.get(dsEvent.getEid()).getSourceLists())
								|| dsEvent.addSourcePages(detailedEvents.get(dsEvent.getEid()).getSourcePages())) {
							dsEvent.addSourceLists(detailedEvents.get(dsEvent.getEid()).getSourceLists());
							dsEvent.addSourcePages(detailedEvents.get(dsEvent.getEid()).getSourcePages());
							detailedEvents.put(dsEvent.getEid(), dsEvent); // DiscoveredEvent.merge(dsEvent,
																			// detailedEvents.get(dsEvent.getFbEvent().getEid())));
						} else
							detailedEvents.remove(dsEvent.getEid());

					}
				}

				if (detailedEvents.size() > 0)
					saveToDatastore(detailedEvents);
				// invalidate memcache
			}
		}

		ofy().save().entities(wallPosts).now();
		out.print("wall posts: " + wallPosts.size());
		wallPosts = null;
		wallPosts = new ArrayList<WallPost>();

		out.println("</pre>");
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 * 
	 * In case doPost is called, somehow.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * App Engine URLFetch was throwing a MalformedUrlException because the URLs
	 * were too long This method takes a full list of ids and splits them into
	 * comma separated strings with max 100 in each.
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
	 * Created because MalformeddUrlException was being thrown when URLs were
	 * too long due to too many ids.
	 * 
	 * @see http
	 *      ://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app
	 *      -engine/
	 * @param fqlCalls
	 * @return
	 */
	List<String> asyncFqlCall(List<String> fqlCalls) {

		List<String> json = new ArrayList<String>();

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

				json.add(new String(response.getContent()));

			} catch (InterruptedException e) {
				System.out.println("InterruptedException: " + e);
			} catch (ExecutionException e) {
				System.out.println("ExecutionException: " + e);
			}
		}

		return json;
	}


	/**
	 * We're querying via event_member table. As a page cannot be invited to an
	 * event, if it is a member then it was the creator.
	 * 
	 * Even using the app access token, we can query all ids at once.
	 * 
	 * If a page creates an event rsvp_status = "" If a profile does, odds are
	 * it will be attending
	 * 
	 * @param ids
	 * @return
	 */
	 Map<Long, DiscoveredEvent> findCreatedEventsForClient(ClientPageData client) {

		Map<Long, DiscoveredEvent> createdEvents = new HashMap<Long, DiscoveredEvent>();

		Map<Long, SourcePage> sourcePages = client.getIncludedIdsPagesMap();

		List<String> fqlCalls = new ArrayList<String>();
		for (String idList : getBrokenIdsLists(sourcePages.keySet()))
			fqlCalls.add("SELECT%20uid%2C%20eid%2C%20start_time%20FROM%20event_member%20WHERE%20start_time%20%3E%20now()%20AND%20uid%20IN%20("
					+ idList + ")");

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {

			Type fooType = new TypeToken<FbResponse<FqlEventMember>>() {
			}.getType();

			FbResponse<FqlEventMember> response = gson.fromJson(json, fooType);

			for (FqlEventMember item : response.getData())
				createdEvents.put(item.getEid(), new DiscoveredEvent(item.getUid(), client.getClientPageId(),
						sourcePages.get(item.getUid())));
		}

		out.println("Created events : " + createdEvents.size());

		if (createdEvents.size() > 0)
			for (DiscoveredEvent event : createdEvents.values())
				out.println("Created event : "
						+ client.getPageById(event.getSourcePages().get(0).getPageId()).getName() + " : "
						+ event.getEid());

		return createdEvents;
	}

	/**
	 * @param sourcePage
	 * @return FQL to retrieve last ~month of wall posts
	 */
	String getFqlStreamCall(SourcePage sourcePage) {
		String streamCallStub = "SELECT%20source_id%2C%20post_id%2C%20permalink%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment.media%2C%20created_time%2C%20type%20FROM%20stream%20WHERE%20source_id%20%3D%20"; // &access_token="+access_token;
		return streamCallStub + sourcePage.getPageId() + "%20AND%20actor_id%20=%20" + sourcePage.getPageId()
				+ "%20AND%20created_time%20%3E%20" + ((new DateTime().getMillis() / 1000) - 2592000);
	}

	/**
	 * Loop through the uids and make a fql call for each to get their stream.
	 * Read the stream items for event URLs in the messages and the attachments
	 * 
	 * We can't query for multiple source ids in stream as an app
	 * http://stackoverflow
	 * .com/questions/12306564/accessing-stream-data-for-pages
	 * -using-application-access-token
	 * 
	 * 
	 * @param ids
	 * @see http
	 *      ://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app
	 *      -engine/
	 */
	 Map<Long, DiscoveredEvent> findPostedEventsForClient(ClientPageData client) {

		Map<Long, DiscoveredEvent> postedEvents = new HashMap<Long, DiscoveredEvent>();

		Map<Long, SourcePage> sourcePages = client.getIncludedIdsPagesMap();

		List<String> fqlCalls = new ArrayList<String>();

		for (SourcePage sourcePage : sourcePages.values())
			fqlCalls.add(getFqlStreamCall(sourcePage));

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {
						
			Type fooType = new TypeToken<FbResponse<FqlStream>>() {
			}.getType();

			FbResponse<FqlStream> response = gson.fromJson(json, fooType);

			// TODO make it never be null here.
			if (response != null && response.getData() != null && response.getData().size() > 0) {

				ArrayList<FqlStream> pageStream = response.getData();

				List<FqlStream> pagesOwnPosts = findPostsByPageInStream(pageStream);

				if (pagesOwnPosts.size() > 0) {

					SourcePage sourcePage = client.getPageById(pagesOwnPosts.get(0).getActorId());

					// Do event
					List<Long> eventIds = findEventsInStreamPosts(pagesOwnPosts);

					Map<Long, DiscoveredEvent> justFoundEvents = new HashMap<Long, DiscoveredEvent>();

					// Create DiscoveredEvents
					for (Long eventId : eventIds) {
						DiscoveredEvent newDiscoveredEvent = new DiscoveredEvent(eventId, client.getClientPageId(),
								sourcePage);

						justFoundEvents.put(eventId, newDiscoveredEvent);

					}
					out.println("Posted event  : " + sourcePage.getName() + " : " + justFoundEvents.size() + " : "
							+ Joiner.on(",").join(justFoundEvents.keySet()));

					postedEvents = mergeEventMaps(postedEvents, justFoundEvents);

					// Do WallPosts
					wallPosts.addAll(findPostsByPageToSave(pagesOwnPosts, client));
				}
			}

		}

		out.println("Posted events : " + postedEvents.size());

		return postedEvents;
	}

	/**
	 * Filters a stream of posts from a page down to only those posted by the
	 * page TODO Maybe the FQL call could do this for us
	 * 
	 * @return
	 */
	List<FqlStream> findPostsByPageInStream(List<FqlStream> stream) {

		List<FqlStream> posts = new ArrayList<FqlStream>();

		for (FqlStream item : stream) {

			if (item.getActorId().equals(item.getSourceId())) {

				posts.add(item);

			}
		}

		return posts;
	}

	/**
	 * Filter down to post types 46, 66 or 80 that were made in the last 15
	 * minutes (the assumed cron run frequency) i.e. remove events and photos we
	 * already have events photos don't display properly in Embedded Posts
	 * 
	 * @param pagePosts
	 * @param client
	 * @return
	 */
	List<WallPost> findPostsByPageToSave(List<FqlStream> pagePosts, ClientPageData client) {

		// TODO but really, figure out what happens when we save twice with the
		// same id... datastore/objectify issue

		List<WallPost> theWallPosts = new ArrayList<WallPost>();

		for (FqlStream item : pagePosts) {

			if ((item.getType() == 46 || item.getType() == 66 || item.getType() == 80)
					&& item.getCreatedTime() > (((new Date().getTime()) / 1000) - (60 * 16))
					&& item.getActorId() != client.getClientPageId())
				theWallPosts.add(new WallPost(client.getClientPageId(), item.getPostId(), item.getCreatedTime(), item
						.getPermalink()));
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
	List<Long> findEventsInStreamPosts(List<FqlStream> stream) {

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
	 * Takes the list of event ids and gets the name, location etc. of those in
	 * the future.
	 * 
	 * @param discoveredEvents
	 * @return
	 */
	Map<Long, DiscoveredEvent> findEventDetails(Map<Long, DiscoveredEvent> discoveredEvents) {

		Map<Long, DiscoveredEvent> detailedEvents = new HashMap<Long, DiscoveredEvent>();

		List<String> fqlCalls = new ArrayList<String>();
		for (String idList : getBrokenIdsLists(discoveredEvents.keySet()))
			fqlCalls.add("SELECT%20eid%2C%20name%2C%20location%2C%20venue%2C%20start_time%2C%20end_time%2C%20is_date_only%20FROM%20event%20WHERE%20eid%20IN%20("
					+ idList + ")%20AND%20start_time%3Enow()");

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {

			// System.out.println(json);

			Type fooType = new TypeToken<FbResponse<FqlEvent>>() {
			}.getType();

			FbResponse<FqlEvent> response = gson.fromJson(json, fooType);
			ArrayList<FqlEvent> eventsDetails = response.getData();

			out.println("Upcoming events: " + eventsDetails.size());

			for (FqlEvent ei : eventsDetails) {
				if (discoveredEvents.containsKey(ei.getEid())) {

					DiscoveredEvent de = new DiscoveredEvent(ei, discoveredEvents.get(ei.getEid()).getSourceLists(),
							discoveredEvents.get(ei.getEid()).getSourcePages());

					detailedEvents.put(ei.getEid(), de);
				}
			}

		}

		out.println("Upcoming events:    " + detailedEvents.size());

		return detailedEvents;
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

	/**
	 * @param detailedEvents
	 */
	void saveToDatastore(Map<Long, DiscoveredEvent> detailedEvents) {

		// Now the discoveredEvents objects will all be merged (i.e. current)
		ofy().save().entities(detailedEvents.values()).now();

		out.println("Saved/updated: " + detailedEvents.size() + " events: "
				+ Joiner.on(",").join(detailedEvents.keySet()));
		log.info("Saved/updated: " + detailedEvents.size() + " events: " + Joiner.on(",").join(detailedEvents.keySet()));

	}

	Map<Long, DiscoveredEvent> mergeEventMaps(Map<Long, DiscoveredEvent> map1, Map<Long, DiscoveredEvent> map2) {

		if (map1 == null || map1.size() == 0)
			return map2;
		if (map2 == null || map2.size() == 0)
			return map1;

		Map<Long, DiscoveredEvent> newMap = new HashMap<Long, DiscoveredEvent>();

		for (Long key : map1.keySet())
			if (map2.containsKey(key)) {
				map1.get(key).addSourceLists(map2.get(key).getSourceLists());
				map1.get(key).addSourcePages(map2.get(key).getSourcePages());
				newMap.put(key, map1.get(key));
			} else
				newMap.put(key, map1.get(key));

		for (Long key : map2.keySet())
			if (!newMap.containsKey(key))
				newMap.put(key, map2.get(key));

		return newMap;
	}

}