package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

import ie.sortons.events.server.SEUtil;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.dto.DiscoveredEventsResponse;

@Api(name = "upcomingEvents", version = "v1")
public class UpcomingEventsEndpoint {

	private static final Logger LOG = Logger.getLogger(UpcomingEventsEndpoint.class.getName());

	static {
		ObjectifyService.register(DiscoveredEvent.class);
	}

	public DiscoveredEventsResponse getList(@Named("id") Long clientPageId) {

		List<DiscoveredEvent> upcomingEvents = new ArrayList<DiscoveredEvent>();

		Date now = new Date();

		DiscoveredEventsResponse dto = new DiscoveredEventsResponse();

		// time to move this into a db class
		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		if (clientPageData == null) { // TODO handle exception!
			LOG.info("cpd null");
			return null;
		}
		
		List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class).filter("clientId", clientPageId)
				.filter("startTime >", SEUtil.getHoursAgoOrToday(12)).order("startTime").list();

		if (dsEvents == null)
			LOG.info("dsEvents null"); // should be 0?

		LOG.info("Returning " + dsEvents.size() + " events for " + clientPageId + " - " + clientPageData.getName());

		for (DiscoveredEvent datastoreEvent : dsEvents) {

			if ((datastoreEvent.getEndTime() == null) || (datastoreEvent.getEndTime().after(now))) {

				DiscoveredEvent de = new DiscoveredEvent(datastoreEvent, datastoreEvent.getSourcePages());

				upcomingEvents.add(de);

			}
		}

		dto.setData(upcomingEvents);
		return dto;
		// return upcomingEvents;
	}

}