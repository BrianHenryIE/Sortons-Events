package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.server.datastore.ClientPageData;
import ie.sortons.events.shared.FbConfig;
import ie.sortons.events.shared.FbPage;
import ie.sortons.gwtfbplus.server.fql.FqlPage;
import ie.sortons.gwtfbplus.server.fql.FqlPage.FqlPageItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.gson.Gson;
import com.googlecode.objectify.ObjectifyService;

@Api(name = "clientdata", version = "v1")
public class ClientPageDataEndpoint {
	
	{
		ObjectifyService.register(ClientPageData.class);
	}
	
	//TODO maybe use int and not String: more efficient!?
	public ClientPageData getClientPageData(@Named("clientid") String clientPageId) {

		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		if ( clientPageData == null ) {

			// FQL call pieces
			String fqlCallStub = "https://graph.facebook.com/fql?q=";
			String pageCallStub = "SELECT page_id, name, page_url FROM page WHERE page_id = "; // &access_token="+access_token;

			String fql = pageCallStub + clientPageId;

			Gson gson = new Gson();

			String json = "";
			try {
				// System.out.println("Getting all page events: " + fql);
				URL url = new URL(fqlCallStub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + FbConfig.getAppAccessToken());
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String line;

				while ((line = reader.readLine()) != null) {
					json += line;           	
				}
				reader.close();

			} catch (MalformedURLException e) {
				// System.out.println("catch (MalformedURLException e)");
				// ...
			} catch (IOException e) {
				// System.out.println("catch (IOException e)");
				// ...
			}

			// Convert the json string to java object
			FqlPageItem fqlPage = gson.fromJson(json, FqlPage.class).getData()[0];
						
			FbPage clientPageDetails = new FbPage(fqlPage.getName(), fqlPage.getPageUrl(), clientPageId);

			// Add new entry
			ClientPageData newClient = new ClientPageData(clientPageDetails);
			ofy().save().entity(newClient).now();

			return newClient;
		
		} else {

			return clientPageData;
			
		}
	}
		
	@ApiMethod(name = "clientdata.addPage", httpMethod = "post")
	public FbPage addPage(@Named("clientpageid") String clientpageid, FbPage newPage) {
		
		// TODO some sort of security
		
		ClientPageData clientPageData = getClientPageData(clientpageid);
		
		clientPageData.addPage(newPage);
		
		ofy().save().entity(clientPageData).now();
		
		// TODO return an error, if appropriate
		return newPage;
	}
	

	@ApiMethod(name = "clientdata.excludePage", httpMethod = "post")
	public FbPage excludePage(@Named("clientpageid") String clientpageid, FbPage newPage) {
		
		// TODO some sort of security
		
		ClientPageData clientPageData = getClientPageData(clientpageid);
		
		clientPageData.excludePage(newPage);
		
		ofy().save().entity(clientPageData).now();
		
		// TODO return an error, if appropriate
		return newPage;
	}
	

}