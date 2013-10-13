package ie.sortons.events.server.servlet;


import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.FbConfig;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * Servlet which polls Facebook for events created by a list of ids
 * and events posted on their walls by them.
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
	private String access_token = FbConfig.getAppAccessToken();

	// FQL call pieces
	private String fqlcallstub = "https://graph.facebook.com/fql?q=";
	private String streamCallStub = "SELECT%20source_id%2C%20post_id%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment.media%20FROM%20stream%20WHERE%20source_id%20%3D%20"; // &access_token="+access_token;

	// Adapter added because of differences in structure between when
	// there is an attachment or not in stream items.
	private Gson gson = new GsonBuilder().registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Map<String, FbPage> sourcePages = getSourceIdsFromDatastore();

		Map<String, DiscoveredEvent> discoveredEvents = new HashMap<String, DiscoveredEvent>();

		out = response.getWriter();

		out.println("<pre>");

		discoveredEvents = findEventsCreatedByIds(sourcePages);
		
		discoveredEvents = mergeEventMaps(discoveredEvents, findEventsPostedByIdsAsync(sourcePages, false));
		
		discoveredEvents = mergeEventMaps(discoveredEvents, findEventDetails(discoveredEvents));
		
		saveToDatastore(discoveredEvents);

		out.println("</pre>");
		out.flush();
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * 
	 * In case doPost is called, somehow.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}


	private Map<String, DiscoveredEvent> mergeEventMaps(Map<String, DiscoveredEvent> map1, Map<String, DiscoveredEvent> map2){

		if( map1 == null || map1.size() == 0 ) return map2;
		if( map2 == null || map2.size() == 0 ) return map1;

		for(String map1key : map1.keySet()){
			
			if(map2.containsKey(map1key)){
				map2.get(map1key).getFbEvent().mergeWithFbEvent(map1.get(map1key).getFbEvent());
				map2.get(map1key).addSourcePages(map1.get(map1key).getSourcePages());
			}else{
				map2.put(map1key, map1.get(map1key));
			}
			
		}
		
		return map2;
	}


	private Map<String, FbPage> getSourceIdsFromDatastore(){ 

		Map<String, FbPage> sourceClientPages = new HashMap<String, FbPage>();

		// TODO
		// Static this
		ObjectifyService.register(ClientPageData.class);

		Query<ClientPageData> sourceClients = ofy().load().type(ClientPageData.class);

		for(ClientPageData client : sourceClients){

			for(FbPage page : client.getIncludedPages()) {
				sourceClientPages.put(page.getPageId(), page);
			}
		}

		return sourceClientPages;		
	}


	/**
	 * We're querying event_member table. As a page cannot be invited to an event, if it is a member
	 * then it was the creator. 
	 * 
	 * Even using the app access token, we can query all ids at once.
	 * 
	 * If a page creates an event rsvp_status = ""
	 * If a profile does, odds are it will be attending
	 * 
	 * @param ids
	 * @return 
	 */
	private Map<String, DiscoveredEvent> findEventsCreatedByIds(Map<String,FbPage> sourcePages){

		Map<String, DiscoveredEvent> createdEvents = new HashMap<String, DiscoveredEvent>();

		String json = "";

		String fql = "SELECT uid, eid, rsvp_status FROM event_member WHERE start_time > '" + startTime() + "' AND uid IN (" + Joiner.on(",").join(sourcePages.keySet()) + ")";

		try {
			URL url = new URL(fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				json += line;           	
			}
			reader.close();

			// Convert the json string to java object
			FqlEventMember fqlEventMember = gson.fromJson(json, FqlEventMember.class);

			// TODO Figure out why this is sometimes coming up null
			if( fqlEventMember!=null && fqlEventMember.getData()!=null ){
				for (FqlEventMemberItem item : fqlEventMember.getData()) {
					createdEvents.put(item.getEid(), new DiscoveredEvent(item.getEid(), sourcePages.get(item.getUid())));
				}
			} else {
				System.out.println("null in findEventsCreatedByIds(). No upcoming events? Json:");
				System.out.println(json);
			}

		} catch (MalformedURLException e) {
			System.out.println("catch (MalformedURLException e) :findEventsCreatedByIds");
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println("catch (IOException e) :findEventsCreatedByIds");
			System.out.println(e.getMessage());
		}

		out.println("Created events : " + createdEvents.size());
		
		return createdEvents;
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
	private Map<String, DiscoveredEvent> findEventsPostedByIdsAsync(Map<String, FbPage> sourcePages, boolean efficientSearch){

		// System.out.println("findEventsPostedByIdsAsync()");

		Map<String, DiscoveredEvent> postedEvents = null;

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		Map<FbPage, Future<HTTPResponse>> asyncResponses = new HashMap<FbPage, Future<HTTPResponse>>();

		String efficient = ""; 
				
		if(efficientSearch)
			efficient = "%20AND%20created_time%20%3E%20" + (new DateTime().withTimeAtStartOfDay().getMillis()/1000);
				
		for(FbPage sourcePage : sourcePages.values()) {

			try {

				URL graphcall = new URL(fqlcallstub + streamCallStub + sourcePage.getPageId() + efficient + "&access_token=" + access_token);

				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.put(sourcePage, responseFuture);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		int failures = 0;

		Iterator<Entry<FbPage, Future<HTTPResponse>>> it = asyncResponses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<FbPage, Future<HTTPResponse>> future = (Map.Entry<FbPage, Future<HTTPResponse>>)it.next();

			try {
				HTTPResponse response = future.getValue().get(); 

				FqlStream fqlStream = gson.fromJson(new String(response.getContent()), FqlStream.class);

				postedEvents = mergeEventMaps(postedEvents, findEventsInStreamPosts(fqlStream, future.getKey()));

			} catch (InterruptedException e) {
				System.out.println("InterruptedException " + future.getKey());
				System.out.println(e.toString());
				failures++;
			} catch (ExecutionException e) {
				System.out.println(future.getKey() + " -- " + e.toString());
				failures++;
			}
		}

		if(failures>0){
			log.info("failures: " + failures + " of " + sourcePages.size());
		}

		out.println("Posted events : " + postedEvents.size());
		
		return postedEvents;
	}


	private Map<String, DiscoveredEvent> findEventsInStreamPosts(FqlStream wallPosts, FbPage sourcePage) {

		Map<String, DiscoveredEvent> foundEvents = new HashMap<String, DiscoveredEvent>();

		// TODO use the simpler regex without a pattern and matcher ??
		// Regex for extracting the url from wall posts
		Pattern pattern = Pattern.compile("facebook.com/events/[0-9]*");
		Matcher matcher;

		if( (wallPosts != null) && (wallPosts.getData() != null) && (wallPosts.getData().length>0) ) {
			for (FqlStreamItem item : wallPosts.getData()) {

				// Read the message
				matcher = pattern.matcher(item.getMessage());
				while (matcher.find()) {

					// If hasn't been recorded yet
					if(!foundEvents.containsKey(matcher.group().substring(20))) {
						foundEvents.put(matcher.group().substring(20), new DiscoveredEvent(matcher.group().substring(20), sourcePage));

						// If the event has been recorded, but doesn't have this source page
					} else if(!foundEvents.get(matcher.group().substring(20)).hasSourcePage(sourcePage)){
						foundEvents.get(matcher.group().substring(20)).addSourcePage(sourcePage);
					}
				}

				// If we have an attachment and the attachment array is > 0...
				if((item.getAttachment()!=null)&&(item.getAttachment().getMedia().length>0)){

					for(FqlStreamItemAttachmentMediaItem mediaitem : item.getAttachment().getMedia()){

						if((mediaitem.getHref()!=null)){

							matcher = pattern.matcher(mediaitem.getHref());
							// so we've found an event
							while (matcher.find()) {
								// If the event doesn't have a list yet...
								if(!foundEvents.containsKey(matcher.group().substring(20))){
									foundEvents.put(matcher.group().substring(20), new DiscoveredEvent(matcher.group().substring(20), sourcePage));

									// If the event doesn't have this page recorded yet...
								} else if(!foundEvents.get(matcher.group().substring(20)).hasSourcePage(sourcePage)){
									foundEvents.get(matcher.group().substring(20)).addSourcePage(sourcePage);
								}
							}	
						}	
					}
				}
			}
		}

		if(foundEvents.size()>0)
			out.println("Posted events: " + sourcePage.getName() + " : " + foundEvents.size() + " : " + Joiner.on(",").join(foundEvents.keySet()));

		return foundEvents;
	}	


	/**
	 * Takes the list of event ids found on or by pages and gets their name, location etc.
	 * @param discoveredEvents 
	 * @return 
	 */
	private Map<String, DiscoveredEvent> findEventDetails(Map<String, DiscoveredEvent> discoveredEvents){

		out.println("Total events:    " + discoveredEvents.size());

		// The events we have now are all probably in the future.
		// Some came from pages' created events, which Facebook default to only future events
		// The rest come from wall posts and we're only searching today's wall posts, so people probably aren't posting old ones

		// TODO
		// It's maybe quicker here/less likely to timeout to check if the events already exist in the datastore before 
		// making a fb api call.

		// Ask Facebook for their details
		String eventDetailsFql  = "SELECT eid, name, location, start_time, end_time, pic_square FROM event WHERE eid IN (" + Joiner.on(",").join(discoveredEvents.keySet()) + ") AND start_time > '" + startTime() + "' ORDER BY start_time";

		Date before = new Date();

		String json = "";

		try {

			// TODO Make this easier on Facebook so it won't timeout.
			URL url = new URL(fqlcallstub + URLEncoder.encode(eventDetailsFql, "UTF-8") + "&access_token=" + access_token);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(25000);
			connection.setReadTimeout(25000);
			connection.setDoOutput(true);
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				json += line;           	
			}
			reader.close();

		} catch (MalformedURLException e) {
			System.out.println("catch (MalformedURLException e)");
			// ...
		} catch (IOException e) {
			System.out.println(e.toString());
			System.out.println(e.getCause());

			Date after2 = new Date();
			out.println(e.toString());
			out.println("Time taken: " + (after2.getTime()-before.getTime()));

		}

		FqlEventItem[] eventsDetails = gson.fromJson(json, FqlEvent.class).getData();

		try {
			out.println("Upcoming events: " + eventsDetails.length);
		} catch(NullPointerException e) {
			System.out.println("NullPointerException");
			out.println("NullPointerException");
		}

		// Update discoveredEvents with the retrieved details

		for(FqlEventItem ei : eventsDetails){
			if(discoveredEvents.containsKey(ei.getEid())){
				// Have to created the new FbEvent here because the merge method is also used client side where Gson makes no sense
				discoveredEvents.get(ei.getEid()).getFbEvent().mergeWithFbEvent(new FbEvent(ei.getEid(), ei.getName(), ei.getLocation(), ei.getStart_time(), ei.getEnd_time(), ei.getPic_square()));
			}
		}

		return discoveredEvents;
	}


	private void saveToDatastore(Map<String, DiscoveredEvent> discoveredEvents){

		// TODO
		// Do this statically
		ObjectifyService.register(DiscoveredEvent.class);


		// Read discoveredEvents.values() from the datastore
		// merge everything
		// save everything

		List<DiscoveredEvent> datastoreEvents = ofy().load().type(DiscoveredEvent.class).filter("fbEvent.startTimeDate >", getHoursAgoOrToday(12)).filter("sourcePages.pageId in", discoveredEvents.keySet()).list();

		for(DiscoveredEvent dsEvent : datastoreEvents){
			if(discoveredEvents.containsKey(dsEvent.getFbEvent().getEid())){
				discoveredEvents.get(dsEvent.getFbEvent().getEid()).getFbEvent().mergeWithFbEvent(dsEvent.getFbEvent());
				discoveredEvents.get(dsEvent.getFbEvent().getEid()).addSourcePages(dsEvent.getSourcePages());
			}
		}

		// Now the discoveredEvents objects will all be merged (i.e. current)
		ofy().save().entities(discoveredEvents.values()).now();

		out.println("Saved/updated: " + discoveredEvents.size() + " events.");

	}


	private String startTime() {
		// TODO
		// Set date to search from to yesterday? - No, that's dealt with on the display side.
		// Use Jodatime?
		SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String startTime = ISO8601FORMAT.format(new Date());
		//convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
		//- note the added colon for the Timezone
		startTime = startTime.substring(0, startTime.length()-2) + ":" + startTime.substring(startTime.length()-2);

		return startTime;
	}


	//	TODO : get this out of here... it's a double of the UpcomingEventsEndpoint method
	private Date getHoursAgoOrToday(int hours) {

		Calendar calvar = Calendar.getInstance();

		int today = calvar.get(Calendar.DAY_OF_YEAR);

		// Subtract the specified number of hours 
		calvar.add(Calendar.HOUR_OF_DAY, -hours);

		// Keep subtracting hours until we get to last night
		while (calvar.get(Calendar.DAY_OF_YEAR) == today){
			calvar.add(Calendar.HOUR_OF_DAY, -1);
		}

		Date ago = calvar.getTime();

		return ago;
	}

}