package ie.sortons.events.server.facebook.gson;


import com.google.gson.Gson;

import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 
 * {
 *  "data": [
 *    {
 *      "uid": 118467031573937,
 *      "eid": 290199141098468,
 *      "rsvp_status": "",
 *      "start_time": "2012-11-08T15:30:00+0000"
 *    }, 
 * 
 * @author brianhenry
 *
 */
@Getter @NoArgsConstructor
public class FqlEventMember {

	private FqlEventMember.FqlEventMemberItem[] data; 

	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}


	
	@Getter @NoArgsConstructor
	public static class FqlEventMemberItem {
	
		private String eid;
		private String inviter;
		private String inviter_type;
		private String rsvp_status;
		private String start_time;
		private String uid;
		
		//TODO
		//Do a date convert in here.
		
		public String toString(){
			Gson gson = new Gson();
			return gson.toJson(this);
		}
		

	}
}