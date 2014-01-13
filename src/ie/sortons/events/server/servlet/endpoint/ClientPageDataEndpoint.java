package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.FbDataArray;
import ie.sortons.events.shared.FbPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "clientdata", version = "v1")
public class ClientPageDataEndpoint {

	static {
		ObjectifyService.register(ClientPageData.class);
	}

	// TODO maybe use int and not String: more efficient!?
	public ClientPageData getClientPageData(@Named("clientid") String clientPageId) {

		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		// When the customer has just signed up
		if (clientPageData == null) {

			FbPage clientPageDetails = getPageFromId(clientPageId);

			System.out.println("Added to new page: " + clientPageDetails.getName() + " " + clientPageDetails.getPageUrl() + " " + clientPageId);

			// Add new entry
			ClientPageData newClient = new ClientPageData(clientPageDetails);

			ofy().save().entity(newClient).now();

			return newClient;

		} else {

			return clientPageData;
		}
	}

	@ApiMethod(name = "clientdata.addPage", httpMethod = "post")
	public FbPage addPage(@Named("clientpageid") String clientpageid, FbPage jsonPage) {
		// TODO some sort of security

		System.out.println("addPage: " + jsonPage.getName() + " " + jsonPage.getPageId());

		FbPage newPage = getPageFromId(jsonPage.getPageId());

		ClientPageData clientPageData = getClientPageData(clientpageid);

		if (clientPageData.addPage(newPage))
			ofy().save().entity(clientPageData).now();

		// TODO
		// Check for events on this page immediately

		// TODO return an error, if appropriate
		clientPageData = null;
		clientPageData = getClientPageData(clientpageid);

		return clientPageData.getPageById(jsonPage.getPageId());
	}

	@ApiMethod(name = "clientdata.ignorePage", httpMethod = "post")
	public FbPage ignorePage(@Named("clientpageid") String clientpageid, FbPage jsonPage) {

		// TODO some sort of security

		ClientPageData clientPageData = getClientPageData(clientpageid);

		FbPage newPage;
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

	FbPage getPageFromId(String pageId) {
		// FQL call pieces
		String fqlcallstub = "https://graph.facebook.com/fql?q=";
		String fql = "SELECT page_id, name, page_url, location FROM page WHERE page_id = " + pageId;
		String access_token = Config.getAppAccessToken();

		String json = "";

		try {
			// System.out.println("Getting all page events: " + fql);
			String call = fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token;
			System.out.println(call);
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

		Gson gson = new Gson();
		// Convert the json string to java object
		Type fooType = new TypeToken<FbDataArray<FbPage>>() {
		}.getType();
		FbDataArray<FbPage> pages = gson.fromJson(json, fooType);

		if (pages.getData() != null)
			return pages.getData().get(0);
		else
			return null;
		
		// TODO: return an error.
	}

}