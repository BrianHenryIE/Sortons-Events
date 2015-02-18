package ie.sortons.events.server.servlet;

import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.server.LandingPageServlet;

@SuppressWarnings("serial")
public class LandingPage extends LandingPageServlet {

	// TODO: scriptinject from the bing maps project
	String head ="			<script src='//ecn.dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=7.0&amp;s=1' />";
	
	
	private static String APPID = Config.getAppID();
	private static String APPSECRET = Config.getAppSecret();
	private static String ENTRYPOINT = "../sortonsevents/sortonsevents.nocache.js";

	public LandingPage() {
		super(ENTRYPOINT, APPID, APPSECRET);
	}

}
