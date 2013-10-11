package ie.sortons.events.server.servlet;


import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.FbEvent;
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
import java.util.ArrayList;
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
	private String access_token = "470244209665073%7CrbUtPwZewT7KpkNinkKym5LDaHw";

	// FQL call pieces
	private String fqlcallstub = "https://graph.facebook.com/fql?q=";
	private String streamCallStub = "SELECT%20source_id%2C%20post_id%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment.media%20FROM%20stream%20WHERE%20source_id%20%3D%20"; // &access_token="+access_token;

	
	// Map<EventID, List<Pages event found from>> 
	private Map<String, ArrayList<String>> eventsWithSources;

	// Gson object to contain the details of events
	private FqlEvent eventsDetails;

	// Instance of Gson which will convert json to objects. 
	// Adapter added because of differences in structure between when
	// there is an attachment or not in stream items.
	private Gson gson = new GsonBuilder().registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Start the list fresh because if the servlet is still alive from the 
		// last run, it will still have that data.
		eventsWithSources = new HashMap<String, ArrayList<String>>();

		String[] sourceIds = getSourceIdsFromDatastore();
		
		out = response.getWriter();

		out.println("<pre>");

		findEventsCreatedByIds(sourceIds);

		findEventsPostedByIdsAsync(sourceIds);

		findEventDetails();

		saveToDatastore();

		out.println("</pre>");
		out.flush();
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// In case doPost is called, somehow.
		doGet(request, response);

	}
	
	
	private String[] getSourceIdsFromDatastore(){ 

		
		ArrayList<String> sourceClientPagesStrings = new ArrayList<String>();
	
		ObjectifyService.register(ClientPageData.class);

		Query<ClientPageData> sourceClientPages = ofy().load().type(ClientPageData.class);
		
		for(ClientPageData client : sourceClientPages){
			
			for(String pageId : client.getIncludedPageIds()) {
				sourceClientPagesStrings.add(pageId);
			}
		}

		String[] pagesArray = new String[sourceClientPagesStrings.size()];
		
		pagesArray = sourceClientPagesStrings.toArray(new String[sourceClientPagesStrings.size()]);
		
		return pagesArray;

	}


	/**
	 * Even using the app access token, we can query all ids at once. We're querying
	 * event_member table. As a page cannot be invited to an event, if it is a member
	 * then it was the creator. 
	 * 
	 * If a page creates an event rsvp_status = ""
	 * If a profile does, odds are it will be attending
	 * 
	 * @param ids
	 */
	private void findEventsCreatedByIds(String[] sourceIds){

		String json = "";

		String fql = "SELECT uid, eid, rsvp_status FROM event_member WHERE start_time > '" + startTime() + "' AND uid IN (" + Joiner.on(",").join(sourceIds) + ")";

		try {
			// System.out.println("Getting all page events: " + fql);
			URL url = new URL(fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				json += line;           	
			}
			reader.close();

		} catch (MalformedURLException e) {
			// System.out.println("catch (MalformedURLException e)");
			// ...
		} catch (IOException e) {
			// System.out.println("catch (IOException e)");
			// ...
		}

		// Convert the json string to java object
		FqlEventMember fqlEventMember = gson.fromJson(json, FqlEventMember.class);

		// System.out.println("Events by pages " + fqlEventMember.getData().length);

		// TODO Figure out why this is sometimes coming up null
		if(fqlEventMember!=null&&fqlEventMember.getData()!=null){

			out.println("Created events : " + fqlEventMember.getData().length);
			// log.info("Created events : " + fqlEventMember.getData().length);

			for (FqlEventMemberItem item : fqlEventMember.getData()) {

				// If this event hasn't yet been recorded in the list 
				if(!eventsWithSources.containsKey(item.getEid())){
					// Give it an entry with a list ready for its source
					eventsWithSources.put(item.getEid(), new ArrayList<String>());
				}
				// If the event doesn't have this page recorded yet...
				if(!eventsWithSources.get(item.getEid()).contains(item.getUid())){
					eventsWithSources.get(item.getEid()).add(item.getUid());
				}
			}
		} else {
			System.out.println("null in findEventsCreatedByIds(). Json:");
			System.out.println(json);
		}
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
	private void findEventsPostedByIdsAsync(String[] sourceIds){

		// System.out.println("findEventsPostedByIdsAsync()");

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		Map<String, Future<HTTPResponse>> asyncResponses = new HashMap<String, Future<HTTPResponse>>();

		DateTime today = new DateTime();
		long unixTimeToday = (today.withTimeAtStartOfDay().getMillis()/1000);

		for(String uid : sourceIds) {

			try {

				URL graphcall = new URL(fqlcallstub + streamCallStub + uid + "%20AND%20created_time%20%3E%20" + unixTimeToday + "&access_token=" + access_token);

				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.put(uid, responseFuture);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}

		int failures = 0;

		Iterator<Entry<String, Future<HTTPResponse>>> it = asyncResponses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Future<HTTPResponse>> future = (Map.Entry<String, Future<HTTPResponse>>)it.next();

			try {
				HTTPResponse response = future.getValue().get(); 
				processResponse(new String(response.getContent()), future.getKey());
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
			log.info("failures: " + failures + " of " + sourceIds.length);
		}

	}



	private void processResponse(String json, String uid) {

		// System.out.println("processResponse(String json, String uid)");

		// TODO use the simpler regex without a pattern and matcher
		// Regex for extracting the url from wall posts
		Pattern pattern = Pattern.compile("facebook.com/events/[0-9]*");
		Matcher matcher;

		int streamEvents = 0;

		//TODO
		//check that we've actually got data before trying to process it

		FqlStream fqlStream = gson.fromJson(json, FqlStream.class);
		// System.out.println("json cast to FqlStream object");


		// System.out.println("Wall posts found on " + uid +": "+ fqlStream.getData().length );
		// out.println("Wall posts found on " + uid +": "+ fqlStream.getData().length );

		if((fqlStream != null) && (fqlStream.getData() != null) && (fqlStream.getData().length>0)) {
			for (FqlStreamItem item : fqlStream.getData()) {

				// Read the message
				matcher = pattern.matcher(item.getMessage());
				while (matcher.find()) {

					// If the event doesn't have a list yet...
					if(!eventsWithSources.containsKey(matcher.group().substring(20))){
						eventsWithSources.put(matcher.group().substring(20), new ArrayList<String>());
					}
					// If the event doesn't have this page recorded yet...
					if(!eventsWithSources.get(matcher.group().substring(20)).contains(uid)){
						eventsWithSources.get(matcher.group().substring(20)).add(uid);

						streamEvents++;
					}
				}

				// Read the atachments
				if((item.getAttachment()!=null)&&(item.getAttachment().getMedia().length>0)){

					for(FqlStreamItemAttachmentMediaItem mediaitem : item.getAttachment().getMedia()){

						if((mediaitem.getHref()!=null)){

							matcher = pattern.matcher(mediaitem.getHref());
							while (matcher.find()) {
								// If the event doesn't have a list yet...
								if(!eventsWithSources.containsKey(matcher.group().substring(20))){
									eventsWithSources.put(matcher.group().substring(20), new ArrayList<String>());
								}
								// If the event doesn't have this page recorded yet...
								if(!eventsWithSources.get(matcher.group().substring(20)).contains(uid)){
									eventsWithSources.get(matcher.group().substring(20)).add(uid);

									streamEvents++;
								}
							}	
						}	
					}
				}
			}
		}

		// System.out.println("Events found on " + uid + ": " + events);
		// out.println("Events found on " + uid + ": " + events);

		if(streamEvents>0){
			out.println("Posted events: " + uid + " : " + streamEvents);
		}


	}

	
	/**
	 * Takes the list of event ids found on or by pages and gets their name, location etc.
	 */
	private void findEventDetails(){

		//System.out.println("findEventDetails()");

		out.println("Total events:    " + eventsWithSources.size());
		
		// The events we have now are all probably in the future.
		// Some came from pages' created events, which Facebook default to only future events
		// The rest come from wall posts and we're only searching today's wall posts, so people probably aren't posting old ones

		// TODO
		// It's probably quicker here/less likely to timeout to check if the events already exist in the datastore before 
		// making a fb api call.


		// Get the list of events from eventsWithSources
		// TODO This will fail with an empty list. (will it?)
		String eventIdsList = Joiner.on(",").join(eventsWithSources.keySet());


		// Ask Facebook for their details
		String eventDetailsFql  = "SELECT eid, name, location, start_time, end_time, pic_square FROM event WHERE eid IN (" + eventIdsList + ") AND start_time > '" + startTime() + "' ORDER BY start_time";

		out.println(eventDetailsFql);

		Date before = new Date();

		String json = "";

		try {
			// System.out.println("Getting details of all events");
			// TODO Make this easier on Facebook so it won't timeout.
			URL url = new URL(fqlcallstub + URLEncoder.encode(eventDetailsFql, "UTF-8") + "&access_token=" + access_token);
			//System.out.println(url);

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

			// System.out.println("json: "+json);

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

		eventsDetails = gson.fromJson(json, FqlEvent.class);

		try {
			out.println("Upcoming events: " + eventsDetails.getData().length);
		} catch(NullPointerException e) {

			System.out.println("NullPointerException");
			out.println("NullPointerException");

		}
		// log.info("Upcoming events: " + eventsDetails.getData().length);

	}


	private void saveToDatastore(){

		// TODO: only if there's something to save!

		// System.out.println("saveToDatastore()");

		// Save to Datastore
		ObjectifyService.register(FbEvent.class);


		// Counters for logs
		int newEvents = 0;
		int updatedEvents = 0;


		// Create a list for FbEvent entities from the events we know are in the future
		Map<String, FbEvent> fbEventEntities = new HashMap<String, FbEvent>();

		// and fill it with what we've found
		if((eventsDetails != null)&&(eventsDetails.getData() != null)&&(eventsDetails.getData().length>0)){


			for (FqlEventItem item : eventsDetails.getData()) {
				// Create datastore entity objects
				// Create FbEvent objects for the datastore
				fbEventEntities.put(item.getEid(), new FbEvent(item.getEid(), item.getName(), item.getLocation(), item.getStart_time(), item.getEnd_time(), item.getPic_square(), eventsWithSources.get(item.getEid())));
			}

			// Save the fbEventEntities to the datastore
			for(String eid : fbEventEntities.keySet()) {

				// System.out.println("Saving " + eid + " to datastore.");
				// // System.out.println("Searching datastore for: " + eid);

				// TODO
				// Batch this datastore call

				// Search the datastore for the event
				FbEvent existingEvent = ofy().load().type(FbEvent.class).filter("eid", eid).first().now();

				// TODO
				// This could be null if the ds call fails. Deal with it.
				if( existingEvent == null ){
					// If the event hasn't already been stored in the datastore
					// System.out.println("New event. Saving.");
					ofy().save().entity(fbEventEntities.get(eid)).now();
					newEvents++;
				} else {
					// If there's one already there
					// Add the current list of pages associated with the event

					if( existingEvent.addFbPages(eventsWithSources.get(eid)) ){
						// And save if it was updated
						ofy().save().entity(existingEvent).now();
						// System.out.println("Page list updated. Saving.");
						updatedEvents++;
					} else {
						// System.out.println("Page list unaltered.");
					}
				}
			}

			// Do a count of how many were new/saved.
			out.println("New events:      " + newEvents);
			out.println("Updated events:  " + updatedEvents);

			if(newEvents>0){
				log.info("Events added:    " + newEvents);
			}
			if(updatedEvents>0){
				log.info("Events updated:  " + updatedEvents);
			}


			// System.out.println("New events:     " + newEvents);
			// System.out.println("Updated events: " + updatedEvents);

		} // end of gson length check

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


}
