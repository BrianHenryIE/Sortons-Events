package ie.sortons.events.client.appevent;

import ie.sortons.events.shared.FbPage;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class PageLikesReceivedEvent extends GenericEvent { 
	
	  private final JsArray<FbPage.Overlay> likes;

	  public PageLikesReceivedEvent(JsArray<FbPage.Overlay> likes) {

		  this.likes = likes;
	
	  }
	  
	  public JsArray<FbPage.Overlay> getLikes() {
	    return likes;
	  }
		  
}

