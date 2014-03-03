package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.ClientPageDataResponse;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.PageList;
import ie.sortons.gwtfbplus.server.SimpleStringCipher;
import ie.sortons.gwtfbplus.shared.domain.FbResponse;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventDatesAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenue;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenueAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiAuth;
import com.google.api.server.spi.config.ApiMethod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.ObjectifyService;

/**
 * @author brianhenry
 * 
 */
@Api(name = "clientdata", version = "v1")
@ApiAuth(allowCookieAuth = AnnotationBoolean.TRUE)
public class ClientPageDataEndpoint {

	private static final Logger log = Logger.getLogger(ClientPageDataEndpoint.class.getName());

	static {
		ObjectifyService.register(ClientPageData.class);
	}

	/**
	 * @param req
	 * @param clientPageId
	 * @return
	 */
	public ClientPageData getClientPageData(HttpServletRequest req, @Named("clientid") Long clientPageId) {
//		if (!(isPageAdmin(req, clientPageId) || isAppAdmin(req))) // Don't need to be a page admin to get the data for using with the map
//			return null;
		// TODO return an error
		
		return getClientPageData(clientPageId);
	}

	private ClientPageData getClientPageData(Long clientPageId) {
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

			System.out.println(clientPageData.getClientPage().getName());
			
			return clientPageData;

		}
	}

	@ApiMethod(name = "clientdata.addPage", httpMethod = "post")
	public FqlPage addPage(HttpServletRequest req, @Named("clientpageid") Long clientPageId, FqlPage jsonPage) {
		if (!(isPageAdmin(req, clientPageId) || isAppAdmin(req)))
			return null;
		// TODO return an error

		log.info("addPage: " + jsonPage.getName() + " " + jsonPage.getPageId());

		FqlPage newPage = getPageFromId(jsonPage.getPageId());

		log.info("fbdetails 2: " + newPage.getName() + " " + newPage.getPageId());

		ClientPageData clientPageData = getClientPageData(clientPageId);

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
		clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId).now();

		log.info("returning from ds: " + clientPageData.getPageById(jsonPage.getPageId()).getName());

		return clientPageData.getPageById(jsonPage.getPageId());
	}

	@ApiMethod(name = "clientdata.addPagesList", httpMethod = "post")
	public List<FqlPage> addPagesList(HttpServletRequest req, @Named("clientpageid") Long clientPageId, PageList pagesList) {
		if (!(isPageAdmin(req, clientPageId) || isAppAdmin(req)))
			return null;
		// TODO return an error

		System.out.println("cpdendpoint: " + pagesList);
		log.info("addPagesList: " + pagesList);

		ofy().clear();

		ClientPageData clientPageData = getClientPageData(clientPageId);

		List<FqlPage> newPages = new ArrayList<FqlPage>();

		for (String pageid : pagesList.getList()) {
			FqlPage newPage = getPageFromId(Long.parseLong(pageid));
			if (clientPageData.addPage(newPage)) {
				newPages.add(newPage);
				log.info("page added: " + newPage.getName() + " " + newPage.getPageId());
			}
		}

		if (newPages.size() > 0) {
			ofy().save().entity(clientPageData).now();
			log.info("saved");
		}

		// TODO
		// Check for events on new pages immediately

		return newPages;
	}

	@ApiMethod(name = "clientdata.ignorePage", httpMethod = "post")
	public FqlPage ignorePage(HttpServletRequest req, @Named("clientpageid") Long clientPageId, FqlPage jsonPage) {
		if (!(isPageAdmin(req, clientPageId) || isAppAdmin(req)))
			return null;
		// TODO return an error

		ClientPageData clientPageData = getClientPageData(clientPageId);

		FqlPage newPage;
		if (jsonPage.getName() == "" || jsonPage.getPageUrl() == "") {
			newPage = getPageFromId(jsonPage.getPageId());
		} else {
			newPage = jsonPage;
		}

		clientPageData.removePage(newPage);

		ofy().save().entity(clientPageData).now();

		// TODO return an error, if appropriate
		return newPage;
	}

	public ClientPageDataResponse getAllClients(HttpServletRequest req) {
		if (!isAppAdmin(req))
			return null;
		// TODO return an error

		List<ClientPageData> clients = new ArrayList<ClientPageData>();
		clients = ofy().load().type(ClientPageData.class).list();

		return new ClientPageDataResponse(clients);
	}

	FqlPage getPageFromId(Long pageId) {
		// FQL call pieces
		String fqlcallstub = "https://graph.facebook.com/fql?q=";
		String fql = "SELECT page_id, name, page_url, location, about, phone FROM page WHERE page_id = " + pageId;
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

	private boolean isAppAdmin(HttpServletRequest req) {
		if (req.getCookies() == null) // TODO wait for google for fix the bug and remove
			return true;

		// TODO
		// SELECT application_id, developer_id, role FROM developer WHERE developer_id = 37302520

		// The client will know it's an admin and add the signed request to the cookie.
		// It will always request the clientPageData, so now is the best time to see if the
		// signedrequest says they're an admin and add them if they are.

		ClientCookieData c = new ClientCookieData(req);

		// For now it's just me!
		return (c.getUserId() != null ? c.getUserId() == 37302520 : false);

	}

	private boolean isPageAdmin(HttpServletRequest req, Long clientPageId) {
		if (req.getCookies() == null) { // wait for google to fix the bug them remove
			// System.out.println("still no cookies");
			return true;
		}
		ClientCookieData c = new ClientCookieData(req);
		ClientPageData cpd = getClientPageData(clientPageId);

		// Check the encrypted signed request
		// Add them to the admin list if possible
		if (c.getSignedRequest() != null && c.getSignedRequest().getPage() != null && c.getSignedRequest().getPage().isAdmin() == true
				&& c.getSignedRequest().getPage().getId().equals(Long.toString(clientPageId))) {
			if (c.getSignedRequest().getUserId() != null && cpd.addPageAdmin(Long.parseLong(c.getSignedRequest().getUserId()))) {
				ofy().clear();
				ofy().save().entity(cpd).now();
			}
			return true;
		}

		// Check the existing admin list
		if (isValidAccessTokenForUser(c.getAccessToken(), c.getUserId()))
			return cpd.getPageAdmins().contains(c.getUserId());

		return false;

	}

	private boolean isValidAccessTokenForUser(String accessToken, Long userId) {
		String json = "";
		try {
			URL url = new URL("https://graph.facebook.com/me?access_token=" + accessToken);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				json += line;
			}
			reader.close();

		} catch (MalformedURLException e) {
			// TODO error
		} catch (IOException e) {
			// TODO error
		}
		if (!json.equals("")) {
			Gson gson = new GsonBuilder().registerTypeAdapter(FqlEventVenue.class, new FqlEventVenueAdapter())
					.registerTypeAdapter(Date.class, new FqlEventDatesAdapter()).create();
			GraphUser user = gson.fromJson(json, GraphUser.class);

			if (user.getError() != null) {
				// error... maybe the access token has expired...
				return false;
			} else if (userId.equals(user.getId())) {
				return true;
			}
		}
		return false; // Shouldn't ever get here
	}

	class ClientCookieData {

		/**
		 * @return the signedRequest
		 */
		public SignedRequest getSignedRequest() {
			return signedRequest;
		}

		/**
		 * @return the userId
		 */
		public Long getUserId() {
			return userId;
		}

		/**
		 * @return the accessToken
		 */
		public String getAccessToken() {
			return accessToken;
		}

		private SignedRequest signedRequest;
		private Long userId;
		private String accessToken;

		public ClientCookieData(HttpServletRequest req) {

			if (req.getCookies() != null) {
				for (Cookie c : req.getCookies()) {
					System.out.println("cookies: " + c.getName());
					if (c.getName().equals("accessToken"))
						accessToken = c.getValue();

					if (c.getName().equals("userId"))
						userId = Long.parseLong(c.getValue());

					if (c.getName().equals("encryptedSignedRequest")) {
						SimpleStringCipher ssc = new SimpleStringCipher(Config.getAppSecret());
						try {
							String signedRequestFromCookie = ssc.decrypt(c.getValue());
							signedRequest = SignedRequest.parseSignedRequest(signedRequestFromCookie);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if ((signedRequest != null && (userId == null || accessToken == null))&&signedRequest.getUserId()!=null) {
						
						userId = Long.parseLong(signedRequest.getUserId());
						accessToken = signedRequest.getOauthToken();
					}
				}
			} else {
				System.out.println("no cookies!");
			}
		}
	}
}