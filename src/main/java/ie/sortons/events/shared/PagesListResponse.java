package ie.sortons.events.shared;

import java.util.List;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

/**
 * @author brianhenry
 * 
 *         This is just because could endpoints can't use strings directly!
 */
public class PagesListResponse implements JsonSerializable {

	public List<SourcePage> items;

	public List<SourcePage> getPages() {
		return items;
	}

	public PagesListResponse() {
	}

}
