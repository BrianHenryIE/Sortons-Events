package ie.sortons.events.server.facebook.gson;


import com.google.gson.Gson;

import lombok.Getter;

/**

http://developers.facebook.com/docs/reference/fql/event/

{
  "data": [
    {
      "name": "✯✯✯ AIR @ ANDREWS LANE THEATRE presented by UCD DANCE SOCIETY ✯✯✯",
      "location": "Andrew's Lane Theatre",
      "eid": 314646171897576,
      "start_time": "2011-11-30T23:00:00",
      "end_time": "2011-12-01T02:30:00",
      "pic_square": "http://profile.ak.fbcdn.net/hprofile-ak-ash4/373192_314646171897576_693607468_n.jpg"
    }, 

eid
name
pic_small
pic_big
pic_square
pic
host
description
start_time
end_time
creator
update_time
location
venue
privacy
hide_guest_list
can_invite_friends
all_members_count
attending_count
unsure_count
declined_count
not_replied_count


*/


@Getter 
public class FqlEvent {

	private FqlEvent () {}
	
	private FqlEvent.FqlEventItem[] data; 
 
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	
	
	@Getter
	public static class FqlEventItem {
		
		private FqlEventItem () {}

		private String eid;
		private String name;
		private String pic_small;
		private String pic_big;
		private String pic_square;
		private String pic;
		private String host;
		private String description;
		private String start_time;
		private String end_time;
		private String creator;
		private String update_time;
		private String location;
		// private String venue;
		private String privacy;
		private String hide_guest_list;
		private String can_invite_friends;
		private String all_members_count;
		private String attending_count;
		private String unsure_count;
		private String declined_count;
		private String not_replied_count;
		
		
		
		//TODO
		//Do a date convert in here.
		
		public String toString(){
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}


}
