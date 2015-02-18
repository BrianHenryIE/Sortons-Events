package ie.sortons.events.client.appevent;

import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.binder.GenericEvent;

public class ResponseErrorEvent extends GenericEvent {

	private final Response response;

	public ResponseErrorEvent(Response response) {

		this.response = response;

	}

	public Response getResponse() {
		return response;
	}

}
