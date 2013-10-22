package ie.sortons.events.client.appevent;

import ie.sortons.gwtfbplus.client.overlay.LoginResponse;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class LoginEvent extends GenericEvent { 
	
	  private final JavaScriptObject response;
	  private final LoginResponse loginObject;

	  public LoginEvent(JavaScriptObject response) {

		  System.out.println("fbCore.getLoginStatus() onSuccess");
		  
		  this.response = response;
		  
		  // Parse the json to an object
		  loginObject = response.cast();
	  }

	  public JavaScriptObject getResponse() {
	    return response;
	  }
		  
	  public LoginResponse getLoginObject() {
	    return loginObject;
	  }
		  
}

