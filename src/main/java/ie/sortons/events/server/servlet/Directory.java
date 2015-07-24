package ie.sortons.events.server.servlet;


@SuppressWarnings("serial")
public class Directory extends LandingPageServlet {

	// TODO: scriptinject from the bing maps project
	String head ="			<script src='//ecn.dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=7.0&amp;s=1' />";
	
	// TODO not asd
	public Directory() {
		super("../sortonsevents/sortonsevents.nocache.js", "361530767318220", "670c011e487aa0fabe7b54ecb56c2629");
	}

	@Override
	protected void readWriteRequest() {
		// TODO Auto-generated method stub

	}

}
