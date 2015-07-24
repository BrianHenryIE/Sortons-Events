package ie.sortons.events.shared;

import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

/**
 * @author brianhenry
 * 
 *         This is just because could endpoints can't use strings directly!
 */
public class PagesListResponse implements JsonSerializable {

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public String classname = "ie.sortons.events.shared.PagesListResponse";

	
	public List<SourcePage> items;

	public List<SourcePage> getPages() {
		return items;
	}

	public PagesListResponse() {
	}

}
