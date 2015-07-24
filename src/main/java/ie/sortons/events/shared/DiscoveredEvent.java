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
	private Long eid;

	@Index
	private List<Long> sourceLists = new ArrayList<Long>();

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
		addSourceList(clientId);
		addSourcePage(sourcePage);
	}

	public DiscoveredEvent(Long eid, Long clientId, SourcePage sourcePage) {
		this.eid = eid;
		addSourceList(clientId);
		addSourcePage(sourcePage);
	}

	public DiscoveredEvent(String eid, Long clientId, SourcePage sourcePage) {
		new DiscoveredEvent(Long.parseLong(eid), clientId, sourcePage);
	}

	public DiscoveredEvent(FqlEvent fbEvent, List<Long> sourceLists, List<SourcePage> sourcePages) {
		setEvent(fbEvent);
		this.sourceLists = sourceLists;
		this.sourcePages = sourcePages;
	}

	public DiscoveredEvent(FqlEvent fbEvent, List<SourcePage> sourcePages) {
		setEvent(fbEvent);
		this.sourcePages = sourcePages;
	}

	public DiscoveredEvent(DiscoveredEvent dEvent, List<SourcePage> sourcePages) {
		this.eid = dEvent.getEid();
		this.name = dEvent.getName();
		this.location = dEvent.getLocation();
		this.startTime = dEvent.getStartTime();
		this.endTime = dEvent.getEndTime();
		this.dateOnly = dEvent.is_date_only();
		this.sourcePages = sourcePages;
	}
	
	private void setEvent(FqlEvent fbEvent) {
		this.eid = fbEvent.getEid();
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
	
	public boolean is_date_only(){
		return dateOnly;
	}
	
	public Long getEid() {
		return eid;
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

	public List<Long> getSourceLists() {
		return sourceLists;
	}

	public boolean hasSourceList(String source) {
		return sourceLists.contains(source);
	}

	public boolean addSourceList(Long source) {
		if (!sourceLists.contains(source)) {
			sourceLists.add(source);
			return true;
		}
		return false;
	}

	public boolean addSourceLists(List<Long> newSourceLists) {
		boolean changed = false;
		for (Long source : newSourceLists)
			if (addSourceList(source))
				changed = true;
		return changed;
	}

	/**
	 * This is used to remove the source pages that are related to other clients from the same event
	 */
	public void setSourcePagesToClientOnly(ClientPageData client) {
		List<SourcePage> newSources = new ArrayList<SourcePage>();
		for (SourcePage page : sourcePages)
			if (client.getIncludedPages().contains(page))
				newSources.add(page);
		sourcePages = newSources;
	}
	
	/**
	 * This is used to remove the source lists before transferring the data to the client
	 */
	public void setSourceListsNull() {
			sourceLists = null;
	}

	public boolean isDateOnly() {
		return dateOnly;
	}

	public void setDateOnly(boolean dateOnly) {
		this.dateOnly = dateOnly;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public void setSourceLists(List<Long> sourceLists) {
		this.sourceLists = sourceLists;
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
