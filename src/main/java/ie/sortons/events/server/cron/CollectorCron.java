package ie.sortons.events.server.cron;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;

import ie.sortons.events.server.DataStore;
import ie.sortons.events.server.Facebook;
import ie.sortons.events.server.SEUtil;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.events.shared.WallPost;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphEvent;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphFeedItem;

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

	private static final Logger LOG = Logger.getLogger(CollectorCron.class.getName());

	DataStore dataStore = new DataStore();

	String serverAccessToken = Config.getAppAccessTokenServer();
	String apiVersion = Config.getFbApiVersion();
	Facebook facebook = new Facebook(serverAccessToken, apiVersion);

	// TODO: Move all variables to fields with Java doc
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		// Get the list of source pages from the datastore
		List<SourcePage> sourcePages = dataStore.getSourcePages();

		LOG.info(sourcePages.size() + " SourcePages in DataStore");

		out.write("\n<br/>" + sourcePages.size() + " SourcePages in DataStore");

		Map<Long, List<SourcePage>> clientPages = new HashMap<Long, List<SourcePage>>();

		/**
		 * <fbPageId, <clientId>>
		 */
		Map<String, Set<Long>> pagesAndClients = new HashMap<String, Set<Long>>();

		// Set<String> sourcePageFbIds = new HashSet<String>();
		List<String> sourcePageFbIds = new ArrayList<String>();

		// <FbPageId, SourcePage>
		Map<String, SourcePage> sourcePageMap = new HashMap<String, SourcePage>();

		for (SourcePage sp : sourcePages) {

			if (!clientPages.containsKey(sp.getClientId()))
				clientPages.put(sp.getClientId(), new ArrayList<SourcePage>());
			clientPages.get(sp.getClientId()).add(sp);

			sourcePageMap.put(sp.getFbPageId(), sp);
			sourcePageFbIds.add(sp.getFbPageId());

			if (!pagesAndClients.containsKey(sp.getFbPageId()))
				pagesAndClients.put(sp.getFbPageId(), new HashSet<Long>());
			pagesAndClients.get(sp.getFbPageId()).add(sp.getClientId());

		}

		LOG.info(clientPages.size() + " client pages (groups) in DataStore");
		out.write("\n<br/>" + clientPages.size() + " client pages (groups) in DataStore");
		clientPages = null;

		LOG.info(sourcePageFbIds.size() + " unique Facebook Pages");
		out.write("\n<br/>" + sourcePageFbIds.size() + " unique Facebook Pages");

		// Fix for out of memory error on App Engine!
		// ArrayList<String> sourcePageFbIdsList = new
		// ArrayList<String>(sourcePageFbIds);
		List<List<String>> partitionedSourcePages = Lists.partition(sourcePageFbIds, 50);
		sourcePageFbIds = null;

		out.write("<br/>\npartitionedSourcePages.size()" + partitionedSourcePages.size());
		out.write("<br/>\npartitionedSourcePages.get(0).size() " + partitionedSourcePages.get(0).size());

		// Get all the events id for all the pages! whoop!
		// <FbPageId, <EventID>>
		Map<String, Set<String>> pagesAndEvents = facebook.getEventIdsPerPage(partitionedSourcePages);
		partitionedSourcePages = null;

		Set<String> allEventIds = new HashSet<String>();
		for (Set<String> eventIds : pagesAndEvents.values())
			allEventIds.addAll(eventIds);

		LOG.info(allEventIds.size() + " Facebook Events found");
		out.write("\n<br/>" + allEventIds.size() + " Facebook Events found");

		// But we already have lots of these saved!
		List<DiscoveredEvent> existingDataStoreEvents = dataStore.getUpcomingEvents();

		LOG.info(existingDataStoreEvents.size() + " Facebook Events already in DataStore");
		out.write("\n<br/>" + existingDataStoreEvents.size() + " Facebook Events already in DataStore");

		// Wrangle the existing DataStore events into a structure we can merge
		// with
		// <ClientId <FbEventId, <FbPageId>>>
		Map<Long, Map<String, Set<String>>> discoveredEventsMap = new HashMap<Long, Map<String, Set<String>>>();
		Map<String, DiscoveredEvent> dataStoreEventsMap = new HashMap<String, DiscoveredEvent>();
		for (DiscoveredEvent de : existingDataStoreEvents) {
			dataStoreEventsMap.put(de.getEventId(), de);

			Long clientId = de.getClientId();
			if (!discoveredEventsMap.containsKey(clientId))
				discoveredEventsMap.put(clientId, new HashMap<String, Set<String>>());

			String fbEventId = de.getEventId();
			allEventIds.add(fbEventId);

			Set<String> fbPageIds = new HashSet<String>();
			for (SourcePage sourcePage : de.getSourcePages()) {
				// TODO I think possible null because of earlier coding errors
				// (renaming property) putting null into the datastore.
				if (sourcePage != null && sourcePage.getFbPageId() != null)
					fbPageIds.add(sourcePage.getFbPageId());
			}

			if (!discoveredEventsMap.get(clientId).containsKey(fbEventId))
				discoveredEventsMap.get(clientId).put(fbEventId, fbPageIds);
			else
				discoveredEventsMap.get(clientId).get(fbEventId).addAll(fbPageIds);
		}
		existingDataStoreEvents = null;

		// Look through the SourcePages to see if we found anything new and add
		// it
		for (SourcePage sp : sourcePages) {

			String fbPageId = sp.getFbPageId();

			if (!pagesAndEvents.containsKey(fbPageId))
				continue;

			Long clientId = sp.getClientId();
			if (!discoveredEventsMap.containsKey(clientId))
				discoveredEventsMap.put(clientId, new HashMap<String, Set<String>>());

			for (String fbEventId : pagesAndEvents.get(fbPageId)) {
				if (!discoveredEventsMap.get(clientId).containsKey(fbEventId))
					discoveredEventsMap.get(clientId).put(fbEventId, new HashSet<String>());
				discoveredEventsMap.get(clientId).get(fbEventId).add(fbPageId);
			}
		}
		pagesAndEvents = null;

		// Get up to date data // TODO What happens when we graph call on a
		// deleted event?
		// <EventId, GraphEvent>

		Map<String, GraphEvent> graphEvents = facebook.getGraphEventsFromEventIds(allEventIds);
		allEventIds = null;

		LOG.info(graphEvents.size() + " events w/details retrieved from Facebook");
		out.write("\n<br/>" + graphEvents.size() + " events w/details retrieved from Facebook");

		List<DiscoveredEvent> finalEvents = new ArrayList<DiscoveredEvent>();

		Date upcomingTime = SEUtil.getHoursAgoOrToday(12);

		// Transcribe from id numbers to objects
		// <ClientId, <EventId, <FbPageId>>>
		for (Entry<Long, Map<String, Set<String>>> clientEntry : discoveredEventsMap.entrySet()) {

			Long clientId = clientEntry.getKey();

			//
			Map<String, Set<String>> clientEvents = clientEntry.getValue();

			for (Entry<String, Set<String>> clientEvent : clientEvents.entrySet()) {
				Set<SourcePage> eventSourcePages = new HashSet<SourcePage>();
				for (String sourcePageFbPageId : clientEvent.getValue()) {
					SourcePage sourcePageDetails = sourcePageMap.get(sourcePageFbPageId);

					// Some sourcepgeid/ fbid mix up here
					if (sourcePageDetails == null) {
						LOG.info(sourcePageFbPageId + " not found in sourcePageMap");
						LOG.info(sourcePageMap.keySet().toArray().toString());
					}

					SourcePage sourcePageForClient = new SourcePage(sourcePageDetails, clientId);

					eventSourcePages.add(sourcePageForClient);
				}
				// I think it can be null when an event id was found in a post
				// but the event has been deleted
				GraphEvent graphEvent = graphEvents.get(clientEvent.getKey());
				if (graphEvent != null && graphEvent.getStart_time().after(upcomingTime))
					finalEvents.add(new DiscoveredEvent(graphEvent, clientId, eventSourcePages));

			}
		}
		discoveredEventsMap = null;
		graphEvents = null;

		LOG.info(finalEvents.size() + " upcoming events built");
		out.write("\n<br/>" + finalEvents.size() + " upcoming events built");

		List<DiscoveredEvent> saveEvents = new ArrayList<DiscoveredEvent>();

		// Only save new/updated event
		for (DiscoveredEvent de : finalEvents)
			if (dataStoreEventsMap.get(de.getEventId()) == null) {
				saveEvents.add(de);
			} else if (!dataStoreEventsMap.get(de.getEventId()).equals(de))
				saveEvents.add(de);
		dataStoreEventsMap = null;
		finalEvents = null;

		LOG.info(saveEvents.size() + " new/updated events to save");
		out.write("\n<br/>" + saveEvents.size() + " new/updated events to save");

		dataStore.saveDiscoveredEvents(saveEvents);
		saveEvents = null;

		List<GraphFeedItem> posts = facebook.getPosts();
		facebook.nullPosts();

		Date anHourAgo = SEUtil.getHoursAgo(1);

		// TODO: pull out the recent posts and don't save duplicates
		
		List<WallPost> wallPosts = new ArrayList<WallPost>();
		for (GraphFeedItem post : posts) {
			String fbPageId = post.getId().split("_")[0];
			String fbPostId = post.getId().split("_")[1];
			long date = post.getCreated_time().getTime();
			String url = "https://www.facebook.com/" + fbPageId + "/posts/" + fbPostId;

			if (post.getCreated_time().after(anHourAgo))
				for (Long clientId : pagesAndClients.get(fbPageId)) {
					WallPost wallPost = new WallPost(clientId, post.getId(), date, url);
					wallPosts.add(wallPost);
				}
		}

		LOG.info(wallPosts.size() + " wall posts to save");
		out.write("\n<br/>" + wallPosts.size() + " wall posts to save");

		dataStore.saveWallPosts(wallPosts);
		wallPosts = null;
	}

}
