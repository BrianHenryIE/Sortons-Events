package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;

import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.WallPost;
import ie.sortons.events.shared.dto.RecentPostsResponse;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "recentPosts", version = "v1")
public class RecentPostsEndpoint {

	private static final Logger log = Logger.getLogger(RecentPostsEndpoint.class.getName());

	static {
		ObjectifyService.register(WallPost.class);
	}

	public RecentPostsResponse getList(@Named("id") Long clientPageId) {

		// time to move this into a db class
		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();
		
		log.info("Returning posts for " + clientPageId + " - " + clientPageData.getName());

		RecentPostsResponse dto = new RecentPostsResponse();

		List<WallPost> dsRecentPosts = ofy().load().type(WallPost.class).filter("clientId", clientPageId).order("-date").limit(100).list();
		
		dto.setData(dsRecentPosts);
		
		return dto;
	}

}