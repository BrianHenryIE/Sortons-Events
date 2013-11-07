package ie.sortons.events.server.servlet;

import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.server.LandingPageServlet;

@SuppressWarnings("serial")
public class LandingPage extends LandingPageServlet {

	public LandingPage(){
		super("sortonsevents/sortonsevents.nocache.js", Config.getAppID());
	}
	
}
