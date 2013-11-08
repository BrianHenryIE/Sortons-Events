package ie.sortons.events.server.servlet;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.FbEvent;
import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.server.fql.FqlEvent;
import ie.sortons.gwtfbplus.server.fql.FqlEvent.FqlEventItem;
import ie.sortons.gwtfbplus.server.fql.FqlEventMember;
import ie.sortons.gwtfbplus.server.fql.FqlEventMember.FqlEventMemberItem;
import ie.sortons.gwtfbplus.server.fql.FqlStream;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItem;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItemAttachment;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItemAttachmentAdapter;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItemAttachmentMediaItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

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

	// For logging
	PrintWriter out;
	private static final Logger log = Logger.getLogger(CollectorCron.class.getName());

	// The app's fb access token. Never to be used client-side.
	private String access_token = Config.getAppAccessToken();

	// FQL call pieces
	private String fqlcallstub = "https://graph.facebook.com/fql?q=";
	private String streamCallStub = "SELECT%20source_id%2C%20post_id%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment.media%20FROM%20stream%20WHERE%20source_id%20%3D%20"; // &access_token="+access_token;

	// Adapter added because of differences in structure between when there is an attachment or not in stream items.
	private Gson gson = new GsonBuilder().registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		List<ClientPageData> clients = getClientPageDataFromDatastore();

		out = response.getWriter();

		out.println("<pre>");

		for(ClientPageData client : clients) {

			Map<String, DiscoveredEvent> createdEvents = findCreatedEventsForClient(client);

			Map<String, DiscoveredEvent> postedEvents = findPostedEventsForClient(client);

			Map<String, DiscoveredEvent> discoveredEvents = mergeEventMaps(postedEvents, createdEvents);


			if( discoveredEvents.size() > 0 ) {

				Map<String, DiscoveredEvent> detailedEvents = findEventDetails(discoveredEvents);

				// TODO
				// This isn't enough... we need the events anytime they show up... in case we overwrite the
				// sourcelists and another source is forgotten
				List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class).filter("sourceLists", client.getClientPageId()).filter("fbEvent.startTimeDate >", getHoursAgoOrToday(12)).order("fbEvent.startTimeDate").list();

				out.println("Datastore events : " + dsEvents.size());

				for(DiscoveredEvent dsEvent : dsEvents){

					if( detailedEvents.containsKey( dsEvent.getFbEvent().getEid() ) ){

						// Add the datastore info to the discovered events list
						// if it changes resave, if it doesn't discard.

						if( !( detailedEvents.get(dsEvent.getFbEvent().getEid()).addSourceLists(dsEvent.getSourceLists()) || detailedEvents.get(dsEvent.getFbEvent().getEid()).addSourcePages(dsEvent.getSourcePages()) ) ){
							detailedEvents.remove(dsEvent.getFbEvent().getEid());
						}
					}			
				}

				if( detailedEvents.size() > 0 )				
					saveToDatastore(detailedEvents);

			}
		}

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


	private DiscoveredEvent mergeDiscoveredEvents(DiscoveredEvent d1, DiscoveredEvent d2){
		// TODO
		// Imlpement clone() on DiscoveredEvent

		DiscoveredEvent newDe = new DiscoveredEvent(d1.getFbEvent(), d1.getSourceLists(), d1.getSourcePages());
		newDe.addSourceLists(d2.getSourceLists());
		newDe.addSourcePages(d2.getSourcePages());

		return newDe;
	}


	private Map<String, DiscoveredEvent> mergeEventMaps(Map<String, DiscoveredEvent> map1, Map<String, DiscoveredEvent> map2) {

		if (map1 == null || map1.size() == 0)
			return map2;
		if (map2 == null || map2.size() == 0)
			return map1;

		Map<String, DiscoveredEvent> newMap = new HashMap<String, DiscoveredEvent>();

		for (String key : map1.keySet()) {

			if (map2.containsKey(key)) {			
				newMap.put(key, mergeDiscoveredEvents(map1.get(key), map2.get(key)));
			} else {
				newMap.put(key, map1.get(key));
			}			
		}

		for (String key : map2.keySet()) {

			if ( !newMap.containsKey(key) )		
				newMap.put(key, map2.get(key));

		}

		return newMap;
	}


	private List<ClientPageData> getClientPageDataFromDatastore() {
		Query<ClientPageData> sourceClientsQuery = ofy().load().type(
				ClientPageData.class);

		List<ClientPageData> sourceClients = new ArrayList<ClientPageData>();
		for (ClientPageData client : sourceClientsQuery) {
			sourceClients.add(client);
		}
		return sourceClients;

	}


	{
		ObjectifyService.register(ClientPageData.class);
		ObjectifyService.register(DiscoveredEvent.class);
	}


	private Map<String, FbPage> getClientsSourceIds(ClientPageData client) {

		Map<String, FbPage> sourceClientPages = new HashMap<String, FbPage>();

		for (FbPage page : client.getIncludedPages()) {
			sourceClientPages.put(page.getPageId(), page);
		}

		return sourceClientPages;
	}

	// TODO something with this.
	@SuppressWarnings("unused")
	private Map<String, DiscoveredEvent> findCreatedEventsForClients(List<ClientPageData> clients) {
		Map<String, DiscoveredEvent> createdEvents = new HashMap<String, DiscoveredEvent>();

		for (ClientPageData client : clients) {
			mergeEventMaps(createdEvents, findCreatedEventsForClient(client));
		}

		return createdEvents;

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
	private Map<String, DiscoveredEvent> findCreatedEventsForClient(ClientPageData client) {

		Map<String, DiscoveredEvent> createdEvents = new HashMap<String, DiscoveredEvent>();

		Map<String, FbPage> sourcePages = getClientsSourceIds(client);

		List<String> fqlCalls = new ArrayList<String>();
		for (String idList : getBrokenIdsLists( sourcePages.keySet() ) ) 
			fqlCalls.add("SELECT%20uid%2C%20eid%2C%20start_time%20FROM%20event_member%20WHERE%20start_time%20%3E%20now()%20AND%20uid%20IN%20(" + idList + ")"); 
		

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {

			// Convert the json string to java object
			FqlEventMember fqlEventMember = gson.fromJson(json, FqlEventMember.class);

			for (FqlEventMemberItem item : fqlEventMember.getData()) {
				createdEvents.put(item.getEid(), new DiscoveredEvent(item.getEid(), client.getClientPage().getPageId(), sourcePages.get(item.getUid())));
			}

		}

		out.println("Created events : " + createdEvents.size());
		System.out.println("Created events : " + createdEvents.size());

		if( createdEvents.size() > 0 ){
			for(DiscoveredEvent event : createdEvents.values()){
				out.println("Created event : " + client.getPageById(event.getSourcePages().get(0).getPageId()).getName() + " : " + event.getFbEvent().getEid());
			}
		}

		return createdEvents;
	}


	/**
	 * App Engine URLFetch was throwing a MalformedUrlException because the URLs were too long
	 * This method takes a full list of ids and splits them into comma separated strings with
	 * max 100 in each.
	 * 
	 * @param idSet
	 * @return
	 */
	public List<String> getBrokenIdsLists(Set<String> idSet){
		// MalformerlException was being throw when the url was too long. This
		// is keeping it short.
		int i = 0;
		List<String> nextIds = new ArrayList<String>();
		List<String> idLists = new ArrayList<String>();
		for (String s : idSet) {
			nextIds.add(s);
			i++;
			if ( i % 75 == 0 ) {
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
	 * @see http://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app-engine/
	 * @param fqlCalls
	 * @return
	 */
	private List<String> asyncFqlCall(List<String> fqlCalls) {

		List<String> json = new ArrayList<String>();

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		List<Future<HTTPResponse>> asyncResponses = new ArrayList<Future<HTTPResponse>>();

		for (String fql : fqlCalls) {

			try {

				URL graphcall = new URL(fqlcallstub + fql + "&access_token="
						+ access_token);

				Future<HTTPResponse> responseFuture = fetcher
						.fetchAsync(graphcall);
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
	 * Loop through the uids and make a graph call for each to get their stream
	 * Read the stream items for event URLs in the messages and the attachments
	 * 
	 * We can't query for multiple source ids in stream as an app
	 * http://stackoverflow.com/questions/12306564/accessing-stream-data-for-pages-using-application-access-token
	 * 
	 * 
	 * @param ids
	 * @see http://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app-engine/
	 */
	private Map<String, DiscoveredEvent> findPostedEventsForClient(ClientPageData client) {

		boolean efficientSearch = false;

		Map<String, DiscoveredEvent> postedEvents = null;

		String efficient = "";

		if (efficientSearch)
			efficient = "%20AND%20created_time%20%3E%20" + ((new DateTime().withTimeAtStartOfDay().getMillis() / 1000) - 3600);
		else
			efficient = "%20AND%20created_time%20%3E%20" + ((new DateTime().getMillis() / 1000) - 2592000); // month

		Map<String, FbPage> sourcePages = getClientsSourceIds(client);

		List<String> fqlCalls = new ArrayList<String>();

		for (FbPage sourcePage : sourcePages.values())
			fqlCalls.add(streamCallStub + sourcePage.getPageId() + "AND%20actor_id=" + sourcePage.getPageId() + efficient);

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {

			FqlStream fqlStream = gson.fromJson(json, FqlStream.class);

			// TODO check the string for "event" before bothering to process 

			if(fqlStream.getData().length>0)
				postedEvents = mergeEventMaps(postedEvents, findEventsInStreamPosts(fqlStream, client));
		}

		out.println("Posted events : " + postedEvents.size());
		// System.out.println("Posted events : " + postedEvents.size());

		return postedEvents;
	}


	private Map<String, DiscoveredEvent> findEventsInStreamPosts(FqlStream wallPosts, ClientPageData client) {

		Map<String, DiscoveredEvent> foundEvents = new HashMap<String, DiscoveredEvent>();

		// TODO use the simpler regex without a pattern and matcher ??
		// Regex for extracting the url from wall posts
		Pattern pattern = Pattern.compile("facebook.com/events/[0-9]*");
		Matcher matcher;

		FbPage sourcePage = null;

		if ((wallPosts != null) && (wallPosts.getData() != null) && (wallPosts.getData().length > 0)) {
			for (FqlStreamItem item : wallPosts.getData()) {

				if ( item.getActorId().equals(item.getSourceId()) ) {

					sourcePage = client.getPageById(item.getSourceId());

					// Read the message
					matcher = pattern.matcher(item.getMessage());
					while (matcher.find()) {

						// If hasn't been recorded yet
						if (!foundEvents.containsKey(matcher.group().substring(20))) {
							foundEvents.put( matcher.group().substring(20), new DiscoveredEvent(matcher.group().substring(20), client.getClientPage().getPageId(), sourcePage));

							// If the event has been recorded, but doesn't have this
							// source page
						} else if (!foundEvents.get(matcher.group().substring(20)).hasSourcePage(sourcePage)) {
							foundEvents.get(matcher.group().substring(20)).addSourcePage(sourcePage);
						}
					}

					// If we have an attachment and the attachment array is > 0...
					if ( (item.getAttachment() != null) && (item.getAttachment().getMedia().length > 0) ) {

						for (FqlStreamItemAttachmentMediaItem mediaitem : item.getAttachment().getMedia()) {

							if ((mediaitem.getHref() != null)) {

								matcher = pattern.matcher(mediaitem.getHref());
								// so we've found an event
								while (matcher.find()) {
									// If the event doesn't have a list yet...
									if (!foundEvents.containsKey(matcher.group().substring(20))) {
										foundEvents.put(matcher.group().substring(20), new DiscoveredEvent(matcher.group().substring(20), client.getClientPage().getPageId(), sourcePage));

										// If the event doesn't have this page
										// recorded yet...
									} else if (!foundEvents.get(matcher.group().substring(20)).hasSourcePage(sourcePage)) {
										foundEvents.get(matcher.group().substring(20)).addSourcePage(sourcePage);
									}
								}
							}
						}
					}


				}
			}
		}


		if (sourcePage != null && foundEvents.size() > 0) {
			out.println("Posted event  : " + sourcePage.getName() + " : " + foundEvents.size() + " : " + Joiner.on(",").join(foundEvents.keySet()));
		} else {
			// System.out.println("Posted event  : " + sourcePage.getName() + " : " + foundEvents.size());
		}

		return foundEvents;
	}




	/**
	 * Takes the list of event ids found on or by pages and gets their name,
	 * location etc.
	 * 
	 * @param discoveredEvents
	 * @return
	 */
	private Map<String, DiscoveredEvent> findEventDetails(Map<String, DiscoveredEvent> discoveredEvents) {

		Map<String, DiscoveredEvent> detailedEvents = new HashMap<String, DiscoveredEvent>();

		// The events we have now are all probably in the future.
		// Some came from pages' created events, which Facebook default to only
		// future events
		// The rest come from wall posts and we're only searching today's wall
		// posts, so people probably aren't posting old ones


		// Ask Facebook for their details

		List<String> fqlCalls = new ArrayList<String>();
		for (String idList : getBrokenIdsLists( discoveredEvents.keySet() ) ) 
			fqlCalls.add("SELECT%20eid%2C%20name%2C%20location%2C%20start_time%2C%20end_time%20FROM%20event%20WHERE%20eid%20IN%20(" + idList + ")%20AND%20start_time%3Enow()");

		List<String> jsons = asyncFqlCall(fqlCalls);

		for (String json : jsons) {

			try {
				FqlEventItem[] eventsDetails = gson.fromJson(json, FqlEvent.class).getData();
				out.println("Upcoming events: " + eventsDetails.length);

				for (FqlEventItem ei : eventsDetails) {
					if (discoveredEvents.containsKey(ei.getEid())) {

						FbEvent upcomingEvent = new FbEvent(ei.getEid(), ei.getName(), ei.getLocation(), ei.getStart_time(), ei.getEnd_time(), ei.getPic_square());

						DiscoveredEvent de = new DiscoveredEvent(discoveredEvents.get(ei.getEid()).getSourceLists(), discoveredEvents.get(ei.getEid()).getSourcePages(), upcomingEvent);

						detailedEvents.put(ei.getEid(), de);
					}
				}

			} catch (NullPointerException e) {
				System.out.println("NullPointerException : gson.fromJson(json, FqlEvent.class)");
				out.println(e);
				System.out.println(json);
			}
		}

		// Update discoveredEvents with the retrieved details

		out.println("Upcoming events:    " + detailedEvents.size());

		return detailedEvents;
	}

	/**
	 * @param discoveredEvents
	 */
	private void saveToDatastore(Map<String, DiscoveredEvent> discoveredEvents) {

		// TODO
		// Do this statically
		ObjectifyService.register(DiscoveredEvent.class);

		// Read discoveredEvents.values() from the datastore
		// merge everything
		// save everything

		// We need to pull out all the upcoming events we've found in case
		// another Client has also found them.

		// TODO
		// Will this fail if we're dealing with more than 30 events?
		//		List<DiscoveredEvent> datastoreEvents = ofy().load()
		//				.type(DiscoveredEvent.class)
		//				.filter("fbEvent.startTimeDate >", getHoursAgoOrToday(12))
		//				.filter("sourcePages.pageId in", discoveredEvents.keySet()) //sourceLists
		//				.list();
		//
		//		
		//		for (DiscoveredEvent dsEvent : datastoreEvents) {
		//			if (discoveredEvents.containsKey(dsEvent.getFbEvent().getEid())) {
		//				discoveredEvents.get(dsEvent.getFbEvent().getEid()).getFbEvent().mergeWithFbEvent(dsEvent.getFbEvent());
		//				discoveredEvents.get(dsEvent.getFbEvent().getEid()).addSourcePages(dsEvent.getSourcePages());
		//			}
		//		}

		// Now the discoveredEvents objects will all be merged (i.e. current)
		ofy().save().entities(discoveredEvents.values()).now();

		out.println("Saved/updated: " + discoveredEvents.size() + " events.");

	}

	@SuppressWarnings("unused")
	private String startTime() {
		// TODO
		// 1. Move this away because it's not being used
		// Set date to search from to yesterday? - No, that's dealt with on the
		// display side.
		// Use Jodatime?
		SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		String startTime = ISO8601FORMAT.format(new Date());
		// convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
		// - note the added colon for the Timezone
		startTime = startTime.substring(0, startTime.length() - 2) + ":"
				+ startTime.substring(startTime.length() - 2);

		return startTime;
	}

	// TODO : get this out of here... it's a double of the
	// UpcomingEventsEndpoint method
	private Date getHoursAgoOrToday(int hours) {

		Calendar calvar = Calendar.getInstance();

		int today = calvar.get(Calendar.DAY_OF_YEAR);

		// Subtract the specified number of hours
		calvar.add(Calendar.HOUR_OF_DAY, -hours);

		// Keep subtracting hours until we get to last night
		while (calvar.get(Calendar.DAY_OF_YEAR) == today) {
			calvar.add(Calendar.HOUR_OF_DAY, -1);
		}

		Date ago = calvar.getTime();

		return ago;
	}

}