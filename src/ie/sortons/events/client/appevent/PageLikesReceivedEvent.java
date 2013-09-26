package ie.sortons.events.client.appevent;

import ie.sortons.events.client.view.overlay.FbPageOverlay;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class PageLikesReceivedEvent extends GenericEvent { 
	
	  private final JsArray<FbPageOverlay> likes;

	  public PageLikesReceivedEvent(JsArray<FbPageOverlay> likes) {

		  this.likes = likes;
	
	  }
	  
	  public JsArray<FbPageOverlay> getLikes() {
	    return likes;
	  }
		  
}

