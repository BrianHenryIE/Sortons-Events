package ie.sortons.events.server.servlet;


import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.server.FbEvent;
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

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.ObjectifyService;


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
	private Map<String, ArrayList<String>> eventsWithSources = null;
	
	// Gson object to contain the details of events
	private FqlEvent eventsDetails;
		
	// Instance of Gson which will convert json to objects. 
	// Adapter added because of differences in structure between when
	// there is an attachment or not in stream items.
	private Gson gson = new GsonBuilder().registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();

	// TODO move into datastore
	private String[] sourceIdArray = {"118467031573937", "473152689386103", "447777375242973", "127545843963923", "239881959387896", "161778087221610", "126104739882", "175454489163498", "139957459378369", "278853952124958", "137692179658572", "173016349413959", "131131310360391", "202573026466256", "146517502081660", "316221368472305", "211370445611484", "202260986482121", "156373707999", "133490393390530", "136338279838233", "150292068388184", "337965352958721", "460882940611003", "286540920602", "131129730276987", "268895813228504", "141777375891020", "174621319227004", "282741040919", "34990218027", "83511592970", "409660182427219", "28513863752", "155115431976", "152283968129269", "180101625380523", "253158991449992", "133490393390530", "241174982589463", "174520041078", "203697936329506", "91134337138", "111658608910928", "396054886046", "141264382554448", "181396551877444", "208084049281702", "143041413568", "176275335721055", "108890152514874", "192366654091", "176384918291", "126606836145", "78735572813", "138793279477130", "115418846072", "2257757601", "148350735311367", "207531919313205", "344354688954336", "261859710608040", "332118046848072", "173887419321014", "415460538531087", "207087485969553", "254826547898099", "204916542348", "228161603938446", "141778049173767", "221771424506555", "211108608943007", "107542286070006", "168336586481", "421568037902568", "112316168824785", "348767665216313", "160971937270778", "116499245089367", "354215754630222", "115485713810", "102655619896504", "200252276677578", "299856336707985", "118381161532495", "209211902542558", "282209558476898", "171367331615", "143646962364105", "212287795548180", "246639295371049", "297966243582851", "126140080035", "165317087536", "418483748205965", "307219992653932", "375771609166382", "132009863517651", "109526719109431", "161510670543551", "115612350207", "207201872643691", "132833570085389", "108200809206512", "206311936133875", "316177085126933", "288191144552313", "174860662650484", "194358640696009", "206346189385219", "101900576539434", "313567945399874", "215914288544352", "157542607689069", "156646187812921", "156811441017133", "251121894916612", "168697359423", "157273654298850", "152874088057448", "195776623828731", "119514614775123", "109391145752624", "136370403167283", "459546714076305", "184945418245572", "102098431584", "459214567428021", "281450966645", "312695760434", "118723664854693", "107304295955006", "130080540384787", "165385645244", "201851259837577", "122785661107885", "298531403568454", "155413827805891", "111543909024337", "113594572035355", "155350824485272", "161670733944640", "118467031573937", "521394121234482", "136993096401894", "196489580433396", "139259739475312", "230980453691830", "226759940744267", "154038127958057", "115320111818273", "110728775624246", "235263863153669", "169336618997", "111590025562911", "423079787763984", "273389792679439", "100968186727371", "160859433936358", "128079370576571", "384088081662771", "100677603374187", "214806051864062", "467180833326125", "102917616412477", "175452312503085", "216264865098997", "149133168480230"};

	
	/**
	 * Takes and array of Strings {"abc", "def", "ghi"}
	 * and returns a String "abc, def, ghi"
	 * 
	 * @param strings
	 * @return
	 */
	private String arrayToCommaSeparatedList(String[] strings) {

		// Take the array of page ids and make it into a regular string
		StringBuilder sb = new StringBuilder();
		for (String id : strings) { 
		    if (sb.length() > 0) sb.append(',');
		    sb.append(id);
		}
		return sb.toString();
	}
	
	// TODO
	// This method should be called by the constructor and populate the field once!
	// And shouldn't be in here at all
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
	
	
	/**
	 * Even using the app access token, we can query all ids at once. We're querying
	 * event_member table. As a page cannot be invited to an event, if it is a member
	 * then it was the creator. 
	 * For profiles, they are members of events once invited which doesn't suit us.
	 * 
	 * If a page creates an event rsvp_status = ""
	 * If a profile does, odds are it will be 
	 * 
	 * @param ids
	 */
	private void findEventsCreatedByIds(){
		
		String json = "";
		
		String fql = "SELECT uid, eid, rsvp_status FROM event_member WHERE start_time > '" + startTime() + "' AND uid IN ("+arrayToCommaSeparatedList(sourceIdArray)+")";

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
	private void findEventsPostedByIdsAsync(){

		System.out.println("findEventsPostedByIdsAsync()");
		
		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		
		Map<String, Future<HTTPResponse>> asyncResponses = new HashMap<String, Future<HTTPResponse>>();

		for(String uid : sourceIdArray) {

			// graphcall = fqlcallstub + streamCallStub + uid + "%20AND%20created_time%20%3E%20" + unixTimeInPast(1) + "&access_token=" + access_token;
			// created_time > gives an oauth exception

			try {
				
				URL graphcall = new URL(fqlcallstub + streamCallStub + uid + "&access_token=" + access_token);
	            /*	            
	            HttpURLConnection connection = null;
				try {
					connection = (HttpURLConnection) graphcall.openConnection();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            connection.setConnectTimeout(25000);
	            connection.setReadTimeout(25000);
	            try {
					connection.connect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
/*
	            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	            	json += line;           	
	            }
	            reader.close();
	            //*/
	            
	            
				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.put(uid, responseFuture);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		/* Didn't help
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		int failures = 0;
		
		Iterator<Entry<String, Future<HTTPResponse>>> it = asyncResponses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Future<HTTPResponse>> future = (Map.Entry<String, Future<HTTPResponse>>)it.next();

			try {
				HTTPResponse response = future.getValue().get();
				// it.remove(); 
				processResponse(new String(response.getContent()), future.getKey());
				// System.out.println("processResponse " + future.getKey());
			} catch (InterruptedException e) {
				//System.out.println("InterruptedException " + future.getKey());
				// System.out.println(e.toString());
				failures++;
			} catch (ExecutionException e) {
				//System.out.println(future.getKey() + " -- " + e.toString());
				failures++;
			}
		}

		System.out.println("failures: " + failures);

	}

    
    

	private void processResponse(String json, String uid) {
		
		// System.out.println("processResponse(String json, String uid)");
		
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
		
		out.println("Posted events: " + uid + " : " + streamEvents);
		// log.info("Posted events:   " + streamEvents);
		
		
		
	}
	
	/**
	 * Takes the list of event ids found on or by pages and gets their name, location etc.
	 */
	private void findEventDetails(){
		
		System.out.println("findEventDetails()");
		
		// Get the list of events from eventsWithSources
		// TODO This will fail with an empty list.
		StringBuilder eidsb = new StringBuilder();
		for(String eid : eventsWithSources.keySet()) {
			 if (eidsb.length() > 0) eidsb.append(',');
			 eidsb.append(eid);
		}
		String eventIdsList = eidsb.toString();
		
		
		
		// Ask Facebook for their details
		String eventDetailsFql  = "SELECT eid, name, location, start_time, end_time, pic_square FROM event WHERE eid IN (" + eventIdsList + ") AND start_time > '" + startTime() + "' ORDER BY start_time";
		
		out.println(eventDetailsFql);
		
		Date before = new Date();

		String json = "";
        
		try {
			// System.out.println("Getting details of all events");
            // TODO Make this easier on Facebook so it won't timeout.
            URL url = new URL(fqlcallstub + URLEncoder.encode(eventDetailsFql, "UTF-8") + "&access_token=" + access_token);
            System.out.println(url);
            
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
        	
            // ...
        }
		
		Date after = new Date();
		
		System.out.println("Time taken: " + (after.getTime()-before.getTime()));
		
		// GSON!
		// Convert the json string to java object
		eventsDetails = gson.fromJson(json, FqlEvent.class);
		

		try {
			out.println("Upcoming events: " + eventsDetails.getData().length);
		} catch(NullPointerException e) {
			
			System.out.println("NullPointerException");
			
			
		}
		// log.info("Upcoming events: " + eventsDetails.getData().length);
		
	}
	
	
	
	private void saveToDatastore(){

		System.out.println("saveToDatastore()");
		
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
				FbEvent existingEvent = ofy().load().type(FbEvent.class).filter("eid", eid).first().get();
				
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
	
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		// System.out.println("Servlet execution... GET");
		
		
		// TODO
		// Not sure what was going on but it seemed the total events was accumulating. I guess
		// the servlet was staying alive and just rerunning?! 
		eventsWithSources = null;
		eventsWithSources = new HashMap<String, ArrayList<String>>();
		
		
		out = response.getWriter();
		
		out.println("<pre>");

		findEventsCreatedByIds();
		findEventsPostedByIdsAsync();
		
		out.println("Total events:    " + eventsWithSources.size());
		// log.info("Total events:    " + eventsWithSources.size());
		
		// Now we have the ids of all events posted to the wall and all events created by the pages.
		
		findEventDetails();
		saveToDatastore();
	 
		

		out.println("</pre>");
		out.flush();
	}
	

	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// System.out.println("Servlet execution... POST");

		// In case doPost is called, somehow.
		doGet(request, response);
		
	}
}
		