package ie.sortons.events.client.appevent;

import ie.sortons.gwtfbplus.client.overlay.LoginResponse;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class LoginEvent extends GenericEvent { 
	
	  private final JavaScriptObject response;
	  private final LoginResponse loginObject;

	  public LoginEvent(JavaScriptObject response) {

		  System.out.println("fbCore.getLoginStatus() onSuccess");
		  
		  this.response = response;
		  
		  // Parse the json to an object
		  loginObject = response.cast();
		  
		  
		  // {"authResponse":{"accessToken":"CAADkpnjyyEwBAMq26YY0WHpolgJZCYCzlulL27y1ZBMJSFH2faBeFIT51gPW1eiO0kS04KTK5ZBiyDIpK7sV1nb2oMmolkBwVklSzZAFJLIHxYRetu4CyLWGB7bHUYxgA9TTSI7koD9XoQupg1E72m5LoPCqZCZAZCJXwTbkcITwGBz8FwuhUeQn51QErMvvJLpYmHpmF2J0AZDZD", "userID":"37302520", "expiresIn":5634, "signedRequest":"-2AYs6uyqYNeej-WqJ7brMRI5Ncz_4nFAiAlaRjGVM0.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUUJFdmp3WENDdmd6cEE5N1EwYXBna3lPTFFJM0d3VzRnQjRyLVVJTmgzSTk4VWRQYnFRSzZBR3FfZ2tEZVZ0ZG10c0k4WWxHU1ZlcnBZYUJFZVV5UzdMMThOa3ljT3YxZXJuaXFKVERycURUSWZxWmRVSzRmdC1aczNibUZfc2xpS0tBU05JX2dlT3NZZUJlNU95cTJOU0tENDBicWJiWkNPZnZrSjZ2N3ZNd0pNdjFOcDJ6RGEwMnIwRGoyWGx4NGZoc2d5WjVhakVYd2pSajRwYlA2Ry16N3R5aGRjLU1zbUtDM0hhQjFXc1QwSFFsQllucXNWazZFNE9KcHBRbDZXaUljZ2tSekdqZloyemllQmJ2SDdDbktDQ2VIYndyME1kWmpUSzdwUmEwYnV2NXlKdGo5VmhWRVZfV2NjRFlSNCIsImlzc3VlZF9hdCI6MTM4OTIxOTk2NiwidXNlcl9pZCI6IjM3MzAyNTIwIn0"}, "status":"connected"}

		  System.out.println(new JSONObject(loginObject).toString());
	  }

	  public JavaScriptObject getResponse() {
	    return response;
	  }
		  
	  public LoginResponse getLoginObject() {
	    return loginObject;
	  }
		  
}

