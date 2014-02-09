package ie.sortons.events.shared;

import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

@Entity
@SkipNullSerialization
public class DiscoveredEvent implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.DiscoveredEvent";

	@Id
	public Long eid;

	public FqlEvent fbEvent;

	@Index
	public List<Long> sourceLists = new ArrayList<Long>();

	public List<FqlPage> sourcePages = new ArrayList<FqlPage>();

	/**
	 * No args constructor for Objectify etc
	 */
	public DiscoveredEvent() {
	}

	public DiscoveredEvent(FqlEvent fbEvent, Long clientId, FqlPage sourcePage) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		addSourceList(clientId);
		addSourcePage(sourcePage);
	}

	public DiscoveredEvent(FqlEvent fbEvent, List<Long> sourceLists, List<FqlPage> sourcePages) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		this.sourceLists = sourceLists;
		this.sourcePages = sourcePages;
	}

	public DiscoveredEvent(FqlEvent fbEvent, List<FqlPage> sourcePages) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		this.sourcePages = sourcePages;
	}

	public FqlEvent getFbEvent() {
		return this.fbEvent;
	}

	public List<FqlPage> getSourcePages() {
		return sourcePages;
	}

	public boolean hasSourcePage(FqlPage fbPage) {
		return (sourcePages.contains(fbPage));
	}

	public boolean addSourcePage(FqlPage sourcePage) {
		if (!sourcePages.contains(sourcePage)) {
			sourcePages.add(sourcePage);
			return true;
		}
		return false;
	}

	public boolean addSourcePages(List<FqlPage> newSourcePages) {
		boolean changed = false;
		for (FqlPage newSourcePage : newSourcePages) {
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
		List<FqlPage> newSources = new ArrayList<FqlPage>();
		for (FqlPage page : sourcePages)
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

}
