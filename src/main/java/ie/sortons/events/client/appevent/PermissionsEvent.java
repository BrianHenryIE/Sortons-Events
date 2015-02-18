package ie.sortons.events.client.appevent;

import ie.sortons.gwtfbplus.client.overlay.Permissions;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class PermissionsEvent extends GenericEvent {
	
	  private final JavaScriptObject response;
	  private final Permissions permissionsObject;

	  public PermissionsEvent(JavaScriptObject response) {
		  
		  this.response = response;
		  
		  // Parse the json to an object
		  permissionsObject = response.cast();

		  System.out.println("Permissions received: " + permissionsObject.getPermissionsList().size());
	  }

	  public JavaScriptObject getResponse() {
	    return response;
	  }
		  
	  public Permissions getPermissionsObject() {
	    return permissionsObject;
	  }
		  
}
