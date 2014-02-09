package ie.sortons.events.server.servlet;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.ObjectifyService;

import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.server.LandingPageServlet;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;

@SuppressWarnings("serial")
public class LandingPage extends LandingPageServlet {

	static {
		ObjectifyService.register(ClientPageData.class);
	}

	private static String APPID = Config.getAppID();
	private static String ENTRYPOINT = "sortonsevents/sortonsevents.nocache.js";

	public LandingPage() {
		super(ENTRYPOINT, APPID);
	}

	@Override
	protected void readWriteRequest() {
		// Add page admins to the clientpage data

		if (signedRequest != null && signedRequest.getPage() != null && signedRequest.getPage().isAdmin() == true && signedRequest.getUserId() != null) {

			ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(Long.parseLong(signedRequest.getPage().getId())).now();
			// This is just here for the first time the page tab is loaded. Once the server side is
			// design patterned up, DRY, this will be taken care of TODO
			// TODO meaning admins can't add pages until the thing has refreshed. damn NB NB NB
			if (clientPageData != null) {
				if (clientPageData.addPageAdmin(Long.parseLong(signedRequest.getUserId()))) {
					ofy().clear();
					ofy().save().entity(clientPageData).now();
				}
			}

		}

	}
}
