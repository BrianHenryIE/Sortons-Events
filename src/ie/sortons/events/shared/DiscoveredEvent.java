package ie.sortons.events.shared;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.ArrayListSerializer;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;
//import com.google.api.server.spi.config.AnnotationBoolean;
//import com.google.api.server.spi.config.ApiResourceProperty;


@Entity
public class DiscoveredEvent implements JsonSerializable {


	@Id 
	public String eid;

	public FbEvent fbEvent;

	//@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	@Index
	public List<String> sourceLists = new ArrayList<String>();

	public List<FbPage> sourcePages = new ArrayList<FbPage>();

	/**
	 * No args constructor for Objectify etc
	 */
	public DiscoveredEvent () {}


	public DiscoveredEvent(FbEvent fbEvent, String clientId, FbPage sourcePage) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		addSourceList(clientId);
		addSourcePage(sourcePage);
	}


	/**
	 * This constructor is used when events are found in the event_member table,
	 * i.e. only the event id and source page are known
	 * 
	 * @param eventId
	 * @param sourcePage
	 */
	public DiscoveredEvent(String eventId, String clientId, FbPage sourcePage) {
		this.eid = eventId;
		this.fbEvent = new FbEvent(eventId);
		addSourceList(clientId);
		addSourcePage(sourcePage);
	}

	public DiscoveredEvent(FbEvent fbEvent, List<String> sourceLists, List<FbPage> sourcePages) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		this.sourceLists = sourceLists;
		this.sourcePages = sourcePages;
	}


	/**
	 * Copy Constructor!
	 * 
	 * @param DiscoveredEvent to copy
	 */
	public DiscoveredEvent(DiscoveredEvent de) {
		this.eid = de.getFbEvent().getEid();
		this.fbEvent = new FbEvent(de.getFbEvent());
		this.sourceLists = de.getSourceLists(); // same reference
		this.sourcePages = de.getSourcePages(); // same reference
	}


	public FbEvent getFbEvent(){
		return this.fbEvent;
	}


	public List<FbPage> getSourcePages() {
		return sourcePages;
	}


	public boolean hasSourcePage(FbPage fbPage) {
		return (sourcePages.contains(fbPage));
	}


	public boolean addSourcePage(FbPage sourcePage){
		if(!sourcePages.contains(sourcePage)){
			sourcePages.add(sourcePage);
			return true;
		}
		return false;
	}


	public boolean addSourcePages(List<FbPage> newSourcePages) {
		boolean changed = false;
		for(FbPage newSourcePage : newSourcePages){
			if(addSourcePage(newSourcePage))
				changed = true;
		}
		return changed;
	}


	public List<String> getSourceLists() {
		return sourceLists;
	}


	public boolean hasSourceList(String source) {
		return sourceLists.contains(source);
	}


	public boolean addSourceList(String source){
		if(!sourceLists.contains(source)){
			sourceLists.add(source);
			return true;
		}
		return false;
	}


	public boolean addSourceLists(List<String> newSourceLists) {
		boolean changed = false;
		for(String source : newSourceLists) {
			if(addSourceList(source))
				changed = true;
		}
		return changed;
	}


	public static DiscoveredEvent fromJson(String json) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (DiscoveredEvent)serializer.deSerialize(json,"ie.sortons.events.shared.DiscoveredEvent");
	}


	@SuppressWarnings("unchecked")
	public static List<DiscoveredEvent> oldlistFromJson(String json) {
		ArrayListSerializer serializer = (ArrayListSerializer) GWT.create(ArrayListSerializer.class);
		return (List<DiscoveredEvent>)serializer.deSerialize(json,"ie.sortons.events.shared.DiscoveredEvent");
	}


	public static List<DiscoveredEvent> listFromJson(String json) {
		ItemArray ira = ItemArray.fromJson(json);
		return ira.getItems();
	}


	public String toJson() {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return serializer.serialize(this);
	}


	public static class ItemArray implements JsonSerializable {

		public List<DiscoveredEvent> items = new ArrayList<DiscoveredEvent>();;

		public List<DiscoveredEvent> getItems(){
			return items;
		}

		public ItemArray() {}

		public static ItemArray fromJson(String json) {
			Serializer serializer = (Serializer) GWT.create(Serializer.class);
			return (ItemArray)serializer.deSerialize(json,"ie.sortons.events.shared.DiscoveredEvent.ItemArray");
		}

		public String toJson() {
			Serializer serializer = (Serializer) GWT.create(Serializer.class);
			return serializer.serialize(this);
		}
	}


	public static DiscoveredEvent merge(DiscoveredEvent d1, DiscoveredEvent d2){

		if ( d1.getFbEvent().getEid().equals(d2.getFbEvent().getEid()) ) {

			DiscoveredEvent newDe = new DiscoveredEvent(d1);
			newDe.addSourceLists(d2.getSourceLists());
			newDe.addSourcePages(d2.getSourcePages());		

			return newDe;
		}
		return null;
	}


	/**
	 * This is used to remove the source lists before transferring the data to the client
	 */
	public void setSourceListsNull() {
		sourceLists = null;		
	}

	
	/**
	 * This is used to remove the source pages that are related to other clients from the same event
	 */
	public void setSourcePagesToClientOnly(ClientPageData client){
		List<FbPage> newSources = new ArrayList<FbPage>();
		for(FbPage page : sourcePages)
			if( client.getIncludedPages().contains(page) ) 
				newSources.add(page);
		sourcePages = newSources;
	}

}
