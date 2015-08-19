package ie.sortons.events.server.servlet.endpoint;

import static com.googlecode.objectify.ObjectifyService.ofy;

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

import ie.sortons.events.server.servlet.SimpleStringCipher;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.PageList;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.events.shared.dto.PagesListResponse;
import ie.sortons.gwtfbplus.shared.domain.FbResponse;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventDatesAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenue;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent.FqlEventVenueAdapter;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphUser;

/**
 * @author brianhenry
 * 
 */
@Api(name = "clientdata", version = "v1", auth = @ApiAuth(allowCookieAuth = AnnotationBoolean.TRUE))
public class ClientPageDataEndpoint {

	// TODO Should everything be returned in a generic object with a data and an
	// error property?

	private static final Logger log = Logger.getLogger(ClientPageDataEndpoint.class.getName());

	static {
		ObjectifyService.register(ClientPageData.class);
		ObjectifyService.register(SourcePage.class);
	}

	/**
	 * @param req
	 * @param clientPageId
	 * @return
	 */
	public ClientPageData getClientPageData(HttpServletRequest req, @Named("clientid") Long clientPageId) {
		
		ClientPageData clientPageData = getClientPageData(clientPageId);

		log.info("_ah/api/clientdata/v1/clientpagedata/ :" + clientPageData.getName());

		return clientPageData;
	}

	private ClientPageData getClientPageData(Long clientId) {

		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientId).now();

