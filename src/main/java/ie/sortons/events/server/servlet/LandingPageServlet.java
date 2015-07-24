package ie.sortons.events.server.servlet;

import ie.sortons.gwtfbplus.server.SimpleStringCipher;
import ie.sortons.gwtfbplus.shared.domain.SignedRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public abstract class LandingPageServlet extends HttpServlet {

	static Logger log;
	
	private String gwtEntryPoint;

	private String appId;

	private String signedRequestData = "";
	private String style = "";

	private String appSecret;

	private String httpOrS(HttpServletRequest request) {
		return request.getRequestURL().toString().replaceAll("(https?://).*", "$1");
	}

	protected LandingPageServlet(String gwtEntryPoint, String appId, String appSecret) {
		this.gwtEntryPoint = gwtEntryPoint;
		this.appId = appId;
		this.appSecret = appSecret;
	}

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	// So the subclass can write into the head or body.
	protected String head = "";
	protected String body = "";

	protected SignedRequest signedRequest;

	/**
	 * Method which runs on each request, giving the extending class a chance to read and write to the request and
	 * response Also be aware of String head and String body which add strings into the head and body!
	 */
	protected void readWriteRequest() {
	};

	private Gson gson = new Gson();

	// Inside Facebook, it will always be POST
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// encrypt and add the signed request cookies
		if (request.getParameter("signed_request") != null) {

			log.info("signed request present");
			
			signedRequest = SignedRequest.parseSignedRequest(request.getParameter("signed_request"));
			
			String encryptedSignedRequest = null;
			SimpleStringCipher ssc = new SimpleStringCipher(appSecret);
			try {
				encryptedSignedRequest = ssc.encrypt(request.getParameter("signed_request"));
				Cookie encryptedSignedRequestCookie = new Cookie("encryptedSignedRequest", encryptedSignedRequest);
				encryptedSignedRequestCookie.setPath("/");
				response.addCookie(encryptedSignedRequestCookie);
				log.info("encrypted sr cookie added (blob)");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (signedRequest.getOauthToken() != null) {
				Cookie accessTokenCookie = new Cookie("accessToken", signedRequest.getOauthToken());
				// Store the cookie for as long as the access token lasts
				accessTokenCookie.setMaxAge((int) (Integer.parseInt(signedRequest.getExpires()) - (new Date().getTime() / 1000)));
				accessTokenCookie.setPath("/");
				response.addCookie(accessTokenCookie);
				log.info("accessToken cookie added: "+signedRequest.getOauthToken());
			}

			if (signedRequest.getUserId() != null) {
				Cookie userIdCookie = new Cookie("userId", signedRequest.getUserId());
				userIdCookie.setPath("/");
				userIdCookie.setMaxAge((int) (Integer.parseInt(signedRequest.getExpires()) - (new Date().getTime() / 1000)));
				response.addCookie(userIdCookie);
				log.info("userId cookie added: " + signedRequest.getUserId());
			}

			signedRequestData = "  <script id=\"signedRequest\">\n" + "    var _sr_data = " + gson.toJson(signedRequest) + "\n  </script>\n\n";
		
		}
		// This isn't needed/desirable outside the fb canvas
		style += " overflow: hidden;";

		doGet(request, response);

		// TODO
		// The "Go to application" link in the admin section of a Page links to
		// http://apps.facebook.com/sortonsdev/?fb_page_id=176727859052209
	}

	// TODO
	// If it's a GET, use the referrer to find the Facebook page that linked it.
	// Or canvas redirect to apps.facebook.com/AppID

	
	/* 
	 * If it's a GET, i.e. outside the FB Canvas frame, page_id can be passed in for testing. 
	 * 
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// TODO
		// out.write should be its own thing, not post calling get
		
		System.out.println("Servlet execution... " + request.getMethod());

		if(request.getParameter("page_id")!=null){
						 
			signedRequestData = "  <script id=\"signedRequest\">\n" + "    var _sr_data = {\"page\": { \"id\": \""+request.getParameter("page_id")+"\", \"admin\": false}, \"user_id\": \""+request.getParameter("user_id")+"\" }\n  </script>\n\n";

		}
		
		// If the app has been added as a Page tab, it redirects to the canvas with a URL:
		// http://apps.facebook.com/sortonsdev/?tabs_added[356718097671739]=1#_=_
		// or if it's added to multiple pages:
		// http://apps.facebook.com/sortonsdev/?tabs_added[367864846557326]=1&tabs_added[356718097671739]=1#_=_
		// Regex for extracting the url from wall posts

		// TODO check get parameters :
		// http://sortonsevents.appspot.com/recentposts/?tabs_added%5B218374251693428%5D=1#_=_
		
		if (request.getHeader("referer") != null && request.getHeader("referer").contains("tabs_added")) {

			// If the Canvas load was a redirect from adding the page tab
			// redirect to the page that added it, at the app's new tab
			// TODO Test this... it didn't seem to work once, though the URL worked when tested in isolation
			// TODO maybe just // instead of a method there.
			// TODO This will only work when adding to one page at a time!
			String redirectTo = httpOrS(request) + "www.facebook.com/" + request.getHeader("referer").replaceAll(".*tabs_added%5B(\\d+)%5D.*", "$1")
					+ "?sk=app_" + appId;

			PrintWriter out = response.getWriter();
			out.print("<script>window.top.location.href = \"" + redirectTo + "\";</script>");

			// TODO
			// Put some text here in case the redirect doesn't work (why wouldn't it?)
			// If multiple pages have had the app added, show a list rather than redirect.
			// Do it with GWT? The app will probably be cached already

		} else {

			this.request = request;
			this.response = response;
			readWriteRequest();

			PrintWriter out = response.getWriter();

			// Write out head
			out.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n"
					+ // As specified for Bing Maps API
					"<html xmlns=\"http://www.w3.org/1999/xhtml\""
					+ "style=\""+style+" margin:0;\""
					+ "> \n\n"
					+ "<head> \n\n"
					+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/> \n\n"
					+ signedRequestData
					+ // If available, print the Signed Request
					"  <meta name=\"gwt:property\" content=\""
					+ request.getLocale()
					+ "\">\n\n"
					+ "  <script type=\"text/javascript\" language=\"javascript\" src=\""
					+ gwtEntryPoint
					+ "\"></script> \n\n"
					+ "  <script src=\"//connect.facebook.net/en_US/all.js\"></script> \n\n" + // Facebook API
					head + "</head> \n\n");

			// Write out body
			out.print("<body" + style + "> \n\n" + "  <div id='fb-root'></div> \n\n" + // required for Facebook API
					"  <div id=\"gwt\"></div> \n\n" + // root of document for GWT
					body + "</body> \n\n");

			out.print("</html>");
			out.flush();

		}

		signedRequestData = "";
		style = "";
	}
}
