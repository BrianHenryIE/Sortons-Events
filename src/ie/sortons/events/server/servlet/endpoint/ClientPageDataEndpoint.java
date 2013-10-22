package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
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

		// When the customer has just signed up
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
	public FbPage addPage(@Named("clientpageid") String clientpageid, FbPage jsonPage) {
		// TODO some sort of security
		
		// TODO move out!
		FbPage newPage;
		
		if( jsonPage.getName()=="" || jsonPage.getPageUrl()=="" ){
			newPage = getPageFromId(jsonPage.getPageId());
		} else {
			newPage = jsonPage;
		}
		
		ClientPageData clientPageData = getClientPageData(clientpageid);
		
		clientPageData.addPage(newPage);
		
		ofy().save().entity(clientPageData).now();
		
		// TODO
		// Check for events on this page immediately
		
		// TODO return an error, if appropriate
		return newPage;
	}
	

	@ApiMethod(name = "clientdata.ignorePage", httpMethod = "post")
	public FbPage ignorePage(@Named("clientpageid") String clientpageid, FbPage jsonPage) {
		
		// TODO some sort of security
		
		ClientPageData clientPageData = getClientPageData(clientpageid);
		
		FbPage newPage;
		if( jsonPage.getName()=="" || jsonPage.getPageUrl()=="" ){
			newPage = getPageFromId(jsonPage.getPageId());
		} else {
			newPage = jsonPage;
		}
		
		clientPageData.ignorePage(newPage);
		
		ofy().save().entity(clientPageData).now();
		
		// TODO return an error, if appropriate
		return newPage;
	}
	
	private FbPage getPageFromId(String pageId) {
		// FQL call pieces
		String fqlcallstub = "https://graph.facebook.com/fql?q=";
		String fql = "SELECT page_id, name, page_url FROM page WHERE page_id = " + pageId;
		String access_token = "470244209665073%7CrbUtPwZewT7KpkNinkKym5LDaHw";
		
		String json = "";
		
		try {
			// System.out.println("Getting all page events: " + fql);
            URL url = new URL(fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token);
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
		
		Gson gson = new Gson();
		// Convert the json string to java object
		FbPage newPage = gson.fromJson(json, FbPage.class);
		
		return newPage;
	}

}