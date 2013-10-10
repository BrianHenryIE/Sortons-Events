package ie.sortons.events.server.datastore;


import ie.sortons.events.shared.FbPage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;

import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;



@Entity
public class FbEvent {
	

	 @Id @Getter
	 private String eid;
	 @Getter
	 private String name;
	 @Getter
	 private String location;
	 @Index @Getter
	 private Date start_time_date;
	 @Index @Getter
	 private Date end_time_date;
	 @Getter
	 private String start_time;
	 @Getter
	 private String end_time;
	 @Getter
	 private String pic_square;
	 
	 @Index
	 @AlsoLoad("fbPages") private List<String> fbPagesStrings = new ArrayList<String>();
	 
	 @Getter
	 private List<FbPage> fbPagesDetail = new ArrayList<FbPage>();
	 
	 public void addFbPageDetail(FbPage fbPage){
		 if(!fbPagesDetail.contains(fbPage)){
			 fbPagesDetail.add(fbPage);
		 }
	 }
	 
	 public FbEvent() {
	 }


	 
	 public FbEvent(String eid, String name, String location, String start_time, String end_time, String pic_square, ArrayList<String> fbPagesStrings) {
		this.eid = eid;
		this.name = name;
		this.location = location;
		this.start_time= start_time;
		this.start_time_date = parseDate(start_time);
		this.end_time  = end_time;
		this.end_time_date = parseDate(end_time);
		this.pic_square = pic_square;
		this.fbPagesStrings = fbPagesStrings;
	 }
	 
	 
	 public FbEvent(String eid, String name, String location, String start_time, String end_time, String pic_square, String fbPageString) {
		this.eid = eid;
		this.name = name;
		this.location = location;
		this.start_time= start_time;
		this.start_time_date = parseDate(start_time);
		this.end_time  = end_time;
		this.end_time_date = parseDate(end_time);
		this.pic_square = pic_square;
		this.fbPagesStrings.add(fbPageString);
	 }
	 
	 public ArrayList<String> getFbPagesStrings() {
		 return (ArrayList<String>) fbPagesStrings;
	 }
	 
	 public void addFbPage(String fbPageString){
		 if(!fbPagesStrings.contains(fbPageString)){
			 fbPagesStrings.add(fbPageString);
		 }
	 }

	 /**
	  * Adds the list of Facebook Page uids to the existing list
	  * associated with this event.
	  * 
	  * @param  fbPages A list of Facebook Page uids that reference this event
	  * @return true if the list was changed false otherwise
	  */
	 public boolean addFbPages(ArrayList<String> newFbPages){
		 int before = fbPagesStrings.size();
		 
		 for(String uid : newFbPages) {
			 addFbPage(uid);
		 }
		 return (fbPagesStrings.size() > before);
		 
	 }
	 

	 
	 private Date parseDate(String iso8601string){
			
		 	Date date = new Date();
		 	if(iso8601string==null){
		 		date = null;
		 	} else if (iso8601string.length()==10){
		 		try {
					date = new SimpleDateFormat("yyyy-MM-dd").parse(iso8601string);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 	} else {
		 	
				try {
					date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(iso8601string);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 	}
			return date;
	 }
	
}