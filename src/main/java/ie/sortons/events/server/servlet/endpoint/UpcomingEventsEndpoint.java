package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.DiscoveredEventsResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiAuth;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "upcomingEvents", version = "v1")
@ApiAuth(allowCookieAuth = AnnotationBoolean.TRUE)

public class UpcomingEventsEndpoint {

	static {
		ObjectifyService.register(DiscoveredEvent.class);
	}

	
	public DiscoveredEventsResponse getList(@Named("id") Long clientPageId) {
		
		List<DiscoveredEvent> upcomingEvents = new ArrayList<DiscoveredEvent>();
		
		Date now = new Date();

		DiscoveredEventsResponse dto = new DiscoveredEventsResponse();

		List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class).filter("sourceLists", clientPageId).filter("fbEvent.start_time >", getHoursAgoOrToday(12)).order("fbEvent.start_time").list();
		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		for (DiscoveredEvent datastoreEvent : dsEvents) {

			if ((datastoreEvent.getFbEvent().getEndTime() == null) || (datastoreEvent.getFbEvent().getEndTime().after(now))) {

				DiscoveredEvent de = new DiscoveredEvent(datastoreEvent.getFbEvent(), datastoreEvent.getSourcePages());
				de.setSourceListsNull();
				de.setSourcePagesToClientOnly(clientPageData);

				upcomingEvents.add(de);

			}
		}

		dto.setData(upcomingEvents);
		return dto;
		// return upcomingEvents;
	}
	
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