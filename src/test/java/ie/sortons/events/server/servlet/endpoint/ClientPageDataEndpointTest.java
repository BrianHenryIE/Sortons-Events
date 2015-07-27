package ie.sortons.events.server.servlet.endpoint;

import ie.sortons.events.shared.SourcePage;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientPageDataEndpointTest {

	@Test
	public void testGetPageFromId() {
	
		ClientPageDataEndpoint cpde = new ClientPageDataEndpoint();
	
		SourcePage fbPage = cpde.getPageDetailsFromFacebook(Long.parseLong("176727859052209"));
		
		System.out.println(fbPage.getName());
	
	}
	
	// Failing when processing lists of page due to NPE on the odd one.
	
	
	// Removing pages doesn't currently work
	
	
}
