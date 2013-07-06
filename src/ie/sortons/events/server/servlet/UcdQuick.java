package ie.sortons.events.server.servlet;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;

import ie.sortons.events.domain.FbEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@SuppressWarnings("serial")
public class UcdQuick extends HttpServlet {
	
	
	private PrintWriter out;
	
	// TODO
	// What about when an event has been deleted?
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		System.out.println("Servlet execution... GET");
		
		ObjectifyService.register(FbEvent.class);

	            
		out = response.getWriter();
		
		// Write out Head 
		out.print("<!doctype html>\n" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
				"<head>\n" +
				"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n\n");

		out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"../theme.css\">\n\n");


		
		out.print("</head>\n\n" +
				"<body style=\"overflow:hidden; width: 690px; margin:0; \">\n\n");
		
		// This is needed to call the js resize when inside the fb canvas 
		out.print("<div id=\"fb-root\"></div>\n"+
				"<script>\n"+
				"  window.fbAsyncInit = function() {\n"+
				"    // init the FB JS SDK\n"+
				"    FB.init({\n"+
				"      appId      : '470244209665073', // App ID from the App Dashboard\n"+
				"      channelUrl : '//ucdfbevents.appspot.com/channel.html', // Channel File for x-domain communication\n"+
				"      status     : true, // check the login status upon init?\n"+
				"      cookie     : true, // set sessions cookies to allow your server to access the session?\n"+
				"      xfbml      : true  // parse XFBML tags on this page?\n"+
				"    });\n"+
				"    \n"+
				"    // Additional initialization code such as adding Event Listeners goes here\n"+
				"    \n"+
				"  };\n"+
				"  \n"+
				"  // Load the SDK's source Asynchronously\n"+
				"  // Note that the debug version is being actively developed and might\n"+ 
				"  // contain some type checks that are overly strict. \n"+
				"  // Please report such bugs using the bugs tool.\n"+
				"  (function(d, debug){\n"+
				"     var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];\n"+
				"     if (d.getElementById(id)) {return;}\n"+
				"     js = d.createElement('script'); js.id = id; js.async = true;\n"+
				"     js.src = \"//connect.facebook.net/en_US/all\" + (debug ? \"/debug\" : \"\") + \".js\";\n"+
				"     ref.parentNode.insertBefore(js, ref);\n"+
				"   }(document, /*debug*/ false));\n"+
				"</script>\n\n");
		

		out.print("<div class=\"container\">\n\n");
		
		
		
		
		Date now = new Date();
		
		
		out.print("<table class=\"etable\">\n");
				
		List<FbEvent> dsEvents = ofy().load().type(FbEvent.class).filter("start_time >", getHoursAgoOrToday(12)).list();
		for(FbEvent dse : dsEvents){
			
			if((dse.getEnd_time()==null)||(dse.getEnd_time().after(now))){
			out.print(eventTemplate(dse));
			}
		}
		out.print("</table>\n\n");
		
		out.print("</div>\n\n");
		
		// Once everything is printed, resize the FB Canvas iframe so it fits everything
		out.print("<script>window.onload=function(){ FB.Canvas.setSize(); };</script>\n\n");
		
		out.print("</body>\n</html>");
		out.flush();
	}
	
	private Date getHoursAgoOrToday(int hours) {
		
		
		Calendar calvar = Calendar.getInstance();

        int today = calvar.get(Calendar.DAY_OF_YEAR);
        
        // Subtract the specified number of hours 
        calvar.add(Calendar.HOUR_OF_DAY, -hours);

        // Keep subtracting hours until we get to last night
        while (calvar.get(Calendar.DAY_OF_YEAR) == today){
        	calvar.add(Calendar.HOUR_OF_DAY, -1);
        }

        Date ago = calvar.getTime();
        		
		return ago;
	}
	
	private String eventTemplate(FbEvent dse) {
		
		String html = "";
		
		html += "	<tr class=\"etr\">\n" +
				"		<td class=\"picture\">\n" +
				"			<div class=\"picture\">\n" +
				"				<a target=\"_top\" href=\"http://www.facebook.com/events/" + dse.getEid() + "\">\n" +
				"					<img class =\"pic_square\" src=\"" + dse.getPic_square() + "\"/>\n" +
				"				</a>\n";
		html += "			</div>\n" +
				"		</td>\n" +
				"		<td>\n";
		html += "			<div class=\"name\">\n";
		html += "				<a target=\"_top\" href=\"http://www.facebook.com/events/" + dse.getEid() + "\">" + dse.getName() + "</a>\n";
		html += "			</div>\n" +
				"			<div class=\"time\">\n				";
		
		SimpleDateFormat dformat;
		// Thursday, 14 February, 2013, at 8:00

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dse.getStart_time());
		
		if(calendar.get(Calendar.HOUR_OF_DAY)==0){
			dformat = new SimpleDateFormat("EEEE, dd MMMM, yyyy");
		} else {
			dformat = new SimpleDateFormat("EEEE, dd MMMM, yyyy, 'at' HH:mm");
		}
		
		html += dformat.format(dse.getStart_time());
		html += "\n			</div>\n" +
				"			<div class=\"location\">\n				";
		html += ((dse.getLocation()!=null) ? dse.getLocation() : "");
		html += "\n			</div>";
		html += "		</td>\n	</tr>\n";
		
		
		return html;
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		System.out.println("Servlet execution... POST");

		
		doGet(request, response);
		
	}
}
		