package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.shared.domain.FbResponse;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "clientdata", version = "v1")
public class ClientPageDataEndpoint {

	private static final Logger log = Logger.getLogger(ClientPageDataEndpoint.class.getName());

	static {
		ObjectifyService.register(ClientPageData.class);
	}

	public ClientPageData getClientPageData(HttpServletRequest req, @Named("clientid") Long clientPageId) {

		for (Cookie c : req.getCookies()) {
			if(c.getName().equals("accessToken"))
				System.out.println("access token: " + c.getValue());
		}

		// TODO
		ofy().clear();

		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		// When the customer has just signed up
		if (clientPageData == null) {

			FqlPage clientPageDetails = getPageFromId(clientPageId);

			System.out.println("Added to new page: " + clientPageDetails.getName() + " " + clientPageDetails.getPageUrl() + " " + clientPageId);

			// TODO
			// This fails with a NPE if the page isn't public, i.e. test users and unpublished pages

			// Add new entry
			ClientPageData newClient = new ClientPageData(clientPageDetails);

			ofy().save().entity(newClient).now();

			return newClient;

		} else {

			return clientPageData;

		}
	}

	@ApiMethod(name = "clientdata.addPage", httpMethod = "post")
	public FqlPage addPage(HttpServletRequest req, @Named("clientpageid") Long clientpageid, FqlPage jsonPage) {
		// TODO some sort of security

		log.info("addPage: " + jsonPage.getName() + " " + jsonPage.getPageId());

		FqlPage newPage = getPageFromId(jsonPage.getPageId());

		log.info("fbdetails 2: " + newPage.getName() + " " + newPage.getPageId());

		ClientPageData clientPageData = getClientPageData(req, clientpageid);

		ofy().clear();

		if (clientPageData.addPage(newPage)) {
			ofy().save().entity(clientPageData).now();
			log.info("saved");
		}

		// TODO
		// Check for events on this page immediately

		// TODO Understand and remove troubleshooting
		ofy().clear();

		// TODO return an error, if appropriate
		clientPageData = null;

		// Moved here to make succinct
		clientPageData = ofy().load().type(ClientPageData.class).id(clientpageid).now();

		log.info("returning from ds: " + clientPageData.getPageById(jsonPage.getPageId()).getTitle());

		return clientPageData.getPageById(jsonPage.getPageId());
	}

	@ApiMethod(name = "clientdata.ignorePage", httpMethod = "post")
	public FqlPage ignorePage(HttpServletRequest req, @Named("clientpageid") Long clientpageid, FqlPage jsonPage) {

		// TODO some sort of security

		ClientPageData clientPageData = getClientPageData(req, clientpageid);

		FqlPage newPage;
		if (jsonPage.getName() == "" || jsonPage.getPageUrl() == "") {
			newPage = getPageFromId(jsonPage.getPageId());
		} else {
			newPage = jsonPage;
		}

		clientPageData.ignorePage(newPage);

		ofy().save().entity(clientPageData).now();

		// TODO return an error, if appropriate
		return newPage;
	}

	FqlPage getPageFromId(Long pageId) {
		// FQL call pieces
		String fqlcallstub = "https://graph.facebook.com/fql?q=";
		String fql = "SELECT page_id, name, page_url, location FROM page WHERE page_id = " + pageId;
		String access_token = Config.getAppAccessToken();

		String json = "";

		try {
			// System.out.println("Getting all page events: " + fql);
			String call = fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token;
			// System.out.println(call);
			URL url = new URL(call);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				json += line;
			}
			reader.close();

		} catch (MalformedURLException e) {
			System.out.println("getPageFromId: catch (MalformedURLException e)");
			// ...
		} catch (IOException e) {
			System.out.println("getPageFromId: catch (IOException e)");
			// ...
		}

		log.info("Page details from fb: " + json);

		Gson gson = new Gson();
		// Convert the json string to java object
		Type fooType = new TypeToken<FbResponse<FqlPage>>() {
		}.getType();
		FbResponse<FqlPage> pages = gson.fromJson(json, fooType);

		if (pages.getError() == null && pages.getData() != null && pages.getData().size() > 0)
			return pages.getData().get(0);
		else
			return null;

		// TODO: return an error.
	}

}