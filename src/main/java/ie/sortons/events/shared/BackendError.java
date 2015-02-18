package ie.sortons.events.shared;

import java.util.ArrayList;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

/*
 500 Internal Server Error

 "error" : {
 "message" : "<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n<title>Error 500 INTERNAL_SERVER_ERROR</title>\n</head>\n<body><h2>HTTP ERROR 500</h2>\n<p>Problem accessing /_ah/spi/ie.sortons.events.server.servlet.endpoint.ClientPageDataEndpoint.addPage. Reason:\n<pre>    INTERNAL_SERVER_ERROR</pre></p><h3>Caused by:</h3><pre>java.lang.NullPointerException\n\tat ie.sortons.events.shared.FbPage.hashCode(FbPage.java:76)\n\tat java.lang.Object.toString(Object.java:237)\n\tat java.lang.String.valueOf(String.java:2854)\n\tat java.util.Arrays.toString(Arrays.java:3565)\n\tat com.google.api.server.spi.SystemService.invokeServiceMethod(SystemService.java:351)\n\tat com.google.api.server.spi.SystemServiceServlet.execute(SystemServiceServlet.java:124)\n\tat com.google.api.server.spi.SystemServiceServlet.doPost(SystemServiceServlet.java:82)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:637)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:717)\n\tat org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1166)\n\tat com.google.appengine.api.socket.dev.DevSocketFilter.doFilter(DevSocketFilter.java:74)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.ResponseRewriterFilter.doFilter(ResponseRewriterFilter.java:123)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.HeaderVerificationFilter.doFilter(HeaderVerificationFilter.java:34)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.api.blobstore.dev.ServeBlobFilter.doFilter(ServeBlobFilter.java:63)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.apphosting.utils.servlet.TransactionCleanupFilter.doFilter(TransactionCleanupFilter.java:43)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.StaticFileFilter.doFilter(StaticFileFilter.java:125)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doDirectRequest(DevAppServerModulesFilter.java:368)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doDirectModuleRequest(DevAppServerModulesFilter.java:351)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doFilter(DevAppServerModulesFilter.java:116)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:388)\n\tat org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)\n\tat org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)\n\tat org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:765)\n\tat org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:418)\n\tat com.google.appengine.tools.development.DevAppEngineWebAppContext.handle(DevAppEngineWebAppContext.java:97)\n\tat org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)\n\tat com.google.appengine.tools.development.JettyContainerService$ApiProxyHandler.handle(JettyContainerService.java:485)\n\tat org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)\n\tat org.mortbay.jetty.Server.handle(Server.java:326)\n\tat org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:542)\n\tat org.mortbay.jetty.HttpConnection$RequestHandler.content(HttpConnection.java:938)\n\tat org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:755)\n\tat org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:218)\n\tat org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:404)\n\tat org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:409)\n\tat org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)\n</pre>\n<hr /><i><small>Powered by Jetty://</small></i><br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n\n</body>\n</html>\n",
 "code" : 503,
 "errors" : [ {
 "domain" : "global",
 "reason" : "backendError",
 "message" : "<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>\n<title>Error 500 INTERNAL_SERVER_ERROR</title>\n</head>\n<body><h2>HTTP ERROR 500</h2>\n<p>Problem accessing /_ah/spi/ie.sortons.events.server.servlet.endpoint.ClientPageDataEndpoint.addPage. Reason:\n<pre>    INTERNAL_SERVER_ERROR</pre></p><h3>Caused by:</h3><pre>java.lang.NullPointerException\n\tat ie.sortons.events.shared.FbPage.hashCode(FbPage.java:76)\n\tat java.lang.Object.toString(Object.java:237)\n\tat java.lang.String.valueOf(String.java:2854)\n\tat java.util.Arrays.toString(Arrays.java:3565)\n\tat com.google.api.server.spi.SystemService.invokeServiceMethod(SystemService.java:351)\n\tat com.google.api.server.spi.SystemServiceServlet.execute(SystemServiceServlet.java:124)\n\tat com.google.api.server.spi.SystemServiceServlet.doPost(SystemServiceServlet.java:82)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:637)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:717)\n\tat org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1166)\n\tat com.google.appengine.api.socket.dev.DevSocketFilter.doFilter(DevSocketFilter.java:74)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.ResponseRewriterFilter.doFilter(ResponseRewriterFilter.java:123)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.HeaderVerificationFilter.doFilter(HeaderVerificationFilter.java:34)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.api.blobstore.dev.ServeBlobFilter.doFilter(ServeBlobFilter.java:63)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.apphosting.utils.servlet.TransactionCleanupFilter.doFilter(TransactionCleanupFilter.java:43)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.StaticFileFilter.doFilter(StaticFileFilter.java:125)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doDirectRequest(DevAppServerModulesFilter.java:368)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doDirectModuleRequest(DevAppServerModulesFilter.java:351)\n\tat com.google.appengine.tools.development.DevAppServerModulesFilter.doFilter(DevAppServerModulesFilter.java:116)\n\tat org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1157)\n\tat org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:388)\n\tat org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)\n\tat org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)\n\tat org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:765)\n\tat org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:418)\n\tat com.google.appengine.tools.development.DevAppEngineWebAppContext.handle(DevAppEngineWebAppContext.java:97)\n\tat org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)\n\tat com.google.appengine.tools.development.JettyContainerService$ApiProxyHandler.handle(JettyContainerService.java:485)\n\tat org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)\n\tat org.mortbay.jetty.Server.handle(Server.java:326)\n\tat org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:542)\n\tat org.mortbay.jetty.HttpConnection$RequestHandler.content(HttpConnection.java:938)\n\tat org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:755)\n\tat org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:218)\n\tat org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:404)\n\tat org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:409)\n\tat org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)\n</pre>\n<hr /><i><small>Powered by Jetty://</small></i><br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n<br/>                                                \n\n</body>\n</html>\n"
 } ]
 }	

 */


public class BackendError implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.BackendError";

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the errors
	 */
	public ArrayList<BackendErrors> getErrors() {
		return errors;
	}

	public String message;
	public String code;

	public ArrayList<BackendErrors> errors;

	public BackendError() {
	}

}
