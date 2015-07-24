package ie.sortons.events.server.servlet;

import java.util.logging.Logger;

import ie.sortons.events.shared.Config;

@SuppressWarnings("serial")
public class LandingPage extends LandingPageServlet {

	static {
		log = Logger.getLogger(LandingPage.class.getName());
	}

	// TODO: scriptinject from the bing maps project
	String head = "			<script src='//ecn.dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=7.0&amp;s=1' />";

	private static String APPID = Config.getAppIDServer();
	private static String APPSECRET = Config.getAppSecret();
	private static String ENTRYPOINT = "../sortonsevents/sortonsevents.nocache.js";

	public LandingPage() {
		super(ENTRYPOINT, APPID, APPSECRET);
	}

}
