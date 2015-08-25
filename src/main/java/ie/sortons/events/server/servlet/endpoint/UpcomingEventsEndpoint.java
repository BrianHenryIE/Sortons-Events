package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.dto.DiscoveredEventsResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "upcomingEvents", version = "v1")
public class UpcomingEventsEndpoint {

	private static final Logger log = Logger.getLogger(UpcomingEventsEndpoint.class.getName());

	static {
		ObjectifyService.register(DiscoveredEvent.class);
	}

	public DiscoveredEventsResponse getList(@Named("id") Long clientPageId) {

		List<DiscoveredEvent> upcomingEvents = new ArrayList<DiscoveredEvent>();

		Date now = new Date();

		DiscoveredEventsResponse dto = new DiscoveredEventsResponse();

		log.info("Searching for events for " + clientPageId);

		List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class).filter("clientId", clientPageId)
				.filter("startTime >", getHoursAgoOrToday(12)).order("startTime").list();

		for (DiscoveredEvent datastoreEvent : dsEvents) {

			log.info("EVENT: " + datastoreEvent.getName());

			if ((datastoreEvent.getEndTime() == null) || (datastoreEvent.getEndTime().after(now))) {

				DiscoveredEvent de = new DiscoveredEvent(datastoreEvent, datastoreEvent.getSourcePages());

				upcomingEvents.add(de);

			}
		}

		dto.setData(upcomingEvents);
		return dto;
		// return upcomingEvents;
	}

	public static Date getHoursAgoOrToday(int hours) {

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