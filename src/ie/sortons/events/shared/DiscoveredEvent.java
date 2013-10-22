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


@Entity
public class DiscoveredEvent implements JsonSerializable {


	@Id 
	public String eid;

	public FbEvent fbEvent;

	public List<FbPage> sourcePages = new ArrayList<FbPage>();

	@Index
	public List<String> sourceLists = new ArrayList<String>();


	/**
	 * No args constructor for Objectify
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


	public DiscoveredEvent(List<String> newSourceLists, List<FbPage> newSourcePages, FbEvent upcomingEvent) {
		this.addSourceList(newSourceLists);
		this.addSourcePages(newSourcePages);
		this.fbEvent = upcomingEvent;
		this.eid = upcomingEvent.getEid();
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


	public void addSourcePage(FbPage sourcePage){
		if(!sourcePages.contains(sourcePage)){
			sourcePages.add(sourcePage);
		}
	}


	public void addSourcePages(List<FbPage> newSourcePages) {
		for(FbPage newSourcePage : newSourcePages){
			addSourcePage(newSourcePage);
		}
	}


	public List<String> getSourceLists() {
		return sourceLists;
	}


	public boolean hasSourceList(String source) {
		return (sourceLists.contains(source));
	}


	public void addSourceList(String source){
		if(!sourceLists.contains(source)){
			sourceLists.add(source);
		}
	}


	public void addSourceList(List<String> newSourceLists) {
		for(String source : newSourceLists) {
			addSourceList(source);
		}
	}

	public static DiscoveredEvent fromJson(String json) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (DiscoveredEvent)serializer.deSerialize(json,"ie.sortons.events.shared.DiscoveredEvent");
	}

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

	//	public static class Overlay extends JavaScriptObject {
	//
	//		protected Overlay() {}
	//
	//		public final native FbEvent.Overlay getFbEvent() /*-{ return this.fbEvent; }-*/;
	//		public final native JsArray<FbPage.FBPOverlay> getSourcePages() /*-{ return this.sourcePages; }-*/;
	//		public final native List<String> getSourceLists() /*-{ return this.sourceLists; }-*/;
	//	}


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


}
