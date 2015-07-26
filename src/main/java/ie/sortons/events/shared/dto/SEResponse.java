package ie.sortons.events.shared.dto;

import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

abstract class SEResponse<T> implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public String classname = this.getClass().getCanonicalName(); 

	protected String friendlyError;
	protected String error;

	abstract List<T> getData();
	abstract void setData(List<T> data);
	
	public String getFriendlyError() {
		return friendlyError;
	}

	public void setFriendlyError(String friendlyError) {
		this.friendlyError = friendlyError;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
