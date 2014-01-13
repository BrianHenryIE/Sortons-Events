package ie.sortons.events.server.servlet.endpoint;

import ie.sortons.events.shared.FbPage;

import org.junit.Test;

public class ClientPageDataEndpointTest {

	@Test
	public void testGetPageFromId() {
	
		ClientPageDataEndpoint cpde = new ClientPageDataEndpoint();
	
		FbPage fbPage = cpde.getPageFromId("176727859052209");
		
		System.out.println(fbPage.getName());
	
	}
}
