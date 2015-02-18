package ie.sortons.events.shared;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class BackendErrors implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.BackendErrors";

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	public BackendErrors() {
	}

	public String domain;
	public String reason;
	public String message;
}
