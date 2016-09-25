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
import ie.sortons.gwtfbplus.shared.domain.graph.GraphEvent;

// (old) terrible fix for execution time exceeded!
// Collections.shuffle(partitionedSourcePages);

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

	// TODO: Filter to events in the future when saving
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		// Get the list of source pages from the datastore
		List<SourcePage> sourcePages = dataStore.getSourcePages();

		LOG.info(sourcePages.size() + " SourcePages in DataStore");

		out.write("\n<br/>" + sourcePages.size() + " SourcePages in DataStore");

		Map<Long, List<SourcePage>> clientPages = new HashMap<Long, List<SourcePage>>();

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
		}

		LOG.info(clientPages.size() + " client pages (groups) in DataStore");
		out.write("\n<br/>" + clientPages.size() + " client pages (groups) in DataStore");

		LOG.info(sourcePageFbIds.size() + " unique Facebook Pages");
		out.write("\n<br/>" + sourcePageFbIds.size() + " unique Facebook Pages");

		// Fix for out of memory error on App Engine!
		// ArrayList<String> sourcePageFbIdsList = new
		// ArrayList<String>(sourcePageFbIds);
		List<List<String>> partitionedSourcePages = Lists.partition(sourcePageFbIds, 50);

		out.write("<br/>\npartitionedSourcePages.size()" + partitionedSourcePages.size());
		out.write("<br/>\npartitionedSourcePages.get(0).size() " + partitionedSourcePages.get(0).size());

		// Get all the events id for all the pages! whoop!
		// <FbPageId, <EventID>>
		Map<String, Set<String>> pagesAndEvents = facebook.getEventIdsPerPage(partitionedSourcePages);

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

		// Get up to date data // TODO What happens when we graph call on a
		// deleted event?
		// <EventId, GraphEvent>

		Map<String, GraphEvent> graphEvents = facebook.getGraphEventsFromEventIds(allEventIds);

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

		LOG.info(finalEvents.size() + " upcoming events built");
		out.write("\n<br/>" + finalEvents.size() + " upcoming events built");
		
		List<DiscoveredEvent> saveEvents = new ArrayList<DiscoveredEvent>();

		System.out.println("dataStoreEventsMap.size() " + dataStoreEventsMap.size());
		System.out.println(dataStoreEventsMap);
		
		// Only save new/updated event
		for (DiscoveredEvent de : finalEvents) {
			
			System.out.println("searching " + de.getEventId());
			
			if (dataStoreEventsMap.get(de.getEventId()) == null) {
				saveEvents.add(de);
				System.out.println("not in ds de map");
			} else if (!dataStoreEventsMap.get(de.getEventId()).equals(de)) {
				saveEvents.add(de);
				System.out.println("unequal");
			}
		}
		
		LOG.info(saveEvents.size() + " new/updated events to save");
		out.write("\n<br/>" + saveEvents.size() + " new/updated events to save");

		dataStore.saveDiscoveredEvents(finalEvents);

		// Get the calls for reading their walls
		// Map<SourcePage, String> fqlCalls = new HashMap<SourcePage, String>();
		//
		// for (SourcePage sourcePage : sourcePagePartition)
		// fqlCalls.put(sourcePage, getFqlStreamCall(sourcePage));
		//
		// Map<SourcePage, String> jsonWalls = asyncFqlCall(fqlCalls);
		//
		// partitionedSourcePages = null;
		//
		// Map<SourcePage, List<FqlStream>> parsedWalls =
		// parseJsonWalls(jsonWalls);
		//
		// wallsWithPostsCount += parsedWalls.size();
		//
		// List<DiscoveredEvent> eventsPosted =
		// findEventsInStreams(parsedWalls);
		//
		// postedEventsCount += eventsPosted.size();
		//
		// List<DiscoveredEvent> eventsCreated =
		// findCreatedEventsByPages(sourcePages);
		//
		// createdEventsCount += eventsCreated.size();

		// Now we have many lists of events including some duplicates and many
		// nulls. Merge them.

		// List<DiscoveredEvent> mergedLists = new ArrayList<DiscoveredEvent>();
		// mergedLists.addAll(eventsPosted);
		// mergedLists.addAll(eventsCreated);
		// mergedLists.removeAll(Collections.singleton(null));

		// List<DiscoveredEvent> allEvents = mergeEvents(mergedLists);

		// Some of the events don't have info, i.e. from the event_member table
		// Some will be in the past – the fql finding details will filter them
		// out

		// futureEventsCount += eventsReady.size();

		// Check if there are changes from the datastore's existing events
		// Only save new/edited events

		// Get upcoming datastore DiscoveredEvents

		// Get a list of them by id

		// Check the events we've just found
		// if it's new, save it
		// if it's existing, make sure the source pages are all there

		// Get wall posts

		// Save wall posts

		// Record number of wall posts for info logging

		// }
		//
		// LOG.info(sourcePages.size() + " SourcePages in datastore.\n" +
		// wallsWithPostsCount + " pages with wall posts.\n"
		// + postedEventsCount + " events posted on walls.\n" +
		// createdEventsCount + " events created.\n"
		// + mergedEventsCount + " total events (duplicates merged).\n" +
		// futureEventsCount + " future events.\n"
		// + wallPostsCount + " wall posts saved.\n");

		// out.write("\n<br/> sourcePages.size() + " SourcePages in
		// datastore.\n" +
		// wallsWithPostsCount
		// + " pages with wall posts.\n" + postedEventsCount + " events posted
		// on walls.\n" + createdEventsCount
		// + " events created.\n" + mergedEventsCount + " total events
		// (duplicates merged).\n" + futureEventsCount
		// + " future events.\n" + wallPostsCount + " wall posts saved.\n");

	}

}
