package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.RecentPostsResponse;
import ie.sortons.events.shared.WallPost;

import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "recentPosts", version = "v1")
public class RecentPostsEndpoint {

	static {
		ObjectifyService.register(WallPost.class);
	}

	public RecentPostsResponse getList(@Named("id") Long clientPageId) {

		RecentPostsResponse dto = new RecentPostsResponse();

		List<WallPost> dsRecentPosts = ofy().load().type(WallPost.class).filter("sourceList", clientPageId).order("-date").limit(100).list();
		
		dto.setData(dsRecentPosts);
		
		return dto;
	}

}