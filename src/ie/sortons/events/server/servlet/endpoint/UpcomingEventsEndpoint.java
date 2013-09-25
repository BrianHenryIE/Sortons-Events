package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.server.datastore.FbEvent;
import ie.sortons.events.shared.Data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "upcomingEvents", version = "v1")
public class UpcomingEventsEndpoint {
	
	public static ArrayList<FbEvent> upcomingEvents = new ArrayList<FbEvent>();
	
	
	public List<FbEvent> getList(@Named("id") Integer id) {
			 
		Date now = new Date();
		ObjectifyService.register(FbEvent.class);
		
		upcomingEvents.clear();
		
		List<FbEvent> dsEvents = ofy().load().type(FbEvent.class).filter("start_time_date >", getHoursAgoOrToday(12)).list();
				
		for(FbEvent datastoreEvent : dsEvents){
			
			if((datastoreEvent.getEnd_time_date()==null)||(datastoreEvent.getEnd_time_date().after(now))){
				
				for(String s : datastoreEvent.getFbPagesStrings()){
					datastoreEvent.addFbPageDetail(Data.getUcdPages().get(s));
				}
				
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