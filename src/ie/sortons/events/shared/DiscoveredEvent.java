package ie.sortons.events.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;


@Entity
public class DiscoveredEvent {


	@Id 
	private String eid;

	private FbEvent fbEvent;

	private List<FbPage> sourcePages = new ArrayList<FbPage>();


	/**
	 * No args constructor for Objectify
	 */
	protected DiscoveredEvent () {}


	public DiscoveredEvent(FbEvent fbEvent, FbPage sourcePage) {
		this.eid = fbEvent.getEid();
		this.fbEvent = fbEvent;
		addSourcePage(sourcePage);
	}


	/**
	 * This constructor is used when events are found in the event_member table,
	 * i.e. only the event id and source page are known
	 * 
	 * @param eventId
	 * @param sourcePage
	 */
	public DiscoveredEvent(String eventId, FbPage sourcePage) {
		this.eid = eventId;
		this.fbEvent = new FbEvent(eventId);
		addSourcePage(sourcePage);
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


	public static class Overlay extends JavaScriptObject {

		protected Overlay() {}

		public final native FbEvent.Overlay getFbEvent() /*-{ return this.fbEvent; }-*/;
		public final native JsArray<FbPage.Overlay> getSourcePages() /*-{ return this.sourcePages; }-*/;
	}
	
}
