package ie.sortons.events.shared;




import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Index;



@Embed
public class FbEvent {


	private String eid;

	private String name;

	private String location;
	@Index
	private Date startTimeDate;
	@Index
	private Date endTimeDate;

	private String startTime;

	private String endTime;

	private String picSquare;


	/**
	 * No args constructor for Objectify entity
	 */
	protected FbEvent() {}


	public FbEvent(String eid) {
		this.eid = eid;
	}


	public FbEvent(String eid, String name, String location, String startTime, String endTime, String picSquare) {
		this.eid = eid;
		this.name = name;
		this.location = location;
		this.startTime= startTime;
		this.startTimeDate = parseDate(startTime);
		this.endTime  = endTime;
		this.endTimeDate = parseDate(endTime);
		this.picSquare = picSquare;
	}


	public void mergeWithFbEvent(FbEvent otherEvent){

		if(this.name==null) this.name = otherEvent.getName();
		if(this.location==null) this.location = otherEvent.getLocation();
		if(this.startTimeDate==null) this.startTimeDate = otherEvent.getStartTimeDate();	
		if(this.endTimeDate==null) this.endTimeDate = otherEvent.getEndTimeDate();
		if(this.startTime==null) this.startTime = otherEvent.getStartTime();
		if(this.endTime==null) this.endTime = otherEvent.getEndTime();
		if(this.picSquare==null) this.picSquare = otherEvent.getPicSquare();

	}


	public String getEid() {
		return eid;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public Date getStartTimeDate() {
		return startTimeDate;
	}

	public Date getEndTimeDate() {
		return endTimeDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getPicSquare() {
		return picSquare;
	}


	private Date parseDate(String iso8601string){

		DateTimeFormat dtf;

		DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();

		if(iso8601string==null){
			return null;
		} else if (iso8601string.length()==10){
			dtf = new DateTimeFormat("yyyy-MM-dd", info) {};
		} else {
			dtf = new DateTimeFormat("yyyy-MM-dd'T'HH:mm:ssZ", info) {};
		}
		return dtf.parse(iso8601string);
	}




	public static class Overlay extends JavaScriptObject {

		protected Overlay() {} 

		public final native String getEid() /*-{ return this.eid; }-*/;
		public final native String getName() /*-{ return this.name; }-*/;
		public final native String getLocation() /*-{ return this.location; }-*/;
		public final native String getStartTime() /*-{ return this.startTime; }-*/;
		// public final native Date getStartTimeDate() /*-{ return this.startTimeDate; }-*/;
		public final native String getPicSquare() /*-{ return this.picSquare; }-*/;

		//	"2013-09-15T11:00:00+0100"
		//	"2013-09-15T03:00:00.000-07:00",

		public final Date getStartTimeDate() { return (this.getStartTime().length()>10 ? DateTimeFormat.getFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(getStartTime()) : DateTimeFormat.getFormat("yyyy-MM-dd").parse(getStartTime()) ); }

		/**
		 * @return The start time formatted e.g. Thursday, 11 September, 2014, at 14:00
		 */
		public final String getStartTimeString() { 
			return (this.getStartTime().length()>10 ? DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy, 'at' k:mm").format(getStartTimeDate()) : DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(getStartTimeDate()) ); 
		}
		// public final String getStartTimeString() { return DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(getStartTimeDate()); }


	}
}