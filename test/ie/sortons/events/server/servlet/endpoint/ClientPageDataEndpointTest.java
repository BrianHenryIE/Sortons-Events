package ie.sortons.events.server.servlet.endpoint;

import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import org.junit.Test;

public class ClientPageDataEndpointTest {

	@Test
	public void testGetPageFromId() {
	
		ClientPageDataEndpoint cpde = new ClientPageDataEndpoint();
	
		FqlPage fbPage = cpde.getPageFromId(Long.parseLong("176727859052209"));
		
		System.out.println(fbPage.getName());
	
	}
}
