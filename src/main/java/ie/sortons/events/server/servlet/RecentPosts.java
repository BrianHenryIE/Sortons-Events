package ie.sortons.events.server.servlet;

import java.util.logging.Logger;

@SuppressWarnings("serial")
public class RecentPosts extends LandingPageServlet {

	static {
		log = Logger.getLogger(RecentPosts.class.getName());
	}
	// TODO not asd
	public RecentPosts() {
		super("../sortonsevents/sortonsevents.nocache.js", "346300752178533", "appsecretshouldgohere");
	}

	@Override
	protected void readWriteRequest() {
		// TODO Auto-generated method stub

	}

}
