package ie.sortons.events.server;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.logging.Logger;

import com.googlecode.objectify.ObjectifyService;

import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.events.shared.WallPost;

public class DataStore {

	private static final Logger LOG = Logger.getLogger(DataStore.class.getName());

	static {
		ObjectifyService.register(SourcePage.class);
		ObjectifyService.register(DiscoveredEvent.class);
		ObjectifyService.register(WallPost.class);
	}

	/**
	 * @return All SourcePages in DataStore
	 */
	public List<SourcePage> getSourcePages() {

		List<SourcePage> sourcePages = ofy().load().type(SourcePage.class).list();

		return sourcePages;
	}

	public List<DiscoveredEvent> getUpcomingEvents() {

		List<DiscoveredEvent> datastoreEvents = ofy().load().type(DiscoveredEvent.class)
				.filter("startTime >", SEUtil.getHoursAgoOrToday(12)).order("startTime").list();

		LOG.info(datastoreEvents.size() + " - all upcoming DiscoveredEvents retrieved from DataStore");

		return datastoreEvents;
	}

	public void saveDiscoveredEvents(List<DiscoveredEvent> finalEvents) {

		// Why is this a loop?
		
		for (DiscoveredEvent readyEvent : finalEvents)
			ofy().save().entity(readyEvent).now();

	}

}
