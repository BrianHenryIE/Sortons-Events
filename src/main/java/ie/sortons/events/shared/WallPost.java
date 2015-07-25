package ie.sortons.events.shared;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

@Entity
@Cache
public class WallPost implements JsonSerializable {

	// TODO get rid of public modifiers!
	
	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.WallPost";

	@Id
	public String uniqueId;

	public String postId;

	@Index
	public int date;

	public String url;

	@Index
	public Long clientId;

	/**
	 * No args constructor for Objectify etc
	 */
	public WallPost() {
	}

	public WallPost(Long clientId, String postId, int date, String url) {
		this.clientId = clientId;
		this.postId = postId;
		this.date = date;
		this.url = url;
		this.uniqueId = clientId + "_" + postId;
	}

	public String getUrl() {

		return this.url;
	}

}
