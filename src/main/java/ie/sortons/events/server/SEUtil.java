package ie.sortons.events.server;

import java.util.Calendar;
import java.util.Date;

/**
 * @author BrianHenryIE
 *
 */
public class SEUtil {


	
	/**
	 * This really isn't needed since Graph API events for an end time, unlike FQL
	 * 
	 * @param number hours
	 * @return the earlier of (this morning at midnight) or (now - the number of hours)
	 */
	public static Date getHoursAgoOrToday(int hours) {

		Calendar calvar = Calendar.getInstance();

		int today = calvar.get(Calendar.DAY_OF_YEAR);

		// Subtract the specified number of hours
		calvar.add(Calendar.HOUR_OF_DAY, -hours);

		// Keep subtracting hours until we get to last night
		while (calvar.get(Calendar.DAY_OF_YEAR) == today)
			calvar.add(Calendar.HOUR_OF_DAY, -1);

		Date ago = calvar.getTime();

		return ago;
	}

}