		// When the customer has just signed up
		// TODO check they're a page admin else return: no such client
		if (clientPageData == null) {

			log.info("clientPageData == null");

			SourcePage clientPageDetails = getPageDetailsFromFacebook(clientId);

			log.info("Added to new page: " + clientPageDetails.getName() + " " + clientPageDetails.getPageUrl() + " "
					+ clientId);

			// TODO
			// This fails with a NPE if the page isn't public, i.e. test users
			// and unpublished pages

			// Add new entry
			ClientPageData newClient = new ClientPageData(clientPageDetails);

			ofy().save().entity(newClient).now();

			return newClient;

		} else {

			List<SourcePage> includedPages = ofy().load().type(SourcePage.class).filter("clientId", clientId).list();
			clientPageData.setIncludedPages(includedPages);

			return clientPageData;
		}
	}

	// Not sure if I can take a String as a POST parameter here (Endpoints
	// doesn't elsewhere)
	@ApiMethod(name = "clientdata.addPage", httpMethod = "post")
	public SourcePage addPage(HttpServletRequest req, @Named("clientpageid") Long clientPageId, SourcePage jsonPage) {
		log.info("addPage pre auth check");
		ClientPageData clientPageData = getClientPageData(clientPageId);
		if (req.getCookies()==null || !(isPageAdmin(req.getCookies(), clientPageData) || isAppAdmin(req.getCookies())))
			return null;
		// TODO return an error: permission denied

		log.info("addPage: " + jsonPage.getName() + " " + jsonPage.getPageId());

		SourcePage newPage = getSourcePageFromId(clientPageId, jsonPage.getPageId());

		SourcePage fromDs = savePage(newPage);

		return fromDs;
	}

	/**
	 * Due to earlier problems, this method saves the page and then queries the
	 * datastore for it. I think it's overkill now and the old problems may have
	 * been related to resaving ClientPageData with its 1MB list over itself
	 * before everything had settled.
	 * 
	 * Eventually anywhere that calls this should just have the ofy() call there
	 * I'm not even convinced a ClientPageData object is needed, maybe just a SourcePage
	 * with no parent would be adequate
	 * 
	 * @param newPage
	 * @return
	 */
	SourcePage savePage(SourcePage newPage) {

		ofy().save().entity(newPage).now();

		SourcePage fromDs = ofy().load().type(SourcePage.class).id(newPage.getId()).now();

		return fromDs;
	}

	
	@ApiMethod(name = "clientdata.addPagesList", httpMethod = "post")
	public PagesListResponse addPagesList(HttpServletRequest req, @Named("clientpageid") Long clientPageId,
			PageList pagesList) {
		
		ClientPageData clientPageData = getClientPageData(clientPageId);
		if (!(isPageAdmin(req.getCookies(), clientPageData) || isAppAdmin(req.getCookies())))
			return null;
		// TODO return an error

		
		System.out.println("cpdendpoint: " + pagesList);
		log.info("addPagesList: " + pagesList);

		PagesListResponse newPages = new PagesListResponse();

		List<SourcePage> dsPages = new ArrayList<SourcePage>();
		List<String> failed = new ArrayList<String>();

		// TODO... for lists, this will be slow (the many calls to fb)
		for (String pageId : pagesList.getList()) {
			SourcePage newPage = getSourcePageFromId(clientPageId, Long.parseLong(pageId));
			if (newPage == null)
				failed.add(pageId);
			else {
				SourcePage added = savePage(newPage);
				if (added != null)
					dsPages.add(added);
				else
					failed.add(pageId);
			}

		}

		return newPages;
	}

	// TODO This doesn't work anymore
	@ApiMethod(name = "clientdata.removePage", httpMethod = "post")
	public SourcePage removePage(HttpServletRequest req, @Named("clientpageid") Long clientPageId, SourcePage jsonPage) {
		ClientPageData clientPageData = getClientPageData(clientPageId);

		if (req.getCookies()==null || !(isPageAdmin(req.getCookies(), clientPageData) || isAppAdmin(req.getCookies())))
			return null;
		// TODO return an error

		
		SourcePage newPage;
		if (jsonPage.getName() == "" || jsonPage.getPageUrl() == "") {
			newPage = getPageDetailsFromFacebook(jsonPage.getPageId());
		} else {
			newPage = jsonPage;
		}

		clientPageData.removePage(newPage);

		ofy().save().entity(clientPageData).now();

		// TODO return an error, if appropriate
		return newPage;
	}


	SourcePage getSourcePageFromId(Long clientPageId, Long pageId) {

		SourcePage newPage = getPageDetailsFromFacebook(pageId);
		if (newPage != null)
			newPage.setClientId(clientPageId);

		return newPage;
	}

	SourcePage getPageDetailsFromFacebook(Long pageId) {
		// FQL call pieces
		String fqlcallstub = "https://graph.facebook.com/fql?q=";
		String fql = "SELECT page_id, name, page_url, location, about, phone FROM page WHERE page_id = " + pageId;
		String access_token = Config.getAppAccessTokenServer();

		String json = "";

		try {

			String call = fqlcallstub + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + access_token;
			log.info(call);

			URL url = new URL(call);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				json += line;
			}
			reader.close();

			log.info(json);

		} catch (MalformedURLException e) {
			System.out.println("getPageFromId: catch (MalformedURLException e)");
			// ...
			return null;
		} catch (IOException e) {
			System.out.println("getPageFromId: catch (IOException e)");
			// ...
			return null;
		}

		log.info("Page details from fb: " + json);

		Gson gson = new Gson();
		// Convert the json string to java object
		Type fooType = new TypeToken<FbResponse<FqlPage>>() {
		}.getType();
		FbResponse<FqlPage> pages = gson.fromJson(json, fooType);

		if (pages.getError() == null && pages.getData() != null && pages.getData().size() > 0) {
			log.info("returning page details");
			return new SourcePage(pages.getData().get(0));
		} else {
			log.info("returning null");
			return null;
		}

		// TODO: return an error.
	}

	boolean isAppAdmin(Cookie[] cookies) {
		
		// TODO
		// SELECT application_id, developer_id, role FROM developer WHERE
		// developer_id = 37302520

		// The client will know it's an admin and add the signed request to the
		// cookie.
		// It will always request the clientPageData, so now is the best time to
		// see if the
		// signedrequest says they're an admin and add them if they are.

		AppCookieData c = new AppCookieData(cookies);

		// For now it's just me!

		log.info("c.getUserId() " + c.getUserId());

		boolean isAppAdmin = (c.getUserId() != null ? c.getUserId().equals(37302520l) : false);

		log.info("checking is app admin: " + isAppAdmin);

		return isAppAdmin;

	}

	boolean isPageAdmin(Cookie[] cookies, ClientPageData clientPageData) {
		if (cookies == null)
			return false;

		AppCookieData c = new AppCookieData(cookies);

		// Check the encrypted signed request
		// Add them to the admin list if possible
		if (c.getSignedRequest() != null && c.getSignedRequest().getPage() != null
				&& c.getSignedRequest().getPage().isAdmin() == true
				&& c.getSignedRequest().getPage().getId().equals(Long.toString(clientPageData.getClientPageId()))) {
			if (c.getSignedRequest().getUserId() != null
					&& clientPageData.addPageAdmin(Long.parseLong(c.getSignedRequest().getUserId()))) {

				ofy().save().entity(clientPageData).now();
			}
			return true;
		}

		// Check the existing admin list
		if (isValidAccessTokenForUser(c.getAccessToken(), c.getUserId()))
			return clientPageData.getPageAdmins().contains(c.getUserId());

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

	class AppCookieData {

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

		public AppCookieData(Cookie[] cookies) {

			if (cookies != null) {
				for (Cookie c : cookies) {

					log.info("Reading COOKIE: " + c.getName() + " " + c.getValue());

					System.out.println("cookies: " + c.getName());
					if (c.getName().equals("accessToken"))
						accessToken = c.getValue();

					if (c.getName().equals("userId"))
						userId = Long.parseLong(c.getValue());

					if (c.getName().equals("encryptedSignedRequest")) {
						SimpleStringCipher ssc = new SimpleStringCipher(Config.getAppSecret());
						try {
							String signedRequestFromCookie = ssc.decrypt(c.getValue());
							log.info(signedRequestFromCookie);
							signedRequest = SignedRequest.parseSignedRequest(signedRequestFromCookie);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if ((signedRequest != null && (userId == null || accessToken == null))
							&& signedRequest.getUserId() != null) {

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