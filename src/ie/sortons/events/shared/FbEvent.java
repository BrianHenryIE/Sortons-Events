package ie.sortons.events.shared;




import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;



@Embed
public class FbEvent implements JsonSerializable, Comparable<FbEvent> {


	public String eid;

	public String name;

	public String location;
	@Index
	public Date startTimeDate;
	@Index
	public Date endTimeDate;

	public String startTime;

	public String endTime;

	public String picSquare;


	/**
	 * No args constructor for Objectify entity and JsonSerializable
	 */
	public FbEvent() {}


	public FbEvent(String eid) {
		this.eid = eid;
	}


	public FbEvent(String eid, String name, String location, String startTime, String endTime, String picSquare) {
		this.eid = eid;
		this.name = name;
		this.location = location;
		this.startTime= startTime;
		this.startTimeDate = parseDate(startTime); // Time from Facebook
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


	@Override
	public int compareTo(FbEvent other) {
		return this.eid.compareTo(other.getEid());
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FbEvent))
			return false;
		return compareTo((FbEvent) obj) == 0;
	}

	@Override
	public final int hashCode() {
		return eid.hashCode();
	}
	
	
	public static FbEvent fromJson(String json) {
	        Serializer serializer = (Serializer) GWT.create(Serializer.class);
	        return (FbEvent)serializer.deSerialize(json,"ie.sortons.events.shared.FbEvent");
	}
	 
	public String toJson() {
	        Serializer serializer = (Serializer) GWT.create(Serializer.class);
	        return serializer.serialize(this);
	}
}