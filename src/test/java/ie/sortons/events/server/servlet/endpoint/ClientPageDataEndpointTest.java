package ie.sortons.events.server.servlet.endpoint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.Config;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.gwtfbplus.server.SimpleStringCipher;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.Cookie;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

public class ClientPageDataEndpointTest {

	@Test
	public void testGetPageFromId() {

		ClientPageDataEndpoint cpde = new ClientPageDataEndpoint();

		SourcePage fbPage = cpde.getPageDetailsFromFacebook(Long.parseLong("176727859052209"));

		System.out.println(fbPage.getName());

	}

	// Failing when processing lists of page due to NPE on the odd one.

	// Removing pages doesn't currently work

	
	// This test is waiting for the GwtFb+ SignedRequest signature checking to be implemented, which will then simplify the cookie we need to use
	@Ignore
	@Test
	public void testIsPageAdmin() throws Exception {

		// Get ClientPageData object from src/test/resources
		String resource = "/UCDEventsClientPageData.json";
		assertNotNull("Test file missing: " + resource, getClass().getResource(resource));
		URL url = getClass().getResource(resource);
		Path resPath = Paths.get(url.toURI());
		String json = new String(Files.readAllBytes(resPath), "UTF8");
		Gson gson = new Gson();
		ClientPageData clientPageData = gson.fromJson(json, ClientPageData.class);

		ClientPageDataEndpoint clientPageDataEndpoint = new ClientPageDataEndpoint();

		Cookie userId = new Cookie("userId", "37302520");

		// Get SignedRequest object from src/test/resources
		String srResource = "/SignedRequest.json";
		assertNotNull("Test file missing: " + srResource, getClass().getResource(srResource));
		url = getClass().getResource(srResource);
		resPath = Paths.get(url.toURI());
		json = new String(Files.readAllBytes(resPath), "UTF8");

		Cookie signedRequest = new Cookie("signedRequest", json);

		SimpleStringCipher ssc = new SimpleStringCipher(Config.getAppSecret());
		String encryptedSignedRequest = ssc.encrypt(json);
		Cookie encryptedSignedRequestCookie = new Cookie("encryptedSignedRequest", encryptedSignedRequest);

		Cookie[] cookies1 = { userId, signedRequest, encryptedSignedRequestCookie };

		assertTrue(clientPageDataEndpoint.isPageAdmin(cookies1, clientPageData));

		Cookie c21 = new Cookie("userId", "3722302520");

		Cookie[] cookies2 = { c21 };

		assertFalse(clientPageDataEndpoint.isPageAdmin(cookies2, clientPageData));

		// Admins get added to the admin list every time they visit.

	}

}
