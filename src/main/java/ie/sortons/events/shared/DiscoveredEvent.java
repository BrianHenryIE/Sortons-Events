package ie.sortons.events.shared;

import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

@Entity
@Cache
@SkipNullSerialization
public class DiscoveredEvent implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.DiscoveredEvent";

	
	@Id
	private String id;
	
	private Long eventId;

	@Index
	private Long clientId;

	private List<SourcePage> sourcePages = new ArrayList<SourcePage>();

	String name;
	String location;
	
	@Index
	Date startTime;
	Date endTime;
	boolean dateOnly;
	
	/**
	 * No args constructor for Objectify etc
	 */
	public DiscoveredEvent() {
	}

	public DiscoveredEvent(FqlEvent fbEvent, Long clientId, SourcePage sourcePage) {
		setEvent(fbEvent);
		this.clientId = clientId;
		addSourcePage(sourcePage);
		updateDatastoreId();
	}

	// TODO: having clientId and sourcePage is redundant. Is it possible to have a 
	// sourcePage without a clientId in it? (parent)
	public DiscoveredEvent(Long eventId,SourcePage sourcePage) {
		this.eventId = eventId;
		this.clientId = sourcePage.getClientId();
		addSourcePage(sourcePage);
		updateDatastoreId();
	}

	public DiscoveredEvent(String eventId, Long clientId, SourcePage sourcePage) {
		new DiscoveredEvent(Long.parseLong(eventId), sourcePage);
	}


	/**
	 * Copy constructor
	 * 
	 * @param dEvent
	 * @param sourcePages
	 */
	public DiscoveredEvent(DiscoveredEvent dEvent, List<SourcePage> sourcePages) {
		this.eventId = dEvent.getEventId();
		this.clientId = dEvent.getClientId();
		this.name = dEvent.getName();
		this.location = dEvent.getLocation();
		this.startTime = dEvent.getStartTime();
		this.endTime = dEvent.getEndTime();
		this.dateOnly = dEvent.isDateOnly();
		this.sourcePages = sourcePages;
	}
	
	public void setEvent(FqlEvent fbEvent) {
		this.eventId = fbEvent.getEid();
		this.name = fbEvent.getName();
		this.location = fbEvent.getLocation();
		this.startTime = fbEvent.getStartTime();
		this.endTime = fbEvent.getEndTime();
		this.dateOnly = fbEvent.is_date_only;
	}
	
	// Encapsulating in preparation for fql deprecation
	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}

	public Long getEventId() {
		return eventId;
	}


	
	public List<SourcePage> getSourcePages() {
		return sourcePages;
	}

	public boolean hasSourcePage(SourcePage fbPage) {
		return (sourcePages.contains(fbPage));
	}

	public boolean addSourcePage(SourcePage sourcePage) {
		if (!sourcePages.contains(sourcePage)) {
			sourcePages.add(sourcePage);
			return true;
		}
		return false;
	}

	public boolean addSourcePages(List<SourcePage> newSourcePages) {
		boolean changed = false;
		for (SourcePage newSourcePage : newSourcePages) {
			if (addSourcePage(newSourcePage))
				changed = true;
		}
		return changed;
	}

	public Long getClientId() {
		return clientId;
	}

	
	public boolean isDateOnly() {
		return dateOnly;
	}

	public void setDateOnly(boolean dateOnly) {
		this.dateOnly = dateOnly;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
		updateDatastoreId();
	}

	public void setClientIdFromCPD(ClientPageData clientPageData) {
		setClientId(clientPageData.getClientPageId());
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
		updateDatastoreId();
	}

	public void setSourcePages(List<SourcePage> sourcePages) {
		this.sourcePages = sourcePages;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void updateDatastoreId(){
		this.id = clientId + "" + eventId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + (dateOnly ? 1231 : 1237);
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sourcePages == null) ? 0 : sourcePages.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscoveredEvent other = (DiscoveredEvent) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (dateOnly != other.dateOnly)
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sourcePages == null) {
			if (other.sourcePages != null)
				return false;
		} else if (!sourcePages.equals(other.sourcePages))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}
	
	
	
	

	
	
}
