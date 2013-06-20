package ie.sortons.events.server.servlet;


import ie.sortons.events.server.facebook.gson.SignedRequest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LandingPageServlet extends HttpServlet {
	

	private PrintWriter out;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		System.out.println("Servlet execution... GET");
	            
		out = response.getWriter();
		
		// Write out Head 
		out.print("<!doctype html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		out.print("<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n\n");
		out.print("<script src=\"http://connect.facebook.net/en_US/all.js\"></script>\n\n");
		out.write("<script type=\"text/javascript\" language=\"javascript\" src=\"../friends__events/friends__events.nocache.js\"></script>\n\n");
		out.write("</head>\n");

		// Body: special div FB and GWT
		out.print("<body style=\"width: 750px;\">\n\n");

		// Body: special div FB and GWT
		out.print("<div style=\"width: 500px; border: 1px solid black;\">\n\n");
		
		out.print("</div>\n\n");
		
		out.print("<div id=\"fb-root\" style=\"width: 750px;\" ></div>\n\n" +
				"<div id=\"gwt\" style=\"width: 750px;\"></div>\n\n");

		// End
		out.print("</body>\n</html>");
		out.flush();
	}
	
	
	
	// Inside Facebook, it will always be POST
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		
		System.out.println("Servlet execution... POST");
	
		
		SignedRequest signedrequest = SignedRequest.parseSignedRequest(request.getParameter("signed_request"));
		
		System.out.println("gson'd signed_request: " + signedrequest.toJsonString());
		
		
		String referrer = request.getHeader("referer"); 
		System.out.println("Referrer: " + referrer);
		
		out = response.getWriter();
		
		// Write out Head 
		out.print("<!doctype html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" style=\"overflow: hidden\">\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n\n");
				
		out.print("<script src=\"http://connect.facebook.net/en_US/all.js\"></script>\n\n");
		out.write("<script type=\"text/javascript\" language=\"javascript\" src=\"../friends__events/friends__events.nocache.js\"></script>\n\n");
		out.write("</head>\n");
		
		// Body: special div FB and GWT
		out.print("<body style=\"overflow=hidden;\">\n\n");
		out.print("<div id=\"fb-root\"></div>\n\n");
		out.print("<div id=\"gwt\"></div>\n\n");
		
		// End
		out.print("</body>\n</html>");
		out.flush();
	}

}
		