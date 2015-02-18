package ie.sortons.events.shared;

import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class ClientPageDataResponse implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public String classname = "ie.sortons.events.shared.ClientPageDataResponse";

	public List<ClientPageData> data;

	public ClientPageDataResponse() {
	}

	public ClientPageDataResponse(List<ClientPageData> clients) {
		setData(clients);
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(List<ClientPageData> data) {
		this.data = data;
	}

	public List<ClientPageData> getData() {
		return data;
	}
	/*
	 * public BackendError error;
	 * 
	 * public BackendError getError() { return error; }
	 */

}
