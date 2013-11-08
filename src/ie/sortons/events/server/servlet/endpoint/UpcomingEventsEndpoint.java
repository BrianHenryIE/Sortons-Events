package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.DiscoveredEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "upcomingEvents", version = "v1")
public class UpcomingEventsEndpoint {

	{
		ObjectifyService.register(DiscoveredEvent.class);
		
	}


	public ArrayList<DiscoveredEvent> upcomingEvents = new ArrayList<DiscoveredEvent>();


	public List<DiscoveredEvent> getList(@Named("id") String clientPageId) {

		Date now = new Date();

		upcomingEvents.clear();
		
		List<DiscoveredEvent> dsEvents = ofy().load().type(DiscoveredEvent.class).filter("sourceLists", clientPageId).filter("fbEvent.startTimeDate >", getHoursAgoOrToday(12)).order("fbEvent.startTimeDate").list();

		for(DiscoveredEvent datastoreEvent : dsEvents){

			if((datastoreEvent.getFbEvent().getEndTimeDate()==null)||(datastoreEvent.getFbEvent().getEndTimeDate().after(now))){

				upcomingEvents.add(datastoreEvent);
				
			}
		}

		return upcomingEvents;
	}


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