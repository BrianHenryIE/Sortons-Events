package ie.sortons.events.shared;

import java.util.List;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class RecentPostsResponse implements JsonSerializable {

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public String classname = "ie.sortons.events.shared.RecentPostsResponse";

	public List<WallPost> data;

	/**
	 * @param dsRecentPosts
	 *            the data to set
	 */
	public void setData(List<WallPost> dsRecentPosts) {
		this.data = dsRecentPosts;
	}

	public List<WallPost> getData() {
		return data;
	}
/*
	public BackendError error;

	public BackendError getError() {
		return error;
	}
*/

}
