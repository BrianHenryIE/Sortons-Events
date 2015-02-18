package ie.sortons.events.shared;

import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class DiscoveredEventsResponse implements JsonSerializable {

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public String classname = "ie.sortons.events.shared.DiscoveredEventsResponse";

	public List<DiscoveredEvent> data;

	/**
	 * @param upcomingEvents
	 *            the data to set
	 */
	public void setData(List<DiscoveredEvent> upcomingEvents) {
		this.data = upcomingEvents;
	}

	public List<DiscoveredEvent> getData() {
		return data;
	}
/*
	public BackendError error;

	public BackendError getError() {
		return error;
	}
*/

}
