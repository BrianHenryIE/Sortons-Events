package ie.sortons.events.server.servlet;

import static com.googlecode.objectify.ObjectifyService.ofy;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.server.SignedRequest;
import ie.sortons.gwtfbplus.server.fql.FqlStream;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItem;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItemAttachment;
import ie.sortons.gwtfbplus.server.fql.FqlStream.FqlStreamItemAttachmentAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.ObjectifyService;

public class RecentPosts extends HttpServlet {

	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = 2418318532718973612L;

	static {
		ObjectifyService.register(ClientPageData.class);
	}

	// Adapter added because of differences in structure between when there is an attachment or not in stream items.
	private Gson gson = new GsonBuilder().registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();

	String fqlCallStub = "https://graph.facebook.com/fql?q=";
	String streamCallStub = "SELECT%20permalink,actor_id%2C%20post_id%2C%20created_time,message,attachment.media%20FROM%20stream%20WHERE%20source_id%20%3D%20"; 

	private String fqlCall(String id){

		return  fqlCallStub + streamCallStub + id +"%20AND%20actor_id=source_id%20AND%20type!=247%20AND%20created_time%20%3E%20" + ((new DateTime().getMillis() / 1000) - 604800) + "&access_token="+Config.getAppAccessToken();

	}

	private String embedPost(String permalink) {
		return "<div class=\"post-container\"><div class=\"fb-post\" data-href=\""+permalink+"\"></div></div>";
	}


	private String clientPageId;
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( request.getParameter("client") != null ) 
			clientPageId = request.getParameter("client");
		else
			clientPageId = "176727859052209"; ;

			doCommonOutput(request, response);

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		SignedRequest signedRequest = SignedRequest.parseSignedRequest(request.getParameter("signed_request"));
		clientPageId = signedRequest.getPage().getId();

		doCommonOutput(request, response);
	}

	private void doCommonOutput(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 176727859052209

		ClientPageData clientPageData = ofy().load().type(ClientPageData.class).id(clientPageId.trim()).now();

		String ids = Joiner.on(",").join(clientPageData.getIncludedPageIds());

		// String ids = "176727859052209,180585223095,144085445617928,195462027143869,153597848050326,327354357259,140345306086891,142002899168824,261291960566266,427072610713452,210398799111123,509676619068377,439340592846537";

		List<String> fqlCalls = new ArrayList<>();
		for(String id : ids.split(","))
			fqlCalls.add(fqlCall(id));

		List<String> jsons = asyncFqlCall(fqlCalls);

		PrintWriter out = response.getWriter();

		String startHtml = "<html>\n" +
				"<head>\n" +
				"<meta charset=\"utf-8\" />\n" +
				"<style type=\"text/css\">\n" +
				"  body { margin :0; padding: 0; text-align: center; }\n" +
				"  .post-container { padding: 10px 0 10px; margin-left: auto; margin-right: auto; }\n" +
				"</style>\n" +
				"</head>\n" +
				"<body style=\"overflow: hidden\">\n";

		String fbRoot = "<div id=\"fb-root\"></div>\n" +
				"<script>\n" +
				"  window.fbAsyncInit = function() {\n" +
				"    // init the FB JS SDK\n" +
				"    FB.init({\n" +
				"      appId      : 'YOUR_APP_ID',                        // App ID from the app dashboard\n" +
				"      status     : true,                                 // Check Facebook Login status\n" +
				"      xfbml      : true                                  // Look for social plugins on the page\n" +
				"    });\n" +
				"  };\n" +
				"  // Load the SDK asynchronously\n" +
				"  (function(){\n" +
				"     // If we've already installed the SDK, we're done\n" +
				"     if (document.getElementById('facebook-jssdk')) {return;}\n" +
				"     // Get the first script element, which we'll use to find the parent node\n" +
				"     var firstScriptElement = document.getElementsByTagName('script')[0];\n" +
				"     // Create a new script element and set its id\n" +
				"     var facebookJS = document.createElement('script');\n" + 
				"     facebookJS.id = 'facebook-jssdk';\n" +
				"     // Set the new script's source to the source of the Facebook JS SDK\n" +
				"     facebookJS.src = '//connect.facebook.net/en_US/all.js';\n" +
				"     // Insert the Facebook JS SDK into the DOM\n" +
				"     firstScriptElement.parentNode.insertBefore(facebookJS, firstScriptElement);\n" +
				"   }());\n" +
				"</script>\n";

		String embedJs = "<script>(function(d, s, id) {\n" +
				"  var js, fjs = d.getElementsByTagName(s)[0];\n" +
				"  if (d.getElementById(id)) return;\n" +
				"  js = d.createElement(s); js.id = id;\n" +
				"  js.src = \"//connect.facebook.net/en_GB/all.js#xfbml=1&appId="+Config.getAppID()+"\";\n" +
				"  fjs.parentNode.insertBefore(js, fjs);\n" +
				"}(document, 'script', 'facebook-jssdk'));</script>\n";

		
		String resizeCanvas = "	<script>\n" +
				"	function canvasSize() {\n" +
				"		FB.Canvas.setSize();\n" +
				"     console.log('resizing')" +
				"	}\n" +
				"   (function() {\n" +
				"	setTimeout(canvasSize, 1000)\n" +
				"	setTimeout(canvasSize, 3000)\n" +
				"	setTimeout(canvasSize, 6000)\n" +
				"	setTimeout(canvasSize, 10000)\n" +
				"	setTimeout(canvasSize, 15000)\n" +
				"   })();\n" +
				"	</script>\n";
		
		out.print(startHtml);
		out.print(resizeCanvas);
		out.print(fbRoot);
		out.print(embedJs);

		Map<Integer, FqlStreamItem> feed = new TreeMap<Integer, FqlStreamItem>();

		for (String json : jsons) {
			FqlStream fqlStream = gson.fromJson(json, FqlStream.class);
			if(fqlStream.getData().length>0)
				for(FqlStreamItem item : fqlStream.getData())
					feed.put(item.getCreatedTime()*-1, item);
		}


		int count = 0;
		String lastPoster = "";
		for(FqlStreamItem item : feed.values())
			if ( !item.getActorId().equals(lastPoster) 
					&& count < 30 
					&& !item.getMessage().contains("facebook.com/events") ) {
				out.print(embedPost(item.getPermalink())+"\n");
				lastPoster = item.getActorId();
				count++;
			}
		
		//		&& (item.getAttachment() != null ? !item.getAttachment().getMedia()[0].getHref().contains("facebook.com/events") : true)

	}

	// TODO get out of here
	private List<String> asyncFqlCall(List<String> fqlCalls) {

		List<String> json = new ArrayList<String>();

		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

		List<Future<HTTPResponse>> asyncResponses = new ArrayList<Future<HTTPResponse>>();

		for (String fql : fqlCalls) {
			
			try {
				URL graphcall = new URL(fql);

				Future<HTTPResponse> responseFuture = fetcher.fetchAsync(graphcall);
				asyncResponses.add(responseFuture);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		for (Future<HTTPResponse> future : asyncResponses) {
			try {
				// response = future.get();
				HTTPResponse response = future.get();

				json.add(new String(response.getContent()));

			} catch (InterruptedException e) {
				System.out.println("InterruptedException: " + e);
			} catch (ExecutionException e) {
				System.out.println("ExecutionException: " + e);
			}
		}

		return json;
	}
}
