package ie.sortons.events.client.appevent;

import ie.sortons.gwtfbplus.client.overlay.AuthResponse;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class LoginAuthResponseEvent extends GenericEvent { 
	
	  private final AuthResponse loginObject;

	  public LoginAuthResponseEvent(JavaScriptObject response) {
 		  loginObject = response.cast();

	  }
  
	  public AuthResponse getLoginObject() {
	    return loginObject;
	  }
		  
}

