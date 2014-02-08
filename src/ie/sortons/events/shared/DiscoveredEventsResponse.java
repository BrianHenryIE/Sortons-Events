package ie.sortons.events.shared;

import java.util.ArrayList;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class DiscoveredEventsResponse implements JsonSerializable {

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public String classname = "ie.sortons.events.shared.DiscoveredEventsResponse";

	public ArrayList<DiscoveredEvent> data;

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(ArrayList<DiscoveredEvent> data) {
		this.data = data;
	}

	public ArrayList<DiscoveredEvent> getData() {
		return data;
	}
/*
	public BackendError error;

	public BackendError getError() {
		return error;
	}
*/

}
