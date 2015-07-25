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

	@GwtIncompatible
	@Id Long id;
	
	private Long eventId;

	@Index
	private Long clientId;

	private List<SourcePage> sourcePages = new ArrayList<SourcePage>();

	String name;
	String location;
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
	}

	public DiscoveredEvent(Long eventId, Long clientId, SourcePage sourcePage) {
		this.eventId = eventId;
		this.clientId = clientId;
		addSourcePage(sourcePage);
	}

	public DiscoveredEvent(String eventId, Long clientId, SourcePage sourcePage) {
		new DiscoveredEvent(Long.parseLong(eventId), clientId, sourcePage);
	}

	public DiscoveredEvent(FqlEvent fbEvent, Long clientId, List<SourcePage> sourcePages) {
		this(fbEvent, sourcePages);
		this.clientId = clientId;
	}
	
	public DiscoveredEvent(FqlEvent fbEvent, List<SourcePage> sourcePages) {
		setEvent(fbEvent);
		this.sourcePages = sourcePages;
	}

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
	
	private void setEvent(FqlEvent fbEvent) {
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
	}

	public void setClientIdFromCPD(ClientPageData clientPageData) {
		this.clientId = clientPageData.getClientPageId();
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
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

	// public getters and setters for serialization
	
	
	
}
