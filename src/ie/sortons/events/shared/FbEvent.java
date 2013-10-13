package ie.sortons.events.shared;



import ie.sortons.gwtfbplus.server.fql.FqlEvent.FqlEventItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;



@Entity
public class FbEvent {


	public String getEid() {
		return eid;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public Date getStart_time_date() {
		return start_time_date;
	}

	public Date getEnd_time_date() {
		return end_time_date;
	}

	public String getStart_time() {
		return start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public String getPic_square() {
		return pic_square;
	}

	public List<FbPage> getSourcePages() {
		return sourcePages;
	}



	@Id 
	private String eid;

	private String name;

	private String location;
	@Index
	private Date start_time_date;
	@Index
	private Date end_time_date;

	private String start_time;

	private String end_time;

	private String pic_square;

	@Index
	private List<String> fbPagesStrings = new ArrayList<String>();

	private List<FbPage> sourcePages = new ArrayList<FbPage>();

	@Deprecated
	public void addFbPageDetail(FbPage fbPage){
		addSourcePage(fbPage);
	}


	/**
	 * No args constructor for Objectify entity
	 */
	protected FbEvent() {}

	/**
	 * This constructor is used when events are found in the event_memebr table,
	 * i.e. only the event id and source page are known
	 * 
	 * @param eventId
	 * @param sourcePage
	 */
	public FbEvent(String eventId, FbPage sourcePage) {
		this.eid = eventId;
		addSourcePage(sourcePage);
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



	public boolean hasSourcePage(FbPage fbPage) {
		return (sourcePages.contains(fbPage));
	}

	public void addSourcePage(FbPage sourcePage){
		if(!sourcePages.contains(sourcePage)){
			sourcePages.add(sourcePage);
		}
		addFbPage(sourcePage.getPageId());
	}

	public void addSourcePages(List<FbPage> newSourcePages) {
		for(FbPage newSourcePage : newSourcePages){
			addSourcePage(newSourcePage);
		}
	}


	public ArrayList<String> getFbPagesStrings() {
		return (ArrayList<String>) fbPagesStrings;
	}

	@Deprecated
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


	public FbEvent mergeWithFbEvent(FbEvent otherEvent){

		addSourcePages(otherEvent.sourcePages);

		if(this.name==null) this.name = otherEvent.getName();
		if(this.location==null) this.location = otherEvent.getLocation();
		if(this.start_time_date==null) this.start_time_date = otherEvent.getStart_time_date();	
		if(this.end_time_date==null) this.end_time_date = otherEvent.getEnd_time_date();
		if(this.start_time==null) this.start_time = otherEvent.getStart_time();
		if(this.end_time==null) this.end_time = otherEvent.getEnd_time();
		if(this.pic_square==null) this.pic_square = otherEvent.getPic_square();

		return this;
	}

	public FbEvent mergeWithFbEvent(FqlEventItem otherEvent) {
		
		if(this.name==null) this.name = otherEvent.getName();
		if(this.location==null) this.location = otherEvent.getLocation();
		if(this.start_time_date==null) this.start_time_date = parseDate(otherEvent.getStart_time());	
		if(this.end_time_date==null) this.end_time_date = parseDate(otherEvent.getEnd_time());
		if(this.start_time==null) this.start_time = otherEvent.getStart_time();
		if(this.end_time==null) this.end_time = otherEvent.getEnd_time();
		if(this.pic_square==null) this.pic_square = otherEvent.getPic_square();

		return this;
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